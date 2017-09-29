package com.tdsops.etl


class ETLProcessor {

    List<List<String>> crudData = []
    Integer currentRowPosition = 0
    Integer currentColumnPosition = 0
    String currentFieldValue
    DebugConsole debugConsole
    DomainAssetFieldsMapper domainAssetFieldsMapper
    Map metadata = [:]

    ETLProcessor() {
        this([], new DebugConsole(buffer: new StringBuffer()), new DomainAssetFieldsMapper())
    }

    ETLProcessor(List<List<String>> data, DomainAssetFieldsMapper domainAssetFieldsMapper) {
        this(data, new DebugConsole(buffer: new StringBuffer()), domainAssetFieldsMapper)
    }

    ETLProcessor(List<List<String>> data, DebugConsole console, DomainAssetFieldsMapper domainAssetFieldsMapper) {
        this.crudData = data
        this.debugConsole = console
        this.domainAssetFieldsMapper = domainAssetFieldsMapper
        this.metadata = [domain: null, columns: [names: [:], ordinals: [:]], rows: []]
    }

    ETLProcessor domain(DomainAssets aDomain) {
        metadata.domain = aDomain
        debugConsole.info("Selected Domain: $aDomain")
        this
    }

    ETLProcessor read(DataPart dataPart) {

        if (dataPart == DataPart.labels) {
            debugConsole.info("Reading labels")

            crudData.get(currentRowPosition++).eachWithIndex { String columnName, Integer index ->
                metadata.columns.names[columnName] = [ordinal: index]
                metadata.columns.ordinals[index] = [name: columnName]
            }
        }
        this
    }

    List<List<String>> rows() {
        crudData[1..(crudData.size() - 1)]
    }

    ETLProcessor iterate(Closure closure) {
        closure.delegate = this
        crudData[currentRowPosition..(crudData.size() - 1)].each {
            metadata.rows[currentRowPosition] = [:]
            closure(it)
            currentRowPosition++
            currentColumnPosition = 0
        }
        this
    }

    ETLProcessor console(ConsoleStatus status) {
        debugConsole.status = status

        debugConsole.info "Console status changed: $status"
        this
    }

    ETLProcessor extract(Integer index) {
        currentColumnPosition = index
        currentFieldValue = crudData[currentRowPosition][currentColumnPosition]

        debugConsole.info "Current field value: $currentFieldValue"
        this
    }

    ETLProcessor extract(String columnName) {
        currentColumnPosition = metadata.columns.names[columnName].ordinal
        currentFieldValue = crudData[currentRowPosition][currentColumnPosition]

        debugConsole.info "Current field value: $currentFieldValue"
        this
    }

    ETLProcessor transform(StringTransformation transformation) {
        String oldValue = currentFieldValue
        currentFieldValue = transformation.apply(currentFieldValue)
        crudData[currentRowPosition][currentColumnPosition] = currentFieldValue

        debugConsole.info "Transformation $oldValue -> $currentFieldValue"
        this
    }

    ETLProcessor translate(Map map) {

        if(map.containsKey(currentFieldValue)){
            String oldValue = currentFieldValue

            currentFieldValue = map[currentFieldValue]
            crudData[currentRowPosition][currentColumnPosition] = currentFieldValue

            debugConsole.info "Translate $oldValue -> $currentFieldValue"
        } else {
            debugConsole.warn "Could not translate $currentFieldValue"
        }
        this
    }




    ETLProcessor load(String assetProperty) {
        //Map<String, ?> fieldSpec = domainAssetFieldsMapper.field(metadata.domain, assetProperty)

        metadata.rows[currentRowPosition][assetProperty] = currentFieldValue
        this
    }

    ETLProcessor into(String assetProperty) {

        this
    }

//    def methodMissing(String methodName, args) {
//        debugConsole.info "Method missing: ${methodName}, args: ${args}"
//
//    }

    DomainAssets getSelectedDomain() {
        metadata.domain
    }

    Map column(String columnName) {
        metadata.columns.names[columnName]
    }

    Set getColumnNames() {
        metadata.columns.names.keySet()
    }

    Map<String, ?> getTransformationResult() {

        [data: [
                domain   : metadata.domain?.name(),
                instances: metadata.rows?.findAll { !!it }
        ]]
    }

    List<?> getTableHeaders() {
        metadata.rows.find {!!it}?.keySet() as List
    }

    List<List<?>> getTableRows() {
        metadata.rows.findAll{!!it}.collect{it.values()}
    }

}
