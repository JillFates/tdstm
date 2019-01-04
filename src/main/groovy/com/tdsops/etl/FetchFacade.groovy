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
	Map<String, ETLFieldDefinition> fieldDefinitionMap = [:]

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
		this.fieldDefinitionMap = fields.collectEntries { String fieldName ->
			[(fieldName): processor.lookUpFieldDefinitionForCurrentDomain(fieldName)]
		}
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

		RowResult rowResult = processor.result.currentRow()

		if(!rowResult){
			throw ETLProcessorException.incorrectFetchCommandUse()
		}

		Map<String, Object> context = [
			project    : processor.project,
			domainClass: processor.selectedDomain.domain.clazz
		]

		Object objectResult = SearchQueryHelper.findEntityByMetaData(
			this.propertyName,
			rowResult.fields,
			context,
			null
		)

		return (objectResult != null && objectResult != -1) ? buildMapResults(objectResult) : null
	}

	/**
	 * <p>It builds fetch command results based onf field names configures.</p>
	 * <p>If user did not select fields, it returns all the object results properties.</p>
	 * <p></p>
	 * @param objectResult an Object used to build the Map results
	 * @return a map with fields results
	 */
	private Map<String, Object> buildMapResults(Object objectResult) {

		Map mapResult = [
			id: objectResult.id
		]

		if (fieldNames) {
			mapResult += fieldNames.collectEntries { String fieldName ->
				ETLFieldDefinition fieldDefinition = fieldDefinitionMap[fieldName]
				[(fieldName): objectResult[fieldDefinition.name]]
			}
		}

		return mapResult
	}

}
