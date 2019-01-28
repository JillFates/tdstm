package net.transitionmanager.domain
/**
 * Indicates what direction that the data is flowing for a data script (in or out)
 */
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
	 static enum PROPS {
		 id, name, description, target, mode, etlSourceCode, provider, dateCreated,
		 lastUpdated, sampleFilename, originalSampleFilename
	 }
	 static Set<PROPS> MINIMAL_INFO = [ PROPS.id, PROPS.name ]
	 static Set<PROPS> ALL_INFO = PROPS.values()
	 static Set<PROPS> SOURCE_CODE = [ PROPS.etlSourceCode, PROPS.sampleFilename, PROPS.originalSampleFilename ]
	 static Set<PROPS> BASE_INFO = ALL_INFO - SOURCE_CODE

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

    Date dateCreated=new Date()
    Date lastUpdated

    String sampleFilename = ''
    String originalSampleFilename = ''

    static belongsTo = [ project: Project, provider: Provider ]

    static constraints = {
        name blank: false, size: 1..255, unique: ['project', 'provider']
        description size: 0..255, nullable: true
        target size: 0..255, nullable: true
        lastModifiedBy nullable: true
        lastUpdated nullable: true
        etlSourceCode nullable: true
        sampleFilename  blank: true, size: 0..255
        originalSampleFilename  blank: true, size: 0..255
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

	static transients = [ 'MINIMAL_INFO', 'ALL_INFO', 'SOURCE_CODE', 'BASE_INFO' ]

    /**
     * Return a map representation of the DataScript instance.
     * @param minimalInfo: if set to true only the id and name will be returned.
     * @return
     */
    Map toMap(Set<PROPS> props = null) {

	     if( ! props ) {
		     props = ALL_INFO
	     }


	     Map retVal = props.inject([:]) { map, prop ->
		     String key = prop.name()
		     def value

		     switch( prop ) {
			     case PROPS.mode :
				            value = mode.toString()
				            break

			     case PROPS.provider :
				            value = [id: provider.id, name: provider.name]
				            break

			     default:
				            value = this."${key}"
		     }

		     map[key] = value

		     return map
	     }

	    return retVal
    }

    /**
     * List of domain references for DataScripts
     * TODO : JPM 2/2018 : TM-9346 - Change DataScriptService to not require DataScript.domainReferences
     */
    static final List<Map> domainReferences = [
        [domain: ApiAction, delete: "restrict", property: "defaultDataScript", domainLabel: "API Action"],
        [domain: ImportBatch, delete: "cascade", property: "dataScript", domainLabel: "Import Batch"]
    ]

}