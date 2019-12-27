package com.tdsops.etl.etlmap

import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLAssertTrait
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLFileSystemTrait
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdsops.etl.dataset.ETLDataset
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.project.Project
import spock.lang.Specification

class ETLMapSpec extends Specification implements FieldSpecValidateableTrait, ETLFileSystemTrait, ETLAssertTrait, DataTest, AutowiredTest {

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

    void 'test can create a ETL map definition'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				read labels
				domain Device
				defineETLMap 'verni-devices', {
				    add 'device-name', 'Name'
				    add 'description'
				    add 'zone', uppercase(), left(3)
				    add 'environment', 'Environment', substitute(['PROD':'Production', 'DEV': 'Development'])
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
}
