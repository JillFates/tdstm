package com.tdsops.etl

/**
 *
 * ETLProcess exception used in DSL processing
 *
 */
class ETLProcessorException extends GroovyRuntimeException {

    ETLProcessorException (String message) {
        super(message)
    }

    static ETLProcessorException invalidDomain (String domain) {
        new ETLProcessorException("Invalid domain: '$domain'. It should be one of these values: ${ETLDomain.values()}".toString())
    }

    static ETLProcessorException invalidSkipStep (Integer skip) {
        new ETLProcessorException("Incorrect skip step: $skip".toString())
    }

    static ETLProcessorException invalidCommand (String message) {
        new ETLProcessorException(message)
    }

    static ETLProcessorException unknownDomainFieldsSpec (ETLDomain domain) {
        new ETLProcessorException("There is not validator for domain $domain".toString())
    }

    static ETLProcessorException domainWithoutFieldsSpec (ETLDomain domain, String field) {
        new ETLProcessorException("The domain $domain does not have specifications for field: $field".toString())
    }

    static ETLProcessorException methodMissing (String method, args) {
        new ETLProcessorException("Unrecognized command $method with args $args".toString())
    }

    static ETLProcessorException extractMissingColumn (String columnName) {
        new ETLProcessorException("Missing column name '$columnName'".toString())
    }

    static ETLProcessorException extractInvalidColumn (Integer index) {
        new ETLProcessorException("Invalid column index: $index".toString())
    }

    static ETLProcessorException unknownTransformation (String name) {
        new ETLProcessorException("Unknown transformation: $name".toString())
    }

    static ETLProcessorException invalidConsoleStatus (String status) {
        new ETLProcessorException("Unknown console command option: $status".toString())
    }

    static ETLProcessorException nonUniqueResults (List<String> fields) {
        new ETLProcessorException("The match was non-unique for fields ${fields}".toString())
    }

    static ETLProcessorException incorrectAmountOfParameters (List<String> fields, List values) {
        new ETLProcessorException("Incorrect amount of parameter for this command. Fields are:${fields?.size()}[${fields}] and values are:${values?.size()}[${values}]".toString())
    }

}
