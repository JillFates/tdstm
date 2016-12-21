package net.transitionmanager.service.license

import net.transitionmanager.service.license.prefs.FilePrivateKeyDataProvider
import net.transitionmanager.service.license.prefs.FilePublicKeyDataProvider
import net.transitionmanager.service.license.prefs.MyLicenseProvider
import net.transitionmanager.service.license.prefs.TDSPasswordProvider
import net.nicholaswilliams.java.licensing.DefaultLicenseValidator
import net.nicholaswilliams.java.licensing.License
import net.nicholaswilliams.java.licensing.LicenseManager
import net.nicholaswilliams.java.licensing.LicenseManagerProperties
import net.nicholaswilliams.java.licensing.encryption.PasswordProvider
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties
import org.apache.commons.codec.binary.Base64
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.codecs.MD5Codec

class LicenseService {
	static transactional = false

	GrailsApplication grailsApplication
	private PasswordProvider tdsPasswordProvider
	private MyLicenseProvider licenseProvider

	/**
	 * Initialize the license service
	 * @return
	 */
	def initialize() {
		if(isEnabled() && !tdsPasswordProvider) {
			tdsPasswordProvider = new TDSPasswordProvider(grailsApplication.config.tdstm.license.password)
			licenseProvider = new MyLicenseProvider()

			File basePath = new File('.')  //grailsApplication.parentContext.getResource("/..").file

			//Manager License Generator
			LicenseCreatorProperties.setPrivateKeyDataProvider(new FilePrivateKeyDataProvider(basePath))
			LicenseCreatorProperties.setPrivateKeyPasswordProvider(tdsPasswordProvider)

			LicenseManagerProperties.setPublicKeyDataProvider(new FilePublicKeyDataProvider(basePath))
			LicenseManagerProperties.setPublicKeyPasswordProvider(tdsPasswordProvider)
			LicenseManagerProperties.setLicenseProvider(licenseProvider)

			// Optional; set only if you are using a different password to encrypt licenses than your key
			// should we set a different password per client?
			LicenseManagerProperties.setLicensePasswordProvider(tdsPasswordProvider)

			// Optional; set only if you wish to validate licenses
			//Global validation or per access?
			LicenseManagerProperties.setLicenseValidator(new DefaultLicenseValidator())

			// Optional; defaults to 0, which translates to a 10-second (minimum) cache time
			LicenseManagerProperties.setCacheTimeInMinutes(24*60)

			LicenseManager.getInstance()
			//Warmup the objects to use...
			//License license = new License.Builder().build()
			//LicenseCreator.getInstance().signAndSerializeLicense(license)


			//License Loader
			//LicenseManager manager = LicenseManager.getInstance()
			//log.info("OLB: Load License")
			//licenseProvider.addLicense("rO0ABXNyADFuZXQubmljaG9sYXN3aWxsaWFtcy5qYXZhLmxpY2Vuc2luZy5TaWduZWRMaWNlbnNlioT/n36yaoQCAAJbAA5saWNlbnNlQ29udGVudHQAAltCWwAQc2lnbmF0dXJlQ29udGVudHEAfgABeHB1cgACW0Ks8xf4BghU4AIAAHhwAAABICRMR4APL4M1cNX0873tLulzM4u0iHsTGjR3+QqdnAB3dVJIGYI15o5rDMfVcO+WtAOnzjhJobAQunl6wniNYvrzBZNYEFX+w/siIxVkVNlI98UL7kXPzWMn/sjM/UvKvKHNCYLdRBD+mpwG/IGo4YSQuxYSOlCx65kB2yHGrSEhqNQqFX5p3+6/hMePjb3ZOgOujYkosrH8Q9xenTv9jeNPdH5xBC8wjcw5HefMJJHO2RlEzuq8otkYdyd4dUEdpTjCvMN3SzUxvwqQEg4RrnGZd+cdV3bcPFFLVx233rpMw74Gdh1YMXLk82v89IRldvh2/7d8pIA5DD2334vb/4mSj8SUrNxYFvLsMnKYm64p0yLQGQGRnjv7dAgf8EQ/6HVxAH4AAwAAAQBXcYEC7z81w9XHS6lotp/ys1Nvnw1pv7F0NPhPS8CstiGdQrSbeiMU4bJ/XosTzI8uV+y4db2uJI8wq2mBoqc/iTrRFgBeEZZ3kuEtlbsywblcKFsuHcuKDEWWQOBiyzhMcb25nuJj/UDSGIl90mHiwl11YtBlbEhvnMvsa8fWOBlVE5SZgbebAs5Yf8D8ACf1bkSzf1iv1m8Op6bMcmQRYFaXtf/CD0CKyVjK9S2UfimmKQ9sse8b6zsBgvDrlBjMP+itZxY7tIflwkZhdIbIbxTRVco4Gey1GHVhMWg5UYJuMKEidpBtBGDaAqHytG1oBQ9aNoAjnLvnfTXGXf+L")
			//log.info("OLB: Loaded")
			//licenseProvider.getLicense("")
			log.info("License Service Initialized")
		}
	}

	def isEnabled(){
		return grailsApplication.config.tdstm.license.enabled
	}

	//Installation  <--
	String getInstalationId(){
		String hostname = getHostName()
		String fqdn = getFQDN()

		def hwkey = "${hostname}|${fqdn}"
		def md5key = MD5Codec.encode(hwkey)

		log.debug("***************************************************************************")
		log.info("hwkey: $hwkey")
		log.debug("md5key: $md5key")
		log.debug("***************************************************************************")

		return md5key
	}

	String getFQDN(){
		URL url = new URL(grailsApplication.config.grails.serverURL)
		return url.getHost()
	}

	String getHostName(){
		InetAddress addr = InetAddress.getLocalHost()

		//return addr.getCanonicalHostName() // this returns the IP in case that we cannot reverse check the IP-name
		return addr.getHostName()  //always return the name, no check performed
	}



	/**
	 * Not in current use
	 * @return
	 */
	private String getApplicationPath(){
		File layoutFolder = grailsApplication.parentContext.getResource("/").file
		return layoutFolder.absolutePath
	}

	/**
	 * Not in current use
	 * @param divider
	 * @return
	 */
	private String getMacAddresses(divider='|'){
		def macs = NetworkInterface.networkInterfaces.collect { iface ->
			iface.hardwareAddress?.encodeHex().toString()
		}

		return macs.join(divider)
	}

	boolean hasModule(projectGuid, moduleName){

		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("global")
		//License license = manager.getLicense("project:<Guid>")
		//This is only for the current license
		license.hasLicenseForAllFeatures("module:$moduleName")
	}

	//boolean isValid(projectGuid, featureName){
	boolean isValid(){
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
