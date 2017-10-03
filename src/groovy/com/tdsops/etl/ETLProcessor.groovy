package com.tdsops.etl

/**
 *
 *
 */
class ETLProcessor {

    List<List<String>> crudData = []
    Integer startIteration = 0
    Integer currentRowPosition = 0
    Integer currentColumnPosition = 0

    DebugConsole debugConsole
    ETLFieldsMapper domainAssetFieldsMapper

    DomainAssets selectedDomain
    List<ETLProcessor.Column> columns = []
    Map<String, ETLProcessor.Column> columnsMap = [:]
    List<ETLProcessor.Row> rows = []
    ETLProcessor.Row currentRow
    //ETLProcessor.Cell currentCell
    List<Map<String, ?>> results = []


    ETLProcessor() {
        this([], new DebugConsole(buffer: new StringBuffer()), new ETLFieldsMapper())
    }

    ETLProcessor(List<List<String>> data, ETLFieldsMapper domainAssetFieldsMapper) {
        this(data, new DebugConsole(buffer: new StringBuffer()), domainAssetFieldsMapper)
    }

    ETLProcessor(List<List<String>> data, DebugConsole console, ETLFieldsMapper domainAssetFieldsMapper) {
        this.crudData = data
        this.debugConsole = console
        this.domainAssetFieldsMapper = domainAssetFieldsMapper
    }

    ETLProcessor domain(DomainAssets domain) {
        selectedDomain = domain
        debugConsole.info("Selected Domain: $domain")
        this
    }

    ETLProcessor domain(String domain) {
        selectedDomain = DomainAssets.values().find { it.name() == domain } ?: DomainAssets.External
        debugConsole.info("Selected Domain: $domain")
        this
    }

    ETLProcessor read(DataPart dataPart) {

        if (dataPart == DataPart.labels) {
            debugConsole.info "Reading labels"

            crudData.get(currentRowPosition++).eachWithIndex { String columnName, Integer index ->
                ETLProcessor.Column column = new ETLProcessor.Column(label: columnName, index: index)
                columns.add(column)
                columnsMap[column.label] = column
            }
        }
        this
    }

    List<List<String>> rows() {
        crudData[1..(crudData.size() - 1)]
    }

    ETLProcessor iterate(Closure closure) {
        startIteration = currentRowPosition

        crudData[currentRowPosition..(crudData.size() - 1)].each { List<String> crudRowData ->
            currentColumnPosition = 0
            closure(addCrudRowData(crudRowData))
            currentRowPosition++
        }
        currentRowPosition--
        this
    }

    private void addCrudRowData(List<String> crudRowData) {
        currentRow = new Row(crudRowData, debugConsole)
        rows.add(currentRow)
        currentRow
    }

    ETLProcessor console(ConsoleStatus status) {
        debugConsole.status = status
        debugConsole.info "Console status changed: $status"
        this
    }

    ETLProcessor.Cell extract(Integer index) {
        currentColumnPosition = index
        doExtract()
    }

    ETLProcessor.Cell extract(String columnName) {
        currentColumnPosition = columnsMap[columnName].index
        doExtract()
    }

    private ETLProcessor.Cell doExtract() {
        ETLProcessor.Cell cell = currentRow.getCell(currentColumnPosition)
        debugConsole.info "currentColumnPosition${cell.value}"
        cell
    }

    ETLProcessor skip(Integer amount) {
        if (amount + currentRowPosition <= crudData.size()) {
            currentRowPosition += amount
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
    ETLProcessor transform(StringTransformation transformation) {
        currentRow.getCell(currentColumnPosition).transform(transformation)
        this
    }
    /**
     *
     * Delegate translate methods using actions to maintenance method's chain
     *
     * @param actions
     * @return
     */
    ETLProcessor translate(Map actions) {
        currentRow.getCell(currentColumnPosition).translate(actions)
        this
    }
    /**
     *
     * Delegate load methods to maintenance method's chain
     *
     * @param fieldProperty
     * @return
     */
    ETLProcessor load(String fieldProperty) {
        currentRow.getCell(currentColumnPosition).load(fieldProperty)

        this
    }
    /**
     *
     * Saves current domain class and rows already processed as a partial result
     *
     */
    void save() {
        debugConsole.info "Saving results for domain ${selectedDomain}"

        results.add([
                domain: selectedDomain.name(),
                rows  : alreadyProcessedRows()
        ])
    }
    /**
     *
     * Collects and formats already processed rows to be used as a ETL process result.
     *
     * @return
     */
    private List<Map<String, ?>> alreadyProcessedRows() {
        List<ETLProcessor.Row> alreadyProcessed = rows.subList(startIteration, currentRowPosition)

        startIteration = currentRowPosition
        alreadyProcessed.collect { ETLProcessor.Row row ->
            row.cells.collect { ETLProcessor.Cell cell ->
                [
                        originalValue: cell.originalValue,
                        value        : cell.value,
                        field        : [
                                name: ""
                        ]
                ]
            }
        }
    }

//    def methodMissing(String methodName, args) {
//        debugConsole.info "Method missing: ${methodName}, args: ${args}"
//
//    }

    DomainAssets getSelectedDomain() {
        selectedDomain
    }

    ETLProcessor.Column column(String columnName) {
        columnsMap[columnName]
    }

    ETLProcessor.Column column(Integer columnName) {
        columns[columnName]
    }

    Set getColumnNames() {
        columnsMap.keySet()
    }

    Map<String, ?> getTransformationResult() {

        [data: [
                domain   : selectedDomain?.name(),
                instances: rows?.findAll { !!it }
        ]]
    }

    ETLProcessor.Row getCurrentRow() {
        currentRow
    }

    ETLProcessor.Row getRow(Integer index) {
        rows[index]
    }

    static class Column {

        String label
        Integer index
    }

    static class Row {

        List<Cell> cells

        Row() {
            cells = []
        }

        Row(List<String> values, DebugConsole console) {
            cells = []
            values.eachWithIndex { String value, int i ->
                addCell new Cell(originalValue: value, value: value, index: i, debugConsole: console)
            }
        }

        void addCell(Cell cell) {
            cells.add(cell)
        }

        Cell getCell(Integer index) {
            cells[index]
        }
    }

    static class Cell {

        String originalValue
        String value
        Integer index
        List<StringTransformation> transformations = []
        DebugConsole debugConsole
        Map field = [name: ""]

        ETLProcessor.Cell transform(StringTransformation transformation) {
            value = transformation.apply(value)
            transformations.add(transformation)
            this
        }

        ETLProcessor.Cell translate(Map actions) {

            if (actions.containsKey('with')) {
                Map map = actions.get 'with'
                if (map.containsKey(value)) {
                    String oldValue = value
                    value = map[value]
                    debugConsole.info "Translate $oldValue -> $value"
                } else {
                    debugConsole.warn "Could not translate $value"
                }
            }
            this
        }

        ETLProcessor.Cell load(String fieldProperty) {
            //Map<String, ?> fieldSpec = domainAssetFieldsMapper.field(selectedDomain, assetProperty)
            field.name = fieldProperty
            this
        }
    }
}
