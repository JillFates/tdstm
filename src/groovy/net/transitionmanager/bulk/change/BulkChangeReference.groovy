package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ServiceMethods

/**
 * A service for managing bulk operations on Assets for assigning moveBundles.
 */
@Transactional
class BulkChangeReference implements ServiceMethods {

	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['replace', 'clear']

	/**
	 * Coerces the string value passed from the BulkChangeService to a moveBundle for the current project.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param field the field that is used
	 * @param value The string value that need to be coerce.
	 * @param fieldMapping not used for moveBundle just here for the interface.
	 *
	 * @return a moveBundle.
	 */
	static Object coerceBulkValue(Project currentProject, String value, String fieldName, Class type) {
		Long id = NumberUtil.toPositiveLong(value)

		if (!id) {
			throw new InvalidParamException("Invaid move bundle id $value")
		}

		Class clazz = GormUtil.getDomainPropertyType(type, fieldName)
		Object referenceObject = GormUtil.findInProject(currentProject, clazz, id)

		return referenceObject

	}

	/**
	 * Replaces the current moveBundle with a new one.
	 *
	 * @param type the class to use in the query.
	 * @param moveBundle to replace the current moveBundle for the assets.
	 * @param ids The ids of the domain to replace the moveBundle for.
	 * @param idsFilterQuery filtering query to use if ids are not present.
	 */
	static void replace(Class type, Object object, String field, List<Long> ids = [], Map idsFilterQuery = null) {
		if (!object) {
			throw new InvalidParamException("For bulk replace you need to specify a valid $field.")
		}

		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsQuery(type, ids, idsFilterQuery, params)
		params[field] = object

		AssetEntity.executeUpdate("UPDATE  $type.simpleName a SET $field = :$field WHERE a.id in($queryForIds)", params)
	}

	/**
	 * Clears the current value from a list with null.
	 *
	 * @param type the class to use in the query.
	 * @param value the value which should be null for clear.
	 * @param field the list field to update
	 * @param ids The ids of the domain to clear the list for.
	 * @param idsFilterQuery filtering query to use if ids are not present.
	 */
	static void clear(Class type, Object object, String field, List<Long> ids = [], Map idsFilterQuery = null) {

		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsQuery(type, ids, idsFilterQuery, params)
		params[field] = null

		AssetEntity.executeUpdate("UPDATE  $type.simpleName a SET $field = :$field WHERE a.id in($queryForIds)", params)
	}

}
