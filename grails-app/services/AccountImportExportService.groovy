/**
 * AccountImportExportService - A set of service methods the importing and exporting of project staff and users
 */

import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.WorkbookUtil
import org.apache.poi.hssf.usermodel.HSSFWorkbook

class AccountImportExportService {

	def partyRelationshipService
	def projectService
	def securityService	

	static final LOGIN_OPT_ALL = 'A'
	static final LOGIN_OPT_ACTIVE = 'Y'
	static final LOGIN_OPT_INACTIVE = 'N'

	static final Map accountSpreadsheetColumnMap = [
		username      : [colPos:0,  type:'U'],
		firstName     : [colPos:1,  type:'P'],
		middleName    : [colPos:2,  type:'P'],
		lastName      : [colPos:3,  type:'P'],
		company       : [colPos:5,  type:'P'],
		workPhone     : [colPos:4,  type:'P'],
		mobilePhone   : [colPos:14, type:'P'],
		email         : [colPos:8,  type:'P'],
		title         : [colPos:9,  type:'P'],
		department    : [colPos:10, type:'P'],
		location      : [colPos:11, type:'P'],
		state         : [colPos:12, type:'P'],
		country       : [colPos:13, type:'P'],
		teams         : [colPos:6,  type:'P'],
		roles         : [colPos:7,  type:'P'],
		loginActive   : [colPos:15, type:'U'],
		password      : [colPos:16, type:'U'],
		accountExp    : [colPos:17, type:'U'],
		passwordExp   : [colPos:18, type:'U'],
		passwordFixed : [colPos:19, type:'U'],
		accountLocal  : [colPos:20, type:'U']
	]

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

		// Load the spreadsheet template and populate it
		String templateFilename = '/templates/TDS-Accounts_template.xls'
        def book = ExportUtil.loadSpreadsheetTemplate(templateFilename) 
		def sheet = book.getSheet("Accounts")
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
						map << userToFieldMap(user, session)
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
	private Map userToFieldMap(UserLogin user, Object session) {
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

}