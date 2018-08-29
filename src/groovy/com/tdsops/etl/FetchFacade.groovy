package com.tdsops.etl

import net.transitionmanager.dataImport.SearchQueryHelper

/**
 * <p>Fetch results from database using same logic from Import Process by{@code SearchQueryHelper}</p>
 * The search methods are processed in the following order of precedence:
 * <ul>
 *     <li>1. By ID</li>
 *     <li>2. By find/elseFind</li>
 *     <li>3. By Alternate Key</li>
 * </ul>
 * <h3>Syntax</h3>
 * <pre>
 * 	fetch 'id' fields 'rackSource', 'sourceRackPosition' set deviceVar
 * </pre>
 * <pre>{@code fetch 'id'}</pre>
 * <p><br>The fetch command requires the specification of the field that represents the domain to retrieve.</p>
 * <br>
 * <pre>{@code fields 'rackSource', 'sourceRackPosition'}</pre>
 * <p><br>The fields parameter accepts one or more properties but is not required if one only wants to determine if the entity exists. The list of fields can consist of the custom field label and/or internal field name. Any subsequent reference to the properties must use the same reference strings. In other words one can not use the label in the fields argument and then use the field name in the follow on expressions.</p>
 * <h3>Accessing Results</h3>
 * <p>The result of the command will be a Map of the field/label names and their values. Accessing the fields in the code will be supported with array or dot syntax as the follow examples illustrate.</p>
 * <pre>
 * 	fetch 'id' fields 'Move Methodology' set deviceVar
 * 	if (deviceVar.'Move Methodology' == 'xyzzy') {* 		load 'description' with 'reference with dot notation'
 *}* 	if (deviceVar['Move Methodology'] == 'fubar') {* 		load 'description' with 'referenc with array notation'
 *}* </pre>
 */
class FetchFacade {

	ETLProcessor processor
	String propertyName
	List<String> fieldNames = []

	FetchFacade(ETLProcessor processor, String propertyName) {
		this.processor = processor
		ETLFieldDefinition fieldDefinition = this.processor.lookUpFieldDefinitionForCurrentDomain(propertyName)
		processor.validateDomainPropertyAsReference(fieldDefinition.name)
		this.propertyName = fieldDefinition.name
	}

	/**
	 * fields method specifies the field or fields that will be used by subsequent code for evaluation.
	 * @param names an array of propertyName
	 * @return current instance of FetchFacade
	 */
	FetchFacade fields(String... names) {
		List list = names as List
		return this.fields(list)
	}

	/**
	 * fields method specifies the field or fields that will be used by subsequent code for evaluation.
	 * @param names a List of propertyName
	 * @return current instance of FetchFacade
	 */
	FetchFacade fields(List<String> fields) {
		fields.each { processor.lookUpFieldDefinitionForCurrentDomain(it) }
		this.fieldNames = fields
		return this
	}

	/**
	 * Create a local variable using variableName parameter with {@code FetchFacade} results.
	 * @param variableName
	 * @return a Map with fields values
	 * @see SearchQueryHelper
	 */
	Map<String, Object> set(String variableName) {
		Map<String, Object> mapValues = doFetch()
		processor.addLocalVariableInBinding(variableName, mapValues)
		return mapValues
	}

	/**
	 * Executes the fetch command logic.
	 * 1. By ID
	 * 2. By find/elseFind
	 * 3. By Alternate Key
	 *
	 * @return a Map with fields values
	 */
	private Map<String, Object> doFetch() {

		Element latestLoadedField = processor.result.latestLoadedField
		if (latestLoadedField && latestLoadedField.fieldDefinition) {
			return doFetchBy(latestLoadedField)
		} else if (processor.currentFindElement) {
			return doFetchByFindElseFind()
		} else {
			throw ETLProcessorException.incorrectFetchCommandUse()
		}
	}

	/**
	 * Executes a fetch command based on a {@code Element}
	 * @param element
	 * @return a Map with fields values
	 */
	private Map<String, Object> doFetchBy(Element element) {

		if (element.fieldDefinition.name == 'id') {
			return doFetchByID(element)
		} else {
			return doFetchByAlternateKey(element)
		}
	}

	/**
	 * Executes a fetch command based on an Alternate Key
	 * @param element
	 * @return a Map with fields values
	 */
	private Map<String, Object> doFetchByAlternateKey(Element element) {

		Map<String, Object> context = [
			project    : processor.project,
			domainClass: processor.selectedDomain.domain.clazz
		]

		Map fieldsInfo = [
			(element.fieldDefinition.name): element.value
		]

		Object objectResult = SearchQueryHelper.findEntityByMetaData(
			this.propertyName,
			fieldsInfo,
			context,
			null
		)

		return objectResult ? buildMapResults(objectResult) : null
	}

	/**
	 * Executes a fetch command based on an ID
	 * @param element
	 * @return a Map with fields values
	 */
	private Map<String, Object> doFetchByID(Element element) {

		Map<String, Object> context = [
			project    : processor.project,
			domainClass: processor.selectedDomain.domain.clazz
		]

		Map fieldsInfo = [
			id: element.value
		]

		Object objectResult = SearchQueryHelper.findEntityByMetaData(
			this.propertyName,
			fieldsInfo,
			context,
			null
		)

		return objectResult ? buildMapResults(objectResult) : null
	}

	/**
	 * Executes a fetch command based on an find/elseFind
	 * @return a Map with fields values
	 */
	private Map<String, Object> doFetchByFindElseFind() {
		Map<String, Object> context = [
			project    : processor.project,
			domainClass: ETLDomain.valueOf(processor.currentFindElement.currentFind.domain).clazz
		]

		Object objectResult = SearchQueryHelper.findEntityByMetaData(
			this.propertyName,
			processor.currentFindElement.currentFind.kv,
			context,
			null
		)

		return objectResult ? buildMapResults(objectResult) : null
	}

	/**
	 * It builds fetch command results based onf field names configures.
	 * If user did not select fields, it returns all the object results properties
	 * @param objectResult an Object used to build the Map results
	 * @return a map with fields results
	 */
	private Map<String, Object> buildMapResults(Object objectResult) {

		if (fieldNames) {
			return fieldNames.collectEntries { String fieldName ->
				[(fieldName): objectResult[fieldName]]
			}
		} else {
			return objectResult.properties
		}
	}

}
