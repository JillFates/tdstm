package com.tdsops.etl

class LocalVariableFacade {

	/**
	 *
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
	Element transform(Object withParameter) {
		Element element = new Element(
			value: wrappedValue,
			originalValue: wrappedValue,
			processor: etlProcessor
		)
		element.loadedElement = true
		return element
	}

	def methodMissing(String name, def args) {
		return wrappedValue.invokeMethod(name, args)
	}

	def propertyMissing(String name) {
		return wrappedValue[name]
	}

	def propertyMissing(String name, def arg) {

	}

	Boolean asBoolean() {
		return wrappedValue.asBoolean()
	}

	boolean equals(Object object) {
		if (!wrappedValue && object) return false
		if (!wrappedValue && !object) return true

		return wrappedValue.equals(object)
	}

	int hashCode() {
		return (wrappedValue != null ? wrappedValue.hashCode() : 0)
	}

	@Override
	String toString() {
		return wrappedValue?.toString()
	}
}
