package com.tdssrc.grails

import com.fasterxml.jackson.databind.ObjectMapper
import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.web.json.JSONElement
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
            throw new InvalidParamException("Invalid JSON : ${e.message}")
        }
    }

    /**
     * Used for parsing a JSON string that contains a list of elements.
     * The parseJson in this class returns a JSONObject (a map) and will fail
     * when passing a JSON with a list.
     *
     * @param jsonText
     * @return a list after parsing the text
     */
    static List parseJsonList(String jsonText) {
        // TODO : JPM 2/2018 : Need to add try/catch
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            return jsonSlurper.parseText(jsonText) as List
        } catch (e) {
            throw new InvalidParamException("Invalid JSON : ${e.message}")
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
            throw new InvalidParamException("Invalid JSON : ${e.message}")
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
            throw new InvalidParamException("Invalid JSON : ${e.message}")
        }
    }

    /**
     * Convert given json string to a Map representation
     * @param json
     * @return
     */
    static Map<String, ?> convertJsonToMap(String json) {
        // TODO : JPM 2/2018 : Need to add try/catch
        Map<String, Object> jsonMap = new ObjectMapper().readValue(json, HashMap.class)
        return jsonMap
    }

    /**
     * Convert a JSONElement to map
     * @param json
     * @return
     */
    static Map<String, ?> convertJsonToMap(JSONElement json) {
        if (json == null) return null
        return convertJsonToMap(json.toString())
    }

    /**
     * Convert a map to json string
     * @param map
     * @return
     */
    static String convertMapToJsonString(Map<String, ?> map) {
        return JsonOutput.toJson(map)
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
        // TODO : JPM 2/2018 : Need to add try/catch

        return (JSONObject)JSON.parse(ExportUtil.getResource(fileName).inputStream.text)
    }

    /**
     * Parse the given inputStream of text into an JSONObject instance
     * @param inputStream
     * @return JSON
     */
    static JSONObject parseFile(InputStream inputStream) {
        // TODO : JPM 2/2018 : Need to add try/catch

        return (JSONObject)JSON.parse(inputStream.text)
    }

    /**
     * Used to transform a JSON string into an object of a particular class
     *
     * @param json - Some JSON text
     * @param target - a Class to be instantiated and populated appropriately
     * @returns an instance of the target Class
     * @throws InvalidParamException for syntax errors or if the Object structure doesn't match
     */
    static <T> T mapToObject(String json, Class<T> target) throws InvalidParamException {
        try {
            return new ObjectMapper().readValue(json, target)
        } catch (Exception e) {
            logger.error(e.message)
            throw new InvalidParamException("Invalid JSON : ${e.message}")
        }
    }

    /**
     * Used to transform a JSONObject into a target object. This is primarily used in conjunction
     * with controllers and request.JSON
     *
     * @param json - the JSON send as part of the request
     * @param target - a Class to be instantiated and populated appropriately
     * @returns an instance of the target Class
     * @throws InvalidParamException for syntax errors or if the Object structure doesn't match
     */
    static <T> T mapToObject(JSONObject json, Class<T> target) {
        String jsonStr
        try {
            // Convert the JSON object back to a String (seems silly but...)
            jsonStr = toJson(json)
        } catch (Exception e) {
            logger.error("mapToObject() Failed to convert JSONObject to JSON Text: ${e.message}")
            throw new InvalidParamException("Invalid JSON : ${e.message}")
        }
        return mapToObject(jsonStr, target)
    }

    /**
     * Parse a file by Path (Absolute or relative to Projects dir to JSONObject
     * @param fileName
     * @return JSONElement
     */
    static JSONElement parseFilePath(String fileName) {
        File file = new File(fileName)
        return JSON.parse(file.text)
    }

    /**
     * method from JSONElements to let use XPATH like access
     * e.g.
     * <PRE>
     *       assert xpathAt(result, "person.name") == "Guillaume"
     *       assert xpathAt(result, "")            == [person:[name:Guillaume, age:33, pets:[dog, cat]]]
     *       assert xpathAt(result, ".")           == [person:[name:Guillaume, age:33, pets:[dog, cat]]]
     * </PRE>
     * @param json
     * @param xpath
     * @param separator
     * @return
     */
    static JSONElement xpathAt(JSONElement json, String xpath, String separator = '.') {
        if (!xpath || xpath == separator) {
            return json
        }

        if (!xpath.contains(separator)) {
            return json[xpath] as JSONElement
        }

        def firstPropName = xpath[0..xpath.indexOf(separator) - 1]
        def remainingPath = xpath[xpath.indexOf(separator) + 1 .. -1]
        JSONElement firstProperty = json[firstPropName] as JSONElement
        return xpathAt(firstProperty, remainingPath, separator)
    }

    /**
     * method from Maps to let use GPATH like access in a String
     * e.g.
     * <PRE>
     *       assert gpathAt(result, "person.name") == "Guillaume"
     *       assert gpathAt(result, "")            == [person:[name:Guillaume, age:33, pets:[dog, cat]]]
     *       assert gpathAt(result, ".")           == [person:[name:Guillaume, age:33, pets:[dog, cat]]]
     * </PRE>
     * @param Map
     * @param gpath
     * @param separator
     * @return
     */
    static Object gpathAt(Map map, String xpath, String separator = '.') {
        if (!xpath || xpath == separator) {
            return map
        }

        if (!xpath.contains(separator)) {
            return map[xpath]
        }

        def firstPropName = xpath[0..xpath.indexOf(separator) - 1]
        def remainingPath = xpath[xpath.indexOf(separator) + 1 .. -1]
        def firstProperty = map[firstPropName]
        if( !(firstProperty instanceof Map) ){
            throw new MissingPropertyException("No such property: '${remainingPath}' for class: ${firstProperty.class}")
        }
        return gpathAt(firstProperty as Map, remainingPath, separator)
    }

}
