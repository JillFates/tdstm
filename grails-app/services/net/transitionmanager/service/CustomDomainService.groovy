package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.Project
import org.codehaus.groovy.grails.web.json.JSONObject

class CustomDomainService implements ServiceMethods {
    public static final String ALL_ASSET_CLASSES = "ASSETS"
    public static final String CUSTOM_FIELD_NAME_PART = "custom"

    SecurityService securityService
    SettingService settingService
    AssetEntityService assetEntityService

    /**
     * Retrieve custom field specs
     * @param domain
     * @param showOnly : flag to request only those visible fields.
     * @return
     */
    Map customFieldSpecs(String domain, boolean showOnly = false) {
        Project currentProject = securityService.loadUserCurrentProject()
        return getFilteredFieldSpecs(currentProject, domain, 1, showOnly)
    }

    /**
     * Retrieve standard field specs as map
     * @param domain
     * @return
     */
    Map standardFieldSpecsByField(String domain) {
        Project currentProject = securityService.loadUserCurrentProject()
        Map fieldSpecs = getFilteredFieldSpecs(currentProject, domain, 0)
        Map domainFieldSpecs = createFieldSpecsViewMap(fieldSpecs, domain)
        return domainFieldSpecs
    }

    /**
     * Retrieve standard field specs as map
     * @param project
     * @param domain
     * @return
     */
    Map standardFieldSpecsByField(Project project, String domain) {
        Map fieldSpecs = getFilteredFieldSpecs(project, domain, 0)
        Map domainFieldSpecs = createFieldSpecsViewMap(fieldSpecs, domain)
        return domainFieldSpecs
    }

    /**
     * Retrieve all custom field specs
     * @param domain
     * @return
     */
    Map allFieldSpecs(String domain){
        Project currentProject = securityService.loadUserCurrentProject()
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassTypes(domain)

        for (String assetClassType : assetClassTypes) {
            def fieldSpecMap = settingService.getAsMap(currentProject, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType)
            if (fieldSpecMap) {
                fieldSpec["${assetClassType.toUpperCase()}"] = fieldSpecMap
            } else {
                fieldSpec["${assetClassType.toUpperCase()}"] = null
            }
        }

        return fieldSpec
    }

    /**
     * Save single or all custom field specs
     * @param domain
     * @param fieldSpec
     */
    void saveFieldSpecs(String domain, JSONObject fieldSpec) {
        Project currentProject = securityService.loadUserCurrentProject()
        List<String> assetClassTypes = resolveAssetClassTypes(domain)
        for (String assetClassType : assetClassTypes) {
            JSONObject customFieldSpec = fieldSpec[assetClassType]
            if (customFieldSpec) {
                Integer customFieldSpecVersion = customFieldSpec[SettingService.VERSION_KEY] as Integer
                settingService.save(currentProject, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, assetClassType, customFieldSpec.toString(), customFieldSpecVersion)
            } else {
                throw new InvalidParamException("Custom field specification not provided: ${assetClassType}")
            }
        }

    }

    /**
     * Retrieve a list of distinct values found for the specified domain and field spec
     * @param domain
     * @param fieldSpec
     * @param failOnFirst
     * @return
     */
    List<String> distinctValues(String domain, JSONObject fieldSpec, Boolean failOnFirst=false) {
        Project currentProject = securityService.loadUserCurrentProject()
        AssetClass assetClass = resolveAssetClassType(domain)
        JSONObject parsedJson = JsonUtil.parseJson(fieldSpec.toString())
        String fieldName = parsedJson["fieldSpec"]["field"]
        boolean shared = parsedJson["fieldSpec"]["shared"]

        validateCustomFieldName(fieldName)
        return assetEntityService.getDistinctAssetEntityCustomFieldValues(currentProject, fieldName, shared, assetClass)
    }

    /**
     * Get field specs
     * @param domain AssetClass type
     * @param udf whether to return custom or standard fields
     * @param showOnly flag to filter those fields that shown in views, etc.
     * @return
     */
    private Map getFilteredFieldSpecs(Project project, String domain, int udf, boolean showOnly = false) {
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassTypes(domain)
        for (String assetClass : assetClassTypes) {
            def fieldSpecMap = settingService.getAsMap(project, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, domain.toUpperCase())
            fieldSpec["${assetClass.toUpperCase()}"] = fieldSpecMap
            if (showOnly) {
                fieldSpec["${assetClass.toUpperCase()}"]["fields"] = fieldSpecMap["fields"].findAll({ field -> field.udf == udf && field.show == 1})
            } else {
                fieldSpec["${assetClass.toUpperCase()}"]["fields"] = fieldSpecMap["fields"].findAll({ field -> field.udf == udf })
            }
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
     */
    private void validateCustomFieldName(String fieldName) {
        if (!StringUtil.isBlank(fieldName)) {
            String[] foundField = fieldName.split("(?<=[\\w&&\\D])(?=\\d)")
            String fieldNamePart = foundField[0]
            int fieldCountPart = foundField[1] as Integer

            if (!(CUSTOM_FIELD_NAME_PART == fieldNamePart) || !(fieldCountPart > 0 && fieldCountPart <= Project.CUSTOM_FIELD_COUNT)) {
                reportFieldNameViolation(fieldName)
            }
        } else {
            reportFieldNameViolation(fieldName)
        }
    }

    /**
     * Report security violation and throw an exception
     * @param fieldName
     */
    private void reportFieldNameViolation(String fieldName) {
        securityService.reportViolation("Attempted to report distinct field values for unexisting field name: [${fieldName}]", securityService.currentUsername)
        throw new InvalidRequestException("Field name does not exist or not valid: ${fieldName}")
    }
}
