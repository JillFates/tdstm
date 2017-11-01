package com.tdsops.etl

class Element {

    String originalValue
    String value
    Integer rowIndex
    Integer columnIndex
    ETLDomain domain
    ETLProcessor processor

    Field field = new Field()
    /**
     *
     *
     *
     * @param closure
     * @return
     */
    def transform (Closure closure) {
        Transformation transformation = new Transformation(this)
        def code = closure.rehydrate(transformation, transformation, transformation)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        this
    }
    /**
     *
     *
     * @param closure
     * @return closure instance
     */
    Closure translate (Closure closure) {
        closure
        //closure.owner = etlProcessor
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.delegate = this
        closure
    }
    /**
     *
     *
     * @param field
     * @return
     */
    Element load (String fieldName) {

        //TODO: Diego. Review this interaction
        Map<String, ?> fieldSpec = processor.lookUpFieldSpecs(processor.selectedDomain, fieldName)

        if (fieldSpec) {
            field.name = fieldName
            domain = processor.selectedDomain

            field.label = fieldSpec.label
            field.control = fieldSpec.control
            field.constraints = fieldSpec.constraints
        }
        processor.addElementLoaded(processor.selectedDomain, this)
        this
    }
    /**
     *
     *
     * @param methodName
     * @param args
     */
    def methodMissing (String methodName, args) {
        processor.debugConsole.info "Method missing: ${methodName}, args: ${args}"
        throw ETLProcessorException.methodMissing(methodName, args)
    }

    def propertyMissing (String name) {
        println "Missing property $name"
    }
}