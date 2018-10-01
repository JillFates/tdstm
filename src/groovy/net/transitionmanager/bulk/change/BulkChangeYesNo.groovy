package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
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
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	static void replace(Class type, String value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {

		if (!value) {
			throw new InvalidParamException('New value cannot be null')
		}

		bulkUpdate(type, value, fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk clear asset entity specified field
	 *
	 * @param type the class to use in the query.
	 * @param value the value is not used, just here for interface consistency.
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	static void clear(Class type, String value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		bulkUpdate(type, null, fieldName, assetIds, assetIdsFilterQuery)
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
	 * @param value - yes/no value
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @return - the boolean value of the string, {@code false} if no match or the String is null
	 */
	static String coerceBulkValue(Project currentProject, String value) {
		return BooleanUtils.toBoolean(value) ? 'Y' : 'N'
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
		String queryForIds = BulkChangeUtil.getIdsquery(type, ids, idsFilterQuery, params)

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

		AssetEntity.executeUpdate(query, params)
	}
}
