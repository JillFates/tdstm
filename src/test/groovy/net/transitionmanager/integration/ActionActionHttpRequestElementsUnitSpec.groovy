package net.transitionmanager.integration

import spock.lang.Specification
import org.springframework.http.HttpMethod

import net.transitionmanager.integration.ActionRequest

/**
 * Test specifications for the ActionHttpRequestElements class
 */
class ActionActionHttpRequestElementsUnitSpec extends Specification {
	static String url = 'https://{host}.example.com/rest/{method}/{action}/something?format={format}&limit=50'
	static String expectedPost = 'https://xray.example.com/rest/query/update/something?format=json&limit=50'
	static String queryStringGet = 'extra=xyzzy&desc=this is cool'
	static String expectedGet = expectedPost + '&' + queryStringGet
	static Map queryParams = [
		format: 'json',
		limit: '50'
	]
	static Map extraParams = [
		extra: 'xyzzy',
		desc: 'this is cool'
	]
	static Map params = [
		host: 'xray',
		method: 'query',
		action: 'update',
		format: queryParams.format
	] + extraParams


	def 'Test that the various properties of ActionHttpRequestElements instance are as they should'() {
        given: 'an ActionHttpRequestElements is instantiated'
			ActionHttpRequestElements ahre = new ActionHttpRequestElements(url, new ActionRequest(params))

		expect: 'the baseUrl was parsed and placeholder replaced'
			'https://xray.example.com' == ahre.baseUrl
		and: 'the urlPath was parsed and placeholders replaced'
			'/rest/query/update/something' == ahre.urlPath
		and: 'the query string was parsed and placeholder replaced'
			'format=json&limit=50' == ahre.queryString
		and: 'queryStringMap contains all params that were specified in the URI'
			queryParams == ahre.queryParams
		and: 'the remaining extraParams has the extra attribute'
			extraParams == ahre.extraParams
	}

	def 'test buildQueryStringParams'() {
        given: 'an ActionHttpRequestElements is instantiated'
			ActionHttpRequestElements ahre = new ActionHttpRequestElements(url, new ActionRequest(params))

		when: 'calling the buildQueryStringParams method'
			String result = ahre.buildQueryStringParams()
		then: 'the value returned should include all of the parametes not consumed by  placeholders'
			queryStringGet == result
	}

    def 'test the uri method'() {

        given: 'an ActionHttpRequestElements is instantiated'
			ActionHttpRequestElements ahre = new ActionHttpRequestElements(url, new ActionRequest(params))

		expect: 'that calling uril(GET) should return all query string parameters'
			expectedGet == ahre.uri(HttpMethod.GET)
		and: 'that calling uril(POST) should return only the query string parameters that were explicit in the original URI'
			expectedPost == ahre.uri(HttpMethod.POST)
    }
}
