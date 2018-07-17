import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.tag.CreateTagAssetCommand
import net.transitionmanager.command.tag.DeleteTagAssetCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.TagAsset
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TagAssetService

@Secured('isAuthenticated()')
class WsTagAssetController implements ControllerMethods {
	TagAssetService tagAssetService

	@HasPermission(Permission.TagView)
	def list(Long id) {
		renderSuccessJson(tagAssetService.list(projectForWs, id)*.toMap())
	}

	@HasPermission(Permission.TagCreate)
	def create() {
		CreateTagAssetCommand newAssetTag = populateCommandObject(CreateTagAssetCommand)
		validateCommandObject(newAssetTag)
		List<TagAsset> tagAssets = tagAssetService.applyTags(projectForWs,newAssetTag.tagIds, newAssetTag?.assetId)

		renderSuccessJson(tagAssets*.toMap())

	}

	@HasPermission(Permission.TagDelete)
	def merge(Long targetId, Long sourceId) {
		List<TagAsset> tagAssets = tagAssetService.merge(projectForWs, targetId, sourceId)

		renderSuccessJson(tagAssets*.toMap())
	}

	@HasPermission(Permission.TagDelete)
	def delete(List<Long> ids) {
		DeleteTagAssetCommand delete = populateCommandObject(DeleteTagAssetCommand)
		validateCommandObject(delete)
		tagAssetService.removeTags(projectForWs, delete.ids)
		return renderSuccessJson()
	}
}
