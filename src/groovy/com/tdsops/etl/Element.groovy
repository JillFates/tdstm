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
     * Transform command on an element with a closure to be executed
     * @param closure
     * @return the element instance that received this command
     */
    Element transform (Closure closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        this
    }

    /**
     * Transform command with a hack for this example:
     * <code>
     *     extract .. transform with uppercase() lowercase()
     * </code>
     *
     * @param command
     * @return the element instance that received this command
     */
    Element transform (String command) {
        this
    }

    /**
     * Loads a field using fields spec based on domain validation
     * @param field
     * @return the element instance that received this command
     */
    Element load (String fieldName) {

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
     * Validation for incorrect methods on script content
     * @param methodName
     * @param args
     */
    def methodMissing (String methodName, args) {
        processor.debugConsole.info "Method missing: ${methodName}, args: ${args}"
        throw ETLProcessorException.methodMissing(methodName, args)
    }

    /**
     * Validation for incorrect properties on script content
     * @param name
     * @return
     */
    def propertyMissing (String name) {
        processor.debugConsole.info "Missing property $name"
        throw ETLProcessorException.parameterMissing(name)
    }

    /**
     * Middle transformation. It takes <code>n</code> characters from position  <code>m</code>
     * <code>
     *      load ... transformation with take(n, m)
     * <code>
     * @param take
     * @param position
     * @return the element instance that received this command
     */
    Element middle (int take, int position) {
        int start = (position - 1)
        int to = (start + take - 1)
        value = value[start..to]
        this
    }

    /**
     * Translate an element value using dictionary Map
     * <code>
     *      dictionary = [prod: 'Production', dev: 'Development']
     *      load ... transformation with translate(dictionary)
     * <code>
     *
     * @param dictionary
     * @return the element instance that received this command
     */
    Element translate (def dictionary) {
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
     * The method will also remove any leading and trailing whitespaces
     * @return the element instance that received this command
     */
    Element sanitize () {
        value = StringUtil.sanitizeAndStripSpaces(value)
        this
    }

    /**
     * Trim command removes any leading and trailing whitespace removed
     * <code>
     *      load ... transformation with trim()
     * <code>
     * @return the element instance that received this command
     */
    Element trim () {
        value = value.trim()
        this
    }

    /**
     * Replace the first string content in the element value
     * <code>
     *      load ... transformation with first(content)
     * <code>
     * @param content
     * @return the element instance that received this command
     */
    Element first (String content) {
        value = value.replaceFirst(content, '')
        this
    }

    /**
     * Replace all the string content in the element value
     * <code>
     *      load ... transformation with all(content)
     * <code>
     * @param content
     * @return the element instance that received this command
     */
    Element all (String content) {
        value = value.replaceAll(content, '')
        this
    }

    /**
     * Replace the last string content in the element value
     * <code>
     *      load ... transformation with last(content)
     * <code>
     * @param content
     * @return the element instance that received this command
     */
    Element last (String content) {
        value = value.reverse().replaceFirst(content, '').reverse()
        this
    }

    /**
     * Converts all of the characters in this element value to upper
     * case using the rules of the default locale.
     * <code>
     *      load ... transformation with uppercase()
     * <code>
     * @return the element instance that received this command
     */
    Element uppercase () {
        value = value.toUpperCase()
        this
    }

    /**
     * Converts all of the characters in this element value to lower
     * case using the rules of the default locale.
     * <code>
     *      load ... transformation with lowercase()
     * <code>
     * @return the element instance that received this command
     */
    Element lowercase () {
        value = value.toLowerCase()
        this
    }

    /**
     * Takes the first <code>n</code> elements from this element value
     * assigning it as a new element value.
     * <code>
     *      load ... transformation with left(n)
     * <code>
     * @param amount
     * @return the element instance that received this command
     */
    Element left (Integer amount) {
        value = value.take(amount)
        this
    }

    /**
     * Takes the last <code>n</code> elements from this element value
     * assigning it as a new element value.
     * <code>
     *      load ... transformation with right(n)
     * <code>
     * @param amount
     * @return the element instance that received this command
     */
    Element right (Integer amount) {
        value = value.reverse().take(amount).reverse()
        this
    }

    /**
     * Replaces each substring of this string that matches the given <a
     * href="../util/regex/Pattern.html#sum">regular expression</a> with the
     * given replacement in the element value.
     * <code>
     *      load ... transformation with replace (regex, replacement)
     * <code>
     * @param regex
     * @param replacement
     * @return the element instance that received this command
     */
    Element replace (String regex, String replacement) {
        value = value.replaceAll(regex, replacement)
        this
    }

    /**
     * Saves a new variable in the binding context in order to use it later
     * It's used in this ETL script command
     * <code>
     *     extract 3 transform with lowercase() store myVar
     * </code>
     * * @param variableName
     * @return
     */
    Element store (String variableName) {
        processor.storeVariable(variableName, this)
    }

    /**
     * Appends Element and String values from a ETL Script and assign result String value.
     * It's used in this ETL script command
     * <code>
     *     extract 4 transform append('-', myVar) load description
     * </code>
     * @param objects
     * @return
     */
    Element append (Object... objects) {

        String newValue = objects.sum { object ->
            if (Element.class.isInstance(object)) {
                ((Element)object).value
            } else {
                object ? object.toString() : ''
            }
        }
        this.value += newValue
        this
    }

    /**
     * Overiding Equals method for this command in an ETL script.
     * <code>
     *     .....
     *     if (myVar == 'Cool Stuff') {
     *          .....
     *     }
     * </code>
     * @param otherObject
     * @return
     */
    boolean equals (otherObject) {
        if (this.is(otherObject)) return true

        if (otherObject.class == String) return value.equals(otherObject)

        if (getClass() != otherObject.class) return false

        Element element = (Element) otherObject

        if (value != element.value) return false

        return true
    }

    int hashCode () {
        return value.hashCode()
    }
}