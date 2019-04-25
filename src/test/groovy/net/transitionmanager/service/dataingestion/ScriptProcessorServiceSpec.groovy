import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.Database
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FindOperator
import com.tdsops.etl.ProgressCallback
import com.tdsops.etl.QueryResult
import com.tdsops.etl.TDSExcelDriver
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.WorkbookUtil
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import net.transitionmanager.imports.DataScript
import net.transitionmanager.project.Project
import net.transitionmanager.common.Setting
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.SettingService
import net.transitionmanager.imports.ScriptProcessorService
import net.transitionmanager.security.SecurityService
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFRow
import spock.lang.Specification

import static com.tdsops.etl.ProgressCallback.ProgressStatus.COMPLETED
import static com.tdsops.etl.ProgressCallback.ProgressStatus.RUNNING

class ScriptProcessorServiceSpec extends Specification implements ServiceUnitTest<ScriptProcessorService>, DataTest, GrailsWebUnitTest {

	String sixRowsDataSetFileName
	String applicationDataSetFileName
	FileSystemService fileSystemService

	static doWithConfig(c) {
		c.graph.tmpDir = '/tmp/'
	}

	void setupSpec(){
		mockDomains DataScript, Project, Database, AssetEntity, Setting, Application, Database, AssetOptions
	}

	def setup() {
		defineBeans {

			coreService(CoreService) {
				grailsApplication = ref('grailsApplication')
			}
			securityService(SecurityService) {
				grailsApplication = ref('grailsApplication')
			}
			fileSystemService(FileSystemService) {
				coreService = ref('coreService')
				securityService = ref('securityService')
				transactionManager = ref('transactionManager')
			}
			settingService(SettingService)
		}

		fileSystemService = grailsApplication.mainContext.getBean(FileSystemService)

		def (String fileName, OutputStream sixRowsDataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
		sixRowsDataSetFileName = fileName
		sixRowsDataSetOS << 'device id,model name,manufacturer name\n'
		sixRowsDataSetOS << '152251,SRW24G1,LINKSYS\n'
		sixRowsDataSetOS << '152252,SRW24G2,LINKSYS\n'
		sixRowsDataSetOS << '152253,SRW24G3,LINKSYS\n'
		sixRowsDataSetOS << '152254,SRW24G4,LINKSYS\n'
		sixRowsDataSetOS << '152255,SRW24G5,LINKSYS\n'
		sixRowsDataSetOS << '152256,ZPHA MODULE,TippingPoint\n'
		sixRowsDataSetOS.close()

		def (String otherFileName, OutputStream applicationDataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
		applicationDataSetFileName = otherFileName
		applicationDataSetOS << 'application id,vendor name,technology,location\n'
		applicationDataSetOS << '152254,Microsoft,(xlsx updated),ACME Data Center\n'
		applicationDataSetOS << '152255,Mozilla,NGM,ACME Data Center\n'
		applicationDataSetOS.close()

		service.customDomainService = Mock(CustomDomainService)
		service.customDomainService.fieldSpecsWithCommon(_) >> { Project project ->
			fieldSpecsMap
		}

		ETLProcessorResult.registerObjectMarshaller()
	}

	def cleanup() {
		fileSystemService.deleteTemporaryFile(sixRowsDataSetFileName)
		fileSystemService.deleteTemporaryFile(applicationDataSetFileName)
	}

	void 'test can check a script content without errors'() {

		given:
			Project project = GroovyMock(Project)
		and:
			String script = """
                console on
                iterate { }
            """.stripIndent()

		when: 'Service executes the script with correct syntax'
			Map<String, ?> result = service.checkSyntax(project, script, fileSystemService.getTemporaryFullFilename(sixRowsDataSetFileName))

		then: 'Service result has validSyntax equals true an a empty list of errors'
			with(result) {
				validSyntax
				errors.isEmpty()
			}
	}

	void 'test can check a script content with errors like an incorrect closure statement'() {

		given:
			Project project = GroovyMock(Project)
		and:
			String script = """
                console on
                iterate {
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.checkSyntax(project, script, fileSystemService.getTemporaryFullFilename(sixRowsDataSetFileName))

		then: 'Service result has validSyntax equals false and a list of errors'
			with(result) {
				!validSyntax
				errors.size() == 1
				with(errors[0]) {
					startLine == 4
					endLine == 4
					startColumn == 1
					endColumn == 2
					fatal
					message == 'unexpected token:  @ line 4, column 1.'
				}
			}
	}

	void 'test can test a script content for Application domain Asset'() {

		given:
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			String script = """
                console on
                read labels
                iterate {
                    domain Application
                    extract 'application id' transform with toLong() load 'id'
                    extract 'vendor name' load 'Vendor'
                    load 'environment' with 'Production'

                    find Application by 'id' with DOMAIN.id into 'id'
                }
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(GMDEMO, script, fileSystemService.getTemporaryFullFilename(applicationDataSetFileName))

		then: 'Service result has validSyntax equals false and a list of errors'

			result.isValid
			result.consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
			result.data.domains.size() == 1

			result.data.domains[0].domain == ETLDomain.Application.name()
			result.data.domains[0].fieldNames == ['id', 'appVendor', 'environment'] as Set


			result.data.domains[0].data[0].fields.id.value == 152254l
			result.data.domains[0].data[0].fields.id.originalValue == '152254'
			result.data.domains[0].data[0].fields.id.find.results == [152254l]
			result.data.domains[0].data[0].fields.id.find.matchOn == 0
			result.data.domains[0].data[0].fields.id.find.query.size() == 1

			assertQueryResult(
				result.data.domains[0].data[0].fields.id.find.query[0],
				ETLDomain.Application,
				[
					['id', FindOperator.eq.name(), 152254l]
				]
			)


			result.data.domains[0].data[1].fields.id.value == 152255l
			result.data.domains[0].data[1].fields.id.originalValue == '152255'
			result.data.domains[0].data[1].fields.id.find.results == [152255l]
			result.data.domains[0].data[1].fields.id.find.matchOn == 0
			result.data.domains[0].data[1].fields.id.find.query.size() == 1

			assertQueryResult(
				result.data.domains[0].data[1].fields.id.find.query[0],
				ETLDomain.Application,
				[
					['id', FindOperator.eq.name(), 152255l]
				]
			)


			result.data.domains[0].data[0].fields.appVendor.value == 'Microsoft'
			result.data.domains[0].data[0].fields.appVendor.originalValue == 'Microsoft'
			!result.data.domains[0].data[0].fields.appVendor.find.query


			result.data.domains[0].data[1].fields.appVendor.value == 'Mozilla'
			result.data.domains[0].data[1].fields.appVendor.originalValue == 'Mozilla'
			!result.data.domains[0].data[1].fields.appVendor.find.query


			result.data.domains[0].data[0].fields.environment.value == 'Production'
			result.data.domains[0].data[0].fields.environment.originalValue == 'Production'
			!result.data.domains[0].data[0].fields.environment.find.query


			result.data.domains[0].data[1].fields.environment.value == 'Production'
			result.data.domains[0].data[1].fields.environment.originalValue == 'Production'
			!result.data.domains[0].data[1].fields.environment.find.query
	}

	void 'test can test a script content can have blank lines at the top without failing the line number in error messages'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', """
application id,vendor name,technology,location
152254,Microsoft,(xlsx updated),ACME Data Center
152255,Mozilla,NGM,ACME Data Center""".stripIndent())

		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				[]
			}

		and:
			String script = """
			
			
			// Make sure to include the above blank lines
			skip 1
			read labels
			domain Application
			iterate {
				extract 'vendor name' set nameVar
				extract 'vendor name' set nameVar
			}
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(
				GMDEMO,
				script,
				fileSystemService.getTemporaryFullFilename(fileName)
			)

		then: 'Service result has validSyntax equals false and a list of errors'
			with(result) {
				!validSyntax
				with(error) {
					startLine == 10
					endLine == 10
					startColumn == null
					endColumn == null
					fatal
					message == "Invalid variable name specified for 'set' command. Variable names must end in 'Var' and can not be reassigned within iterate loop. at line 10"
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can test a script content for Application domain Asset using a excel dataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', """invalid headers, are not part, of the valid data set
application id,vendor name,technology,location
152254,Microsoft,(xlsx updated),ACME Data Center
152255,Mozilla,NGM,ACME Data Center""".stripIndent())

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			String script = """
                console on
                sheet 'Applications'
                skip 1
                read labels
                iterate {
                    domain Application
                    extract 'application id' transform with toLong() load 'id'
                    extract 'vendor name' load 'Vendor'
                    load 'environment' with 'Production'

                    find Application by 'id' with DOMAIN.id into 'id'
                }
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(GMDEMO,
				script,
				fileSystemService.getTemporaryFullFilename(fileName))

		then: 'Service result has validSyntax equals false and a list of errors'

			result.isValid
			result.consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
			result.data.domains.size() == 1

			result.data.domains[0].domain == ETLDomain.Application.name()
			result.data.domains[0].fieldNames == ['id', 'appVendor', 'environment'] as Set


			result.data.domains[0].data[0].fields.id.value == 152254l
			result.data.domains[0].data[0].fields.id.originalValue == '152254'
			result.data.domains[0].data[0].fields.id.find.results == [152254l]
			result.data.domains[0].data[0].fields.id.find.matchOn == 0
			result.data.domains[0].data[0].fields.id.find.query.size() == 1

			assertQueryResult(
				result.data.domains[0].data[0].fields.id.find.query[0],
				ETLDomain.Application,
				[
					['id', FindOperator.eq.name(), 152254l]
				]
			)


			result.data.domains[0].data[1].fields.id.value == 152255l
			result.data.domains[0].data[1].fields.id.originalValue == '152255'
			result.data.domains[0].data[1].fields.id.find.results == [152255l]
			result.data.domains[0].data[1].fields.id.find.matchOn == 0
			result.data.domains[0].data[1].fields.id.find.query.size() == 1

			assertQueryResult(
				result.data.domains[0].data[1].fields.id.find.query[0],
				ETLDomain.Application,
				[
					['id', FindOperator.eq.name(), 152255l]
				]
			)

			result.data.domains[0].data[0].fields.appVendor.value == 'Microsoft'
			result.data.domains[0].data[0].fields.appVendor.originalValue == 'Microsoft'
			!result.data.domains[0].data[0].fields.appVendor.find.query


			result.data.domains[0].data[1].fields.appVendor.value == 'Mozilla'
			result.data.domains[0].data[1].fields.appVendor.originalValue == 'Mozilla'
			!result.data.domains[0].data[1].fields.appVendor.find.query


			result.data.domains[0].data[0].fields.environment.value == 'Production'
			result.data.domains[0].data[0].fields.environment.originalValue == 'Production'
			!result.data.domains[0].data[0].fields.environment.find.query


			result.data.domains[0].data[1].fields.environment.value == 'Production'
			result.data.domains[0].data[1].fields.environment.originalValue == 'Production'
			!result.data.domains[0].data[1].fields.environment.find.query

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can test a script content with an invalid command'() {

		given:

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
			String script = """
                console on
                read labels
                iterate {
                    domain Unknown
                    set environmentVar with 'Production'
                    extract 'location' load 'Vendor'
                }
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(GMDEMO, script, fileSystemService.getTemporaryFullFilename(applicationDataSetFileName))

		then: 'Service result has validSyntax equals false and a list of errors'
			with(result) {
				!isValid
				consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
				!data.domains
				with(error){
					message == 'No such property: Unknown at line 5'
					startLine == 5
					endLine  == 5
					startColumn == null
					endColumn == null
					fatal == true
				}
			}
	}

	void 'test can test a script and register progress'() {

		given:
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			String script = """
                console on
                read labels
                iterate {
                    domain Application
                    extract 'application id' transform with toLong() load 'id'
                    extract 'vendor name' load 'Vendor'
                    load 'environment' with 'Production'

                    find Application by 'id' with DOMAIN.id into 'id'
                }
            """.stripIndent()

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'Service executes the script with incorrect syntax'
			def (ETLProcessor etlProcessor, String outputFilename) = service.executeAndSaveResultsInFile(
				GMDEMO,
				54321l,
				script,
				fileSystemService.getTemporaryFullFilename(applicationDataSetFileName),
				callback)

		then: 'Service result a valid filename with results'
			outputFilename != null

		and: 'Service result returns the instance of ETLProcessor and its results'
			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Application.name()
			results.domains[0].fieldNames == ['id', 'appVendor', 'environment'] as Set

			results.domains[0].data.size() == 2

			results.domains[0].data[0].fields.id.value == 152254l
			results.domains[0].data[0].fields.id.originalValue == '152254'
			results.domains[0].data[0].fields.id.find.results == [152254l]
			results.domains[0].data[0].fields.id.find.matchOn == 0
			results.domains[0].data[0].fields.id.find.query.size() == 1
			assertQueryResult(
				results.domains[0].data[0].fields.id.find.query[0],
				ETLDomain.Application,
				[
					['id', FindOperator.eq.name(), 152254l]
				]
			)


			results.domains[0].data[1].fields.id.value == 152255l
			results.domains[0].data[1].fields.id.originalValue == '152255'
			results.domains[0].data[1].fields.id.find.results == [152255l]
			results.domains[0].data[1].fields.id.find.matchOn == 0
			results.domains[0].data[1].fields.id.find.query.size() == 1
			assertQueryResult(
				results.domains[0].data[1].fields.id.find.query[0],
				ETLDomain.Application,
				[
					['id', FindOperator.eq.name(), 152255l]
				]
			)


			results.domains[0].data[0].fields.appVendor.value == 'Microsoft'
			results.domains[0].data[0].fields.appVendor.originalValue == 'Microsoft'
			!results.domains[0].data[0].fields.appVendor.find.query


			results.domains[0].data[1].fields.appVendor.value == 'Mozilla'
			results.domains[0].data[1].fields.appVendor.originalValue == 'Mozilla'
			!results.domains[0].data[1].fields.appVendor.find.query


			results.domains[0].data[0].fields.environment.value == 'Production'
			results.domains[0].data[0].fields.environment.originalValue == 'Production'
			!results.domains[0].data[0].fields.environment.find.query


			results.domains[0].data[1].fields.environment.value == 'Production'
			results.domains[0].data[1].fields.environment.originalValue == 'Production'
			!results.domains[0].data[1].fields.environment.find.query

		and: 'ProgressCallback registered all report messages'
			with(callback) {
				1 * reportProgress(0, true, RUNNING, '')
				1 * reportProgress(50, false, RUNNING, '')
				1 * reportProgress(100, false, RUNNING, '')
				1 * reportProgress(100, true, RUNNING, '')
				1 * reportProgress(100, true, COMPLETED, { it != null })
			}

		cleanup:
			if (outputFilename) {
				fileSystemService.deleteTemporaryFile(outputFilename)
			}

	}

	static Map fieldSpecsMap = [
		(AssetClass.APPLICATION.toString()): [
			fields: [
				[constraints: [required: 0],
				 "control"  : "Number",
				 "default"  : "",
				 "field"    : "id",
				 "imp"      : "U",
				 "label"    : "Id",
				 "order"    : 0,
				 "shared"   : 0,
				 "show"     : 0,
				 "tip"      : "",
				 "udf"      : 0
				],
				[constraints: [required: 0],
				 "control"  : "String",
				 "default"  : "",
				 "field"    : "appVendor",
				 "imp"      : "N",
				 "label"    : "Vendor",
				 "order"    : 0,
				 "shared"   : 0,
				 "show"     : 0,
				 "tip"      : "",
				 "udf"      : 0
				],
				[constraints: [required: 0],
				 "control"  : "String",
				 "default"  : "",
				 "field"    : "environment",
				 "imp"      : "N",
				 "label"    : "Environment",
				 "order"    : 0,
				 "shared"   : 0,
				 "show"     : 0,
				 "tip"      : "",
				 "udf"      : 0
				],
				[constraints: [required: 0],
				 "control"  : "String",
				 "default"  : "",
				 "field"    : "location",
				 "imp"      : "N",
				 "label"    : "Location",
				 "order"    : 0,
				 "shared"   : 0,
				 "show"     : 0,
				 "tip"      : "",
				 "udf"      : 0
				]
			]
		],
		(AssetClass.DEVICE.toString())     : [
			fields: [
				[constraints: [required: 0],
				 "control"  : "Number",
				 "default"  : "",
				 "field"    : "id",
				 "imp"      : "U",
				 "label"    : "Id",
				 "order"    : 0,
				 "shared"   : 0,
				 "show"     : 0,
				 "tip"      : "",
				 "udf"      : 0
				],
				[constraints: [required: 0],
				 "control"  : "String",
				 "default"  : "",
				 "field"    : "location",
				 "imp"      : "N",
				 "label"    : "Location",
				 "order"    : 0,
				 "shared"   : 0,
				 "show"     : 0,
				 "tip"      : "",
				 "udf"      : 0
				]
			]
		],
		(AssetClass.STORAGE.toString())    : [fields: []],
		(AssetClass.DATABASE.toString())   : [fields: []],
		(CustomDomainService.COMMON)       : [fields: []]
	]

	/**
	 * Builds a SpreadSheet dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildSpreadSheetDataSet(String sheetName, String sheetContent) {

		def (String fileName, OutputStream outputStream) = fileSystemService.createTemporaryFile('unit-test-', 'xlsx')
		Workbook workbook = WorkbookUtil.createWorkbook('xlsx')

		// Getting the Sheet at index zero
		Sheet sheet = workbook.createSheet(sheetName)
		sheetContent.readLines().eachWithIndex { String line, int rowNumber ->
			XSSFRow currentRow = sheet.createRow(rowNumber)
			line.split(",").eachWithIndex { String cellContent, int columnNumber ->
				currentRow.createCell(columnNumber).setCellValue(cellContent)
			}
		}

		WorkbookUtil.saveToOutputStream(workbook, outputStream)

		ExcelConnection con = new ExcelConnection(
			path: fileSystemService.temporaryDirectory,
			fileName: fileName,
			driver: TDSExcelDriver)
		ExcelDataset dataSet = new ExcelDataset(connection: con, header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}

	/**
	 * Assertions for a {@code QueryResult} instance
	 * @param queryResult
	 * @param domain
	 * @param values
	 */
	static boolean assertQueryResult(QueryResult queryResult, ETLDomain domain, List<List<Object>> values) {
		assert queryResult.domain == domain.name()

		queryResult.criteria.eachWithIndex { Map map, int i ->
			assert map['propertyName'] == values[i][0]
			assert map['operator'] == values[i][1]
			assert map['value'] == values[i][2]
		}

		return true
	}

}
