package net.transitionmanager.task.taskgraph

import com.tdssrc.grails.StringUtil
import net.transitionmanager.command.task.TaskSearchCommand
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.task.timeline.CPAResults
import net.transitionmanager.task.timeline.TaskVertex

class TaskSearch {

    /**
     * The user's current project.
     */
    Project project

    /**
     * The Command Object with the filters.
     */
    TaskSearchCommand taskSearchCommand

    /**
     * The parameters for the main query.
     */
    Map queryParams = [:]

    /**
     * Reference to the query for tags that should be executed based on the filters.
     */
    String tagQuery

    /**
     * Reference to the ApiAction query if the "With TDM Actions" is set.
     */
    String apiActionQuery

    /**
     * The where clauses that should be considered when querying the task table.
     */
    List<String> taskWhereClauses = [ "comment_type = 'issue'" ]

    /**
     * If the asset table needs to be queried, include these clauses.
     */
    List<String> assetWhereClauses = []

    /**
     * Basic constructor that keeps a local copy of the command object that needs to be used
     * to search for the tasks.
     * @param TaskSearchCommand
     */
    TaskSearch(Project project, TaskSearchCommand taskSearchCommand) {
        this.project = project
        this.taskSearchCommand = taskSearchCommand
    }


    /**
     * Evaluate if, based on the command object, the CPA should be calculated.
     * @return true: the CPA needs to be calculated. False otherwise.
     */
    boolean needsCPACalculation() {
        return taskSearchCommand.criticalPathMode == 'Realtime' || taskSearchCommand.cyclicalPath == 1
    }

    /**
     * Based on the CPAResults, obtain a list of unique task ids.
     *
     * @param cpaResults - the CPA results.
     * @return a list of unique task ids.
     */
    List<Long> getCPATasks(CPAResults cpaResults) {
        List<Long> tasksFound = []
        // If the CPA mode is set to Realtime fetch the CPA tasks ids.
        if (taskSearchCommand.criticalPathMode == 'Realtime') {
            tasksFound = cpaResults.tasks*.id
        }
        // If the cyclical path flag is set, add the tasks in the cycles.
        if (taskSearchCommand.cyclicalPath == 1) {
            for (List<TaskVertex> cycle in cpaResults.summary.cycles) {
                tasksFound.addAll(cycle*.taskId)
            }
        }
        // Return a unique list of task ids.
        return tasksFound.unique{ Long a, Long b -> a <=> b}

    }

    /**
     * Based on the TaskSearchCommand, the given event and the viewUnpublished flag,
     * construct a SQL query and its required parameters.
     *
     * @param moveEvent
     * @param viewUnpublished
     * @return a map with the query and its parameters.
     */
    Map buildSearchQuery(MoveEvent moveEvent, boolean viewUnpublished) {

        // Evaluate all the filter, except for most of the CPA related filters which are handled separately.
        evaluateFilters(moveEvent, viewUnpublished)

        String query

        // Put together the query against the asset_comment table.
        String taskQuery = buildQuery(TASK_QUERY, taskWhereClauses)

        String assetQuery
        // The Assets table needs to be joined if there are asset, tag or action related filters
        if (assetWhereClauses || tagQuery || apiActionQuery) {
            assetQuery = buildQuery(ASSET_QUERY.trim(), assetWhereClauses)
            query = "SELECT DISTINCT(ac.taskId) AS taskId FROM ($taskQuery) ac INNER JOIN ($assetQuery) ae ON (ac.asset_entity_id = ae.asset_entity_id)"
            if (tagQuery) {
                query = "$query INNER JOIN ($tagQuery) ta ON(ae.asset_entity_id = ta.asset_id)"
            }
            if (apiActionQuery) {
                query = "$query INNER JOIN ($apiActionQuery) ac ON(ae.api_action_id = ac.id)"
            }
        } else {
            query = taskQuery
        }

        return [
                query: query,
                params: queryParams
        ]

    }

    /**
     * Given a base query and a where clause, this auxiliary method simply constructs a SQL query.
     * @param baseQuery
     * @param whereClauses
     * @return
     */
    private String buildQuery(String baseQuery, List<String> whereClauses) {
        String query = baseQuery
        if (whereClauses) {
            query = "$baseQuery WHERE ${whereClauses.join(' AND ')}"
        }
        return query
    }


    /**
     * Iterate over all of the filters, processing each of one accordingly.
     * @param moveEvent
     * @param viewUnpublished
     */
    private void evaluateFilters(MoveEvent moveEvent, boolean viewUnpublished) {


        if (taskSearchCommand.criticalPathMode == 'Baseline') {
            taskWhereClauses.add('is_critical_path = true')
        }

        if (!moveEvent) {
            taskWhereClauses.add('project_id = :projectId')
            queryParams['projectId'] = project.id
        }

        if (taskSearchCommand.taskText) {
            String comment = "%${taskSearchCommand.taskText}%"
            taskWhereClauses.add('comment = :taskText')
            queryParams['taskText'] = comment
        }

        if (taskSearchCommand.withActions) {
            taskWhereClauses.add('api_action_id IS NOT NULL')
        }

        if (!viewUnpublished) {
            taskWhereClauses.add('is_published = true')
        }

        if (taskSearchCommand.environment) {
            assetWhereClauses.add('environment = :environment')
            queryParams['environment'] = taskSearchCommand.environment
        }

        evaluateOwnerAndSmes()
        evaluateTags()
        evaluateTmdActions()
    }

    /**
     * Based on the owner and smes filter, add the appropriate parameter and 'where' clause.
     */
    private void evaluateOwnerAndSmes() {
        if (taskSearchCommand.ownerSmeId != null) {
            assetWhereClauses.add('(app_owner_id = :appOwnerId OR sme_id = :smeId OR sme2_id = :sme2Id)')
            queryParams['appOwnerId'] = taskSearchCommand.ownerSmeId
            queryParams['smeId'] = taskSearchCommand.ownerSmeId
            queryParams['sme2Id'] = taskSearchCommand.ownerSmeId
        }
    }

    /**
     * Check the tags related filters and determine which query should be executed -- if any.
     */
    private void evaluateTags() {
        if (taskSearchCommand.tagIds) {
            String tagMatch = taskSearchCommand.tagMatch ?: 'ANY'
            if (tagMatch == 'ANY') {
                tagQuery = TAG_ANY_QUERY
            } else {
                tagQuery = TAG_ALL_QUERY
                queryParams['tagListSize'] = taskSearchCommand.tagIds.size()
            }
            queryParams['tagList'] = taskSearchCommand.tagIds
        }
    }


    /**
     * Set the query for remote actions if the withTmdActions flag is set.
     */
    private void evaluateTmdActions() {
        if (taskSearchCommand.withTmdActions) {
            apiActionQuery = REMOTE_ACTIONS_QUERY
        }
    }


    /**
     * This is the basic query for the AssetComment table.
     */
    private static final String TASK_QUERY = "SELECT asset_comment_id as taskId, asset_entity_id, api_action_id FROM asset_comment"

    /**
     * Query for retrieving the assets that have any of the tags in a given list.
     */
    private static final String TAG_ANY_QUERY = "SELECT DISTINCT(asset_id) as asset_id FROM tag_asset ta WHERE tag_id IN (:tagList)"

    /**
     * Query that retrieves the assets that have every tag included in the given list.
     */
    private static final String TAG_ALL_QUERY = "SELECT asset_id FROM tag_asset WHERE tag_id IN (:tagList) GROUP BY asset_id HAVING COUNT(*) = :tagListSize"

    private static final String REMOTE_ACTIONS_QUERY = "SELECT id FROM api_action WHERE is_remote = 1"

    /**
     * The query for assets actually does a join with the application table and projects the id, environment, app owner and smes.
     */
    private static final String ASSET_QUERY = """
            SELECT asset_entity_id, environment, app_owner_id, sme_id, sme2_id
            FROM asset_entity aen INNER JOIN application a ON (aen.asset_entity_id = a.app_id)
            """

}
