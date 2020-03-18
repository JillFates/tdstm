package net.transitionmanager.etl

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.MethodClosure

/**
 * Reader class takes ETL output data array in an input stream data,
 * and converts each ETL row in a Map that can be used in the import process.
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
 * @see net.transitionmanager.imports.DataImportService#importRowsIntoBatchUsingStreaming(java.lang.Object, net.transitionmanager.imports.ImportBatch, java.io.InputStream, net.transitionmanager.imports.ImportProgressCalculator, java.util.Map)
 */
@CompileStatic
class ETLStreamingReader {

    JsonParser parser

    // Define the Map when the Reader is constructed
    final Map<String, MethodClosure> fieldMethods = [:]

    ETLStreamingReader(InputStream inputStream) {
        this.parser = new JsonFactory().createParser(inputStream)
        fieldMethods.'comments' = (MethodClosure) this.&readStringList
        fieldMethods.'duplicate' = (MethodClosure) this.&readBooleanField
        fieldMethods.'errorCount' = (MethodClosure) this.&readIntegerField
        fieldMethods.'errors' = (MethodClosure) this.&readStringList
        fieldMethods.'fields' = (MethodClosure) this.&readFields
        fieldMethods.'domain' = (MethodClosure) this.&readStringField
        fieldMethods.'op' = (MethodClosure) this.&readStringField
        fieldMethods.'rowNum' = (MethodClosure) this.&readIntegerField
        fieldMethods.'warn' = (MethodClosure) this.&readBooleanField
        fieldMethods.'tags' = (MethodClosure) this.&readTagsField

        fieldMethods.'fieldOrder' = (MethodClosure) this.&readIntegerField
        fieldMethods.'warn' = (MethodClosure) this.&readBooleanField
        // errors
        fieldMethods.'init' = (MethodClosure) this.&readObjectField
        fieldMethods.'originalValue' = (MethodClosure) this.&readObjectField
        fieldMethods.'value' = (MethodClosure) this.&readObjectField
        fieldMethods.'find' = (MethodClosure) this.&readFindField
        fieldMethods.'create' = (MethodClosure) this.&readStringMap
        fieldMethods.'update' = (MethodClosure) this.&readStringMap

        // Fields for readFindField method
        fieldMethods.'matchOn' = (MethodClosure) this.&readIntegerField
        fieldMethods.'results' = (MethodClosure) this.&readLongList
        fieldMethods.'size' = (MethodClosure) this.&readIntegerField
        fieldMethods.'query' = (MethodClosure) this.&readQueryList

        // Methods for readQueryField
        fieldMethods.'domain' = (MethodClosure) this.&readStringField
        fieldMethods.'criteria' = (MethodClosure) this.&readCriteriaList

        // Fields for readCriteriaField method
        fieldMethods.'propertyName' = (MethodClosure) this.&readStringField
        fieldMethods.'operator' = (MethodClosure) this.&readStringField
        fieldMethods.'value' = (MethodClosure) this.&readObjectField

        // Fields for readTagsField method
        fieldMethods.'add' = (MethodClosure) this.&readStringList
        fieldMethods.'remove' = (MethodClosure) this.&readStringList
        fieldMethods.'replace' = (MethodClosure) this.&readStringMap
    }

    /**
     * <p>This method creates a new row Map from ETL InputStream data.</p>
     * <pre>
     *     InputStream inputStream = fileSystemService.openTemporaryFile(filename)
     *     new JacksonDeserializer(inputStream).eachRow { Map<String, ?> rowData ->
     *         println rowData
     *     //}//
     * </pre>
     *  Variable {@code rowData} contains a Map with teh following JSON contact:
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
     * @param createImportBatchRecord
     */
    void eachRow(Closure createImportBatchRecord) {

        startArray()
        while (nextToken() != JsonToken.END_ARRAY) {
            Map<String, ?> row = parseRow()
            createImportBatchRecord(row)
        }
        parser.close()
    }

    Map<String, ?> parseRow() {
        Map<String, ?> row = [:]

        while (nextToken() != JsonToken.END_OBJECT) {
            String currentName = parser.currentName()
            row[currentName] = fieldMethods[currentName]()
        }
        return row
    }

    /**
     *
     * <pre>
     *  "fields": {//
     *      "assetName": { //
     *          "create": null,
     *          "errors": [],
     *          "fieldOrder": 0,
     *          "find": { //
     *              "matchOn": null,
     *              "query": [],
     *              "results": [],
     *              "size": 0
     *         //}//,
     *           "init": null,
     *           "originalValue": "Application 2",
     *           "update": null,
     *           "value": "Application 2",
     *           "warn": false
     *     //}//,
     *     ...
     * </pre>
     * @return a Map with fields JSON content
     */
    private Map<String, ?> readFields() {

        JsonToken token = nextToken()
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null
        }

        validateStartObject(token)

        Map<String, ?> fields = [:]
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
            fieldResult[currentName] = fieldMethods[currentName]()
        }
        return fieldResult
    }

    private Map<String, ?> readFindField() {

        Map<String, ?> find = [:]
        startObject()
        while (nextToken() != JsonToken.END_OBJECT) {
            String currentName = parser.currentName()
            find[currentName] = fieldMethods[currentName]()
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
            query[currentName] = fieldMethods[currentName]()
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
            query[currentName] = fieldMethods[currentName]()
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

        JsonToken token = nextToken()
        if (token == JsonToken.VALUE_NULL) {
            return null
        }

        validateStartObject(token)
        Map<String, ?> tags = [:]
        while (nextToken() != JsonToken.END_OBJECT) {

            String currentName = parser.currentName()
            tags[currentName] = fieldMethods[currentName]()
        }
        return tags
    }

    JsonToken nextToken() {
        JsonToken token = parser.nextToken()
        return token
    }

    private String readStringField() {
        nextToken()
        String res = parser.getValueAsString()
        return res
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

    private Map<String, String> readStringMap() {
        JsonToken token = nextToken()
        if (token == JsonToken.VALUE_NULL) {
            return null
        }

        validateStartObject(token)
        Map<String, String> stringMap = [:]
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

    private void validateStartObject(JsonToken token) {
        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected content to be an Object")
        }
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
}
