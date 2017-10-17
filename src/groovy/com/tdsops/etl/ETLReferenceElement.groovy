package com.tdsops.etl

class ETLReferenceElement {

    ETLProcessor processor
    List<String> fields

    ETLReferenceElement (ETLProcessor processor, List<?> fields) {
        this.processor = processor
        this.fields = fields
    }
    /**
     *
     *
     *
     * @param values
     * @return
     */
    ETLReferenceElement with (Object... dataSourceFieldNames) {

        List dataSourceFields = processor.currentRowResult[processor.selectedDomain].elements.findAll {
            it.field?.name in dataSourceFieldNames || it.field?.label in dataSourceFieldNames
        }

        if (dataSourceFields.size() != fields.size()) {
            throw ETLProcessorException.invalidReferenceCommand(fields, new ArrayList(dataSourceFields))
        }

        List assets = AssetClassQueryHelper.where(processor.selectedDomain, fields, dataSourceFields.collect {it.value})

        if (assets.size() == 1) {
            processor.addAssetEntityReferenced(assets.first())
        } else if (assets.size() > 1) {
            throw ETLProcessorException.nonUniqueResults(fields)
        }
        this
    }
}
