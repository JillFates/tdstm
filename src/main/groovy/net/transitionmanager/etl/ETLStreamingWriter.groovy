package net.transitionmanager.etl

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.google.gson.JsonObject
import com.tdsops.etl.DomainOnDisk
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldResult
import com.tdsops.etl.FindResult
import com.tdsops.etl.QueryResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults
import com.tdssrc.grails.TimeUtil
import groovy.transform.CompileStatic

import java.text.SimpleDateFormat

/**
 * <p>Streaming reader solution for {@link ETLProcessorResult},
 * This Writer class can convert to a JSON file, 2 different parts from {@link ETLProcessorResult}.</p>
 * <p>This solution splits {@link ETLProcessorResult} in different files.
 * One for the header, and one for each different data list in {@link DomainResult#data}
 * </p>
 * <BR/>
 * <p>1) Saves data part of each {@link ETLProcessorResult#domains} in JSON format: </p>
 * <pre>
 *   List tmpFile = fileSystemService.createTemporaryFile(PROCESSED_FILE_PREFIX, 'json')
 *   String outputFilename = tmpFile[0]
 *   OutputStream outputStream = (OutputStream) tmpFile[1]
 *   new ETLStreamingWriter(outputStream).writeETLResultsData(domain.data)
 * </pre>
 * <p>At the same time, it populates 2 fields in {@link ETLProcessorResult}:</p>
 *  <ul>
 *      <li>{@link DomainOnDisk#outputFilename}: defines where {@link ETLStreamingWriter}
 *          saves each {@link DomainResult#getData()} list.
 *      <li>{@link DomainResult#dataSize}: defines the amount of rows from {@link DomainResult#getData()}
 *          were saved
 *  </ul
 * <BR>
 * <p>2) Saves one part of {@link ETLProcessorResult} as <tt>header</tt> in JSON format:</p>
 * <pre>
 *  List tmpFile = fileSystemService.createTemporaryFile(PROCESSED_FILE_PREFIX, 'json')
 *  String outputFilename = tmpFile[0]
 *  OutputStream os = (OutputStream) tmpFile[1]
 *  new ETLStreamingWriter(os).writeETLResultsHeader(processorResult)
 * </pre>
 *
 * @see net.transitionmanager.imports.ScriptProcessorService#saveDomainDataUsingStreaming
 */
@CompileStatic
class ETLStreamingWriter {

    OutputStream outputStream
    JsonGenerator generator

    static SimpleDateFormat dateFormat = new SimpleDateFormat('')

    ETLStreamingWriter(OutputStream outputStream) {
        this.outputStream = outputStream
        generator = new JsonFactory()
                .createGenerator(outputStream, JsonEncoding.UTF8)
    }

    void startDataArray() {
        generator.writeStartArray()
    }

    void endDataArray() {
        generator.writeEndArray()
        close()
    }


    /**
     * Writes in {@link ETLStreamingWriter#outputStream} field,
     * the main structure from {@link ETLProcessorResult}.
     * <pre>
     *  List tmpFile = fileSystemService.createTemporaryFile(PROCESSED_FILE_PREFIX, 'json')
     *  String outputFilename = tmpFile[0]
     *  OutputStream os = (OutputStream) tmpFile[1]
     *  new ETLStreamingWriter(os).writeETLResultsHeader(processorResult)
     * </pre>
     * @param result an instance of {@link ETLStreamingWriter#outputStream}
     */
    void writeETLResultsHeader(ETLProcessorResult result) {
        generator.writeStartObject()
        writeETLInfo(result.ETLInfo)
        generator.writeStringField('consoleLog', result.consoleLog)
        generator.writeNumberField('version', result.version)
        writeDomains(result.domains)
        generator.writeEndObject()
        close()
    }
    /**
     * <p>Writes in {@link ETLStreamingWriter#outputStream} field,
     * a list of fields from {@link DomainResult#data}.</p>
     * <p>Iterating {@link DomainResult#data}, it creates a new entry in
     * {@link ETLStreamingWriter#outputStream} field following
     * {@link ETLStreamingWriter#writeRowResult} field</p>
     *
     * @param data {@link DomainResult#data} list
     */
    void writeETLResultsData(List<RowResult> data) {
        writeDataArray(data)
        close()
    }

    void close() {
        generator.close()
        outputStream.flush()
        outputStream.close()
    }
    /**
     * Writes in {@link ETLStreamingWriter#outputStream}
     * fields from {@link DomainResult}.
     *
     * @param domains
     */
    private void writeDomains(List<DomainResult> domains) {

        generator.writeFieldName('domains')
        generator.writeStartArray()
        for (DomainResult domainResult in domains) {
            writeDomains((DomainOnDisk) domainResult)
        }
        generator.writeEndArray()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream}
     * fields from {@link DomainResult}.
     *
     * @param domain
     */
    private void writeDomains(DomainOnDisk domain) {
        generator.writeStartObject()
        generator.writeStringField('domain', domain.domain)
        generator.writeStringField('outputFilename', domain.outputFilename)
        generator.writeNumberField('dataSize', domain.dataSize)
        writeStringMapField('fieldLabelMap', domain.fieldLabelMap)
        writeStringCollectionField('fieldNames', domain.fieldNames)
        generator.writeEndObject()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} fields from {@link RowResult}.
     *
     * @param data
     */
    private void writeDataArray(List<RowResult> data) {
        generator.writeStartArray()
        for (RowResult rowResult in data) {
            writeRowResult(rowResult)
        }
        generator.writeEndArray()
    }
    /**
     * Writes in {@link ETLStreamingWriter#outputStream} fields from {@link RowResult}.
     *
     * @param rowResult
     */
    void writeRowResult(RowResult rowResult) {
        generator.writeStartObject()
        writeStringCollectionField('errors', rowResult.errors)
        generator.writeNumberField('rowNum', rowResult.rowNum)
        generator.writeNumberField('errorCount', rowResult.errorCount)
        generator.writeBooleanField('warn', rowResult.warn)
        generator.writeBooleanField('duplicate', rowResult.duplicate)
        generator.writeStringField('op', rowResult.op.toString())
        writeFields(rowResult.fields)
        generator.writeStringField('domain', rowResult.domain)
        writeStringCollectionField('comments', rowResult.comments)
        if (rowResult.tags) writeTagResults(rowResult.tags)
        generator.writeEndObject()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} fields from {@link FieldResult}
     * assigned to a fieldName in {@link RowResult#fields} Map.
     *
     * @param fieldName
     * @param fieldResult
     */
    private void writeFieldResult(String fieldName, FieldResult fieldResult) {
        generator.writeFieldName(fieldName)
        generator.writeStartObject()

        generator.writeNumberField('fieldOrder', fieldResult.fieldOrder)
        generator.writeBooleanField('warn', fieldResult.warn)
        writeStringCollectionField('errors', fieldResult.errors)

        writeObjectField('init', fieldResult.init)
        writeObjectField('originalValue', fieldResult.originalValue)
        writeObjectField('value', fieldResult.value)

        if (fieldResult.find) writeFind(fieldResult.find)
        if (fieldResult.create) writeObjectMapField('create', fieldResult.create)
        if (fieldResult.update) writeObjectMapField('update', fieldResult.update)

        generator.writeEndObject()
    }

    /**
     * <p>Overrides {@link JsonGenerator#writeObjectField(java.lang.String, java.lang.Object)} for not supported data types.</p>
     * <p>At the moment we are serializing {@link FieldResult#init}, {@link FieldResult#value} and {@link FieldResult#originalValue}
     * we need to detect if data type is supported by {@link JsonGenerator}.
     * If that is not the case we are converting Object field using toString() method.
     * After that, DataImport can detect and convert it smartly based on Domain data types.</p>
     *
     * @param fieldName a field name
     * @param pojo an Object to be serialized.
     */
    private void writeObjectField(String fieldName, Object pojo) {

        switch (pojo?.class) {
            case Date:
                generator.writeStringField(fieldName, TimeUtil.formatToISO8601DateTime((Date) pojo))
                break
            case Map:
            case JsonObject:
            case List:
            case Set:
                generator.writeStringField(fieldName, pojo.toString())
                break
            default:
                generator.writeObjectField(fieldName, pojo)
        }
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} fields from {@link FindResult}.
     *
     * @param findResult
     */
    private void writeFind(FindResult findResult) {
        generator.writeFieldName('find')
        generator.writeStartObject()
        generator.writeObjectField('matchOn', findResult.matchOn)
        generator.writeNumberField('size', findResult.size)
        writeNumberCollectionField('results', findResult.results)
        writeQueryList(findResult.query)
        generator.writeEndObject()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} fields from {@link QueryResult#criteria} List.
     *
     * @param criteria
     */
    private void writeCriteria(Map<String, Object> criteria) {
        generator.writeStartObject()
        generator.writeStringField('propertyName', criteria.propertyName?.toString())
        generator.writeStringField('operator', criteria.operator?.toString())
        generator.writeObjectField('value', criteria.value)
        generator.writeEndObject()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} {@link QueryResult#criteria} List.
     *
     * @param criteriaList
     */
    private void writeCriteriaList(List<Map<String, Object>> criteriaList) {
        generator.writeFieldName('criteria')
        generator.writeStartArray()
        for (Map<String, Object> criteria in criteriaList) {
            writeCriteria(criteria)
        }
        generator.writeEndArray()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} fields from {@link QueryResult}.
     *
     * @param queryResult
     */
    private void writeQuery(QueryResult queryResult) {

        generator.writeStartObject()
        generator.writeStringField('domain', queryResult.domain)
        writeCriteriaList(queryResult.criteria)
        generator.writeEndObject()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} a List of from {@link QueryResult}.
     *
     * @param queryResults
     */
    private void writeQueryList(List<QueryResult> queryResults) {
        generator.writeFieldName('query')
        generator.writeStartArray()
        for (QueryResult queryResult in queryResults) {
            writeQuery(queryResult)
        }
        generator.writeEndArray()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} a Map with {@link FieldResult}.
     *
     * @param fields
     */
    private void writeFields(Map<String, FieldResult> fields) {
        generator.writeFieldName('fields')
        generator.writeStartObject()

        for (Map.Entry<String, FieldResult> fieldResultEntry in fields) {
            writeFieldResult(fieldResultEntry.key, fieldResultEntry.value)
        }
        generator.writeEndObject()
    }

    /**
     * Writes in {@link ETLStreamingWriter#outputStream} {@link TagResults} field.
     *
     * @param tagResults
     */
    private void writeTagResults(TagResults tagResults) {
        generator.writeFieldName('tags')
        generator.writeStartObject()
        writeStringCollectionField('add', tagResults.add)
        writeStringCollectionField('remove', tagResults.remove)
        writeStringMapField('replace', tagResults.replace)
        generator.writeEndObject()
    }

    /**
     * Writes a {@code Collection<Long>} values in {@link ETLStreamingWriter#outputStream}.
     *
     * @param fieldName
     * @param values
     */
    private void writeNumberCollectionField(String fieldName, Collection<Long> values) {
        generator.writeFieldName(fieldName)
        generator.writeStartArray()
        for (String value in values) {
            generator.writeNumber(value)
        }

        generator.writeEndArray()
    }

    /**
     * Writes a {@code Collection<String>} values in {@link ETLStreamingWriter#outputStream}.
     *
     * @param fieldName
     * @param values
     */
    private void writeStringCollectionField(String fieldName, Collection<String> values) {
        generator.writeFieldName(fieldName)
        generator.writeStartArray()
        for (String value in values) {
            generator.writeString(value)
        }

        generator.writeEndArray()
    }

    /**
     * Writes a {@code Map<String, String>} Map in {@link ETLStreamingWriter#outputStream}.
     *
     * @param fieldName
     * @param map
     */
    private void writeStringMapField(String fieldName, Map<String, String> map) {
        generator.writeFieldName(fieldName)
        generator.writeStartObject()
        for (Map.Entry<String, String> entry in map) {
            generator.writeStringField(entry.key, entry.value)
        }
        generator.writeEndObject()
    }

    /**
     * Writes a {@code Map<String, Object>} Map in {@link ETLStreamingWriter#outputStream}.
     *
     * @param fieldName
     * @param map
     */
    private void writeObjectMapField(String fieldName, Map<String, Object> map) {
        generator.writeFieldName(fieldName)
        generator.writeStartObject()
        for (Map.Entry<String, Object> entry in map) {
            generator.writeObjectField(entry.key, entry.value)
        }
        generator.writeEndObject()
    }

    /**
     * Writes a {@link ETLProcessorResult#ETLInfo} in {@link ETLStreamingWriter#outputStream}.
     *
     * @param ETLInfo
     */
    private void writeETLInfo(Map<String, ?> ETLInfo) {
        generator.writeFieldName('ETLInfo')
        generator.writeStartObject()
        generator.writeStringField('originalFilename', ETLInfo.originalFilename?.toString())
        generator.writeEndObject()
    }
}
