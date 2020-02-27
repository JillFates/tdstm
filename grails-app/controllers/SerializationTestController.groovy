import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.google.flatbuffers.FlatBufferBuilder
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.fbs.FBSCreate
import net.transitionmanager.fbs.FBSDomainResult
import net.transitionmanager.fbs.FBSFieldLabelMap
import net.transitionmanager.fbs.FBSFieldResult
import net.transitionmanager.fbs.FBSFindResult
import net.transitionmanager.fbs.FBSInfo
import net.transitionmanager.fbs.FBSProcessorResult
import net.transitionmanager.fbs.FBSQueryResult
import net.transitionmanager.fbs.FBSQueryResultCriteria
import net.transitionmanager.fbs.FBSRowResult
import net.transitionmanager.fbs.FBSTagReplace
import net.transitionmanager.fbs.FBSTagResults
import net.transitionmanager.fbs.FBSUpdate
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

    def jackson(){

        int rowsAmount = params.rows ? params.rows.toInteger() : 10000
        Map<String, ?> exampleResponse = buildModel(rowsAmount)

        def (String filename, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile(rowsAmount + '-rows-jakson-', 'json')

        //ByteArrayOutputStream stream = new ByteArrayOutputStream()
        JsonFactory jfactory = new JsonFactory()
        JsonGenerator jGenerator = jfactory
                .createGenerator(fileOutputStream, JsonEncoding.UTF8)

        jGenerator.writeStartObject()
        jGenerator.writeNumberField("version", exampleResponse.version)
        jGenerator.writeFieldName('rows')
        jGenerator.writeStartArray()
        for (Map map in exampleResponse.rows){

            jGenerator.writeStartObject()
            jGenerator.writeStringField('domain', map.domain)
            jGenerator.writeNumberField('errorCount', map.errorCount)
            jGenerator.writeNumberField('rowNum', map.rowNum)
            jGenerator.writeEndObject()
        }
        jGenerator.writeEndArray()
        jGenerator.writeEndObject()
        jGenerator.close()

        fileOutputStream.flush()
        fileOutputStream.close()

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
                0,
                0
        )

        int queryOffset = FBSFindResult.createQueryVector(builder,
                (int[]) [
                        FBSQueryResult.createFBSQueryResult(builder,
                                builder.createString('Application'),
                                FBSQueryResult.createCriteriaVector(builder,
                                        (int[]) [
                                                FBSQueryResultCriteria.createFBSQueryResultCriteria(builder,
                                                        builder.createString('propertyName'),
                                                        builder.createString('eq'),
                                                        builder.createString('zulu01'),
                                                        builder.createString('String')
                                                )
                                        ]
                                )

                        )
                ],
        )

        int findOffset = FBSFindResult.createFBSFindResult(builder,
                queryOffset,
                FBSFindResult.createResultsVector(builder, (long[]) [1l]),
                0,
                0,

        )

        int createOffset = builder.createSortedVectorOfTables(new FBSUpdate(),
                (int[]) [
                        FBSCreate.createFBSCreate(builder,
                                builder.createString('assetName'),
                                builder.createString('ACMEVMPROD01'),
                                builder.createString('String')
                        ),
                        FBSUpdate.createFBSUpdate(builder,
                                builder.createString('assetType'),
                                builder.createString('VM'),
                                builder.createString('String')
                        )
                ]
        )

        int updateOffset = builder.createSortedVectorOfTables(new FBSUpdate(),
                (int[]) [
                        FBSUpdate.createFBSUpdate(builder,
                                builder.createString('assetName'),
                                builder.createString('ACMEVMPROD01'),
                                builder.createString('String')
                        )
                ]
        )


        int fieldResultOffset = FBSFieldResult.createFBSFieldResult(builder,
                builder.createString('assetName'),
                builder.createString('originalValue'),
                builder.createString('value'),
                builder.createString('init'),
                builder.createString('String'),
                1,
                FBSRowResult.createErrorsVector(builder, (int[]) [builder.createString('Quick Error Example')]),
                true,
                findOffset,
                createOffset,
                updateOffset
        )


        int fieldOffset = builder.createSortedVectorOfTables(new FBSFieldResult(),
                (int[]) [fieldResultOffset],
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

        int fieldsLabelMapOffset = builder.createSortedVectorOfTables(new FBSFieldLabelMap(),
                (int[]) [
                        FBSFieldLabelMap.createFBSFieldLabelMap(builder,
                                builder.createString('id'),
                                builder.createString('Id')
                        ),
                        FBSFieldLabelMap.createFBSFieldLabelMap(builder,
                                builder.createString('assetName'),
                                builder.createString('Name')
                        )
                ]
        )

        domains[1] = FBSDomainResult.createFBSDomainResult(
                builder,
                builder.createString('Device'),
                FBSDomainResult.createFieldNamesVector(builder, (int[]) [
                        builder.createString('assetName'),
                        builder.createString('description'),
                        builder.createString('id')
                ]),
                fieldsLabelMapOffset,
                FBSDomainResult.createDataVector(builder, dataOffset)
        )

        int processorResult = FBSProcessorResult.createFBSProcessorResult(
                builder,
                FBSInfo.createFBSInfo(builder, builder.createString('300000-rows-gson-Uxg4l2UwNQkwtjKxSjeSJgRQN63TC8i7.json')),
                builder.createString('[...  console content ...]'),
                FBSDomainResult.createDataVector(builder, domains),
                12
        )

        builder.finish(processorResult)

        FBSProcessorResult result = FBSProcessorResult.getRootAsFBSProcessorResult(builder.dataBuffer())
        for (int i = 0; i < result.domainsLength(); i++) {
            FBSDomainResult domain = result.domains(i)
            domain.domain() == 'Application'
        }


        def (String filename, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile('flatbuffers-', 'txt')
        fileOutputStream << builder.sizedInputStream()
        fileOutputStream.flush()
        fileOutputStream.close()

        renderAsJson([filename: filename])
    }
}

