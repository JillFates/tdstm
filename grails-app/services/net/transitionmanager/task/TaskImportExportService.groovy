package net.transitionmanager.task

import net.transitionmanager.common.CoreService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.PersonService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.AssetComment
import com.tdsops.common.security.SecurityUtil
import com.tdsops.tm.enums.domain.TimeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletRequest

class TaskImportExportService implements ServiceMethods {

	static transactional = false

	static final String TEMPLATE_TAB_NAME  = 'Tasks'
	static final String TEMPLATE_TAB_TITLE = 'Title'
	static final String IMPORT_OPTION_PARAM_NAME = 'importOption'
	static final String IMPORT_FILENAME_PREFIX = 'TaskImport'

	// Indicates the alternate property name with the original and defaulted values
	// that are stored in the task map (e.g. firstName, firstName_o and firstName_d)
	static final String ERROR_SUFFIX = '_e'
	static final String ORIGINAL_SUFFIX = '_o'
	static final String DEFAULTED_SUFFIX = '_d'
	static final List<String> SUFFIX_LIST = [ORIGINAL_SUFFIX, DEFAULTED_SUFFIX, ERROR_SUFFIX]

	// The offset from zero to the first row in the spreadsheet that data appears
	static final int FIRST_DATA_ROW_OFFSET=1

	static final List<String> DELIM_OPTIONS = [';',':','|']


	static final xfrmInteger = {val, options -> return NumberUtil.toInteger(val)}
	// Transform a string, changing null to blank
	static final xfrmString = { val, options -> val ? val.toString(): '' }

	// Transforms a List to a comma separated list
	// @IntegrationTest
	static final xfrmListToString = { List list, options -> list?.join(', ') ?: '' }

	// Transforms a List to a pipe (|) separated string
	// @IntegrationTest
	static final xfrmListToPipedString = { List list, options -> list?.join('|') ?: '' }

	static final xfrmDateToString = { val, options ->
		return TimeUtil.dateToStringFormat( val, options.dateFormatter )
	}

	/**
	 * Returns a String representation of a Move Event.
	 * If val is a String, it means the value is coming from the UI and nothing has to be done.
	 * If it's a MoveEvent instance, we're recording the change before updating the task in the DB.
	 */
	static final xfrmMoveEventToString = { val, options ->
		String result = ''
		if(val){
			if (val instanceof String) {
				result = val
			} else {
				result = val.name
			}
		}
		return result
	}

	// Transformer that is used to populate the spreadsheet using the user's TZ
	static final xfrmDateTimeToString = { val, options ->
		return TimeUtil.dateToStringFormat( val, options.dateTimeFormatter )
	}

	// Transformer that is used for getting a string representation TimeScale enums
	static final xfrmTimeScaleToString = { val, options ->
		if (val == null) {
			return ""
		}

		if (val instanceof String) {
			return val
		}

		if (val instanceof TimeScale) {
			return val.toString()
		}
		log.error "xfrmTimeScaleToString() got unexpected data type ${val?.getClass()?.getName()}"
		return val?.toString()
	}

	// ------------------------------------------------------------------------
	// Kendo Template builder closures
	// ------------------------------------------------------------------------

	// Used in the map below to set the various template strings used by Kendo
	private static String changeTmpl(String prop) {
		"\"#= !$prop ?'':showChanges(data, '$prop') #\""
	}

	private static final String errorListTmpl = "kendo.template(\$('#error-template').html())"

	// --------------------------------
	// Meta Definition Maps
	// --------------------------------

	/*
	 * The following map is used to drive the Import and Export tables and forms. The properties consist of:
	 *        ssPos: the position that the property appears in the spreadsheet
	 *      formPos: the position that the property appears in the online form
	 * 	       type: P)erson, U)serLogin, T)ransient
	 *        width: the width in the online grid
	 *       locked: a flag to indicate that the column is locked (for horizontal scrolling)
	 *        label: the column heading label in the grid and export spreadsheet
	 *     template: a closure that will render the appropriate content used by the Kendo UI Grid
	 *    transform: a closure that will take the property value and the sheetInfoOpts map to compute the value to be rendered
	 *				 in the data grid.
	 *    validator: a closure that takes the property and sheetInfoOpts Map and will evaluate the current value to determine if it is valid
	 * defaultValue: a literal value to be used as a default when not supplied by the user or a closure to compute a value
	 *				 by using the task and the sheetInfoOpts maps that are passed into the closure.
	 */
	static final Map taskSpreadsheetColumnMap = [

		taskNumber 				: [type:'number', ssPos:0, formPos:1, domain:'C', width:80, locked: true,
										label: 'Task #', template:changeTmpl('taskNumber')],

		comment					: [type: 'string', ssPos:1, formPos:2, domain: 'C', width:120, locked:true,
										label: 'Task Description', template:changeTmpl('comment'), modifiable:true],

		errors                 : [type:'list',    ssPos:null, formPos:2,  domain:'T', width:240, locked:false, label:'Errors',
								  template:errorListTmpl, templateClass:'error', transform:xfrmListToPipedString ],

		assetEntity				: [type: 'string', ssPos:2, formPos:3, domain: 'A', width:120, locked:false,
										label: 'Related Asset', template:changeTmpl('assetEntity'), transform:xfrmString],

		assetClass				: [type: 'string', ssPos:3, formPos:4, domain: 'A', width:120, locked:false,
										label: 'Asset Class', template:changeTmpl('assetClass'), transform:xfrmString],

		assetId					: [type: 'number', ssPos:4, formPos:5, domain: 'A', width:80, locked:false,
										label: 'Asset ID', template:changeTmpl('assetId'), transform:xfrmString],

		predecessorTasks		: [type:'list',    ssPos:5,   formPos:7, domain:'T', width:190, locked:false, label:'Predecessor Task(s)',
									template:changeTmpl('predecessorTasks'), transform: xfrmListToPipedString ],


		assignedTo		: [type: 'string', ssPos:6, formPos:7, domain: 'C', width:120, locked:false, modifiable:true, foreignKey: true,
										label: 'Responsible Resource', template:changeTmpl('assignedTo'), transform:xfrmString],


		instructionsLink		: [type: 'string', ssPos:7, formPos:8, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Instructions Link', template:changeTmpl('instructionsLink'), transform:xfrmString],

		role					: [type: 'string', ssPos:8, formPos:9, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Team', template:changeTmpl('role'), transform:xfrmString],

		status					: [type: 'string', ssPos:9, formPos:10, domain: 'C', width:120, locked:false,
										label: 'Status', template:changeTmpl('status'), transform:xfrmString],

		datePlanned				: [type: 'date', ssPos:10, formPos:11, domain: 'C', width:120, locked:false,
										label: 'Date Planned', template:changeTmpl('datePlanned'), transform:xfrmDateTimeToString],

		outstanding				: [type: 'string', ssPos:11, formPos:12, domain: 'C', width:120, locked:false,
										label: 'Outstanding', template:changeTmpl('outstanding'), transform:xfrmString],

		dateRequired			: [type: 'date', ssPos:12, formPos:13, domain: 'C', width:120, locked:false,
										label: 'Date Required', template:changeTmpl('dateRequired'), transform: xfrmDateToString],

		comments				: [type: 'string', ssPos:13, formPos:14, domain: 'C', width:120, locked:false,
										label: 'Comments', template:changeTmpl('comments'), transform:xfrmString],

		duration				: [type: 'number', ssPos:14, formPos:15, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Duration', template:changeTmpl('duration'), transform:xfrmInteger],

		durationLocked			: [type: 'string', ssPos:15, formPos:16, domain: 'C', width:120, locked:false,
										label: 'Duration Locked', transform:xfrmString],

		durationScale			: [type: 'timescale', ssPos:16, formPos:17, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Duration Scale', template:changeTmpl('durationScale'), transform:xfrmTimeScaleToString],

		estStart			: [type: 'datetime', ssPos:17, formPos:18, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Estimated Start', template:changeTmpl('estStart'), transform:xfrmDateTimeToString],

		estFinish			: [type: 'datetime', ssPos:18, formPos:19, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Estimated Finish', template:changeTmpl('estFinish'), transform:xfrmDateTimeToString],

		actualStart				: [type: 'datetime', ssPos:19, formPos:20, domain: 'C', width:120, locked:false,
										label: 'Actual Start', template:changeTmpl('actualStart'), transform:xfrmDateTimeToString],

		actualFinish			: [type: 'datetime', ssPos:20, formPos:21, domain: 'C', width:120, locked:false,
										label: 'Actual Finish', template:changeTmpl('actualFinish'), transform:xfrmDateTimeToString],

		workflowStep			: [type: 'string', ssPos:21, formPos:22, domain: 'C', width:120, locked:false,
										label: 'WorkFlow Step', template:changeTmpl('workflowStep'), transform:xfrmString],

		category				: [type: 'string', ssPos:22, formPos:23, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Category', template:changeTmpl('category'), transform:xfrmString],

		dueDate					: [type: 'date', ssPos:23, formPos:24, domain: 'C', width:120, locked:false, modifiable:true,
										label: 'Due Date', template:changeTmpl('dueDate'), transform:xfrmDateToString],

		createdOn				: [type: 'date', ssPos:24, formPos:25, domain: 'C', width:120, locked:false,
										label: 'Created On', template:changeTmpl('createdOn'), transform:xfrmDateToString],

		createdBy				: [type: 'string', ssPos:25, formPos:26, domain: 'C', width:120, locked:false,
										label: 'Created By', template:changeTmpl('createdBy')],

		moveEvent				: [type: 'string', ssPos:26, formPos:27, domain: 'A', width:120, locked:false, modifiable:true,
										label: 'Move Event', template:changeTmpl('moveEvent'), transform:xfrmMoveEventToString, foreignKey: true],

		batchId 				: [type:'number', ssPos:27, formPos:28, domain:'C', width:80, locked: false,
										label: 'Batch Id', transform:xfrmString, template:changeTmpl('batchId')],
	]

	// Map of properties that can be modified by the user
	static final Map modifiableProperties = taskSpreadsheetColumnMap.findAll{ p, k -> k.modifiable}
	// The map of the location for the properties on the Title sheet.
	static final Map TitlePropMap = [
		 clientName: [1,2, 'String'],
		  projectId: [1,3, 'Integer'],
		projectName: [2,3, 'String'],
		 exportedBy: [1,6, 'String'],
		 exportedOn: [1,7, 'Datetime'],
		   timezone: [1,8, 'String'],
		 dateFormat: [1,9, 'String']
	]

	CoreService              coreService
	PartyRelationshipService partyRelationshipService
	PersonService            personService
	UserPreferenceService    userPreferenceService

	/**
	 * Load the spreadsheet into memory and validate that it contains some information. If
	 * successful, save the file with a random name and then return the model containing the filename.
	 *
	 * @param request - the servlet request object
	 * @param project - the project that the import is being applied againstclearDefaultedValues
	 * @param fileParamName - the servlet request params name of the var that references the upload spreadsheet file
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    tasks - the tasks that were read from the spreadsheet
	 *    labels - the list column header labels used in the tasks list
	 *    properties - the list of the property names used in the tasks list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	Map processFileUpload(HttpServletRequest request, Project project, Map formOptions) {
		if (! securityService.hasPermission(Permission.RecipeGenerateTasks, true)) {
			throw new UnauthorizedException('Do not have the required permission for to import task information')
		}

		Map model = [:]

		// Handle the file upload
		def file = request.getFile(formOptions.fileParamName)
		if (! file || file.empty) {
			throw new EmptyResultException('The file you uploaded appears to be empty')
		}

		// Save the spreadsheet file and then read it into a Workbook
		model.filename = saveImportSpreadsheet(request, formOptions.fileParamName)
		Workbook workbook = readImportSpreadsheet(model.filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		sheetInfoOpts.putAll(formOptions)

		if (sheetInfoOpts.sheetProjectId.toString() != project.id.toString()) {
			throw new InvalidRequestException('The imported spreadsheet did not originate from the currently selected project')
		}

		if (! sheetInfoOpts.sheetTzId) {
			throw new InvalidRequestException('The imported spreadsheet was missing the Timezone value on the Title sheet')
		}

		if (! sheetInfoOpts.sheetDateFormat) {
			throw new InvalidRequestException('The imported spreadsheet was missing the Date Format value on the Title sheet')
		}

		if (! validateSpreadsheetHeader(workbook)) {
			String fqfn = getFilenameWithPath(model.filename)
			if (! new File(fqfn).delete()) {
				log.error "Unable to delete temporary task import worksheet $fqfn"
			}
			throw new InvalidParamException('The spreadsheet column headers did not match the expected format. Please '+
											' export a new template before attempt an import.')
		}

		/*
		// The following code adds an unecessary overhead.
		List tasks = readTasksFromSpreadsheet(workbook, sheetInfoOpts)
		if (!tasks) {
			throw new EmptyResultException('Unable to read the spreadsheet or the spreadsheet was empty')
		}
		*/
		return model
	}

	/**
	 * Pull the uploaded file from the request and save it to a temporary file with a randomly generated
	 * name. After saving the file the filename and File handle are returned in a list.
	 * @param request - the servlet request object
	 * @param paramName - the name of the form parameter that contains the upload file
	 * @return The name of the filename that was saved (excluding the path)
	 */
	private String saveImportSpreadsheet(HttpServletRequest request, String paramName) {
		MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
		MultipartFile xlsFile = mpr.getFile(paramName)

		// Generate a random filename to store the spreadsheet between page loads
		String filename = "TaskImport-${securityService.currentUserLoginId}-" + SecurityUtil.randomString(10)+'.xls'

		// Save file locally
		String fqfn = getFilenameWithPath(filename)
		log.info "saveImportSpreadsheet() user $securityService.currentUsername uploaded TaskImport spreadsheet to $fqfn"

		xlsFile.transferTo(new File(fqfn))

		return filename
	}

	/**
	 * Read a spreadsheet from the file system into a Workbook which is returned
	 * @param filename - the filename to spreadsheet (which assumes it is in the app configured tmp directory)
	 * @return the spreadsheet itself
	 */
	private Workbook readImportSpreadsheet(String filename) {
		if (! filename) {
			throw new InvalidParamException('The import filename parameter was missing')
		}

		String fqfn = "${coreService.getAppTempDirectory()}/$filename"
		File file = new File(fqfn)
		if (!file.exists()) {
			throw new EmptyResultException('Unable to read from the uploaded spreadsheet')
		}
		return WorkbookFactory.create(file)
	}

	/**
	 * Read in the TitleSheet information and load the TZ and Date/DateTime formatters used for most of the
	 * application.
	 * @return A map containing the values from the title page plus the userTzId, and userDateFormat and formatters
	 */
	private Map getSheetInfoAndOptions(Project project, Workbook workbook) {

		// Collect the details off of the title sheet including the project id, exportedOn and the timezone when the data was exported
		Map sheetInfoOpts = getUserPreferences()
		readTitleSheetInfo(project, workbook, sheetInfoOpts)

		// Save a reference to the project in the Map
		sheetInfoOpts.project = project

		return sheetInfoOpts
	}

	/**
	 * Read the spreadsheet title tab that should have various information from the export that
	 * is required for the import process (e.g. timezone and exportedOn)
	 * @param project - the project that this export is for
	 * @param sheet - the spreadsheet to update
	 */
	 //Workbook
	private Map readTitleSheetInfo(Project project, workbook, Map sheetOpts) {

		def sheet = workbook.getSheet(TEMPLATE_TAB_TITLE)
		if (!sheet) {
			throw new InvalidRequestException("The spreadsheet was missing the '${TEMPLATE_TAB_TITLE}' sheet")
		}

		Map map = [:]

		// Helper closure that will get cell values from the sheet based on the col/row mapping in the TitlePropMap MAP
		// Note that the Datetime case will depend on the map.timezone property read from the spreadsheet
		def getCell = { prop ->
			def (col, row, type) = TitlePropMap[prop]
			def val

			switch (type) {
				case 'Integer':
					val = WorkbookUtil.getIntegerCellValue(sheet, col, row)
					break
				case 'Date':
					val = WorkbookUtil.getDateCellValue(sheet, col, row, sheetOpts.sheetDateFormatter)
					break
				case 'Datetime':
					val = WorkbookUtil.getDateTimeCellValue(sheet, col, row, sheetOpts.sheetTzId, sheetOpts.sheetDateTimeFormatter)
					break
				case 'String':
					val = WorkbookUtil.getStringCellValue(sheet, col, row)
					break
				default:
					throw new RuntimeException("readTitleSheetInfo.getCell had unhandled case for $type")
			}
		}

		sheetOpts.sheetTzId = getCell('timezone')
		sheetOpts.sheetProjectId = getCell('projectId')
		sheetOpts.sheetDateFormat = getCell('dateFormat')

		// Date Formatter is forced to be UTC/GMT, and the Date time is based on the sheet creation Timezone
		sheetOpts.sheetDateFormatter = TimeUtil.createFormatterForType(sheetOpts.sheetDateFormat, TimeUtil.FORMAT_DATE)
		assert sheetOpts.sheetDateFormatter
		sheetOpts.sheetDateTimeFormatter = TimeUtil.createFormatterForType(sheetOpts.sheetDateFormat, TimeUtil.FORMAT_DATE_TIME, sheetOpts.sheetTzId)
		assert sheetOpts.sheetDateTimeFormatter

		// Note that the exportedOn property is dependent on timezone being previously loaded
		sheetOpts.sheetExportedOn = getCell('exportedOn')
		if (sheetOpts.sheetExportedOn == -1) {
			log.error "*** readTitleSheetInfo() the Exported On wasn't properly read from the spreadsheet"
			throw new InvalidRequestException("Unable to parse the 'Exported On' value from the '${TEMPLATE_TAB_TITLE}' sheet")
		}

		return sheetOpts
	}

	/**
	 * Provide the user preferences for formatting date/times that will be used in rendering and reading the spreadsheet
	 * @return The map that contains:
	 *		tzId - the user's timezone id
	 *		dateFormat - the date format used for outputing and parse date properties
	 *		dateTimeFormat - the datetime format used for outputing and parse datetime properties
	 * @IntegrationTest
	 */
	private Map getUserPreferences() {
		Map map = [userTzId: userPreferenceService.timeZone, userDateFormat: userPreferenceService.dateFormat]

		// Get the appropriate date and datetime formatter based on the user's preferences for middle or little endian date formats
		map.dateFormatter = TimeUtil.createFormatterForType(map.userDateFormat, TimeUtil.FORMAT_DATE)
		map.dateTimeFormatter = TimeUtil.createFormatterForType(map.userDateFormat, TimeUtil.FORMAT_DATE_TIME_22)

		if (! map.dateFormatter ) {
			throw new RuntimeException("Unable to load Date formatter for ${map.dateFormatter}")
		}
		if (! map.dateTimeFormatter) {
			throw new RuntimeException("Unable to load DateTime formatter for ${map.dateTimeFormatter}")
		}

		return map
	}


	/**
	 * This method will compare the column headers to the map to determine if the spreadsheet being read in
	 * matches the format that we are expecting.
	 * @param sheet - the spreadsheet to inspect
	 * @return true if the headers match otherwise false
	 */
	private boolean validateSpreadsheetHeader(sheet) {
		boolean ok = true
		def tab = sheet.getSheet(TEMPLATE_TAB_NAME)
		taskSpreadsheetColumnMap.each { prop, info ->
			def colPos = info.ssPos
			if (colPos != null) {
				def label = WorkbookUtil.getCell(tab, colPos, 2)
				if (info.label.toString() != label.toString()) {
					log.debug "validateSpreadsheetHeader() expected '${info.label}' but found '$label' for column ${WorkbookUtil.columnCode(colPos)}"
					ok = false
				}
			}
		}
		return ok
	}

	/**
	 * Get the fully qualified filename with path of where the temporary files are written
	 * @param filename - the name of the file without a path
	 * @return the filename with the path prefix
	 */
	private String getFilenameWithPath(String filename) {
		// Make sure that the user can NOT perform any PATH tranversal
		if (StringUtil.containsPathTraversals(filename)) {
			log.warn "getFilenameWithPath() called with a HACKED filename $filename"
			throw new EmptyResultException('Unable to locate uploaded spreadsheet')
		}
		String fqfn = coreService.getAppTempDirectory() + '/' + filename
		return fqfn
	}

	/**
	 * Used to read the Task Import Spreadsheet and load up a list of task properties.
	 * Iterates over the taskSpreadsheetColumnMap Map to pluck the values out of the appropriate columns of
	 * each row and add to the map that is returned for each person/userlogin.
	 * ---- The values are just saved in their String type and will be manipulated later. ----
	 * The values are saved into the Map in the domain specific data type primarily because we need to
	 * be able to change date/datetimes potentially in different timezones and saving as string will prevent
	 * this capability.
	 * @param spreadsheet - the spreadsheet to read from
	 * @return the list that is read in
	 */
	private List<Map> readTasksFromSpreadsheet(spreadsheet, Map sheetInfoOpts) {
		int firstTaskRow = 3
		def sheet = spreadsheet.getSheet( TEMPLATE_TAB_NAME )
		int lastRow = WorkbookUtil.getLastRowNum(sheet)
		List tasks = []

		for (int row = firstTaskRow; row <= lastRow; row++) {
			// Initialize the task map
			Map task = [errors:[], matches:[], flags: [:], changeHistory: [:]]

			int pIdx = 0
			taskSpreadsheetColumnMap.each { prop, info ->
				Integer colPos = info.ssPos
				def value=null
				if (colPos != null) {
					switch (info.type) {
						case 'datetime':
							value = WorkbookUtil.getDateTimeCellValue(sheet, colPos, row, sheetInfoOpts.sheetTzId, sheetInfoOpts.sheetDateTimeFormatter)
							if (value == -1) {
								task.errors << "Invalid date value in ${WorkbookUtil.columnCode(colPos)}${row + FIRST_DATA_ROW_OFFSET}"
								setErrorValue(task, prop, 'Invalid datetime')
								value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )

							}
							break
						case 'date':
							value = WorkbookUtil.getDateCellValue(sheet, colPos, row, sheetInfoOpts.sheetDateFormatter) //dateFormatter)
							if (value == -1) {
								task.errors << "Invalid datetime value in ${WorkbookUtil.columnCode(colPos)}${row + FIRST_DATA_ROW_OFFSET}"
								setErrorValue(task, prop, 'Invalid date')
								value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							}
							break

						case 'timescale':
							value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							if (value) {
								TimeScale ts = TimeScale.asEnum(value)
								if (ts == null) {
									task[prop] = value
									setErrorValue(task, prop, "Invalid Duration Scale value")
								} else {
									value = ts
								}
							}
							break
						case 'boolean':
							value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							if (value?.size()>0) {
								Boolean bv = StringUtil.toBoolean(value)
								if (bv == null) {
									task[prop] = value
									setErrorValue(task, prop, 'Must be Y|N')
								} else {
									value = bv
								}
							}
							break

						case 'number':
							value = WorkbookUtil.getIntegerCellValue(sheet, colPos, row)
							break

						default:
							value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							if (info.containsKey('validator')) {
								if (! info.validator(value, sheetInfoOpts)) {
									task.errors << "${info.label} is invalid (${WorkbookUtil.columnCode(colPos)}${row + FIRST_DATA_ROW_OFFSET})"
									setErrorValue(task, prop, value)
								}
							}

							if (info.type == 'list') {
								value = StringUtil.splitter(value, ',', DELIM_OPTIONS)
							}
							break
					}

					task[prop] = value
				}
			}
			tasks.add(task)
		}
		return tasks
	}

	/**
	 * Load the Import request parameters from params into a Map that has been validated and formatted
	 * @param params - the request parameters
	 * @return the map of the parameters
	 */
	Map importParamsToOptionsMap(Map params) {
		Map options = [
			importOption:params[IMPORT_OPTION_PARAM_NAME],
		]

		if (params.filename) {
			options.filename = params.filename
		}
		return options
	}

	/**
	 * Assign the correct icon to the individual tasks based on the state of the task
	 * @param tasks - the list of task maps
	 */
	private void setIconsOnTasks(List<Map> tasks) {
		for (Map task in tasks) {
			String icon = 'pencil.png'
			if (task.errors) {
				icon = 'exclamation.png'
			}
			task.icon = HtmlUtil.resource([dir: 'assets/icons', file: icon, absolute: false])
		}
	}

	/**
	 * Set the appropriate property on the task Map to indicate that a defaulted value
	 * was used on the task. This also sets the actual property value since it is defaulting afterall.
	 * @param task - the Map with all of the task information
	 * @param property - the name of the property to set
	 * @param value - the value to set the defaulted field to
	 */
	private void setDefaultedValue(Map task, String property, value) {
		setValue(task, DEFAULTED_SUFFIX, property, value)
		task[property] = value
	}

	/**
	 * Used to set the appropriate property on the task Map to indicate that the original value is being
	 * changed on the task.
	 * @param task - the Map with all of the task information
	 * @param property - the name of the property to set
	 * @param value - the original value that the domain object had
	 */
	private void setOriginalValue(Map task, String property, value) {
		setValue(task, ORIGINAL_SUFFIX, property, value)
	}

	/**
	 * Used to set the appropriate property on the task Map to indicate that the original value
	 * failed the validation.
	 * @param task - the Map with all of the task information
	 * @param property - the name of the property to set
	 * @param value - the original value that the domain object had
	 */
	private void setErrorValue(Map task, String property, value) {
		setValue(task, ERROR_SUFFIX, property, value)
	}

	/**
	 * Used by setDefaultedValue and setOriginalValue to do the bulk of the work. You shouldn't be calling this
	 * method directly.
	 * @param task - the Map with all of the task information
	 * @param suffix - the suffix that is tacked onto the end of the variable name
	 * @param property - the name of the property to set
	 * @param value - the value to set the defaulted field to
	 */
	private void setValue(Map task, String suffix, String property, value) {
		String prop = "$property$suffix".toString()
		task[prop] = value
	}


	/**
	 * Populate the model with the necessary properties for the Review form
	 * @param project - the project that the import is being applied against
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    people - the task that were read from the spreadsheet
	 *    labels - the list column header labels used in the task list
	 *    properties - the list of the property names used in the task list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	Map generateModelForReview(Project project, Map formOptions) {
		if (formOptions.flagToUpdatePerson && ! securityService.hasPermission(Permission.PersonImport, true)) {
			throw new UnauthorizedException('Do not have the required permission for to import personnel information')
		}

		Map model = [:]

		model.filename = formOptions.filename
		model.labels = getLabelsInColumnOrder('formPos')
		model.properties = getPropertiesInColumnOrder('formPos')
		model.gridMap = taskSpreadsheetColumnMap
		model.defaultSuffix = DEFAULTED_SUFFIX
		model.originalSuffix = ORIGINAL_SUFFIX
		model.errorSuffix = ERROR_SUFFIX
		model.importOption = formOptions[IMPORT_OPTION_PARAM_NAME]
		/*
		Workbook workbook = readImportSpreadsheet(formOptions.filename)
		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		*/
		return model
	}

	/**
	 * Used to get the property names of the taskSpreadsheetColumnMap in the column order
	 */
	private List getPropertiesInColumnOrder(type) {
		Map subMap = taskSpreadsheetColumnMap.findAll { it.value.get(type) > 0 }
		List list = subMap.collect { prop, info ->  [info.get(type), prop] }
		list.sort { it[0] }
		list.collect { it[1] }
	}

	/**
	 * Used to get the labels in column order
	 */
	private List getLabelsInColumnOrder(type) {
		assert ['ssPos', 'formPos'].contains(type)

		Map subMap = taskSpreadsheetColumnMap.findAll { it.value.get(type) > 0 }
		List list = subMap.collect { prop, info ->  [info.get(type), info.get('label')] }
		list.sort { it[0] }
		list.collect { it[1] }
	}



	/**
	 * Used to cancel a previously started import process
	 * @param params - the parameters from the HttpRequest
	 */
	void cancelPreviousUpload(Project project, Map formOptions) {
		securityService.requirePermission(Permission.RecipeGenerateTasks, false,
			"attempted to cancel an task import for project $project")

		deleteUploadedSpreadsheet(formOptions.filename)
	}

	/**
	 * Used by the controller in the event that an exception was thrown and there's the potential that a file
	 * exists.
	 * @param formOptions - the params which will include the filename
	 */
	void deletePreviousUpload(Map formOptions) {
		securityService.requirePermission(Permission.RecipeGenerateTasks, false,
			"attempted to delete an uploaded task import spreadsheet(${formOptions.filename})")

		// Delete the upload file
		deleteUploadedSpreadsheet(formOptions.filename)
	}

	/**
	 * Used to delete the uploaded spreadsheet file from the temporary upload directory
	 * @param filename - the name of the file without a path
	 */
	private void deleteUploadedSpreadsheet(String filename) {
		String fqfn = getFilenameWithPath(filename)
		File file = new File(fqfn)
		if (file.exists()) {
			if (file.delete()) {
				log.debug "deleteUploadedSpreadsheet() deleted $fqfn"
			} else {
				log.error "deleteUploadedSpreadsheet() Failed to delete $fqfn"
			}
		} else {
			log.debug "deleteUploadedSpreadsheet() didn't find file $fqfn"
		}
	}

	/**
	 * This is used to return the JSON data generated by the Task Import POST step. The file should be written
	 * to the application temp directory with the same file name (.json extention) as the temporary spreadsheet
	 * @param project  - the project that the import is being applied against
	 * @param filename - the name of the temporarilly saved spreadsheet
	 * @controllerMethod
	 */
	List generateReviewData(Project project, String filename, Map formOptions) {
		if (!securityService.hasPermission(Permission.RecipeGenerateTasks, true)) {
			throw new UnauthorizedException('Do not have the required permission for to import task information')
		}

		// Load the spreadsheet
		Workbook workbook = readImportSpreadsheet(filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		sheetInfoOpts.putAll(formOptions)

		List tasks = validateSpreadsheetContent(project, workbook, sheetInfoOpts, formOptions)

		return transformTasks(tasks, sheetInfoOpts)
	}

	/**
	 * This method will read in all of the content from the tasks sheet and the invoke the validation logic
	 * @param project - the user's project in his context
	 * @param workbook - the spreadsheet that was loaded
	 * @param sheetInfoOpts - the map containing the information from the workbook title and the TZ/Date stuff
	 * @param formOptions - the user input params from the form
	 * @return The list of tasks mapped out
	 */
	private List<Map> validateSpreadsheetContent(Project project, Workbook workbook, Map sheetInfoOpts, Map formOptions) {

		List tasks = readTasksFromSpreadsheet(workbook, sheetInfoOpts)

		validateUploadedTasks(tasks, project, sheetInfoOpts, formOptions)

		return tasks
	}

	/**
	 * Used to validate the list of tasks that were uploaded and will populate the individual maps with
	 * properties errors when anything is found. The logic will update the tasks object that is passed into
	 * the method.  The logic will update the tasks data with the following information:
	 *    - set companyObj to the company
	 *    - set person properties with ORIGINAL_SUFFIX suffix if the value has been changed from the original
	 *    - creates submap flags to hold all logic flags used in various spots in the code
	 *    - set default values for properties not populated - values coming from the domain objects or from the default values in the def map
	 *    - creates errors List to track errors
	 *    -     appends any error messages to errors list that occur
	 * @param tasks - the list of tasks that are read from the spreadsheet
	 * @param project - the user's project in their context
	 * @param sheetInfoOpts - the worksheet title page info plus the TZ/Date stuff
	 * @param formOptions - form params from the page submission
	 */
	private void validateUploadedTasks(List<Map> tasks, Project project, Map sheetInfoOpts, Map formOptions) {
		// Retrieves available teams, including AUTO.
		List<String> allTeamCodes = partyRelationshipService.getTeamCodes(true)
		allTeamCodes << 'STAFF'
		List<MoveEvent> moveEvents = MoveEvent.findAllByProject(project)

		for (Map task in tasks) {
			if (!StringUtil.isBlank(task.assignedTo)) {
				Map personInfo = personService.findPersonByFullName(task.assignedTo)
				if (personInfo.isAmbiguous) {
					task.errors << 'Found multiple people by name'
				}
				task["_assignedTo"] = personInfo.person
			}
			if (!StringUtil.isBlank(task.role)){
				String team = allTeamCodes.find { it == task.role }
				if (!team) {
					task.errors << "Team doesn't exist."
				}
			}

			if (!StringUtil.isBlank(task.moveEvent)) {
				MoveEvent event = moveEvents.find { it.name == task.moveEvent }
				if (event) {
					task["_moveEvent"] = event
				} else {
					task.errors << "Move Event doesn't exist."
				}
			}

			AssetComment assetComment = AssetComment.findByTaskNumberAndProject(task.taskNumber, project)
			applyChangesToDomainObject(assetComment, task, sheetInfoOpts, true, true )
			assetComment.discard()
		}

		setIconsOnTasks(tasks)
	}

	/**
	 * Used to transform the list of tasks into the form that can be displayed in the spreadsheet
	 * copying only the properties that are used by the spreadsheet.
	 * @param tasks - the list of tasks that were loaded from the spreadsheet
	 * @param sheetInfoOpts - the map of various preferences
	 * @return the list of tasks formatted for presentation
	 */
	private List transformTasks(List tasks, Map sheetInfoOpts) {
		List list = []
		tasks.each { row ->
			Map task = [:]
			taskSpreadsheetColumnMap.each { prop, info ->
				if (info.formPos != null) {
					// Get the transformer if one exists
					def transformer = (info.containsKey('transform') ? info.transform : false)
					def value = row[prop]

					if (prop == 'errors' && value.size() > 0) {
						value.unique()
					}

					if (transformer) {
						task[prop] = transformer(value, sheetInfoOpts)
					} else {
						task[prop] = row[prop]
					}

					// Check for the Original, Defaulted values and add accordingly
					SUFFIX_LIST.each { suffix ->
						String sfxProp = "$prop$suffix".toString()
						if (row.containsKey(sfxProp)) {
							if (transformer) {
								task[sfxProp] = transformer(row[sfxProp], sheetInfoOpts)
							} else {
								task[sfxProp] = row[sfxProp]
							}
						}
					}
				}

				['flags', 'icon', 'teamChangeMap', 'securityChanges'].each { p ->
					if (row.containsKey(p)) {
						task[p] = row[p]
					}
				}
			}

			list << task
		}
		// return tasks
		return list
	}


	/**
	 * Used to validate the Import options are okay
	 * @param options - the map created by the importParamsToOptionsMap method
	 * @param a list of the errors which will be blank if no errors
	 */
	List validateImportOptions(Map options) {
		List errors = []

		int expireDays = 90
		if (options.expireDays) {
			options.expireDays = NumberUtil.toPositiveLong(options.expireDays, -1)
			if (expireDays == -1) {
				errors << 'The expiry days value must be a positive number'
			}
		}

		return errors
	}


	/**
	 * Used to populate the model with the necessary properties for the Review form
	 * @param project - the project that the import is being applied against
	 * @param options - the form options that the user selected for how to update tasks
	 * @param filename - the filename of the locally saved spreadsheet
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    people - the tasks that were read from the spreadsheet
	 *    labels - the list column header labels used in the tasks list
	 *    properties - the list of the property names used in the tasks list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	@Transactional
	Map postChangesToTasks(Project project, formOptions) {

		if (!securityService.hasPermission(Permission.RecipeGenerateTasks, true)) {
			throw new UnauthorizedException('Do not have the required permission to import task information')
		}

		Workbook workbook = readImportSpreadsheet(formOptions.filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		sheetInfoOpts.putAll(formOptions)

		// Read in the tasks and then validate them
		List tasks = validateSpreadsheetContent(project, workbook, sheetInfoOpts, formOptions)
		if (!tasks) {
			throw new EmptyResultException('Unable to read the spreadsheet or the spreadsheet was empty')
		}

		Map results = [
			taskSkipped: 0,
			taskCreated: 0,
			taskUpdated: 0,
			taskError: 0,
			taskUnchanged: 0,
		]

		List updatedTasks = []

		log.debug "postChangesToTasks() formOptions=$formOptions - processing ${tasks.size()} tasks"
		StringBuilder chgSB = new StringBuilder('<h2>Change History</h2>')
		StringBuilder errorsSB = new StringBuilder('<br><h2>Errors</h2><table><tr><th width=20>Row</th><th>Task #</th><th>Errors</th></tr>')
		boolean recordedErrors = false

		for (int i=0; i < tasks.size(); i++) {
			tasks[i].postErrors = []

			// Skip over the tasks that have errors from the validation process
			if (tasks[i].errors) {
				results.taskSkipped++

				if (formOptions.testMode) {
					recordedErrors = true
					errorsSB.append("<tr><td>${i+2}</td><td>${tasks[i].taskNumber}</td><td><ul><li>" +
						tasks[i].errors.join('<li>') + '</ul></td></tr>')
				}

				continue
			}

			String error
			boolean taskChanged = false


			// Reset the errors on the task
			tasks[i].errors = []


			(error, taskChanged) = applyTaskChanges(tasks[i], sheetInfoOpts, formOptions)
			if(error){
				tasks[i].errors << ["Row ${i+2} $error"]
				results.taskError++
			}
			// Add the task to the new updatedTasks to be displayed on the results page
			if (tasks[i].errors || taskChanged) {

				// Copy all of the properties out of the Task Map and transform to the format that the
				// data grid can use later
				Map task = [:]
				taskSpreadsheetColumnMap.each { prop, info ->
					String origKey = "$prop$ORIGINAL_SUFFIX".toString()
					String defKey = "$prop$DEFAULTED_SUFFIX".toString()
					String errKey = "$prop$ERROR_SUFFIX".toString()
					List keys = [prop, origKey, defKey, errKey]
					boolean hasTransform = info.transform
					keys.each { key ->
						if (tasks[i].containsKey(key)) {
							if (hasTransform) {
								task[key] = info.transform(tasks[i][key], sheetInfoOpts)
							} else {
								task[key] = tasks[i][key]
							}
						}
					}
					if (tasks[i].changeHistory) {
						task.changeHistory = tasks[i].changeHistory
					}
					task.flags = tasks[i].flags
				}
				updatedTasks << tasks[i]
			}

			results["taskUpdated"] = updatedTasks.size()

			// Add something to the temporary results view
			if (formOptions.testMode) {

				if (taskChanged) {
					chgSB.append("\r\n<br>Changes for ${tasks[i].taskNumber}:<br><table><th>Property</th><th>Orig Value</th><th>New Value</th></tr>\r\n")
					StringBuilder changeMsg = new StringBuilder("***** Change History for $tasks[i].taskNumber changed:")
					tasks[i].changeHistory.each { prop, origVal ->
						changeMsg.append("\n\t$prop was '$origVal' now is '${tasks[i][prop]}'")
						chgSB.append("<tr><td>$prop</td><td>$origVal</td><td>${tasks[i][prop]}</td></tr>\r\n")
					}
					log.info changeMsg.toString()
					chgSB.append("</table>\r\n")
				}

				if (tasks[i].errors) {
					recordedErrors = true
					errorsSB.append("<tr><td>${i+2}</td><td>${tasks[i].taskNumber}</td><td><ul><li>" +
						tasks[i].errors.join('<li>') + '</ul></td></tr>')
				}

			}

		}


		// Add the appropriate icons to the new list
		setIconsOnTasks(updatedTasks)

		saveResultsAsJson(updatedTasks, formOptions.filename, sheetInfoOpts)

		// Delete the upload file
		deleteUploadedSpreadsheet(formOptions.filename)
		// Throw an exception so we don't commit the data while testing (to be removed)
		if (formOptions.testMode) {
			errorsSB.append('</table>')
			String resultData = results.toString() + chgSB.toString() +
				(recordedErrors ? errorsSB.toString() : '')
			throw new DomainUpdateException('<H2>Test Mode - Import Results</H2>' + resultData )
		}
		return results
	}


	private void applyChangesToDomainObject(AssetComment assetComment, Map task, Map sheetInfoOpts, boolean shouldUpdateDomain, boolean setDefaults=false) {

		boolean unplannedChange = false
		String unChgdLabel='Unplanned change'
		boolean identifyUnplannedChanges = true

		modifiableProperties.each { prop, info ->
			if (propertyHasError(task, prop)) {
				if (taskSpreadsheetColumnMap.containsKey('defaultOnError')) {
					// Set a default value so that the domain validation will not fail
					assetComment[prop] = taskSpreadsheetColumnMap.defaultOnError()
				}
				return
			}


			boolean blockBlankOverwrites = task.flags.blockBlankOverwrites

			def origValue = assetComment[prop]
			if (info.type == 'date' && origValue != null) {
				origValue.clearTime()
			}

			def origValueTransformed = transformProperty(prop, origValue, sheetInfoOpts)
			def chgValueTransformed = transformProperty(prop, task[prop], sheetInfoOpts)

			// Check if it's a FK reference
			if(info["foreignKey"]){
				def reference = task["_${prop}"]
				if (origValueTransformed != chgValueTransformed) {
					assetComment[prop] = reference
					setOriginalValue(task, prop, origValueTransformed)
				}

			} else {
				boolean valuesEqual = origValueTransformed == chgValueTransformed
				boolean origValueIsBlank = StringUtil.isBlank(origValueTransformed.toString())
				boolean chgValueIsBlank = StringUtil.isBlank(chgValueTransformed.toString())
				// Checks if the incoming value is empty but the task has a value
				if (chgValueIsBlank){
					if ( !origValueIsBlank) {
						// Checks if it should write a null value.
						if (! blockBlankOverwrites) {
							assetComment[prop] = task[prop]
							setOriginalValue(task, prop, origValueTransformed)
						} else {
							unplannedChange = true
							setErrorValue(task, prop, unChgdLabel)
						}
					}
				} else {
					if (! valuesEqual) {
						assetComment[prop] = task[prop]
						setOriginalValue(task, prop, origValueTransformed)
					}
				}

			}
		}

	}

	/**
	 * Utility method to mapping the properties that were changed and the original values
	 * @param account - the map to record the changes into which will be the propertyName:originalValue
	 * @param domainObj - the object that the changes are read from
	 */
	private void recordChangeHistory(Map changeHistory, Object domainObj, Map options) {
		for (prop in domainObj.dirtyPropertyNames) {
			changeHistory[prop] = transformProperty(prop, domainObj.getPersistentValue(prop), options)
		}
	}

	/**
	 * Used to update tasks
	 * @param task - the task map
	 * @param options - the map of the options
	 * @return a list containing:
	 *    String - an error message if the update failed
	 *    boolean - a flag indicating if the task object was changed (true) or unchanged (false)
	 */
	private List applyTaskChanges( Map task, Map sheetInfoOpts, Map formOptions) {
		String error
		boolean changed = false
		Project project = sheetInfoOpts.project

		// Ignore comments
		if(NumberUtil.isPositiveLong(task.taskNumber)){

			AssetComment assetComment = AssetComment.findByTaskNumberAndProject(task.taskNumber, project)
			if (assetComment) {
				log.debug "applyTaskChanges() About to apply changes to $assetComment"

				// Update the person with the values passed in
				applyChangesToDomainObject(assetComment, task, sheetInfoOpts, true, true )

				changed = assetComment.dirtyPropertyNames

				if (changed) {
					recordChangeHistory(task.changeHistory, assetComment, sheetInfoOpts)

					if (!assetComment.save(failOnError: false)) {
						task.changeHistory = null
						error = GormUtil.allErrorsString(assetComment)
						assetComment.discard()
					}
				}
			}
		}
		return [error, changed]
	}

	/**
	 * This method clears the references to MoveEvents and persons
	 * used when updating tasks. This has to be done before saving
	 * the result as JSON to avoid StackOverflow and redundant data.
	 */
	private void removeTmpReferences(List tasks){
		tasks.each{
			it.remove("_moveEvent")
			it.remove("_assignedTo")
		}
	}

	private String saveResultsAsJson(List tasks, String filename, Map sheetInfoOpts) {
		removeTmpReferences(tasks)
		tasks.each{ task ->
			taskSpreadsheetColumnMap.each{key, value ->
				task[key] = transformProperty(key, task[key], sheetInfoOpts)
			}
		}
		String fqfn = getJsonFilename(filename)
		log.debug "saveResultsAsJson() filename=$filename fqfn=$fqfn"
		new File(fqfn).write(new JsonBuilder(tasks).toPrettyString())
		return filename
	}

	/**
	 * Used to get the derived filename for the JSON file from the Excel filename plus the
	 * fully qualified path to access the file.
	 * @param filename - the spreadsheet filename
	 * @return the FQPN to the where the JSON file is written to
	 */
	private String getJsonFilename(String filename) {
		// Swap out the XLS? extension and replace with .json
		getFilenameWithPath(filename.substring(0, filename.lastIndexOf('.')) + '.json')
	}

	/**
	 * Used to determine if an error was previously recorded against a property
	 * @param task - the task map information
	 * @param property - the name of the property
	 * @return true if a previous error was recorded on the property
	 * @IntegrationTest
	 */
	private boolean propertyHasError(Map task, String property) {
		return task.containsKey("${property}${ERROR_SUFFIX}".toString())
	}

	/**
	 * Utility method used to transform a property where there is a tranform defined in the taskSpreadsheetColumnMap map for a property
	 * otherwise it just returns the current value
	 * @param propName - the name of the property
	 * @param value - the current value
	 * @param sheetInfoOpts - the Map that contains all the goodies for tranforming data
	 * @return the transformed or original value appropriately
	 */
	private transformProperty(String propName, value, Map sheetInfoOpts) {
		def transformedValue = null
		if (!taskSpreadsheetColumnMap.containsKey(propName)) {
			throw new RuntimeException("transformProperty called with invalid property name $propName")
		}

		if (taskSpreadsheetColumnMap[propName].containsKey('transform')) {
			transformedValue = taskSpreadsheetColumnMap[propName].transform(value, sheetInfoOpts)
		}
		else {
			transformedValue = (value == null) ? '' : value
		}
		return transformedValue
	}


	/**
	 * Used to populate the model with the necessary properties for the Results form
	 * @param formOptions
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    tasks - the tasks that were read from the spreadsheet
	 *    labels - the list column header labels used in the tasks list
	 *    properties - the list of the property names used in the tasks list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	Map generateModelForPostResults(Map formOptions) {

		Map model = [:]

		model.filename = formOptions.filename
		model.labels = getLabelsInColumnOrder('formPos')
		model.properties = getPropertiesInColumnOrder('formPos')
		model.gridMap = taskSpreadsheetColumnMap
		model.defaultSuffix = DEFAULTED_SUFFIX
		model.originalSuffix = ORIGINAL_SUFFIX
		model.errorSuffix = ERROR_SUFFIX

		return model
	}

	File generatePostResultsData(String filename, Map formOptions) {
		securityService.requirePermission(Permission.RecipeGenerateTasks, true)

		File file = new File(getJsonFilename(filename))
		if (!file.exists()) {
			throw new EmptyResultException('Unable to load the Task Import Post Results data')
		}

		return file
	}
}
