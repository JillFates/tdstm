package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException

@Transactional
class BulkChangePerson {
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
	 * @param personId - a person id
	 * @param currentProject - current project
	 * @return - person if found
	 */
	static Person coerceBulkValue(Project currentProject, String personId) {
		if (!NumberUtil.isNumber(personId)) {
			return null
		}

		// get person from database by id
		Long pId = NumberUtil.toPositiveLong(personId, 0)
		Person person = Person.get(pId)

		// person was found, see if it has a user login
		if (person && person?.userLogin) {
			// obtain found person current project
			Project foundPersonProject = person.getUserLogin().getCurrentProject()
			if (foundPersonProject) {
				// if found person current project is the same
				// as the current project passed by param, them return found person
				if (currentProject.id == foundPersonProject.getId()) {
					return person
				}
			}
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
		String queryForIds = BulkChangeUtil.getIdsquery(type, ids, idsFilterQuery, params)

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
}
