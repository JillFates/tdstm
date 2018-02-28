package com.tdsops.etl
/**
 * ETL find command implementation.
 * <code>
 *     domain Dependency
 *  // Try to find the Application using different searches
 * 	find Application 	 for assetId by id 				     with assetId
 * 	elseFind Application for assetId by assetName, assetType with primaryName, primaryType
 * 	elseFind Application for assetId by assetName            with primaryName
 * 	elseFind Asset 		 for assetId by assetName            with primaryName              warn 'found with wrong asset class'
 * </code>
 * @param values
 * @return
 */
class ETLFindElement {

	ETLProcessor processor
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
	ETLFindElement(ETLProcessor processor, ETLDomain domain) {
		this.processor = processor
		setCurrentDomain(domain)
	}

	/**
	 * Defines a new find option. See this code:
	 * <pre>
	 * 	find Application for assetId by id with assetId
	 * 	elseFind Application for assetId by assetName, assetType with primaryName, primaryType
	 * </pre>
	 * @param domain
	 * @return
	 */
	ETLFindElement elseFind(ETLDomain domain) {
		findings.add(currentFind)
		setCurrentDomain(domain)

		this
	}

	/**
	 * Defines the dependentId for the current find element
	 * @param dependentId
	 * @return
	 */
	ETLFindElement of(String dependentId) {
		validateReference(dependentId)
		currentFind.dependentId = dependentId
		this
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
		if(!currentFind.dependentId){
			if(fields.size() != 1){
				throw ETLProcessorException.findElementWithoutDependentIdDefinition(fields)
			}
			currentFind.dependentId = fields[0]
		}
		this
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

			if(currentFind.objects && !currentFind.objects.isEmpty()){
				results = [
					size: currentFind.objects.size(),
					objects: currentFind.objects,
					matchOn: findings.size() + 1
				]
			}
		}
		processor.addFindElement(this)
		this
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

		return values.collect { def value ->
			def fieldValue

			switch(value){
				case DomainField:     //DOMAIN.name // Label name or property name from fieldSpecs
					fieldValue = ((DomainField)value).value
					break
				case Element:            // LocalVariable
					fieldValue = ((Element)value).value
					break
				case SourceField:
					fieldValue = ((SourceField)value).value // SOURCE.'application id'
					break
				default:
					fieldValue = value
					break
			}

			return fieldValue
		}
	}

	/**
	 * Checks if the current instance of processor has a project already defiend.
	 * If not It throws an exception
	 */
	private void checkProject() {
		if(!processor.project){
			throw ETLProcessorException.nonProjectDefined()
		}
	}

	ETLFindElement warn(String message) {
		this.warnMessage = message
		this.processor.addFindWarnMessage(this)
		this
	}

	/**
	 * Returns {@code true} if, and only if, {@link #currentFind} has a dependentId as {@code dependentId}.
	 * @return {@code true} if {@link #currentFind} has dependentId as {@code dependentId}, otherwise
	 * {@code false}
	 */
	boolean hasDependentId(String dependentId) {
		return this.currentFind.dependentId == dependentId
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