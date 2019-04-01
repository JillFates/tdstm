package net.transitionmanager.license

import com.tdsops.common.exceptions.InvalidLicenseException
import com.tdsops.common.exceptions.ServiceException
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.service.LicenseManagerService
/**
 * Created by octavio on 11/30/16.
 */

@Secured('isAuthenticated()')
class WsLicenseManagerController implements ControllerMethods {
	LicenseManagerService licenseManagerService

	private renderIfNotNull(id, Closure closure){
		def obj
		try{
			obj = closure()
			if(obj != null) {
				renderSuccessJson(obj)
			}else{
				response.status = 404 //Not Found
				render "${id} not found."
			}
		}catch(InvalidLicenseException ex){
			renderAsJson([status:"error", "data":ex.message])
		}
	}

	def getLicenseManagerEnabled() {
		renderSuccessJson(licenseManagerService.isManagerEnabled())
	}

	/* list the licenses */
	def getLicenses(){
		renderSuccessJson(licenseManagerService.list()*.toMap())
	}

	def getLicense(String id){
		renderIfNotNull(id){
			LicensedClient lic = licenseManagerService.fetch(id)
			lic?.toMap()
		}
	}

	def deleteLicense(String id){
		renderIfNotNull(id){
			LicensedClient lic = licenseManagerService.delete(id)
			lic ? "successful deleted" : null
		}
	}

	def revokeLicense(String id){
		renderIfNotNull(id){
			LicensedClient lic = licenseManagerService.revoke(id)
			lic ? "successful deleted" : null
		}
	}

	def activateLicense(String id){
		renderIfNotNull(id){
			licenseManagerService.activate(id)
		}
	}

	def activityLog(String id){
		renderIfNotNull(id) {
			licenseManagerService.activityLog(id).collect{
				[
						author: [
								username: it.userLogin.username,
								personName: it.userLogin?.person.toString()
						],
						dateCreated: it.dateCreated,
						changes: it.changesList
				]
			}
		}
	}

	/**
	 * Updates the license definition
	 * In case of problems throws a REST response (using 40x messages)
	 * @return
	 */
	def updateLicense(){
		try {
			licenseManagerService.updateLicense(params.id, request.JSON)
			renderSuccessJson("saved")
		} catch (ServiceException e) {
			response.status = 400
			throw e
		} catch (FileNotFoundException nfe) {
			response.status = 404
			throw nfe
		}
	}

	def loadRequest(){
		def rjson = request.JSON
		def body = rjson?.data
		if(body){
			LicensedClient lc = licenseManagerService.loadRequest(body)

			if(lc.hasErrors()){
				def errors = ""
				lc.errors.each {err->
					errors << "${err}/n"
				}
				response.status = 400
				render errors
			}else{
				renderSuccessJson(lc.toMap())
			}
		}else{
			response.status = 400 //bad Request
			render "Wrong format on License Request"
		}
	}

	def getLicenseKey(String id){
		renderIfNotNull(id) {
			licenseManagerService.getLicenseKey(id)
		}
	}

	def emailLicense(String id){
		if(licenseManagerService.emailLicense(id)){
			renderSuccessJson("Ok")
		}else{
			renderFailureJson("Email could not be sent")
		}
	}
}
