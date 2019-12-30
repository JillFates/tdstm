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

    ETLMapBuilder add(String domainProperty, ETLMapTransform... transformations) {
        this.add(domainProperty, domainProperty, transformations)
        return this
    }

    ETLMapBuilder add(Integer sourcePosition, String domainProperty, ETLMapTransform... transformations) {
        checkAndAddCurrentInstruction()
        currentInstruction.sourcePosition = sourcePosition
        this.defineDomainProperty(domainProperty)
        this.addTransformations(transformations)
        return this
    }

    ETLMapBuilder add(String columnName, String domainProperty, ETLMapTransform... transformations) {
        checkAndAddCurrentInstruction()
        currentInstruction.sourceName = columnName
        this.defineDomainProperty(domainProperty)
        this.addTransformations(transformations)
        return this
    }

    private void addTransformations(ETLMapTransform... transformations) {
        if (transformations) {
            currentInstruction.transformations.addAll(transformations)
        }
    }

    private void defineDomainProperty(String domainProperty) {
        this.currentInstruction.domainProperty = fieldsValidator.lookup(this.etlMap.domain, domainProperty)
    }

    /**
     * Overriding methodMissing this class collects
     * transformation methods and its params to be invoked later.
     * <p>Given the following ETL script example:</p>
     * <pre>
     *  defineETLMap 'verni-devices', { //
     *      add 'zone', uppercase(), left(3)
     * </pre>
     * <p>An instance of {@code ETLMapBuilder} is going to convert it
     * in two instances of {@code ETLMapTransform}:</p>
     * <pre>
     *  new ETLMapTransform('uppercase', [])
     *  new ETLMapTransform('left', [3])
     * </pre>
     * @param name missing method name
     * @param args an array with method arguments
     * @return the current instance of {@code ETLMapBuilder}
     */
    def methodMissing(String name, def args) {
        return new ETLMapTransform(name, args?.toList())
    }

}
