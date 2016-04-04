/**
 * AccountImportExportService - A set of service methods the importing and exporting of project staff and users
 */

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
		personId      : [ssPos:1,  formPos:1,  type:'I', width:50,  locked:true,  label:'ID'],
		firstName     : [ssPos:2,  formPos:2,  type:'P', width:90,  locked:true,  label:'First Name'],
		middleName    : [ssPos:3,  formPos:3,  type:'P', width:90,  locked:true,  label:'Middle Name'],
		lastName      : [ssPos:4,  formPos:4,  type:'P', width:90,  locked:true,  label:'Last Name'],
		company       : [ssPos:5,  formPos:5,  type:'T', width:90,  locked:true,  label:'Company'],
		errors        : [ssPos:0,  formPos:6,  type:'T', width:200, locked:false, label:'Errors', xtemplate:'kendo-errors-template'],
		workPhone     : [ssPos:6,  formPos:7,  type:'P', width:100, locked:false, label:'Work Phone'],
		mobilePhone   : [ssPos:7,  formPos:8,  type:'P', width:100, locked:false, label:'Mobile Phone'],
		email         : [ssPos:8,  formPos:9,  type:'P', width:100, locked:false, label:'Email'],
		title         : [ssPos:9,  formPos:10, type:'P', width:100, locked:false, label:'Title'],
		department    : [ssPos:10, formPos:11, type:'P', width:100, locked:false, label:'Department'],
		location      : [ssPos:11, formPos:12, type:'P', width:100, locked:false, label:'Location/City'],
		stateProv     : [ssPos:12, formPos:13, type:'P', width:100, locked:false, label:'State/Prov'],
		country       : [ssPos:13, formPos:14, type:'P', width:100, locked:false, label:'Country'],
		personTeams   : [ssPos:14, formPos:15, type:'T', width:150, locked:false, label:'Person Team(s)'],
		projectTeams  : [ssPos:15, formPos:15, type:'T', width:150, locked:false, label:'Project Team(s)'],
		roles         : [ssPos:16, formPos:17, type:'T', width:100, locked:false, label:'Security Role(s)'],
		username      : [ssPos:17, formPos:18, type:'U', width:120, locked:false, label:'Username'],
		accountLocal  : [ssPos:18, formPos:19, type:'U', width:100, locked:false, label:'Local Account?'],
		loginActive   : [ssPos:19, formPos:20, type:'U', width:100, locked:false, label:'Login Active?'],
		accountExp    : [ssPos:20, formPos:21, type:'U', width:100, locked:false, label:'Account Expiration'],
		passwordExp   : [ssPos:21, formPos:22, type:'U', width:100, locked:false, label:'Password Expiration'],
		passwordFixed : [ssPos:22, formPos:23, type:'U', width:100, locked:false, label:'Pswd Never Expires?'],
		match         : [ssPos:0,  formPos:24, type:'T', width:100, locked:false, label:'Matched On']
	]

	/*********************************************************************************************************
	 ** Controller Methods
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
		validateUploadedAccounts(accounts, project, options)

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
			def (person, error, changed) = addOrUpdatePerson(user, accounts[i], options)
			log.debug "importAccount_Step3_PostChanges() call to addOrUpdatePerson() returned person=$person, error=$error, changed=$changed"
			if (error) {
				accounts[i].postErrors << error
				results.failedPerson << "Row ${i+2} $error"
			} else {
				accounts[i].person = person
				if (accounts[i].isNewAccount) {
					results.createdPerson++
				}

				// Update teams
				def teamChanged = addOrUpdateTeams(user, accounts[i], project, options)
				if (teamChanged) {
					results.teamsUpdated++
				}
				/*
				if (teamError) {
					accounts[i].postError << teamError
				}
				*/

				if (changed || teamChanged) {
					results.updatedPerson++
				} else {
					results.unchangedPerson++
				}
			}

			// Deal with the userLogin
			if (options.createUserlogin) {

			}

		}

		throw new DomainUpdateException(results.toString())	
		return results	
	}

	/*********************************************************************************************************
	 ** Helper Methods
	 *********************************************************************************************************/

	/**
	 * Used to add or update the person
	 * @param account - the account information
	 * @param options - the map of the options
	 * @return a list containing:
	 *    Person - the person created or updated
	 *    String - an error message if the save or update failed
	 *    boolean - a flag indicating if the account was changed (true) or unchanged (false)
	 */
	List addOrUpdatePerson(UserLogin byWhom, Map account, Map options) {
		Person person
		String error
		boolean changed=false

		if (account.isNewAccount) {
			person = new Person()
		} else {
			person = account.person
		}

		// Update the person with the values passed in
		accountSpreadsheetColumnMap.each {prop, info ->
			if (info.type == 'P' && prop != 'personId') {
				if ((person[prop] == null && account[prop]) ||
					(person[prop] != account[prop] ) )
				{
					person[prop] = account[prop]
				}
				
			}
		}

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

		return [person, error, changed]
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

// TODO : JPM 4/2016 : Set timezone to that of the project when creating the user

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
			} else {
				// Nothing to be done here
				return false
			}
		}

		userLogin.expiryDate = options.expiryDate
	}




/*

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

						// Create/Update UserLogin
						if (person && createUserLogin && p.username) {

							error = createUserForAccount(person, project, userSettings)
							if (error) {
								p.errors << error
								haveMessage = true
							}

							if (!failed) created++

						}

						if (failed || haveMessage) {
							failedPeople << p	
						}
					}

				} // people.each


		def u = UserLogin.findByPerson(person)
		if (!u) {
			def userPass = commonPassword
			if (!StringUtils.isEmpty(userSettings.password)) {
				userPass = userSettings.password
			}
			u = new UserLogin(
				username: userSettings.username,
				active: userSettings.activateLogin
				expiryDate: userSettings.expiryDate,
				person: person,
				forcePasswordChange: userSettings.forcePasswordChange
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
*/				



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
			createUserLogin: params.createUserlogin?.toBoolean(),
			activateLogin: (params.activateLogin == 'Y'),
			randomPassword: (params.randomPassword == 'Y'),
			forcePasswordChange: (params.forcePasswordChange == 'Y'),
			commonPassword: params.password,
			userRoles: StringUtil.splitter(params.role, ',', [' ', ';', '|'])
		]
		if (params.filename) {
			options.filename = params.filename
		}

		// log.debug "importParamsToOptionsMap \n     params=$params\n     options=$options"

		return options
	}

	/**
	 * Used to reverse the Import options Map back into something that can be used as params for a page request
	 * @param options - the map created by the importParamsToOptionsMap method
	 * @param a map that can be used with requests
	 */
	Map importOptionsAsParams(Map options) {
		Map params = [
			createUserLogin: (options.createUserLogin ? 'Y' : 'N'),
			activateLogin: (options.activateLogin ? 'Y' : 'N'),
			randomPassword: (options.randomPassword ? 'Y' : 'N'),
			forcePasswordChange: (options.forcePasswordChange ? 'Y' : 'N'),
			commonPassword: options.commonPassword,
			userRoles: options.userRoles.join(',')
		]
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
		if (! filename) {
			throw new InvalidParamException('The import filename parameter was missing')
		}

		String fqfn=getTempDirectory() + '/' + filename	
		File file = new File(fqfn)
		HSSFWorkbook xlsWorkbook = new HSSFWorkbook(new FileInputStream(file))
		return xlsWorkbook
	}

	/**
	 * Used to validate the list of accounts that were uploaded and will populate the individual maps with 
	 * properties errors when anything is found.
	 * @param accounts - the list of accounts that are read from the spreadsheet
	 * @return the accounts list updated with errors
	 */
	List<Map> validateUploadedAccounts(List<Map> accounts, Project project, Map options) {

		// Retrieves all the roles that this user is allowed to assign.
		List validRoleCodes = securityService.getAssignableRoles(securityService.getUserLoginPerson()).id
		List teamCodes = partyRelationshipService.getStaffingRoles().id
		// def emailValidator = EmailValidator.getInstance()

		PartyGroup client = project.client
		Map companiesByNames = projectService.getCompaniesMappedByName(project)
		Map companiesById = projectService.getCompaniesMappedById(project)

		// Validate the teams, roles, company and any other things need be validated

		for (int i=0; i < accounts.size(); i++) {
			accounts[i].errors = []

			boolean invalidCompany=false
			PartyGroup company

			// StringUtils.equalsIgnoreCase()
			// Validate that the company name specified is one of the project related companies
			if (accounts[i].company) {
				company = companiesByNames.get(accounts[i].company.toLowerCase())
				if (company) {
					accounts[i].companyId = company.id
					accounts[i].companyObj = company
				} else {
					accounts[i].errors << 'Company not found'
					invalidCompany = true
				}
			} else {
				// Default the project company if it wasn't specified
				accounts[i].company = client.name
				accounts[i].companyId = client.id
				accounts[i].companyObj = client
			}

			Person personById, personByEmail, personByUserLogin, personByName, personToUse

			// If it is an existing person (has an id), lets see if the person someone from the project
			if (accounts[i].personId) {
				Long pid = NumberUtil.toPositiveLong(accounts[i].personId, -1)
				if (pid == -1) {
					accounts[i].errors << 'Invalid person ID'
				} else {
					personById = Person.get(accounts[i].personId)
					if (personById) {
						accounts[i].match << "personId:${accounts[i].personId}"
						if (personById.company.id != accounts[i].companyId) {
							accounts[i].errors << 'Account by ID conflicts with company'
							accounts[i].companyId = null
						} else {
							personToUse = personById
						}
					} else {
						accounts[i].errors << 'Person by ID not found'
					}
				}
			}

			// Attempt to lookup account by username
			if (accounts[i].username) {
				personByUserLogin = UserLogin.findByUsername(accounts[i].username)?.person
				if (personByUserLogin) {
					if (personById) {
						if (personByUserLogin.id != personByUserLogin.id) {
							accounts[i].errors << 'Account by email conflicts with ID'
						}
					} else {
						if (personByUserLogin.company.id != company.id) {
							accounts[i].errors << 'Account by username conflicts with company'
						} else {
							personToUse = personByUserLogin
						}
					}
				}
			}

			// Attempt to lookup account by email
			if (accounts[i].email) {
				personByEmail = Person.findByEmail(accounts[i].email)
				if (personByEmail) {
					if (personById && personByEmail.id != personById.id) {
						accounts[i].errors << 'Account by email conflicts with ID'
					}
					if (personByUserLogin && personByEmail.id != personByUserLogin.id) {
						accounts[i].errors << 'Account by email conflicts with username'
					}					
					// Check to see if the email address is valid
					//if (! emailValidator.isValid(accounts[i].email)) {
					//	accounts[i].errors << 'Invalid email format'
					//}
					if (!personToUse && !accounts[i].errors) {
						personToUse = personByEmail
					}
				}
			}

			// Attempt to find the person by name which is only possible if we know the company
			if (company) {
				Map nameMap = [first:accounts[i].firstName,
					middle:accounts[i].middleName,
					last:accounts[i].lastName
				]
				List people = personService.findByCompanyAndName(company, nameMap)
				if (people.size() > 0) {
					if (people.size() > 1) {
						accounts[i].errors << 'Found ambiguity searching by name'
					} else {
						personByName = people[0]
						// See if the name match is different than the above
						if (personById && personById.id != personByName.id) {
							accounts[i].errors << 'Account by name conflicts with ID'
						}
						if (personByUserLogin && personByUserLogin.id != personByName.id) {
							accounts[i].errors << 'Account by name conflicts with username'
						}
						if (personByEmail && personByEmail.id != personByName.id) {
							accounts[i].errors << 'Account by name conflicts with email'
						}
						if (!personToUse && !accounts[i].errors) {
							personToUse = personByName
							accounts[i].match << 'name'
						}
					}
				}
			}

			// Set a flag on the account if it is going to be a new account
			if (personToUse) {
				accounts[i].isNewAccount = false
				accounts[i].person = personToUse
			} else {
				accounts[i].isNewAccount = true
				accounts[i].person = null
			}

			// Load a temporary Person domain object with the properties from the spreadsheet and see if any
			// of the valids will break the validation constraints
			Person validatePerson = new Person()
			accountSpreadsheetColumnMap.each {prop, info ->
				if (info.type == 'P') {
					validatePerson[prop] = accounts[i][prop]
				}
			}
			if (! validatePerson.validate()) {
				validatePerson.errors.allErrors.each {
					accounts[i].errors << "${it.getField()} error ${it.getCode()}"
				}
			}
			validatePerson.discard()


			// Validate the Teams
			['person', 'project'].each { tp ->
				String teamProperty = "${tp}Teams"
				// Strip off the minus (-) prefix to validate the team codes
				String teamsWithoutMinus = accounts[i][teamProperty]?.replaceAll('-','')

				List teams = StringUtil.splitter(teamsWithoutMinus, ',', [';',':','|'])
				if (teams) {
					List invalidTeams = teams - teamCodes
					// log.debug "validateUploadedAccounts() invalidTeams=$invalidTeams"
					if (invalidTeams) {
						accounts[i].errors << "Invalid $tp team(s) ${invalidTeams.join(',')}"
					}

					// Now put the appropriate team codes into the account map
					accounts[i][teamProperty] = StringUtil.splitter(accounts[i][teamProperty], ',', [';',':','|'])
				} else {
					accounts[i][teamProperty] = []
				}
			}

			// Review the security roles that are going to be assigned to the person
			List currentRoles = []
			List invalidRoles = []
			if (accounts[i].role) {
				currentRoles = StringUtil.splitter(accounts[i].role, ',', [';',':','|'])
				invalidRoles = currentRoles - validRoleCodes
			}			

			if (!StringUtils.isEmpty(accounts[i].role) && invalidRoles) {
				accounts[i].errors << "Invalid role: ${invalidRoles.join(';')}"
			}

			// Attempt to match the persons to existing users
			// List staff = partyRelationshipService.getCompanyStaff( project.client.id )
			// TODO : JPM 4/2016 : Should check if the user can see people unassigned to the project  

			// Set the icon to be displayed base on what is being done
			if (accounts[i].errors) {
				accounts[i].icon = '/tdstm/icons/exclamation.png'
			} else if (accounts[i].isNewAccount) {
				accounts[i].icon = '/tdstm/icons/add.png'				
			} else {
				accounts[i].icon = '/tdstm/icons/pencil.png'
			}

		}

		return accounts
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