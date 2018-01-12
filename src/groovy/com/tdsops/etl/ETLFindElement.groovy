package com.tdsops.etl

class ETLFindElement {

	ETLDomain domain
	ETLProcessor processor
	List<String> fields

	ETLFindElement(ETLProcessor processor, ETLDomain domain) {
		this.processor = processor
		this.domain = domain
	}
	/**
	 *
	 *
	 *
	 * @param values
	 * @return
	 */

	ETLFindElement by(String... fields) {
		this.fields = fields
		this
	}

	ETLFindElement with (Object... dataSourceFieldNames) {

		if (!processor.project) {
			throw ETLProcessorException.nonProjectDefined()
		}

		Map<String, ?> fieldsMap = [:]

		processor.currentRowResult[processor.selectedDomain].elements.each {
			fieldsMap[it?.field?.name] = it
			fieldsMap[it?.field?.label] = it
		}

		List<Map<String, ?>> dataSourceFields = []

		dataSourceFieldNames.each {
			if (fieldsMap.containsKey(it)) {
				dataSourceFields.add(fieldsMap[it])
			}
		}

		if (dataSourceFields.size() != fields.size()) {
			throw ETLProcessorException.incorrectAmountOfParameters(fields, new ArrayList(dataSourceFieldNames))
		}

		Map<String, ?> fieldsSpec = fields.withIndex().collectEntries { def field, int i ->
			[("$field".toString()): dataSourceFields[i].value]
		}

		List assets = AssetClassQueryHelper.where(processor.project, processor.selectedDomain, fieldsSpec)

		if (assets.size()) {
			//processor.addAssetEntityReferenced(assets.first())
			for (asset in assets) {
				processor.addAssetEntityReferenced(asset)
			}
		}
		this
	}

	ETLFindElement 'for'(String dependentId){


	}
}