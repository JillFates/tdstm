package net.transitionmanager.license.prefs

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
	private static MyLicenseProvider INSTANCE

	static MyLicenseProvider getInstance(){
		if(INSTANCE==null){
			synchronized (MyLicenseProvider.class){
				if(INSTANCE==null) INSTANCE= new MyLicenseProvider()
			}
		}
		return INSTANCE
	}

	private Map<Object,String>licenses = [:]
	private MyLicenseProvider(){}

	@Override
	protected byte[] getLicenseData(Object o) {
		log.debug("License to retrieve: {}", o)

		String license = licenses[o]
		if(license) {
			try {
				return license.decodeBase64()
			} catch(e) {
				log.error(e.message)
			}
		}
		return null
	}

	void addLicense(String name, String license){
		synchronized (licenses) {
			licenses[name] = license
			//log.info("licenses size: {}", licenses.size())
		}
	}

	void remove(String name){
		synchronized (licenses) {
			licenses.remove(name)
		}
	}

}
