package net.transitionmanager.service

import com.tds.asset.AssetEntity
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
	TagService tagService

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

		tagIds.collect { Long tagId ->
			Tag tag = get(Tag, tagId, currentProject)

			TagAsset tagAsset = new TagAsset(tag: tag, asset: asset)
			asset.refresh()
			return tagAsset.save(failOnError: true)
		}
	}

	/**
	 * Removes a tagAssets by id.
	 *
	 * @param tagAssetIds the id of the tagAsset to remove.
	 */
	void removeTags(Project currentProject, List<Long> tagAssetIds) {
		tagAssetIds.each { Long id ->
			TagAsset tagAsset = get(TagAsset, id, currentProject)
			if (currentProject.id != tagAsset?.tag?.project?.id) {
				securityService.reportViolation("attempted to access asset from unrelated project (asset ${tagAsset?.id})")
				throw new EmptyResultException()
			}

			tagAsset.delete(flush: true)
		}
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
	def coerceBulkValue(Project currentProject, String value) {
		JsonSlurper jsonSlurper = new JsonSlurper()
		def parsedValue = jsonSlurper.parseText(value)
		if(!(parsedValue instanceof List<Integer>)){
			throw new InvalidParamException('Value is not a list of numbers.')
		}

		List<Long> tagIds = ((List<Integer>) parsedValue).collect { Integer i -> i.toLong() }
		validateBulkValues(currentProject, tagIds)

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
		tagIds.each { Long id ->
			tagService.get(Tag, id, currentProject)
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

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
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
	}

	/**
	 * Bulk removes tags from assets.
	 *
	 * @param tagIds The tags to remove, is it's an empty list, all tags will be removed.
	 * @param assetIds The ids of the assets to remove tags from.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkRemove(List<Long> tagIds = [], List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		String queryForAssetIds
		String queryForTagIds = ''
		Map params = [:]

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
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
	}

	/**
	 * Replaces the current tags with new ones.
	 *
	 * @param tagIds The tag ids to replace the current ones for the assets.
	 * @param assetIds The ids of the assets to replace tags for.
	 * @param assetIdsFilterQuery filtering query to use if assetIds are not present.
	 */
	void bulkReplace(List<Long> tagIds, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		bulkRemove([], assetIds, assetIdsFilterQuery)
		bulkAdd(tagIds, assetIds, assetIdsFilterQuery)
	}
}
