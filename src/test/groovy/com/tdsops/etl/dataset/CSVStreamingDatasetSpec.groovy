package com.tdsops.etl.dataset

import com.tdsops.etl.*
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.project.Project
import spock.lang.See

@TestMixin(ControllerUnitTestMixin)
@Mock([AssetEntity])
class CSVStreamingDatasetSpec extends ETLBaseSpec {

    static doWithSpring = {
        coreService(CoreService) {
            grailsApplication = ref('grailsApplication')
        }
        fileSystemService(FileSystemService) {
            coreService = ref('coreService')
            transactionManager = ref('transactionManager')
        }
    }

    DebugConsole debugConsole
    Project GMDEMO
    ETLFieldsValidator validator

    def setup() {
        GMDEMO = Mock(Project)
        GMDEMO.getId() >> 125612l
        debugConsole = new DebugConsole(buffer: new StringBuilder())
        validator = createDomainClassFieldsValidator()
    }

    @See('TM-16579')
    void 'test can read labels from csv dataset and create a map of columns'() {

        given:
            String fileName = createCSVFIle("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVStreamingDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
			""".stripIndent())

        then: 'A column map is created'
            etlProcessor.column('name').index == 0
            etlProcessor.column(0).label == 'name'

        and:
            etlProcessor.column('cpu').index == 1
            etlProcessor.column(1).label == 'cpu'

        and:
            etlProcessor.column('description').index == 2
            etlProcessor.column(2).label == 'description'

        and:
            etlProcessor.currentRowIndex == 1

        cleanup:
            if (fileName) {
                fileSystemService.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can skip rows until read labels from csv dataset and create a map of columns'() {

        given:
            String fileName = createCSVFIle("""
                UNKNOWN
                UNNECESSARY
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVStreamingDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				skip 2
				read labels
			""".stripIndent())

        then: 'A column map is created'
            etlProcessor.column('name').index == 0
            etlProcessor.column(0).label == 'name'

        and:
            etlProcessor.column('cpu').index == 1
            etlProcessor.column(1).label == 'cpu'

        and:
            etlProcessor.column('description').index == 2
            etlProcessor.column(2).label == 'description'

        and:
            etlProcessor.currentRowIndex == 3

        cleanup:
            if (fileName) {
                fileSystemService.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can extract a field value over all rows based on column ordinal position'() {

        given:
            String fileName = createCSVFIle("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVStreamingDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 1
				}
			""".stripIndent())

        then:

            assertWith(etlProcessor.currentRow.getElement(0), Element) {
                value == "zuludb01"
                originalValue == "zuludb01"
            }

        cleanup:
            if (fileName) {
                fileSystemService.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can extract a field value over all rows based on column name'() {

        given:
            String fileName = createCSVFIle("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVStreamingDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 'name'
				}
			""".stripIndent())

        then:

            assertWith(etlProcessor.currentRow.getElement(0), Element) {
                value == "zuludb01"
                originalValue == "zuludb01"
            }

        cleanup:
            if (fileName) {
                fileSystemService.deleteTemporaryFile(fileName)
            }
    }

}
