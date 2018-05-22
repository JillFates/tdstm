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

    static ETLProcessorException notCurrentFindElement() {
        new ETLProcessorException('You need to define a find element first')
    }

    static ETLProcessorException unknownDomainFieldsSpec (ETLDomain domain, String field) {
        new ETLProcessorException("There is not validator for domain $domain and field $field".toString())
    }

    static ETLProcessorException domainWithoutFieldsSpec (ETLDomain domain, String field) {
        new ETLProcessorException("The domain $domain does not have specifications for field: $field".toString())
    }

    static ETLProcessorException methodMissing (String method, args) {
        new ETLProcessorException("Unrecognized command $method with args $args".toString())
    }

    static ETLProcessorException methodMissingInFindCommand (String method, args) {
        new ETLProcessorException("Unrecognized command $method with args $args for the find / elseFind command".toString())
    }

    static ETLProcessorException parameterMissing (String parameter) {
        new ETLProcessorException("Unrecognized command $parameter".toString())
    }

    static ETLProcessorException extractMissingColumn (String columnName) {
        new ETLProcessorException("Extracting a missing column name '$columnName'".toString())
    }

    static ETLProcessorException missingColumn (String columnName) {
        new ETLProcessorException("Missing column name '$columnName'".toString())
    }

    static ETLProcessorException extractInvalidColumn (Integer index) {
        new ETLProcessorException("Invalid column index: $index".toString())
    }

    static ETLProcessorException unknownTransformation (String name) {
        new ETLProcessorException("Invalid transformation method '$name'".toString())
    }

    static ETLProcessorException invalidConsoleStatus (String status) {
        new ETLProcessorException("Unknown console command option: $status".toString())
    }

    static ETLProcessorException nonUniqueResults (List<String> fields) {
        new ETLProcessorException("The match was non-unique for fields ${fields}".toString())
    }

    static ETLProcessorException incorrectAmountOfParameters (List<String> fields, List values) {
        new ETLProcessorException("Incorrect number of parameter for this command. Fields are:${fields?.size()}[${fields}] and values are:${values?.size()}[${values}]".toString())
    }

    static ETLProcessorException nonProjectDefined () {
        new ETLProcessorException('No project selected in the user context')
    }

    static ETLProcessorException invalidRange (String message) {
        new ETLProcessorException(message)
    }

    static ETLProcessorException UnknownVariable (Object value) {
        new ETLProcessorException("Unknown variable: ${value}".toString())
    }

    static ETLProcessorException unknownDataSetProperty (Object value) {
        new ETLProcessorException("Unknown dataSet property: ${value}".toString())
    }

    static ETLProcessorException unknownDomainProperty (Object value) {
        new ETLProcessorException("Unknown DOMAIN property: ${value}".toString())
    }

    static ETLProcessorException currentElementNotDefined () {
        new ETLProcessorException('CE (current element) is not defined.')
    }

    static ETLProcessorException invalidFindCommand (String dependentId) {
        new ETLProcessorException("Find commands need to have defined a previous column result with ${dependentId} value".toString())
    }

    static ETLProcessorException invalidDomainPropertyName (ETLDomain domain, String fieldName) {
        new ETLProcessorException("$fieldName is not a domain property for ${domain.name()}".toString())
    }

    static ETLProcessorException invalidDomainReference (ETLDomain domain, String fieldName) {
        new ETLProcessorException("$fieldName is not a domain reference for ${domain.name()}".toString())
    }

    static ETLProcessorException invalidWhenFoundCommand(String fieldName) {
        new ETLProcessorException("Incorrect whenFound command. Use whenFound $fieldName update { .... }".toString())
    }

    static ETLProcessorException invalidWhenNotFoundCommand(String fieldName) {
        new ETLProcessorException("Incorrect whenNotFound command. Use whenNotFound $fieldName create { .... }".toString())
    }

    static ETLProcessorException incorrectDomain (ETLDomain domain) {
        new ETLProcessorException("Cannot create a query for domain ${domain.name()}".toString())
    }

	static ETLProcessorException incorrectFindingsMethodInvocation (String method) {
		new ETLProcessorException("You cannot use $method with more than one results in FINDINGS".toString())
	}

	static ETLProcessorException unknownAssetControlType (String controlType) {
		new ETLProcessorException("Unknown AssetControlType: ${controlType}".toString())
	}

    static ETLProcessorException invalidIgnoreCommand () {
        new ETLProcessorException('You cannot use ignore rows in an empty results')
    }

    static ETLProcessorException invalidSheetCommand () {
        new ETLProcessorException('You cannot sheet command without an spreadsheet file')
    }

    static ETLProcessorException invalidSheetName (String sheetName) {
        new ETLProcessorException("Sheet '$sheetName' not found in workbook".toString())
    }

    static ETLProcessorException invalidSheetNumber (Integer sheetNumber) {
        new ETLProcessorException("Sheet number $sheetNumber not found in workbook".toString())
    }

    static ETLProcessorException invalidExcelDriver () {
        new ETLProcessorException('Use TDSExcelDriver for an instance of ExcelConnection in an ETL Script')
    }

    static ETLProcessorException invalidETLVariableName (String variableName) {
        new ETLProcessorException("Invalid variable name: ${variableName}. Variable names must end with 'Var'".toString())
    }

    static final String missingPropertyExceptionMessage = "No such property: variableName"
	static ETLProcessorException missingPropertyException (String variableName) {
		return new ETLProcessorException(missingPropertyExceptionMessage.replace('variableName', variableName))
	}

	static final String invalidReadCommandMessage = "Incorrect use of 'read labels' command'"
	static ETLProcessorException invalidReadCommand () {
		return new ETLProcessorException(invalidReadCommandMessage)
	}

    /**
     * Exception being thrown when no domain is specified upon load clause
     * @return
     */
    static ETLProcessorException domainMustBeSpecified () {
        new ETLProcessorException('A \'domain Class\' must be specified before any load or find commands')
    }

    /**
     * Exception being thrown domain command is incorrectly used
     * @return
     */
    static ETLProcessorException invalidDomainComand() {
        new ETLProcessorException('Invalid class specified for domain command. Usage: domain Classname')
    }

    static ETLProcessorException invalidReplaceCommand() {
        new ETLProcessorException("Use 'replace on/off' command")
    }

    static ETLProcessorException invalidUseOfMethod( String methodName, Object value ) {
        new ETLProcessorException("${methodName} function only supported for String values (${value} : ${value.class})")
    }

    static ETLProcessorException ignoreOnlyAllowOnNewRows() {
        new ETLProcessorException('ignore command only when processing new rows')
    }

    static ETLProcessorException domainOnlyAllowOnNewRows() {
        new ETLProcessorException('DOMAIN variable only available after load commands')
    }

}

