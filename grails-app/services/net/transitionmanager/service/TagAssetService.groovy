package net.transitionmanager.service

import com.tds.asset.AssetEntity
import grails.transaction.Transactional
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset

/**
 * A service for managing the relationship of Tags to Assets.
 */
@Transactional
class TagAssetService {

	TagService      tagService
	SecurityService securityService

	/**
	 * Retrieves a tagAssets by id
	 *
	 * @param tagAssetId The id of the tagAsset to lookup
	 *
	 * @return The looked up tagAsset
	 */
	@Transactional(readOnly = true)
	TagAsset get(Long tagAssetId) {
		return TagAsset.get(tagAssetId)
	}

	/**
	 * Gets a list of tagAssets for an asset.
	 *
	 * @param asset the asset to get tagAssets for.
	 *
	 * @return A list of tagAssets for an asset.
	 */
	@Transactional(readOnly = true)
	List<TagAsset> list(AssetEntity asset) {
		securityService.assertCurrentProject(asset?.project)

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
	List<Tag> getTags(AssetEntity asset) {
		securityService.assertCurrentProject(asset?.project)

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
	List<TagAsset> applyTags(List<Long> tagIds, AssetEntity asset) {
		securityService.assertCurrentProject(asset?.project)

		tagIds.collect { Long tagId ->
			Tag tag = tagService.get(tagId)
			securityService.assertCurrentProject(tag?.project)

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
	void removeTags(List<Long> tagAssetIds) {
		tagAssetIds.each { Long id ->
			TagAsset link = get(id)

			if (!link) {
				throw new EmptyResultException('Tag not found, or already removed.')
			}

			securityService.assertCurrentProject(link?.tag?.project)

			link.delete(flush: true)
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
	List<TagAsset> merge(Tag primary, Tag secondary) {
		securityService.assertCurrentProject(primary?.project)
		securityService.assertCurrentProject(secondary?.project)

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
