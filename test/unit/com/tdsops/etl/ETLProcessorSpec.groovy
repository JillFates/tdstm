package com.tdsops.etl

import com.tds.asset.AssetEntity
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
import net.transitionmanager.domain.Project
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
@Mock([DataScript])
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

		applicationFieldsValidator = new ETLAssetClassFieldsValidator()
		applicationFieldsValidator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

		nonSanitizedDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

		new Flow().writeTo(dest: nonSanitizedDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['application id': '152254', 'vendor name': '\r\n\tMicrosoft\b\nInc\r\n\t', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': '152255', 'vendor name': '\r\n\tMozilla\t\t\0Inc\r\n\t', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}
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
				data == []
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
					data == []
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
				data == []
			}

			with(etlProcessor.result.domains[1]) {
				domain == ETLDomain.Device.name()
				data == []
			}

			with(etlProcessor.result.domains[2]) {
				domain == ETLDomain.Storage.name()
				data == []
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
				data == []
			}

			with(etlProcessor.result.domains[1]) {
				domain == ETLDomain.Device.name()
				data == []
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

	void 'test can transform a field value with uppercase transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase() 
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'SRW24G1'
			etlProcessor.getElement(1, 1).value == 'ZPHA MODULE'
			etlProcessor.getElement(2, 1).value == 'SLIDEAWAY'
	}

	void 'test can transform a field value with uppercase transformation inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform { 
								uppercase() 
							}
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'SRW24G1'
			etlProcessor.getElement(1, 1).value == 'ZPHA MODULE'
			etlProcessor.getElement(2, 1).value == 'SLIDEAWAY'
	}

	void 'test can transform a field value to lowercase transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with lowercase()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'srw24g1'
			etlProcessor.getElement(1, 1).value == 'zpha module'
			etlProcessor.getElement(2, 1).value == 'slideaway'
	}

	void 'test can transform a field value to lowercase transformation inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform { 
								lowercase()
							}    
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'srw24g1'
			etlProcessor.getElement(1, 1).value == 'zpha module'
			etlProcessor.getElement(2, 1).value == 'slideaway'
	}

	void 'test can transform a field value with taking left 4 characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with left(4)
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to left 4 transformation'
			etlProcessor.getElement(0, 1).value == "SRW2"
			etlProcessor.getElement(1, 1).value == "ZPHA"
			etlProcessor.getElement(2, 1).value == "Slid"
	}

	void 'test can transform a field value with taking left 4 characters inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform {
										left(4)
								  }
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to left 4 transformation'
			etlProcessor.getElement(0, 1).value == "SRW2"
			etlProcessor.getElement(1, 1).value == "ZPHA"
			etlProcessor.getElement(2, 1).value == "Slid"
	}

	void 'test can transform a field value with taking middle 2 characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with middle(3, 2) lowercase()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 1).value == "w2"
			etlProcessor.getElement(1, 1).value == "ha"
			etlProcessor.getElement(2, 1).value == "id"
	}

	void 'test can throw an exception when a middle transformation is staring in zero'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with middle(0, 2) lowercase()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'
	}

	void 'test can transform a field value with taking middle 2 characters inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with middle(3, 2) lowercase()  
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 1).value == "w2"
			etlProcessor.getElement(1, 1).value == "ha"
			etlProcessor.getElement(2, 1).value == "id"
	}

	void 'test can transform a field value striping first A characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase() first('A')
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row striping first "A" character'
			etlProcessor.getElement(0, 1).value == "SRW24G1"
			etlProcessor.getElement(1, 1).value == "ZPH MODULE"
			etlProcessor.getElement(2, 1).value == "SLIDEWAY"
	}

	void 'test can transform a field value striping last A characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase() last('A')
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row striping last "A" character'
			etlProcessor.getElement(0, 1).value == "SRW24G1"
			etlProcessor.getElement(1, 1).value == "ZPH MODULE"
			etlProcessor.getElement(2, 1).value == "SLIDEAWY"
	}

	void 'test can transform a field value striping all A characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase() all('A')
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row striping all "A" characters'
			etlProcessor.getElement(0, 1).value == "SRW24G1"
			etlProcessor.getElement(1, 1).value == "ZPH MODULE"
			etlProcessor.getElement(2, 1).value == "SLIDEWY"
	}

	void 'test can apply another transformation for a field value after striping all A characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase() all('A') lowercase()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row striping all "A" characters'
			etlProcessor.getElement(0, 1).value == "srw24g1"
			etlProcessor.getElement(2, 1).value == "slidewy"
			etlProcessor.getElement(1, 1).value == "zph module"
	}

	void 'test can transform a field value with taking right 4 characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with right(4)
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed with right 4 transformation'
			etlProcessor.getElement(0, 1).value == "24G1"
			etlProcessor.getElement(1, 1).value == "DULE"
			etlProcessor.getElement(2, 1).value == "away"
	}

	void 'test can transform a use left 4 transformation in a chain of transformations'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(etlProcessor.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with left(4) lowercase()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == "srw2"
			etlProcessor.getElement(1, 1).value == "zpha"
			etlProcessor.getElement(2, 1).value == "slid"
	}

	void 'test can transform a field value using replace command with a String value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						read labels
						iterate {
							extract 'vendor name' transform with trim() replace(Inc, Incorporated) load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			with(etlProcessor.getElement(0, 1)) {
				value == "Microsoft\b\nIncorporated"
				field.name == "appVendor"
			}

			with(etlProcessor.getElement(1, 1)) {
				value == "Mozilla\t\t\0Incorporated"
				field.name == "appVendor"
			}
	}

	void 'test can transform a field value using replace command with a Regular expression value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						read labels
						iterate {
							extract 'vendor name' transform with trim() replace(/a|b|c/, '') load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			with(etlProcessor.getElement(0, 1)) {
				value == "Mirosoft\b\nIn"
				field.name == "appVendor"
			}

			with(etlProcessor.getElement(1, 1)) {
				value == "Mozill\t\t\0In"
				field.name == "appVendor"
			}
	}

	void 'test can apply transformations on a field value many times'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase() lowercase()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == "srw24g1"
			etlProcessor.getElement(1, 1).value == "zpha module"
			etlProcessor.getElement(2, 1).value == "slideaway"
	}

	void 'test can check syntax errors at parsing time'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Device
						read labels
						iterate 
							extract 'MODEL NAME' transform with unknown()
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			thrown MultipleCompilationErrorsException
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
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
							console on
							domain Device
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			console.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
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

	void 'test can translate an extracted value using a dictionary'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), environmentDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""domain Device
							def final dictionary = [prod: 'Production', dev: 'Development']
							read labels
							iterate {
								extract 'environment' transform with lowercase() translate(dictionary)
							}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'The column is trsanlated for every row'
			etlProcessor.getElement(0, 3).value == "Production"
			etlProcessor.getElement(1, 3).value == "Production"
			etlProcessor.getElement(2, 3).value == "Development"
	}

	void 'test can load field with an extracted element value after validate fields specs'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, buildFieldSpecsFor(AssetClass.DEVICE))

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

				with(data[1].fields.appVendor) {
					value == 'Microsoft'
					originalValue == 'Microsoft'
				}

				with(data[2].fields.id) {
					value == '152256'
					originalValue == '152256'
				}

				with(data[3].fields.appVendor) {
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

				with(data[1].fields.name) {
					value == 'SRW24G1'
					originalValue == 'SRW24G1'
				}

				with(data[2].fields.id) {
					value == '1523'
					originalValue == '1523'
				}

				with(data[3].fields.name) {
					value == 'ZPHA MODULE'
					originalValue == 'ZPHA MODULE'
				}
			}

		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	void 'test can use if else groovy clause to load a field with an extracted element value'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
									extract 'vendor name' set myVar
									
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

	void 'test can append strings and element in a transformation chain'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
									extract 'vendor name' transform with lowercase() set myVar
									
									extract 'location' transform with append(' - ', myVar) load description
								  
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.description) {
					value == 'ACME Data Center - microsoft'
					originalValue == 'ACME Data Center'
				}

				with(data[1].fields.description) {
					value == 'ACME Data Center - mozilla'
					originalValue == 'ACME Data Center'
				}
			}
	}

	void 'test can plus strings, current element and a defined variable in a transformation'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), applicationDataSet, console, validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
								read labels
								domain Application
								
								iterate {
									extract 'vendor name' transform with lowercase() set myVar
									
									extract 'location' transform with append(myVar + ' - ' + CE) load description
								  
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Centermicrosoft - ACME Data Center'
				}

				with(data[1].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Centermozilla - ACME Data Center'
				}
			}
	}

	void 'test can append strings, current element and a defined variable in a transformation'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
									extract 'vendor name' transform with lowercase() set myVar
									
									extract 'location' transform with append('-', myVar, '-' , CE ) load description
								  
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Center-microsoft-ACME Data Center'
				}

				with(data[1].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Center-mozilla-ACME Data Center'
				}
			}
	}

	void 'test can append strings and elements in a transformation'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
									extract 'vendor name' transform with lowercase() set myVar
									
									extract 'location' transform with append(' - ', myVar, ' - ') load description
								  
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Center - microsoft - '
				}

				with(data[1].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Center - mozilla - '
				}
			}
	}

	void 'test can use a set element in a transformation'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
									extract 'vendor name' transform with lowercase() set myVar
									
									extract 'location' transform with append(' - ', myVar) load description
								  
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Center - microsoft'
				}

				with(data[1].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'ACME Data Center - mozilla'
				}
			}
	}

	void 'test can use a set element in a transformation closure'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
									extract 'location' transform { 
										lowercase() append('**') 
								} load description
								  
								}""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'acme data center**'
				}

				with(data[1].fields.description) {
					originalValue == 'ACME Data Center'
					value == 'acme data center**'
				}
			}
	}

	void 'test can load field many times with the same extracted value'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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

				with(data[1].fields.description) {
					originalValue == "Microsoft"
					value == "Microsoft"
				}

				with(data[2].fields.appVendor) {
					originalValue == "Mozilla"
					value == "Mozilla"
				}

				with(data[3].fields.description) {
					originalValue == "Mozilla"
					value == "Mozilla"
				}
			}
	}

	void 'test can throw an ETLProcessorException when try to load without domain definition'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

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
			e.message == 'There is not validator for domain Application'

	}

	void 'test can throw an ETLProcessorException when try to load with domain definition but without domain fields specification'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
			e.message == 'The domain Application does not have specifications for field: vendedor'
	}

	void 'test can extract a field value and load into a domain object property name'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))

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
				field.name == "appVendor"
			}
	}


	void 'test can create new results loading values without extract previously'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, buildFieldSpecsFor(AssetClass.DEVICE))

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

				with(data[1].fields.id) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[2].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				with(data[3].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[4].fields.id) {
					originalValue == '152255'
					value == '152255'
				}

				with(data[5].fields.appVendor) {
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

				with(data[1].fields.location) {
					originalValue == 'Development'
					value == 'Development'
				}

				with(data[2].fields.id) {
					originalValue == '152255'
					value == '152255'
				}

				with(data[3].fields.location) {
					originalValue == 'Development'
					value == 'Development'
				}
			}
	}

	void 'test can find a domain Property Name with loaded Data Value'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, buildFieldSpecsFor(AssetClass.DEVICE))

		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						console on
						read labels
						iterate {
							domain Application
							set environment with Production
							extract 'application id' load id
							find Application 'for' id by id with SOURCE.'application id'
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[2].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[3].fields.id) {
					originalValue == '152255'
					value == '152255'
				}
			}
	}

	void 'test can find a domain Property Name with loaded Data Value using elseFind command'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, buildFieldSpecsFor(AssetClass.DEVICE))

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,vendor name,technology,location
				152255,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""".stripIndent())

		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						console on
						read labels
						iterate {
							domain Application
							set environment with Production
							extract 'application id' load id
							find Application 'for' id by id with SOURCE.'application id'
							elseFind Application 'for' id by assetClass, assetName with 'Application', SOURCE.'location'
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[2].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[3].fields.id) {
					originalValue == '152255'
					value == '152255'
				}
			}

		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with loaded Data Value for a dependent'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, buildFieldSpecsFor(AssetClass.DEVICE))

		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						console on
						read labels
						domain Dependency
						
						iterate {
						   // Try to find the Application using different searches
							find Application by id with assetId for assetId 
							find Application by assetName, assetType with primaryName, primaryType for assetId 
							find Application by assetName with primaryName for assetId
							find Asset by assetName with primaryName for assetId warn 'found with wrong asset class'
							
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == 'Application'
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[2].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[3].fields.id) {
					originalValue == '152255'
					value == '152255'
				}
			}
	}

	void 'test can throw an Exception if script find to a domain Property and it was not defined in the ETL Processor'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, buildFieldSpecsFor(AssetClass.DEVICE))

		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.assetName == args.assetName && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuffer()),
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						console on
						read labels
						iterate {
							domain Application
							set environment with Production
							extract 'application id' load id
							find Application 'for' id by id with id
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'It throws an Exception because project was not defined'

			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Project not defined.'
	}

	void 'test can find multiple asset entities for a domain Property Name with loaded Data Value'() {

		given:
			ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
			validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, buildFieldSpecsFor(AssetClass.APPLICATION))
		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.assetName == args.assetName && it.project.id == args.project.id }
			}

		and:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, applicationDataSet, console, validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						console on
						read labels
						iterate {
							domain Application
							set environment with Production
							extract 'location' load Vendor
							find assetName by Vendor
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'

			with(etlProcessor.results.get(ETLDomain.Application)[0]) {

				with(elements[0]) {
					originalValue == "Production"
					value == "Production"
					field.name == "environment"
				}

				with(elements[1]) {
					originalValue == "ACME Data Center"
					value == "ACME Data Center"
					field.name == "Vendor"
					field.label == "Vendor"
				}

				reference == [152254, 152255]
			}

			with(etlProcessor.results.get(ETLDomain.Application)[1]) {

				with(elements[0]) {
					originalValue == "Production"
					value == "Production"
					field.name == "environment"
				}

				with(elements[1]) {
					originalValue == "ACME Data Center"
					value == "ACME Data Center"
					field.name == "Vendor"
					field.label == "Vendor"
				}

				reference == [152254, 152255]
			}
	}

	void 'test can trim element values to remove leading and trailing whitespaces'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						read labels
						iterate {
							extract 'vendor name' transform with trim() load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getRow(0).getElement(1).value == "Microsoft\b\nInc"
			etlProcessor.getRow(0).getElement(1).field.name == "appVendor"

			etlProcessor.getRow(1).getElement(1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getRow(1).getElement(1).field.name == "appVendor"

	}

	void 'test can sanitize element value to replace all of the escape characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						domain Application
						read labels
						iterate {
							extract 'vendor name' transform with sanitize() load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getRow(0).getElement(1).value == "Microsoft~+Inc"
			etlProcessor.getRow(0).getElement(1).field.name == "appVendor"

			etlProcessor.getRow(1).getElement(1).value == "Mozilla++~Inc"
			etlProcessor.getRow(1).getElement(1).field.name == "appVendor"

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
			etlProcessor.getElement(0, 1).field.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getElement(1, 1).field.name == "appVendor"

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
			etlProcessor.getElement(0, 1).field.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getElement(1, 1).field.name == "appVendor"

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
			etlProcessor.getElement(0, 1).field.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla++~Inc"
			etlProcessor.getElement(1, 1).field.name == "appVendor"

	}

	void 'test can transform globally a field value using replace command with a String value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						trim on
						replace Inc, Incorporated
						domain Application
						read labels
						iterate {
							extract 'vendor name' load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nIncorporated"
			etlProcessor.getElement(0, 1).field.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Incorporated"
			etlProcessor.getElement(1, 1).field.name == "appVendor"
	}

	void 'test can transform globally a field value using replace command using a range in the iteration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						trim on
						replace ControlCharacters with '~'
						domain Application
						read labels
						from 1 to 2 iterate {
							extract 'vendor name' load appVendor
						}
					""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nInc"
			etlProcessor.getElement(0, 1).field.name == "appVendor"
	}

	/**
	 * Helper method to create Fields Specs based on Asset Class definition
	 * @param assetClass
	 * @return
	 */
	private List<Map<String, ?>> buildFieldSpecsFor(AssetClass assetClass) {

		List<Map<String, ?>> fieldSpecs = []
		switch (assetClass) {
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
				control    : type,
				default    : '',
				field      : field,
				imp        : 'U',
				label      : label,
				order      : 0,
				shared     : 0,
				show       : 0,
				tip        : "",
				udf        : 0
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
