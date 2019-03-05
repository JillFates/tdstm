package com.tdsops.etl

import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.Specification

class ETLTransformPrimaryVerbSpec extends Specification implements FieldSpecValidateableTest, ETLAssertTest, DataTest, AutowiredTest {

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

		// for multiple domains, call mockDomains...
		// mockDomains Person, Address, Company
	}

	void setup() {
		assert fileSystemService != null

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l
		debugConsole = new DebugConsole(buffer: new StringBuilder())

		validator = createDomainClassFieldsValidator()
	}

	void 'test can transform local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name
				acmevmprod01
		""")

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
					transform nameVar uppercase() set upperNameVar 
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
						assertFieldResult(fields['assetName'], 'ACMEVMPROD01', 'ACMEVMPROD01')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName)
	}

	/**
	 *
	 * @param fileName
	 */
	void deleteTemporaryFile(String fileName) {
		if (fileName) {
			fileSystemService.deleteTemporaryFile(fileName)
		}
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	protected List buildCSVDataSet(String csvContent) {
		def (String fileName, OutputStream dataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
		dataSetOS << csvContent
		dataSetOS.close()

		String fullName = fileSystemService.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}
}
