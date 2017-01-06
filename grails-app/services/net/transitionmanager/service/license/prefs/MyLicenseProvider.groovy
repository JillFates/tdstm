package net.transitionmanager.service.license.prefs

import groovy.util.logging.Slf4j
import net.nicholaswilliams.java.licensing.DeserializingLicenseProvider

/**
 * Created by octavio on 9/8/16.
 *
 * Getting the License
 * If 2 Licenses overlap the Old license precedes the new license
 *
 * We should load only the licenses that are active within the valid activation dates
 * date >= today
 *
 * the map key is the license subject
 * Should we create a license info?
 * [license Table]
 * | subject
 * | activationDate
 * | expirationDate
 * | licenseHash
 */
@Slf4j
class MyLicenseProvider extends DeserializingLicenseProvider{
	Map<Object,String>licenses = [:]

	@Override
	protected byte[] getLicenseData(Object o) {
		log.info("License to retrieve: {}", o)

		String license = licenses[o]
		if(license) {
			return license.decodeBase64()
		}else{
			return null
		}
	}

	public void addLicense(String name, String license){
		licenses[name] = license
	}

}
