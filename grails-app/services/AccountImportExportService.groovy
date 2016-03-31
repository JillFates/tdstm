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
		username      : [colPos:0,  type:'U', width:100, locked:true, label:'Username'],
		firstName     : [colPos:1,  type:'P', width:100, locked:true, label:'First Name'],
		middleName    : [colPos:2,  type:'P', width:100, locked:true, label:'Middle Name'],
		lastName      : [colPos:3,  type:'P', width:100, locked:true, label:'Last Name'],
		company       : [colPos:5,  type:'P', width:100, locked:true, label:'Company'],
		workPhone     : [colPos:4,  type:'P', width:100, locked:false, label:'Work Phone'],
		mobilePhone   : [colPos:14, type:'P', width:100, locked:false, label:'Mobile Phone'],
		email         : [colPos:8,  type:'P', width:100, locked:false, label:'Email'],
		title         : [colPos:9,  type:'P', width:100, locked:false, label:'Title'],
		department    : [colPos:10, type:'P', width:100, locked:false, label:'Department'],
		location      : [colPos:11, type:'P', width:100, locked:false, label:'Location/City'],
		state         : [colPos:12, type:'P', width:100, locked:false, label:'State/Prov'],
		country       : [colPos:13, type:'P', width:100, locked:false, label:'Country'],
		teams         : [colPos:6,  type:'P', width:100, locked:false, label:'Team(s)'],
		roles         : [colPos:7,  type:'P', width:100, locked:false, label:'Security Role(s)'],
		loginActive   : [colPos:15, type:'U', width:100, locked:false, label:'Login Active'],
		password      : [colPos:16, type:'U', width:100, locked:false, label:'Password'],
		accountExp    : [colPos:17, type:'U', width:100, locked:false, label:'Account Exp'],
		passwordExp   : [colPos:18, type:'U', width:100, locked:false, label:'Password Exp'],
		passwordFixed : [colPos:19, type:'U', width:100, locked:false, label:'Permanent Pswd'],
		accountLocal  : [colPos:20, type:'U', width:100, locked:false, label:'Local Account'],
		//errors        : [colPos:21, type:'T', label:'Errors'],
		//match         : [colPos:22, type:'T', label:'Matched'],
	]


	/**
	 * Used to get the labels in column order
	 */ 
	List getLabelsInColumnOrder() {
		List list = accountSpreadsheetColumnMap.collect { prop, info ->  [info.get('colPos'), info.get('label')] }
		list.sort { it[0] }
		list = list.collect { it[1] }

		return list
	}

	/**
	 * Used to get the property names of the accountSpreadsheetColumnMap in the column order
	 */
	List getPropertiesInColumnOrder() {
		List list = accountSpreadsheetColumnMap.collect { prop, info ->  [info.get('colPos'), prop] }
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
        HSSFWorkbook book = ExportUtil.loadSpreadsheetTemplate(templateFilename) 
        return book
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
	 * This method outputs all the fields to the sheet
	 */
	private void addRowToAccountSpreadsheet(sheet, Map account, int rowNumber) {
		accountSpreadsheetColumnMap.each { prop, info ->
			if (account.containsKey(prop)) {
				WorkbookUtil.addCell(sheet, info.colPos, rowNumber, account[prop])
			}
		}
	}

	/**
	 * Used to map a Person to the accountSpreadsheetColumnMap 
	 * @param person - the person to map to the AccountFieldMap format
	 * @return a map of the person information
	 */
	private Map personToFieldMap(Person person) {
		List teams = partyRelationshipService.getCompanyStaffFunctions(person.company, person)*.toString()

		List roles = securityService.getAssignedRoles(person).id

		Map map = [
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
			teams        : teams.join(";"), 
			roles        : roles.join(";")
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
		Map map = [
			username      : user.username,
			accountLocal  : user.isLocal? "Y" : "N",
			loginActive   : user.active,
			password      : '',	// NEVER export the password
			accountExp    : TimeUtil.formatDateTime(session, user.expiryDate, TimeUtil.FORMAT_DATE_TIME_6),
			passwordExp   : ( user.passwordExpirationDate 
								? TimeUtil.formatDateTime(session, user.passwordExpirationDate, TimeUtil.FORMAT_DATE_TIME_6) 
								: ''),
			passwordFixed : user.passwordNeverExpires? "Y" : "N",
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
	 * @param filename - the name of the spreadsheet
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
		for (int row = firstAccountRow; row <= lastRow; row++) {
			Map account = [:]
			accountSpreadsheetColumnMap.each { prop, info ->
				account.put(prop, WorkbookUtil.getStringCellValue(accountsSheet, info.colPos, row))
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