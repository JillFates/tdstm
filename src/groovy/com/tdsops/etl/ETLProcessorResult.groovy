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
	Map<String, ?> ETLInfo

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
		this.ETLInfo = [
				originalFilename: processor.dataSetFacade.fileName()
		]
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
	 * Adds a find Element to the ETL Processor result.
	 * It also prepares query map results.
	 * with the domain.data following the current format:
	 * <pre>
	 *  [
	 *  	"size": 3,
	 * 		"matchOn": 2,
	 * 		"results": [
	 * 			115123,
	 * 			115123,
	 * 			115123
	 * 		]
	 * 	]
	 * </pre>
	 * Size is the total amount of results. Results are the id of the domain classes
	 * collected by the find command, and matcOn defines the ordinal position
	 * in the query object list where those results where found.
	 * @param findElement is an instance of ETLFindElement
	 * 			used to calculate a query data result
	 */
	void addFindElement(ETLFindElement findElement) {

		String dependentId = findElement.currentFind.dependentId

		Map<String, ?> data = currentData()

		if (!data.fields.containsKey(dependentId)) {
			throw ETLProcessorException.invalidFindCommand(dependentId)
		}

		Map<String, ?> find = data.fields[dependentId].find

		find.query.add([
				domain: findElement.currentFind.domain,
				kv    : findElement.currentFind.kv
		])

		if (findElement.results) {
			find.size = findElement.results.size
			find.results = findElement.results.objects.collect {it.id}
			find.matchOn = findElement.results.matchOn
		}
	}

	/**
	 * Ass a war message in the result instance
	 * <pre>
	 *		find Application 'for' id by id with SOURCE.'application id'
	 *		elseFind Application 'for' id by appVendor with DOMAIN.appVendor warn 'found without asset id field'
	 * <pre>
	 * @param findElement
	 */
	void addFindWarnMessage(ETLFindElement findElement) {

		if(findElement.currentFind.objects){
			Map<String, ?> data = reference.data.last()
			data.fields[findElement.currentFind.dependentId].warn = true
			data.fields[findElement.currentFind.dependentId].warnMsg = findElement.warnMessage
		}
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
				warn     : false,
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

		if (reference.data.size() < processor.currentRowIndex) {
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
