package com.tdsops.etl
/**
 *
 *
 */
class ETLProcessor {

    List<List<String>> dataSource = []
    Map<ETLDomain, List<Map<String, ?>>> domainFieldsSpec

    Integer currentRowIndex = 0
    Integer currentColumnIndex = 0

    DebugConsole debugConsole

    ETLDomain selectedDomain
    List<ETLProcessor.Column> columns = []
    Map<String, ETLProcessor.Column> columnsMap = [:]
    List<ETLProcessor.Row> rows = []

    ETLProcessor.Row currentRow
    ETLProcessor.Element currentElement

    Map<ETLDomain, List<Map<String, ?>>> results = [:]

    /**
     *
     * Creates an instance of ETL Processor with all default values
     *
     */
    ETLProcessor () {
        this([], new DebugConsole(buffer: new StringBuffer()), [:])
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data
     *
     * @param data
     * @param domainFieldsSpec
     */
    ETLProcessor (List<List<String>> data) {
        this(data, new DebugConsole(buffer: new StringBuffer()), [:])
    }
    /**
     *
     * Creates an instance of ETL processor with a source of data and a domain mapper validator
     *
     * @param data
     * @param domainFieldsSpec
     */
    ETLProcessor (List<List<String>> data, Map<ETLDomain, List<Map<String, ?>>> domainFieldsSpec) {
        this(data, new DebugConsole(buffer: new StringBuffer()), domainFieldsSpec)
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
     * a domain mapper validator and an instance of etlDomainFieldsValidator
     *
     * @param data
     * @param console
     * @param domainFieldsSpec
     */
    ETLProcessor (List<List<String>> data, DebugConsole console,
                  Map<ETLDomain, List<Map<String, ?>>> domainFieldsSpec) {
        this.dataSource = data
        this.debugConsole = console
        this.domainFieldsSpec = domainFieldsSpec
    }
    /**
     *
     * Selects a domain or throws an ETLProcessorException in case of an invalid domain
     *
     * @param domain
     * @return
     */
    ETLProcessor domain (String domain) {
        if (currentRow != null) {
            commitChanges()
        }
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
    ETLProcessor read (DataPart dataPart) {

        if (dataPart == DataPart.labels) {
            debugConsole.info "Reading labels"

            dataSource.get(currentRowIndex++).eachWithIndex { String columnName, Integer index ->
                ETLProcessor.Column column = new ETLProcessor.Column(label: columnName, index: index)
                columns.add(column)
                columnsMap[column.label] = column
            }
        }
        this
    }

    ETLProcessor iterate (Closure closure) {

        dataSource[currentRowIndex..(dataSource.size() - 1)].each { List<String> crudRowData ->
            currentColumnIndex = 0
            closure(addCrudRowData(crudRowData))
            commitChanges()
            currentRowIndex++
        }
        currentRowIndex--
        this
    }

    private void addCrudRowData (List<String> crudRowData) {
        currentRow = new Row(crudRowData)
        rows.add(currentRow)
        currentRow
    }

    ETLProcessor console (ConsoleStatus status) {
        debugConsole.status = status
        debugConsole.info "Console status changed: $status"
        this
    }

    private Element getCurrentElement () {
        currentElement
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
     * Delegate method to maintenance method's chain
     *
     * @param transformation
     * @return
     */
    ETLProcessor transform (StringTransformation transformation) {
        if (currentElement) {
            currentElement.value = transformation.apply(currentElement.value)
            currentElement.transformations.add(transformation)

            debugConsole.info "Applying transformation on element: $currentElement "
            this
        } else {
            throw ETLProcessorException.invalidCommand("Invalid command. Cannot apply transitions without extract previously.")
        }
    }

    ETLProcessor translateWith (Map ditionary) {

        if (currentElement) {
            if (ditionary.containsKey(currentElement.value)) {
                String oldValue = currentElement.value
                currentElement.value = ditionary[currentElement.value]

                debugConsole.info "Translate $oldValue -> ${currentElement.value}"
            } else {

                debugConsole.warn "Could not translate ${currentElement.value}"
            }
        } else {
            throw ETLProcessorException.invalidCommand("Invalid command. Cannot apply translations without extract previously.")
        }
    }
    /**
     *
     * Delegate translate methods using actions to maintenance method's chain
     *
     * @param actions
     * @return
     */
    ETLProcessor translate (Map actions) {
        if (actions.containsKey('with')) {
            translateWith(actions.get('with'))
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
    ETLProcessor extract (Integer index) {
        currentColumnIndex = index

        currentElement = currentRow.getElement(currentColumnIndex)
        debugConsole.info "Extract element: ${currentElement.value} by index: $index"
        this
    }
    /**
     *
     * Extracts an element from dataSource by its column name
     *
     * @param columnName
     * @return
     */
    ETLProcessor extract (String columnName) {
        currentColumnIndex = columnsMap[columnName].index

        currentElement = currentRow.getElement(currentColumnIndex)
        debugConsole.info "Extract element: ${currentElement.value} by column name: $columnName"
        this
    }
    /**
     *
     * Loads field values in results. From an extracted value or just as a fixed new Element
     *
     * @param field
     * @return
     */
    ETLProcessor load (String field) {

        if (!currentElement) {
            currentElement = currentRow.addNewElement()
            loadPropertyAndValidateFieldSpecs(field)
        } else {
            loadPropertyAndValidateFieldSpecs(field)
            currentElement = null
        }
        this
    }
    /**
     *
     * First, it loads field name in the current selected element.
     * Then, if selected domain is not ETLDomain.External, then it validates with fields specs
     *
     * @param field
     */
    void loadPropertyAndValidateFieldSpecs (String field) {

        currentElement.field.name = field
        currentElement.domain = selectedDomain

        if (ETLDomain.External != selectedDomain) {

            if (!domainFieldsSpec.containsKey(selectedDomain)) {
                throw ETLProcessorException.unknownDomainFieldsSpec(selectedDomain)
            }

            Map<String, ?> fieldSpec = domainFieldsSpec[selectedDomain].find { it.field == field || it.label == field }
            if (!fieldSpec) {
                throw ETLProcessorException.domainWithoutFieldsSpec(selectedDomain, field)
            }

            currentElement.field.label = fieldSpec.label
            currentElement.field.control = fieldSpec.control
            currentElement.field.constraints = fieldSpec.constraints
        }
    }
    /**
     *
     *  Loads a fixed value in a current element
     *
     * @param value
     * @return
     */
    ETLProcessor with (String value) {
        currentElement.value = value
        currentElement = null
        this
    }
    /**
     *
     * Commits current changes with the current domain class and rows already processed as a partial result
     *
     */
    void commitChanges () {

        if (!results.containsKey(selectedDomain)) {
            results.put(selectedDomain, [])
        }

        List<Map<String, ?>> partialResults = currentRow.getLoadedElements(selectedDomain).collect { Element element ->
            [
                    originalValue: element.originalValue,
                    value        : element.value,
                    field        : element.field
            ]
        }
        debugConsole.info "Saving partial results ${partialResults*.field.name} for domain ${selectedDomain}"

        if (partialResults) {
            results.get(selectedDomain).add(partialResults)
        }
    }

//    def methodMissing(String methodName, args) {
//        debugConsole.info "Method missing: ${methodName}, args: ${args}"
//
//    }

    ETLDomain getSelectedDomain () {
        selectedDomain
    }

    ETLProcessor.Column column (String columnName) {
        columnsMap[columnName]
    }

    ETLProcessor.Column column (Integer columnName) {
        columns[columnName]
    }

    Set getColumnNames () {
        columnsMap.keySet()
    }

    ETLProcessor.Row getCurrentRow () {
        currentRow
    }

    ETLProcessor.Row getRow (Integer index) {
        rows[index]
    }

    static class Column {

        String label
        Integer index
    }

    static class Row {

        List<Element> elements

        Row () {
            elements = []
        }

        Row (List<String> values) {
            elements = []
            values.eachWithIndex { String value, int i ->
                addElement new Element(originalValue: value, value: value, index: i)
            }
        }

        void addElement (Element element) {
            elements.add(element)
        }

        Element addNewElement () {
            Element element = new Element(originalValue: "", value: "", index: elements.size())
            addElement element
            element
        }

        Element getElement (Integer index) {
            elements[index]
        }

        List<Element> getLoadedElements (ETLDomain domain) {
            elements.findAll { it.domain == domain && it.isSelected() }
        }
    }

    static class Element {

        String originalValue
        String value
        Integer index
        ETLDomain domain
        List<StringTransformation> transformations = []
        Field field = new Field()

        Boolean isSelected () {
            !!field.name
        }
    }

    static class Field {

        String name = ""
        String control
        String label
        Map constraints
    }
}
