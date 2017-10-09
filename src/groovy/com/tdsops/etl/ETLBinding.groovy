package com.tdsops.etl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper

/**
 *
 * ETLBinding represents all the custom bindings associated a DSL ETL Script.
 *
 * It'll use from outside the script to pass variables into it.
 *
 */
class ETLBinding extends Binding {

    ETLBinding (ETLProcessor etlProcessor, Map vars = [:]) {
        this.variables.putAll([
                *: etlProcessor.metaClass.methods.collectEntries {
                    [(it.name): InvokerHelper.getMethodPointer(etlProcessor, it.name)]
                },
                *: vars
        ])
    }

    /**
     *
     * Custom lookup variable
     *
     * @param name
     * @return
     */
    @Override
    Object getVariable (String name) {

        if (variables == null)
            throw new MissingPropertyException(name, this.getClass())

        Object result = variables.get(name)

        if (result == null && !variables.containsKey(name)) {
            //throw new MissingPropertyException(name, this.getClass())
            result = name
        }

        result
    }
}
