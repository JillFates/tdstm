package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.Element
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.ReflectionMethodInvoker

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

        Element element
        if (instruction.sourcePosition) {
            element = processor.extract(instruction.sourcePosition)
        } else {
            element = processor.extract(instruction.sourceName)
        }

        if (element) {
            element.load(instruction.domainProperty.name)
            for (ETLMapTransform transformation in instruction.transformations) {
                ReflectionMethodInvoker.invoke(element, transformation.methodName, transformation.parameters)
            }
        }


    }
}
