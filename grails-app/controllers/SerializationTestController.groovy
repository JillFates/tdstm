import com.google.flatbuffers.FlatBufferBuilder
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.fbs.FBSDomainResult
import net.transitionmanager.fbs.FBSETLInfo
import net.transitionmanager.fbs.FBSETLProcessorResult
import net.transitionmanager.fbs.FBSFieldResult
import net.transitionmanager.fbs.FBSRowResult
import net.transitionmanager.fbs.FBSTagReplace
import net.transitionmanager.fbs.FBSTagResults
import net.transitionmanager.util.JsonViewRenderService

import java.nio.ByteBuffer

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

        FlatBufferBuilder builder = new FlatBufferBuilder(1024)

        int[] domains = new int[2]

        domains[0] = FBSDomainResult.createFBSDomainResult(
                builder,
                builder.createString('Application'),
                FBSDomainResult.createFieldNamesVector(builder, (int[]) [
                        builder.createString('assetName'),
                        builder.createString('description'),
                        builder.createString('id')
                ]),
                0
        )

        int fieldOffset = builder.createSortedVectorOfTables(new FBSFieldResult(),
                FBSFieldResult.createFBSFieldResult(builder,
                        builder.createString('assetName'),
                        builder.createString('originalValue'),
                        builder.createString('value'),
                        builder.createString('init'),
                        builder.createString('String')
                ),
                FBSFieldResult.createFBSFieldResult(builder,
                        builder.createString('description'),
                        builder.createString('originalValue'),
                        builder.createString('value'),
                        builder.createString('init'),
                        builder.createString('String')
                )
        )

        int tagsOffset = FBSTagResults.createFBSTagResults(builder,
                FBSTagResults.createAddVector(builder,
                        (int[]) [
                                builder.createString('Tag I'),
                                builder.createString('Tag II')
                        ]
                ),

                FBSTagResults.createAddVector(builder,
                        (int[]) [
                                builder.createString('Tag III'),
                        ]
                ),
                builder.createSortedVectorOfTables(new FBSTagResults(),
                        (int[]) [
                                FBSTagReplace.createFBSTagReplace(builder,
                                        builder.createString('TAG V'),
                                        builder.createString('TAG X')
                                ),
                        ]
                )
        )

        int[] dataOffset = [
                FBSRowResult.createFBSRowResult(builder,
                        builder.createString('Insert'),
                        1,
                        1,
                        false,
                        false,
                        FBSRowResult.createErrorsVector(builder, (int[]) [builder.createString('Quick Error Example')]),
                        false,
                        fieldOffset,
                        builder.createString('Application'),
                        FBSRowResult.createCommentsVector(builder, (int[]) [builder.createString('Comment 1'), builder.createString('Comment 2')]),
                        tagsOffset
                )
        ]

        domains[1] = FBSDomainResult.createFBSDomainResult(
                builder,
                builder.createString('Device'),
                FBSDomainResult.createFieldNamesVector(builder, (int[]) [
                        builder.createString('assetName'),
                        builder.createString('description'),
                        builder.createString('id')
                ]),
                FBSDomainResult.createDataVector(builder, dataOffset)
        )

        int processorResult = FBSETLProcessorResult.createFBSETLProcessorResult(
                builder,
                FBSETLInfo.createFBSETLInfo(builder, builder.createString('300000-rows-gson-Uxg4l2UwNQkwtjKxSjeSJgRQN63TC8i7.json')),
                builder.createString('[...  console content ...]'),
                FBSDomainResult.createDataVector(builder, domains),
                12
        )

        builder.finish(processorResult)

        ByteBuffer buf = builder.dataBuffer();
        FBSETLProcessorResult result = FBSETLProcessorResult.getRootAsFBSETLProcessorResult(buf)

        def (String filename, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile('flatbuffers-', 'txt')
        fileOutputStream << builder.sizedInputStream()
        fileOutputStream.flush()
        fileOutputStream.close()

        renderAsJson([filename: filename])
    }
}