package com.tdsops.etl

class ETLProcessor {

    DomainAssets selectedDomain
    List<List<String>> crudData = []
    Integer currentRowPosition = 0
    Integer currentColumnPosition = 0
    String currentFieldValue

    Map<String, Integer> labelMap

    ETLProcessor domain(DomainAssets aDomain) {
        selectedDomain = aDomain
        this
    }

    ETLProcessor read(DataPart dataPart) {

        if (dataPart == DataPart.labels) {
            labelMap = [:]

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

    def iterate(Closure closure) {
        closure.delegate = this
        crudData[currentRowPosition..(crudData.size() - 1)].each {
            closure(it)
            currentRowPosition++
            currentColumnPosition = 0
        }
        this
    }

    def extract(Integer index) {
        currentColumnPosition = index
        currentFieldValue = crudData[currentRowPosition][currentColumnPosition]
        this
    }

    def extract(String columnName) {
        currentColumnPosition = labelMap[columnName]
        currentFieldValue = crudData[currentRowPosition][currentColumnPosition]
        this
    }

    def transform(StringTransformation transformation ){
        currentFieldValue = transformation.apply(currentFieldValue)
        crudData[currentRowPosition][currentColumnPosition] = currentFieldValue
        this
    }

    def uppercase = {

    }

    def lowercase = {

    }

    def methodMissing(String methodName, args) {

        println methodName
    }


}
