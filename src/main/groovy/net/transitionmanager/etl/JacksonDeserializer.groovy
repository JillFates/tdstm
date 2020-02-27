package net.transitionmanager.etl

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import groovy.transform.CompileStatic

/**
 * Deserialization class takes ETL output data array in an input stream data,
 * and converts each ETL row in a Map that can be used in the import processe.
 * <pre>
 * 	"data": [
 * 		//{//
 * 		    "op": "I",
 * 		    "errorCount": 0,
 * 		    "warn": true,
 * 		    "duplicate": true,
 * 		    "errors": [],
 * 		    "fields": {...} //
 * 	    //}//
 * </pre>
 *
 * @see net.transitionmanager.imports.DataImportService#importRowsIntoBatch(java.lang.Object, net.transitionmanager.imports.ImportBatch, java.io.InputStream, java.util.Map)
 */
@CompileStatic
class JacksonDeserializer {

    JsonParser parser

    JacksonDeserializer(InputStream inputStream) {
        this.parser = new JsonFactory().createParser(inputStream)
    }

    /**
     * <p>This method creates a new row Map from ETL InputStream data.</p>
     * <pre>
     *     InputStream inputStream = fileSystemService.openTemporaryFile(filename)
     *     Closure closure = { Map<String, ?> rowData ->
     *         println rowData
     *     //}//
     *     new JacksonDeserializer(inputStream).eachRow(closure)
     * </pre>
     *  Variable {@code rowData} contains a
     *
     * @param closure
     */
    void eachRow(Closure closure) {

        startArray()
        while (nextToken() != JsonToken.END_ARRAY) {
            Map<String, ?> row = parseRow()
            closure(row)
        }
        parser.close()
    }

    private void startObject() {
        if (nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected content to be an Object")
        }
    }

    private void endObject() {
        if (nextToken() != JsonToken.END_OBJECT) {
            throw new IllegalStateException("Unexpected end of Object")
        }
    }

    private void startArray() {
        if (nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected content to be an Array")
        }
    }

    private void endArray() {
        if (nextToken() != JsonToken.END_ARRAY) {
            throw new IllegalStateException("Unexpected end of Array")
        }
    }

    JsonToken nextToken() {
        JsonToken token = parser.nextToken()
        return token
    }

    private String readStringField() {
        nextToken()
        return parser.getValueAsString()
    }

    private Integer readIntegerField() {
        nextToken()
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null
        } else {
            return parser.getIntValue()
        }

    }

    private Object readObjectField() {
        nextToken()
        switch (parser.currentToken()) {
            case JsonToken.VALUE_NULL:
                return null
                break
            case JsonToken.VALUE_STRING:
                return parser.getValueAsString()
                break
            case JsonToken.VALUE_NUMBER_INT:
                return parser.getIntValue()
                break
            case JsonToken.VALUE_NUMBER_FLOAT:
                return parser.getFloatValue()
                break
            default:
                return null
        }
    }

    private Boolean readBooleanField() {
        nextToken()
        return parser.getBooleanValue()
    }

    Map<String, String> readStringMap() {
        Map<String, String> stringMap = [:]
        startObject()
        while (nextToken() != JsonToken.END_OBJECT) {
            String key = parser.currentName()
            String value = parser.nextTextValue()
            stringMap[key] = value
        }

        return stringMap
    }

    private List<String> readStringList() {
        List<String> stringList = []
        startArray()
        while (nextToken() != JsonToken.END_ARRAY) {
            stringList.add(parser.getValueAsString())
        }

        return stringList
    }

    private List<Long> readLongList() {
        List<Long> stringList = []
        startArray()
        while (nextToken() != JsonToken.END_ARRAY) {
            stringList.add(parser.getValueAsLong())
        }

        return stringList
    }

    Map<String, ?> parseRow() {
        Map<String, ?> row = [:]

        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            switch (currentName) {
                case 'comments':
                    row.comments = readStringList()
                    break
                case 'duplicate':
                    row.duplicate = readBooleanField()
                    break
                case 'errorCount':
                    row.errorCount = readIntegerField()
                    break
                case 'errors':
                    row.errors = readStringList()
                    break
                case 'fields':
                    row.fields = readFields()
                    break
                case 'domain':
                    row.domain = readStringField()
                    break
                case 'op':
                    row.op = readStringField()
                    break
                case 'rowNum':
                    row.rowNum = readIntegerField()
                    break
                case 'warn':
                    row.warn = readBooleanField()
                    break
                case 'tags':
                    row.warn = readTagsField()
                    break
                default:
                    throw new IllegalStateException("Unexpected field for domain row: '$currentName'")
            }

        }
        return row
    }


    /**
     *
     * @return
     */
    private Map<String, ?> readFields() {

        Map<String, ?> fields = [:]
        startObject()
        while (nextToken() != JsonToken.END_OBJECT) {
            String currentName = parser.currentName()
            Map<String, ?> fieldResult = readFieldResult()
            fields[currentName] = fieldResult
        }
        return fields
    }

    /**
     *
     * @return
     */
    private Map<String, ?> readFieldResult() {

        Map<String, ?> fieldResult = [:]
        startObject()
        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            switch (currentName) {
                case 'fieldOrder':
                    fieldResult.fieldOrder = readIntegerField()
                    break
                case 'warn':
                    fieldResult.warn = readBooleanField()
                    break
                case 'errors':
                    fieldResult.errors = readStringList()
                    break
                case 'init':
                    fieldResult.init = readObjectField()
                    break
                case 'originalValue':
                    fieldResult.originalValue = readObjectField()
                    break
                case 'value':
                    fieldResult.value = readObjectField()
                    break
                case 'find':
                    fieldResult.find = readFindField()
                    break
                case 'create':
                    fieldResult.create = readStringMap()
                    break
                case 'update':
                    fieldResult.update = readStringMap()
                    break
                default:
                    throw new IllegalStateException("Unexpected field for domain row field result: '$currentName'")
            }
        }
        return fieldResult
    }

    private Map<String, ?> readFindField() {

        Map<String, ?> find = [:]
        startObject()
        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            switch (currentName) {
                case 'matchOn':
                    find.matchOn = readIntegerField()
                    break
                case 'results':
                    find.results = readLongList()
                    break
                case 'size':
                    find.size = readIntegerField()
                    break
                case 'query':
                    find.query = readQueryList()
                    break
                default:
                    throw new IllegalStateException("Unexpected field for find: '${currentName}'")
            }
        }
        return find
    }

    /**
     *
     * @return
     */
    private List<Map<String, ?>> readQueryList() {

        List<Map<String, ?>> queryList = []
        startArray()
        while (nextToken() != JsonToken.END_ARRAY) {
            queryList.add(readQueryField())

        }
        return queryList
    }

    private Map<String, ?> readQueryField() {

        Map<String, ?> query = [:]
        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            switch (currentName) {
                case 'domain':
                    query.domain = readStringField()
                    break
                case 'criteria':
                    query.criteria = readCriteriaList()
                    break
                default:
                    throw new IllegalStateException("Unexpected field for query: '${currentName}'")
            }
        }
        return query
    }

    /**
     *
     * @return
     */
    private List<Map<String, ?>> readCriteriaList() {

        List<Map<String, ?>> criteriaList = []
        startArray()
        while (nextToken() != JsonToken.END_ARRAY) {
            criteriaList.add(readCriteriaField())

        }
        return criteriaList
    }

    private Map<String, ?> readCriteriaField() {

        Map<String, ?> query = [:]
        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            switch (currentName) {
                case 'propertyName':
                    query.propertyName = readStringField()
                    break
                case 'operator':
                    query.operator = readStringField()
                    break
                case 'value':
                    query.value = readObjectField()
                    break
                default:
                    throw new IllegalStateException("Unexpected field for criteria: '${currentName}'")
            }
        }
        return query
    }

    /**
     *
     * <pre>
     *    "tags": {//
     *      "add": [
     *         "GDPR",
     *         "PCI"
     *       ],
     *       "remove": [
     *         "HIPPA"
     *       ],
     *       "replace": { //
     *          "PCI": "SOX"
     *    // }//
     * //}//
     * </pre>
     * @return
     */
    private Map<String, ?> readTagsField() {

        Map<String, ?> tags = [:]
        startObject()

        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            switch (currentName) {
                case 'add':
                    tags.add = readStringList()
                    break
                case 'remove':
                    tags.add = readStringList()
                    break
                case 'replace':
                    tags.replace = readStringMap()
                    break
                default:
                    throw new IllegalStateException("Unexpected field for domain row")
            }
        }
        return tags
    }
}
