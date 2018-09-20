package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project

@Transactional
class BulkChangeNumberService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Bulk replace asset entity specified field with given numeric value
	 *
	 * @param value - new field value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	void bulkReplace(Integer value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
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
	 * Parse the given value to determine if it is valid or not
	 *
	 * @param value - numeric value
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @return - same number if it is a number
	 */
	Integer coerceBulkValue(Project currentProject, String value) {
		if (NumberUtil.isNumber(value)) {
			return NumberUtil.toPositiveInteger(value)
		}

		return null
	}

	/**
	 * Bulk update asset entity specified field with given value
	 *
	 * @param value - new value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	private void bulkUpdate(Integer value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		String queryForAssetIds
		Map params = [:]
		Map assetQueryParams = [:]
		params.value = value

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
			assetQueryParams['assetIds'] = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
			assetQueryParams = assetIdsFilterQuery.params
		}

		String query = """
			UPDATE AssetEntity SET ${fieldName} = :value  
			WHERE id IN ($queryForAssetIds)
		"""

		AssetEntity.executeUpdate(query, params)

		// Bump the lastUpdated field on those assets that were affected by the remove operation.
		assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)
	}
}
