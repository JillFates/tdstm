import com.github.icedrake.jsmaz.Smaz
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.service.license.LicenseService
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang.time.DateUtils

import javax.swing.text.DateFormatter
import java.text.ParseException
import java.text.SimpleDateFormat

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

	def loadRequest(){
		//def body = request.reader.text
		def rjson = request.JSON
		def body = rjson?.data
		if(body){

			def dateParser = {String strDate ->
				if(strDate != null){

					try {
						return DateUtils.parseDate(strDate, "yyyy-MM-dd'T'HH:mm:ssZ")
					}catch(ParseException pe){}
				}

				return null
			}

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

			LicensedClient lc = new LicensedClient()

			lc.id = json.id
			lc.instalationNum = json.instalationNum
			lc.email = json.email
			lc.requestNote = json.requestNote

			lc.expirationDate = dateParser(json.expirationDate)
			lc.activationDate = dateParser(json.activationDate)
			lc.requestDate = dateParser(json.requestDate)

			lc.environment = License.Environment.forId(json.environment?.id)
			lc.method = License.Method.forId(json.method?.id)
			lc.max = (json.method?.max)?:0
			lc.type = License.Type.forId(json.type?.id)
			lc.status = License.Status.forId(json.status?.id)
			lc.project = json.project?.toString()
			lc.client = json.client?.toString()
			lc.owner = json.owner?.toString()

			lc.save()

			if(lc.hasErrors()){
				def errors = ""
				lc.errors.each {err->
					errors += "${err}/n";
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
			String holder = lic.email
			String subject = lic.instalationNum

			Date validAfter = lic.requestDate //lic.activationDate
			Date validBefore  = lic.requestDate //lic.expirationDate

			//Date validAfter = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.activationDate)
			//Date validBefore  = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.expirationDate)
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
