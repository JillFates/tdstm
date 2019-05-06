import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import grails.util.Metadata
import net.transitionmanager.common.CoreService
import org.grails.config.NavigableMap
import org.grails.core.exceptions.GrailsConfigurationException
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@Integration
@Rollback
class CoreServiceTests extends Specification {

	CoreService coreService

	void "getAppName"() {
		expect: 'Should return the application name'
			coreService.getAppName() == Metadata.current.getApplicationName()
	}

	void "getAppConfig"() {
		expect: 'getAppConfig() should not return null'
		coreService.getAppConfig('tdstm') != null

		when:
		coreService.getAppConfig('bogusAppName')

		then:
			GrailsConfigurationException ex = thrown()
		ex.message.contains('configuration not found')
	}

	void "getConfigSetting"() {
		when:
		def setting = coreService.getConfigSetting('dataSource')

		then: 'Should return ConfigObject valid setting from grails-app/conf/Config.groovy'
		setting instanceof NavigableMap

		when:
		setting = coreService.getConfigSetting('dataSource.driverClassName')

		then: 'Should return String valid setting from grails-app/conf/Config.groovy'
		setting instanceof String
	}

	void "getAppConfigSetting"() {
		when:
		def setting = coreService.getAppConfigSetting('testing.foo.intVal', 'tdstm')

		then: 'Should return Integer valid setting from grails-app/conf/Config.groovy'
		setting == 123
		setting instanceof Integer

		when:
		setting = coreService.getAppConfigSetting('testing.foo.stringVal', 'tdstm')

		then: 'Should return String valid setting from grails-app/conf/Config.groovy'
		setting == 'abc'
		setting instanceof String

		when:
		setting = coreService.getAppConfigSetting('testing.foo.configVal', 'tdstm')

		then: 'Should return ConfigObject valid setting from grails-app/conf/Config.groovy'
		setting instanceof NavigableMap

		when:
		setting = coreService.getAppConfigSetting('testing.foo.man.choo', 'tdstm')

		then: 'Missing settings should return null value'
		setting == null
	}

	void "getEnvironment"() {
		expect:
		coreService.getEnvironment() ==~ /(?i)TEST/
	}
}
