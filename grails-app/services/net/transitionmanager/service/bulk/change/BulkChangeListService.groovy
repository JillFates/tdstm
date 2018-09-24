package net.transitionmanager.service.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ServiceMethods

/**
 * A service for managing bulk operations on Assets for assigning values from a list.
 */
@Transactional
class BulkChangeListService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Coerces the string value passed from the BulkChangeService and validates it to be a value from a list.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param field the list field to update
	 * @param value The string value that needs to be coerce.
	 * @param values the looked up values to use to validate the value to be in the list of values.
	 *
	 * @return a string, that is valid for a list.
	 */
	String coerceBulkValue(Project currentProject, String field, String value, List<String> values = []) {

		if (value && !values.contains(value)) {
			throw new InvalidParamException("Value $value is not valid bulk update of $field.")
		}

		return value
	}

	/**
	 * Clears the current value from a list with ''.
	 *
	 * @param value the value which should be null for clear.
	 * @param field the list field to update
	 * @param assetIds The ids of the assets to clear the list for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkClear(String value, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (value) {
			throw new InvalidParamException("For bulk clear you can not specify a value.")
		}

		replace('', field, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Replaces the current list value with a new one.
	 *
	 * @param value to replace the current list value with a new one. The value can't be '' or null.
	 * @param field the list field to update
	 * @param assetIds The ids of the assets to replace the list  for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkReplace(String value, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (!value) {
			throw new InvalidParamException("For bulk replace you need to specify a valid $field value.")
		}

		replace(value, field, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Replaces the current list value with a new one.
	 *
	 * @param value to replace the current list value with a new one.
	 * @param field the list field to update
	 * @param assetIds The ids of the assets to replace the list for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void replace(String value, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		String queryForAssetIds
		Map params = [:]

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
		}

		params.value = value

		AssetEntity.executeUpdate("UPDATE AssetEntity a SET $field = :value WHERE a.id in($queryForAssetIds)", params)
	}


}
