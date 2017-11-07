package com.tdsops.etl

import com.tds.asset.AssetEntity
import getl.data.Dataset
import net.transitionmanager.domain.Project

/**
 *
 *
 */
class ETLProcessor {

    Project project
    Dataset dataSet
    List<getl.data.Field> fields
    ETLFieldsValidator fieldsValidator

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

    Set globalTransformers = [] as Set

    static Trimmer = { Element element ->
        element.trim()
    }

    static Sanitizer = { Element element ->
        element.sanitize()
    }

    static Replacer = { String regex, String replacement ->
        { Element element ->
            element.replace(regex, replacement)
        }
    }
    /**
     *
     *  https://en.wikipedia.org/wiki/Control_character
     */
    static ControlCharactersRegex = /\\0|\\a\\0\\b\\t\\n\\v\\f\\r/
    /**
     *
     * Creates an instance of ETL processor with a source of data,
     * a domain mapper validator and an instance of fieldsValidator
     * with a map of available transformations
     *
     * @param project
     * @param dataSet
     * @param console
     * @param fieldsValidator
     */
    ETLProcessor (Project project, Dataset dataSet, DebugConsole console, ETLFieldsValidator fieldsValidator) {
        this.project = project
        this.dataSet = dataSet
        this.debugConsole = console
        this.fieldsValidator = fieldsValidator
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

            fields = this.dataSet.connection.driver.fields(this.dataSet)
            fields.eachWithIndex { getl.data.Field field, Integer index ->
                Column column = new Column(label: field.name, index: index)
                columns.add(column)
                columnsMap[column.label] = column
            }
            currentRowIndex++
            debugConsole.info "Reading labels ${columnsMap.values().collectEntries { [("${it.index}"): it.label] }}"
        }
        this
    }
    /**
     *
     *
     * @param from
     * @return
     */
    def from (int from) {

        [to: { int to ->
            [iterate: { Closure closure ->
                List subList = this.dataSet.rows().subList(from, to)
                doIterate(subList, closure)
            }]
        }]
    }
    /**
     *
     *
     *
     * @param numbers
     * @return
     */
    def using (int[] numbers) {

        [iterate: { Closure closure ->
            List rowNumbers = numbers as List
            List rows = this.dataSet.rows()
            List subList = rowNumbers.collect { rows.get(it) }
            doIterate(subList, closure)
        }]
    }
    /**
     *
     *
     * @param rows
     * @param closure
     * @return
     */
    ETLProcessor doIterate (List rows, Closure closure) {

        rows.each { def row ->
            currentColumnIndex = 0
            closure(addCrudRowData(currentRowIndex, row))

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
     * Iterates and applies closure to every row in the dataSource
     *
     * @param closure
     * @return
     */
    ETLProcessor iterate (Closure closure) {
        doIterate(this.dataSet.rows(), closure)
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
    /**
     *
     * Removes leading and trailing whitespace from a string.
     *
     * @param status
     * @return the instance of ETLProcessor who received this message
     */
    ETLProcessor trim (String status) {

        if (status == 'on') {
            globalTransformers.add(Trimmer)
        } else if (status == 'of') {
            globalTransformers.remove(Trimmer)
        }

        debugConsole.info "Global trim status changed: $status"
        this
    }
    /**
     *
     *
     * @param status
     * @return the instance of ETLProcessor who received this message
     */
    ETLProcessor sanitize (String status) {

        if (status == 'on') {
            globalTransformers.add(Sanitizer)
        } else if (status == 'of') {
            globalTransformers.remove(Sanitizer)
        }

        debugConsole.info "Global sanitize status changed: $status"
        this
    }

    ETLProcessor replace (String regex, String replacement) {

        globalTransformers.add(Replacer(regex, replacement))
        debugConsole.info "Global replace regex: $regex wuth replacement: $replacement"
        this
    }

    /**
     *
     *
     * @param control
     * @return
     */
    def replace (String control) {

        debugConsole.info "Global trm status changed: $control"
        if (control == 'ControlCharacters') {
            [with: { y ->
                globalTransformers.add(Replacer(ControlCharactersRegex, y))
            },
             off : { ->

             }]
        }

    }

    ETLProcessor skip (Integer amount) {
        if (amount + currentRowIndex <= this.dataSet.readRows) {
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

        if (!(index in (0..currentRow.size()))) {
            throw ETLProcessorException.extractInvalidColumn(index)
        }

        currentColumnIndex = index
        doExtract()
    }
    /**
     *
     * Extracts an element from dataSource by its column name
     *
     * @param columnName
     * @return
     */
    def extract (String columnName) {

        if (!columnsMap.containsKey(columnName)) {
            throw ETLProcessorException.extractMissingColumn(columnName)
        }
        currentColumnIndex = columnsMap[columnName].index

        doExtract()
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
     * Add a message in console for an element from dataSource by its index in the row
     *
     * @param index
     * @return
     */
    def debug (Integer index) {

        if (index in (0..currentRow.size())) {
            currentColumnIndex = index
            doDebug currentRow.getElement(currentColumnIndex)
        } else {
            throw ETLProcessorException.missingColumn(index)
        }
    }
    /**
     *
     * Add a message in console for an element from dataSource by its column name
     *
     * @param columnName
     * @return
     */
    def debug (String columnName) {

        if (columnsMap.containsKey(columnName)) {
            currentColumnIndex = columnsMap[columnName].index
            doDebug currentRow.getElement(currentColumnIndex)
        } else {
            throw ETLProcessorException.missingColumn(columnName)
        }
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
    /**
     *
     * Adds a message debug with element content in console
     *
     * @param element
     */
    private def doDebug (Element element) {
        debugConsole.debug "${[position: [element.columnIndex, element.rowIndex], value: element.value]}"
        element
    }

    private void addCrudRowData (Integer rowIndex, Map row) {
        currentRow = new Row(rowIndex, fields.collect { row[it.name] }, this)
        rows.add(currentRow)
        currentRow
    }
    /**
     *
     * Private method that executes extract method command internally.
     * @return
     */
    private def doExtract () {
        Element selectedElement = currentRow.getElement(currentColumnIndex)
        debugConsole.info "Extract element: ${selectedElement.value} by column index: ${currentColumnIndex}"
        applyGlobalTransformations(selectedElement)
        selectedElement
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

        // Add the Asset ID number to the reference list if it isn't already there
        if (!currentRowResult[selectedDomain].reference.contains(assetEntity.id)) {
            currentRowResult[selectedDomain].reference << assetEntity.id
        }
    }

    void applyGlobalTransformations (Element element) {

        globalTransformers.each { transformer ->
            transformer(element)
        }
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

    List<String> getAvailableMethods () {
        ['domain', 'read', 'iterate', 'console', 'skip', 'extract', 'load', 'reference', 'with', 'on', 'labels', 'transform', 'translate', 'debug']
    }

    List<String> getAssetFields () {
        ['id', 'assetName', 'moveBundle']
    }
}
