package net.transitionmanager.bulk.change


import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import grails.util.Holders
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ProjectService

@Transactional
class BulkChangePerson {
	static ProjectService projectService

	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Bulk replace asset entity specified field with given numeric value
	 *
	 * @param type the class to use in the query.
	 * @param person - new person field value
	 * @param fieldName - field name
	 * @param ids - list of assets to update
	 * @param idsFilterQuery - additional assets query filter
	 */
	static void replace(Class type, Person person, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		if (!person) {
			throw new InvalidParamException('New person value cannot be null')
		}

		update(type, person, fieldName, ids, idsFilterQuery)
	}

	/**
	 * Bulk clear asset entity specified field
	 *
	 * @param type the class to use in the query.
	 * @param person the value is not used, just here for interface consistency.
	 * @param fieldName - field name
	 * @param ids - list of assets to update
	 * @param idsFilterQuery - additional assets query filter
	 */
	static void clear(Class type, Person person, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		update(type,null, fieldName, ids, idsFilterQuery)
	}

	/**
	 * Parse the given person id to determine if it belongs to current project or it has access to it
	 *
	 * @param currentProject - current project
	 * @param personId - a person id
	 * @param field not used in this class just here for the interface.
	 * @param fieldMapping not used for this class just here for the interface.
	 * @return - person if found
	 */
	static Person coerceBulkValue(Project currentProject, String personId) {
		if (!NumberUtil.isNumber(personId)) {
			return null
		}

		// get person from database by id
		Long pId = NumberUtil.toPositiveLong(personId, 0)
		Person person = Person.get(pId)

		if(projectService.getAssignableStaff(currentProject, person)){
			return person
		}

		return null
	}

	/**
	 * Bulk update asset entity specified field with given value
	 *
	 * @param type the class to use in the query.
	 * @param value - new value
	 * @param fieldName - field name
	 * @param ids - list of assets to update
	 * @param idsFilterQuery - additional assets query filter
	 */
	private static void update(Class type, Person person, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		String setFieldQueryPart
		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsQuery(type, ids, idsFilterQuery, params)

		if (person) {
			params.value = person
			setFieldQueryPart = "SET ${fieldName} = :value"
		} else {
			setFieldQueryPart = "SET ${fieldName} = NULL"
		}

		String query = """
			UPDATE ${type.simpleName} ${setFieldQueryPart}
			WHERE id IN ($queryForIds)
		"""

		type.executeUpdate(query, params)
	}

	static ProjectService getProjectService(){
		if(!projectService) {
			projectService = Holders.applicationContext.getBean('projectService')
		}

		projectService
	}
}
