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
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'applications'
					""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 0
			etlProcessor.currentRowIndex == 0

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}


	void 'test can switch from one rootNode to another in JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						rootNode 'applications'
						rootNode 'devices'
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 0
			etlProcessor.currentRowIndex == 0

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by default in first row by default for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('location').index == 1
			etlProcessor.column(1).label == 'location'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('vendor name').index == 3
			etlProcessor.column(3).label == 'vendor name'

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}


	void 'test can define a quoted string for the JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('location').index == 1
			etlProcessor.column(1).label == 'location'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('vendor name').index == 3
			etlProcessor.column(3).label == 'vendor name'

		and:
			etlProcessor.currentRowIndex == 1
		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if rootNode is incorrect key in the JSON DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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
			e.message == "JSON rootNode: 'Active Applications' XPATH is invalid"

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if rootNode name case is incorrect for a JSON DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "JSON rootNode: 'Applications' XPATH is invalid"

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'applications'
						skip 1
						read labels

						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 0
			etlProcessor.currentRowIndex == 2

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('location').index == 1
			etlProcessor.column(1).label == 'location'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('vendor name').index == 3
			etlProcessor.column(3).label == 'vendor name'

		and:
			etlProcessor.currentRowIndex == 2


		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.resultsMap().domains[0]) {
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
					"applications": [ 
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
							"technology":"NGM",
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
						rootNode 'applications'
						read labels
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.resultsMap().domains[0]) {
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

	void 'test can read and skip rows in an iterate for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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
						skip 1
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.resultsMap().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1

				with(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for more than one rootNode in a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
					rootNode 'applications'
					read labels
					domain Application
					iterate {
						extract 'vendor name' load 'Vendor'
					}

					rootNode 'devices'
					read labels
					domain Device
					iterate {
						extract 'name' load 'Name'
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.resultsMap().domains.size() == 2

		and: 'Results contains values'
			with(etlProcessor.resultsMap().domains[0]) {
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

			with(etlProcessor.resultsMap().domains[1]) {
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
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						rootNode 'applications'
						skip 1
						read labels
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.resultsMap().domains.size() == 0

		and: 'Results contains values'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('location').index == 1
			etlProcessor.column(1).label == 'location'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('vendor name').index == 3
			etlProcessor.column(3).label == 'vendor name'

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read rows ignoring rows in the middle of an iteration for a JSON DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildJSONDataSet(DataSet)

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

						iterate {
							domain Application

							extract 'vendor name' load 'Vendor'
							if(CE == 'Microsoft'){
								ignore row
							}
						}
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.resultsMap().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.resultsMap().domains[0]) {
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

	static final String DataSet = """
		{
			"applications": [
				{
					"application id":152254,
					"vendor name":"Microsoft",
					"location":"ACME Data Center"
				},
				{
					"application id":152255,
					"vendor name": "Mozilla",
					"technology":"NGM",
					"location":"ACME Data Center"
				}
			],
			"devices": [
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
					"technology":"NGM",
					"location":"ACME Data Center"
				}
			],
		}	
	""".stripIndent().trim()

}
