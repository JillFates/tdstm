package net.transitionmanager.service

import com.github.icedrake.jsmaz.Smaz
import com.tdsops.common.exceptions.InvalidLicenseException
import com.tdsops.common.exceptions.ServiceException
import com.tdssrc.grails.StringUtil
import grails.converters.JSON
import grails.plugin.mail.MailService
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicenseActivityTrack
import net.transitionmanager.domain.LicensedClient
import org.apache.commons.codec.binary.Base64
import org.codehaus.groovy.grails.web.json.JSONElement

/**
 * Created by octavio on 12/9/16.
 * Manager
 */
//@CompileStatic
@Slf4j
class LicenseManagerService extends LicenseCommonService {
	MailService mailService

	Collection<LicensedClient> list() {
		LicensedClient.findAll()
	}

	LicensedClient fetch(String id) {
		LicensedClient lic
		if (id) {
			lic = LicensedClient.get(id)
		}

		return lic
	}

	@Transactional
	LicensedClient delete(String id) {
		LicensedClient lic = fetch(id)

		if (lic) {
			lic.delete()
		}

		return lic
	}

	@Transactional
	LicensedClient revoke(String id) {
		LicensedClient lic = fetch(id)

		if (lic) {
			lic.status = License.Status.TERMINATED
			lic.save()
		}

		return lic
	}

	@Transactional
	def loadRequest(String body) {
		body = StringUtil.openEnvelop(License.BEGIN_REQ_TAG, License.END_REQ_TAG, body)
		String decodedString = Smaz.decompress(Base64.decodeBase64(body))
		JSONElement json = grails.converters.JSON.parse(decodedString)
		LicensedClient lc = LicensedClient.fetch(json, true)
		return lc.save()
	}

	String getLicenseKey(String id) throws InvalidLicenseException {
		LicensedClient lic = fetch(id)
		getLicenseKey(lic)
	}

	String getLicenseKey(LicensedClient lic) throws InvalidLicenseException {
		if (lic && lic.status == License.Status.ACTIVE) {
			String errors = lic.missingPropertyErrors()
			if (errors) {
				throw new InvalidLicenseException(errors)
			}

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

			log.debug("Lets Generate a License!!")
			if (productKey != null && validAfter != null && validBefore != null && numberOfInstances > 0) {
				log.debug("Generating the License!!")
				licString = generateLicense(productKey, holder, subject, numberOfInstances, validAfter, validBefore)
			}

			licString = "${BEGIN_LIC_TAG}\n${licString}\n${END_LIC_TAG}"
			log.debug("License String: {}", licString)
			licString
		} else {
			""
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
	private String generateLicense(String productKey, String holder, String subject, int numberOfLicenses, Date goodAfter, Date goodBefore) {

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

		try {
			byte[] licenseData = LicenseCreator.getInstance().signAndSerializeLicense(license)
			String trns = new String(Base64.encodeBase64(licenseData))

			return trns
		}catch(e){
			log.error("License Creation Problem", e)
			throw e
		}
	}

	/**
	 * Activate a license
	 * @param id
	 * @return
	 * @throws InvalidLicenseException
	 */
	@Transactional
	String activate(String id) throws InvalidLicenseException{
		LicensedClient lic = fetch(id)
		if (lic) {
			String errors = lic.missingPropertyErrors()
			if (errors) {
				throw new InvalidLicenseException(errors)
			}

			lic.status = License.Status.ACTIVE
			lic.save()
			"Ok"
		}
	}

	/**
	 * Get the activity log on a license object
	 * @param uuid identifier of the License to get the log from
	 * @return List of activity tracks
	 */
	List<LicenseActivityTrack> activityLog(String uuid) {
		LicensedClient licensedClient = fetch(uuid)
		List<LicenseActivityTrack> log = []
		if (licensedClient) {
			log = LicenseActivityTrack.findAllByLicensedClient(licensedClient, [sort: "dateCreated", order:"desc"])
		}
		log
	}

	/**
	 * Email a license to the stored client email
	 * @param uuid identifier of the license
	 * @return
	 */
	boolean emailLicense(String uuid) {
		LicensedClient licensedClient = fetch(uuid)
		emailLicense(licensedClient)
	}

	/**
	 * Email a license to the stored client email
	 * @param licensedClient license client object with the license information
	 * @return
	 */
	boolean emailLicense(LicensedClient licensedClient) {
		log.debug("SEND License Request")
		String toEmail = licensedClient.email

		if (toEmail) {
			String message = """
				|Website Name: ${licensedClient.websitename}
				|
				|${getLicenseKey(licensedClient)}
			""".stripMargin().trim()
			String buff = ""
			message.eachLine { line ->
				buff += line.split("(?<=\\G.{50})").join('\n') +'\n'
			}

			mailService.sendMail {
				to toEmail
				subject "TM License - ${licensedClient.websitename}"
				body buff
			}
			true

		} else {
			log.error("no email found for the client")
			false
		}
	}

	@Transactional
	void updateLicense(id, json) throws Exception{
		LicensedClient lic
		if(id) {
			json.id = id
			lic = LicensedClient.fetch(json)
		}

		if(lic) {
			if(! lic.save() ){
				if( log.isDebugEnabled() ) {
					String errors = ""
					lic.errors.each { err ->
						errors << "${err}/n"
					}
					log.debug("Errors {}", errors)
				}
				throw new ServiceException("${id} not found.")
			}
		}else{
			throw new FileNotFoundException("${id} not found.")
		}
	}
}
