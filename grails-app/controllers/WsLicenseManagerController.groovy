import com.github.icedrake.jsmaz.Smaz
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.service.license.LicenseService
import org.apache.commons.codec.binary.Base64

/**
 * Created by octavio on 11/30/16.
 */

@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
class WsLicenseManagerController implements ControllerMethods {
	LicenseService licenseService

	def loadRequest(){
		def body = request.reader.text

		if(body){
			String decodedString = Smaz.decompress(Base64.decodeBase64(body))

			log.info("Decoded String:")
			log.info(decodedString)

			def json = grails.converters.JSON.parse(decodedString)

			License lc = new License()
			//LicensedClient lc = new LicensedClient()
			//lc.id = json.id
			//lc.requestDate = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.requestDate)
			//lc.email = json.email
			//lc.
			/*
			String productKey = json.id
			String holder = json.email
			String subject = json.instalationNum


			Date validAfter = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.activationDate)
			Date validBefore  = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.expirationDate)
			int numberOfInstances = json.max

			String lic = licenseService.generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)
			*/
			renderSuccessJson(json)
		}else{
			response.status = 400 //bad Request
			render "Wrong format on License Request"
		}
	}
}
