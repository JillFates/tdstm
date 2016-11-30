import com.github.icedrake.jsmaz.Smaz
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.license.LicenseService
import groovy.json.JsonBuilder
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.time.DateUtils
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient

//Client Controller
class LicenseManagerController {

	LicenseService  licenseService
	SecurityService securityService
	ProjectService projectService

	/**
	 * forward to Manager
	 * @return
	 */
	def index() {
		redirect action:"manager"
	}

	/**
	 * Manager of all licenses
	 * @return
	 */
	def manager(){
		def licenses = LicensedClient.findAll()
		[licenses:licenses]
	}

	def loadRequest(){
		def request = params.request
		if(request){
			String decodedString = Smaz.decompress(Base64.decodeBase64(request))

			log.info("Decoded String:")
			log.info(decodedString)

			def json = grails.converters.JSON.parse(decodedString)

			LicensedClient lc = new LicensedClient()
			lc.id = json.id
			lc.requestDate = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.requestDate)
			lc.email = json.email
			//lc.
			//String productKey = json.id
			//String holder = json.email
			//String subject = json.instalationNum


			//Date validAfter = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.validStart)
			//Date validBefore  = org.apache.tools.ant.util.DateUtils.parseIso8601DateTime((String)json.validEnd)
			//int numberOfInstances = json.max

			//String lic = licenseService.generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)

			render decodedString
			//render(view:"showLicense", model:[email:json.email,encodedString:lic])
		}
	}

/*
	def load(){
		def license = params.license
		if(license) {
			licenseService.load(license)
			flash.message = "license loaded"
		}
	}

	def loadLicense(){
		def license = params.license
		if(license) {
			licenseService.load(license)
			flash.message = "license loaded"
		}
	}

	def clientRequest(){
		String instalationNum = licenseService.getInstalationId()
		[instalationNum:instalationNum]
	}

	def generate(){
		String productKey = params.productKey
		String holder = "Who's the Client"
		String subject = params.subject
		Date validAfter = DateUtils.parseDate(params.validAfter, 'yyyy-MM-dd');
		Date validBefore  = DateUtils.parseDate(params.validBefore, 'yyyy-MM-dd');
		int numberOfInstances = NumberUtils.toInt(params.instances, 14);

		String lic = licenseService.generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)
		[lic:lic]
	}



	def uselicense(){
		licenseService.useLicense()
		render "OK"
	}
*/
}
