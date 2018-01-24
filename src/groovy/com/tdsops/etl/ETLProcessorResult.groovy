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
					data  : []
			]
			domains.add(reference)
		}
	}

	/**
	 * Adds a find Element to the TEL Processor result
	 * @param findElement an instance of ETLFindElement
	 */
	void addFindElement(ETLFindElement findElement) {
		reference.find = findElement
	}

	/**
	 * Appends a loaded element in the results
	 * @param element
	 */
	void loadElement(Element element) {
		Map data = [
				op       : 'I',
				warn     : false,
				duplicate: false,
				errors   : [],
				fields: [
						("${element.field.name}".toString()): [
								value        : element.value,
								originalValue: element.originalValue
						]
				]
		]
		reference.fields.add(element.field.name)
		reference.data.add(data)



	}
}
