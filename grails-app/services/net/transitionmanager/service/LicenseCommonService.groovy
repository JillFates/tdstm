package net.transitionmanager.service

import groovy.util.logging.Slf4j
import grails.core.GrailsApplication

/**
 * Created by octavio on 12/27/16.
 */
@Slf4j
class LicenseCommonService {
	/* TAG grapper for messaging */
	public static BEGIN_LIC_TAG = "-----BEGIN LICENSE-----"
	public static END_LIC_TAG = "-----END LICENSE-----"

	GrailsApplication grailsApplication

	//Singleton cached data
	private String fqdn

	/**
	 * @deprecate
	 * Is not in actual USE....
	 * Get the Installation ID from the current Instance
	 * Right now this is performed by concatenating the hostname + '|' + fqdn
	 * as a hardware key, this algorithm can be changed later to something else
	 * @return String with the installation id
	 */
	String getInstallationId(){
		String hostname = getHostName()
		String fqdn = getFQDN()

		String hwkey = "${hostname}|${fqdn}"
		String md5key = hwkey.encodeAsMD5()

		log.debug("***************************************************************************")
		log.debug("hwkey: $hwkey")
		log.debug("md5key: $md5key")
		log.debug("***************************************************************************")

		return md5key
	}

	/**
	 * return the Fully Qualified Domain Name (FQDN) of the server
	 * @return String representing the FQDN
	 */
	synchronized
	String getFQDN(){
		if(!fqdn) {
			// oluna: this should be always in the configuration, but let's play safe
			String serverURL = grailsApplication.config.grails.serverURL ?:
								"http://${InetAddress.getLocalHost().getHostName()}"

			URL url = new URL(serverURL)
			fqdn = url.getHost()
		}
		return fqdn
	}

	/**
	 * return the hostname of the current instance
	 * @return String representing the hostname
	 */
	String getHostName(){
		InetAddress addr = InetAddress.getLocalHost()

		//return addr.getCanonicalHostName() // this returns the IP in case that we cannot reverse check the IP-name
		return addr.getHostName()  //always return the name, no check performed
	}

	/**
	 * Get the application  installation Path (Even in Tomcat)
	 * Not in current use
	 * @return
	 */
	String getApplicationPath(){
		File layoutFolder = grailsApplication.parentContext.getResource("/").file
		return layoutFolder.absolutePath
	}

	/**
	 * Get the MacAddresses (All interfaces) of the current installation
	 * Not in current use
	 * @param divider
	 * @return
	 */
	String getMacAddresses(String divider='|'){
		List<String> macs = NetworkInterface.networkInterfaces.collect { iface ->
			iface.hardwareAddress?.encodeHex().toString()
		}

		return macs.join(divider)
	}

	/**
	 * Is the License Manager enabled for this instance?
	 * @return
	 */
	boolean isManagerEnabled(){
		return grailsApplication.config.manager?.license?.enabled
	}

	/**
	 * Is the License Admin capabilities enabled for this instance?
	 * Enforce the licensing!, by default if the config property is not present (NULL) the license is enabled
	 * @return true if the licensing should be enabled
	 */
	boolean isAdminEnabled(){
		boolean enabled = true

		ConfigObject licenseConfig = grailsApplication.config.tdstm?.license

		if(licenseConfig != null && licenseConfig.isSet("enabled")){
			enabled = licenseConfig.enabled
		}

		return enabled
	}

	/**
	 * Is the License Manager Generator enabled for this instance?
	 * @return
	 */
	boolean isLGen(){
		return isManagerEnabled() || (grailsApplication.config.manager?.license?.back)
	}
}
