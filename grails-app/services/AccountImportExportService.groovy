/**
 * AccountImportExportService - A set of service methods the importing and exporting of project staff and users
 */

import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.WorkbookUtil
import org.apache.commons.lang.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*

class AccountImportExportService {

	def coreService
	def partyRelationshipService
	def projectService
	def securityService	

	static final LOGIN_OPT_ALL = 'A'
	static final LOGIN_OPT_ACTIVE = 'Y'
	static final LOGIN_OPT_INACTIVE = 'N'

	static final ACCOUNT_EXPORT_TEMPLATE = '/templates/TDS-Accounts_template.xls'
	static final EXPORT_FILENAME_PREFIX = 'AccountExport'
	static final TEMPLATE_TAB_NAME = 'Accounts'

	static final Map accountSpreadsheetColumnMap = [
		personId      : [ssPos:1,  formPos:1,  type:'U', width:100, locked:true,  label:'ID'],
		firstName     : [ssPos:2,  formPos:2,  type:'P', width:100, locked:true,  label:'First Name'],
		middleName    : [ssPos:3,  formPos:3,  type:'P', width:100, locked:true,  label:'Middle Name'],
		lastName      : [ssPos:4,  formPos:4,  type:'P', width:100, locked:true,  label:'Last Name'],
		company       : [ssPos:5,  formPos:5,  type:'P', width:100, locked:true,  label:'Company'],
		errors        : [ssPos:0,  formPos:6,  type:'T', width:200, locked:false, label:'Errors'],
		workPhone     : [ssPos:6,  formPos:7,  type:'P', width:100, locked:false, label:'Work Phone'],
		mobilePhone   : [ssPos:7,  formPos:8,  type:'P', width:100, locked:false, label:'Mobile Phone'],
		email         : [ssPos:8,  formPos:9,  type:'P', width:100, locked:false, label:'Email'],
		title         : [ssPos:9,  formPos:10, type:'P', width:100, locked:false, label:'Title'],
		department    : [ssPos:10, formPos:11, type:'P', width:100, locked:false, label:'Department'],
		location      : [ssPos:11, formPos:12, type:'P', width:100, locked:false, label:'Location/City'],
		state         : [ssPos:12, formPos:13, type:'P', width:100, locked:false, label:'State/Prov'],
		country       : [ssPos:13, formPos:14, type:'P', width:100, locked:false, label:'Country'],
		teams         : [ssPos:14, formPos:15, type:'P', width:100, locked:false, label:'Team(s)'],
		roles         : [ssPos:16, formPos:16, type:'P', width:100, locked:false, label:'Security Role(s)'],
		username      : [ssPos:17, formPos:17, type:'U', width:100, locked:false, label:'Username'],
		accountLocal  : [ssPos:18, formPos:18, type:'U', width:100, locked:false, label:'Local Account?'],
		loginActive   : [ssPos:19, formPos:19, type:'U', width:100, locked:false, label:'Login Active?'],
		accountExp    : [ssPos:20, formPos:20, type:'U', width:100, locked:false, label:'Account Expiration'],
		passwordExp   : [ssPos:21, formPos:21, type:'U', width:100, locked:false, label:'Password Expiration'],
		passwordFixed : [ssPos:22, formPos:22, type:'U', width:100, locked:false, label:'Pswd Never Expires?'],
		match         : [ssPos:0,  formPos:24, type:'T', width:100, locked:false, label:'Matched']
	]


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

		// Now get the staff for the project
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
		populateAccountSpreadsheet(session, persons, sheet, company, includeUserLogins, userLoginOption)

		return book
	}

	/**
	 * This method will iterate over the list of persons and populate the spreadsheet appropriately
	 * @param includeUserLogins - a boolean flag if user information should be exported 
	 * @param userLoginOption - an option to indicate A:All users, Y:Active users, or N: Inactive users
	 */
	private void populateAccountSpreadsheet(session, persons, sheet, companyId, includeUserLogins, userLoginOption) {
		Date now = new Date()
		persons.eachWithIndex{ person, index ->
			Map map = personToFieldMap(person)

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
	private Map personToFieldMap(Person person) {
		List teams = partyRelationshipService.getCompanyStaffFunctions(person.company, person)*.id

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
			state        : person.stateProv ?: '', 
			country      : person.country ?: '', 
			teams        : teams.join(", "), 
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

	private String getTempDirectory() {
		// TODO : JPM 3/2016 : getTempDirectory has to pull the configuration property
		return "/data/tmp"
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
		String fqfn=getTempDirectory() + '/' + filename	
		File file = new File(fqfn)
		HSSFWorkbook xlsWorkbook = new HSSFWorkbook(new FileInputStream(file))
		return xlsWorkbook
	}

	/**
	 * Used to load the spreadsheet into memory and validate that the information is correct
	 * @param byWhom - the user that is making the request
	 * @param project - the project that the import is being applied against
	 * @param filename - the name of the temporarilly saved spreadsheet
	 * @controllerMethod
	 */
	List loadAndValidateSpreadsheet(UserLogin byWhom, Project project, String filename) {
		// Load the spreadsheet
		HSSFWorkbook spreadsheet = readImportSpreadsheet(filename)

		// Read in the accounts and then validate them
		List accounts = readAccountsFromSpreadsheet(spreadsheet)

		// Validate the sheet
		List teamCodes = partyRelationshipService.getStaffingRoles().description
		validateUploadedAccounts(accounts, teamCodes)

		// Attempt to match the persons to existing users
		List staff = partyRelationshipService.getCompanyStaff( project.client.id )
		searchAccountsForExisting(accounts, staff)

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
	Map importAccount_Step2_Review(UserLogin byWhom, Project project, Object request, String filename) {
		Map model = [:]
		model.filename = filename
		model.labels = getLabelsInColumnOrder('formPos')
		model.properties = getPropertiesInColumnOrder('formPos')
		model.gridMap = accountSpreadsheetColumnMap

		return model
	}

	/**
	 * Used to validate the list of accounts that were uploaded and will populate the individual maps with 
	 * properties errors when anything is found.
	 * @param accounts - the list of accounts that are read from the spreadsheet
	 * @return the accounts list updated with errors
	 */
	List<Map> validateUploadedAccounts(List<Map> people, List teamCodes) {
		// Retrieves all the roles that this user is allowed to assign.
		List validRoles = securityService.getAssignableRoles(securityService.getUserLoginPerson())
		List validRoleCodes = validRoles.id

		// TODO : JPM 3/2016 : Refactor into function into StringUtils as standard split function (unittest)
		def splitTeams = { t ->
			List teams = t.split(';')
			teams = teams*.trim()
		}

		// TODO : JPM 3/2016 : Should be able to just use LIST math to remove valid teams (e.g. invalidTeams = teams - teamCodes)
		def validateTeams = { teams -> 
			String errors = ''
			teams.each { tc -> 
				if (! teamCodes.contains(tc)) {
					errors += (errors ? ', ' : 'Invalid team code(s): ') + tc
				}
			}
			return errors
		}

		// Validate the teams && role
		for (int i=0; i < people.size(); i++) {
			people[i].errors = []
			if (people[i].teams) {
				List teams = splitTeams(people[i].teams)
				log.debug "teams=(${people[i].teams} -- $teams"
				people[i].errors << validateTeams(teams)
			}

			def currentRoles = people[i].role?.split(";")
			def invalidRoles = []

			currentRoles.each{
				if(!validRoleCodes.contains(it)){
					invalidRoles << it
				}
			}

			if (!StringUtils.isEmpty(people[i].role) && invalidRoles) {
				people[i].errors << "Invalid role: ${invalidRoles.join(';')}"
			}

		}

		return people
	}

	/**
	 * Used to scan the list of accounts and attempt to match them up with existing accounts 
	 * properties errors when anything is found.
	 * @param accounts - the list of accounts that are read from the spreadsheet
	 * @param staff - the list of staff that is associated with the project some how
	 * @return the accounts list updated with errors
	 */
	List searchAccountsForExisting(List people, List staff) { 
		List matches = []

		// TODO : JPM 3/2016 : Believe that we have a person match function in PersonService that we might be able to leverage
		// TODO : JPM 3/2016 : the findPerson is NOT case-insensitive which can/will cause problems
		def findPerson = { personInfo ->
			def person = staff.find {
				it.firstName == personInfo.firstName &&
				(it.lastName == personInfo.lastName || ( ! it.lastName && ! personInfo.lastName)) &&
				(it.middleName == personInfo.middleName || ( ! it.middleName && ! personInfo.middleName))
			} 
			return person
		}

		// Look over the people and try to find them in the system and then mark them as existing if they are.
		for (int i=0 ; i < people.size; i++) {
			def person = findPerson(people[i])

			if (person) {
				people[i].match = 'person'
				matches << people[i]

			} else {
				if (people[i].username) {
					def user = UserLogin.findByUsername(people[i].username)
					if (user) {
						people[i].match = 'username:'+user.id 
						matches << people[i]
					}
				}
			}
		}
		return matches
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
			properties.each { 
				account.put(it[1], WorkbookUtil.getStringCellValue(accountsSheet, (it[0] - 1), row))
			}
			account.put('errors', [])
			accounts.add(account)
		}
		return accounts
	}	



/****** 
 ** IMPORT SECTION
 ******/


	def importAccounts() {


		switch (params.step) {


			case 'post':

				def createUserLogin = params.createUserlogin == 'Y'
				def activateLogin = params.activateLogin == 'Y'
				def randomPassword = params.randomPassword == 'Y'
				def forcePasswordChange = params.forcePasswordChange == 'Y'
				def commonPassword = params.password
				def expireDays = NumberUtils.toInt(params.expireDays,90)
				def header = params.header == 'Y'
				def role = params.role

				//MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
				//CommonsMultipartFile xls = ( CommonsMultipartFile ) mpr.getFile('/tmp/tdstm-account-import.xls')

				HSSFWorkbook xlsWorkbook = new HSSFWorkbook(new FileInputStream(new File('/tmp/tdstm-account-import.xls')))


				people = parseXLS(xlsWorkbook, header, createUserLogin)
				lookForMatches()

				if (randomPassword) {
					commonPassword = UUID.randomUUID().toString()
				}

				def expiryDate = new Date()

				use(TimeCategory) {
					expiryDate = expiryDate + expireDays.days
				}

				log.info "expiryDate=$expiryDate"

				def failedPeople = []
				def created = 0

				if (!StringUtils.isEmpty(role) && !validRoleCodes.contains(role)) {
					failed = true
					people = []
				}

				def projectCompanies = partyRelationshipService.getProjectCompanies(project.id)

				people.each() { p -> 

					def company = projectCompanies.find{it.partyIdTo.name == p.company}
					if(!company){
						p.errors << "Unable to assign ${p.name} to ${p.company}"
					}else{
						def person
						boolean failed = false
						boolean haveMessage = false

						if (p.match ) {
							// Find the person
							person = findPerson(p)
							if (! person) {
								p.errors << "Unable to find previous Person match"
								failed = true
							} else {
								person.email = p.email
								person.workPhone = p.phone
								person.title = p.title
								person.deparment = p.department
								person.location = p.location
								person.stateProv = p.stateProv
								person.country = p.country
								person.mobilePhone = p.mobile

								if (person.validate() && person.save(flush:true)) {
									log.info "importAccounts() : updated person $person"
									partyRelationshipService.addCompanyStaff(company, person)
								} else {
									p.errors << "Error" + GormUtil.allErrorsString(person)
									failed = true
								}
							}
						} else {
							person = new Person(
								firstName:p.firstName, 
								middleName:p.middleName, 
								lastName:p.lastName,
								email:p.email,
								workPhone: p.phone,
								title: p.title,
								department: p.department,
								location: p.location,
								stateProv: p.stateProv,
								country: p.country,
								mobilePhone: p.mobile,
								staffType: 'Salary'
								)
						
							if (person.validate() && person.save(flush:true)) {
								log.info "importAccounts() : created person $person"
								partyRelationshipService.addCompanyStaff(company, person)
								partyRelationshipService.addProjectStaff(project, person)
							} else {
								p.errors << "Error" + GormUtil.allErrorsString(person)
								failed = true
							}

							// Assign the user to one or more teams appropriately
							if (!failed && p.teams) {
								List teams = splitTeams(p.teams)

								teams.each { t ->
									if (teamCodes.contains(t)) {
										partyRelationshipService.addStaffFunction(person, t, project.client, project)
									}
								}
							}
						}

						def userRole = role
						if (!StringUtils.isEmpty(p.role) && validRoleCodes.contains(p.role)) {
							userRole = p.role
						}
						if (!validRoleCodes.contains(userRole)) {
							userRole = DEFAULT_ROLE
						}
						if (!failed && !StringUtils.isEmpty(userRole)) {
							log.debug "importAccounts() : creating Role $userRole for $person"
							// Delete previous security roles if they exist
							def assignedRoles = []
							def assignRole = false
							if (p.match) {
								def personRoles = userPreferenceService.getAssignedRoles(person);
								personRoles.each { r ->
									assignedRoles << r.id
									if (r.id != userRole) {
										assignRole = true
									}
								}
								if (assignRole) {
									userPreferenceService.deleteSecurityRoles(person)
								}
								if (personRoles.size() == 0) {
									assignRole = true
								}
							} else {
								assignRole = true
							}
							if (assignRole) {
								userPreferenceService.setUserRoles([userRole], person.id)

								// Audit role changes
								def currentUser = securityService.getUserLogin()
								if (p.match) {
									p.errors << "Roles ${assignedRoles.join(',')} removed and assigned role ${userRole}."
									haveMessage = true
									auditService.logMessage("$currentUser changed ${person} roles, removed ${assignedRoles.join(',')} and assigned the role ${userRole}.")
								} else {
									auditService.logMessage("$currentUser assigned to ${person} the role ${userRole}.")
								}
							}
						}

						if (person && createUserLogin && p.username) {
							def u = UserLogin.findByPerson(person)
							if (!u) {
								def userPass = commonPassword
								if (!StringUtils.isEmpty(p.password)) {
									userPass = p.password
								}
								u = new UserLogin(
									username: p.username,
									active: (activateLogin ? 'Y' : 'N'),
									expiryDate: expiryDate,
									person: person,
									forcePasswordChange: (forcePasswordChange ? 'Y' : 'N')
								)

								u.applyPassword(userPass)

								if (! u.validate() || !u.save(flush:true)) {
									p.errors << "Error" + GormUtil.allErrorsString(u)
									log.debug "importAccounts() UserLogin.validate/save failed - ${GormUtil.allErrorsString(u)}"
									failed = true
								} else {
									log.info "importAccounts() : created UserLogin $u"
									def up = new UserPreference(
										userLogin: u,
										preferenceCode: 'CURR_PROJ',
										value: project.id.toString()
									)
									if (! up.validate() || ! up.save()) {
										log.error "importAccounts() : failed creating User Preference for $person : " + GormUtil.allErrorsString(up)
										p.errors << "Setting Default Project Errored"
										failed = true
									}
								}
							} else {
								failed = true
								p.errors << "Person already have a userlogin: $u"
							}

							if (!failed) created++

						}

						if (failed || haveMessage) {
							failedPeople << p	
						}
					}

				} // people.each

				map.step = 'results'
				map.failedPeople = failedPeople
				map.created = created
				break

			default: 
				break

		} // switch

		return map

	}

}