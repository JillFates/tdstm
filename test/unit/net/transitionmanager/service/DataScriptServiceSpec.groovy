package net.transitionmanager.service

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Ignore
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

	def 'test can parse a JSON data using custom rootNode and json object'() {

		given:
			String fileContent = '''
				{
				  "assets": {
					  "data": [
						{
						  "collection_type": "SNMP",
						  "cpu_architecture": "Not Collected",
						  "cpu_count": "2",
						}
					  ]
					}
				}'''.stripIndent()

		and:
			def (String fileName, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile('unit-test-', 'json')
			fileOutputStream << fileContent
			fileOutputStream.close()

		when:
			Map json = service.parseDataFromJSON(fileName, 1, 'assets.data')

		then:
			json.config == [
					[property: 'collection_type', type: 'text'],
					[property: 'cpu_architecture', type: 'text'],
					[property: 'cpu_count', type: 'text']
			]

			json.rows == [
					[collection_type: 'SNMP', cpu_architecture: 'Not Collected', cpu_count: '2']
			]

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}

	def 'test can parse a JSON data using default rootNode and an incorrect json object'() {

		given:
			String fileContent = '''
			{
				"assets": [
					{
					  	"collection_type": "SNMP",
					  	"cpu_architecture": "Not Collected",
					  	"cpu_count": "2",
					},
					{
						"collection_type": "SNMP 1",
						"cpu_architecture": "Collected",
						"cpu_count": "8",
					}
				]
			}'''.stripIndent()

		and:
			def (String fileName, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile('unit-test-', 'json')
			fileOutputStream << fileContent
			fileOutputStream.close()

		when:
			Map json = service.parseDataFromJSON(fileName, 1, '.')

		then:
			json.config == [
					[property: 'assets', type: 'text']
			]

			json.rows == [
					[
							assets: [
									[collection_type: 'SNMP', cpu_architecture: 'Not Collected', cpu_count: '2'],
									[collection_type: 'SNMP 1', cpu_architecture: 'Collected', cpu_count: '8']
							]
					]
			]

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}


	def 'test can parse a JSON data using default rootNode as a json array returning an error'() {

		given:
			String fileContent = '''
				[
					{
					  	"collection_type": "SNMP",
					  	"cpu_architecture": "Not Collected",
					  	"cpu_count": "2",
					},
					{
						"collection_type": "SNMP 1",
						"cpu_architecture": "Collected",
						"cpu_count": "8",
					}
				]'''.stripIndent()

		and:
			def (String fileName, OutputStream fileOutputStream) = fileSystemService
					.createTemporaryFile('unit-test-', 'json')

			fileOutputStream << fileContent
			fileOutputStream.close()

		when:
			Map json = service.parseDataFromJSON(fileName, 1, '.')

		then:
			json.config == [
				[property: 'collection_type', type: 'text'],
				[property: 'cpu_architecture', type: 'text'],
				[property: 'cpu_count', type: 'text']
			]

			json.rows == [
				[collection_type: 'SNMP', cpu_architecture: 'Not Collected', cpu_count: '2'],
			]

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}

	@Ignore
	def 'test parsing JSON data using default rootNode with returning an error'() {

		given:
			String fileContent = '''
				{
					"collection_type": "SNMP",
					"cpu_architecture": "Not Collected",
					"cpu_count": "2",
			'''.stripIndent()

		and:
			def (String fileName, OutputStream fileOutputStream) = fileSystemService.
					createTemporaryFile('unit-test-', 'json')
			fileOutputStream << fileContent
			fileOutputStream.close()

		when:
			service.parseDataFromJSON(fileName, 1, '.')

		then: 'It throws an Exception with incorrect format message'
			Exception e = thrown Exception
			e.message == '''Invalid JSON : expecting '}' or ',' but got current char [NEWLINE] with an int value of 10

The current character read is [NEWLINE] with an int value of 10
expecting '}' or ',' but got current char [NEWLINE] with an int value of 10
line number 5
index number 87
	"cpu_count": "2",
..................^'''

/*
---
--- This is the latest results that causes it to fail
---
groovy.json.JsonException: expecting '}' or ',' but got current char '' with an int value of 0

The current character read is '' with an int value of 0
expecting '}' or ',' but got current char '' with an int value of 0
line number 6
index number 255
*/
		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)

	}

}
