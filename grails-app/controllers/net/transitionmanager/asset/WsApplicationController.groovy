package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApplicationService
import net.transitionmanager.service.QzSignService
/**
 * Handles WS calls of the ApplicationService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
class WsApplicationController implements ControllerMethods {

	ApplicationService applicationService
	QzSignService qzSignService

	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then it returns all unassigned
	 * applications for the user's current project
	 */
	@HasPermission(Permission.AssetView)
	def listInBundle() {
		renderSuccessJson(list: applicationService.listInBundle(params.id))
	}

	/**
	 * Serves the Certificate file for the QZTray
	 * @return
	 */
	def qzCertificate() {
		def file = qzSignService.findCertificateFile()
		render(file: file, contentType:'text/plain' , fileName: "qz-certificate")
	}
}
