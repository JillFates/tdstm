package net.transitionmanager.imports

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.security.SecurityUtil
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import net.transitionmanager.common.CoreService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.exception.LogicException
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.AuditService
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ServiceMethods
import org.apache.commons.lang3.RandomStringUtils as RSU
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartFile

import java.text.DateFormat

/**
 * Methods for importing and exporting of project staff and users
 */
class AccountImportExportService implements ServiceMethods {

	AuditService             auditService
	CoreService              coreService
	PartyRelationshipService partyRelationshipService
	PersonService            personService
	ProjectService           projectService
	UserPreferenceService    userPreferenceService

	static final String LOGIN_OPT_ALL = 'A'
	static final String LOGIN_OPT_ACTIVE = 'Y'
	static final String LOGIN_OPT_INACTIVE = 'N'

	private static final ACCOUNT_EXPORT_TEMPLATE = [
			xls:'/templates/AccountsImportExport.xls',
			xlsx:'/templates/AccountsImportExport.xlsx'
	]
	static final String EXPORT_FILENAME_PREFIX = 'AccountExport'
	static final String IMPORT_FILENAME_PREFIX = 'AccountImport'


	static final String TEMPLATE_TAB_NAME  = 'Accounts'
	static final String TEMPLATE_TAB_TITLE = 'Title'
	static final String TEMPLATE_TAB_ROLES = 'Roles'
	static final String TEMPLATE_TAB_TEAMS = 'Teams'

	// Used to indicate the alternate property name with the original and defaulted values
	// that are stored in the account map (e.g. firstName, firstName_o and firstName_d)
	static final String ERROR_SUFFIX = '_e'
	static final String ORIGINAL_SUFFIX = '_o'
	static final String DEFAULTED_SUFFIX = '_d'
	private static final List   SUFFIX_LIST = [ORIGINAL_SUFFIX, DEFAULTED_SUFFIX, ERROR_SUFFIX].asImmutable()

	static final String IMPORT_OPTION_PARAM_NAME='importOption'
	static final String IMPORT_OPTION_BOTH='B'
	static final String IMPORT_OPTION_PERSON='P'
	static final String IMPORT_OPTION_USERLOGIN='U'

	// The offset from zero to the first row in the spreadsheet that data appears
	static final int FIRST_DATA_ROW_OFFSET=1

	// Users can split Teams and Security roles on the follow characters as well as the default comma (,)
	private static final List DELIM_OPTIONS = [';',':','|'].asImmutable()

	// ------------------------------------------------------------------------
	// Kendo Template builder
	// ------------------------------------------------------------------------

	// Used in the map below to set the various template strings used by Kendo
	private static String changeTmpl(String prop) {
		"\"#= showChanges(data, '$prop') #\""
	}

	private static final String errorListTmpl = "kendo.template(\$('#error-template').html())"

	// --------------------------------------------------------
	// Validator are used when reading values from spreadsheet
	// --------------------------------------------------------

	// Closures used for validating imported data
	private static final validator_YN = { val, options ->
		boolean valid = false
		if (StringUtil.isBlank(val)) {
			valid = true
		} else if (val && (val instanceof String)) {
			valid = ['Y','N'].contains(val.toUpperCase())
		}
		return valid
	}

	// Closure used to validate that a string can be parsed as a Date
	private static final validator_date = { val, Map options ->
		boolean valid = false
		if (val) {
			if (val instanceof Date) {
				valid = true
			} else {
				// log.debug "*** validator_date() val isa ${val?.getClass().name} and formatter isa ${options.dateFormatter?.getClass().name}"
				valid = TimeUtil.parseDate(val, options.dateFormatter) != null
			}
		}
		return valid
	}

	// -------------------------------------------------------------
	// Tranformers are used when converting to write to spreadsheet
	// -------------------------------------------------------------

	// Used to transform a string which will change null to blank
	private static final xfrmString = { val, options ->
		val ?: ''
	}

	// @IntegrationTest
	private static final xfrmToYN = { val, options ->
		String r = val
		if (val == null) {
			r = ''
		} else if (val instanceof Boolean) {
			r = (val ? 'Y' : 'N')
		}
		// log.debug "xfrmToYN val=$val, r=$r"
		return r
	}

	private static final xfrmDateToString = { val, options ->
		String r = ''
		if (val != null) {
			if (val instanceof Date) {
				r = TimeUtil.formatDate(val, options.dateFormatter)
				// log.debug "xfrmDateToString() did formatDate on val($val) and got r($r)"
			} else {
				r = val
				log.error "xfrmDateToString() got unexpected data type ${val?.getClass()?.name}"
			}
		}
		return r
	}

	// Transformer that is used to populate the spreadsheet using the user's TZ
	private static final xfrmDateTimeToString = { val, options ->
		String r = ''
		if (val != null) {
			if (val instanceof Date) {
				r = TimeUtil.formatDate(val, options.dateTimeFormatter)
				// log.debug "xfrmDateTimeToString() did formatDate on val($val) and got r($r)"
			} else {
				r = val
				log.error "xfrmDateTimeToString() got unexpected data type ${val?.getClass()?.name}"
			}
		}
		return r
	}

	// Transforms a List to a comma separated list
	// @IntegrationTest
	private static final xfrmListToString = { list, options ->
		if (list != null && ! (list instanceof List)) {
			throw new LogicException("xfrmListToString() called with ${list.getClass().name} $list")
		}
		String r = ''
		if (list != null) {
			r = (list instanceof List) ? list.join(', ') : list
		}
		// log.debug "xfrmListToString() converted $list to $r which isa ${r.getClass().name}"
		return r
	}

	// Transforms a List to a pipe (|) separated string
	// @IntegrationTest
	private static final xfrmListToPipedString = { list, options ->
		if (list != null && ! (list instanceof List)) {
			throw new LogicException("xfrmListToPipedString() called with ${list.getClass().name} $list")
		}
		String r = ''
		if (list != null) {
			r = (list instanceof List) ? list.join('|') : list
		}
		// log.debug "xfrmListToString() converted $list to $r which isa ${r.getClass().name}"
		return r
	}

	// ------------------------------------------------------------------------
	// Default closures are used to set computed default values on properties
	// ------------------------------------------------------------------------

	private static final defaultExpiration = { propertyName, accountMap, options ->
		if (!options.containsKey('project') || ! (options.project instanceof Project)) {
			throw new RuntimeException('Require Project object was not passed in the options map')
		}

		// For some reason the projectService on the class is not in scope to the closure so
		// we'll fetch it from the App Context.
		ProjectService projectService = ApplicationContextHolder.getBean('projectService', ProjectService)
		return projectService.defaultAccountExpirationDate(options.project)
	}

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
	 *				 by using the account and the sheetInfoOpts maps that are passed into the closure.
	 */
	private static final Map accountSpreadsheetColumnMap = [
		personId               : [type:'number',  ssPos:0,    formPos:1, domain:'I', width:80,  locked:true, label:'ID',
									template:changeTmpl('personId')],
		firstName              : [type:'string',  ssPos:1,    formPos:2, domain:'P', width:120,  locked:true, label:'First Name',
									template:changeTmpl('firstName'), transform:xfrmString],
		middleName             : [type:'string',  ssPos:2,    formPos:3,  domain:'P', width:120,  locked:true,  label:'Middle Name',
									 template:changeTmpl('middleName'), transform:xfrmString],
		lastName               : [type:'string',  ssPos:3,    formPos:4, domain:'P', width:120,  locked:true,  label:'Last Name',
								 	template:changeTmpl('lastName'), transform:xfrmString],
		company                : [type:'string',  ssPos:4,    formPos:5, domain:'T', width:120,  locked:true,  label:'Company',
									template:changeTmpl('company'), transform:xfrmString],
		errors                 : [type:'list',    ssPos:null, formPos:6,  domain:'T', width:240, locked:false, label:'Errors',
									template:errorListTmpl, templateClass:'error', transform:xfrmListToPipedString ],
		workPhone              : [type:'string',  ssPos:5,    formPos:7,  domain:'P', width:120, locked:false, label:'Work Phone',
									template:changeTmpl('workPhone'), transform:xfrmString],
		mobilePhone            : [type:'string',  ssPos:6,    formPos:8,  domain:'P', width:120, locked:false, label:'Mobile Phone',
									template:changeTmpl('mobilePhone'), transform:xfrmString],
		email                  : [type:'string',  ssPos:7,    formPos:9,  domain:'P', width:100, locked:false, label:'Email',
									template:changeTmpl('email'), transform:xfrmString],
		title                  : [type:'string',  ssPos:8,    formPos:10, domain:'P', width:100, locked:false, label:'Title',
									template:changeTmpl('title'), transform:xfrmString],
		department             : [type:'string',  ssPos:9,    formPos:11, domain:'P', width:120, locked:false, label:'Department',
									template:changeTmpl('department'), transform:xfrmString],
		location               : [type:'string',  ssPos:10,   formPos:12, domain:'P', width:120, locked:false, label:'Location/City',
									template:changeTmpl('location'), transform:xfrmString],
		stateProv              : [type:'string',  ssPos:11,   formPos:13, domain:'P', width:120, locked:false, label:'State/Prov',
									template:changeTmpl('stateProv'), transform:xfrmString],
		country                : [type:'string',  ssPos:12,   formPos:14, domain:'P', width:100, locked:false, label:'Country',
									template:changeTmpl('country'), transform:xfrmString],
		personTeams            : [type:'list',    ssPos:13,   formPos:15, domain:'T', width:190, locked:false, label:'Person Team(s)',
									template:changeTmpl('personTeams'), transform: xfrmListToString ],
		projectTeams           : [type:'list',    ssPos:14,   formPos:15, domain:'T', width:190, locked:false, label:'Project Team(s)',
									template:changeTmpl('projectTeams'), transform: xfrmListToString ],
		roles                  : [type    :'list', ssPos:15, formPos:17, domain:'T', width:120, locked:false, label:'Security Role(s)',
								  template:changeTmpl('roles'), defaultValue: SecurityService.DEFAULT_SECURITY_ROLE_CODE, transform: xfrmListToString],
		username               : [type:'string',  ssPos:16,   formPos:18, domain:'U', width:120, locked:false, label:'Username',
									template:changeTmpl('username'), transform:xfrmString, defaultOnError:{RSU.randomAlphabetic(10)}],
		isLocal                : [type:'boolean', ssPos:17,   formPos:19, domain:'U', width:140, locked:false, label:'Local Account?',
									template:changeTmpl('isLocal'), defaultValue: 'Y', validator: validator_YN, transform: xfrmToYN],
		active                 : [type:'string',  ssPos:18,   formPos:20, domain:'U', width:140, locked:false, label:'Login Active?',
									template:changeTmpl('active'), transform:xfrmString, defaultValue: 'N', validator: validator_YN],
		expiryDate             : [type:'date',  ssPos:19,   formPos:21, domain:'U', width:150, locked:false, label:'Account Expiration',
									template:changeTmpl('expiryDate'), transform:xfrmDateToString, validator:validator_date,
									defaultValue:defaultExpiration],
		// TODO : swtich passwordExpirationDate back to date after testing
		passwordExpirationDate : [type:'date',    ssPos:20,   formPos:22, domain:'U', width:160, locked:false, label:'Password Expiration',
									template:changeTmpl('passwordExpirationDate'), transform:xfrmDateToString, validator:validator_date],
		passwordNeverExpires   : [type:'boolean', ssPos:21,   formPos:23, domain:'U', width:168, locked:false, label:'Pswd Never Expires?',
									template:changeTmpl('passwordNeverExpires'), defaultValue: 'N', validator: validator_YN, transform:xfrmToYN],
		forcePasswordChange    : [type:'string',  ssPos:22,   formPos:24, domain:'U', width:150, locked:false, label:'Force Chg Pswd?',
									template:changeTmpl('forcePasswordChange'), transform:xfrmString, defaultValue: 'N', validator: validator_YN ],
		lastLogin              : [type:'datetime',ssPos:23,   formPos:24, domain:'T', width:170, locked:false, label:'Last Login (readonly)',
									transform:xfrmDateTimeToString ],
		matches                : [type:'list',    ssPos:null, formPos:25, domain:'T', width:120, locked:false, label:'Matched On',
									transform: xfrmListToString]
	].asImmutable()

	// The map of the location of the various properties on the Title page of the Account Spreadsheet
	private static final Map TitlePropMap = [
		 clientName: [1,3, 'String'],
		  projectId: [1,4, 'Integer'],
		projectName: [2,4, 'String'],
		 exportedBy: [1,5, 'String'],
		 exportedOn: [1,6, 'Datetime'],
		   timezone: [1,7, 'String'],
		 dateFormat: [1,8, 'String']
	].asImmutable()

	// --------------------------------
	// Controller called methods
	// --------------------------------

	/**
	 * Used to load a blank import template that updates the title sheet and then downloads the file to the
	 * end user.
	 * @param response - the HttpResponse object
	 * @param project - the user's currently selected project
	 * @param filename - the name of the file that the download should have for the mime-type
	 */
	void generateImportTemplateToBrowser(response, Project project, String filename) {
		securityService.requirePermission(Permission.PersonExport, true)

		Workbook workbook = getAccountExportTemplate()
		Map sheetOptions = getUserPreferences()

		updateTitleSheetInfo(project, workbook, sheetOptions)
		sendSpreadsheetToBrowser(response, workbook, filename)
	}

	/**
	 * Used to generate the spreadsheet of accounts based on the user submitted options and will stream
	 * it directly to the browser.
	 * @param response - the HttpResponse object to write the spreadsheet to
	 * @param project - the user's current project
	 * @param formOptions - the params values used by the request
	 * @permission PersonExport, UserEdit
	 */
	void generateAccountsExportToBrowser(response, Project project, Map formOptions) {
		securityService.requirePermission(Permission.PersonExport, true)
		if (shouldUpdateUserLogin(formOptions)) {
			securityService.requirePermission(Permission.UserExport, true)
		}

		int loginChoice = NumberUtil.toPositiveLong(formOptions.loginChoice, -1)
		if (loginChoice < 0 || loginChoice > 4) {
				throw new InvalidParamException('The User Login Filter option was not properly specified')
		}
		formOptions.loginChoice = loginChoice

		def spreadsheet = generateAccountExportSpreadsheet(project, formOptions)

		// Formulate the download filename ExportAccounts + ProjectCode + yyyymmdd sans the extension
		String projectName = project.projectCode.replaceAll(' ','')
		String formattedDate = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		String filename = EXPORT_FILENAME_PREFIX + '-' + projectName + '-' + formattedDate

		// Send the file out to the browser
		sendSpreadsheetToBrowser(response, spreadsheet, filename)
	}

	/**
	 * This method is used to load the spreadsheet into memory and validate that it contains some information. If
	 * successful it will save the file with a random name and then return the model containing the filename.
	 * @param request - the servlet request object
	 * @param project - the project that the import is being applied againstclearDefaultedValues
	 * @param fileParamName - the servlet request params name of the var that references the upload spreadsheet file
	 * @return a Map of data used in the controller view including:
	 *    filename - the local filename of the spreadsheet
	 *    people - the accounts that were read from the spreadsheet
	 *    labels - the list column header labels used in the accounts list
	 *    properties - the list of the property names used in the accounts list
	 *    gridMap - the meta data used by the data grid
	 * @controllerMethod
	 */
	@Transactional
	Map processFileUpload(request, Project project, Map formOptions) {
		if (formOptions.flagToUpdatePerson) {
			securityService.requirePermission(Permission.PersonImport, true)
		}
		if (formOptions.flagToUpdateUserLogin) {
			securityService.requirePermission(Permission.UserImport, true)
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
			File fqfn = getFile(model.filename)
			if (!fqfn.delete()) {
				log.error "Unable to delete temporary account import worksheet $fqfn.path"
			}
			throw new InvalidParamException('The spreadsheet column headers did not match the expected format. Please '+
				' export a new template before attempt an import.')
		}

		// Read in the accounts to VALIDATE that we're able read it without errors
		if (!readAccountsFromSpreadsheet(workbook, sheetInfoOpts)) {
			throw new EmptyResultException('Unable to read the spreadsheet or the spreadsheet was empty')
		}

		return model
	}

	/**
	 * This is used to return the JSON data generated by the Account Import POST step. The file should be written
	 * to the application temp directory with the same file name (.json extention) as the temporary spreadsheet
	 * @param response - the HttpResponse object
	 * @param project  - the project that the import is being applied against
	 * @param filename - the name of the temporarilly saved spreadsheet
	 * @controllerMethod
	 */
	List generateReviewData(Project project, String filename, Map formOptions) {
		securityService.requirePermission(Permission.PersonImport, true)
		if (shouldUpdateUserLogin(formOptions)) {
			securityService.requirePermission(Permission.UserImport, true)
		}

		// Load the spreadsheet
		Workbook workbook = readImportSpreadsheet(filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		sheetInfoOpts.putAll(formOptions)

		List accounts = validateSpreadsheetContent(project, workbook, sheetInfoOpts, formOptions)

		return transformAccounts(accounts, sheetInfoOpts)
	}

	/**
	 * Used to load the spreadsheet into memory and validate that the information is correct
	 * @param filename - the name of the temporarilly saved spreadsheet
	 * @param options  - the options that the user chose when submitting the form
	 * @controllerMethod
	 * @return the spreadsheet file
	 */
	File generatePostResultsData(String filename, Map formOptions) {
		securityService.requirePermission(Permission.PersonImport, true)
		if (shouldUpdateUserLogin(formOptions)) {
			securityService.requirePermission(Permission.UserImport, true)
		}

		File file = new File(getJsonFilename(filename))
		if (!file.exists()) {
			throw new EmptyResultException('Unable to load the Account Import Post Results data')
		}

		file
	}

	/**
	 * Used to populate the model with the necessary properties for the Review form
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
	Map generateModelForReview(Project project, Map formOptions) {
		if (formOptions.flagToUpdatePerson) {
			securityService.requirePermission(Permission.PersonImport, true)
		}
		if (formOptions.flagToUpdateUserLogin) {
			securityService.requirePermission(Permission.UserImport, true)
		}

		Map model = [:]
		Map optionLabels = [
			(IMPORT_OPTION_BOTH): 'Person and UserLogin',
			(IMPORT_OPTION_PERSON): 'Person only',
			(IMPORT_OPTION_USERLOGIN): 'UserLogin only'
		]

		if (! formOptions[IMPORT_OPTION_PARAM_NAME] || ! optionLabels.containsKey(formOptions[IMPORT_OPTION_PARAM_NAME])) {
			log.warn "User $securityService.currentUsername invoked generateModelForReview() with invalid/missing importOption params=$formOptions"
			throw new InvalidParamException('The import option must be specified')
		}

		model.filename = formOptions.filename
		model.labels = getLabelsInColumnOrder('formPos')
		model.properties = getPropertiesInColumnOrder('formPos')
		model.gridMap = accountSpreadsheetColumnMap
		model.defaultSuffix = DEFAULTED_SUFFIX
		model.originalSuffix = ORIGINAL_SUFFIX
		model.errorSuffix = ERROR_SUFFIX
		model.importOption = formOptions[IMPORT_OPTION_PARAM_NAME]
		model.importOptionDesc = optionLabels[formOptions[IMPORT_OPTION_PARAM_NAME]]	// Used to display the user's option selection

		// Need to load the spreadsheet into the list of accounts so we can determine the accountsToRemoveFromProject param. This param
		// will be used to determine if warning message should be show when it is > 0 when user is about to post
		Workbook workbook = readImportSpreadsheet(formOptions.filename)
		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		List accounts = readAccountsFromSpreadsheet(workbook, sheetInfoOpts)
		model.accountsToRemoveFromProject = countRemoveFromProject(accounts)

		return model
	}

	/**
	 * Used to populate the model with the necessary properties for the Results form
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
	Map generateModelForPostResults(Project project, Map formOptions) {
		if (formOptions.flagToUpdatePerson) {
			securityService.requirePermission(Permission.PersonImport, true)
		}
		if (formOptions.flagToUpdateUserLogin) {
			securityService.requirePermission(Permission.UserImport, true)
		}

		Map model = [:]

		model.filename = formOptions.filename
		model.labels = getLabelsInColumnOrder('formPos')
		model.properties = getPropertiesInColumnOrder('formPos')
		model.gridMap = accountSpreadsheetColumnMap
		model.defaultSuffix = DEFAULTED_SUFFIX
		model.originalSuffix = ORIGINAL_SUFFIX
		model.errorSuffix = ERROR_SUFFIX

		return model
	}

	/**
	 * Used to populate the model with the necessary properties for the Review form
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
	Map postChangesToAccounts(Project project, Object formOptions) {
		if (formOptions.flagToUpdatePerson) {
			securityService.requirePermission(Permission.PersonImport, true)
		}
		if (formOptions.flagToUpdateUserLogin) {
			securityService.requirePermission(Permission.UserImport, true)
		}

		Workbook workbook = readImportSpreadsheet(formOptions.filename)

		Map sheetInfoOpts = getSheetInfoAndOptions(project, workbook)
		sheetInfoOpts.putAll(formOptions)

		// Read in the accounts and then validate them
		List accounts = validateSpreadsheetContent(project, workbook, sheetInfoOpts, formOptions)
		if (!accounts) {
			throw new EmptyResultException('Unable to read the spreadsheet or the spreadsheet was empty')
		}

		Map results = [personSkipped: 0, personCreated: 0, personUpdated: 0, personError: 0, personUnchanged: 0,
		               teamsUpdated: 0, teamsError: 0, userLoginCreated: 0, userLoginUpdated: 0, userLoginError: 0,
		               securityRoleError: 0, securityRolesUpdated: 0]

		List updatedAccounts = []

		log.debug "postChangesToAccounts() formOptions=$formOptions - processing ${accounts.size()} accounts"
		StringBuilder chgSB = new StringBuilder('<h2>Change History</h2>')
		StringBuilder errorsSB = new StringBuilder('<br><h2>Errors</h2><table><tr><th width=20>Row</th><th>Name</th><th>Errors</th></tr>')
		boolean recordedErrors = false

		for (int i=0; i < accounts.size(); i++) {
			accounts[i].postErrors = []

			// Skip over the accounts that have errors from the validation process
			if (accounts[i].errors) {
				if (formOptions.flagToUpdatePerson) {
					results.personSkipped++
				}
				if (formOptions.testMode) {
					recordedErrors = true
					errorsSB.append("<tr><td>${i+2}</td><td>${accounts[i].firstName} ${accounts[i].lastName}</td><td><ul><li>" +
						accounts[i].errors.join('<li>') + '</ul></td></tr>')
				}

				continue
			}

			String error
			boolean personChanged = false
			boolean userChanged = false
			boolean teamsChanged = false
			boolean securityRolesChanged = false

			// Reset the errors on the account
			accounts[i].errors = []

			Person person = accounts[i].person

			// Create / Update the persons and associated teams
			if (formOptions.flagToUpdatePerson) {
				(error, personChanged) = applyPersonChanges(person, accounts[i], sheetInfoOpts, formOptions)
				boolean personAndOrTeamsChanged = false
				log.debug "postChangesToAccounts() call to applyPersonChanges() returned person=${accounts[i].person}, error=$error, personChanged=$personChanged"
				if (error) {
					accounts[i].errors << ["Row ${i+2} $error"]
					updateAccounts << accounts[i]
					results.personError++
				} else {
					if (accounts[i].flags.isNewAccount) {
						results.personCreated++
					} else {
						if (personChanged) {
							personAndOrTeamsChanged = true
						}
					}

					// Update both the person personal teams as well as the team assignments to the project
					(error, teamsChanged) = applyTeamsChanges(accounts[i], project, sheetInfoOpts, formOptions)
					if (error) {
						results.teamsError++
						accounts[i].errors << error
					} else if (teamsChanged) {
						results.teamsUpdated++
						personAndOrTeamsChanged = true
					}
				}

				// Track the metrics of adds/updates
				if (personAndOrTeamsChanged) {
					results.personUpdated++
				} else if (! accounts[i].flags.isNewAccount) {
					results.personUnchanged++
				}
			}

			// Deal with the userLogin
			if (! error && accounts[i].flags.canUpdateUser) {
				boolean updateUserAndOrSecurity = false
				boolean isNewUser = accounts[i].flags.isNewUserLogin
				UserLogin userLogin
				if (person.id) {
					if (isNewUser) {
						userLogin = new UserLogin()
						userLogin.person = person
					} else {
						userLogin = person.userLogin
					}
				} else {
					throw new RuntimeException("Can not create a UserLogin until Person is saved for $person")
				}
				person.userLogin = userLogin

				(error, userChanged) = applyUserLoginChanges(userLogin, accounts[i], sheetInfoOpts, formOptions)
				if (error) {
					results.userLoginError++
					accounts[i].errors << error
				} else if (userChanged) {
					if (isNewUser) {
						personService.addToProject(securityService.getUserLogin(), project.id.toString(), person.id.toString())
						results.userLoginCreated++
					} else {
						updateUserAndOrSecurity = true
					}
					userPreferenceService.storePreference(userLogin, UserPreferenceEnum.CURR_PROJ, project.id)
				}

				(error, securityRolesChanged) = applySecurityRoleChanges(accounts[i])
				if (error) {
					results.securityRoleError++
					accounts[i].errors << error
				} else if (securityRolesChanged) {
					updateUserAndOrSecurity = true
					results.securityRolesUpdated++
				}

				// Track the update metrics base of either user or security updated
				if (updateUserAndOrSecurity && ! isNewUser) {
					results.userLoginUpdated++
				}
			}

			// Add the account to the new updatedAccounts to be displayed on the results page
			if (accounts[i].errors || personChanged || userChanged || teamsChanged || securityRolesChanged) {

				// Copy all of the properties out of the Accounts Map and transform to the format that the
				// data grid can use later
				Map account = [:]
				accountSpreadsheetColumnMap.each { prop, info ->
					String origKey = prop + ORIGINAL_SUFFIX
					String defKey = prop + DEFAULTED_SUFFIX
					String errKey = prop + ERROR_SUFFIX
					List keys = [prop, origKey, defKey, errKey]
					boolean hasTransform = info.transform
					keys.each { key ->
						if (accounts[i].containsKey(key)) {
							if (hasTransform) {
								account[key] = info.transform(accounts[i][key], sheetInfoOpts)
							} else {
								account[key] = accounts[i][key]
							}
						}
					}

					if (accounts[i].changeHistory) {
						account.changeHistory = accounts[i].changeHistory
					}
					account.flags = accounts[i].flags
				}
				updatedAccounts << account
			}

			// Add something to the temporary results view
			if (formOptions.testMode) {

				if (personChanged || userChanged || teamsChanged || securityRolesChanged) {
					chgSB.append("\r\n<br>Changes for $person ($person.id):<br><table><th>Property</th><th>Orig Value</th><th>New Value</th></tr>\r\n")
					StringBuilder changeMsg = new StringBuilder("***** Change History for $person changed:")
					accounts[i].changeHistory.each { prop, origVal ->
						String p = prop.split(/\./)[1]
						changeMsg.append("\n\t$prop was '$origVal' now is '${accounts[i][p]}'")
						chgSB.append("<tr><td>$prop</td><td>$origVal</td><td>${accounts[i][p]}</td></tr>\r\n")
					}
					log.info changeMsg.toString()
					chgSB.append("</table>\r\n")
				}

				if (accounts[i].errors) {
					recordedErrors = true
					errorsSB.append("<tr><td>${i+2}</td><td>${accounts[i].firstName} ${accounts[i].lastName}</td><td><ul><li>" +
						accounts[i].errors.join('<li>') + '</ul></td></tr>')
				}
			}
		}

		// Add the appropriate icons to the new list
		setIconsOnAccounts(updatedAccounts)

		saveResultsAsJson(updatedAccounts, formOptions.filename)

		// Delete the upload file
		deleteUploadedSpreadsheet(formOptions.filename)

		// Throw an exception so we don't commit the data while testing (to be removed)
		if (formOptions.testMode) {
			errorsSB.append('</table>')
			String resultData = results.toString() + chgSB + (recordedErrors ? errorsSB : '')
			throw new DomainUpdateException('<H2>Test Mode - Import Results</H2>' + resultData)
		}

		return results
	}

	/**
	 * Used to cancel a previously started import process
	 * @param params - the parameters from the HttpRequest
	 */
	void cancelPreviousUpload(Project project, Map formOptions) {
		securityService.requirePermission([Permission.PersonImport, Permission.UserImport], false,
			"attempted to cancel an account import for project $project")

		deleteUploadedSpreadsheet(formOptions.filename)
	}

	/**
	 * Used by the controller in the event that an exception was thrown and there's the potential that a file exists.
	 * @param formOptions - the params which will include the filename
	 */
	void deletePreviousUpload(Map formOptions) {
		securityService.requirePermission([Permission.PersonImport, Permission.UserImport], false,
			"attempted to delete an uploaded account import spreadsheet($formOptions.filename)")

		deleteUploadedSpreadsheet(formOptions.filename)
	}

	// --------------------------------
	// General purpose helper methods
	// --------------------------------

	/**
	 * This map is used to provide the user preferences for formatting date/times that will be used in rendering and reading the spreadsheet
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
		if (!map.dateFormatter) {
			throw new RuntimeException("Unable to load Date formatter for $map.dateFormatter")
		}

		map.dateTimeFormatter = TimeUtil.createFormatterForType(map.userDateFormat, TimeUtil.FORMAT_DATE_TIME_22)
		if (!map.dateTimeFormatter) {
			throw new RuntimeException("Unable to load DateTime formatter for $map.dateTimeFormatter")
		}

		// log.debug "getUserPreferences() preferences=$map, dateFormatter=${map.dateFormatter.toPattern()}, dateTimeFormatter=${map.dateTimeFormatter.toPattern()}"

		return map
	}

	/**
	 * Used to read in the TitleSheet information and load the TZ and Date/DateTime formatters used for most of the
	 * application.
	 * @param project
	 * @param workbook
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
	 * Used to determine based on user import options if the Person should be updated
	 * @param options - the map of the form params
	 * @return true if the user selected the correct options to update the Person otherwise false
	 */
	private boolean shouldUpdatePerson(Map options) {
		return [IMPORT_OPTION_PERSON, IMPORT_OPTION_BOTH].contains(options[IMPORT_OPTION_PARAM_NAME])
	}

	/**
	 * Used to determine based on user import options if the UserLogin should be updated
	 * @param options - the map of the form params
	 * @return true if the user selected the correct options to update the UserLogin otherwise false
	 */
	private boolean shouldUpdateUserLogin(Map options) {
		return [IMPORT_OPTION_USERLOGIN, IMPORT_OPTION_BOTH].contains(options[IMPORT_OPTION_PARAM_NAME])
	}

	/**
	 * Used to determine if any of the UserLogin properties have been set by the user
	 * based on there being an Original value set. Keep in mind if the value was Defaulted there
	 * could be both the Original and Defaulted.
	 * @param account - the account to examine
	 * @return true if changes were detected otherwise false
	 */
	private boolean hasUserLoginPropertiesSet(Map account) {

		debugLogAccountInfo('In hasUserLoginPropertiesSet about to see if there are changes:', account, 'UserLogin')
		log.debug "hasUserLoginPropertiesSet() account isa ${account.getClass().name}"

		boolean found = accountSpreadsheetColumnMap.find { prop, info ->
			boolean yup = false
			if (info.domain == 'U') {
				log.debug "hasUserLoginPropertiesSet() on prop $prop"
				String origKey = prop + ORIGINAL_SUFFIX
				String defKey = prop + DEFAULTED_SUFFIX
				yup = account.containsKey(origKey) && !account.containsKey(defKey)
				if (yup) {
					log.debug "hasUserLoginPropertiesSet() property $prop has been set"
				} else {
					log.debug "hasUserLoginPropertiesSet() for prop $prop, origKey=$origKey, defKey=$defKey"
				}

				if (! yup) {
					if ((account[prop] instanceof String)) {
						yup = ! StringUtil.isBlank(account[prop])
					} else {
						yup = account[prop] != null
					}
				}
			}
			return yup
		}

		debugLogAccountInfo("hasUserLoginPropertiesSet(found=$found)", account, 'UserLogin')
		return found
	}

	/**
	 * Used to get the labels in column order
	 */
	private List getLabelsInColumnOrder(type) {
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
	private List getPropertiesInColumnOrder(type) {
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
	private Map importParamsToOptionsMap(Map params) {
		Map options = [
			importOption:params[IMPORT_OPTION_PARAM_NAME],
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
	private Map importOptionsAsParams(Map options) {
		Map params = [importOption: options[IMPORT_OPTION_PARAM_NAME]]
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
	private List validateImportOptions(Map options) {
		List errors = []

		int expireDays = 90
		if (options.expireDays) {
			options.expireDays = NumberUtil.toPositiveLong(options.expireDays, -1)
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
			} else if (accounts[i].flags.isNewAccount) {
				icon = 'add.png'
			}
			accounts[i].icon = HtmlUtil.resource([dir: 'assets/icons', file: icon, absolute: false])
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
	 * Used to set the appropriate property on the account Map to indicate that the original value
	 * failed the validation.
	 * @param account - the Map with all of the account information
	 * @param property - the name of the property to set
	 * @param value - the original value that the domain object had
	 */
	private void setErrorValue(Map account, String property, value) {
		setValue(account, ERROR_SUFFIX, property, value)
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
		account[property + suffix] = value
	}

	/**
	 * Used to determine if an error was previously recorded against a property
	 * @param account - the account map information
	 * @param property - the name of the property
	 * @return true if a previous error was recorded on the property
	 * @IntegrationTest
	 */
	private boolean propertyHasError(Map account, String property) {
		account.containsKey(property + ERROR_SUFFIX)
	}

	/**
	 * Used to remove errors if they got recorded
	 * Note that this does not remove errors recorded against the account, just the property
	 * @param account - the account Map with all the goodies
	 * @param prop - the property for which to remove the error
	 */
	private void removeErrorValue(Map account, String prop) {
		if (accountSpreadsheetColumnMap.containsKey(prop)) {
			accountSpreadsheetColumnMap[prop].remove(prop + ERROR_SUFFIX)
		}
	}

	/**
	 * Used to access the ORIGINAL value set on the account
	 * @param account - the Map of the account
	 * @param property - the propertyName
	 * @return the value set on the property
	 */
	private getOriginalValue(Map account, String property) {
		return getPropertyValue(account, property, ORIGINAL_SUFFIX)
	}

	/**
	 * Used to access the DEFAULTED value set on the account
	 * @param account - the Map of the account
	 * @param property - the propertyName
	 * @return the value set on the property
	 */
	private getDefaultedValue(Map account, String property) {
		getPropertyValue(account, property, DEFAULTED_SUFFIX)
	}

	/**
	 * Used to access the ERROR value set on the account
	 * @param account - the Map of the account
	 * @param property - the propertyName
	 * @return the value set on the property
	 */
	private getErrorValue(Map account, String property) {
		getPropertyValue(account, property, ERROR_SUFFIX)
	}

	/**
	 * Used by the above get*Value methods to access the original/defaulted/error
	 * value set on the account.
	 * @param account - the Map of the account
	 * @param property - the propertyName
	 * @param suffix - the suffix for the property name
	 * @return the value set on the property
	 */
	private getPropertyValue(Map account, String property, String suffix) {
		account[property + suffix]
	}

	/**
	 * Used to determine the number of accounts that are flagged to be removed from the project
	 * @param accounts - the list of all of the accounts
	 * @return the count of accounts to be removed from the project
	 */
	private countRemoveFromProject(accounts) {
		int count = 0
		accounts.each { acct ->
			// Look through the projectTeam for any -STAFF references
			if ( acct.projectTeams.find( { tc -> isMinus(tc) && stripTheMinus(tc) == 'STAFF'} ) ) {
				count++
			}
		}
		return count
	}

	/**
	 * Utility method used to evaluate the default value of a property if defined. The default value may be a literal or
	 * a closure that must be used to compute the value. In this case it uses the sheetInfoOpts that contains any context
	 * data that might be necessary (e.g. the Project or the User)
	 * @param propertyName - the name of the property
	 * @param account - the Map with all of the account details
	 * @param sheetInfoOpts - the Map containing all of the context related data
	 * @param defValue - the default value pulled from the Meta Map, if null it will be looked up on demand
	 * @return the literal or computed default value for a given property
	 */
	private evaluateDefaultValue(String propertyName, Map account, Map sheetInfoOpts, def defValue=null) {
		// TODO : JPM 4/2016 : convert evaluateDefaultValue to a method from a closure
		def result
		if (defValue == null) {
			defValue = getPropertyDefaultValue(propertyName)
		}
		if (defValue instanceof Closure) {
			// Invoke the default closure
			result = defValue(propertyName, account, sheetInfoOpts)
			log.debug "evaluateDefaultValue($propertyName) computed the default $result"
		} else {
			// Convert the data type into the type that the system expects
			result = transformValueToDomainType(propertyName, defValue, sheetInfoOpts)
			log.debug "evaluateDefaultValue($propertyName) literal '$defValue' transformValueToDomainType to '$result'"
		}
		return result
	}

	/**
	 * Used to determine a property has had a default value set on it
	 * @param account - the account map information
	 * @param property - the name of the property
	 * @return true if a previous error was recorded on the property
	 * @IntegrationTest
	 */
	private boolean propertyHasDefaulted(Map account, String property) {
		account.containsKey(property + DEFAULTED_SUFFIX)
	}

	/**
	 * Used to remove the traces of any property having had a defaulted value set for a given domain on an account map. This is
	 * useful when we're not updating a particular domain so we don't want to show defaulted values which would cause confusion
	 * in the review process.
	 * @param account - the account map with all of the goodies
	 * @param domainName - the name of the domain to clear the properties on
	 */
	private void clearDefaultedValues(Map account, String domainName) {
		String domainCode = domainName[0]
		accountSpreadsheetColumnMap.each { prop, info ->
			if (info.domain == domainCode) {
				String defaulted = prop + DEFAULTED_SUFFIX
				if (account.containsKey(defaulted)) {
					account.remove(defaulted)

					String original = prop + ORIGINAL_SUFFIX
					account.remove(original)
				}
				// account[prop] = null
			}
		}
	}

	/**
	 * Utility method used to transform a property where there is a tranform defined in the accountSpreadsheetColumnMap map for a property
	 * otherwise it just returns the current value
	 * @param propName - the name of the property
	 * @param value - the current value
	 * @param sheetInfoOpts - the Map that contains all the goodies for tranforming data
	 * @return the transformed or original value appropriately
	 */
	private transformProperty(String propName, value, Map sheetInfoOpts) {
		def result
		if (accountSpreadsheetColumnMap.containsKey(propName)) {
			if (accountSpreadsheetColumnMap[propName].containsKey('transform')) {
				result = accountSpreadsheetColumnMap[propName].transform(value, sheetInfoOpts)
			} else {
				result = (value == null ? '' : value)
			}
		} else {
			throw new RuntimeException("transformProperty called with invalid property name $propName")
		}
		return result
	}

	/**
	 * Utility method used to return the default value of a property if defined in the accountSpreadsheetColumnMap map for a property
	 * otherwise it will return a null value
	 * @param propName - the name of the property
	 * @return the transformed or original value appropriately
	 */
	private getPropertyDefaultValue(String propName) {
		def result
		String defValPropName = 'defaultValue'
		if (accountSpreadsheetColumnMap.containsKey(propName)) {
			if (accountSpreadsheetColumnMap[propName].containsKey(defValPropName)) {
				result = accountSpreadsheetColumnMap[propName][defValPropName]
			}
		} else {
			throw new RuntimeException("getPropertyDefaultValue() called with invalid property name $propName")
		}
		return result
	}

	// A helper method used to determine if a value has leading minus
	private boolean isMinus(String str) {
		return (str?.startsWith('-'))
	}

	// A helper method that will strip off a minus prefix if it exists
	private String stripTheMinus(String str) {
		String r = str
		if ( isMinus(str) ) {
			r = (str.size() > 1 ? str.substring(1).trim() : '')
		}
		return r
	}

	/**
	 * Used to render a list of error messages from the Gorm constraints violations
	 * @param domainObj - the domain object to generate the list of errors on
	 * @return list of errors
	 */
	private List gormValidationErrors(domainObj) {
		List msgs = []
		domainObj.errors.allErrors.each {
			msgs << "$it.field error $it.code"
		}
		return msgs
	}

	/**
	 * Used to set the error codes onto the properties that errored so that we can put the error
	 * in context of where the error is.
	 * @param account - the Map with all of the account details
	 * @param domainObj - the domain object to generate the list of errors on
	 */
	private registerGormErrorsOnProperties(Map account, domainObj) {
		domainObj.errors.allErrors.each {
			String prop=it.getField()
			if (! propertyHasError(account, prop)) {
				setErrorValue(account, prop, it.getCode())
			}
		}
	}

	/**
	 * Used to verify for a given domain if unplanned changes should be identified because the user's input option
	 * was to exclude the domain
	 * @param options - the sheet options that are passed around
	 * @param domainName - the name of the domain being processed
	 * @return a boolean indicating true if changes should be identify changes
	 */
	private boolean shouldIdentifyUnplannedChanges(Map options, String domainName) {
		boolean should=false
		if (domainName == 'Person' && ! shouldUpdatePerson(options)) {
			should = true
		} else if (domainName == 'UserLogin' && ! shouldUpdateUserLogin(options)) {
			should = true
		}
		return should
	}

	/**
	 * Used for outputting an account or the whole accounts list to the debug log
	 * @param header - a string header message
	 * @param accounts - a single account or the list of accounts
	 */
	private void debugLogAccountInfo(String header, Map accounts, String domainName) {
		Collection list = CollectionUtils.asCollection(accounts)

		// Used to possibly filter the domain to list properties for
		List domainCodes = ['P','U','T']

		StringBuilder sb = new StringBuilder()
		if (domainName) {
			domainCodes = [ domainName[0] ]
		}

		list.each { account ->
			sb.append("$header:")
			log.debug "debugLogAccountInfo() account isa ${account.getClass().name} $account.firstName $account.lastName"
			accountSpreadsheetColumnMap.each { prop, info ->
				if (info.domain in domainCodes) {
					String acctPropAlternates = prop + '_'
					Map properties = account.findAll { acctPropName, value ->
						acctPropName == prop ||  acctPropName.startsWith(acctPropAlternates)
					}
					properties.each { p, v ->
						sb.append("\n\t$p=$v")
					}
				}
			}
		}
		log.debug sb.toString()
	}

	// ---------------------------
	// Spreadsheet Helper Methods
	// ---------------------------

	/**
	 * Used to retrieve a blank Account Export Spreadsheet
	 * @return The blank spreadsheet
	 */
	private Workbook getAccountExportTemplate(format = "xlsx") {
		// Load the spreadsheet template and populate it
		String templateFilename = ACCOUNT_EXPORT_TEMPLATE[format]
        Workbook spreadsheet = ExportUtil.loadSpreadsheetTemplate(templateFilename)
		log.info("OLB generating: ${spreadsheet.class}")
        updateSpreadsheetHeader(spreadsheet)
        addRolesToSpreadsheet(spreadsheet)
        addTeamsToSpreadsheet(spreadsheet)

        return spreadsheet
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
	 * @param project - the project that this export is for
	 * @param sheet - the spreadsheet to update
	 */
	private void updateTitleSheetInfo(Project project, sheet, sheetOptions) {
		def tab = sheet.getSheet(TEMPLATE_TAB_TITLE)
		if (!tab) {
			throw new EmptyResultException("The $TEMPLATE_TAB_TITLE sheet is missing from the workbook")
		}

		def exportedOn = TimeUtil.formatDateTimeWithTZ(sheetOptions.userTzId, new Date(), sheetOptions.dateTimeFormatter)
		log.debug "updateTitleSheetInfo() sheetOptions=$sheetOptions"
		log.debug "updateTitleSheetInfo() exportedOn=$exportedOn, dateTimeFormat=${sheetOptions.dateTimeFormatter.toPattern()}"

		def addToCell = { prop, val ->
			if (! TitlePropMap.containsKey(prop)) {
				throw new RuntimeException("updateTitleSheetInfo() referenced invalid element '$prop' of TitlePropMap")
			}
			WorkbookUtil.addCell(tab, TitlePropMap[prop][0], TitlePropMap[prop][1], val)
		}

		addToCell('clientName', project.client.toString())
		addToCell('projectId', project.id.toString())
		addToCell('projectName', project.name)
		addToCell('exportedBy', securityService.userLoginPerson.toString())
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
	 //Workbook
	private Map readTitleSheetInfo(Project project, Workbook workbook, Map sheetOpts) {

		def sheet = workbook.getSheet(TEMPLATE_TAB_TITLE)
		if (!sheet) {
			throw new InvalidRequestException("The spreadsheet was missing the '$TEMPLATE_TAB_TITLE' sheet")
		}

		Map map = [:]

		// Helper closure that will get cell values from the sheet based on the col/row mapping in the TitlePropMap MAP
		// Note that the Datetime case will depend on the map.timezone property read from the spreadsheet
		def getCell = { prop ->
			def (int col, int row, String type) = TitlePropMap[prop]
			def val

			// log.debug "getCell($prop,$type)"
			// log.debug "   for col ${TitlePropMap[prop][0]}, row ${TitlePropMap[prop][1]}"
			switch (type) {
				case 'Integer':
					val = WorkbookUtil.getIntegerCellValue(sheet, col, row)
					break
				case 'Date':
					// log.debug "readTitleSheetInfo() calling WorkbookUtil.getDateCellValue for $col, $row with ${sheetOpts.sheetDateFormatter.toPattern()}"
					val = WorkbookUtil.getDateCellValue(sheet, col, row, (DateFormat) sheetOpts.sheetDateFormatter)
					break
				case 'Datetime':
					// log.debug "readTitleSheetInfo() calling WorkbookUtil.getDateTimeCellValue for $col, $row with ${sheetOpts.sheetDateTimeFormatter.toPattern()}"
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
		sheetOpts.sheetDateTimeFormatter = TimeUtil.createFormatterForType(sheetOpts.sheetDateFormat, TimeUtil.FORMAT_DATE_TIME_22, sheetOpts.sheetTzId)
		assert sheetOpts.sheetDateTimeFormatter

		// Note that the exportedOn property is dependent on timezone being previously loaded
		sheetOpts.sheetExportedOn = getCell('exportedOn')
		if (sheetOpts.sheetExportedOn == -1) {
			log.error "*** readTitleSheetInfo() the Exported On wasn't properly read from the spreadsheet"
			throw new InvalidRequestException("Unable to parse the 'Exported On' value from the '$TEMPLATE_TAB_TITLE' sheet")
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
					log.debug "validateSpreadsheetHeader() expected '$info.label' but found '$label' for column ${WorkbookUtil.columnCode(colPos)}"
					ok = false
				}
			}
		}
		return ok
	}

	/**
	 * Used to output a spreadsheet to the browser
	 * @param response - the servlet response object
	 * @param spreadsheet - the spreadsheet object
	 * @param filename - the filename that it should be saved as on the client
	 */
	private void sendSpreadsheetToBrowser(response, Workbook spreadsheet, String filename) {
		ExportUtil.setContentType(response, filename + "." + ExportUtil.getWorkbookExtension(spreadsheet))
		spreadsheet.write(response.outputStream)
		response.outputStream.flush()
	}

	/**
	 * This is used to get the fully qualified filename with path of where the temporary files are written
	 * @param filename - the name of the file without a path
	 * @return the filename with the path prefix
	 */
	private String getFilenameWithPath(String filename) {
		checkPathTraversals filename
		coreService.getAppTempDirectory() + '/' + filename
	}

	private File getFile(String filename) {
		checkPathTraversals filename
		new File(coreService.getAppTempDirectory(), filename)
	}

	private void checkPathTraversals(String filename) {
		// Make sure that the user can NOT perform any PATH tranversal
		if (StringUtil.containsPathTraversals(filename)) {
			log.warn "getFilenameWithPath() called with a HACKED filename $filename"
			throw new EmptyResultException('Unable to locate uploaded spreadsheet')
		}
	}

	/**
	 * Used to delete the uploaded spreadsheet file from the temporary upload directory
	 * @param filename - the name of the file without a path
	 */
	@Transactional
	private void deleteUploadedSpreadsheet(String filename) {
		File fqfn = getFile(filename)
		if (fqfn.exists()) {
			if (fqfn.delete()) {
				log.debug "deleteUploadedSpreadsheet() deleted $fqfn.path"
			} else {
				log.error "deleteUploadedSpreadsheet() Failed to delete $fqfn.path"
			}
		} else {
			log.debug "deleteUploadedSpreadsheet() didn't find file $fqfn.path"
		}
	}

	// ---------------------------
	// Export Spreadsheet Methods
	// ---------------------------

	/**
	 * Used to generate a spreadsheet of project staff and optionally their login information
	 * @param project - the user's project context
	 * @param formOptions - A map of the form variables submitted by the user
	 * @permission PersonExport
	 */
	private Workbook generateAccountExportSpreadsheet(Project project, Map formOptions) {
		if (!project) return

		List persons = []

		// Get the staff for the project
		switch (formOptions.staffType) {
			case 'CLIENT_STAFF':
				persons = partyRelationshipService.getAllCompaniesStaffPersons([project.client])
				break

			case 'AVAIL_STAFF':
				// TODO : JPM 4/2016 : This needs to be reviewed because we're not returning all of the correct accounts
				// Long companyId = project.client.id
				// persons = partyRelationshipService.getAllCompaniesStaffPersons(Party.get(companyId))
				persons = projectService.getAssignableStaff(project, securityService.userLoginPerson)
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

		Map sheetInfoOpts = getUserPreferences()

		//log.info "FORMAT: $formOptions.exportFormat"
		def workbook = getAccountExportTemplate(formOptions.exportFormat)

		populateAccountSpreadsheet(project, persons, workbook, formOptions, sheetInfoOpts)

		return workbook
	}

	/**
	 * This method will iterate over the list of persons and populate the spreadsheet appropriately
	 * @param project - the project associated to the accounts being exported
	 * @param persons - the list of persons to be exported
	 * @param workbook - the spreadsheet workbook to be populated
	 * @param formOptions - a map of options from the user input fomr
	 * @param sheetInfoOpts - a map of options used in formating dates in the sheet
	 */
	private void populateAccountSpreadsheet(
		Project project, List persons, Workbook workbook,
		Map formOptions,
		Map<String, Object> sheetInfoOpts ) {

		List elapsedNow = [new Date()]

		updateTitleSheetInfo(project, workbook, sheetInfoOpts)

		// log.debug "updateTitleSheetInfo took ${TimeUtil.elapsed(elapsedNow)}"

		def sheet = workbook.getSheet(TEMPLATE_TAB_NAME)

		Date now = new Date()
		int row = 1
		int max = persons.size()
		persons.eachWithIndex{ person, index ->
			if (row % 10 == 0) {
				log.info "Exported $row staff records of $max"
			}
			Map account = personToFieldMap(person, project, sheetInfoOpts)

			boolean includeAccount=false
			UserLogin userLogin = person.userLogin

			//log.debug "personToFieldMap took ${TimeUtil.elapsed(elapsedNow)}"
			switch (formOptions.loginChoice) {
				case 1:
					// With login account
					if (userLogin) {
						includeAccount=true
					}
					break

				case 2:
					// With NO login account
					if (! userLogin) {
						includeAccount=true
					}
					break

				case 3:
					// With ACTIVE account
					if (userLogin && userLogin.userActive()) {
						includeAccount=true
					}
					break

				case 4:
					// With INACTIVE account
					if (userLogin && ! userLogin.userActive()) {
						includeAccount=true
					}
					break

				default:
					includeAccount=true
			}

			if (! includeAccount) {
				return
			}

			if (formOptions.includeLogin=='Y') {

				if (userLogin) {
					Map userMap = userLoginToFieldMap(userLogin, sheetInfoOpts)
					// log.debug "userMap = $userMap"
					account.putAll(userMap)
					// log.debug "userLoginToFieldMap took ${TimeUtil.elapsed(elapsedNow)}"
				}
			}

			// Now that we have the map, we can iterate over the account map
			addAccountToSpreadsheet(sheet, account, row++, sheetInfoOpts)

			// log.debug "addRowToAccountSpreadsheet took ${TimeUtil.elapsed(elapsedNow)}"

		}
	}

	/**
	 * This method outputs all the account mapped fields to a row in the sheet
	 * @param sheet - the spreadsheet to update
	 * @param account - the map of the account properties
	 * @param rowNumber - the row in the spreadsheet to insert the values
	 */
	private void addAccountToSpreadsheet(sheet, Map account, int rowNumber, Map sheetInfoOpts) {
		// Loop through the SpreadSheet Map and add to the cells
		accountSpreadsheetColumnMap.each { prop, info ->
			def colPos = info.ssPos
			if (colPos != null) {
				def val = account[prop]
				if (info.transform) {
					val = info.transform(val, sheetInfoOpts)
				}
				// log.debug "addAccountToSpreadsheet() adding col=$colPos, row=$rowNumber, prop $prop value isa ${val?.getClass()?.name} : $val "
				WorkbookUtil.addCell(sheet, colPos, rowNumber, val)
			}
		}
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
		List roles = securityService.allRoles
		//RoleType.findAllByType(RoleType.SECURITY, [order:'level'])
		int row = 1
		roles.each {r ->
			if (r.id == 'TEST_ROLE') {
				return
			}
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

		// map.personTeams  = personTeams.join(", ")
		// map.projectTeams = projectTeams.join(", ")
		// map.roles        = roles.join(", ")
		map.personTeams  = personTeams
		map.projectTeams = projectTeams
		map.roles        = roles
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
		// map.lastLogin = TimeUtil.formatDateTimeWithTZ(sheetInfoOpts.userTzId, user.lastLogin, sheetInfoOpts.dateTimeFormatter)
		map.lastLogin = user.lastLogin

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
			if (info.type == 'date' && domainObj[prop]) {
				// For dates we need to strip out the time element
				map[prop] = domainObj[prop].clearTime()
			} else {
				map[prop] = domainObj[prop]
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
		def result
		if (! type) {
			throw RuntimeException("Unable to find '$property' in field definition map")
		}
		if (value != null) {
			switch (type) {
				case 'boolean':
					result = StringUtil.toBoolean(value)
					log.debug "transformValueToDomainType($property, $value)=$result"
					break
				case 'date':
					result = value instanceof Date ? value : TimeUtil.parseDate(value, sheetInfoOpts.dateFormatter)
					break
				case 'datetime':
					result = value instanceof Date ? value : TimeUtil.parseDateTimeWithFormatter(
						sheetInfoOpts.sheetTzId, value, sheetInfoOpts.dateTimeFormatter)
					break
				default:
					// For properties (e.g. String that don't get converted)
					result = value
			}
		}
		// log.debug "*** transformValueToDomainType() property=$property, type=$type, $result isa ${result?.getClass()?.name}"
		return result
	}

	// ---------------------------
	// Import Spreadsheet Methods
	// ---------------------------

	/**
	 * This method will read in all of the content from the accounts sheet and the invoke the validation logic
	 * @param project - the user's project in his context
	 * @param workbook - the spreadsheet that was loaded
	 * @param sheetInfoOpts - the map containing the information from the workbook title and the TZ/Date stuff
	 * @param formOptions - the user input params from the form
	 * @return The list of accounts mapped out
	 */
	private List<Map> validateSpreadsheetContent(Project project, Workbook workbook, Map sheetInfoOpts, Map formOptions) {

		// Read in the accounts and then validate them
		List accounts = readAccountsFromSpreadsheet(workbook, sheetInfoOpts)

		//debugLogAccountInfo('validateSpreadsheetContent before', accounts)
		// Validate the sheet
		validateUploadedAccounts(accounts, project, sheetInfoOpts, formOptions)

		//debugLogAccountInfo('validateSpreadsheetContent after', accounts)

		return accounts
	}

	/**
	 * Used to pull the uploaded file from the request and save it to a temporary file with a randomly generated
	 * name. After saving the file the filename and File handle are returned in a list.
	 * @param request - the servlet request object
	 * @param paramName - the name of the form parameter that contains the upload file
	 * @return The name of the filename that was saved (excluding the path)
	 */
	private String saveImportSpreadsheet(request, String paramName) {
		MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
		MultipartFile xlsFile = mpr.getFile(paramName)

		// Generate a random filename to store the spreadsheet between page loads
		String filename = 'AccountImport-' + securityService.currentUserLoginId + '-' +
			SecurityUtil.randomString(10) + '.xls'

		// Save file locally
		File fqfn = getFile(filename)
		log.info "saveImportSpreadsheet() user $securityService.currentUsername uploaded AccountImport spreadsheet to $fqfn.path"

		xlsFile.transferTo(fqfn)

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
	 * Used to write the results of the update out to the temporary directory as JSON so that it can then be
	 * rendered to the user.
	 * @param filename - the filename to spreadsheet (which assumes it is in the app configured tmp directory)
	 * @return the spreadsheet itself
	 */
	private String saveResultsAsJson(List accounts, String filename) {
		String fqfn = getJsonFilename(filename)
		log.debug "saveResultsAsJson() filename=$filename fqfn=$fqfn"
		File file = new File(fqfn)
		if (file) {
			file.write( new JsonBuilder(accounts).toPrettyString() )
		}
		return filename
	}

	/**
	 * Used to read a spreadsheet from the file system into a Workbook which is returned
	 * @param filename - the filename to spreadsheet (which assumes it is in the app configured tmp directory)
	 * @return the spreadsheet itself
	 */
	private Workbook readImportSpreadsheet(String filename) {
		if (! filename) {
			throw new InvalidParamException('The import filename parameter was missing')
		}

		File file = new File(coreService.getAppTempDirectory(), filename)
		if (! file || ! file.exists()) {
			throw new EmptyResultException('Unable to read from the uploaded spreadsheet')
		}
		return WorkbookFactory.create(file)
	}

	/**
	 * Used to read the Account Import Spreadsheet and load up a list of account+user properties. This will
	 * iterate over the accountSpreadsheetColumnMap Map to pluck the values out of the appropriate columns of
	 * each row and add to the map that is returned for each person/userlogin.
	 * ---- The values are just saved in their String type and will be manipulated later. ----
	 * The values are saved into the Map in the domain specific data type primarily because we need to
	 * be able to change date/datetimes potentially in different timezones and saving as string will prevent
	 * this capability.
	 * @param spreadsheet - the spreadsheet to read from
	 * @return the list that is read in
	 */
	private List<Map> readAccountsFromSpreadsheet(Workbook spreadsheet, Map sheetInfoOpts) {
		int firstAccountRow = 1
		def sheet = spreadsheet.getSheet( TEMPLATE_TAB_NAME )
		int lastRow = sheet.getLastRowNum()
		List accounts = []

		for (int row = firstAccountRow; row <= lastRow; row++) {
			// Initialize the account map
			Map account = [errors:[], matches:[], flags: [:], changeHistory: [:]]

			int pIdx = 0
			accountSpreadsheetColumnMap.each { prop, info ->
				Integer colPos = info.ssPos
				def value=null
				if (colPos != null) {
					switch (info.type) {
						case 'datetime':
							value = WorkbookUtil.getDateTimeCellValue(sheet, colPos, row, sheetInfoOpts.sheetTzId, sheetInfoOpts.sheetDateTimeFormatter)
							if (value == -1) {
								account.errors << "Invalid date value in ${WorkbookUtil.columnCode(colPos)}${row + FIRST_DATA_ROW_OFFSET}"
								setErrorValue(account, prop, 'Invalid datetime')
								value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							}
							break
						case 'date':
							value = WorkbookUtil.getDateCellValue(sheet, colPos, row, (DateFormat) sheetInfoOpts.sheetDateFormatter)
							if (value == -1) {
								account.errors << "Invalid datetime value in ${WorkbookUtil.columnCode(colPos)}${row + FIRST_DATA_ROW_OFFSET}"
								setErrorValue(account, prop, 'Invalid date')
								value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							}
							break

						case 'boolean':
							value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							if (value?.size()>0) {
								Boolean bv = StringUtil.toBoolean(value)
								if (bv == null) {
									account[prop] = value
									setErrorValue(account, prop, 'Must be Y|N')
								} else {
									value = bv
								}
							}
							break

						default:
							value = StringUtil.sanitize( WorkbookUtil.getStringCellValue(sheet, colPos, row) )
							if (info.containsKey('validator')) {
								if (! info.validator(value, sheetInfoOpts)) {
									account.errors << "$info.label is invalid (${WorkbookUtil.columnCode(colPos)}${row + FIRST_DATA_ROW_OFFSET})"
									setErrorValue(account, prop, value)
								}
							}

							if (info.type == 'list') {
								value = StringUtil.splitter(value, ',', DELIM_OPTIONS)
							}
							break
					}

					// if (prop == 'roles')  log.debug "+=+=+=+=+=+=+ roles = $value"
					// account[prop] = (account.errors.size() > 0 ? null : value)
					account[prop] = value
				}
			}
			//accounts.add(account)
			addToAccounts(accounts, account)
		}
		return accounts
	}

	private void addToAccounts(List accounts, Map account) {
		if (isValidAccount(account)) {
			accounts.add(account)
		}
	}

	/**
	 * Validate if required account fields are not empty.
	 * Fields to be validated from <code>accountSpreadsheetColumnMap [firstName, middleName, lastName, company]</code>
	 * @param account
	 * @return
	 */
	private boolean isValidAccount(Map account) {
		return (
				StringUtils.isNotEmpty(account['fistName'] as String)
						|| StringUtils.isNotEmpty(account['middleName'] as String)
						|| StringUtils.isNotEmpty(account['lastName'] as String)
						|| StringUtils.isNotEmpty(account['company'] as String)
		)
	}

	/**
	 * Used to validate the list of accounts that were uploaded and will populate the individual maps with
	 * properties errors when anything is found. The logic will update the accounts object that is passed into
	 * the method.  The logic will update the accounts data with the following information:
	 *    - set companyObj to the company
	 *    - set person properties with ORIGINAL_SUFFIX suffix if the value has been changed from the original
	 *    - creates submap flags to hold all logic flags used in various spots in the code
	 *    -    set flags.isNewAccount to indicate if the person is new or not
	 *    - set default values for properties not populated - values coming from the domain objects or from the default values in the def map
	 *    - creates errors List to track errors
	 *    -     appends any error messages to errors list that occur
	 * @param accounts - the list of accounts that are read from the spreadsheet
	 * @param project - the user's project in their context
	 * @param sheetInfoOpts - the worksheet title page info plus the TZ/Date stuff
	 * @param formOptions - form params from the page submission
	 */
	private void validateUploadedAccounts(List<Map> accounts, Project project, Map sheetInfoOpts, Map formOptions) {
		List usernameList
		List validRoleCodes
		//log.debug "validateUploadedAccounts() formOptions=$formOptions"

		// TODO : JPM 4/2016 : readAccountsFromSpreadsheet - need to check the person last modified against the spreadsheet time

		// def emailValidator = EmailValidator.getInstance()

		List personIdList = accounts.findAll { it.personId }.collect {it.personId }

		// Get all of the email address that were uploaded
		List emailList = accounts.findAll( { it.email })?.collect({it.email.toLowerCase()})

		List<String> allTeamCodes

		Map companiesById = projectService.getCompaniesMappedById(project)

		if (true || formOptions.flagToUpdateUserLogin) {
			validRoleCodes = securityService.allRoleCodes

			// Get all of the usernames that were uploaded
			usernameList = accounts.findAll( { it.username })?.collect({it.username.toLowerCase()})
		}

		if (true || formOptions.flagToUpdatePerson) {
			// Get all teams except AUTO and we need to stuff STAFF into it
			allTeamCodes = partyRelationshipService.getTeamCodes()
			allTeamCodes << 'ROLE_STAFF'
			//log.debug "allTeamCodes = $allTeamCodes"
		}

		// Validate the teams, roles, company and any other things need be validated
		for (int i=0; i < accounts.size(); i++) {
			//log.debug "validateUploadedAccounts() Starting logic for $i \n\t accounts[$i] = ${accounts[i]}"

			// Boolean found
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
			validateCompanyName(accounts[i], project, companiesById, sheetInfoOpts)

			// Now attempt to find the person by their name
			List people = findPersonsByName(accounts[i])
			if (people.size() > 0) {
				accounts[i].matches << 'name'
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
					}
				}
			}

			// Set a flag on the account if it is going to be a new account
			if (accounts[i].person == null) {
				//log.debug "validateUploadedAccounts() - creating blank Person"
				accounts[i].flags.isNewAccount = true
				accounts[i].person = new Person()
			} else {
				accounts[i].flags.isNewAccount = false
				if (!personById) {
					setDefaultedValue(accounts[i], 'personId', accounts[i].person.id)

					// Since the account was NOT found by the ID, this flag will be used to prevent
					// blank values in the spreadsheet from overwriting existing values
					accounts[i].flags.blockBlankOverwrites = true
				}
			}

			//
			// Now for some Person specific validation
			//
			if (true || formOptions.flagToUpdatePerson) {
				validatePerson(accounts[i], sheetInfoOpts)

				//debugLogAccountInfo('validateUploadedAccounts() Person after validatePerson', accounts[i], 'Person')
				//debugLogAccountInfo('validateUploadedAccounts() UserLogin after validatePerson', accounts[i], 'UserLogin')

				// Get all teams except AUTO
				validateTeams(accounts[i], project, allTeamCodes)
			}
			// log.debug "validateUploadedAccounts() formOptions=$formOptions"
			//
			// User specific validation
			//
			if (true || formOptions.flagToUpdateUserLogin) {

				// Validate user and security roles if user requested to update Users
				boolean canUpdateUser = (accounts[i].username?.size() > 0)
				//log.debug "validateUploadedAccounts() 1. canUpdateUser=$canUpdateUser"
				// TODO : JPM 4/2016 : check for new create user permission
				// Check if user is trying to create a user without a person already created
				if (canUpdateUser && accounts[i].flags.isNewAccount && !formOptions.flagToUpdatePerson) {
					accounts[i].errors << 'User can not be created without a saved person'
					canUpdateUser = false
				}
				//log.debug "validateUploadedAccounts() 2. canUpdateUser=$canUpdateUser"

				if (canUpdateUser) {
					accounts[i].flags.canUpdateUser = true
				}
				validateUserLogin(accounts[i], project, sheetInfoOpts)

				// Validate that the teams codes are correct and map out what are to add and delete appropriately
				validateSecurityRoles(accounts[i], validRoleCodes, sheetInfoOpts)
				//debugLogAccountInfo('validateUploadedAccounts() Person after validateSecurityRoles', accounts[i], 'Person')
				//debugLogAccountInfo('validateUploadedAccounts() UserLogin after validateSecurityRoles', accounts[i], 'UserLogin')
			}

			if (!accounts[i].flags.canUpdateUser) {
				accounts[i].flags.canUpdateUser = false
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
			if (emailList.findAll{ it == accounts[i].email?.toLowerCase() }.size() > 1) {
				accounts[i].errors << 'Email referenced on multiple rows'
			}

			// Check the username to make sure it isn't used by more than one row
			if (usernameList.findAll{ it == accounts[i].username?.toLowerCase() }.size() > 1) {
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
	 * @param account - the map used to track the person/user properties
	 * @param sheetInfoOpts - the workbook title sheet info + the TzID and date stuff
	 * @return returns true if the lookup was successful, false if not or Null if the routine err
	 */
	private Boolean validatePerson(Map account, Map sheetInfoOpts) {
		Boolean ok = false
		// Load a temporary Person domain object with the properties from the spreadsheet and see if any
		// of the valids will break the validation constraints

		debugLogAccountInfo('**** validatePerson', account, 'Person')

		Person.withNewSession { ses ->
			Person personToValidate = (account.person?.id ? Person.get((account.person.id)) : new Person() )

			applyChangesToDomainObject(personToValidate, account, sheetInfoOpts, true)

			ok = personToValidate.validate()
			if (! ok) {
				account.errors.addAll(gormValidationErrors(personToValidate))
				registerGormErrorsOnProperties(account, personToValidate)

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
	 * @param account - the map used to track the person/user properties
	 * @param project - the user's project in their context
	 * @param sheetInfoOpts - the workbook title sheet info + the TzID and date stuff
	 * @return returns true if the lookup was successful, false if not or Null if the routine err
	 */
	private Boolean validateUserLogin(Map account, Project project, Map sheetInfoOpts) {
		boolean ok = true
		boolean hasUserChanges = hasUserLoginPropertiesSet(account)
		boolean shouldUpdateUser = shouldUpdateUserLogin(sheetInfoOpts)
		boolean hasUsername = ! StringUtil.isBlank(account.username)

		log.debug "validateUserLogin() hasUserChanges=$hasUserChanges, shouldUpdateUser=$shouldUpdateUser"
		debugLogAccountInfo("validateUserLogin", account, 'UserLogin')

		if (hasUserChanges && ! hasUsername) {
			account.errors << 'User changes but missing Username'
			return false
		}

		if (! hasUserChanges && ! hasUsername) {
			return true
		}

//		def existingPersonUserLogin = account.person.userLogin

		UserLogin.withNewSession { ses ->
			boolean personExists = account.person.id
			UserLogin userLogin

			// Get or create a UserLogin
			if (personExists) {
//				userLogin = existingPersonUserLogin
				userLogin = account.person.userLogin
			}
			if (! userLogin) {
				account.flags.isNewUserLogin = true
				userLogin = new UserLogin()

				// We need a real person associated with the UserLogin to pass validation so we
				// can use the authenticated user if necessary.
				userLogin.person = personExists ? account.person : securityService.loadCurrentPerson()
				// Set the password for the test
				userLogin.password = 'phoof'
				//	userLogin.expiryDate = projectService.defaultAccountExpirationDate(project)
			}

			if (hasUserChanges) {
				if ( !shouldUpdateUser) {
					// TODO : iterate over the changed fields and put the error changed values
				} else {
					// Let's check to make sure that the person has an email address which is a requirement
				}
			}

			// Let's check to make sure that the person has a username and  email address which are required for existing
			// User logins or when we're in the process of creating a new one
			if (( ! account.flags.isNewUserLogin || (account.flags.isNewUserLogin && hasUserChanges) ) ) {
				if (! account.email ) {
					setErrorValue(account, 'email', 'Required')
					account.errors << 'Email Required'
				}

				if (! account.username) {
					setErrorValue(account, 'username', 'Required')
					account.errors << 'Username Required'

					// Throw a bogus username on the domain for the validation purposes
					userLogin.username = RSU.randomAlphabetic(10)
				}
			}

			debugLogAccountInfo("validateUserLogin BEFORE applyChangesToDomainObject() hasUserChanges=$hasUserChanges", account, 'UserLogin')

			applyChangesToDomainObject(userLogin, account, sheetInfoOpts, true, true)

			debugLogAccountInfo("validateUserLogin AFTER applyChangesToDomainObject() hasUserChanges=$hasUserChanges", account, 'UserLogin')


			hasUserChanges = hasUserLoginPropertiesSet(account)

			debugLogAccountInfo("validateUserLogin() AFTER hasUserLoginPropertiesSet() hasUserChanges=$hasUserChanges", account, 'UserLogin')

			// log.debug "validateUserLogin() hasUserChanges now=$hasUserChanges"

			if ( shouldUpdateUser && hasUserChanges ) {
				ok = userLogin.validate()
				if (! ok ) {
					registerGormErrorsOnProperties(account, userLogin)
					account.errors.addAll(gormValidationErrors(userLogin))
				}
			}
			userLogin.discard()
		}
		// If we are not going to be updating the UserLogin then we should remove any of the defaulted values
		// so that in the UI it doesn't look like anything occurred
		if (! shouldUpdateUser || ! hasUserChanges) {
			log.debug "validateUserLogin() calling clearDefaultedValues()"
			clearDefaultedValues(account, 'UserLogin')
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
	private Boolean validateCompanyName(Map account, Project project, Map companiesById, Map sheetInfoOpts) {
		Boolean ok = false
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
				ok = true
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
	 * @param account - the Map containing all of the changes
	 * @param validRoleCodes - a list of the valid role codes in the system
	 * @param A flag that indicates success (true) or an error occurred (false)
	 */
	private boolean validateSecurityRoles(Map account, List<String> validRoleCodes, Map sheetInfoOpts ) {
		boolean ok=false

		String prop = 'roles'
		List<String> currentRoles = []
		boolean isNewUser = true

		// Validate the Setup the default security role if necessary
		UserLogin userLogin
		if (account.person.id) {
			userLogin = account.person.userLogin
			if (userLogin) {
				currentRoles = securityService.getAssignedRoleCodes(userLogin)
				isNewUser = false
			}
		}

		// String importedRoles = account[prop]
		// List roleChanges = StringUtil.splitter(importedRoles, ',', DELIM_OPTIONS)
		List<String> roleChanges = account[prop].clone() as List

		// Check to see if there is a username while creating and if not we'll bail out of this
		if (isNewUser && ! account.username) {
			if (roleChanges) {
				account.errors << 'Security change but missing username'
				return false
			}
			return true
		}

		boolean setDefaulted = false

		// Add the DEFAULT security code if it appears that the user wouldn't otherwise be assigned one
		if (! currentRoles && ! roleChanges.find { it[0] != '-' }) {
			log.debug "validateSecurityRoles() set default role"
			String defRole = SecurityService.DEFAULT_SECURITY_ROLE_CODE
			roleChanges << defRole
			setDefaultedValue(account, prop, roleChanges)
			setDefaulted = true
		}

		Map changeMap = determineSecurityRoleChanges(validRoleCodes, currentRoles, roleChanges)
		log.debug "validateSecurityRoles() changeMap=$changeMap"
		if (changeMap.error) {
			account.errors << changeMap.error
			setErrorValue(account, prop, [changeMap.error])
		} else {
			ok = true
			if (changeMap.hasChanges) {
				// For roles we are going to want to show the imported values along with the original and the resulting
				// changes so we'll use all three underlying properties
				if (! setDefaulted) {
					setOriginalValue(account, prop, currentRoles)
				}

				// Here's the special case so that we can show original, user input and results in the review
				if (!setDefaulted && !isNewUser) {
					setDefaultedValue(account, prop, changeMap.results)
					account[prop] = roleChanges
					//setDefaultedValue(account, prop, roleChanges)
					//account[prop] = changeMap.results
				}

				// Save the changeMap to be used later on by the update logic
				account.securityChanges = changeMap
			}
		}

		if (! shouldUpdateUserLogin(sheetInfoOpts)) {
			if (changeMap.hasChanges) {
				account.errors << 'Unplanned change on security roles'
			}
		}

		return ok
	}

	/**
	 * Used to validate that the team codes are correct and will set the ORIGINAL or DEFAULT appropriately
	 * @param account - the Account map to review
	 * @return true if validated with no errors otherwise false
	 */
	private boolean validateTeams(Map account, Project project, List<String> allTeamCodes) {
		boolean ok = true

		List currPersonTeams = [], currProjectTeams=[]
		if (account.person.id) {
			currPersonTeams = account.person.getTeamsCanParticipateIn().id

			// currProjectTeams= account.person.getTeamsAssignedTo(project)
			currProjectTeams = partyRelationshipService.getProjectStaffFunctionCodes(project, account.person)

		}

		// List chgPersonTeams = StringUtil.splitter(account.personTeams, ',', DELIM_OPTIONS)
		// List chgProjectTeams = StringUtil.splitter(account.projectTeams, ',', DELIM_OPTIONS)
		List chgPersonTeams = account.personTeams
		List chgProjectTeams = account.projectTeams

		Map changeMap = determineTeamChanges(allTeamCodes, currPersonTeams, chgPersonTeams, currProjectTeams, chgProjectTeams)
		/*
		log.debug """
			validateTeams($account.firstName $account.lastName)
				currPersonTeams=$currPersonTeams
				currProjectTeams=$currProjectTeams
				chgPersonTeams=$chgPersonTeams
				chgProjectTeams=$chgProjectTeams
		"""
		log.debug "validateTeams() \n\tchangeMap=$changeMap"
		*/

		ok = ! changeMap.error
		if (ok) {
			// Save the changeMap to the account for use later during the update process
			account.teamChangeMap = changeMap

			// Update the account.teams with the various information to show the changes, etc
			if (changeMap.personHasChanges) {
				// setOriginalValue(account, 'personTeams', currPersonTeams.join(', '))
				// setDefaultedValue(account, 'personTeams', changeMap.resultPerson.join(', '))
				// account.personTeams = chgPersonTeams.join(', ')
				setOriginalValue(account, 'personTeams', currPersonTeams)
				setDefaultedValue(account, 'personTeams', changeMap.resultPerson)
				account.personTeams = chgPersonTeams
			}
			if (changeMap.projectHasChanges) {
				// setOriginalValue(account, 'projectTeams', currProjectTeams.join(', '))
				// setDefaultedValue(account, 'projectTeams', changeMap.resultProject.join(', '))
				// account.projectTeams = chgProjectTeams.join(', ')
				setOriginalValue(account, 'projectTeams', currProjectTeams)
				setDefaultedValue(account, 'projectTeams', changeMap.resultProject)
				account.projectTeams = chgProjectTeams
			}
		} else {
			account.errors << changeMap.error
		}

		return ok
	}

	/**
	 * Used to compare a list against another list wher the list to check may have a minus (-) prefix
	 * @param validCodes the list of the valid codes to compare against
	 * @param codesToCheck - the list of codes to validate
	 * @return a list of the invalid codes or empty if all match
	 */
	private List checkMinusListForInvalidCodes(List<String> validCodes, List<String> codesToCheck) {
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
	 * @return A Map object that will contain the following elements:
	 *        add: a list of roles to be added
	 *     delete: a list of roles to be deleted
	 *     result: a list of the resulting roles after the changes are applied
	 *     errors: an error message String if invalid codes are referenced or tried to assign
	 * hasChanges: a boolean indicating if there were changes or not
	 */
	private Map determineSecurityRoleChanges(List<String> allRoles, List<String> currentRoles, List<String> changes) {

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

		def person = securityService.userLogin.person

		List authorizedRoleCodes = securityService.getAssignableRoleCodes(person)

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
			remainingCodes = currentRoles.clone() as List
			List violationCodes = []
			for (String chgCode in changes) {
				// Determine if there is a security violation. Note that a user may have security roles
				// previously assigned that are higher than what the individual who is making the changes can
				// assign so as long as the code was in the original list we're fine. The individual can not
				// add or remove codes above their clearence.
				boolean toDelete = isMinus(chgCode)
				String code = stripTheMinus(chgCode)

				if (!authorizedRoleCodes.contains(code)) {
					boolean alreadyAssignedToUser = currentRoles.contains(code)
					// See if they are trying to add or delete an unauthorize code
					if ((!alreadyAssignedToUser && !toDelete) || (alreadyAssignedToUser && toDelete)) {
						violationCodes << code
						continue
					}
				}

				// Check for violations
				if (violationCodes) {
					panicButton "Unauthorized security role ${violationCodes.size() > 1 ? 's' : ''} (${violationCodes.join(', ')})"
					continue
				}

				boolean alreadyInList = remainingCodes.contains(code)
				if (toDelete && alreadyInList) {
					remainingCodes = remainingCodes - code
					map.delete << code
					map.hasChanges = true
				}
				else if (!toDelete && !alreadyInList) {
					remainingCodes << code
					map.add << code
					map.hasChanges = true
				}
			}

			//println "\n   remainingCodes=$remainingCodes\n   $allRoles=$allRoles\n   currentRoles=$currentRoles\n   changes=$changes" +
			// "\n   authorizedRoleCodes=$authorizedRoleCodes\n   map=$map" +
			// "\n   remainingCodes=${remainingCodes ? true : false} ${remainingCodes.size()}"

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

		while (true) {
			List list

			// Check the roles currently assigned to the person (coming from the system)
			[currPersonTeams, currProjectTeams].each { teams ->
				list = teams.clone() as List
				list.removeAll(allTeams)
				if (list) {
					// This shouldn't happen but just in case
					panicButton "System issue with invalid team role${list.size() > 1 ? 's' : ''} $list"
					log.error "determineTeamChanges() accounted person with invalid team assignment(s) $allTeams"
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
			// noting that any additions may affect the person list. Beforehand we're going to check to see if they are
			// trying to remove the person from the project (-STAFF), if so everything goes.
			if ( chgProjectTeams.find { isMinus(it) && stripTheMinus(it) == 'STAFF'} ) {
				map.resultProject = []
				map.addToProject = []
				map.deleteFromProject = ['STAFF']
				map.projectHasChanges = true
			} else {
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
							map.projectHasChanges = true
						}
						// Check the Person personal teams and add if the person doesn't already have the assignment
						// but keep in mind that the person already has the STAFF assignment.
						if (team != 'STAFF' && ! resultPersonTeams.contains(team)) {
							resultPersonTeams << team
							map.addToPerson << team
							map.personHasChanges = true
						}
					}
				}
				map.resultProject = resultProjectTeams.sort()
				map.addToProject = map.addToProject.sort()
				map.deleteFromProject = map.deleteFromProject.sort()
			}

			map.resultPerson = resultPersonTeams.sort()
			map.addToPerson = map.addToPerson.sort()
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
					account.matches << 'personId'
					account.flags.matchedPersonById=true
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
					account.matches << 'email'
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
					account.matches << 'username'
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
	 * Used to transform the list of accounts into the form that can be displayed in the spreadsheet
	 * copying only the properties that are used by the spreadsheet.
	 * @param accounts - the list of accounts that were loaded from the spreadsheet
	 * @param sheetInfoOpts - the map of various preferences
	 * @return the list of accounts formatted for presentation
	 */
	private List transformAccounts(List accounts, Map sheetInfoOpts) {
		List list = []

		accounts.each { row ->
			Map account = [:]
			accountSpreadsheetColumnMap.each { prop, info ->
				if (info.formPos != null) {
					// Get the transformer if one exists
					def transformer = (info.containsKey('transform') ? info.transform : false)

					def value = row[prop]

					// Remove duplicate error messages that can happen because the Person is validated and then the UserLogin that
					// has a reference to the person. If the person has a constraints error then it gets logged twice.
					if (prop == 'errors' && value.size() > 0) {
						value.unique()
					}

					// Add the value to the account map
					if (transformer) {
						account[prop] = transformer(value, sheetInfoOpts)
						// log.debug "transformAccounts() $prop from '${row[prop]}'' to '${account[prop]}'"
					} else {
						account[prop] = row[prop]
					}

					// Check for the Original, Defaulted values and add accordingly
					SUFFIX_LIST.each { suffix ->
						String sfxProp = prop + suffix
						if (row.containsKey(sfxProp)) {
							if (transformer) {
								account[sfxProp] = transformer(row[sfxProp], sheetInfoOpts)
							} else {
								account[sfxProp] = row[sfxProp]
							}
						}
					}
				}

				// Add some additional properties we find interesting
				['flags', 'icon', 'teamChangeMap', 'securityChanges'].each { p ->
					if (row.containsKey(p)) {
						account[p] = row[p]
					}
				}
			}

			list << account
		}
		// return accounts
		return list
	}

	// --------------------------------
	// Posting Account to DB Methods
	// --------------------------------

	/**
	 * Utility method to mapping the properties that were changed and the original values
	 * @param account - the map to record the changes into which will be the propertyName:originalValue
	 * @param domainObj - the object that the changes are read from
	 */
	private void recordChangeHistory(Map changeHistory, Object domainObj) {
		String className = domainObj.getClass().name
		for (prop in domainObj.dirtyPropertyNames) {
			changeHistory[className + '.' + prop] = domainObj.getPersistentValue(prop)
		}
	}

	/**
	 * Used to add or update the person
	 * @param account - the account information
	 * @param options - the map of the options
	 * @return a list containing:
	 *    String - an error message if the save or update failed
	 *    boolean - a flag indicating if the Person object was changed (true) or unchanged (false)
	 */
	@Transactional
	private List applyPersonChanges(Person person, Map account, Map sheetInfoOpts, Map formOptions) {
		String error
		boolean changed=false
		if (formOptions.flagToUpdatePerson) {
			boolean isNew = ! person.id

			log.debug "applyPersonChanges() About to apply changes to $person"

			// Update the person with the values passed in
			applyChangesToDomainObject(person, account, sheetInfoOpts, true, true)

			changed = (isNew || person.dirtyPropertyNames.size()>0)

			// TODO : JPM 4/2016 : add auditService log message about the account
			if (person.id) {
				log.debug "applyPersonChanges() is update person $person ($person.id) properties: $person.dirtyPropertyNames"
			} else {
				log.debug "applyPersonChanges() is creating person $person"
			}

			if (changed) {
				recordChangeHistory(account.changeHistory, person)
				if (! person.save(failOnError: false)) {
					account.changeHistory = null
					log.error "applyPersonChanges() save person $person failed : ${GormUtil.allErrorsString(person)}"
					error = gormValidationErrors(person)
					person.discard()
					person = null
				} else {
					if (account.flags.isNewAccount) {
						account.personId = person.id

						partyRelationshipService.addCompanyStaff(account.companyObj, person)
						// auditService.logMessage("$securityService.currentUsername created new person $person for company $account.company")
					}
				}
			}
		}
		return [error, changed]
	}

	/**
	 * Used to add or update a person's UserLogin account and set the security role(s) accordingly based on their
	 * security level.
	 * @param account - the account map with all of the information about the person/user
	 * @param options - the options that the user selected for the update
	 * @return a list containing:
	 *    String - an error message if the save or update failed
	 *    boolean - a flag indicating if the Person object was changed (true) or unchanged (false)
	 */
	@Transactional
	private List applyUserLoginChanges(UserLogin userLogin, Map account, Map sheetInfoOpts, Map formOptions) {
		boolean changed=false
		String error

		boolean isNew = ! userLogin.id

		//debugLogAccountInfo('In applyUserLoginChanges, about to call apply to Domain', account, 'UserLogin')
		// Update the person with the values passed in
		applyChangesToDomainObject(userLogin, account, sheetInfoOpts, formOptions.flagToUpdateUserLogin, true)
		if (isNew) {
			userLogin.person = account.person
			userLogin.password = RSU.randomAlphanumeric(20)
		}

		// Try saving the user but if it errors we'll discard it and just report an error
		recordChangeHistory(account.changeHistory, userLogin)
		if (! userLogin.save(failOnError: false)) {
			error = gormValidationErrors(userLogin)
			userLogin.discard()
			userLogin = null
			account.changeHistory = null
		} else {
			if (isNew) {
				changed = true
			} else {
				log.debug "applyUserLoginChanges() is changing $userLogin.dirtyPropertyNames"
				if (userLogin.dirtyPropertyNames.size()) {
					changed = true
				}
			}
		}
		return [error, changed]
	}

	/**
	 * Used to update the person's SecurityRoles associated to them
	 * by examining the SecurityRoles of the person and the roles lists passed in the account map.
	 * Removing those not pressent in the new array and adding the new ones
	 *************************
	 * TODO: If I remove all roles from the Spreadsheet the account.roles gets filled with some roles, I don't know if I should commit those or just fail.
	 * also should I double check the security and if those roles belongs to me? (I think this has already been done on the review step.) my best guess is
	 * I should, but I'll check before anything else.
	 *************************
	 * @author @tavo_luna
	 *
	 * @param account - the account map with all of the information about the person/use
	 * @return an array with [errorMessage, flagThatThereWereChanges]
	 */
	private List applySecurityRoleChanges(Map account){
		String error
		boolean changed = false

		// Check to see if there were any security changes
		if(account?.securityChanges) {
			UserLogin userLogin
			if(account.person?.id){
				userLogin = account.person?.userLogin
			}
			//TODO: @tavo_luna: What if it's a new User? I can't assume that is has been created before, I need a builder to ask for the user or create it to be reused elsewhere
			if (userLogin){
				def person = userLogin.person

				def rolesToRemove = account.securityChanges.delete
				def rolesToAdd    = account.securityChanges.add

				if(rolesToRemove) securityService.unassignRoleCodes(person, rolesToRemove)
				if(rolesToAdd)    securityService.assignRoleCodes(person, rolesToAdd)

				changed = true
			}
		}

		return [error, changed]
	}

	/**
	 * Used to update the person's teams associated to themselves/company and to the project
	 * by examining the personTeams and projectTeams lists passed in the account map. If the codes have
	 * a minus(-) suffix then the team will be removed otherwise it is added if it doesn't already exist.
	 * @param account - the account map with all of the information about the person/user
	 * @param project - the project to associate the person's teams to
	 * @param options - the options that the user selected for the update
	 * @return an array with [errorMessage, flagThatThereWereChanges]
	 */
	private List applyTeamsChanges(Map account, Project project, Map sheetInfoOpts, Map options) {
		List teamsCanParticapateIn = []
		List projectTeams = []
		String error
		boolean changed = false

		// Check to see if there were any teams specified for the user
		if (! account.personTeams && ! account.projectTeams) {
			log.debug "applyTeamsChanges() bailed as there were no changes - account.personTeams=${account.personTeams?.getClass().name}, account.projectTeams=${account.projectTeams?.getClass().name}"
		} else {

			assert (account.person instanceof Person)
			Person person = account.person
			String personName = account.person.toString()

			Map chgMap = account.teamChangeMap
			if (chgMap?.hasChanges) {
				if(chgMap.personHasChanges) {
					def personTeams = chgMap.resultPerson //@tavo_luna ugly hack to test removing STAFF:    - ["STAFF"]
					//log.debug "partyRelationshipService.updateAssignedTeams($person, $personTeams)"
					partyRelationshipService.updateAssignedTeams(person, personTeams)
					account.changeHistory['Person.personTeams'] = getOriginalValue(account, 'personTeams')
				}

				if(chgMap.projectHasChanges) {
					List deleteTeamsFromProject = chgMap.deleteFromProject
					if (deleteTeamsFromProject.contains('STAFF') ) {
						// If we're removing STAFF there ain't nothing else going to be happening here...
						personService.removeFromProject(project.id.toString(), person.id.toString())
					} else {
						if (deleteTeamsFromProject) {
							log.debug "projectService.removeTeamMember($project, $person, $deleteTeamsFromProject)"
							projectService.removeTeamMember(project, person, deleteTeamsFromProject)
						}

						def addTeamsToProject = chgMap.addToProject
						if (addTeamsToProject) {
							log.debug "projectService.addTeamMember($project, $person, $addTeamsToProject)"
							projectService.addTeamMember(project, person, addTeamsToProject)
							account.changeHistory["Person.projectTeams"] = getOriginalValue(account, 'projectTeams')
						}
					}
				}

				changed = true
			}
		}

		return [error, changed]
	}

	/**
	 * This method serves two purposes:
	 *    1. Applies values from the account map into the domain object passed in when shouldUpdateDomain is true
	 *    2. Updates the account map to track changed, Defaulted and Original values that is used in the review process
	 *
	 * Note: When applying changes to existing domains there needs to be some caution to prevent overwritting existing information
	 * in the case that the import of a person/userLogin occurred as a result of someone just entering values in the spreadsheet and
	 * a match of the person happens to occur. In this scenario we can not be certain that the person  entered all of the information so
	 * we will ONLY overwrite values with blanks if we the load was matched on the person's id. This match indicates that the spreadsheet
	 * originated from an export so all of the data should be there.
	 * @param domainObject - the Person or UserLogin domain object to populate
	 * @param account - the Map containing all of the changes
	 * @param sheetInfoOpts - a Map containing TZ, Date formatters, etc used for transforming values
	 * @param shouldUpdatePerson - a flag if true will record the original values to show changes being made
	 */
	private void applyChangesToDomainObject(Object domainObject, Map account, Map sheetInfoOpts, boolean shouldUpdateDomain, boolean setDefaults=false) {
		// log.debug "\n\n*******\napplyChangesToDomainObject() called for ${domainObject.getClass().name} $domainObject ($domainObject.id)"

		boolean isNew = ! domainObject.id
		String className = domainObject.getClass().simpleName
		String domainCode = className[0]
		assert ['P','U'].contains(domainCode)
		boolean identifyUnplannedChanges=shouldIdentifyUnplannedChanges(sheetInfoOpts, className)
		boolean blockBlankOverwrites = account.flags.blockBlankOverwrites

		// log.debug "applyChangesToDomainObject() identifyUnplannedChanges=${identifyUnplannedChanges ? 'TRUE' : 'false'} for domain $className"

		// Used to indicate that user entered data into the spreadsheet when the domain update
		// option was declined/not selected.
		boolean unplannedChange = false
		String unChgdLabel='Unplanned change'

		accountSpreadsheetColumnMap.each { prop, info ->
			if (info.domain != domainCode) {
				// log.debug "applyChangesToDomainObject() Skipped - wrong domain info.domain=$info.domain,domainCode=$domainCode, prop=$prop"
				return // Skip property
			}

			//log.debug "\r\n\r\n****** applyChangesToDomainObject() Processing property $prop"

			if (propertyHasError(account, prop)) {
				//log.debug "applyChangesToDomainObject() SKIPPED property $prop due to it having an error"
				if (accountSpreadsheetColumnMap.containsKey('defaultOnError')) {
					// Set a default value so that the domain validation will not fail
					domainObject[prop] = accountSpreadsheetColumnMap.defaultOnError()
				}
				return
			}

			// Note that origValueTransformed for null values will return as a blank String such that the comparison to the spreadsheet values will match
			def origValue = domainObject[prop]
			if (info.type == 'date' && origValue != null) {
				// We have a special case with date in that the value coming from the database could have a time element so we need to
				// clear the time so that we don't have in appropriate indication that the value was changed.
				origValue.clearTime()
			}
			String origValueTransformed = transformProperty(prop, origValue, sheetInfoOpts)
			String chgValueTransformed = transformProperty(prop, account[prop], sheetInfoOpts)

			// Check for null or blank)
			boolean valuesEqual = origValueTransformed == chgValueTransformed
			boolean origValueIsBlank = StringUtil.isBlank(origValueTransformed)
			boolean chgValueIsBlank = StringUtil.isBlank(chgValueTransformed)
			/*
			log.debug "applyChangesToDomainObject(${account.firstName + ' ' + account.lastName}) prop $prop value"
			log.debug "existing='${domainObject[prop]==null ? 'null' : domainObject[prop]}' formatted to '${origValueTransformed==null?'null':origValueTransformed}'"
			log.debug "changed is '${account[prop]==null?'null':account[prop]}' value='${chgValueTransformed ==null?'null': chgValueTransformed}'"
			log.debug "blockBlankOverwrites=$blockBlankOverwrites, shouldUpdateDomain=$shouldUpdateDomain, isNew=$isNew"
			log.debug "origValueIsBlank=$origValueIsBlank, chgValueIsBlank=$chgValueIsBlank, valuesEqual=$valuesEqual"
			log.debug "account.errors=${account.errors.size()>0}"
			*/
			if (isNew) {
				//
				// Working with a NEW Domain object
				//
				if (chgValueIsBlank) {
					if (shouldUpdateDomain) {
						// Try to first default it from the model has a default value
						def defPropertyValue = evaluateDefaultValue(prop, account, sheetInfoOpts)
						if (defPropertyValue != null) {
							setDefaultedValue(account, prop, defPropertyValue)
						} else if (! origValueIsBlank ) {
							// Then we will fall back to the default values on the domain class itself
							setDefaultedValue(account, prop, domainObject[prop])
						}
					}
				} else {
					// if (shouldUpdateDomain) {
						if (! valuesEqual) {
							setOriginalValue(account, prop, domainObject[prop])
						}
					//} else {
						if (identifyUnplannedChanges) {
							unplannedChange = true
							setErrorValue(account, prop, unChgdLabel)
						}
					//}
				}
			} else {
				//
				// Working with an EXISTING Domain object
				//
				if (chgValueIsBlank) {
					// User didn't give us any changes so do we want to overwrite existing values?
					// Only if there was a perfect match from an export - which is flagged with the
					// setting account.flags.blockBlankOverwrites.
					if (! origValueIsBlank) {
						if (! blockBlankOverwrites) {
							//if (shouldUpdateDomain) {
								// Guess we can overwrite the value
								setOriginalValue(account, prop, domainObject[prop])
							//} else {
								if (identifyUnplannedChanges) {
									unplannedChange = true
									setErrorValue(account, prop, unChgdLabel)
								}
						} else {
							// Show the current values by default, not that they are being changed...
							setDefaultedValue(account, prop, domainObject[prop])
						}
					}
				} else {
					// The user gave us some changes so what to do
					// if (true || shouldUpdateDomain) {
						if (! valuesEqual) {
							setOriginalValue(account, prop, domainObject[prop])
							if (identifyUnplannedChanges) {
								unplannedChange = true
								setErrorValue(account, prop, unChgdLabel)
							}
						}
					//}
				}
			}

			if (! propertyHasError(account, prop)) {
				// Let's see what the current value is now and if different from the original then we will
				// update the actual domain property, finally...
				chgValueTransformed = transformProperty(prop, account[prop], sheetInfoOpts)
				chgValueIsBlank = StringUtil.isBlank(chgValueTransformed)
				if (! chgValueIsBlank && chgValueTransformed != origValueTransformed) {
					domainObject[prop] = account[prop]
				}
			}
		} // accountSpreadsheetColumnMap.each

		if (unplannedChange) {
			account.errors << "Unplanned change(s) for $className"
		}
	}
}
