package net.transitionmanager.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.FieldResult
import com.tdsops.etl.RowResult
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.test.mixin.Mock
import groovy.json.JsonSlurper
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
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
            new Integer('123')                                    || 123
            new BigDecimal('12.30')                               || new BigDecimal('12.30')
            123456789                                             || 123456789
            '123456789'                                           || '123456789'
            TimeUtil.parseISO8601DateTime("1978-06-22T17:05:22Z") || '1978-06-22T17:05:22Z'
            JSON.parse('{"key": "value"}')                        || '[key:value]'
            [key: 'value']                                        || '[key:value]'
            ['key', 'value']                                      || '[key, value]'
            ['key', 'value'] as Set                               || '[key, value]'
    }
}
