package net.transitionmanager.service

import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.Project
import org.apache.commons.lang3.ObjectUtils
import org.codehaus.groovy.grails.web.json.JSONObject

class CustomDomainService implements ServiceMethods {
    public static final String ALL_ASSET_CLASSES = 'ASSETS'
    public static final String CUSTOM_FIELD_NAME_PART = 'custom'

    public static final int CUSTOM_USER_FIELD = 1
    public static final int STANDARD_FIELD = 0
    public static final int ALL_FIELDS = 2

    SecurityService securityService
    SettingService settingService
    def jdbcTemplate

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
     * @param domain
     * @return
     */
    Map allFieldSpecs(Project project, String domain){
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassTypes(domain)

        for (String assetClassType : assetClassTypes) {
            Map fieldSpecMap = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType)
            if (fieldSpecMap) {
                fieldSpec["${assetClassType.toUpperCase()}"] = fieldSpecMap
            } else {
                // If the configuration is missing then this is a serious issue for the application and should bail out
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
        for (String assetClassType : assetClassTypes) {
            JSONObject customFieldSpec = fieldSpec[assetClassType]
            if (customFieldSpec) {
                Integer customFieldSpecVersion = customFieldSpec[SettingService.VERSION_KEY] as Integer
                settingService.save(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType, customFieldSpec.toString(), customFieldSpecVersion)
            } else {
                throw new InvalidParamException("Custom field specification not provided for class ${assetClassType}")
            }
        }
    }

    /**
     * Retrieve a list of distinct values found for the specified domain and field spec
     * @param project - the project to look up asset distinct values on
     * @param domain - the class name of the domain
     * @param fieldSpec - the field specification as JSON data
     * @param failOnFirst - flag fail on the first found not in spec (NOT used...)
     * @return a list of the distinct values
     * TODO : distinctValues - this method is Asset specific but the method name give no indication of that
     * TODO : distinctValues - should be changed to require project as param
     */
    List<String> distinctValues(Project project, String domain, JSONObject fieldSpec, Boolean failOnFirst=false) {
        AssetClass assetClass = resolveAssetClassType(domain)
        String fieldName = fieldSpec.fieldSpec?.field
        boolean shared = fieldSpec.fieldSpec?.shared

        validateCustomFieldName(fieldName)

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
        StringBuilder query = new StringBuilder("SELECT * FROM (SELECT DISTINCT ${fieldName} COLLATE latin1_bin AS ${fieldName} ")
        query.append("FROM asset_entity WHERE ${fieldName} IS NOT NULL AND project_id = ? ")

        // If shared then it won't filter on the individual asset class in order to get all distinct values
        if (!shared) {
            query.append(" AND asset_class = ? ")
        }

        // Set the sort order to be case sensitive
        query.append(") tmp ORDER BY ${fieldName} COLLATE latin1_general_ci ASC")

        List<String> result = []
        List<Map<String, Object>> values = null
        if (shared) {
            values = jdbcTemplate.queryForList(query.toString(), project.id)
        } else {
            values = jdbcTemplate.queryForList(query.toString(), project.id, assetClass.toString())
        }
        for (Map<String, Object> value : values) {
            result.add(value[fieldName])
        }
        return result
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
     * @throws InvalidRequestException if the fieldName is invalid
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
     *             @see ALL_FIELDS
     *             @see STANDARD_FIELD
     *             @see CUSTOM_USER_FIELD
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
                    fields = allFieldSpecs(domain)
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

}