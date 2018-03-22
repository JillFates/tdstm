import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.TDSExcelDriver
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WorkbookUtil
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.SettingService
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFRow
import spock.lang.Specification

@TestFor(ScriptProcessorService)
@Mock([DataScript, Project, Database, AssetEntity, Setting, Application, Database])
class ScriptProcessorServiceSpec extends Specification {

	String sixRowsDataSetFileName
	String applicationDataSetFileName
	FileSystemService fileSystemService

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
		settingService(SettingService)
	}

	static doWithConfig(c) {
		c.graph.tmpDir = '/tmp/'
	}

	def setup() {

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
					startLine == 2
					endLine == 2
					startColumn == 10
					endColumn == 11
					fatal
					message == 'unexpected token:  @ line 2, column 10.'
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
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz ->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			GroovyMock(GormUtil, global: true)
			GormUtil.isDomainProperty(_, _) >> { Object domainObject, String propertyName ->
				true
			}
			GormUtil.isDomainIdentifier(_, _) >> { Class<?> clazz, String propertyName ->
				propertyName == 'id'
			}
			GormUtil.isReferenceProperty(_, _) >> { Object domainObject, String propertyName ->
				true
			}

		and:
			String script = """
                console on
                read labels
                iterate {
                    domain Application
                    extract 'application id' load id
                    extract 'vendor name' load Vendor
                    load environment with 'Production'
                    
                    find Application by id with DOMAIN.id into id
                }
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(GMDEMO, script, fileSystemService.getTemporaryFullFilename(applicationDataSetFileName))

		then: 'Service result has validSyntax equals false and a list of errors'
			with(result) {
				isValid
				consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
				data.domains.size() == 1
				with(data.domains[0]) {
					domain == ETLDomain.Application.name()
					fields == ['id', 'appVendor', 'environment'] as Set

					with(data[0].fields.id) {
						value == '152254'
						originalValue == '152254'
						find.size == 1
						find.results == [152254]
						find.matchOn == 1
						find.query.size() == 1
						find.query[0].domain == ETLDomain.Application.name()
						find.query[0].kv.id == '152254'
					}

					with(data[1].fields.id) {
						value == '152255'
						originalValue == '152255'
						find.size == 1
						find.results == [152255]
						find.matchOn == 1
						find.query.size() == 1
						find.query[0].domain == ETLDomain.Application.name()
						find.query[0].kv.id == '152255'
					}

					with(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
						!find.query
					}

					with(data[1].fields.appVendor) {
						value == 'Mozilla'
						originalValue == 'Mozilla'
						!find.query
					}

					with(data[0].fields.environment) {
						value == 'Production'
						originalValue == 'Production'
						!find.query
					}

					with(data[1].fields.environment) {
						value == 'Production'
						originalValue == 'Production'
						!find.query
					}
				}
			}
	}

	void 'test can test a script content for Application domain Asset using a excel dataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications',
"""invalid headers, are not part, of the valid data set
application id,vendor name,technology,location
152254,Microsoft,(xlsx updated),ACME Data Center
152255,Mozilla,NGM,ACME Data Center""".stripIndent().trim())

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
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz ->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			GroovyMock(GormUtil, global: true)
			GormUtil.isDomainProperty(_, _) >> { Object domainObject, String propertyName ->
				true
			}
			GormUtil.isDomainIdentifier(_, _) >> { Class<?> clazz, String propertyName ->
				propertyName == 'id'
			}
			GormUtil.isReferenceProperty(_, _) >> { Object domainObject, String propertyName ->
				true
			}

		and:
			String script = """
                console on
                sheet 'Applications'
                skip 1
                read labels
                iterate {
                    domain Application
                    extract 'application id' load id
                    extract 'vendor name' load Vendor
                    load environment with 'Production'
                    
                    find Application by id with DOMAIN.id into id
                }
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(GMDEMO,
				script,
				fileSystemService.getTemporaryFullFilename(fileName))

		then: 'Service result has validSyntax equals false and a list of errors'
			with(result) {
				isValid
				consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
				data.domains.size() == 1
				with(data.domains[0]) {
					domain == ETLDomain.Application.name()
					fields == ['id', 'appVendor', 'environment'] as Set

					with(data[0].fields.id) {
						value == '152254'
						originalValue == '152254'
						find.size == 1
						find.results == [152254]
						find.matchOn == 1
						find.query.size() == 1
						find.query[0].domain == ETLDomain.Application.name()
						find.query[0].kv.id == '152254'
					}

					with(data[1].fields.id) {
						value == '152255'
						originalValue == '152255'
						find.size == 1
						find.results == [152255]
						find.matchOn == 1
						find.query.size() == 1
						find.query[0].domain == ETLDomain.Application.name()
						find.query[0].kv.id == '152255'
					}

					with(data[0].fields.appVendor) {
						value == 'Microsoft'
						originalValue == 'Microsoft'
						!find.query
					}

					with(data[1].fields.appVendor) {
						value == 'Mozilla'
						originalValue == 'Mozilla'
						!find.query
					}

					with(data[0].fields.environment) {
						value == 'Production'
						originalValue == 'Production'
						!find.query
					}

					with(data[1].fields.environment) {
						value == 'Production'
						originalValue == 'Production'
						!find.query
					}
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz ->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.assetName == args.assetName && it.project.id == args.project.id }
			}

		and:
			String script = """
                console on
                read labels
                iterate {
                    domain Unknown
                    load environment with Production
                    extract 'location' load Vendor
                    reference assetName with Vendor
                }
            """.stripIndent()

		when: 'Service executes the script with incorrect syntax'
			Map<String, ?> result = service.testScript(GMDEMO, script, fileSystemService.getTemporaryFullFilename(applicationDataSetFileName))

		then: 'Service result has validSyntax equals false and a list of errors'
			with(result) {
				!isValid
				error == 'Invalid domain: \'Unknown\'. It should be one of these values: [Application, Device, Database, Storage, External, Task, Person, Comment, Asset, Manufacturer, Model, Dependency, Rack, Bundle, Room, Files]'
				consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
				!data.domains
			}
	}

	static Map fieldSpecsMap = [
		(AssetClass.APPLICATION.toString()): [
			fields: [
				[constraints: [required: 0],
					"control": "Number",
					"default": "",
					"field": "id",
					"imp": "U",
					"label": "Id",
					"order": 0,
					"shared": 0,
					"show": 0,
					"tip": "",
					"udf": 0
				],
				[constraints: [required: 0],
					"control": "String",
					"default": "",
					"field": "appVendor",
					"imp": "N",
					"label": "Vendor",
					"order": 0,
					"shared": 0,
					"show": 0,
					"tip": "",
					"udf": 0
				],
				[constraints: [required: 0],
					"control": "String",
					"default": "",
					"field": "environment",
					"imp": "N",
					"label": "Environment",
					"order": 0,
					"shared": 0,
					"show": 0,
					"tip": "",
					"udf": 0
				],
				[constraints: [required: 0],
					"control": "String",
					"default": "",
					"field": "location",
					"imp": "N",
					"label": "Location",
					"order": 0,
					"shared": 0,
					"show": 0,
					"tip": "",
					"udf": 0
				]
			]
		],
		(AssetClass.DEVICE.toString()): [
			fields: [
				[constraints: [required: 0],
					"control": "Number",
					"default": "",
					"field": "id",
					"imp": "U",
					"label": "Id",
					"order": 0,
					"shared": 0,
					"show": 0,
					"tip": "",
					"udf": 0
				],
				[constraints: [required: 0],
					"control": "String",
					"default": "",
					"field": "location",
					"imp": "N",
					"label": "Location",
					"order": 0,
					"shared": 0,
					"show": 0,
					"tip": "",
					"udf": 0
				]
			]
		],
		(AssetClass.STORAGE.toString()): [fields: []],
		(AssetClass.DATABASE.toString()): [fields: []],
		(CustomDomainService.COMMON): [fields: []]
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
		sheetContent.readLines().eachWithIndex {String line, int rowNumber ->
			XSSFRow currentRow = sheet.createRow(rowNumber)
			line.split(",").eachWithIndex{ String cellContent, int columnNumber ->
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

}