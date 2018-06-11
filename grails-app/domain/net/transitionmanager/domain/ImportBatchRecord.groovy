package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.ImportBatchStatusEnum
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.JsonUtil
import org.codehaus.groovy.grails.web.json.JSONObject
/**
 * ImportBatchRecord
 *
 * Used to represent a single row of information about a single domain record. This will contain discrete
 * properties as well as a complex JSON structure of data about each property that will be manipulated within
 * the domain object.
 */
class ImportBatchRecord {
	ImportBatch importBatch

	// The status of the individual record
	ImportBatchStatusEnum status = ImportBatchStatusEnum.PENDING

	// The type of operation that will/was performed on the record (Insert, Update, Delete, Undetermined)
	ImportOperationEnum operation

	// The id found by the lookup operation when only reference was found. This would be the primary
	// id of the domain being UPDATED or DELETED.
	Long domainPrimaryId

	// The row index within the data source that the current record originated
	Integer sourceRowId = 0

	// The number of attributes that have errors in the fieldsInfo JSON specification
	Integer errorCount = 0

	// A flag that indicates that the record can not be automatically posted without warning the user of
	// some concern. If set, the warning will be associated to one or more individual properties.
	Integer warn = 0

	// A flag that indicates that one or more of the reference find operations during the ETL process found
	// more than one domain object that matches the lookup values. These rows will need human intervention to
	// resolve the multiple references.
	Integer duplicateReferences = 0

	/*
	 * A JSON List<Map> of error messages that were generated by the ETL and/or the batch processing logic
	 * [
	 *     { "code": "MISSING_REF", "field": "id", "msg": "Find by ID failed" },
	 *     { "code": "DUP_REF", "field": "assetId", "msg": "Multiple records found" }
	 * ]
	 */
	String errorList = '[]'

	/*
	 * The JSON Map of fields and their meta attributes generated by the ETL process and utilized by the posting process
	 * for the lookup, creating, updating of domains plus all of the individual property values etc.
	 * The format will be similar to:
	 *
	 * "id": {
	 * 	"value":null,
	 * 	"warn":true,
	 * 	"warnMsg": "found with wrong asset class",
	 * 	"duplicate": true,
	 * 	// Used by the Posting process to deal with scenario that asset wasn't found during ETL execution. Will
	 * 	// attempt to lookup asset again during posting, hoping that the asset was created by other batch
	 * 	"find": {
	 * 		"query": [
	 * 			{   "domain": "Application",
	 * 				"kv": {"id": null}
	 * 			},
	 * 			{   "domain": "Application",
	 * 				"kv": { "assetName": "CommGen", "assetType": "Application" }
	 * 			},
	 * 			{
	 * 				// Find as Application but just the name
	 * 				"domain": "Application",
	 * 				"kv": { "assetName": "CommGen" }
	 * 			},
	 * 			{
	 * 				// Try finding in all classes but warn
	 * 				"domain": "Asset",
	 * 				"kv": { "assetName": "CommGen" },
	 * 				"warn": true,
	 * 			}
	 * 		],
	 * 		"size": 2
	 * 	},
	 * 	// The create is specified since the asset wasn't found. The logic
	 * 	// will retry the find commands above first during the posting and if not
	 * 	// found at that point the asset will be made with thes attributes
	 * 	"create": {
	 * 		"domain": "Application",
	 * 		"assetName": "CommGen",
	 * 		"assetType": "Application",
	 * 		"TM Last Seen": "2017-12-24 03:23:24Z"
	 * 	}
	 * 	// Notice that there is a "create" but no "update" because the asset was
	 * 	// not found.
	 * },
	 */
	String fieldsInfo

	Date lastUpdated

	static belongsTo = [
		importBatch: ImportBatch
	]

	static constraints = {
		domainPrimaryId nullable: true
		duplicateReferences range: 0..1
		errorList size: 2..16777215
		fieldsInfo size: 2..16777215
		lastUpdated nullable: true
		sourceRowId nullable: true
		warn range: 0..1
	}

	static mapping = {
		id column: 'import_batch_record_id'
		createInfo sqltype: 'TEXT'
		domainPrimaryId sqltype: 'INT(11)'
		duplicateReferences sqltype: 'TINYINT(1)'
		errorList sqltype: 'TEXT' // JSON
		fieldsInfo sqltype: 'TEXT' // JSON
		lastUpdated sqltype: 'DATE'
		operation  enumType: "String"
		sourceRowId sqltype: 'INT(8)'
		status sqltype: 'VARCHAR(32)',  enumType: "String"
		updateInfo sqltype: 'TEXT'
		warn sqltype: 'TINYINT(1)'
	}

	/**
	 * Used to convert the domain object into a Map object
	 * We may want to have an option to flatten some of the data from the fieldsInfo like
	 * each of the fields current value to render the datagrid (TBD)
	 */
	Map toMap(boolean minimalInfo = false) {
		Map domainMap = [
			id: id,
			importBatch: [ id: importBatch.id ],
			domainPrimaryId: domainPrimaryId,
			duplicateReferences: duplicateReferences,
			errorCount: errorCount,
			lastUpdated: lastUpdated,
			operation: operation.name(),
			sourceRowId: sourceRowId,
			status: [
				code: status.name(),
				label: status.toString()
			],
			warn: warn
		]

		if (minimalInfo) {
			// Populate the currentValues map with the current values from the fieldsInfo
			// for each field specified in the batch
			//
			domainMap.currentValues = [:]
			Map info = fieldsInfoAsMap()
			for ( fieldName in importBatch.fieldNameListAsList() ) {
				def value = (info.containsKey(fieldName) ? info[fieldName].value : null)
				domainMap.currentValues[fieldName] = value
			}
		} else {
			// Populate the full errors and fieldsInfo sections instead of the currentValues
			domainMap.errorList = errorListAsList()
			domainMap.fieldsInfo = fieldsInfoAsMap()
		}

		return domainMap
	}

	/**
	 * Used to access the errors property as an Object instead of JSON
	 */
	List<Map> errorListAsList() {
		return ( errorList ? JsonUtil.parseJsonList(errorList) : [] )
	}

	/**
	 * Used to access the fieldsInfo property as an Object instead of JSON
	 */
	JSONObject fieldsInfoAsMap() {
		return JsonUtil.parseJson(fieldsInfo)
	}

	// TODO : JPM 2/2018 : When using these setters the assignments were NOT working correctly
	//
	// Setter functions to deal with the JSON properties
	//
	// void setErrorList(Object value) {
	// 	errorList = JsonUtil.toJson(value)
	// }

	// void setFieldsInfo(Object value) {
	// 	println "** setFieldsInfo() called with value=${value.inspect()}"
	// 	fieldsInfo = JsonUtil.toJson(value)
	// }

	// Used to clear out any existing errors on the record
	void resetErrors() {
		errorList = '[]'
	}

	// Used to add an error message to the list
	void addError(String error) {
		List errors = (errorList ? JsonUtil.parseJsonList(errorList) : [])
		errors << error
		errorList = JsonUtil.toJson(errors)
	}
}
