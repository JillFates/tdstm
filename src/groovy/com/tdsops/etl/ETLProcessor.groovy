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

    List<ETLProcessor.Column> columns = []
    Map<String, ETLProcessor.Column> columnsMap = [:]
    List<ETLProcessor.Row> rows = []
    ETLProcessor.Row currentRow

    ETLDomain selectedDomain
    Map<ETLDomain, List<List<Map<String, ?>>>> results = [:]
    Map<ETLDomain, List<List<Map<String, ?>>>> currentRowResult = [:]

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
                ETLProcessor.Column column = new ETLProcessor.Column(label: columnName, index: index)
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
            closure(addCrudRowData  (currentRowIndex, crudRowData))

            currentRowResult.each { ETLDomain key, List<List<Map<String, ?>>> value ->
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

    private void addCrudRowData (Integer rowIndex, List<String> crudRowData) {
        currentRow = new Row(rowIndex, crudRowData, this)
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

                    Element newELement = currentRow.addNewElement(value, this)
                    newELement.field.name = field
                    newELement.domain = selectedDomain

                    if (fieldSpec) {
                        newELement.field.label = fieldSpec.label
                        newELement.field.control = fieldSpec.control
                        newELement.field.constraints = fieldSpec.constraints
                    }

                    addLoadedElement(selectedDomain, newELement)
                }
        ]
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
     * Add a loaded element with the current domain in results
     *
     */
    void addLoadedElement (ETLDomain domain, ETLProcessor.Element element) {

        if (!currentRowResult.containsKey(selectedDomain)) {
            currentRowResult[selectedDomain] = []
        }

        currentRowResult[selectedDomain].add([
                originalValue: element.originalValue,
                value        : element.value,
                field        : element.field
        ])

        debugConsole.info "Adding element ${element} in results for domain ${domain}"
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


    def resultAsTable () {


        List<String> headers = columns.collect { ETLProcessor.Column column -> column.label }
        List<List<String>> rows = rows.collect { ETLProcessor.Row row ->
            row.elements.collect { ETLProcessor.Element element -> element.value }
        }

    }


    static class Column {

        String label
        Integer index
    }

    static class Row {

        List<Element> elements
        Integer index

        Row () {
            elements = []
        }

        Row (Integer index, List<String> values, ETLProcessor processor) {
            this.index = index
            this.elements = values.withIndex().collect { String value, int i ->
                new Element(
                        originalValue: value,
                        value: value,
                        rowIndex: index,
                        columnIndex: i,
                        processor: processor)
            }
        }

        Element addNewElement (String value, ETLProcessor processor) {
            Element newElement = new Element(originalValue: value,
                    value: value,
                    rowIndex: index,
                    columnIndex: elements.size(),
                    processor: processor)
            elements.add(newElement)
            newElement
        }

        Element getElement (Integer index) {
            elements[index]
        }

        int size () {
            elements.size()
        }
    }

    static class Element {

        String originalValue
        String value
        Integer rowIndex
        Integer columnIndex
        ETLDomain domain
        ETLProcessor processor

        Field field = new Field()
        /**
         *
         *
         * @param transformationName
         * @return
         */
        Element and (String transformationName) {
            transform(transformationName)
        }
        /**
         *
         *
         * @param transformationName
         * @return
         */
        Element transform (String transformationName) {
            ETLTransformation transformation = lookupTransformation(transformationName)
            transformation.apply(this)
            processor.debugConsole.info "Applying transformation on element: $this"
            this
        }
        /**
         *
         *
         * @param actions
         * @return
         */
        Element translate (Map actions) {
            if (actions.containsKey('with')) {
                translateWith(actions.get('with'))
            }
            this
        }
        /**
         *
         *
         * @param field
         * @return
         */
        Element load (String fieldName) {

            //TODO: Diego. Review this interaction
            Map<String, ?> fieldSpec = processor.lookUpFieldSpecs(processor.selectedDomain, fieldName)

            if (fieldSpec) {
                field.name = fieldName
                domain = processor.selectedDomain

                field.label = fieldSpec.label
                field.control = fieldSpec.control
                field.constraints = fieldSpec.constraints
            }
            processor.addLoadedElement(processor.selectedDomain, this)
            this
        }
        /**
         *
         *
         *
         * @param methodName
         * @param args
         */
        //TODO: Review it. "methodMissing" implementations are not supported on static inner classes as a synthetic version of "methodMissing" is added during compilation for the purpose of outer class delegation.
//        def methodMissing (String methodName, args) {
//            processor.debugConsole.info "Method missing: ${methodName}, args: ${args}"
//            throw ETLProcessorException.methodMissing(methodName, args)
//        }

        /** Private Methods. Non public API for ETL processor */

        private ETLTransformation lookupTransformation (String name) {
            if (processor.transformations
                    && processor.transformations.containsKey(name)) {
                processor.transformations[name]
            } else {
                processor.debugConsole.error "Unknown transformation: $name"
                throw ETLProcessorException.unknownTransformation(name)
            }
        }

        private Element translateWith (Map dictionary) {

            if (dictionary.containsKey(value)) {
                String oldValue = value
                value = dictionary[value]

                processor.debugConsole.info "Translate $oldValue -> ${value}"
            } else {
                processor.debugConsole.warn "Could not translate ${value}"
            }
            this
        }
    }

    static class Field {

        String name = ""
        String control
        String label
        Map constraints
    }
}
