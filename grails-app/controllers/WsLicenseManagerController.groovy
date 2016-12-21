import com.github.icedrake.jsmaz.Smaz
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.service.LicenseManagerService
import org.apache.commons.codec.binary.Base64

/**
 * Created by octavio on 11/30/16.
 */

@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
class WsLicenseManagerController implements ControllerMethods {
	LicenseManagerService licenseManagerService

	private renderIfNotNull(id, obj){
		if(obj) {
			renderSuccessJson(obj)
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}

	/* list the licenses */
	def getLicenses(){
		renderSuccessJson(licenseManagerService.list()*.toJsonMap())
	}

	def getLicense(){
		def id = params.id
		LicensedClient lic = licenseManagerService.fetch(id)

		renderIfNotNull(id, lic?.toJsonMap())
	}

	def deleteLicense(){
		def id = params.id
		LicensedClient lic = licenseManagerService.delete(id)

		renderIfNotNull(id, lic ? "successful deleted" : null)
	}

	def activateLicense(){
		def id = params.id
		LicensedClient lic = licenseManagerService.fetch(id)

		renderIfNotNull(id, {
			if(lic){
				//TODO: send the email
				return "Email sent"
			}
		}())
	}

	def updateLicense(){
		def id = params.id
		LicensedClient lic
		def json = request.JSON
		if(id) {
			json.id = id
			lic = licenseManagerService.fetchJsonToLicense(json)
		}

		if(lic) {
			lic.save()
			if(lic.hasErrors()){
				def errors = ""
				log.info("da Error {}", lic.errors)
				lic.errors.each {err->
					errors << "${err}/n"
				}
				response.status = 400
				log.info("Errors {}", errors)
				render errors
			}else{
				renderSuccessJson("saved")
			}
			log.info("aggghhh")
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}

	def loadRequest(){
		//def body = request.reader.text
		def rjson = request.JSON
		def body = rjson?.data
		if(body){

			log.info("Body Before: {}", body)
			LicensedClient lc = licenseManagerService.loadRequest(body)
			lc.save()

			if(lc.hasErrors()){
				def errors = ""
				lc.errors.each {err->
					errors << "${err}/n"
				}
				response.status = 400
				render errors
			}else{
				renderSuccessJson(lc.toJsonMap())
			}

		}else{
			response.status = 400 //bad Request
			render "Wrong format on License Request"
		}
	}

	def getLicenseKey(){
		def id = params.id

		renderIfNotNull(
			id,
			licenseManagerService.getLicenseKey(id)
		)
	}
}
