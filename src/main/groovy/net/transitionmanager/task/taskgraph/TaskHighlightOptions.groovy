package net.transitionmanager.task.taskgraph

import com.tdsops.common.sql.SqlUtil
import groovy.transform.CompileStatic

@CompileStatic
class TaskHighlightOptions {

    /**
     * This method takes in the information returned from the database to construct a map for the Task Highlight
     * Options including: environments (of the assets associated to tasks), the different teams, the persons
     * assigned to the tasks and all the owners and smes.
     * @param tasks - the information obtained after querying the database for the highlight options.
     * @return
     */
    static Map getHighlightOptions(List<Map> tasks) {
        Map teams = [:]
        Map persons = [:]
        Map ownersAndSmes = [:]
        Map environments = [:]

        // Add the 'field' map to the collection if its id field is not null.
        Closure addIfNotNull = { Map field, Map collection ->
            if (field.id) {
                collection.put(field.id, field)
            }
        }

        /*
            Create a list with all the values in the map (sorted by name) and with the 'firstEntry' at the beginning.
            firstEntry is optional but it can't have a default value because, if only the first map is provided,
            it will try to map its keys as individual parameters and throw a compilation error.
         */
        Closure toList = { Map collection, Map firstEntry ->
            List<Map> values = collection.values().toList()
            List<Map> sortedValues = values.sort { a,  b ->
                (String) a.name <=> (String) b.name
            }
            if (firstEntry != null) {
                sortedValues = [firstEntry] + sortedValues
            }
            return sortedValues
        }

        // Iterate over all the tasks found for the event, putting together different data structures.
        for (Map taskInfo in tasks) {
            addIfNotNull((Map)taskInfo['team'], teams)
            addIfNotNull((Map)taskInfo['environment'], environments)
            addIfNotNull((Map)taskInfo['assignedTo'], persons)
            addIfNotNull((Map)taskInfo['appOwner'], ownersAndSmes)
            addIfNotNull((Map)taskInfo['sme'], ownersAndSmes)
            addIfNotNull((Map)taskInfo['sme2'], ownersAndSmes)
        }

        return [
                persons: toList(persons, [id: 0, name: 'Unassigned']),
                teams: toList(teams, [id: 'UNASSIGNED', name: 'Unassigned']),
                ownersAndSmes: toList(ownersAndSmes, null),
                environments: toList(environments, null)
        ]
    }

    /**
     * Convert a row from the database into a suitable Map for the front-end.
     *
     * @param row - a record from the database query
     * @return a Map with each column appropriately formatted.
     */
    static Map mapRowToHighlightMap(Map row) {
        return [
                environment: [
                        id: row['environment'],
                        name: row['environment']
                ],
                team: [
                        id: row['team_code'],
                        name: row['team_label']
                ],
                assignedTo: [
                        id: row['assigned_to'],
                        name: row['assigned_to_name']
                ],
                sme: [
                        id: row['sme'],
                        name: row['sme_name']
                ],
                sme2: [
                        id: row['sme2'],
                        name: row['sme2_name']

                ],
                appOwner: [
                        id: row['app_owner'],
                        name: row['owner_name']
                ]
        ]
    }


    /**
     * Put together the query for retrieving the info necessary for the task highlight options. The query is mostly
     * a static string, but if the 'viewUnpublished' flag hasn't been set, only published tasks should be considered.
     * @param viewUnpublished - whether or not unpublished tasks should be included.
     * @return the query to fetch the highlight options.
     */
    static String getHighlightOptionsQuery(Boolean viewUnpublished) {
        String viewUnpublishedClause = viewUnpublished ? '' : ' AND is_published = true'
        return  """SELECT
                    ae.environment as environment,
                    ro.role_type_code as team_code,
                    ro.description as team_label,
                    pa.person_id as assigned_to,
                    ${SqlUtil.personFullNameSql('pa')} AS assigned_to_name,
                    psme.person_id as sme,
                    ${SqlUtil.personFullNameSql('psme')} AS sme_name,
                    psme2.person_id as sme2,
                    ${SqlUtil.personFullNameSql('psme2')} AS sme2_name,
                    pao.person_id as app_owner,
                    ${SqlUtil.personFullNameSql('pao')} AS owner_name
                FROM
                     (
                         SELECT asset_entity_id, role, assigned_to_id
                         FROM asset_comment
                         WHERE comment_type = 'issue' AND move_event_id = :moveEventId ${viewUnpublishedClause}
                    ) ac
                LEFT OUTER JOIN
                    (
                        SELECT role_type_code, description
                        FROM role_type
                    ) ro ON (ro.role_type_code = ac.role)
                LEFT OUTER JOIN
                    (
                        SELECT first_name, middle_name, last_name, person_id
                        FROM person
                    ) pa ON (ac.assigned_to_id = pa.person_id)
                LEFT OUTER JOIN
                    (
                        SELECT sme_id, sme2_id, app_id
                        FROM application
                    ) app ON (app.app_id = ac.asset_entity_id)
                LEFT OUTER JOIN
                    (
                        SELECT asset_entity_id, environment, app_owner_id
                        FROM asset_entity
                    ) ae ON (ae.asset_entity_id = ac.asset_entity_id)
                    LEFT OUTER JOIN
                    (
                        SELECT first_name, middle_name, last_name, person_id
                        FROM person
                    ) psme ON (app.sme_id = psme.person_id)
                LEFT OUTER JOIN
                    (
                        SELECT first_name, middle_name, last_name, person_id
                        FROM person
                    ) psme2 ON (app.sme2_id = psme2.person_id)
                LEFT OUTER JOIN
                    (
                        SELECT first_name, middle_name, last_name, person_id
                        FROM person
                    ) pao ON (ae.app_owner_id = pao.person_id)"""
    }


}
