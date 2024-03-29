package net.transitionmanager.license

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project

@Secured('isAuthenticated()')
class LicenseController implements ControllerMethods {

	// IOC
	LicenseAdminService licenseAdminService

	/**
	 * Returns the licensing information for the project (id) or the user's current project if null
	 * @param id - the project id to get licensing for (defaults to user's current id)
	 * @return JSON structure
	 */
	def info() {
		Project project = getProjectForWs()
		Map licenseInfo = licenseAdminService.licenseInfo(project)

		renderSuccessJson(license: licenseInfo)
	}
}