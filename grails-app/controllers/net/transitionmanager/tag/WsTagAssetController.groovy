package net.transitionmanager.tag

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.IdsCommand
import net.transitionmanager.command.tag.CreateTagAssetCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TagAssetService

@Secured('isAuthenticated()')
class WsTagAssetController implements ControllerMethods {
	TagAssetService tagAssetService

	@HasPermission(Permission.TagView)
	def list(Long id) {
		renderSuccessJson(tagAssetService.list(projectForWs, id)*.toMap())
	}

	@HasPermission(Permission.AssetEdit)
	def create() {
		CreateTagAssetCommand newAssetTag = populateCommandObject(CreateTagAssetCommand)
		validateCommandObject(newAssetTag)
		List<TagAsset> tagAssets = tagAssetService.applyTags(projectForWs,newAssetTag.tagIds, newAssetTag?.assetId)

		renderSuccessJson(tagAssets*.toMap())

	}

	@HasPermission(Permission.AssetDelete)
	def merge(Long targetId, Long sourceId) {
		List<TagAsset> tagAssets = tagAssetService.merge(projectForWs, targetId, sourceId)

		renderSuccessJson(tagAssets*.toMap())
	}

	@HasPermission(Permission.AssetDelete)
	def delete() {
		IdsCommand delete = populateCommandObject(IdsCommand)
		validateCommandObject(delete)
		tagAssetService.removeTags(projectForWs, delete.ids)
		return renderSuccessJson()
	}
}
