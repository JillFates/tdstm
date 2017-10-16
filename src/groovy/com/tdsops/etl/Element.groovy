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
     * @param transformationName
     * @return
     */
    Element and (String transformationName) {
        transform(transformationName)
    }
    /**
     *
     *
     * @param transformationName
     * @return
     */
    def transform (String transformationName) {
        ETLTransformation transformation = lookupTransformation(transformationName)
        transformation.apply(this)
        processor.debugConsole.info "Applying transformation on element: $this"
        this
    }
    /**
     *
     *
     * @param actions
     * @return
     */
    Element translate (Map actions) {
        if (actions.containsKey('with')) {
            translateWith(actions.get('with'))
        }
        this
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
        processor.addLoadedElement(processor.selectedDomain, this)
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

    /** Private Methods. Non public API for ETL processor */

    private ETLTransformation lookupTransformation (String name) {
        if (processor.transformations
                && processor.transformations.containsKey(name)) {
            processor.transformations[name]
        } else {
            processor.debugConsole.error "Unknown transformation: $name"
            throw ETLProcessorException.unknownTransformation(name)
        }
    }

    private Element translateWith (Map dictionary) {

        if (dictionary.containsKey(value)) {
            String oldValue = value
            value = dictionary[value]

            processor.debugConsole.info "Translate $oldValue -> ${value}"
        } else {
            processor.debugConsole.warn "Could not translate ${value}"
        }
        this
    }
}