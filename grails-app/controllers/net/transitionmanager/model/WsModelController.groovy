package net.transitionmanager.model

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.ModelService
import net.transitionmanager.command.ModelCommand
import net.transitionmanager.command.model.ExportMgrAndModelCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService

@Secured("isAuthenticated()")
class WsModelController implements ControllerMethods {

	ModelService modelService
	SecurityService securityService

	@HasPermission(Permission.ModelCreate)
	def save() {
		ModelCommand modelCommand = populateCommandObject(ModelCommand)
		Project project = securityService.userCurrentProject
		modelService.createOrUpdateModel(project, modelCommand)
		renderSuccessJson()
	}

	@HasPermission(Permission.ModelEdit)
	def update() {
		ModelCommand modelCommand = populateCommandObject(ModelCommand)
		Project project = securityService.userCurrentProject
		modelService.createOrUpdateModel(project, modelCommand)
		renderSuccessJson()
	}

	def delete(Long id) {
		Model model = fetchDomain(Model, [id: id])
		modelService.delete(model)
		renderSuccessJson()
	}

	/**
	 * Fetch the Models, Manufacturers and Connectors for the export process.
	 * @return
	 */
	@HasPermission(Permission.ModelExport)
	def export() {
		ExportMgrAndModelCommand command = populateCommandObject(ExportMgrAndModelCommand)
		Map exportData = modelService.getManufacturersAndModelsExportData(command)
		render view: '/model/exportManufacturersAndModels', model: [data: exportData]
	}

}
