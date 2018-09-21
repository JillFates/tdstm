package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project

@Transactional
class BulkChangeDateService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Bulk replace asset entity specified field with given date
	 *
	 * @param date - new date
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	void bulkReplace(Date date, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (!date) {
			throw new InvalidParamException('New date value cannot be null')
		}

		bulkUpdate(date, fieldName, assetIds, assetIdsFilterQuery)
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
	 * @param value - date value
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @return - parsed Date object
	 */
	Date coerceBulkValue(Project currentProject, String value) {
		def parsedValue = parseDateTime(value, TimeUtil.FORMAT_DATE_TIME_ISO8601)
		if (!parsedValue) {
			parsedValue = parseDateTime(value, TimeUtil.FORMAT_DATE_TIME_6)
		}

		return parsedValue
	}

	/**
	 * Bulk update asset entity specified field with given value
	 *
	 * @param value - new value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	private void bulkUpdate(Date value, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
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

		// Bump the lastUpdated field on those assets that were affected by the remove operation.
		assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)
	}

	/**
	 * Parse a date/time value with given format
	 * @param value - date/time to parse
	 * @param format - format
	 * @return - parsed Date object
	 */
	private Date parseDateTime(String value, String format) {
		return TimeUtil.parseDateTime(value, format)
	}

}
