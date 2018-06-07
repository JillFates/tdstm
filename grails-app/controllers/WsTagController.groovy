import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.tag.CreateCommand
import net.transitionmanager.command.tag.ListCommand
import net.transitionmanager.command.tag.UpdateCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Tag
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

		renderAsJson(tagService.list(filter.name, filter.description, filter.dateCreated, filter.lastUpdated))
	}

	@HasPermission(Permission.TagCreate)
	def create() {
		CreateCommand newTag = populateCommandObject(CreateCommand)

		if (newTag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(newTag)))
			return
		}

		Tag tag = tagService.create(newTag.name, newTag.description, newTag.color)

		if (tag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(tag)))
			return
		}

		renderSuccessJson(tag.toMap())

	}

	@HasPermission(Permission.TagEdit)
	def update(Long id) {
		UpdateCommand updatedTag = populateCommandObject(UpdateCommand)

		if (updatedTag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(updatedTag)))
			return
		}

		Tag tag = tagService.get(id)

		if (!tag) {
			return sendNotFound()
		}

		tag = tagService.update(tag, updatedTag.name, updatedTag.description, updatedTag.color)

		if (tag.hasErrors()) {
			sendInvalidInput(renderAsJson(GormUtil.validateErrorsI18n(tag)))
			return
		}

		renderSuccessJson(tag.toMap())
	}

	@HasPermission(Permission.TagDelete)
	def delete(Long id) {
		Tag tag = tagService.get(id)

		if (tag) {
			tagService.delete(tag)
			return renderSuccessJson()
		} else {
			return sendNotFound()
		}
	}
}
