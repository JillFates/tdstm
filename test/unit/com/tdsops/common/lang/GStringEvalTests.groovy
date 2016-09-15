package com.tdsops.common.lang

import grails.test.*

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the GStringEval class
 */
class GStringEvalTests extends Specification {

	public void testDefaultConstructor() {
    	def e = new GStringEval()
    	expect:
			'name is Jack'.equals(e.toString('name is ${it.name}', [name:'Jack', gender:'m']))
			'name is Jill'.equals(e.toString('name is ${it.name}', [name:'Jill', gender:'f']))
			'name is Pat'.equals(e.toString('name is ${it.name}', [name:'Pat', gender:'not sure']))
	}

	public void testTemplateConstructor() {
    	def e = new GStringEval('name is ${it.name}')
    	expect:
			'name is Jack'.equals(e.toString([name:'Jack']))
			'name is Jill'.equals(e.toString([name:'Jill']))
			// Make sure that passing a template still works
			'1+1=2'.equals(e.toString('1+1=${it.answer}', [answer:2]))
			// And that the original template is still in tack
			'name is Tommy'.equals(e.toString([name:'Tommy']))
	}

	/*
	 * Tests that an exception is thrown if code invokes the toString without passing a template and used the default constructor
	 */
	public void testForException() {
    	def e = new GStringEval()
    	when:
    		'name is Jack'.equals(e.toString([name:'Jack']))
    	then:
    		thrown(RuntimeException)
	}
}
