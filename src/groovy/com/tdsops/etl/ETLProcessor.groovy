package com.tdsops.etl
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
    ETLProcessor read (String dataPart) {

        if ("labels".equalsIgnoreCase(dataPart)) {
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

    ETLProcessor console (String status) {

        ConsoleStatus consoleStatus = ConsoleStatus.values().find { it.name() == status }

        if (consoleStatus == null) {
            throw ETLProcessorException.invalidConsoleStatus(status)
        }
        debugConsole.status = consoleStatus
        debugConsole.info "Console status changed: $consoleStatus"
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
    ETLProcessor transform (String transformationName) {

        ETLTransformation transformation = lookupTransformation(transformationName)

        if (currentElement) {
            transformation.apply(currentElement)
            currentElement.transformations.add(transformation)

            debugConsole.info "Applying transformation on element: $currentElement "
            this
        } else {
            throw ETLProcessorException.invalidCommand("Invalid command. Cannot apply transitions without extract previously.")
        }
    }

    ETLTransformation lookupTransformation (String name) {
        if (transformations && transformations.containsKey(name)) {
            transformations[name]
        } else {
            throw ETLProcessorException.unknownTransformation(name)
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

        if (index in (0..currentRow.size())) {
            currentColumnIndex = index

            currentElement = currentRow.getElement(currentColumnIndex)
            debugConsole.info "Extract element: ${currentElement.value} by index: $index"
            this
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
    ETLProcessor extract (String columnName) {

        if (columnsMap.containsKey(columnName)) {
            currentColumnIndex = columnsMap[columnName].index

            currentElement = currentRow.getElement(currentColumnIndex)
            debugConsole.info "Extract element: ${currentElement.value} by column name: $columnName"
            this
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
    ETLProcessor load (final String field) {

        Map<String, ?> fieldSpec = lookUpFieldSpecs(selectedDomain, field)

        Boolean hasWith = false

        [
                with: { value ->
                    hasWith = true
                    currentElement = currentRow.addNewElement(value)
                    currentElement.field.name = field
                    currentElement.domain = selectedDomain

                    if (fieldSpec) {
                        currentElement.field.label = fieldSpec.label
                        currentElement.field.control = fieldSpec.control
                        currentElement.field.constraints = fieldSpec.constraints
                    }
                }
        ]

        if (!hasWith) {

            if (fieldSpec) {
                currentElement.field.name = field
                currentElement.domain = selectedDomain

                currentElement.field.label = fieldSpec.label
                currentElement.field.control = fieldSpec.control
                currentElement.field.constraints = fieldSpec.constraints
            }
        }

        this
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
     *  Loads a fixed value in a current element
     *
     * @param value
     * @return
     */
//    ETLProcessor with (String value) {
//        currentElement.value = value
//        currentElement = null
//        this
//    }
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

    def methodMissing (String methodName, args) {
        debugConsole.info "Method missing: ${methodName}, args: ${args}"
        throw ETLProcessorException.methodMissing(methodName, args)
    }

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

        Element addNewElement (String value) {
            Element element = new Element(originalValue: value, value: value, index: elements.size())
            addElement element
            element
        }

        Element getElement (Integer index) {
            elements[index]
        }

        List<Element> getLoadedElements (ETLDomain domain) {
            elements.findAll { it.domain == domain && it.isSelected() }
        }

        int size () {
            elements.size()
        }
    }

    static class Element {

        String originalValue
        String value
        Integer index
        ETLDomain domain
        List<ETLTransformation> transformations = []
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
