package com.tdsops.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.marshall.AnnotationDrivenObjectMarshaller
import com.tdsops.tm.enums.domain.ImportOperationEnum
import grails.converters.JSON
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.etl.ETLStreamingWriter
import net.transitionmanager.imports.ScriptProcessorService

/**
 * Results collected from an ETL Processor instance processing an ETL script.
 * It prepares the results used in the import process or for rendering results in the UI.
 * <br>
 * Every part of the results are covered in formatter functions.
 */
class ETLProcessorResult {

    static final Integer CURRENT_VERSION = 2

    static final String ETL_FILENAME_EXTENSION = 'json'
    /**
     * ETL Processor used to collect results in a ETL Procesor Result instance.
     */
    ETLProcessor processor
    /**
     * Defines JSON version for the import the process.
     */
    Integer version = CURRENT_VERSION
    /**
     * ETL info map details
     */
    Map<String, ?> ETLInfo

    /**
     * Current reference for the domain instance and its contents
     */
    DomainResult reference
    /**
     * Collection of results with their data fields map
     */
    List<DomainResult> domains = (List<DomainResult>) []

    /**
     * Debug Console content filed used to create the final result
     */
    String consoleLog = ''

    FileSystemService fileSystemService

    String outputFilename

    OutputStream outputStream

    /**
     * Defines if lookup
     */
    Boolean isLookupEnable = false

    ETLProcessorResult(ETLProcessor processor) {
        this.processor = processor
        this.ETLInfo = [
                originalFilename: processor.getFilename()
        ]

        this.fileSystemService = ApplicationContextHolder.getBean('fileSystemService', FileSystemService)
        List tmpFile = this.fileSystemService.createTemporaryFile(ScriptProcessorService.PROCESSED_FILE_PREFIX, ETL_FILENAME_EXTENSION)
        this.outputFilename = tmpFile[0]
        this.outputStream = (OutputStream) tmpFile[1]
    }

    /**
     * Defines when an ETL script was finished in its evaluation step.
     * @see ETLProcessor#evaluate(java.lang.String)
     */
    void scriptFinished() {
        for (DomainResult domain in this.domains) {
            domain.finish()
        }
    }

    /**
     * Adds dataScriptId in ETLInfo map for results
     * @param dataScriptId an id of an instance of DataScript
     */
    void addDataScriptIdInETLInfo(Long dataScriptId) {
        this.ETLInfo.dataScriptId = dataScriptId
    }

    /**
     * Used to allow the script to use the 'lookup' command. This will cause the ETL process to retain
     * the processed records in memory and stream them to disk at the end of the process.
     */
    void enableLookup() {
        this.isLookupEnable = true
    }

    /**
     * Adds a new json entry in results list
     * @param domain
     */
    void addCurrentSelectedDomain(ETLDomain domain) {

        reference = domains.find { it.domain == domain.name() }
        if (!reference) {
            String filename = outputFilename.replace(".${ETL_FILENAME_EXTENSION}", "_${domain.name()}.$ETL_FILENAME_EXTENSION")
            ETLStreamingWriter writer = new ETLStreamingWriter(fileSystemService.createTemporaryFileWithGivenName(filename))

            if (isLookupEnable) {
                reference = new DomainInMemory(domain.name(), filename, writer)
            } else {
                reference = new DomainOnDisk(domain.name(), filename, writer)
            }
            domains.add(reference)
        }
    }

    /**
     * Appends a loaded element in the results.
     * First It adds a new element.field.name in the current domain fields list
     * and if it already exits, updates that element with the element originalValue and value.
     * After that, It saves the new element in the data results.
     * @param element an instance of Element
     */
    @CompileStatic
    void loadElement(Element element) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        reference.addFieldName(element)
        currentRow.addLoadElement(element)
    }

    @CompileStatic
    void loadElements(List<Element> elements) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        for (Element element in elements) {
            reference.addFieldName(element)
            currentRow.addLoadElement(element)
        }
    }

    /**
     * Appends a loaded initialized element in the results.
     * First It adds a new element.field.name in the current domain fields list
     * and if it already exits, updates that element with the element init value.
     * After that, It saves the new element in the data results.
     * @param element an instance of Element
     */
    void loadInitializedElement(Element element) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        reference.addFieldName(element)
        currentRow.addInitElement(element)
    }

    /**
     * Adds a find Element to the ETL Processor result.
     * It needs to collect results and errors.
     * Errors are located at the field level or at the row level.
     * Results are added using find query results
     * @see ETLFindElement#currentFind
     * @param findElement is an instance of ETLFindElement
     * 			used to calculate a query data, results and errors
     */
    void addFindElement(ETLFindElement findElement) {
        RowResult currentRow = findOrCreateCurrentRow()
        reference.addFieldName(findElement)
        currentRow.addFindElement(findElement)
    }

    /**
     * Removes an element instance value current results
     * @param element ans instance of {@code Element}
     */
    void removeElement(Element element) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.removeElement(element.fieldDefinition.name)
    }

    /**
     * Add a FoundElement in the result based on its property
     * <pre>
     * 		whenFound asset create {* 			assetClass Application
     * 			assetName primaryName
     * 			assetType primaryType
     * 			"SN Last Seen": NOW
     *}* </pre>
     * @param foundElement
     */
    void addFoundElement(FoundElement foundElement) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        currentRow.addFoundElement(foundElement)
    }

    /**
     * It adds a wanr message in the result instance
     * <pre>
     * 		find Application 'for' id by id with SOURCE.'application id'
     * 		elseFind Application 'for' id by appVendor with DOMAIN.appVendor warn 'found without asset id field'
     * <pre>
     * @param findElement
     */
    void addFindWarnMessage(ETLFindElement findElement) {
        if (findElement.currentFind.objects) {
            RowResult currentRow = findOrCreateCurrentRow()
            currentRow.addFindElementWarnMessage(findElement)
        }
    }

    /**
     * After completing a load 'comments' command, this methods adds results in {@code ETLProcessorResult}
     * <pre>
     * 		extract 'column.name' load 'comments'
     * 		....
     * 		load 'comments' with myVar
     * </pre>
     * @param commentElement an instance of {@code CommentElement}
     */
    void addComments(CommentElement commentElement) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        currentRow.addComments(commentElement)
    }

    /**
     * Adds a single tag to an asset if not already associated
     * to the current row.
     * <pre>
     * 	tagAdd 'Code Blue'
     * </pre>
     * @param tag a String tag name
     */
    void addTag(String tag) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        currentRow.addTag(tag)
    }

    /**
     * Removes a single tag from an asset if associated
     * to the current row.
     * <pre>
     * 	tagRemove 'Code Blue'
     * </pre>
     * @param tag a String tag name
     */
    void removeTag(String tag) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        currentRow.removeTag(tag)
    }

    /**
     * Replaces one tag with another tag on an asset if associated
     * to the current row
     * <pre>
     * 	tagReplace 'a', 'b'
     * </pre>
     * @param currentTag a Tag name to be replaced
     * @param newTag a new Tag for replacing
     */
    void replaceTag(String currentTag, String newTag) {
        RowResult currentRow = findOrCreateCurrentRow()
        currentRow.ignore = false
        currentRow.replaceTag(currentTag, newTag)
    }

    /**
     * Restart result index for the next row to be processed in an ETL script iteration
     */
    void startRow() {
        //resultIndex = -1
    }

    /**
     * Mark the end of a row cleaning up the ignored result.
     */
    void endRow() {
        for (DomainResult domain in domains) {
            domain.endCurrentRow()
        }
    }

    /**
     * Remove the current row from results going back the previous row results.
     */
    void ignoreCurrentRow() {
        if (reference.currentRow) {
            reference.currentRow.ignore = true
        }
    }
    /**
     * <p>Find the current row in ETLProcessorResult#reference</p>
     * If ETLProcessorResult#resultIndex is equals -1,
     * then a new instance of RowResult is created and added in
     * ETLProcessorResult#reference#data
     *
     * @return and instance of RowResult
     */
    @CompileStatic
    RowResult findOrCreateCurrentRow() {

        if (!reference.currentRow) {
            reference.currentRow = new RowResult(
                    fieldsValidator: processor.fieldsValidator,
                    rowNum: processor.currentRowIndex,
                    domain: reference.domain)
        }
        return reference.currentRow // data[resultIndex]
    }

    /**
     * Return current row using resultIndex value
     * @return an instance of RowResult
     * @see RowResult* @see ETLProcessorResult#resultIndex
     */
    @CompileStatic
    RowResult currentRow() {
        return reference.currentRow
    }

    /**
     * Return the value for a field name using the current row in the JSON results
     * @param fieldNameOrLabel a name or label for a domain field.
     * @return an object with value content
     */
    @CompileStatic
    Object getFieldValue(String fieldNameOrLabel) {
        if (reference.currentRow) {
            FieldResult fieldResult = reference.currentRow.getField(fieldNameOrLabel)
            return fieldResult.value

        } else {
            throw ETLProcessorException.domainOnlyAllowOnNewRows()
        }
    }
    /**
     * Returns true if {@code ETLProcessorResult}, in the current {@code RowResult}
     * contains a column with columnName parameter
     *
     * @param columnName
     * @return
     */
    Boolean hasColumn(String columnName) {
        if (!reference.currentRow) {
            throw ETLProcessorException.domainOnlyAllowOnNewRows()
        }
        return reference.currentRow.hasField(columnName)
    }

    /**
     * Used to render the ETLProcessorResult instance as a Map object that will contain the following:
     * 		ETLInfo <Map>
     * 		domains <List><Map>
     *      consoleLog <String> (optional)
     * It also adds the label map used during an ETL script execution based on field name and field label
     * @param includeConsoleLog - flag if console data should be returned (default false)
     * @return A map of this object
     * @see ETLFieldsValidator#fieldLabelMapForResults()
     */
    Map<String, ?> toMap(Boolean includeConsoleLog = false) {

        Map<String, Map<String, String>> map = processor.fieldsValidator.fieldLabelMapForResults()

        domains.each { DomainResult domainResult ->
            if (map?.containsKey(domainResult.domain)) {
                domainResult.setFieldLabelMap(map[domainResult.domain])
            }
        }

        Map results = [
                ETLInfo: this.ETLInfo,
                domains: this.domains
        ]

        if (includeConsoleLog) {
            results.put('consoleLog', this.processor.debugConsole.content())
        }

        return results
    }

    void addFieldLabelMapInResults(Map<String, Map<String, String>> map) {
        domains.each { DomainResult domainResult ->
            if (map?.containsKey(domainResult.domain)) {
                domainResult.setFieldLabelMap(map[domainResult.domain])
            }
        }
    }
    /**
     * Look up a field name that contain a value equals to the value and if found then the current
     * result reference will be move back to a previously processed row for the domain. This is done
     * by setting the rowFoundInLookup to the data row reference in the in the results.
     *
     * If the object is not found then future references to current result should be referring to the
     * last row in the data result.
     *
     * For example the following command:
     * <pre>
     *  iterate {*    .....
     *    lookup appVendor with 'Microsoft'
     *    .....
     *    if(LOOKUP) {*        load custom1 with 'App Vendor Found'
     *}*}*
     * </pre>
     * <pre>
     *  "fields": {*     "appVendor": {*          "value": "Microsoft",
     *          .....
     *}*}*     "custom1": {*         "value": "App Vendor Found",
     *         "originalValue": "App Vendor Found",
     *         .....
     *}*}* </pre>
     * If the value is found, then it is used in the following commands during the iteration
     * @param fieldNames - A list of the field elements to examine for a match
     * @param values - A list of the values to compare the fields against
     * @return true if the data row was found otherwise false
     */
    // boolean lookupInReference(String fieldName, String value) {
    // 	// TODO : JPM 3/2018 : lookupInReference will have issues if there are multiple matches so we should look to expand the search to multiple fields/values
    // 	Integer lookupPosition = reference.data.findIndexOf { RowResult dataRow ->
    // 		dataRow.fields.containsKey(fieldName) && dataRow.fields[fieldName]?.value == value
    // 	}
    // 	if(lookupPosition >= 0){
    // 		resultIndex = lookupPosition
    // 		return true
    // 	} else {
    // 		return false
    // 	}
    // }
    boolean lookupInReference(List<String> fieldNames, List<Object> valuesFromETL) {

        // First go through the list of values and pluck out value from an ETL variable or other types
        List<Object> values = valuesFromETL.collect { ETLValueHelper.valueOf(it) }

        // This closure will iterate through all of the rows and return the indexes of those
        // rows that match all of the fields specified in the 'lookup' command. String evaluations
        // will be done with case-insensitivity.
        int rowNum = 0
        List<Integer> positions = reference.data.findIndexValues { RowResult row ->
            boolean matched = true
            int i = 0
            rowNum++
            for (String fname in fieldNames) {
                if (row.fields.containsKey(fname)) {
                    Object rowValue = row.fields[fname].value

                    // Determine if the value is a String
                    if (rowValue instanceof CharSequence && values[i] instanceof CharSequence) {
                        matched = rowValue.equalsIgnoreCase(values[i].toString())
                    } else {
                        matched = rowValue == values[i]
                    }
                } else {
                    // Missing the field so no match
                    matched = false
                }
                if (!matched) {
                    break
                }
                i++
            }
            return matched
        }

        int size = positions.size()
        if (size > 1) {
            throw ETLProcessorException.lookupFoundMultipleResults()
        } else if (size == 1) {
            /**
             * Once the lookup command found a coincidence,
             * It is necessary first, to save in current row with
             * all its fields. After that, we can change the pointer of
             * current row to a found RowResult.
             */
            this.reference.endCurrentRow()
            Integer position = positions[0]
            this.reference.currentRow = (RowResult) this.reference.data[position]
        }

        return (size == 1)
    }

    /**
     * Register an instance of AnnotationDrivenObjectMarshaller for ETLProcessorResult
     */
    static void registerObjectMarshaller() {
        JSON.registerObjectMarshaller(new AnnotationDrivenObjectMarshaller<JSON>())
    }
}

/**
 * <pre>
 *  "domains": { //
 *    "domain": "Device",
 *    "fieldNames": [
 *      "assetName",
 *      "externalRefId"
 *    ],
 * 	  "fieldLabelMap": { //
 * 	    "asset": "asset",
 *      "dependent": "dependent",
 *      "c1": "c1"
 *     //}, //
 *    "data": [ list of RowResult instances]
 *  //}//
 *  </pre>
 */
@CompileStatic
abstract class DomainResult {

    /**
     * An instance of {@code DomainResult} is represented as list of {@RowResult}
     * that are defined by a {@code ETLDomain}. This field saves that value as String.
     */
    String domain
    /**
     * Filename of DomainResult saved on disk using a Streaming solution.
     * @see net.transitionmanager.etl.ETLStreamingWriter#writeDataArray(java.util.List)
     */
    String outputFilename
    /**
     * Writer of {@link RowResult} on Disk.
     */
    ETLStreamingWriter writer
    /**
     * <p>Saves a list of fields used during a ETL script executions for this particular domain</p>
     * <pre>
     *  ...,
     *  "fieldNames": [
     *     "asset",
     *     "dependent",
     *     "c1"
     *  ],
     *  ....
     * </pre>
     */
    Set fieldNames
    /**
     * <p>This map is used to have a map between field label and field name used in an ETL processor results</p>
     * <pre>
     *  ....
     *  "fieldLabelMap": {//
     *      "asset": "asset",
     *      "dependent": "dependent",
     *      "c1": "c1"
     *  //},
     *  ....
     * <pre>
     */
    Map<String, String> fieldLabelMap
    /**
     * During an iteration of an ETL script,
     * each {@link RowResult} collected is pointed
     * to this variable.
     * After finishing each iteration, it is saved
     * in memory {@link DomainInMemory} or on disk
     * {@link DomainOnDisk}
     */
    RowResult currentRow

    DomainResult(String domain, String outputFilename, ETLStreamingWriter writer) {
        this.domain = domain
        this.outputFilename = outputFilename
        this.writer = writer
        this.fieldNames = [] as Set
        this.fieldLabelMap = [:]
    }

    /**
     * In an iterate command, at the end of each row process,
     * ETLProcessor invokes this method to complete progress
     * saving data in disk or collecting them in memory.
     */
    abstract void endCurrentRow()
    /**
     * Finishes the ETL script evaluation.
     * This method is necessary to cleanup resources.
     */
    abstract void finish()
    /**
     * Defines amount of {@code DomainResult#data} list. It used in serialized step
     * using streaming solution.
     */
    Integer dataSize = 0
    /**
     * Returns data collected during an ETL script evaluation.
     *
     * @return a List of results. It could be a List of {@link RowResult}
     *      or a List<Map>.
     * @see DomainInMemory#getData()
     * @see DomainOnDisk#getData()
     */
    abstract List<Object> getData()
    /**
     * Add the field name for an instance of Element
     * to the fieldNames Set property
     * @param element an instance of Element ETL class
     */
    void addFieldName(Element element) {
        fieldNames.add(element.fieldDefinition.name)
    }
    /**
     * Add the field name for an instance of ETLFindElement
     * into the fieldNames Set property
     * @param findElement
     */
    void addFieldName(ETLFindElement findElement) {
        fieldNames.add(findElement.currentFind.property)
    }

    /**
     * Assign a label map collected during an ETL script execution
     * @param fieldLabelMap a Map instance that contains the relation between field name and field label
     */
    void setFieldLabelMap(Map<String, String> fieldLabelMap) {
        this.fieldLabelMap = fieldLabelMap
    }

}
/**
 * <p>{@link DomainResult} class extension. It serialized ETL results
 * on FileSystem using an instance of {@link ETLStreamingWriter}.</p>
 */
@CompileStatic
class DomainOnDisk extends DomainResult {

    /**
     * A list of data saved in disk and collected.
     */
    List<Object> data

    DomainOnDisk(String domain, String outputFilename, ETLStreamingWriter writer) {
        super(domain, outputFilename, writer)
        this.writer.startDataArray()
    }

    /**
     * Completes the process of an Iterate execution.
     * For this particular implementation, it is necessary
     * to finish the array of data in disk and finally,
     * flush all the content on disk.
     *
     * @see ETLProcessorResult#scriptFinished()
     */
    void finish() {
        this.writer.endDataArray()
        this.writer.close()
    }

    /**
     * At the end of each row, iterate command is calling this method
     * for collecting a row. For an instance of {@link DomainOnDisk}
     * it also save results in {@link DomainOnDisk#writer}
     */
    void endCurrentRow() {
        if (currentRow && !currentRow.ignore) {
            this.dataSize++
            this.writer.writeRowResult(currentRow)
        }
        currentRow = null
    }
    /**
     * Returns data collected during an ETL script evaluation.
     *
     * @return a List of results. It could be a List of {@link RowResult}
     *      or a List<Map>.
     */
    List<Object> getData() {
        if (!data) {
            FileSystemService fileSystemService = ApplicationContextHolder.getBean('fileSystemService', FileSystemService)
            String fileContent = fileSystemService.openTempFile(outputFilename).text
            data = (new JsonSlurper().parse(fileContent.getBytes())) as List
        }
        return data
    }
}
/**
 * <p>This class extends {@link DomainResult} collecting {@link RowResult}
 * in {@link DomainInMemory#rows} field.</p>
 * <p>Once an ETL script enable lookup command, {@link ETLProcessorResult}
 * collects {@link RowResult} in a list in memory instead of flushing each row on disk
 * as {@link DomainOnDisk} implementation.</p>
 * @see ETLProcessor#enable(org.codehaus.groovy.runtime.MethodClosure)
 */
class DomainInMemory extends DomainResult {

    /**
     * Collection of {@link RowResult} saved in memory
     * during an ETL script evaluation.
     */
    List<RowResult> rows

    DomainInMemory(String domain, String outputFilename, ETLStreamingWriter writer) {
        super(domain, outputFilename, writer)
        this.rows = []
    }

    /**
     * <p>This method adds {@link DomainResult#currentRow} in
     * {@link DomainInMemory#rows} List.</p>
     * There are 3 possible scenarios where current row
     * should not be added in {@link DomainInMemory#rows} List:
     * <p>1) If {@link DomainResult#currentRow} is null.
     * It only happends in lookup command scenario.</p>
     * <p>2) If the {@link DomainResult#currentRow} needs to be ignored.</p>
     * <p>3)If {@link DomainResult#currentRow} was already added in
     * {@link DomainInMemory#rows} List.</p>
     */
    void endCurrentRow() {
        if (currentRow
                && !currentRow.ignore
                && (rows.size() <= (currentRow.rowNum - 1))
        ) {
            this.dataSize++
            this.rows.add(currentRow)
        }
        currentRow = null
    }

    /**
     * After {@link ETLProcessor#evaluate(java.lang.String)} method,
     * an instance of {@link DomainInMemory} saves all rows collected
     * in {@link DomainInMemory#rows} using Streaming solution.
     *
     * @see ETLProcessorResult#scriptFinished()
     * @see DomainResult#finish()
     */
    void finish() {
        this.writer.startDataArray()
        for (RowResult row in rows) {
            this.writer.writeRowResult(row)
        }
        this.writer.endDataArray()
        this.writer.close()
    }

    /**
     * Returns all the {@link RowResult} collected
     * in an ETL script execution with lookup command enabled.
     *
     * @return a List of {@link RowResult}
     */
    @Override
    List<Object> getData() {
        return rows.toList()
    }
}

/**
 * Init a row data map.
 * <pre>
 * 	"data": [
 *      //{ //
 *          op": "I",
 * 		    "errorCount": 0,
 * 		    "warn": true,
 * 		    "duplicate": true,
 * 		    "errors": [],
 * 		    "fields": {}//
 * 		  //}//
 * </pre>
 */
@CompileStatic
class RowResult {

    String op = ImportOperationEnum.INSERT
    Integer rowNum
    Integer errorCount = 0
    Boolean warn = false
    Boolean duplicate = false
    List<String> errors = []
    Boolean ignore = true
    Map<String, FieldResult> fields = [:]
    String domain
    ETLFieldsValidator fieldsValidator
    List<String> comments = []
    TagResults tags

    /**
     * Add element to the current row data
     * @param element
     */
    @CompileStatic
    void addLoadElement(Element element) {
        FieldResult fieldData = findOrCreateFieldData(element.fieldDefinition)
        fieldData.addLoadElement(element)
        this.errorCount = fieldData.errors.size() + this.errors.size()
    }

    /**
     * Add initialized element to the current row data
     * @param element
     */
    void addInitElement(Element element) {
        FieldResult fieldData = findOrCreateFieldData(element.fieldDefinition)
        fieldData.init = element.init
    }
    /**
     * It adds the find result in the FieldResult
     * <pre>
     *  "data": {*    "warn":true,
     *    "errors": ["found with wrong asset class"],
     *
     *    "asset": {*    	....
     * 		"warn":true,
     * 		"errors": ["found with wrong asset class"],
     * 		    ....
     *}*}* </pre>
     * @param findElement the find element with the warn message
     */
    void addFindElement(ETLFindElement findElement) {
        FieldResult fieldData = findOrCreateFieldData(findElement.currentFind.fieldDefinition)
        fieldData.addFindElement(findElement)

        if (fieldData.find.results.isEmpty()) {
            this.op = ImportOperationEnum.INSERT
        } else if (fieldData.find.results.size() == 1) {
            this.op = ImportOperationEnum.UPDATE
        } else {
            this.op = ImportOperationEnum.TBD
        }

        this.errorCount = fieldData.errors.size()
    }

    /**
     * Returns <tt>true</tt> if {@code RowResult#fields} Map contains a mapping for the specified
     * fieldName.
     * @param fieldName a String field Name
     * @return true if {@code RowResult#fields} Map fieldName key
     */
    Boolean containsKey(String fieldName) {
        return fields.containsKey(fieldName)
    }

    /**
     * Used to store a FoundElement into the ELT Result
     * @param foundElement
     */
    void addFoundElement(FoundElement foundElement) {
        FieldResult fieldData = findOrCreateFieldData(foundElement.fieldDefinition)
        fieldData.addFoundElement(foundElement)
    }

    /**
     * It adds the warn message result in the FieldResult
     * <pre>
     *  "data": {*    "warn":true,
     *    "errors": ["found with wrong asset class"],
     *
     *    "asset": {*    	....
     * 		"warn":true,
     * 		"errors": ["found with wrong asset class"],
     * 		    ....
     *}*}* </pre>
     * @param findElement the find element with the warn message
     */
    void addFindElementWarnMessage(ETLFindElement findElement) {
        warn = true
        errors.add(findElement.warnMessage)
        FieldResult fieldData = findOrCreateFieldData(findElement.currentFind.fieldDefinition)
        fieldData.addFindElementWarnMessage(findElement)
    }

    /**
     * Removes an element instance value current results
     * @param fieldName a field name used in {@code RowResul#fields}
     */
    void removeElement(String fieldName) {
        if (fields.containsKey(fieldName)) {
            fields.remove(fieldName)
        }
    }

    /**
     * Find or creates a Field Data content based onf fieldName parameter
     * @param element
     * @return
     */
    @CompileStatic
    FieldResult findOrCreateFieldData(ETLFieldDefinition fieldDefinition) {
        if (!fields.containsKey(fieldDefinition.name)) {
            fields[fieldDefinition.name] = new FieldResult(fieldOrder: fields.size(), fieldDefinition: fieldDefinition)
        }
        return fields[fieldDefinition.name]
    }

    /**
     * Return {@code FieldResult} based on field name or label
     * @param fieldNameOrLabel a name or label for a domain field.
     * @return an instance of {@code FieldResult}
     */
    FieldResult getField(String fieldNameOrLabel) {

        Map<String, String> fieldLabelMap = fieldsValidator.labelFieldMap[domain]
        String fieldName = fieldLabelMap.containsKey(fieldNameOrLabel) ? fieldLabelMap[fieldNameOrLabel] : fieldNameOrLabel

        if (!fields.containsKey(fieldName)) {
            throw ETLProcessorException.unknownDomainProperty(fieldName)
        }

        return fields[fieldName]
    }
    /**
     * Returns <tt>true</tt> if {@code RowResult#fields} map contains a mapping for the specified
     * field name.  More formally, returns <tt>true</tt> if and only if
     * this map contains a mapping for a key
     *
     * @param fieldName a field name
     * @return true or false if {@code RowResult}
     * 			contains a field name or not.
     */
    Boolean hasField(String fieldName) {
        return fields.containsKey(fieldName)
    }

    /**
     * Adds a new comment content in {@code RowResult#comments} field.
     * @param commentElement an instance of {@code CommentElement}
     */
    void addComments(CommentElement commentElement) {
        this.comments.add(commentElement.commentText)
    }

    /**
     * Adds a new comment content in {@code RowResult#tags} field.
     * <pre>
     * 	tagAdd 'Code Blue'
     * </pre>
     * @param tag
     */
    void addTag(String tag) {
        getTagResults().add(tag)
    }

    /**
     * Removes a single tag from an asset if associated
     * to the current row.
     * <pre>
     * 	tagRemove 'Code Blue'
     * </pre>
     * @param tag a String tag name
     */
    void removeTag(String tag) {
        getTagResults().remove(tag)
    }

    /**
     * Replaces one tag with another tag on an asset if associated
     * <pre>
     * 	tagReplace 'a', 'b'
     * </pre>
     * @param currentTag a Tag name to be replaced
     * @param newTag a new Tag for replacing
     */
    void replaceTag(String currentTag, String newTag) {
        getTagResults().replace(currentTag, newTag)
    }
    /**
     * Lazy initialization for {@code RowResult#tags}
     * field.
     * @return an instance of {@code TagResults}
     */
    private TagResults getTagResults() {
        if (!tags) {
            tags = new TagResults()
        }
        return tags
    }
}

/**
 * Prepares some of the fields result in the data result.
 * <pre>
 * 	"asset": {* 	    "value": "",
 * 	    "originalValue": "",
 * 	    "init": "Default Value"
 * 		"warn":true,
 * 		"warnMsg": "found with wrong asset class",
 * 		"duplicate": true,
 * 			"find": {* 				"query": [
 *{"domain": "Application", "kv": {"id": null}},
 *{"domain": "Application", "kv": { "assetName": "CommGen", "assetType": "Application" }},
 *{"domain": "Application", "kv": { "assetName": "CommGen" }},
 *{"domain": "Asset", "kv": { "assetName": "CommGen" }, "warn": true}* 				],
 * 				"matchOn": 2,
 * 				"results": [12312,123123,123123123]
 *},
 *}* </pre>
 */
@CompileStatic
class FieldResult {
    ETLFieldDefinition fieldDefinition
    Object originalValue
    Object value
    Object init
    /**
     * Define order in which fields are created during an ETL script execution
     * <pre>
     *  domain Device
     *  iterate {*  	extract 1 load 'Name'
     *  	extract 2 load 'Description'
     *}* </pre>
     * Then {@code RowResult.data} will be added using this order:
     * <pre>
     *  rowResult.data.put('assetName', new FieldResult(fieldOrder: 0, value:..))
     *  rowResult.data.put('description', new FieldResult(fieldOrder: 1, value:..))
     * </pre>
     */
    Integer fieldOrder
    List<String> errors = []
    Boolean warn = false
    FindResult find = new FindResult()
    Map<String, Object> create
    Map<String, Object> update

    /**
     * Add errors list in JSON results
     * @param errors
     */
    private void addErrors(List<String> errors) {
        if (errors) {
            this.errors.addAll(errors)
        }
    }

    /**
     * Set field result values obtained from Element
     */
    void addLoadElement(Element element) {
        this.value = element.value
        this.originalValue = element.originalValue
        this.addErrors(element.errors)
    }

    /**
     * Prepares the query data Map in the ETLProcessorResult
     * <pre>
     * 	"query": [
     *{* 			"domain": "Application",
     * 			"kv": {"id": null},
     * 	    	"errors" : ["Named parameter [id] value may not be null"]
     *},
     * 	]
     * </pre>
     * @param findElement
     * @return
     */
    void addFindElement(ETLFindElement findElement) {
        this.addErrors((List<String>) findElement.currentFind.errors)
        this.find.addQueryAndResults(findElement)
    }

    /**
     * Add find wran message in JSON results
     * @param findElement
     */
    void addFindElementWarnMessage(ETLFindElement findElement) {
        warn = true
        errors.add(findElement.warnMessage)
    }

    /**
     * Add FoundElement map fields in JSON results
     * @param foundElement
     */
    void addFoundElement(FoundElement foundElement) {
        if (foundElement.getAction() == FoundElement.FoundElementType.create) {
            create = foundElement.propertiesMap
        } else {
            update = foundElement.propertiesMap
        }
    }
}

@CompileStatic
class FindResult {

    List<QueryResult> query = []
    List<Long> results = []
    Integer size = 0
    Integer matchOn

    /**
     * It prepares query map results with the domain.data
     * following the current format:
     * <pre>
     *  [
     * 		"matchOn": 2,
     * 		"results": [
     * 			115123,
     * 			115123,
     * 			115123
     * 		]
     * 	]
     * </pre>
     * Results are the id of the domain classes collected by the find command
     * and matcOn defines the ordinal position
     * in the query object list where those results where found.
     * @param fieldDataMap a field data map
     * @param findElement the find element with the warn message
     */
    private void addResults(ETLFindElement findElement) {
        if (!this.results && findElement.results) {
            this.results = findElement.results.objects.collect { it as Long }
            this.size = this.results.size()
            this.matchOn = findElement.results.matchOn as Integer
        }
    }
    /**
     * Add Query content in JSON result
     * @param findElement
     */
    private void addQuery(ETLFindElement findElement) {
        query.add(
                new QueryResult(
                        domain: findElement.currentDomain.name(),
                        criteria: (List<Map<String, Object>>) findElement.currentFind.statement.conditions.collect { FindCondition condition ->
                            return [
                                    propertyName: condition.propertyName,
                                    operator    : condition.operator.name(),
                                    value       : condition.value
                            ]
                        }
                )
        )
    }

    /**
     * Add query and Results in JSON result
     * @param findElement
     */
    void addQueryAndResults(ETLFindElement findElement) {
        addQuery(findElement)
        addResults(findElement)
    }
}
/**
 * Prepares the query data Map in the ETLProcessorResult
 * <pre>
 * 	"query": [
 *{* 			"domain": "Application",
 * 			"criteria": [
 *{"propertyName": "assetName", "operator": "eq","value": "zulu01"},
 *{"propertyName": "priority", "operator": "gt","value": 2},
 * 			],
 * 	    	"error" : "Named parameter [id] value may not be null"
 *},
 * 	]
 * </pre>
 * @param findElement
 * @return
 */
@CompileStatic
class QueryResult {

    String domain
    List<Map<String, Object>> criteria = []

    @Override
    String toString() {
        return "QueryResult{"
                .concat("domain=")
                .concat(domain)
                .concat(", kv=")
                .concat(criteria.toString())
                .concat('}')

    }
}

/*
 * JSON representation for tags added in an ETL script.
 * <pre>
 * {
 * 	"fields": { ... },
 * 	"tags" : {
 * 		"add": ["Code Blue", "FUBAR", "SNAFU"],
 *  	"remove": ["Blah", "xyzzy"],
 *  	"replace": {"a":"b", "e":"f", "c":"d", "g":"h"}
 *  }
 * }
 * </pre>
 * This class manages all the ETL tag commands.
 * <pre>
 *     tagAdd 'Code Blue'
 *     tagAdd 'FUBAR','SNAFU'
 *
 *     tagRemove 'Blah'
 *     tagRemove 'Blah','xyzzy'
 *
 *     tagReplace 'a', 'b'
 *     tagReplace 'e':'f'
 *     tagReplace 'c':'d', 'g':'h'
 * </pre>
 * @see com.tdsops.etl.ETLProcessor#tagAdd(java.lang.String)
 * @see com.tdsops.etl.ETLProcessor#tagRemove(java.lang.String)
 * @see com.tdsops.etl.ETLProcessor#tagReplace(java.lang.String, java.lang.String)
 */

class TagResults {

    Set<String> add = [] as Set
    Set<String> remove = [] as Set
    Map<String, String> replace = [:]

    void add(String tagName) {
        add.add(tagName)
    }

    void remove(String tagName) {
        remove.add(tagName)
    }

    void replace(String currentTag, String newTag) {
        replace[currentTag] = newTag
    }

}
