package com.tdsops.etl.dataset

import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.ImportOperationEnum
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
            def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""".stripIndent())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    sixRowsDataSet,
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
			""".stripIndent())

        then: 'A column map is created'
            etlProcessor.column('device id').index == 0
            etlProcessor.column(0).label == 'device id'

        and:
            etlProcessor.column('model name').index == 1
            etlProcessor.column(1).label == 'model name'

        and:
            etlProcessor.column('manufacturer name').index == 2
            etlProcessor.column(2).label == 'manufacturer name'

        and:
            etlProcessor.currentRowIndex == 1

        cleanup:
            if (fileName) {
                fileSystemService.deleteTemporaryFile(fileName)
            }
    }
}
