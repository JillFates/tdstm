package net.transitionmanager.service

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.codecs.MD5Codec

/**
 * Created by octavio on 12/27/16.
 */
@Slf4j(value='log', category='net.transitionmanager.service.LicenseCommon')
class LicenseCommonService {
	GrailsApplication grailsApplication

	/**
	 * Get the Installation ID from the current Instance
	 * Right now this is performed by concatenating the hostname + '|' + fqdn
	 * as a hardware key, this algorithm can be changed later to something else
	 * @return String with the installation id
	 */
	String getInstallationId(){
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

	/**
	 * return the Fully Qualified Domain Name (FQDN) of the server
	 * @return String representing the FQDN
	 */
	String getFQDN(){
		URL url = new URL(grailsApplication.config.grails.serverURL)
		return url.getHost()
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
	String getMacAddresses(divider='|'){
		def macs = NetworkInterface.networkInterfaces.collect { iface ->
			iface.hardwareAddress?.encodeHex().toString()
		}

		return macs.join(divider)
	}

	/**
	 * Is the License Manager enabled for this instance?
	 * @return
	 */
	boolean isManagerEnabled(){
		return (grailsApplication.config.tdstm?.license?.manager?.enabled)?true:false
	}

	/**
	 * Is the License Admin capabilities enabled for this instance?
	 * @return
	 */
	boolean isAdminEnabled(){
		return (grailsApplication.config.tdstm?.license?.enabled)?true:false
	}
}
