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
    ETLReferenceElement with (Object... values) {
        List assets = AssetClassQueryHelper.where(processor.selectedDomain, fields, values as List)

        if (assets.size() == 1) {
            processor.currentRow.instance = assets.first()
        } else if (assets.size() > 1) {
            ETLProcessorException.methodMissing(fields)
        }
        this
    }
}
