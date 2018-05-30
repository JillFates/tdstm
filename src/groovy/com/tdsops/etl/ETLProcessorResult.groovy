package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 * Results collected from an ETL Processor instance processing an ETL script.
 * It prepares the results used in the import process or for rendering results in the UI.
 * <br>
 * Every part of the results are covered in formatter functions.
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
	DomainResult reference
	/**
	 * Collection of results with their data fields map
	 */
	List<Map<String, DomainResult>> domains = []
	/**
	 * Result row index position in the reference.data list
	 * @see DomainResult#data
	 */
	Integer resultIndex = -1

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
			reference = new DomainResult(domain: domain.name())
			domains.add(reference)
		}
		resultIndex = -1
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
		RowResult currentData = findOrCreateCurrentRow()
		reference.addFieldName(element)
		currentData.addLoadElement(element)
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
		RowResult currentRow = findOrCreateCurrentRow()
		reference.addFieldName(findElement)
		currentRow.addFindElement(findElement)
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
		RowResult currentRow = findOrCreateCurrentRow()
		currentRow.addFoundElement(foundElement)
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
			RowResult currentRow = findOrCreateCurrentRow()
			currentRow.addFindElementWarnMessage(findElement)
		}
	}

	/**
	 *
	 * @param rowIndex
	 */
	void startRow(){
		resultIndex = -1
	}

	/**
	 * Remove the current row from results going back the previous row results
	 */
	void ignoreCurrentRow() {
		if(resultIndex >= 0){
			if(resultIndex >= reference.data.size() + 1){
				throw ETLProcessorException.ignoreOnlyAllowOnNewRows()
			}
			reference.data.remove(resultIndex)
			resultIndex = -1
		}
	}

	/**
	 * TODO: Complete docs!!
	 * @return
	 */
	private RowResult findOrCreateCurrentRow() {
		if(resultIndex == -1){
			reference.data.add(new RowResult(rowNum: processor.iterateIndex.pos))
			resultIndex = reference.data.size() - 1
		}
		return reference.data[resultIndex]
	}

	/**
	 * Return current row using resultIndex value
	 * @return an instance of RowResult
	 * @see RowResult
	 * @see ETLProcessorResult#resultIndex
	 */
	private RowResult currentRow(){
		return reference.data[resultIndex]
	}

	/**
	 * Return the value for a field name using the current row in the JSON results
	 * @param field
	 * @return an object with value content
	 *
	 */
	Object getFieldValue(String fieldName){
		if (resultIndex >= 0) {
			RowResult row = currentRow()
			if(!row.fields.containsKey(fieldName)) {
				throw ETLProcessorException.unknownDomainProperty(fieldName)
			}
			return row.fields[fieldName].value

		} else {
			throw ETLProcessorException.domainOnlyAllowOnNewRows()
		}
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
	 * @param fieldName - the field to examine for a match
	 * @param value - the value that the field should have
	 * @return true if the data row was found otherwise false
	 */
	boolean lookupInReference(String fieldName, String value) {
		// TODO : JPM 3/2018 : lookupInReference will have issues if there are multiple matches so we should look to expand the search to multiple fields/values
		Integer lookupPosition = reference.data.findIndexOf { RowResult dataRow ->
			dataRow.fields.containsKey(fieldName) && dataRow.fields[fieldName]?.value == value
		}
		if(lookupPosition >= 0){
			resultIndex = lookupPosition
			return true
		} else {
			return false
		}
	}

}

/**
 * <pre>
 *  "domains": {
 *    "domain": "Device",
 *    "fieldNames": [
 *          "assetName",
 *          "externalRefId"
 *     ],
 *
 *    "data": [ list of RowResult instances]
 * 	}
 * </pre>
 */
@CompileStatic
class DomainResult {
	String domain
	Set fieldNames = [] as Set
	List<RowResult> data = new ArrayList<RowResult>()

	/**
	 * Add the field name for an instance of Element
	 * to the fieldNames Set property
	 * @param element an instance of Element ETL class
	 */
	void addFieldName(Element element){
		fieldNames.add(element.fieldDefinition.name)
	}
	/**
	 * Add the field name for an instance of ETLFindElement
	 * into the fieldNames Set property
	 * @param findElement
	 */
	void addFieldName(ETLFindElement findElement){
		fieldNames.add(findElement.currentFind.property)
	}
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
 */
@CompileStatic
class RowResult {
	String op = 'I'
	Integer rowNum
	Integer errorCount = 0
	Boolean warn = false
	Boolean duplicate = false
	List<String> errors = []
	Boolean ignore = false
	Map<String, FieldResult> fields = [:]

	/**
	 * Add element to the current row data
	 * @param element
	 */
	void addLoadElement(Element element){
		//TODO: Review with John. ETLInitilizeSpec'test can init field defined before the load command'
		FieldResult fieldData = findOrCreateFieldData(element.fieldDefinition.name)
		fieldData.originalValue = element.originalValue?:fieldData.originalValue
		fieldData.value = element.value?:fieldData.value
		fieldData.init = element.init?:fieldData.init
	}

	/**
	 * It adds the find result in the FieldResult
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
	 * @param findElement the find element with the warn message
	 */
	void addFindElement(ETLFindElement findElement){
		FieldResult fieldData = findOrCreateFieldData((String)findElement.currentFind.property)
		fieldData.addFindElement(findElement)
		this.errorCount = fieldData.errors.size()
	}

	void addFoundElement(FoundElement foundElement){
		FieldResult fieldData = findOrCreateFieldData(foundElement.domainPropertyName)
		fieldData.addFoundElement(foundElement)
	}

	/**
	 * It adds the warn message result in the FieldResult
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
	 * @param findElement the find element with the warn message
	 */
	void addFindElementWarnMessage(ETLFindElement findElement) {
		warn = true
		errors.add(findElement.warnMessage)
		FieldResult fieldData = findOrCreateFieldData((String)findElement.currentFind.property)
		fieldData.addFindElementWarnMessage(findElement)
	}

	/**
	 * Find or creates a Field Data content based onf fieldName parameter
	 * @param element
	 * @return
	 */
	FieldResult findOrCreateFieldData(String fieldName){
		if(!fields.containsKey(fieldName)){
			fields[fieldName] = new FieldResult()
		}
		return fields[fieldName]
	}
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
 */
@CompileStatic
class FieldResult {

	Object originalValue
	Object value
	Object init
	List<String> errors = []
	Boolean warn = false
	FindResult find = new FindResult()
	Map<String, Object> create
	Map<String, Object> update

	/**
	 * Add errors list in JSON results
	 * @param errors
	 */
	private void addErrors(List<String> errors){
		if(errors){
			this.errors.addAll(errors)
		}
	}

	/**
	 * Prepares the query data Map in the ETLProcessorResult
	 * <pre>
	 *	"query": [
	 *		{
	 *			"domain": "Application",
	 *			"kv": {"id": null},
	 *	    	"errors" : ["Named parameter [id] value may not be null"]
	 *		},
	 *	]
	 * </pre>
	 * @param findElement
	 * @return
	 */
	void addFindElement(ETLFindElement findElement) {
		this.addErrors((List<String>)findElement.currentFind.errors)
		this.find.addQueryAndResults(findElement)
	}

	/**
	 * Add find wran message in JSON results
	 * @param findElement
	 */
	void addFindElementWarnMessage(ETLFindElement findElement) {
		warn = true
		errors.add(findElement.warnMessage)
	}

	/**
	 * Add FoundElement map fields in JSON results
	 * @param foundElement
	 */
	void addFoundElement(FoundElement foundElement) {
		if(foundElement.getAction() == FoundElement.FoundElementType.create){
			create = foundElement.propertiesMap
		} else {
			update = foundElement.propertiesMap
		}
	}
}

@CompileStatic
class FindResult {
	List<QueryResult> query = []
	List<Long> results = []
	Integer matchOn

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
	private void addResults(ETLFindElement findElement) {
		if(!this.results && findElement.results){
			this.results = findElement.results.objects.collect { it as Long }
			this.matchOn = findElement.results.matchOn as Integer
		}
	}
	/**
	 * Add Query content in JSON result
	 * @param findElement
	 */
	private void addQuery(ETLFindElement findElement) {
		query.add(new QueryResult(domain: findElement.currentDomain.name(), kv: (Map<String, Object>)findElement.currentFind.kv))
	}

	/**
	 * Add query and Results in JSON result
	 * @param findElement
	 */
	void addQueryAndResults(ETLFindElement findElement){
		addQuery(findElement)
		addResults(findElement)
	}

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
@CompileStatic
class QueryResult {
	String domain
	Map<String, Object> kv = [:]


	@Override
	String toString() {
		return "QueryResult{"
			.concat( "domain=")
			.concat(domain)
			.concat(", kv=")
			.concat(kv.toString())
			.concat('}')

	}
}
