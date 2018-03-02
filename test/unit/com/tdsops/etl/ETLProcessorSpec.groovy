package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.json.JSONConnection
import getl.json.JSONDataset
import getl.proc.Flow
import getl.tfs.TFS
import getl.utils.FileUtils
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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLProcessorSpec extends Specification {

	@Shared
	Map conParams = [path: "${TFS.systemPath}/test_path_csv", createPath: true, extension: 'csv', codePage: 'utf-8']

	@Shared
	CSVConnection csvConnection

	@Shared
	JSONConnection jsonConnection

	DataSetFacade simpleDataSet
	DataSetFacade jsonDataSet
	DataSetFacade environmentDataSet
	DataSetFacade applicationDataSet
	DataSetFacade nonSanitizedDataSet
	DataSetFacade sixRowsDataSet
	DebugConsole debugConsole
	ETLFieldsValidator applicationFieldsValidator
	Project GMDEMO
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
		csvConnection = new CSVConnection(config: conParams.extension, path: conParams.path, createPath: true)
		jsonConnection = new JSONConnection(config: 'json')
		FileUtils.ValidPath(conParams.path)
		String.mixin StringAppendElement
	}

	def cleanupSpec() {
		new File(conParams.path).deleteOnExit()
	}

	def setup() {

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		simpleDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))

		simpleDataSet.getDataSet().field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "STRING", isNull: false, isKey: true)
		simpleDataSet.getDataSet().field << new getl.data.Field(name: 'model name', alias: 'MODEL NAME', type: "STRING", isNull: false)
		simpleDataSet.getDataSet().field << new getl.data.Field(name: 'manufacturer name', alias: 'MANUFACTURER NAME', type: "STRING", isNull: false)

		new Flow().writeTo(dest: simpleDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['device id': '152254', 'model name': 'SRW24G1', 'manufacturer name': 'LINKSYS'])
			updater(['device id': '152255', 'model name': 'ZPHA MODULE', 'manufacturer name': 'TippingPoint'])
			updater(['device id': '152256', 'model name': 'Slideaway', 'manufacturer name': 'ATEN'])
		}

		File jsonFile = new File("${conParams.path}/${UUID.randomUUID()}.json".toString())
		jsonFile << """[
				{ "device id": "152254", "model name": "SRW24G1", "manufacturer name": "LINKSYS"},
				{ "device id": "152255", "model name": "ZPHA MODULE", "manufacturer name": "TippingPoint"},
				{ "device id": "152256", "model name": "Slideaway", "manufacturer name": "ATEN"}
		]""".stripIndent()

		jsonDataSet = new DataSetFacade(new JSONDataset(connection: jsonConnection, fileName: jsonFile.path, rootNode: ".", convertToList: true))
		jsonDataSet.getDataSet().field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "STRING", isNull: false, isKey: true)
		jsonDataSet.getDataSet().field << new getl.data.Field(name: 'model name', alias: 'MODEL NAME', type: "STRING", isNull: false)
		jsonDataSet.getDataSet().field << new getl.data.Field(name: 'manufacturer name', alias: 'MANUFACTURER NAME', type: "STRING", isNull: false)

		environmentDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		environmentDataSet.getDataSet().field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "STRING", isKey: true)
		environmentDataSet.getDataSet().field << new getl.data.Field(name: 'model name', alias: 'MODEL NAME', type: "STRING")
		environmentDataSet.getDataSet().field << new getl.data.Field(name: 'manufacturer name', alias: 'MANUFACTURER NAME', type: "STRING")
		environmentDataSet.getDataSet().field << new getl.data.Field(name: 'environment', alias: 'ENVIRONMENT', type: "STRING")

		new Flow().writeTo(dest: environmentDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['device id': '152254', 'model name': 'SRW24G1', 'manufacturer name': 'LINKSYS', 'environment': 'Prod'])
			updater(['device id': '152255', 'model name': 'ZPHA MODULE', 'manufacturer name': 'TippingPoint', 'environment': 'Prod'])
			updater(['device id': '152256', 'model name': 'Slideaway', 'manufacturer name': 'ATEN', 'environment': 'Dev'])
		}

		sixRowsDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		sixRowsDataSet.getDataSet().field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "STRING", isKey: true)
		sixRowsDataSet.getDataSet().field << new getl.data.Field(name: 'model name', alias: 'MODEL NAME', type: "STRING")
		sixRowsDataSet.getDataSet().field << new getl.data.Field(name: 'manufacturer name', alias: 'MANUFACTURER NAME', type: "STRING")

		new Flow().writeTo(dest: sixRowsDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['device id': "152251", 'model name': "SRW24G1", 'manufacturer name': "LINKSYS"])
			updater(['device id': "152252", 'model name': "SRW24G2", 'manufacturer name': "LINKSYS"])
			updater(['device id': "152253", 'model name': "SRW24G3", 'manufacturer name': "LINKSYS"])
			updater(['device id': "152254", 'model name': "SRW24G4", 'manufacturer name': "LINKSYS"])
			updater(['device id': "152255", 'model name': "SRW24G5", 'manufacturer name': "LINKSYS"])
			updater(['device id': "152256", 'model name': "ZPHA MODULE", 'manufacturer name': "TippingPoint"])
		}

		applicationDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		applicationDataSet.getDataSet().field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
		applicationDataSet.getDataSet().field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
		applicationDataSet.getDataSet().field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
		applicationDataSet.getDataSet().field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

		new Flow().writeTo(dest: applicationDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['application id': '152254', 'vendor name': 'Microsoft', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': '152255', 'vendor name': 'Mozilla', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}

		debugConsole = new DebugConsole(buffer: new StringBuffer())

		applicationFieldsValidator = new DomainClassFieldsValidator()
		applicationFieldsValidator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		nonSanitizedDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

		new Flow().writeTo(dest: nonSanitizedDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['application id': '152254', 'vendor name': '\r\n\tMicrosoft\b\nInc\r\n\t', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': '152255', 'vendor name': '\r\n\tMozilla\t\t\0Inc\r\n\t', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}

		validator = new DomainClassFieldsValidator()
		validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Dependency, buildFieldSpecsFor(ETLDomain.Dependency))
	}

	void 'test can define a the primary domain'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						
					 """.stripIndent(),
					ETLProcessor.class.name)

		then: 'A domain is selected'
			etlProcessor.selectedDomain == ETLDomain.Application

		and: 'A new result was added in the result'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				data[0].fields == [:]
			}
	}

	void 'test can add groovy comments'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						// Script supports one line comments
						domain Application
						/*
							And multiple Lines comments
						*/
						
					 """.stripIndent(),
					ETLProcessor.class.name)

		then: 'A domain is selected'
			etlProcessor.selectedDomain == ETLDomain.Application

		and: 'A new result was added in the result'
			with(etlProcessor.result) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data[0].fields == [:]
				}
			}
	}

	void 'test can throw an exception if an invalid domain is defined'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""

						domain Unknown
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Invalid domain: 'Unknown'. It should be one of these values: ${ETLDomain.values()}"
	}

	void 'test can define a several domains in an ETL script'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""

						domain Application
						domain Device
						domain Storage
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The last domain selected could be recovered'
			etlProcessor.selectedDomain == ETLDomain.Storage

		and: 'A new result was added in the result'
			etlProcessor.result.domains.size() == 3
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				data[0].fields == [:]
			}

			with(etlProcessor.result.domains[1]) {
				domain == ETLDomain.Device.name()
				data[0].fields == [:]
			}

			with(etlProcessor.result.domains[2]) {
				domain == ETLDomain.Storage.name()
				data[0].fields == [:]
			}
	}

	void 'test can define a domain more than once in an ETL script'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""

						domain Application
						domain Device
						domain Application
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The last domain selected could be recovered'
			etlProcessor.selectedDomain == ETLDomain.Application

		and: 'A new result was added in the result'
			etlProcessor.result.domains.size() == 2
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				data[0].fields == [:]
			}

			with(etlProcessor.result.domains[1]) {
				domain == ETLDomain.Device.name()
				data[0].fields == [:]
			}
	}

	void 'test can throw an Exception if the skip parameter is bigger that rows count'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("skip 20", ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Incorrect skip step: 20"
	}

	void 'test can throw an Exception if the scrip command is not recognized'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("invalid command", ETLProcessor.class.name)

		then: 'An MissingMethodException exception is thrown'
			MissingMethodException missingMethodException = thrown MissingMethodException
			missingMethodException.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber == 1
	}

	void 'test can read labels from dataSource and create a map of columns'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
					""".stripIndent(), ETLProcessor.class.name)

		then: 'A column map is created'
			etlProcessor.column('device id').index == 0
			etlProcessor.column(0).label == 'device id'

		and:
			etlProcessor.column('model name').index == 1
			etlProcessor.column(1).label == 'model name'

		and:
			etlProcessor.column('manufacturer name').index == 2
			etlProcessor.column(2).label == 'manufacturer name'

		and:
			etlProcessor.currentRowIndex == 1
	}

	void 'test can use SOURCE reference object to connect with data source'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					sixRowsDataSet,
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						
					""".stripIndent(), ETLProcessor.class.name)

		then: 'A column map is created'
			etlProcessor.column('device id').index == 0
			etlProcessor.column(0).label == 'device id'

		and:
			etlProcessor.column('model name').index == 1
			etlProcessor.column(1).label == 'model name'

		and:
			etlProcessor.column('manufacturer name').index == 2
			etlProcessor.column(2).label == 'manufacturer name'

		and:
			etlProcessor.currentRowIndex == 1
	}

	/**
	 * The iterate command will create a loop that iterate over the remaining rows in the data source
	 */
	void 'test can iterate over all data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == sixRowsDataSet.readRows()
	}

	void 'test can iterate over all data source rows from a json dataset'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), jsonDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == jsonDataSet.readRows()
	}

	void 'test can iterate over a range of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						from 1 to 3 iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == 2
	}

	void 'test can throw an exception when iterates over an invalid range of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script with iterate staring in zero is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						from 0 to 3 iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown with a message for the invalid from parameter'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'


		when: 'The ETL script with iterate with a bigger to parameter is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						from 1 to 8 iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown with a message for the invalid from parameter'
			e = thrown ETLProcessorException
			e.message == "Invalid to parameter = 8"

	}

	void 'test can iterate over a list of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						from 1, 2, 3 iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == 3
	}

	void 'test can throw an exception with a message when iterates over a invalid list of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						from 0, 2, 4 iterate {
							println it
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown with a message for the invalid from parameter'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'
	}
	/**
	 * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
	 * 	The extract puts the value into a local register that can then be manipulated and eventually
	 * 	saved into the target domain object.
	 */
	void 'test can extract a field value over all rows based on column ordinal position'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""

						domain Device
						read labels
						iterate {
							extract 2
						}
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The last column index is selected correctly'
			etlProcessor.currentColumnIndex == 1

		and: 'The last column and row is selected'
			with(etlProcessor.currentRow.getElement(1)) {
				value == "Slideaway"
				originalValue == "Slideaway"
			}
	}
	/**
	 * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
	 * 	The extract puts the value into a local register that can then be manipulated and eventually
	 * 	saved into the target domain object.
	 */
	void 'test can extract a field value over all rows based on column name'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
					
						domain Device
						read labels
						iterate {
							extract 'model name'
						}
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The last column index is selected correctly'
			etlProcessor.currentColumnIndex == 1

		and: 'The last column and row is selected'
			with(etlProcessor.currentRow.getElement(1)) {
				value == "Slideaway"
				originalValue == "Slideaway"
			}
	}

	void 'test can throw an Exception if a column name is invalid'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
					
						domain Device
						read labels
						iterate {
							extract 'model'
						}
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Extracting a missing column name 'model'"

	}

	void 'test can throw an Exception if a column position is zero'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 0
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'
	}

	void 'test can throw an Exception if a column index is not between row elements range'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
					
						domain Device
						read labels
						iterate {
							extract 10000
						}
						
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Invalid index = 10000"
	}

	void 'test can check syntax errors at evaluation time'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""domain Device
						read labels
						iterate 
							extract 'MODEL NAME' 
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			thrown MultipleCompilationErrorsException
	}

	void 'test can disallow closure creation using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
//        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import


		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration)
					.evaluate("""
						domain Device
						read labels
						def greeting = { String name -> "Hello, \$name!" }
						assert greeting('Diego') == 'Hello, Diego!'
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors[0].cause*.message == ["Closures are not allowed"]
	}

	void 'test can disallow method creation using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration).
					evaluate("""
			domain Device
			read labels
			def greeting(String name){ 
				"Hello, \$name!" 
			}
			assert greeting('Diego') == 'Hello, Diego!'
		""".stripIndent(), ETLProcessor.class.name)

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Method definitions are not allowed"]
	}

	void 'test can disallow unnecessary imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration).
					evaluate("""
			
			import java.lang.Math
			
			domain Device
			read labels
			Math.max 10, 100
		""".stripIndent(),
							ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math] is not allowed"]
	}

	void 'test can disallow unnecessary stars imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
			secureASTCustomizer.starImportsWhitelist = []

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration).
					evaluate("""
			
			import java.lang.Math.*
			
			domain Device
			read labels
			max 10, 100
		""".stripIndent(),
							ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math.*] is not allowed"]
	}

	void 'test can allow stars imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars Math.class.name
			customizer.addStaticStars DebugConsole.ConsoleStatus.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
			secureASTCustomizer.starImportsWhitelist = []

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration)
					.evaluate("""
						read labels
						max 10, 100
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is not thrown'
			notThrown MultipleCompilationErrorsException
	}

	void 'test can enable console and log domain selected'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					simpleDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
							console on
							domain Device
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			etlProcessor.debugConsole.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
					.append(System.lineSeparator())
					.append("INFO - Selected Domain: Device")
					.append(System.lineSeparator())
					.toString()
	}

	@Ignore
	void 'test can enable console without defining on parameter'() {

		given:
			DebugConsole debugConsole = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(simpleDataSet, debugConsole)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
							console
							domain Device
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			debugConsole.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
					.append(System.lineSeparator())
					.append("INFO - Selected Domain: Device")
					.append(System.lineSeparator())
					.toString()
	}

	void 'test can debug a selected value for a column name'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
							console on
							read labels
							domain Device
							iterate {
								debug 'device id'
							}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			console.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
					.append(System.lineSeparator())
					.append("INFO - Reading labels [0:device id, 1:model name, 2:manufacturer name]")
					.append(System.lineSeparator())
					.append("INFO - Selected Domain: Device")
					.append(System.lineSeparator())
					.append("DEBUG - [position:[0, 1], value:152254]")
					.append(System.lineSeparator())
					.append("DEBUG - [position:[0, 2], value:152255]")
					.append(System.lineSeparator())
					.append("DEBUG - [position:[0, 3], value:152256]")
					.append(System.lineSeparator())
					.toString()
	}

	void 'test can throw an ETLProcessorException for an invalid console status'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
							console open
							domain Device
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Unknown console command option: open"
	}

	void 'test can load field with an extracted element value after validate fields specs'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								domain Application
								iterate {
									extract 'vendor name' load appVendor
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.ETLInfo.originalFilename == applicationDataSet.fileName()
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.appVendor) {
					value == 'Microsoft'
					originalValue == 'Microsoft'
				}

				with(data[1].fields.appVendor) {
					value == 'Mozilla'
					originalValue == 'Mozilla'
				}
			}
	}

	void 'test can load fields for more than one domain in the same iteration'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE))

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,vendor name,technology,location,device id,model name,manufacturer name
				152255,Microsoft,(xlsx updated),ACME Data Center,1522,SRW24G1,LINKSYS
				152256,Mozilla,NGM,ACME Data Center,1523,ZPHA MODULE,TippingPoint
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					dataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								iterate {
									domain Application
									extract 'application id' load id
									extract 'vendor name' load appVendor
									
									domain Device
									extract 'device id' load id
									extract 'model name' load name
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 2
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.id) {
					value == '152255'
					originalValue == '152255'
				}

				with(data[0].fields.appVendor) {
					value == 'Microsoft'
					originalValue == 'Microsoft'
				}

				with(data[1].fields.id) {
					value == '152256'
					originalValue == '152256'
				}

				with(data[1].fields.appVendor) {
					value == 'Mozilla'
					originalValue == 'Mozilla'
				}
			}

			with(etlProcessor.result.domains[1]) {
				domain == 'Device'
				with(data[0].fields.id) {
					value == '1522'
					originalValue == '1522'
				}

				with(data[0].fields.name) {
					value == 'SRW24G1'
					originalValue == 'SRW24G1'
				}

				with(data[1].fields.id) {
					value == '1523'
					originalValue == '1523'
				}

				with(data[1].fields.name) {
					value == 'ZPHA MODULE'
					originalValue == 'ZPHA MODULE'
				}
			}

		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	void 'test can use if else groovy clause to load a field with an extracted element value'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								domain Application
								iterate {
									extract 'vendor name' 
									
									if ( CE == 'Microsoft'){
										load appVendor
									} else {
										load environment
									}
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.appVendor) {
					value == 'Microsoft'
					originalValue == 'Microsoft'
				}

				with(data[1].fields.environment) {
					value == 'Mozilla'
					originalValue == 'Mozilla'
				}
			}
	}

	void 'test can set a an extracted element in a variable'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								domain Application
								iterate {
									extract 'vendor name'
									def myVar = CE
									if ( myVar == 'Microsoft'){
										load appVendor
									} else {
										load environment
									}
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.appVendor) {
					value == 'Microsoft'
					originalValue == 'Microsoft'
				}

				with(data[1].fields.environment) {
					value == 'Mozilla'
					originalValue == 'Mozilla'
				}
			}
	}

	void 'test can load field many times with the same extracted value'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								domain Application
								iterate {
									extract 'vendor name' load appVendor load description
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.appVendor) {
					originalValue == "Microsoft"
					value == "Microsoft"
				}

				with(data[0].fields.description) {
					originalValue == "Microsoft"
					value == "Microsoft"
				}

				with(data[1].fields.appVendor) {
					originalValue == "Mozilla"
					value == "Mozilla"
				}

				with(data[1].fields.description) {
					originalValue == "Mozilla"
					value == "Mozilla"
				}
			}
	}

	void 'test can throw an ETLProcessorException when try to load without domain definition'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								domain Application
								read labels
								iterate {
									extract 'vendor name' load appVendor
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'There is not validator for domain Application and field appVendor'

	}

	void 'test can throw an ETLProcessorException when try to load with domain definition but without domain fields specification'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								domain Application
								iterate {
									extract 'vendor name' load vendedor
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'There is not validator for domain Application and field vendedor'
	}

	void 'test can extract a field value and load into a domain object property name'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						read labels
						iterate {
							extract 'vendor name' load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.appVendor) {
					originalValue == "Microsoft"
					value == "Microsoft"
				}

				with(data[1].fields.appVendor) {
					originalValue == "Mozilla"
					value == "Mozilla"
				}
			}
	}

	void 'test can process a selected domain rows'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						read labels
						iterate {
							extract 'vendor name' load appVendor
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.appVendor) {
					originalValue == "Microsoft"
					value == "Microsoft"
				}

				with(data[1].fields.appVendor) {
					originalValue == "Mozilla"
					value == "Mozilla"
				}
			}
			with(etlProcessor.getElement(0, 1)) {
				value == "Microsoft"
				fieldSpec.name == "appVendor"
			}
	}


	void 'test can create new results loading values without extract previously'() {

		given:
			ETLFieldsValidator validator = new DomainClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						read labels
						iterate {
							domain Application
							set environment with Production
							extract 1 load id
							extract 'vendor name' load appVendor
							
							domain Device
							extract 1 load id 
							set location with 'Development'        
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 2
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[0].fields.id) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				with(data[1].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152255'
					value == '152255'
				}

				with(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		and: 'Results should contain Device domain results associated'
			with(etlProcessor.result.domains[1]) {
				domain == 'Device'
				with(data[0].fields.id) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[0].fields.location) {
					originalValue == 'Development'
					value == 'Development'
				}

				with(data[1].fields.id) {
					originalValue == '152255'
					value == '152255'
				}

				with(data[1].fields.location) {
					originalValue == 'Development'
					value == 'Development'
				}
			}
	}

	void 'test can turn on globally trim command to remove leading and trailing whitespaces'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						trim on
						domain Application
						read labels
						iterate {
							extract 'vendor name' load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nInc"
			etlProcessor.getElement(0, 1).fieldSpec.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getElement(1, 1).fieldSpec.name == "appVendor"

	}

	void 'test can turn on globally trim command without defining on parameter'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						trim on
						domain Application
						read labels
						iterate {
							extract 'vendor name' load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nInc"
			etlProcessor.getElement(0, 1).fieldSpec.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getElement(1, 1).fieldSpec.name == "appVendor"

	}

	void 'test can turn on globally sanitize command to replace all of the escape characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						sanitize on
						domain Application
						read labels
						iterate {
							extract 'vendor name' load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft~+Inc"
			etlProcessor.getElement(0, 1).fieldSpec.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla++~Inc"
			etlProcessor.getElement(1, 1).fieldSpec.name == "appVendor"

	}

	void 'test can load Room domain instances'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
roomId,Name,Location,Depth,Width,Source,Address,City,Country,StateProv,Postal Code
673,DC1,ACME Data Center,26.00,40.00,Source,112 Main St ,Cumberland,,IA,50843
674,ACME Room 1,New Colo Provider,4.00,42.00,Target,411 Elm St,Dallas,,TX,75202""".stripIndent())

		and:
			List<Room> rooms = buildRooms([
				[673, GMDEMO, 'DC1', 'ACME Data Center', 26, 40, '112 Main St', 'Cumberland', 'IA', '50843'],
				[674, GMDEMO, 'ACME Room 1', 'New Colo Provider', 40, 42, '411 Elm St', 'Dallas', 'TX', '75202']
			])

		and:
			GroovyMock(Room, global: true)
			Room.executeQuery(_, _) >> { String query, Map args ->
				rooms.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						iterate {
							domain Room
							extract roomId load id 
							extract Name load roomName
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Room domain results associated'
			etlProcessor.result.domains.size() == 1



		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	void 'test can load Rack domain instances'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
rackId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
13144,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,Source,500,235,3300,3300,0,Rack,R
13145,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,Source,280,252,3300,3300,0,Rack,L
13167,VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,Source,160,0,1430,1430,0,Rack,R
13187,Storage,ACME Data Center,42U Rack,ACME Data Center / DC1,Source,1,15,0,0,0,Object,L
13358,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,Source,41,42,0,0,0,block3x5,L""".stripIndent())

		and:
			List<Room> rooms = buildRooms([
				[673, GMDEMO, 'DC1', 'ACME Data Center', 26, 40, '112 Main St', 'Cumberland', 'IA', '50843'],
				[674, GMDEMO, 'ACME Room 1', 'New Colo Provider', 40, 42, '411 Elm St', 'Dallas', 'TX', '75202']
			])

		and:
			List<Rack> racks = buildRacks([
				[13144, GMDEMO, 673, -1, -1, 'ACME Data Center', 'R', 'Source', 500, 235, 3300, 3300, 0, 'Rack'],
				[13167, GMDEMO, 673, -1, -1, 'ACME Data Center', 'L', 'Source', 160, 0, 1430, 1430, 0, 'Rack'],
				[13358, GMDEMO, -1, -1, -1, 'New Colo Provider', 'L', 'Source', 41, 42, 0, 0, 0, 'block3x5']
			],
				rooms)

		and:
			GroovyMock(Room, global: true)
			Room.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			Room.executeQuery(_, _) >> { String query, Map args ->
				rooms.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						iterate {
							domain Rack
							extract rackId load id 
							extract Location load location
							extract Room load room
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Rack domain results associated'
			etlProcessor.result.domains.size() == 1


		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if an domain is not specified'() {
		given:
		def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
id
1""".stripIndent())
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), dataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
					read labels
					
					iterate {
						extract 1 load id
					}						
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown()
			e.message == 'A domain must be specified'
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'roomName', 'location', 'roomDepth', 'roomWidth', 'address', 'city', 'stateProv', 'postalCode']
	 * @param valuesList
	 * @return a list of Mock(Room)
	 */
	List<Room> buildRooms(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Room room = Mock()
			room.getId() >> values[0]
			room.getProject() >> values[1]
			room.getRoomName() >> values[2]
			room.getLocation() >> values[3]
			room.getRoomDepth() >> values[4]
			room.getRoomWidth() >> values[5]
			room.getAddress() >> values[6]
			room.getCity() >> values[7]
			room.getStateProv() >> values[8]
			room.getPostalCode() >> values[9]
			room
		}
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'modelId', 'manufacturerId', 'roomId', 'location', 'front', 'source', 'roomX', 'roomY', 'powerA', 'powerB', 'powerC', 'rackType'],
	 * @param valuesList
	 * @return a list of Mock(Rack)
	 */
	List<Rack> buildRacks(List<List<?>> valuesList, List<Room> rooms, List<Model> models = [], List<Manufacturer> manufacturers = []) {
		return valuesList.collect { List<?> values ->
			Rack rack = Mock()
			rack.getId() >> values[0]
			rack.getProject() >> values[1]
			rack.getModel() >> models.find { it.getId() == values[2] }
			rack.getManufacturer() >> manufacturers.find { it.getId() == values[3] }
			rack.getRoom() >> rooms.find { it.getId() == values[4] }
			rack.getLocation() >> values[5]
			rack.getFront() >> values[6]
			rack.getSource() >> values[7]
			rack.getRoomX() >> values[8]
			rack.getRoomY() >> values[9]
			rack.getPowerA() >> values[10]
			rack.getPowerB() >> values[11]
			rack.getPowerC() >> values[12]
			rack.getRackType() >> values[13]
			rack
		}
	}

	/**
	 * Helper method to create Fields Specs based on Asset definition
	 * @param asset
	 * @return
	 */
	private List<Map<String, ?>> buildFieldSpecsFor(def asset) {

		List<Map<String, ?>> fieldSpecs = []
		switch(asset){
			case AssetClass.APPLICATION:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('appVendor', 'Vendor'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('description', 'Description'),
					buildFieldSpec('assetName', 'Name'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case AssetClass.DATABASE:

				break
			case AssetClass.DEVICE:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('location', 'Location'),
					buildFieldSpec('name', 'Name'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case ETLDomain.Dependency:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('assetName', 'AssetName'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('asset', 'Asset'),
					buildFieldSpec('comment', 'Comment'),
					buildFieldSpec('status', 'Status'),
					buildFieldSpec('dataFlowFreq', 'DataFlowFreq'),
					buildFieldSpec('dataFlowDirection', 'DataFlowDirection')
				]
				break
			case AssetClass.STORAGE:

				break
		}

		return fieldSpecs
	}

	/**
	 * Builds a spec structure used to validate asset fields
	 * @param field
	 * @param label
	 * @param type
	 * @param required
	 * @return a map with the correct fieldSpec format
	 */
	private Map<String, ?> buildFieldSpec(String field, String label, String type = "String", Integer required = 0) {
		return [
			constraints: [
				required: required
			],
			control: type,
			default: '',
			field: field,
			imp: 'U',
			label: label,
			order: 0,
			shared: 0,
			show: 0,
			tip: "",
			udf: 0
		]
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildCSVDataSet(String csvContent) {

		def (String fileName, OutputStream sixRowsDataSetOS) = service.createTemporaryFile('unit-test-', 'csv')
		sixRowsDataSetOS << csvContent
		sixRowsDataSetOS.close()

		String fullName = service.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}
}
