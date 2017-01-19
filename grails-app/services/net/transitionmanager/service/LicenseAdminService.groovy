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

		log.debug("computing Seals")
		computeSeals()

		if(isEnabled() && !tdsPasswordProvider) {

			if(!licenseCache.getCache(CACHE_NAME)) {
				log.debug("configuring cache")
				int ttl = 15 * 60
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
				// log.info("OLB ($id) License Key: $key")
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
				// log.info("OLB: Load License")
				// licenseProvider.addLicense("tst", "rO0ABXNyADFuZXQubmljaG9sYXN3aWxsaWFtcy5qYXZhLmxpY2Vuc2luZy5TaWduZWRMaWNlbnNlioT/n36yaoQCAAJbAA5saWNlbnNlQ29udGVudHQAAltCWwAQc2lnbmF0dXJlQ29udGVudHEAfgABeHB1cgACW0Ks8xf4BghU4AIAAHhwAAABICRMR4APL4M1cNX0873tLulzM4u0iHsTGjR3+QqdnAB3dVJIGYI15o5rDMfVcO+WtAOnzjhJobAQunl6wniNYvrzBZNYEFX+w/siIxVkVNlI98UL7kXPzWMn/sjM/UvKvKHNCYLdRBD+mpwG/IGo4YSQuxYSOlCx65kB2yHGrSEhqNQqFX5p3+6/hMePjb3ZOgOujYkosrH8Q9xenTv9jeNPdH5xBC8wjcw5HefMJJHO2RlEzuq8otkYdyd4dUEdpTjCvMN3SzUxvwqQEg4RrnGZd+cdV3bcPFFLVx233rpMw74Gdh1YMXLk82v89IRldvh2/7d8pIA5DD2334vb/4mSj8SUrNxYFvLsMnKYm64p0yLQGQGRnjv7dAgf8EQ/6HVxAH4AAwAAAQBXcYEC7z81w9XHS6lotp/ys1Nvnw1pv7F0NPhPS8CstiGdQrSbeiMU4bJ/XosTzI8uV+y4db2uJI8wq2mBoqc/iTrRFgBeEZZ3kuEtlbsywblcKFsuHcuKDEWWQOBiyzhMcb25nuJj/UDSGIl90mHiwl11YtBlbEhvnMvsa8fWOBlVE5SZgbebAs5Yf8D8ACf1bkSzf1iv1m8Op6bMcmQRYFaXtf/CD0CKyVjK9S2UfimmKQ9sse8b6zsBgvDrlBjMP+itZxY7tIflwkZhdIbIbxTRVco4Gey1GHVhMWg5UYJuMKEidpBtBGDaAqHytG1oBQ9aNoAjnLvnfTXGXf+L")
				// log.info("OLB: Loaded")
				// License lic = manager.getLicense("tst") //set the license to test
				// log.info "License loaded (${lic.productKey} ${lic.issuer})? ${lic.goodAfterDate} - ${lic.goodBeforeDate}"
				// END: TEST CLIENT LICENSE //
			}
		}

	}

	private computeSeals(){
		ProjectDailyMetric.findAll().each {
			it.seal = it.computeSeal()
			it.save()
		}
	}

	def isEnabled(){
		return licenseCommonService.isAdminEnabled()
	}

	/**
	 * return the license Message for the required project or current if Null
	 * @return the License Message
	 */
	String licenseStateMessage(Project project = null){
		return getLicenseStateMessage(project)
	}

	String getLicenseStateMessage(Project project = null){
		return getLicenseStateMap(project)?.message
	}

	String getLicenseBannerMessage(Project project = null){
		return getLicenseStateMap(project)?.banner
	}

	State getLicenseState(Project project = null){
		return getLicenseStateMap(project)?.state
	}

	boolean hasModule(projectGuid, moduleName){
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

		// Attempt to load the license from the EhCache
		Cache ch = licenseCache.getCache(CACHE_NAME)
		def cacheEl = ch.get(project.id)
		licState = cacheEl?.getObjectValue()

		// If the license wasn't in the cache then one will be created and
		// added to the cache
		if(!licState) {
			licState = [:]
			ch.put(new Element(project.id, licState))
			def licenses = DomainLicense.findAllByProject(project.id)
			def license = licenses.find { it.hash }

			//Validate that the license in the DB has not been compromised
			//LicenseManager manager = LicenseManager.getInstance()
			//License licObj = manager.getLicense(license.id)

			if (!license || !license.hash) {
				// UNLICENSED
				licState.state = State.UNLICENSED
				licState.message = "Your TransitionManager project is not licensed."
				licState.valid = false
				licState.banner = ""
			}else {
				licState.banner = license.bannerMessage
				Date now = new Date()
				log.info("lincense: ${license.id}; [${license.activationDate} - ${license.expirationDate}]; Max: ${license.max}")

				if (now.compareTo(license.activationDate) >= 0 && now.compareTo(license.expirationDate) <= 0) {
					long numServers = assetEntityService.countServers(project)
					log.info("NumServers: ${numServers}")
					if (numServers <= license.max) {
						licState.state = State.VALID
						licState.message = ""
						licState.valid = true
					} else {
						/*
						if(gracePeriodDaysRemaining(license.expirationDate) > 0) {
							licState.state = State.NONCOMPLIANT
							licState.message = "Your TransitionManager project is in grace period."
							licState.valid = true
						}
						*/

						licState.state = State.NONCOMPLIANT
						licState.message = "Your TransitionManager project is no longer compliant with license specifications."
						licState.valid = false
					}
				} else {
					// EXPIRED
					licState.state = State.EXPIRED
					licState.message = "The license for your TransitionManager project has expired."
					licState.valid = false
				}
			}
		}

		return licState
	}

	boolean isLicenseComplient(Project project){
		return isValid(project)
	}


	int gracePeriodDaysRemaining(Date originalDate){
		int maxDays = 5
		Date graceDate = DateUtils.addDays(originalDate, maxDays)
		Date now = new Date()
		return (graceDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
	}

	def load(DomainLicense license){
		initialize()
		String id = license.id
		String hash = license.hash

		String beginTag = DomainLicense.BEGIN_TAG
		String endTag = DomainLicense.END_TAG
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
		licenseProvider.addLicense(id, hash)
		License licObj = manager.getLicense(id)
		log.info( "license.id: " + license.id)
		log.info( "License ID: " + licObj.productKey)

		def jsonData = JSON.parse(licObj.subject)
		String installationNum= jsonData.installationNum
		String bannerMessage  = jsonData.bannerMessage
		String hostName 	  = jsonData.hostName
		String websitename 	  = jsonData.websitename

		if(license.id != licObj.productKey){
			throw new RuntimeException("Error loading licence data: Wrong product Key")
		}
		/*
		if(getInstallationId() != installationNum){
			log.debug("[${getInstallationId()}] == [$installationNum]")
			throw new RuntimeException("Error loading licence data: Wrong Instalation Id")
		}*/
		if(getHostName() != hostName){
			log.debug("[${getHostName()}] == [$hostName]")
			throw new RuntimeException("Error loading licence data: Wrong Host Name")
		}
		if(getFQDN() != websitename){
			log.debug("[${getFQDN()}] == [$websitename]")
			throw new RuntimeException("Error loading licence data: Wrong Website Name")
		}

		license.hash = hash
		license.max = licObj.numberOfLicenses
		license.bannerMessage = bannerMessage
		license.activationDate = new Date(licObj.goodAfterDate)
		license.expirationDate = new Date(licObj.goodBeforeDate)

		def servers = assetEntityService.countServers()
		log.info("TOTAL SERVERS: " + servers)

		license.status = DomainLicense.Status.ACTIVE
		return license.save()

	}

}
