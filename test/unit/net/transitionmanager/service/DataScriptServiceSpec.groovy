package net.transitionmanager.service

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestFor(DataScriptService)
@TestMixin(GrailsUnitTestMixin)
class DataScriptServiceSpec extends Specification {

	FileSystemService fileSystemService

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
		settingService(SettingService)
	}

	def setup() {
		service.grailsApplication = grailsApplication
		fileSystemService = grailsApplication.mainContext.getBean(FileSystemService)
		assert fileSystemService != null
	}

	def 'test can parse a JSON data using default rootNode'(){

		given:
			String fileContent = '''
				{
				  "assets": [
					{
					  "data": [
						{
						  "collection_type": "SNMP",
						  "cpu_architecture": "Not Collected",
						  "cpu_count": "2",
						  "cpu_frequency": "2100000000",
						  "cpu_model": "GenuineIntel: Intel(R) Xeon(R) CPU E5-2620 v2 @ 2.10GHz",
						  "deviceid": 3232238251,
						  "devicetype": "Virtual-Generic Server",
						  "dist_full": "Not Collected",
						  "hardware_model": "Not Collected",
						  "hardware_serial": "Not Collected",
						  "hardware_vendor": "Not Collected",
						  "hostname": "lab-opensuse",
						  "identifying_mac": "00:50:56:a8:f6:0d"
						}
					  ]
					}
				  ]
				}'''.stripIndent()

		and:
			def (String fileName, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile('unit-test-', 'json')
			fileOutputStream << fileContent
			fileOutputStream.close()

		when:
			Map json = service.parseDataFromJSON(fileName)

		then:
			json

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)

	}

}
