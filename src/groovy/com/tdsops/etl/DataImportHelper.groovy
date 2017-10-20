package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.agent.Environment

/**
 * This class provides a series of method for the
 * DataImportService.
 */
class DataImportHelper {

    static final String NAME_FIELD = "Name"
    static final String ID_FIELD = "id"

    /**
     * Validate the Name field is present and a value is provided.
     */
    static final validateNameField = { asset, params ->
        String errorMsg = null
        // Retrieve the JSON for the Name field.
        def field = findFieldInAssetJson(asset, NAME_FIELD)
        // Check if the field is null or no value is given.
        if (!field || !field.value){
            errorMsg = "Missing required field 'name' for Asset No. ${params.assetIdx} in class ${params.assetClass}."
        }
        return errorMsg
    }

    /**
     *  Validate the Id field is present, is a positive long, it's not being modified
     *  and it's part of this project.
     */
    static final validateIdField = {asset, params ->
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
            // The 'id' field is required, report the error if it couldn't been found.
            errorMsg = "Missing required field 'id' for Asset No. ${params.assetIdx} in class ${params.assetClass}."
        }

        return errorMsg
    }

    /**
     * Map that contains the validators for all the fields that need
     * to be checked before importing.
     */
    private static final Map assetFieldsValidator = [
            name: validateNameField,
            id: validateIdField
    ]

    /**
     *  Check if the json for this asset is valid in terms of the content. Validate
     *  the id is present (and wasn't changed) and also the asset name.
     *
     *  If an error is detected, an exception will be thrown.
     *
     * @param asset
     * @param params - additional parameters required for logging
     */
    static String validateAsset(asset, Map params) throws RuntimeException{
        // Validate fields until the first error is detected.
        for (fieldValidator in assetFieldsValidator) {
            String errorMsg = fieldValidator.value(asset, params)
            // If an error is detected, report it back.
            if (errorMsg) {
                return errorMsg
            }
        }

        return null

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
                    parseNumberField(originalValue, newValue, fieldName, results, params )
                    break
                case "Options.Environment":
                    parseEnvironmentField(originalValue, newValue, fieldName, results, params )
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
    private static void parseEnvironmentField(String originalValue, String newValue, String fieldName, Map results, Map params) {
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
     * Find a given field in the asset json
     * @param asset
     * @param fieldName
     * @return
     */
    static findFieldInAssetJson(asset, fieldName) {
        return asset.elements.find {it.field?.name == fieldName}
    }
}
