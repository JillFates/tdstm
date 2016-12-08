import com.github.icedrake.jsmaz.Smaz
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.service.license.LicenseService
import org.apache.commons.codec.binary.Base64

import java.text.ParseException

/**
 * Created by octavio on 11/30/16.
 */

@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
class WsLicenseManagerController implements ControllerMethods {
	LicenseService licenseService

	/* list the licenses */
	def getLicenses(){
		renderSuccessJson(LicensedClient.findAll()*.toJsonMap())
	}

	def getLicense(){
		def id = params.id
		def lic
		if(id) {
			lic = LicensedClient.get(id)
		}

		if(lic) {
			renderSuccessJson(lic.toJsonMap())
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}

	}

	def deleteLicense(){
		def id = params.id
		LicensedClient lic
		if(id) {
			lic = LicenseClient.get(id)
		}

		if(lic) {
			lic.delete()
			renderSuccessJson("Successful Deleted")
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}

	private fetchJsonToLicense(json, createIfNotFound = false){
		def dateParser = {String strDate ->
			if(strDate != null){
				try {
					return org.apache.tools.ant.util.DateUtils.parseIso8601DateTime(strDate)
				}catch(ParseException pe){
					log.error("Error Parsing Date", pe)
				}
			}
			return null
		}

		if(!json.id){
			return null
		}

		LicensedClient lc = LicensedClient.get(json.id)

		if(!lc && createIfNotFound) {
			lc = new LicensedClient()
		}

		if(!lc){
			return null
		}

		lc.id = json.id

		if(json.instalationNum != null) {
			lc.instalationNum = json.instalationNum
		}
		if(json.email != null) {
			lc.email = json.email
		}
		if(json.requestNote != null) {
			lc.requestNote = json.requestNote
		}
		if(json.expirationDate != null) {
			lc.expirationDate = dateParser(json.expirationDate)
		}
		if(json.activationDate != null) {
			log.info("set Activation DAte: {}", json.activationDate)
			lc.activationDate = dateParser(json.activationDate)
			log.info("set Activation DAte: {}", lc.activationDate)
		}
		if(json.requestDate != null) {
			lc.requestDate = dateParser(json.requestDate)
		}

		if(json.requestDate != null) {
			lc.environment = License.Environment.forId(json.environment?.id)
		}
		if(json.method?.id != null) {
			lc.method = License.Method.forId(json.method?.id)
		}
		if(json.method?.max != null) {
			lc.max = (json.method?.max) ?: 0
		}
		if(json.type?.id != null) {
			lc.type = License.Type.forId(json.type?.id)
		}
		if(json.status?.id != null) {
			lc.status = License.Status.forId(json.status?.id)
		}
		if(json.project != null) {
			lc.project = json.project?.toString()
		}
		if(json.client != null) {
			lc.client = json.client?.toString()
		}
		if(json.owner != null) {
			lc.owner = json.owner?.toString()
		}

		return lc
	}

	def updateLicense(){
		def id = params.id
		LicensedClient lic
		def json = request.JSON
		if(id) {
			json.id = id
			lic = fetchJsonToLicense(json)
		}

		if(lic) {
			lic.save()

			if(lic.hasErrors()){
				def errors = ""
				lic.errors.each {err->
					errors << "${err}/n"
				}
				response.status = 400
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
		//def body = request.reader.text
		def rjson = request.JSON
		def body = rjson?.data
		if(body){

			log.info("Body Before: {}", body)

			String beginTag = "-----BEGIN HASH-----"
			String endTag = "-----END HASH-----"
			def idxB = body.indexOf(beginTag)
			if(idxB >= 0){
				def idxE = body.indexOf(endTag)
				if(idxE < 0){
					response.status = 400
					render "Missing ${endTag} tag for request"
					return
				}
				body = body.substring(idxB + beginTag.length(), idxE)
				body = body.trim()
			}

			log.info("Body: {}", body)

			String decodedString = Smaz.decompress(Base64.decodeBase64(body))

			log.info("Decoded String:")
			log.info(decodedString)

			def json = grails.converters.JSON.parse(decodedString)

			LicensedClient lc = fetchJsonToLicense(json, true)
			lc.save()

			if(lc.hasErrors()){
				def errors = ""
				lc.errors.each {err->
					errors << "${err}/n"
				}
				response.status = 400
				render errors
			}else{
				renderSuccessJson(json)
			}

		}else{
			response.status = 400 //bad Request
			render "Wrong format on License Request"
		}
	}

	def getLicenseKey(){
		def id = params.id
		def lic = LicensedClient.get(id)

		if(lic) {
			String productKey = lic.id
			String holder  = lic.email
			String subject = lic.instalationNum

			Date validAfter  = lic.activationDate
			Date validBefore = lic.expirationDate

			int numberOfInstances = lic.max

			String licString = licenseService.generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)

			log.info("licString: {}", licString)
			renderSuccessJson(licString)
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}
}
