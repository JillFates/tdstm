package com.tdsops.etl

import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.GormUtil
import getl.data.Field
import groovy.transform.TimedInterrupt
import net.transitionmanager.domain.Project
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.messages.SyntaxErrorMessage

import static org.codehaus.groovy.syntax.Types.COMPARE_EQUAL
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN
import static org.codehaus.groovy.syntax.Types.COMPARE_GREATER_THAN_EQUAL
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN
import static org.codehaus.groovy.syntax.Types.COMPARE_LESS_THAN_EQUAL
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL
import static org.codehaus.groovy.syntax.Types.DIVIDE
import static org.codehaus.groovy.syntax.Types.EQUALS
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET
import static org.codehaus.groovy.syntax.Types.LOGICAL_AND
import static org.codehaus.groovy.syntax.Types.LOGICAL_OR
import static org.codehaus.groovy.syntax.Types.MINUS
import static org.codehaus.groovy.syntax.Types.MINUS_MINUS
import static org.codehaus.groovy.syntax.Types.MOD
import static org.codehaus.groovy.syntax.Types.MULTIPLY
import static org.codehaus.groovy.syntax.Types.NOT
import static org.codehaus.groovy.syntax.Types.PLUS
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL
import static org.codehaus.groovy.syntax.Types.PLUS_PLUS
import static org.codehaus.groovy.syntax.Types.POWER
import static org.codehaus.groovy.syntax.Types.RIGHT_SQUARE_BRACKET

/**
 * Class that receives all the ETL initial commands.
 * <pre>
 *	extract 'dataSetFieldName' load 'assetFieldName'
 *	set assetFieldName with 'A simple label value'
 *  iterate {
 *     ....
 *  }
 * </pre>
 * There is a method for each one of this methods in ETLProcessor class.
 * @see com.tdsops.etl.ETLProcessor#load
 * @see com.tdsops.etl.ETLProcessor#set
 * @see com.tdsops.etl.ETLProcessor#iterate
 */
class ETLProcessor implements RangeChecker, ProgressIndicator {

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
	 * Static variable name definition for SOURCE script variable
	 */
	static final String FINDINGS_VARNAME = 'FINDINGS'
	/**
	 * Static variable name definition for NOW script variable
	 */
	static final String NOW_VARNAME = 'NOW'
	/**
	 * Static variable name definition for LOOKUP script variable
	 */
	static final String LOOKUP_VARNAME = 'LOOKUP'
	/**
	 * Static variable name definition for script name
	 */
	static final String ETLScriptName = 'TDSETLScript'


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
	/**
	 * An instance of this interface should be assigned
	 * to be used in fieldDefinition validations
	 * @see com.tdsops.etl.ETLProcessor#lookUpFieldDefinition(com.tdsops.etl.ETLDomain, java.lang.String)
	 */
	ETLFieldsValidator fieldsValidator
	/**
	 * Represents the variable bindings of an ETL script.
	 */
	ETLBinding binding
	/**
	 * Object where all the results will be collected
	 * when an ETL script is being executing.
	 */
	ETLProcessorResult result
	/**
	 * Iteration index to control the realtion between current position
	 * and the total amount of rows in an iteration.<br>
	 * It defines if the ETLProcessor instance is in a loop using iterate command.
	 */
	IterateIndex iterateIndex

	Integer currentRowIndex = 0
	Integer currentColumnIndex = 0
	/**
	 * Current Element. Assigned and exposed in dsl scripts using 'CURR_ELEMENT_VARNAME' number
	 */
	Element currentElement
	/**
	 * A debug output assignable in the ETLProcessor creation
	 */
	DebugConsole debugConsole

	List<Column> columns = []
	Map<String, Column> columnsMap = [:]
	List<Row> rows = []
	Row currentRow

	SelectedDomain selectedDomain
	ETLFindElement currentFindElement

	/**
	 * List of command that needs to be completed.
	 */
	private Stack<ETLStackableCommand> commandStack = []

	/**
	 * A set of Global transformations that will be apply over each iteration
	 */
	Set<Closure> globalTransformers = new HashSet<Closure>()

	/**
	 * Safe Global transformation that checks the type before applying a transformation
	 */
	static Trimmer = { Element element ->
		if(element.value instanceof CharSequence) {
			element.trim()
		}
	}

	/**
	 * Safe Global transformation that checks the type before applying a transformation
	 */
	static Sanitizer = { Element element ->
		if(element.value instanceof CharSequence) {
			element.sanitize()
		}
	}

	/**
	 * Safe Global transformation that checks the type before applying a transformation
	 */
	static Replacer = { String regex, String replacement ->
		return { Element element ->
			if (element.value instanceof CharSequence) {
			  element.replace(regex, replacement)
			}
		}
	}

	/**
	 * Some words to be used in an ETL script.
	 * <b> read labels</b>
	 * <b> console on/off</b>
	 * <b> ignore record</b>
	 * <b> ... transform with ...</b>
	 */
	static enum ReservedWord {
		labels, with, on, off, record, ControlCharacters
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
		this.initializeDefaultGlobalTransformations()
	}

	// ------------------------------------
	// ETL DSL methods
	// ------------------------------------
	/**
	 * <p>Selects a domain</p>
	 * <p>Every domain command also clean up bound variables and results in the lookup command</p>
	 * <pre>
	 *  domain Application
	 * </pre>
	 * @param domain a domain String value
	 * @return the current instance of {@code ETLProcessor} class
	 */
	ETLProcessor domain (ETLDomain domain) {
		validateStack()
		if(selectedDomain?.domain == domain){
			selectedDomain.addNewRow = true
		} else {
			selectedDomain = new SelectedDomain(domain)
		}
		cleanUpBindingAndReleaseLookup()
		result.addCurrentSelectedDomain(selectedDomain.domain)
		debugConsole.info("Selected Domain: $domain")
		return this
	}

	/**
	 * <p>Selects a domain</p>
	 * <p>Every domain command also clean up bound variables and results in the lookup command</p>
	 * @param element an instance of {@code Element} class
	 * @return the current instance of {@code ETLProcessor} class
	 * @see ETLProcessor#domain(com.tdsops.etl.ETLDomain)
	 */
	ETLProcessor domain(Element element){
		return domain(element.value)
	}

	/**
	 * <p>Selects a domain</p>
	 * <p>Every domain command also clean up bound variables and results in the lookup command</p>
	 * If value is an invalid Domain class name, it throws an Exception.
	 * @param domainName
	 * @return the current instance of {@code ETLProcessor} class
	 * @see ETLProcessor#domain(com.tdsops.etl.ETLDomain)
	 */
	ETLProcessor domain(String domainName){
		ETLDomain domain = ETLDomain.lookup(domainName)
		if(domain){
			return domain(domain)
		}
		throw ETLProcessorException.invalidDomain(domainName)
	}

	/**
	 * Read Labels from source of data
	 * @param reservedWord
	 * @return the current instance of ETLProcessor
	 */
	ETLProcessor read (ReservedWord reservedWord) {
		validateStack()
		if (reservedWord == ReservedWord.labels) {
			columnsMap = [:]
			this.dataSetFacade.fields().eachWithIndex { getl.data.Field field, Integer index ->
				Column column = new Column(label: fieldNameToLabel(field), index: index)
				columns.add(column)
				columnsMap[column.label] = column
			}
			currentRowIndex++
			dataSetFacade.setCurrentRowIndex(currentRowIndex)
			debugConsole.info "Reading labels ${columnsMap.values().collectEntries { [("${it.index}"): it.label] }}"
		} else {
			throw ETLProcessorException.invalidReadCommand()
		}
		return this
	}

	/**
	 * Iterate command from one row to another one using their position in the DataSet.
	 * <code>
	 *  from 1 to 3 iterate {
	 *  	...
	 *  }
	 * <code>
	 * @param from
	 * @return a Map with the next steps in this command.
	 */
	Map<String, ?> from (int from) {
		validateStack()
		return [to: { int to ->
			[iterate: { Closure closure ->
				from--
				to
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
	 * from 1, 3, 5 iterate {
	 * 			......
	 * }
	 * </code>
	 * @param numbers an arrays of ordinal row numbers
	 * @return
	 */
	Map<String, ?> from (int[] numbers) {
		validateStack()
		return [iterate: { Closure closure ->
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
	 * Aborts processing of the current row for the domain in the context
	 * <pre>
	 *   if (SOURCE.Env == 'Development) {
	 *      ignore record
	 *   }
	 * </pre>
	 * @param label just a label to detect if the command was used with 'row' label
	 * @return current instance of ETLProcessor
	 */
	ETLProcessor ignore (ReservedWord reservedWord) {
		validateStack()
		if(reservedWord == ReservedWord.record){
			if (!hasSelectedDomain()) {
				throw ETLProcessorException.domainMustBeSpecified()
			}
			result.ignoreCurrentRow()
			debugConsole.info("Ignore record ${currentRowIndex}")
		} else {
			// TODO: add validation for invalid use of this command
		}

		return this
	}

	/**
	 * Method invoked at the begin within the iterate loop
	 * @see ETLProcessor#doIterate(java.util.List, groovy.lang.Closure)
	 */
	void topOfIterate(){
		result.startRow()
	}

	/**
	 * Method invoked at the begin within the iterate loop
	 * @see ETLProcessor#doIterate(java.util.List, groovy.lang.Closure)
	 * @param rowNum the current number de the rows
	 * @param totalNumRows total number of rows for the current iterate loop
	 */
	void bottomOfIterate(Integer rowNum, Integer totalNumRows){
		reportRowProgress(rowNum, totalNumRows)
		result.endRow()
	}

	/**
	 * Iterates a list of rows applying a closure
	 * It initialize context variables in the ETL Binding context
	 *
	 * @param rows
	 * @param closure
	 * @return the ETLProcesor instance
	 * @see ETLBinding#getVariable(java.lang.String)
	 */
	ETLProcessor doIterate (List rows, Closure closure) {

		iterateIndex = new IterateIndex(rows.size())
		currentRowIndex = 1
		rows.each { def row ->
			topOfIterate()
			currentColumnIndex = 0
			cleanUpBindingAndReleaseLookup()
			bindVariable(SOURCE_VARNAME, new DataSetRowFacade(row))
			bindVariable(DOMAIN_VARNAME, new DomainFacade(result))
			bindVariable(NOW_VARNAME, new NOW())

			closure(addCrudRowData(row))

			bottomOfIterate(currentRowIndex, rows.size())
			currentRowIndex++
			iterateIndex.next()
			binding.removeAllDynamicVariables()
		}
		finishIterate()
		iterateIndex = null
		currentRowIndex--
		return this
	}

	/**
	 * Iterates and applies closure to every row in the dataSource
	 * @todo After discussing with @dcorrea we agreed that he can add a Pre/Post Conditions to the User script (TM-9746) that will run the validations instead of figuring out where do we need to run it.
	 * @param closure
	 * @return
	 */
	ETLProcessor iterate (Closure closure) {
		validateStack()
		doIterate(this.dataSetFacade.rows(), closure)
	}

	/**
	 * Sets Status console to on/off for allow/disallow log messages.
	 * @param status
	 * @return
	 */
	ETLProcessor console (ReservedWord reservedWord) {
		validateStack()
		DebugConsole.ConsoleStatus consoleStatus = DebugConsole.ConsoleStatus.values().find { it.name() == reservedWord.name() }

		if (consoleStatus == null) {
			throw ETLProcessorException.invalidConsoleStatus(reservedWord.name())
		}
		debugConsole.status = consoleStatus
		this
	}

	/**
	 * Removes leading and trailing whitespace from a string.
	 * @param status
	 * @return the instance of ETLProcessor who received this message
	 */
	ETLProcessor trim (ReservedWord reservedWord) {

		if (reservedWord == ReservedWord.on) {
			globalTransformers.add(Trimmer)
		} else if (reservedWord == ReservedWord.off) {
			globalTransformers.remove(Trimmer)
		}

		debugConsole.info "Global trim status changed: $reservedWord"
		return this
	}

	/**
	 * Global sanitize global function
	 * @param status
	 * @return the instance of ETLProcessor who received this message
	 */
	ETLProcessor sanitize (ReservedWord reservedWord) {

		if (reservedWord == ReservedWord.on) {
			globalTransformers.add(Sanitizer)
		} else if (reservedWord == ReservedWord.off) {
			globalTransformers.remove(Sanitizer)
		}

		debugConsole.info "Global sanitize status changed: $reservedWord"
		return this
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
		return this
	}

	/**
	 * Global Replace function
	 * @param control
	 * @return
	 */
	Map<String, ?> replace (ReservedWord reservedWord) {
		debugConsole.info "Global trm status changed: $reservedWord"
		if (reservedWord == ReservedWord.ControlCharacters) {
			return [
				with: { y ->
					globalTransformers.add(Replacer(ControlCharactersRegex, y))
				}
			]
		} else {
			throw ETLProcessorException.invalidReplaceCommand()
		}
	}

	/**
	 * Skip a fixed amount of row for the iterate process
	 * @param amount
	 * @return
	 */
	ETLProcessor skip (Integer amount) {
		currentRowIndex += amount
		dataSetFacade.setCurrentRowIndex(currentRowIndex)
		return this
	}

	/**
	 * Defines the sheet name to be used in an ETl script
	 * @param sheetName
	 * @return
	 */
	ETLProcessor sheet (String sheetName) {
		currentRowIndex = 0
		dataSetFacade.setCurrentRowIndex(currentRowIndex)
		dataSetFacade.setSheetName(sheetName)
	}

	/**
	 * Defines the sheetNumber to be used in an ETl script
	 * @param sheetNumber
	 * @return
	 */
	ETLProcessor sheet (Integer sheetNumber) {
		currentRowIndex = 0
		dataSetFacade.setCurrentRowIndex(currentRowIndex)
		dataSetFacade.setSheetNumber(sheetNumber)
	}

	/**
	 * Defines the rootNode XPath to be used in an ETl script and a JSON dataset
	 * @param sheetName
	 * @return
	 */
	ETLProcessor rootNode (String rootNode) {
		currentRowIndex = 0
		dataSetFacade.setCurrentRowIndex(currentRowIndex)
		dataSetFacade.setRootNode(rootNode)
	}

	/**
	 * Extracts an element from dataSource by its index in the row
	 * <code>
	 *     domain Application
	 *     iterate {
	 *          extract 1 load 'appName'
	 *          extract 3 load 'description'
	 *     }
	 * <code>
	 * @param index
	 * @return
	 */
	Element extract (Integer index) {
		validateStack()
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
	 *          extract 'column name' load 'appName'
	 *      }
	 * <code>
	 * @param columnName
	 * @return
	 */
	Element extract (String columnName) {
		validateStack()
		if (!columnsMap.containsKey(labelToFieldName(columnName))) {
			throw ETLProcessorException.extractMissingColumn(columnName)
		}
		currentColumnIndex = columnsMap[labelToFieldName(columnName)].index

		doExtract()
	}

	/**
	 * Load a domain fieldName using an explicit value. It could be a simple String,
	 * a DOMAIN or SOURCE reference, or a CE/local variable.
	 * <pre>
	 *    domain Application
	 *    load 'assetName' with 'Asset Name'
	 *    load 'assetName' with CE
	 *    load 'assetName' with myLocalVar
	 *    load 'assetName' with DOMAIN.id
	 *    load 'assetName' with SOURCE.'data name'
	 *    load 'assetName' with concat(',', SOURCE.'column 1', SOURCE.'column 2')
	 * </pre>
	 * @param fieldName
	 * @return
	 */
	Element load(final String fieldName) {
		validateStack()
		Element element = findOrCreateCurrentElement(lookUpFieldDefinition(selectedDomain.domain, fieldName))
		element.loadedElement = true
		return element
	}


	/**
	 * Create a local variable using variableName parameter.
	 * It adds a new dynamic variable in he current script row execution.
	 * <pre>
	 *	iterate {
	 *		domain Application
	 *	    ...
	 *		set environmentVar with 'Production'
	 *		set environmentVar with SOURCE.'application id'
	 *		set environmentVar with DOMAIN.id
	 *		.....
	 *	}
	 * </pre>
	 * @param field
	 * @return
	 */
	Map<String, ?> set(final String variableName) {
		if(!binding.isValidETLVariableName(variableName)){
			throw ETLProcessorException.invalidETLVariableName(variableName)
		}
		validateStack()

		return [
			with: { value ->
				Object localVariable = ETLValueHelper.valueOf(value)
				if(iterateIndex){
					addLocalVariableInBinding(variableName, localVariable)
				} else {
					addGlobalVariableInBinding(variableName, localVariable)
				}
				localVariable
			}
		]
	}

	/**
	 * Lookup ETL command implementation:
	 * <pre>
	 *  iterate {
	 *      ...
	 *      domain Device
	 *      extract 'Vm' load 'Name'
	 *      extract Cluster
	 *      def clusterName = CE
	 *
	 *      lookup 'assetName' with 'clusterName'
	 *  }
	 * </pre>
	 * @param fieldNames
	 */
	Map<String, ?> lookup(final String fieldName){
		validateStack()
		lookUpFieldDefinition(selectedDomain.domain, fieldName)
		return [
		    with: { value ->
			    Object stringValue = ETLValueHelper.valueOf(value)

			    boolean found = result.lookupInReference(fieldName, stringValue)
			    if (found) {
				    bindVariable(DOMAIN_VARNAME, new DomainFacade(result))
			    }
			    addLocalVariableInBinding(LOOKUP_VARNAME, new LookupFacade(found))
		    }
		]
	}

	/**
	 * Initialize a fieldName using a default value
	 * <pre>
	 *	iterate {
	 *		domain Application
	 *		initialize 'environment' with 'Production'
	 *	    initialize 'environment' with Production
	 *	    initialize 'environment' with SOURCE.'application id'
	 *	    initialize 'environment' with DOMAIN.id
	 *
	 *	    extract 'application id'
	 *	    initialize 'environment' with CE
	 *	}
	 * </pre>
	 * @param field
	 * @return
	 */
	Map<String, ?> initialize(String field){
		validateStack()
		return [
			with: { defaultValue ->

				Element element = findOrCreateCurrentElement(lookUpFieldDefinition(selectedDomain.domain, field))
				element.init = ETLValueHelper.valueOf(defaultValue)
				addElementInitialized(selectedDomain.domain, element)
				element
			}
		]
	}

	/**
	 * Initialize a fieldName using a default value
	 * <pre>
	 *	iterate {
	 *		domain Application
	 *		init 'environment' with 'Production'
	 *	}
	 * </pre>
	 * @param field
	 * @return
	 * @see ETLProcessor#initialize(java.lang.String)
	 */
	Map<String, ?> init(final String field) {
		validateStack()
		initialize(field)
	}

	/**
	 * Create a Find object for a particular Domain instance.
	 * <pre>
	 *  find Application by 'Name' with nameVar into 'id'
	 * </pre>
	 * @param domain an instance of {@code ETLDomain} class
	 * @return an instance of {@code ETLFindElement} class
	 */
	ETLFindElement find (ETLDomain domain) {
		debugConsole.info("find Domain: $domain")
		validateStack()
		bindCurrentFindElement(new ETLFindElement(this, domain, this.currentRowIndex))
		pushIntoStack(currentFindElement)
		return currentFindElement
	}

	/**
	 * Create a Find object for a particular {@code Element.value}.
	 * <pre>
	 *  Map map = [
	 * 	    'App': Application,
	 * 		'Srv': Device
	 * 	]
	 *  extract 'type' transform with substitute(map) set domainClassVar
	 *  find domainClassVar by 'Name' with nameVar into 'id'
	 * </pre>
	 * If value is an invalid Domain class name, it throws an Exception.
	 * @param element an instance of {@code Element} class
	 * @return an instance of {@code ETLFindElement} class
	 */
	ETLFindElement find(Element element){
		return find(element.value)
	}

	/**
	 * Create a Find object for a particular Domain instance name
	 * <pre>
	 *  Map map = [
	 * 	    'App': Application,
	 * 		'Srv': Device
	 * 	]
	 *  extract 'type' transform with substitute(map) set domainClassVar
	 *  find domainClassVar.value by 'Name' with nameVar into 'id'
	 * </pre>
	 * If the String is an invalid Domain, it throws an Exception.
	 * @param domainName a domain class name
	 * @return an instance of {@code ETLFindElement}
	 */
	ETLFindElement find(String domainName){
		ETLDomain domain = ETLDomain.lookup(domainName)
		if(domain){
			return find(domain)
		}
		throw ETLProcessorException.invalidDomain(domainName)
	}

	/**
	 * Adds another find results in the current find element.
	 * <pre>
	 *  find Application by 'Name' with nameVar into 'id'
	 *  elseFind Application by 'appVersion' with appVersionVar into 'id'
	 * </pre>
	 * If there is not {@code ETLProceesor.currentFindElement}
	 * it throws an Exception {@code ETLProcessorException.#notCurrentFindElement}
	 * @return an instance of {@code ETLFindElement} class
	 */
	ETLFindElement elseFind(ETLDomain domain) {
		validateStack()
		if (!currentFindElement) {
			throw ETLProcessorException.notCurrentFindElement()
		}

		pushIntoStack(currentFindElement)
		return currentFindElement.elseFind(domain)
	}

	/**
	 * Adds another find results in the current find element
	 * for a particular {@code Element.value}
	 * <pre>
	 *  Map map = [
	 * 	    'App': Application,
	 * 		'Srv': Device
	 * 	]
	 *  extract 'type' transform with substitute(map) set domainClassVar
	 *  find domainClassVar by 'Name' with nameVar into 'id'
	 *  elseFind domainClassVar by 'appVersion' with appVersionVar into 'id'
	 * </pre>
	 * @param element an instance of {@code Element} class
	 * @return an instance of {@code ETLProcessor} class
	 */
	ETLProcessor elseFind(Element element){
		return elseFind(element.value)
	}

	/**
	 * Adds another find results in the current find element
	 * for a particular Domain instance name
	 * <pre>
	 *  Map map = [
	 * 	    'App': Application,
	 * 		'Srv': Device
	 * 	]
	 *  extract 'type' transform with substitute(map) set domainClassVar
	 *  find domainClassVar.value by 'Name' with nameVar into 'id'
	 *  elseFind domainClassVar.value by 'appVersion' with appVersionVar into 'id'
	 * </pre>
	 * If the String is an invalid Domain, it throws an Exception.
	 * @param domainName a domain class name
	 * @return an instance of {@code ETLProcessor}
	 */
	ETLProcessor elseFind(String domainName){
		ETLDomain domain = ETLDomain.lookup(domainName)
		if(domain){
			return elseFind(domain)
		}
		throw ETLProcessorException.invalidDomain(domainName)
	}

	/**
	 * WhenFound ETL command. It defines what should based on find command results
	 * <pre>
	 *		whenNotFound 'asset' create {
	 *			assetClass: Application
	 *			assetName: primaryNameVar
	 *			assetType: primaryTypeVar
	 *			"SN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param fieldName
	 * @return the current find Element
	 */
	FoundElement whenNotFound(String fieldName) {
		validateStack()
		if(!currentFindElement){
			throw ETLProcessorException.whenNotFoundCommandWithoutCurrentFindElement(fieldName)
		}
		return new WhenNotFoundElement(fieldName, currentFindElement.mainSelectedDomain, this)
	}

	/**
	 * WhenNotFound ETL command. It defines what should based on find command results
	 * <pre>
	 *		whenFound asset update {
	 *			"TN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param fieldName
	 * @return the current find Element
	 */
	FoundElement whenFound(String fieldName) {
		validateStack()
		if(!currentFindElement){
			throw ETLProcessorException.whenFoundCommandWithoutCurrentFindElement(fieldName)
		}
		return new WhenFoundElement(fieldName, currentFindElement.mainSelectedDomain, this)
	}

	/**
	 * Add a message in console for an element from dataSource by its index in the row
	 * @param index
	 * @return current instance of ETLProcessor
	 */
	ETLProcessor debug(Integer index) {

		if (index in (0..currentRow.size())){
			currentColumnIndex = index
			doDebug(currentRowIndex, currentColumnIndex, currentRow.getDataSetValue(currentColumnIndex))
		} else {
			throw ETLProcessorException.missingColumn(index)
		}
		return this
	}

	/**
	 * Add a message in console for an element from dataSource by its column name
	 * @param columnName
	 * @return current instance of ETLProcessor
	 */
	ETLProcessor debug(String columnName) {

		if (columnsMap.containsKey(columnName)){
			currentColumnIndex = columnsMap[columnName].index
			doDebug(currentRowIndex, currentColumnIndex, currentRow.getDataSetValue(currentColumnIndex))
		} else {
			throw ETLProcessorException.missingColumn(columnName)
		}
		return this
	}

	/**
	 * Log a message using DebugConsole.LevelMessage type
	 * @param message an String message to be logged
	 * @param level level used to add a new message in debug console
	 * @return current instance of ETLProcessor
	 */
	ETLProcessor log (Object message, DebugConsole.LevelMessage level = DebugConsole.LevelMessage.DEBUG) {
		debugConsole.append(level, ETLValueHelper.valueOf(message))
		return this
	}

	// ------------------------------------
	// Support methods
	// ------------------------------------

	/**
	 * Validate that the stack is not in Violation of an object waiting to be completed when other is loaded
	 * @param expectedObjectOnStack
	 */
	private validateStack(ETLStackableCommand expectedObjectOnStack = null) {

		boolean stackViolation = false
		if(expectedObjectOnStack == null && commandStack.size() > 0){
			stackViolation = true
		} else if(expectedObjectOnStack &&
				  (commandStack.size() == 0 || commandStack.peek() != expectedObjectOnStack) ) {
			stackViolation = true
		}
		if(stackViolation){
			ETLStackableCommand stackableCommand = commandStack.pop()
			throw new ETLProcessorException(stackableCommand.stackableErrorMessage())
		}
	}

	boolean pushIntoStack(command) {
		commandStack.push(command)
	}

	ETLStackableCommand popFromStack(){
		commandStack.pop()
	}

	/**
	 * It looks up the field definition for a Domain by fieldName.
	 * It is in charge of validating if a field belongs to a domain class.
	 * That domain class can be within the AssetEntity hierarchy or be any of the other domain classes in the system.
	 * <br>
	 * Validation is based on the transformation from {@link ETLDomain#clazz} field.
	 * Then it builds an instance of ETLFieldDefinition with all the necessary data used in ETLProcessorResult
	 * @param an instance of ETLDomain used to validate fieldName parameter
	 * @param fieldName field name used in the lookup process
	 * @return an instance of {@link  ETLFieldDefinition}
	 * @see ETLFieldDefinition
	 * @see ETLProcessorResult
	 */
	ETLFieldDefinition lookUpFieldDefinition(ETLDomain domain, String field) {

		ETLFieldDefinition fieldSpec

		if (ETLDomain.External != domain) {

			if (!fieldsValidator.hasSpecs(domain, field)) {
				throw ETLProcessorException.unknownDomainFieldsSpec(domain, field)
			}

			fieldSpec = fieldsValidator.lookup(domain, field)
			if (!fieldSpec) {
				throw ETLProcessorException.domainWithoutFieldsSpec(domain, field)
			}
		}
		return fieldSpec
	}

	/**
	 * It looks up the field Spec for Domain by fieldName.
	 * It is in charge of validating if a field belongs to a domain class.
	 * That domain class can be within the AssetEntity hierarchy or be any of the other domain classes in the system.
	 * @param fieldName field name used in the lookup process
	 * @return an instance of {@link  ETLFieldDefinition}
	 * @see ETLProcessor#lookUpFieldDefinition(com.tdsops.etl.ETLDomain, java.lang.String)
	 */
	ETLFieldDefinition lookUpFieldDefinitionForCurrentDomain(String fieldName) {
		return lookUpFieldDefinition(selectedDomain.domain, fieldName)
	}

	/**
	 * It validates if a field is a reference domain fieldName or
	 * if it's a domain identifier
	 * @param domain
	 * @param fieldName
	 * @throws ETLProcessorException if some of the validations fail
	 */
	void validateDomainPropertyAsReference(String property) {

		//TODO: Refactor this logig moving some of this to fieldsValidator implementation
		Class<?> clazz = selectedDomain.domain.clazz

		if(!GormUtil.isDomainProperty(clazz, property)) {
			throw ETLProcessorException.invalidDomainPropertyName(selectedDomain.domain, property)
		}
		if(!GormUtil.isDomainIdentifier(clazz, property) &&
			!GormUtil.isReferenceProperty(clazz, property)){
			throw ETLProcessorException.invalidDomainReference(selectedDomain.domain, property)
		}
	}

	/**
	 * Adds a message debug with element content in console
	 * @param element
	 */
	private def doDebug(Integer columnIndex, Integer rowIndex, Object value) {
		debugConsole.debug "${[position: [rowIndex, columnIndex], value: value]}"
	}

	/**
	 * Add a variable within the script as a dynamic variable.
	 * @param variableName binding name for a variable value
	 * @param value an object to be binding in context
	 */
	void addLocalVariableInBinding(String variableName, Object value) {
		binding.addDynamicVariable(variableName, value)
	}

	/**
	 * Add a variable within the script as a global variable.
	 * Tipically this variable is defined outside a iterate command
	 * @see ETLProcessor#doIterate(java.util.List, groovy.lang.Closure)
	 * @param variableName binding name for a variable value
	 * @param value an object to be binding in context
	 */
	void addGlobalVariableInBinding(String variableName, Object value) {
		binding.addGlobalVariable(variableName, value)
	}
	/**
	 * Adds a new row in the list of rows
	 * @param rowIndex
	 * @param row
	 */
	private Row addCrudRowData (Map row) {
		currentRow = new Row(dataSetFacade.fields().collect { row[it.name] }, this)
		rows.add(currentRow)
		return currentRow
	}

	/**
	 * Private method that executes extract method command internally.
	 * @return
	 */
	private Element doExtract () {
		Element element = createCurrentElement(currentColumnIndex)
		element.loadedElement = false
		debugConsole.info "Extract element: ${element.value} by column index: ${currentColumnIndex}"
		applyGlobalTransformations(element)
		return element
	}

	/**
	 * Adds a loaded element with the current domain in results.
	 * It also removes CE (currentElement) from script context.
	 * @param domain
	 * @param element
	 */
	void addElementLoaded (ETLDomain domain, Element element) {
		result.loadElement(element)
		debugConsole.info "Adding element ${element.fieldDefinition.getName()}='${element.value}' to domain ${domain} results"
	}

	/**
	 * Adds a loaded element with using the init value for the current domain in results.
	 * It also removes CE (currentElement) from script context.
	 * @param domain
	 * @param element
	 */
	void addElementInitialized(ETLDomain domain, Element element) {
		result.loadInitializedElement(element)
		debugConsole.info "Adding element ${element.fieldDefinition.getName()}='${element.init}' to domain ${domain} results"
	}

	/**
	 * Adds an entry for a finding command results.
	 * @param findElement
	 */
	void addFindElement(ETLFindElement findElement) {
		validateStack(findElement)
		// TODO: review it with John.
		findOrCreateCurrentElement(findElement.currentFind.fieldDefinition)
		result.addFindElement(findElement)
		popFromStack()
	}

	/**
	 * Add a FoundElement in the result based on its fieldName
	 * <pre>
	 *		whenFound 'asset' create {
	 *			assetClass Application
	 *			assetName primaryName
	 *			assetType primaryType
	 *			"SN Last Seen": NOW
	 *		}
	 * </pre>
	 * @param foundElement
	 */
	void addFoundElement(FoundElement foundElement){
		result.addFoundElement(foundElement)
	}

	/**
	 * Adds a warn message in the current result
	 * @param findElement
	 */
	void addFindWarnMessage(ETLFindElement findElement) {
		result.addFindWarnMessage(findElement)
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

	private void initializeDefaultGlobalTransformations(){
		globalTransformers.add(Trimmer)
		globalTransformers.add(Sanitizer)
	}

	/**
	 * Add an Element as CURR_ELEMENT_VARNAME value as variable within the binding script context.
	 * @param element a selected Element
	 * @return the curent element selected in ETLProcessor
	 */
	private Element bindCurrentElement(Element element) {
		currentElement = element
		binding.setVariable(CURR_ELEMENT_VARNAME, currentElement)
		return currentElement
	}

	/**
	 * Add an ETLFindElement as FINDINGS_VARNAME value as variable within the binding script context.
	 * If @findElement is not null an instance of FindingsFacade will be bound in the ETLBinding context.
	 * If @findElement is null, the null value will be bound in order to use it in an ETL script
	 * @param ETLFindElement a selected findElement
	 * @return the currentFindElement selected in ETLProcessor
	 */
	private ETLFindElement bindCurrentFindElement(ETLFindElement findElement) {
		currentFindElement = findElement
		binding.addDynamicVariable(FINDINGS_VARNAME, findElement ? new FindingsFacade(currentFindElement) : findElement)
		return currentFindElement
	}

	/**
	 * A clean up method to release variables in the context and
	 * release lookup references
	 */
	private void cleanUpBindingAndReleaseLookup() {
		bindCurrentElement(null)
		bindCurrentFindElement(null)
	}

	/**
	 * Bind in ETL Content variables using name parameters
	 * @param name a String name for a variable
	 * @param value the Object instance to be bound in the ETL context
	 */
	private void bindVariable(String name, Object value) {
		binding.addDynamicVariable(name, value)
	}

	/**
	 * Find or create a current element based onf field definition
	 * @param fieldDefinition
	 * @return
	 */
	private Element findOrCreateCurrentElement(ETLFieldDefinition fieldDefinition) {
		RowResult rr
		// if there is an element found after a lookup invokation,
		// let's return that element so the commands like append has access
		// to the found elemnts
		if (result.resultIndex >= 0) {
			rr = result.currentRow()
			if (rr.fields.containsKey(fieldDefinition.name)) {
				FieldResult fieldResult = rr.fields[fieldDefinition.name]
				currentElement = new Element(
						value: fieldResult.value,
						originalValue: fieldResult.originalValue,
						init: fieldResult.init,
						fieldDefinition: fieldDefinition,
						processor: this
				)
			}
		}

		if (currentElement?.fieldDefinition?.name == fieldDefinition.name) {
			return bindCurrentElement(currentElement)
		} else {
			return bindCurrentElement(currentRow.addNewElement(null, fieldDefinition, this))
		}

	}

	/**
	 * Create the current element adding it in the ETL binding context
	 * @param currentColumnIndex
	 * @return
	 */
	private Element createCurrentElement(Integer currentColumnIndex) {
		return bindCurrentElement(currentRow.getDataSetElement(currentColumnIndex))
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

	/**
	 * Converts Field#name instance to the ETL label column name.
	 * <b>CSVDriver class</b> is converting field names to lower case, so we need to
	 * supply that issue for the all the labels read until to fix this in ticket TM-9268
	 * @param field an instance of getl.data.Field
	 * @return the label column name
	 * @see getl.data.Field#name
	 */
	private String fieldNameToLabel(Field field) {
		if(FilenameUtil.isCsvFile(dataSetFacade.fileName())){
			// TODO - remove toLowerCase once GETL library is fixed - see TM-9268.
			return field.name.trim().toLowerCase()
		} else {
			return field.name.trim()
		}
	}

	/**
	 * Converts ETL label value to a correct Field#name to be used in th ETL xtract command.
	 * <b>CSVDriver class</b> is converting field names to lower case, so we need to
	 * supply that issue for the all the labels read until to fix this in ticket TM-9268
	 * @param label a column name
	 * @return the field name used to check dataset fields
	 * @see getl.data.Field#name
	 */
	private String labelToFieldName(String label) {
		if(FilenameUtil.isCsvFile(dataSetFacade.fileName())){
			// TODO - remove toLowerCase once GETL library is fixed - see TM-9268.
			return label.toLowerCase()
		} else {
			return label
		}
	}

	Column column (String columnName) {
		return columnsMap[columnName]
	}

	Column column (Integer columnName) {
		return columns[columnName]
	}

	Row getCurrentRow () {
		return currentRow
	}

	Row getRow (Integer index) {
		return rows[index]
	}

	Element getElement (Integer rowIndex, Integer columnIndex) {
		return rows[rowIndex].getElement(columnIndex)
	}

	/**
	 * Used to return a Map of the results with keys:
	 *    ETLInfo - Information regarding the ETL Job
	 *    domains - Data structure with each of the domains processed in DataScript
	 *    consoleLog - A String containing the console log output (optional)
	 *
	 * An instance of ETLProcessorResult will be returned and it needs to be converted to JSON using
	 * com.tdsops.etl.marshall.AnnotationDrivenObjectMarshaller
	 * @param includeConsoleLog - flag if the console log should appear in the results (default false)
	 * @return an instance of ETLProcessorResult
	 * @see com.tdsops.etl.marshall.AnnotationDrivenObjectMarshaller
	 */
	ETLProcessorResult finalResult(Boolean includeConsoleLog = false){
		this.result.addFieldLabelMapInResults(fieldsValidator.fieldLabelMapForResults())
		if (includeConsoleLog) {
			this.result.consoleLog = this.debugConsole.content()
		} else {
			this.result.consoleLog = ''
		}
		return this.result
	}

	List<String> getAvailableMethods () {
		return ['domain', 'read', 'iterate', 'console', 'skip', 'extract', 'load', 'reference',
		 'with', 'on', 'labels', 'transform with', 'translate', 'debug', 'translate',
		 'uppercase()', 'lowercase()', 'first(content)', 'last(content)', 'all(content)',
		 'left(amount)', 'right(amount)', 'replace(regex, replacement)']
	}

	List<String> getAssetFields () {
		['id', 'assetName', 'moveBundle']
	}

	/**
	 * Checks if there is a domain entity being specified within the script
	 * @return
	 */
	boolean hasSelectedDomain() {
		return selectedDomain != null
	}

	// ---------------------------------------------
	// ETL DSL evaluation/check syntax methods
	// ---------------------------------------------
	/**
	 * It returns the default compiler configuration used by an instance of ETLProceesor.
	 * It prepares an instance of CompilerConfiguration with an instance of ImportCustomizer
	 * and an instance of SecureASTCustomizer.
	 * @see CompilerConfiguration
	 * @see SecureASTCustomizer
	 * @see ImportCustomizer
	 * @return a default instance of CompilerConfiguration
	 */
	private CompilerConfiguration defaultCompilerConfiguration(){

		SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
		secureASTCustomizer.with {
			// allow closure creation for the ETL iterate command
			closuresAllowed = true
			// disallow method definitions
			methodDefinitionAllowed = false
			// Empty withe list means forbid imports
			importsWhitelist = []
			starImportsWhitelist = []
			// Language tokens allowed
			tokensWhitelist = [
				DIVIDE, PLUS, MINUS, MULTIPLY, MOD, POWER, PLUS_PLUS, MINUS_MINUS, PLUS_EQUAL, LOGICAL_AND,
				COMPARE_EQUAL, COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL, LOGICAL_OR, NOT,
				COMPARE_GREATER_THAN, COMPARE_GREATER_THAN_EQUAL, EQUALS, COMPARE_NOT_EQUAL, COMPARE_EQUAL,
				LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET
			].asImmutable()
			// Types allowed to be used (Including primitive types)
			constantTypesClassesWhiteList = [
				Object, Integer, Float, Long, Double, BigDecimal, String, Map,
				Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
			].asImmutable()
			// Classes who are allowed to be receivers of method calls
			receiversClassesWhiteList = [
			    Object, // TODO: This is too much generic class.
				Integer, Float, Double, Long, BigDecimal, String, Map,
			].asImmutable()
		}

		ImportCustomizer customizer = new ImportCustomizer()

		CompilerConfiguration configuration = new CompilerConfiguration()
		configuration.addCompilationCustomizers customizer, secureASTCustomizer
		return  configuration
	}

	/**
	 * Using an instance of GroovyShell, it evaluates an ETL script content
	 * using this instance of the ETLProcessor.
	 * @see GroovyShell#evaluate(java.lang.String)
	 * @param script an ETL script content
	 * @param progressCallback callback to report ETL script evaluation progress
	 * @return
	 */
	@TimedInterrupt(600l)
	Object evaluate(String script, ProgressCallback progressCallback = null){
		return evaluate(script, defaultCompilerConfiguration(), progressCallback)
	}

	@TimedInterrupt(600l)
	Object execute(String script){
		Object result = new GroovyShell(
				this.class.classLoader,
				this.binding,
				defaultCompilerConfiguration())
				.evaluate(script, ETLScriptName)

		return result
	}

	/**
	 * Calculates an return error message and line number where the ocurred.<br>
	 * If exception paramter is an instance of {@code MultipleCompilationErrorsException}
	 * line number is calculated by {@code MultipleCompilationErrorsException#errorCollector}
	 * and {@code SyntaxErrorMessage#cause#startLine}<br>
	 * Otherwise, line number is calculated by {@code StackTraceElement#fileName} equals to
	 * {@code ETLProcessor#ETLScriptName} <br>
	 * Result map is returned as follow:
	 * <pre>
	 * return [
	 *  startLine: 4,
	 *  endLine: 4,
	 *  startColumn: 12,
	 *  endColumn: 24,
	 *  fatal: true,
	 *  message: '...',
	 *
	 * ]
	 * </pre>
	 * @param exception an instance of {@code Throwable}
	 * @return a Map with 2 fields: message and lineNumber
	 */
	static Map<String, ?> getErrorMessage(Throwable exception){

		Map<String, ?> error = [:]
		if(exception instanceof MultipleCompilationErrorsException){
			SyntaxErrorMessage syntaxErrorMessage = ((MultipleCompilationErrorsException)exception).getErrorCollector().errors.find {it.source.name == ETLProcessor.ETLScriptName}
			error.message    = syntaxErrorMessage.cause?.message
			error.startLine  = syntaxErrorMessage.cause?.startLine
			error.endLine    = syntaxErrorMessage.cause?.endLine
			error.startColumn= syntaxErrorMessage.cause?.startColumn
			error.endColumn  = syntaxErrorMessage.cause?.endColumn
			error.fatal      = syntaxErrorMessage.cause?.fatal
		}  else{
			error.message = exception.getMessage()
			error.startLine  = exception.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.ETLScriptName }?.lineNumber
			error.endLine    = error.startLine
			error.startColumn= null
			error.endColumn  = null
			error.fatal      = true
		}
		return error

	}

	/**
	 * Using an instance of GroovyShell, it evaluates an ETL script content
	 * using this instance of the ETLProcessor.
	 * It throws an InterruptedException when checks indicate code ran longer than desired
	 * @see GroovyShell#evaluate(java.lang.String)
	 * @param script an ETL script content
	 * @params configuration
	 * @return the result of evaluate ETL script param
	 * @param progressCallback callback to report ETL script evaluation progress
	 * @see TimedInterrupt
	 */
	@TimedInterrupt(600l)
	Object evaluate(String script, CompilerConfiguration configuration, ProgressCallback progressCallback = null){
		setUpProgressIndicator(script, progressCallback)
		Object result = new GroovyShell(this.class.classLoader, this.binding, configuration)
			.evaluate(script, ETLScriptName)
		return result
	}

	/**
	 * Using an instance of GroovyShell, it checks syntax of an ETL script content
	 * using this instance of the ETLProcessor.
	 * @see GroovyShell#evaluate(java.lang.String)
	 * @param script an ETL script content
	 * @param configuration an instance of CompilerConfiguration
	 * @return a Map with validSyntax field boolean value and a list of errors
	 */
	Map<String, ?> checkSyntax(String script, CompilerConfiguration configuration){

		List<Map<String, ?>> errors = []

		try {
			new GroovyShell(
				this.class.classLoader,
				this.binding,
				configuration
			).parse(script?.trim(), ETLScriptName)

		} catch (MultipleCompilationErrorsException cfe) {
			ErrorCollector errorCollector = cfe.getErrorCollector()
			errors = errorCollector.getErrors()
		}

		List errorsMap = errors.collect { error ->

			if(error instanceof SyntaxErrorMessage){
				[
					startLine  : error.cause?.startLine,
					endLine    : error.cause?.endLine,
					startColumn: error.cause?.startColumn,
					endColumn  : error.cause?.endColumn,
					fatal      : error.cause?.fatal,
					message    : error.cause?.message
				]
			} else {
				[
					startLine  : null,
					endLine    : null,
					startColumn: null,
					endColumn  : null,
					fatal      : true,
					message    : error.cause?.message
				]

			}
		}

		return [
			validSyntax: errors.isEmpty(),
			errors     : errorsMap
		]
	}

	/**
	 * Using an instance of GroovyShell, it checks syntax of an ETL script content
	 * using this instance of the ETLProcessor and its defaultCompilerConfiguration
	 * @see ETLProcessor#defaultCompilerConfiguration()
	 * @see GroovyShell#parse(java.lang.String)
	 * @param script an ETL script content
	 * @param configuration an instance of CompilerConfiguration
	 * @return a Map with validSyntax field boolean value and a list of errors
	 */
	Map<String, ?>  checkSyntax(String script){
		return checkSyntax(script, defaultCompilerConfiguration())
	}

}

class IterateIndex {
	Integer pos
	Integer size

	IterateIndex(Integer size){
		this.pos = 1
		this.size = size
	}

	Integer next(){
		this.pos++
		return this.pos
	}

	Boolean isFirst(){
		return this.pos == 1
	}
	Boolean isLast() {
		return this.pos == size
	}
}

