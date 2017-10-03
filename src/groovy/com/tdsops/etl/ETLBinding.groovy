package com.tdsops.etl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 *
 * ETLBinding represents all the custom bindings associated a DSL ETL Script.
 *
 * It'll use from outside the script to pass variables into it.
 *
 */
class ETLBinding extends Binding {

    ETLDomainFieldsValidator fieldsMapper = new ETLDomainFieldsValidator()

    ETLBinding (Map vars) {
        this.variables.putAll([
                *: vars,
                *: DataPart.values().collectEntries { [(it.name()): it] }
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

    CompilerConfiguration getConfiguration () {

        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars ConsoleStatus.class.name
        customizer.addStaticStars DataPart.class.name

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer
        configuration.scriptBaseClass = ETLProcessorBaseScript.class.name

        configuration
    }


}
