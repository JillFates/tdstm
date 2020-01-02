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
    // The name of the domain class that the map was defined for
    ETLDomain domain
    List<ETLMapInstruction> instructions = []

    ETLMap(ETLDomain domain) {
        this.domain = domain
    }

    /**
     * Loads current instance of {@code ETLMap}
     * and the list of {@code ETLMapInstruction}.
     * <p>1) Initially it sets current domain on {@code ETLProcessor} </p>
     * <p></p>
     * <p></p>
     * <p></p>
     * <p></p>
     * <p></p>
     *
     * @param processor
     */
    void load(ETLProcessor processor) {

        processor.domain(this.domain)

        for (ETLMapInstruction instruction in instructions) {
            loadInstruction(instruction, processor)
        }
    }

    /**
     *
     * @param instruction
     */
    private void loadInstruction(ETLMapInstruction instruction, ETLProcessor processor) {

        Element element = processor.extract(instruction.column.label)

        if (element) {
            for (ETLMapTransform transformation in instruction.transformations) {
                element = (Element) InvokerHelper.invokeMethod(element, transformation.methodName, transformation.parameters ?: null)
            }

            element.load(instruction.domainProperty.name)
        }
    }
}
