package net.transitionmanager.common

import com.tdsops.tm.enums.ControlType
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.FieldSpecsCacheService
import net.transitionmanager.exception.ConfigurationException
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.ObjectUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class CustomDomainService implements ServiceMethods {
    static final String ALL_ASSET_CLASSES = 'ASSETS'
	// Common Domian name (used to gather common fields in Domains)
    static final String COMMON = 'COMMON'
    static final String CUSTOM_FIELD_NAME_PART = 'custom'

    static final int CUSTOM_USER_FIELD = 1
    static final int STANDARD_FIELD = 0
    static final int ALL_FIELDS = 2

    static final List<String> CUSTOM_REQUIRED_BULK_ACTIONS = ["replace"]
    static final List<String> CUSTOM_NON_REQUIRED_BULK_ACTIONS = ["replace", "clear"]

    static final List<String> COMPUTED_FIELD_NAMES = ['dependencyGroup', 'conflict', 'tbd']

    NamedParameterJdbcTemplate namedParameterJdbcTemplate

    SettingService settingService
    /**
     * Cache for field specs
     */
    FieldSpecsCacheService fieldSpecsCacheService
    /**
     * This method retrieves the specs for the standard fields.
     * It's added for consistency with return format of customFieldSpecs
     * and allFieldSpecs.
     *
     * @param project
     * @param domain
     * @param showOnly
     * @return
     */
    Map standardFieldSpecs(Project project, String domain, boolean showOnly = false) {
        return getFilteredFieldSpecs(project, domain, STANDARD_FIELD, showOnly)
    }


    /**
     * Retrieve custom field specs
     * @param domain
     * @param showOnly : flag to request only those visible fields.
     * @return
     */
    Map customFieldSpecs(Project project, String domain, boolean showOnly = false) {
        return getFilteredFieldSpecs(project, domain, CUSTOM_USER_FIELD, showOnly)
    }

    /**
     * Used to retrieve a list of all of the properties for a given domain name
     * @param project - the project to filter on
     * @param domain - the name of the domain to fetch
     * @param showOnly - flag when true will only return the names that are to be displayed
     * @return A map of the field names with the value true
     */
    Map fieldNamesAsMap(Project project, String domain, showOnly=false) {
        List<String> assetClassTypes = resolveAssetClassTypes(domain)
        Map fields = [:]

        for (String assetClassType in assetClassTypes) {
            Map fieldSpecMap = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType)
            if (fieldSpecMap) {
                for (fieldSpec in fieldSpecMap.fields) {
                    if (showOnly && ! fieldSpec.show) {
                        continue
                    }
                    fields.put(fieldSpec.field, true)
                }
            } else {
                // If the configuration is missing then this is a serious issue for the application and should bail out
                throw new ConfigurationException("No Field Specification found for project ${project.id} and asset class ${assetClassType}")
            }
        }
        return fields
    }

    /**
     * Used to return the list of the list of the custom field names
     * @param domain - the domain to retrieve the field specs from
     * @param showOnly - a flag when true only returns the fields to be shown otherwise returns all (default false)
     * @return A list containing the Map of each custom field for the domain
     */
    List<Map> customFieldsList(Project project, String domainName, boolean showOnly = false) {
        Map customFieldsSpec = customFieldSpecs(project, domainName, showOnly)

        // Get all the fields in the set
        List<Map> fields = customFieldsSpec[domainName.toUpperCase()]?.fields

		fields = ObjectUtils.defaultIfNull(fields, [])

        return fields
    }

    /**
     * Used to find a single field from the custom field of the field specifications by filtering with closure parameter
     * @param domain - the domain to find the field in
     * @param findClosure - the closure that will perform the filtering
     * @return the field specification map of the field if found otherwise null
     */
    Map findCustomField(Project project, String domain, Closure findClosure) {
        List list = customFieldsList(project, domain)
        return (list ? list.find(findClosure) : null)
    }

    /**
     * Retrieve standard field specs as map, overloading the method
     * that receives the domain instead of the AssetClass for the asset.
     * @param project
     * @param AssetClass for the asset
     * @return
     */
    Map standardFieldSpecsByField(Project project, AssetClass assetClass) {
        return standardFieldSpecsByField(project, assetClass.toString())
    }

    /**
     * Retrieve standard field specs as map
     * @param project
     * @param domain
     * @return
     */
    Map standardFieldSpecsByField(Project project, String domain) {
        Map fieldSpecs = getFilteredFieldSpecs(project, domain, STANDARD_FIELD)
        Map domainFieldSpecs = createFieldSpecsViewMap(fieldSpecs, domain)
        return domainFieldSpecs
    }

    /**
     * Retrieve all field specifications as a Map
     * @param project
     * @param domain
     * @param showOnly - boolean to ask only for the fields required for export/import and other views.
     * @return
     */
    Map allFieldSpecs(Project project, String domain, boolean showOnly = false){

        Map fieldSpec = fieldSpecsCacheService.getAllFieldSpecs(project, domain)
        if (fieldSpec) {
            return fieldSpec
        } else {
            fieldSpec = [:]
        }

        List<String> assetClassTypes = resolveAssetClassTypes(domain)

        for (String assetClassType : assetClassTypes) {
            Map fieldSpecMap = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType)
            if (fieldSpecMap) {
                if (showOnly) {
                    fieldSpecMap.fields = fieldSpecMap.fields.findAll( {field -> field.show == 1} )
                }
                fieldSpec["${assetClassType.toUpperCase()}"] = fieldSpecMap
            } else {
                // If the configuration is missing then this is a serious issue for the application and should bail out
                throw new ConfigurationException("No Field Specification found for project ${project.id} and asset class ${assetClassType}")
            }
        }

        fieldSpecsCacheService.setAllFieldSpecs(project, domain, fieldSpec)
        return fieldSpec
    }

    /**
     * Return a map with the field specs for the asset export. This include all the standard fields marked
     * to be displayed (and special cases such as lastUpdated), plus all custom fields regardless of them
     * being marked for display. It also takes into consideration the list of headers in the export template,
     * as these fields need to be included as well.
     *
     * @param project - user's current project
     * @param domain - used to filter the fields for a particular domain.
     * @param templateHeaders
     * @return a map with the field settings.
     */
    Map getFieldSpecsForAssetExport(Project project, String domain, List<String> templateHeaders) {
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassTypes(domain)
        for (String assetClassType : assetClassTypes) {
            Map fieldSpecMap = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType)
            if (fieldSpecMap) {
                fieldSpecMap.fields = fieldSpecMap.fields.findAll( {field -> field.show == 1 || field.udf == 1 || field.label in templateHeaders})
                fieldSpec["${assetClassType.toUpperCase()}"] = fieldSpecMap
            } else {
                throw new ConfigurationException("No Field Specification found for project ${project.id} and asset class ${assetClassType}")
            }
        }

        return fieldSpec
    }

    /**
     * Save single or all custom field specs
     * @param domain
     * @param fieldSpec
     */
    void saveFieldSpecs(Project project, String domain, JSONObject fieldSpec) {
        List<String> assetClassTypes = resolveAssetClassTypes(domain)

        Map currentStoredFieldSpecs = [:]


        try{
            currentStoredFieldSpecs = allFieldSpecs(project, domain)
        } catch(ConfigurationException silentFail) {}


        for (String assetClassType : assetClassTypes) {
            JSONObject customFieldSpec = fieldSpec[assetClassType]

            // This list will contain all the portions of SQL code for updating the fields that are being added/updated.
            List<String> updateFieldStrings = []

            if (customFieldSpec) {
                Integer customFieldSpecVersion = customFieldSpec[SettingService.VERSION_KEY] as Integer
                List<String> customFieldsToSave = []
                List<Map> fields = (currentStoredFieldSpecs[assetClassType]?.fields) ?: []
                List<String> currentCustomFields = (fields*.field).findAll { it.startsWith('custom') }

                Map fieldsLookUp = fields.collectEntries{ Map field ->
                    [(field.field): field]
                }

                for (JSONObject field : customFieldSpec.fields) {

                    if ( field.control == ControlType.LIST.value() && ! field.constraints?.values ) {
                        List<String> values = distinctValues(project, assetClassType, [fieldSpec: field])

                        if(isYesNoToList(fieldsLookUp[(String)field.field],field)){
                            values = ['Yes', 'No']
                        }

                        if (values) {

                            if (!field.constraints) {
                                field.constraints = [:]
                            }

                            field.constraints.values = values
                        }
                    }

                    if (((String) field.field).startsWith('custom')) {

                        // Prevent inconsistencies when saving the field by accidentally leaving the 'shared' property empty.
                        if (field.shared == null) {
                            field.shared = 0
                        }

                        String updateFieldString = updateFieldData(fieldsLookUp[(String)field.field],field)
                        // If a change in the specs for this field is detected, add the SQL code to the list.
                        if (updateFieldString) {
                            updateFieldStrings << updateFieldString
                        }
                        customFieldsToSave << field.field

                        if(field?.constraints?.required){
                            field.bulkChangeActions = CUSTOM_REQUIRED_BULK_ACTIONS as JSONArray
                        }else {
                            field.bulkChangeActions = CUSTOM_NON_REQUIRED_BULK_ACTIONS as JSONArray
                        }
                    }
                }

                // Determine the fields that were added.
                List<String> customFieldsToClear = CollectionUtils.disjunction( currentCustomFields, customFieldsToSave)

                if (customFieldsToClear) {
                    // Set all the new fields to null, to override possible all data.
                    List<String> customFieldsToClearSql = setAllToNull(customFieldsToClear)

                    // Add all the new fields to the list of fields to be updated.
                    updateFieldStrings.addAll(customFieldsToClearSql)
                }

                // Execute a single SQL update statement for clear all the necessary custom fields for this asset class.
                clearCustomFieldsForClass(project, assetClassType, updateFieldStrings)


                settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType, customFieldSpec.toString(), customFieldSpecVersion)
            } else {
                throw new InvalidParamException("Custom field specification not provided for class ${assetClassType}")
            }
        }

        fieldSpecsCacheService.removeFieldSpecs(project, domain)
    }

    /**
     * Test the control values to see if we are switch from a yes/no to a list.
     *
     * @param oldFieldSpec The old specification used for the field name, and to determine what the old control was.
     * @param newFieldSpec The new Specification, what the field was updated to, used to determine what the new control is.
     *
     * @return True is the fields was a yes/no and it now being changed to a list.
     */
    boolean isYesNoToList(Map oldFieldSpec, Map newFieldSpec) {
        oldFieldSpec && oldFieldSpec.control == ControlType.YES_NO.value && newFieldSpec.control == ControlType.LIST.value
    }

    /**
     * Updates data stored in custom fields based on how the control has changed, from an old spec to a new one.
     *
     * @param oldFieldSpec The old specification used for the field name, and to determine what the old control was.
     * @param newFieldSpec The new Specification, what the field was updated to, used to determine what the new control is.
     * @return the string for updating the given field.
     */
    String updateFieldData(Map oldFieldSpec, Map newFieldSpec){
        if(!oldFieldSpec || (oldFieldSpec.control == newFieldSpec.control && oldFieldSpec.shared == newFieldSpec.shared)||
           (oldFieldSpec.control == newFieldSpec.control && oldFieldSpec.shared && !newFieldSpec.shared)){
            return
        }

        String key = newFieldSpec.shared ? '': "${oldFieldSpec.control}-${newFieldSpec.control}"

        String updateFieldString = null

        switch (key) {
            case "$ControlType.DATE.value-$ControlType.DATETIME.value":
                updateFieldString = dataDateToDateTime(oldFieldSpec.field)
                break

            case "$ControlType.DATETIME.value-$ControlType.DATE.value":
                updateFieldString = dataDateTimeToDate(oldFieldSpec.field)
                break

            case "$ControlType.STRING.value-$ControlType.YES_NO.value":
            case "$ControlType.LIST.value-$ControlType.YES_NO.value":
                updateFieldString = dataToYesNo(oldFieldSpec.field)
                break

            case "$ControlType.NUMBER.value-$ControlType.LIST.value":
            case "$ControlType.STRING.value-$ControlType.LIST.value":
            case "$ControlType.YES_NO.value-$ControlType.LIST.value":
            case "$ControlType.DATETIME.value-$ControlType.LIST.value":
            case "$ControlType.DATE.value-$ControlType.LIST.value":
                //The list conversion doesn't require any data conversion
                break

            case "$ControlType.NUMBER.value-$ControlType.STRING.value":
            case "$ControlType.YES_NO.value-$ControlType.STRING.value":
            case "$ControlType.DATE.value-$ControlType.STRING.value":
            case "$ControlType.DATETIME.value-$ControlType.STRING.value":
            case "$ControlType.LIST.value-$ControlType.STRING.value":
                updateFieldString = dataToString(oldFieldSpec.field, newFieldSpec?.constraints?.maxSize ?: 255)
                return

            default:
                updateFieldString = anyTypeToNull(oldFieldSpec.field)
        }

        return updateFieldString


    }

    /**
     * Retrieve a list of distinct values found for the specified domain and field spec
     * @param project - the project to look up asset distinct values on
     * @param domain - the class name of the domain
     * @param fieldSpec - the field specification as Map/Json data
     * @param failOnFirst - flag fail on the first found not in spec (NOT used...)
     * @return a list of the distinct values
     * TODO : distinctValues - this method is Asset specific but the method name give no indication of that
     * TODO : distinctValues - should be changed to require project as param
     */
    List<String> distinctValues(Project project, String domain, Map fieldSpec, Boolean failOnFirst=false) {
        AssetClass assetClass = resolveAssetClassType(domain)
        String fieldName = fieldSpec.fieldSpec?.field
        boolean shared = fieldSpec.fieldSpec?.shared

        // TM-6617 JPM Commented out the validation because we want to be able to use distinctValues on standard
        // fields for testing in particular and  this was breaking it. This does mean that we run the risk of
        // passing a bad fieldName. See ticket TM-6763 regarding correcting this situation.

        //validateCustomFieldName(fieldName)

        return getDistinctAssetCustomFieldValues(project, fieldName, shared, assetClass)
    }

    /**
     * Retrieve distinct asset entity "custom(n)" field values for all or a specific asset class
     * @param project - the project to look for asset values
     * @param fieldName - the custom name field (e.g. custom12)
     * @param shared - flag if the property was shared. If true the distinct values will be from all asset classes.
     * @param assetClass - the enum specifying the asset class to search on
     * @return A list of distinct values that are case-sensitive sorted by the values case insensitive ascending
     */
    List<String> getDistinctAssetCustomFieldValues(Project project, String fieldName, boolean shared, AssetClass assetClass) {
        assert project

        return AssetEntity.where {
            project == project
            isNotNull(fieldName)
            ne(fieldName, '')

            if (!shared) {
                assetClass == assetClass
            }

            projections {
                distinct(fieldName)
            }

        }.list([sort: fieldName, order: 'asc'])
    }

    /**
     * Use to retrieve the Field specs from the Setting domain for one or more domain classes
     * @param project - the project to get the field specifications for
     * @param domain - the domain name to fetch the specs for. If the value is Asset then it returns all asset domains otherwise just the one.
     * @param udf - when 0, return standard fields or when 1 returns the custom fields
     * @param showOnly - when true only returns fields specified to be shown in views
     * @return a Map containing the one or more field specification maps. The keys to the maps are the domain names in uppercase.
     * @throws ConfigurationException when the domain is missing the Field Settings Spec
     * TODO : getFilteredFieldSpecs : Method is asset specific but name give no indication
     */
    private Map getFilteredFieldSpecs(Project project, String domain, int udf, boolean showOnly = false) {
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassTypes(domain)

        for (String assetClass : assetClassTypes) {
            def fieldSpecMap = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, domain.toUpperCase())

            // If no fieldSpec Map was found then this is a serious issue and need to bomb out
            if (! fieldSpecMap) {
                throw new ConfigurationException("No Field Specification found for project id ${project.id} and asset class ${assetClass}")
            }

			fieldSpec["${assetClass.toUpperCase()}"] = fieldSpecMap

			Closure filterClosure
			if (showOnly) {
				filterClosure = { field -> field.udf == udf && field.show == 1 }
			} else {
				filterClosure = { field -> field.udf == udf }
			}

			fieldSpec["${assetClass.toUpperCase()}"]["fields"] = fieldSpecMap["fields"].findAll( filterClosure )

        }

        return fieldSpec
    }

    /**
     * Giving the fields specs map for certain domain/asset class,
     * it returns the list of fields as a map using the field name as the map key
     * @param fieldSpecs
     * @param domain
     * @return
     */
    private Map createFieldSpecsViewMap(Map fieldSpecs, String domain) {
        List domainFields = fieldSpecs[domain.toUpperCase()]["fields"]
        Map fieldSpecsMap = [:]
        if (!domainFields) {
            return fieldSpecsMap
        }

        fieldSpecsMap = domainFields.collectEntries { [(it["field"]) : it] }
        return fieldSpecsMap
    }

    /**
     * Resolve which AssetClass field specs to return or fail
     * @param domain
     * @return
     */
    private List<String> resolveAssetClassTypes(String domain) {
        if (domain.toUpperCase() == ALL_ASSET_CLASSES) {
            return AssetClass.values().collect({ac -> ac.toString().toUpperCase()})
        } else {
            if (AssetClass.safeValueOf(domain.toUpperCase())) {
                return [domain.toUpperCase()]
            }
            throw new InvalidParamException("Invalid AssetClass name: ${domain}")
        }
    }

    /**
     * Resolve AssetClass from domain or fail
     * @param domain
     * @return
     */
    private AssetClass resolveAssetClassType(String domain) {
        AssetClass assetClass = AssetClass.safeValueOf(domain.toUpperCase())
        if (assetClass) {
            return assetClass
        }
        throw new InvalidParamException("Invalid AssetClass name: ${domain}")
    }

    /**
     * Validate if custom field name is within custom field counts
     * @param fieldName
     * @throws net.transitionmanager.exception.InvalidRequestException if the fieldName is invalid
     */
    private void validateCustomFieldName(String fieldName) {
        if (! StringUtil.isBlank(fieldName) && fieldName.startsWith(CUSTOM_FIELD_NAME_PART)) {
            String numberStr = fieldName.substring(CUSTOM_FIELD_NAME_PART.size())

            Integer number = NumberUtil.toInteger(numberStr, -1)
            if (number > 0 && number <= Project.CUSTOM_FIELD_COUNT) {
                return
            }
        }
        // If we got here then there was an issue...
        reportFieldNameViolation(fieldName)
    }

    /**
     * Report security violation and throw an exception
     * @param fieldName
     */
    private void reportFieldNameViolation(String fieldName) {
        securityService.reportViolation("Attempted to access distinct field values for undefined field name: [${fieldName}]", securityService.currentUsername)
        throw new InvalidRequestException("Invalid field name (${fieldName}) specified")
    }


    /**
     * This method retrieves the field specs given a domain and an option of which
     * fields are needed (standard, custom or all).
     * It also allows to filter only a particular subset of the keys, for cases
     * where the whole specs aren't required.
     *
     * @param project - project instance
     * @param domain - asset domain
     * @param option - which fields are needed.
     *             ALL_FIELDS
     *             STANDARD_FIELD
     *             CUSTOM_USER_FIELD
     * @param values - a list of string with the name of the fields to be included in the result.
     *
     * @return a list with the specs.
     */
    List fieldSpecs(Project project, String domain, int option = ALL_FIELDS, List values = null) {
        // This list will contain the resulting specs.
        List fieldSpecs = []
        // Checks that an actual domain is given.
        if (domain) {
            // Retrieves the specs for all available fields for the domain.
            Map fields = null

            switch (option) {
                case ALL_FIELDS:
                    fields = allFieldSpecs(project, domain)
                    break
                case STANDARD_FIELD:
                    fields = standardFieldSpecs(project, domain)
                    break
                case CUSTOM_USER_FIELD:
                    fields = customFieldSpecs(project, domain)
                    break

            }

            domain = domain.toUpperCase()

            // Validates that field specs were found and with the right format.
            if (fields && fields.containsKey(domain)) {
                // Strips the actual list of specs
                fieldSpecs = fields[domain].fields
                // Checks if we need to filter particular values.
                if (values && values.size()) {
                    // Creates a list with only the requested fields for each field.
                    fieldSpecs = fieldSpecs.collect { spec ->
                        spec.subMap(values).findAll {
                            it.value
                        }
                    }
                }
            }
        }
        return fieldSpecs
    }

    /**
     * Create a COMMON domain from the common fields (belonging to AssetEntity.COMMON_FIELD_LIST or *shared*)
     * in all the domains returned by allFieldsSpecs.
     * It returns that content in a JSON string format.
     *
     * @param project
     * @return a String json content with field specs retrieve from database
     * @see CustomDomainService#allFieldSpecs(net.transitionmanager.project.Project, java.lang.String, boolean)
     */
    String jsonFieldSpecsWithCommon(Project project = null){

        String jsonFieldSpec = fieldSpecsCacheService.getJsonFieldSpecs(project)
        if (jsonFieldSpec) {
            return jsonFieldSpec
        } else {
            jsonFieldSpec = ''
        }
        Map fieldSpec = fieldSpecsWithCommon(project)
        jsonFieldSpec = (fieldSpec as JSON).toString()
        fieldSpecsCacheService.setJsonFieldSpecs(project, jsonFieldSpec)

        return jsonFieldSpec
    }
	/**
	 * Create a COMMON domain from the common fields (belonging to AssetEntity.COMMON_FIELD_LIST or *shared*)
	 * in all the domains returned by allFieldsSpecs
	 * Jira: TM-6838
	 * @param project
	 * @return
	 */
    Map<String, ?>  fieldSpecsWithCommon(Project project = null){
        assert project
        String APPLICATION = AssetClass.APPLICATION as String

		// Get all Domain (fields) Specs
        Map fieldSpecs = allFieldSpecs(project, ALL_ASSET_CLASSES)

        Map applicationFields = fieldSpecs[APPLICATION]

        List fields = applicationFields.fields ?: []

        // Split the common fields from the individual ones
        def (commonFields, individualFields) = fields.split {
            AssetEntity.COMMON_FIELD_LIST.contains(it.field) ||
                    (it.shared && BooleanUtils.toBoolean(it.shared))
        }

        def commonFieldNames = commonFields.collect { it.field }

        // Change the original fields to the individual ones
        applicationFields.fields = individualFields

        //Remove all common fields from the rest of the Domains
        fieldSpecs.each { k, v ->
            if ( k != APPLICATION ){
                def filteredFields = v.fields.findAll {
                    ! commonFieldNames.contains( it.field )
                }
                v.fields = filteredFields
            }
        }

        // we add the common fields
        fieldSpecs[COMMON] = [
            domain : COMMON.toLowerCase(),
            fields : commonFields
        ]

        return fieldSpecs
    }

    /**
     * Provides a mapping of field names to their service/actions.
     *
     * @param currentProject the current project to use to get fieldToBulkChangeMapping.
     *
     * @return a map of field names to their service/actions.
     */
    Map<String, Map> fieldToBulkChangeMapping(Project currentProject) {
        Map<String, Map> types = [:]
        Map<String, Map> fields = [:]

        allFieldSpecs(currentProject, ALL_ASSET_CLASSES).each { key, value ->
            value.fields.each { Map<String, String> field ->
                fields[field.field] = [
                    control          : field.control,
                    bulkChangeActions: field.bulkChangeActions,
                    customValues     : field?.constraints?.values ?: [],
                    constraints      : field.constraints,
                    label            : field.label
                ]
            }

            types[key]= fields
            fields = [:]
        }

        return types
    }

    /**
     * It creates an instance of {@code FieldSpecProject} with all the field Spec
     * associated to a particular {@code Project} instance
     * @param project an instance of {@code Project}
     * @return and instance {@code FieldSpecProject}
     * @see net.transitionmanager.dataview.FieldSpecProject#addFieldSpecs(java.lang.String, java.util.Map)
     * @see CustomDomainService#fieldSpecsWithCommon(Project)
     */
    FieldSpecProject createFieldSpecProject(Project project){
        return new FieldSpecProject(this.fieldSpecsWithCommon(project))
    }

    /**
     * Based on Field Spec definitions it assigns all the default values
     * for every custom field in an entity object.
     * @param fieldSpecs an instance of {@code FieldSpecProject}
     * @param domainClass a Class in AssetEntity hierarchy
     * @param entity an instance of an AssetEntity hierarchy
     * @return entity instance with default values assigned
     */
    Object setCustomFieldsDefaultValue(FieldSpecProject fieldSpecProject, Class domainClass, Object entity) {
        Map<String, FieldSpec> fieldSpecMap = fieldSpecProject.getAllCustomFields(domainClass.getSimpleName())
        fieldSpecMap.each { String fieldName, FieldSpec fieldSpec ->
            entity[fieldName] = fieldSpec.defaultValue
        }

        return entity
    }

    /**
     * Used to clear all (NULL) custom asset values
     * This function first filters all fields given for those that are CustomFields
     * @param project
     * @param fieldSpec
     * @return
     */
    Map <String, JSONArray> clearFieldSpecsData(Project project, Map fieldSpec) {
        Map <String, JSONArray> clearedFields = [:]
        FieldSpecProject fieldSpecProject

        fieldSpec.each { assetClassName, fieldNames ->
            if (fieldNames) {
                if (! fieldSpecProject) {
                    fieldSpecProject = createFieldSpecProject(project)
                }

                fieldNames = fieldNames.findAll {
                    fieldSpecProject.isCustomField(assetClassName, it)
                }

                if (fieldNames) {
                    clearCustomFields(project, assetClassName, fieldNames)
                    clearedFields[assetClassName] = fieldNames
                }
            }
        }

        return clearedFields
    }

    /**
     * Clears data stored in custom fields
     *
     * @param project The project to clear the data for.
     * @param assetClassName The assetClass to clear the data for.
     * @param fieldNames The fieldNames/columns to clear the data for.
     */
    @Transactional
    Integer clearCustomFields(Project project, String assetClassName, List<String> fieldNames) {
        if (fieldNames) {
            AssetClass assetClass = AssetClass.safeValueOf(assetClassName)
            String sql = 'update AssetEntity set '

            sql += fieldNames.collect {
                "$it = NULL"
            }.join(', ')

            sql += ' where project=:project and assetClass=:assetClass'

            AssetEntity.executeUpdate(
                    sql,
                    [project: project, assetClass: assetClass]
            )
        }
    }

    /**
     * Update the assets for the project and asset class using the list of strings to set each individual field.
     * @param project - user's current project.
     * @param assetClassName - the asset class.
     * @param updateFieldStrings - the list of sql codes to update the fields.
     * @return the number of assets that were updated (-1 if the list is null or empty).
     */
    @Transactional
    Long clearCustomFieldsForClass(Project project, String assetClassName, List<String> updateFieldStrings) {
        Long recordsUpdated = -1
        String assetClass = AssetClass.safeValueOf(assetClassName).name()
        if (updateFieldStrings) {
            String updateString = updateFieldStrings.join(", ")
            String sql = "UPDATE asset_entity SET $updateString WHERE project_id=:project AND asset_class=:assetClass"

            recordsUpdated = namedParameterJdbcTemplate.update(sql,[project: project.id, assetClass: assetClass])
        }
        return recordsUpdated
    }

    /**
     * For each field construct the portion of SQL statement to set that field to null.
     * @param fieldNames - a list of field names.
     * @return a list with strings to set the given fields to null.
     */
    List<String> setAllToNull(List<String> fieldNames) {
        List<String> updateFieldStrings = []
        for (String fieldName in fieldNames) {
            updateFieldStrings << anyTypeToNull(fieldName)
        }
        return updateFieldStrings
    }

    /**
     * Statement for setting the given field to null.
     *
     * @param fieldName the field to be updated.
     * @return The part of a SQL statement needed to appropriately update the given field.
     */
    String anyTypeToNull(String fieldName) {
        return "$fieldName = NULL"
    }

    /**
     * Converts data stored in a custom field from a date to a date time, adding the time 00:00:00.
     *
     * @param fieldName The fieldName/column to update the data for.
     * @return The part of a SQL statement needed to appropriately update the given field.
     */
    String dataDateToDateTime(String fieldName) {
        String timeOffset = TimeUtil.timeZoneOffset
        return "$fieldName = DATE_FORMAT( CONVERT_TZ( $fieldName, '$timeOffset', '+00:00') , '%Y-%m-%dT%H:%i:%sZ')"
    }

    /**
     * Converts data stored in a custom field from a date time to a date, striping off the time.
     *
     * @param fieldName The fieldName/column to update the data for.
     * @return The part of a SQL statement needed to appropriately update the given field.
     */
    String dataDateTimeToDate(String fieldName) {
        return "$fieldName = DATE(STR_TO_DATE($fieldName , '%Y-%m-%dT%TZ'))"
    }

    /**
     * Converts data stored in a custom field to yes/no data.
     *
     * @param project The project to update the data for.
     * @param assetClassName The assetClass to update the data for.
     * @param fieldName The fieldName/column to update the data for.
     * @return The part of a SQL statement needed to appropriately update the given field.
     */
    String dataToYesNo(String fieldName) {
        return """$fieldName = case when (lower($fieldName) = 'yes') then 'Yes'
                                    when (lower($fieldName) = 'no') then 'No'
                                    else NULL
                               end
               """
    }

    /**
     * Converts data stored in a custom field to a string, truncating to the maxLength size for the new field.
     *
     * @param fieldName The fieldName/column to update the data for.
     * @param maxLength The max length to truncate the string to.
     * @return The part of a SQL statement needed to appropriately update the given field.
     */
    String dataToString(String fieldName, Integer maxLength) {
        return "$fieldName = substring($fieldName,1, $maxLength)"
    }
}
