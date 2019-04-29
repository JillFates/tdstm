package net.transitionmanager.controller

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll
import spock.lang.IgnoreRest
import com.tds.asset.Application
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.service.InvalidParamException
import org.codehaus.groovy.grails.exceptions.GrailsDomainException

class PaginationMethodsUtilSpec extends Specification {

	class PaginationTestController implements PaginationMethods {
		Map params = [:]
	}

	@Shared PaginationTestController testController

	void setup() {
		testController = new PaginationTestController()
	}

	@Unroll
	void 'test paginationSortOrder'() {

		expect:
			result == setParamsInTestCtrl(paramName, paramValue) + testController.paginationSortOrder(paramName, defaultSort)

		where:
			paramName	| paramValue	| defaultSort	| result
			'sord'		| 'DESC'		| 'ASC'			| 'DESC'
			'sord'		| 'ASC'			| 'DESC'		| 'ASC'
			'sord'		| 'desc'		| 'ASC'			| 'DESC'
			'sord'		| 'aSc'			| 'DESC'		| 'ASC'
			'sord'		| 'A'			| 'D'			| 'ASC'
			'sord'		| 'D'			| 'A'			| 'DESC'
			'sord'		| 'a'			| 'D'			| 'ASC'
			'sord'		| 'd'			| 'A'			| 'DESC'
			'sord'		| ''			| 'A'			| 'ASC'
			'sord'		| null			| 'A'			| 'ASC'
			null		| null			| 'ASC'			| 'ASC'
			null		| null			| 'DESC'		| 'DESC'
			null		| null			| 'A'			| 'ASC'
			null		| null			| 'D'			| 'DESC'
	}

	@Unroll
	// @IgnoreRest
	void 'test paginationSortOrder invalid params throwing exception'() {
		when:
			setParamsInTestCtrl(paramName, paramValue)
			testController.paginationSortOrder(paramName, (paramValue ?: defaultSort) )

		then:
			def ex = thrown(InvalidParamException)
			ex.message == testController.PAGINATION_INVALID_SORT_ORDER_EXCEPTION.message

		where:
			paramName	| paramValue	| defaultSort
			'sorder'	| 'FUBAR'		| 'ASC'
			null		| null			| 'FUBAR'
			null		| 'FUBAR'		| 'ASC'
	}

	void 'test paginationOrderBy'() {
		when: 'a valid domain and property are supplied'
			String sortByParam = 'sorder'
			setParamsInTestCtrl(sortByParam, 'license')
		and: 'the paginationOrderBy is called'
			String property = testController.paginationOrderBy(Application, sortByParam, 'assetName')
		then: 'params supplied property name is returned'
			'license' == property

		when: 'a valid domain and no sort property specified in params'
			setParamsInTestCtrl(sortByParam, '')
		and: 'the paginationOrderBy is called'
			property = testController.paginationOrderBy(Application, sortByParam, 'assetName')
		then: 'the default sort is returned'
			'assetName' == property

		when: 'the sort by params property is blank'
			setParamsInTestCtrl(sortByParam, '')
		and: 'a invalid default property is supplied to paginationOrderBy'
			testController.paginationOrderBy(Application, sortByParam, 'FUBAR')
		then: 'a PAGINATION_INVALID_DEFAULT_ORDER_BY_EXCEPTION should be thrown'
			def ex = thrown(RuntimeException)
			ex.message == testController.PAGINATION_INVALID_DEFAULT_ORDER_BY_EXCEPTION.message

		when: 'a invalid class is supplied'
			testController.paginationOrderBy(String, sortByParam, 'FUBAR')
		then:
			ex = thrown(GrailsDomainException)
	}


	void 'test paginationOrderBy for missing sort param'() {
		when: 'a valid domain and default property are supplied to paginationOrderBy'
			String sortByParam = 'sorder'
			String property = testController.paginationOrderBy(Application, sortByParam, 'assetName')
		then: 'the default sort is returned'
			'assetName' == property

		when: 'a invalid property is supplied'
			testController.paginationOrderBy(Application, sortByParam, 'FUBAR')
		then:
			def ex = thrown(RuntimeException)
			ex.message == testController.PAGINATION_INVALID_DEFAULT_ORDER_BY_EXCEPTION.message

	}


	/**
	 * Used by test cases to set params appropriately in the Test Controller Object which returns a blank string so that it
	 * can be used in expect/where statements
	 */
	String setParamsInTestCtrl(String paramName, String paramValue) {
		if (paramName) {
			testController.params = [ (paramName) : paramValue ]
		}
		return ''
	}

}

