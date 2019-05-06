package net.transitionmanager.tag

import net.transitionmanager.asset.AssetEntity
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.Tag
import net.transitionmanager.tag.TagAsset
import net.transitionmanager.tag.TagEvent
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
			return tagAsset.save()
		}

		// Bump the last updated date for the given asset.
		asset.lastUpdated = TimeUtil.nowGMT()
		asset.save()

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
		assetEntityService.bulkBumpAssetLastUpdated(currentProject, assetIds)
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

		List<TagEvent> tagEvents = TagEvent.findAllWhere(tag: secondary)

		//This will ignore nulls resulting from a duplicate key.
		tagEvents.findResults { TagEvent link ->
			link.tag = primary
			return link.save(flush: true, failOnError: false)
		}

		//removes the secondary tag, and by cascade any links, that would create a duplicate key.
		secondary.delete(flush: true)

		return updatedLinks
	}
}
