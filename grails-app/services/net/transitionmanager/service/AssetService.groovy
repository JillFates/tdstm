package net.transitionmanager.service

import com.tds.asset.AssetEntity

class AssetService {

    /**
     * Used to set default values for custom fields on the given asset
     * @param asset - the asset to set the default custom fields values into
     * @param fieldSpec - the list of custom fields
     */
    void setCustomDefaultValues(AssetEntity asset, List fieldSpec) {

        fieldSpec.each {
            if (it.default) {
                asset[it.field] = it.default
            }
        }
    }
}
