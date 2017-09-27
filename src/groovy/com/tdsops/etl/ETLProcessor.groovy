package com.tdsops.etl

class ETLProcessor {

    DomainAssets selectedDomain
    List<List<String>> crudData = []
    Integer currentRowPosition = 0
    Integer currentColumnPosition = 0
    String currentFieldValue
    DebugConsole debugConsole = new DebugConsole(buffer: new StringBuffer())


    Map<String, Integer> labelMap

    ETLProcessor domain(DomainAssets aDomain) {
        selectedDomain = aDomain
        debugConsole.info("Selected Domain: $aDomain")
        this
    }

    ETLProcessor read(DataPart dataPart) {

        if (dataPart == DataPart.labels) {
            labelMap = [:]
            debugConsole.info("Reading labels")

            crudData.get(0).eachWithIndex { String columnName, Integer index ->
                labelMap[columnName] = index
            }

            currentRowPosition = 1
        }
        this
    }

    List<List<String>> rows(){
        crudData[1..(crudData.size() - 1)]
    }

    ETLProcessor iterate(Closure closure) {
        closure.delegate = this
        crudData[currentRowPosition..(crudData.size() - 1)].each {
            closure(it)
            currentRowPosition++
            currentColumnPosition = 0
        }
        this
    }

    ETLProcessor console(ConsoleStatus status){
        debugConsole.status = status
        this
    }

    ETLProcessor extract(Integer index) {
        currentColumnPosition = index
        currentFieldValue = crudData[currentRowPosition][currentColumnPosition]
        debugConsole.info "Current field value: $currentFieldValue"
        this
    }

    ETLProcessor extract(String columnName) {
        currentColumnPosition = labelMap[columnName]
        currentFieldValue = crudData[currentRowPosition][currentColumnPosition]
        debugConsole.info "Current field value: $currentFieldValue"
        this
    }

    ETLProcessor transform(StringTransformation transformation ){
        String oldValue =  currentFieldValue
        currentFieldValue = transformation.apply(currentFieldValue)
        crudData[currentRowPosition][currentColumnPosition] = currentFieldValue
        debugConsole.info "Transformation $oldValue -> $currentFieldValue"
        this
    }


    def methodMissing(String methodName, args) {

        println methodName
    }


}
