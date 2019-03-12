package com.tdsops.etl

import org.codehaus.groovy.runtime.MethodClosure

/**
 * Trait used to add dynamically the 'transform' method in a local variable
 * to continue with the chain of transformation methods in an ETL command.
 * <pre>
 * 	iterate {* 		extract 'name' set nameVar
 * 		nameVar transform with middle(3, 2) uppercase() set upperNameVar
 * 		load 'Name' with upperNameVar
 *}* </pre>
 */
trait ETLTransformable {

	/**
	 * Instance of {@code ETLProcessor} used to continue in the chain of methods
	 */
	ETLProcessor etlProcessor
	/**
	 * It creates an instance of {@code Element} class
	 * in order to continue with the chain of transformation
	 * after a local variable in an ETL script.
	 * @param withParameter
	 * @return
	 */
	Element transform(ETLProcessor.ReservedWord withParameter) {
		Element element = new Element(
			value: this,
			originalValue: this,
			processor: etlProcessor
		)
		element.loadedElement = true
		return element
	}

	/**
	 *
	 * http://mrhaki.blogspot.com/2017/02/groovy-goodness-using-call-operator.html
	 * @param methodClosure
	 * @return
	 */
	def call(MethodClosure methodClosure) {
		return methodClosure.call()
	}
}