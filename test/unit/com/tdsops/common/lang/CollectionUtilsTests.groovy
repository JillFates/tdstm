package com.tdsops.common.lang

import grails.test.*
import com.tdsops.common.lang.ExceptionUtil

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the CollectionUtils class
 * TODO: A lot of methods are still missing to test ( We need some code coverage)
 */
class CollectionUtilsTests extends Specification {

	/**
	 * Testing the Sorting using closure builder
	 * @author @tavo_luna
	 */
	public void testCaseInsensitiveSorterBuilder() {
		def list = [[name:"IContact"], [name:"iContact"], [name:"A"], [name:"K"], [name:"k"]]

		def expectedSort = [[name:"A"], [name:"iContact"], [name:"IContact"], [name:"k"], [name:"K"]]

		def sorter = CollectionUtils.caseInsensitiveSorterBuilder({ it.name })
		list.sort(sorter)

		expect:
			assert expectedSort == list
	}

}
