package net.transitionmanager.model

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.ModelService
import net.transitionmanager.command.ModelCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService

@Secured("isAuthenticated()")
class WsModelController implements ControllerMethods {

	ModelService modelService
	SecurityService securityService

	@HasPermission(Permission.ModelEdit)
	def save() {
		ModelCommand modelCommand = populateCommandObject(ModelCommand, false)
		Project project = securityService.userCurrentProject
		modelService.createOrUpdateModel(project, modelCommand)
		renderSuccessJson()
	}

	def delete(Long id) {
		Model model = fetchDomain(Model, [id: id])
		modelService.delete(model)
		renderSuccessJson()
	}

}
