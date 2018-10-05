package net.transitionmanager.service

import com.tdsops.common.exceptions.InvalidLicenseException
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.converters.JSON
import grails.plugin.mail.MailService
import grails.transaction.Transactional
import groovy.util.logging.Slf4j

import net.nicholaswilliams.java.licensing.License
import net.nicholaswilliams.java.licensing.LicenseManager
import net.nicholaswilliams.java.licensing.LicenseManagerProperties
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.transitionmanager.domain.License as DomainLicense
import net.transitionmanager.domain.Project
import net.transitionmanager.service.license.prefs.*
import net.transitionmanager.domain.PartyGroup
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.io.Resource


@Slf4j
class LicenseAdminService extends LicenseCommonService implements InitializingBean {
	static transactional = false

	private static int TTL = 15 * 60  //TODO: 20170124 Move to a config variable (default 15 min)

	/**
	 * States that can have a license in the system
	 */
	static enum State {
		UNLICENSED, TERMINATED, EXPIRED, INBREACH, NONCOMPLIANT, VALID
	}
	static final String CACHE_NAME = "LIC_STATE"
	CacheManager licenseCache = CacheManager.getInstance()

	AssetEntityService assetEntityService
	MailService mailService
	SecurityService	securityService


	/**
	 * Initialize the license service, configuring the cache and the licensing library
	 */
	@Override
	void afterPropertiesSet() throws Exception {
		if(!licenseCache.getCache(CACHE_NAME)) {
			log.debug("configuring cache")
			Cache memoryOnlyCache = new Cache(CACHE_NAME, 200, false, false, TTL, 0)
			licenseCache.addCache(memoryOnlyCache)
		}

		if(isEnabled()) {
			log.debug("LAdmin is Enabled?: {}", isEnabled())
			MyLicenseProvider licenseProvider = MyLicenseProvider.getInstance()

			if(isLGen()) {
				log.debug("License Manager Enabled")
				String keyFile = grailsApplication.config.manager?.license?.key
				String password = grailsApplication.config.manager?.license?.password

				File file = new File(keyFile)

				//if the file doesn't exists load from the web-app resources
				if(!file.exists()){
					Resource resource = grailsApplication.parentContext.getResource(keyFile)
					if(resource.exists()) {
						file = resource.file
					}
				}

				TDSPasswordProvider tdsPasswordProvider = new TDSPasswordProvider(password)

				// BEGIN: License Manager Configuration //
				LicenseCreatorProperties.setPrivateKeyDataProvider(new FilePrivateKeyDataProvider(file))
				LicenseCreatorProperties.setPrivateKeyPasswordProvider(tdsPasswordProvider)
				LicenseCreator.getInstance()
				// END: License Manager Configuration //

				// BEGIN: TEST MANAGER LICENSE //
				//String id = "84612874-7d78-4a69-906c-2e02c27ab54d"
				//String key = licenseManagerService.getLicenseKey(id)
				//log.debug("OLB ($id) License Key: $key")
				// END: TEST MANAGER LICENSE //
			}

			log.debug("License Admin Enabled")
			String keyFile = grailsApplication.config.tdstm.license.key
			String password = grailsApplication.config.tdstm.license.password
			log.debug("Admin Key: '{}', password: '{}'", keyFile, password)

			if(keyFile) {
				File file = new File(keyFile)

				//if the file doesn't exists load from the web-app resources
				if (!file.exists()) {
					Resource resource = grailsApplication.parentContext.getResource(keyFile)
					if (resource.exists()) {
						file = resource.file
					}
				}

				TDSPasswordProvider tdsPasswordProvider = new TDSPasswordProvider(password)

				// BEGIN: License Admin Configuration //

				LicenseManagerProperties.setPublicKeyDataProvider(new FilePublicKeyDataProvider(file))
				LicenseManagerProperties.setPublicKeyPasswordProvider(tdsPasswordProvider)
				LicenseManagerProperties.setLicenseProvider(licenseProvider)
				LicenseManagerProperties.setLicensePasswordProvider(tdsPasswordProvider)
				// should we set a different password per client?
				LicenseManagerProperties.setLicenseValidator(new TDSLicenseValidator())
				// Optional; defaults to 0, which translates to a 10-second (minimum) cache time
				LicenseManagerProperties.setCacheTimeInMinutes(24 * 60)

				LicenseManager.getInstance()
			}

			// END: License Admin Configuration //

			// BEGIN: TEST CLIENT LICENSE //
			// LicenseManager manager = LicenseManager.getInstance()
			// log.debug("OLB: Load License")
			// licenseProvider.addLicense("tst", "rO0ABXNyADFuZXQubmljaG9sYXN3aWxsaWFtcy5qYXZhLmxpY2Vuc2luZy5TaWduZWRMaWNlbnNlioT/n36yaoQCAAJbAA5saWNlbnNlQ29udGVudHQAAltCWwAQc2lnbmF0dXJlQ29udGVudHEAfgABeHB1cgACW0Ks8xf4BghU4AIAAHhwAAABICRMR4APL4M1cNX0873tLulzM4u0iHsTGjR3+QqdnAB3dVJIGYI15o5rDMfVcO+WtAOnzjhJobAQunl6wniNYvrzBZNYEFX+w/siIxVkVNlI98UL7kXPzWMn/sjM/UvKvKHNCYLdRBD+mpwG/IGo4YSQuxYSOlCx65kB2yHGrSEhqNQqFX5p3+6/hMePjb3ZOgOujYkosrH8Q9xenTv9jeNPdH5xBC8wjcw5HefMJJHO2RlEzuq8otkYdyd4dUEdpTjCvMN3SzUxvwqQEg4RrnGZd+cdV3bcPFFLVx233rpMw74Gdh1YMXLk82v89IRldvh2/7d8pIA5DD2334vb/4mSj8SUrNxYFvLsMnKYm64p0yLQGQGRnjv7dAgf8EQ/6HVxAH4AAwAAAQBXcYEC7z81w9XHS6lotp/ys1Nvnw1pv7F0NPhPS8CstiGdQrSbeiMU4bJ/XosTzI8uV+y4db2uJI8wq2mBoqc/iTrRFgBeEZZ3kuEtlbsywblcKFsuHcuKDEWWQOBiyzhMcb25nuJj/UDSGIl90mHiwl11YtBlbEhvnMvsa8fWOBlVE5SZgbebAs5Yf8D8ACf1bkSzf1iv1m8Op6bMcmQRYFaXtf/CD0CKyVjK9S2UfimmKQ9sse8b6zsBgvDrlBjMP+itZxY7tIflwkZhdIbIbxTRVco4Gey1GHVhMWg5UYJuMKEidpBtBGDaAqHytG1oBQ9aNoAjnLvnfTXGXf+L")
			// License lic = manager.getLicense("tst") //set the license to test
			// log.debug("License loaded (${lic.productKey} ${lic.issuer})? ${lic.goodAfterDate} - ${lic.goodBeforeDate}")
			// END: TEST CLIENT LICENSE //
		}

	}

	/**
	 * is the license Administrator Enabled to manage licenses
	 * if not the system is license-free
	 * @return
	 */
	boolean isEnabled(){
		return isAdminEnabled()
	}

	/**
	 * @deprecated
	 * return the license Message for the required project or current if Null
	 * @return the License Message
	 */
	String licenseStateMessage(Project project = null){
		return getLicenseStateMessage(project)
	}

	/**
	 * return the license Message for the required project or current if Null
	 * @return the License Message
	 */
	String getLicenseStateMessage(Project project = null){
		return getLicenseStateMap(project)?.message
	}

	/**
	 * License Baner of the current license
	 * @param project
	 * @return
	 */
	String getLicenseBannerMessage(Project project = null){
		return getLicenseStateMap(project)?.banner
	}

	/**
	 * Current license State
	 * @param project
	 * @return
	 */
	State getLicenseState(Project project = null){
		return getLicenseStateMap(project)?.state
	}

	/**
	 * Used to determine if there is a valid license for the project or a
	 * valid global license
	 * @param project
	 * @return true if there is a valid license
	 * 	 // alternative idea -- boolean isValid(projectGuid, featureName)
	 */
	boolean isValid(Project project = null){
		Map licState = getLicenseStateMap(project)

		return licState.valid
	}

	/**
	 * check if a license exists valid to the passed project or throw an InvalidLicenseException
	 * @param project
	 * @throws InvalidLicenseException
	 */
	void checkValidForLicenseOrThrowException(Project project = null) throws InvalidLicenseException{
		if(!isValid(project)){
			throw new InvalidLicenseException()
		}
	}

	/**
	 * Used to retrieve the license state for the project
	 * @param project
	 * @return true if there is a valid license
	 * 	 // alternative idea -- boolean isValid(projectGuid, featureName)
	 */
	@Transactional
	Map getLicenseStateMap(Project project = null){

		Map defaultValidState = [
			state	: State.VALID,
			message : "",
			valid 	: true,
			banner 	: ""
		]

		//Is license check disabled then is always valid
		if (!adminEnabled) {
			return defaultValidState
		}

		if(project == null){
			project = securityService.userCurrentProject
		}

		//If the current project is null return true
		if(project == null) {
			return defaultValidState
		}

		String projectId = project.id

		// Attempt to load the license from the EhCache
		Cache cache = licenseCache.getCache(CACHE_NAME)
		def cacheEl = cache.get(projectId)

		Map licState = (Map)cacheEl?.getObjectValue()

		// licState = null  //testing proposes

		// If the license wasn't in the cache then one will be created and added
		if(!licState) {
			log.debug("LOAD LICENSE FROM STORE")
			licState = [:]
			cache.put(new Element(projectId, licState))
			List<DomainLicense> licenses = DomainLicense.findAllByProjectAndStatus(projectId, DomainLicense.Status.ACTIVE) //dateCreated?

			//TODO: iterate over the licenses to find the one that fits
			DomainLicense license = licenses.find { it.hash }

			//If no active license for this project, check ALL for a multi-project one
			if(!license) {
				license = DomainLicense.findByProjectAndStatus("all", DomainLicense.Status.ACTIVE)
			}

			//Validate that the license in the DB has not been compromised
			//LicenseManager manager = LicenseManager.getInstance()
			//License licObj = manager.getLicense(license.id)

			if (!license || !license.hash) {
				// UNLICENSED
				licState.state = State.UNLICENSED
				licState.message = "A license is required in order to enable all features of the application."
				licState.valid = false
				licState.banner = ""
				licState.numberOfLicenses = 0
				licState.goodAfterDate = null
				licState.goodBeforeDate = null
				licState.type = null
			}else {
				License licObj = getLicenseObj(license)
				int max = licObj?.numberOfLicenses ?: 0
				licState.numberOfLicenses = max
				licState.goodAfterDate = licObj.goodAfterDate ? new Date(licObj.goodAfterDate) : null
				licState.goodBeforeDate = licObj.goodBeforeDate ? new Date(licObj.goodBeforeDate) : null
				licState.type = license.type

				if(licObj == null){
					licState.state = State.UNLICENSED
					licState.message = "A license is required in order to enable all features of the application."
					licState.valid = false
					licState.banner = ""
					return licState
				}

				def jsonData = JSON.parse(licObj.subject)
				int gracePeriodDays = jsonData.gracePeriodDays
				String hostName 	= jsonData.hostName
				String websitename 	= jsonData.websitename

				String projectName	   = JSON.parse(jsonData.project)?.name
				log.debug("Lic: {}", licObj.productKey)
				log.debug("jsonData: {}", jsonData)
				log.debug("projectName : {}", projectName)
				log.debug("project.name : {}", project.name)

				licState.banner = license.bannerMessage

				String currentHost = getHostName()
				if (
					DomainLicense.WILDCARD != hostName &&
					! StringUtils.equalsIgnoreCase(currentHost, hostName)
				){
					licState.message = """
						|Error loading license:<br/> 
						|current host:<br/><strong>${currentHost}</strong><br/>
						|licensed host:<br/><strong>${hostName}</strong>
					""".stripMargin()
					licState.valid = false
					return licState
				}

				String fqdn = getFQDN()
				if (
					DomainLicense.WILDCARD != websitename &&
					! StringUtils.equalsIgnoreCase(fqdn, websitename)
				){
					licState.message = """
						|Error loading license:<br/> 
						|current website:<br/><strong>${fqdn}</strong><br/>
						|licensed website:<br/><strong>${websitename}</strong>
					""".stripMargin()
					licState.valid = false
					return licState
				}

				//The Name still the same? and is not a Multiproject one
				if(projectName != project.name && projectName != "all"){
					licState.message = "The name of the project was changed but must be <strong>'${projectName}'</strong> for license compliance"
					licState.valid = false
					return licState
				}


				Date now = new Date()
				long nowTime = now.getTime()

				if (nowTime >= licObj.goodAfterDate && nowTime <= licObj.goodBeforeDate) {
					long numServers = assetEntityService.countServers(project)
					log.debug("NumServers: {}", numServers)
					if (numServers <= max) {
						licState.state = State.VALID
						licState.message = ""
						licState.lastCompliantDate = now
						licState.valid = true
					} else {
						int gracePeriod = gracePeriodDaysRemaining(gracePeriodDays, licState.lastCompliantDate)
						if(gracePeriod > 0) {
							licState.state = State.NONCOMPLIANT
							licState.message = "The Server count has exceeded the license limit of ${license.max} by ${numServers - license.max} servers. The application functionality will be limited in ${gracePeriod} days if left unresolved."
							licState.valid = true
						} else {
							licState.state = State.INBREACH
							licState.message = "The Server count has exceeded the license limit beyond the grace period. Please reduce the server count below the limit of ${license.max} to re-enable all application features."
							licState.valid = false
						}
					}
				} else {
					licState.state = State.EXPIRED
					license.status = DomainLicense.Status.EXPIRED

					licState.message = "The license has expired. A new license is required in order to enable all features of the application."
					licState.valid = false
				}

				if(license.isDirty()){
					license.save()
				}
			}
		} else {
			log.debug("LICENSE LOADED FROM CACHE")
		}

		return licState
	}

	/**
	 * Check if a project is compliant to a license
	 * @param project
	 * @return
	 */
	boolean isLicenseCompliant(Project project){
 		Map licState = getLicenseStateMap(project)
		return licState.state != State.NONCOMPLIANT
	}

	/**
	 * Grace period remaining
	 * @param gracePeriodDays
	 * @param lastCompliantDate
	 * @return
	 */
	int gracePeriodDaysRemaining(int gracePeriodDays=5, Date lastCompliantDate){
		lastCompliantDate = lastCompliantDate ?: new Date()
		Date graceDate = DateUtils.addDays(lastCompliantDate, gracePeriodDays)
		Date now = new Date()
		return (graceDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
	}

	/**
	 * loads a license provided as Domain License into the system verifying that is valid
	 * @param license
	 * @return
	 */
	boolean load(DomainLicense license) {
		//first we clear the catched licenses to validate them
		clearCachedLicenses()
		return checkLicense(license)
	}

	/**
	 * check the provided license checking that is valid against the data stored in the database
	 * @param license
	 * @return
	 */
	@Transactional
	private boolean checkLicense(DomainLicense license){
		String id = license.id
		String hash = license.hash

		//strip the actual license from the envelope
		hash = StringUtil.openEnvelop(LicenseCommonService.BEGIN_LIC_TAG, LicenseCommonService.END_LIC_TAG, hash)

		/******
		 * This LicenseManager variable is from the License Library, DO NOT confuse it with the TDS LM!
		 ******/
		LicenseManager manager = LicenseManager.getInstance()

		/*******************
		 * The License Library cache is clearead to guarantee that we are loading the latest license
		 * from the storage instead of relying on the lib cache, for License CAche we use or own Cache
		 * implementation using EhCache
		 *******************/
		manager.clearLicenseCache()

		log.debug("ID: {}", id)
		log.debug("Hash: {}", hash)
		MyLicenseProvider licenseProvider = MyLicenseProvider.getInstance()
		licenseProvider.addLicense(id, hash)

		License licObj
		try {
			licObj = manager.getLicense(id)
		}catch(Exception ex){
			log.error(ex.message)
			licenseProvider.remove(id)
			license.status = DomainLicense.Status.CORRUPT
			//return license.save()
			return false
		}
		//Validate if is valid

		log.debug("license.id: {}", license.id)
		log.debug("License Obj: {}", licObj)
		log.debug("License ID: {}", licObj?.productKey)

		if(licObj == null) return false

		if(license.id != licObj?.productKey){
			log.debug("[{}] == [{}]", license.id, licObj?.productKey)
			log.error("Error loading license data: Wrong product Key")
		}

		def jsonData = JSON.parse(licObj.subject)
		//String installationNum = jsonData.installationNum
		//int gracePeriodDays	   = jsonData.gracePeriodDays
		String bannerMessage   = jsonData.bannerMessage
		String project		   = jsonData.project
		String hostName 	   = jsonData.hostName
		String websitename 	   = jsonData.websitename

		log.debug("LicenseAdminService - Project: {}", project)

		//if is not wildcard and Sites don't match, FAIL
		if(
			DomainLicense.WILDCARD != hostName &&
			! StringUtils.equalsIgnoreCase(getHostName(), hostName)
		){
			log.debug("[{}] == [{}]", getHostName(), hostName)
			log.error("Error loading license data: Wrong Host Name")
		}

		//if is not wildcard and sitename don't match, FAIL
		if(
			DomainLicense.WILDCARD != websitename &&
			! StringUtils.equalsIgnoreCase(getFQDN(), websitename)
		){
			log.debug("[${getFQDN()}] == [$websitename]")
			log.error("Error loading license data: Wrong Website Name")
		}

		//Refreshing database license properties with the actual values from the Manager License, Keep client honest

		license.hash = hash
		license.max = licObj.numberOfLicenses
		license.bannerMessage = bannerMessage

		license.activationDate = new Date(licObj.goodAfterDate)
		license.expirationDate = new Date(licObj.goodBeforeDate)

		log.debug("license.activationDate: {}", license.activationDate)
		log.debug("license.expirationDate: {}", license.expirationDate)

		license.status = DomainLicense.Status.ACTIVE
		return license.save()

	}

	/**
	 * Get a License Object from the LicenseLibrary
	 * @param license
	 * @return
	 */
	private License getLicenseObj(DomainLicense license){
		checkLicense(license)
		LicenseManager manager = LicenseManager.getInstance()
		return manager.getLicense(license.id)
	}


    /**
     * Creates a new License Request. If the uuid parameter is null or there is no license with that uid,
     * a new License will be created.
     *
     *
     * @param uuid - the uid of the License
     * @param owner - The owner
     * @param email - The email address to be assigned to the license request.
     * @param environmentId - The environment id.
     * @param projectId - The id of the Project.
     * @param requestNote - The note attached to the License Request
     *
     */
	@Transactional
    DomainLicense generateRequest(String uuid, PartyGroup owner, String email, String environment, def projectId, String requestNote ){
		DomainLicense lic

		if (uuid != null && !uuid.isNumber()) {
        	throw new IllegalArgumentException('Not a license Id number')
        }

		if(uuid){
			lic = DomainLicense.get(uuid)
		} else {

			lic = new DomainLicense()
			lic.owner = owner
			//since we are representing this as JSON we get rid of the millisecond part
			lic.requestDate = DateUtils.truncate(new Date(), Calendar.SECOND)
			lic.status = DomainLicense.Status.PENDING
			lic.method = DomainLicense.Method.MAX_SERVERS
			lic.installationNum = getInstallationId()
			lic.hostName = hostName
			lic.websitename = FQDN
		}

		lic.email = email
		lic.environment = environment as DomainLicense.Environment
		lic.project = projectId
		lic.requestNote = requestNote

		if(lic.project != "all"){
			lic.type = DomainLicense.Type.SINGLE_PROJECT
			def project = Project.get(lic.project)
			if(project !=  null) {
				def client = project.client
				lic.installationNum = "${lic.installationNum}|${project.name}|${client.name}"
			} else {
				lic.errors.rejectValue("project", "Project (id:${lic.project}) not found")
			}
		} else {
			lic.type = DomainLicense.Type.MULTI_PROJECT
		}

        if (lic.save(flush:true)) {
        	return lic
        } else {
        	if (lic.hasErrors()) {
            	String errors = GormUtil.allErrorsString(lic)
                log.error("lic error: {}",  errors)
            }
            throw new DomainUpdateException("Error while creating License Request")
        }
    }

	/**
	 * resend the mail request
	 * @param uuid identifier of the license
	 * @return true if the mail was sent, false otherwise
	 */
	boolean resubmitRequest(String uuid){
		DomainLicense license = DomainLicense.get(uuid)
		if(license){
			sendMailRequest(license)
		}else{
			log.error("License Request identified by '{}' not found", uuid)
			false
		}
	}

	/**
	 * Return a map with the information to build an email to request a license
	 * @param uuid
	 * @return Map with the information to build the email
	 */
	EmailHolder emailRequestData(String uuid){
		DomainLicense license = DomainLicense.get(uuid)
		return emailRequestData(license)
	}

	/**
	 * Return a map with the information to build an email to request a license
	 * @param license Object
	 * @return EmailHolder with the information to build the email
	 */
	EmailHolder emailRequestData(DomainLicense license){
		EmailHolder emailHolder
		if(license){
			String toEmail = grailsApplication.config.tdstm?.license?.request_email
			emailHolder = [:]
			emailHolder.subject = "License Request - ${license.websitename}"
			emailHolder.toEmail = toEmail
			emailHolder.ccEmail = license.email
			/* The email address provided in the license request form is used as the
			*  "from" for the email notification. Note that if the grails.mail.overrideAddress
			*  is present in the configuration file, this address is going to be overridden. */
			emailHolder.from = license.email
			emailHolder.body = getLicenseRequestBody(license)
		}

		return emailHolder
	}

	/**
	 * Retrieve license request body used to send email and pressent to the user
	 * @param uuid identifier of the stored license
	 * @return String reptresentation of the hash request
	 */
	String getLicenseRequestBody(String uuid) {
		DomainLicense lic

		if (uuid) {
			lic = DomainLicense.get(uuid)
		}

		return getLicenseRequestBody(lic)
	}

	/**
	 * Retrieve license request body used to send email and pressent to the user
	 * @param lic license object
	 * @return String reptresentation of the hash request
	 */
	String getLicenseRequestBody(DomainLicense lic) {
		String buff
		if(lic) {
			String body = """
				|from: ${lic.email}
				|Website Name: ${lic.websitename}
				|
				|${lic.toEncodedMessage()}
			""".stripMargin().trim()

			buff = ""
			body.eachLine{ line ->
				buff += line.split("(?<=\\G.{50})").join('\n') +'\n'
			}

		}
		return buff
	}

	/**
	 * Send license request mail back to License Manager
	 * @param license License Object
	 * @return true if the mail was sent, false otherwise
	 */
	private boolean sendMailRequest(DomainLicense license){
		log.debug("SEND License Request")
		EmailHolder emailData = emailRequestData(license)

		if(emailData) {
			mailService.sendMail {
				// oluna: Next is commented since it's not needed and causes SPAM issues (TM-6738)
				// from emailData.from
				to emailData.toEmail
				subject emailData.subject
				body emailData.body
			}
			true

		}else{
			log.error("could not send email, not properly configured")
			false
		}
	}

    /**
     * Deletes a License Request.
     * @param uuid - the id of the License.
     * @return true if the license was successfully deleted. false if the license does not exist.
     *
     */
	@Transactional
    boolean deleteLicense(String uuid){
		//Clear catched licenses forcing to recheck them
		clearCachedLicenses()

		if(uuid) {
			DomainLicense lic = DomainLicense.get(uuid)
			if(lic) {
				lic.delete()
				return true
			} else {
				return false
			}
        } else {
            return false
        }
    }

	/**
	 * Clears Cached licenses values to re-check them against the license API
	 * @return
	 */
	private clearCachedLicenses() {
		//I don't care removing all since only a few are enabled per instance
		Cache cache = licenseCache.getCache(CACHE_NAME)
		cache.removeAll()
	}

	/**
	 * Objecto to hold the Email data in static Type style
	 */
	class EmailHolder {
		String from
		String toEmail
		String ccEmail
		String subject
		String body

		//marshaller to avoid to render the 'class' property loaded when the Type is first loaded
		static {
			grails.converters.JSON.registerObjectMarshaller(EmailHolder) {
				return it.properties.findAll{k, v -> k != 'class'}
			}

		}
	}

}
