package com.tdsops.etl

import org.codehaus.groovy.runtime.InvokerHelper

/**
 * ETLBinding represents all the custom bindings associated a DSL ETL Script.
 * It'll use from outside the script to pass variables into it.
 */
class ETLBinding extends Binding {

    Set dynamicVariables = [] as Set

    ETLBinding (ETLProcessor etlProcessor, Map vars = [:]) {
        this.variables.putAll([
                *: etlProcessor.metaClass.methods.collectEntries {
                    [(it.name): InvokerHelper.getMethodPointer(etlProcessor, it.name)]
                },
	            *: ETLProcessor.ReservedWord.values().collectEntries { [(it.name()): it] },
	            *: ETLDomain.values().collectEntries { [(it.name()): it] },
                *: vars,
				concat: ETLTransformation.&concat,
        ])
    }

    /**
     * Custom lookup variable
     * @param name
     * @return
     */
    @Override
    Object getVariable (String name) {

        if (variables == null) {
	        throw new MissingPropertyException(name, this.getClass())
        }

        Object result = variables.get(name)

        if (result == null && !variables.containsKey(name)) {
	        // TM-10103: We are (in a nasty way) forcing local variables ending with this sufix names
	        // Now set command is working with:
	        // set environmentVar with 'Production'
	        if(isValidETLVariableName(name)){
		        result = name
	        } else {
		        throw ETLProcessorException.missingPropertyException(name)
	        }
        }
        // TODO : JPM 7/2018 : Check to see if this is a Var variable and if so then return the myVar.value instead

        return result
    }

    /**
     * Adds a new Dynamic variable from an ETL script.
     * It uses name parameter to define it in an internal map definition.
     * @param name the name of the variable to be added dynamically within the binding context.
     * @param value the ETL Element define for the name variable
     */
    void addDynamicVariable (String name, def value) {
	    dynamicVariables.add(name)
	    this.variables[name] = value
    }

    /**
     * Adds a new Global variable from an ETL script.
     * It uses name parameter to define it in an internal map definition.
     * @param name the name of the variable to be added dynamically within the binding context.
     * @param value the ETL Element define for the name variable
     */
    void addGlobalVariable (String name, def value) {
        this.variables[name] = value
    }

    /**
     * Removes all the dynamic variables added by an ETL script.
     */
    void removeAllDynamicVariables () {
        dynamicVariables.each {variables.remove(it)}
    }

	/**
	 * Validates if an ETL variable name is correctly defined.
	 * An ETL variable name must end with 'Var' postfix.
	 * @param variableName
	 * @return true if variable name is valid or false in all the other cases.
	 */
	boolean isValidETLVariableName(String variableName){
		return variableName?.endsWith('Var')
	}

}
