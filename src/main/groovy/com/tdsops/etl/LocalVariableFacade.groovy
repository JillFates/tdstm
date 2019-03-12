package com.tdsops.etl
/**
 * <p>Defines a wrapper over a local variable.</p>
 * <p>User can define local variables: </p>
 * <pre>
 * 	...
 * 	extract 1 set nameVar
 * 	...
 * </pre>
 * <p>Then set command adds a local variable in an ETL Binding content</p>
 * @see ETLBinding* @see Element#set(com.tdsops.etl.LocalVariableDefinition)
 */
class LocalVariableFacade {

	/**
	 * Wrapped instance. It contains original value.
	 */
	Object wrappedValue

	/**
	 * Instance of {@code ETLProcessor} used to continue in the chain of methods
	 */
	ETLProcessor etlProcessor

	LocalVariableFacade(Object wrappedValue, ETLProcessor etlProcessor) {
		this.wrappedValue = wrappedValue
		this.etlProcessor = etlProcessor
	}

	/**
	 * <p>It creates an instance of {@code Element} class
	 * in order to continue with the chain of transformation
	 * after a local variable in an ETL script.</p>
	 * @param withParameter
	 * @return an instance of {@code Element}
	 */
	def transform(Object withParameter) {
		Element element = new Element(
			value: wrappedValue,
			originalValue: wrappedValue,
			processor: etlProcessor
		)
		element.loadedElement = true
		return element
	}

	/**
	 * <p>Override method missing to manage called method for the wrapped value.</p>
	 *
	 * @param name a method name
	 * @param args method arguments
	 * @return result of method invokation on wrappedValue
	 */
	def methodMissing(String name, def args) {
		return wrappedValue?.invokeMethod(name, args)
	}
	/**
	 *
	 * @param name
	 * @return
	 */
	def propertyMissing(String name) {
		return wrappedValue[name]
	}

	/**
	 * <p>Overrides Groovy Truth</p>
	 * <p></p>
	 * http://gr8labs.org/getting-groovy/
	 * @return
	 *
	 */
	Boolean asBoolean() {
		return !!wrappedValue
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	boolean equals(Object object) {
		if (!wrappedValue && object) return false
		if (!wrappedValue && !object) return true

		return wrappedValue.equals(object)
	}
	/**
	 *
	 * @return
	 */
	int hashCode() {
		return (wrappedValue != null ? wrappedValue.hashCode() : 0)
	}

	@Override
	String toString() {
		return wrappedValue?.toString()
	}
}
