package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset

/**
 * A service for managing the relationship of Tags to Assets.
 */
@Transactional
class TagAssetService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Gets a list of tagAssets for an asset.
	 *
	 * @param asset the asset to get tagAssets for.
	 *
	 * @return A list of tagAssets for an asset.
	 */
	@Transactional(readOnly = true)
	List<TagAsset> list(Project currentProject, Long assetId) {
		AssetEntity asset = get(AssetEntity, assetId, currentProject)

		return TagAsset.findAllWhere(asset: asset)
	}

	/**
	 * Gets a list of tags for an asset.
	 *
	 * @param asset the asset to get tags for.
	 *
	 * @return A list of tags for an asset.
	 */
	@Transactional(readOnly = true)
	List<Tag> getTags(Project currentProject, Long assetId) {
		AssetEntity assetToLookUp = get(AssetEntity, assetId, currentProject)

		return TagAsset.where {
			asset == assetToLookUp
			projections {
				property("tag")
			}
		}.list(sort: "id", order: "asc")
	}

	/**
	 * Creates tagAssets for a list of tags, to an asset, and returns the list of tagAssets.
	 *
	 * @param tagIds The ids of the tags to apply create tagAssets for the asset.
	 * @param asset The asset to make tagAssets for.
	 *
	 * @return A List of tagAssets linking tags, to assets.
	 */
	List<TagAsset> applyTags(Project currentProject, List<Long> tagIds, Long assetId) {
		AssetEntity asset = get(AssetEntity, assetId, currentProject)

		List<TagAsset> tagAssets = tagIds.collect { Long tagId ->
			Tag tag = get(Tag, tagId, currentProject)

			TagAsset tagAsset = new TagAsset(tag: tag, asset: asset)
			asset.refresh()
			return tagAsset.save(failOnError: true)
		}

		// Bump the last updated date for the given asset.
		asset.lastUpdated = TimeUtil.nowGMT()
		asset.save(failOnError: true)

		return tagAssets
	}

	/**
	 * Removes a tagAssets by id.
	 *
	 * @param tagAssetIds the id of the tagAsset to remove.
	 */
	void removeTags(Project currentProject, List<Long> tagAssetIds) {
		Set<Long> assetIds = []
		tagAssetIds.each { Long id ->
			TagAsset tagAsset = get(TagAsset, id, currentProject)
			if (currentProject.id != tagAsset?.tag?.project?.id) {
				securityService.reportViolation("attempted to access asset from unrelated project (asset ${tagAsset?.id})")
				throw new EmptyResultException()
			}
			assetIds << tagAsset.asset.id
			tagAsset.delete(flush: true)
		}

		// Bump the last updated date for all the assets involved.
		assetEntityService.bulkBumpAssetLastUpdated(currentProject, ":assetIds", [assetIds: assetIds])
	}

	/**
	 * Merges a secondary tag into The primary one, and returns the updated links.
	 * All assetTags that would create a duplicate key will be removed by cascade, once the secondary tag is deleted.
	 *
	 * @param primary The tag used to overwrite the secondary tag, in the TagAssets.
	 * @param secondary The tag to be overwritten by the primary tag, in the TagAssets.
	 *
	 * @return A list of AssetTags that have been updated.
	 */
	List<TagAsset> merge(Project currentProject, Long targetId, Long sourceId) {
		Tag primary = get(Tag, targetId, currentProject)
		Tag secondary = get(Tag, sourceId, currentProject)

		List<TagAsset> tagAssets = TagAsset.findAllWhere(tag: secondary)

		//This will ignore nulls resulting from a duplicate key.
		List<TagAsset> updatedLinks = tagAssets.findResults { TagAsset link ->
			link.tag = primary
			return link.save(flush: true, failOnError: false)
		}

		//removes the secondary tag, and by cascade any links, that would create a duplicate key.
		secondary.delete(failOnError:true)

		return updatedLinks
	}

	/**
	 * Coerces the string value passed from the BulkChangeService to a List of longs, and validates them as tag ids.
	 *
	 * @param currentProject the current project passed from the controller for use in validating the tag ids.
	 * @param value The string value that need to be coerce.
	 *
	 * @return a List of longs, that represent tag ids.
	 */
	List<Long> coerceBulkValue(Project currentProject, String value) {
		JsonSlurper jsonSlurper = new JsonSlurper()
		def parsedValue = jsonSlurper.parseText(value)

		if(!(parsedValue instanceof List)){
			throw new InvalidParamException('Value is not a list of numbers')
		}

		List<Long> tagIds = (parsedValue).collect { i -> i.toLong() }

		if(tagIds) {
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
	void validateBulkValues(Project currentProject, List<Long> tagIds) {
		int tagCount = Tag.where {id in tagIds && project == currentProject}.count()

		if(tagCount != tagIds.size()){
			throw new InvalidParamException('One or more tags specified were not found')
		}
	}

	/**
	 * Bulk adds tags to assets.
	 *
	 * @param tagIds The ids of the tags to add.
	 * @param assetIds The assets to add TagAssetLinks to. this can be an empty list meaning that the filtering query will be used instead.
	 * @param assetIdsFilterQuery If assetIds are not specified  this query and params are added to the query to apply to all assets,
	 * from the filtering in the frontend.
	 */
	void bulkAdd(List<Long> tagIds, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		String queryForAssetIds
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
		assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)
	}

	/**
	 * Bulk clears tags for assets.
	 *
	 * @param tagIds should be null because it is not used. The parameter is specified so that all the bulk methods have the same signature.
	 * @param assetIds The ids of the assets to remove tags from
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present
	 */
	void bulkClear(List<Long> tagIds = null, List<Long> assetIds = [], Map assetIdsFilterQuery = null){
		if (tagIds) {
			throw InvalidParamException("Specifying Tag IDs is invalid when clearing all tags")
		}

		remove([], assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk removes tags from assets.
	 *
	 * @param tagIds The ids of the tags to be removed.
	 * @param assetIds The ids of the assets to remove tags from.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkRemove(List<Long> tagIds, List<Long> assetIds = [], Map assetIdsFilterQuery = null){
		if(!tagIds){
			throw InvalidParamException("Tag IDs must be specified for removal")
		}

		remove(tagIds, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk removes tags from assets.
	 *
	 * @param tagIds The tags to remove, is it's an empty list, all tags will be removed.
	 * @param assetIds The ids of the assets to remove tags from.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	private void remove(List<Long> tagIds, List<Long> assetIds, Map assetIdsFilterQuery = null) {
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
		assetEntityService.bulkBumpAssetLastUpdated(securityService.userCurrentProject, queryForAssetIds, assetQueryParams)
	}

	/**
	 * Replaces the current tags with new ones.
	 *
	 * @param tagIds The tag ids to replace the current ones for the assets.
	 * @param assetIds The ids of the assets to replace tags for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkReplace(List<Long> tagIds, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		bulkClear([], assetIds, assetIdsFilterQuery)
		bulkAdd(tagIds, assetIds, assetIdsFilterQuery)
	}
}
