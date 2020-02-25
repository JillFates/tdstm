package net.transitionmanager.tag

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.tag.CreateCommand
import net.transitionmanager.command.tag.ListCommand
import net.transitionmanager.command.tag.SearchCommand
import net.transitionmanager.command.tag.UpdateCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class WsTagController implements ControllerMethods {
	TagService tagService

	@HasPermission(Permission.TagView)
	def list() {
		ListCommand filter = populateCommandObject(ListCommand)

		List<Map> tags = tagService.list(
			projectForWs,
			filter.name,
			filter.description,
			filter.dateCreated,
			filter.lastUpdated,
			filter.bundleId ?[filter.bundleId] : [],
			filter.eventId
		)

		renderSuccessJson(tags)
	}

	@HasPermission(Permission.TagView)
	def search() {
		SearchCommand filter = populateCommandObject(SearchCommand)

		List<Map> tags = tagService.list(
			projectForWs,
			filter.name,
			filter.description,
			filter.dateCreated,
			filter.lastUpdated,
			filter.bundleIds,
			filter.eventId
		)

		renderSuccessJson(tags)
	}

	@HasPermission(Permission.TagCreate)
	def create() {
		CreateCommand newTag = populateCommandObject(CreateCommand)

		Tag tag = tagService.create(projectForWs, newTag.name, newTag.description, newTag.color)

		renderSuccessJson(tag.toMap())

	}

	@HasPermission(Permission.TagEdit)
	def update(Long id) {
		UpdateCommand updatedTag = populateCommandObject(UpdateCommand)

		Tag tag = tagService.update(id, projectForWs, updatedTag.name, updatedTag.description, updatedTag.color)

		renderSuccessJson(tag.toMap())
	}

	@HasPermission(Permission.TagDelete)
	def delete(Long id) {
		tagService.delete(id, projectForWs)
		return renderSuccessJson()
	}
}
