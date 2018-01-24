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
	 *	find Application for assetId by id with assetId
	 *	elseFind Application for assetId by assetName, assetType with primaryName, primaryType
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
	 * Define the list of fields that are located in dataSource and used to find domain instances
	 * @param fields
	 * @return
	 */
	ETLFindElement by(String... fields) {
		for(field in fields){
			checkAssetFieldSpec(field)
			currentFind.findings.fields.add(field)
		}
		this
	}

	/**
	 * Sets the dataSource Fields and executes the query looking for assets
	 * based on currentFind.findings.assetFields
	 * @param values
	 * @return
	 */
	ETLFindElement with(Object... values) {

		checkProject()

		currentFind.findings.values = values as List

		if (currentFind.findings.fields.size() != values.size()) {
			throw ETLProcessorException.incorrectAmountOfParameters(
					currentFind.findings.fields,
					currentFind.findings.values)
		}

		Map<String, ?> fieldsSpec = currentFind.findings.values.withIndex().collectEntries { def value, int i ->

			def fieldValue

			if(value instanceof DomainField) { 			//DOMAIN.name // Label name or property name from fieldSpecs

			} else if(value instanceof Element) { 		// LocalVariable
				fieldValue = ((Element)value).value
			} else if(value instanceof SourceField) { 	// SOURCE.'application id'
				fieldValue = ((SourceField)value).value
			} else if(value instanceof String) {
				fieldValue = value
			} else {
				throw ETLProcessorException.UnknownVariable(value)
			}

			String fieldName = currentFind.findings.fields[i]
			[("$fieldName".toString()): fieldValue]
		}

		currentFind.findings.assets = AssetClassQueryHelper.where(processor.project, currentFind.domain, fieldsSpec)

		this
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


	ETLFindElement warn(String message) {
		this.currentFind.findings.warnMessage = message
		this.processor.addDependencyWarnMessage(this)
		this
	}

	/**
	 * Checks a fieldSpec based on asset field name
	 * @param fieldName an asset field name
	 */
	private Map<String, ?> checkAssetFieldSpec(String fieldName) {
		return processor.lookUpFieldSpecs(currentFind.domain, fieldName)
	}

	private void setCurrentDomain(ETLDomain domain) {
		currentFind = [
		        domain: domain,
				findings: [
				        fields: [],
						values: []
				],
		]
	}

}