package net.transitionmanager.etl

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldResult
import com.tdsops.etl.FindResult
import com.tdsops.etl.QueryResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.TagResults
import groovy.transform.CompileStatic

@CompileStatic
class JacksonSerializer {

    OutputStream outputStream
    JsonGenerator generator

    JacksonSerializer(OutputStream outputStream) {
        this.outputStream = outputStream
        generator = new JsonFactory()
                .createGenerator(outputStream, JsonEncoding.UTF8)
    }

    void writeETLResultsHeader(ETLProcessorResult result) {
        generator.writeStartObject()
        writeETLInfo(result.ETLInfo)
        generator.writeStringField('consoleLog', result.consoleLog)
        generator.writeNumberField('version', result.version)
        writeDomains(result.domains)
        generator.writeEndObject()
        generator.close()
    }

    void writeETLResultsData(List<RowResult> data) {
        writeDataArray(data)
        generator.close()
    }

    private void writeDomains(List<DomainResult> domains) {

        generator.writeFieldName('domains')
        generator.writeStartArray()
        for (DomainResult domainResult in domains) {
            writeDomains(domainResult)
        }
        generator.writeEndArray()
    }

    private void writeDomains(DomainResult domain) {
        generator.writeStartObject()
        generator.writeStringField('domain', domain.domain)
        writeStringMapField('fieldLabelMap', domain.fieldLabelMap)
        writeStringCollectionField('fieldNames', domain.fieldNames)
        // writeDataArray(domain.data)
        generator.writeEndObject()
    }

    private void writeDataArray(List<RowResult> data) {
        generator.writeStartArray()
        for (RowResult rowResult in data) {
            writeRowResult(rowResult)
        }
        generator.writeEndArray()
    }

    private void writeRowResult(RowResult rowResult) {
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
        writeTagResults(rowResult.tags)
        generator.writeEndObject()
    }

    private writeFieldResult(String fieldName, FieldResult fieldResult) {
        generator.writeFieldName(fieldName)
        generator.writeStartObject()

        generator.writeNumberField('fieldOrder', fieldResult.fieldOrder)
        generator.writeBooleanField('warn', fieldResult.warn)
        writeStringCollectionField('errors', fieldResult.errors)

        generator.writeObjectField('init', fieldResult.init)
        generator.writeObjectField('originalValue', fieldResult.originalValue)
        generator.writeObjectField('value', fieldResult.value)

        if (fieldResult.find) writeFind(fieldResult.find)
        if (fieldResult.create) writeObjectMapField('create', fieldResult.create)
        if (fieldResult.update) writeObjectMapField('update', fieldResult.update)

        generator.writeEndObject()
    }

    private writeFind(FindResult findResult) {
        generator.writeFieldName('find')
        generator.writeStartObject()
        generator.writeObjectField('matchOn', findResult.matchOn)
        generator.writeNumberField('size', findResult.size)
        writeNumberCollectionField('results', findResult.results)
        writeQueryList(findResult.query)
        generator.writeEndObject()
    }

    private void writeCriteria(Map<String, Object> criteria) {
        generator.writeStartObject()
        generator.writeStringField('propertyName', criteria.propertyName?.toString())
        generator.writeStringField('operator', criteria.operator?.toString())
        generator.writeObjectField('value', criteria.value)
        generator.writeEndObject()
    }

    private void writeCriteriaList(List<Map<String, Object>> criteriaList) {
        generator.writeFieldName('criteria')
        generator.writeStartArray()
        for (Map<String, Object> criteria in criteriaList) {
            writeCriteria(criteria)
        }
        generator.writeEndArray()
    }

    private void writeQuery(QueryResult queryResult) {

        generator.writeStartObject()
        generator.writeStringField('domain', queryResult.domain)
        writeCriteriaList(queryResult.criteria)
        generator.writeEndObject()
    }

    private void writeQueryList(List<QueryResult> queryResults) {
        generator.writeFieldName('query')
        generator.writeStartArray()
        for (QueryResult queryResult in queryResults) {
            writeQuery(queryResult)
        }
        generator.writeEndArray()
    }

    private writeFields(Map<String, FieldResult> fields) {
        generator.writeFieldName('fields')
        generator.writeStartObject()

        for (Map.Entry<String, FieldResult> fieldResultEntry in fields) {
            writeFieldResult(fieldResultEntry.key, fieldResultEntry.value)
        }

        generator.writeEndObject()
    }

    private void writeTagResults(TagResults tagResults) {
        generator.writeFieldName('tags')
        generator.writeStartObject()
        writeStringCollectionField('add', tagResults.add)
        writeStringCollectionField('remove', tagResults.remove)
        writeStringMapField('replace', tagResults.replace)
        generator.writeEndObject()
    }

    private void writeNumberCollectionField(String fieldName, Collection<Long> values) {
        generator.writeFieldName(fieldName)
        generator.writeStartArray()
        for (String value in values) {
            generator.writeNumber(value)
        }

        generator.writeEndArray()
    }

    private void writeStringCollectionField(String fieldName, Collection<String> values) {
        generator.writeFieldName(fieldName)
        generator.writeStartArray()
        for (String value in values) {
            generator.writeString(value)
        }

        generator.writeEndArray()
    }

    private void writeStringMapField(String fieldName, Map<String, String> fieldLabelMap) {
        generator.writeFieldName(fieldName)
        generator.writeStartObject()
        for (Map.Entry<String, String> entry in fieldLabelMap) {
            generator.writeStringField(entry.key, entry.value)
        }
        generator.writeEndObject()
    }

    private void writeObjectMapField(String fieldName, Map<String, Object> fieldLabelMap) {
        generator.writeFieldName(fieldName)
        generator.writeStartObject()
        for (Map.Entry<String, Object> entry in fieldLabelMap) {
            generator.writeObjectField(entry.key, entry.value)
        }
        generator.writeEndObject()
    }

    private void writeETLInfo(Map<String, ?> ETLInfo) {
        generator.writeFieldName('ETLInfo')
        generator.writeStartObject()
        generator.writeStringField('originalFilename', ETLInfo.originalFilename?.toString())
        generator.writeEndObject()
    }
}
