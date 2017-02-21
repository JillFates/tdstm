package net.transitionmanager.service

import com.tdsops.common.exceptions.InvalidLicenseException
import grails.converters.JSON
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
import org.apache.commons.lang.time.DateUtils
import net.transitionmanager.domain.PartyGroup
import org.springframework.core.io.Resource


@Slf4j
class LicenseAdminService extends LicenseCommonService {
	static transactional = false

	static enum State {
		UNLICENSED, TERMINATED, EXPIRED, INBREACH, NONCOMPLIANT, VALID
	}
	static final String CACHE_NAME = "LIC_STATE"
	CacheManager licenseCache = CacheManager.getInstance()

	private boolean loaded = false
	AssetEntityService assetEntityService
	SecurityService	securityService

	/**
	 * Initialize the license service
	 * @return
	 */
	def initialize() {
		/* //Using the enviromnment variable
		def tdslm = System.getenv("TDS_LM")
		if(tdslm){
			grailsApplication.config.tdstm.license.enabled = true
		}
		*/

		log.info("LM is Enabled?: ${isEnabled()} && !loaded: ${!loaded}")
		if(isEnabled() && !loaded) {
			loaded = true
			MyLicenseProvider licenseProvider = MyLicenseProvider.getInstance()

			if(!licenseCache.getCache(CACHE_NAME)) {
				log.debug("configuring cache")
				int ttl = 15 * 60  //TODO: 20170124 Move to a config variable
				Cache memoryOnlyCache = new Cache(CACHE_NAME, 200, false, false, ttl, 0)
				licenseCache.addCache(memoryOnlyCache)
			}

			log.info("load LM?: ${isLGen()}")
			if(isLGen()) {
				log.info("License Manager Enabled")
				String keyFile = grailsApplication.config.manager?.license?.key
				String password = grailsApplication.config.manager?.license?.password
				log.info("Manager Key: '{}', password: '{}'", keyFile, password)

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
				//log.info("OLB ($id) License Key: $key")
				// END: TEST MANAGER LICENSE //
			}

			if(isAdminEnabled() && !isManagerEnabled()) {
				log.debug("License Admin Enabled")
				String keyFile = grailsApplication.config.tdstm.license.key
				String password = grailsApplication.config.tdstm.license.password
				log.debug("Admin Key: '{}', password: '{}'", keyFile, password)

				File file = new File(keyFile)

				//if the file doesn't exists load from the web-app resources
				if(!file.exists()){
					Resource resource = grailsApplication.parentContext.getResource(keyFile)
					if(resource.exists()) {
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
				// END: License Admin Configuration //

				// BEGIN: TEST CLIENT LICENSE //
				// LicenseManager manager = LicenseManager.getInstance()
				// log.debug("OLB: Load License")
				// licenseProvider.addLicense("tst", "rO0ABXNyADFuZXQubmljaG9sYXN3aWxsaWFtcy5qYXZhLmxpY2Vuc2luZy5TaWduZWRMaWNlbnNlioT/n36yaoQCAAJbAA5saWNlbnNlQ29udGVudHQAAltCWwAQc2lnbmF0dXJlQ29udGVudHEAfgABeHB1cgACW0Ks8xf4BghU4AIAAHhwAAABICRMR4APL4M1cNX0873tLulzM4u0iHsTGjR3+QqdnAB3dVJIGYI15o5rDMfVcO+WtAOnzjhJobAQunl6wniNYvrzBZNYEFX+w/siIxVkVNlI98UL7kXPzWMn/sjM/UvKvKHNCYLdRBD+mpwG/IGo4YSQuxYSOlCx65kB2yHGrSEhqNQqFX5p3+6/hMePjb3ZOgOujYkosrH8Q9xenTv9jeNPdH5xBC8wjcw5HefMJJHO2RlEzuq8otkYdyd4dUEdpTjCvMN3SzUxvwqQEg4RrnGZd+cdV3bcPFFLVx233rpMw74Gdh1YMXLk82v89IRldvh2/7d8pIA5DD2334vb/4mSj8SUrNxYFvLsMnKYm64p0yLQGQGRnjv7dAgf8EQ/6HVxAH4AAwAAAQBXcYEC7z81w9XHS6lotp/ys1Nvnw1pv7F0NPhPS8CstiGdQrSbeiMU4bJ/XosTzI8uV+y4db2uJI8wq2mBoqc/iTrRFgBeEZZ3kuEtlbsywblcKFsuHcuKDEWWQOBiyzhMcb25nuJj/UDSGIl90mHiwl11YtBlbEhvnMvsa8fWOBlVE5SZgbebAs5Yf8D8ACf1bkSzf1iv1m8Op6bMcmQRYFaXtf/CD0CKyVjK9S2UfimmKQ9sse8b6zsBgvDrlBjMP+itZxY7tIflwkZhdIbIbxTRVco4Gey1GHVhMWg5UYJuMKEidpBtBGDaAqHytG1oBQ9aNoAjnLvnfTXGXf+L")
				// log.debug("OLB: Loaded")
				// License lic = manager.getLicense("tst") //set the license to test
				// log.debug("License loaded (${lic.productKey} ${lic.issuer})? ${lic.goodAfterDate} - ${lic.goodBeforeDate}")
				// END: TEST CLIENT LICENSE //
			}
		}

	}

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

	String getLicenseBannerMessage(Project project = null){
		return getLicenseStateMap(project)?.banner
	}

	State getLicenseState(Project project = null){
		return getLicenseStateMap(project)?.state
	}

	boolean hasModule(String projectGuid, String moduleName){
		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("global")
		//License license = manager.getLicense("project:<Guid>")
		//This is only for the current license
		license.hasLicenseForAllFeatures("module:$moduleName")
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

	void checkValidForLicense(Project project = null) throws InvalidLicenseException{
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
	Map getLicenseStateMap(Project project = null){
		initialize()

		Map defaultValidState = [
			state	: State.VALID,
			message : "",
			valid 	: true,
			banner 	: ""
		]

		//Is licence check disabled then is always valid
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
		licState = null  //testing proposes

		log.debug("OLB: {}",licState)
		// If the license wasn't in the cache then one will be created and
		// added to the cache
		if(!licState) {
			licState = [:]
			cache.put(new Element(projectId, licState))
			List<DomainLicense> licenses = DomainLicense.findAllByProjectAndStatus(projectId, DomainLicense.Status.ACTIVE) //dateCreated?
			//TODO: iterate over the licenses to find the one that fits
			DomainLicense license = licenses.find { it.hash }

			//Validate that the license in the DB has not been compromised
			//LicenseManager manager = LicenseManager.getInstance()
			//License licObj = manager.getLicense(license.id)

			if (!license || !license.hash) {
				// UNLICENSED
				licState.state = State.UNLICENSED
				licState.message = "A license is required in order to enable all features of the application."
				licState.valid = false
				licState.banner = ""
			}else {
				License licObj = getLicenseObj(license)
				if(licObj == null){
					licState.state = State.UNLICENSED
					licState.message = "A license is required in order to enable all features of the application."
					licState.valid = false
					licState.banner = ""
					return licState
				}


				def jsonData = JSON.parse(licObj.subject)
				int gracePeriodDays = jsonData.gracePeriodDays

				String projectName	   = JSON.parse(jsonData.project)?.name
				log.debug("Lic: {}", licObj.productKey)
				log.debug("jsonData: {}", jsonData)
				log.debug("projectName : {}", projectName)
				log.debug("project.name : {}", project.name)

				licState.banner = license.bannerMessage

				//The Name still the same?
				if(projectName != project.name){
					licState.message = "The name of the project was changed but must be '${projectName}' for license compliance"
					licState.valid = false
					licState.banner = ""
					return licState
				}


				Date now = new Date()
				long nowTime = now.getTime()
				int max = licObj.numberOfLicenses;

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
					licState.message = "The license has expired. A new license is required in order to enable all features of the application."
					licState.valid = false
				}
			}
		}

		return licState
	}

	boolean isLicenseCompliant(Project project){
		Map licState = getLicenseStateMap(project)
		return licState.state != State.NONCOMPLIANT
	}


	int gracePeriodDaysRemaining(int gracePeriodDays=5, Date lastCompliantDate){
		lastCompliantDate = lastCompliantDate ?: new Date()
		Date graceDate = DateUtils.addDays(lastCompliantDate, gracePeriodDays)
		Date now = new Date()
		return (graceDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
	}

	/**
	 * Comments, comments, comments
	 * @param license
	 * @return
	 */
	boolean load(DomainLicense license){
		initialize()
		String id = license.id
		String hash = license.hash

		//strip the actual license from the envelope
		String beginTag = LicenseCommonService.BEGIN_LIC_TAG
		String endTag = LicenseCommonService.END_LIC_TAG
		def idxB = hash.indexOf(beginTag)
		if(idxB >= 0){
			def idxE = hash.indexOf(endTag)
			if(idxE < 0){
				throw new RuntimeException("Malformed Message", "Missing ${endTag} tag for request")
			}
			hash = hash.substring(idxB + beginTag.length(), idxE)
		}
		hash = hash.trim()

		LicenseManager manager = LicenseManager.getInstance()
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
			throw new RuntimeException("Error loading licence data: Wrong product Key")
		}

		def jsonData = JSON.parse(licObj.subject)
		//String installationNum = jsonData.installationNum
		//int gracePeriodDays	   = jsonData.gracePeriodDays
		String bannerMessage   = jsonData.bannerMessage
		String project		   = jsonData.project
		String hostName 	   = jsonData.hostName
		String websitename 	   = jsonData.websitename

		log.debug("LicenseAdminService - Project: " + project)

		//if is not wildcard and Sites don't match, FAIL
		if(DomainLicense.WILDCARD != hostName && getHostName() != hostName){
			log.debug("[${getHostName()}] == [$hostName]")
			throw new RuntimeException("Error loading licence data: Wrong Host Name")
		}

		//if is not wildcard and sitename don't match, FAIL
		if(DomainLicense.WILDCARD != websitename && getFQDN() != websitename){
			log.debug("[${getFQDN()}] == [$websitename]")
			throw new RuntimeException("Error loading licence data: Wrong Website Name")
		}

		//Refreshing database license properties with the actual values from the Manager License, Keep client honest

		license.hash = hash
		license.max = licObj.numberOfLicenses
		license.bannerMessage = bannerMessage
		license.activationDate = new Date(licObj.goodAfterDate)
		license.expirationDate = new Date(licObj.goodBeforeDate)

		//def servers = assetEntityService.countServers()
		//log.debug("TOTAL SERVERS: " + servers)

		license.status = DomainLicense.Status.ACTIVE
		return license.save()

	}

	private License getLicenseObj(DomainLicense license){
		load(license)
		LicenseManager manager = LicenseManager.getInstance()
		return manager.getLicense(license.id)
	}



//
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
    def License generateRequest(String uuid, PartyGroup owner, String email, Long environmentId, def projectId, String requestNote ){
		DomainLicense lic

		if(uuid){
			lic = DomainLicense.get(uuid)
		}else{

			lic = new DomainLicense()
			lic.owner = owner
			lic.requestDate = new Date()
			lic.status = DomainLicense.Status.PENDING
			lic.method = DomainLicense.Method.MAX_SERVERS
			lic.installationNum = getInstallationId()
			lic.hostName = hostName
			lic.websitename = FQDN
		}

		lic.email = email
		lic.environment = DomainLicense.Environment.forId(environmentId.intValue())
		lic.project = projectId
		lic.requestNote = requestNote

		if(lic.project != "all"){
			lic.type = DomainLicense.Type.SINGLE_PROJECT
			def project = Project.get(lic.project)
			if(project !=  null) {
				def client = project.client
				lic.installationNum = "${lic.installationNum}|${project.name}|${client.name}"
			}else{
				lic.errors.rejectValue("project", "Project (id:${lic.project}) not found")
			}
		}else{
			lic.type = DomainLicense.Type.MULTI_PROJECT
		}

		if (lic.save(flush:true)) {
			return lic
		}else{
			lic.errors.each {    // TODO GormUtil.allErrorsString
				log.error("lic error: {}",  it)
			}
			throw new DomainUpdateException("Error while creating License Request")
		}

    }



    // TODO validate that the user has access to the license - check with Octavio
    /**
     * Deletes a License Request.
     *
     *
     * @param uuid - the id of the License.
     * @return true if the license was successfully deleted. false if the license does not exist.
     *
     */
    boolean deleteLicense(String uuid){

		DomainLicense lic

        if(uuid) {
            lic = DomainLicense.get(uuid)
        }
        else {
            return false;
        }

        if(lic) {
            lic.delete()
            return true
        }else{
            return false
        }
    }



}