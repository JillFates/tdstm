package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass

class DomainAssetFieldsMapper {


    Map<AssetClass, Map> assetClassFieldsSpecMap

    DomainAssetFieldsMapper() {
        assetClassFieldsSpecMap = [:]
        assetClassFieldsSpecMap[AssetClass.APPLICATION] = [:]
        assetClassFieldsSpecMap[AssetClass.STORAGE] = [:]
        assetClassFieldsSpecMap[AssetClass.DATABASE] = [:]
        assetClassFieldsSpecMap[AssetClass.DEVICE] = [:]
    }

    void setFieldsSpecFor(AssetClass assetClass, List<Map<String, ?>> fieldsSpec) {

        fieldsSpec.each { spec ->
            Map<String, ?> fieldSpec = [
                    constraints: spec.constraints,
                    control    : spec.control,
                    field      : spec.field,
                    label      : spec.label,
            ]

            assetClassFieldsSpecMap[assetClass][spec.field] = fieldSpec
            assetClassFieldsSpecMap[assetClass][spec.label] = fieldSpec
        }
    }

    Boolean hasField(AssetClass assetClass, String field) {
        assetClassFieldsSpecMap[assetClass].containsKey(field)
    }

    Map<String, ?> field(AssetClass assetClass, String field) {
        assetClassFieldsSpecMap[assetClass][field]
    }


}
