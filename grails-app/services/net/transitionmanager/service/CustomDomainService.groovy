package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Project
import org.codehaus.groovy.grails.web.json.JSONObject

class CustomDomainService implements ServiceMethods {
    public static final String ALL_ASSET_CLASSES = "ASSETS"

    SecurityService securityService
    SettingService settingService

    /**
     * Retrieve custom field specs
     * @param domain
     * @param showOnly : flag to request only those visible fields.
     * @return
     */
    Map customFieldSpecs(String domain, boolean showOnly = false) {
        return getFilteredFieldSpecs(domain, 1, showOnly)
    }

    /**
     * Retrieve standard field specs
     * @param domain
     * @return
     */
    Map standardFieldSpecs(String domain) {
        return getFilteredFieldSpecs(domain, 0)
    }

    /**
     * Retrieve all custom field specs
     * @param domain
     * @return
     */
    Map allFieldSpecs(String domain){
        Project currentProject = securityService.loadUserCurrentProject()
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassType(domain)

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
        List<String> assetClassTypes = resolveAssetClassType(domain)
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
     * Get field specs
     * @param domain AssetClass type
     * @param udf whether to return custom or standard fields
     * @param showOnly flag to filter those fields that shown in views, etc.
     * @return
     */
    private Map getFilteredFieldSpecs(String domain, int udf, boolean showOnly = false) {
        Project currentProject = securityService.loadUserCurrentProject()
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassType(domain)

        for (String assetClass : assetClassTypes) {
            def fieldSpecMap = settingService.getAsMap(currentProject, SettingType.CUSTOM_DOMAIN_FIELD_SPEC, domain.toUpperCase())
            if (showOnly) {
            fieldSpec["${assetClass.toUpperCase()}"] = fieldSpecMap
            } else {
            fieldSpec["${assetClass.toUpperCase()}"]["fields"] = fieldSpecMap["fields"].findAll({ field -> field.udf == udf })
            }
            
        }

        return fieldSpec
    }

    /**
     * Resolve which AssetClass field specs to return or all
     * @param domain
     * @return
     */
    private List<String> resolveAssetClassType(String domain) {
        if (domain.toUpperCase() == ALL_ASSET_CLASSES) {
            return AssetClass.values().collect({ac -> ac.toString().toUpperCase()})
        } else {
            if (AssetClass.safeValueOf(domain.toUpperCase())) {
                return [domain.toUpperCase()]
            } else {
                throw new InvalidParamException("Invalid AssetClass name: ${domain}")
            }
        }
    }

}
