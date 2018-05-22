package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
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
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Shared

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>transform</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database])
class ETLTransformSpec extends ETLBaseSpec {

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
		applicationFieldsValidator.addAssetClassFieldsSpecFor(ETLDomain.Application,
			buildFieldSpecsFor(AssetClass.APPLICATION))

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

	void 'test can transform a field value with uppercase transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with uppercase()
						}
					""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'SRW24G1'
			etlProcessor.getElement(1, 1).value == 'ZPHA MODULE'
			etlProcessor.getElement(2, 1).value == 'SLIDEAWAY'
	}

	void 'test can check syntax errors at parsing time'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate
							extract 'MODEL NAME' transform with unknown()
						}
					""".stripIndent())

		then: 'An MultipleCompilationErrorsException exception is thrown'
			thrown MultipleCompilationErrorsException
	}

	void 'test can transform a field value with uppercase transformation inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform {
								uppercase()
							}
						}
					""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'SRW24G1'
			etlProcessor.getElement(1, 1).value == 'ZPHA MODULE'
			etlProcessor.getElement(2, 1).value == 'SLIDEAWAY'
	}

	void 'test can transform a field value to lowercase transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with lowercase()
						}
					""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'srw24g1'
			etlProcessor.getElement(1, 1).value == 'zpha module'
			etlProcessor.getElement(2, 1).value == 'slideaway'
	}

	void 'test can transform a field value to lowercase transformation inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform {
								lowercase()
							}
						}
					""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == 'srw24g1'
			etlProcessor.getElement(1, 1).value == 'zpha module'
			etlProcessor.getElement(2, 1).value == 'slideaway'
	}

	void 'test can transform a field value with taking left 4 characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'model name' transform with left(4)
						}
					""".stripIndent())

		then: 'Every column for every row is transformed to left 4 transformation'
			etlProcessor.getElement(0, 1).value == "SRW2"
			etlProcessor.getElement(1, 1).value == "ZPHA"
			etlProcessor.getElement(2, 1).value == "Slid"
	}

	void 'test can transform a field value with taking left 4 characters inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
					extract 'model name' transform {
							left(4)
						}
					}
				""".stripIndent())

		then: 'Every column for every row is transformed to left 4 transformation'
			etlProcessor.getElement(0, 1).value == "SRW2"
			etlProcessor.getElement(1, 1).value == "ZPHA"
			etlProcessor.getElement(2, 1).value == "Slid"
	}

	void 'test can transform a field value with taking middle 2 characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(3, 2) lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 1).value == "w2"
			etlProcessor.getElement(1, 1).value == "ha"
			etlProcessor.getElement(2, 1).value == "id"
	}

	void 'test can throw an exception when a middle transformation is staring in zero'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(0, 2) lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'From To initial position must be >= 1'
	}

	void 'test can transform a field value with taking middle 2 characters inside a closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(3, 2) lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 1).value == "w2"
			etlProcessor.getElement(1, 1).value == "ha"
			etlProcessor.getElement(2, 1).value == "id"
	}

	void 'test can transform a field value replacing first A characters'() {
		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with uppercase() replaceFirst('A', 'X')
					}
				""".stripIndent())

		then: 'The first column for every row should be uppercase and swap first "A" character with "X"'
			etlProcessor.getElement(0, 1).value == "SRW24G1"
			etlProcessor.getElement(1, 1).value == "ZPHX MODULE"
			etlProcessor.getElement(2, 1).value == "SLIDEXWAY"
	}

	void 'test can transform a field value replacing last A characters'() {
		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with uppercase() replaceLast('A', 'X')
					}
				""".stripIndent())

		then: 'the first column for every row should be uppercase and last "A" character replaced with "X"'
			etlProcessor.getElement(0, 1).value == "SRW24G1"
			etlProcessor.getElement(1, 1).value == "ZPHX MODULE"
			etlProcessor.getElement(2, 1).value == "SLIDEAWXY"
	}

	void 'test can transform a field value replacing all A characters'() {
		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with uppercase() replaceAll('A', 'X')
					}
				""".stripIndent())

		then: 'Every column for every row striping all "A" characters'
			etlProcessor.getElement(0, 1).value == "SRW24G1"
			etlProcessor.getElement(1, 1).value == "ZPHX MODULE"
			etlProcessor.getElement(2, 1).value == "SLIDEXWXY"
	}

	void 'test can apply another transformation for a field value after replacing all A characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with uppercase() replaceAll('A', 'X') lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row striping all "A" characters'
			etlProcessor.getElement(0, 1).value == "srw24g1"
			etlProcessor.getElement(1, 1).value == "zphx module"
			etlProcessor.getElement(2, 1).value == "slidexwxy"
	}

	void 'test can transform a field value with taking right 4 characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with right(4)
					}
				""".stripIndent())

		then: 'Every column for every row is transformed with right 4 transformation'
			etlProcessor.getElement(0, 1).value == "24G1"
			etlProcessor.getElement(1, 1).value == "DULE"
			etlProcessor.getElement(2, 1).value == "away"
	}

	void 'test can transform a use left 4 transformation in a chain of transformations'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with left(4) lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == "srw2"
			etlProcessor.getElement(1, 1).value == "zpha"
			etlProcessor.getElement(2, 1).value == "slid"
	}

	void 'test can transform a field value using replace command with a String value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Application
					read labels
					iterate {
						extract 'vendor name' transform with trim() replace('Inc', 'Incorporated') load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			with(etlProcessor.resultsMap()){
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					with(data[0].fields.appVendor) {
						originalValue.contains('Microsoft\b\nInc')
						value == 'Microsoft\b\nIncorporated'
					}

					with(data[1].fields.appVendor) {
						originalValue.contains('Mozilla\t\t\0Inc')
						value == 'Mozilla\t\t\0Incorporated'
					}
				}
			}
	}

	void 'test can transform a field value using replace command with a Regular expression value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Application
					read labels
					iterate {
						extract 'vendor name' transform with trim() replace(/a|b|c/, '') load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			with(etlProcessor.resultsMap()) {
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					with(data[0].fields.appVendor) {
						originalValue.contains('Microsoft\b\nInc')
						value == "Mirosoft\b\nIn"
					}

					with(data[1].fields.appVendor) {
						originalValue.contains('Mozilla\t\t\0Inc')
						value == "Mozill\t\t\0In"
					}
				}
			}
	}

	void 'test can apply transformations on a field value many times'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with uppercase() lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == "srw24g1"
			etlProcessor.getElement(1, 1).value == "zpha module"
			etlProcessor.getElement(2, 1).value == "slideaway"
	}

	void 'test can append strings and element in a transformation chain using local variables'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuffer()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name' transform with lowercase() set myLocalVar
						extract 'location' transform with append(' - ', myLocalVar) load 'description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.resultsMap()) {
				domains.size() == 1
				with(domains[0]) {
					domain == 'Application'
					with(data[0].fields.description) {
						value == 'ACME Data Center - microsoft'
						originalValue == 'ACME Data Center'
					}

					with(data[1].fields.description) {
						value == 'ACME Data Center - mozilla'
						originalValue == 'ACME Data Center'
					}
				}
			}
	}

	void 'test can throw an ETLProcessorException for an invalid console status'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					console 'open'
					domain Device
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Unrecognized command console with args [open]'
	}

	void 'test can translate an extracted value using a dictionary'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), environmentDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					def final dictionary = [prod: 'Production', dev: 'Development']
					read labels
					iterate {
						extract 'environment' transform with lowercase() substitute(dictionary)
					}
				""".stripIndent())

		then: 'The column is trsanlated for every row'
			etlProcessor.getElement(0, 3).value == "Production"
			etlProcessor.getElement(1, 3).value == "Production"
			etlProcessor.getElement(2, 3).value == "Development"
	}

	void 'test can plus strings, current element and a defined variable in a transformation using local variables'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), applicationDataSet, console, validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application

					iterate {
						extract 'vendor name' transform with lowercase() set myLocalVar
						extract 'location' transform with append(myLocalVar + ' - ' + CE) load 'description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.resultsMap()) {
				domains.size() == 1
				with(domains[0]) {
					domain == 'Application'
					with(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Centermicrosoft - ACME Data Center'
					}

					with(data[1].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Centermozilla - ACME Data Center'
					}
				}
			}
	}

	void 'test can append strings, current element and a defined variable in a transformation using local variables'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuffer()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
					extract 'vendor name' transform with lowercase() set myLocalVar
					extract 'location' transform with append('-', myLocalVar, '-' , CE ) load 'description'
				}""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.resultsMap()) {
				domains.size() == 1
				with(domains[0]) {
					domain == 'Application'
					with(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center-microsoft-ACME Data Center'
					}

					with(data[1].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center-mozilla-ACME Data Center'
					}
				}
			}

	}

	void 'test can append strings and elements in a transformation using local variables'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuffer()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name' transform with lowercase() set myLocalVar
						extract 'location' transform with append(' - ', myLocalVar, ' - ') load 'description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.resultsMap()) {
				domains.size() == 1
				with(domains[0]) {
					domain == 'Application'
					with(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center - microsoft - '
					}

					with(data[1].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center - mozilla - '
					}
				}
			}
	}

	void 'test can use a set element in a transformation using local variables'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuffer()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name' transform with lowercase() set myLocalVar
						extract 'location' transform with append(' - ', myLocalVar) load 'description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.resultsMap()) {
				domains.size() == 1
				with(domains[0]) {
					domain == 'Application'
					with(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center - microsoft'
					}

					with(data[1].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center - mozilla'
					}
				}
			}
	}

	void 'test can use a set element in a transformation closure'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				applicationDataSet,
				new DebugConsole(buffer: new StringBuffer()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'location' transform {
							lowercase() append('**')
						} load 'description'

					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.resultsMap()) {
				domains.size() == 1
				with(domains[0]) {
					domain == 'Application'
					with(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'acme data center**'
					}

					with(data[1].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'acme data center**'
					}
				}
			}
	}

	void 'test can sanitize element value to replace all of the escape characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Application
					read labels
					iterate {
						extract 'vendor name' transform with sanitize() load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getRow(0).getElement(1).value == "Microsoft~+Inc"
			etlProcessor.getRow(0).getElement(1).fieldDefinition.name == "appVendor"

			etlProcessor.getRow(1).getElement(1).value == "Mozilla++~Inc"
			etlProcessor.getRow(1).getElement(1).fieldDefinition.name == "appVendor"

	}

	void 'test can trim element values to remove leading and trailing whitespaces'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Application
					read labels
					iterate {
						extract 'vendor name' transform with trim() load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getRow(0).getElement(1).value == "Microsoft\b\nInc"
			etlProcessor.getRow(0).getElement(1).fieldDefinition.name == "appVendor"

			etlProcessor.getRow(1).getElement(1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getRow(1).getElement(1).fieldDefinition.name == "appVendor"

	}

	void 'test can transform globally a field value using replace command with a String value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					trim on
					replace 'Inc', 'Incorporated'
					domain Application
					read labels
					iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nIncorporated"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Incorporated"
			etlProcessor.getElement(1, 1).fieldDefinition.name == "appVendor"
	}

	void 'test can transform globally a field value using replace command using a range in the iteration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					trim on
					replace ControlCharacters with '~'
					domain Application
					read labels
					from 1 to 2 iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nInc"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"
	}

	void 'test can turn on globally trim command to remove leading and trailing whitespaces'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						trim on
						domain Application
						read labels
						iterate {
							extract 'vendor name' load 'appVendor'
						}
					""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nInc"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getElement(1, 1).fieldDefinition.name == "appVendor"

	}

	void 'test can turn on globally trim command without defining on parameter'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					trim on
					domain Application
					read labels
					iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft\b\nInc"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla\t\t\0Inc"
			etlProcessor.getElement(1, 1).fieldDefinition.name == "appVendor"

	}

	void 'test can turn on globally sanitize command to replace all of the escape characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					sanitize on
					domain Application
					read labels
					iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft~+Inc"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla++~Inc"
			etlProcessor.getElement(1, 1).fieldDefinition.name == "appVendor"
	}


	void 'test can transform a field value using to number transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'device id' transform with toLong()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 0).value == 152254l
			etlProcessor.getElement(1, 0).value == 152255l
			etlProcessor.getElement(2, 0).value == 152256l
	}

	void 'test can throw an Exception transforming incorrectly a value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'device id' transform with toInteger() uppercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'uppercase function only supported for String values (152254 : class java.lang.Integer)'

	}
}
