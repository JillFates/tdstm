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

//import org.apache.commons.validator.routines.EmailValidator

class AccountImportExportService {

	static transactional = false

	def auditService
	def coreService
	def partyRelationshipService
	def personService
	def projectService
	def securityService	

	static final LOGIN_OPT_ALL = 'A'
	static final LOGIN_OPT_ACTIVE = 'Y'
	static final LOGIN_OPT_INACTIVE = 'N'

	static final ACCOUNT_EXPORT_TEMPLATE = '/templates/AccountsImportExport.xls'
	static final EXPORT_FILENAME_PREFIX = 'AccountExport'
	static final TEMPLATE_TAB_NAME = 'Accounts'

	// Used to indicate the alternate property name with the original and defaulted values
	// that are stored in the account map (e.g. firstName, firstName_o and firstName_d)
	static final ORIGINAL_SUFFIX = '_o'
	static final DEFAULTED_SUFFIX = '_d'

	static final IMPORT_OPTION_BOTH='B'
	static final IMPORT_OPTION_PERSON='P'
	static final IMPORT_OPTION_USERLOGIN='U'

	// Users can split Teams and Security roles on the follow characters as well as the default comma (,)
	static final List DELIM_OPTIONS = [';',':','|'] 

	/*
	 * The following map is used to drive the Import and Export tables and forms. The properties consist of:
	 *    ssPos: 	the position that the property appears in the spreadsheet
	 *    formPos: 	the position that the property appears in the online form
	 * 	  type: 	P)erson, U)serLogin, T)ransitent
	 *    width: 	the width in the online grid
	 *    locked: 	a flag to indicate that the column is locked (for horizontal scrolling)
	 *    label: 	the column heading label in the grid and export spreadsheet
	 */
	static final Map accountSpreadsheetColumnMap = [
		personId      : [type:'number', ssPos:1,  formPos:1,  domain:'I', width:50,  locked:true,  label:'ID'],
		firstName     : [type:'string', ssPos:2,  formPos:2,  domain:'P', width:90,  locked:true,  label:'First Name', template:"\"#= showChanges(data, 'firstName') #\""],
		middleName    : [type:'string', ssPos:3,  formPos:3,  domain:'P', width:90,  locked:true,  label:'Middle Name', template:"\"#= showChanges(data, 'middleName') #\""],
		lastName      : [type:'string', ssPos:4,  formPos:4,  domain:'P', width:90,  locked:true,  label:'Last Name', template:"\"#= showChanges(data, 'lastName') #\""],
		company       : [type:'string', ssPos:5,  formPos:5,  domain:'T', width:90,  locked:true,  label:'Company', template:"\"#= showChanges(data, 'company') #\""],
		errors        : [type:'list',   ssPos:0,  formPos:6,  domain:'T', width:240, locked:false, label:'Errors', template: "kendo.template(\$('#error-template').html())" ],
		workPhone     : [type:'string', ssPos:6,  formPos:7,  domain:'P', width:100, locked:false, label:'Work Phone', template:"\"#= showChanges(data, 'workPhone') #\""],
		mobilePhone   : [type:'string', ssPos:7,  formPos:8,  domain:'P', width:100, locked:false, label:'Mobile Phone', template:"\"#= showChanges(data, 'mobilePhone') #\""],
		email         : [type:'string', ssPos:8,  formPos:9,  domain:'P', width:100, locked:false, label:'Email', template:"\"#= showChanges(data, 'email') #\""],
		title         : [type:'string', ssPos:9,  formPos:10, domain:'P', width:100, locked:false, label:'Title', template:"\"#= showChanges(data, 'title') #\""],
		department    : [type:'string', ssPos:10, formPos:11, domain:'P', width:100, locked:false, label:'Department', template:"\"#= showChanges(data, 'department') #\""],
		location      : [type:'string', ssPos:11, formPos:12, domain:'P', width:100, locked:false, label:'Location/City', template:"\"#= showChanges(data, 'location') #\""],
		stateProv     : [type:'string', ssPos:12, formPos:13, domain:'P', width:100, locked:false, label:'State/Prov', template:"\"#= showChanges(data, 'stateProv') #\""],
		country       : [type:'string', ssPos:13, formPos:14, domain:'P', width:100, locked:false, label:'Country', template:"\"#= showChanges(data, 'country') #\""],
		personTeams   : [type:'list',   ssPos:14, formPos:15, domain:'T', width:150, locked:false, label:'Person Team(s)'],
		projectTeams  : [type:'list',   ssPos:15, formPos:15, domain:'T', width:150, locked:false, label:'Project Team(s)'],
		roles         : [type:'list',   ssPos:16, formPos:17, domain:'T', width:100, locked:false, label:'Security Role(s)', template:"\"#= showChanges(data, 'roles') #\""],
		username      : [type:'string', ssPos:17, formPos:18, domain:'U', width:120, locked:false, label:'Username'],
		accountLocal  : [type:'string', ssPos:18, formPos:19, domain:'U', width:100, locked:false, label:'Local Account?'],
		loginActive   : [type:'string', ssPos:19, formPos:20, domain:'U', width:100, locked:false, label:'Login Active?'],
		accountExp    : [type:'string', ssPos:20, formPos:21, domain:'U', width:100, locked:false, label:'Account Expiration'],
		passwordExp   : [type:'string', ssPos:21, formPos:22, domain:'U', width:100, locked:false, label:'Password Expiration'],
		passwordFixed : [type:'string', ssPos:22, formPos:23, domain:'U', width:100, locked:false, label:'Pswd Never Expires?'],
		match         : [type:'list',   ssPos:0,  formPos:24, domain:'T', width:100, locked:false, label:'Matched On']
	]

	/*********************************************************************************************************
	 ** Controller methods
	 *********************************************************************************************************/

	/**
	 * Used to load the spreadsheet into memory and validate that the information is correct
	 * @param byWhom   - the user that is making the request
	 * @param project  - the project that the import is being applied against
	 * @param filename - the name of the temporarilly saved spreadsheet
	 * @param options  - the options that the user chose when submitting the form
	 * @controllerMethod
	 */
	List loadAndValidateSpreadsheet(UserLogin byWhom, Project project, String filename, Map options) {
		// Load the spreadsheet
		HSSFWorkbook spreadsheet = readImportSpreadsheet(filename)

		// Read in the accounts and then validate them
		List accounts = readAccountsFromSpreadsheet(spreadsheet)

		// Validate the sheet
		validateUploadedAccounts(byWhom, accounts, project, options)

		return accounts
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
	Map importAccount_Step1_Upload(UserLogin byWhom, Project project, Object request, String fileParamName) {
		Map model = [:]

		// Handle the file upload
		def file = request.getFile(fileParamName)
		if (file.empty) {
			throw new EmptyResultException('The file you uploaded appears to be empty')
		}

		// Save the spreadsheet file and then read it into a HSSFWorkbook
		model.filename = saveImportSpreadsheet(request, byWhom, fileParamName)
		HSSFWorkbook spreadsheet = readImportSpreadsheet(model.filename)

		// Read in the accounts and then validate them
		List accounts = readAccountsFromSpreadsheet(spreadsheet)

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
	Map importAccount_Step2_Review(UserLogin byWhom, Project project, Object request, Object params) {
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
	Map importAccount_Step3_PostChanges(UserLogin user, Project project, Object options) {
		// Read in the accounts and then validate them
		List accounts = loadAndValidateSpreadsheet(user, project, options.filename, options)
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
				def (error, changed) = addOrUpdatePerson(user, accounts[i], options)

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
	 * Used to setup some new options used for the account import posting process
	 * @return the options with a few new values
	 */
	String generateRandonPassword() {
		return UUID.randomUUID().toString()
	}

	/**
	 * Used to compute a date some number of days into the future
	 * @param daysOffset - the number of days into the future (+) or past (-)
	 * @param current - the date to start with (default to now)
	 * @return the date based on the days offset
	 */
	Date daysOffset(int daysOffset, Date current=new Date()) {
		Date offsetDate
		// Compute the date into the future based on the user input
		use (TimeCategory) {
			offsetDate = current + daysOffset
		}

		return offsetDate
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
	 * @param staffOption - the option to export STAFF (all of the client staff) or PROJ_STAFF (all individuals assigned to project)
	 * @param includeUserLogins - a boolean flag if user information should be exported 
	 * @param userLoginOption - an option to indicate A:All users, Y:Active users, or N: Inactive users
	 * @permission PersonExport 
	 */
	HSSFWorkbook generateAccountExportSpreadsheet(
		Object session,
		UserLogin byWhom, 
		Project project,
		String staffOption, 
		boolean includeUserLogins=false, 
		String userLoginOption=null
	) {
		// Project project = controllerService.getProjectForPage(this, 'PersonExport')
		if (!project) {
			return
		}

		List persons = []

		// Get the staff for the project
		def company = project.client.id
		if (staffOption == "STAFF"){
			persons = partyRelationshipService.getAllCompaniesStaffPersons(Party.findById(company))
		} else if(staffOption == "PROJ_STAFF") {
			persons = projectService.getStaff(project)
		}

		if (! persons) {
			throw new EmptyResultException('No accounts were found for given filter')
		}

		def book = getAccountExportTemplate()
		def sheet = book.getSheet(TEMPLATE_TAB_NAME)

		populateAccountSpreadsheet(session, project, persons, sheet, company, includeUserLogins, userLoginOption)

		return book
	}

	/**
	 * This method will iterate over the list of persons and populate the spreadsheet appropriately
	 * @param includeUserLogins - a boolean flag if user information should be exported 
	 * @param userLoginOption - an option to indicate A:All users, Y:Active users, or N: Inactive users
	 */
	private void populateAccountSpreadsheet(session, Project project, List persons, sheet, companyId, includeUserLogins, userLoginOption) {
		Date now = new Date()
		persons.eachWithIndex{ person, index ->
			Map map = personToFieldMap(person, project)

			if (includeUserLogins) {
				UserLogin user = UserLogin.findByPerson(person)
				if (user) {
					boolean isLoginInfoOkay = (userLoginOption == LOGIN_OPT_ALL)
					if (! isLoginInfoOkay) {
						// Check if the user matches the filter options
						if (userLoginOption == LOGIN_OPT_ACTIVE) {	
							// TODO : JPM 3/2016 : The UserLogin.userActive function should probably include the additional checks that are here
							if (user.userActive() && (user.passwordNeverExpires || !(user.isLocal && user.passwordExpirationDate < now))){
							// if (p.active == "Y" && user.active == "Y" && user.expiryDate > now && (loginInfo.passwordNeverExpires || !(loginInfo.isLocal && loginInfo.passwordExpirationDate < now))){
								isLoginInfoOkay = true
							}
						} else if (userLoginOption == LOGIN_OPT_INACTIVE) {
							if (!user.userActive() || (!user.passwordNeverExpires && (user.isLocal && user.passwordExpirationDate < now))){
								// if (p.active == "N" || loginInfo.active == "N" || loginInfo.expiryDate < now  || (!loginInfo.passwordNeverExpires && (loginInfo.isLocal && loginInfo.passwordExpirationDate < now))){
								isLoginInfoOkay = true
							}
						}
					}

					if (isLoginInfoOkay) {
						// Add the User properties to the map
						map << userLoginToFieldMap(user, session)
					}
				}
			}

			addRowToAccountSpreadsheet(sheet, map, (index+1))			
		}
	}	

	/**
	 * This method outputs all the account mapped fields to a row in the sheet
	 * @param sheet - the spreadsheet to update
	 * @param account - the map of the account properties
	 * @param rowNumber - the row in the spreadsheet to insert the values 
	 */
	private void addRowToAccountSpreadsheet(sheet, Map account, int rowNumber) {
		List properties = getPropertiesInColumnOrder('ssPos')
		// log.debug "addRowToAccountSpreadsheet() properties=$properties, sheet isa ${sheet.getClass().getName()}"
		for (int i=0; i < properties.size(); i++) {
			if (account.containsKey(properties[i])) {
				WorkbookUtil.addCell(sheet, i, rowNumber, account[properties[i]])
			}
		}
	}

	/**
	 * This method is used to update the header labels on the spreadsheet to match the mapping table
	 * @param sheet - the spreadsheet to update
	 */
	private void updateSpreadsheetHeader(sheet) {
		List labels = getLabelsInColumnOrder('ssPos')
		def tab = sheet.getSheet(TEMPLATE_TAB_NAME)

		for(int i=0; i < labels.size(); i++) { 
			WorkbookUtil.addCell(tab, i, 0, labels[i])
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
		List roles = RoleType.findAllByType(RoleType.SECURITY, [order:'level'])
		int row = 1
		roles.each {r ->
			if (r.id == 'TEST_ROLE') return
			WorkbookUtil.addCell(tab, 0, row, r.id)
			WorkbookUtil.addCell(tab, 1, row++, r.toString())
		}
	}

	/**
	 * Used to map a Person to the accountSpreadsheetColumnMap 
	 * @param person - the person to map to the AccountFieldMap format
	 * @return a map of the person information
	 */
	private Map personToFieldMap(Person person, Project project) {

		List personTeams = person.getSuitableTeams().id
		List projectTeams = partyRelationshipService.getProjectStaffFunctions(project, person).id
		List roles = securityService.getAssignedRoles(person).id

		Map map = [
			personId     : person.id,
			firstName    : person.firstName ?: '', 
			middleName   : person.middleName ?: '', 
			lastName     : person.lastName ?: '', 
			company      : person.company.name ?: '', 
			workPhone    : person.workPhone ?: '',
			mobilePhone  : person.mobilePhone ?: '', 
			email        : person.email ?: '', 
			title        : person.title ?: '',
			department   : person.department ?: '', 
			location     : person.location ?: '', 
			stateProv    : person.stateProv ?: '', 
			country      : person.country ?: '', 
			personTeams  : personTeams.join(", "), 
			projectTeams : projectTeams.join(", "), 
			roles        : roles.join(", ")
		]  

		return map
	}

	/**
	 * Used to map a Person to the accountSpreadsheetColumnMap format
	 * @param user - the UserLogin to map to the AccountFieldMap format
	 * @param session - the request session which is used to access the timezone information
	 * @return a map of the person information
	 */
	private Map userLoginToFieldMap(UserLogin user, Object session) {
		def pswdExpDate=''
		if (user.passwordExpirationDate) {
			pswdExpDate = TimeUtil.formatDateTime(session, user.passwordExpirationDate, TimeUtil.FORMAT_DATE) 
		}
		Map map = [
			username      : user.username,
			accountLocal  : (user.isLocal ? 'Y' : 'N'),
			loginActive   : (user.active ? 'Y' : 'N'),
			accountExp    : TimeUtil.formatDateTime(session, user.expiryDate, TimeUtil.FORMAT_DATE),
			passwordExp   : pswdExpDate,
			passwordFixed : (user.passwordNeverExpires ? 'Y' : 'N')
		]
		return map
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
		CommonsMultipartFile xlsFile = ( CommonsMultipartFile ) mpr.getFile(paramName)
		
		// Generate a random filename to store the spreadsheet between page loads
		String filename = "AccountImport-${byWhom.id}-" + com.tdsops.common.security.SecurityUtil.randomString(10)+'.xls'

		// Save file locally
		String fqfn=coreService.getAppTempDirectory() + '/' + filename	
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
	 * each row and add to the map that is returned for each person/userlogin.
	 * @param spreadsheet - the spreadsheet to read from
	 * @return the list that is read in
	*/
	private List<Map> readAccountsFromSpreadsheet(spreadsheet) {
		int firstAccountRow = 1
		def accountsSheet = spreadsheet.getSheet( TEMPLATE_TAB_NAME )
		int lastRow = accountsSheet.getLastRowNum()
		List accounts = []

		List properties = getPropertiesInColumnOrder('ssPos')
		for (int row = firstAccountRow; row <= lastRow; row++) {
			Map account = [:]
			int pIdx = 0
			properties.each { 
				account.put(it, WorkbookUtil.getStringCellValue(accountsSheet, pIdx++, row).trim())
			}
			account.errors = []
			account.match = []

			accounts.add(account)
		}
		return accounts
	}

	/**
	 * Used to validate the list of accounts that were uploaded and will populate the individual maps with 
	 * properties errors when anything is found. This will update the accounts data with the following information:
	 *    - set companyObj to the company
	 *    - set companyId to the id of the company (seems redundant...)
	 *    - set person properties with ORIGINAL_SUFFIX suffix if the value has been changed from the original
	 *    - set isNewAccount to indicate if the person is new or not
	 *    - appends any error messages to errors list
	 * @param accounts - the list of accounts that are read from the spreadsheet
	 * @return the accounts list updated with errors
	 */
	private void validateUploadedAccounts(UserLogin byWhom, List<Map> accounts, Project project, Map options) {

		List usernameList
		List validRoleCodes
		List teamCodes
		List assignableRoleCodes = securityService.getAssignableRoleCodes(byWhom.person)

		// def emailValidator = EmailValidator.getInstance()

		List personIdList = accounts.findAll { it.personId }.collect {it.personId }

		// Get all of the email address that were uploaded
		List emailList = accounts.findAll( { it.email })?.collect({it.email.toLowerCase()})

		PartyGroup client = project.client
		Map companiesByNames = projectService.getCompaniesMappedByName(project)
		Map companiesById = projectService.getCompaniesMappedById(project)

		if (options.flagToUpdatePerson) {
			// Retrieves all the team codes that this user is allowed to assign
			teamCodes = partyRelationshipService.getStaffingRoles().id
		}

		if (options.flagToUpdateUserLogin) {
			// Retrieves all the roles that this user is allowed to assign.
			validRoleCodes = securityService.getAssignableRoles(securityService.getUserLoginPerson()).id

			// Get all of the usernames that were uploaded
			usernameList = accounts.findAll( { it.username })?.collect({it.username.toLowerCase()})
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

			// log.debug "validateUploadedAccounts() account.person=${accounts[i].person != null}, personById=${personById!=null}, personByEmail=${personByEmail!=null}, personByUsername=${personByUsername!=null}, personByName=${personByName!=null}"
			// Set a flag on the account if it is going to be a new account
			if (accounts[i].person == null) {
				log.debug "validateUploadedAccounts() - creating blank Person"
				accounts[i].isNewAccount = true
				accounts[i].person = new Person()
			} else {
				accounts[i].isNewAccount = false
			}

			// Check for duplication person ids in the list
			String pid = accounts[i].person.id
			if (pid && personIdList.findAll{ it == pid }.size() > 1) {
				accounts[i].errors << 'Duplicate Person ID referenced'
			}

			// Check the username to make sure it isn't used by more than one row
			if (emailList.findAll{ it == accounts[i].email.toLowerCase() }.size()>1) {
				accounts[i].errors << 'Email referenced on multiple rows'
			}

			// Load a temporary Person domain object with the properties from the spreadsheet and see if any
			// of the valids will break the validation constraints
			Person.withNewSession { session -> 
				Person personToValidate = (accounts[i].person?.id ? Person.get((accounts[i].person.id)) : new Person() )
				applyChangesToPerson(personToValidate, accounts[i], options.flagToUpdatePerson)
				if (! personToValidate.validate()) {
					personToValidate.errors.allErrors.each {
						accounts[i].errors << "${it.getField()} error ${it.getCode()}"
					}
				}
				personToValidate.discard()
			}

			// Check the username to make sure it isn't used by more than one row
			if (usernameList.findAll{ it == accounts[i].username.toLowerCase() }.size()>1) {
				accounts[i].errors << 'Username referenced on multiple rows'
			}

			if (options.flagToUpdatePerson) {
				// Attempt to validate the teams that are assigned
				validateTeams(accounts[i], teamCodes) 
			}

			// Validate user and security roles if user requested to update Users
			if (options.flagToUpdateUserLogin) {
				boolean canUpdateUser = accounts[i].username
				// Check if user is trying to create a user without a person already created 
				if (canUpdateUser && accounts[i].isNewAccount && !options.flagToUpdatePerson) {
					accounts[i].errors << 'User can not be created without a saved person'
					canUpdateUser = false
				}

				if (canUpdateUser) {
					// Validate that the teams codes are correct and map out what are to add and delete appropriately
					validateSecurityRoles(byWhom, accounts[i], validRoleCodes, assignableRoleCodes)
				}	

			}

			// Attempt to match the persons to existing users
			// List staff = partyRelationshipService.getCompanyStaff( project.client.id )
			// TODO : JPM 4/2016 : Should check if the user can see people unassigned to the project  
		}

		// Update all the accounts icons
		setIconsOnAccounts(accounts)
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
						accounts.errors << 'Email found mismatched found person'
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
						account.errors << 'Username mismatched found person'
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
				account.company = company.name
				account["company$DEFAULTED_SUFFIX"] = company.name				
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
	 * Used to validate that the team codes are correct and will set the ORIGINAL or DEFAULT appropriately
	 * @param account - the Account map to review
	 * @return true if validated with no errors otherwise false
	 */
	private boolean validateTeams(Map account, List teamCodes) {
		boolean ok = true

		// Review the personTeam and the projectTeam
		['person', 'project'].each { tp ->
			// TODO : cross check the addition and removal of teams  between the two lists
			// TODO : update the ORIGINAL and DEFAULT property settings to reflect the changes, note we might need a different template for this
			// to show both the results and the uploaded (with minus potentially)
			String teamProperty = "${tp}Teams"
			// Strip off the minus (-) prefix to validate the team codes
			String teamsWithoutMinus = account[teamProperty]?.replaceAll('-','')

			List teams = StringUtil.splitter(teamsWithoutMinus, ',', DELIM_OPTIONS)
			if (teams) {
				List invalidTeams = teams - teamCodes
				if (invalidTeams) {
					ok = false
					account.errors << "Invalid $tp team(s) ${invalidTeams.join(',')}"
				}

				// Now put the appropriate team codes into the account map
				account[teamProperty] = StringUtil.splitter(account[teamProperty], ',', DELIM_OPTIONS)
			} else {
				account[teamProperty] = []
			}
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
	 * @param assignableRoleCodes - a list of the role codes that byWhom has the privilege of
	 * @param A flag that indicates success (true) or an error occurred (false)
	 */	 
	private boolean validateSecurityRoles(UserLogin byWhom, Map account, List validRoleCodes, List assignableRoleCodes ) {
		boolean ok=false

		// Validate the Setup the default security role if necessary
		UserLogin userLogin 
		if (account.person.id) {
			userLogin = account.person.userLogin
		}

		// Validate the roles
		// Review the security roles that are going to be assigned to the person
		List currentRoles = []
		List invalidRoles = []
		if (account.roles) {
			String rolesWithoutMinus = account.roles?.replaceAll('-','')
			currentRoles = StringUtil.splitter(rolesWithoutMinus, ',', DELIM_OPTIONS)
			invalidRoles = currentRoles - validRoleCodes
			if (invalidRoles) {
				account.errors << "Invalid role: ${invalidRoles.join(';')}"
			} else {
				account.roles = StringUtil.splitter(account.roles, ',', DELIM_OPTIONS)
				if (! account.isNewAccount && userLogin) {
					// Set the original values
					List origRoles = securityService.getRoles(userLogin)?.id
					List chgRoles = account.roles
					// log.debug "VALIDATING ROLES: ${account.person}\nOrig: $origRoles\n Chg: $chgRoles"
					// See if there are any differences in the uses' changing roles and the original
					if ( (chgRoles - origRoles) || (origRoles - chgRoles) ) {
						// If so then save off the original
						account["roles${ORIGINAL_SUFFIX}"] = origRoles.join(', ')
					}
				}
			}

		} else {
			// Set the default role 
			account.roles = [DEFAULT_ROLE]
			account["roles${DEFAULTED_SUFFIX}"] = DEFAULT_ROLE
		}		
	}
	/**
	 * Used to load any property a person object where the values are different
	 * @param person - the person object to be changed
	 * @param account - the Map containing all of the changes
	 * @param shouldUpdatePerson - a flag if true will record the original values to show changes being made
	 * @return 
	 */
	private Map applyChangesToPerson(Person person, Map account, boolean shouldUpdatePerson) {
		boolean existing = person.id
		accountSpreadsheetColumnMap.each {prop, info ->
			if (info.domain == 'P') {
				if (existing) {
					// Attempt to save the original value
					if (account[prop]) {
						if ( (! person[prop]) || ( person[prop] && person[prop] != account[prop] ) ) {
							// Save the original value so it can be displayed later
							if (shouldUpdatePerson) {
								account["${prop}$ORIGINAL_SUFFIX"] = (person[prop] ?: '')
							}
							person[prop] = account[prop]
						}
					} else {
						// Attempt to save the default value
						if ( person[prop] && !account[prop]) {
							account["${prop}$DEFAULTED_SUFFIX"] = person[prop]
						}
					}
				} else {
					person[prop] = account[prop]
				}
			}
		}
		return account
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
	List addOrUpdatePerson(UserLogin byWhom, Map account, Map options) {
		String error
		boolean changed=false

		Person person = account.person

		// Update the person with the values passed in
		account = applyChangesToPerson(person, account, options.flagToUpdatePerson)

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
	boolean addOrUpdateUser(UserLogin byWhom, Map account, Map options) {
		if (! securityService.hasPermission(byWhom, 'EditUserLogin', true)) {
			throw new UnauthorizedException('You are unauthorized to edit UserLogins')
		}

		UserLogin userLogin = account.person.userLogin
		if (! userLogin ) {
			if (account.username) {
				userLogin = new UserLogin()
				userLogin.username = account.username
				userLogin.person = account.person
				userLogin.active = options.activateLogin.asYN()
				userLogin.forcePasswordChange = options.forcePasswordChange.asYN()

				// TODO : JPM 4/2016 : Set timezone to that of the project when creating the user

			} else {
				// Nothing to be done here
				return false
			}
		}

		userLogin.expiryDate = options.expiryDate
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
	boolean addOrUpdateTeams(UserLogin byWhom, Map account, Project project, Map options) {
		List suitableTeams = []
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
			suitableTeams = account.person.getSuitableTeams().id
			// For existing accounts we need to get all of their accounts + any new ones that were specified
			teamsForPerson.addAll(suitableTeams)
			teamsForPerson = teamsForPerson.unique()

			// Try removing the teams again now that we have the teams from the database
			teamsForPerson = teamsForPerson - personTeamsToDelete

			// Determine if there are any new or removed teams
			changed = (suitableTeams - teamsForPerson) || (teamsForPerson - suitableTeams) 
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

}