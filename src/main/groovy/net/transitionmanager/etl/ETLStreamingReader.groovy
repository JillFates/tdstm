package net.transitionmanager.etl

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import groovy.transform.CompileStatic

import java.util.function.Supplier

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
    final Map<String, Supplier> fieldMethods = [:]

    /**
     * <p>Constructor prepares a Map of String keys and {@link Supplier} java Functional Interface.
     * This maps is used as a pointer to the correct function in an instance of {@link ETLStreamingReader}.
     * For each field, it needs to invoke a particular method inside {@link ETLStreamingReader}</p>
     * For example:
     * <pre>
     *  fieldMethods.'comments' = this.&readStringList as Supplier
     * </pre>
     * <p>When an instance of {@link ETLStreamingReader} is deserializing comments field
     * from {@link com.tdsops.etl.RowResult#comments} the following method is applied:</p>
     * <pre>
     *      private List<String> readStringList() {//
     *          List<String> stringList = []
     *         startArray()
     *         while (nextToken() != JsonToken.END_ARRAY) { //
     *              stringList.add(parser.getValueAsString())
     *         //} //
     *         return stringList
     *     //}//
     * </pre>
     * @param inputStream
     */
    ETLStreamingReader(InputStream inputStream) {
        this.parser = new JsonFactory().createParser(inputStream)

        fieldMethods.'comments' = this.&readStringList as Supplier
        fieldMethods.'duplicate' = this.&readBooleanField as Supplier
        fieldMethods.'errorCount' = this.&readIntegerField as Supplier
        fieldMethods.'errors' = this.&readStringList as Supplier
        fieldMethods.'fields' = this.&readFields as Supplier
        fieldMethods.'domain' = this.&readStringField as Supplier
        fieldMethods.'op' = this.&readStringField as Supplier
        fieldMethods.'rowNum' = this.&readIntegerField as Supplier
        fieldMethods.'warn' = this.&readBooleanField as Supplier
        fieldMethods.'tags' = this.&readTagsField as Supplier

        fieldMethods.'fieldOrder' = this.&readIntegerField as Supplier
        fieldMethods.'warn' = this.&readBooleanField as Supplier
        // errors
        fieldMethods.'init' = this.&readObjectField as Supplier
        fieldMethods.'originalValue' = this.&readObjectField as Supplier
        fieldMethods.'value' = this.&readObjectField as Supplier
        fieldMethods.'find' = this.&readFindField as Supplier
        fieldMethods.'create' = this.&readStringMap as Supplier
        fieldMethods.'update' = this.&readStringMap as Supplier

        // Fields for readFindField method
        fieldMethods.'matchOn' = this.&readIntegerField as Supplier
        fieldMethods.'results' = this.&readLongList as Supplier
        fieldMethods.'size' = this.&readIntegerField as Supplier
        fieldMethods.'query' = this.&readQueryList as Supplier

        // Methods for readQueryField
        fieldMethods.'domain' = this.&readStringField as Supplier
        fieldMethods.'criteria' = this.&readCriteriaList as Supplier

        // Fields for readCriteriaField method
        fieldMethods.'propertyName' = this.&readStringField as Supplier
        fieldMethods.'operator' = this.&readStringField as Supplier
        fieldMethods.'value' = this.&readObjectField as Supplier

        /** Fields for {@link  } method */
        fieldMethods.'add' = this.&readStringList as Supplier
        fieldMethods.'remove' = this.&readStringList as Supplier
        fieldMethods.'replace' = this.&readStringMap as Supplier
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

    /**
     *
     * @return
     */
    Map<String, ?> parseRow() {
        Map<String, ?> row = [:]

        while (nextToken() != JsonToken.END_OBJECT) {
            String currentName = parser.currentName()
            row[currentName] = fieldMethods[currentName].get()
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
            fieldResult[currentName] = fieldMethods[currentName].get()
        }
        return fieldResult
    }

    private Map<String, ?> readFindField() {

        Map<String, ?> find = [:]
        startObject()
        while (nextToken() != JsonToken.END_OBJECT) {
            String currentName = parser.currentName()
            find[currentName] = fieldMethods[currentName].get()
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
            query[currentName] = fieldMethods[currentName].get()
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
            query[currentName] = fieldMethods[currentName].get()
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
            tags[currentName] = fieldMethods[currentName].get()
        }
        return tags
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
