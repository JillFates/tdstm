package com.tdsops.etl
/**
 * ETL find command implementation.
 * <code>
 *  domain Dependency
 *  // Try to find the Application using different searches
 * 	find Application 	 by 'id' 				     with 'assetId' into 'property'
 * 	elseFind Application by 'assetName', 'assetType' with primaryNameVar, primaryTypeVar into 'property'
 * 	elseFind Application by 'assetName'            with primaryNameVar into 'property'
 * 	elseFind Asset 		 by 'assetName'            with primaryNameVar into 'property' warn 'found with wrong asset class'
 * </code>
 * @param values
 * @return
 */
class ETLFindElement implements ETLStackableCommand{

	/**
	 * Reference to the ETLProcessor instance that created this instance of ETLFindElement
	 * @see ETLFindElement(ETLProcessor processor, ETLDomain domain, Integer rowIndex)
	 */
	ETLProcessor processor
	/**
	 * Each command in sequence of find/elseFind commands has defined the row index in the iteration loop.
	 */
	Integer rowIndex
	/**
	 * One warn message could be added in a sequence of find/elseFind commands
	 */
	String warnMessage
	/**
	 * A sequence of find/elseFind commands are associated to a ETLDomain value
	 */
	ETLDomain currentDomain
	/**
	 * This variable contains the current find command params and results in a sequence of find/elseFind commands
	 */
	Map<String, ?> currentFind = [:]
	/**
	 * Total results collected towards a sequence of find/elseFind commands
	 */
	Map<String, ?> results
	/**
	 * List of references to each one in a sequence of find/elseFind commands
	 */
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
	 * 	find Application by 'id' with 'assetId' into 'assetId'
	 * 	elseFind Application by 'assetName', 'assetType' with primaryNameVar, primaryTypeVar into 'assetId'
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
			ETLFieldSpec fieldSpec = checkAssetFieldSpec(field)
			currentFind.fields.add(fieldSpec.name)
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

		if(!results?.objects){

			try{
				currentFind.objects = DomainClassQueryHelper.where(
					ETLDomain.lookup(currentFind.domain),
					processor.project,
					currentFind.kv)
			} catch(all){

				processor.debugConsole.debug("Error in find command: ${all.getMessage()} ")
				if(currentFind.errors == null) {
					currentFind.errors = []
				}
				currentFind.errors.add(all.getMessage())
			}

			results = [
				objects : [],
				matchOn: null
			]
			if(currentFind.objects && !currentFind.objects.isEmpty()){
				results.objects = currentFind.objects
				results.matchOn = findings.size()

				if(currentFind.objects.size() > 1){
					currentFind.errors = ['The find/elseFind command(s) found multiple records']
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

		return values.collect { ETLValueHelper.valueOf(it) }
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
	 * 	find Application by 'id' with 'assetId' into 'assetId'
	 * 	elseFind Application by 'assetName', 'assetType' with primaryNameVar, primaryTypeVar into 'assetId' warn 'found with wrong asset class'
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

		if (! currentFind.fields ) {
			missingProperties << 'by'
		}

		if (! currentFind.values ) {
			missingProperties << 'with'
		}

		if (! currentFind.property ) {
			missingProperties << 'into'
		}

		if (missingProperties) {
			error = "find/elseFind statement is missing required ${missingProperties} ${(missingProperties.size() < 2) ? 'keyword' : 'keywords'}"
		}

		return error
	}
}