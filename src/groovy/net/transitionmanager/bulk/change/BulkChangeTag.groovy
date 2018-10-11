package net.transitionmanager.bulk.change

import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.ServiceMethods

@Transactional
class BulkChangeTag {
	/**
	 * Actions that are allowed to be dynamically called by the Bulk Services.
	 */
	static final List<String> ALLOWED_ACTIONS = ['add', 'remove', 'replace', 'clear']

	/**
	 * Coerces the string value passed from the BulkChangeService to a List of longs, and validates them as tag ids.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param value The string value that need to be coerce.
	 *
	 * @return a List of longs, that represent tag ids.
	 */
	static List<Long> coerceBulkValue(Project currentProject, String value) {
		JsonSlurper jsonSlurper = new JsonSlurper()
		def parsedValue = jsonSlurper.parseText(value)

		if (!(parsedValue instanceof List)) {
			throw new InvalidParamException('Value is not a list of numbers')
		}

		List<Long> tagIds = (parsedValue).collect { i -> i.toLong() }

		if (tagIds) {
			validateBulkValues(currentProject, tagIds)
		}

		return tagIds
	}

	/**
	 * Validates the tagId sent in to make sure they are valid for the project, by looking them up.
	 *
	 * @param currentProject the current project passed down through the controller.
	 *
	 * @param tagIds the ids to validate.
	 */
	static void validateBulkValues(Project currentProject, List<Long> tagIds) {
		if (!tagIds) {
			return
		}

		int tagCount = Tag.where { id in tagIds && project == currentProject }.count()

		if (tagCount != tagIds.size()) {
			throw new InvalidParamException('One or more tags specified were not found')
		}
	}

	/**
	 * Bulk adds tags to assets.
	 *
	 * @param type the class to use in the query.
	 * @param tagIds The ids of the tags to add.
	 * @param ids The assets to add TagAssetLinks to. this can be an empty list meaning that the filtering query will be used instead.
	 * @param idsFilterQuery If assetIds are not specified  this query and params are added to the query to apply to all assets,
	 * from the filtering in the frontend.
	 */
	static void add(Class type, List<Long> tagIds, String field, List<Long> ids = [], Map idsFilterQuery = null) {
		if (!tagIds) {
			return
		}

		String queryForAssetIds
		Map params = [:]

		Map assetQueryParams = [:]

		if (ids && !idsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = ids
			assetQueryParams['assetIds'] = ids
		} else {
			queryForAssetIds = idsFilterQuery.query
			params << idsFilterQuery.params
			assetQueryParams = idsFilterQuery.params
		}

		params.tagIds = tagIds

		String query = """
			INSERT into TagAsset
			(asset, tag, dateCreated)
			SELECT
				a as asset,
				t as tag,
				current_date() as dateCreated
			FROM AssetEntity a, Tag t
			WHERE a.id in ($queryForAssetIds) AND t.id in (:tagIds)
			AND NOT EXISTS(FROM TagAsset ta WHERE ta.asset = a AND ta.tag = t)
		"""

		TagAsset.executeUpdate(query, params)
		// Bump the lastUpdated field for those assets for which tags were added.
		AssetEntityService assetEntityService = Holders.applicationContext.getBean('assetEntityService')
		SecurityService securityService = Holders.applicationContext.getBean('securityService')
		assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)
	}

	/**
	 * Bulk clears tags for assets.
	 *
	 * @param type the class to use in the query.
	 * @param tagIds should be null because it is not used. The parameter is specified so that all the bulk methods have the same signature.
	 * @param assetIds The ids of the assets to remove tags from
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present
	 */
	static void clear(Class type, List<Long> tagIds = null, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (tagIds) {
			throw new InvalidParamException("Specifying Tag IDs is invalid when clearing all tags")
		}

		bulkRemove(type, [], field, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk removes tags from assets.
	 *
	 * @param type the class to use in the query.
	 * @param tagIds The ids of the tags to be removed.
	 * @param assetIds The ids of the assets to remove tags from.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	static void remove(Class type, List<Long> tagIds, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (!tagIds) {
			throw new InvalidParamException("Tag IDs must be specified for removal")
		}

		bulkRemove(type, tagIds, field, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk removes tags from assets.
	 *
	 * @param type the class to use in the query.
	 * @param tagIds The tags to remove, is it's an empty list, all tags will be removed.
	 * @param assetIds The ids of the assets to remove tags from.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	private static void bulkRemove(Class type, List<Long> tagIds, String field, List<Long> assetIds, Map assetIdsFilterQuery = null) {
		String queryForAssetIds
		String queryForTagIds = ''
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

		if (tagIds) {
			queryForTagIds = 'and tag.id in(:tagIds)'
			params.tagIds = tagIds
		}

		String query = """
			delete from TagAsset
			where  asset.id in($queryForAssetIds) $queryForTagIds
		"""

		TagAsset.executeUpdate(query, params)

		// Bump the lastUpdated field on those assets that were affected by the remove operation.
		AssetEntityService assetEntityService = Holders.applicationContext.getBean('assetEntityService')
		SecurityService securityService = Holders.applicationContext.getBean('securityService')
		assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)
	}

	/**
	 * Replaces the current tags with new ones.
	 *
	 * @param type the class to use in the query.
	 * @param tagIds The tag ids to replace the current ones for the assets.
	 * @param assetIds The ids of the assets to replace tags for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	static void replace(Class type, List<Long> tagIds, String field, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		clear(type, [], field, assetIds, assetIdsFilterQuery)
		add(type, tagIds, field, assetIds, assetIdsFilterQuery)
	}
}
