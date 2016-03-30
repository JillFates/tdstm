import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException
import spock.lang.*

class CoreServiceTests extends Specification {

	def coreService	

	def "getAppName"() {
		expect: 'Should return the application name'
			coreService.getAppName() == 'tdstm'
	}		

	def "getAppConfig"() {
		expect: 'getAppConfig() should not return null'
			coreService.getAppConfig() != null

		when:
			coreService.getAppConfig('bogusAppName')
		then:
			GrailsConfigurationException ex = thrown()
   			ex.message.contains('configuration not found')
	}

	def "getAppConfigSetting"() {
		when:
			def setting = coreService.getAppConfigSetting('testing.foo.intVal')
		then: 'Should return Integer valid setting from grails-app/conf/Config.groovy'
			setting == 123
			(setting instanceof Integer)

		when:
			setting = coreService.getAppConfigSetting('testing.foo.stringVal')
		then: 'Should return String valid setting from grails-app/conf/Config.groovy'
			setting == 'abc'
			(setting instanceof String)

		when:
			setting = coreService.getAppConfigSetting('testing.foo.configVal')
		then: 'Should return ConfigObject valid setting from grails-app/conf/Config.groovy'
			(setting instanceof groovy.util.ConfigObject)

		when:
			setting = coreService.getAppConfigSetting('testing.foo.man.choo') 
		then: 'Missing settings should return null value'
			setting == null
	}


	def "getEnvironment"() {
		expect:
			coreService.getEnvironment() == 'TEST'
	}


}
