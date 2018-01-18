package com.tdsops.etl

/**
 * ETL find command implementation.
 * <code>
 *  // Try to find the Application using different searches
 * 	find Application by id with assetId for assetId
 * 	find Application by assetName, assetType with primaryName, primaryType for assetId
 * 	find Application by assetName with primaryName for assetId
 * 	find Asset by assetName with primaryName for assetId warn 'found with wrong asset class'
 * </code>
 * @param values
 * @return
 */
class ETLFindElement {

	ETLDomain domain
	ETLProcessor processor
	List<String> fields
	List<Map<String, ?>> dataSourceFields = []
	String dependentId
	String warnMessage

	/**
	 * ETLFindElement instances creation defined by the ETLProcessor instance and a particular value of ETL Domain
	 * @param processor
	 * @param domain
	 */
	ETLFindElement(ETLProcessor processor, ETLDomain domain) {
		this.processor = processor
		this.domain = domain
	}
	/**
	 * Define the list of fields that are located in dataSource and used to find domain instances
	 * @param fields
	 * @return
	 */
	ETLFindElement by(String... fields) {
		this.fields = fields
		this
	}

	ETLFindElement with(Element... dataSourceFieldNames) {

	}
	/**
	 *
	 * @param dataSourceFieldNames
	 * @return
	 */
	ETLFindElement with(Object... dataSourceFieldNames) {

		if (!processor.project) {
			throw ETLProcessorException.nonProjectDefined()
		}

		Map<String, ?> fieldsMap = [:]

		processor.currentRowResult[domain].elements.each {
			fieldsMap[it?.field?.name] = it
			fieldsMap[it?.field?.label] = it
		}

		dataSourceFieldNames.each {
			if (fieldsMap.containsKey(it)) {
				dataSourceFields.add(fieldsMap[it])
			}
		}

		if (dataSourceFields.size() != fields.size()) {
			throw ETLProcessorException.incorrectAmountOfParameters(fields, new ArrayList(dataSourceFieldNames))
		}

		Map<String, ?> fieldsSpec = fields.withIndex().collectEntries { def field, int i ->


//			if(field instanceof DomainField) { //DOMAIN.name // Label name or property name from fieldSpecs
//
//			} else if(field instanceof Element) { //LocalVariable
//
//			} else if(field instanceof SourceField) { // SOURCE.'application id'
//
//			} else if(field instanceof String) {
//
//			} else {
//				throw new UNknowVariableException()
//			}


			[("$field".toString()): dataSourceFields[i].value]
		}

		List assets = AssetClassQueryHelper.where(processor.project, domain, fieldsSpec)

		if (assets.size()) {
			for (asset in assets) {
				processor.addAssetEntityReferenced(asset)
			}
		}

		this.processor.addDependency(this)
		this
	}

	ETLFindElement 'for'(String dependentId) {
		this.dependentId = dependentId
		this.processor.addDependency(this)
		this
	}

	ETLFindElement warn(String message) {
		this.warnMessage = message
		this.processor.addDependencyWarnMessage(this)
		this
	}
}