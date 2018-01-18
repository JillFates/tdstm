package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.StringUtil
import net.transitionmanager.asset.AssetFacade

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
     * Factory method that constructs a AssetFacade populating the custom fields specs for the asset entity type provided
     * @param assetEntity - the assent entity
     * @param readonly - whether the facade needs to be readonly
     * @return
     */
    AssetFacade getAssetFacade(AssetEntity assetEntity, boolean readonly) {
        Map<String, ?> fieldSpecs = customDomainService.customFieldSpecs(assetEntity.project, assetEntity.assetClass.toString(), false)
        return new AssetFacade(assetEntity, fieldSpecs, readonly)
    }
}
