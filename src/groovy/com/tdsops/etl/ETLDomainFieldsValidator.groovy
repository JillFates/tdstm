package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass

class ETLDomainFieldsValidator {

    Map<ETLDomain, Map> assetClassFieldsSpecMap

    ETLDomainFieldsValidator () {
        assetClassFieldsSpecMap = [:]
        assetClassFieldsSpecMap[ETLDomain.Application] = [:]
        assetClassFieldsSpecMap[ETLDomain.Device] = [:]
        assetClassFieldsSpecMap[ETLDomain.Database] = [:]
        assetClassFieldsSpecMap[ETLDomain.Storage] = [:]
        assetClassFieldsSpecMap[ETLDomain.Person] = [:]
        assetClassFieldsSpecMap[ETLDomain.External] = [:]
        assetClassFieldsSpecMap[ETLDomain.Comment] = [:]
    }

    void setFieldsSpecFor (ETLDomain domain, List<Map<String, ?>> fieldsSpec) {

        fieldsSpec.each { spec ->
            Map<String, ?> fieldSpec = [
                    constraints: spec.constraints,
                    control    : spec.control,
                    field      : spec.field,
                    label      : spec.label,
            ]

            assetClassFieldsSpecMap[domain][spec.field] = fieldSpec
            assetClassFieldsSpecMap[domain][spec.label] = fieldSpec
        }
    }

    Boolean hasField (AssetClass assetClass, String field) {
        assetClassFieldsSpecMap[assetClass].containsKey(field)
    }

    Map<String, ?> field (AssetClass assetClass, String field) {
        assetClassFieldsSpecMap[assetClass][field]
    }


}
