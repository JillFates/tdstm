import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.tag.CreateTagAssetCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TagAssetService
import net.transitionmanager.service.TagService

@Secured('isAuthenticated()')
class WsTagAssetController implements ControllerMethods {
	TagAssetService tagAssetService
	TagService      tagService

	@HasPermission(Permission.TagView)
	def list(Long id) {
		AssetEntity asset = AssetEntity.get(id)

		if (!asset) {
			return sendNotFound('Asset not found')
		}

		renderAsJson(tagAssetService.list(asset)*.toMap())
	}

	@HasPermission(Permission.TagCreate)
	def create() {
		CreateTagAssetCommand newAssetTag = populateCommandObject(CreateTagAssetCommand)

		if (newAssetTag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(newAssetTag)))
			return
		}

		AssetEntity asset = AssetEntity.get(newAssetTag?.assetId)

		if (!asset) {
			return sendNotFound('Asset not found')
		}

		List<TagAsset> tagAssets = tagAssetService.applyTags(newAssetTag.tagIds, asset)

		renderSuccessJson(tagAssets*.toMap())

	}

	@HasPermission(Permission.TagEdit)
	def merge(Long primaryId, Long secondaryId) {
		Tag primaryTag = tagService.get(primaryId)
		Tag secondaryTag = tagService.get(secondaryId)

		if (!primaryTag) {
			return sendNotFound('Missing primary tag.')
		}

		if (!secondaryTag) {
			return sendNotFound('Missing secondary tag.')
		}

		List<TagAsset> tagAssets = tagAssetService.merge(primaryTag, secondaryTag)

		renderSuccessJson(tagAssets*.toMap())
	}

	@HasPermission(Permission.TagDelete)
	def delete(Long id) {
		TagAsset tagAsset = tagAssetService.get(id)

		if (tagAsset) {
			tagAssetService.removeTags([id])
			return renderSuccessJson()
		} else {
			return sendNotFound()
		}
	}
}
