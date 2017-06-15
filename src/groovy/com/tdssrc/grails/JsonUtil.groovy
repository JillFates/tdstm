package com.tdssrc.grails

import groovy.json.JsonBuilder
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

}
