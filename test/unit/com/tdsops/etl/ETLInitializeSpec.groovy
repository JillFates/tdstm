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
import spock.lang.Shared

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>initialize</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLInitializeSpec extends ETLBaseSpec {

	@Shared
	Map conParams = [path: "${TFS.systemPath}/test_path_csv", createPath: true, extension: 'csv', codePage: 'utf-8']

	@Shared
	CSVConnection csvConnection

	@Shared
	JSONConnection jsonConnection

	DataSetFacade applicationDataSet
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

		validator = createDomainClassFieldsValidator()
	}

	/**
	 * Initialization commands
	 */
	void 'test can initialize field defined before the load command'() {

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
						initialize 'appVendor' with 'Apple'
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					with(data[0]) {
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == 'Apple'
						}
					}

					with(data[1]) {
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
							init == 'Apple'
						}
					}
				}
			}
	}

	void 'test can initialize field defined after the load command'() {

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
						extract 'vendor name' load 'appVendor'
						initialize 'appVendor' with 'Apple'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					with(data[0]) {
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == 'Apple'
						}
					}

					with(data[1]) {
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
							init == 'Apple'
						}
					}
				}
			}
	}

	void 'test can init field defined before the load command'() {

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
						init 'appVendor' with 'Apple'
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					with(data[0]) {
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == 'Apple'
						}
					}

					with(data[1]) {
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
							init == 'Apple'
						}
					}
				}
			}
	}

	void 'test can init field defined after the load command'() {

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
						extract 'vendor name' load 'appVendor'
						init 'appVendor' with 'Apple'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					with(data[0]) {
						rowNum == 1
						with(fields.appVendor) {
							value == 'Microsoft'
							originalValue == 'Microsoft'
							init == 'Apple'
						}
					}

					with(data[1]) {
						rowNum == 2
						with(fields.appVendor) {
							value == 'Mozilla'
							originalValue == 'Mozilla'
							init == 'Apple'
						}
					}
				}
			}
	}

	void 'test can initialize an element with defined value'() {

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
						extract 'vendor name' initialize 'appVendor'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					with(data[0]) {
						rowNum == 1
						with(fields.appVendor) {
							value == null
							originalValue == null
							init == 'Microsoft'
						}
					}

					with(data[1]) {
						rowNum == 2
						with(fields.appVendor) {
							value == null
							originalValue == null
							init == 'Mozilla'
						}
					}
				}
			}
	}

	void 'test can initialize values using a literal String'() {

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
						init 'appVendor' with 'Apple'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					data.collect { it.rowNum } == [1, 2]
					data.collect { it.fields.appVendor.value } == [null, null]
					data.collect { it.fields.appVendor.originalValue } == [null, null]
					data.collect { it.fields.appVendor.init } == ['Apple', 'Apple']
				}
			}
	}

	void 'test can initialize values using an implicit String'() {

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
						init 'appVendor' with 'Apple'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					data.collect { it.rowNum } == [1, 2]
					data.collect { it.fields.appVendor.value } == [null, null]
					data.collect { it.fields.appVendor.originalValue } == [null, null]
					data.collect { it.fields.appVendor.init } == ['Apple', 'Apple']
				}
			}
	}

	void 'test can initialize values using an SOURCE value'() {

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
						init 'appVendor' with SOURCE.'vendor name'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					data.collect { it.rowNum } == [1, 2]
					data.collect { it.fields.appVendor.value } == [null, null]
					data.collect { it.fields.appVendor.originalValue } == [null, null]
					data.collect { it.fields.appVendor.init } == ['Microsoft', 'Mozilla']
				}
			}
	}

	void 'test can initialize values using an DOMAIN value'() {

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
						extract 'vendor name' load 'appVendor'
						init 'appVendor' with DOMAIN.appVendor
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					data.collect { it.rowNum } == [1, 2]
					data.collect { it.fields.appVendor.value } == ['Microsoft', 'Mozilla']
					data.collect { it.fields.appVendor.originalValue } == ['Microsoft', 'Mozilla']
					data.collect { it.fields.appVendor.init } == ['Microsoft', 'Mozilla']
				}
			}
	}

	void 'test can initialize values using CE value'() {

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
						extract 'vendor name'
						init 'appVendor' with CE
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 2
					data.collect { it.rowNum } == [1, 2]
					data.collect { it.fields.appVendor.value } == [null, null]
					data.collect { it.fields.appVendor.originalValue } == [null, null]
					data.collect { it.fields.appVendor.init } == ['Microsoft', 'Mozilla']
				}
			}
	}

}
