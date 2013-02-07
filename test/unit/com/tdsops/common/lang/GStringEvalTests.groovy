package com.tdsops.common.lang

import grails.test.*

class GStringEvalTests  extends GrailsUnitTestCase {
	
	public void testDefaultConstructor() {
    	def e = new GStringEval()
		assertEquals 'name is Jack', e.toString('name is ${it.name}', [name:'Jack', gender:'m'])
		assertEquals 'name is Jill', e.toString('name is ${it.name}', [name:'Jill', gender:'f'])
		assertEquals 'name is Pat', e.toString('name is ${it.name}', [name:'Pat', gender:'not sure'])
	}
	
	public void testTemplateConstructor() {
    	def e = new GStringEval('name is ${it.name}')
		assertEquals 'name is Jack', e.toString([name:'Jack'])
		assertEquals 'name is Jill', e.toString([name:'Jill'])

		// Make sure that passing a template still works
		assertEquals '1+1=2', e.toString('1+1=${it.answer}', [answer:2])
		
		// And that the original template is still in tack
		assertEquals 'name is Tommy', e.toString([name:'Tommy'])
		
	}
	
	/*
	 * Tests that an exception is thrown if code invokes the toString without passing a template and used the default constructor
	 */
	public void testForException() {
    	def e = new GStringEval()
		shouldFail(RuntimeException) {
			assertEquals 'name is Jack', e.toString([name:'Jack'])
		}
	}
}