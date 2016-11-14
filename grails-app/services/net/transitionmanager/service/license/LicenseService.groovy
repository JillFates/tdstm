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
	private initialize() {
		if(!tdsPasswordProvider) {
			tdsPasswordProvider = new TDSPasswordProvider(grailsApplication.config.tdstm.license.password)
			licenseProvider = new MyLicenseProvider()

			File basePath = new File('.')  //grailsApplication.parentContext.getResource("/..").file
			LicenseCreatorProperties.setPrivateKeyDataProvider(new FilePrivateKeyDataProvider(basePath))
			LicenseCreatorProperties.setPrivateKeyPasswordProvider(tdsPasswordProvider)

			LicenseManagerProperties.setPublicKeyDataProvider(new FilePublicKeyDataProvider(basePath))
			LicenseManagerProperties.setPublicKeyPasswordProvider(tdsPasswordProvider)
			LicenseManagerProperties.setLicenseProvider(licenseProvider)

			// Optional; set only if you are using a different password to encrypt licenses than your public key
			// should we set a different password per client?
			//LicenseManagerProperties.setLicensePasswordProvider(tdsPasswordProvider)

			// Optional; set only if you wish to validate licenses
			//Global validation or per access?
			LicenseManagerProperties.setLicenseValidator(new DefaultLicenseValidator());

			// Optional; defaults to 0, which translates to a 10-second (minimum) cache time
			LicenseManagerProperties.setCacheTimeInMinutes(24*60);

			//Warmup the objects to use...
			License license = new License.Builder().build()
			LicenseCreator.getInstance().signAndSerializeLicense(license)
		}
	}
	public String getInstalationId(){
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

	private String getFQDN(){
		URL url = new URL(grailsApplication.config.grails.serverURL)
		return url.getHost()
	}

	private String getHostName(){
		InetAddress addr = InetAddress.getLocalHost()

		//return addr.getCanonicalHostName() // this returns the IP in case that we cannot reverse check the IP-name
		return addr.getHostName()  //always return the name, no chek performed
	}

	private String getApplicationPath(){
		File layoutFolder = grailsApplication.parentContext.getResource("/").file
		return layoutFolder.absolutePath
	}

	private String getMacAddresses(divider='|'){
		def macs = NetworkInterface.networkInterfaces.collect { iface ->
			iface.hardwareAddress?.encodeHex().toString()
		}

		return macs.join(divider)
	}


	public String generateLicense(String productKey, String holder, String subject, int numberOfLicenses, Date goodAfter, Date goodBefore){
		initialize()

		License.Builder licenseBuilder = new License.Builder().
				withProductKey(productKey). //TM-CORE-XXX
				withIssuer("TDS").
				withHolder(holder). //Partner - Client
				withSubject(subject). //project:<guid> | global
				withIssueDate(new Date().getTime()).
				withGoodAfterDate(goodAfter.getTime()).
				withGoodBeforeDate(goodBefore.getTime()).
				withNumberOfLicenses(numberOfLicenses)

		//Use the name of the license name "project:<guid>"
		License license = licenseBuilder.
				addFeature("environment:[engineering | training | demo | production]").
				addFeature("module:dr").
				addFeature("module:cookbook").
				addFeature("module:core"). //All will have this so maybe is not required
				build()

		byte[] licenseData = LicenseCreator.getInstance().signAndSerializeLicense(license)

		String trns = new String(Base64.encodeBase64(licenseData))

		return trns
	}

	public boolean hasModule(projectGuid, moduleName){

		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("global")
		//License license = manager.getLicense("project:<Guid>")
		//This is only for the current license
		license.hasLicenseForAllFeatures("module:$moduleName")
	}

	//public boolean isValid(projectGuid, featureName){
	public boolean isValid(){
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

	public void load(String license){
		initialize()
		LicenseManager manager = LicenseManager.getInstance()
		licenseProvider.addLicense(license)
	}

	public void useLicense() {
		initialize()
		LicenseManager manager = LicenseManager.getInstance()
		License license = manager.getLicense("")
		try {
			manager.validateLicense(license)
		//} catch(ExpiredLicenseException | InvalidLicenseException e) {
		} catch(Exception e) {
			log.error("Error Validating license: ${e}")
			return
		}


		int seats = license.getNumberOfLicenses()
		log.info("SEATS: $seats")

	}

}
