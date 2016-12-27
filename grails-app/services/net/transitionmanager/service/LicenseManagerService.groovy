package net.transitionmanager.service

import com.github.icedrake.jsmaz.Smaz
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
@Slf4j(value='log', category='net.transitionmanager.service.LicenseManagerService')
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
		String beginTag = License.BEGIN_TAG
		String endTag = License.END_TAG

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

		log.info("Body: {}", body)

		String decodedString = Smaz.decompress(Base64.decodeBase64(body))

		log.info("Decoded String:")
		log.info(decodedString)

		def json = grails.converters.JSON.parse(decodedString)

		return fetchJsonToLicense(json, true)
	}

	def getLicenseKey(id){
		def lic = fetch(id)

		if(lic) {
			/*
			String productKey = lic.id
			String holder = lic.email
			String subject = lic.installationNum

			Date validAfter = lic.activationDate
			Date validBefore = lic.expirationDate

			int numberOfInstances = lic.max

			String licString = generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)
			*/

			String licString = "rO0ABXNyADFuZXQubmljaG9sYXN3aWxsaWFtcy5qYXZhLmxpY2Vuc2luZy5TaWduZWRMaWNlbnNlioT/n36yaoQCAAJbAA5saWNlbnNlQ29udGVudHQAAltCWwAQc2lnbmF0dXJlQ29udGVudHEAfgABeHB1cgACW0Ks8xf4BghU4AIAAHhwAAABICRMR4APL4M1cNX0873tLulzM4u0iHsTGjR3+QqdnAB3dVJIGYI15o5rDMfVcO+WtAOnzjhJobAQunl6wniNYvrzBZNYEFX+w/siIxVkVNlI98UL7kXPzWMn/sjM/UvKvKHNCYLdRBD+mpwG/IGo4YSQuxYSOlCx65kB2yHGrSEhqNQqFX5p3+6/hMePjb3ZOgOujYkosrH8Q9xenTv9jeNPdH5xBC8wjcw5HefMJJHO2RlEzuq8otkYdyd4dUEdpTjCvMN3SzUxvwqQEg4RrnGZd+cdV3bcPFFLVx233rpMw74Gdh1YMXLk82v89IRldvh2/7d8pIA5DD2334vb/4mSj8SUrNxYFvLsMnKYm64p0yLQGQGRnjv7dAgf8EQ/6HVxAH4AAwAAAQBXcYEC7z81w9XHS6lotp/ys1Nvnw1pv7F0NPhPS8CstiGdQrSbeiMU4bJ/XosTzI8uV+y4db2uJI8wq2mBoqc/iTrRFgBeEZZ3kuEtlbsywblcKFsuHcuKDEWWQOBiyzhMcb25nuJj/UDSGIl90mHiwl11YtBlbEhvnMvsa8fWOBlVE5SZgbebAs5Yf8D8ACf1bkSzf1iv1m8Op6bMcmQRYFaXtf/CD0CKyVjK9S2UfimmKQ9sse8b6zsBgvDrlBjMP+itZxY7tIflwkZhdIbIbxTRVco4Gey1GHVhMWg5UYJuMKEidpBtBGDaAqHytG1oBQ9aNoAjnLvnfTXGXf+L"

			log.info("licString: {}", licString)
			licString
		}

	}

	//// HELPER FUNCTIONS ///////////

	private String generateLicense(String productKey, String holder, String subject, int numberOfLicenses, Date goodAfter, Date goodBefore){
		licenseAdminService.initialize()

		net.nicholaswilliams.java.licensing.License.Builder licenseBuilder = new net.nicholaswilliams.java.licensing.License.Builder().
				withProductKey(productKey). //TM-CORE-XXX
				withIssuer("TDS").
				withHolder(holder). //Partner - Client
				withSubject(subject). //project:<guid> | global
				withIssueDate(new Date().getTime()).
				withGoodAfterDate(goodAfter.getTime()).
				withGoodBeforeDate(goodBefore.getTime()).
				withNumberOfLicenses(numberOfLicenses)

		//Use the name of the license name "project:<guid>"
		net.nicholaswilliams.java.licensing.License license = licenseBuilder.
				addFeature("environment:[engineering | training | demo | production]").
				addFeature("module:dr").
				addFeature("module:cookbook").
				addFeature("module:core"). //All will have this so maybe is not required
				build()

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
		if(json.environment?.id != null) {
			lc.environment = License.Environment.forId(json.environment?.id)
		}
		if(json.project?.id != null) {
			def dProject = [
					id:"null",
					name:"null",
					client: [ id:'null', label:'null']
			]
			if(json.project?.id.toString() != "all"){
				Project prj = Project.get(json.project?.id)
				dProject.id = prj?.id
				dProject.name = prj?.name
			}
			lc.project = dProject
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

		return lc
	}
}
