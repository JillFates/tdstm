package com.tdsops.etl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class ETLBinding extends Binding {

    ETLFieldsMapper fieldsMapper = new ETLFieldsMapper()

    private Map variables

    ETLBinding(Map vars) {

        this.variables = [
                *: vars,
                *: DomainAssets.values().collectEntries { [(it.name()): it] }
        ]
    }

    @Override
    Object getVariable(String name) {

        if (variables == null)
            throw new MissingPropertyException(name, this.getClass())

        Object result = variables.get(name)

        if (result == null && !variables.containsKey(name)) {
            //throw new MissingPropertyException(name, this.getClass())
            result = name
        }

        result
    }

    CompilerConfiguration getConfiguration() {
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DomainAssets.class.name
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer
        configuration.scriptBaseClass = ETLProcessorBaseScript.class.name

        configuration
    }


}
