package net.transitionmanager.service.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.domain.TagEvent
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ServiceMethods
import org.apache.tools.ant.taskdefs.Move

/**
 * A service for managing bulk operations on Assets for assigning moveBundles.
 */
@Transactional
class BulkChangeMoveBundleService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Coerces the string value passed from the BulkChangeService to a moveBundle for the current project.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param value The string value that need to be coerce.
	 *
	 * @return a moveBundle.
	 */
	MoveBundle coerceBulkValue(Project currentProject, String value) {
		Long id = NumberUtil.toPositiveLong(value)

		if (!id) {
			throw new InvalidParamException("Invaid move bundle id $value")
		}

		return get(MoveBundle, id, currentProject, false)
	}


	/**
	 * Replaces the current moveBundle with a new one.
	 *
	 * @param moveBundle to replace the current moveBundle for the assets.
	 * @param assetIds The ids of the assets to replace the moveBundle for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkReplace(MoveBundle moveBundle, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (!moveBundle) {
			throw new InvalidParamException("For bulk replace you need to specify a valid move bundle.")
		}

		String queryForAssetIds
		Map params = [:]

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
		}

		params['moveBundle'] = moveBundle

		AssetEntity.executeUpdate("UPDATE AssetEntity a SET moveBundle = :moveBundle WHERE a.id in($queryForAssetIds)", params)
	}


}
