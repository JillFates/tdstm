package com.tdsops.etl

/**
 * Results collected from an ETL Processor instance processing an ETL script.
 * It prepares the results used in the import process or for rendering results in the UI.
 * <br>
 * Every part of the results are covered in formatter functions.
 *
 * @see ETLProcessorResult#initialRowDataMap()
 * @see ETLProcessorResult#initialFieldDataMap(com.tdsops.etl.Element)
 * @see ETLProcessorResult#queryDataMap(com.tdsops.etl.ETLFindElement)
 * @see ETLProcessorResult#addWarnMessageDataMap(java.util.Map, com.tdsops.etl.ETLFindElement)
 * @see ETLProcessorResult#addResultsDataMap(java.util.Map, com.tdsops.etl.ETLFindElement)
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
	 * Current reference for the domain instance and its contents
	 */
	Map<String, ?> reference = [:]
	/**
	 * Collection of results with their data fields map
	 */
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

		if(!reference){
			reference = [
				domain: domain.name(),
				fields: [] as Set,
				data: [initialRowDataMap()]
			]

			domains.add(reference)
		}
	}

	/**
	 * Adds a find Element to the ETL Processor result.
	 * @param findElement is an instance of ETLFindElement
	 * 			used to calculate a query data result
	 */
	void addFindElement(ETLFindElement findElement) {

		String dependentId = findElement.currentFind.dependentId

		Map<String, ?> data = currentData()

		if(!data.fields.containsKey(dependentId)){
			throw ETLProcessorException.invalidFindCommand(dependentId)
		}

		Map<String, ?> find = data.fields[dependentId].find

		find.query.add(queryDataMap(findElement))

		if(findElement.results){
			addResultsDataMap(find, findElement)
		}
	}

	/**
	 * It adds a wanr message in the result instance
	 * <pre>
	 * 		find Application 'for' id by id with SOURCE.'application id'
	 * 		elseFind Application 'for' id by appVendor with DOMAIN.appVendor warn 'found without asset id field'
	 * <pre>
	 * @param findElement
	 */
	void addFindWarnMessage(ETLFindElement findElement) {

		if(findElement.currentFind.objects){
			Map<String, ?> data = reference.data.last()
			addWarnMessageDataMap(data.fields[findElement.currentFind.dependentId], findElement)
		}
	}

	/**
	 * Add a FoundElement in the result based on its dependentId
	 * <pre>
	 *		whenFound asset create {
	 *			assetClass Application
	 *			assetName primaryName
	 *			assetType primaryType
	 *			"SN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param foundElement
	 */
	void addFoundElement(FoundElement foundElement){

		Map<String, ?> data = reference.data.last()
		if(!data.fields[foundElement.domainPropertyName]){
			throw ETLProcessorException.notCurrentFindElement()
		}

		Map<String, ?> field = data.fields[foundElement.domainPropertyName]
		field[foundElement.actionName] = foundElement.propertiesMap
	}

	/**
	 * Appends a loaded element in the results.
	 * First It adds a new element.field.name in the current domain fields list
	 * After that, It saves the new element in the data results.
	 * @param element
	 */
	void loadElement(Element element) {
		reference.fields.add(element.field.name)
		currentData().fields[element.field.name] = initialFieldDataMap(element)
	}

	/**
	 * Calculates the current data map based on row index
	 * @return a map with the current data node
	 */
	Map<String, ?> currentData() {
		if(reference.data.size() < processor.currentRowIndex){
			reference.data.add(initialRowDataMap())
		}
		return reference.data.last()
	}

	/**
	 * Init a row data map.
	 * <pre>
	 *	"data": [
	 *		{
	 * 		    "op": "I",
	 * 		    "warn": true,
	 * 		    "duplicate": true,
	 * 		    "errors": [],
	 * 		    "fields": { }
	 * 	    }
	 * </pre>
	 * @return
	 */
	private Map<String, ?> initialRowDataMap() {
		return [
			op: 'I',
			warn: false,
			duplicate: false,
			errors: [],
			fields: [:]
		]
	}

	/**
	 * Prepares some of the fields result in the data result.
	 * <pre>
	 *	"asset": {
	 *	    "value":null,
	 *		"warn":true,
	 *		"warnMsg": "found with wrong asset class",
	 *		"duplicate": true,
	 *			"find": {
	 *				"query": [
	 *					{"domain": "Application", "kv": {"id": null}},
	 *					{"domain": "Application", "kv": { "assetName": "CommGen", "assetType": "Application" }},
	 *					{"domain": "Application", "kv": { "assetName": "CommGen" }},
	 *					{"domain": "Asset", "kv": { "assetName": "CommGen" }, "warn": true}
	 *				],
	 *				"size": 2,
	 *				"matchOn": 2,
	 *				"results": [12312,123123,123123123]
	 *			},
	 *		}
	 * </pre>
	 * @param element an element instance used to populate some of the data fields
	 * @return a Map that contains a final structure of the field node in ETLProcessorResult
	 */
	private Map<String, ?> initialFieldDataMap(Element element) {
		return [
			value: element.value,
			originalValue: element.originalValue,
			error: false,
			warn: false,
			find: [
				query: []
			]
		]
	}

	/**
	 * Prepares the query data Map in the ETLProcessorResult
	 * <pre>
	 *	"query": [
	 *		{
	 *			"domain": "Application", "kv": {"id": null}
	 *		},
	 *	]
	 * </pre>
	 * @param findElement
	 * @return
	 */
	private Map<String, ?> queryDataMap(ETLFindElement findElement) {
		return [
			domain: findElement.currentFind.domain,
			kv    : findElement.currentFind.kv
		]
	}

	/**
	 * It adds the warn message result in the field Data Map
	 * <pre>
	 * "asset": {
	 * 		....
	 * 		"warn":true,
	 * 		"warnMsg": "found with wrong asset class",
	 * 		....
	 * 	}
	 * </pre>
	 * @param fieldDataMap a field data map
	 * @param findElement the find element with the warn message
	 */
	private void addWarnMessageDataMap(Map<String, ?> fieldDataMap, ETLFindElement findElement) {
		fieldDataMap.warn = true
		fieldDataMap.warnMsg = findElement.warnMessage
	}

	/**
	 * It prepares query map results with the domain.data
	 * following the current format:
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
	 * Size is the total amount of results.
	 * Results are the id of the domain classes collected by the find command
	 * and matcOn defines the ordinal position
	 * in the query object list where those results where found.
	 * @param fieldDataMap a field data map
	 * @param findElement the find element with the warn message
	 */
	private void addResultsDataMap(Map<String, ?> fieldDataMap, ETLFindElement findElement) {
		fieldDataMap.size = findElement.results.size
		fieldDataMap.results = findElement.results.objects.collect { it.id }
		fieldDataMap.matchOn = findElement.results.matchOn
	}
}
