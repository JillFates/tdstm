package net.transitionmanager.fbs

import com.google.flatbuffers.FlatBufferBuilder
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldResult
import com.tdsops.etl.FindResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults

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

    FBSProcessorResult build() {

        int processorResult = FBSProcessorResult.createFBSProcessorResult(
                builder,
                buildETLInfoOffset(result),
                buildConsoleLogOffset(result),
                buildDomainResultVector(result),
                result.version
        )

        builder.finish(processorResult)

        return FBSProcessorResult.getRootAsFBSProcessorResult(builder.dataBuffer())
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
     * @return @return an offset defines in FlatBuffers table for an instance of {@code FBSRowResult}
     * @see FBSProcessorResultBuilder#buildFieldResultOffset(java.util.Map)
     * @see FBSProcessorResultBuilder#buildVectorFromCollection(java.util.Collection)
     */
    int[] buildDataVectorOffset(DomainResult domainResult) {

        int[] dataOffset = new int[domainResult.data.size()]
        for (int i = 0; i < domainResult.data.size(); i++) {

            RowResult rowResult = domainResult.data[0]
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
     *
     * @param fields
     * @return
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
                    builder.createString(fieldResult.value?.class?.simpleName),
                    fieldResult.fieldOrder,
                    FBSRowResult.createErrorsVector(builder, buildVectorFromCollection(fieldResult.errors)),
                    fieldResult.warn,
                    buildFindOffset(fieldResult.find),
                    buildCreateOffset(fieldResult.create),
                    buildUpdateOffset(fieldResult.update)
            )
        }

        return fieldResultOffset
    }

    int buildFindOffset(FindResult findResult) {
        return 0
    }

    int buildCreateOffset(Map<String, Object> create) {
        return 0
    }

    int buildUpdateOffset(Map<String, Object> update) {
        return 0
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
