package net.transitionmanager.common

import groovy.transform.EqualsAndHashCode
import net.transitionmanager.project.Project

/**
 * Represents categories of key/value pairs at a project level.
 */
@EqualsAndHashCode(includes = ['project', 'category', 'key'])
class KeyValue implements Serializable {

	Project project
	String  category
	String  key
	String  value

	static constraints = {
		category blank: false, size: 1..30
		key blank: false, size: 1..30
	}

	static mapping = {
		version false
		id composite: ['project', 'category', 'key'], generator: 'assigned'
		key column: 'fi_key' //Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL
		columns {
			category sqltype: 'varchar(30)'
			key sqltype: 'varchar(30)'
		}
	}

	String toString() { value }

	/**
	 * Prevent null values
	 * @param newValue
	 */
	void setValue(String newValue) {
		value = newValue ?: ''
	}

	/**
	 * Returns a KeyValue record for the specified project and category. If a default project
	 * is supplied then key values will looked up in the default project if it doesn't exist in project
	 * @param project - the project to get the override values from
	 * @param defProject - the project to get the default values from (optional)
	 */
	static KeyValue get(Project project, String category, String key, Project defProject) {
		KeyValue kv = findByProjectAndCategory(project, category)
		if (kv) {
			kv
		}
		else if (defProject) {
			findByProjectAndCategory(defProject, category)
		}
	}

	/**
	 * Returns KeyValue records for the specified project and category. If a default project
	 * is supplied then key values will be interweaved for keys that only exist in the default project's list.
	 * @param project - the project to get the override values from
	 * @param defProject - the project to get the default values from (optional)
	 */
	static List<KeyValue> getCategoryList(Project project, String category, Project defProject = null) {
		assert false, 'Need to implement getCategoryList()'
		// This should start with getting the defProject list and then add anything that was overridden with the project settings. The list should
		// be sorted on the key
	}

	/**
	 * Used to add or update a key value pair for a specified project. If the default project is presented then before saving it will check
	 * to see if the value exists in the default. If the value does not equal the default, then the key/value is added or updated. If it does equal
	 * the default and one exists for the project, then it is deleted.
	 * @param project - the project to get the override values from
	 * @param category - the category of the key/value
	 * @param value - the value to store for the key/store
	 * @param defProject - the project to compare the value with the default (optional)
	 * @return the object that was added or updated
	 */
	static KeyValue AddOrUpdate(Project project, String category, String value, Project defProject = null) {
		assert false, 'Need to implement getCategoryList()'
		// This should pay attention to project.isDefaultProject() and not bother with defProject in that case.
		// If defProject.value == value and project has the key then delete it.
	}

	/**
	 * Returns KeyValue records for the specified project and category.
	 * If none exist then they will be fetched from defProject.
	 * @param project - the project to get the override values from
	 * @param defProject - the project to get the default values from (optional)
	 * @param category - the category of the key/value
	 * @return the KeyValue entries
	 */
	static List<KeyValue> getAll(Project project, String category, Project defProject) {
		List<KeyValue> kv = findAllByProjectAndCategory(project, category)
		if (kv) {
			kv
		}
		else if (defProject) {
			findAllByProjectAndCategory(defProject, category)
		}
	}
}
