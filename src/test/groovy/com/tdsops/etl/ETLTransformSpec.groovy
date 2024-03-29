package com.tdsops.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.dataset.ETLDataset
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdsops.tm.enums.domain.ValidationType
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.json.JSONConnection
import getl.json.JSONDataset
import getl.proc.Flow
import getl.tfs.TFS
import getl.utils.FileUtils
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.project.Project
import org.apache.commons.lang3.time.DateUtils
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.joda.time.DateMidnight
import spock.lang.See
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>transform</b></li>
 * </ul>
 */
class ETLTransformSpec extends ETLBaseSpec implements DataTest {

	@Shared
	Map conParams = [path: "${TFS.systemPath}/test_path_csv", createPath: true, extension: 'csv', codePage: 'utf-8']

	@Shared
	CSVConnection csvConnection

	@Shared
	JSONConnection jsonConnection

	@Shared
	Date now = new DateMidnight(1974, 06, 26).toDate()
	@Shared
	Date otherD = new DateMidnight(2018, 7, 4).toDate()
	DataSetFacade simpleDataSet
	DataSetFacade jsonDataSet
	DataSetFacade environmentDataSet
	DataSetFacade applicationDataSet
	DataSetFacade nonSanitizedDataSet
	DataSetFacade sixRowsDataSet
	DataSetFacade mixedTypeDataSet
	DebugConsole debugConsole
	ETLFieldsValidator applicationFieldsValidator
	ETLFieldsValidator validator

	Closure doWithSpring() {
		{ ->
			coreService(CoreService) {
				grailsApplication = ref('grailsApplication')
			}
			fileSystemService(FileSystemService) {
				coreService = ref('coreService')
			}
			applicationContextHolder(ApplicationContextHolder) { bean ->
				bean.factoryMethod = 'getInstance'
			}
		}
	}

	def setupSpec() {
		mockDomains DataScript, AssetDependency, AssetEntity, Application, Database
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
			updater(['device id': '152257', 'model name': 'Blaster', 'manufacturer name': 'SUN'])
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
		applicationDataSet.getDataSet().field << new getl.data.Field(name: 'desc', alias: 'DESC', type: "STRING")

		new Flow().writeTo(dest: applicationDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['application id': '152254', 'vendor name': 'Microsoft', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': '152255', 'vendor name': 'Mozilla', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}

		debugConsole = new DebugConsole(buffer: new StringBuilder())

		applicationFieldsValidator = new ETLFieldsValidator()
		applicationFieldsValidator.addAssetClassFieldsSpecFor(
			ETLDomain.Application,
			buildFieldSpecsFor(AssetClass.APPLICATION)
		)

		nonSanitizedDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

		new Flow().writeTo(dest: nonSanitizedDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['application id': '152254', 'vendor name': '\r\n\tMicrosoft\b\nInc\r\n\t', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
			updater(['application id': '152255', 'vendor name': '\r\n\tMozilla\t\t\0Inc\r\n\t', 'technology': 'NGM', 'location': 'ACME Data Center'])
		}

		mixedTypeDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))

		mixedTypeDataSet.getDataSet().field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "NUMERIC", isNull: false, isKey: true)
		mixedTypeDataSet.getDataSet().field << new getl.data.Field(name: 'user count', alias: 'USER COUNT', type: "INTEGER", isNull: false)
		mixedTypeDataSet.getDataSet().field << new getl.data.Field(name: 'expiration date', alias: 'EXPIRATION DATE', type: "DATE", isNull: false)
		mixedTypeDataSet.getDataSet().field << new getl.data.Field(name: 'issue date', alias: 'ISSUE DATE', type: "DATE", isNull: false)

		new Flow().writeTo(dest: mixedTypeDataSet.getDataSet(), dest_append: true) { updater ->
			updater(['device id'      : 152255, 'user count': 12345,
					 'expiration date': DateUtils.parseDate("1974-06-26", 'yyyy-MM-dd'),
					 'issue date'     : DateUtils.parseDate("1977-02-18", 'yyyy-MM-dd')
			])
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

	void 'test can apply coalesce transformation'() {

		expect:
			result == ETLProcessor.coalesce(value1, value2, value3)

		where:
			result                || value1 | value2 | value3
			null                  || null   | ''     | null
			5                     || ''     | null   | 5
			'tadah'               || null   | ''     | 'tadah'
			false                 || null   | null   | false
			true                  || ''     | true   | 3
			0                     || ''     | 0      | 1
			now                   || ''     | null   | now
			new Element(value: 5) || null   | null   | new Element(value: 5)
	}

	void 'test can apply defaultValue transformation'() {

		expect:
			result == new Element(value: value).defaultValue(dafaultVal).value

		where:

			result || value  | dafaultVal
			'abc'  || null   | 'abc'
			'abc'  || ''     | 'abc'
			'xyz'  || 'xyz'  | 'abc'
			now    || null   | now
			otherD || otherD | now
			5      || null   | 5
			42     || 42     | 5
			5      || ''     | new Element(value: 5)

	}

	void 'test can apply ellipsis transformation'() {

		expect:
			result == new Element(value: value).ellipsis(len).value

		where:

			result       || value                   | len
			'this is...' || 'this is a long string' | 10
			'Ye'         || 'Ye'                    | 10
			null         || null                    | 8
			'1974-06...' || now                     | 10
			'1234...'    || 123456                  | 7
			'123...'     || 1234.56                 | 6
			'1.23'       || 1.23456                 | 6
			'Ye'         || 'Ye'                    | 2


	}

	void 'test can apply truncate transformation'() {

		expect:
			result == new Element(value: value).truncate(len).value

		where:

			result       || value                   | len
			'this is a ' || 'this is a long string' | 10
			'Ye'         || 'Ye'                    | 10
			null         || null                    | 8
			'1974-06-26' || now                     | 10
			'123456'     || 123456                  | 6
			'1234.5'     || 1234.56                 | 6
			'1.23'       || 1.23456                 | 6
			'Ye'         || 'Ye'                    | 2

	}

	void 'test can apply prepend transformation'() {

		expect:
			result == new Element(value: value).prepend(param).value

		where:

			result                                          || value  | param
			'Prefix null'                                   || null   | 'Prefix '
			'Prefix data'                                   || 'data' | 'Prefix '
			'Prefix data'                                   || 'data' | new Element(value: 'Prefix ')
			'Prefix ' + now.format(Element.DATETIME_FORMAT) || now    | 'Prefix '
			'Prefix 12.34'                                  || 12.34  | 'Prefix '
			'Prefix 12.00'                                  || 12.00  | 'Prefix '
			'Prefix 0.00'                                   || 0.0    | 'Prefix '
			'Prefix 0.00'                                   || 0      | 'Prefix '
			'Prefix 12.00'                                  || 12     | 'Prefix '

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

	void 'test can transform a field value with taking middle out of range in taking characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(4, 10) lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 1).value == "24g1"
			etlProcessor.getElement(1, 1).value == "a module"
			etlProcessor.getElement(2, 1).value == "deaway"
	}

	void 'test can transform a field value with taking middle starts beyond the size'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(20, 10) lowercase()
					}
				""".stripIndent())

		then: 'Every column for every row is transformed with middle 2 transformation'
			etlProcessor.getElement(0, 1).value == ""
			etlProcessor.getElement(1, 1).value == ""
			etlProcessor.getElement(2, 1).value == ""
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
			e.message == 'Must use positive values greater than 0 for "middle" transform function'
	}

	void 'test can throw an exception when a middle transformation is staring in negative value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(-1, 2) lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Must use positive values greater than 0 for "middle" transform function'
	}

	void 'test can throw an exception when a middle transformation "takes" zero characters'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(1, 0) lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Must use positive values greater than 0 for "middle" transform function'
	}

	void 'test can throw an exception when a middle transformation "takes" is negative'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(1, -5) lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Must use positive values greater than 0 for "middle" transform function'
	}

	void 'test can throw an exception when a middle transformation with NO arguments'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle() lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'The middle transformation requires two parameters (startAt, numOfChars)'
	}

	void 'test can throw an exception when a middle transformation with one argument'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(1) lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'The middle transformation requires two parameters (startAt, numOfChars)'
	}

	void 'test can throw an exception when a middle transformation with more than 2 arguments'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Device
					read labels
					iterate {
						extract 'model name' transform with middle(1, 2, 3) lowercase()
					}
				""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'The middle transformation requires two parameters (startAt, numOfChars)'
	}


	/*
	// TODO: Delete the following code, how is this different from previous test?
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
	*/

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
					trim off
					iterate {
						extract 'vendor name' transform with trim() replace('Inc', 'Incorporated') load 'appVendor'
					}
				""".stripIndent())

			ETLProcessorResult result = etlProcessor.finalResult()

		then: 'Every field property is assigned to the correct element'
			result.domains.size() == 1
			result.domains[0].domain == ETLDomain.Application.name()
			result.domains[0].data[0].fields.appVendor.originalValue.contains('Microsoft\b\nInc')
			result.domains[0].data[0].fields.appVendor.value == 'Microsoft~+Incorporated'

			result.domains[0].data[1].fields.appVendor.originalValue.contains('Mozilla\t\t\0Inc')
			result.domains[0].data[1].fields.appVendor.value == 'Mozilla++~Incorporated'
	}

	void 'test can transform a field value using replace command with a Regular expression value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					domain Application
					read labels
					trim off
					iterate {
						extract 'vendor name' transform with trim() replace(/a|b|c/, '') load 'appVendor'
					}
				""".stripIndent())

			ETLProcessorResult result = etlProcessor.finalResult()

		then: 'Every field property is assigned to the correct element'

			result.domains[0].domain == ETLDomain.Application.name()
			result.domains[0].data[0].fields.appVendor.originalValue.contains('Microsoft\b\nInc')
			result.domains[0].data[0].fields.appVendor.value == "Mirosoft~+In"

			result.domains[0].data[1].fields.appVendor.originalValue.contains('Mozilla\t\t\0Inc')
			result.domains[0].data[1].fields.appVendor.value == "Mozill++~In"
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
				new DebugConsole(buffer: new StringBuilder()),
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

			ETLProcessorResult result = etlProcessor.finalResult()

		then: 'Results should contain domain results associated'

			result.domains[0].domain == ETLDomain.Application.name()
			result.domains[0].data[0].fields.description.value == 'ACME Data Center - microsoft'
			result.domains[0].data[0].fields.description.originalValue == 'ACME Data Center'

			result.domains[0].data[1].fields.description.value == 'ACME Data Center - mozilla'
			result.domains[0].data[1].fields.description.originalValue == 'ACME Data Center'
	}

	void 'test can throw an ETLProcessorException for an invalid console status'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuilder())

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
			DebugConsole console = new DebugConsole(buffer: new StringBuilder())

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

		then: 'The column is translated for every row'
			etlProcessor.getElement(0, 3).value == "Production"
			etlProcessor.getElement(1, 3).value == "Production"
			etlProcessor.getElement(2, 3).value == "Development"
	}

	void 'test can translate an extracted value using a dictionary and a default value'() {
		given: 'A simple CSV DataSet  '
			String sampleData = """
				name,model
				xraysrv01,ProLiant BL460c Gen8
				zulu,A Unknown Model
			""".stripIndent()

			def (String fileName, ETLDataset dataSet) = buildCSVDataSet(sampleData)

		and: "A DataScript using 'substitute' with and without a default value"
			String dataScript = """
				domain Device
				Map modelsAssetTypes = [
					'ProLiant BL460c Gen8': 'Blade',
					'ProLiant BL460c Gen9': 'Blade',
					'ProLiant DL380 Gen9': 'Server',
					'UCSB-B200-M3': 'Blade',
					'PowerEdge R710': 'Server'
				]
				read labels
				iterate {
					extract 'name' load 'Name'
					extract 'model' transform with substitute(modelsAssetTypes) load 'custom1'
					extract 'model' transform with substitute(modelsAssetTypes, 'VM') load 'custom2'
				}
				""".stripIndent()
		and: 'A new ETLProcessor instantiated appropriately configured'
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)
		when: 'Evaluating a DataScript with having substitute with default value'
			etlProcessor.evaluate(dataScript)
		then: 'The evaluate process completed successfully replacing using the default value for custom2 where applicable.'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == ETLDomain.Device.name()
					assertWith(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						assertWith(fields.assetName) {
							originalValue == 'xraysrv01'
							value == 'xraysrv01'
							init == null
							errors == []
							warn == false
						}
						assertWith(fields.custom1) {
							originalValue == 'ProLiant BL460c Gen8'
							value == 'Blade'
							init == null
							errors == []
							warn == false
						}
						assertWith(fields.custom2) {
							originalValue == 'ProLiant BL460c Gen8'
							value == 'Blade'
							init == null
							errors == []
							warn == false
						}
					}

					assertWith(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						assertWith(fields.assetName) {
							originalValue == 'zulu'
							init == null
							errors == []
							warn == false
						}
						assertWith(fields.custom1) {
							originalValue == 'A Unknown Model'
							value == 'A Unknown Model'
							init == null
							errors == []
							warn == false
						}
						assertWith(fields.custom2) {
							originalValue == 'A Unknown Model'
							value == 'VM'
							init == null
							errors == []
							warn == false
						}
					}
				}
			}
	}

	void 'test can plus strings, current element and a defined variable in a transformation using local variables'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuilder())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), applicationDataSet, console, validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application

					iterate {
						extract 'vendor name' transform with lowercase() set myLocalVar
						extract 'location' transform with append('', myLocalVar + ' - ' + CE) load 'description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == 'Application'
					assertWith(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Centermicrosoft - ACME Data Center'
					}

					assertWith(data[1].fields.description) {
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
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
					extract 'vendor name' transform with lowercase() set myLocalVar
					extract 'location' transform with append('', '-', myLocalVar, '-' , CE ) load 'description'
				}""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == 'Application'
					assertWith(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center-microsoft-ACME Data Center'
					}

					assertWith(data[1].fields.description) {
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
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'vendor name' transform with lowercase() set myLocalVar
						extract 'location' transform with append('', ' - ', myLocalVar, ' - ') load 'description'
					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == 'Application'
					assertWith(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center - microsoft - '
					}

					assertWith(data[1].fields.description) {
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
				new DebugConsole(buffer: new StringBuilder()),
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
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == 'Application'
					assertWith(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'ACME Data Center - microsoft'
					}

					assertWith(data[1].fields.description) {
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
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Application
					iterate {
						extract 'location' transform {
							lowercase() append('', '**')
						} load 'description'

					}
				""".stripIndent())

		then: 'Results should contain domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == 'Application'
					assertWith(data[0].fields.description) {
						originalValue == 'ACME Data Center'
						value == 'acme data center**'
					}

					assertWith(data[1].fields.description) {
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
					trim off
					iterate {
						extract 'vendor name' transform with trim() load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getRow(0).getElement(1).value == "Microsoft~+Inc"
			etlProcessor.getRow(0).getElement(1).fieldDefinition.name == "appVendor"

			etlProcessor.getRow(1).getElement(1).value == "Mozilla++~Inc"
			etlProcessor.getRow(1).getElement(1).fieldDefinition.name == "appVendor"

	}

	void 'test can transform globally a field value using replace command with a String value'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					replace 'Inc', 'Incorporated'
					domain Application
					read labels
					iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft~+Incorporated"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"

			etlProcessor.getElement(1, 1).value == "Mozilla++~Incorporated"
			etlProcessor.getElement(1, 1).fieldDefinition.name == "appVendor"
	}

	void 'test can transform globally a field value using replace command using a range in the iteration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					replace ControlCharacters with '~'
					domain Application
					read labels
					from 1 to 2 iterate {
						extract 'vendor name' load 'appVendor'
					}
				""".stripIndent())

		then: 'Every field property is assigned to the correct element'
			etlProcessor.getElement(0, 1).value == "Microsoft~+Inc"
			etlProcessor.getElement(0, 1).fieldDefinition.name == "appVendor"
	}

	void 'test can turn on globally trim command to remove leading and trailing whitespaces by default'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
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

	void 'test can turn on globally trim command without defining on parameter'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), nonSanitizedDataSet, debugConsole, applicationFieldsValidator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
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

	@See('TM-10726')
	@Unroll
	void 'test ETLTransformation concat'() {
		expect: 'concatenation build correctly'

			ETLTransformation.concat(separator, values) == results

		where:
			separator | values                                  || results
			''        | ['one', 'two', 'three']                 || 'onetwothree'
			','       | ['one', 'two', 'three']                 || 'one,two,three'
			','       | ['one', '', 'three']                    || 'one,three'
			','       | ['one', '', 'three', true]              || 'one,,three'
			','       | ['one', null, 'three', true]            || 'one,,three'
			','       | ['one', 'two', ['three', 'four']]       || 'one,two,three,four'
			','       | ['one', 'two', ['three', 'four', null]] || 'one,two,three,four'

	}

	@See('TM-11233')
	void 'test can transform a field value using to date transformation'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
					id,retire date
					1,2018-06-25
					2,2018/06/25
					3,06/25/2018
					4,99/99/2018
					5, 
					6, 
					7,abc-123
					""".stripIndent())
			Date goodDate = new Date(2018 - 1900, 6 - 1, 25)
		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), dataSet, GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'retire date' transform with toDate('yyyy-MM-dd','yyyy/MM/dd','MM/dd/yyyy') load 'Retire Date'
							extract 'retire date' transform with toDate() load 'maintExpDate'
						}
					""".stripIndent())

		then: 'Every column for every row is transformed with toDate transformation'
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 7
					assertWith(data[0]) {
						rowNum == 1
						errorCount == 0
						assertWith(fields.retireDate) {
							value == goodDate
							init == null
							errors == []
						}
						assertWith(fields.maintExpDate) {
							value == goodDate
							init == null
							errors == []
						}
					}
					assertWith(data[1]) {
						rowNum == 2
						errorCount == 0
						assertWith(fields.retireDate) {
							value == goodDate
							init == null
							errors == []
						}
						assertWith(fields.maintExpDate) {
							value == goodDate
							init == null
							errors == []
						}
					}
					assertWith(data[2]) {
						rowNum == 3
						errorCount == 1
						assertWith(fields.retireDate) {
							value == goodDate
							init == null
							errors == []
						}
						assertWith(fields.maintExpDate) {
							value == '06/25/2018'
							init == null
							errors == ['Unable to transform value to a date with pattern(s) yyyy-MM-dd, yyyy/MM/dd']
						}
					}
					assertWith(data[3]) {
						rowNum == 4
						errorCount == 1
						assertWith(fields.retireDate) {
							value == '99/99/2018'
							init == null
							errors == ['Unable to transform value to a date with pattern(s) yyyy-MM-dd, yyyy/MM/dd, MM/dd/yyyy']
						}
						assertWith(fields.maintExpDate) {
							value == '99/99/2018'
							init == null
							errors == ['Unable to transform value to a date with pattern(s) yyyy-MM-dd, yyyy/MM/dd']
						}
					}
					assertWith(data[4]) {
						rowNum == 5
						errorCount == 1    // Should be 2
						assertWith(fields.retireDate) {
							value == null
							init == null
							errors == ['Unable to transform blank or null value to a date']
						}
						assertWith(fields.maintExpDate) {
							value == null
							init == null
							errors == ['Unable to transform blank or null value to a date']
						}
					}
					assertWith(data[5]) {
						rowNum == 6
						errorCount == 1 // Should be 2
						assertWith(fields.retireDate) {
							value == null
							init == null
							errors == ['Unable to transform blank or null value to a date']
						}
						assertWith(fields.maintExpDate) {
							value == null
							init == null
							errors == ['Unable to transform blank or null value to a date']
						}
					}
					assertWith(data[6]) {
						rowNum == 7
						errorCount == 1 // Should be 2
						assertWith(fields.retireDate) {
							value == 'abc-123'
							init == null
							errors == ['Unable to transform value to a date with pattern(s) yyyy-MM-dd, yyyy/MM/dd, MM/dd/yyyy']
						}
						assertWith(fields.maintExpDate) {
							value == 'abc-123'
							init == null
							errors == ['Unable to transform value to a date with pattern(s) yyyy-MM-dd, yyyy/MM/dd']
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}

	}

	void 'test can transform a field value with format transformation'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), mixedTypeDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						domain Device
						read labels
						iterate {
							extract 'user count' transform with format('%,d')
							extract 'expiration date' transform with format('%tD')
							extract 'issue date' transform with format()
						}
					""".stripIndent())

		then: 'Every column for every row is transformed to uppercase'
			etlProcessor.getElement(0, 1).value == '12,345'
			etlProcessor.getElement(0, 2).value == '06/26/74'
			etlProcessor.getElement(0, 3).value == '1977-02-18'
	}

	@See('TM-12289')
	void 'test can transform a decimal field value using toInteger transformation'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				Name,Environment,Group,Size,Validation,Plan Status
				NGM01,Production,B,10.22,Validated,Unassigned
				NGM03,Production,C,,${ValidationType.PLAN_READY},Confirmed
				NGM04,UAT,B,5,Validated,Unassigned
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), dataSet, GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Storage
				iterate {
					extract 'Name' load 'Name'
					extract 'Size' transform with toInteger() load 'size'
				}
			""".stripIndent())

		then: 'Every column for every row is transformed with toDate transformation'
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Storage.name()
					data.size() == 3
					assertWith(data[0]) {
						errorCount == 1
						assertWith(fields.size) {
							originalValue == '10.22'
							value == '10.22'
							init == null
							errors == ['Unable to transform value to Integer']
						}
					}
					assertWith(data[1]) {
						errorCount == 0
						assertWith(fields.size) {
							originalValue == null
							value == null
							init == null
							errors == []
						}
					}
					assertWith(data[2]) {
						errorCount == 0
						assertWith(fields.size) {
							originalValue == '5'
							value == 5
							init == null
							errors == []
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}

	}

	@See('TM-12289')
	void 'test can transform a decimal field value using toLong transformation'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				Name,Environment,Group,Size,Validation,Plan Status
				NGM01,Production,B,10.22,Validated,Unassigned
				NGM03,Production,C,,${ValidationType.PLAN_READY},Confirmed
				NGM04,UAT,B,5,Validated,Unassigned
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), dataSet, GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Storage
				iterate {
					extract 'Name' load 'Name'
					extract 'Size' transform with toLong() load 'size'
				}
			""".stripIndent())

		then: 'Every column for every row is transformed with toDate transformation'
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Storage.name()
					data.size() == 3
					assertWith(data[0]) {
						errorCount == 1
						assertWith(fields.assetName) {
							value == 'NGM01'
							init == null
							errors == []
						}
						assertWith(fields.size) {
							originalValue == '10.22'
							value == '10.22'
							init == null
							errors == ['Unable to transform value to Long']
						}
					}
					assertWith(data[1]) {
						rowNum == 2
						errorCount == 0
						assertWith(fields.assetName) {
							value == 'NGM03'
							init == null
							errors == []
						}
						assertWith(fields.size) {
							originalValue == null
							value == null
							init == null
							errors == []
						}
					}
					assertWith(data[2]) {
						rowNum == 3
						errorCount == 0
						assertWith(fields.assetName) {
							value == 'NGM04'
							init == null
							errors == []
						}
						assertWith(fields.size) {
							originalValue == '5'
							value == 5
							init == null
							errors == []
						}
					}
					assertWith(data[2]) {
						errorCount == 0
						assertWith(fields.assetName) {
							value == 'NGM04'
							init == null
							errors == []
						}
						assertWith(fields.size) {
							originalValue == '5'
							value == 5
							init == null
							errors == []
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-12079')
	void 'test can use GroovyCollections, Math, RandomStringUtils, RandomUtils, RegExUtils and StringUtils transformations'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,vendor name,app version,url
				12134556,Apple Inc.,1.0.0,www.apple.com
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					load 'Name' with Math.random()
					load 'appVersion' with GroovyCollections.max([2, 4, 6])
					load 'Description' with RandomStringUtils.randomAlphanumeric(5)
					load 'appSource' with RandomUtils.nextDouble()
					load 'appTech' with RegExUtils.removeAll('ABCabc123abc', 'ABC123')
					load 'license' with StringUtils.getLevenshteinDistance('hello', 'hallo')
				}
			""".stripIndent())

		then: 'Every column for every row is transformed with toDate transformation'
			assertWith(etlProcessor.finalResult()) {
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 1

					assertWith(data[0]) {
						errorCount == 0
						assertWith(fields.assetName) {
							value != null
						}
						assertWith(fields.appVersion) {
							value == 6
						}
						assertWith(fields.description) {
							value != null
						}
						assertWith(fields.appSource) {
							value != null
						}
						assertWith(fields.appTech) {
							value != null
						}
						assertWith(fields.license) {
							value == 1
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-12079')
	void 'test can use Math and and StringUtils transformations in transform command'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,vendor name,app version,url,reference id
				12134556,Apple Inc.,1.0.0,www.apple,
			""")

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'application id' transform with toNumber() min(9999999) load 'Name' 
					extract 'reference id' transform with random() load 'externalRefId' 
					extract 'vendor name' transform with appendIfMissing('Vendor: ') load 'Vendor'
					extract 'app version' transform with prependIfMissing('V.') load 'appVersion'
					extract 'url' transform with appendIfMissing('.com') load 'license'
				}
			""".stripIndent())

		then: 'Every column for every row is transformed with toDate transformation'
			assertWith(etlProcessor.finalResult()) {
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 1

					assertWith(data[0]) {
						errorCount == 0
						assertWith(fields.assetName) {
							value == 12134556l
						}
						assertWith(fields.externalRefId) {
							value != null
						}
						assertWith(fields.appVendor) {
							value == 'Vendor: Apple Inc.'
						}
						assertWith(fields.appVersion) {
							value == 'V.1.0.0'
						}
						assertWith(fields.license) {
							value == 'www.apple.com'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-17215')
	void 'test can use NumberUtil and and StringUtil in an ETl script'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				application id,vendor name,app version,url,reference id
				12134556,Apple Inc.,1.0.0,www.tds.com/license,
			""")

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'application id' set applicationId 
					if (NumberUtil.isLong(applicationId)){
						load 'id' with NumberUtil.toLong(applicationId)
					} 
					extract 'url' set url
					if (StringUtil.isNotBlank(url)){
						 load 'license' with StringUtil.ellipsis(url, 15)
					}
				}
			""".stripIndent())

		then: 'Every column for every row is transformed with toDate transformation'
			assertWith(etlProcessor.finalResult()) {
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					data.size() == 1

					assertWith(data[0]) {
						errorCount == 0
						assertWith(fields.id) {
							value == 12134556l
							originalValue = null
							init == null
						}
						assertWith(fields.license) {
							value == 'www.tds.com/...'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				getFileSystemServiceTestBean().deleteTemporaryFile(fileName)
			}
	}
}
