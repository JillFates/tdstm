package com.tdsops.etl

import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil

class Element implements RangeChecker {
	/**
	 * Original value extracted from Dataset and used to create an instance of Element
	 */
    Object originalValue
	/**
	 * Value with transformations applied
	 */
	Object value
	/**
	 * Default o initialize value
	 */
	String init
    Integer rowIndex
    Integer columnIndex
    ETLDomain domain
    ETLProcessor processor

    ETLFieldSpec fieldSpec

    /**
     * Transform command on an element with a closure to be executed
     * <pre>
     *     domain Application
     *     extract 'location' transform {
     *          lowercase() append('**')
     *     } load 'description'
     * </pre>
     * @param closure
     * @return the element instance that received this command
     */
    Element transform (Closure closure) {
        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
	    return this
    }

    /**
     * Transform command with a hack for this example:
     * <code>
     *     domain Application
     *     extract 'location' transform with uppercase() lowercase() load 'description'
     * </code>
     * @param command
     * @return the element instance that received this command
     */
    Element transform (ETLProcessor.ReservedWord reservedWord) {
	    //TODO: validate invalid reserved words
	    return this
    }

    /**
     * Loads a field using fields spec based on domain validation
     * It's used in this ETL script command
     * <code>
     *     extract 3 transform with lowercase() load 'description'
     * </code>
     * @param fieldName
     * @return the element instance that received this command
     */
    Element load (String fieldName) {
        if (processor.hasSelectedDomain()) {
            this.fieldSpec = processor.lookUpFieldSpecs(processor.selectedDomain, fieldName)
            processor.addElementLoaded(processor.selectedDomain, this)
            return this
        } else {
            throw ETLProcessorException.domainMustBeSpecified()
        }
    }

	/**
	 * Initialize an Element with a particular value
	 * <code>
	 *     domain Device
	 *     extract 'name' initialize 'custom1'
	 * </code>
	 * @param fieldName
	 * @return the element instance that received this command
	 */
	Element initialize (String fieldName) {
		if (processor.hasSelectedDomain()) {
			this.fieldSpec = processor.lookUpFieldSpecs(processor.selectedDomain, fieldName)
			this.init = this.value
			this.originalValue = ''
			this.value = ''
			processor.addElementLoaded(processor.selectedDomain, this)
			return this
		} else {
			throw ETLProcessorException.domainMustBeSpecified()
		}
	}

	/**
	 * Initialize an Element with a particular value
	 * <code>
	 *     domain Device
	 *     extract 'name' init 'custom1'
	 * </code>
	 * * @param init
	 * @param fieldName
	 * @return the element instance that received this command
	 * @see Element#initialize(java.lang.String)
	 */
	Element init (String initValue) {
		return initialize(initValue)
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
     * This method also validate the range that is trying to be taken.
     * @param take
     * @param position
     * @return the element instance that received this command
     */
    Element middle (int position, int take) {

        int start = (position - 1)
        int to = (start + take - 1)
        subListRangeCheck(start, start + to, value.size())
        value = value[start..to]
	    return this
    }

    /**
     * Translate an element value using dictionary Map
     * <code>
     *      dictionary = [prod: 'Production', dev: 'Development']
     *      load ... transformation with substitute(dictionary)
     * <code>
     *
     * @param dictionary
     * @return the element instance that received this command
     */
    Element substitute (def dictionary) {
        if (dictionary.containsKey(value)) {
            value = dictionary[value]
        }
	    return this
    }

	/**
	 * Transform current value in this Element instance to a Long number
	 * <code>
	 *      load ... transformation with toLong()
	 * <code>
	 * @see NumberUtil#toLong(java.lang.Object)
	 * @return the element instance that received this command
	 */
	Element toLong(){
		value = NumberUtil.toLong(value)
		return this
	}

	/**
	 * Transform current value in this Element instance to a Integer number
	 * <code>
	 *      load ... transformation with toInteger()
	 * <code>
	 * @see NumberUtil#toInteger(java.lang.Object)
	 * @return the element instance that received this command
	 */
	Element toInteger(){
		value = NumberUtil.toInteger(value)
		return this
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
	    return this
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
	    return this
    }

    /**
     * Replace the first string content in the element value
     * <code>
     *      load ... transformation with replaceFirst(content)
     * <code>
     * @param content
     * @return the element instance that received this command
     */
    Element replaceFirst (String content) {
        value = value.replaceFirst(content, '')
	    return this
    }

    /**
     * Replace all the string content in the element value
     * <code>
     *      load ... transformation with replaceAll(content)
     * <code>
     * @param content
     * @return the element instance that received this command
     */
    Element replaceAll (String content) {
        value = value.replaceAll(content, '')
	    return this
    }

    /**
     * Replace the last string content in the element value
     * <code>
     *      load ... transformation with replaceLast(content)
     * <code>
     * @param content
     * @return the element instance that received this command
     */
    Element replaceLast (String content) {
        value = value.reverse().replaceFirst(content, '').reverse()
	    return this
    }

    /**
     * Converts all of the characters in this element value to upper
     * case using the rules of the default locale.
     * <code>
     *      load ... transform with uppercase()
     * <code>
     * @return the element instance that received this command
     */
    Element uppercase () {
        value = value.toUpperCase()
	    return this
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
	    return this
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
	    return this
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
	    return this
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
	    return this
    }

    /**
     * Saves a new local variable in the binding context in order to use it later
     * It's used in following ETL script command
     * <code>
     *     extract 3 transform with lowercase() set myLocalVariable
     * </code>
     * * @param variableName
     * @return
     */
    Element set (String variableName) {
        processor.addLocalVariableInBinding(variableName, this)
	    return this
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
                ((Element) object).value
            } else {
                object ? object.toString() : ''
            }
        }
        this.value += newValue
	    return this
    }

    /**
     * Appends element.value content with anotherElement.value
     * It's used in this ETL script command
     * <code>
     *     extract 4 transform append(myVar + CE) load description
     * </code>
     * @param anotherElement an ETL Element
     * @return
     */
    Element plus (Element anotherElement) {
        this.value += anotherElement?.value
	    return this
    }

    /**
     * Appends element.value content with a String value
     * It's used in this ETL script command
     * <code>
     *     extract 4 transform append(myVar + '******') load description
     * </code>
     * @param anotherElement an ETL Element
     * @return
     */
    Element plus (String value) {
        this.value += value
	    return this
    }

    /**
     * Overriding Equals method for this command in an ETL script.
     * <code>
     *      .....
     *      if (myVar == 'Cool Stuff') {
     *          .....
     *      }
     *  </code>
     * @param otherObject
     * @return
     */
    boolean equals (otherObject) {
        if (this.is(otherObject)) return true

        if (String.isInstance(otherObject)) return value.equals(otherObject)

        if (getClass() != otherObject.class) return false

        Element element = (Element) otherObject

        if (value != element.value) return false

        return true
    }

    int hashCode () {
        return value.hashCode()
    }
}
