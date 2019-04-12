package com.tdsops.etl

import com.tdsops.etl.ETLProcessor.ReservedWord

/**
 * <p>Defines a wrapper over a local variable.</p>
 * <p>User can define local variables: </p>
 * <pre>
 * 	...
 * 	extract 1 set nameVar
 * 	...
 * </pre>
 * <p>Then set command adds a local variable in an ETL Binding content</p>
 * @see ETLBinding
 * @see Element#set(com.tdsops.etl.LocalVariableDefinition)
 */
class LocalVariableFacade {
	/**
	 * Wrapped instance. It contains original value.
	 */
	Object wrappedObject
	/**
	 * Instance of {@code ETLProcessor} used to continue in the chain of methods
	 */
	ETLProcessor etlProcessor

	LocalVariableFacade(Object wrappedObject, ETLProcessor etlProcessor) {
		this.wrappedObject = wrappedObject
		this.etlProcessor = etlProcessor
	}
	/**
	 * <p>It creates an instance of {@code Element} class
	 * in order to continue with the chain of transformation
	 * after a local variable in an ETL script.</p>
	 * @param withParameter
	 * @return an instance of {@code Element}
	 */
	Element transform(ReservedWord withParameter) {
		Element element = new Element(
			value: wrappedObject,
			originalValue: wrappedObject,
			processor: etlProcessor
		)
		element.loadedElement = true
		return element
	}
	/**
	 * <p>Override method missing to manage called method for the wrapped value.</p>
	 * <pre>
	 *	LocalVariableFacade localVariableFacade = new LocalVariableFacade('FOOBAR', etlProcessor)
	 *  ...
	 *  localVariableFacade.toLowerCase()
	 * </pre>
	 * <p>This method is forwarding method {@code toLowerCase()} to the String 'FOOBAR'</p>
	 * @param name a method name
	 * @param args method arguments
	 * @return result of method invokation on wrappedObject
	 */
	def methodMissing(String name, def args) {
		return wrappedObject?.invokeMethod(name, args)
	}
	/**
	 * Fowards a property invokation to the wrappedObject
	 * @param name
	 * @return
	 */
	def propertyMissing(String name) {
		return wrappedObject[name]
	}

	/**
	 * <p>Overrides Groovy Truth</p>
	 * <pre>
	 *     LocalVariableFacade localVariableFacade = new LocalVariableFacade(true, etlProcessor)
	 *     if(localVariableFacade){
	 *         ....
	 *     }
	 * </pre>
	 * @return boolean groovy truth for {@code wrappedObject}
	 */
	Boolean asBoolean() {
		return !!wrappedObject
	}

	/**
	 * <p>Overrides equals method forwarding invokation to the {@code wrappedObject}</p>
	 * @param   obj   the reference object with which to compare.
	 * @return  {@code true} if this {@code wrappedObject} is the same as the obj
	 *          argument; {@code false} otherwise.
	 */
	boolean equals(Object object) {
		return wrappedObject.equals(object)
	}

	/**
	 *
	 * @return
	 */
	int hashCode() {
		return (wrappedObject != null ? wrappedObject.hashCode() : 0)
	}

	@Override
	String toString() {
		return wrappedObject?.toString()
	}
}
