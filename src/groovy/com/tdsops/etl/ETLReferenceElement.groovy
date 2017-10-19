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

        Map<String, ?> fieldsMap = [:]

        processor.currentRowResult[processor.selectedDomain].elements.each {
            fieldsMap[it?.field?.name] = it
            fieldsMap[it?.field?.label] = it
        }

        List<Map<String, ?>> dataSourceFields = []

        dataSourceFieldNames.each {
            if(fieldsMap.containsKey(it)){
                dataSourceFields.add(fieldsMap[it])
            }
        }

        if (dataSourceFields.size() != fields.size()) {
            throw ETLProcessorException.incorrectAmountOfParameters(fields, new ArrayList(dataSourceFieldNames))
        }

        Map<String, ?> fieldsSpec = fields.withIndex().collectEntries { def field, int i ->
            [("$field".toString()): dataSourceFields[i].value]
        }

        List assets = AssetClassQueryHelper.where(processor.selectedDomain, fieldsSpec)

        if (assets.size() == 1) {
            processor.addAssetEntityReferenced(assets.first())
        } else if (assets.size() > 1) {
            throw ETLProcessorException.nonUniqueResults(fields)
        }
        this
    }
}
