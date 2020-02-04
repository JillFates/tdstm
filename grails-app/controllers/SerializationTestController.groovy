import com.google.flatbuffers.FlatBufferBuilder
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.fbs.ETLProcessorResult
import net.transitionmanager.fbs.RowResult
import net.transitionmanager.util.JsonViewRenderService

@Secured('isAuthenticated()')
class SerializationTestController implements ControllerMethods {

    FileSystemService fileSystemService
    JsonViewRenderService jsonViewRenderService


    private Map<String, ?> buildModel(Integer rowsAmount) {
        Map<String, ?> mapModel = [
                version: 1,
                rows   : []
        ]

        for (int index = 0; index < rowsAmount; index++) {
            mapModel.rows.add(
                    [
                            domain    : 'Application',
                            errorCount: 1,
                            rowNum    : index
                    ]
            )
        }

        return mapModel
    }

    def gson() {

        int rowsAmount = params.rows ? params.rows.toInteger() : 10000
        Map<String, ?> exampleResponse = buildModel(rowsAmount)

        def (String filename, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile(rowsAmount + '-rows-gson-', 'json')
        jsonViewRenderService.render('/serializationTest/example', exampleResponse, fileOutputStream)

        renderAsJson([filename: filename])
    }

    def flatbuffers() {

        int rowsAmount = params.rows ? params.rows.toInteger() : 10000
        Map<String, ?> mapModel = buildModel(rowsAmount)

        FlatBufferBuilder builder = new FlatBufferBuilder(1024)

        int[] rowResultList = new int[rowsAmount + 1]
        int index = 1

        for (Map<String, ?> rowMap : mapModel.rows) {
            int applicationDomain = builder.createString(rowMap.domain)
            int rowResult = RowResult.createRowResult(builder, index, rowMap.errorCount, applicationDomain)
            rowResultList[index] = rowResult
            index++
        }

        int rowsVector = ETLProcessorResult.createRowsVector(builder, rowResultList)
        int etlProcessorResult = ETLProcessorResult.createETLProcessorResult(builder, rowsVector, mapModel.version)

        builder.finish(etlProcessorResult)

        def (String filename, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile(rowsAmount + '-rows-flatbuffers-', 'txt')
        fileOutputStream << builder.sizedInputStream()
        fileOutputStream.flush()
        fileOutputStream.close()

        renderAsJson([filename: filename])
    }


}