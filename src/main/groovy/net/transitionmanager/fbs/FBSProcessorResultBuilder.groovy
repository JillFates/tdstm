package net.transitionmanager.fbs

import com.google.flatbuffers.FlatBufferBuilder
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldResult
import com.tdsops.etl.FindResult
import com.tdsops.etl.QueryResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults

import java.nio.ByteBuffer

/**
 * Class to convert an instance of {@code ETLProcessorResult} and a binary serialized instance of {@code}
 */
class FBSProcessorResultBuilder {

    ETLProcessorResult result
    FlatBufferBuilder builder

    FBSProcessorResultBuilder(ETLProcessorResult result) {
        this.result = result
        this.builder = new FlatBufferBuilder(1024)
    }

    /**
     * Build {@code FlatBufferBuilder} content from {@code ETLProcessorResult}.
     * <pre>
     *  ByteBuffer dataBuffer = new FBSProcessorResultBuilder(processorResult).buildDataBuffer()
     *  FBSProcessorResult serialized = FBSProcessorResult.getRootAsFBSProcessorResult(dataBuffer)
     * </pre>
     *
     * @return serialized instance in {@code ByteBuffer}
     */
    ByteBuffer buildDataBuffer() {
        return serializeETLProcessorResult().dataBuffer()
    }

    /**
     * Build {@code FlatBufferBuilder} content from {@code ETLProcessorResult}
     * and returns serialized results in an {@code InputStream}
     * <pre>
     *  InputStream serialized = new FBSProcessorResultBuilder(processorResult).buildInputStream()
     *  def (String filename, OutputStream fileOutputStream) = fileSystemService.createTemporaryFile('flatbuffers-', 'binary')
     *  fileOutputStream << builder.sizedInputStream()
     *  fileOutputStream.flush()
     *  fileOutputStream.close()
     * </pre>
     * @return serialized instance in an {@code InputStream}
     */
    InputStream buildInputStream() {
        return serializeETLProcessorResult().sizedInputStream()
    }

    /**
     * Builds an instance of auto-generated model in {@code FBSProcessorResult}
     * using a {@code FlatBufferBuilder} instance.
     * <pre>
     *  FBSProcessorResult serialized = new FBSProcessorResultBuilder(processorResult).build()
     *  serialized.domains(0).domain() == ETLDomain.Application.name()
     * </pre>
     *
     * @return an instance of {@code FBSProcessorResult}
     */
    FBSProcessorResult build() {
        return FBSProcessorResult.getRootAsFBSProcessorResult(buildDataBuffer())
    }

    /**
     * Private method to serialized an instance of {@code ETLProcessorResult}
     * saved in {@code FBSProcessorResultBuilder#result} field using an instance of
     * {@code FlatBufferBuilder} saved in {@code FBSProcessorResultBuilder#builder} field.
     * @return {@code FBSProcessorResultBuilder#builder}
     */
    private FlatBufferBuilder serializeETLProcessorResult() {
        int processorResult = FBSProcessorResult.createFBSProcessorResult(
                builder,
                buildETLInfoOffset(result),
                buildConsoleLogOffset(result),
                buildDomainResultVector(result),
                result.version
        )

        builder.finish(processorResult)

        return builder
    }

    /**
     * Creates an offset in FlatBuffers table for {@code DomainResult#fieldLabelMap}
     * from an instance of {@code DomainResult}
     * It converts this part of the {@code ETLProcessorResult} in JSON format:
     * <pre>
     *  "fieldLabelMap": { //
     *      "assetName": "Name",
     *      "description": "Description",
     *      "assetType": "Device Type",
     *      "id": "Id"
     * </pre>
     * @param domainResult
     * @return an offset defines in FlatBuffers table for sorted vector of {@code FBSFieldLabelMap}
     */
    int buildFieldLabelMapOffset(DomainResult domainResult) {

        int[] offsets = new int[domainResult.fieldLabelMap.size()]
        int index = 0
        for (Map.Entry<String, String> entry in domainResult.fieldLabelMap) {
            offsets[index++] = FBSFieldLabelMap.createFBSFieldLabelMap(builder,
                    builder.createString(entry.key),
                    builder.createString(entry.value)
            )
        }

        return builder.createSortedVectorOfTables(new FBSFieldLabelMap(), offsets)
    }

    /**
     * <p>Builds this part of the model that represents the following part
     * in the ETLProcessorResult JSON structure:</p>
     * <pre>
     *{ //
     *     "comments": [],
     *     "duplicate": false,
     *     "errorCount": 0,
     *     "errors": [],
     *     "fields": {},
     *     "op": "Insert",
     *     "rowNum": 1,
     *     "warn": false,
     *     "tags": null
     * </pre>
     * @param domainResult
     * @return an offset defines in FlatBuffers table for an instance of {@code FBSRowResult}
     * @see FBSProcessorResultBuilder#buildFieldResultOffset(java.util.Map)
     * @see FBSProcessorResultBuilder#buildVectorFromCollection(java.util.Collection)
     */
    int[] buildDataVectorOffset(DomainResult domainResult) {

        int[] dataOffset = new int[domainResult.data.size()]
        for (int i = 0; i < domainResult.data.size(); i++) {

            RowResult rowResult = domainResult.data[i]
            dataOffset[i] = FBSRowResult.createFBSRowResult(builder,
                    builder.createString(rowResult.op),
                    rowResult.rowNum,
                    rowResult.errorCount,
                    rowResult.warn,
                    rowResult.duplicate,
                    FBSRowResult.createErrorsVector(builder, buildVectorFromCollection(rowResult.errors)),
                    rowResult.ignore,
                    builder.createSortedVectorOfTables(new FBSFieldResult(), buildFieldResultOffset(rowResult.fields)),
                    builder.createString(rowResult.domain),
                    FBSRowResult.createCommentsVector(builder, buildVectorFromCollection(rowResult.comments)),
                    rowResult.tags ? buildTagsOffset(rowResult.tags) : 0
            )
        }
        return dataOffset
    }
    /**
     * <p>Builds this part of the model that represents the following part
     * in the ETLProcessorResult JSON structure:</p>
     * <pre>
     *     "fields": { //
     *         "assetName": { //
     *         "create": null,
     *         "errors": [],
     *         "fieldOrder": 0,
     *         "find": { //
     *             "matchOn": null,
     *             "query": [],
     *             "results": [],
     *             "size": 0
     *         //}, //
     *         "init": null,
     *         "originalValue": "Application 2",
     *         "update": null,
     *         "value": "Application 2",
     *         "warn": false
     *     //},//
     * </pre>
     * @param fields
     * @return an offset defines in FlatBuffers table for instances of {@code FieldResult}
     */
    int[] buildFieldResultOffset(Map<String, FieldResult> fields) {

        int[] fieldResultOffset = new int[fields.size()]
        int index = 0

        for (Map.Entry<String, FieldResult> entry in fields) {
            FieldResult fieldResult = entry.value

            fieldResultOffset[index++] = FBSFieldResult.createFBSFieldResult(builder,
                    builder.createString(entry.key),
                    fieldResult.originalValue ? builder.createString(fieldResult.originalValue.toString()) : 0,
                    fieldResult.value ? builder.createString(fieldResult.value.toString()) : 0,
                    fieldResult.init ? builder.createString(fieldResult.init.toString()) : 0,
                    builder.createString(calculateValueClass(fieldResult.value)),
                    fieldResult.fieldOrder,
                    FBSRowResult.createErrorsVector(builder, buildVectorFromCollection(fieldResult.errors)),
                    fieldResult.warn,
                    fieldResult.find ? buildFindOffset(fieldResult.find) : 0,
                    fieldResult.create ? buildCreateOffset(fieldResult.create) : 0,
                    fieldResult.update ? buildUpdateOffset(fieldResult.update) : 0
            )
        }

        return fieldResultOffset
    }

    /**
     * <p>Builds this part of the model that represents the following part
     * in the ETLProcessorResult JSON structure:</p>
     * <pre>
     *    "find": { //
     *       "query": [//
     *                //{ //
     *                    "domain": "Application",
     *                    "criteria": [ //
     *                        //{ //
     *                            "propertyName": "assetName",
     *                            "operator": "eq",
     *                            "value": "Application 2"
     *                        //}//
     *               //]//
     *              //}//
     *        //],//
     *        "matchOn": 1,
     *        "results": [12346789],
     *       "size": 1
     * </pre>
     * @param findResult
     * @return an offset defines in FlatBuffers table for an instance of {@code FBSFindResult}
     */
    int buildFindOffset(FindResult findResult) {

        int[] data = new int[findResult.query.size()]
        for (int i = 0; i < findResult.query.size(); i++) {
            QueryResult queryResult = findResult.query[i]

            int[] queryResultDataOffset = new int[queryResult.criteria.size()]
            for (int j = 0; j < queryResult.criteria.size(); j++) {
                Map<String, Object> criteria = queryResult.criteria[j]
                queryResultDataOffset[j] = FBSQueryResultCriteria.createFBSQueryResultCriteria(builder,
                        builder.createString(criteria.propertyName),
                        builder.createString(criteria.operator),
                        builder.createString(criteria.value?.toString()),
                        builder.createString(calculateValueClass(criteria.value))
                )

            }
            data[i] = FBSQueryResult.createFBSQueryResult(builder,
                    builder.createString(queryResult.domain),
                    FBSQueryResult.createCriteriaVector(builder, queryResultDataOffset)
            )
        }

        return FBSFindResult.createFBSFindResult(builder,
                FBSFindResult.createQueryVector(builder, data),
                FBSFindResult.createResultsVector(builder, (long[]) findResult.results),
                findResult.size ?: 0,
                findResult.matchOn ?: 0
        )
    }

    /**
     * Retrieves {@code Class#simpleName} from an Object.
     * @param object
     * @return simpleName from object param Class
     */
    String calculateValueClass(Object object) {
        return object?.class?.simpleName
    }
    /**
     *
     * @param create
     * @return
     */
    int buildCreateOffset(Map<String, Object> create) {

        int[] offsets = new int[create.size()]
        int index = 0
        for (Map.Entry<String, Object> entry in create) {
            offsets[index++] = FBSCreate.createFBSCreate(builder,
                    builder.createString(entry.key),
                    builder.createString(entry.value?.toString()),
                    builder.createString(calculateValueClass(entry.value))
            )
        }

        return builder.createSortedVectorOfTables(new FBSCreate(), offsets)
    }

    /**
     *
     * @param update
     * @return
     */
    int buildUpdateOffset(Map<String, Object> update) {

        int[] offsets = new int[update.size()]
        int index = 0
        for (Map.Entry<String, Object> entry in update) {
            offsets[index++] = FBSUpdate.createFBSUpdate(builder,
                    builder.createString(entry.key),
                    builder.createString(entry.value?.toString()),
                    builder.createString(calculateValueClass(entry.value))
            )
        }

        return builder.createSortedVectorOfTables(new FBSUpdate(), offsets)
    }

    /**
     * Creates an offset in FlatBuffers table for {@code FBSTagResults}
     * from an instance of {@code TagResults}
     *
     * <pre>
     *  "tags": { //
     * 	    "add":["GDPR","PCI","SOX"],
     * 		"remove":["HIPPA","PCI","SOX"],
     * 		"replace":{ //
     * 		    "PCI":"PCI",
     * 		    "HIPPA":"SOX",
     * 			"GDPR":"PCI"
     * </pre>
     * @param tagResults
     * @return an offset defines in FlatBuffers table for an instance of {@code FBSTagResults}
     */
    int buildTagsOffset(TagResults tagResults) {

        int addOffset = tagResults.add ? FBSTagResults.createAddVector(builder, buildVectorFromCollection(tagResults.add)) : 0
        int removeOffset = tagResults.remove ? FBSTagResults.createAddVector(builder, buildVectorFromCollection(tagResults.remove)) : 0
        int replaceOffset = tagResults.replace ? builder.createSortedVectorOfTables(new FBSTagResults(),
                (int[]) tagResults.replace.collect { Map.Entry<String, String> entry ->
                    FBSTagReplace.createFBSTagReplace(builder,
                            builder.createString(entry.key),
                            builder.createString(entry.value)
                    )
                }
        ) : 0

        return FBSTagResults.createFBSTagResults(builder,
                addOffset,
                removeOffset,
                replaceOffset
        )
    }


    /**
     * It creates an array of offsets in FlatBuffers for a List of String.
     *
     * @param values a List of String
     * @return an offset defines in FlatBuffers for a Vector of Strings
     */
    int[] buildVectorFromCollection(Collection<String> values) {
        return values.collect { builder.createString(it) }
    }

    /**
     * Creates an offset in FlatBuffers table for {@code DomainResult#fieldNames}
     * from an instance of {@code DomainResult}
     *
     * @param builder
     * @param domainResult
     * @return an offset defines in FlatBuffers table for an instance of {@code FBSDomainResult}
     */
    int buildFieldNamesOffset(DomainResult domainResult) {

        int[] fieldNamesVector = new int[domainResult.fieldNames.size()]

        for (int i = 0; i < domainResult.fieldNames.size(); i++) {
            fieldNamesVector[i] = builder.createString(domainResult.fieldNames[i])
        }

        return FBSDomainResult.createFieldNamesVector(builder, fieldNamesVector)
    }

    /**
     *
     * @param builder
     * @param result
     * @return an offset defines in FlatBuffers table for an instance of {@code FBSDomainResult#createDataVector}
     */
    int buildDomainResultVector(ETLProcessorResult result) {
        int[] domains = new int[result.domains.size()]
        int index = 0
        for (DomainResult domainResult in result.domains) {

            domains[index++] = FBSDomainResult.createFBSDomainResult(
                    builder,
                    builder.createString(domainResult.domain),
                    buildFieldNamesOffset(domainResult),
                    buildFieldLabelMapOffset(domainResult),
                    FBSDomainResult.createDataVector(builder, buildDataVectorOffset(domainResult))
            )
        }

        return FBSDomainResult.createDataVector(builder, domains)
    }

    /**
     * Creates an offset in FlatBuffers table for {@code ETLProcessorResult#ETLInfo}
     * @param builder
     * @param result
     * @return an offset defines in FlatBuffers table
     */
    int buildETLInfoOffset(ETLProcessorResult result) {
        return FBSInfo.createFBSInfo(builder, builder.createString(result.ETLInfo.originalFilename))
    }

    /**
     * Creates an offset in FlatBuffers table for {@code ETLProcessorResult#consoleLog}
     * @param builder
     * @param result
     * @return an offset defines in FlatBuffers table
     */
    int buildConsoleLogOffset(ETLProcessorResult result) {
        return result.consoleLog ? builder.createString(result.consoleLog) : 0
    }
}
