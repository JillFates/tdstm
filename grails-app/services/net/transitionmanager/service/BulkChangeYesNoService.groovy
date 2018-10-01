package net.transitionmanager.service

import com.tds.asset.AssetEntity
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.BooleanUtils

@Transactional
class BulkChangeYesNoService implements ServiceMethods {

	/**
	 * Bulk replace asset entity specified field with given yes/no value
	 *
	 * @param value - new field value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	void bulkReplace(String value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {

		if (!value) {
			throw new InvalidParamException('New value cannot be null')
		}

		bulkUpdate(value, fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk clear asset entity specified field
	 *
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	void bulkClear(String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		bulkUpdate(null, fieldName, assetIds, assetIdsFilterQuery)
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
	String coerceBulkValue(Project currentProject, String value) {
		return BooleanUtils.toBoolean(value) ? 'Y' : 'N'
	}

	/**
	 * Bulk update asset entity specified field with given value
	 *
	 * @param value - new value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	private void bulkUpdate(String value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		String queryForAssetIds
		String setFieldQueryPart
		Map params = [:]
		Map assetQueryParams = [:]

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
			assetQueryParams['assetIds'] = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
			assetQueryParams = assetIdsFilterQuery.params
		}

		if (value) {
			params.value = value
			setFieldQueryPart = "SET ${fieldName} = :value"
		} else {
			setFieldQueryPart = "SET ${fieldName} = NULL"
		}

		String query = """
			UPDATE AssetEntity ${setFieldQueryPart}
			WHERE id IN ($queryForAssetIds)
		"""

		AssetEntity.executeUpdate(query, params)
	}
}
