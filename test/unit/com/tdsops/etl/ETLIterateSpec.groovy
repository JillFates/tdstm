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
import org.junit.Ignore
import spock.lang.Shared

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>iterate</b></li>
 *     <li><b>iterate with ranges</b></li>
 *     <li><b>skip</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLIterateSpec extends ETLBaseSpec {

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


	@Ignore
	//TODO: dcorrea. Since I added support for Excel Driver,
	// this step doesn't work with all the Dataset.
	// We need a new ticket to improve this validation or directly removed it if it is not necessary.
//	void 'test can throw an Exception if the skip parameter is bigger that rows count'() {
//
//		given:
//			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
//				GroovyMock(ETLFieldsValidator))
//
//		when: 'The ETL script is evaluated'
//			new GroovyShell(this.class.classLoader, etlProcessor.binding)
//				.evaluate("skip 20", ETLProcessor.class.name)
//
//		then: 'An ETLProcessorException is thrown'
//			ETLProcessorException e = thrown ETLProcessorException
//			e.message == "Incorrect skip step: 20"
//	}

	/**
	 * The iterate command will create a loop that iterate over the remaining rows in the data source
	 */
	void 'test can iterate over all data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							log it
						}
					""".stripIndent())

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == sixRowsDataSet.rowsSize()
	}

	void 'test can iterate over a range of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						from 1 to 3 iterate {
							log it
						}
					""".stripIndent())

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == 3
	}

	void 'test can throw an exception when iterates over an invalid range of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script with iterate staring in zero is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						from 0 to 3 iterate {
							println it
						}
					""".stripIndent())

		then: 'An ETLProcessorException is thrown with a message for the invalid from parameter'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'


		when: 'The ETL script with iterate with a bigger to parameter is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						from 1 to 8 iterate {
							println it
						}
					""".stripIndent())

		then: 'An ETLProcessorException is thrown with a message for the invalid from parameter'
			e = thrown ETLProcessorException
			e.message == "Invalid to parameter = 9"

	}

	void 'test can iterate over a list of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						from 1, 2, 3 iterate {
							log it
						}
					""".stripIndent())

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == 3
	}

	void 'test can throw an exception with a message when iterates over a invalid list of data source rows'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), sixRowsDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						from 0, 2, 4 iterate {
							println it
						}
					""".stripIndent())

		then: 'An ETLProcessorException is thrown with a message for the invalid from parameter'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Initial position starts with 1'
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
			etlProcessor.evaluate("""
					read labels
					iterate {
						domain Application
						extract 'application id' load 'id'
						extract 'vendor name' load 'appVendor'
									
						domain Device
						extract 'device id' load 'id'
						extract 'model name' load 'Name'
					}""".stripIndent())

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

				with(data[0].fields.assetName) {
					value == 'SRW24G1'
					originalValue == 'SRW24G1'
				}

				with(data[1].fields.id) {
					value == '1523'
					originalValue == '1523'
				}

				with(data[1].fields.assetName) {
					value == 'ZPHA MODULE'
					originalValue == 'ZPHA MODULE'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can load fields with more than one iteration'() {

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
			etlProcessor.evaluate("""
					read labels
					iterate {
						domain Application
						extract 'application id' load 'id'
						extract 'vendor name' load 'appVendor'
					}

					iterate {
						domain Device
						extract 'device id' load 'id'
						extract 'model name' load 'Name'
					}
				""".stripIndent())

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

				with(data[0].fields.assetName) {
					value == 'SRW24G1'
					originalValue == 'SRW24G1'
				}

				with(data[1].fields.id) {
					value == '1523'
					originalValue == '1523'
				}

				with(data[1].fields.assetName) {
					value == 'ZPHA MODULE'
					originalValue == 'ZPHA MODULE'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can load fields combining iterators'() {

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
			etlProcessor.evaluate("""
					read labels
					iterate {
						domain Application
						extract 'application id' load 'id'
						extract 'vendor name' load 'appVendor'
					}

					from 1 to 2 iterate {
						domain Device
						extract 'device id' load 'id'
						extract 'model name' load 'assetName'
					}
				""".stripIndent())

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

				with(data[0].fields.assetName) {
					value == 'SRW24G1'
					originalValue == 'SRW24G1'
				}

				with(data[1].fields.id) {
					value == '1523'
					originalValue == '1523'
				}

				with(data[1].fields.assetName) {
					value == 'ZPHA MODULE'
					originalValue == 'ZPHA MODULE'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

}
