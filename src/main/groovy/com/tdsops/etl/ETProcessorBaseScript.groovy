package com.tdsops.etl

/**
 * In ETL script context, we can use a base script class for delegating method to the {@code ETLProcessor}.
 * We can also implement {@code ETProcessorBaseScript#methodMissing} behaviour when script is trying to use
 * a non existing method in the {@code ETLProcessor}.
 * These are examples of the mentioned scenario:
 *
 */
abstract class ETProcessorBaseScript extends Script {

	/**
	 * Validates calls within the DSL mscript that can not be managed
	 *
	 * myDomain variable
	 * @param methodName
	 * @param args
	 */
	def methodMissing(String methodName, args) {
		//debugConsole.info "Method missing: ${methodName}, args: ${args}"

		throw ETLProcessorException.methodMissing(methodName, args)
	}
}
