package net.transitionmanager.bulk.change


import grails.gorm.transactions.Transactional
import net.transitionmanager.project.Project
import net.transitionmanager.exception.InvalidParamException

@Transactional
class BulkChangeInteger {
	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Bulk replace asset entity specified field with given numeric value
	 *
	 * @param type the class to use in the query.
	 * @param value - new field value
	 * @param fieldName - field name
	 * @param ids - list of assets to update
	 * @param idsFilterQuery - additional assets query filter
	 */
	static void replace(Class type, Integer value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		if (!value) {
			throw new InvalidParamException('New value cannot be null')
		}

		update(type, value, fieldName, ids, idsFilterQuery)
	}

	/**
	 * Bulk clear asset entity specified field
	 *
	 * @param type the class to use in the query.
	 * @param value the value is not used, just here for interface consistency.
	 * @param fieldName - field name
	 * @param ids - list of assets to update
	 * @param idsFilterQuery - additional assets query filter
	 */
	static void clear(Class type, Integer value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		update(type,null, fieldName, ids, idsFilterQuery)
	}

	/**
	 * Parse the given value to determine if it is valid or not
	 *
	 *
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @param field  not used this class just here for the interface
	 * @param value - numeric value
	 * @param fieldMapping not used for this class just here for the interface.
	 *
	 * @return - same number if it is a number
	 */
	static Integer coerceBulkValue(Project currentProject, Integer value) {
		return value
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
	private static void update(Class type, Integer value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		String setFieldQueryPart
		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsQuery(type, ids, idsFilterQuery, params)

		if (value) {
			params.value = value
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
