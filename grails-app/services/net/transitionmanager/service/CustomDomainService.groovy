package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AssetClass
import groovy.json.JsonSlurper

class CustomDomainService implements ServiceMethods {
    def grailsResourceLocator

    /**
     * Retrieve custom field specs
     * @param domain
     * @return
     */
    Map customFieldSpecs(String domain) {
        return getFieldSpecs(domain, 1)
    }

    /**
     * Retrieve standard field specs
     * @param domain
     * @return
     */
    Map standardFieldSpecs(String domain) {
        return getFieldSpecs(domain, 0)
    }

    def saveFieldSpecs() {

    }

    /**
     * Get field specs
     * @param domain AssetClass type
     * @param udf whether to return custom or standard fields
     * @return
     */
    private Map getFieldSpecs(String domain, int udf) {
        Map fieldSpec = [:]
        List<String> assetClassTypes = resolveAssetClassType(domain)

        for (String assetClass : assetClassTypes) {
            def fieldSpecJSON = readFieldSpecFromFileSystem(assetClass)
            fieldSpec["${assetClass.toUpperCase()}"] = fieldSpecJSON.fields.findAll({ field -> field.udf == udf })
        }

        return fieldSpec
    }

    /**
     * Resolve which AssetClass field specs to return or all
     * @param domain
     * @return
     */
    private List<String> resolveAssetClassType(String domain) {
        if (domain.toLowerCase() == "assets") {
            return AssetClass.values().collect({ac -> ac.toString().toLowerCase()})
        } else {
            if (AssetClass.safeValueOf(domain.toUpperCase())) {
                return [domain.toLowerCase()]
            } else {
                throw new InvalidParamException("Invalid AssetClass name: ${domain}")
            }
        }
    }

    /**
     * Load JSON field specs file from filesystem
     * @param domain
     * @return
     */
    private readFieldSpecFromFileSystem(String domain) {
        def inputFile = grailsResourceLocator.findResourceForURI("classpath:/customField/${domain}.json").file
        def inputJSON = new JsonSlurper().parseText(inputFile.text)
        return inputJSON
    }
}
