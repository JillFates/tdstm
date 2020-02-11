package net.transitionmanager.fbs

import com.google.flatbuffers.FlatBufferBuilder
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLProcessorResult

class ETLFlatBuffersMapper {

    ETLProcessorResult result

    ETLFlatBuffersMapper(ETLProcessorResult result) {
        this.result = result
    }

    FlatBufferBuilder build() {

        FlatBufferBuilder builder = new FlatBufferBuilder(1024)

        int processorResult = FBSETLProcessorResult.createFBSETLProcessorResult(
                builder,
                mapETLInfoOffset(builder, result),
                mapConsoleLogOffset(builder, result),
                mapDomainResultVector(builder, result),
                result.version
        )

        builder.finish(processorResult)

        return builder
    }

    /**
     *
     * @param builder
     * @param domainResult
     * @return
     */
    int mapFieldNamesVector(FlatBufferBuilder builder, DomainResult domainResult) {

        int[] fieldNamesVector = new int[domainResult.fieldNames.size()]
        int index = 0

        for (String fieldName in domainResult.fieldNames) {
            fieldNamesVector[index] = builder.createString(fieldName)
        }

        return FBSDomainResult.createFieldNamesVector(builder, fieldNamesVector)
    }

    /**
     *
     * @param builder
     * @param result
     * @return
     */
    int mapDomainResultVector(FlatBufferBuilder builder, ETLProcessorResult result) {
        int[] domains = new int[result.domains.size()]
        int index = 0
        for (DomainResult domainResult in result.domains) {

            domains[index++] = FBSDomainResult.createFBSDomainResult(
                    builder,
                    builder.createString(domainResult.domain),
                    mapFieldNamesVector(builder, domainResult),
                    0
            )
        }

        return FBSDomainResult.createDataVector(builder, domains)
    }

    /**
     * Creates an offset in Flatbuffers table for {@code ETLProcessorResult#ETLInfo}
     * @param builder
     * @param result
     * @return an offset defines in Flatbuffers table
     */
    int mapETLInfoOffset(FlatBufferBuilder builder, ETLProcessorResult result) {
        return FBSETLInfo.createFBSETLInfo(builder, builder.createString(result.ETLInfo.originalFilename))
    }

    /**
     * Creates an offset in Flatbuffers table for {@code ETLProcessorResult#consoleLog}
     * @param builder
     * @param result
     * @return an offset defines in Flatbuffers table
     */
    int mapConsoleLogOffset(FlatBufferBuilder builder, ETLProcessorResult result) {

        if (result.consoleLog) {
            return builder.createString(result.consoleLog)
        } else {
            return 0
        }
    }
}
