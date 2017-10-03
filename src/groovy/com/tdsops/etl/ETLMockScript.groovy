package com.tdsops.etl

abstract class ETLMockScript extends Script {

    Integer currentRowPosition = 0
    Integer currentColumnPosition = 0
    String currentFieldValue

    ETLMockScript domain(ETLDomain domain) {
        getMetadata().domain = domain
        getDebugConsole().info("Selected Domain: $domain")
        this
    }

    ETLMockScript domain(String domain) {
        getMetadata().domain = ETLDomain.values().find { it.name() == domain } ?: ETLDomain.External
        getDebugConsole().info("Selected Domain: $domain")
        this
    }

    ETLProcessor read(DataPart dataPart) {

        if (dataPart == DataPart.labels) {
            getDebugConsole().info("Reading labels")

            getDatasource().get(currentRowPosition++).eachWithIndex { String columnName, Integer index ->
                getMetadata().columns.names[columnName] = [ordinal: index]
                getMetadata().columns.ordinals[index] = [name: columnName]
            }
        }
        this
    }

    private DebugConsole getDebugConsole() {
        if (!getBinding().hasVariable(DebugConsole.class.name)) {
            getBinding().setVariable(DebugConsole.class.name, new DebugConsole(buffer: new StringBuffer()))
        }
        getBinding().getVariable(DebugConsole.class.name)
    }

    private Map getMetadata() {
        if (!getBinding().hasVariable('metadata')) {
            getBinding().setVariable('metadata',
                    [domain: null, columns: [names: [:], ordinals: [:]], rows: []])
        }
        getBinding().getVariable('metadata')
    }

    private List<List<String>> getDatasource() {
        if (!getBinding().hasVariable('datasource')) {
            getBinding().setVariable('datasource', [])
        }
        getBinding().getVariable('datasource')
    }


}
