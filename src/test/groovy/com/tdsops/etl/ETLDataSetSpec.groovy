package com.tdsops.etl

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
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
import net.transitionmanager.imports.DataScript
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.Shared

@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLDataSetSpec extends ETLBaseSpec {

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
		jsonConnection = new JSONConnection(config: 'json', driver:TDSJSONDriver)
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
		/*
		jsonDataSet.getDataSet().field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "STRING", isNull: false, isKey: true)
		jsonDataSet.getDataSet().field << new getl.data.Field(name: 'model name', alias: 'MODEL NAME', type: "STRING", isNull: false)
		jsonDataSet.getDataSet().field << new getl.data.Field(name: 'manufacturer name', alias: 'MANUFACTURER NAME', type: "STRING", isNull: false)
		*/

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

		debugConsole = new DebugConsole(buffer: new StringBuilder())

		applicationFieldsValidator = new ETLFieldsValidator()
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

		validator = createDomainClassFieldsValidator()
	}

	void 'test can read labels from dataSource and create a map of columns'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				sixRowsDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

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

	void 'test can iterate over all data source rows from a json dataset'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				jsonDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							log SOURCE.'device id'
						}
					""".stripIndent())

		then: 'The current row index is the last row in data source'
			etlProcessor.currentRowIndex == jsonDataSet.rowsSize()

			// Auto detecting JSON headers.
		and : 'A column map is created in alphanumeric order (Json Map)'
			etlProcessor.column('device id').index == 0
			etlProcessor.column(0).label == 'device id'

		and:
			etlProcessor.column('manufacturer name').index == 1
			etlProcessor.column(1).label == 'manufacturer name'

		and:
			etlProcessor.column('model name').index == 2
			etlProcessor.column(2).label == 'model name'
	}
}
