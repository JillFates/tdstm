package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.TimeUtil
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.json.JSONConnection
import getl.json.JSONDataset
import getl.proc.Flow
import getl.tfs.TFS
import getl.utils.FileUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import org.apache.http.client.utils.DateUtils
import spock.lang.See
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>domain</b></li>
 *     <li><b>extract</b></li>
 *     <li><b>load</b></li>
 *     <li><b>read labels</b></li>
 *     <li><b>ignore record</b></li>
 * </ul>
 */
@TestMixin(ControllerUnitTestMixin)
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model, AssetOptions])
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
			updater(['application id': 152254, 'vendor name': 'Microsoft', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': 152255, 'vendor name': 'Mozilla', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}

		debugConsole = new DebugConsole(buffer: new StringBuilder())

		nonSanitizedDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

		new Flow().writeTo(dest: nonSanitizedDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['application id': '152254', 'vendor name': '\r\n\tMicrosoft\b\nInc\r\n\t', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': '152255', 'vendor name': '\r\n\tMozilla\t\t\0Inc\r\n\t', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}

		validator = createDomainClassFieldsValidator()
	}

	void 'test can define a the primary domain'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project),
				GroovyMock(DataSetFacade),
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Application
			 """.stripIndent())

		then: 'A domain is selected'
			etlProcessor.selectedDomain.domain == ETLDomain.Application

		and: 'A new result was added in the result'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}
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
			etlProcessor.evaluate("""
				// Script supports one line comments
				domain Application
				/*
					And multiple Lines comments
				*/
			 """.stripIndent())

		then: 'A domain is selected'
			etlProcessor.selectedDomain.domain == ETLDomain.Application

		and: 'A new result was added in the result'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}
			}
	}

	void 'test can throw an exception if an invalid domain is defined'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Unknown
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'No such property: Unknown'
	}

	void 'test can define a several domains in an ETL script'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Application
				domain Device
				domain Storage
			""".stripIndent())

		then: 'The last domain selected could be recovered'
			etlProcessor.selectedDomain.domain == ETLDomain.Storage

		and: 'A new result was added in the result'
			with(etlProcessor.finalResult()) {
				domains.size() == 3
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 0
				}

				with(domains[2], DomainResult) {
					domain == ETLDomain.Storage.name()
					data.size() == 0
				}
			}
	}

	void 'test can define a domain more than once in an ETL script'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), GroovyMock(DataSetFacade), GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Application
				domain Device
				domain Application
			""".stripIndent())

		then: 'The last domain selected could be recovered'
			etlProcessor.selectedDomain.domain == ETLDomain.Application

		and: 'A new result was added in the result'

			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 0
				}
			}
	}

	void 'test can throw an Exception if the script command is not recognized'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("invalid on")

		then: 'An MissingMethodException exception is thrown'
			ETLProcessorException exception = thrown ETLProcessorException
			exception.message == 'No such property: invalid'
	}

	void 'test can read labels from dataSource and create a map of columns'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				sixRowsDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator)
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Device
				read labels
			""".stripIndent())

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
			etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 2
				}

			""".stripIndent())

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
			etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 'model name'
				}
			""".stripIndent())

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
			etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 'model'
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.extractMissingColumn('model').message

	}

	void 'test can throw an Exception if a column position is zero'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 0
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'
	}

	void 'test can throw an Exception if a column index is not between row elements range'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 10000
				}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Invalid index = 10000"
	}

	void 'test can load field with an extracted element value after validate fields specs'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'Vendor'
					extract 'technology' load 'appTech'
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'appTech'] as Set
					with(fieldLabelMap) {
						appVendor == 'Vendor'
						appTech == 'Technology'
					}

					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						fields.keySet().size() == 2
						with(fields.appVendor, FieldResult) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == null
						}
						with(fields.appTech, FieldResult) {
							value == '(xlsx updated)'
							originalValue == '(xlsx updated)'
							init == null
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						fields.keySet().size() == 2
						with(fields.appVendor, FieldResult) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						with(fields.appTech, FieldResult) {
							value == 'NGM'
							originalValue == 'NGM'
							init == null
						}
					}
				}
			}
	}

	void 'test can load a field using a string literal'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name'
					if ( CE == 'Microsoft'){
						load 'Name' with 'This is a Microsoft Application'
					} else {
						load 'environment' with 'This is not a Microsoft Application'
					}
				}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName', 'environment'] as Set
					with(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
					}

					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'This is a Microsoft Application'
							originalValue == 'This is a Microsoft Application'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.environment) {
							value == 'This is not a Microsoft Application'
							originalValue == 'This is not a Microsoft Application'
						}
					}
				}
			}
	}

	void 'test can load a field using DOMAIN property'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					extract 'location' load 'environment'

					if ( CE == 'Microsoft'){
						load 'Name' with DOMAIN.appVendor
					} else {
						load 'Name' with DOMAIN.environment
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment', 'assetName'] as Set
					with(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
						appVendor == 'Vendor'
					}
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
						with(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						with(fields.assetName) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						with(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						with(fields.assetName) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
					}
				}
			}
	}

	void 'test can load a field using SOURCE.property'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					extract 'location' load 'environment'

					if ( CE == 'Microsoft'){
						load 'Name' with SOURCE.'vendor name'
					} else {
						load 'Name' with SOURCE.'application id'
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment', 'assetName'] as Set
					with(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
						appVendor == 'Vendor'
					}
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
						with(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						with(fields.assetName) {
							value == '152254'
							originalValue == '152254'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						with(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						with(fields.assetName) {
							value == '152255'
							originalValue == '152255'
						}
					}
				}
			}
	}

	void 'test can load a field with a local variable'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name'
						def myLocalVar = CE

						if ( myLocalVar == 'Microsoft'){
							load 'appVendor' with myLocalVar
						} else {
							load 'environment' with myLocalVar
						}
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					with(fieldLabelMap) {
						appVendor == 'Vendor'
						environment == 'Environment'
					}
					data.size() == 2
					with(fieldLabelMap) {
						appVendor: 'Vendor'
						environment: 'Environment'
					}
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.environment) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
					}
				}
			}
	}

	void 'test can load field many times with the same extracted value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name' load 'appVendor' load 'Description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					with(fieldLabelMap) {
						description == 'Description'
					}
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							originalValue == "Microsoft"
							value == "Microsoft"
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.description) {
							originalValue == "Microsoft"
							value == "Microsoft"
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.appVendor) {
							originalValue == "Mozilla"
							value == "Mozilla"
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.description) {
							originalValue == "Mozilla"
							value == "Mozilla"
						}
					}
				}
			}
	}

	void 'test can throw an ETLProcessorException when try to load without domain definition'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					domain Application
					read labels
					iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.unknownDomainFieldName(ETLDomain.Application, 'appVendor').message

	}

	void 'test can throw an ETLProcessorException when try to load with domain definition but without domain fields specification'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name' load 'vendedor'
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.unknownDomainFieldName(ETLDomain.Application, 'vendedor').message
	}

	void 'test can extract a field value and load into a domain object property name'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					domain Application
					read labels
					iterate {
						extract 'application id' load 'id'
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					with(fieldLabelMap) {
						id == 'Id'
						appVendor == 'Vendor'
					}
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							originalValue == "Microsoft"
							value == "Microsoft"
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.appVendor) {
							originalValue == "Mozilla"
							value == "Mozilla"
						}
					}
				}
			}
	}

	@See('TM-9283')
	void 'test correct trimming of spaces in column names'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
			name , mfg, model
			xraysrv01,Dell,PE2950
			oradbsrv02,HP,DL8150
			oradbsrv03,HP,DL8155""".stripIndent())
		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName'
					extract 'mfg' load 'manufacturer'
					extract 3 load 'model'
				}
			""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					with(fieldLabelMap) {
						assetName == 'Name'
						manufacturer == 'Manufacturer'
						model == 'Model'
					}
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							originalValue == "xraysrv01"
							value == "xraysrv01"
						}
						with(fields.manufacturer) {
							originalValue == "Dell"
							value == "Dell"
						}
						with(fields.model) {
							originalValue == "PE2950"
							value == "PE2950"
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							originalValue == "oradbsrv02"
							value == "oradbsrv02"
						}
						with(fields.manufacturer) {
							originalValue == "HP"
							value == "HP"
						}
						with(fields.model) {
							originalValue == "DL8150"
							value == "DL8150"
						}
					}

					with(data[2], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 3
						with(fields.assetName) {
							originalValue == "oradbsrv03"
							value == "oradbsrv03"
						}
						with(fields.manufacturer) {
							originalValue == "HP"
							value == "HP"
						}
						with(fields.model) {
							originalValue == "DL8155"
							value == "DL8155"
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can create new results loading values without extract previously'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					read labels
					iterate {
						domain Application
						load 'environment' with 'Production'
						extract 1 load 'id'
						extract 'vendor name' load 'Vendor'

						domain Device
						extract 1 load 'id'
						load 'description' with 'Development'
					}
				""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					with(fieldLabelMap) {
						appVendor == 'Vendor'
					}
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.environment) {
							originalValue == 'Production'
							value == 'Production'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							originalValue == 'Microsoft'
							value == 'Microsoft'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.environment) {
							originalValue == 'Production'
							value == 'Production'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							originalValue == 'Microsoft'
							value == 'Microsoft'
						}
					}
				}


				with(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.description) {
							originalValue == 'Development'
							value == 'Development'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.description) {
							originalValue == 'Development'
							value == 'Development'
						}
					}
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
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Room
					extract 'roomId' load 'id'
					extract 'Name' load 'roomName'
				}
				""".stripIndent())

		then: 'Results should contain Room domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

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
				13358,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,Source,41,42,0,0,0,block3x5,L""".
				stripIndent())

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
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Rack
					extract 'rackId' load 'id'
					extract 'Location' load 'location'
					extract 'Room' load 'room'
				}
			""".stripIndent())

		then: 'Results should contain Rack domain results associated'
			etlProcessor.finalResult().domains.size() == 1

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can evaluate a value loaded into the DOMAIN.property'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					if (DOMAIN.appVendor.startsWith('Mi')){
						load 'environment' with 'Production'
					} else {
						load 'environment' with 'Development'
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment'] as Set
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
					}

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.environment) {
							value == 'Production'
							originalValue == 'Production'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.environment) {
							value == 'Development'
							originalValue == 'Development'
						}
					}
				}
			}
	}

	void 'test can throw an exception if an domain is not specified'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				rackId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
				13144,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,Source,500,235,3300,3300,0,Rack,R
				13145,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,Source,280,252,3300,3300,0,Rack,L
				13167,VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,Source,160,0,1430,1430,0,Rack,R
				13187,Storage,ACME Data Center,42U Rack,ACME Data Center / DC1,Source,1,15,0,0,0,Object,L
				13358,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,Source,41,42,0,0,0,block3x5,L""".
				stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator)
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels

				iterate {
					extract 1 load 'id'
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown()
			e.message == 'A \'domain Class\' must be specified before any load or find commands'

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an exception if script tries evaluate an invalid method loaded into the DOMAIN.property'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					if (DOMAIN.appVendor.unknownMethod('Mi')){
						set environmentVar with 'Production'
					} else {
						set environmentVar with 'Development'
					}
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			MissingMethodException e = thrown MissingMethodException
			e.message == 'No signature of method: java.lang.String.unknownMethod() is applicable for argument types: (java.lang.String) values: [Mi]'
	}

	void 'test can evaluate a value loaded into the SOURCE.property'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					if (!SOURCE.technology.startsWith('NGM')){
						load 'environment' with 'Production'
					} else {
						load 'environment' with 'Development'
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment'] as Set

					with(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
					}

					with(data[0].fields.environment) {
						value == 'Production'
						originalValue == 'Production'
					}

					with(data[1].fields.appVendor) {
						value == 'Mozilla'
						originalValue == 'Mozilla'
					}

					with(data[1].fields.environment) {
						value == 'Development'
						originalValue == 'Development'
					}
				}
			}
	}

	void 'test can throw an exception if script tries evaluate an invalid method loaded into the SOURCE.property'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					if (!SOURCE.technology.unknownMethod('NGM')){
						set environmentVar with 'Production'
					} else {
						set environmentVar with 'Development'
					}
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			MissingMethodException e = thrown MissingMethodException
			e.message == 'No signature of method: com.tdsops.etl.SourceField.unknownMethod() is applicable for argument types: (java.lang.String) values: [NGM]'
	}

	void 'test can ignore current row based on some condition'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					domain Application
					extract 'application id' load 'id'
					extract 'vendor name' load 'appVendor'

					if (!SOURCE.'vendor name'.startsWith('Mi')){
						ignore record
					} else {
						domain Device
						extract 'application id' load 'id'
						extract 'technology' load 'Name'
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							value == '152254'
							originalValue == '152254'
						}
					}

					with(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
					}
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['id', 'assetName'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							value == '152254'
							originalValue == '152254'
						}

						with(fields.assetName) {
							value == '(xlsx updated)'
							originalValue == '(xlsx updated)'
						}
					}
				}
			}
	}

	void 'test can ignore current row more than once in the same iteration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					domain Application
					extract 'application id' load 'id'
					extract 'vendor name' load 'appVendor'

					if (!SOURCE.'vendor name'.startsWith('Mi')){
						ignore record
					} else {
						domain Device
						extract 'application id' load 'id'
						extract 'technology' load 'Name'

						if(DOMAIN.assetName.contains('updated')){
							ignore record
						} else {
							domain Database
							extract 'application id' load 'id'
						}
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.id) {
							value == '152254'
							originalValue == '152254'
						}
					}

					with(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
					}
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['id', 'assetName'] as Set
					data.isEmpty()
				}
			}
	}

	void 'test can throw and exception when script tries to ignore a row and there isn not a domain already defined'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
			validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE))
			validator.addAssetClassFieldsSpecFor(ETLDomain.Database, buildFieldSpecsFor(AssetClass.DATABASE))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					ignore record
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'A \'domain Class\' must be specified before any load or find commands'
	}

	void 'test can ignore even if results are empty'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'technology'
					if( CE == 'NGM') {
						ignore record
					} else {
						load 'Name' with CE
					}
				}
			""".stripIndent())

		then: 'Results will ignore a row'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName'] as Set
					data.size() == 1
				}
			}
	}

	void 'test can ignore records without loading values previously'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'technology'
					if( CE != 'NGM') {
						ignore record
					} else {
						load 'Name' with CE
					}
				}
			""".stripIndent())

		then: 'Results will ignore a row'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName'] as Set
					data.size() == 1
				}
			}
	}

	void 'test can ignore records in the middle of a data set'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				sixRowsDataSet,
				GroovyMock(DebugConsole),
				validator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'device id' load 'id'
					extract 'model name' transform with lowercase() load 'Name'

					if( SOURCE.'device id'.startsWith('152253') ){
						ignore record
					}

				}
			""".stripIndent())

		then: 'Third row was removed from the domain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['id', 'assetName'] as Set
					data.size() == 5

					data*.rowNum == [1, 2, 4, 5, 6]
					data.collect { it.fields.id.value } == [
						'152251', '152252', '152254', '152255', '152256'
					]
					data.collect { it.fields.assetName.value } == [
						'srw24g1', 'srw24g2', 'srw24g4', 'srw24g5', 'zpha module'
					]
					data.collect { it.fields.assetName.originalValue } == [
						'SRW24G1', 'SRW24G2', 'SRW24G4', 'SRW24G5', 'ZPHA MODULE'
					]
				}
			}
	}

	void 'test can set a local variable with a string literal'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())
		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					set myLocalVar with 'Custom Name'
					load 'Name' with myLocalVar
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can set a local variable with a SOURCE.property'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					set myLocalVar with SOURCE.'name'
					load 'Name' with myLocalVar
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can set a local variable with a DOMAIN.property'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'type' load 'environment'
					set myLocalVar with DOMAIN.environment
					load 'Name' with myLocalVar
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['environment', 'assetName'] as Set
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.environment) {
							value == 'Server'
							originalValue == 'Server'
						}
						with(fields.assetName) {
							value == 'Server'
							originalValue == 'Server'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.environment) {
							value == 'Blade'
							originalValue == 'Blade'
						}
						with(fields.assetName) {
							value == 'Blade'
							originalValue == 'Blade'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can set a multiple local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'name' load 'Name'
					load 'custom1' with 'abc'

					extract 'mfg' set myMfgVar
					myMfgVar += " (" + extract('type') + ")"
					load 'Manufacturer' with myMfgVar

					set anotherVar with 'xyzzy'
					load 'custom2' with anotherVar
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'custom1', 'manufacturer', 'custom2'] as Set
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
						}
						with(fields.custom1) {
							value == 'abc'
							originalValue == 'abc'
						}
						with(fields.manufacturer) {
							value == 'Dell (Server)'
							originalValue == 'Dell (Server)'
						}
						with(fields.custom2) {
							value == 'xyzzy'
							originalValue == 'xyzzy'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
						}
						with(fields.custom1) {
							value == 'abc'
							originalValue == 'abc'
						}
						with(fields.manufacturer) {
							value == 'HP (Blade)'
							originalValue == 'HP (Blade)'
						}
						with(fields.custom2) {
							value == 'xyzzy'
							originalValue == 'xyzzy'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test NOW variable'() {

		given:
			String iso8601 = TimeUtil.FORMAT_DATE_TIME_ISO8601.replace("'", '')
			def dataSetCSV = """
				name
				fubar
			""".stripIndent().trim()
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(dataSetCSV)

		and:
			def scriptContent = """
				console on
				domain Device
				read labels
				iterate {
				    extract 'name' load 'Name'
					load 'Network Interfaces' with NOW
				}
			""".stripIndent().trim()

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate(scriptContent)

		then:
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						with(fields, Map) {

							with(assetName, FieldResult) {
								value == 'fubar'
								originalValue == 'fubar'
							}

							with(custom1, FieldResult) {
								Date date = DateUtils.parseDate(value, TimeUtil.FORMAT_DATE_TIME_ISO8601)
								assert date != null: "$value is not parseable using ISO8601 format (${TimeUtil.FORMAT_DATE_TIME_ISO8601})"
							}
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-9995')
	void 'test can declare local variables outside the iteration command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				set myLocalVar with 'Custom Name'
				iterate {
					domain Device
					load 'Name' with myLocalVar
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if variable names does not end with Var postfix'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				set myLocalVariable with 'Custom Name'
				iterate {
					domain Device
					load 'Name' with myLocalVar
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('myLocalVariable').message

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if variable names is used incorrectly in second time'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'

			etlProcessor.evaluate("""
				console on
				read labels
				set myLocalVar with 'Custom Name'
				iterate {
					domain Device
					set myLocalVar with 'Another value'
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.invalidSetParameter().message

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-10678')
	void 'test can load a new row using twice the domain command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					domain Device
					extract 'name' load 'Name'
					domain Device
					extract 'model' load 'model'
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
							init == null
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.model) {
							value == 'PE2950'
							originalValue == 'PE2950'
							init == null
						}
					}
				}

			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11037')
	void 'test can load current element with a blank content from an ETL Script'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,,PE2950,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					domain Device
					load 'Name' with ''
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName, FieldResult) {
							value == ''
							originalValue == ''
							init == null
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11037')
	void 'test can init current element with a blank content from an ETL Script'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,,PE2950,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					domain Device
					init 'Name' with ''
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					with(data[0], RowResult) {
						rowNum == 1
						with(fields.assetName, FieldResult) {
							value == null
							originalValue == null
							init == ''
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11037')
	void 'test can init and load with a blank content from an ETL Script'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,,PE2950,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				iterate {
					domain Device
					init 'Name' with ''
					load 'Manufacturer' with ''
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName, FieldResult) {
							value == null
							originalValue == null
							init == ''
						}
						with(fields.manufacturer, FieldResult) {
							value == ''
							originalValue == ''
							init == null
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-10726')
	void 'test can transform with concat function'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					read labels
					iterate {
						domain Application
						load 'environment' transform with concat(',', SOURCE.'vendor name', SOURCE.'location')
					}
				""".stripIndent())

		then: 'Results should contain Application vendor name and location domain fields concatenated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 2

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.environment) {
							originalValue == 'Microsoft,ACME Data Center'
							value == 'Microsoft,ACME Data Center'
						}
					}
					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.environment) {
							originalValue == 'Mozilla,ACME Data Center'
							value == 'Mozilla,ACME Data Center'
						}
					}
				}
			}
	}

	@See('TM-10726')
	void 'test can transform with concat transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					read labels
					iterate {
						domain Application
						set envVar with 'Prod'
						extract 'vendor name' transform with append('-', envVar) load 'environment'
					}
				""".stripIndent())

		then: 'Results should contain Application vendor name and location domain fields concatenated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 2

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.environment) {
							originalValue == 'Microsoft'
							value == 'Microsoft-Prod'
						}
					}
					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.environment) {
							originalValue == 'Mozilla'
							value == 'Mozilla-Prod'
						}
					}
				}
			}
	}

	@See('TM-10726')
	void 'test can load with append transformation'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
					srv,ip
					x,1.2.3.4
					y,4.5.4.2
					x,
					z,3.3.3.3
					x,1.3.5.1
					""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
						read labels
						iterate {
							domain Device
							extract 'ip' transform with lowercase() set ipVar
							extract 'srv' set srvVar

							lookup 'assetName' with srvVar
							if ( LOOKUP.notFound() ) {
								// Set the server name first time seen
								load 'Name' with srvVar
							}
							load 'IP Address' transform with append(', ', ipVar)
						}
					""".stripIndent())

		then: 'Results should contain Application vendor name and location domain fields concatenated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 3

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							originalValue == 'x'
							value == 'x'
						}
						with(fields.ipAddress) {
							originalValue == '1.2.3.4, 1.3.5.1'
							value == '1.2.3.4, 1.3.5.1'
						}
					}
					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							originalValue == 'y'
							value == 'y'
						}
						with(fields.ipAddress) {
							originalValue == '4.5.4.2'
							value == '4.5.4.2'
						}
					}
					with(data[2], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 4
						with(fields.assetName) {
							originalValue == 'z'
							value == 'z'
						}
						with(fields.ipAddress) {
							originalValue == '3.3.3.3'
							value == '3.3.3.3'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-10726')
	void 'test load with append transformation should fail'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
						read labels
						iterate {
							domain Application
							set envVar with 'Prod'

							load 'Name' with append('-', envVar)
						}
					""".stripIndent())

		then: 'exception should be thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'No such property: append'
	}

	@See('TM-11530')
	void 'test can use set command with local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				FirstName,LastName
				Tony,Baker
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'firstname' set firstNameVar
					assert firstNameVar == 'Tony'
					
					extract 'lastname' set lastNameVar
					assert lastNameVar == 'Baker'
					
					set fullNameVar with firstNameVar + ' ' + lastNameVar
					assert firstNameVar == 'Tony'
					assert lastNameVar == 'Baker'
					assert fullNameVar == 'Tony Baker'
					
					load 'description' with fullNameVar
				}
				""".stripIndent())

		then: 'Results should contain correctly set full name'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 1

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.description, FieldResult) {
							originalValue == 'Tony Baker'
							value == 'Tony Baker'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11590')
	void 'test can use use when populated qualifier command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description,nothingThere,retire date
				xraysrv01,2,,,2018-06-25
				zuludb01,,Some description,,
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
				read labels
				domain Device
				iterate {
				   extract 'name' load 'assetName'
				   extract 'cpu' load 'custom1' when populated
				   extract 'description' set descVar
				   load 'description' with descVar when populated
				   extract 'nothingThere' load 'custom2' when populated
				   extract 'retire date' transform with toDate() load 'Retire Date' when populated
				}
				""".stripIndent())

		then: 'Results should contain correctly set full name'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields) {
							with(it.assetName, FieldResult) {
								originalValue == 'xraysrv01'
								value == 'xraysrv01'
							}
							with(it.custom1, FieldResult) {
								originalValue == '2'
								value == '2'
							}

							with(it.retireDate, FieldResult) {
								value == new Date(2018 - 1900, 6 - 1, 25)
								init == null
								errors == []
							}
							it.description == null
							it.custom2 == null
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields) {
							with(it.assetName, FieldResult) {
								originalValue == 'zuludb01'
								value == 'zuludb01'
							}
							it.custom1 == null

							with(it.description, FieldResult) {
								originalValue == 'Some description'
								value == 'Some description'
							}
							it.custom2 == null
							it.retireDate == null
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11590')
	void 'test can use use when populated qualifier with a closure definition'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description,nothingThere
				xraysrv01,100,,
				zuludb01,10,Some description,
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
				read labels
				domain Device
				iterate {
				   extract 'name' load 'assetName'
				   extract 'cpu' transform with toInteger() load 'custom1' when { it > 50 }
				}
				""".stripIndent())

		then: 'Results should contain correctly set full name'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2

					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields) {
							with(it.assetName, FieldResult) {
								originalValue == 'xraysrv01'
								value == 'xraysrv01'
							}
							with(it.custom1, FieldResult) {
								originalValue == '100'
								value == 100
							}
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields) {
							with(it.assetName, FieldResult) {
								originalValue == 'zuludb01'
								value == 'zuludb01'
							}
							!it.custom1
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11590')
	void 'test can throw an Exception when populated qualifier is configured incorrectly'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description,nothingThere
				xraysrv01,100,,
				zuludb01,10,Some description,
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
				read labels
				domain Device
				iterate {
				   extract 'name' load 'assetName' when off
				}
				""".stripIndent())

		then: 'It throws an Exception because command is incorrect was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.incorrectWhenCommandStructure().message} at line 5"
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can trim whitespaces after backslash character'() {

		given:
			ETLFieldsValidator validator = new ETLFieldsValidator()
			validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'

			String script = "read labels\n" +
				"domain Application\n" +
				"iterate {\n" +
				"\textract 'vendor name'  \\ \t\t  \n" +
				"\tload 'Vendor'\n" +
				"\textract 'technology'  \\  \t  \r" +
				"\tload 'appTech'\n \r\n" +
				"}\n"

			etlProcessor.evaluate(script)

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'appTech'] as Set
					with(fieldLabelMap) {
						appVendor == 'Vendor'
						appTech == 'Technology'
					}

					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						fields.keySet().size() == 2
						with(fields.appVendor, FieldResult) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == null
						}
						with(fields.appTech, FieldResult) {
							value == '(xlsx updated)'
							originalValue == '(xlsx updated)'
							init == null
						}
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						fields.keySet().size() == 2
						with(fields.appVendor, FieldResult) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						with(fields.appTech, FieldResult) {
							value == 'NGM'
							originalValue == 'NGM'
							init == null
						}
					}
				}
			}
	}

	@See('TM-13627')
	void 'test can throw an exception if undefined variables are referenced in ETL extract load with statements'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				abc
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					extract 'name' load 'Description' with aBogusVariableNameVar
				}
			""".stripIndent())

		then: 'It throws an Exception because comments command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.missingPropertyException('aBogusVariableNameVar').message} at line 5".toString()
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	@See('TM-13627')
	void 'test can throw an exception if undefined variables are referenced in ETL load with statements'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				abc
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					load 'Description' with aBogusVariableNameVar
				}
			""".stripIndent())

		then: 'It throws an Exception because comments command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.missingPropertyException('aBogusVariableNameVar').message} at line 5".toString()
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	@See('TM-13627')
	void 'test can throw an exception if invalid variable is referenced in ETL load with statements'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				abc
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					load 'Name' with aBogusVariableName
				}
			""".stripIndent())

		then: 'It throws an Exception because comments command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.missingPropertyException('aBogusVariableName').message} at line 5".toString()
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			service.deleteTemporaryFile(fileName)
	}

	@See('TM-13627')
	@Unroll
	void 'test can throw an exception if undefined variables are referenced in the ETL statement -> #findStatement'() {

		setup:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				simpleDataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					 $findStatement
				}
			""".stripIndent())

		then: 'It throws an Exception because comments command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.missingPropertyException(variableName).message} at line 5".toString()
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}

		where:
			findStatement                                                       || variableName
			"find Device by 'Name' eq aBogusVariableNameVar into 'id'"          || 'aBogusVariableNameVar'
			"find Device by 'Name' eq invalidVariableName into 'id'"            || 'invalidVariableName'
			"find Device by 'Name' ne aBogusVariableNameVar into 'id'"          || 'aBogusVariableNameVar'
			"find Device by 'Name' ne invalidVariableName into 'id'"            || 'invalidVariableName'
			"find Device by 'Name' nseq aBogusVariableNameVar into 'id'"        || 'aBogusVariableNameVar'
			"find Device by 'Name' lt aBogusVariableNameVar into 'id'"          || 'aBogusVariableNameVar'
			"find Device by 'Name' le aBogusVariableNameVar into 'id'"          || 'aBogusVariableNameVar'
			"find Device by 'Name' gt aBogusVariableNameVar into 'id'"          || 'aBogusVariableNameVar'
			"find Device by 'Name' ge aBogusVariableNameVar into 'id'"          || 'aBogusVariableNameVar'
			"find Device by 'Name' like aBogusVariableNameVar into 'id'"        || 'aBogusVariableNameVar'
			"find Device by 'Name' notLike aBogusVariableNameVar into 'id'"     || 'aBogusVariableNameVar'
			"find Device by 'Name' contains aBogusVariableNameVar into 'id'"    || 'aBogusVariableNameVar'
			"find Device by 'Name' notContains aBogusVariableNameVar into 'id'" || 'aBogusVariableNameVar'
			"find Device by 'Name' inList aBogusVariableNameVar into 'id'"      || 'aBogusVariableNameVar'
			"find Device by 'Name' notInList aBogusVariableNameVar into 'id'"   || 'aBogusVariableNameVar'
			"find Device by 'Name' between aBogusVariableNameVar into 'id'"     || 'aBogusVariableNameVar'
			"find Device by 'Name' notBetween aBogusVariableNameVar into 'id'"  || 'aBogusVariableNameVar'
	}

	@See('TM-13627')
	@Unroll
	void 'test can throw an exception if undefined variables are referenced in the ETL statement -> #statement'() {

		setup:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				simpleDataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					extract 1 load 'Name' set nameVar
					find Device by 'Name' eq nameVar into 'id'
					$statement
				}
			""".stripIndent())

		then: 'It throws an Exception because comments command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.missingPropertyException(variableName).message} at line 9".toString()
				startLine == 9
				endLine == 9
				startColumn == null
				endColumn == null
				fatal == true
			}

		where:
			statement                                                                                                               || variableName
			"whenNotFound 'id' create {\n\t\t\t\t\t\tassetName nameVar\n\t\t\t\t\t\tdescription aBogusVariableNameVar\n\t\t\t\t\t}" || 'aBogusVariableNameVar'
			"whenFound 'id' update {\n\t\t\t\t\t\tassetName nameVar\n\t\t\t\t\t\tdescription aBogusVariableNameVar\n\t\t\t\t\t}"    || 'aBogusVariableNameVar'
	}

}
