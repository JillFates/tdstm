package net.transitionmanager.service

import net.nicholaswilliams.java.licensing.SignedLicense
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.transitionmanager.domain.Project
import net.transitionmanager.service.license.prefs.FilePrivateKeyDataProvider
import net.transitionmanager.service.license.prefs.FilePublicKeyDataProvider
import net.transitionmanager.service.license.prefs.MyLicenseProvider
import net.transitionmanager.service.license.prefs.TDSLicenseValidator
import net.transitionmanager.service.license.prefs.TDSPasswordProvider
import net.nicholaswilliams.java.licensing.License
import net.nicholaswilliams.java.licensing.LicenseManager
import net.nicholaswilliams.java.licensing.LicenseManagerProperties
import net.nicholaswilliams.java.licensing.encryption.PasswordProvider
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties

class LicenseAdminService extends LicenseCommonService {
	static transactional = false

	private PasswordProvider tdsPasswordProvider
	private MyLicenseProvider licenseProvider
	LicenseCommonService  licenseCommonService

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
			log.debug("License Config Enabled")
			/** BEGIN: License Common Configuration **/
			File basePath = new File('.')  //grailsApplication.parentContext.getResource("/..").file
			tdsPasswordProvider = new TDSPasswordProvider(grailsApplication.config.tdstm.license.password)
			/** END: License Common Configuration **/

			if(licenseCommonService.isManagerEnabled()) {
				log.debug("License Manager Enabled")
				/** BEGIN: License Manager Configuration **/
				LicenseCreatorProperties.setPrivateKeyDataProvider(new FilePrivateKeyDataProvider(basePath))
				LicenseCreatorProperties.setPrivateKeyPasswordProvider(tdsPasswordProvider)
				LicenseCreator.getInstance()
				/** END: License Manager Configuration **/

				/** BEGIN: TEST MANAGER LICENSE **//*
				String id = "f5e087eb-0ff2-433b-aa4c-04fd3f8dcedb"
				String key = licenseManagerService.getLicenseKey(id)
				log.info("OLB ($id) License Key: $key")
				*//** END: TEST MANAGER LICENSE **/

			}else {
				log.debug("License Admin Enabled")
				/** BEGIN: License Admin Configuration ***/
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
				/** END: License Admin Configuration ***/

				/** BEGIN: TEST CLIENT LICENSE **//*
				LicenseManager manager = LicenseManager.getInstance()
				log.info("OLB: Load License")
				licenseProvider.addLicense("rO0ABXNyADFuZXQubmljaG9sYXN3aWxsaWFtcy5qYXZhLmxpY2Vuc2luZy5TaWduZWRMaWNlbnNlioT/n36yaoQCAAJbAA5saWNlbnNlQ29udGVudHQAAltCWwAQc2lnbmF0dXJlQ29udGVudHEAfgABeHB1cgACW0Ks8xf4BghU4AIAAHhwAAABICRMR4APL4M1cNX0873tLulzM4u0iHsTGjR3+QqdnAB3dVJIGYI15o5rDMfVcO+WtAOnzjhJobAQunl6wniNYvrzBZNYEFX+w/siIxVkVNlI98UL7kXPzWMn/sjM/UvKvKHNCYLdRBD+mpwG/IGo4YSQuxYSOlCx65kB2yHGrSEhqNQqFX5p3+6/hMePjb3ZOgOujYkosrH8Q9xenTv9jeNPdH5xBC8wjcw5HefMJJHO2RlEzuq8otkYdyd4dUEdpTjCvMN3SzUxvwqQEg4RrnGZd+cdV3bcPFFLVx233rpMw74Gdh1YMXLk82v89IRldvh2/7d8pIA5DD2334vb/4mSj8SUrNxYFvLsMnKYm64p0yLQGQGRnjv7dAgf8EQ/6HVxAH4AAwAAAQBXcYEC7z81w9XHS6lotp/ys1Nvnw1pv7F0NPhPS8CstiGdQrSbeiMU4bJ/XosTzI8uV+y4db2uJI8wq2mBoqc/iTrRFgBeEZZ3kuEtlbsywblcKFsuHcuKDEWWQOBiyzhMcb25nuJj/UDSGIl90mHiwl11YtBlbEhvnMvsa8fWOBlVE5SZgbebAs5Yf8D8ACf1bkSzf1iv1m8Op6bMcmQRYFaXtf/CD0CKyVjK9S2UfimmKQ9sse8b6zsBgvDrlBjMP+itZxY7tIflwkZhdIbIbxTRVco4Gey1GHVhMWg5UYJuMKEidpBtBGDaAqHytG1oBQ9aNoAjnLvnfTXGXf+L")
				log.info("OLB: Loaded")
				License lic = manager.getLicense("") //set the license to test
				log.info "License loaded (${lic.productKey} ${lic.issuer})? ${lic.goodAfterDate} - ${lic.goodBeforeDate}"
				*//** END: TEST CLIENT LICENSE **/
			}
		}

	}

	def isEnabled(){
		return licenseCommonService.isLicenseAdmin()
	}

	boolean hasModule(projectGuid, moduleName){

		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("global")
		//License license = manager.getLicense("project:<Guid>")
		//This is only for the current license
		license.hasLicenseForAllFeatures("module:$moduleName")
	}

	//boolean isValid(projectGuid, featureName){
	boolean isValid(Project project){
		initialize()

		//Is licence check disabled then is always valid
		if (!(grailsApplication.config.tdstm.license.enabled)) {
			return true
		}

		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("")
		//manager.hasLicenseForAnyFeature("project:guid")

		if(license == null){
			return false
		}

		long dateTime = new Date().getTime()

		if(dateTime >= license.goodAfterDate && dateTime <= license.goodBeforeDate){
			return true
		}else{
			return false
		}
	}

	boolean isLicenseComplient(Project project){
		return isValid(project)
	}

	void load(String license){
		initialize()
		LicenseManager manager = LicenseManager.getInstance()
		licenseProvider.addLicense(license)
	}

	License useLicense() {
		initialize()
		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("") //All
		try {
			manager.validateLicense(license)
		//} catch(ExpiredLicenseException | InvalidLicenseException e) {
		} catch(Exception e) {
			log.error("Error Validating license: ${e}")
			return
		}


		//int seats = license.getNumberOfLicenses()
		license.getNumberOfLicenses() //MaxServers

		//log.info("SEATS: $seats")

		return license

	}

}
