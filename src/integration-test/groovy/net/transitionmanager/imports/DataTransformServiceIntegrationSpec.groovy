package net.transitionmanager.imports

import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.exception.InvalidParamException
import spock.lang.Specification

@Integration
@Rollback
class DataTransformServiceIntegrationSpec extends Specification implements DataTransformServiceIntegrationData {

    DataTransformService dataTransformService

    void 'test can throw an Exception in transform ETL data if filename param is null'() {

        when: 'etl data transformation is called with a null dataset filename'
            dataTransformService.transformEtlData(null, null, null, null, null, null)

        then: 'an exception is thrown'
            InvalidParamException e = thrown InvalidParamException
            e.message == 'Missing filename parameter'
    }

    void 'test can throw an Exception in transform ETL data if filename has an invalid extension'() {

        setup: 'an dataset filename with invalid filename extension'
            setupData()
                    .withDataset("""
                        <foo></foo>
                    """, 'txt')

        when: 'etl data transformation is called'
            dataTransformService.transformEtlData(null, null, null, datasetFilename, null, null)

        then: 'an exception is thrown'
            InvalidParamException e = thrown InvalidParamException
            e.message == 'Invalid File extension.'
    }

    void 'test can throw an Exception in transform ETL data if dataScriptId is null'() {

        setup: 'a valid dataset created'
            setupData()
                    .withDataset("""
                        name
                        FOO
                    """, 'csv')

        when: 'etl data transformation is called'
            dataTransformService.transformEtlData(null, null, null, datasetFilename, null, null)

        then: 'an exception is thrown'
            InvalidParamException e = thrown InvalidParamException
            e.message == 'Missing required dataScriptId parameter'
    }

    void 'test can throw an Exception in transform ETL data if etlSourceCode dataScript is empty'() {

        setup: 'a valid dataset created'
            setupData()
                    .withDataset("""
                        name
                        FOO
                    """, 'csv')
                    .withDataScript()

        when: 'etl data transformation is called'
            dataTransformService.transformEtlData(null, project.id, dataScript.id, datasetFilename, null, null)

        then: 'an exception is thrown'
            InvalidParamException e = thrown InvalidParamException
            e.message == 'ETL Script has no source specified'
    }

    void 'test can transform data using an ETL Dataset and a DataScript with auto process false'() {

        setup: 'a valid dataset created for a valid dataScript with autoProcess false'
            setupData()
                    .withDataset("""
                        name
                        FOO
                    """, 'csv')
                    .withDataScript("""
                        read labels
                        domain Application
                        iterate {
                            extract 'name' load 'Name'
                        }
                    """)

        when: 'etl data transformation is called'
            Map<String, String> results = dataTransformService.transformEtlData(null, project.id, dataScript.id, datasetFilename, false, null)

        then: 'results contains etl JSON results'
            results.filename.endsWith('.json')
    }

    void 'test can transform data using an ETL Dataset and a DataScript with auto process false that contains errors in etlSourceCode'() {

        setup: 'a valid dataset created for a valid dataScript with autoProcess false'
            setupData()
                    .withDataset("""
                        name
                        FOO
                    """, 'csv')
                    .withDataScript("""
                        read labels
                        domain Application
                        iterate {
                            extract 'Unknown Column' load 'Name'
                        }
                    """)

        when: 'etl data transformation is called'
            Map<String, String> results = dataTransformService.transformEtlData(null, project.id, dataScript.id, datasetFilename, false, null)

        then: 'results contains etl JSON results'
            results.errorMessage == "Extracting a missing column name 'Unknown Column'"
    }
}
