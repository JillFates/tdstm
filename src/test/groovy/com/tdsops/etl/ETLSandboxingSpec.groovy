package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.tm.enums.domain.ImportOperationEnum
import grails.test.mixin.Mock
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.See

@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Rack, Model])
class ETLSandboxingSpec extends ETLBaseSpec {

	Project GMDEMO
	Project TMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator
	DataSetFacade simpleDataSet
	String simpleDataSetFileName

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
		String.mixin StringAppendElement
	}

	def setup() {

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()

		debugConsole = new DebugConsole(buffer: new StringBuilder())

		(simpleDataSetFileName, simpleDataSet) = buildCSVDataSet(deviceDataSetContent)
	}

	def cleanup() {
		if (simpleDataSetFileName) fileSystemService.deleteTemporaryFile(simpleDataSetFileName)
	}

	void 'test can check syntax in an ETL script with groovy comments'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			Map<String, ?> result = etlProcessor.checkSyntax("""
				// Script supports one line comments
				/*
					And multiple Lines comments
				*/
				domain Application
			""".stripIndent())

		then: 'It has a valid syntax'
			result.validSyntax == true
			result.errors == []

	}

	void 'test can evaluate an ETL script with groovy comments'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					GroovyMock(DataSetFacade),
					GroovyMock(DebugConsole),
					GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				// Script supports one line comments
				/*
					And multiple Lines comments
				*/
				domain Application
			""".stripIndent())

		then: 'A domain is selected'
			etlProcessor.selectedDomain.domain == ETLDomain.Application

		and: 'A new result was added in the result'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 0
				}
			}
	}

	void 'test can check syntax in an ETL script'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			Map<String, ?> result = etlProcessor.checkSyntax("""
					domain Device
					read labels
					iterate
						extract 'MODEL NAME'
					}
				""".stripIndent())

		then: 'Result has validSyntax equals false and a list of errors'
			assertWith(result) {
				validSyntax == false
				errors.size() == 1
				assertWith(errors[0]) {
					startLine == 5
					endLine == 5
					startColumn == 1
					endColumn == 2
					fatal == true
					message == 'unexpected token: } @ line 5, column 1.'
				}
			}
	}

	void 'test can evaluate an ETL script with syntax errors'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					domain Device
					read labels
					iterate
						extract 'MODEL NAME'
					}
				""".stripIndent())

		then: 'An MultipleCompilationErrorsException exception is thrown'
			thrown MultipleCompilationErrorsException
	}

	void 'test can check syntax of an ETL script disallowing closure creation and using a custom compiler configuration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // allow closure creation for the ETL iterate command
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = ['org.springframework.beans.factory.annotation.Autowired']  // Empty withe list means forbid imports
			secureASTCustomizer.starImportsWhitelist = []

			ImportCustomizer customizer = new ImportCustomizer()

			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

		when: 'The ETL script is evaluated'
			Map<String, ?> result = etlProcessor
				.checkSyntax("""
					domain Device
					read labels
					def greeting = { String name -> "Hello, \$name!" }
					assert greeting('Diego') == 'Hello, Diego!'
				""".stripIndent(),
				configuration)

		then: 'Result has validSyntax equals false and a list of errors'
			assertWith(result) {
				validSyntax == false
				errors.size() == 1
				assertWith(errors[0]) {
					startLine == null
					endLine == null
					startColumn == null
					endColumn == null
					fatal == true
					message == 'Closures are not allowed'
				}
			}
	}

	void 'test can evaluate an ETL script disallowing closure creation and using a custom compiler configuration'() {
		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // allow closure creation for the ETL iterate command
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = ['org.springframework.beans.factory.annotation.Autowired']  // Empty withe list means forbid imports
			secureASTCustomizer.starImportsWhitelist = []

			ImportCustomizer customizer = new ImportCustomizer()

			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

		when: 'The ETL script is evaluated'
			etlProcessor
				.evaluate("""
					domain Device
					read labels
					def greeting = { String name -> "Hello, \$name!" }
					assert greeting('Diego') == 'Hello, Diego!'
				""".stripIndent(),
				configuration)

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors[0].cause*.message == ['Closures are not allowed']
	}

	void 'test can disallow method creation using the default ETLProcessor AST compiler configuration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				domain Device
				read labels
				def greeting(String name){
					"Hello, \$name!"
				}
				assert greeting('Diego') == 'Hello, Diego!'
			""".stripIndent())

		then: 'An MissingMethodException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Method definitions are not allowed"]
	}

	void 'test can disallow unnecessary imports using using the default ETLProcessor AST compiler configuration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				import java.lang.Math

				domain Device
				read labels
				Math.max 10, 100
				""".stripIndent())

		then: 'An MultipleCompilationErrorsException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math] is not allowed"]
	}

	void 'test can disallow unnecessary stars imports using a secure syntax with AST customizer'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.
				evaluate("""
					import java.lang.Math.*

					domain Device
					read labels
					max 10, 100
				""".stripIndent())

		then: 'An MultipleCompilationErrorsException exception is thrown'
			MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
			e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math.*] is not allowed"]
	}

	void 'test can allow stars imports using custom secure using a custom compiler configuration'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				GroovyMock(DebugConsole),
				GroovyMock(ETLFieldsValidator))

		and:
			SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
			secureASTCustomizer.closuresAllowed = false             // allow closure creation for the ETL iterate command
			secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
			secureASTCustomizer.importsWhitelist = ['org.springframework.beans.factory.annotation.Autowired']  // Empty withe list means forbid imports

			ImportCustomizer customizer = new ImportCustomizer()
			customizer.addStaticStars(Math.name)
			CompilerConfiguration configuration = new CompilerConfiguration()
			configuration.addCompilationCustomizers customizer, secureASTCustomizer

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				max 10, 100
			""".stripIndent(),
				configuration)

		then: 'An MultipleCompilationErrorsException exception is not thrown'
			notThrown MultipleCompilationErrorsException
	}

	void 'test can enable console and log domain selected'() {

		given:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				simpleDataSet,
				new DebugConsole(buffer: new StringBuilder()),
				GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				domain Device
			""".stripIndent())

		then: 'A console content could be recovered after processing an ETL Script'
			etlProcessor.debugConsole.buffer.toString().startsWith(
				new StringBuilder("INFO - Selected Domain: Device")
					.append(System.lineSeparator())
					.toString()
			)
	}

	void 'test can debug a selected value for a column name'() {

		given:
			DebugConsole console = new DebugConsole(buffer: new StringBuilder())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), simpleDataSet, console, GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				domain Device
				read labels
				iterate {
				    10/2 == 5
					debug 'device id'
				}
				""".stripIndent())

		then: 'A console content could be recovered after processing an ETL Scrtipt'
			console.buffer.toString().contains('INFO - Reading labels [0:device id, 1:model name, 2:manufacturer name, 3:location]')
			console.buffer.toString().contains('INFO - Selected Domain: Device')
			console.buffer.toString().contains('DEBUG - [position:[0, 1], value:152254]')
			console.buffer.toString().contains('DEBUG - [position:[0, 2], value:152255]')
	}

	@See('TM-11563')
	void 'test can use type boolean expressions'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
					name
					x
					y
					z
			'''.stripIndent())
		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
					def isNew = true
					read labels
					domain Device
					iterate {
						extract 1 load 'assetName'
					}
			'''.stripIndent())

		then: 'Results should contain Device Name assigment'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 3

					assertWith(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						assertWith(fields.assetName) {
							originalValue == 'x'
							value == 'x'
						}
					}
					assertWith(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						assertWith(fields.assetName) {
							originalValue == 'y'
							value == 'y'
						}
					}
					assertWith(data[2], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 3
						assertWith(fields.assetName) {
							originalValue == 'z'
							value == 'z'
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	final static String deviceDataSetContent = """
		device id,model name,manufacturer name,location
		152254,Microsoft,(xlsx updated),ACME Data Center
		152255,Mozilla,NGM,ACME Data Center
	""".stripIndent()

}
