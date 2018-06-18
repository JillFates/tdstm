package net.transitionmanager.service

import com.tds.asset.AssetEntity
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset

/**
 * A service for managing the relationship of Tags to Assets.
 */
@Transactional
class TagAssetService implements ServiceMethods {

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
		AssetEntity asset = get(AssetEntity, assetId, currentProject)

		return TagAsset.findAllWhere(asset: asset)*.tag
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
	List<TagAsset> merge(Project currentProject, Long primaryId, Long secondaryId) {
		Tag primary = get(Tag, primaryId, currentProject)
		Tag secondary = get(Tag, secondaryId, currentProject)

		List<TagAsset> tagAssets = TagAsset.findAllWhere(tag: secondary)

		//This will ignore nulls resulting from a duplicate key.
		List<TagAsset> updatedLinks = tagAssets.findResults { TagAsset link ->
			link.tag = primary
			return link.save(flush: true)
		}

		//removes the secondary tag, and by cascade any links, that would create a duplicate key.
		secondary.delete()

		return updatedLinks
	}
}
