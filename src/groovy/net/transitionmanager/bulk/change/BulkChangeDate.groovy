package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ServiceMethods

@Transactional
class BulkChangeDate implements ServiceMethods {
	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Bulk replace asset entity specified field with given date
	 *
	 * @param type the class to use in the query.
	 * @param date - new date
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	static void replace(Class type, Date date, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (!date) {
			throw new InvalidParamException('New date value cannot be null')
		}

		update(type, date, fieldName, assetIds, assetIdsFilterQuery)
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
	static void clear(Class type, Date date, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		update(type,null, fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Parse the given value to determine if it is valid or not
	 *
	 * @param value - date value
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @param fieldMapping not used for this class just here for the interface.
	 *
	 * @return - parsed Date object
	 */
	static Date coerceBulkValue(Project currentProject, String field, String value, Map fieldMapping) {
		def parsedValue = parseDateTime(value, TimeUtil.FORMAT_DATE_TIME_ISO8601)
		if (!parsedValue) {
			parsedValue = parseDateTime(value, TimeUtil.FORMAT_DATE_TIME_6)
		}

		return parsedValue
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
	private static void update(Class type, Date value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
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

	/**
	 * Parse a date/time value with given format
	 * @param value - date/time to parse
	 * @param format - format
	 * @return - parsed Date object
	 */
	static private Date parseDateTime(String value, String format) {
		return TimeUtil.parseDateTime(value, format)
	}

}
