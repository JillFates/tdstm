package com.tdssrc.grails;

import spock.lang.Specification;

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

}
