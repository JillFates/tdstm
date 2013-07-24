/** 
 * Used to represent categories of key/value pairs at a project level
 */

class KeyValue  implements Serializable {

	Project project
	String category
	String key
	String value

	static constraints = {
		project( nullable:false)
		category(blank:false, nullable:false, maxLength:10)
		key(blank:false, nullable:false, maxLength:30)
		value(maxLength:255)
	}

	static mapping  = {
		version false
		id composite:['project', 'category', 'key'], generator:'assigned'
		columns {
			key sqltype: 'varchar(30)'
			category sqltype: 'varchar(10)'
		}
		key column:'fi_key' //Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL
	}

	static transients = [ 'getCategoryList' ]

	String toString(){
		value
	}

	/**
	 * Used to return a KeyValue record for the specified project and category. If a default project
	 * is supplied then key values will looked up in the default project if it doesn't exist in project
	 * @param Project project - the project to get the override values from
	 * @param Boolean includeDefault - if true will 
	 * @param Project defProject - the project to get the default values from (optional)
	 * @return List of KeyValue entries
	 */
	static KeyValue get(Project project, String category, String key, Project defProject) { 
		def kv = KeyValue.findByProjectAndCategory(project, category)
		if ( !kv && defProject ) {
			kv = KeyValue.findByProjectAndCategory(defProject, category)
		}
		return kv
	}

	/**
	 * Used to return a map list of KeyValue records for the specified project and category. If a default project
	 * is supplied then key values will be interweaved for keys that only exist in the default project's list.
	 * @param Project project - the project to get the override values from
	 * @param Boolean includeDefault - if true will 
	 * @param Project defProject - the project to get the default values from (optional)
	 * @return List of KeyValue entries
	 */
	static List<KeyValue> getCategoryList(Project project, String category, Project defProject=null) {
		assert false, 'Need to implement getCategoryList()'
		// This should start with getting the defProject list and then add anything that was overridden with the project settings. The list should
		// be sorted on the key
	}

	/**
	 * Used to add or update a key value pair for a specified project. If the default project is presented then before saving it will check
	 * to see if the value exists in the default. If the value does not equal the default, then the key/value is added or updated. If it does equal
	 * the default and one exists for the project, then it is deleted.
	 * @param Project project - the project to get the override values from
	 * @param String category - the category of the key/value
	 * @param String key - the key to the key/value
	 * @param String value - the value to store for the key/store
	 * @param Project defProject - the project to compare the value with the default (optional)
	 * @return KeyValue - the object that was added or updated
	 */
	static KeyValue AddOrUpdate(Project project, String category, String value, Project defProject=null) {
		assert false, 'Need to implement getCategoryList()'
		// This should pay attention to project.isDefaultProject() and not bother with defProject in that case.
		// If defProject.value == value and project has the key then delete it.
	}
	
	/**
	 * Used to return a KeyValue record for the specified project and category.
	 * If keyValue doesn't exist then it will fetch from defProject.
	 * @param Project project - the project to get the override values from
	 * @param Project defProject - the project to get the default values from (optional)
	 * @param String category - the category of the key/value
	 * @return List of KeyValue entries
	 */
	static def getAll(Project project, String category, Project defProject) {
		def kv = KeyValue.findAllByProjectAndCategory(project, category)
		if ( !kv && defProject ) {
			kv = KeyValue.findAllByProjectAndCategory(defProject, category)
		}
		return kv
	}
}	