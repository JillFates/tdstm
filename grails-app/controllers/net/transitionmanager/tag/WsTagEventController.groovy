package net.transitionmanager.tag

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.IdsCommand
import net.transitionmanager.command.tag.CreateTagEventCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class WsTagEventController implements ControllerMethods {
	TagEventService tagEventService

	@HasPermission(Permission.TagView)
	def list(Long id) {
		renderSuccessJson(tagEventService.list(projectForWs, id)*.toMap())
	}

	@HasPermission(Permission.TagCreate)
	def create() {
		CreateTagEventCommand newEventTag = populateCommandObject(CreateTagEventCommand)
		validateCommandObject(newEventTag)
		List<TagAsset> tagEvents = tagEventService.applyTags(projectForWs,newEventTag.tagIds, newEventTag?.eventId)

		renderSuccessJson(tagEvents*.toMap())

	}

	@HasPermission(Permission.TagDelete)
	def delete() {
		IdsCommand delete = populateCommandObject(IdsCommand)
		validateCommandObject(delete)
		tagEventService.removeTags(projectForWs, delete.ids)
		return renderSuccessJson()
	}
}
