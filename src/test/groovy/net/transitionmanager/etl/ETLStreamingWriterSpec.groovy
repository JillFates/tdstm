package net.transitionmanager.etl

import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults
import com.tdsops.tm.enums.domain.ImportOperationEnum
import spock.lang.Shared

class ETLStreamingWriterSpec extends ETLBaseSpec {

    @Shared
    ETLProcessorResult processorResult

    def setupSpec() {

        processorResult = new ETLProcessorResult(
                Mock(ETLProcessor) {
                    getFilename() >> '/tmp/filename.csv'
                })

        processorResult.with {
            consoleLog = ''
            version = 1
            domains = [
                    new DomainResult(
                            domain: 'Application',
                            fieldLabelMap: ['id': 'Id', 'assetName': 'Name'],
                            fieldNames: ['id', 'assetName'].toSet(),
                            data: [
                                    new RowResult(
                                            op: ImportOperationEnum.INSERT,
                                            rowNum: 1,
                                            errorCount: 0,
                                            warn: false,
                                            duplicate: false,
                                            errors: [],
                                            fields: [
                                                    id: new FieldResult(
                                                            originalValue: '123456789',
                                                            value: 123456789,
                                                            init: null,
                                                            fieldOrder: 1,
                                                            errors: [],
                                                            warn: false,
                                                            find: null,
                                                            create: null,
                                                            update: null
                                                    )
                                            ],
                                            domain: 'Application',
                                            comments: [],
                                            tags: new TagResults(add: ['FUBAR', 'SNAFU'])
                                    )
                            ]
                    )
            ]
        }
    }

    void 'test can write DomainResult List in an OutputStream'() {

        given: 'an outputStream and an ETLProcessResult'
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()

        when: 'ETLStreamingWriter serialized ETLProcessResult in the outputStream'
            new ETLStreamingWriter(byteArrayOutputStream).writeETLResultsData(processorResult.domains[0].data)
            String results = new String(byteArrayOutputStream.toByteArray(), 'UTF-8')

        then: 'OutputStream results in String format contains JSON serialized content'
            results.contains('"tags":{"add":["FUBAR","SNAFU"],"remove":[],"replace":{}}')
            results.contains('{"errors":[],"rowNum":1,"errorCount":0,"warn":false,"duplicate":false,"op":"Insert"')
            results.contains('"fields":{"id":{"fieldOrder":1,"warn":false,"errors":[],"init":null,"originalValue":"123456789","value":123456789}}')
            results.contains('"domain":"Application"')
            results.contains('"comments":[]')
    }

    void 'test can write ETLProcessorResult as header in an OutputStream'() {

        given: 'an outputStream and an ETLProcessResult'
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()

        when: 'ETLStreamingWriter serialized ETLProcessResult in the outputStream'
            new ETLStreamingWriter(byteArrayOutputStream).writeETLResultsHeader(processorResult)
            String results = new String(byteArrayOutputStream.toByteArray(), 'UTF-8')

        then: 'OutputStream results in String format contains JSON serialized content'
            results.contains('{"ETLInfo":{"originalFilename":"/tmp/filename.csv"}')
            results.contains('"domain":"Application"')
            results.contains('"consoleLog":""')
            results.contains('"version":1')
            results.contains('"fieldNames":["assetName","id"]')
            results.contains('"fieldLabelMap":{"id":"Id","assetName":"Name"}')
    }


}
