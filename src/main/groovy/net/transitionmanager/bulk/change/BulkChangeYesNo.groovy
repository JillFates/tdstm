package net.transitionmanager.bulk.change


import grails.gorm.transactions.Transactional
import net.transitionmanager.project.Project
import net.transitionmanager.service.InvalidParamException
import org.apache.commons.lang3.BooleanUtils

@Transactional
class BulkChangeYesNo {
	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Bulk replace asset entity specified field with given yes/no value
	 *
	 * @param type the class to use in the query.
	 * @param value - new field value
	 * @param fieldName - field name
	 * @param ids - list of assets to update
	 * @param idsFilterQuery - additional assets query filter
	 */
	static void replace(Class type, String value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {

		if (!value) {
			throw new InvalidParamException('New value cannot be null')
		}

		bulkUpdate(type, value, fieldName, ids, idsFilterQuery)
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
	static void clear(Class type, String value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		bulkUpdate(type, null, fieldName, ids, idsFilterQuery)
	}

	/**
	 * Converts a String to a boolean
	 *
	 * <pre>
	 *   BooleanUtils.toBoolean(null)    = false
	 *   BooleanUtils.toBoolean("true")  = true
	 *   BooleanUtils.toBoolean("TRUE")  = true
	 *   BooleanUtils.toBoolean("tRUe")  = true
	 *   BooleanUtils.toBoolean("on")    = true
	 *   BooleanUtils.toBoolean("yes")   = true
	 *   BooleanUtils.toBoolean("false") = false
	 *   BooleanUtils.toBoolean("x gti") = false
	 * </pre>
	 *
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @param field not used by this class, just here for the interface.
	 * @param value - yes/no value
	 * @param fieldMapping not used by this class, just here for the interface.
	 *
	 * @return - the boolean value of the string, {@code false} if no match or the String is null
	 */
	static String coerceBulkValue(Project currentProject, String value) {
		if(!value){
			return ''
		}

		return BooleanUtils.toBoolean(value) ? 'Yes' : 'No'
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
	private static void bulkUpdate(Class type, String value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		String setFieldQueryPart
		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsQuery(type, ids, idsFilterQuery, params)

		if (value) {
			params.value = value
			setFieldQueryPart = "SET ${fieldName} = :value"
		} else {
			setFieldQueryPart = "SET ${fieldName} = ''"
		}

		String query = """
			UPDATE ${type.simpleName} ${setFieldQueryPart}
			WHERE id IN ($queryForIds)
		"""

		type.executeUpdate(query, params)
	}
}
