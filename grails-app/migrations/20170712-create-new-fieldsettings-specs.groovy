import groovy.json.JsonSlurper
import com.xlson.groovycsv.CsvParser

/**
 * @author John Martin
 * Ticket TM-6619
 */
databaseChangeLog = {
    changeSet(author: "jmartin", id: "20170712 TM-6619 Create Field Settings JSON specs") {
        comment('This will aggregate values from various places and create JSON specs for every project')

        grailsChange {
            change {
                createFieldSpecsForAllProjects(sql)
            }
        }
    }
}

/**
 * Used to load in the CSV file that defines all of the asset domains fields.
 *
 * The file must be in the followin directory to be able to be picked up
 * by the resource loader.
 *
 *     /grails-app/conf/customField/AssetDomainFields.csv
 */
def loadCsvData() {
    String sourceFile = 'customField/AssetDomainFields.csv'
    String csvFile = this.getClass().getResource( sourceFile ).text

    if (!csvFile) {
        throw new RuntimeException("Unable to load FieldSpecs file $sourceFile")
    }

    return new CsvParser().parse(csvFile)
}

/**
 * This method will loop over every project and create field Specifications for each asset class
 * aggregating data from Project.custom*, FieldImportance and KeyValue domains to build a
 * concise JSON object and insert them into the Setting domain
 *
 * @param sql - the SQL database object to use to interact with the DB
 */
void createFieldSpecsForAllProjects(sql) {
    println "Beginning the Field Settings Migration Script"

    List projects = sql.rows('select * from project order by project_id')
    //List projects = sql.rows('select * from project where project_id=11500 order by project_id')

    println "${projects.size()} Projects to be migrated"
    int counter = 0

    final String UNIMPORTANT = 'U'
    final String HIDDEN = 'H'

    final String SETTING_TYPE = 'CUSTOM_DOMAIN_FIELD_SPEC'
    final String DELETE_SQL = 'delete from setting where type=?'

    println "Purging any pre-existing Asset Field Specs from Setting"
    sql.execute(DELETE_SQL, SETTING_TYPE)

    /**
     * Used to load the tooltips into a Map for any field that there is a tooltip
     */
    def getTooltips = { projectId, assetClass ->
        Map tips = [:]
        sql.eachRow ('select * from key_value where project_id=:pid and category=:ac', [pid:projectId, ac:assetClass] )
        { tip ->
            if (tip.value != null && tip.value != '') {
                tips.put(tip.fi_key, tip.value)
            }
        }
        return tips
    }

    /**
     * Used to read through the FieldImportanct to parse out the value set on the BundleReady set
     */
    def getFieldImportance = { projectId, assetClass ->
        Map params = [pid:projectId, et:assetClass]
        // projectId=11322
        String query = "select config from field_importance where project_id=$projectId and entity_type='$assetClass'"
        def result = sql.firstRow(query)
        String jsonText = result ? result.config : ''
        //println "  getFieldImportance($projectId, $assetClass) $jsonText"
        Map fields = [:]
        if (jsonText) {
            def slurper = new groovy.json.JsonSlurper()
            def jsonMap = slurper.parseText(jsonText)
            for (map in jsonMap) {
                // Grab the value for the BundleReady phase of the the project
                String imp = ''
                if ((map instanceof java.util.Map.Entry) && (map.value instanceof Map) && map.value.containsKey('phase') ) {
                    imp = map.value.phase?.B
                }
                if (imp != '') {
                    fields.put(map.key, imp)
                }
            }
        } else {
            println "   No FieldImportance found for assetClass=$assetClass"
        }

        return fields
    }

    for (project in projects) {
        counter++
        println "$counter) Starting project ${project.project_id} ${project.project_code}"

        Map tips = [:]
        [APPLICATION:'tt_app', DEVICE:'tt_asset', DATABASE:'tt_database', STORAGE:'tt_storage'].each { key, assetClass ->
            tips.put(key, getTooltips(project.project_id, assetClass))
        }

        Map imps = [:]
        [APPLICATION:'Application', DEVICE:'AssetEntity', DATABASE:'Database', STORAGE:'Files'].each { key, assetClass ->
            imps.put(key, getFieldImportance(project.project_id, assetClass))
        }

        String domain=''
        String mapKey=''
        Map domainFieldsMap = [:]

        // Iterate through the lines from the CSV and create the JSON for each of the asset classes for the project
        for (line in loadCsvData()) {
            if (domain != line.domain) {
                // order = 0
                domain = line.domain
                mapKey = domain.toUpperCase()
                domainFieldsMap[mapKey] = [domain:domain, fields:[]]
                println "   Configuring Standard Fields for $mapKey"
            }
            if (line.order == '-1') {
                // Skip properties marked to be removed
                continue
            }

            if (line.field ==~ /custom(\d{1,2})/ ) {
                // We'll handle the customs separately
                continue
            }

            String importance = (imps[mapKey][line.field] ?: UNIMPORTANT)
            if (importance == HIDDEN) importance = UNIMPORTANT

            Map fieldSpec = [
                field: line.field,
                label: line.label,
                tip: (tips[mapKey][line.field] ?: ''),
                udf: 0,
                default: '',
                shared: 0,
                show: (line.show == 'true' ? 1 : 0 ),
                control: line.control,
                order: line.order.toLong(),
                imp: importance,
                constraints: [
                    required: (line.field == 'assetName' ? 1 : 0),
                ]
            ]

            domainFieldsMap[mapKey].fields.add(fieldSpec)
        }

        int showMax = project.custom_fields_shown
        // Now go through the custom fields for each of the Asset Classes and add appropriately
        for (domainMap in domainFieldsMap) {
            String assetClass = domainMap.key
            Map fieldSpecs = domainMap.value

            println "   Configuring Custom Fields for $assetClass"

            (1..96).each { num ->
                boolean addIt=true
                boolean show=true
                String fieldName = 'custom' + num
                String importance

                if (num > showMax) {
                    // Fields beyond those shown will be included but hidden if the label doesn't match the fieldName
                    if (! project[fieldName] || project[fieldName].equalsIgnoreCase(fieldName)) {
                        addIt = false
                    }
                    show = false
                    importance = UNIMPORTANT
                } else {
                    if (imps[assetClass][fieldName]) {
                        importance = imps[assetClass][fieldName]
                    } else {
                        importance = UNIMPORTANT
                        println "   Unable to find imps for assetClass=$assetClass, fieldName=$fieldName"
                    }
                }

                if (importance == HIDDEN) importance = UNIMPORTANT

                Map fieldSpec = [
                    field: fieldName,
                    label: project[fieldName],
                    tip: (tips[assetClass][fieldName] ?: ''),
                    udf: 1,
                    default: '',
                    shared: 0,
                    show: (show == 'true' ? 1 : 0 ),
                    control: 'String',
                    order: 100 + num,
                    imp: importance,
                    constraints: [
                        required: 0,
                        minSize: 0,
                        maxSize: 255
                    ]
                ]

                domainFieldsMap[assetClass].fields.add(fieldSpec)
            }

            // Now inject the data into the projects' setting table for each class
            final String INSERT_SQL = '''insert into setting (project_id, type, setting_key, json, date_created, version)
                values (:pid, :type, :key, :json, now(), :version)'''

            def builder = new groovy.json.JsonBuilder(domainFieldsMap[assetClass])
            Map params = [
                pid:    project.project_id,
                type:   SETTING_TYPE,
                key:    assetClass,
                json:   builder.toString(),
                version: 0
            ]
            println "   Creating field specs for $assetClass"
            sql.execute(INSERT_SQL, params)
        }
    }
}