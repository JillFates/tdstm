package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 * A facade object to be used in ETL script using the following syntax:
 * <pre>
 *     find Application of id by id with DOMAIN.id
 *     ...
 *     find Application of id by id with DOMAIN['id']
 * </pre>
 * Where id property is the value in the current row data for the column 'id'
 */
@CompileStatic
class DomainFacade {

	private ETLProcessorResult result

	DomainFacade(ETLProcessorResult result) {
		this.result = result
	}

	/**
	 * Return property value
	 * <pre>
	 *     find Application of id by id with DOMAIN.id
	 * </pre>
	 * @param name a property name
	 * @return 
	 */
	Object getProperty(String name) {
		return result.getFieldValue(name)
	}

	/**
	 * Traps EQL expression with undefined variable therefore throws an exception
	 * <pre>
	 * 	DOMAIN[aNotPreviouslyDefinedVariable]
	 * </pre>
	 * @param localVariableDefinition
	 * @throws {@code ETLProcessorException}
	 * @see {@code ETLProcessorException.missingPropertyException}
	 */
	Object getProperty(LocalVariableDefinition localVariableDefinition){
		throw ETLProcessorException.missingPropertyException(localVariableDefinition.name)
	}


	/**
	 * Return property value
	 * <pre>
	 *     find Application of id by id with DOMAIN['id']
	 * </pre>
	 * @param name a property name
	 * @return
	 */
	Object getAt(String name) {
		return result.getFieldValue(name)
	}

	/**
	 * Traps EQL expression with undefined variable therefore throws an exception
	 * <pre>
	 * 	DOMAIN[aNotPreviouslyDefinedVariable]
	 * </pre>
	 * @param localVariableDefinition
	 * @throws {@code ETLProcessorException}
	 * @see {@code ETLProcessorException.missingPropertyException}
	 */
	Object getAt(LocalVariableDefinition localVariableDefinition){
		throw ETLProcessorException.missingPropertyException(localVariableDefinition.name)
	}

	/**
	 * Returns <tt>true</tt> if {@code ETLProcessorResult#currentRow} map
	 * contains a value for the specified property name.
	 * More formally, returns <tt>true</tt> if and only if
	 * this {@code ETLProcessorResult#currentRow#fields} map
	 * contains a mapping for a key with propertyName param.
	 *
	 * @param propertyName an domain property name
	 * @return true or false if {@code ETLProcessorResult#currentRow#fields}
	 * 			contains a field name or not.
	 */
	Boolean hasProperty(String propertyName){
		return result.hasColumn(propertyName)
	}

	/**
	 * Traps EQL expression with undefined variable therefore throws an exception
	 * <pre>
	 * 	DOMAIN.hasProperty(aNotPreviouslyDefinedVariable)
	 * </pre>
	 * @param localVariableDefinition
	 * @throws {@code ETLProcessorException}
	 * @see {@code ETLProcessorException.missingPropertyException}
	 */
	Boolean hasProperty(LocalVariableDefinition localVariableDefinition){
		throw ETLProcessorException.missingPropertyException(localVariableDefinition.name)
	}
	/**
	 * Returns a {@code ETLProcessorResult#currentRow} to be used in {@code DependencyBuilder}
	 * @return
	 */
	RowResult currentRowMap(){
		return this.result.currentRow()
	}

	@Override
	String toString() {

		List<?> fieldsMap = this.result.currentRow()?.fields?.collect {String fieldName, FieldResult fieldResult ->
			[(fieldName): fieldResult.value]
		}
		return """DOMAIN {
		 	fields=${fieldsMap}
		}"""
	}
}

