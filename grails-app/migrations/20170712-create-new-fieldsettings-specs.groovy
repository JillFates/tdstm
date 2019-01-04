import groovy.json.JsonSlurper
import com.xlson.groovycsv.CsvParser
import com.tdssrc.grails.StringUtil

/**
 * @author John Martin
 * Ticket TM-6619
 * version 8
 *   - Changed logic checking for duplicate label references and renamed to indicate name conflict
 * version 6/7
 *   - Fixed some of the control types that are used for standard fields (superficial)
 * version 5
 *   - Fix issue where custom.title fields sometimes set to null
 * version 4
 *   - Changed some properties to Date
 * version 3
 *   - fixed issue with all customs being included
 *   - Tips were not always being added
 *   - Display value now correctly set
 *   - Custom that were marked hidden now correctly hidden
 */
databaseChangeLog = {
    changeSet(author: "jmartin", id: "20170712 TM-6619-v8 Create Field Settings JSON specs") {
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
 * This will return a map by asset class where each will have a map of the fields
 */
Map getDomainFieldNamesAsMap() {
    Map fields = [:]
    String domain = ''
    String mapKey = ''
    for (line in loadCsvData()) {
        if (domain != line.domain) {
            domain = line.domain
            mapKey = domain.toUpperCase()
            fields.put(mapKey, [:])
        }
        if (line.order == '-1') {
            // Skip properties marked to be removed
            continue
        }
        if (line.field ==~ /custom(\d{1,2})/ ) {
            // We'll handle the customs separately
            continue
        }

        fields[mapKey].put(line.field, true)
    }
    // println "getDomainFieldsAsMap:  $fields"
    return fields
}

/**
 * This will return a map by asset class where each will have a map of the fields
 */
Map getDomainLabelsAsMap() {
    Map fields = [:]
    String domain = ''
    String mapKey = ''
    for (line in loadCsvData()) {
        if (domain != line.domain) {
            domain = line.domain
            mapKey = domain.toUpperCase()
            fields.put(mapKey, [:])
        }
        if (line.order == '-1') {
            // Skip properties marked to be removed
            continue
        }
        if (line.field ==~ /custom(\d{1,2})/ ) {
            // We'll handle the customs separately
            continue
        }

        fields[mapKey].put(line.label, true)
    }
    // println "getDomainFieldsAsMap:  $fields"
    return fields
}
/**
 * Used to load the tooltips into a Map for any field that there is a tooltip
 */
Map getTooltips(sql, projectId, assetClass) {
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
Map getFieldImportance(sql, projectId, assetClass) {
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

/**
 * This method will loop over every project and create field Specifications for each asset class
 * aggregating data from Project.custom*, FieldImportance and KeyValue domains to build a
 * concise JSON object and insert them into the Setting domain
 *
 * @param sql - the SQL database object to use to interact with the DB
 */
void createFieldSpecsForAllProjects(sql) {
    println "Beginning the Field Settings Migration Script"

    String projectFilter = ''
    // projectFilter = 'where project_id=3489'
    String query = "select * from project ${projectFilter} order by project_id"
    // println "*** query=$query"
    List projects = sql.rows(query)

    println "${projects.size()} Projects to be migrated"
    int counter = 0

    final String UNIMPORTANT = 'U'
    final String HIDDEN = 'H'

    final String SETTING_TYPE = 'CUSTOM_DOMAIN_FIELD_SPEC'
    final String DELETE_SQL = 'delete from setting where type=?'

    println "Purging any pre-existing Asset Field Specs from Setting"
    sql.execute(DELETE_SQL, SETTING_TYPE)

    Map assetFields = getDomainFieldNamesAsMap()
    Map assetLabels = getDomainLabelsAsMap()

    for (project in projects) {
        counter++
        println "$counter) Starting project ${project.project_id} ${project.project_code}"

        Map tips = [:]
        [APPLICATION:'tt_app', DEVICE:'tt_asset', DATABASE:'tt_database', STORAGE:'tt_storage'].each { key, assetClass ->
            tips.put(key, getTooltips(sql, project.project_id, assetClass))
        }

        Map imps = [:]
        [APPLICATION:'Application', DEVICE:'AssetEntity', DATABASE:'Database', STORAGE:'Files'].each { key, assetClass ->
            imps.put(key, getFieldImportance(sql, project.project_id, assetClass))
        }

        String domain=''
        String mapKey=''
        Map domainFieldsMap = [:]

        // Iterate through the lines from the CSV and create the JSON for each of the asset classes for the project.
        // Note that there are rows where the order == -1 that indicate that they're to be removed and there are
        // custom fields used for testing which will be ignored.
        // This for loop is JUST for the standard fields
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
            if (importance == HIDDEN) {
                importance = UNIMPORTANT
            }

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

        // Now go through the custom fields for each of the Asset Classes and add appropriately
        for (domainMap in domainFieldsMap) {
            String assetClass = domainMap.key
            Map fieldSpecs = domainMap.value

            println "   Configuring Custom Fields for $assetClass"

            Map usedNames = [:]     // Will be used to track the names used to prevent duplicates

            for (num in (1..96)) {
                boolean show=true
                String fieldName = 'custom' + num
                String importance

                String currentLabel = project[fieldName]
                if (StringUtil.isBlank(currentLabel)) {
                    currentLabel = fieldName
                }

                // Check if the label is a standard field
                if (assetFields[assetClass][currentLabel]) {
                    String dupName = currentLabel
                    currentLabel = "X_${currentLabel}_X"
                    println "   WARNING - $fieldName references standard field name $dupName, renamed to '$currentLabel'"
                }

                // Check if the label is a standard field
                if (assetLabels[assetClass][currentLabel]) {
                    String dupName = currentLabel
                    currentLabel = "X_${currentLabel}_X"
                    println "   WARNING - $fieldName references standard field label '$dupName', renamed to '$currentLabel'"
                }

                // Make sure the label isn't a dup
                if (usedNames.containsKey(currentLabel)) {
                    String dupName = currentLabel
                    currentLabel = "X_${currentLabel}_X"
                    println "   WARNING - $fieldName duplicated label '$dupName', renamed to '$currentLabel'"
                }

                // Save that the label has been used
                usedNames.put(currentLabel, true)

                // Make sure no other properties improperly reference any other custom field
                usedNames.put(fieldName, true)
                usedNames.put('Custom'+num, true)

                if (num > project.custom_fields_shown) {
                    // Hidden Custom Fields
                    if (currentLabel.equalsIgnoreCase(fieldName)) {
                        // If the label was not changed then skip it
                        continue
                    }
                    show = false
                    importance = UNIMPORTANT
                } else {
                    // Shown Custom Fields
                    if (imps[assetClass][fieldName]) {
                        importance = imps[assetClass][fieldName]
                    } else {
                        importance = UNIMPORTANT
                        // println "   Unable to find imps for assetClass=$assetClass, fieldName=$fieldName"
                    }
                }

                if (importance == HIDDEN) {
                    show = false
                    importance = UNIMPORTANT
                }

                Map fieldSpec = [
                    field: fieldName,
                    label: currentLabel,
                    tip: (tips[assetClass][fieldName] ?: ''),
                    udf: 1,
                    default: '',
                    shared: 0,
                    show: (show ? 1 : 0 ),
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
            final String INSERT_SQL = '''
                insert into setting (project_id, type, setting_key, json, date_created, version)
                values (:pid, :type, :key, :json, now(), :version)
            '''

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
