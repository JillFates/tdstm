package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project

@Transactional
class BulkChangeStringService implements ServiceMethods {

	/**
	 * Bulk replace asset entity specified field with given string value
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
		bulkUpdate('', fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Parse the given value to determine if it is valid or not
	 *
	 * @param value - string value
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @return - same string if it is not blank
	 */
	String coerceBulkValue(Project currentProject, String value) {
		if (StringUtil.isBlank(value)) {
			return null
		}

		return value
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
