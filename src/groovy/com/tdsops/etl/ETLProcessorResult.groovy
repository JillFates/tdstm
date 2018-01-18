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
					fields: [],
					data  : [:]
			]
			domains.add(reference)
		}
	}

	/**
	 * Collects all the necessary information to build an entry in the 'Dependency' map results
	 * @param findElement an instance of ETLFindElement
	 */
	void addDependency(ETLFindElement findElement) {
		//Dependency.fields.addAll()
	}

	void loadElement(ETLDomain domain, Element element) {


	}
}
