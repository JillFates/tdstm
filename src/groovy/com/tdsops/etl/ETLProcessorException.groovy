package com.tdsops.etl


/**
 *
 * ETLProcess exception used in DSL processing
 *
 */
class ETLProcessorException extends GroovyRuntimeException {

    ETLProcessorException (CharSequence message) {
        super(message)
    }

    static ETLProcessorException invalidDomain (String domain) {
        new ETLProcessorException("Invalid domain: '$domain'. It should be one of these values: ${ETLDomain.values()}")
    }

    static ETLProcessorException invalidSkipStep (Integer skip) {
        new ETLProcessorException("Incorrect skip step: $skip")
    }

    static ETLProcessorException notCurrentFindElement() {
        new ETLProcessorException('You need to define a find element first')
    }

    static ETLProcessorException unknownDomainFieldName(ETLDomain domain, String field) {
        new ETLProcessorException("The domain $domain does not have field name: $field")
    }

    static ETLProcessorException methodMissing (String method, args) {
        new ETLProcessorException("Unrecognized command $method with args $args")
    }

    static ETLProcessorException unrecognizedFindCriteria (String method) {
        //TODO: dcorrea Unrecognized find criteria operator [xxx] specified. Options are eq, ne, ...
        new ETLProcessorException("Unrecognized find criteria operator [$method] specified. Options are ${FindOperator.values()*.name()}")
    }

    static ETLProcessorException methodMissingInFindCommand (String method, args) {
        new ETLProcessorException("Unrecognized command $method with args $args for the find / elseFind command")
    }

    static ETLProcessorException parameterMissing (String parameter) {
        new ETLProcessorException("Unrecognized command $parameter")
    }

    static ETLProcessorException extractMissingColumn (String columnName) {
        new ETLProcessorException("Extracting a missing column name '$columnName'")
    }

    static ETLProcessorException missingColumn (String columnName) {
        new ETLProcessorException("Missing column name '$columnName'")
    }

    static ETLProcessorException extractInvalidColumn (Integer index) {
        new ETLProcessorException("Invalid column index: $index")
    }

    static ETLProcessorException unknownTransformation (String name) {
        new ETLProcessorException("Invalid transformation method '$name'")
    }

    static ETLProcessorException invalidConsoleStatus (String status) {
        new ETLProcessorException("Unknown console command option: $status")
    }

    static ETLProcessorException nonUniqueResults (List<String> fields) {
        new ETLProcessorException("The match was non-unique for fields ${fields}")
    }

    static ETLProcessorException incorrectAmountOfParameters (List<String> fields, List values) {
        new ETLProcessorException("Incorrect number of parameter for this command. Fields are:${fields?.size()}[${fields}] and values are:${values?.size()}[${values}]")
    }

    static ETLProcessorException nonProjectDefined () {
        new ETLProcessorException('No project selected in the user context')
    }

    static ETLProcessorException invalidRange (String message) {
        new ETLProcessorException(message)
    }

    static ETLProcessorException lookupFoundMultipleResults() {
        new ETLProcessorException('The lookup command found multiple results with the criteria')
    }

    static ETLProcessorException UnknownVariable (Object value) {
        new ETLProcessorException("Unknown variable: ${value}")
    }

    static ETLProcessorException unknownDataSetProperty (Object value) {
        new ETLProcessorException("Unknown dataSet property: ${value}")
    }

    static ETLProcessorException unknownDomainProperty (Object value) {
        new ETLProcessorException("Unknown DOMAIN property: ${value}")
    }

    static ETLProcessorException currentElementNotDefined () {
        new ETLProcessorException('CE (current element) is not defined.')
    }

    static ETLProcessorException invalidFindCommand (String dependentId) {
        new ETLProcessorException("Find commands need to have defined a previous column result with ${dependentId} value")
    }

    static ETLProcessorException invalidDomainPropertyName (ETLDomain domain, String fieldName) {
        new ETLProcessorException("$fieldName is not a domain property for ${domain.name()}")
    }

    static ETLProcessorException invalidDomainReference (ETLDomain domain, String fieldName) {
        new ETLProcessorException("$fieldName is not a domain reference for ${domain.name()}")
    }

    static ETLProcessorException invalidWhenFoundCommand(String fieldName) {
        new ETLProcessorException("Incorrect whenFound command. Use whenFound $fieldName update { .... }")
    }

    static ETLProcessorException whenNotFoundCommandWithoutCurrentFindElement(String fieldName) {
        new ETLProcessorException("Incorrect used: whenNotFound for field '$fieldName'. It must have defined a find command previously")
    }

    static ETLProcessorException whenFoundCommandWithoutCurrentFindElement(String fieldName) {
        new ETLProcessorException("Incorrect used: whenFound for field '$fieldName'. It must have defined a find command previously")
    }

    static ETLProcessorException invalidWhenNotFoundCommand(String fieldName) {
        new ETLProcessorException("Incorrect whenNotFound command. Use whenNotFound $fieldName create { .... }")
    }

	static ETLProcessorException incorrectFoundUseWithoutAssetClass() {
		new ETLProcessorException("Incorrect whenFound/whenNotFound command. It must define 'assetClass' first.")
	}

	static ETLProcessorException incorrectFoundUseWithoutArgValue(String fieldName) {
		new ETLProcessorException("Incorrect whenFound/whenNotFound command: $fieldName must be defined using arguments.")
	}

    static ETLProcessorException incorrectDomain (ETLDomain domain) {
        new ETLProcessorException("Cannot create a query for domain ${domain.name()}")
    }

	static ETLProcessorException incorrectFindingsMethodInvocation (String method) {
		new ETLProcessorException("You cannot use $method with more than one results in FINDINGS")
	}

	static ETLProcessorException unknownAssetControlType (String controlType) {
		new ETLProcessorException("Unknown AssetControlType: ${controlType}")
	}

    static ETLProcessorException invalidIgnoreCommand () {
        new ETLProcessorException('You cannot use ignore records in an empty results')
    }

    static ETLProcessorException invalidSheetCommand () {
        new ETLProcessorException('You cannot use the sheet command without a corresponding spreadsheet file')
    }

    static ETLProcessorException invalidSheetName (String sheetName) {
        new ETLProcessorException("Sheet '$sheetName' not found in workbook")
    }

    static ETLProcessorException invalidSheetNumber (Integer sheetNumber) {
        new ETLProcessorException("Sheet number $sheetNumber not found in workbook")
    }

    static ETLProcessorException invalidExcelDriver () {
        new ETLProcessorException('Use TDSExcelDriver for an instance of ExcelConnection in an ETL Script')
    }

    static ETLProcessorException invalidJSONDriver () {
        new ETLProcessorException('Use TDSJSONDriver for an instance of JSONConnection in an ETL Script')
    }

    static ETLProcessorException invalidRootNode (String rootNode) {
        new ETLProcessorException("Data was not found in JSON at rootNode '$rootNode'")
    }

    static ETLProcessorException invalidETLVariableName (String variableName) {
        new ETLProcessorException("Invalid variable name: ${variableName}. Variable names must end with 'Var'")
    }

    static ETLProcessorException missingPropertyException (String variableName) {
        return new ETLProcessorException("No such property: ${variableName}")
    }

    static ETLProcessorException invalidReadCommand () {
        return new ETLProcessorException("Incorrect use of 'read labels' command'")
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

	static ETLProcessorException incorrectFetchCommandUse() {
		new ETLProcessorException('Incorrect use of "fetch" command')
	}

    static ETLProcessorException incorrectFindCommandStructure() {
        new ETLProcessorException('Incorrect structure for find command')
    }

    static ETLProcessorException invalidSetParameter() {
        new ETLProcessorException("Invalid variable name specified for 'set' command. " +
                "Variable names must end in 'Var' and can not be reassigned within iterate loop.")
    }

    static ETLProcessorException incorrectWhenCommandStructure() {
        new ETLProcessorException("Invalid 'when' syntax. Options are 'when populated' or 'when { boolean expression }'")
    }
}

