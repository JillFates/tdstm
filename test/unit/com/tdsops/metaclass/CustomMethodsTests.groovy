package com.tdsops.metaclass

import spock.lang.Specification

class CustomMethodsTests extends Specification {

	void setupSpec() {
		// Initialize the metaClass settings
		CustomMethods.initialize(true)
	}

	void testAsYN() {
		expect:
		true.asYN() == 'Y'
		false.asYN() == 'N'
	}

	void testAsMap() {
		List beatles = [
			['John', 1, 'Guitar', true],
			['Paul', 2, 'Bass', true],
			['Ringo', 3, 'Drums', true],
			['George', 4, 'Lead guitar', false]
		]

		when:
		def map = beatles.asMap(0)

		then:
		map instanceof Map
		map.containsKey('Paul')
		map.Ringo[1] == 3

		when:
		map = beatles.asMap { it[0].toUpperCase() }

		then:
		map instanceof Map
		map.containsKey('PAUL')
		map.RINGO[1] == 3
	}

	// TODO : JPM 4/2016 : Need to implement tests for List.asGroup metaClass method
	void testAsGroup() {
	}
}
