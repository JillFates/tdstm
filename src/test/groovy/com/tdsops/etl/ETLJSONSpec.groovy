package com.tdsops.etl

import com.tdsops.common.grails.ApplicationContextHolder
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import grails.test.mixin.Mock
import net.transitionmanager.imports.DataScript
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import spock.lang.See

/**
 * Using JSON Object in ETL script. It manages the following commands:
 * <ul>
 *     <li><b>rootNode devices</b></li>
 *     <li><b>rootNode 'Production Apps'</b></li>
 *     <li><b>read labels on 2</b></li>
 * </ul>
 */
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
        applicationContextHolder(ApplicationContextHolder) { bean ->
            bean.factoryMethod = 'getInstance'
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

		debugConsole = new DebugConsole(buffer: new StringBuilder())
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			etlProcessor.getColumnByName('application id') != null

		and:
			etlProcessor.getColumnByName('location') != null

		and:
			etlProcessor.getColumnByName('Technology') != null

		and:
			etlProcessor.getColumnByName('vendor name').index != null

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			etlProcessor.getColumnByName('application id') != null

		and:
			etlProcessor.getColumnByName('location') != null

		and:
			etlProcessor.getColumnByName('Technology') != null

		and:
			etlProcessor.getColumnByName('vendor name') != null

		and:
			etlProcessor.currentRowIndex == 1
		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			e.message == "Data was not found in JSON at rootNode 'Active Applications'"

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			e.message == "Data was not found in JSON at rootNode 'applications'"

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			etlProcessor.getColumnByName('application id') != null

		and:
			etlProcessor.getColumnByName('location') != null

		and:
			etlProcessor.getColumnByName('Technology') != null

		and:
			etlProcessor.getColumnByName('vendor name') != null

		and:
			etlProcessor.currentRowIndex == 2


		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				assertWith(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				assertWith(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				assertWith(data[0].fields.appVendor) {
					originalValue == '[value:Microsoft]'
					value == '[value:Microsoft]'
				}

				assertWith(data[1].fields.appVendor) {
					originalValue == '[value:Mozilla]'
					value == '[value:Mozilla]'
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Device.name()
				data.size() == 1
				assertWith(data[0].fields.custom1) {
					originalValue == 4096
					value == 4096
				}

				assertWith(data[0].fields.custom2) {
					originalValue == 2
					value == 2
				}

				assertWith(data[0].fields.custom3) {
					originalValue == 2
					value == 2
				}

				assertWith(data[0].fields.custom4) {
					originalValue == ["IDE", "SSD"]
					value == ["IDE", "SSD"]
				}

				assertWith(data[0].fields.custom5) {
					originalValue == null
					value == null
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1

				assertWith(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			etlProcessor.evaluate("""
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
					""".stripIndent())

		then: 'DATASET was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 2

		and: 'Results contains values'
			assertWith(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				assertWith(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				assertWith(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

			assertWith(etlProcessor.finalResult().domains[1]) {
				domain == ETLDomain.Device.name()
				data.size() == 2
				assertWith(data[0].fields.assetName) {
					originalValue == 'xraysrv01'
					value == 'xraysrv01'
				}

				assertWith(data[1].fields.assetName) {
					originalValue == 'zuludb01'
					value == 'zuludb01'
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			etlProcessor.getColumnByName('application id') != null

		and:
			etlProcessor.getColumnByName('location') != null

		and:
			etlProcessor.getColumnByName('Technology') != null

		and:
			etlProcessor.getColumnByName('vendor name') != null

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1
				assertWith(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
				    extract 'attribs.hostname' load 'custom3'
				}
				""".stripIndent())

		then: 'results should have one domain'
			etlProcessor.finalResult().domains.size() == 1

		and: 'the results contain expected values'
			assertWith(etlProcessor.finalResult().domains[0], DomainResult) {
				domain == ETLDomain.Device.name()
				data.size() == 1

				assertWith(data[0]) {
					rowNum == 1

					assertWith(fields.id) {
						originalValue == 123
						value == 123
					}

					assertWith(fields.custom1) {
						originalValue == 4096
						value == 4096
					}

					assertWith(fields.custom2) {
						originalValue == 2
						value == 2
					}

					assertWith(fields.custom3) {
						originalValue == 'zulu01'
						value == 'zulu01'
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11181')
	void 'test can use dot notation with more than one level in extract command'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet('''
				{
					"devices": [
					   {
					      "vm id": 123,
					      "attribs": {
					         "memory": 4096,
					         "cpu": 2,
					         "hostname": {
					         	"value": "zulu01"
					         }
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
				    extract 'attribs.hostname.value' load 'custom3'
				}
				""".stripIndent())

		then: 'results should have one domain'
			etlProcessor.finalResult().domains.size() == 1

		and: 'the results contain expected values'
			assertWith(etlProcessor.finalResult().domains[0], DomainResult) {
				domain == ETLDomain.Device.name()
				data.size() == 1

				assertWith(data[0]) {
					rowNum == 1

					assertWith(fields.id) {
						originalValue == 123
						value == 123
					}

					assertWith(fields.custom1) {
						originalValue == 4096
						value == 4096
					}

					assertWith(fields.custom2) {
						originalValue == 2
						value == 2
					}

					assertWith(fields.custom3) {
						originalValue == 'zulu01'
						value == 'zulu01'
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
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
