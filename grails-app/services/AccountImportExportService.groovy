/**
 * AccountImportExportService - A set of service methods the importing and exporting of project staff and users
 */

import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.WorkbookUtil
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import org.apache.commons.lang.StringUtils
import grails.transaction.*
import groovy.json.*
import java.text.SimpleDateFormat
import javax.servlet.http.HttpSession 

//import org.apache.commons.validator.routines.EmailValidator

class AccountImportExportService {

	static transactional = false

	def auditService
	def coreService
	def partyRelationshipService
	def personService
	def projectService
	def securityService	

	static final String LOGIN_OPT_ALL = 'A'
	static final String LOGIN_OPT_ACTIVE = 'Y'
	static final String LOGIN_OPT_INACTIVE = 'N'

	static final String ACCOUNT_EXPORT_TEMPLATE = '/templates/AccountsImportExport.xls'
	static final String EXPORT_FILENAME_PREFIX = 'AccountExport'
	static final String IMPORT_FILENAME_PREFIX = 'AccountImport'


	static final String TEMPLATE_TAB_NAME  = 'Accounts'
	static final String TEMPLATE_TAB_TITLE = 'Title'
	static final String TEMPLATE_TAB_ROLES = 'Roles'
	static final String TEMPLATE_TAB_TEAMS = 'Teams'

	// Used to indicate the alternate property name with the original and defaulted values
	// that are stored in the account map (e.g. firstName, firstName_o and firstName_d)
	static final String ORIGINAL_SUFFIX = '_o'
	static final String DEFAULTED_SUFFIX = '_d'

	static final String IMPORT_OPTION_BOTH='B'
	static final String IMPORT_OPTION_PERSON='P'
	static final String IMPORT_OPTION_USERLOGIN='U'

	static final String DEFAULT_SECURITY_ROLE='USER'

	// Users can split Teams and Security roles on the follow characters as well as the default comma (,)
	static final List DELIM_OPTIONS = [';',':','|'] 

	// Used in the map below to set the various template strings used by Kendo
	static final changeTmpl = { prop ->
		"\"#= showChanges(data, '$prop') #\""
	}
	static final errorListTmpl = { prop ->
		"kendo.template(\$('#error-template').html())"
	}

	// Closures used for validating imported data
	static final validator_YN = { val, options ->
		boolean valid = false
		if (val && (val instanceof String)) {
			valid = ['Y','N'].contains(val.toUpperCase()) 
		}
		return valid
	}

	// Closure used to validate that a string can be parsed as a Date
	static final validator_date = { val, Map options -> 
		boolean valid = false
		if (val) {
			if (val instanceof Date) {
				valid=true
			} else {
log.debug "*** validator_date() val isa ${val?.getClass().getName()} and formatter isa ${options.dateFormatter?.getClass().getName()}"
				valid = TimeUtil.parseDate(val, options.dateFormatter) != null
			}
		}
		return valid
	}

	// Closure used to validate that a string can be parsed as a DateTime
	static final validator_datetime = { val, Map options -> 
		boolean valid = false
		if (val) {
			valid = TimeUtil.parseDateTime(options.sheetTzId, val, options.dateTimeFormatter) != null
		}
		return valid
	}

	static final xfrmToYN = { val, options ->
		(val != null && (val instanceof Boolean) ? val.asYN() : val) 
		return val
	}

	static final xfrmDateToString = { val, options ->
		(val != null && (val instanceof Date) ? TimeUtil.formatDate(val, options.dateFormatter) : val) 
		return val
	}

	// Transformer that is used to populate the spreadsheet using the user's TZ
	static final xfrmDateTimeToString = { val, options ->
		(val != null && (val instanceof Date) ? TimeUtil.formatDateTimeWithTZ(options.userTzId, val, options.dateTimeFormatter) : val) 
		return val
	}

	/*
	 * The following map is used to drive the Import and Export tables and forms. The properties consist of:
	 *    ssPos: 	the position that the property appears in the spreadsheet
	 *    formPos: 	the position that the property appears in the online form
	 * 	  type: 	P)erson, U)serLogin, T)ransient
	 *    width: 	the width in the online grid
	 *    locked: 	a flag to indicate that the column is locked (for horizontal scrolling)
	 *    label: 	the column heading label in the grid and export spreadsheet
	 */
	static final Map accountSpreadsheetColumnMap = [
		personId               : [type:'number',  ssPos:0,    formPos:1, domain:'I', width:50,  locked:true, label:'ID'],																	
		firstName              : [type:'string',  ssPos:1,    formPos:2, domain:'P', width:90,  locked:true, label:'First Name', 
									template:changeTmpl('firstName')],
		middleName             : [type:'string',  ssPos:2,    formPos:3,  domain:'P', width:90,  locked:true,  label:'Middle Name',
									 template:changeTmpl('middleName')],
		lastName               : [type:'string',  ssPos:3,    formPos:4, domain:'P', width:90,  locked:true,  label:'Last Name', 
								 	template:changeTmpl('lastName')],
		company                : [type:'string',  ssPos:4,    formPos:5, domain:'T', width:90,  locked:true,  label:'Company', 
									template:changeTmpl('company')],
		errors                 : [type:'list',    ssPos:null, formPos:6,  domain:'T', width:240, locked:false, label:'Errors', 
									template:errorListTmpl() ],
		workPhone              : [type:'string',  ssPos:5,    formPos:7,  domain:'P', width:100, locked:false, label:'Work Phone', 
									template:changeTmpl('workPhone')],
		mobilePhone            : [type:'string',  ssPos:6,    formPos:8,  domain:'P', width:100, locked:false, label:'Mobile Phone', 
									template:changeTmpl('mobilePhone')],
		email                  : [type:'string',  ssPos:7,    formPos:9,  domain:'P', width:100, locked:false, label:'Email', 
									template:changeTmpl('email')],
		title                  : [type:'string',  ssPos:8,    formPos:10, domain:'P', width:100, locked:false, label:'Title', 
									template:changeTmpl('title')],
		department             : [type:'string',  ssPos:9,    formPos:11, domain:'P', width:100, locked:false, label:'Department', 
									template:changeTmpl('department')],
		location               : [type:'string',  ssPos:10,   formPos:12, domain:'P', width:100, locked:false, label:'Location/City', 
									template:changeTmpl('location')],
		stateProv              : [type:'string',  ssPos:11,   formPos:13, domain:'P', width:100, locked:false, label:'State/Prov', 
									template:changeTmpl('stateProv')],
		country                : [type:'string',  ssPos:12,   formPos:14, domain:'P', width:100, locked:false, label:'Country', 
									template:changeTmpl('country')],
		personTeams            : [type:'list',    ssPos:13,   formPos:15, domain:'T', width:150, locked:false, label:'Person Team(s)', 
									template:changeTmpl('personTeams')],
		projectTeams           : [type:'list',    ssPos:14,   formPos:15, domain:'T', width:150, locked:false, label:'Project Team(s)', 
									template:changeTmpl('projectTeams')],
		roles                  : [type:'list',    ssPos:15,   formPos:17, domain:'T', width:100, locked:false, label:'Security Role(s)', 
									template:changeTmpl('roles'), defaultValue: DEFAULT_SECURITY_ROLE],
		username               : [type:'string',  ssPos:16,   formPos:18, domain:'U', width:120, locked:false, label:'Username', 
									template:changeTmpl('username')],
		isLocal                : [type:'boolean', ssPos:17,   formPos:19, domain:'U', width:100, locked:false, label:'Local Account?', 
									template:changeTmpl('isLocal'), defaultValue: 'Y', validator: validator_YN, transform: xfrmToYN],
		active                 : [type:'string',  ssPos:18,   formPos:20, domain:'U', width:100, locked:false, label:'Login Active?', 
									template:changeTmpl('active'), defaultValue: 'N', validator: validator_YN],
		expiryDate             : [type:'date',  ssPos:19,   formPos:21, domain:'U', width:100, locked:false, label:'Account Expiration', 
									template:changeTmpl('expiryDate'), transform:xfrmDateToString, validator:validator_date],
		passwordExpirationDate : [type:'date',    ssPos:20,   formPos:22, domain:'U', width:100, locked:false, label:'Password Expiration', 
									template:changeTmpl('passwordExpirationDate'), transform:xfrmDateToString, validator:validator_date],
		passwordNeverExpires   : [type:'boolean', ssPos:21,   formPos:23, domain:'U', width:100, locked:false, label:'Pswd Never Expires?', 
									template:changeTmpl('passwordNeverExpires'), defaultValue: 'N', validator: validator_YN],
		forcePasswordChange    : [type:'string',  ssPos:22,   formPos:24, domain:'U', width:100, locked:false, label:'Force Chg Pswd?', 
									template:changeTmpl('forcePasswordChange'), defaultValue: 'N', validator: validator_YN ],
		lastLogin              : [type:'datetime',ssPos:23,   formPos:24, domain:'T', width:100, locked:false, label:'Last Login (readonly)'],
		match                  : [type:'list',    ssPos:null, formPos:25, domain:'T', width:100, locked:false, label:'Matched On']
	]

	// The map of the location of the various properties on the Title page of the Account Spreadsheet
	static final Map TitlePropMap = [
		 clientName: [1,3, 'String'],
		  projectId: [1,4, 'Integer'],
		projectName: [2,4, 'String'],
		 exportedBy: [1,5, 'String'],
		 exportedOn: [1,6, 'Datetime'],
		   timezone: [1,7, 'String'],
		 dateFormat: [1,8, 'String']
	]

	/*********************************************************************************************************
	 ** Controller methods
	 *********************************************************************************************************/

	/** 
	 * Used to load a blank import template that updates the title sheet and then downloads the file to the 
	 * end user.
	 * @param response - the HttpResponse object
	 * @param session - the HttpSession object
	 * @param byWhom - the user that is attempting to download the spreadsheet template
	 * @param project - the user's currently selected project 
	 * @param filename - the name of the file that the download should have for the mime-type
	 */
	void generateImportTemplateToBrowser(response, session, UserLogin byWhom, Project project, String filename) {
		HSSFWorkbook workbook = getAccountExportTemplate()

		Map sheetOptions = getUserPreferences(session)

		updateSpreadsheetTitleTab(byWhom, project, workbook, sheetOptions)

		sendSpreadsheetToBrowser(response, workbook, filename)

	}

	/**
	 * Used to load the spreadsheet into memory and validate that the information is correct
	 * @param byWhom   - the user that is making the request
	 * @param project  - the project that the import is being applied against
	 * @param filename - the name of the temporarilly saved spreadsheet
	 * @param options  - the options that the user chose when submitting the form
	 * @controllerMethod
	 */
	List loadAndValidateSpreadsheet(session, UserLogin byWhom, Project project, String filename, Map formOptions) {
		// Load the spreadsheet
		HSSFWorkbook workbook = readImportSpreadsheet(filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(session, project, workbook)

		return validateSpreadsheetContent(byWhom, project, workbook, sheetInfoOpts, formOptions)
	}

	/**
	 * This method is used to load the spreadsheet into memory and validate that it contains some information. If 
	 * successful it will save the file with a random name and then return the model containing the filename.
	 * @param byWhom - the user that is making the request
	 * @param project - the project that the import is being applied against
	 * @param request - the servlet request object
	 * @param fileParamName - the servlet request params name of the var that references the upload spreadsheet file
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    people - the accounts that were read from the spreadsheet
	 *    labels - the list column header labels used in the accounts list
	 *    properties - the list of the property names used in the accounts list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	Map importAccount_Step1_Upload(session, UserLogin byWhom, Project project, Object request, String fileParamName) {
		Map model = [:]

		// Handle the file upload
		def file = request.getFile(fileParamName)
		if (file.empty) {
			throw new EmptyResultException('The file you uploaded appears to be empty')
		}

		// Save the spreadsheet file and then read it into a HSSFWorkbook
		model.filename = saveImportSpreadsheet(request, byWhom, fileParamName)
		HSSFWorkbook workbook = readImportSpreadsheet(model.filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(session, project, workbook)

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
			String fqfn=getFilenameWithPath(model.filename)
			if (! new File(fqfn).delete()) {
				log.error "Unable to delete temporary account import worksheet $fqfn"
			}
			throw new InvalidParamException('The spreadsheet column headers did not match the expected format. Please '+ 
				' export a new template before attempt an import.')
		}

		// Read in the accounts just to see that we're able to without errors
		List accounts = readAccountsFromSpreadsheet(workbook, sheetInfoOpts)

		if (!accounts) {
			throw new EmptyResultException('Unable to read the spreadsheet or the spreadsheet was empty')
		}

		return model
	}

	/**
	 * Used to populate the model with the necessary properties for the Review form
	 * @param byWhom - the user that is making the request
	 * @param project - the project that the import is being applied against
	 * @param request - the servlet request object
	 * @param filename - the filename of the locally saved spreadsheet
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    people - the accounts that were read from the spreadsheet
	 *    labels - the list column header labels used in the accounts list
	 *    properties - the list of the property names used in the accounts list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	Map importAccount_Step2_Review(session, UserLogin byWhom, Project project, Object request, Object params) {
		Map model = [:]
		Map optionLabels = [
			(IMPORT_OPTION_BOTH): 'Person and UserLogin',
			(IMPORT_OPTION_PERSON): 'Person only',
			(IMPORT_OPTION_USERLOGIN): 'UserLogin only'
		]

		String processOption = params.processOption
		if (! processOption || ! optionLabels.containsKey(processOption)) {
			log.debug "params=$params"
			throw new InvalidParamException('The import option must be specified')
		}

		model.filename = params.filename
		model.labels = getLabelsInColumnOrder('formPos')
		model.properties = getPropertiesInColumnOrder('formPos')
		model.gridMap = accountSpreadsheetColumnMap
		model.defaultSuffix = DEFAULTED_SUFFIX
		model.originalSuffix = ORIGINAL_SUFFIX
		model.processOptionDesc = optionLabels[processOption]

		return model
	}

	/**
	 * Used to populate the model with the necessary properties for the Review form
	 * @param byWhom - the user that is making the request
	 * @param project - the project that the import is being applied against
	 * @param options - the form options that the user selected for how to update account/user
	 * @param filename - the filename of the locally saved spreadsheet
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    people - the accounts that were read from the spreadsheet
	 *    labels - the list column header labels used in the accounts list
	 *    properties - the list of the property names used in the accounts list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	@Transactional	
	Map importAccount_Step3_PostChanges(session, UserLogin byWhom, Project project, Object formOptions) {

		HSSFWorkbook workbook = readImportSpreadsheet(formOptions.filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(session, project, workbook)

		// Read in the accounts and then validate them
		// List accounts = loadAndValidateSpreadsheet(user, project, formOptions.filename, formOptions)
		List accounts = validateSpreadsheetContent(byWhom, project, workbook, sheetInfoOpts, formOptions)
		if (!accounts) {
			throw new EmptyResultException('Unable to read the spreadsheet or the spreadsheet was empty')
		}

		Map results = [
			failedPerson: [],
			skippedPerson: 0,
			createdPerson: 0,
			updatedPerson: 0,
			unchangedPerson: 0,
			teamsUpdated: 0
		]
		
		for(int i=0; i < accounts.size(); i++) {
			accounts[i].postErrors = []

			// Skip over the accounts that have errors from the validation process
			if (accounts[i].errors){
				results.skippedPerson++
				continue
			}
			
			// Create / Update the persons
			if (options.flagToUpdatePerson) {
				def (error, changed) = addOrUpdatePerson(user, accounts[i], sheetInfoOpts, formOptions)

				log.debug "importAccount_Step3_PostChanges() call to addOrUpdatePerson() returned person=${accounts[i].person}, error=$error, changed=$changed"
				if (error) {
					accounts[i].postErrors << error
					results.failedPerson << "Row ${i+2} $error"
				} else {
					if (accounts[i].isNewAccount) {
						results.createdPerson++
					}

					// Update teams
					def teamChanged = addOrUpdateTeams(user, accounts[i], project, options)
					if (teamChanged) {
						results.teamsUpdated++
					}

					if (changed || teamChanged) {
						results.updatedPerson++
					} else {
						results.unchangedPerson++
					}
				}
			}

			// Deal with the userLogin
			if (options.flagToUpdateUserLogin) {

			}

		}

		// Throw an exception so we don't commit the data while testing (to be removed)
		throw new DomainUpdateException(results.toString())

		return results	
	}

	/*********************************************************************************************************
	 ** Helper Methods
	 *********************************************************************************************************/

	/**
	 * This map is used to provide the user preferences for formatting date/times that will be used in rendering and reading the spreadsheet
	 * @return The map that contains:
	 *		tzId - the user's timezone id
	 *		dateFormat - the date format used for outputing and parse date properties
	 *		dateTimeFormat - the datetime format used for outputing and parse datetime properties
	 */
	private Map getUserPreferences(HttpSession session) {
		Map map = [ 
			userTzId: TimeUtil.getUserTimezone(session),
			userDateFormat: TimeUtil.getUserDateFormat(session)
		]

		// Get the appropriate date and datetime formatter based on the user's preferences for middle or little endian date formats
		map.dateFormatter = TimeUtil.createFormatterForType(map.userDateFormat, TimeUtil.FORMAT_DATE)
		map.dateTimeFormatter = TimeUtil.createFormatterForType(map.userDateFormat, TimeUtil.FORMAT_DATE_TIME_22)

		if (! map.dateFormatter ) {
			throw new RuntimeException("Unable to load Date formatter for ${map.dateFormat}")
		}
		if (! map.dateTimeFormatter) {
			throw new RuntimeException("Unable to load DateTime formatter for ${map.dateTimeFormat}")
		}

		return map
	}

	/**
	 * Used to read in the TitleSheet information and load the TZ and Date/DateTime formatters used for most of the 
	 * application.
	 * @param session 
	 * @param project
	 * @param workbook
	 * @return A map containing the values from the title page plus the userTzId, and userDateFormat and formatters
	 */
	private Map getSheetInfoAndOptions(session, Project project, HSSFWorkbook workbook) {

		// Collect the details off of the title sheet including the project id, exportedOn and the timezone when the data was exported
		Map sheetInfoOpts = getUserPreferences(session)

		readTitleSheetInfo(project, workbook, sheetInfoOpts)
	
		return sheetInfoOpts
	}

	/**
	 * Used to determine based on user import options if the Person should be updated
	 * @param options - the map of the form params
	 * @return true if the user selected the correct options to update the Person otherwise false
	 */
	private boolean shouldUpdatePerson(Map options) {
		return [IMPORT_OPTION_PERSON, IMPORT_OPTION_BOTH].contains(options.processOption)
	}

	/**
	 * Used to determine based on user import options if the UserLogin should be updated
	 * @param options - the map of the form params
	 * @return true if the user selected the correct options to update the UserLogin otherwise false
	 */
	private boolean shouldUpdateUserLogin(Map options) {
		return [IMPORT_OPTION_USERLOGIN, IMPORT_OPTION_BOTH].contains(options.processOption)
	}

	/**
	 * Used to get the labels in column order
	 */
	List getLabelsInColumnOrder(type) {
		assert ['ssPos', 'formPos'].contains(type)

		Map subMap = accountSpreadsheetColumnMap.findAll { it.value.get(type) > 0 }
		List list = subMap.collect { prop, info ->  [info.get(type), info.get('label')] }
		list.sort { it[0] }
		list = list.collect { it[1] }

		return list
	}

	/**
	 * Used to get the property names of the accountSpreadsheetColumnMap in the column order
	 */
	List getPropertiesInColumnOrder(type) {
		Map subMap = accountSpreadsheetColumnMap.findAll { it.value.get(type) > 0 }
		List list = subMap.collect { prop, info ->  [info.get(type), prop] }
		list.sort { it[0] }
		list = list.collect { it[1] }

		return list
	}

	/**
	 * Used to load the Import request parameters from params into a Map that has been validated and formatted
	 * @param params - the request parameters
	 * @return the map of the parameters
	 */
	Map importParamsToOptionsMap(params) {
		Map options = [ 
			processOption:params.processOption,
		]

		options.flagToUpdatePerson = shouldUpdatePerson(options)
		options.flagToUpdateUserLogin = shouldUpdateUserLogin(options)

		if (params.filename) {
			options.filename = params.filename
		}
		return options
	}

	/**
	 * Used to reverse the Import options Map back into something that can be used as params for a page request
	 * @param options - the map created by the importParamsToOptionsMap method
	 * @param a map that can be used with requests
	 */
	Map importOptionsAsParams(Map options) {
		Map params = [processOption: options.processOption]
		if (options.filename) {
			params.filename = options.filename
		}

		return params
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
			options.expireDays = NumberUtils.toPositiveLong(options.expireDays, -1)
			if (expireDays == -1) {
				errors << 'The expiry days value must be a positive number'
			}
		}

		// TODO : JPM 4/2016 : Need to determine what else to validate from the user import
		// The userRoles - now a SELECT in the form but should still validate (should NOT exceed user's limit)
		// Check if the user has perms to create/edit users

		return errors
	}

	/**
	 * Used to assign the correct icon to the individual accounts based on the state of the account
	 * @param accounts - the list of account maps
	 */
	private void setIconsOnAccounts(List accounts) {
		for (int i=0; i < accounts.size() ; i++) {
			String icon = 'pencil.png'
			if (accounts[i].errors) {
				icon = 'exclamation.png'
			} else if (accounts[i].isNewAccount) {
				icon = 'add.png'				
			}
			accounts[i].icon = HtmlUtil.resource([dir: 'icons', file: icon, absolute: false])
		}
	}

	/**
	 * Used to set the appropriate property on the account Map to indicate that a defaulted value
	 * was used on the Account. This also sets the actual property value since it is defaulting afterall.
	 * @param account - the Map with all of the account information
	 * @param property - the name of the property to set 
	 * @param value - the value to set the defaulted field to
	 */
	private void setDefaultedValue(Map account, String property, value) {
		setValue(account, DEFAULTED_SUFFIX, property, value)
		account[property] = value
	}

	/**
	 * Used to set the appropriate property on the account Map to indicate that the original value is being
	 * changed on the Account.
	 * @param account - the Map with all of the account information
	 * @param property - the name of the property to set 
	 * @param value - the original value that the domain object had
	 */
	private void setOriginalValue(Map account, String property, value) {
		setValue(account, ORIGINAL_SUFFIX, property, value)
	}

	/**
	 * Used by setDefaultedValue and setOriginalValue to do the bulk of the work. You shouldn't be calling this
	 * method directly.
	 * @param account - the Map with all of the account information
	 * @param suffix - the suffix that is tacked onto the end of the variable name
	 * @param property - the name of the property to set 
	 * @param value - the value to set the defaulted field to
	 */
	private void setValue(Map account, String suffix, String property, value) {
		String prop = "$property$suffix".toString()
		account[prop] = value
	}
	
	/** 
	 * Used to retrieve a blank Account Export Spreadsheet
	 * @return The blank spreadsheet
	 */
	HSSFWorkbook getAccountExportTemplate() {
		// Load the spreadsheet template and populate it
		String templateFilename = ACCOUNT_EXPORT_TEMPLATE
        HSSFWorkbook spreadsheet = ExportUtil.loadSpreadsheetTemplate(templateFilename)
        updateSpreadsheetHeader(spreadsheet)
        addRolesToSpreadsheet(spreadsheet)
        addTeamsToSpreadsheet(spreadsheet)

        return spreadsheet
	}

	/**
	 * Used to output a spreadsheet to the browser
	 * @param response - the servlet response object
	 * @param spreadsheet - the spreadsheet object
	 * @param filename - the filename that it should be saved as on the client
	 */ 
	void sendSpreadsheetToBrowser(Object response, HSSFWorkbook spreadsheet, String filename) {
		ExportUtil.setExcelContentType(response, filename)
		spreadsheet.write( response.getOutputStream() )
		response.outputStream.flush()
	}

	/**
	 * Used to generate a spreadsheet of project staff and optionally their login information
	 * @param controller - the controller from which this method is being invoked
	 * @param byWhom - the user that invoked the method
	 * @param project - the user's project context
	 * @param formOptions - A map of the form variables submitted by the user
	 * @permission PersonExport 
	 */
	HSSFWorkbook generateAccountExportSpreadsheet(Object session, UserLogin byWhom, Project project, Map formOptions) {
		if (!project) {
			return
		}

		List persons = []

		// Get the staff for the project
		switch (formOptions.staffType) {
			case 'CLIENT_STAFF':
				persons = partyRelationshipService.getAllCompaniesStaffPersons([project.client])
				break

			case 'AVAIL_STAFF':
				// TODO : JPM 4/2016 : This needs to be reviewed because we're not returning all of the correct accounts
				Long companyId = project.client.id
				
				// persons = partyRelationshipService.getAllCompaniesStaffPersons(Party.findById(companyId))
				persons = projectService.getAssignableStaff(project, byWhom.person)
				break

			case 'PROJ_STAFF':
				persons = projectService.getStaff(project)
				break

			default:
				throw new InvalidParamException("The staffing type parameter was invalid")
		}

		if (! persons) {
			throw new EmptyResultException('No accounts were found for given filter')
		}

		log.debug "generateAccountExportSpreadsheet() found $persons.size() staff to export"

		Map sheetInfoOpts = getUserPreferences(session)

		def workbook = getAccountExportTemplate()

		populateAccountSpreadsheet(byWhom, project, persons, workbook, formOptions, sheetInfoOpts)

		return workbook
	}

	/**
	 * This method will iterate over the list of persons and populate the spreadsheet appropriately
	 * @param byWhom - the user that is making the request for the spreadsheet
	 * @param project - the project associated to the accounts being exported
	 * @param persons - the list of persons to be exported
	 * @param workbook - the spreadsheet workbook to be populated
	 * @param formOptions - a map of options from the user input fomr 
	 * @param sheetInfoOpts - a map of options used in formating dates in the sheet
	 */
	private void populateAccountSpreadsheet( 
		UserLogin byWhom, 
		Project project, 
		List persons, 
		HSSFWorkbook workbook, 
		Map formOptions,
		Map sheetInfoOpts) {

		List elapsedNow = [new Date()]

		updateSpreadsheetTitleTab(byWhom, project, workbook, sheetInfoOpts)

		// log.debug "updateSpreadsheetTitleTab took ${ TimeUtil.elapsed(elapsedNow) }"

		def sheet = workbook.getSheet(TEMPLATE_TAB_NAME)

		Date now = new Date()
		int row = 1
		int max = persons.size()
		persons.eachWithIndex{ person, index ->
			if (row % 10 == 0) {
				log.info "Exported $row staff records of $max"
			}
			Map account = personToFieldMap(person, project, sheetInfoOpts)

		//log.debug "personToFieldMap took ${ TimeUtil.elapsed(elapsedNow) }"


			if (formOptions.includeLogin=='Y') {
				UserLogin userLogin = person.userLogin
				if (userLogin) {
					boolean includeLogin = formOptions.loginChoice=='0'
					if (!includeLogin) {
						boolean isActive = userLogin.userActive()
						if ((formOptions.loginChoice=='1' && isActive) || (formOptions.loginChoice=='2' && !isActive)) {
							includeLogin = true
						}
					}
					if (includeLogin) {
						log.debug "populateAccountSpreadsheet() calling userLoginToFieldMap with sheetInfoOpts=$sheetInfoOpts"	
						
						Map userMap = userLoginToFieldMap(userLogin, sheetInfoOpts)
						// log.debug "userMap = $userMap"
						account.putAll(userMap)
						// log.debug "userLoginToFieldMap took ${ TimeUtil.elapsed(elapsedNow) }"
					}

				}
			}

			// Now that we have the map, we can iterate over the account map
			addRowToAccountSpreadsheet(sheet, account, row++, sheetInfoOpts)
		
			// log.debug "addRowToAccountSpreadsheet took ${ TimeUtil.elapsed(elapsedNow) }"

		}
	}	

	/**
	 * This method outputs all the account mapped fields to a row in the sheet
	 * @param sheet - the spreadsheet to update
	 * @param account - the map of the account properties
	 * @param rowNumber - the row in the spreadsheet to insert the values 
	 */
	private void addRowToAccountSpreadsheet(sheet, Map account, int rowNumber, Map sheetInfoOpts) {
		// Loop through the SpreadSheet Map and add to the cells
		accountSpreadsheetColumnMap.each { prop, info ->
			def colPos = info.ssPos
			if (colPos != null) {
				def val = account[prop]
				if (val && info.transform) {
					val = info.transform(val, sheetInfoOpts)
				}
				WorkbookUtil.addCell(sheet, colPos, rowNumber, val)
			}
		}
	}

	/**
	 * This method is used to update the header labels on the spreadsheet to match the mapping table
	 * @param sheet - the spreadsheet to update
	 */
	private void updateSpreadsheetHeader(sheet) {
		def tab = sheet.getSheet(TEMPLATE_TAB_NAME)
		accountSpreadsheetColumnMap.each { prop, info ->
			def colPos = info.ssPos
			if (colPos != null) {
				WorkbookUtil.addCell(tab, colPos, 0, info.label)
			}
		}
	}

	/**
	 * This method is used to update the spreadsheet title tab with various information about the export that 
	 * will come in handy on a subsequent import.
	 * @param session - the servlet container session 
	 * @param byWhom - the user that is requesting the export
	 * @param project - the project that this export is for
	 * @param sheet - the spreadsheet to update
	 */
	private void updateSpreadsheetTitleTab(UserLogin byWhom, Project project, sheet, sheetOptions) {
		def tab = sheet.getSheet(TEMPLATE_TAB_TITLE)

		if (!tab) {
			throw new EmptyResultException("The $TEMPLATE_TAB_TITLE sheet is missing from the workbook")
		}

		def exportedOn = TimeUtil.formatDateTimeWithTZ(sheetOptions.userTzId, sheetOptions.dateTimeFormat, new Date())

		def addToCell = { prop, val ->
			if (! TitlePropMap.containsKey(prop)) {
				throw new RuntimeException("updateSpreadsheetTitleTab() referenced invalid element '$prop' of TitlePropMap")
			}
			WorkbookUtil.addCell(tab, TitlePropMap[prop][0], TitlePropMap[prop][1], val)
		}

		addToCell('clientName', project.client.toString())

		addToCell('projectId', project.id.toString())
		addToCell('projectName', project.name.toString())
		addToCell('exportedBy', byWhom.person.toString())
		addToCell('exportedOn', exportedOn)
		addToCell('timezone', sheetOptions.userTzId)
		addToCell('dateFormat', sheetOptions.userDateFormat)
	}

	/**
	 * This method is used to read the spreadsheet title tab that should have various information from the export that 
	 * is required for the import process (e.g. timezone and exportedOn)
	 * @param project - the project that this export is for
	 * @param sheet - the spreadsheet to update
	 */
	 //HSSFWorkbook
	private Map readTitleSheetInfo(Project project, workbook, Map sheetOpts) {

		def sheet = workbook.getSheet(TEMPLATE_TAB_TITLE)
		if (!sheet) {
			throw new InvalidRequestException("The spreadsheet was missing the '${TEMPLATE_TAB_TITLE}' sheet")
		}

		Map map = [:]

		// Helper closure that will get cell values from the sheet based on the col/row mapping in the TitlePropMap MAP
		// Note that the Datetime case will depend on the map.timezone property read from the spreadsheet
		def getCell = { prop ->
			def val
			String type = TitlePropMap[prop][2]
			// log.debug "getCell($prop,$type)"
			// log.debug "   for col ${TitlePropMap[prop][0]}, row ${TitlePropMap[prop][1]}"
			switch (type) {
				case 'Integer': 
					val = WorkbookUtil.getIntegerCellValue(sheet, TitlePropMap[prop][0], TitlePropMap[prop][1])
					break
				case 'Datetime':
					val = WorkbookUtil.getDateCellValue(sheet, TitlePropMap[prop][0], TitlePropMap[prop][1], sheetOpts.sheetTzId, sheetOpts.dateTimeFormatter)
					break
				case 'String':
					val = WorkbookUtil.getStringCellValue(sheet, TitlePropMap[prop][0], TitlePropMap[prop][1])
					break;
				default:
					throw new RuntimeException("readTitleSheetInfo.getCell had unhandled case for $type")
			} 
		}

		sheetOpts.sheetTzId = getCell('timezone')
		sheetOpts.sheetProjectId = getCell('projectId')
		sheetOpts.sheetDateFormat = getCell('dateFormat')

		// Note that the exportedOn property is dependent on timezone being previously loaded
		sheetOpts.sheetExportedOn = getCell('exportedOn')	
		if (sheetOpts.sheetExportedOn == -1) {
			log.error "*** readTitleSheetInfo() the Exported On wasn't properly read from the spreadsheet" 
			// TODO : JPM 4/2016 : Once this is solved then the exception can be re-enabled
			// throw new InvalidRequestException("Unable to parse the Exported On value from the '${TEMPLATE_TAB_TITLE}' sheet")			
		}

		return sheetOpts
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
		accountSpreadsheetColumnMap.each { prop, info ->
			def colPos = info.ssPos
			if (colPos != null) {
				def label = WorkbookUtil.getCell(tab, colPos, 0)
				if (info.label.toString() != label.toString()) {
					log.debug "validateSpreadsheetHeader() expected '${info.label}' but found '$label' for column ${WorkbookUtil.columnCode(colPos)}"
					ok = false
				}
			}
		}
		return ok
	}

	/**
	 * This method will read in all of the content from the accounts sheet and the invoke the validation logic
	 * @param byWhom - the user that is invoking the upload process
	 * @param project - the user's project in his context
	 * @param workbook - the spreadsheet that was loaded
	 * @param sheetInfoOpts - the map containing the information from the workbook title and the TZ/Date stuff
	 * @param formOptions - the user input params from the form
	 * @return The list of accounts mapped out
	 */
	List<Map> validateSpreadsheetContent(UserLogin byWhom, Project project, HSSFWorkbook workbook, Map sheetInfoOpts, Map formOptions) {

		// Read in the accounts and then validate them
		List accounts = readAccountsFromSpreadsheet(workbook, sheetInfoOpts)

		// Validate the sheet
		validateUploadedAccounts(byWhom, accounts, project, sheetInfoOpts, formOptions)

		return accounts
	}

	/**
	 * Used to write the teams to the spreadsheet Teams tab
	 * @param sheet - the spreadsheet to write to
	 */
	private void addTeamsToSpreadsheet(sheet) {
		def tab = sheet.getSheet('Teams')
		assert tab	
		List teams = RoleType.findAllByType(RoleType.TEAM, [order:'description'])
		int row = 1
		teams.each {t ->
			WorkbookUtil.addCell(tab, 0, row, t.id)
			WorkbookUtil.addCell(tab, 1, row++, t.toString())
		}
	}

	/**
	 * Used to write the teams to the spreadsheet Teams tab
	 * @param sheet - the spreadsheet to write to
	 */
	private void addRolesToSpreadsheet(sheet) {
		def tab = sheet.getSheet('Roles')
		assert tab		
		List roles = securityService.getAllRoles()
		//RoleType.findAllByType(RoleType.SECURITY, [order:'level'])
		int row = 1
		roles.each {r ->
			if (r.id == 'TEST_ROLE') 
				return
			WorkbookUtil.addCell(tab, 0, row, r.id)
			WorkbookUtil.addCell(tab, 1, row++, r.toString())
		}
	}

	/**
	 * Used to map a Person to the accountSpreadsheetColumnMap 
	 * @param person - the person to map to the AccountFieldMap format
	 * @return a map of the person information
	 */
	private Map personToFieldMap(Person person, Project project, Map sheetInfoOpts) {
		List enow = [new Date()]
		Map map = buildMapFromDomain(person, 'P', sheetInfoOpts)
		//log.debug "   buildMapFromDomain took ${TimeUtil.elapsed(enow)}"
		// Deal with transient properties that we can't handle through the map just yet...
		List personTeams = person.getTeamsCanParticipateIn().id
		
		//log.debug "   getTeamsCanParticipateIn took ${TimeUtil.elapsed(enow)}"
		List projectTeams = partyRelationshipService.getProjectStaffFunctionCodes(project, person)
		//log.debug "   getProjectStaffFunctionCodes took ${TimeUtil.elapsed(enow)}"
		List roles = securityService.getAssignedRoleCodes(person)
		//log.debug "   getAssignedRoleCodes took ${TimeUtil.elapsed(enow)}"

		map.personTeams  = personTeams.join(", ") 
		map.projectTeams = projectTeams.join(", ") 
		map.roles        = roles.join(", ")
		map.personId = person.id
		map.company = person.company.name

		return map
	}

	/**
	 * Used to map a Person to the accountSpreadsheetColumnMap format
	 * @param user - the UserLogin to map to the AccountFieldMap format
	 * @param sheetInfoOpts - a map that includes the tzId and date/time formats
	 * @return a map of the person information
	 */
	private Map userLoginToFieldMap(UserLogin user, Map sheetInfoOpts) {	
		Map map = buildMapFromDomain(user, 'U', sheetInfoOpts)

		// Handle Transient properties
		map.lastLogin = (user.lastLogin ? TimeUtil.formatDateTimeWithTZ(sheetInfoOpts.userTzId, user.lastLogin, sheetInfoOpts.dateTimeFormatter) : '')

		return map
	}

	/**
	 * Used to create a map of properties from a Domain object by using the accountSpreadsheetColumnMap 
	 * to determine the properties to fetch.
	 * @param domainObj - the object to pull the values from (Person, UserLogin)
	 * @param domainCode - the code for the domain property in the Map e.g. P)erson or U)ser
	 * @param sheetInfoOpts - a map containing the user tzId and date/time formats
	 * @return The map containing the values from the domain that are identified
	 */
	private Map buildMapFromDomain(Object domainObj, String domainCode, Map sheetInfoOpts) {
		Map map = [:]
		accountSpreadsheetColumnMap.each { prop, info ->
			if (info.domain != domainCode) {
				return
			}
			switch (info.type) {
				case 'date':
					map[prop] = (domainObj[prop] ? TimeUtil.formatDate(domainObj[prop], sheetInfoOpts.dateFormatter) : '')
					break
				case 'datetime':
					map[prop] = (domainObj[prop] ? TimeUtil.formatDateTimeWithTZ(sheetInfoOpts.userTzId, domainObj[prop], sheetInfoOpts.dateTimeFormatter) : '')
					break
				case 'boolean':
					map[prop] = (domainObj[prop].asYN())
					break
				default:	
					map[prop] = domainObj[prop]
					break
			}
		}
		return map
	}

	/**
	 * Used to transform a value coming from the spreadsheet into the format that the domain object is 
	 * expected based on the definition in accountSpreadsheetColumnMap. 
	 * @param property - the name of the property
	 * @param value - the value to transform
	 * @return the transformed value
	 */
	private Object transformValueToDomainType(String property, value, Map sheetInfoOpts) {
		String type = accountSpreadsheetColumnMap[property]?.type
		if (! type) {
			throw RuntimeException("Unable to find '$property' in field definition map")
		}
		if (value != null) {
			switch (type) {
				case 'boolean':
					value = value.asBoolean()
					break
				case 'date':
					value = (value instanceof Date ? value : TimeUtil.parseDate(value, sheetInfoOpts.dateFormatter))
					break
				case 'datetime':
					value = (value instanceof Date ? value : TimeUtil.parseDateTimeWithFormatter(sheetInfoOpts.sheetTzId, value, sheetInfoOpts.dateTimeFormatter))
					break
			}
		}
		// log.debug "*** transformValueToDomainType() property=$property, type=$type, $value isa ${value?.getClass()?.getName()}"
		return value
	}

	/**
	 * This is used to get the fully qualified filename with path of where the temporary files are written
	 * @param filename - the name of the file without a path
	 * @return the filename with the path prefix
	 */
	private String getFilenameWithPath(String filename) {
		String fqfn=coreService.getAppTempDirectory() + '/' + filename	
		return fqfn
	}

	/**
	 * Used to pull the uploaded file from the request and save it to a temporary file with a randomly generated
	 * name. After saving the file the filename and File handle are returned in a list.
	 * @param request - the servlet request object
	 * @param byWhom - the user that is saving the file (will use their id as part of the filename)
	 * @param paramName - the name of the form parameter that contains the upload file
	 * @return The name of the filename that was saved (excluding the path)
	 */
	String saveImportSpreadsheet(Object request, UserLogin byWhom, String paramName) {
		MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
		CommonsMultipartFile xlsFile = ( CommonsMultipartFile )mpr.getFile(paramName)
		
		// Generate a random filename to store the spreadsheet between page loads
		String filename = "AccountImport-${byWhom.id}-" + com.tdsops.common.security.SecurityUtil.randomString(10)+'.xls'

		// Save file locally
		String fqfn=getFilenameWithPath(filename)
		log.info "saveImportSpreadsheet() user $byWhom uploaded AccountImport spreadsheet to $fqfn"	

		File localFile = new File(fqfn)
		xlsFile.transferTo(localFile)

		return filename
	}

	/**
	 * Used to read a spreadsheet from the file system into a HSSFWorkbook which is returned
	 * @param filename - the filename to spreadsheet (which assumes it is in the app configured tmp directory)
	 * @return the spreadsheet itself
	 */
	HSSFWorkbook readImportSpreadsheet(String filename) {
		if (! filename) {
			throw new InvalidParamException('The import filename parameter was missing')
		}

		String fqfn = "${coreService.getAppTempDirectory()}/$filename"
		File file = new File(fqfn)
		HSSFWorkbook xlsWorkbook = new HSSFWorkbook(new FileInputStream(file))
		return xlsWorkbook
	}

	/**
	 * Used to read the Account Import Spreadsheet and load up a list of account+user properties. This will 
	 * iterate over the accountSpreadsheetColumnMap Map to pluck the values out of the appropriate columns of
	 * each row and add to the map that is returned for each person/userlogin. The values are just saved in their
	 * String type and will be manipulated later.
	 * @param spreadsheet - the spreadsheet to read from
	 * @return the list that is read in
	 */
	private List<Map> readAccountsFromSpreadsheet(spreadsheet, Map sheetInfoOpts) {

		int firstAccountRow = 1
		def sheet = spreadsheet.getSheet( TEMPLATE_TAB_NAME )
		int lastRow = sheet.getLastRowNum()
		List accounts = []

		for (int row = firstAccountRow; row <= lastRow; row++) {
			Map account = [:]
			int pIdx = 0
			accountSpreadsheetColumnMap.each { prop, info ->
				Integer colPos = info.ssPos
				def value
				if (colPos != null) {
					switch (info.type) {
						case 'string':
						case 'boolean':
						case 'list':
							value = WorkbookUtil.getStringCellValue(sheet, colPos, row).trim()
							break
						case 'datetime': 
							value = WorkbookUtil.getDateCellValue(sheet, colPos, row, sheetInfoOpts.sheetTzId, sheetInfoOpts.dateTimeFormatter)
							break
						case 'date':
							value = WorkbookUtil.getDateCellValue(sheet, colPos, row, sheetInfoOpts.sheetTzId, sheetInfoOpts.dateFormatter)
							break
					}
					account[prop] = value
				}
			}
			account.errors = []
			account.match = []
			accounts.add(account)
		}

		return accounts
	}

	/**
	 * Used to render a list of error messages from the Gorm constraints violations
	 * @param domainObj - the domain object to generate the list of errors on
	 * @return list of errors
	 */
	private List gormValidationErrors(domainObj) {
		List msgs = []
		domainObj.errors.allErrors.each {
			msgs << "${it.getField()} error ${it.getCode()}"
		}
		return msgs
	}

	/**
	 * Used to validate the list of accounts that were uploaded and will populate the individual maps with 
	 * properties errors when anything is found. The logic will update the accounts object that is passed into
	 * the method.  The logic will update the accounts data with the following information:
	 *    - set companyObj to the company
	 *    - set companyId to the id of the company (seems redundant...)
	 *    - set person properties with ORIGINAL_SUFFIX suffix if the value has been changed from the original
	 *    - set isNewAccount to indicate if the person is new or not
	 *    - set default values 
	 *    - load in information from the pre-existing person and userLogin domains
	 *    - appends any error messages to errors list
	 * @param byWhom - the user invoking the upload
	 * @param accounts - the list of accounts that are read from the spreadsheet
	 * @param project - the user's project in their context
	 * @param sheetInfoOpts - the worksheet title page info plus the TZ/Date stuff
	 * @param formOptions - form params from the page submission
	 */
	private void validateUploadedAccounts(UserLogin byWhom, List<Map> accounts, Project project, Map sheetInfoOpts, Map formOptions) {

		List usernameList
		List validRoleCodes
		
		// TODO : JPM 4/2016 : readAccountsFromSpreadsheet - need to check the person last modified against the spreadsheet time

		// Get the list of roles that the byWhom can assign to others
		List authorizedRoleCodes = securityService.getAssignableRoleCodes(byWhom.person)

		// def emailValidator = EmailValidator.getInstance()

		List personIdList = accounts.findAll { it.personId }.collect {it.personId }

		// Get all of the email address that were uploaded
		List emailList = accounts.findAll( { it.email })?.collect({it.email.toLowerCase()})
		
		List allTeamCodes

		PartyGroup client = project.client
		Map companiesByNames = projectService.getCompaniesMappedByName(project)
		Map companiesById = projectService.getCompaniesMappedById(project)

		if (formOptions.flagToUpdateUserLogin) {
			// Retrieves all the roles that this user is allowed to assign.
			authorizedRoleCodes = securityService.getAssignableRoleCodes(byWhom.person)
			validRoleCodes = securityService.getAllRoleCodes()

			// Get all of the usernames that were uploaded
			usernameList = accounts.findAll( { it.username })?.collect({it.username.toLowerCase()})
		}

		if (formOptions.flagToUpdatePerson) {
			// Get all teams except AUTO and we need to stuff STAFF into it
			allTeamCodes = partyRelationshipService.getTeamCodes()
			allTeamCodes << 'STAFF'
			log.debug "allTeamCodes = $allTeamCodes"
		}

		// Validate the teams, roles, company and any other things need be validated
		for (int i=0; i < accounts.size(); i++) {
			accounts[i].errors = []

			Boolean found
			Person personById, personByEmail, personByUsername, personByName

			// Attempt to find the person by the personId
			if (fetchPersonByPersonId(accounts[i])) {
				personById = accounts[i].person
			}

			// Attempt to find the person by their email
			if (fetchPersonByEmail(accounts[i])) {
				personByEmail = accounts[i].person
			}

			// Attempt to find the person by their username
			if (fetchPersonByUsername(accounts[i])) {
				personByUsername = accounts[i].person
			}

			// Now attempt to find by company and validate it matches the company of the person found above (if the case)
			validateCompanyName(accounts[i], project, companiesById)

			// Now attempt to find the person by their name
			List people = findPersonsByName(accounts[i])
			if (people.size() > 0) {
				if (people.size() > 1) {
					accounts[i].errors << 'Found multiple people by name'
				} else {
					personByName = people[0]
					// See if the name match is different than the above
					if (personById && personById.id != personByName.id) {
						accounts[i].errors << 'Account by name conflicts with ID'
					}
					if (personByUsername && personByUsername.id != personByName.id) {
						accounts[i].errors << 'Account by name conflicts with username'
					}
					if (personByEmail && personByEmail.id != personByName.id) {
						accounts[i].errors << 'Account by name conflicts with email'
					}
					if (!accounts[i].errors && ! accounts[i].person) {
						accounts[i].person = personByName
						accounts[i].match << 'name'
					}
				}
			}

			// Set a flag on the account if it is going to be a new account
			if (accounts[i].person == null) {
				log.debug "validateUploadedAccounts() - creating blank Person"
				accounts[i].isNewAccount = true
				accounts[i].person = new Person()
			} else {
				accounts[i].isNewAccount = false
			}

			//
			// Now for som Person specific validation
			//
			if (formOptions.flagToUpdatePerson) {
				validatePerson(byWhom, accounts[i], sheetInfoOpts)

				// Get all teams except AUTO
				validateTeams(accounts[i], project, allTeamCodes) 
			}

			//
			// User specific validation
			//
			if (formOptions.flagToUpdateUserLogin) {
				
				// Validate user and security roles if user requested to update Users
				boolean canUpdateUser = accounts[i].username
				// TODO : JPM 4/2016 : check for new create user permission
				// Check if user is trying to create a user without a person already created 
				if (canUpdateUser && accounts[i].isNewAccount && !formOptions.flagToUpdatePerson) {
					accounts[i].errors << 'User can not be created without a saved person'
					canUpdateUser = false
				}

				if (canUpdateUser) {
					validateUserLogin(byWhom, accounts[i], project, sheetInfoOpts)

					// Validate that the teams codes are correct and map out what are to add and delete appropriately
					validateSecurityRoles(byWhom, accounts[i], validRoleCodes, authorizedRoleCodes)
				}	

			}

			// Checks for duplicate references in the spreadsheet for personId, email and username after
			// the above code potentially loaded some default data from pre-existing accounts. That can result
			// in duplicate people and/or userlogins.

			// Check for duplication person ids in the list
			String pid = accounts[i].person.id
			if (pid && personIdList.findAll{ it == pid }.size() > 1) {
				accounts[i].errors << 'Duplicate Person ID referenced'
			}

			// Check the username to make sure it isn't used by more than one row
			if (emailList.findAll{ it == accounts[i].email.toLowerCase() }.size()>1) {
				accounts[i].errors << 'Email referenced on multiple rows'
			}

			// Check the username to make sure it isn't used by more than one row
			if (usernameList.findAll{ it == accounts[i].username.toLowerCase() }.size()>1) {
				accounts[i].errors << 'Username referenced on multiple rows'
			}

			// List staff = partyRelationshipService.getCompanyStaff( project.client.id )
			// TODO : JPM 4/2016 : Should check if the user can see people unassigned to the project  
		}

		// Update all the accounts icons
		setIconsOnAccounts(accounts)
	}	

	/**
	 * Used to validate the UserLogin properties that were imported are valid and to update the account 
	 * Map with the appropriate values for tracking changes as well setting default values where applicable.
	 * If the account doesn't have an expiration date then it will attempt to get it from the project expiration 
	 * otherwise it will default N days in the future.
	 *
	 * The method will populate the following properties on the account map accordingly:
	 *    errors - appends any errors that occurred
	 * @param byWhom - the user that invoked the upload process
	 * @param account - the map used to track the person/user properties
	 * @param sheetInfoOpts - the workbook title sheet info + the TzID and date stuff
	 * @return returns true if the lookup was successful, false if not or Null if the routine err
	 */
	private Boolean validatePerson(UserLogin byWhom, Map account, Map sheetInfoOpts) {
		Boolean ok = false
		// Load a temporary Person domain object with the properties from the spreadsheet and see if any
		// of the valids will break the validation constraints
		Person.withNewSession { ses -> 
			Person personToValidate = (account.person?.id ? Person.get((account.person.id)) : new Person() )

			applyChangesToPerson(personToValidate, account, sheetInfoOpts, true)

			ok = personToValidate.validate() 
			if (! ok) {
				/*
				personToValidate.errors.allErrors.each {
					account.errors << "${it.getField()} error ${it.getCode()}"
				}
				*/
				account.errors.addAll(gormValidationErrors(personToValidate))
			} 
			personToValidate.discard()
		}
		return ok
	}

	/**
	 * Used to validate the UserLogin properties that were imported are valid and to update the account 
	 * Map with the appropriate values for tracking changes as well setting default values where applicable.
	 * If the account doesn't have an expiration date then it will attempt to get it from the project expiration 
	 * otherwise it will default N days in the future.
	 *
	 * The method will populate the following properties on the account map accordingly:
	 *    errors - appends any errors that occurred
	 * @param byWhom - the user that invoked the upload process
	 * @param account - the map used to track the person/user properties
	 * @param project - the user's project in their context
	 * @param sheetInfoOpts - the workbook title sheet info + the TzID and date stuff
	 * @return returns true if the lookup was successful, false if not or Null if the routine err
	 */
	private Boolean validateUserLogin(UserLogin byWhom, Map account, Project project, Map sheetInfoOpts) {
		Boolean ok=true

		UserLogin.withNewSession { ses ->
			boolean isExisting = !!account.person.id
			UserLogin userLogin
			if (isExisting) {
				userLogin = account.person.userLogin
			}
			if (!userLogin) {
				userLogin = new UserLogin()

				// We need a real person associated with the UserLogin to pass validation so we
				// can use the byWhom if necessary.
				userLogin.person = ( isExisting ? account.person : byWhom.person )
				// Set the password for the test
				userLogin.password = 'phoof'
				userLogin.expiryDate = projectService.defaultAccountExpirationDate(project)
			}

			// Iterate through the accountSpreadsheetColumnMap for all of the UserLogin attributes
			// setting the defaults and changed on the account map. Also setting the values on the domain
			// object so that we can test that there won't be any validation errors later.
			accountSpreadsheetColumnMap.each { prop, info ->
				if (info.domain != 'U') return

				def value = account[prop]
				if (value) {
					if (info.validator) {
						if (! info.validator.call(value, sheetInfoOpts)) {
							account.errors << "$prop has an invalid value"
							ok = false
							return
						}
					} else {
						// Manual validation? 
					}

					// Set the property on the UserLogin object
					userLogin[prop] = transformValueToDomainType(prop, value, sheetInfoOpts) 
				} else {
					if (isExisting && userLogin[prop]) {
						value = userLogin[prop]
					} else {
						if ( info.containsKey('defaultValue')) {
							value = info.defaultValue
						} else {
							return
						}
					}

					setDefaultedValue(account, prop, value)
					userLogin[prop] = transformValueToDomainType(prop, value, sheetInfoOpts)
				}
			}

			// Determine if we have enough information to assume that the administrator is trying to update or
			// create a new user
			if (userLogin.username) {
				if (! userLogin.validate()) {
					account.errors.addAll(gormValidationErrors(userLogin))
					ok = false
				}
			}
			userLogin.discard()
		}
		return ok
	}

	/**
	 * Used to validate the company accounts against the previous found person.company if found and compare by looking up
	 * the company by name. If the company name was blank and we found one then we'll default it.
	 * The method will populate the following properties on the account map accordingly:
	 *    company - set as default if not previously specified
	 *    company$DEFAULTED_SUFFIX - the default value for the data grid
	 *    errors - appends any errors that occurred
	 * @param account - the map used to track the person/user properties
	 * @param companiesById - the map of the companies affilated with the project
	 * @return returns true if the lookup was successful, false if not or Null if the routine err
	 */
	private Boolean validateCompanyName(Map account, Project project, Map companiesById) {
		Boolean ok
		PartyGroup company

		while (true) {
			// First off if a person was previously found, lets validate that their company is affliated with the project
			if (account.person) {
				company = account.person.company
				if (! companiesById.containsKey(company.id.toString())) {
					account.errors << 'Person is from non-affilated company'
					break
				}

				// If the sheet has the company name lets see if it matches the person's organization
				if (account.company) {
					if (account.company.toLowerCase() != company.name.toLowerCase()) {
						account.errors << "Person's company did not match company name"
						break
					}
					ok = true
					break
				}
			}

			if (account.company) {
				// Attempt to find the company by name
				company = PartyGroup.findByName(account.company)
				if (! company) {
					account.errors << 'Unable to find company by name' 
				} else {
					if (companiesById.containsKey(company.id.toString())) {
						account.company = company.name
						ok = true
					} else {
						account.errors << 'Company not affilated with project'
					}
				}
			} else {
				// Set the company default to the project.client when the company wasn't specified and 
				// we haven't found a person account
				company = project.client
				setDefaultedValue(account, 'company', company.name)
				// account.company = company.name
				// account["company$DEFAULTED_SUFFIX"] = company.name				
				ok = false
			}
			break
		}

		if (ok) {
			account.companyObj = company
		}

		return ok
	}

	/**
	 * Used to review and validate the account.roles values. When successful it will create map property roleActions that contain
	 * the various changes that need to occur for the user security settings. In addition this logic will:
	 *    - validate that the codes are legitimate
	 *    - validate that the user is only modifying roles for which he has capabilities of
	 *    - create list of adds and deletes for the team codes for person and project
	 * @param byWhom - the individual that is making the modifications
	 * @param account - the Map containing all of the changes
	 * @param validRoleCodes - a list of the valid role codes in the system
	 * @param authorizedRoleCodes - a list of the role codes that byWhom is authorized to manage
	 * @param A flag that indicates success (true) or an error occurred (false)
	 */	 
	private boolean validateSecurityRoles(UserLogin byWhom, Map account, List validRoleCodes, List authorizedRoleCodes ) {
		boolean ok=false
		
		String prop='roles'
		List currentRoles = []

		// Validate the Setup the default security role if necessary
		UserLogin userLogin 
		if (account.person.id) {
			userLogin = account.person.userLogin
			if (userLogin) {
				currentRoles = securityService.getAssignedRoleCodes(userLogin)
			}
		}

		String importedRoles = account[prop]
		List roleChanges = StringUtil.splitter(importedRoles, ',', DELIM_OPTIONS)

		// Add the DEFAULT security code if it appears that the user wouldn't otherwise be assigned one
		if (! currentRoles && ! roleChanges.find { it[0] != '-' }) {
			log.debug "validateSecurityRoles() set default role"
			roleChanges << securityService.getDefaultSecurityRoleCode()
		}

		Map changeMap = determineSecurityRoleChanges(validRoleCodes, currentRoles, roleChanges, authorizedRoleCodes)
		log.debug "validateSecurityRoles() changeMap=$changeMap"
		if (changeMap.error) {
			account.errors << changeMap.error
		} else {
			ok = true
			if (changeMap.hasChanges) {
				// For roles we are going to want to show the imported values along with the original and the resulting 
				// changes so we'll use all three underlying properties
				setOriginalValue(account, prop, currentRoles.join(', '))
				setDefaultedValue(account, prop, changeMap.results.join(', '))
				account[prop] = importedRoles

				// Save the changeMap to be used later on by the update logic
				account.securityChanges = changeMap
			}
		}
		return ok

	}

	/**
	 * Used to validate that the team codes are correct and will set the ORIGINAL or DEFAULT appropriately
	 * @param account - the Account map to review
	 * @return true if validated with no errors otherwise false
	 */
	private boolean validateTeams(Map account, Project project, List allTeamCodes) {
		boolean ok = true

		List currPersonTeams=[], currProjectTeams=[]
		if (account.person.id) {
			currPersonTeams = account.person.getTeamsCanParticipateIn().id
	
			// currProjectTeams= account.person.getTeamsAssignedTo(project)
			currProjectTeams = partyRelationshipService.getProjectStaffFunctionCodes(project, account.person)

		}

		List chgPersonTeams = StringUtil.splitter(account.personTeams, ',', DELIM_OPTIONS)
		List chgProjectTeams = StringUtil.splitter(account.projectTeams, ',', DELIM_OPTIONS)

		Map changeMap =	determineTeamChanges(allTeamCodes, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)

		log.debug "validateTeams(${account.firstName} ${account.lastName}) \n\tcurrPersonTeams=$currPersonTeams\n\tcurrProjectTeams=$currProjectTeams\n\tchgPersonTeams=$chgPersonTeams\n\tchgProjectTeams=$chgProjectTeams"
		log.debug "validateTeams() \n\tchangeMap=$changeMap"

		ok = ! changeMap.error
		if (ok) {
			// Update the account.teams with the various information to show the changes, etc
			if (changeMap.personHasChanges) {
				setOriginalValue(account, 'personTeams', currPersonTeams.join(', '))
				setDefaultedValue(account, 'personTeams', changeMap.resultPerson.join(', '))
				account.personTeams = chgPersonTeams.join(', ')
			}
			if (changeMap.projectHasChanges) {
				setOriginalValue(account, 'projectTeams', currProjectTeams.join(', '))
				setDefaultedValue(account, 'projectTeams', changeMap.resultProject.join(', '))
				account.projectTeams = chgProjectTeams.join(', ')
			}
		} else {
			account.errors << changeMap.error
		}

		return ok
	}

	// A helper method used to determine if a value has leading minus
	boolean isMinus(String str) { 
		return (str?.startsWith('-'))
	}

	// A helper method that will strip off a minus prefix if it exists
	String stripTheMinus(String str) { 
		String r = str
		if ( isMinus(str) ) {
			r = (str.size() > 1 ? str.substring(1).trim() : '')
		}
		return r
	}

	/**
	 * Used to compare a list against another list wher the list to check may have a minus (-) prefix
	 * @param validCodes the list of the valid codes to compare against
	 * @param codesToCheck - the list of codes to validate
	 * @return a list of the invalid codes or empty if all match
	 */
	private List checkMinusListForInvalidCodes(List<String> validCodes, List<String> codesToCheck) {
		String error
		List list = codesToCheck.collect { stripTheMinus(it) }
		list.removeAll(validCodes)
		return list
	}

	/**
	 * Used to determine the actions that should be done based on the current roles and a list of actions. This
	 * will return a Map [add: [list of roles], delete:[list of roles], error:'Message if violation'].
	 * @param allRoles - the list of all of the security roles
	 * @param currentRoles - the list of security role codes that the user already has
	 * @param changes - a list of the updated roles which the intent of removal will have a minus (-) prefix
	 * @param authorizedRoleCodes - a list of the roles that the individual can assign based on their security level
	 * @return A Map object that will contain the following elements:
	 *        add: a list of roles to be added
	 *     delete: a list of roles to be deleted
	 *     result: a list of the resulting roles after the changes are applied
	 *     errors: an error message String if invalid codes are referenced or tried to assign 
	 * hasChanges: a boolean indicating if there were changes or not
	 */
	private Map determineSecurityRoleChanges(List allRoles, List currentRoles, List changes, List authorizedRoleCodes) {

		Map map = [:]

		// Used to reset the results array and inject the error message before the code bails out 
		def panicButton = { errorMessage ->
			map.hasChanges=false
			map.add = []
			map.delete = []
			map.error = errorMessage 
		}

		panicButton ''	// Initialize the map without an error message

		List remainingCodes = []

		log.debug "determineSecurityRoleChanges() \n   allRoles=$allRoles\n   currentRoles=$currentRoles\n   changes=$changes\n   authorizedRoleCodes=$authorizedRoleCodes" 

		while (true) {
			List list
			//
			// First validate that there are no invalid codes
			//

			// Check the roles currently assigned to the user (coming from the system)
			list = currentRoles - allRoles
			if (list) {
				// This shouldn't happen but just in case
				map.error = "System issue with invalid security role${list.size() > 1 ? 's' : ''} $list"
				break
			}

			// We'll assume that the accessible roles are legit as they're pulled from the same table as the allRoles

			// Check the updatedRoles which may contain leading minus (-) character indicating removal
			list = checkMinusListForInvalidCodes(allRoles, changes)
			log.debug "determineSecurityRoleChanges()          changes: $changes"
			log.debug "determineSecurityRoleChanges() withMinusRemoved: $list"
			list = list - allRoles
			if (list) {
				panicButton "Invalid security code${list.size() > 1 ? 's' : ''} $list"
				break
			}

			// Now we're going to go through the list of changes and apply them to the current list
			remainingCodes = currentRoles.clone()
			List violationCodes = []
			changes.each { chgCode ->
				// Determine if there is a security violation. Note that a user may have security roles 
				// previously assigned that are higher than what the individual who is making the changes can
				// assign so as long as the code was in the original list we're fine. The individual can not
				// add or remove codes above their clearence.
				boolean toDelete = isMinus(chgCode)
				String code = stripTheMinus(chgCode)

				if ( ! authorizedRoleCodes.contains(code) ) {
					boolean alreadyAssignedToUser = currentRoles.contains(code) 
					// See if they are trying to add or delete an unauthorize code
					if ( ( !alreadyAssignedToUser && ! toDelete) || (alreadyAssignedToUser && toDelete) ) {
						violationCodes << code
						return
					}
				}

				// Check for violations
				if (violationCodes) {
					panicButton "Specified unauthorized security role${violationCodes.size()>1?'s':''} ($violationCodes)"
					return
				}
				
				boolean alreadyInList = remainingCodes.contains(code)
				if (toDelete && alreadyInList) {
					remainingCodes = remainingCodes - code
					map.delete << code
					map.hasChanges = true
				} else if ( !toDelete && ! alreadyInList) {
					remainingCodes << code
					map.add << code
					map.hasChanges = true
				}
			}

			//println "\n   remainingCodes=$remainingCodes\n   $allRoles=$allRoles\n   currentRoles=$currentRoles\n   changes=$changes" +
			 "\n   authorizedRoleCodes=$authorizedRoleCodes\n   map=$map" +
			 "\n   remainingCodes=${remainingCodes ? true : false} ${remainingCodes.size()}"

			if (remainingCodes) {
				map.results = remainingCodes.sort()
			} else {
				panicButton "Deleting all security roles not permitted"
			}
			break
		}
		return map
	}

	/**
	 * Used to determine the actions that should be done based on the current teams assigned to the person directly as well as 
	 * to the project. Here are a few rules to how the changes will work:
	 *    - Any team code with a minus (-) prefix in the chgPersonTeams or chgProjectTeams is an indicator to delete the 
	 *      appropriate reference
	 *    - If a chgPersonTeams entry contains a minus then the team is removed from the person as well as *ALL* projects
	 *    - If a chgProjectTeams is listed that is not associated to the person's existing personal list then it will be added
	 *
	 * The end result of the method will return a Map than can be used to update the person's references to teams.
	 * @param allTeams - the list of all of the team codes
	 * @param currPersonTeams - the list of team codes that the user already has assigned personally assigned
	 * @param chgPersonTeams - a list of the changes to the personal teams codes which if have the minus (-) prefix should be removed
	 * @param currPartyTeams - the list of team codes that the user already has assigned to the project
	 * @param chgPartyTeams - a list of the changes to the project teams codes which if have the minus (-) prefix should be removed
	 * @return A Map object that will contain the following elements:
	 *         addToPerson: a list of teams to add to the person
	 *    deleteFromPerson: a list of teams to be deleted from the person
	 *        addToProject: a list of teams to add to the project
	 *   deleteFromProject: a list of teams to be deleted from the project
	 *        resultPerson: a list of the resulting teams assigned to the person personally after the changes are applied
	 *       resultProject: a list of the resulting teams assigned to the person for the project after the changes are applied
	 *              errors: an error message string indicating any errors
	 *          hasChanges: a boolean when true indicates that there were changes to either the person or project lists
	 *    personHasChanges: a boolean when true indicates that there was a change for the Person team list
	 *   projectHasChanges: a boolean when true indicates that there was a change for the Project team list
	 */
	private Map determineTeamChanges(List allTeams, List currPersonTeams, List chgPersonTeams, List currProjectTeams, List chgProjectTeams) {

		Map map = [:]

		// Used to reset the results array and inject the error message before the code bails out 
		def panicButton = { errorMessage ->
			map.hasChanges=false
			map.personHasChanges=false
			map.projectHasChanges=false
			map.addToPerson = []
			map.deleteFromPerson = []
			map.addToProject = []
			map.deleteFromProject = []
			map.resultPerson = []
			map.resultProject = []
			map.error = errorMessage 
		}
		panicButton ''	// Initialize the map without an error message

		//log.debug "determineTeamChanges() \n\tallTeams=$allTeams" +
		//	"\n\t currPersonTeams=$currPersonTeams\n\t chgPersonTeams=$chgPersonTeams" +
		//	"\n\t currProjectTeams=$currProjectTeams\n\t chgProjectTeams=$chgProjectTeams" 

		while (true) {
			List list

			// Check the roles currently assigned to the user (coming from the system)
			[currPersonTeams, currProjectTeams].each { teams ->
				list = teams.clone()
				list.removeAll(allTeams)
				if (list) {
					// This shouldn't happen but just in case
					panicButton "System issue with invalid team role${list.size() > 1 ? 's' : ''} $list"
					log.debug "WTF??? allTeams=$allTeams"
				}
			}
			if (map.error) {
				break
			}

			// Check the chgPersonTeams and chgProjectTeams for invalid codes
			[chgPersonTeams, chgProjectTeams].each { teams ->
				list = checkMinusListForInvalidCodes(allTeams, teams)
				if (list) {
					panicButton "Invalid team code${list.size() > 1?'s':''} $list referenced"
				}
			}
			if (map.error) {
				break
			}

			List resultPersonTeams = currPersonTeams.clone()
			List resultProjectTeams = currProjectTeams.clone()

			// Now we're going to go through the person team list of changes and add/remove based on the change list
			// noting that any deletes may affect the project list
			chgPersonTeams.each { chgTeam ->
				boolean toDelete = isMinus(chgTeam)
				String team = stripTheMinus(chgTeam)
				boolean alreadyInList = resultPersonTeams.contains(team)

				if (toDelete) {
					// Deleting a team for a Person can impact the Project assignment as well
					if (alreadyInList) {
						resultPersonTeams.remove(team)
						map.deleteFromPerson << team
						map.personHasChanges = true
					}
					if (resultProjectTeams.contains(team)) {
						resultProjectTeams.remove(team)
						map.deleteFromProject << team
						map.projectHasChanges = true
					}
				} else if ( !toDelete && ! alreadyInList) {
					// Adding a team only impacts the Person List
					resultPersonTeams << team
					map.addToPerson << team
					map.personHasChanges = true
				}

			}

			// Next we're going to go through the project team list of changes and add/remove based on the change list
			// noting that any additions may affect the person list
			chgProjectTeams.each { chgTeam ->
				boolean toDelete = isMinus(chgTeam)
				String team = stripTheMinus(chgTeam)
				boolean alreadyInList = resultProjectTeams.contains(team)

				if (toDelete && alreadyInList) {
					// Delete the team
					resultProjectTeams.remove(team)
					map.deleteFromProject << team
					map.projectHasChanges = true
				} else if ( !toDelete ) {
					// Add the team
					if ( ! alreadyInList) {
						resultProjectTeams << team
						map.addToProject << team
						map.projectHasChanges
					}
					if (! resultPersonTeams.contains(team)) {
						resultPersonTeams << team
						map.addToPerson << team
						map.personHasChanges
					}
				}
			}

			map.resultPerson = resultPersonTeams.sort()
			map.resultProject = resultProjectTeams.sort()
			map.addToPerson = map.addToPerson.sort()
			map.addToProject = map.addToProject.sort()
			map.hasChanges = (map.personHasChanges || map.projectHasChanges)

			break
		}
		return map
	}

	/**
	 * Used to lookup the person by the personId property and will update the account map with the following:
	 *    person - the person if found and was valid
	 *    errors - appends any errors that occurred
	 * @param account - the map used to track the person/user properties
	 * @return returns 
	 *    true - if the lookup was successful,
	 *    false - if no id was present
	 *    null - if there were any errors
	 */
	private Boolean fetchPersonByPersonId(Map account) {
		Boolean ok
		if (! account.personId) {
			ok = false
		} else {
			Long pid = NumberUtil.toPositiveLong(account.personId, -1)
			if (pid == -1) {
				account.errors << 'Invalid person ID value'
			} else {
				def person = Person.get(pid)
				if (! person) {
					account.errors << 'Person by ID not found'
				} else {
					account.person = person
					account.match << 'personId'
					ok = true
				}
			}
		}
		return ok
	}

	/**
	 * Used to lookup the person by their email address property and will update the account map with the following:
	 *    person - the person if found and was valid
	 *    errors - appends any errors that occurred
	 * @param account - the map used to track the person/user properties
	 * @return returns 
	 *    true - if the lookup was successful,
	 *    false - if no id was present
	 *    null - if there were any errors
	 */
	private Boolean fetchPersonByEmail(Map account) {
		Boolean ok
		if (! account.email) {
			ok = false
		} else {
			def person = Person.findByEmail(account.email)
			if (! person) {
				ok = false
			} else {
				// Check for a person mismatch 
				if (account.person) {
					if (person.id != account.person.id) {
						account.errors << 'Email cross referenced other person'
					} else {
						ok = true
					}
				} else {
					account.person = person
					account.match << 'email'
					ok = true
				}
			} 
		}
		return ok
	}

	/**
	 * Used to lookup the person by their username and will update the account map with the following:
	 *    person - the person if found and was valid
	 *    errors - appends any errors that occurred
	 * @param account - the map used to track the person/user properties
	 * @return returns 
	 *    true - if the lookup was successful,
	 *    false - if no id was present
	 *    null - if there were any errors
	 */
	private Boolean fetchPersonByUsername(Map account) {
		Boolean ok
		if (! account.username) {
			ok = false
		} else {
			UserLogin userLogin = UserLogin.findByUsername(account.username)
			if (userLogin) {
				if (account.person) {
					if (account.person.id != userLogin.person.id) {
						account.errors << 'Username cross-reference other person'
					} else {
						ok = true
					}
				} else {
					ok = true
				}
				if (ok) {
					account.match << 'username'
					if (! account.person) {
						account.person = userLogin.person
					}
				}
			}
		}
		return ok
	}

	/**
	 * Used to lookup the person by their username and will update the account map with the following:
	 *    person - the person if found and was valid
	 *    errors - appends any errors that occurred
	 * @param account - the map used to track the person/user properties
	 * @return returns 
	 *    true - if the lookup was successful,
	 *    false - if no id was present
	 *    null - if there were any errors
	 */
	private List findPersonsByName(Map account) {
		List people = []
		// Attempt to find the person by name which is only possible if we know the company
		if (account.companyObj) {
			Map nameMap = [first:account.firstName,
				middle:account.middleName,
				last:account.lastName
			]
			people = personService.findByCompanyAndName(account.companyObj, nameMap)
		}
		return people
	}

	/**
	 * Used to add or update the person
	 * @param account - the account information
	 * @param options - the map of the options
	 * @return a list containing:
	 *    Person - the person created or updated
	 *    Map - the map of the account
	 *    String - an error message if the save or update failed
	 *    boolean - a flag indicating if the account was changed (true) or unchanged (false)
	 */
	List addOrUpdatePerson(UserLogin byWhom, Map account, Map sheetInfoOpts, Map formOptions) {
		String error
		boolean changed=false

		Person person = account.person

		// Update the person with the values passed in
		account = applyChangesToPerson(person, account, formOptions.flagToUpdatePerson)

		List dirtyProps = person.dirtyPropertyNames
		log.debug "addOrUpdatePerson() ${person} account.isNewAccount=${account.isNewAccount}, dirtyProps=$dirtyProps"
		if ( account.isNewAccount || dirtyProps.size() ) {
			if (! person.validate()) {
				log.debug "addOrUpdatePerson() ${person} person.validate() failed"

				// TODO : JPM 4/2016 : Stuff the failed properties into an errors object to bubble up to the UI
				// person.errors.allErrors.each { log.debug "Property ${it.getField()} failed ${it.getCode()}" }
				error = GormUtil.allErrorsString(person)
				person.discard()
			} else {
				person.save(flush:true)
				changed = true

				if (account.isNewAccount) {
					partyRelationshipService.addCompanyStaff(account.companyObj, person)
					auditService.logMessage("$byWhom created new person $person for company ${account.company}")
				} else {
					auditService.logMessage("$byWhom updated person $person - modified properties $dirtyProps")
				}
			}
		}

		return [error, changed]
	}

	/**
	 * Used to add or update a person's UserLogin account and set the security role(s) accordingly based on their
	 * security level.
	 * @param byWhom - the UserLogin that invoked the update
	 * @param account - the account map with all of the information about the person/user
	 * @param options - the options that the user selected for the update
	 * @return a flag that indicates if the update updating anything (true) or remained unchanged (false)
	 */
	boolean addOrUpdateUser(UserLogin byWhom, Map account, Map sheetInfoOpts, Map formOptions) {
		if (! securityService.hasPermission(byWhom, 'EditUserLogin', true)) {
			throw new UnauthorizedException('You are unauthorized to edit UserLogins')
		}

		UserLogin userLogin = account.person.userLogin
		if (! userLogin ) {
			if (account.username) {
				userLogin = new UserLogin()
				userLogin.username = account.username
				userLogin.person = account.person
				userLogin.active = formOptions.activateLogin.asYN()
				userLogin.forcePasswordChange = formOptions.forcePasswordChange.asYN()

				// TODO : JPM 4/2016 : Set timezone to that of the project when creating the user

			} else {
				// Nothing to be done here
				return false
			}
		}

		userLogin.expiryDate = formOptions.expiryDate
	}

	/**
	 * Used to update the person's teams associated to themselves/company and to the project
	 * by examining the personTeams and projectTeams lists passed in the account map. If the codes have 
	 * a minus(-) suffix then the team will be removed otherwise it is added if it doesn't already exist.
	 * @param byWhom - the UserLogin that invoked the update
	 * @param account - the account map with all of the information about the person/user
	 * @param project - the project to associate the person's teams to
	 * @param options - the options that the user selected for the update
	 * @return a flag that indicates if the update updating anything (true) or remained unchanged (false)
	 */
	boolean addOrUpdateTeams(UserLogin byWhom, Map account, Project project, Map sheetInfoOpts, Map options) {
		List teamsCanParticapateIn = []
		List projectTeams = []

		// Check to see if there were any teams specified for the user
		if (! account.personTeams && ! account.projectTeams) {
			log.debug "addOrUpdateTeams() bailed as there were no changes - account.personTeams=${account.personTeams?.getClass().getName()}, account.projectTeams=${account.projectTeams?.getClass().getName()}"
			return false
		}

		assert (account.person instanceof Person)
		boolean changed = false

		String personName = account.person.toString()

		// Find team codes with minus (-) prefix that are to be deleted
		List personTeamsToDelete = account.personTeams.findAll { it[0] == '-' }
		List projectTeamsToDelete = account.projectTeams.findAll{ it[0] == '-' }

		// Determine the existing project team assignments that are being deleted from the person and remove them
		// from the team adds if they're there.
		def projectTeamsToAdd = account.projectTeams - projectTeamsToDelete

		log.debug "addOrUpdateTeams() teams for person $personName account.personTeams=${account.personTeams}; projectTeamsToAdd=$projectTeamsToAdd"
		// The teams for the person should be all that were specified that don't start with minus (-)
		List teamsForPerson = account.personTeams 
		if (projectTeamsToAdd) {
			teamsForPerson.addAll(projectTeamsToAdd)
		}
		if (personTeamsToDelete) {
			teamsForPerson.removeAll(personTeamsToDelete)
		}
		teamsForPerson = teamsForPerson.unique()

		log.debug "addOrUpdateTeams() teams for person $personName $teamsForPerson"

		if (! account.isNewAccount) {
			teamsCanParticapateIn = account.person.getTeamsCanParticipateIn().id
			// For existing accounts we need to get all of their accounts + any new ones that were specified
			teamsForPerson.addAll(teamsCanParticapateIn)
			teamsForPerson = teamsForPerson.unique()

			// Try removing the teams again now that we have the teams from the database
			teamsForPerson = teamsForPerson - personTeamsToDelete

			// Determine if there are any new or removed teams
			changed = (teamsCanParticapateIn - teamsForPerson) || (teamsForPerson - teamsCanParticapateIn) 
			log.debug "addOrUpdateTeams() teams for existing person $personName : teams=$teamsForPerson"
		} else {
			changed = (teamsForPerson.size() > 0)
		}

		if (changed) {
			log.debug "addOrUpdateTeams() changing ${personName}'s teams $teamsForPerson"
			partyRelationshipService.updateAssignedTeams(account.person, teamsForPerson)
		}

		// Now lets deal with assignment at the project level
		projectTeams = partyRelationshipService.getProjectStaffFunctions(project, account.person, false).id
		projectTeamsToAdd = projectTeamsToAdd - projectTeams
		if (projectTeamsToAdd) {
			projectService.addTeamMember(project, account.person, projectTeamsToAdd)
			changed = true
		}

		if (projectTeamsToDelete) {
			// Strip off the leading minus (-)
			projectTeamsToDelete = projectTeamsToDelete.collect { it.substring(1) }
			log.debug "addOrUpdateTeams() deleting ${personName}'s project teams $projectTeamsToDelete"
			int count = projectService.removeTeamMember(project, account.person, projectTeamsToDelete)
			if (count) {
				changed = true
			}
		}

		return changed
	}

	/**
	 * Used to load any property a person object where the values are different
	 * @param person - the person object to be changed
	 * @param account - the Map containing all of the changes
	 * @param shouldUpdatePerson - a flag if true will record the original values to show changes being made
	 * @return 
	 */
	private Map applyChangesToPerson(Person person, Map account, Map sheetInfoOpts, boolean shouldUpdatePerson) {
		boolean existing = person.id
		accountSpreadsheetColumnMap.each {prop, info ->
			if (info.domain == 'P') {
				if (existing) {
					// Attempt to save the original value
					if (account[prop]) {
						if ( (! person[prop]) || ( person[prop] && person[prop] != account[prop] ) ) {
							// Save the original value so it can be displayed later
							if (shouldUpdatePerson) {
								setOriginalValue(account, prop, (person[prop] ?: '') )
								// account["${prop}$ORIGINAL_SUFFIX"] = (person[prop] ?: '')
							}
							person[prop] = account[prop]
						}
					} else {
						// Attempt to save the default value
						if ( person[prop]) {
							setDefaultedValue(account, prop, person[prop])
							// account["${prop}$DEFAULTED_SUFFIX"] = person[prop]
						}
					}
				} else {
					person[prop] = transformValueToDomainType(prop, account[prop], sheetInfoOpts)
				}
			}
		}
		return account
	}

}