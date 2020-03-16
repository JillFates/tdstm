package com.tdsops.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.dataset.CSVDataset
import com.tdsops.etl.dataset.ETLDataset
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.TimeUtil
import getl.csv.CSVConnection
import getl.json.JSONConnection
import getl.json.JSONDataset
import getl.tfs.TFS
import getl.utils.FileUtils
import grails.test.mixin.Mock
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import org.apache.http.client.utils.DateUtils
import spock.lang.See
import spock.lang.Shared
import spock.util.mop.ConfineMetaClassChanges

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
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model, AssetOptions])
class ETLExtractLoadSpec extends ETLBaseSpec {

	@Shared
	Map conParams = [path: "${TFS.systemPath}/test_path_csv", createPath: true, extension: 'csv', codePage: 'utf-8']

	@Shared
	CSVConnection csvConnection

	@Shared
	JSONConnection jsonConnection

	CSVDataset simpleDataset
	DataSetFacade jsonDataSet
	CSVDataset environmentDataSet
	CSVDataset applicationDataSet
	CSVDataset nonSanitizedDataSet
	CSVDataset sixRowsDataset
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
		applicationContextHolder(ApplicationContextHolder) { bean ->
			bean.factoryMethod = 'getInstance'
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

		simpleDataset = new CSVDataset(createCSVFIle("""
				device id,model name,manufacturer name
				152254,SRW24G1,LINKSYS
				152255,ZPHA MODULE,TippingPoint
				152256,Slideaway,ATEN
			""")
		)

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

		environmentDataSet = new CSVDataset(createCSVFIle("""
				device id,model name,manufacturer name,environment
				152254,SRW24G1,LINKSYS,Prod
				152255,ZPHA MODULE,TippingPoint,Prod
				152256,Slideaway,ATEN,Dev
			""")
		)

		sixRowsDataset = new CSVDataset(createCSVFIle("""
				device id,model name,manufacturer name
				152251,SRW24G1,LINKSYS
				152252,SRW24G2,LINKSYS
				152253,SRW24G3,LINKSYS
				152254,SRW24G4,LINKSYS
				152255,SRW24G5,LINKSYS
				152256,ZPHA MODULE,TippingPoint
			""")
		)

		applicationDataSet = new CSVDataset(createCSVFIle("""
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""")
		)

		nonSanitizedDataSet = new CSVDataset(createCSVFIle("""
				application id,vendor name,technology,location
				152254,\r\n\tMicrosoft\b\nInc\r\n\t,(xlsx updated),ACME Data Center
				152255,\r\n\tMozilla\t\t\0Inc\r\n\t,NGM,ACME Data Center
			""")
		)

		debugConsole = new DebugConsole(buffer: new StringBuilder())
		validator = createDomainClassFieldsValidator()
	}

	void 'test can define a the primary domain'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator)
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Application
			 """.stripIndent())

		then: 'A domain is selected'
			etlProcessor.selectedDomain.domain == ETLDomain.Application

		and: 'A new result was added in the result'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
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
				GroovyMock(ETLFieldsValidator)
			)

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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}
			}
	}

	void 'test can throw an exception if an invalid domain is defined'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator)
			)

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
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator)
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Application
				domain Device
				domain Storage
			""".stripIndent())

		then: 'The last domain selected could be recovered'
			etlProcessor.selectedDomain.domain == ETLDomain.Storage

		and: 'A new result was added in the result'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 3
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}

				assertWith(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 0
				}

				assertWith(domains[2], DomainResult) {
					domain == ETLDomain.Storage.name()
					data.size() == 0
				}
			}
	}

	void 'test can define a domain more than once in an ETL script'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator)
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Application
				domain Device
				domain Application
			""".stripIndent())

		then: 'The last domain selected could be recovered'
			etlProcessor.selectedDomain.domain == ETLDomain.Application

		and: 'A new result was added in the result'

			assertWith(etlProcessor.finalResult()) {
				domains.size() == 2
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}

				assertWith(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 0
				}
			}
	}

	void 'test can throw an Exception if the script command is not recognized'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					sixRowsDataset,
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator)
			)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("invalid on")

		then: 'An MissingMethodException exception is thrown'
			ETLProcessorException exception = thrown ETLProcessorException
			exception.message == 'Unrecognized command invalid with args [on]'
	}

	void 'test can read labels from dataSource and create a map of columns'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				sixRowsDataset,
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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataset, GroovyMock(DebugConsole),
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
			assertWith(etlProcessor.currentRow.getElement(1)) {
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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataset, GroovyMock(DebugConsole),
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
			assertWith(etlProcessor.currentRow.getElement(1)) {
				value == "Slideaway"
				originalValue == "Slideaway"
			}
	}

	@See('TM-13617')
	void 'test can can throw an exception if extract command with column name is used without a previous read labels command'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataset,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Device
				iterate {
					extract 'model name'
				}

			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.extractRequiresNameReadLabelsFirst().message
	}

	@See('TM-13617')
	void 'test can can throw an exception if extract command with column position is used without a previous read labels command'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataset,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Device
				iterate {
					extract 1
				}

			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.extractRequiresNameReadLabelsFirst().message
	}

	void 'test can throw an Exception if a column name is invalid'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataset, GroovyMock(DebugConsole),
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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataset, GroovyMock(DebugConsole),
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
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataset, GroovyMock(DebugConsole),
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'appTech'] as Set
					assertWith(fieldLabelMap) {
						appVendor == 'Vendor'
						appTech == 'Technology'
					}

					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						fields.keySet().size() == 2
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == null
						}
						assertWith(fields.appTech) {
							value == '(xlsx updated)'
							originalValue == '(xlsx updated)'
							init == null
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						fields.keySet().size() == 2
						assertWith(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						assertWith(fields.appTech) {
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName', 'environment'] as Set
					assertWith(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
					}

					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'This is a Microsoft Application'
							originalValue == 'This is a Microsoft Application'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.environment) {
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment', 'assetName'] as Set
					assertWith(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
						appVendor == 'Vendor'
					}
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
					}
				}
			}
	}

	@See('TM-14454')
	void 'test can check in DOMAIN variable contains a property'() {

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
						if (DOMAIN.hasProperty('appVendor')){
							load 'Name' with DOMAIN['Vendor']
						}
						
					} else {
						if (DOMAIN.hasProperty('environment')){
							load 'Name' with DOMAIN['Environment']
						}
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment', 'assetName'] as Set
					assertWith(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
						appVendor == 'Vendor'
					}
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
					}
				}
			}
	}

	@See('TM-14454')
	void 'test can throws an Exception if DOMAIN variable is invoked incorrectly with a not previously defined local variable'() {

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
						if (DOMAIN.hasProperty('appVendor')){ 
							load 'Name' with DOMAIN.appVendor
						}
						
					} else {
						if (DOMAIN.hasProperty(environment)){ // Not previously defined local variable!
							load 'Name' with DOMAIN.environment
						}
							
					}
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('environment').message

		when: 'The ETL script is evaluated'

			applicationDataSet = new CSVDataset(createCSVFIle("""
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""")
			)

			etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					applicationDataSet,
					new DebugConsole(buffer: new StringBuilder()),
					validator)

			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'appVendor'
					extract 'location' load 'environment'

					if ( CE == 'Microsoft'){
						if (DOMAIN.hasProperty('appVendor')){ 
							load 'Name' with DOMAIN.appVendor
						}
						
					} else {
						if (DOMAIN.hasProperty('environment')){ 
							load 'Name' with DOMAIN[environment] // Not previously defined local variable!
						}
					}
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('environment').message
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment', 'assetName'] as Set
					assertWith(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
						appVendor == 'Vendor'
					}
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == '152254'
							originalValue == '152254'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
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
						def myLocal = CE

						if ( myLocal == 'Microsoft'){
							load 'appVendor' with myLocal
						} else {
							load 'environment' with myLocal
						}
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(fieldLabelMap) {
						appVendor == 'Vendor'
						environment == 'Environment'
					}
					data.size() == 2
					assertWith(fieldLabelMap) {
						appVendor: 'Vendor'
						environment: 'Environment'
					}
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.environment) {
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(fieldLabelMap) {
						description == 'Description'
					}
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							originalValue == "Microsoft"
							value == "Microsoft"
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.description) {
							originalValue == "Microsoft"
							value == "Microsoft"
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
							originalValue == "Mozilla"
							value == "Mozilla"
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.description) {
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(fieldLabelMap) {
						id == 'Id'
						appVendor == 'Vendor'
					}
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							originalValue == "Microsoft"
							value == "Microsoft"
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
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
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					assertWith(fieldLabelMap) {
						assetName == 'Name'
						manufacturer == 'Manufacturer'
						model == 'Model'
					}
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							originalValue == "xraysrv01"
							value == "xraysrv01"
						}
						assertWith(fields.manufacturer) {
							originalValue == "Dell"
							value == "Dell"
						}
						assertWith(fields.model) {
							originalValue == "PE2950"
							value == "PE2950"
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							originalValue == "oradbsrv02"
							value == "oradbsrv02"
						}
						assertWith(fields.manufacturer) {
							originalValue == "HP"
							value == "HP"
						}
						assertWith(fields.model) {
							originalValue == "DL8150"
							value == "DL8150"
						}
					}

					assertWith(data[2]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 3
						assertWith(fields.assetName) {
							originalValue == "oradbsrv03"
							value == "oradbsrv03"
						}
						assertWith(fields.manufacturer) {
							originalValue == "HP"
							value == "HP"
						}
						assertWith(fields.model) {
							originalValue == "DL8155"
							value == "DL8155"
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 2
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					assertWith(fieldLabelMap) {
						appVendor == 'Vendor'
					}
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.environment) {
							originalValue == 'Production'
							value == 'Production'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							originalValue == 'Microsoft'
							value == 'Microsoft'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.environment) {
							originalValue == 'Production'
							value == 'Production'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							originalValue == 'Microsoft'
							value == 'Microsoft'
						}
					}
				}


				assertWith(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.description) {
							originalValue == 'Development'
							value == 'Development'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							originalValue == '152254'
							value == '152254'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.description) {
							originalValue == 'Development'
							value == 'Development'
						}
					}
				}
			}
	}

	@ConfineMetaClassChanges([Room])
	void 'test can load Room domain instances'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				roomId,Name,Location,Depth,Width,Source,Address,City,Country,StateProv,Postal Code
				673,DC1,ACME Data Center,26.00,40.00,Source,112 Main St ,Cumberland,,IA,50843
				674,ACME Room 1,New Colo Provider,4.00,42.00,Target,411 Elm St,Dallas,,TX,75202""".stripIndent())

		and:
			List<Room> rooms = buildRooms([
				[673, GMDEMO, 'DC1', 'ACME Data Center', 26, 40, '112 Main St', 'Cumberland', 'IA', '50843'],
				[674, GMDEMO, 'ACME Room 1', 'New Colo Provider', 40, 42, '411 Elm St', 'Dallas', 'TX', '75202']
			])

		and:
			mockDomain(Room)
			Room.metaClass.staticexecuteQuery(_, _) >> { String query, Map args ->
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			fileSystemService.deleteTemporaryFile(fileName)
	}

	@ConfineMetaClassChanges([Room])
	void 'test can load Rack domain instances'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			mockDomain(Room)
			Room.metaClass.static.executeQuery(_, _) >> { String query, Map args ->
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
				fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment'] as Set
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
					}

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.environment) {
							value == 'Production'
							originalValue == 'Production'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.environment) {
							value == 'Development'
							originalValue == 'Development'
						}
					}
				}

			}
	}

	void 'test can throw an exception if an domain is not specified'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				fileSystemService.deleteTemporaryFile(fileName)
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
						set environment with 'Production'
					} else {
						set environment with 'Development'
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment'] as Set

					assertWith(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
					}

					assertWith(data[0].fields.environment) {
						value == 'Production'
						originalValue == 'Production'
					}

					assertWith(data[1].fields.appVendor) {
						value == 'Mozilla'
						originalValue == 'Mozilla'
					}

					assertWith(data[1].fields.environment) {
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
						set environment with 'Production'
					} else {
						set environment with 'Development'
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 2
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor'] as Set
					data.size() == 1
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							value == '152254'
							originalValue == '152254'
						}
					}

					assertWith(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
					}
				}

				assertWith(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['id', 'assetName'] as Set
					data.size() == 1
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							value == '152254'
							originalValue == '152254'
						}

						assertWith(fields.assetName) {
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 2
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor'] as Set
					data.size() == 1
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.id) {
							value == '152254'
							originalValue == '152254'
						}
					}

					assertWith(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
					}
				}

				assertWith(domains[1], DomainResult) {
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
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
					log CE
					if( CE != 'NGM') {
						ignore record
					} else {
						load 'Name' with CE
					}
				}
			""".stripIndent())

		then: 'Results will ignore a row'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
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
				sixRowsDataset,
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
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
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
					set myLocal with 'Custom Name'
					load 'Name' with myLocal
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can set a local variable with a SOURCE.property'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
			""")

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
					set myLocal with SOURCE.'name'
					load 'Name' with myLocal
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can set a local variable with a DOMAIN.property'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
					set myLocal with DOMAIN.environment
					load 'Name' with myLocal
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['environment', 'assetName'] as Set
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.environment) {
							value == 'Server'
							originalValue == 'Server'
						}
						assertWith(fields.assetName) {
							value == 'Server'
							originalValue == 'Server'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.environment) {
							value == 'Blade'
							originalValue == 'Blade'
						}
						assertWith(fields.assetName) {
							value == 'Blade'
							originalValue == 'Blade'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can set a multiple local variables'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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

					extract 'mfg' set myMfg
					myMfg += " (" + extract('type') + ")"
					load 'Manufacturer' with myMfg

					set another with 'xyzzy'
					load 'custom2' with another
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'custom1', 'manufacturer', 'custom2'] as Set
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
						}
						assertWith(fields.custom1) {
							value == 'abc'
							originalValue == 'abc'
						}
						assertWith(fields.manufacturer) {
							value == 'Dell (Server)'
							originalValue == 'Dell (Server)'
						}
						assertWith(fields.custom2) {
							value == 'xyzzy'
							originalValue == 'xyzzy'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
						}
						assertWith(fields.custom1) {
							value == 'abc'
							originalValue == 'abc'
						}
						assertWith(fields.manufacturer) {
							value == 'HP (Blade)'
							originalValue == 'HP (Blade)'
						}
						assertWith(fields.custom2) {
							value == 'xyzzy'
							originalValue == 'xyzzy'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test NOW variable'() {

		given:
			String iso8601 = TimeUtil.FORMAT_DATE_TIME_ISO8601.replace("'", '')
			def dataSetCSV = """
				name
				fubar
			""".stripIndent().trim()
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet(dataSetCSV)

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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						assertWith(fields, Map) {

							assertWith(assetName) {
								value == 'fubar'
								originalValue == 'fubar'
							}

							assertWith(custom1) {
								Date date = DateUtils.parseDate(value, TimeUtil.FORMAT_DATE_TIME_ISO8601_2)
								//assert date != null: "$value is not parseable using ISO8601 format (${TimeUtil.FORMAT_DATE_TIME_ISO8601})"
							}
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-9995')
	void 'test can declare local variables outside the iteration command'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				set myLocal with 'Custom Name'
				iterate {
					domain Device
					load 'Name' with myLocal
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							value == 'Custom Name'
							originalValue == 'Custom Name'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can not throw an Exception if variable names is used incorrectly in second time'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				set myLocal with 'Custom Name'
				iterate {
					domain Device
					set myLocal with 'Another value'
				}
			""".stripIndent())

		then: 'no exception is thrown'
			noExceptionThrown()

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-10678')
	void 'test can load a new row using twice the domain command'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
							init == null
						}
						assertWith(fields.model) {
							value == 'PE2950'
							originalValue == 'PE2950'
							init == null
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11037')
	void 'test can load current element with a blank content from an ETL Script'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == ''
							originalValue == ''
							init == null
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11037')
	void 'test can init current element with a blank content from an ETL Script'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					assertWith(data[0]) {
						rowNum == 1
						assertWith(fields.assetName) {
							value == null
							originalValue == null
							init == ''
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11037')
	void 'test can init and load with a blank content from an ETL Script'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == null
							originalValue == null
							init == ''
						}
						assertWith(fields.manufacturer) {
							value == ''
							originalValue == ''
							init == null
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 2

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.environment) {
							originalValue == 'Microsoft,ACME Data Center'
							value == 'Microsoft,ACME Data Center'
						}
					}
					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.environment) {
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
						set env with 'Prod'
						extract 'vendor name' transform with append('-', env) load 'environment'
					}
				""".stripIndent())

		then: 'Results should contain Application vendor name and location domain fields concatenated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 2

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.environment) {
							originalValue == 'Microsoft'
							value == 'Microsoft-Prod'
						}
					}
					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.environment) {
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
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
						enable lookUp
						read labels
						iterate {
							domain Device
							extract 'ip' transform with lowercase() set ip
							extract 'srv' set srv

							lookup 'assetName' with srv
							if ( LOOKUP.notFound() ) {
								// Set the server name first time seen
								load 'Name' with srv
							}
							load 'IP Address' transform with append(', ', ip)
						}
					""".stripIndent())

		then: 'Results should contain Application vendor name and location domain fields concatenated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 3

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							originalValue == 'x'
							value == 'x'
						}
						assertWith(fields.ipAddress) {
							originalValue == '1.2.3.4, 1.3.5.1'
							value == '1.2.3.4, 1.3.5.1'
						}
					}
					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							originalValue == 'y'
							value == 'y'
						}
						assertWith(fields.ipAddress) {
							originalValue == '4.5.4.2'
							value == '4.5.4.2'
						}
					}
					assertWith(data[2]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 4
						assertWith(fields.assetName) {
							originalValue == 'z'
							value == 'z'
						}
						assertWith(fields.ipAddress) {
							originalValue == '3.3.3.3'
							value == '3.3.3.3'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
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
							set env with 'Prod'

							load 'Name' with append('-', env)
						}
					""".stripIndent())

		then: 'exception should be thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Unrecognized command append with args [-, Prod]'
	}

	@See('TM-11530')
	void 'test can use set command with local variables'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
					extract 'FirstName' set firstName
					extract 'LastName' set lastName
					
					set fullName with firstName + ' ' + lastName
					
					load 'description' with fullName
				}
				""".stripIndent())

		then: 'Results should contain correctly set full name'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 1

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.description) {
							originalValue == 'Tony Baker'
							value == 'Tony Baker'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11590')
	void 'test can use use when populated qualifier command'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				   extract 'description' set desc
				   load 'description' with desc when populated
				   extract 'nothingThere' load 'custom2' when populated
				   extract 'retire date' transform with toDate() load 'Retire Date' when populated
				}
				""".stripIndent())

		then: 'Results should contain correctly set full name'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields) {
							assertWith(it.assetName) {
								originalValue == 'xraysrv01'
								value == 'xraysrv01'
							}
							assertWith(it.custom1) {
								originalValue == '2'
								value == '2'
							}

							assertWith(it.retireDate) {
								value == new Date(2018 - 1900, 6 - 1, 25)
								init == null
								errors == []
							}
							it.description == null
							it.custom2 == null
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields) {
							assertWith(it.assetName) {
								originalValue == 'zuludb01'
								value == 'zuludb01'
							}
							it.custom1 == null

							assertWith(it.description) {
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
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11590')
	void 'test can use use when populated qualifier with a closure definition'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2

					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields) {
							assertWith(it.assetName) {
								originalValue == 'xraysrv01'
								value == 'xraysrv01'
							}
							assertWith(it.custom1) {
								originalValue == '100'
								value == 100
							}
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields) {
							assertWith(it.assetName) {
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
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11590')
	void 'test can throw an Exception when populated qualifier is configured incorrectly'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.incorrectWhenCommandStructure().message} at line 5"
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-15257')
	void 'test can enable console and log SOURCE variable'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,XRay SRV
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
				   console on
				   log SOURCE
				   console off
				}
				""".stripIndent())


		then: 'A console content could be recovered after processing an ETL Script'
			etlProcessor.debugConsole.buffer.toString().contains('row=[name:xraysrv01, cpu:100, description:XRay SRV]')

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-15257')
	void 'test can enable console and log DOMAIN variable'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,XRay SRV
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
				   console on
				   log DOMAIN
				   console off
				}
				""".stripIndent())


		then: 'A console content could be recovered after processing an ETL Script'
			etlProcessor.debugConsole.buffer.toString().contains('fields=[[assetName:xraysrv01]]')

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-15154')
	void 'test can define variables without ending in Var'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet('''
					name
					ACME
			'''.stripIndent())
		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
					read labels
					domain Application
					iterate {
						extract 'name' load 'assetName'
						set appVersion with '1.0.0'  
					}
			'''.stripIndent())

		then: 'Results should contain Device Name assigment'
			assertWith(etlProcessor.finalResult()) {
				assertWith(domains[0], DomainResult) {
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							originalValue == 'ACME'
							value == 'ACME'
						}
					}
					domain == ETLDomain.Application.name()
					data.size() == 1
				}
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-15154')
	void 'test can define variables without ending in Var for Element'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet('''
					name
					ACME
			'''.stripIndent())
		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
					read labels
					domain Application
					iterate {
						extract 'name' set appVersion
						load 'assetName' with appVersion
						load 'appVersion' with '1.0.0'  
					}
			'''.stripIndent())

		then: 'Results should contain Device Name assigment'
			assertWith(etlProcessor.finalResult()) {
				assertWith(domains[0], DomainResult) {
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							originalValue == 'ACME'
							value == 'ACME'
						}
					}
					domain == ETLDomain.Application.name()
					data.size() == 1
				}
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-15154')
	void 'test can throw an Exception if variable names does exists in several scenarios'() {

		given:
			String fileName
			ETLDataset dataSet

			(fileName, dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain variable
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('variable').message

		when: 'The ETL script is evaluated'

			(fileName, dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
			""".stripIndent())

			etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					GroovyMock(DebugConsole),
					validator)

			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'name' transform with append('::', variable) 
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('variable').message

		when: 'The ETL script is evaluated'

			(fileName, dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
			""".stripIndent())

			etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					GroovyMock(DebugConsole),
					validator)

			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'name' transform with concat('-', variable) 
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('variable').message

		when: 'The ETL script is evaluated'

			(fileName, dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
			""".stripIndent())

			etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					GroovyMock(DebugConsole),
					validator)

			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'name' load variable
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('variable').message

		when: 'The ETL script is evaluated'

			(fileName, dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
			""".stripIndent())

			etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					GroovyMock(DebugConsole),
					validator)

			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'name' prepend variable
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('variable').message

		when: 'The ETL script is evaluated'

			(fileName, dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				zuludb01,HP,BL380,Blade
			""".stripIndent())

			etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					GroovyMock(DebugConsole),
					validator)

			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Device
					extract 'name' load 'assetName'
					find Device by 'assetName', 'description' with variable, 'DESC' into 'assetName'
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			e = thrown ETLProcessorException
			e.message == ETLProcessorException.missingPropertyException('variable').message

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-16272')
	void 'test can use fieldSpec command in an ETL scripts to retrieve asset field specs for specific asset domain'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				domain Device
				iterate {
					fieldSpec Device each {
						
						if (it.name == 'assetName') {
							extract 'name' load it.label
						}
						if (it.name == 'custom1'){
							extract 'mfg' load it.label
						}
					}
					
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName'] as Set
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
						}
						assertWith(fields.custom1) {
							value == 'Dell'
							originalValue == 'Dell'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
						}
						assertWith(fields.custom1) {
							value == 'HP'
							originalValue == 'HP'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-16272')
	void 'test can throw an Exception if use fieldSpec command with a non asset domain'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				domain Device
				iterate {
					fieldSpec Person each {
						if (it.name == 'assetName') {
							extract 'name' load it.label
						}
					}
				}
			""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.domainWithoutFieldSpec(ETLDomain.Person).message

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-16272')
	void 'test can verify if SOURCE contains a particular property'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				Application Id,Vendor Name,Technology,Location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'Vendor Name' load 'appVendor'
					extract 'Location' load 'environment'

					if ( CE == 'Microsoft'){
						if (SOURCE.containsKey('Vendor Name')){
							load 'Name' with SOURCE.'Vendor Name'
						} 
						
					} else {
						if (SOURCE.containsKey('Application Id')){
							load 'Name' with SOURCE.'Application Id'
						}
					}
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'environment', 'assetName'] as Set
					assertWith(fieldLabelMap) {
						assetName == 'Name'
						environment == 'Environment'
						appVendor == 'Vendor'
					}
					data.size() == 2
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == '152254'
							originalValue == '152254'
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
						}
						assertWith(fields.environment) {
							value == 'ACME Data Center'
							originalValue == 'ACME Data Center'
						}
						assertWith(fields.assetName) {
							value == '152255'
							originalValue == '152255'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}
}
