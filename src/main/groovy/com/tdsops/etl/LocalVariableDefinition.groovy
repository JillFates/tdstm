package com.tdsops.etl

/**
 * Used to create a Local variable definition in binding context.
 * It is used for validation where variables need to be defined.
 *
 * @see ETLBinding#getVariable(java.lang.String)
 * @see Element#with(com.tdsops.etl.LocalVariableDefinition)
 * @see FindStatementBuilder#eq(com.tdsops.etl.LocalVariableDefinition)
 */
class LocalVariableDefinition {

	String name

	LocalVariableDefinition(String name) {
		this.name = name
	}
}
