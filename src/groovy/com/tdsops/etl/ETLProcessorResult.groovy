package com.tdsops.etl

/**
 * Results collected from an ETL Processor instance processing an ETL script.
 * It prepares the results used in the import process or for rendering results in the UI.
 * <br>
 * Every part of the results are covered in formatter functions.
 *
 * @see ETLProcessorResult#initialRowDataMap()
 * @see ETLProcessorResult#initialFieldDataMap(java.lang.String, java.lang.String, java.lang.String)
 * @see ETLProcessorResult#queryDataMap(com.tdsops.etl.ETLFindElement)
 * @see ETLProcessorResult#addWarnMessageInData(java.util.Map, com.tdsops.etl.ETLFindElement)
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

	/**
	 * Defines if a <b>lookup</b> command found a result
	 * @see ETLFindElement
	 */
	Map<String, ?> rowFoundInLookup = null

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
				fieldNames: [] as Set,
				data: [initialRowDataMap()]
			]

			domains.add(reference)
		}
	}

	/**
	 * Adds a find Element to the ETL Processor result.
	 * It needs to collect results and errors.
	 * Errors are located at the field level or at the row level.
	 * Results are added using find query results
	 * @see ETLFindElement#currentFind
	 * @param findElement is an instance of ETLFindElement
	 * 			used to calculate a query data, results and errors
	 */
	void addFindElement(ETLFindElement findElement) {

		String property = findElement.currentFind.property

		Map<String, ?> rowDataMap = currentRowData()
		rowDataMap.rowNum = findElement.rowIndex

		if(!rowDataMap.fields.containsKey(property)){
			reference.fieldNames.add(property)
			rowDataMap.fields[property] = initialFieldDataMap(null, null, null)
		}

		Map<String, ?> fieldDataMap = rowDataMap.fields[property]
		List findCommandErrors  = findElement.currentFind.errors


		if(findCommandErrors){
			addErrorsToCurrentRow(fieldDataMap, findCommandErrors)
			// After add errors at the field level
			// we need to sum the total amount of errors at the row level
			rowDataMap.errorCount += findCommandErrors.size()
		}

		Map<String, ?> findDataMap = fieldDataMap.find
		findDataMap.query.add(queryDataMap(findElement))
		addResultsDataMap(findDataMap, findElement)
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
			addWarnMessageInData(data, findElement)
		}
	}

	/**
	 * Add a FoundElement in the result based on its property
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
	 * and if it already exits, updates that element with the element values.
	 * After that, It saves the new element in the data results.
	 *
	 * @param element
	 */
	void loadElement(Element element) {

		Map<String, ?> currentData = currentRowData()
		currentData.rowNum = element.rowIndex

		if(currentData.fields[element.fieldDefinition.name]) {
			updateFieldDataMap(currentData, element)

		} else {
			reference.fieldNames.add(element.fieldDefinition.name)
			currentData.fields[element.fieldDefinition.name] = initialFieldDataMap(element.originalValue, element.value, element.init)
		}
	}

	/**
	 * Mark as ignore the current row.
	 * Current row is determined by reference.data.last()
	 * @see ETLProcessorResult#removeIgnoredRows()
	 */
	void ignoreCurrentRow() {
		currentRowData().ignore = true
	}

	/**
	 * Removes ignored rows in the current reference.
	 */
	def removeIgnoredRows() {
		if(reference.data.last().ignore) {
			reference.data = reference.data.dropRight(1)
		}
	}

	/**
	 * Calculates the current data map based on row index
	 * It check if it isn necessary to init a new row
	 * based on the latest data in the current reference
	 * and the 'currentRowIndex'
	 * if in the current iteration there is a lookup result, then it returns the looked up row.
	 * @see ETLProcessorResult#rowFoundInLookup
	 * @return a map with the current data node
	 */
	Map<String, ?> currentRowData() {

		if(rowFoundInLookup){
			return rowFoundInLookup
		}

		if(reference.data.isEmpty()){
			reference.data.add(initialRowDataMap())
		} else if (reference.data.last().rowNum &&
			reference.data.last().rowNum  < processor.currentRowIndex){
			reference.data.add(initialRowDataMap())
		}
		return reference.data.last()
	}

	/**
	 * Used to render the ETLProcessorResult instance as a Map object that will contain the following:
	 * 		ETLInfo <Map>
	 * 		domains <List><Map>
	 *
	 * @return A map of this object
	 */
	Map<String, ?> toMap() {
		return [
			ETLInfo: this.ETLInfo,
			domains: this.domains
		]
	}


	/**
	 * Init a row data map.
	 * <pre>
	 *	"data": [
	 *		{
	 * 		    "op": "I",
	 * 		    "errorCount": 0,
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
			errorCount: 0,
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
	 *	    "value": "",
	 *	    "originalValue": "",
	 *	    "init": "Default Value"
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
	 *				"matchOn": 2,
	 *				"results": [12312,123123,123123123]
	 *			},
	 *		}
	 * </pre>
	 * @param originalValue the original valu from DataSet
	 * @param value value modified by transformations
	 * @param initValue initial value
	 * @return a Map that contains a final structure of the field node in ETLProcessorResult
	 */
	private Map<String, ?> initialFieldDataMap(Object originalValue, Object value, Object initValue) {
		Map<String, ?> dataMap = [
			value: value,
			originalValue: originalValue,
			errors: [],
			warn: false,
			find: [
				query: []
			]
		]

		if(initValue){
			dataMap.init = initValue
		}
		return dataMap
	}

	/**
	 * Prepares the query data Map in the ETLProcessorResult
	 * <pre>
	 *	"query": [
	 *		{
	 *			"domain": "Application",
	 *			"kv": {"id": null},
	 *	    	"error" : "Named parameter [id] value may not be null"
	 *		},
	 *	]
	 * </pre>
	 * @param findElement
	 * @return
	 */
	private Map<String, ?> queryDataMap(ETLFindElement findElement) {

		Map<String, ?> queryDataMap = [
			domain: findElement.currentFind.domain,
			kv    : findElement.currentFind.kv
		]

		return queryDataMap
	}

	/**
	 * It adds the warn message result in the field Data Map
	 * <pre>
	 *  "data": {
	 *    "warn":true,
	 *    "errors": ["found with wrong asset class"],
	 *
	 *    "asset": {
	 * 		....
	 * 		"warn":true,
	 * 		"errors": ["found with wrong asset class"],
	 * 		    ....
	 * 	    }
	 * 	}
	 * </pre>
	 * @param data a row data map
	 * @param findElement the find element with the warn message
	 */
	private void addWarnMessageInData(Map<String, ?> data, ETLFindElement findElement) {
		//TODO. Add this information at the row level too.
		data.warn = true
		data.errors.add(findElement.warnMessage)

		Map<String, ?> fieldDataMap = data.fields[findElement.currentFind.property  ]
		fieldDataMap.warn = true
		fieldDataMap.errors.add(findElement.warnMessage)
	}

	/**
	 * It prepares query map results with the domain.data
	 * following the current format:
	 * <pre>
	 *  [
	 * 		"matchOn": 2,
	 * 		"results": [
	 * 			115123,
	 * 			115123,
	 * 			115123
	 * 		]
	 * 	]
	 * </pre>
	 * Results are the id of the domain classes collected by the find command
	 * and matcOn defines the ordinal position
	 * in the query object list where those results where found.
	 * @param fieldDataMap a field data map
	 * @param findElement the find element with the warn message
	 */
	private void addResultsDataMap(Map<String, ?> fieldDataMap, ETLFindElement findElement) {
		fieldDataMap.results = findElement.results.objects.collect { it.id }
		fieldDataMap.matchOn = findElement.results.matchOn
	}

	/**
	 * Using the current field in data results it updates using element parameter
	 * <pre>
	 * "fields": {
	 *      "appVendor": {
	 *      "value": "Microsoft",
	 *      "originalValue": "Microsoft",
	 *      "init": "Apple",
	 *      "error": false,
	 *      "warn": false,
	 *      "find": {
	 *          "query": []
	 *          }
	 * }
	 * </pre>
	 * If element contains an init value then it a case of update an field
	 * <pre>
	 *  read labels
	 *  iterate {
	 *      domain Application
	 *      extract 'vendor name' load appVendor
	 *      initialize appVendor with 'Apple'
	 *  }
	 * </pre>
	 * if not, then it is a case to update coming from an extract/load command
	 * <pre>
	 *  read labels
	 *  iterate {
	 *      domain Application
	 *      initialize appVendor with 'Apple'
	 *      extract 'vendor name' load appVendor
	 *  }
	 * </pre>
	 * @param currentData
	 * @param element
	 */
	private void updateFieldDataMap(Map<String, ?> currentData, Element element) {
		Map<String, ?> field = currentData.fields[element.fieldDefinition.name]

		if(element.init){
			field.init = element.init
		} else{
			field.value = element.value
			field.originalValue = element.originalValue
		}
	}

	/**
	 * Look up a field name that contain a value equals to the value and if found then the current
	 * result reference will be move back to a previously processed row for the domain. This is done
	 * by setting the rowFoundInLookup to the data row reference in the in the results.
	 *
	 * If the object is not found then future references to current result should be referring to the
	 * last row in the data result.
	 *
	 * For example the following command:
	 * <pre>
	 *  iterate {
	 *    .....
	 *    lookup appVendor with 'Microsoft'
	 *    .....
	 *    if(LOOKUP) {
	 *        load custom1 with 'App Vendor Found'
	 *    }
	 *  }
	 *
	 * </pre>
	 * <pre>
	 *  "fields": {
	 *     "appVendor": {
	 *          "value": "Microsoft",
	 *          .....
	 *          }
	 *     }
	 *     "custom1": {
	 *         "value": "App Vendor Found",
	 *         "originalValue": "App Vendor Found",
	 *         .....
	 *     }
	 *  }
	 * </pre>
	 * If the value is found, then it is used in the following commands during the iteration
	 * @see ETLProcessorResult#rowFoundInLookup
	 * @param fieldName - the field to examine for a match
	 * @param value - the value that the field should have
	 * @return true if the data row was found otherwise false
	 */
	boolean lookupInReference(String fieldName, String value) {
		// TODO : JPM 3/2018 : lookupInReference will have issues if there are multiple matches so we should look to expand the search to multiple fields/values
		rowFoundInLookup = reference.data.find { Map<String, ?> dataRow ->
			dataRow.fields.containsKey(fieldName) && dataRow.fields[fieldName]?.value == value
		}

		return (rowFoundInLookup != null)
	}

	/**
	 * Release the reference to the current result
	 * in the latest lookup command executed
	 * In every iteration, every row to be processed cleans this out.
	 * @see ETLProcessor#doIterate(java.util.List, groovy.lang.Closure)
	 */
	void releaseRowFoundInLookup(){
		rowFoundInLookup = null
	}

	/**
	 * Adds errors to the field Error list.
	 * @param field a field data map result content
	 * @param errors a list of errors to be added in field.property.errors list
	 */
	void addErrorsToCurrentRow(Map<String, ?> field, List<String> errors){
		if(!field.errors){
			field.errors = []
		}
		field.errors.addAll(errors)
	}
}
