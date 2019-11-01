package com.tdsops.etl


/**
 * This traits validates if an instance of {@code LocalVariableDefinition}
 * is passed incorrectly to an ETL command.
 * Basically, there are 2 commands to create local variables.
 * <pre>
 * 	iterate {
 * 		domain Application
 * 	    ...
 * 		set environmentVariable with 'Production'
 * 		set env with SOURCE.'application id'
 * 		set environment with DOMAIN.id
 * 		.....
 * }
 * </pre>
 * <pre>
 * 	iterate { ...
 * 		extract 3 transform with lowercase() set myLocalVariable
 * 		.....
 * }
 * </pre>
 * In all the other scenarios, receiving an instance of {@code LocalVariableDefinition}
 * is an error.
 *
 * @see ETLProcessor#set(com.tdsops.etl.LocalVariableDefinition)
 * @see ETLBinding#getVariable(java.lang.String)
 * @see Element#with(com.tdsops.etl.LocalVariableDefinition)
 * @see FindStatementBuilder#eq(com.tdsops.etl.LocalVariableDefinition)
 */

trait UndefinedLocalVariableValidator {

	/**
	 * Check if one of the objects param contains an undefined variable.
	 * Undefined variables are detected when an instance of {@code LocalVariableDefinition}
	 * is pass incorrectly as an argument in ETL commands.
	 *
	 * @param objects a List of Object
	 */
	void checkUndefinedLocalVariables(Object... objects) {
		for (Object object : objects) {
			checkUndefinedLocalVariable(object)
		}
	}

	/**
	 * Check if object param is an instance of {@code LocalVariableDefinition}
	 *
	 * @param object an object used in ETL command parameters
	 */
	void checkUndefinedLocalVariable(Object object) {
		if (object instanceof LocalVariableDefinition) {
			LocalVariableDefinition localVariableDefinition = (LocalVariableDefinition) object
			throw ETLProcessorException.missingPropertyException(localVariableDefinition.name)
		}
	}
}