package com.tdsops.common.lang

import groovy.text.SimpleTemplateEngine

/**
 * GStringEval class is used to perform late evaluations of a String as a GString where by which a map is passed
 * to the toString method and the template string is then evaluated. This implementation requires that the template
 * references the map values as the variable 'it'. When defining the template make sure to use single quotes or escape
 * the dollar sign (e.g. "blah \${it.value}" ) so that the evaluation doesn't occur immediately.
 *
 * The class has two constructors allowing for templates to be evaluated once or on each toString invocation.
 *
 * Usage:
 *    def e = new GStringEval('The user is ${it.username}')
 *    println e.toString( [username:'xyzzy'] )
 *    println e.toString('A different template ${it.now}', [now:new Date()])
 */
class GStringEval {
	def it
	def engine
	def template

	public GStringEval() {
		engine = new SimpleTemplateEngine()
	}
	public GStringEval( temp ) {
		engine = new SimpleTemplateEngine()
		template = engine.createTemplate( temp )
	}

	/**
	 * Evaluates a GString template with a map of values
	 * @param template - a String or GString template
	 * @param props - a map of values
	 * @return String - the evaluated template
	 */
	String toString(template, props) {
		it = props
		engine.createTemplate( template ).make(this.properties).toString()
	}

	/**
	 * Evaluates the GString template passed in the constructor with a map of values
	 * @param props - a map of values
	 * @return String - the evaluated template
	 * @throws RuntimeException if invoked without calling the GStringEval(template) constructor
	 */
	String toString(props) {
		if (! template ) throw new RuntimeException('GStringEval class not properly initialized')
		it = props
		template.make(this.properties).toString()
	}
}
