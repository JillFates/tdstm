package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.Project

class AssetService {

    def customDomainService
    def securityService

    /**
     * Used to set default values for custom fields on the given asset
     * @param asset - the asset to set the default custom fields values into
     * @param fieldSpec - the list of custom fields
     */
    void setCustomDefaultValues(AssetEntity asset) {

        String domain = asset.assetClass.toString().toUpperCase()

        // The asset has to have a project assigned to it to make this all work correctly
        assert asset.project

        // Get list of ALL custom fields
        Map fieldSpecs = customDomainService.customFieldSpecs(asset.project, domain)
        List fieldList = fieldSpecs[domain].fields

        // Loop over the fields and set the default value appropriately
        for (field in fieldList) {
            if ( ! StringUtil.isBlank(field.default) ) {
                asset[field.field] = field.default
            }
        }
    }

    /**
     * This method retrieves the field specs given a domain and an option of which
     * fields are needed (standard, custom or all).
     * It also allows to filter only a particular subset of the keys, for cases
     * where the whole specs aren't required.
     *
     * @param domain - asset domain
     * @param option - which fields are needed.
     *             @see CustomDomainService.ALL_FIELDS
     *             @see CustomDomainService.STANDARD_FIELD
     *             @see CustomDomainService.USER_DEFINED_FIELD
     * @param values - a list of string with the name of the fields to be included in the result.
     *
     * @return a list with the specs.
     */
    List fieldSpecs(String domain, int option = CustomDomainService.ALL_FIELDS, List values = null) {
        Project project = securityService.loadUserCurrentProject()
        // This list will contain the resulting specs.
        List fieldSpecs = []
        // Checks that an actual domain is given.
        if (domain) {
            // Retrieves the specs for all available fields for the domain.
            Map fields = null

            switch (option) {
                case CustomDomainService.ALL_FIELDS:
                    fields = customDomainService.allFieldSpecs(domain)
                    break
                case CustomDomainService.STANDARD_FIELD:
                    fields = customDomainService.standardFieldSpecs(project, domain)
                    break
                case CustomDomainService.USER_DEFINED_FIELD:
                    customDomainService.customFieldSpecs(project, domain)
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
