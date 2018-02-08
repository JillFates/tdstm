package net.transitionmanager.domain

import com.tdssrc.grails.TimeUtil

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

    Provider provider

    // The etl will contain the source code which will be compiled and executed. Eventually the
    // source code will be broken out and revisioned like how it is done in Recipes.
    String etlSourceCode

    Person createdBy
    Person lastModifiedBy

    Date dateCreated=new Date()
    Date lastUpdated

    static belongsTo = [ project: Project, provider: Provider ]

    static constraints = {
        name blank: false, size: 1..255, unique: 'provider'
        description size: 0..255, nullable: true
        target size: 0..255, nullable: true
        lastModifiedBy nullable: true
        lastUpdated nullable: true
        etlSourceCode nullable: true
    }

    static mapping = {
        id column: 'data_script_id'
        name 			sqlType: 'VARCHAR(255)'
        description 	sqlType: 'VARCHAR(255)'
        target			sqlType: 'VARCHAR(255)'
        etlSourceCode 	sqlType: 'MEDIUMTEXT'
        createdBy       column: 'created_by'
        lastModifiedBy  column: 'last_modified_by'
    }

    def beforeInsert = {
       dateCreated = TimeUtil.nowGMT()
    }
    def beforeUpdate = {
        lastUpdated = TimeUtil.nowGMT()
    }

    /**
     * Return a map representation of the DataScript instance.
     * @param minimalInfo: if set to true only the id and name will be returned.
     * @return
     */
    Map toMap(boolean minimalInfo = false) {
        Map dataMap = [
            id: id,
            name: name
        ]

        if (! minimalInfo) {
            Map additionalFields = [
                description: description,
                target: target,
                mode: mode.toString(),
                etlSourceCode: etlSourceCode,
                provider: [id: provider.id, name: provider.name],
                dateCreated: dateCreated,
                lastUpdated: lastUpdated
            ]
            dataMap.putAll(additionalFields)
        }

        return dataMap
    }


    static final List<Map> domainReferences = [
            [domain: ApiAction, delete: "null", property: "defaultDataScript", domainLabel: "API Action"]

    ]

}