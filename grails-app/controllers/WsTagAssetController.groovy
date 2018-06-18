import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.tag.CreateTagAssetCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TagAssetService

@Secured('isAuthenticated()')
class WsTagAssetController implements ControllerMethods {
	TagAssetService tagAssetService

	@HasPermission(Permission.TagView)
	def list(Long id) {
		renderAsJson(tagAssetService.list(projectForWs, id)*.toMap())
	}

	@HasPermission(Permission.TagCreate)
	def create() {
		CreateTagAssetCommand newAssetTag = populateCommandObject(CreateTagAssetCommand)

		if (newAssetTag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(newAssetTag)))
			return
		}

		List<TagAsset> tagAssets = tagAssetService.applyTags(projectForWs,newAssetTag.tagIds, newAssetTag?.assetId)

		renderSuccessJson(tagAssets*.toMap())

	}

	@HasPermission(Permission.TagEdit)
	def merge(Long primaryId, Long secondaryId) {
		List<TagAsset> tagAssets = tagAssetService.merge(projectForWs, primaryId, secondaryId)

		renderSuccessJson(tagAssets*.toMap())
	}

	@HasPermission(Permission.TagDelete)
	def delete(Long id) {
		tagAssetService.removeTags(projectForWs, [id])
		return renderSuccessJson()
	}
}
