package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.Element
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * The intent of this class is to maintenance 'defineETLMap' command.<BR>
 * In an ETL Script user can define an instance of {@code ETLMap} using the following command:
 * <pre>
 *  read labels
 *  domain Device
 *  defineETLMap 'verni-devices' { //
 *      add 'device-name', 'Name'
 * 	    add 'description'
 * 	    add 'environment', 'Environment', substitute(['PROD':'Production', 'DEV', 'Development'])
 * 	    add 'zone', uppercase(), left(3)
 *}//
 * </pre>
 * Once user defines an instance of {@code ETLMap}, it can be used in 'loadETLMap':
 * <pre>
 *  iterate {//
 *      loadETLMap 'verni-devices'
 *}//
 * </pre>
 * That 'loadETLMap' command is going to execute the following script:
 * <pre>
 *  iterate {//
 *      extract 'device-name' load 'assetName'
 *      extract 'description' load 'description'
 *      extract 'environment' transform with substitute(['PROD':'Production', 'DEV', 'Development']) load 'Environment'
 *      extract 'zone' transform with uppercase() and left(3) load 'zone'
 *}//
 * </pre>
 */
@CompileStatic
class ETLMap {
    /**
     * Saves the {@code ETLDomain} with which it was created.
     */
    ETLDomain domain
    /**
     * Saves a List of {@code ETLMapInstruction} created by an instance of {@code ETLMapBuilder}
     */
    List<ETLMapInstruction> instructions = []

    ETLMap(ETLDomain domain) {
        this.domain = domain
    }

    /**
     * Loads current instance of {@code ETLMap}
     * and the list of {@code ETLMapInstruction}.
     * <p>Initially it sets current domain on {@code ETLProcessor}. See {@code ETLProcessor#domain} method </p>
     * <p>Then, For each instance of {@code ETLMapInstruction} it calls {@code ETLMap#loadInstruction} method. </p>
     *
     * @param processor an instance of {@code ETLProcessor}
     */
    void load(ETLProcessor processor) {

        processor.domain(this.domain)
        List<Element> rowElements = []
        for (ETLMapInstruction instruction in instructions) {
            rowElements.add(createElementFromInstruction(instruction, processor))
        }
        processor.result.loadElements(rowElements)
    }

    /**
     * <p>This method completes the 3 commands configured in an instance of {ETLMapInstruction}</p>
     * <p>'extract' command: using defined {@code ETLMapInstruction#column}
     * it creates an instance of {@code Element} through the {@code ETLProcessor#doExtract} method.</p>
     * <p>'transform with' command:If there are instances of {@code ETLMapTransform} in {@code ETLMapInstruction#transformations},
     * it uses that information to invoke transformation through the {@code InvokerHelper}</p>
     * <p>'load' command: Finally, it load instance of {@code Element} created through the
     * {@code ETLProcessor#addElementLoaded}. NOTE: Previously populate {@code Element#fieldDefinition}
     * to avoid the validation do it by {@code ETLProcessor}. This is to avoid twice invocation
     * for the lookup of a domain property name.</p>
     *
     * @param instruction an instance of {@code ETLMapInstruction}
     * @param processor an instance of {@code ETLProcessor}
     */
    private Element createElementFromInstruction(ETLMapInstruction instruction, ETLProcessor processor) {

        // 1) 'extract' command
        //Object value = processor.currentRow.getDataSetElement(instruction.column.index)
        Object value = processor.currentRow.dataSetValues[instruction.column.index]

        Element element = new Element(
                originalValue: value,
                value: value,
                processor: processor)

        processor.applyGlobalTransformations(element)

        // 2) 'transform with' command
        for (ETLMapTransform transformation in instruction.transformations) {
            // Invoking method dynamically eliminates the @CompileStatic - not sure if it is faster than the older code
            /*
            if (transformation.parameters) {
                element = (Element) element."${transformation.methodName}"(transformation.parameters)
            } else {
                element = (Element) element."${transformation.methodName}"()
            }
            */
            element = (Element) InvokerHelper.invokeMethod(element, transformation.methodName, transformation.parameters ?: null)

        }

        element.fieldDefinition = instruction.domainProperty
        return element
    }

}
