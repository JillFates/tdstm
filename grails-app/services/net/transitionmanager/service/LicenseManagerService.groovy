package net.transitionmanager.service

import com.github.icedrake.jsmaz.Smaz
import grails.converters.JSON
import groovy.util.logging.Slf4j
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.domain.Project
import org.apache.commons.codec.binary.Base64

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

		def json = grails.converters.JSON.parse(decodedString)

		return fetchJsonToLicense(json, true)
	}

	def getLicenseKey(id){
		LicensedClient lic = fetch(id)

		if(lic) {
			def dataJson = [
					installationNum: lic.installationNum,
					project        : lic.project,
					gracePeriodDays: lic.gracePeriodDays,
					bannerMessage  : lic.bannerMessage,
					hostName       : lic.hostName,
					websitename    : lic.websitename
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

	private fetchJsonToLicense(json, createIfNotFound = false){
		def dateParser = {String strDate ->
			if(strDate){
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

		log.debug("Jsonene {}", json)
		if(!lc && createIfNotFound) {
			lc = new LicensedClient()
		}

		if(!lc){
			return null
		}

		lc.id = json.id

		if(json.installationNum != null) {
			lc.installationNum = json.installationNum
		}
		if(json.email != null) {
			lc.email = json.email
		}
		if(json.requestNote != null) {
			lc.requestNote = json.requestNote
		}
		if(json.hostName != null) {
			lc.hostName = json.hostName
		}
		if(json.websitename != null) {
			lc.websitename = json.websitename
		}
		if(json.expirationDate) {
			lc.expirationDate = dateParser(json.expirationDate)
		}
		if(json.activationDate) {
			log.debug("set Activation DAte: {}", json.activationDate)
			lc.activationDate = dateParser(json.activationDate)
			log.debug("set Activation DAte: {}", lc.activationDate)
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
		if(json.environment?.id != null) {
			lc.environment = License.Environment.forId(json.environment?.id)
		}
		if(json.project != null) {
			if(lc.project != null){
				def jsonP = JSON.parse(lc.project)
				if(jsonP.id != json.project.id){
					lc.project = json.project?.toString()
				}
			}else{
				lc.project = json.project?.toString()
			}
		}
		if(json.client != null) {
			lc.client = json.client?.toString()
		}
		if(json.owner != null) {
			lc.owner = json.owner?.toString()
		}

		if(json.bannerMessage != null) {
			lc.bannerMessage = json.bannerMessage
		}

		if(json.gracePeriodDays != null){
			lc.gracePeriodDays = json.gracePeriodDays
		}

		return lc
	}
}
