package com.tdssrc.grails

import com.fasterxml.jackson.databind.ObjectMapper
import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONObject

@CompileStatic
@Slf4j(value='logger')
class JsonUtil {

    /**
     * Parse json text
     * @param json
     * @return
     */
    static JSONObject parseJson(String json) {
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            return jsonSlurper.parseText(json) as JSONObject
        } catch (JsonException e) {
            logger.error(e.message)
            throw new InvalidParamException("JSON is not valid")
        }
    }

    /**
     * Parse and validate json text
     * @param json
     * @return
     */
    static String validateJson(String json) {
        try {
            JSONObject parsedJson = parseJson(json)
            JsonBuilder jsonBuilder = new JsonBuilder(parsedJson)
            return jsonBuilder.toString()
        } catch (JsonException e) {
            logger.error(e.message)
            throw new InvalidParamException("JSON is not valid")
        }
    }

    /**
     * Optimize given json object and return its string representation
     * @param json
     * @return
     */
    static String validateJsonAndConvertToString(JSONObject json) {
        try {
            JsonBuilder jsonBuilder = new JsonBuilder(json)
            return jsonBuilder.toString()
        } catch (JsonException e) {
            logger.error(e.message)
            throw new InvalidParamException("JSON is not valid")
        }
    }

    /**
     * Convert given json string to a Map representation
     * @param json
     * @return
     */
    static Map<String, ?> convertJsonToMap(String json) {
        Map<String, Object> jsonMap = new ObjectMapper().readValue(json, HashMap.class)
        return jsonMap
    }

    /**
     * Converts an object into a String in the JSON format
     * @param object - the object to be converted
     * @return the object as JSON String
     */
    static String toJson(Object object) {
        // new JsonBuilder(object).toString()
        JsonOutput.toJson(object)
    }

    /**
     * Converts an object into a String in the JSON pretty format
     * @param object - the object to be converted
     * @return the object as JSON String
     */
    static String toPrettyJson(Object object) {
        JsonOutput.prettyPrint(toJson(object))
    }

    /**
     * Parse the given file into an JSONObject instance.
     * @param fileName
     * @return
     */
    static JSONObject parseFile(String fileName) {
        return (JSONObject)JSON.parse(ExportUtil.getResource(fileName).inputStream.text)
    }

    /**
     * Parse the given inputStream of text into an JSONObject instance
     * @param inputStream
     * @return JSON
     */
    static JSONObject parseFile(InputStream inputStream) {
        return (JSONObject)JSON.parse(inputStream.text)
    }
}
