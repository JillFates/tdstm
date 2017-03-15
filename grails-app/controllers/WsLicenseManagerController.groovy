import com.tdsops.common.exceptions.InvalidLicenseException
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.service.LicenseManagerService

/**
 * Created by octavio on 11/30/16.
 */

@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
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

	def updateLicense(){
		def id = params.id
		LicensedClient lic
		def json = request.JSON
		if(id) {
			json.id = id
			lic = LicensedClient.fetch(json)
		}

		if(lic) {
			lic.save()
			if(lic.hasErrors()){
				def errors = ""
				log.debug("da Error {}", lic.errors)
				lic.errors.each {err->
					errors << "${err}/n"
				}
				response.status = 400
				log.debug("Errors {}", errors)
				render errors
			}else{
				renderSuccessJson("saved")
			}
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
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
