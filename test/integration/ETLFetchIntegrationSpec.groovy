import com.tds.asset.AssetEntity
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.service.FileSystemService
import spock.lang.IgnoreRest
import test.helper.AssetEntityTestHelper

class ETLFetchIntegrationSpec extends ETLBaseIntegrationSpec {

	FileSystemService fileSystemService
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()
	AssetEntityTestHelper assetEntityTestHelper = new AssetEntityTestHelper()
	test.helper.MoveBundleTestHelper moveBundleTestHelper = new test.helper.MoveBundleTestHelper()
	test.helper.PersonTestHelper personTestHelper = new test.helper.PersonTestHelper()


	Project project
	MoveBundle moveBundle
	DebugConsole debugConsole
	ETLFieldsValidator validator

	def setup() {
		project = projectTestHelper.createProject()
		moveBundle = moveBundleTestHelper.createBundle(project, null)
		validator = createDomainClassFieldsValidator()
		debugConsole = Mock()
	}

	void 'test can fetch results by ID'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'
			device.save(failOnError: true, flush: true)

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
asset id,asset name
12323434,A1 PDU1 A
${device.id},${device.assetName}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels

				domain Device
				iterate {
					
					extract 'asset id' load 'id'
					fetch 'id' set deviceVar
					if(ROW == 1){
						assert deviceVar == null
					}
					if(ROW == 2){
						assert deviceVar.id == ${device.id}
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can fetch results without using id'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'
			device.save(failOnError: true, flush: true)

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
asset id,asset name,environment
12323434,A1 PDU1 A,Development
${device.id},${device.assetName},${device.environment}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels

				domain Device
				iterate {
					
					extract 'asset name' load 'Name' set nameVar
					extract 'environment' load 'Environment' set envVar
					find Device by 'Name' eq nameVar and 'Environment' eq envVar into 'id'
					fetch 'id' set deviceVar
					if(ROW == 1){
						assert deviceVar == null
					}
					if(ROW == 2){
						assert deviceVar.id == ${device.id}
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can fetch results by ID requesting particular fields'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'
			device.save(failOnError: true, flush: true)

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
asset id,asset name
12323434,A1 PDU1 A
${device.id},${device.assetName}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels

				domain Device
				iterate {
					
					extract 'asset id' load 'id'
					fetch 'id' fields 'Environment', 'OS', 'IP Address' set deviceVar
					if(ROW == 1){
						assert deviceVar == null
					}
					if(ROW == 2){
						assert deviceVar.id == ${device.id}
						assert deviceVar.assetName == null
						assert deviceVar.'Environment' == 'Production'
						assert deviceVar.'OS' == 'Microsoft'
						assert deviceVar.'IP Address' == '192.168.1.10'
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

/*	void 'test can fetch Person domain results'() {

		given:
			Person person = personTestHelper.createPerson()

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
sme
John Doe
${person.toString()}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Application
				
				iterate {
					extract 'sme' load 'sme'
					fetch 'sme' set personVar
					if(ROW == 1){
						assert personVar == null
					}	
					if(ROW == 2){
						assert personVar.id == ${person.id}
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}*/

	void 'test can fetch results first by manufacturer and after that results with model'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'

			Manufacturer manufacturer = new Manufacturer(name : "Dell 12345").save(failOnError: true, flush: true)

			Model model = new Model(
				modelName      : "PowerEdge 1950",
				manufacturer   : manufacturer,
				assetType      : "Server",
				poweruse       : 1200,
				connectorLabel : "PE5",
				type           : "Power",
				connectorPosX  : 250,
				connectorPosY  : 90
			).save(failOnError: true, flush: true)

			device.model = model
			device.manufacturer = manufacturer
			device.save(failOnError: true, flush: true)


			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
mfg,model
Dell 11111,PPPPAAAAA
${manufacturer.name},${model.modelName}
""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				
				iterate {

					extract 'model' load 'model'
					fetch 'Model' set firstTryModelVar
					assert firstTryModelVar == null

					extract 'mfg' load 'manufacturer'
					fetch 'Model' set modelVar
					if(ROW == 1){
						assert modelVar == null
					}
					if(ROW == 2){
						assert modelVar.id == ${model.id}
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can fetch results by find/elseFind'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'

			Manufacturer manufacturer = new Manufacturer(name : "Dell 12345").save(failOnError: true, flush: true)

			Model model = new Model(
				modelName      : "PowerEdge 1950",
				manufacturer   : manufacturer,
				assetType      : "Server",
				poweruse       : 1200,
				connectorLabel : "PE5",
				type           : "Power",
				connectorPosX  : 250,
				connectorPosY  : 90
			).save(failOnError: true, flush: true)

			device.model = model
			device.manufacturer = manufacturer
			device.save(failOnError: true, flush: true)

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
model,manufacturer
Dell 11111,PPAA
${model.modelName},${manufacturer.name}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				
				iterate {

					extract 'manufacturer' set manufacturerNameVar
					extract 'model' set modelNameVar
					find Device by 'manufacturer', 'model' with manufacturerNameVar, modelNameVar into 'model'
					fetch 'Model' set deviceVar

					if(ROW == 1){
						assert deviceVar == null
					} 	

					if(ROW == 2){
						assert deviceVar.id == ${device.id}
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can fetch model based on manufacturer and model'() {

		given:
			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'

			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(failOnError: true, flush: true)

			Model model = new Model(
				modelName: "PowerEdge 1950",
				manufacturer: manufacturer,
				assetType: "Server",
				poweruse: 1200,
				connectorLabel: "PE5",
				type: "Power",
				connectorPosX: 250,
				connectorPosY: 90
			).save(failOnError: true, flush: true)

			device.model = model
			device.save(failOnError: true, flush: true)


			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
asset id,asset name,model,manufacturer
12323434,A1 PDU1 A,Dell 11111,
${device.id},${device.assetName},${device.model.modelName},${device.model.manufacturer.name}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				
				iterate {
					extract 'manufacturer' load 'manufacturer' set manufacturerNameVar
					extract 'model' load 'model' set modelNameVar
					fetch 'Model' set modelVar

					if(ROW == 1){
						assert modelVar == null
					}		
					if(ROW == 2){
						assert modelVar.id == ${model.id}
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can fetch Asset Entity results by Alternate Key'() {

		given: 'a defined manufacturer assigned to an Device domain'
			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(failOnError: true, flush: true)

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'
			device.manufacturer = manufacturer
			device.save(failOnError: true, flush: true)

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
asset id,asset name,mfg
12323434,A1 PDU1 A,
${device.id},${device.assetName},${manufacturer.name}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script with fetch command is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					load 'manufacturer' with SOURCE.'mfg'
					fetch 'manufacturer' set mfgVar
					
					if(ROW == 1){
						assert mfgVar == null
					}
					if(ROW == 2){
						assert mfgVar.id == ${manufacturer.id}
					}

				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}


	/*void 'test can fetch results by Alternate Key using a non AssetEntity'() {

		given: 'a defined Model and Manufacturer assigned to an Device domain'
			Manufacturer manufacturer = new Manufacturer(name: "Dell 12345").save(failOnError: true, flush: true)

			AssetEntity device = assetEntityTestHelper.createAssetEntity(AssetClass.DEVICE, project, moveBundle)
			device.assetName = 'AGPM'
			device.environment = 'Production'
			device.os = 'Microsoft'
			device.ipAddress = '192.168.1.10'

			Model model = new Model(
				modelName: "PowerEdge 1950",
				manufacturer: manufacturer,
				assetType: "Server",
				poweruse: 1200,
				connectorLabel: "PE5",
				type: "Power",
				connectorPosX: 250,
				connectorPosY: 90
			).save(failOnError: true, flush: true)

			device.model = model
			device.manufacturer = manufacturer
			device.save(failOnError: true, flush: true)

			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
asset id,asset name,mfg
12323434,A1 PDU1 A,Dell2332
${device.id},${device.assetName},${manufacturer.name}""".stripIndent())

			ETLProcessor etlProcessor = new ETLProcessor(
				project,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script with fetch command is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					// Load the manufacturer with the alternate key
					extract 'mfg' load 'manufacturer' set mfgNameVar
					fetch 'Model' set modelVar
					
					if(ROW == 1){
						assert modelVar == null
					}

					if(ROW == 2){
						assert modelVar.id == ${model.id} 
					}
				}
			""".stripIndent())

		then: 'Results should contain results'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}
	*/
}