package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.See

/**
 * Using JSON Object in ETL script. It manages the following commands:
 * <ul>
 *     <li><b>rootNode devices</b></li>
 *     <li><b>rootNode 'Production Apps'</b></li>
 *     <li><b>read labels on 2</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLJSONSpec extends ETLBaseSpec {

	Project GMDEMO
	Project TMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
	}

	def setupSpec() {
		String.mixin StringAppendElement
	}

	def setup() {
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()

		debugConsole = new DebugConsole(buffer: new StringBuffer())
	}

	void 'test can define a rootNode for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
					""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 0

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can switch from one rootNode to another in JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						rootNode 'Applications'
						rootNode 'Devices'
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 0

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by default in first row by default for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
						read labels
						""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id') != null

		and:
			etlProcessor.column('location') != null

		and:
			etlProcessor.column('Technology') != null

		and:
			etlProcessor.column('vendor name').index != null

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can define a quoted string for the JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'The Applications'
						read labels
						""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id') != null

		and:
			etlProcessor.column('location') != null

		and:
			etlProcessor.column('Technology') != null

		and:
			etlProcessor.column('vendor name') != null

		and:
			etlProcessor.currentRowIndex == 1
		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if rootNode is incorrect key in the JSON DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 10
						read labels
						""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Unrecognized command rootNode with args [10]'

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception rootNode is incorrect (not found) for a JSON DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Active Applications'
						read labels
						""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Unable to find JSON rootNode with path 'Active Applications'"

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if rootNode name case is incorrect for a JSON DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'applications'
						read labels
						""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Unable to find JSON rootNode with path 'applications'"

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
						skip 1
						read labels

						""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 2

		and: 'A column map is created'
			etlProcessor.column('application id') != null

		and:
			etlProcessor.column('location') != null

		and:
			etlProcessor.column('Technology') != null

		and:
			etlProcessor.column('vendor name') != null

		and:
			etlProcessor.currentRowIndex == 2


		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
						read labels
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				with(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				with(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read JSONObject fields iterating rows for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet('''
				{
					"Applications": [
						{
							"application id":152254,
							"vendor name": {
								"value": "Microsoft"
							},
							"location": "ACME Data Center"
						},
						{
							"application id":152255,
							"vendor name": {
								"value": "Mozilla"
							},
							"Technology":"NGM",
							"location":"ACME Data Center"
						}
					]
				}'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				sanitize off
				rootNode 'Applications'
				read labels
				iterate {
					domain Application
					extract 'vendor name' load 'Vendor'
				}
			""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				with(data[0].fields.appVendor) {
					originalValue.value == 'Microsoft'
					value.value == 'Microsoft'
				}

				with(data[1].fields.appVendor) {
					originalValue.value == 'Mozilla'
					value.value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read JSONObject fields for complex JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet('''
				{
					"devices": [
					   {
					      "app id": 123,
					      "attribs": {
					         "memory": 4096,
					         "cpu": 2,
					         "storage":[
					            {
					               "type": "IDE",
					               "size": "1T"
					            },
					            {
					               "type": "SSD",
					               "size": "500G"
					            }
					         ]
					      }
					   }
					]
				}'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					  GMDEMO,
					  dataSet,
					  debugConsole,
					  validator)

		when: 'the ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				sanitize off
				rootNode 'devices'
				read labels
				domain Device
				iterate {
				    extract 'app id' load 'id'
				    extract 'attribs' set attribsVar
				    load 'custom1' with attribsVar['memory']
				    load 'custom2' with attribsVar.cpu
				    load 'custom3' with attribsVar.storage.size()
				    load 'custom4' with attribsVar.storage.collect { it.type }
				    load 'custom5' with attribsVar.propNotFound
				}
				""".stripIndent())

		then: 'results should have one domain'
			etlProcessor.finalResult().domains.size() == 1

		and: 'the results contain expected values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Device.name()
				data.size() == 1
				with(data[0].fields.custom1) {
					originalValue == 4096
					value == 4096
				}

				with(data[0].fields.custom2) {
					originalValue == 2
					value == 2
				}

				with(data[0].fields.custom3) {
					originalValue == 2
					value == 2
				}

				with(data[0].fields.custom4) {
					originalValue == ["IDE", "SSD"]
					value == ["IDE", "SSD"]
				}

				with(data[0].fields.custom5) {
					originalValue == null
					value == null
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can read and skip rows in an iterate for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
						read labels
						skip 1
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1

				with(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for more than one rootNode in a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
					rootNode 'Applications'
					read labels
					domain Application
					iterate {
						extract 'vendor name' load 'Vendor'
					}

					rootNode 'Devices'
					read labels
					domain Device
					iterate {
						extract 'name' load 'Name'
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 2

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				with(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				with(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

			with(etlProcessor.finalResult().domains[1]) {
				domain == ETLDomain.Device.name()
				data.size() == 2
				with(data[0].fields.assetName) {
					originalValue == 'xraysrv01'
					value == 'xraysrv01'
				}

				with(data[1].fields.assetName) {
					originalValue == 'zuludb01'
					value == 'zuludb01'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows before for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
						skip 1
						read labels
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.finalResult().domains.size() == 0

		and: 'Results contains values'
			etlProcessor.column('application id') != null

		and:
			etlProcessor.column('location') != null

		and:
			etlProcessor.column('Technology') != null

		and:
			etlProcessor.column('vendor name') != null

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read rows ignoring rows in the middle of an iteration for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DATASET)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'Applications'
						read labels

						iterate {
							domain Application

							extract 'vendor name' load 'Vendor'
							if(CE == 'Microsoft'){
								ignore record
							}
						}
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1
				with(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-11181')
	void 'test can use dot notation in extract command'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet('''
				{
					"devices": [
					   {
					      "vm id": 123,
					      "attribs": {
					         "memory": 4096,
					         "cpu": 2,
					         "hostname": "zulu01"
					      }
					   }
					]
				}'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'the ETL script is evaluated'
			etlProcessor.evaluate("""
				rootNode 'devices'
				read labels
				domain Device
				iterate {
				    extract 'vm id' load 'id'
				    extract 'attribs' set attribsVar
				    load 'custom1' with attribsVar['memory']
				    load 'custom2' with attribsVar.cpu
				    extract 'attribs.hostname' load 'custom 3'
				}
				""".stripIndent())

		then: 'results should have one domain'
			etlProcessor.finalResult().domains.size() == 1

		and: 'the results contain expected values'
			with(etlProcessor.finalResult().domains[0], DomainResult) {
				domain == ETLDomain.Device.name()
				data.size() == 1

				with(data[0], RowResult) {
					rowNum == 1

					with(fields.id, FieldResult) {
						originalValue == 123
						value == 123
					}

					with(fields.custom1, FieldResult) {
						originalValue == 4096
						value == 4096
					}

					with(data[0].fields.custom2) {
						originalValue == 2
						value == 2
					}

					with(data[0].fields.custom3) {
						originalValue == 2
						value == 2
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	static final String DATASET = """
		{
			"Applications": [
				{
					"application id":152254,
					"vendor name":"Microsoft",
					"location":"ACME Data Center"
				},
				{
					"application id":152255,
					"vendor name": "Mozilla",
					"Technology":"NGM",
					"location":"ACME Data Center"
				}
			],
			"Devices": [
				{
					"name": "xraysrv01",
					"mfg": "Dell",
					"model": "PE2950",
					"type": "Server",
				},
				{
					"name": "zuludb01",
					"mfg": "HP",
					"model": "BL380",
					"type": "Blade",
				}
			],
			"The Applications": [
				{
					"application id":152254,
					"vendor name":"Microsoft",
					"location":"ACME Data Center"
				},
				{
					"application id":152255,
					"Technology":"NGM",
					"location":"ACME Data Center"
				}
			],
		}
	""".stripIndent().trim()

}
