package com.tdsops.etl

import com.tds.asset.AssetEntity

/**
 *
 *
 */
class ETLProcessor {

    List<List<String>> dataSource = []
    ETLFieldsValidator fieldsValidator
    Map<String, ETLTransformation> transformations = [:]

    Integer currentRowIndex = 0
    Integer currentColumnIndex = 0

    DebugConsole debugConsole

    List<Column> columns = []
    Map<String, Column> columnsMap = [:]
    List<Row> rows = []
    Row currentRow

    ETLDomain selectedDomain
    Map<ETLDomain, List<ReferenceResult>> results = [:]
    Map<ETLDomain, ReferenceResult> currentRowResult = [:]

    /**
     *
     * Creates an instance of ETL Processor with all default values
     *
     */
    ETLProcessor () {
        this([], new DebugConsole(buffer: new StringBuffer()), null, [:])
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data
     *
     * @param data
     * @param domainFieldsSpec
     */
    ETLProcessor (List<List<String>> data) {
        this(data, new DebugConsole(buffer: new StringBuffer()), null, [:])
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data
     * and a map with available transformations
     *
     * @param data
     * @param transformations
     */
    ETLProcessor (List<List<String>> data, Map<String, ETLTransformation> transformations) {
        this(data, new DebugConsole(buffer: new StringBuffer()), null, transformations)
    }
    /**
     *
     *
     * @param data
     * @param console
     * @param transformations
     */
    ETLProcessor (List<List<String>> data, DebugConsole console, Map<String, ETLTransformation> transformations) {
        this(data, console, null, transformations)
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data and a domain mapper validator
     *
     * @param data
     * @param fieldsValidator
     */
    ETLProcessor (List<List<String>> data, ETLFieldsValidator fieldsValidator) {
        this(data, new DebugConsole(buffer: new StringBuffer()), fieldsValidator)
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data and a debugguer console
     *
     * @param data
     * @param console
     */
    ETLProcessor (List<List<String>> data, DebugConsole console) {
        this(data, console, [:])
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data,
     * a domain mapper validator and an instance of fieldsValidator
     *
     * @param data
     * @param console
     * @param fieldsValidator
     */
    ETLProcessor (List<List<String>> data, DebugConsole console, ETLFieldsValidator fieldsValidator) {
        this.dataSource = data
        this.debugConsole = console
        this.fieldsValidator = fieldsValidator
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data,
     * a domain mapper validator and an instance of fieldsValidator
     * with a map of available transformations
     *
     * @param data
     * @param console
     * @param fieldsValidator
     * @param transformations
     */
    ETLProcessor (List<List<String>> data,
                  DebugConsole console,
                  ETLFieldsValidator fieldsValidator,
                  Map<String, ETLTransformation> transformations) {
        this.dataSource = data
        this.debugConsole = console
        this.fieldsValidator = fieldsValidator
        this.transformations = transformations
    }
    /**
     *
     * Selects a domain or throws an ETLProcessorException in case of an invalid domain
     *
     * @param domain
     * @return
     */
    ETLProcessor domain (String domain) {
        selectedDomain = ETLDomain.values().find { it.name() == domain }
        if (selectedDomain == null) {
            throw ETLProcessorException.invalidDomain(domain)
        }

        debugConsole.info("Selected Domain: $domain")
        this
    }
    /**
     *
     * Read Labels from source of data
     *
     * @param dataPart
     * @return
     */
    ETLProcessor read (String dataPart) {

        if ("labels".equalsIgnoreCase(dataPart)) {
            debugConsole.info "Reading labels"

            dataSource.get(currentRowIndex++).eachWithIndex { String columnName, Integer index ->
                Column column = new Column(label: columnName, index: index)
                columns.add(column)
                columnsMap[column.label] = column
            }
        }
        this
    }
    /**
     *
     * Iterates and applies closure to every row in the dataSource
     *
     * @param closure
     * @return
     */
    ETLProcessor iterate (Closure closure) {

        dataSource[currentRowIndex..(dataSource.size() - 1)].each { List<String> crudRowData ->
            currentColumnIndex = 0
            closure(addCrudRowData(currentRowIndex, crudRowData))

            currentRowResult.each { ETLDomain key, ReferenceResult value ->
                if (!results.containsKey(key)) {
                    results[key] = []
                }
                results[key].add(value)
            }

            currentRowResult = [:]
            currentRowIndex++
        }
        currentRowIndex--
        this
    }
    /**
     *
     * Sets Status console to on/off for allow/disallow log messages.
     *
     * @param status
     * @return
     */
    ETLProcessor console (String status) {

        DebugConsole.ConsoleStatus consoleStatus = DebugConsole.ConsoleStatus.values().find { it.name() == status }

        if (consoleStatus == null) {
            throw ETLProcessorException.invalidConsoleStatus(status)
        }
        debugConsole.status = consoleStatus
        debugConsole.info "Console status changed: $consoleStatus"
        this
    }

    ETLProcessor skip (Integer amount) {
        if (amount + currentRowIndex <= dataSource.size()) {
            currentRowIndex += amount
        } else {
            throw ETLProcessorException.invalidSkipStep(amount)
        }
        this
    }
    /**
     *
     * Extracts an element from dataSource by its index in the row
     *
     * @param index
     * @return
     */
    def extract (Integer index) {

        if (index in (0..currentRow.size())) {
            currentColumnIndex = index

            Element selectedElement = currentRow.getElement(currentColumnIndex)
            debugConsole.info "Extract element: ${selectedElement.value} by index: $index"
            selectedElement
        } else {
            throw ETLProcessorException.extractInvalidColumn(index)
        }
    }
    /**
     *
     * Extracts an element from dataSource by its column name
     *
     * @param columnName
     * @return
     */
    def extract (String columnName) {

        if (columnsMap.containsKey(columnName)) {
            currentColumnIndex = columnsMap[columnName].index

            Element selectedElement = currentRow.getElement(currentColumnIndex)
            debugConsole.info "Extract element: ${selectedElement.value} by column name: $columnName"

            selectedElement
        } else {
            throw ETLProcessorException.extractMissingColumn(columnName)
        }
    }
    /**
     *
     * Loads field values in results. From an extracted value or just as a fixed new Element
     *
     * @param field
     * @return
     */
    def load (final String field) {

        [
                with: { value ->

                    Map<String, ?> fieldSpec = lookUpFieldSpecs(selectedDomain, field)

                    Element newElement = currentRow.addNewElement(value, this)
                    newElement.field.name = field
                    newElement.domain = selectedDomain

                    if (fieldSpec) {
                        newElement.field.label = fieldSpec.label
                        newElement.field.control = fieldSpec.control
                        newElement.field.constraints = fieldSpec.constraints
                    }

                    addElementLoaded(selectedDomain, newElement)
                    newElement
                }
        ]
    }
    /**
     *
     *
     * @param method
     * @return
     */
    def reference (String... fields) {
        new ETLReferenceElement(this, fields as List)
    }
    /**
     *
     * It looks up the field Spec for Domain by fieldName
     *
     * @param domain
     * @param fieldName
     * @return
     */
    private Map<String, ?> lookUpFieldSpecs (ETLDomain domain, String field) {

        Map<String, ?> fieldSpec

        if (ETLDomain.External != domain) {

            if (!fieldsValidator.hasSpecs(domain, field)) {
                throw ETLProcessorException.unknownDomainFieldsSpec(domain)
            }

            fieldSpec = fieldsValidator.lookup(selectedDomain, field)
            if (!fieldSpec) {
                throw ETLProcessorException.domainWithoutFieldsSpec(domain, field)
            }
        }
        fieldSpec
    }

    private void addCrudRowData (Integer rowIndex, List<String> crudRowData) {
        currentRow = new Row(rowIndex, crudRowData, this)
        rows.add(currentRow)
        currentRow
    }
    /**
     *
     * Adds a loaded element with the current domain in results
     *
     */
    void addElementLoaded (ETLDomain domain, Element element) {

        if (!currentRowResult.containsKey(selectedDomain)) {
            currentRowResult[selectedDomain] = new ReferenceResult()
        }

        currentRowResult[selectedDomain].elements.add([
                originalValue: element.originalValue,
                value        : element.value,
                field        : [
                        name       : element.field.name,
                        control    : element.field.control,
                        label      : element.field.label,
                        constraints: element.field.constraints
                ]
        ])

        debugConsole.info "Adding element ${element} in results for domain ${domain}"
    }
    /**
     *
     * Adds an asset entity instance referenced from a datasource field
     *
     * @param assetEntity
     * @param row
     */
    void addAssetEntityReferenced (AssetEntity assetEntity) {

        if (!currentRowResult.containsKey(selectedDomain)) {
            currentRowResult[selectedDomain] = new ReferenceResult()
        }

        currentRowResult[selectedDomain].reference = [id: assetEntity.id]
    }

    def methodMissing (String methodName, args) {
        debugConsole.info "Method missing: ${methodName}, args: ${args}"
        throw ETLProcessorException.methodMissing(methodName, args)
    }

    ETLDomain getSelectedDomain () {
        selectedDomain
    }

    Column column (String columnName) {
        columnsMap[columnName]
    }

    Column column (Integer columnName) {
        columns[columnName]
    }

    Set getColumnNames () {
        columnsMap.keySet()
    }

    Row getCurrentRow () {
        currentRow
    }

    Row getRow (Integer index) {
        rows[index]
    }
}
