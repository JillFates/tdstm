package net.transitionmanager.service

import com.tdsops.common.exceptions.InvalidLicenseException
import grails.converters.JSON
import net.nicholaswilliams.java.licensing.License
import net.nicholaswilliams.java.licensing.LicenseManager
import net.nicholaswilliams.java.licensing.LicenseManagerProperties
import net.nicholaswilliams.java.licensing.encryption.PasswordProvider
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.transitionmanager.ProjectDailyMetric
import net.transitionmanager.domain.License as DomainLicense
import net.transitionmanager.domain.Project
import net.transitionmanager.service.license.prefs.*
import org.apache.commons.lang.time.DateUtils

class LicenseAdminService extends LicenseCommonService {
	static transactional = false

	static enum State {
		UNLICENSED, TERMINATED, EXPIRED, INBREACH, NONCOMPLIANT, VALID
	}

	private PasswordProvider tdsPasswordProvider
	private MyLicenseProvider licenseProvider

	static final String CACHE_NAME = "LIC_STATE"
	CacheManager licenseCache = CacheManager.getInstance()

	AssetEntityService assetEntityService
	LicenseCommonService  licenseCommonService
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

		if(isEnabled() && !tdsPasswordProvider) {

			if(!licenseCache.getCache(CACHE_NAME)) {
				log.debug("configuring cache")
				int ttl = 15 * 60  //TODO: 20170124 Move to a config variable
				Cache memoryOnlyCache = new Cache(CACHE_NAME, 200, false, false, ttl, 0)
				licenseCache.addCache(memoryOnlyCache)
			}

			log.debug("License Config Enabled")
			/** BEGIN: License Common Configuration **/
			File basePath = new File('.')  //grailsApplication.parentContext.getResource("/..").file
			tdsPasswordProvider = new TDSPasswordProvider(grailsApplication.config.tdstm.license.password)
			/** END: License Common Configuration **/


			if(licenseCommonService.isLGen()) {
				log.debug("License Manager Enabled")
				// BEGIN: License Manager Configuration //
				LicenseCreatorProperties.setPrivateKeyDataProvider(new FilePrivateKeyDataProvider(basePath))
				LicenseCreatorProperties.setPrivateKeyPasswordProvider(tdsPasswordProvider)
				LicenseCreator.getInstance()
				// END: License Manager Configuration //

				// BEGIN: TEST MANAGER LICENSE //
				// String id = "f5e087eb-0ff2-433b-aa4c-04fd3f8dcedb"
				// String key = licenseManagerService.getLicenseKey(id)
				// log.debug("OLB ($id) License Key: $key")
				// END: TEST MANAGER LICENSE //
			}

			if(licenseCommonService.isAdminEnabled()) {
				log.debug("License Admin Enabled")
				// BEGIN: License Admin Configuration //
				licenseProvider = new MyLicenseProvider()
				LicenseManagerProperties.setPublicKeyDataProvider(new FilePublicKeyDataProvider(basePath))
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
		return licenseCommonService.isAdminEnabled()
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
		Map licState = [:]

		//Is licence check disabled then is always valid
		if (!licenseCommonService.adminEnabled) {
			licState.state = State.VALID
			licState.message = "License message"
			licState.valid = true
			licState.banner = "License banner"
			return licState
		}

		if(project == null){
			project = securityService.userCurrentProject
		}

		//If the current project is null return true
		if(project == null) return true

		String projectId = project.id

		// Attempt to load the license from the EhCache
		Cache cache = licenseCache.getCache(CACHE_NAME)
		def cacheEl = cache.get(projectId)

		licState = cacheEl?.getObjectValue()
		licState = null  //testing proposes

		log.debug("OLB: ${licState}")
		// If the license wasn't in the cache then one will be created and
		// added to the cache
		if(!licState) {
			licState = [:]
			cache.put(new Element(projectId, licState))
			List<DomainLicense> licenses = DomainLicense.findAllByProject(projectId) //dateCreated?
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
				def jsonData = JSON.parse(licObj.subject)
				int gracePeriodDays = jsonData.gracePeriodDays

				String projectName	   = JSON.parse(jsonData.project)?.name
				log.debug("Lic: " + licObj.productKey)
				log.debug("jsonData: " + jsonData)
				log.debug("projectName : " + projectName)
				log.debug("project.name : " + project.name)

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
					log.debug("NumServers: ${numServers}")
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

		log.debug("ID: " + id)
		log.debug("Hash: " + hash)
		licenseProvider.addLicense(id, hash)
		License licObj = manager.getLicense(id)
		log.debug("license.id: " + license.id)
		log.debug("License Obj: " + licObj)
		log.debug("License ID: " + licObj?.productKey)

		def jsonData = JSON.parse(licObj.subject)
		//String installationNum = jsonData.installationNum
		//int gracePeriodDays	   = jsonData.gracePeriodDays
		String bannerMessage   = jsonData.bannerMessage
		String project		   = jsonData.project
		String hostName 	   = jsonData.hostName
		String websitename 	   = jsonData.websitename

		log.debug("LicenseAdminService - Project: " + project)

		if(license.id != licObj.productKey){
			throw new RuntimeException("Error loading licence data: Wrong product Key")
		}

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
}
