package com.tdsops.etl

import groovy.transform.CompileStatic

@CompileStatic
class FindStatementBuilder {

	ETLDomain domain
	ETLProcessor processor
	ETLFindElement findElement
	FindCondition currentCondition

	List<FindCondition> conditions = []

	FindStatementBuilder(ETLFindElement findElement, String propertyName){
		this.domain = findElement.currentDomain
		this.processor = findElement.processor
		this.findElement = findElement
		changeCurrentCondition(propertyName)
	}

	FindStatementBuilder(List<FindCondition> conditions){
		this.conditions = conditions
	}

	/**
	 * <p>It adds another condition for the current instance of {@code FindStatementBuilder}</p>
	 * <pre>
	 * find Device by 'assetName' eq srcNameVar and 'IP Address' contains srcIPVar
	 * find Device by 'assetName' like srcNameVar+'%' and 'Tier' in ['Gold','Silver']
	 * </pre>
	 * @param propertyName
	 * @return
	 */
	FindStatementBuilder and(String propertyName) {
		changeCurrentCondition(propertyName)
		return this
	}

	/**
	 * Sets a value for the current find Element.
	 * <pre>
	 * find Application by 'id' with SOURCE.'application id' into 'id'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder with(Object value) {
		return completeCurrentCondition(FindOperator.eq, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>check for field matching a value</p>
	 * <pre>
	 *   find Device by 'custom1' ne 'xyz'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder eq(Object value){
		return completeCurrentCondition(FindOperator.eq, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for field not equaling a value</p>
	 * <pre>
	 *   find Device by 'custom1' ne 'xyz'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder ne(Object value){
		return completeCurrentCondition(FindOperator.ne, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for field matching a value null safe (<=>)</p>
	 * <pre>
	 *   find Device by 'custom1' nseq 'xyz'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder nseq(Object value){
		return completeCurrentCondition(FindOperator.nseq, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for field less than a value</p>
	 * <pre>
	 *   find Device by 'priority' lt 3
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder lt(Object value){
		return completeCurrentCondition(FindOperator.lt, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for field less than or equals a value</p>
	 * <pre>
	 *   find Device by 'priority' le 3
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder le(Object value){
		return completeCurrentCondition(FindOperator.le, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for field greater than a value</p>
	 * <pre>
	 *   find Device by 'priority' gt 4
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder gt(Object value){
		return completeCurrentCondition(FindOperator.gt, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for field greater than or equals a value</p>
	 * <pre>
	 *	find Device by 'priority' ge 4
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder ge(Object value){
		return completeCurrentCondition(FindOperator.ge, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks a string field for a partial match, must include % appropriately</p>
	 * <pre>
	 *  find Device by 'Name' like srvNameVar + '%'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder like(Object value){
		return completeCurrentCondition(FindOperator.like, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks a non-match of a partial string field for, must include % appropriately</p>
	 * <pre>
	 *  find Device by 'Name' notLike srvNameVar + '%'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder notLike(Object value){
		return completeCurrentCondition(FindOperator.notLike, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for a string field that contains a value, same as like with '%' + value + '%'</p>
	 * <pre>
	 *   find Device by 'Name' contains 'prod'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder contains(Object value){
		return completeCurrentCondition(FindOperator.contains, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for a non-match of a string field that contains a value</p>
	 * <pre>
	 *   find Device by 'Name' notContains 'prod'
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder notContains(Object value){
		return completeCurrentCondition(FindOperator.notContains, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for a field being in a list</p>
	 * <pre>
	 *  find Device by 'environment' inList ['Production', 'DR']
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder inList(Object value){
		return completeCurrentCondition(FindOperator.inList, value)
	}

	FindStatementBuilder inList(List<Object> values){
		return completeCurrentCondition(FindOperator.inList, values)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for a field not being in a list</p>
	 * <pre>
	 *  find Device by 'environment' notInList ['QA', 'Development']
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder notInList(Object value){
		return completeCurrentCondition(FindOperator.notInList, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for a value being in a numeric range</p>
	 * <pre>
	 *  find Device by 'priority' between 1..3
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder between(Object value){
		return completeCurrentCondition(FindOperator.between, value)
	}

	/**
	 * Adds a new {@code FindCondition} in current {@code FindStatementBuilder}
	 * <p>checks for a value not being in a numeric range</p>
	 * <pre>
	 *  find Device by 'priority' notBetween 4..6
	 * </pre>
	 * @param value an Object instance to be set in FindStatementBuilder#currentCondition
	 * @return
	 */
	FindStatementBuilder notBetween(Object value){
		return completeCurrentCondition(FindOperator.notBetween, value)
	}

	ETLFindElement into(String propertyName){
		return findElement.into(propertyName, this)
	}

	/**
	 * Takes the current condition and set an operator and a value.
	 * @param operator a instance of {@code FindOperator}
	 * @param value an Object used as a {@code FindCondition} value field
	 * @return current instance of {@code FindStatementBuilder}
	 */
	private FindStatementBuilder completeCurrentCondition(FindOperator operator, Object value){
		currentCondition.defineOperator(operator, value)
		return this
	}

	/**
	 * Takes the current condition and set an operator and a list of value.
	 * @param operator a instance of {@code FindOperator}
	 * @param values a list of Objects Object to be used as a {@code FindCondition} value field
	 * @return current instance of {@code FindStatementBuilder}
	 */
	private FindStatementBuilder completeCurrentCondition(FindOperator operator, List<Object> values){
		currentCondition.defineOperator(operator, values)
		return this
	}

	/**
	 * Validates calls within the DSL script that can not be managed
	 * @param methodName
	 * @param args
	 */
	def methodMissing (String methodName, args) {
		processor.log("Method missing: ${methodName}, args: ${args}", DebugConsole.LevelMessage.ERROR)
		//TODO: dcorrea Unrecognized find criteria operator [xxx] specified. Options are eq, ne, ...
		throw ETLProcessorException.methodMissing(methodName, args)
	}

	/**
	 * Change the current FindCondition validating previously the current status of the previous one.
	 * It also looks up the propertyName definition in order to use a field name or a field label
	 * @param FindCondition the currentCondition for an instance of {@code FindStatementBuilder}
	 * @see FindStatementBuilder#currentCondition
	 * @see ETLFieldsValidator#lookup(com.tdsops.etl.ETLDomain, java.lang.String)
	 */
	private FindCondition changeCurrentCondition(String propertyName){
		if(currentCondition && !currentCondition.isComplete()){
			throw ETLProcessorException.incorrectFindCommandStructure()
		}

		ETLFieldDefinition fieldDefinition = processor.lookUpFieldDefinition(domain, propertyName)
		this.currentCondition = new FindCondition(fieldDefinition.name)
		this.conditions.add(currentCondition)
		return currentCondition
	}

	/**
	 * Evaluates the required properties of the command object and
	 * return an error string if there are any missing in the form:
	 *    missing [x, y, z] keywords
	 *
	 * or an empty string if no problem was found
	 * @return string with errors or empty
	 */
	String stackableErrorMessage() {

		String error = ''

		Set<String> missingProperties = []

		if (currentCondition.isComplete()) {
			missingProperties << 'into'
		} else {
			missingProperties << 'find operation'
		}

		if (missingProperties) {
			error = "find/elseFind statement is missing required [${missingProperties.join(', ')}] ${(missingProperties.size() < 2) ? 'keyword' : 'keywords'}"
		}

		return error
	}
}
