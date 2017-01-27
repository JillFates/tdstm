package net.transitionmanager.service

import com.github.icedrake.jsmaz.Smaz
import grails.converters.JSON
import groovy.util.logging.Slf4j
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.domain.Project
import org.apache.commons.codec.binary.Base64
import org.codehaus.groovy.grails.web.json.JSONElement

import java.text.ParseException

/**
 * Created by octavio on 12/9/16.
 * Manager
 */
//@CompileStatic
@Slf4j
class LicenseManagerService extends LicenseCommonService{
	LicenseAdminService licenseAdminService

	Collection<LicensedClient> list(){
		LicensedClient.findAll()
	}

	LicensedClient fetch(id){
		LicensedClient lic
		if(id) {
			lic = LicensedClient.get(id)
		}

		return lic
	}

	LicensedClient delete(id){
		LicensedClient lic = fetch(id)

		if(lic) {
			lic.delete()
		}

		return lic
	}

	def loadRequest(String body){
		String beginTag = License.BEGIN_REQ_TAG
		String endTag = License.END_REQ_TAG

		def idxB = body.indexOf(beginTag)
		if(idxB >= 0){
			def idxE = body.indexOf(endTag)
			if(idxE < 0){
				LicensedClient lc = new LicensedClient()
				lc.errors.rejectValue("Malformed Message", "Missing ${endTag} tag for request")
				return lc
			}
			body = body.substring(idxB + beginTag.length(), idxE)
			body = body.trim()
		}

		log.debug("Body: {}", body)

		String decodedString = Smaz.decompress(Base64.decodeBase64(body))

		log.debug("Decoded String: {}", decodedString)

		JSONElement json = grails.converters.JSON.parse(decodedString)

		return  LicensedClient.fetch(json, true)
	}

	def getLicenseKey(id){
		LicensedClient lic = fetch(id)

		if(lic) {
			def dataJson = [
					installationNum : lic.installationNum,
					project         : lic.project,
					gracePeriodDays : lic.gracePeriodDays,
					bannerMessage   : lic.bannerMessage,
					hostName        : lic.hostName,
					websitename     : lic.websitename,
					version			: 1
			] as JSON


			String productKey = lic.id
			String holder = lic.email
			String subject = dataJson.toString()
			Date validAfter = lic.activationDate
			Date validBefore = lic.expirationDate
			int numberOfInstances = lic.max

			String licString = ""

			if (productKey != null && validAfter != null && validBefore != null && numberOfInstances > 0){
				licString = generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)

			}

			licString = "${BEGIN_LIC_TAG}\n${licString}\n${END_LIC_TAG}"
			log.debug("licString: {}", licString)
			return licString
		}

	}

	//// HELPER FUNCTIONS ///////////

	/**
	 * @param productKey  defines the id
	 * @param holder
	 * @param subject	holds a JSON object to different extra properties
	 * @param numberOfLicenses
	 * @param goodAfter
	 * @param goodBefore
	 * @return
	 */
	private String generateLicense(String productKey, String holder, String subject, int numberOfLicenses, Date goodAfter, Date goodBefore){
		licenseAdminService.initialize()

		net.nicholaswilliams.java.licensing.License.Builder licenseBuilder = new net.nicholaswilliams.java.licensing.License.Builder().
				withProductKey(productKey).
				withIssuer("TDS").
				withHolder(holder).
				withSubject(subject).
				withIssueDate(new Date().getTime()).
				withGoodAfterDate(goodAfter.getTime()).
				withGoodBeforeDate(goodBefore.getTime()).
				withNumberOfLicenses(numberOfLicenses)

		net.nicholaswilliams.java.licensing.License license = licenseBuilder.build()

		//Use the name of the license name "project:<guid>"
		/*
		net.nicholaswilliams.java.licensing.License license = licenseBuilder.
				addFeature("environment:[engineering | training | demo | production]").
				addFeature("module:dr").
				addFeature("module:cookbook").
				addFeature("module:core"). //All will have this so maybe is not required
				build()
		*/

		byte[] licenseData = LicenseCreator.getInstance().signAndSerializeLicense(license)
		String trns = new String(Base64.encodeBase64(licenseData))

		return trns
	}

}
