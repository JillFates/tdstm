package com.tdsops.etl

/**
 * Results collected from an ETL Processor instance processing an ETL script.
 * <code>
 *
 */
class ETLProcessorResult {

	/**
	 * ETL Processor used to collect results in a ETL Procesor Result instance.
	 */
	ETLProcessor processor

	/**
	 * ETL info map details
	 */
	Map<String, ?> ETLInfo = [:]

	/**
	 *
	 */
	Map<String, ?> reference = [:]

	/**
	 * Current row number in the iterate loop
	 */
	Integer rowNumber = 0

	List<Map<String, ?>> domains = []

	ETLProcessorResult(ETLProcessor processor) {
		this.processor = processor
	}
	/**
	 * Adds a new json entry in results list
	 * @param domain
	 */
	void addCurrentSelectedDomain(ETLDomain domain) {

		reference = domains.find { it.domain == domain.name() }

		if (!reference) {
			reference = [
					domain: domain.name(),
					fields: [] as Set,
					data  : [[
									 op       : 'I',
									 warn     : false,
									 duplicate: false,
									 errors   : [],
									 fields   : [:]
							 ]]
			]

			domains.add(reference)
		}
	}

	/**
	 * Adds a find Element to the TEL Processor result
	 * @param findElement an instance of ETLFindElement
	 */
	void addFindElement(ETLFindElement findElement) {

		String dependentId = findElement.currentFind.dependentId

		Map<String, ?> data = currentData()

		if (!data.fields.containsKey(dependentId)) {
			throw ETLProcessorException.invalidFindCommand(dependentId)
		}

		data.fields[dependentId].find.query.add([
				domain: findElement.currentFind.domain,
				kv    : findElement.currentFind.kv
		])

	}

	/**
	 * Appends a loaded element in the results.
	 * First It adds a new element.field.name in the current domain fields list
	 * After that, It saves the new element in the data results.
	 * @param element
	 */
	void loadElement(Element element) {

		reference.fields.add(element.field.name)
		currentData().fields[element.field.name] = [
				value        : element.value,
				originalValue: element.originalValue,
				error        : false,
				find         : [
						query: []
				]
		]
	}

	/**
	 * Calculates the current data map based on row index
	 * @return a map with the current data node
	 */
	Map<String, ?> currentData() {

		if(reference.data.size() < processor.currentRowIndex){
			Map data = [
					op       : 'I',
					warn     : false,
					duplicate: false,
					errors   : [],
					fields   : [:]
			]

			reference.data.add(data)
		}

		return reference.data.last()
	}
}
