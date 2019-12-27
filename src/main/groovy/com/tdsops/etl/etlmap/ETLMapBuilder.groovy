package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator

class ETLMapBuilder {

    private ETLDomain domain
    private ETLFieldsValidator fieldsValidator
    private ETLMap etlMap
    private ETLMapInstruction currentInstruction
    /**
     * <p>Given the following ETL script example:</p>
     * <pre>
     *  defineETLMap 'verni-devices', { //
     *      add 'zone', uppercase(), left(3)
     * </pre>
     * <p>An instance of {@code ETLMapBuilder} is going to build the following Map:</p>
     * <pre>
     *
     * </pre>
     */
    private Map<String, Object> propertiesMap = [:]

    /**
     * Builder Implemantation for {@code ETLMap} instance creation
     */
    ETLMapBuilder(ETLDomain domain, ETLFieldsValidator fieldsValidator) {
        this.domain = domain
        this.fieldsValidator = fieldsValidator
        this.etlMap = new ETLMap(domain)
    }

    ETLMap build(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure()
        checkAndAddCurrentInstruction()
        return etlMap
    }

    private void checkAndAddCurrentInstruction() {
        if (this.currentInstruction) {
            etlMap.instructions.add(this.currentInstruction)
        }
        this.currentInstruction = new ETLMapInstruction()

    }

    ETLMapBuilder add(String fieldName, ETLMapTransform...transformers) {
        this.add(fieldName, fieldName)
        return this
    }

    ETLMapBuilder add(Integer sourcePosition, String fieldName, ETLMapTransform...transformers) {
        checkAndAddCurrentInstruction()
        this.currentInstruction.sourcePosition = sourcePosition
        this.defineDomainProperty(fieldName)
        return this
    }

    ETLMapBuilder add(String columnName, String fieldName, ETLMapTransform...transformers) {
        checkAndAddCurrentInstruction()
        this.currentInstruction.sourceName = columnName
        this.defineDomainProperty(fieldName)
        return this
    }

    private void defineDomainProperty(String fieldName) {
        this.currentInstruction.domainProperty = fieldsValidator.lookup(this.etlMap.domain, fieldName)
    }

    /**
     * Overriding methodMissing this class collects
     * transformation methods and its params to be invoked later.
     *
     * @param name missing method name
     * @param args an array with method arguments
     * @return the current instance of {@code ETLMapBuilder}
     */
    def methodMissing(String name, def args) {
        propertiesMap[name] = args
        this
    }

}
