package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ServiceMethods

/**
 * A service for managing bulk operations on Assets for assigning moveBundles.
 */
@Transactional
class BulkChangeMoveBundle implements ServiceMethods {

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
	static MoveBundle coerceBulkValue(Project currentProject, String field, String value, Map fieldMapping) {
		Long id = NumberUtil.toPositiveLong(value)

		if (!id) {
			throw new InvalidParamException("Invaid move bundle id $value")
		}

		//Can't use the get method from the service methods, because, it and its dependents are not static.
		MoveBundle moveBundle = MoveBundle.findByIdAndProject(id, currentProject)

		if (moveBundle) {
			return moveBundle
		}

		throw new EmptyResultException()
	}

	/**
	 * Replaces the current moveBundle with a new one.
	 *
	 * @param type the class to use in the query.
	 * @param moveBundle to replace the current moveBundle for the assets.
	 * @param ids The ids of the domain to replace the moveBundle for.
	 * @param idsFilterQuery filtering query to use if ids are not present.
	 */
	static void replace(Class type, MoveBundle moveBundle, String field, List<Long> ids = [], Map idsFilterQuery = null) {
		if (!moveBundle) {
			throw new InvalidParamException("For bulk replace you need to specify a valid move bundle.")
		}

		Map params = [:]
		String queryForIds = BulkChangeUtil.getIdsquery(type, ids, idsFilterQuery, params)
		params['moveBundle'] = moveBundle

		AssetEntity.executeUpdate("UPDATE  $type.simpleName a SET $field = :moveBundle WHERE a.id in($queryForIds)", params)
	}


}
