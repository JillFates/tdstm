package com.tdsops.etl


import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.Specification

class ETLTransformLocalVariableSpec extends Specification implements FieldSpecValidateableTrait, ETLFileSystemTest, ETLAssertTest, DataTest, AutowiredTest {

	Closure doWithSpring() {
		{ ->
			coreService(CoreService) {
				grailsApplication = ref('grailsApplication')
			}
			fileSystemService(FileSystemService) {
				coreService = ref('coreService')
			}
		}
	}

	FileSystemService fileSystemService
	Project GMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator

	void setupSpec() {
		mockDomain Project
	}

	void setup() {
		assert fileSystemService != null

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l
		debugConsole = new DebugConsole(buffer: new StringBuilder())

		validator = createDomainClassFieldsValidator()
	}

	void 'test can transform a local variable using uppercase method'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Application
				iterate {
					set columnVar with 'Name'
					extract columnVar transform with lowercase() set nameVar
					if (nameVar){
						nameVar transform with uppercase() set upperNameVar
						load 'Name' with upperNameVar
					}
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['assetName'], 'ACMEVMPROD01', 'ACMEVMPROD01')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can transform a local variable using a chain of methods'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Application
				iterate {
					extract 'name' set nameVar
					nameVar transform with middle(3, 2) uppercase() set upperNameVar 
					load 'Name' with upperNameVar
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['assetName'], 'ME', 'ME')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can throw an exception applying a transformation using non valid local variable'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Application
				iterate {
					extract 'name' set nameVar
					unknownVar transform with uppercase() set upperNameVar 
				}
			""".stripIndent())

		then: 'It throws an Exception'
			Exception e = thrown Exception
			with(ETLProcessor.getErrorMessage(e)) {
				message == 'No signature of method: com.tdsops.etl.LocalVariableDefinition.transform() is applicable for argument types: (com.tdsops.etl.ETLProcessor$ReservedWord) values: [with] at line 7'
				startLine == 7
				endLine == 7
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}
}
