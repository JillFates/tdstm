package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass

class ETLAssetClassFieldsValidator implements ETLFieldsValidator {

    Map<ETLDomain, List<Map<String, ?>>> assetClassFieldsSpecMap = [:]
    /**
     *
     * Add fields specification for an AssetClass instance
     *
     * @param assetClass
     * @param fieldsSpec
     */
    void addAssetClassFieldsSpecFor (AssetClass assetClass, List<Map<String, ?>> fieldsSpec) {

        switch (assetClass) {
            case AssetClass.APPLICATION:
                assetClassFieldsSpecMap[ETLDomain.Application] = fieldsSpec
                break
            case AssetClass.DATABASE:
                assetClassFieldsSpecMap[ETLDomain.Database] = fieldsSpec
                break
            case AssetClass.DEVICE:
                assetClassFieldsSpecMap[ETLDomain.Device] = fieldsSpec
                break
            case AssetClass.STORAGE:
                assetClassFieldsSpecMap[ETLDomain.Storage] = fieldsSpec
                break
        }
    }
    /**
     *
     * Checks if it has fields specification for a domain instance
     *
     * @param domain
     * @param field
     * @return
     */
    Boolean hasSpecs (ETLDomain domain, String field) {
        (assetClassFieldsSpecMap[domain] != null)
    }
    /**
     *
     * Looks up a fields specification for a domain instance
     *
     * @param domain
     * @param field
     * @return
     */
    Map<String, ?> lookup (ETLDomain domain, String field) {

        if (hasSpecs(domain, field)) {
            assetClassFieldsSpecMap[domain].find { it.field == field || it.label == field }
        }
    }
}
