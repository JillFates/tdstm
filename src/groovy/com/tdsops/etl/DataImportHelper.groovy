package com.tdsops.etl

import com.google.gson.JsonObject
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.agent.Environment
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
		id : validateIdField,
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
	 * Validate the Name field is present and a value is provided.
	 */
	private static final validateRequiredField = { field, fieldName params ->
		String errorMsg = null
		// Check if the field is null or no value is given.
		if (!field || !field.value) {
			errorMsg = "Missing required field '${fieldName}' for Asset No. ${params.assetIdx} in domain ${params.assetClass}."
		}
		return errorMsg
	}

	/**
	 *  Validate the Id field is present, is a positive long, it's not being modified
	 *  and it's part of this project.
	 */
	static final validateIdField = { field, fieldName, params ->
		return null
		/*
		String errorMsg = null
		// Retrieve the 'id' field from the json.
		def field = findFieldInAssetJson(asset, ID_FIELD)
		if (field) {
			// Try to convert both, the old and the new value, to Long.
			Long oldId = NumberUtil.toPositiveLong(field.originalValue)
			Long newId = NumberUtil.toPositiveLong(field.value)
			if (oldId && newId) {
				// If the value match, check the id corresponds to an asset of this project.
				if (oldId == newId) {
					// Look up assets with this id for the current project.
					int count = AssetEntity.where {
						id == oldId
						project == params.project
					}.count()
					// If no asset found, report the error.
					if (count == 0) {
						errorMsg = "The Asset No. ${params.assetIdx} in class ${params.assetClass} doesn't exist or it's not part of this project."
					}
				} else {
					// If the values don't match, report the error.
					errorMsg = "Changing the'id' for Asset No. ${params.assetIdx} in class ${params.assetClass} is not allowed."
				}
			} else {
				// If either of the values couldn't been parsed, report a format error.
				errorMsg = "Wrong format for field 'id' for Asset No. ${params.assetIdx} in class ${params.assetClass}."
			}
		} else {

			if (params.containsKey('reference')) {

			} else {
				// The 'id' field is required, report the error if it couldn't been found.
				errorMsg = "Missing required field 'id' for asset (row ${params.assetIdx}) in class ${params.assetClass}"
			}
		}

		return errorMsg
		*/
	}

	/**
	 *  Check if the json for this asset is valid in terms of the content. Validate
	 *  the id is present (and wasn't changed) and also the asset name.
	 *
	 *  If an error is detected, an exception will be thrown.
	 *
	 * @param assetJson
	 * @param params - additional parameters required for logging
	 */
	static boolean validateAsset(assetJson, Map params) throws RuntimeException {
		boolean validAsset = true
		// Validate fields until the first error is detected.
		for (fieldName in fieldValidator.keySet()) {
			Closure validator = fieldValidator[fieldName]
			JsonObject fieldJson = assetJson[fieldName]
			String errorMsg = validator(fieldJson, fieldName, params)
			// If an error is detected, report it back.
			if (errorMsg) {
				// TODO : JPM 2/2018 : errors in validateAsset are lost
				fieldJson.error = errorMsg
				validAsset = false
			}
		}

		return validAsset

	}

	/**
	 * Parse the original value and the value for the given field using the control
	 * specified in the field json, creating a map with these values and the error message.
	 *
	 * Note that the result map won't necessarily have the same values provided in the JSON.
	 *
	 * TODO: For the time being we're returning the same values.
	 *
	 * @param fieldJson
	 * @param params
	 * @return
	 */
	static Map parseFieldValues(fieldJson, params) {
		String originalValue = fieldJson.originalValue
		String newValue = fieldJson.value
		String control = fieldJson.field.control
		String fieldName = fieldJson.field.name
		String errorMsg = null

		Map results = [originalValue: originalValue, newValue: newValue]

		// Determine if the field is required
		if (fieldJson.field.constraints?.required == 1) {
			errorMsg = checkRequiredFieldIsNotEmpty(newValue, fieldName, params)
		}

		// If there was an error, store the message.
		if (errorMsg) {
			results.errorMsg = errorMsg
		} else {
			// If no errors, continue according to the field's control.
			switch (control) {
				case "Number":
					parseNumberField(originalValue, newValue, fieldName, results, params)
					break
				case "Options.Environment":
					parseEnvironmentField(originalValue, newValue, fieldName, results, params)
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
	 * @param params
	 * @return
	 */
	private static String checkRequiredFieldIsNotEmpty(String value, String fieldName, Map params) {
		String errorMsg = null
		if (StringUtil.isBlank(value)) {
			errorMsg = "Field ${fieldName} cannot be null for Asset No. ${params.assetIdx} in class ${params.assetClass}."
		}
		return errorMsg
	}

	/**
	 * Analyse whether a number field was given valid values (old and new).
	 * @param originalValue
	 * @param newValue
	 * @param fieldName
	 * @param results
	 * @param params
	 */
	private static void parseNumberField(String originalValue, String newValue, String fieldName, Map results, Map params) {
		String errorMsg = null
		Long transformedOriginalValue = NumberUtil.toLong(originalValue)
		Long transformedNewValue = NumberUtil.toLong(newValue)

		// Check if the new value was given but it couldn't be parsed to long.
		if (!StringUtil.isBlank(newValue) && transformedNewValue == null) {
			errorMsg = "Value ${newValue} for field ${fieldName} is not a number. Asset No. ${params.assetIdx} in class ${params.assetClass}."
			// Check if the original value was given but it's not a number
		} else if (!StringUtil.isBlank(originalValue) && transformedOriginalValue == null) {
			errorMsg = "Value ${originalValue} for field ${fieldName} is not a number. Asset No. ${params.assetIdx} in class ${params.assetClass}."
		}

		results.errorMsg = errorMsg
	}

	/**
	 * Determine if the field contain valid Environment values.
	 * @param originalValue
	 * @param newValue
	 * @param fieldName
	 * @param results
	 * @param params
	 */
	private
	static void parseEnvironmentField(String originalValue, String newValue, String fieldName, Map results, Map params) {
		String errorMsg = null
		Environment transformedOriginalValue = Environment.forId(originalValue)
		Environment transformedNewValue = Environment.forId(newValue)

		// Check if the new value is a valid Environment
		if (!transformedNewValue) {
			errorMsg = "Value ${newValue} for field ${fieldName} is not a valid Environment. Asset No. ${params.assetIdx} in class ${params.assetClass}."
		} else if (!transformedOriginalValue) {
			errorMsg = "Value ${originalValue} for field ${fieldName} is not a valid Environment. Asset No. ${params.assetIdx} in class ${params.assetClass}."
		}

		results.errorMsg = errorMsg
	}

	/**
	 * Return the id for the current asset. This value is kept in
	 * field -> find -> results[0]
	 * If there are more than one result, an error will be reported.
	 *
	 * @param assetJson
	 * @return
	 */
	static Long resolveAssetId(JSONObject assetJson) {
		// TODO : JPM 2/2018 : resolveAssetId doesn't appear to be implemented correctly
		Map idField = assetJson.fields[ID_FIELD]
		Long idValue = null
		if (idField.containsKey('find')) {
			List results = idField.find.results
			int numIds = results.size()
			if (numIds == 1) {
				idValue = NumberUtil.toPositiveLong(results[0])
			} else if (numIds > 1) {
				// TODO : JPM 2/2018 : what is the point of recording an error when it isn't returned
				idField.error = 'Duplicate asset references'
			}
		}

		return idValue
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
	static final genericReferenceValueResolver = { JSONObject fieldJson, String createField ->
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
	/*	shutdownBy  : byFieldValueResolver,
		startedBy   : byFieldValueResolver,
		testingBy   : byFieldValueResolver,*/
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
	static resolveFieldValue(String fieldName, JSONObject fieldJson) {
		Closure valueResolver = fieldValueResolver[fieldName]
		return valueResolver(fieldJson)
	}
}