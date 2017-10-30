package net.transitionmanager.domain

import com.tdssrc.grails.TimeUtil
import org.codehaus.groovy.grails.web.json.JSONObject

enum IngestionOperation {
    ADD('Add'),
    UPDATE('Update'),
    DELETE('Delete')

    final String label

    private IngestionOperation(String label) {
        this.label = label
    }
    String getKey() {
        return name()
    }

    String toString() {
        return label
    }
}

enum DataScriptMode {
    IMPORT('Import'),
    EXPORT('Export')

    final String label

    DataScriptMode(String label) {
        this.label = label
    }

    String toString() {
        return label
    }

    String getKey() {
        return name()
    }

    static DataScriptMode forLabel(String label) {
        return DataScriptMode.values().find { it.toString() == label }
    }
}

class DataScript {

    String name

    String description

    // A name or description of the target. In the case of import then it would indicate
    // principle domain or list of domains involved in the ETL.
    String target

    DataScriptMode mode

    // The etl will contain the source code which will be compiled and executed. Eventually the
    // source code will be broken out and revisioned like how it is done in Recipes.
    String etlSourceCode

    Person createdBy
    Person lastModifiedBy

    Date dateCreated
    Date lastUpdated

    static belongsTo = [ project: Project, provider: Provider ]

    static constraints = {
        name size: 1..255, unique: 'provider'
        description size: 0..255
        target size: 0..255
        lastModifiedBy nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        id column: 'data_script_id'
        name 			sqlType: 'VARCHAR(255)'
        description 		sqlType: 'VARCHAR(255)'
        target			sqlType: 'VARCHAR(255)'
        etlSourceCode 	sqlType: 'MEDIUMTEXT'
        createdBy column: 'created_by'
        lastModifiedBy column: 'last_modified_by'
    }

    def beforeInsert = {
       dateCreated = TimeUtil.nowGMT()
    }
    def beforeUpdate = {
        lastUpdated = TimeUtil.nowGMT()
    }

    /**
     * Return a map representation of the DataScript instance.
     * @return
     */
    Map toMap() {
        Map dataMap = [
                id: id,
                name: name,
                description: description,
                target: target,
                mode: mode.toString(),
                etlSourceCode: etlSourceCode,
                provider: [id: provider.id, name: provider.name],
                dateCreated: dateCreated,
                lastUpdated: lastUpdated
        ]

        return dataMap
    }

}