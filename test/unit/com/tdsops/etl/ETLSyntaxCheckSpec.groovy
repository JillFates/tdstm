package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.Ignore
import spock.lang.Shared

@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLSyntaxCheckSpec extends ETLBaseSpec {

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

		nonSanitizedDataSet = new DataSetFacade(new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true))
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
		nonSanitizedDataSet.getDataSet().field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

	}

	void 'test can check syntax errors at evaluation time'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""domain Device
						read labels
						iterate 
							extract 'MODEL NAME' 
						}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			thrown MultipleCompilationErrorsException
	}

	void 'test can disallow closure creation using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
//        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import


		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration)
				.evaluate("""
						domain Device
						read labels
						def greeting = { String name -> "Hello, \$name!" }
						assert greeting('Diego') == 'Hello, Diego!'
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors[0].cause*.message == ["Closures are not allowed"]
	}

	void 'test can disallow method creation using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration).
				evaluate("""
			domain Device
			read labels
			def greeting(String name){ 
				"Hello, \$name!" 
			}
			assert greeting('Diego') == 'Hello, Diego!'
		""".stripIndent(), ETLProcessor.class.name)

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Method definitions are not allowed"]
	}

	void 'test can disallow unnecessary imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration).
				evaluate("""
			
			import java.lang.Math
			
			domain Device
			read labels
			Math.max 10, 100
		""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math] is not allowed"]
	}

	void 'test can disallow unnecessary stars imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars ETLDomain.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
			secureASTCustomizer.starImportsWhitelist = []

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer


		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration).
				evaluate("""
			
			import java.lang.Math.*
			
			domain Device
			read labels
			max 10, 100
		""".stripIndent(),
					ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math.*] is not allowed"]
	}

	void 'test can allow stars imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars Math.class.name
			customizer.addStaticStars DebugConsole.ConsoleStatus.class.name

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // disallow closure creation
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
			secureASTCustomizer.starImportsWhitelist = []

		and:
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding, configuration)
				.evaluate("""
						read labels
						max 10, 100
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'An MultipleCompilationErrorsException exception is not thrown'
			notThrown MultipleCompilationErrorsException
	}

	void 'test can enable console and log domain selected'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				new DebugConsole(buffer: new StringBuffer()),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
							console on
							domain Device
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			etlProcessor.debugConsole.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
				.append(System.lineSeparator())
				.append("INFO - Selected Domain: Device")
				.append(System.lineSeparator())
				.toString()
	}

	@Ignore
	void 'test can enable console without defining on parameter'() {

		given:
			DebugConsole debugConsole = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(simpleDataSet, debugConsole)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
							console
							domain Device
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			debugConsole.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
				.append(System.lineSeparator())
				.append("INFO - Selected Domain: Device")
				.append(System.lineSeparator())
				.toString()
	}

	void 'test can debug a selected value for a column name'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
							console on
							read labels
							domain Device
							iterate {
								debug 'device id'
							}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			console.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
				.append(System.lineSeparator())
				.append("INFO - Reading labels [0:device id, 1:model name, 2:manufacturer name]")
				.append(System.lineSeparator())
				.append("INFO - Selected Domain: Device")
				.append(System.lineSeparator())
				.append("DEBUG - [position:[0, 1], value:152254]")
				.append(System.lineSeparator())
				.append("DEBUG - [position:[0, 2], value:152255]")
				.append(System.lineSeparator())
				.append("DEBUG - [position:[0, 3], value:152256]")
				.append(System.lineSeparator())
				.toString()
	}

	void 'test can throw an ETLProcessorException for an invalid console status'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuffer())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
							console 'open'
							domain Device
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Unrecognized command console with args [open]'
	}

}
