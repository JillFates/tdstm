package com.tdssrc.grails

import net.transitionmanager.exception.InvalidParamException
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

// import net.transitionmanager.command.CommandObject

/**
 * Unit test cases for the JsonUtil class
 */
@Unroll
class JsonUtilUnitSpec extends Specification {

	def 'Test the toJson method'() {
		expect:
			expected == JsonUtil.toJson(value)

		where:
			value       | expected
			[1,2,3]     | '[1,2,3]'
			'abc'       | '"abc"'
			[a:1,b:"x"] | '{"a":1,"b":"x"}'
	}

	def 'Test the toPrettyJson method'() {
		expect:
			expected == JsonUtil.toPrettyJson(value)

		where:
			value       | expected
			[1,2,3]     | '[\n    1,\n    2,\n    3\n]'
			'abc'       | '"abc"'
			[a:1,b:"x"] | '{\n    "a": 1,\n    "b": "x"\n}'
	}

	def 'Test the mapToObject with populating objects from a JSONObject'() {
		given: 'a json string'
			String json = '''
					{ "letters": { "a":"x", "b":"y", "c":"z" } }
				'''.stripIndent()
			JSONObject jo = JsonUtil.parseJson(json)

		when: 'mapToObject() is called with matching JSONObject and target POJO class'
			JsonUtilUnitSpecPOJO pojo = JsonUtil.mapToObject(jo, JsonUtilUnitSpecPOJO)

		then: 'the pojo should be populated correctly'
			'x' == pojo.letters.a
			'y' == pojo.letters.b
			'z' == pojo.letters.c

		when: 'mapToObject is called and the JSONObject does not match the POJO structure'
			json = '''
					{ "numbers": { "a":1, "b":2, "c":3 } }
				'''.stripIndent()

			jo = JsonUtil.parseJson(json)
			pojo = JsonUtil.mapToObject(jo, JsonUtilUnitSpecPOJO)
		then: 'an exception should be thrown'
			thrown InvalidParamException

		// This test can't be performed because of Ambiguous method overloading when the param is null
		// when: 'mapToObject is called with a NULL JSONObject'
		// 	JSONObject nullJO = null
		// 	pojo = JsonUtil.mapToObject(nullJO, JsonUtilUnitSpecPOJO)
		// then: 'an exception should be thrown'
		// 	pojo == null

	}

	def 'Test the mapToObject with populating objects from a String'() {
		given:
			String json = '''
					{ "letters": { "a":"x", "b":"y", "c":"z" } }
				'''.stripIndent()

		when: 'calling mapToObject() with JSON String and valid target POJO class'
			JsonUtilUnitSpecPOJO pojo = JsonUtil.mapToObject(json, JsonUtilUnitSpecPOJO)
		then: 'the pojo should be populated correctly'
			'x' == pojo.letters.a
			'y' == pojo.letters.b
			'z' == pojo.letters.c

		when: 'calling mapToObject() where the JSON does not match the POJO structure'
			json = '''
					{ "numbers": { "a":1, "b":2, "c":3 } }
				'''.stripIndent()

			JsonUtil.mapToObject(json, JsonUtilUnitSpecPOJO)
		then: 'then an exception should be thrown'
			thrown InvalidParamException
	}

	def 'Test the parseJsonObject method'() {
		when:
			def expected = JsonUtil.parseJsonObject('{"a":1,"b":"x"}')

		then:
			expected == [a:1,b:"x"]
			expected instanceof Map

		when:
			expected = JsonUtil.parseJsonObject('[{"a":1,"b":"x"}]')

		then:
			expected == [[a:1,b:"x"]]
			expected instanceof List
	}

	def 'Test the gpathAt method'() {
		given:
			def jsonMap = [
				a:[
					b:[
						c:"value"
               ],
					list: [
						[l1: "one"],
						[l2: "two"]
					]
            ]
			]

		when: "Navigate to a Node"
			def expected = JsonUtil.gpathAt(jsonMap,'a.b')

		then:
			expected == [c:"value"]
			expected instanceof Map

		when: "Navigate to a List"
			expected = JsonUtil.gpathAt(jsonMap,'a.list')

		then:
			expected == [[l1: "one"],[l2: "two"]]
			expected instanceof List

		when: "Navigate to a value"
			expected = JsonUtil.gpathAt(jsonMap,'a.b.c')

		then:
			expected == "value"

		when: "Path not found return null"
			expected = JsonUtil.gpathAt(jsonMap,'a.b.notFound')

		then:
			expected == null

		when: "navigate to root path using 'dot' gpath"
			expected = JsonUtil.gpathAt(jsonMap,'.')

		then:
			expected == jsonMap

		when: "navigate to root Path using 'empty' gpath"
			expected = JsonUtil.gpathAt(jsonMap,'')

		then:
			expected == jsonMap

		when: "Navigate to a path using '/' separator instead of '.'"
			expected = JsonUtil.gpathAt(jsonMap,'a/b', '/')

		then:
			expected == [c:"value"]

	}

}

// class JsonUtilUnitSpecPO implements CommandObject {
class JsonUtilUnitSpecPOJO {
	Map letters

	void setLetters(Object v) {
		letters = v
	}
}
