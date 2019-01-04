package com.tdssrc.grails

import spock.lang.Specification

class UrlUtilTests extends Specification {

	void 'Test is secure url'() {
		expect:
			UrlUtil.isSecure(value) == result

		where:
			value               | result
			'https://test.me'   | true
			'hTtps://test.me'   | true
			'hTTps://test.me'   | true
			'hTTPs://test.me'   | true
			'hTTPS://test.me'   | true
			'HTTPS://test.me'   | true
			'HTTP://test.me'    | false
			'http://test.me'    | false
			'Http://test.me'    | false
			' Https://test.me'  | true
			' https://test.me'  | true
			' Http://test.me'   | false
			' http://test.me'   | false
	}

	void 'Test is sanitize url to be used with Camel Http4'() {
		expect:
			UrlUtil.sanitizeUrlForCamel(value) == result

		where:
			value               | result
			'https://test.me'   | 'https4://test.me'
			'hTtps://test.me'   | 'https4://test.me'
			'hTTps://test.me'   | 'https4://test.me'
			'hTTPs://test.me'   | 'https4://test.me'
			'hTTPS://test.me'   | 'https4://test.me'
			'HTTPS://test.me'   | 'https4://test.me'
			'HTTP://test.me'    | 'http4://test.me'
			'http://test.me'    | 'http4://test.me'
			'Http://test.me'    | 'http4://test.me'
			' Https://test.me'  | 'https4://test.me'
			' https://test.me'  | 'https4://test.me'
			' http://test.me'   | 'http4://test.me'
			' Http://test.me'   | 'http4://test.me'
	}


	void 'Test the encode method'() {
		expect:
			expected == UrlUtil.encode(value)
		where:
			value			| expected
			'a b & c'		| 'a+b+%26+c'
			'a,b,c'			| 'a%2Cb%2Cc'
			'(#?)'			| '%28%23%3F%29'
	}

	void 'Test the decode method'() {
		expect:
			expected == UrlUtil.decode(value)
		where:
			expected		| value
			'a b & c'		| 'a+b+%26+c'
			'a,b,c'			| 'a%2Cb%2Cc'
			'(#?)'			| '%28%23%3F%29'
	}

	void 'Test the queryStringToMap method'() {
		expect:
			expected == UrlUtil.queryStringToMap(queryString, decode)
		where:
			queryString				| decode	| expected
			'a=1&b=2&c=3'			| false		| [a:'1', b:'2', c:'3']
			'a=1&b=%28%23%3F%29'	| false 	| [a:'1', b:'%28%23%3F%29']
			'a=1&b=%28%23%3F%29'	| true 		| [a:'1', b:'(#?)']
			'a=z&b=abc&c=%2C'		| null		| [a:'z', b:'abc', c:',']		// Test default decode==true
			'a=&b=abc'				| false		| [a:null, b:'abc']				// Test empty a gets null
			'a&b=abc'				| false		| [a:null, b:'abc']				// Test a with no equals
	}
}
