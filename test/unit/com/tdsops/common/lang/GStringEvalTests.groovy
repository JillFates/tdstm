package com.tdsops.common.lang

import spock.lang.Specification

class GStringEvalTests extends Specification {

	void testDefaultConstructor() {
		given:
    	def e = new GStringEval()

		expect:
		'name is Jack' == e.toString('name is ${it.name}', [name: 'Jack', gender: 'm'])
		'name is Jill' == e.toString('name is ${it.name}', [name: 'Jill', gender: 'f'])
		'name is Pat' == e.toString('name is ${it.name}', [name: 'Pat', gender: 'not sure'])
	}

	void testTemplateConstructor() {
		given:
		def e = new GStringEval('name is ${it.name}')

		expect:
		'name is Jack' == e.toString(name: 'Jack')
		'name is Jill' == e.toString(name: 'Jill')
		// Make sure that passing a template still works
		'1+1=2' == e.toString('1+1=${it.answer}', [answer: 2])
		// And that the original template is still in tack
		'name is Tommy' == e.toString(name: 'Tommy')
	}

	/*
	 * Tests that an exception is thrown if code invokes the toString without passing a template and used the default constructor
	 */
	void testForException() {
		given:
		def e = new GStringEval()

		when:
		'name is Jack' == e.toString(name: 'Jack')

		then:
		thrown(RuntimeException)
	}
}
