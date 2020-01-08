package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.Element
import org.codehaus.groovy.runtime.InvokerHelper
import groovy.transform.CompileStatic

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
        for (ETLMapInstruction instruction in instructions) {
            loadInstruction(instruction, processor)
        }
    }

    /**
     * <p>This method completes the 3 commands configured in an instance of {ETLMapInstruction}</p>
     * <p>'extract' command: using defined {@code ETLMapInstruction#column}
     * it creates an instance of {@code Element} through the {@code ETLProcessor#doExtract} method.</p>
     * <p>'transform with' command:If there are instances of {@code ETLMapTransform} in {@code ETLMapInstruction#transformations},
     * it uses that information to invoke transformation through the {@code InvokerHelper}</p>
     * <p>'load' command: Finally, it load instance of {@code Element} created through the
     * {@code ETLProcessor#addElementLoaded}. NOTE: Peviously populate {@code Element#fieldDefinition}
     * to avoid the validation do it by {@code ETLProcessor}. This is to avoid twice invocation
     * for the loopup of a domain property name.</p>
     *
     * @param instruction an instance of {@code ETLMapInstruction}
     * @param processor an instance of {@code ETLProcessor}
     */
    private void loadInstruction(ETLMapInstruction instruction, ETLProcessor processor) {

        // 1) 'extract' command
        Element element = processor.doExtract(instruction.column.index)

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

        //3) 'load' command
        element.fieldDefinition = instruction.domainProperty
        processor.addElementLoaded(element)
    }

}
