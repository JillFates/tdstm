package com.tdsops.etl

import com.tdsops.common.lang.CollectionUtils
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil

import java.text.SimpleDateFormat

/**
 * Element represents extract/transform/load ETL command
 * <pre>
 *  extract 'asset name' transform with uppercase() load 'assetName'
 *  // extract command creates an instance of Element class
 *  // then transform with will invoke transformation functions for the same Element instance
 *  // at the end of the chain method invokation, load command will
 *  //
 * </pre>
 */
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
	 * Overrides default assignation to value in case that we are assigning another Element Object
	 * @param obj
	 */
	void setValue ( Object obj ) {
		this.value = ( obj instanceof Element ) ? obj.value : obj
	}

	/**
	 * Default o initialize value
	 */
	Object init
	/**
	 * ETLDomain selected by the current command
	 */
	ETLDomain domain
	/**
	 * ETL Processor Instance used to invoke command execution results
	 */
	ETLProcessor processor
	/**
	 * ETL Field defined by the label and field name
	 */
	ETLFieldDefinition fieldDefinition
	/**
	 * ETL field convertion or transformation errors
	 */
	List<String> errors

	/**
	 * Defines if an Element instance was created by:
	 * <pre>
	 *    load 'name' with 'Foo Bar'
	 * </pre>
	 */
	boolean loadedElement = false

	/**
	 * Transform command on an element with a closure to be executed
	 * <pre>
	 *     domain Application
	 *     extract 'location' transform {*          lowercase() append('**')
	 *} load 'description'
	 * </pre>
	 * @param closure
	 * @return the element instance that received this command
	 */
	Element transform(Closure closure) {
		processor.validateStack()
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
	Element transform(ETLProcessor.ReservedWord reservedWord) {
		processor.validateStack()
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
	Element load(String fieldName) {
		processor.validateStack()
		if(processor.hasSelectedDomain()){
			this.fieldDefinition = processor.lookUpFieldDefinitionForCurrentDomain(fieldName)
			processor.addElementLoaded(processor.selectedDomain.domain, this)
			return this
		} else{
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
	Element initialize(String fieldName) {
		processor.validateStack()
		if (processor.hasSelectedDomain()){
			this.fieldDefinition = processor.lookUpFieldDefinitionForCurrentDomain(fieldName)
			this.init = this.value
			this.originalValue = null
			this.value = null
			processor.addElementInitialized(processor.selectedDomain.domain, this)
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
	Element init(String initValue) {
		return initialize(initValue)
	}

	/**
	 * Validation for incorrect methods on script content
	 * @param methodName
	 * @param args
	 */
	def methodMissing(String methodName, args) {
		processor.debugConsole.info "Method missing: ${methodName}, args: ${args}"
		throw ETLProcessorException.methodMissing(methodName, args)
	}

	/**
	 * Validation for incorrect properties on script content
	 * @param name
	 * @return
	 */
	def propertyMissing(String name) {
		processor.debugConsole.info "Missing property $name"
		throw ETLProcessorException.parameterMissing(name)
	}

	/**
	 * Try to get a property from the content we delegate to the value in case that it is a Map
	 * <code>
	 *      load 'custom2' with attribsVar.cpu
	 * 		load 'custom3' with attribsVar.storage.size()
	 * <code>
	 * The get is called when the parser evalates the attribs.* and passes the .propertyName to the
	 * method. The get method then return's that property (e.g. cpu and storage)
	 *
	 * @param name
	 * @return
	 */
	Object get(String name) {
		return value."$name"
	}

	/**
	 * Middle transformation. It takes <code>n</code> characters from position  <code>m</code>
	 * <code>
	 *      load ... transformation with take(n, m)
	 * <code>
	 * This method also validate the range that is trying to be taken.
	 * @param params List of parameters that SHOULD contain the position (starting in 1) and the characters to Take from the String
	 * @return the element instance that received this command
	 */
	Element middle(Integer...params) {
		value = transformStringObject('middle', value) {

			if ( params.size() != 2 ) {
				throw ETLProcessorException.invalidRange('The middle transformation requires two parameters (startAt, numOfChars)')
			}

			int position = params[0]
			int take = params[1]

			if ( position <=0 || take <= 0 ) {
				throw ETLProcessorException.invalidRange('Must use positive values greater than 0 for "middle" transform function')
			}

			int size = value.size()
			if ( position > size ) {
				return ""
			}

			int start = (position - 1)
			int to = (start + take - 1)
			if ( to >= size ) {
				to = size - 1
			}
			subListRangeCheck(start, to, size)
			it[start..to]
		}
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
	Element substitute(def dictionary) {
		if(dictionary.containsKey(value)){
			value = dictionary[value]
		}
		return this
	}

	/**
	 * Translate an element value using dictionary Map and a default value in case the requested key is not found.
	 * <code>
	 *      dictionary = [prod: 'Production', dev: 'Development']
	 *      load ... transformation with substitute(dictionary, 'someDefaultValue')
	 * <code>
	 *
	 * @param dictionary
	 * @param defaultValue
	 * @return the element instance that received this command
	 */
	Element substitute(def dictionary, defaultValue) {
		if (dictionary.containsKey(value)) {
			value = dictionary[value]
		} else {
			value = defaultValue
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
	Element toLong() {
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
	Element toInteger() {
		value = NumberUtil.toInteger(value)
		return this
	}

	/**
	 * Transform current value in this Element instance to a Date
	 * <code>
	 *     extract ... transform with toDate('yyyy-mm-dd', 'yyyy/mm/dd', 'mm/dd/yyyy') load ...
	 * </code>
	 *
	 * @param format - an array of possible date formats to use
	 * @return - a date instance
	 */
	Element toDate(String... format) {
		if (CollectionUtils.isEmpty(format)) {
			return this
		}

		// if value is blank or null then log an error
		if (StringUtil.isBlank(value)) {
			addToErrors("Not able to transform blank or null date: ${value}")
			return this
		}

		for (String pattern : format) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern)
				simpleDateFormat.setLenient(false)
				value = simpleDateFormat.parse(value)
				break
			} catch (Exception e) {
				addToErrors("Not able to transform date: ${value}, pattern: ${pattern}")
			}
		}

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
	Element sanitize() {
		value = transformStringObject('sanitize', value) {
			StringUtil.sanitize(it)
		}
		return this
	}

	/**
	 * Trim command removes any leading and trailing whitespace removed
	 * <code>
	 *      load ... transformation with trim()
	 * <code>
	 * @param safe this parameter defines if it is necessary to check the Element#value type
	 * @return the element instance that received this command
	 */
	Element trim() {
		value = transformStringObject('trim', value) {
			it.trim()
		}

		return this
	}

	/**
	 * Replace the first string content in the element value
	 * <code>
	 *      load ... transformation with replaceFirst(content, with)
	 * <code>
	 * @param content
	 * @return the element instance that received this command
	 */
	Element replaceFirst(String content, String with) {
		value = transformStringObject('replaceFirst', value) {
			it.replaceFirst(content, with)
		}
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
	Element replaceAll(String content, String with) {
		value = transformStringObject('replaceAll', value) {
			it.replaceAll(content, with)
		}
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
	Element replaceLast(String content, String with) {
		value = transformStringObject('replaceLast', value) {
			it.reverse().replaceFirst(content, with).reverse()
		}

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
	Element uppercase() {
		value = transformStringObject('uppercase', value) {
			it.toUpperCase()
		}
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
	Element lowercase() {
		value = transformStringObject('lowercase', value) {
			it.toLowerCase()
		}
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
	Element left(Integer amount) {
		value = transformStringObject('left', value) {
			it.take(amount)
		}
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
	Element right(Integer amount) {
		value = transformStringObject('right', value) {
			it.reverse()?.take(amount)?.reverse()
		}
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
	Element replace(String regex, String replacement) {
		value = transformStringObject('replace', value) {
			it.replaceAll(regex, replacement)
		}
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
	Element set(String variableName) {
		processor.addLocalVariableInBinding(variableName, this)
		return this
	}

	/**
	 * Concats Element and String values from a ETL Script and assign result String value.
	 * It's used in this ETL script command
	 * <code>
	 *     extract 4 transform concat('-', myVar) load description
	 * </code>
	 * @param objects
	 * @return
	 */
	Element concat(String separator, Object...values){
		this.value = ETLTransformation.concat(separator, this.value, values)
		checkLoadedElement()
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
	Element plus(Element anotherElement) {
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
	Element plus(String value) {
		this.value += value
		return this
	}

	/**
	 * Set a default value when extracting and loading values
	 * So that I can reduce the amount of code to write and simplify the scripts
	 * <code>
	 *     extract 'desc' transform with defaultValue('Something') load 'Description'
	 * </code>
	 * @param objects
	 * @return
	 */
	def defaultValue(Object value) {
		if( ! isValueSet() ) {
			this.value = value
		}

		return this
	}

	/**
	 * checks that the wrapped value is not Null nor Blank
	 * @return
	 */
	private boolean isValueSet() {
		return ! (
				  value == null ||
				  (value instanceof CharSequence) && value.trim().size() == 0
		)
	}

	/**
	 * Perform the append process over all values separated by <code>separator</code> provided
	 * @param separator - value separator
	 * @param values - list of values to concatenate
	 *
	 *
	 * Examples
	 * <code>
	 * extract 'column' transform with append(separator, value1, [value2, value3, ...]) uppercase()
	 *
	 * extract 'column' transform with append(', ', ipVar)
	 * extract 'column' transform with append(', ', DOMAIN.assetName)
	 * extract 'column' transform with append(', ', SOURCE.'device id')
	 *
	 * load 'IP Address' with append(', ', ipVar)
	 * </code>
	 *
	 * @return the joined string
	 */
	Element append(String separator, Object...values){
		this.value = ETLTransformation.append(separator, this.value, values)
		checkLoadedElement()
		return this
	}

	/**
	 * Check if the current element instance was started using load command,
	 * and save it in ETLProcessorResult
	 * <pre>
	 *  load 'Name' transform with append(',', 'foo', 'bar')
	 * </pre>
	 * @see Element#loadedElement
	 */
	private void checkLoadedElement(){
		if(loadedElement){
			this.originalValue = this.value
			processor.addElementLoaded(processor.selectedDomain.domain, this)
			loadedElement = false
		}
	}

	/**
	 * Perform the evaluation of the value parameter and update current element value and original value.
	 * @param value - can be a variable, string, DOMAIN..., SOURCE... or a function like concat(....)
	 *
	 * <pre>
	 *		load 'Name' transform with append(',', 'foo', 'bar')
	 *		initialize 'Name' transform with append(' - ', envVar)
	 *		extract 'Name' transform with append(' - ', envVar) load 'Name'
	 * </pre>
	 *
	 * @return current Element updated
	 */
	Element with(Object value) {
		this.value = ETLValueHelper.valueOf(value)
		this.originalValue = ETLValueHelper.valueOf(value)
		processor.addElementLoaded(processor.selectedDomain.domain, this)
		return this
	}

	/**
	 * Overriding Equals method for this command in an ETL script.
	 * <code>
	 *      .....
	 *      if (myVar == 'Cool Stuff') {*          .....
	 *}*  </code>
	 * @param otherObject
	 * @return
	 */
	boolean equals(otherObject) {
		if(this.is(otherObject)){
			return true
		}

		if(String.isInstance(otherObject)){
			return value.equals(otherObject)
		}

		if(getClass() != otherObject.class){
			return false
		}

		Element element = (Element)otherObject

		if(value != element.value){
			return false
		}

		return true
	}

	int hashCode() {
		return value.hashCode()
	}

	/**
	 * Applies a TransformationFunction to a String Object only if the passed object IS a string
	 * or throws a ETLProcessorException
	 * @param methodName reference to the method name of the closure (Objective)
	 * @param value value to be checked against Null and String
	 * @param transformation Transformation closure to apply
	 * @return result String transformed
	 */
	private static String transformStringObject(String methodName, Object value, Closure transformation) {
		String retVal

		if (value == null) {
			retVal = ''
		} else if (value instanceof CharSequence) {
			retVal = transformation.call(value)
		} else {
			throw ETLProcessorException.invalidUseOfMethod(methodName, value)
		}

		return retVal
	}

	/**
	 *
	 * @param err
	 * @return
	 */
	private addToErrors(String err) {
		if (!errors) {
			errors = new ArrayList<>()
		}
		processor?.debugConsole?.info err
		errors.add err
	}

	@Override
	String toString() {
		return value
	}
}
