/**
 * CoreService contain service methods considered core to the application functionality
 */

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException

class CoreService {
	
	def grailsApplication

	/** 
	 * Used to retrieve the name of the application
	 * @return the application name
	 * @throws GrailsConfigurationException if application name is not found
	 */
	String getAppName() {
		//String name = grailsApplication.getMetadata()['app.name']
		String name = grailsApplication.metadata['app.name']
		log.debug "getAppName() name=$name"
		if (!name) {
			throw new GrailsConfigurationException("Unable to determine application name")
		}
		return name
	}

	/**
	 * Used to retrieve the entire set of application configuration settings
	 * @param overrideAppNameForTesting - only used for testing
	 * @return The property setting or null if not defined
	 * @throws GrailsConfigurationException if application name or app configuration not found
	 */
	def getAppConfig(String overrideAppNameForTesting=null) {
		String appName = overrideAppNameForTesting ?: getAppName()
		if (!grailsApplication.config.containsKey(appName)) {
			throw new GrailsConfigurationException("Application $appName configuration not found")
		}
		def config = grailsApplication.config[appName]
		return config
	}

	/**
	 * Used to retrieve a particular application configuration setting
	 * @param setting - the dot name of the property (e.g. 'security.ldap')
	 * @return The property setting or null if not defined
	 * @throws GrailsConfigurationException if application name or app configuration not found
	 */
	def getAppConfigSetting(String setting) {
		// TODO : JPM 3/2016 : Change code to use isSet when upgrading to Grails 2.5 or later
		def appConfig = getAppConfig()

		// Iterate over the list of the property dot naming convention to see that the property is set
		List settingList = setting.split(/\./)
		for (int i=0; i < settingList.size() - 1; i++) {
			if (! appConfig.containsKey(settingList[i])) {
				return null
			} 
			appConfig = appConfig[settingList[i]]
		}
		appConfig = appConfig[ settingList[-1] ]
		if ( (appConfig instanceof groovy.util.ConfigObject) && appConfig.isEmpty()) {
			appConfig = null
		}
		return appConfig
	}

	/**
	 * Used to return the current runtime environment
	 */
	def getEnvironment() {
		return grails.util.Environment.current.toString()
	}
}