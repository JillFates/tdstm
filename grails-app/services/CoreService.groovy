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
		// log.debug "getAppName() name=$name"
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
	 * Used to retrieve any particular configuration setting
	 * @param setting - the dot name of the property (e.g. 'dataSource.url')
	 * @return The property setting or null if not defined
	 * @throws GrailsConfigurationException if application name or app configuration not found
	 */
	def getConfigSetting(String setting) {
		def config = grailsApplication.config
		// Iterate over the list of the property dot naming convention to see that the property is set
		List settingList = setting.split(/\./)
		for (int i=0; i < settingList.size() - 1; i++) {
			if (! config.containsKey(settingList[i])) {
				return null
			} 
			config = config[settingList[i]]
		}
		config = config[ settingList[-1] ]
		if ( (config instanceof groovy.util.ConfigObject) && config.isEmpty()) {
			config = 
			null
		}
		return config
	}

	/**
	 * Used to retrieve an Application specific configuration setting
	 * @param setting - the dot name of the property (e.g. 'security.ldap')
	 * @return The property setting or null if not defined
	 * @throws GrailsConfigurationException if application name or app configuration not found
	 */
	def getAppConfigSetting(String setting) {
		setting = "${getAppName()}.${setting}"
		return getConfigSetting(setting)
	}

	/**
	 * Used to return the current runtime environment
	 */
	def getEnvironment() {
		return grails.util.Environment.current.toString()
	}

	/**
	 * Used to retrieve the configuration setting used to determine the "temp" directory the application
	 * should use for writing files to.
	 */
	String getAppTempDirectory() {
		String propName = 'graph.tmpDir'
		String tmpDir = getConfigSetting(propName)
		if (! tmpDir) {
			throw new GrailsConfigurationException("The application temp directory configuration setting was not found ($propName)")
		}
		if (tmpDir[-1] == '/') {
			tmpDir = tmpDir[0..-2] 
		}
		return tmpDir
	}
}