package com.tdssrc.grails

import spock.lang.Specification
import spock.lang.Unroll
import com.tdssrc.grails.JsonUtil

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

}