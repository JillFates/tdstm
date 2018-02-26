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
import spock.lang.Shared

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>domain</b></li>
 *     <li><b>extract</b></li>
 *     <li><b>load</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLExtractLoadSpec extends ETLBaseSpec {

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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

	void 'test can throw an Exception if the script command is not recognized'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("invalid command", ETLProcessor.class.name)

		then: 'An MissingMethodException exception is thrown'
			MissingMethodException missingMethodException = thrown MissingMethodException
			missingMethodException.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber == 1
	}

	void 'test can read labels from dataSource and create a map of columns'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
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
	 * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
	 * 	The extract puts the value into a local register that can then be manipulated and eventually
	 * 	saved into the target domain object.
	 */
	void 'test can extract a field value over all rows based on column ordinal position'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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
			GroovyMock(Room, global: true)
			Room.isAssignableFrom(_) >> { Class<?> clazz ->
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

}
