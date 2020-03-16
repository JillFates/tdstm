package net.transitionmanager.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.DomainInDiskResult
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.test.mixin.Mock
import groovy.json.JsonSlurper
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.imports.ScriptProcessorService
import spock.lang.Shared
import spock.lang.Unroll

@Mock([AssetEntity])
class ETLStreamingWriterSpec extends ETLBaseSpec {

    static doWithSpring = {
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

    @Shared
    ETLProcessorResult processorResult

    @Shared
    DomainResult applicationResult

    def setup() {

        processorResult = new ETLProcessorResult(
                Mock(ETLProcessor) {
                    getFilename() >> '/tmp/filename.csv'
                })

        def (String applicationOutputFilename, OutputStream applicationOutputStream) = fileSystemService.createTemporaryFile(ScriptProcessorService.TEST_SCRIPT_PREFIX, 'json')

        applicationResult = new DomainInDiskResult(
                ETLDomain.Application.name(),
                applicationOutputFilename,
                new ETLStreamingWriter(applicationOutputStream)
        )

        applicationResult.currentRow = new RowResult(
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
        applicationResult.writeCurrentRow()

//        def (String deviceOutputFilename, OutputStream deviceOutputStream) = fileSystemService.createTemporaryFile(ScriptProcessorService.TEST_SCRIPT_PREFIX, 'json')
//
//        DomainResult deviceResult = new DomainResult(
//                ETLDomain.Application.name(),
//                deviceOutputFilename,
//                new ETLStreamingWriter(deviceOutputStream)
//        )
//
//        deviceResult.currentRow =  new RowResult()
//        deviceResult.writeCurrentRow()


        processorResult.with {
            consoleLog = ''
            version = 1
            domains = [applicationResult]
        }
    }

    @Unroll
    void 'test can write DomainResult with an instance of RowResult with value:(#value) serialized in serializedValue:(#serializedValue) '() {

        setup: 'an outputStream and an ETLProcessResult'
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
            ETLStreamingWriter streamingWriter = new ETLStreamingWriter(byteArrayOutputStream)

        and: 'ETLStreamingWriter serialized ETLProcessResult in the outputStream'
            streamingWriter.writeRowResult(
                    new RowResult(
                            op: ImportOperationEnum.INSERT,
                            rowNum: 1,
                            errorCount: 0,
                            warn: false,
                            duplicate: false,
                            errors: [],
                            fields: [
                                    description: new FieldResult(
                                            originalValue: '',
                                            value: value,
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
                            tags: null
                    )
            )

            streamingWriter.close()

            Map<String, ?> result = new JsonSlurper().parse(byteArrayOutputStream.toByteArray())

        expect: 'OutputStream results in String format contains JSON serialized content'
            result.fields.description.value == serializedValue

        where:
            value                                                 || serializedValue
            123456789                                             || 123456789
            '123456789'                                           || '123456789'
            TimeUtil.parseISO8601DateTime("1978-06-22T17:05:22Z") || '1978-06-22T17:05:22Z'
            JSON.parse('{"key": "value"}')                        || '[key:value]'
            [key: 'value']                                        || '[key:value]'
            ['key', 'value']                                      || '[key, value]'
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
