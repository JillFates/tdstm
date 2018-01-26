package com.tdsops.etl

/**
 * ETL find command implementation.
 * <code>
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
	Map<String, ?> currentFind = [:]
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
	ETLFindElement 'for'(String dependentId) {
		checkAssetFieldSpec(dependentId)
		currentFind.dependentId = dependentId
		this
	}

	/**
	 * Define the list of fields that are located in dataSource and used to find domain instances
	 * @param fields
	 * @return
	 */
	ETLFindElement by(String... fields) {
		for (field in fields) {
			checkAssetFieldSpec(field)
			currentFind.fields.add(field)
		}
		if (!currentFind.dependentId) {
			if (fields.size() != 1) {
				throw ETLProcessorException.findElementWithoutDependentIdDefinition(fields)
			}
			currentFind.dependentId = fields[0]
		}
		this
	}

	/**
	 * Sets the dataSource Fields and executes the query looking for assets
	 * based on currentFind.fields
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

		currentFind.assets = AssetClassQueryHelper.where(
				ETLDomain.lookup(currentFind.domain),
				processor.project,
				currentFind.kv)
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

		if (currentFind.fields.size() != values.size()) {
			throw ETLProcessorException.incorrectAmountOfParameters(
					currentFind.fields,
					values)
		}

		return values.collect { def value ->
			def fieldValue

			switch (value) {
				case DomainField:     //DOMAIN.name // Label name or property name from fieldSpecs
					fieldValue = ((DomainField) value).value
					break
				case Element:            // LocalVariable
					fieldValue = ((Element) value).value
					break
				case SourceField:
					fieldValue = ((SourceField) value).value // SOURCE.'application id'
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
		if (!processor.project) {
			throw ETLProcessorException.nonProjectDefined()
		}
	}

	ETLFindElement warn(String message) {
		this.currentFind.warnMessage = message
		this.processor.addDependencyWarnMessage(this)
		this
	}

	/**
	 * Checks a fieldSpec based on asset field name
	 * using the selected domain in the current script
	 * @param fieldName an asset field name
	 */
	private Map<String, ?> checkAssetFieldSpec(String fieldName) {
		return processor.lookUpFieldSpecs(processor.selectedDomain, fieldName)
	}

	private void setCurrentDomain(ETLDomain domain) {
		currentFind = [
				domain     : domain.name(),
				fields     : [],
				values     : [],
				queryParams: [:]

		]
	}

}