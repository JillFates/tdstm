package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor

/**
 * The intent of this class is to create an instance of {@code ETLMap} based on 'defineETLMap' command configuration.<BR>
 * Given the following ETL script with 'defineETLMap' command:
 * <pre>
 *  read labels
 *  domain Device
 *  defineETLMap 'verni-devices' { //
 *      map 'device-name', 'Name'
 * 	    map 'description'
 * 	    map 'environment', 'Environment', substitute(['PROD':'Production', 'DEV', 'Development'])
 * 	    map 'zone', uppercase(), left(3)
 *}//
 * </pre>
 * <p>An instance of {@code ETLMapBuilder} can build an instance of {@code ETLMap},
 * reflecting all the configurations defined in 'defineETLMap' command</p>
 *
 * @see ETLProcessor#defineETLMap(java.lang.String, groovy.lang.Closure)
 * @see ETLMapBuilder#build(groovy.lang.Closure)
 */
class ETLMapBuilder {

    /**
     * An instance of {@code ETLDomain} is necessary
     * to build an instance of {@code ETLMap}
     */
    private ETLDomain domain
    /**
     * An instance of {@code ETLProcessor} is used for validations in 3 different places:
     *
     * 1) Validating a source name. ({@code ETLProcessor#checkColumnName})
     * 2) Validating a source position. ({@code ETLProcessor#rangeCheck})
     * 3) Validating a domain property name. ({@code ETLProcessor#lookUpFieldDefinition})
     */
    private ETLProcessor processor
    /**
     * Instance of {@code ETLMap} to be built using an instance of {@code ETLMapBuilder}
     */
    ETLMap etlMap
    /**
     * <p>Given the following ETL script example:</p>
     * <pre>
     *  defineETLMap 'verni-devices', { //
     *      map 'zone', uppercase(), left(3)
     * </pre>
     * <p>An instance of {@code ETLMapBuilder} is going to build an instance of {@oce ETLMapInstruction#transformation}</p>
     *
     * @see ETLMapBuilder#map(java.lang.String, com.tdsops.etl.etlmap.ETLMapTransform [ ])
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

    /**
     * <p>Build Step in this Builder pattern.</p>
     * <p>Once this method is invoked, and while closure param is being executed,
     * current instance of {@code ETLMapBuilder} is in charge to collect all the necessary data
     * to populate an instance of {@code ETLMap}.
     * Finally, instance of created {@code ETLMap} is returned.</p>
     *
     * @param closure a Closure to be executed
     * @return an instance of {@code ETLMap} based on closure params definition.
     *
     * @see ETLMapBuilder#map(java.lang.String, com.tdsops.etl.etlmap.ETLMapTransform [ ])
     * @see ETLMapBuilder#methodMissing(java.lang.String, java.lang.Object)
     */
    ETLMap build(Closure closure) {

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure()
        checkAndAddCurrentInstruction()
        return etlMap
    }

    /**
     * <p>{@code ETLMapBuilder} works as a stack of commands defined in a closure.</p>
     * <p>Each line of closure definition in 'defineETLMap' command
     * creates a new instance of {@code ETLMapInstruction}</p>
     * <p>For each new line in closure definition is invoked, is necessary to check
     * if there was a previous line defined and move it to the {@code ETLMap#instructions} list</p>
     *
     */
    private void checkAndAddCurrentInstruction() {
        if (currentInstruction) {
            etlMap.instructions.add(currentInstruction)
        }
        currentInstruction = new ETLMapInstruction()

    }
    /**
     * This method is invoked by closure builder when it defines same source name
     * and domain property values.
     * It can receive also a list of {@code ETLMapTransform} previously loaded
     * using {@code ETLMapBuilder#methodMissing} code.
     *
     * <pre>
     *  domain Device
     *  defineETLMap 'verni-devices' { //
     *      map 'Name'
     *      map 'assetName', uppercase()
     *      map 'Name', uppercase(), left(3)
     *  ...
     * </pre>
     * @param domainProperty
     * @param transformations
     * @return current instance of {@code ETLMapBuilder}
     */
    ETLMapBuilder map(String domainProperty, ETLMapTransform... transformations) {
        this.map(domainProperty, domainProperty, transformations)
        return this
    }
    /**
     * This method is invoked by closure builder when it defines a source position
     * and domain property values.
     * It can receive also a list of {@code ETLMapTransform} previously loaded
     * using {@code ETLMapBuilder#methodMissing} code.
     *
     * <pre>
     *  domain Device
     *  defineETLMap 'verni-devices' { //
     *      map 1, 'Name'
     *      map 1, 'assetName', uppercase()
     *      map 1, 'Name', uppercase(), left(3)
     *  ...
     * </pre>
     * @param sourcePosition
     * @param domainProperty
     * @param transformations
     * @return current instance of {@code ETLMapBuilder}
     */
    ETLMapBuilder map(Integer sourcePosition, String domainProperty, ETLMapTransform... transformations) {
        this.checkAndAddCurrentInstruction()
        this.defineSourcePosition(sourcePosition)
        this.defineDomainProperty(domainProperty)
        this.addTransformations(transformations)
        return this
    }
    /**
     * This method is invoked by closure builder when it defines a source name
     * and domain property values.
     * It can receive also a list of {@code ETLMapTransform} previously loaded
     * using {@code ETLMapBuilder#methodMissing} code.
     *
     * <pre>
     *  domain Device
     *  defineETLMap 'verni-devices' { //
     *      map 'device-name', 'Name'
     *      map 'device-name', 'assetName', uppercase()
     *      map 'device-name', 'Name', uppercase(), left(3)
     *  ...
     * </pre>
     * @param sourcePosition
     * @param domainProperty
     * @param transformations
     * @return current instance of {@code ETLMapBuilder}
     */
    ETLMapBuilder map(String sourceName, String domainProperty, ETLMapTransform... transformations) {
        this.checkAndAddCurrentInstruction()
        this.defineSourceName(sourceName)
        this.defineDomainProperty(domainProperty)
        this.addTransformations(transformations)
        return this
    }
    /**
     * Validates and defines source position checking dataset labels.
     * @param sourcePosition
     * @see ETLProcessor#rangeCheck(int, int)
     */
    private void defineSourcePosition(int sourcePosition) {
        processor.rangeCheck(sourcePosition - 1, processor.columns.size())
        currentInstruction.column = processor.columns.get(sourcePosition - 1)
    }
    /**
     * Validates and defines source name checking dataset labels.
     * @param sourceName
     * @see ETLProcessor#checkColumnName(java.lang.String)
     */
    private void defineSourceName(String sourceName) {
        processor.checkColumnName(sourceName)
        currentInstruction.column = processor.columnsMap[sourceName]
    }

    private void addTransformations(ETLMapTransform... transformations) {
        if (transformations) {
            currentInstruction.transformations.addAll(transformations)
        }
    }
    /**
     * Validates and defines domain property.
     * @param domainProperty
     * @see ETLProcessor#lookUpFieldDefinition(com.tdsops.etl.ETLDomain, java.lang.String)
     */
    private void defineDomainProperty(String domainProperty) {
        this.currentInstruction.domainProperty = processor.lookUpFieldDefinition(this.etlMap.domain, domainProperty)
    }

    /**
     * Overriding methodMissing this class collects
     * transformation methods and its params to be invoked later.
     * <p>Given the following ETL script example:</p>
     * <pre>
     *  ...
     *  defineETLMap 'verni-devices', { //
     *      map 'name', 'assetName, uppercase(), left(3)
     *      ...
     * </pre>
     * <p>An instance of {@code ETLMapBuilder} is going to convert it
     * in two instances of {@code ETLMapTransform}:</p>
     * <pre>
     *  transformation1 = new ETLMapTransform('uppercase', [])
     *  transformation2 = new ETLMapTransform('left', [3])
     * </pre>
     * After that, a method of {@code ETLMapBuilder} is call using the following arguments:
     * <pre>
     *  map('name', assetName, [transformation1, transformation2])
     * </pre>

     * @param name missing method name
     * @param args an array with method arguments
     * @return the current instance of {@code ETLMapBuilder}
     */
    def methodMissing(String name, def args) {
        // TODO : JM : 1/2020 - Examine the Element class to see if there is a method with this signature and throw exception if not
        return new ETLMapTransform(name, args)
    }

}
