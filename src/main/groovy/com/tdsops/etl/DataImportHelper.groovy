package com.tdsops.etl

import com.google.gson.JsonObject
import com.tdsops.etl.ETLDomain
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.connector.Environment
import com.tds.asset.AssetEntity
import net.transitionmanager.domain.Project
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * This class provides a series of method for the
 * DataImportService.
 */
class DataImportHelper {

	/**
	 * Map that contains the validators for all the fields that need
	 * to be checked before importing.
	 */
	private final static Map fieldValidator = [
		assetName: validateRequiredField
	]

	final static String ID_FIELD = "id"
	/**
	 * Expected key in the 'create' element when dealing with person references.
	 */
	private final static String PERSON_CREATE_FIELD = "name"

	/**
	* Expected key in the 'create' element when dealing with 'by' fields.
	*/
	private final static String BY_PROPERTY_CREATE_FIELD = "byValue"

	/**
	 * Expected key in the 'create' element when dealing with manufacturers
	 */
	private final static String MANUFACTURER_CREATE_FIELD = "name"

	/**
	 * Expected key in the 'create' element when dealing with models
	 */
	private final static String MODEL_CREATE_FIELD = "modelName"

	/**
	 * Expected key in the 'create' element when dealing with racks
	 */
	private final static String RACK_CREATE_FIELD = "tag"

	/**
	 * Expected key in the 'create' element when dealing with rooms
	 */
	private final static String ROOM_CREATE_FIELD = "roomName"

	/**
	 * Expected key in the 'create' element when dealing with bundles
	 */
	private final static String MOVE_BUNDLE_CREATE_FIELD = "name"

	/**
	 * Validate the Name field is present and a value is provided
	 *
	 * @param field - the JSON object that contains all the information about the field from ETL
	 * @param fieldName - the name of the field (id in this case)
	 * @param importContext - the import context container of objects used throughout the import process
	 * @return a String containing any error message or null if successful or no id specified
	 */
	private static final validateRequiredField = { field, fieldName, importContext ->
		if (!field || !field.value) {
			return "Field ${fieldName} is required on row ${importContext.rowNumber}"
		}
		return null
	}

	/**
	 *  Check if the data for the row  is valid in terms of the content. Validate
	 *  the id if present belongs to the current project and if required fields are populated.
	 *
	 * @param rowData
	 * @param importContext
	 * @return true if data is good to import
	 */
	static boolean validateRowData(JSONObject rowData, Long domainId, Map importContext) {
		boolean valid = true

		String error = validDomainId(domainId)
		if (error) {
			importContext.errors << error + " on row ${importContext.rowNumber}"
			return false
		}

		for (fieldName in fieldValidator.keySet()) {
			Closure validator = fieldValidator[fieldName]
			JsonObject fieldJson = rowData[fieldName]
			String errorMsg = validator(rowData, fieldName, importContext)
			// If an error is detected, report it back.
			if (errorMsg) {
				importContext.errors << errorMsg
				valid = false
			}
		}

		return valid
	}

	/**
	 * Parse the original value and the value for the given field using the control
	 * specified in the field json, creating a map with these values and the error message.
	 *
	 * Note that the result map won't necessarily have the same values provided in the JSON.
	 *
	 * TODO: JPM : 2/2018 : MINOR For the time being we're returning the same values. Don't believe
	 * that the service class is even calling this. Need to think this over for the new asset import logic.
	 *
	 * @param fieldJson
	 * @param importContext
	 * @return
	 */
	static Map parseFieldValues(fieldJson, importContext) {
		String originalValue = fieldJson.originalValue
		String newValue = fieldJson.value
		String control = fieldJson.field.control
		String fieldName = fieldJson.field.name
		String errorMsg = null

		Map results = [originalValue: originalValue, newValue: newValue]

		// Determine if the field is required
		// TODO : JPM 2/2018 : Not sure that constraints are on the JSON
		if (fieldJson.field.constraints?.required == 1) {
			errorMsg = checkRequiredFieldIsNotEmpty(newValue, fieldName, importContext)
		}

		// If there was an error, store the message.
		if (errorMsg) {
			results.errorMsg = errorMsg
		} else {
			// If no errors, continue according to the field's control.
			switch (control) {
				case "Number":
					parseNumberField(originalValue, newValue, fieldName, results, importContext)
					break
				case "Options.Environment":
					parseEnvironmentField(originalValue, newValue, fieldName, results, importContext)
					break
				default:
					// We're not doing anything for strings and other types at the moment.
					results.errorMsg = null
					break
			}
		}

		return results
	}

	/**
	 * Check whether a required field is empty or not.
	 * @param value
	 * @param fieldName
	 * @param importContext
	 * @return
	 */
	private static String checkRequiredFieldIsNotEmpty(String value, String fieldName, Map importContext) {
		String errorMsg = null
		if (StringUtil.isBlank(value)) {
			errorMsg = "Field ${fieldName} cannot be null for Asset No. ${importContext.rowNumber} in class ${importContext.assetClass}."
		}
		return errorMsg
	}

	/**
	 * Analyse whether a number field was given valid values (old and new).
	 * @param originalValue
	 * @param newValue
	 * @param fieldName
	 * @param results
	 * @param importContext
	 */
	private static void parseNumberField(String originalValue, String newValue, String fieldName, Map results, Map importContext) {
		String errorMsg = null
		Long transformedOriginalValue = NumberUtil.toLong(originalValue)
		Long transformedNewValue = NumberUtil.toLong(newValue)

		// Check if the new value was given but it couldn't be parsed to long.
		if (!StringUtil.isBlank(newValue) && transformedNewValue == null) {
			errorMsg = "Value ${newValue} for field ${fieldName} is not a number. Asset No. ${importContext.rowNumber} in class ${importContext.assetClass}."
			// Check if the original value was given but it's not a number
		} else if (!StringUtil.isBlank(originalValue) && transformedOriginalValue == null) {
			errorMsg = "Value ${originalValue} for field ${fieldName} is not a number. Asset No. ${importContext.rowNumber} in class ${importContext.assetClass}."
		}

		results.errorMsg = errorMsg
	}

	/**
	 * Determine if the field contain valid Environment values.
	 * @param originalValue
	 * @param newValue
	 * @param fieldName
	 * @param results
	 * @param importContext
	 */
	private static void parseEnvironmentField(String originalValue, String newValue, String fieldName, Map results, Map importContext) {
		String errorMsg = null
		Environment transformedOriginalValue = Environment.forId(originalValue)
		Environment transformedNewValue = Environment.forId(newValue)

		// Check if the new value is a valid Environment
		if (!transformedNewValue) {
			errorMsg = "Value ${newValue} for field ${fieldName} is not a valid Environment. Asset No. ${importContext.rowNumber} in class ${importContext.assetClass}."
		} else if (!transformedOriginalValue) {
			errorMsg = "Value ${originalValue} for field ${fieldName} is not a valid Environment. Asset No. ${importContext.rowNumber} in class ${importContext.assetClass}."
		}

		results.errorMsg = errorMsg
	}

	/**
	 * Used to get the domain id for the current row. The return value will be one of the following:
	 *     null - no id was specified
	 *     > 0 - the id number of the valid domain object
	 *     -1 - an error occurred, possible causes (captured in the importContext.errors)
	 *			- invalid number
	 *			- domain object doesn't exist
	 * 			- domain object doesn't belong to the current project
	 *
	 * @param rowData - the ETL meta-data for the current row
	 * @param importContext - the objects related to the import process
	 * @return the domain id if valid, null if not specified or -1 if there was an error
	 */
	static Long getAndValidateDomainId(JSONObject rowData, Map importContext) {
		Map idField = rowData.fields[ID_FIELD]
		Long id = null
		String msg

		if (idField) {

			// Check for an error in the import
			if (idField.errors) {
				msg = idField.errors.join(', ')
				importContext.errors << "$msg on row ${importContext.rowNumber}"
				// rowData.errors << msg
				return -1
			}

			// First check if there are any results from find/elseFind
			Integer resultsFound = idField?.find?.results?.size()
			if (resultsFound > 0) {
				if (resultsFound == 1) {
					id = idField.find.results[0]
				} else {
					msg = "Multiple entities found"
					importContext.errors << msg + " on row ${importContext.rowNumber}"
					rowData.errors << msg
					id = -1
				}
			} else {
				// Check if the idField is populated directly from the ETL script
				// TODO : JPM 2/2018 : CRITICAL - this method is not working properly at this point
				if (idField?.value) {
					id = NumberUtil.toPositiveLong(idField.value)

					if (id < 1) {
						msg = "The $ID_FIELD must be a numeric value"
						importContext.errors << msg + " on row ${importContext.rowNumber}"
						rowData.errors << msg
						return -1
					}

					// Now check to see that the object exists and belongs to the current project
					// This is necessary because no find/elseFind was performed by the ETL script
					String error = validDomainId(id, importContext.project, importContext.domainClass)
					if (error) {
						importContext.errors << error + " on row ${importContext.rowNumber}"
						rowData.errors << error
						return -1
					}
				}
			}
		}

		return id
	}

	/**
	 * Validate the Id field is present, is a positive long, it's not being modified and it's part of the current project
	 * @param id - the Id of the domain object
	 * @param project - the Project that the domain object should belong to
	 * @return a String containing any error message or null if valid or no id was specified
	 */
	static String validDomainId(Long id, Project project, String domainClassName) {
		String result
		if (id) {
			Class domainClass = ETLDomain.lookup(domainClassName).getClazz()
			String domainName = GormUtil.domainShortName(domainClass)
			Map params = [id:id]
			String hql = "select count(*) from $domainName where id = :id"
			if ( GormUtil.isDomainProperty(domainClass, 'project') ) {
				hql += ' and project.id = :projectId'
				params.projectId = project.id
			}

			List results = domainClass.executeQuery(hql, params)
			Long count = results.size() == 1 ? results[0] : 0

			result = (count == 1 ? null : 'Unable to find by ID')
		}
		return result
	}

	/**
	 * Find a given field in the asset json
	 * @param asset
	 * @param fieldName
	 * @return
	 */
	static Map findFieldInAssetJson(asset, fieldName) {
		Map field = asset.fields[fieldName]

		if (fieldName == "id") {
			if (!field) {
				// Construct what looks like a field for the id
				field = [
					field: [name: 'id', control: 'Number', label: 'ID'],
				]
			}

			// This is a special case that will potentially uses the reference and the the id element
			if (field.containsKey('find')) {
				int numIds = field.find.results.size()
				if (numIds == 1) {
					field.value = field.find.results[0]
					field.originalValue = field.find.results[0]
				} else if (numIds > 1) {
					field.hasError = 1
					field.error = 'Duplicate asset references'
				}
			}
		}

		println "**** findFieldInAssetJson fieldName:$fieldName, field:$field"

		return field
	}

	/**
	 * When validating references the same logic always applies: if the field's value
	 * is null, check if the "create" element was specified
	 */
	static final Object genericReferenceValueResolver( JSONObject fieldJson, String createField) {
		def value = fieldJson.value
		if (!value) {
			if (fieldJson.create) {
				value = fieldJson.create[createField]
			}
		}
		return value
	}

	/**
	 * Resolve the correct value for a field referencing a person. If null was given,
	 * it will look for the personName in the 'create' element, if it exists.
	 */
	static final personValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, PERSON_CREATE_FIELD)
	}

	/**
	 * Resolve the correct value for a by property. If null was given,
	 * it will look for whatever was given for byValue in the 'create' element, if it exists.
	 */
	static final byFieldValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, BY_PROPERTY_CREATE_FIELD)
	}

	/**
	 * Resolve the correct value for a field referencing a manufacturer. If null was given,
	 * it will look for the name in the 'create' element, if it exists.
	 */
	static final manufacturerValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, MANUFACTURER_CREATE_FIELD)
	}

	/**
	 * Resolve the correct value for a field referencing a model. If null was given,
	 * it will look for the modelName in the 'create' element, if it exists.
	 */
	static final modelValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, MODEL_CREATE_FIELD)
	}

	/**
	 * Resolve the correct value for a field referencing a rack. If null was given,
	 * it will look for the tag in the 'create' element, if it exists.
	 */
	static final rackValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, RACK_CREATE_FIELD)
	}

	/**
	 * Resolve the correct value for a field referencing a room. If null was given,
	 * it will look for the roomName in the 'create' element, if it exists.
	 */
	static final roomValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, ROOM_CREATE_FIELD)
	}

	/**
	 * Resolve the correct value for a field referencing a move bundle. If null was given,
	 * it will look for the name in the 'create' element, if it exists.
	 */
	static final moveBundleValueResolver = { JSONObject fieldJson ->
		return genericReferenceValueResolver(fieldJson, MOVE_BUNDLE_CREATE_FIELD)
	}

	static final standardFieldResolver = { JSONObject fieldJson ->
		return fieldJson.value
	}
	/**
	 * The keys in this map are those fields in the AssetEntity class
	 * that reference other domain classes.
	 */
	private final static fieldValueResolver = [
		appOwner    : personValueResolver,
		sme         : personValueResolver,
		sme2        : personValueResolver,

		// TODO : JPM 2/2018 : These are commented out at the moment since they're currently just handled as a
		// text field. They may be reintroduced for the Dependency and in Asset imports. If they are permenently
		// removed than the above code (byFieldValueResolver) and related can be pulled.
		// shutdownBy  : byFieldValueResolver,
		// startedBy   : byFieldValueResolver,
		// testingBy   : byFieldValueResolver,

		manufacturer: manufacturerValueResolver,
		model       : modelValueResolver,
		sourceRack  : rackValueResolver,
		targetRack  : rackValueResolver,
		sourceRoom  : roomValueResolver,
		targetRoom  : roomValueResolver,
		moveBundle  : moveBundleValueResolver
	].withDefault { String field ->
		standardFieldResolver
	}

	/**
	 * Resolve the correct value for a field from its corresponding JSON.
	 *
	 * @param fieldName
	 * @param fieldJson
	 * @return
	 */
	static Object resolveFieldValue(String fieldName, JSONObject fieldJson) {
		Closure valueResolver = fieldValueResolver[fieldName]
		return valueResolver(fieldJson)
	}

	/**
	 * Used to create the results that will be saved into the batch record
	 * @param importContext - Map of the import context objects
	 * @return the text containts the stats and errors in HTML format
	 */
	static String createBatchResultsReport(Map importContext) {
		StringBuilder sb = new StringBuilder("<h3>Import Batch Loading Results</h3>\n")
		sb.append("<p>Results: <ul>\n")
		int rowsRead = importContext.rowsCreated + importContext.rowsSkipped
		sb.append("<li>${importContext.domainClass}: $rowsRead Row(s) read, ${importContext.rowsCreated} Loaded, ${importContext.rowsSkipped} Erred</li>\n")
		sb.append("</ul></p><br>\n")
		if (importContext.errors) {
			sb.append("<p>Errors: <ul>\n")
			for (e in importContext.errors) {
				sb.append("<li>${e}\n")
			}
			sb.append("</ul></p><br>\n")
		}

		return sb.toString()
	}

}
