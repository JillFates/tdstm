package net.transitionmanager.bulk.change

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.validators.CustomValidators
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ServiceMethods

/**
 * A service for managing bulk operations on Assets for assigning values from a list.
 */
@Transactional
class BulkChangeList implements ServiceMethods {

	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Coerces the string value passed from the BulkChangeService and validates it to be a value from a list.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param field the list field to update
	 * @param value The string value that needs to be coerce.
	 * @param fieldMapping the mapping to use to validate the value for custom fields. For standard fields there in an internal mapping to the
	 * values used to validate the value.
	 *
	 * @return a string, that is valid for a list.
	 */
	static String coerceBulkValue(Project currentProject, String value) {
		return value
	}

	/**
	 * A helper method to look up values used in validating standard fields.
	 *
	 * @param field the field to get the list of values used for validation.
	 *
	 * @return the list of values used for validation.
	 */
	static List getListValues(String field) {
		switch (field) {
			case 'validation':
				return ValidationType.list
			case 'railType':
				return AssetEntity.RAIL_TYPES
			case 'criticality':
				return Application.CRITICALITY
			case 'planStatus':
				return CustomValidators.optionsClosure(AssetOptions.AssetOptionsType.STATUS_OPTION)()
			default:
				throw new InvalidParamException("For Bulk update of list field $field is invalid.")
		}
	}

	/**
	 * Clears the current value from a list with ''.
	 *
	 * @param type the class to use in the query.
	 * @param value the value which should be null for clear.
	 * @param field the list field to update
	 * @param ids The ids of the domain to clear the list for.
	 * @param idsFilterQuery filtering query to use if ids are not present.
	 */
	static void clear(Class type, String value, String field, List<Long> ids = [], Map idsFilterQuery = null) {
		bulkReplace(type, '', field, ids, idsFilterQuery)
	}

	/**
	 * Replaces the current list value with a new one.
	 *
	 * @param type the class to use in the query.
	 * @param value to replace the current list value with a new one. The value can't be '' or null.
	 * @param field the list field to update
	 * @param ids The ids of the domain to replace the list for.
	 * @param idsFilterQuery filtering query to use if ids are not present.
	 */
	static void replace(Class type, String value, String field, List<Long> ids = [], Map idsFilterQuery = null) {
		if (!value) {
			throw new InvalidParamException("For bulk replace you need to specify a valid $field value.")
		}

		bulkReplace(type, value, field, ids, idsFilterQuery)
	}

	/**
	 * Replaces the current list value with a new one.
	 *
	 * @param type the class to use in the query.
	 * @param value to replace the current list value with a new one.
	 * @param field the list field to update
	 * @param ids The ids of the domains to replace the list for.
	 * @param idsFilterQuery filtering query to use if ids are not present.
	 */
	static void bulkReplace(Class type, String value, String field, List<Long> ids = [], Map idsFilterQuery = null) {
		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsquery(type, ids, idsFilterQuery, params)
		params.value = value

		AssetEntity.executeUpdate("UPDATE ${type.simpleName} a SET $field = :value WHERE a.id in($queryForIds)", params)
	}


}
