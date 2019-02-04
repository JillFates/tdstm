package net.transitionmanager.service

import grails.util.Environment
import grails.util.Metadata
import org.grails.core.exceptions.GrailsConfigurationException
import org.grails.config.NavigableMap
import org.springframework.beans.factory.annotation.Value

/**
 * Methods considered core to the application functionality
 */
class CoreService implements ServiceMethods {

	static transactional = false

	// The application property setting to use to determine where temporary files will reside
	// TODO - Refactor the tempDirProperty to not be tied to graph
	static final String tempDirProperty = 'graph.tmpDir'

	@Value('${graph.tmpDir}')
	String temporaryDirectory

	/**
	 * Used to retrieve the name of the application
	 * @throws GrailsConfigurationException if application name is not found
	 */
	String getAppName() {
		String name = Metadata.current.getApplicationName()
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
		if (!config.isSet(appName)) {
			throw new GrailsConfigurationException("Application $appName configuration not found")
		}
		config[appName]
	}

	/**
	 * Used to retrieve any particular configuration setting
	 * @param setting - the dot name of the property (e.g. 'dataSource.url')
	 * @return The property setting or null if not defined
	 * @throws GrailsConfigurationException if application name or app configuration not found
	 */
	def getConfigSetting(String setting) {
		ConfigObject config = getConfig()
		// Iterate over the list of the property dot naming convention to see that the property is set
		List<String> settingList = setting.split(/\./) as List
		for (int i=0; i < settingList.size() - 1; i++) {
			if (!config.isSet(settingList[i])) return null

			def next = config[settingList[i]]
			if (!(next instanceof NavigableMap)) return null
			config = next
		}

		if (!config.isSet(settingList[-1])) {
			return null
		}

		return config[settingList[-1]]
	}

	/**
	 * Used to retrieve an Application specific configuration setting
	 * @param setting - the dot name of the property (e.g. 'security.ldap')
	 * @return The property setting or null if not defined
	 * @throws GrailsConfigurationException if application name or app configuration not found
	 */
	def getAppConfigSetting(String setting) {
		return getConfigSetting(getAppName() + '.' + setting)
	}

	/**
	 * Used to return the current runtime environment
	 */
	String getEnvironment() {
		Environment.current.name
	}

	/**
	 * Used to retrieve the configuration setting used to determine the "temp" directory the application
	 * should use for writing files to.
	 */
	String getAppTempDirectory() {
		String tmpDir = temporaryDirectory
		if (! tmpDir) {
			throw new GrailsConfigurationException("The application temp directory configuration setting was not found ($tempDirProperty)")
		}
		if (tmpDir[-1] == '/') {
			tmpDir = tmpDir[0..-2]
		}
		return tmpDir
	}

	/**
	 * Returns the FQ URL to the application entry point
	 */
	String getApplicationUrl() {
		grailsApplication.config.grails.serverURL
	}


	private ConfigObject getConfig() {
		grailsApplication.config
	}
}
