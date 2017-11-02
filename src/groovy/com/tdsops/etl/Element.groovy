package com.tdsops.etl

import com.tdssrc.grails.StringUtil

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
     * @param closure
     * @return
     */
    def transform (Closure closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        this
    }
    /**
     *
     *
     * @param command
     * @return
     */
    Element transform (String command) {
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

    Element middle (int take, int position) {
        int start = (position - 1)
        int to = (start + take - 1)
        value = value[start..to]
        this
    }

    Element translate (def map) {
        Map dictionary = map['with']
        if (dictionary.containsKey(value)) {
            value = dictionary[value]
        }
        this
    }
    /**
     * Replace all of the escape characters
     * (CR|LF|TAB|Backspace|FormFeed|single/double quote) with plus( + )
     * and replaces any non-printable, control and special unicode character
     * with a tilda ( ~ ).
     *
     * The method will also remove any leading and trailing whitespaces
     * @return
     */
    Element sanitize () {
        value = StringUtil.sanitizeAndStripSpaces(value)
        this
    }

    Element trim () {
        value = value.trim()
        this
    }

    Element first (String content) {
        value = value.replaceFirst(content, '')
        this
    }

    Element all (String content) {
        value = value.replaceAll(content, '')
        this
    }

    Element last (String content) {
        value = value.reverse().replaceFirst(content, '').reverse()
        this
    }

    Element uppercase () {
        value = value.toUpperCase()
        this
    }

    Element lowercase () {
        value = value.toLowerCase()
        this
    }

    Element left (Integer amount) {
        value = value.take(amount)
        this
    }

    Element right (Integer amount) {
        value = value.reverse().take(amount).reverse()
        this
    }

}