package net.transitionmanager.license

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugins.mail.MailService
import groovy.util.logging.Slf4j
import net.nicholaswilliams.java.licensing.License
import net.nicholaswilliams.java.licensing.LicenseManager
import net.nicholaswilliams.java.licensing.LicenseManagerProperties
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidLicenseException
import net.transitionmanager.license.License as DomainLicense
import net.transitionmanager.license.prefs.FilePrivateKeyDataProvider
import net.transitionmanager.license.prefs.FilePublicKeyDataProvider
import net.transitionmanager.license.prefs.MyLicenseProvider
import net.transitionmanager.license.prefs.TDSLicenseValidator
import net.transitionmanager.license.prefs.TDSPasswordProvider
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import net.transitionmanager.security.SecurityService
import org.apache.commons.codec.digest.DigestUtils
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
	MailService        mailService
	SecurityService    securityService


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

		// If the license wasn't in the cache then one will be created and added
		if(!licState) {
			log.debug("LOAD LICENSE FROM STORE")
			licState = [:]
			cache.put(new Element(projectId, licState))
			List<DomainLicense> licenses = DomainLicense.where {
				(status == DomainLicense.Status.ACTIVE) && (hash != null) &&
						  ( (project == projectId) || (project == 'all') )
			}.list()

			if ( ! licenses ) { // UNLICENSED
				licState.state = State.UNLICENSED
				licState.message = "A license is required in order to enable all features of the application."
				licState.valid = false
				licState.banner = ""
				licState.numberOfLicenses = 0
				licState.goodAfterDate = null
				licState.goodBeforeDate = null
				licState.type = null

			}else {
				Date now = new Date()
				long nowTime = now.getTime()

				String currentHost = getHostName()
				String fqdn = getFQDN()
				String errorMessage = ''
				int gracePeriodDays = 0

				List<License> licenseObjs = licenses.findResults { DomainLicense lic -> getLicenseObj(lic) }

				licenseObjs = licenseObjs.findAll { License lic ->
					Map jsonData       = JSON.parse(lic.subject)
					gracePeriodDays    = Math.max( gracePeriodDays, jsonData.gracePeriodDays ?: 0)
					String hostName    = jsonData.hostName
					String websitename = jsonData.websitename
					String projectName = JSON.parse(jsonData.project)?.name

					if ( DomainLicense.WILDCARD != hostName &&
							  ! StringUtils.equalsIgnoreCase(currentHost, hostName) ) {

						if ( ! errorMessage ) {
							errorMessage = """
								|Licensed host changed:<br/> 
								|current:<br/><strong>${currentHost}</strong><br/>
								|licensed:<br/><strong>${hostName}</strong>
							""".stripMargin()
						}

						return false
					}

					if ( DomainLicense.WILDCARD != websitename &&
							  ! StringUtils.equalsIgnoreCase(fqdn, websitename)
					){
						if ( ! errorMessage ) {
							errorMessage = """
								|Licensed website changed:<br/> 
								|current:<br/><strong>${fqdn}</strong><br/>
								|licensed:<br/><strong>${websitename}</strong>
							""".stripMargin()
						}

						return false
					}

					//The Name still the same? and is not a Multiproject one
					if( projectName != project.name && projectName != "all" ){
						if ( ! errorMessage ) {
							errorMessage = "The name of the project was changed but must be <strong>'${projectName}'</strong> for license compliance"
						}

						return false
					}

					return (nowTime >= lic.goodAfterDate && nowTime <= lic.goodBeforeDate)

				}

				Map stackedlicense = licenseObjs.inject([
						  numberOfLicenses: 0,
						  goodAfterDate:    null,
						  goodBeforeDate:   null,
						  subject: null,
						  productKey: null,
				]) { Map stackLic, License lic ->
					[
							  numberOfLicenses: stackLic.numberOfLicenses + lic.numberOfLicenses,
							  goodAfterDate: (! stackLic.goodAfterDate ) ? lic.goodAfterDate :
									            Math.max(stackLic.goodAfterDate, lic.goodAfterDate),
							  goodBeforeDate: (! stackLic.goodBeforeDate ) ? lic.goodBeforeDate :
									            Math.min(stackLic.goodBeforeDate, lic.goodBeforeDate),
							  subject: stackLic.subject ?: lic.subject,
							  productKey: stackLic.productKey ?: lic.productKey,
					]
				}

				DomainLicense firstLicense = licenses.first()

				licState.numberOfLicenses = stackedlicense.numberOfLicenses
				licState.goodAfterDate = stackedlicense.goodAfterDate ? new Date(stackedlicense.goodAfterDate) : null
				licState.goodBeforeDate = stackedlicense.goodBeforeDate ? new Date(stackedlicense.goodBeforeDate) : null
				licState.domainLicId = stackedlicense.productKey

				if (firstLicense) {
					licState.type = firstLicense.type
					licState.banner = firstLicense.bannerMessage
				}

				long numServers = assetEntityService.countServers(project)
				log.debug("NumServers: {}", numServers)

				if( !licenseObjs && licState?.numberOfLicenses == 0 ){
					licState.state = State.UNLICENSED
					String message = "A license is required in order to enable all features of the application.<br/>$numServers servers detected."
					if (errorMessage) {
						message += '<hr/>' + errorMessage
					}
					licState.message = message
					licState.valid = false
					licState.banner = ""
					return licState
				}

				DomainLicense domLicense = DomainLicense.get(licState.domainLicId)
				if ( numServers > licState.numberOfLicenses ) {
					if (! domLicense.lastCompliance) {
						domLicense.lastCompliance = now
						domLicense.save()
					}

					licState.lastCompliantDate = domLicense.lastCompliance

					int gracePeriod = gracePeriodDaysRemaining(gracePeriodDays, licState.lastCompliantDate)
					if( gracePeriod > 0 ) {
						licState.state = State.UNLICENSED // State.NONCOMPLIANT
						licState.message = "The Server count has exceeded the license limit of ${licState.numberOfLicenses} by ${numServers - licState.numberOfLicenses} servers. The application functionality will be limited in ${gracePeriod} days if left unresolved."
						licState.valid = true
						// licState.valid = false
					} else {
						licState.state = State.UNLICENSED // State.INBREACH
						licState.message = "The Server count has exceeded the license limit beyond the grace period. Please reduce the server count below the limit of ${licState.numberOfLicenses} to re-enable all application features."
						licState.valid = false
					}

				} else {
					if ( domLicense.lastCompliance ) {
						domLicense.lastCompliance = null
						domLicense.save()
					}
					licState.state = State.VALID
					licState.message = ""
					licState.lastCompliantDate = now
					licState.valid = true
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
		assert lastCompliantDate != null
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
			log.error("Error loading license data: Wrong License Certificate, Perhaps copied from another server?")
			licenseProvider.remove(id)
			license.status = DomainLicense.Status.CORRUPT
			license.save()
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

		String bannerMessage = jsonData.bannerMessage
		String project       = jsonData.project
		String hostName      = jsonData.hostName
		String websitename   = jsonData.websitename

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

        if (lic.save(flush:true, failOnError: false)) {
        	return lic
        } else {
			String errors = GormUtil.allErrorsString(lic)
			log.error("lic error: {}", errors)
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

		String guid = "N/A"
		Project prj = lic.projectInstance
		if(prj) {
			guid = prj.guid
		}

		if(lic) {
			String body = """
				|from: ${lic.email}
				|Website Name: ${lic.websitename}
				|GUID: ${guid}
				|
				|${lic.toEncodedMessage(grailsApplication)}
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
	boolean sendMailRequest(DomainLicense license){
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

	/**
	 * Used to retrieve Licensing information that can be used by the frontend
	 * @param project - the project to get the current licensing information
	 * @return Map of data
	 */
	Map licenseInfo(Project project) {
		Boolean isManager = isManagerEnabled()
		Boolean isTMLicenseEnabled = ! isManager && isAdminEnabled()

		Map info = [:]

		info.isManager = isManager

		if (isManager) {
			// Instance is LicenseManager
			info.put('isValid', true)
			info.put('state', State.VALID.toString())
			info.put('banner', '')
			info.put('message', '')
		} else {
			// Instance is TransitionManager
			if (isTMLicenseEnabled) {
				info.put('isValid', isValid(project))
				info.put('state', getLicenseState(project).toString())
				info.put('banner', getLicenseBannerMessage(project))
				info.put('message', getLicenseStateMessage(project))
			} else {
				info.put('isValid', true)
				info.put('state', State.VALID.toString())
				info.put('banner', 'Licensing is disabled for Development/QA purpose ONLY')
				info.put('message', '')
			}
		}
		return info
	}
}
