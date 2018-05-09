package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
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

/**
 * Test about ETL Current Element (CE):
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLCurrentElementSpec extends ETLBaseSpec {

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
			updater(['application id': 152254, 'vendor name': 'Microsoft', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': 152255, 'vendor name': 'Mozilla', 'technology': 'NGM', 'location': 'ACME Data Center'])
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

	void 'test CE should appear upon extract command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					extract 'name'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == 'xraysrv01'
				value == 'xraysrv01'
				init == null
				fieldDefinition == null
			}

		and: 'Results contains the following values'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Device.name()
				fieldNames == [] as Set
				with(data[0]){
					op == 'I'
					errorCount == 0
					warn == false
					duplicate == false
					errors == []
					fields == [:]
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test load command CE contains a field definition'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					extract 'name' load 'assetName'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == 'xraysrv01'
				value == 'xraysrv01'
				init == null
				with(fieldDefinition){
					name == 'assetName'
					label == 'Name'
				}
			}

		and: 'Results contains the following values'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Device.name()
				fieldNames == ['assetName'] as Set
				with(data[0]){
					op == 'I'
					errorCount == 0
					warn == false
					duplicate == false
					errors == []
					rowNum == 1
					with(fields.assetName){
						originalValue == 'xraysrv01'
						value == 'xraysrv01'
						errors == []
						warn == false
						with(find){
							query == []
						}
					}
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can define CE using initialize command without extracting previously'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					init 'assetName' with 'Initial Name'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == null
				value == null
				init == 'Initial Name'
				with(fieldDefinition){
					name == 'assetName'
					label == 'Name'
				}
			}

		and: 'Results contains the following values'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Device.name()
				fieldNames == ['assetName'] as Set
				with(data[0]){
					op == 'I'
					errorCount == 0
					warn == false
					duplicate == false
					errors == []
					rowNum == 1
					with(fields.assetName){
						originalValue == null
						value == null
						init == 'Initial Name'
						errors == []
						warn == false
						with(find){
							query == []
						}
					}
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can define CE using find command without extracting previously'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				""".stripIndent())

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				[]
			}

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
				domain Device
				iterate {
					find Device by 'model' with SOURCE.'model' into 'id'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == null
				value == null
				init == null
				with(fieldDefinition){
					name == 'id'
					label == 'Id'
				}
			}

		and: 'Results contains the following values'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Device.name()
				fieldNames == ['id'] as Set
				with(data[0]){
					op == 'I'
					errorCount == 0
					warn == false
					duplicate == false
					errors == []
					rowNum == 1
					with(fields.id){
						originalValue == null
						value == null
						errors == []
						warn == false
						with(find){
							query.size() == 1
							with(query[0]){
								domain == ETLDomain.Device.name()
								with(kv){
									model: 'PE2950'
								}
							}

						}
					}
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can define CE using load/with command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
				domain Device
				iterate {
					load 'assetName' with SOURCE.'name'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == 'xraysrv01'
				value == 'xraysrv01'
				init == null
				with(fieldDefinition){
					name == 'assetName'
					label == 'Name'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can change CE after every extract command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					extract 'name' load 'assetName'
					extract 'mfg' load 'manufacturer'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == 'Dell'
				value == 'Dell'
				init == null
				with(fieldDefinition){
					name == 'manufacturer'
					label == 'Manufacturer'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can use same CE with init and extract command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					extract 'name' load 'Name'
					assert CE.value == 'xraysrv01'
					assert CE.originalValue == 'xraysrv01'
					
					initialize 'Name' with 'Unknown'
					assert CE.value == 'xraysrv01'
					assert CE.originalValue == 'xraysrv01'
					assert CE.init == 'Unknown'
				}
			""".stripIndent())

		then: 'Results should contain values from the local variable'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Device.name()
				fieldNames == ['assetName'] as Set
				data.size() == 1
				with(data[0]) {
					rowNum == 1
					with(fields) {
						with(assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
						}

					}
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can define CE as null at the beginning of every iteration'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
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
					assert null == CE
					extract 'name' load 'assetName'
					assert null != CE
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == 'xraysrv02'
				value == 'xraysrv02'
				init == null
				with(fieldDefinition){
					name == 'assetName'
					label == 'Name'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can define CE after using domain command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					assert null == CE
					extract 'name' load 'assetName'
					assert null != CE
					domain Device
					assert null == CE
					extract 'name' load 'assetName'
					assert null != CE
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement){
				originalValue == 'xraysrv01'
				value == 'xraysrv01'
				init == null
				with(fieldDefinition){
					name == 'assetName'
					label == 'Name'
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}
}
