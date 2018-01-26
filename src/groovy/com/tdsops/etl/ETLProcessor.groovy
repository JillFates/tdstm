package com.tdsops.etl

import com.tds.asset.AssetEntity
import net.transitionmanager.domain.Project

/**
 *
 *
 */
class ETLProcessor implements RangeChecker {

	/**
	 * Static variable name definition for CE script variable
	 */
	static final String CURR_ELEMENT_VARNAME = 'CE'
	/**
	 * Static variable name definition for DOMAIN script variable
	 */
	static final String DOMAIN_VARNAME = 'DOMAIN'
	/**
	 * Static variable name definition for SOURCE script variable
	 */
	static final String SOURCE_VARNAME = 'SOURCE'
	/**
	 * Project used in some commands.
	 */
	Project project
	/**
	 * DataSet wrapper used to mange commands sent to DataSet object.
	 * It wrapps a GETL Datasource instance.
	 * @see getl.data.Dataset
	 * @see getl.data.Field
	 */
	DataSetFacade dataSetFacade
	ETLFieldsValidator fieldsValidator
	ETLBinding binding
	ETLProcessorResult result

	Integer currentRowIndex = 0
	Integer currentColumnIndex = 0
	/**
	 * Current Element. Assigned and exposed in dsl scripts using 'CURR_ELEMENT_VARNAME' number
	 */
	Element currentElement

	DebugConsole debugConsole

	List<Column> columns = []
	Map<String, Column> columnsMap = [:]
	List<Row> rows = []
	Row currentRow

	ETLDomain selectedDomain
	ETLFindElement currentFindElement
	@Deprecated
	Map<ETLDomain, List<ReferenceResult>> results = [:]
	Map<ETLDomain, ReferenceResult> currentRowResult = [:]

	Set globalTransformers = [] as Set

	static Trimmer = { Element element ->
		element.trim()
	}

	static Sanitizer = { Element element ->
		element.sanitize()
	}

	static Replacer = { String regex, String replacement ->
		{ Element element ->
			element.replace(regex, replacement)
		}
	}

	/**
	 *
	 *  https://en.wikipedia.org/wiki/Control_character
	 */
	static ControlCharactersRegex = /\\0|\\a\\0\\b\\t\\n\\v\\f\\r/

	/**
	 * Creates an instance of ETL processor with a source of data,
	 * a domain mapper validator and an instance of fieldsValidator
	 * with a map of available transformations
	 * @param binding
	 * @param project
	 * @param dataSetFacade
	 * @param console
	 * @param fieldsValidator
	 */
	ETLProcessor (Project project, DataSetFacade dataSetFacade, DebugConsole console, ETLFieldsValidator fieldsValidator) {
		this.project = project
		this.dataSetFacade = dataSetFacade
		this.debugConsole = console
		this.fieldsValidator = fieldsValidator
		this.binding = new ETLBinding(this)
		this.result = new ETLProcessorResult(this)
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

		result.addCurrentSelectedDomain(selectedDomain)
		debugConsole.info("Selected Domain: $domain")
		this
	}

	/**
	 * Read Labels from source of data
	 * @param dataPart
	 * @return
	 */
	ETLProcessor read (String dataPart) {

		if ("labels".equalsIgnoreCase(dataPart)) {

			this.dataSetFacade.fields().eachWithIndex { getl.data.Field field, Integer index ->
				Column column = new Column(label: field.name, index: index)
				columns.add(column)
				columnsMap[column.label] = column
			}
			currentRowIndex++
			debugConsole.info "Reading labels ${columnsMap.values().collectEntries { [("${it.index}"): it.label] }}"
		}
		this
	}

	/**
	 * Iterate command from one row to another one using their position in the DataSet.
	 * <code>
	 *  from 1 to 3 iterate {*   ..........
	 *}*  <code>
	 * @param from
	 * @return a Map with the next steps in this command.
	 */
	def from (int from) {
		[to: { int to ->
			[iterate: { Closure closure ->
				from--
				to--
				List<Map> rows = this.dataSetFacade.rows()
				subListRangeCheck(from, to, rows.size())
				List subList = rows.subList(from, to)
				doIterate(subList, closure)
			}]
		}]
	}

	/**
	 * Iterates a given number of rows based on its ordinal position
	 * <code>
	 * from 1, 3, 5 iterate {*       ......
	 *}* </code>
	 * @param numbers an arrays of ordinal row numbers
	 * @return
	 */
	def from (int[] numbers) {

		[iterate: { Closure closure ->
			List rowNumbers = numbers as List
			List rows = this.dataSetFacade.rows()
			List subList = rowNumbers.collect { int number ->
				number--
				rangeCheck(number, rows.size())
				rows.get(number)
			}
			doIterate(subList, closure)
		}]
	}

	/**
	 * Iterates a list of rows applying a closure
	 * @param rows
	 * @param closure
	 * @return
	 */
	ETLProcessor doIterate (List rows, Closure closure) {

		rows.each { def row ->
			currentColumnIndex = 0
			binding.addDynamicVariable(SOURCE_VARNAME, new DataSetRowFacade(row))
			binding.addDynamicVariable(DOMAIN_VARNAME, new DomainFacade(result))
			closure(addCrudRowData(currentRowIndex, row))

			currentRowResult.each { ETLDomain key, ReferenceResult value ->
				if (!results.containsKey(key)) {
					results[key] = []
				}
				results[key].add(value)
			}
			currentRowResult = [:]
			currentRowIndex++
			binding.removeAllDynamicVariables()
		}

		currentRowIndex--
		this
	}

	/**
	 * Iterates and applies closure to every row in the dataSource
	 * @param closure
	 * @return
	 */
	ETLProcessor iterate (Closure closure) {
		doIterate(this.dataSetFacade.rows(), closure)
	}

	/**
	 * Sets Status console to on/off for allow/disallow log messages.
	 * @param status
	 * @return
	 */
	ETLProcessor console (String status) {

		DebugConsole.ConsoleStatus consoleStatus = DebugConsole.ConsoleStatus.values().find { it.name() == status }

		if (consoleStatus == null) {
			throw ETLProcessorException.invalidConsoleStatus(status)
		}
		debugConsole.status = consoleStatus
		debugConsole.info "Console status changed: $consoleStatus"
		this
	}

	/**
	 * Removes leading and trailing whitespace from a string.
	 * @param status
	 * @return the instance of ETLProcessor who received this message
	 */
	ETLProcessor trim (String status) {

		if (status == 'on') {
			globalTransformers.add(Trimmer)
		} else if (status == 'of') {
			globalTransformers.remove(Trimmer)
		}

		debugConsole.info "Global trim status changed: $status"
		this
	}

	/**
	 * Global sanitize global function
	 * @param status
	 * @return the instance of ETLProcessor who received this message
	 */
	ETLProcessor sanitize (String status) {

		if (status == 'on') {
			globalTransformers.add(Sanitizer)
		} else if (status == 'of') {
			globalTransformers.remove(Sanitizer)
		}

		debugConsole.info "Global sanitize status changed: $status"
		this
	}

	/**
	 * Global replace method given a regex and a replacement content
	 * @param regex
	 * @param replacement
	 * @return
	 */
	ETLProcessor replace (String regex, String replacement) {

		globalTransformers.add(Replacer(regex, replacement))
		debugConsole.info "Global replace regex: $regex wuth replacement: $replacement"
		this
	}

	/**
	 * Global Replace function
	 * @param control
	 * @return
	 */
	def replace (String control) {

		debugConsole.info "Global trm status changed: $control"
		if (control == 'ControlCharacters') {
			[
					with: { y ->
						globalTransformers.add(Replacer(ControlCharactersRegex, y))
					}
			]
		}
	}

	/**
	 * Skip a fixed amount of row for the iterate process
	 * @param amount
	 * @return
	 */
	ETLProcessor skip (Integer amount) {
		if (amount + currentRowIndex <= this.dataSetFacade.readRows()) {
			currentRowIndex += amount
		} else {
			throw ETLProcessorException.invalidSkipStep(amount)
		}
		this
	}

	/**
	 * Extracts an element from dataSource by its index in the row
	 * <code>
	 *     domain Application
	 *     iterate {
	 *          extract 1 load appName
	 *          extract 3 load description
	 *     }
	 * <code>
	 * @param index
	 * @return
	 */
	def extract (Integer index) {

		index--
		rangeCheck(index, currentRow.size())

		currentColumnIndex = index
		doExtract()
	}

	/**
	 * Extracts an element from dataSource by its column name
	 * <code>
	 *      domain Application
	 *      iterate {
	 *          extract 'column name' load appName
	 *      }
	 * <code>
	 * @param columnName
	 * @return
	 */
	def extract (String columnName) {

		if (!columnsMap.containsKey(columnName)) {
			throw ETLProcessorException.extractMissingColumn(columnName)
		}
		currentColumnIndex = columnsMap[columnName].index

		doExtract()
	}

	/**
	 * Loads field values in results. From an extracted value or just as a fixed new Element
	 * @param field
	 * @return
	 */
	def load (final String field) {

		if (currentElement) {
			currentElement.load(field)
		}
	}

	/**
	 * Set field values in results. From an extracted value or just as a fixed new Element
	 * @param field
	 * @return
	 */
	def set (final String field) {

		[
				with: { value ->

					Map<String, ?> fieldSpec = lookUpFieldSpecs(selectedDomain, field)

					Element newElement = currentRow.addNewElement(value, this)
					newElement.field.name = field
					newElement.domain = selectedDomain

					if (fieldSpec) {

						newElement.field.label = fieldSpec.label
						newElement.field.control = fieldSpec.control
						newElement.field.constraints = fieldSpec.constraints
					}

					addElementLoaded(selectedDomain, newElement)
					newElement
				}
		]
	}

	/**
	 * Create a Find object for a particular Domain instance.
	 * If the String is an invalid Domain, it throws an Exception.
	 * @param domain
	 * @return
	 */
	def find (String domain) {
        ETLDomain findDomain = ETLDomain.lookup(domain)
        if (findDomain == null) {
            throw ETLProcessorException.invalidDomain(findDomain)
        }

        debugConsole.info("find Domain: $findDomain")
		currentFindElement = new ETLFindElement(this, findDomain)
		return currentFindElement
	}

	/**
	 * Adds another find results in the current find element
	 * @param domain
	 */
	def elseFind(String domain) {
		ETLDomain findDomain = ETLDomain.lookup(domain)
		if (findDomain == null) {
			throw ETLProcessorException.invalidDomain(findDomain)
		}

		if (!currentFindElement) {
			throw ETLProcessorException.notCurrentFindElement()
		}

		return currentFindElement.elseFind(findDomain)
	}

	/**
	 * Add a message in console for an element from dataSource by its index in the row
	 * @param index
	 * @return
	 */
	def debug (Integer index) {

		if (index in (0..currentRow.size())) {
			currentColumnIndex = index
			doDebug currentRow.getElement(currentColumnIndex)
		} else {
			throw ETLProcessorException.missingColumn(index)
		}
	}

	/**
	 * Add a message in console for an element from dataSource by its column name
	 * @param columnName
	 * @return
	 */
	def debug (String columnName) {

		if (columnsMap.containsKey(columnName)) {
			currentColumnIndex = columnsMap[columnName].index
			doDebug currentRow.getElement(currentColumnIndex)
		} else {
			throw ETLProcessorException.missingColumn(columnName)
		}
	}

	/**
	 * It looks up the field Spec for Domain by fieldName
	 * @param domain
	 * @param fieldName
	 * @return
	 */
	Map<String, ?> lookUpFieldSpecs (ETLDomain domain, String field) {

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
	 * Adds a message debug with element content in console
	 * @param element
	 */
	private def doDebug (Element element) {
		debugConsole.debug "${[position: [element.columnIndex, element.rowIndex], value: element.value]}"
		element
	}

	/**
	 * Add a variable within the script as a dynamic variable.
	 *
	 * @param variableName
	 * @param element
	 */
	void addDynamicVariable (String variableName, Element element) {
		binding.addDynamicVariable(variableName, element)
	}

	/**
	 * Adds a new row in the list of rows
	 * @param rowIndex
	 * @param row
	 */
	private void addCrudRowData (Integer rowIndex, Map row) {
		currentRow = new Row(rowIndex, dataSetFacade.fields().collect { row[it.name] }, this)
		rows.add(currentRow)
		currentRow
	}

	/**
	 * Private method that executes extract method command internally.
	 * @return
	 */
	private def doExtract () {
		Element element = currentRow.getElement(currentColumnIndex)

		addCurrentElementToBinding(element)

		debugConsole.info "Extract element: ${element.value} by column index: ${currentColumnIndex}"
		applyGlobalTransformations(element)
		element
	}

	/**
	 * Adds a loaded element with the current domain in results.
	 * It also removes CE (currentElement) from script context.
	 * @param domain
	 * @param element
	 */
	void addElementLoaded (ETLDomain domain, Element element) {

		result.loadElement(element)

		if (!currentRowResult.containsKey(selectedDomain)) {
			currentRowResult[selectedDomain] = new ReferenceResult()
		}

		currentRowResult[selectedDomain].elements.add([
				originalValue: element.originalValue,
				value        : element.value,
				field        : [
						name       : element.field.name,
						control    : element.field.control,
						label      : element.field.label,
						constraints: element.field.constraints
				]
		])

		debugConsole.info "Adding element ${element} in results for domain ${domain}"
	}

	/**
	 * Adds an asset entity instance referenced from a datasource field
	 * @param assetEntity
	 * @param row
	 */
	void addAssetEntityReferenced (AssetEntity assetEntity) {

		if (!currentRowResult.containsKey(selectedDomain)) {
			currentRowResult[selectedDomain] = new ReferenceResult()
		}

		// Add the Asset ID number to the reference list if it isn't already there
		if (!currentRowResult[selectedDomain].reference.contains(assetEntity.id)) {
			currentRowResult[selectedDomain].reference << assetEntity.id
		}
	}

	/**
	 * And an entry for a finding command results.
	 * @param findElement
	 */
	void addFindElement(ETLFindElement findElement) {
		result.addFindElement(findElement)
	}
	/**
	 * And an entry in the Dependency results.
	 * @param findElement
	 */
	void addDependencyWarnMessage(ETLFindElement findElement) {
		//result.addDependency(findElement)
	}
	/**
	 * Applies a global transformation for a given element
	 * @param element
	 */
	void applyGlobalTransformations (Element element) {

		globalTransformers.each { transformer ->
			transformer(element)
		}
	}

	/**
	 * Add an Element as CURR_ELEMENT_VARNAME value as variable within the binding script context.
	 * @param element a selected Element
	 * @return the curent element selected in ETLProcessor
	 */
	private void addCurrentElementToBinding (Element element) {
		currentElement = element
		binding.setVariable(CURR_ELEMENT_VARNAME, currentElement)
		currentElement
	}

	/**
	 * Validates calls within the DSL script that can not be managed
	 * @param methodName
	 * @param args
	 */
	def methodMissing (String methodName, args) {
		debugConsole.info "Method missing: ${methodName}, args: ${args}"
		throw ETLProcessorException.methodMissing(methodName, args)
	}

	ETLDomain getSelectedDomain () {
		selectedDomain
	}

	Column column (String columnName) {
		columnsMap[columnName]
	}

	Column column (Integer columnName) {
		columns[columnName]
	}

	Set getColumnNames () {
		columnsMap.keySet()
	}

	Row getCurrentRow () {
		currentRow
	}

	Row getRow (Integer index) {
		rows[index]
	}

	Element getCurrentElement () {
		currentElement
	}

	Element getElement (Integer rowIndex, Integer columnIndex) {
		rows[rowIndex].getElement(columnIndex)
	}


	List<String> getAvailableMethods () {
		['domain', 'read', 'iterate', 'console', 'skip', 'extract', 'load', 'reference',
		 'with', 'on', 'labels', 'transform with', 'translate', 'debug', 'translate',
		 'uppercase()', 'lowercase()', 'first(content)', 'last(content)', 'all(content)',
		 'left(amount)', 'right(amount)', 'replace(regex, replacement)']
	}

	List<String> getAssetFields () {
		['id', 'assetName', 'moveBundle']
	}

}
