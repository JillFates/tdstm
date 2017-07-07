package com.tdsops.tm.asset.export

import net.transitionmanager.service.CustomDomainService

/**
 * Asset export spreadsheet column mapper
 */
class SpreadsheetColumnMapper {
    private List<String> templateHeaders
    private List<Map<String, ?>> customFields = []
    private List<Map<String, ?>> standardFields = []
    private List<String> fixedColumnHeaders = ["appId", "assetId", "dbId", "filesId", "DepGroup"]
    private Map<String, ?> columnFieldMap = [:]

    SpreadsheetColumnMapper(List<String> templateHeaders, List<Map<String, ?>> fieldSpecs) {
        this.templateHeaders = templateHeaders

        setFieldSpecs(fieldSpecs)
        updateColumnFieldMapWithSpreadsheetHeaders()
        updateColumnFieldMapWithCustomFieldSpecs()
    }

    /**
     * Return the spreadsheet columns indexes to fields mapping
     * @return
     */
    Map<String, ?> getColumnFieldMap() {
        return columnFieldMap
    }

    /**
     * Returns a column index looking by column header
     * @param header
     * @return
     */
    int getColumnIndexByHeader(String header) {
        if (columnFieldMap.containsKey(header)) {
            return columnFieldMap[header]["order"] as int
        }
        return -1
    }

    /**
     * Find and returns the list of missing standard column headers in the spreadsheet
     * @return
     */
    Set<String> getMissingHeaders() {
        List<String> standardHeaders = standardFields*.label
        return standardHeaders - templateHeaders
    }

    /**
     * Updates column field map from spreadsheet template headers to standard fields
     */
    private void updateColumnFieldMapWithSpreadsheetHeaders() {
        // map current spreadsheet columns to standard fields
        templateHeaders.eachWithIndex { String columnHeader, int i ->
            if (columnHeader in fixedColumnHeaders) {
                // add fixed column headers to the map
                // this headers are special ones that are mapped differently according to the AssetClass type
                columnFieldMap.put(columnHeader, ["field": columnHeader, "label": columnHeader, "order": i, "udf": CustomDomainService.STANDARD_FIELD])
            } else {
                Map<String, ?> fieldSpec = getStandardFieldSpecByLabel(columnHeader)
                if (fieldSpec) {
                    // update field position from the spreadsheet
                    fieldSpec["order"] = i
                    columnFieldMap.put(columnHeader, fieldSpec)
                }
            }
        }
    }

    /**
     * Updates column field map with custom fields specs
     */
    private void updateColumnFieldMapWithCustomFieldSpecs() {
        int columnIndex = templateHeaders.size()
        Map<String, ?> customFieldsSorted = getCustomFieldsOrdered()
        customFieldsSorted.each { key, value ->
            value["order"] = columnIndex++
            columnFieldMap.put(key, value)
        }
    }

    /**
     * Takes the list of custom fields and creates a ordered map by its order property
     * @return a ordered map like K, V -> where K is the field label and V is the field spec
     */
    private Map<String, ?> getCustomFieldsOrdered() {
        Map<String, ?> customFieldsOrdered = [:]
        customFields.forEach({ Map<String, ?> fieldSpec ->
            customFieldsOrdered.put(fieldSpec["label"] as String, fieldSpec)
        })

        customFieldsOrdered = customFieldsOrdered.sort { a, b ->
            (a.value["order"] as int) <=> (b.value["order"] as int)
        }
        return customFieldsOrdered
    }

    /**
     * Set standard and custom fields in the corresponding collections for internal use
     * @param fieldSpecs
     */
    private void setFieldSpecs(List<Map<String, ?>> fieldSpecs) {
        for (Map<String, ?> fieldSpec : fieldSpecs) {
            if (fieldSpec["udf"] as int == CustomDomainService.CUSTOM_USER_FIELD) {
                customFields.add(fieldSpec)
            } else {
                standardFields.add(fieldSpec)
            }
        }
    }

    /**
     * Finds a standard fiend by label
     * @param label
     * @return
     */
    private Map<String, ?> getStandardFieldSpecByLabel(String label) {
        if (standardFields) {
            return standardFields.find { field -> field["label"] == label }
        }
        return null
    }
}
