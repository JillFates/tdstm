package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor

class ETLMapBuilder {

    private ETLDomain domain
    private ETLProcessor processor
    private ETLMap etlMap
    /**
     * <p>Given the following ETL script example:</p>
     * <pre>
     *  defineETLMap 'verni-devices', { //
     *      add 'zone', uppercase(), left(3)
     * </pre>
     * <p>An instance of {@code ETLMapBuilder} is going to build an instance of {@oce ETLMapInstruction#transformation}:</p>
     *
     * @see ETLMapBuilder#add(java.lang.String, com.tdsops.etl.etlmap.ETLMapTransform [ ])
     * @see ETLMapBuilder#methodMissing(java.lang.String, java.lang.Object)
     */
    private ETLMapInstruction currentInstruction


    /**
     * Builder Implementation for {@code ETLMap} instance creation
     */
    ETLMapBuilder(ETLDomain domain, ETLProcessor processor) {
        this.domain = domain
        this.processor = processor
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
        this.defineSourcePosition(sourcePosition)
        this.defineDomainProperty(domainProperty)
        this.addTransformations(transformations)
        return this
    }
    /**
     * Defines a
     * @param sourcePosition
     */
    private void defineSourcePosition(int sourcePosition) {
        processor.rangeCheck(sourcePosition - 1, processor.columns.size())
        currentInstruction.column = processor.columns.get(sourcePosition - 1)
    }

    ETLMapBuilder add(String sourceName, String domainProperty, ETLMapTransform... transformations) {
        checkAndAddCurrentInstruction()
        this.defineSourceName(sourceName)
        this.defineDomainProperty(domainProperty)
        this.addTransformations(transformations)
        return this
    }

    private void defineSourceName(String sourceName) {
        processor.checkColumnName(sourceName)
        currentInstruction.column = processor.columnsMap[sourceName]
    }

    private void addTransformations(ETLMapTransform... transformations) {
        if (transformations) {
            currentInstruction.transformations.addAll(transformations)
        }
    }

    private void defineDomainProperty(String domainProperty) {
        this.currentInstruction.domainProperty = processor.lookUpFieldDefinition(this.etlMap.domain, domainProperty)
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
