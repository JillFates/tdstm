package net.transitionmanager.tag

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.tag.CreateCommand
import net.transitionmanager.command.tag.ListCommand
import net.transitionmanager.command.tag.SearchCommand
import net.transitionmanager.command.tag.UpdateCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.TagService

@Secured('isAuthenticated()')
class WsTagController implements ControllerMethods {
	TagService tagService

	@HasPermission(Permission.TagView)
	def list() {
		ListCommand filter = populateCommandObject(ListCommand)

		if (filter.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(filter)))
			return
		}

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

		if (filter.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(filter)))
			return
		}

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

		if (newTag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(newTag)))
			return
		}

		Tag tag = tagService.create(projectForWs, newTag.name, newTag.description, newTag.color)

		renderSuccessJson(tag.toMap())

	}

	@HasPermission(Permission.TagEdit)
	def update(Long id) {
		UpdateCommand updatedTag = populateCommandObject(UpdateCommand)

		if (updatedTag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(updatedTag)))
			return
		}

		Tag tag = tagService.update(id, projectForWs, updatedTag.name, updatedTag.description, updatedTag.color)

		renderSuccessJson(tag.toMap())
	}

	@HasPermission(Permission.TagDelete)
	def delete(Long id) {
		tagService.delete(id, projectForWs)
		return renderSuccessJson()
	}
}
