package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException

@Transactional
class BulkChangeString {
	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Bulk replace asset entity specified field with given string value
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

		update(type, value, fieldName, assetIds, assetIdsFilterQuery)
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
		update(type,'', fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Parse the given value to determine if it is valid or not
	 *
	 * @param currentProject - current project, not used but passed by hierarchical service
	 * @param field not used by this class but here for the interface
	 * @param value - string value
	 * @param fieldMapping, not used by this class, but here for the interface.
	 *
	 * @return - same string if it is not blank
	 */
	static String coerceBulkValue(Project currentProject, String field, String value, Map fieldMapping) {
		if (StringUtil.isBlank(value)) {
			return null
		}

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
	private static void update(Class type, String value, String fieldName, List<Long> ids = [], Map idsFilterQuery = null) {
		String setFieldQueryPart
		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsquery(type, ids, idsFilterQuery, params)

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

		AssetEntity.executeUpdate(query, params)
	}

}
