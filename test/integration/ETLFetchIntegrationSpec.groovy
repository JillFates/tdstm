import com.tds.asset.AssetEntity
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.AssetClass
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
				iterate {
					domain Device
					extract 'asset id' load 'id'
					fetch 'id' fields 'Environment', 'OS', 'IP Address' set deviceVar
					
					assert deviceVar.'Environment' == 'Production'
					assert deviceVar.'OS' == 'Microsoft'
					assert deviceVar.'IP Address' == '192.168.1.10'
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

	@IgnoreRest
	void 'test can fetch results by find/elseFind'() {

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
				iterate {
					domain Device
					extract 'asset id' load 'id'
					find Device by 'assetName' with SOURCE.'asset name' into 'id'
					fetch 'id' fields 'Environment', 'OS', 'IP Address' set deviceVar
					
					assert deviceVar.'Environment' == 'Production'
					assert deviceVar.'OS' == 'Microsoft'
					assert deviceVar.'IP Address' == '192.168.1.10'
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

	void 'test can fetch results by Alternate Key'() {

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
				iterate {
					domain Device
					extract 'asset id' load 'id'
					find Device by 'assetName' with SOURCE.'asset name' into 'id'
					fetch 'id' fields 'Environment', 'OS', 'IP Address' set deviceVar
					
					assert deviceVar.'Environment' == 'Production'
					assert deviceVar.'OS' == 'Microsoft'
					assert deviceVar.'IP Address' == '192.168.1.10'
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
}