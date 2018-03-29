package com.tdsops.etl
/**
 * ETL find command implementation.
 * <code>
 *  domain Dependency
 *  // Try to find the Application using different searches
 * 	find Application 	 by id 				     with assetId into assetId
 * 	elseFind Application by assetName, assetType with primaryName, primaryType into assetId
 * 	elseFind Application by assetName            with primaryName into assetId
 * 	elseFind Asset 		 by assetName            with primaryName into assetId warn 'found with wrong asset class'
 * </code>
 * @param values
 * @return
 */
class ETLFindElement {

	ETLProcessor processor
	Integer rowIndex
	String warnMessage
	ETLDomain currentDomain
	Map<String, ?> currentFind = [:]
	Map<String, ?> results
	List<Map<String, ?>> findings = []

	/**
	 * ETLFindElement instances creation defined by the ETLProcessor instance and a particular value of ETL Domain
	 * @param processor
	 * @param domain
	 */
	ETLFindElement(ETLProcessor processor, ETLDomain domain, Integer rowIndex) {
		this.processor = processor
		this.rowIndex = rowIndex
		setCurrentDomain(domain)
	}

	/**
	 * Defines a new find option. See this code:
	 * <pre>
	 * 	find Application by id with assetId into assetId
	 * 	elseFind Application by assetName, assetType with primaryName, primaryType into assetId
	 * </pre>
	 * @param domain
	 * @return
	 */
	ETLFindElement elseFind(ETLDomain domain) {
		findings.add(currentFind)
		setCurrentDomain(domain)
		return this
	}

	/**
	 * Defines the property for the current find element
	 * @param property
	 * @return
	 */
	ETLFindElement into(String property) {
		validateReference(property)
		currentFind.property = property
		processor.addFindElement(this)
		return this
	}

	/**
	 * Define the list of fields that are located in dataSource and used to find domain instances
	 * @param fields
	 * @return
	 */
	ETLFindElement by(String... fields) {
		for(field in fields){
			checkAssetFieldSpec(field)
			currentFind.fields.add(field)
		}
		return this
	}

	/**
	 * Sets the dataSource Fields and executes the query looking for domain instances
	 * based on currentFind.fields
	 * <p>
	 * If there is an error requesting domain class instances
	 * it adds an error message in the currentFind element
	 * @param values
	 * @return
	 */
	ETLFindElement with(Object... values) {

		checkProject()
		currentFind.values = checkValues(values)

		currentFind.kv = [
			currentFind.fields,
			currentFind.values
		].transpose().collectEntries { it }

		if(!results){

			try{
				currentFind.objects = DomainClassQueryHelper.where(
					ETLDomain.lookup(currentFind.domain),
					processor.project,
					currentFind.kv)
			} catch(all){

				processor.debugConsole.debug("Error in find command: ${all.getMessage()} ")
				currentFind.kv.error = all.getMessage()
			}

			results = [
				objects : [],
				matchOn: null
			]
			if(currentFind.objects && !currentFind.objects.isEmpty()){
				results.objects = currentFind.objects
				results.matchOn = findings.size()

				if(currentFind.objects.size() > 1){
					currentFind.error = 'The find/elseFind command(s) found multiple records'
				}
			}
		}

		return this
	}

	/**
	 * It checks if the amount of values is equals to the number of fields.
	 * After that, it converts all the according to their types.
	 * If there is not a valid type it throws an ETLProcessorException
	 * @param values a list of values
	 * @return a list of converted values according to their types
	 */
	private List<?> checkValues(Object... values) {

		if(currentFind.fields.size() != values.size()){
			throw ETLProcessorException.incorrectAmountOfParameters(
				currentFind.fields,
				values)
		}

		return values.collect { ETLValueHelper.stringValueOf(it) }
	}

	/**
	 * Checks if the current instance of processor has a project already defined.
	 * If not It throws an exception
	 */
	private void checkProject() {
		if(!processor.project){
			throw ETLProcessorException.nonProjectDefined()
		}
	}

	/**
	 * Appends a warn message in the ETL Processor result.
	 * <pre>
	 *   find Application by assetName with primaryName into assetId
	 *   elseFind Asset by assetName with primaryName into assetId warn 'found with wrong asset class'
	 * </pre>
	 * @param message
	 * @return
	 */
	ETLFindElement warn(String message) {
		this.warnMessage = message
		this.processor.addFindWarnMessage(this)
		return this
	}

	/**
	 * Checks a fieldSpec based on asset field name
	 * using the selected domain in the current script
	 * @param fieldName an asset field name
	 */
	private ETLFieldSpec checkAssetFieldSpec(String fieldName) {
		return processor.lookUpFieldSpecs(currentDomain, fieldName)
	}

	/**
	 * Validates if property is identifier or reference for the current domain
	 * @param property
	 */
	void validateReference(String property) {
		processor.validateDomainPropertyAsReference(property)
	}

	private void setCurrentDomain(ETLDomain domain) {
		currentDomain = domain
		currentFind = [
			domain: domain.name(),
			fields: [],
			values: [],
			queryParams: [:]
		]
	}

	/**
	 * Defines if an instance of ETLFindElement has results or not
	 * @return true if the instance has result or false
	 * 				if there is any result yet
	 */
	boolean hasResults() {
		return results != null
	}

	/**
	 * Returns the size of results
	 * @return a positive integer value.
	 * 			A number bigger than 0 when there is results
	 * 			or 0 in there is not results yet
	 */
	int resultSize() {
		return hasResults() ? results.objects.size() : 0
	}

	/**
	 * Returns the unique result in the objects list.
	 * @return a an instance of the results saved
	 * 			as a consequence of a ETL find command
	 */
	Object firstResult() {
		return results.objects[0]
	}

	/**
	 * Validates calls within the DSL script that can not be managed
	 * @param methodName
	 * @param args
	 */
	def methodMissing(String methodName, args) {
		processor.debugConsole.info "Method missing: ${methodName}, args: ${args}"
		throw ETLProcessorException.methodMissingInFindCommand(methodName, args)
	}
}