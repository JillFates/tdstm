import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.apache.commons.lang3.StringUtils
import grails.validation.ValidationException
import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.NumberUtil
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.exceptions.ServiceException

class PersonController {
	
	def partyRelationshipService
	def userPreferenceService
	def securityService
	def personService
	def projectService
	def sessionFactory
	def jdbcTemplate
	def controllerService

	def index() { redirect(action:"list",params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']



def test = {
	Person byWhom = securityService.getUserLoginPerson()
	Project project = Project.get(2445)
	//List suitableTeams = getSuitableTeams(byWhom)
	//List assignedTeams = getAssignedTeams(project, byWhom)
	render "Person $byWhom" +
		"<br>Assigned Projects: ${byWhom.assignedProjects}<br>" +
		"<br>Assigned Teams:${byWhom.getAssignedTeams(project)}" +
//		"<br>Assigned Teams:${assignedTeams}" +


		"<br>Suitable Teams: ${byWhom.suitableTeams}" + 
//		"<br>Suitable Teams: ${suitableTeams}" + 
		"<br>Company:${byWhom.company}"
}
	/**
	 * Generates a list view of persons related to company
	 * @param id - company id
	 * @param companyName - optional search by name or 'ALL'
	 */
	def list() {
		if (!controllerService.checkPermission(this, 'PersonListView', true)) 
			return

		def listJsonUrl
		def company
		def currentCompany = securityService.getUserCurrentProject()?.client
		def companyId = params.companyId ?: (currentCompany? currentCompany.id : 'All')
		if(companyId && companyId != 'All'){
			def map = [controller:'person', action:'listJson', id:"${companyId}"]
			listJsonUrl = HtmlUtil.createLink(map)
		} else {
			def map = [controller:'person', action:'listJson']
			listJsonUrl = HtmlUtil.createLink(map)+'/All'
		}
		
		//def partyGroupList = PartyGroup.findAllByPartyType( PartyType.read("COMPANY")).sort{it.name}
		def partyGroupList = partyRelationshipService.associatedCompanies(securityService.getUserLoginPerson())
		// Used to set the default value of company select in the create staff dialog
		if(companyId && companyId != 'All')
			company = PartyGroup.find( "from PartyGroup as p where partyType = 'COMPANY' AND p.id = ?", [companyId.toLong()] )
		else
			company = currentCompany
		
		userPreferenceService.setPreference( "PARTYGROUP", companyId.toString() )

		def companiesList = PartyGroup.findAll( "from PartyGroup as p where partyType = 'COMPANY' order by p.name " )
		//used to show roles in addTeam select
		def availabaleRoles = partyRelationshipService.getStaffingRoles()
		return [companyId:companyId?:'All', company:company, partyGroupList:partyGroupList, 
					listJsonUrl:listJsonUrl, availabaleRoles:availabaleRoles]
	}
	
	def listJson() {
		if (!controllerService.checkPermission(this, 'PersonListView', true)) 
			return

		def sortIndex = params.sidx ?: 'lastname'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows?:'25')
		def currentPage = Integer.valueOf(params.page?:'1')
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def companyId
		def personInstanceList
		def filterParams = ['firstname':params.firstname, 'middlename':params.middlename, 'lastname':params.lastname, 'userLogin':params.userLogin, 'company':params.company, 'dateCreated':params.dateCreated, 'lastUpdated':params.lastUpdated, 'modelScore':params.modelScore]
		
		// Validate that the user is sorting by a valid column
		if( ! sortIndex in filterParams)
			sortIndex = 'lastname'
		
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		
		def query = new StringBuffer("""SELECT * FROM ( SELECT p.person_id AS personId, p.first_name AS firstName, 
			IFNULL(p.middle_name,'') as middlename, IFNULL(p.last_name,'') as lastName, IFNULL(u.username, 'CREATE') as userLogin, pg.name AS company, u.active, 
			date_created AS dateCreated, last_updated AS lastUpdated, u.user_login_id AS userLoginId, IFNULL(p.model_score, 0) AS modelScore 
			FROM person p
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF' 
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=p.person_id 
			LEFT OUTER JOIN party pa on p.person_id=pa.party_id 
			LEFT OUTER JOIN user_login u on p.person_id=u.person_id 
			LEFT OUTER JOIN party_group pg ON pg.party_group_id=r.party_id_from_id 
			""")
		
		if(params.id && params.id != "All" ){
			// If companyId is requested
			companyId = params.id
		}
		if( !companyId && params.id != "All" ){
			// Still if no companyId found trying to get companyId from the session
			companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
			if(!companyId){
				// Still if no luck setting companyId as logged-in user's companyId .
				def person = securityService.getUserLogin().person
				companyId = partyRelationshipService.getStaffCompany(person)?.id
			}
		}
		if(companyId){
			query.append(" WHERE pg.party_group_id = $companyId ")
		}
		
		query.append(" GROUP BY pa.party_id ORDER BY " + sortIndex + " " + sortOrder + ", IFNULL(p.last_name,'') DESC, p.first_name DESC) as people")
		
		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if(it.getValue())
				if(firstWhere){
					query.append(" WHERE people.${it.getKey()} LIKE '%${it.getValue()}%'")
					firstWhere = false
				} else {
					query.append(" AND people.${it.getKey()} LIKE '%${it.getValue()}%'")
				}
		}

		personInstanceList = jdbcTemplate.queryForList(query.toString())
		
		// Limit the returned results to the user's page size and number
		def totalRows = personInstanceList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if(totalRows > 0)
			personInstanceList = personInstanceList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			personInstanceList = []
		
		def haveCreateUserLoginPerm = controllerService.checkPermission(this, "CreateUserLogin", false)
		def haveEditUserLoginPerm = controllerService.checkPermission(this, "EditUserLogin", false)

		def map = [controller:'person', action:'listJson', id:"${params.companyId}"]
		def listJsonUrl = HtmlUtil.createLink(map)
		def createUrl = HtmlUtil.createLink([controller:'userLogin', action:'create'])
		def editUrl = HtmlUtil.createLink([controller:'userLogin', action:'edit'])
		def addUserIconUrl = HtmlUtil.resource([dir: 'icons', file: 'user_add.png', absolute: true])
		
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = personInstanceList?.collect {
			[ cell: ['<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.firstname+'</a>', 
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.middlename+'</a>', 
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.lastname+'</a>', 
			genCreateEditLink(haveCreateUserLoginPerm, haveEditUserLoginPerm, createUrl, editUrl, addUserIconUrl, it), 
			it.company, it.dateCreated, it.lastUpdated, it.modelScore], id: it.personId ]}
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	
	}

	/**
	 * Creates an anchor for specific user based on user permission
	 *
	 * @param haveCreateUserLoginPerm boolean value that indicates if the user have CreateUserLoginPerm
	 * @param haveEditUserLoginPerm boolean value that indicates if the user have EditUserLoginPerm
	 * @param createUrl url used to create a new login for the current person
	 * @param editUrl url used to edit login configuration for the current person
	 * @param person person object to be displayed
	 */
	private def genCreateEditLink(haveCreateUserLoginPerm, haveEditUserLoginPerm, createUrl, editUrl, addUserIconUrl, person) {
		def element = ""
		if (person.userLoginId) {
			if (haveEditUserLoginPerm) {
				element = '<a href="' + editUrl + '/' + person.userLoginId + '">' + person.userLogin + '</a>'
			} else {
				element = person.userLogin
			}
		} else {
			if (haveCreateUserLoginPerm) {
				element = '<a href="' + createUrl + '/' + person.personId + '"><img src="' + addUserIconUrl + '" /> Create User</a>'
			} else {
				element = ''
			}
		}
		return element
	}

	/**
	 * Used to bulk delete Person objects as long as they do not have user accounts or assigned tasks and optionally associated with assets
	 */
	def bulkDelete() {
		def ids = params.get("ids[]") 
		if (!ids) {
			render(ServiceResults.invalidParams('Please select at least one person to be be bulk deleted.') as JSON)
			return
		}

		if (ids instanceof String) {
			def arr = new String[1];
			arr[0] = ids;
			ids = arr;
		}

		try {
			controllerService.checkPermissionForWS('BulkDeletePerson') 
			def deleteIfAssocWithAssets = params.deleteIfAssocWithAssets == 'true'
			def data = personService.bulkDelete(ids, deleteIfAssocWithAssets)
			render(ServiceResults.success(data) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (InvalidParamException e) {
			render(ServiceResults.invalidParams(e.getMessage()) as JSON)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			e.printStackTrace()
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * Note: No reference found to this method
	 */
	def show() {
		if (!controllerService.checkPermission(this, 'PersonEditView')) 
			return

		def personInstance = Person.get( params.id )
		def companyId = params.companyId
		if(!personInstance) {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		} else { 
			def company = partyRelationshipService.getStaffCompany( personInstance )
			
			return [ personInstance : personInstance, companyId:company.id ] 
		}
	}

	/**
	 * Note: No reference found to this method
	 */
	def delete() {
		if (!controllerService.checkPermission(this, 'PersonDeleteView')) 
			return

		def personInstance = Person.get( params.id )
		def companyId = params.companyId
		if ( personInstance ) {
			def partyInstance = Party.findById( personInstance.id )      
			def partyRelnInst = PartyRelationship.findAll("from PartyRelationship pr where pr.partyIdTo = ${personInstance.id}")         
			def partyRole = PartyRole.findAll("from PartyRole p where p.party =${partyInstance.id}")      
			def loginInst = UserLogin.find("from UserLogin ul where ul.person = ${personInstance.id}")
			if ( loginInst ) {
				def preferenceInst = UserPreference.findAll("from UserPreference up where up.userLogin = ${loginInst.id}")
				preferenceInst.each{
				  it.delete()
				  }
				loginInst.delete()
			}       
			partyRelnInst.each{
			it.delete()
			}
			partyRole.each{
			it.delete()
			}      
			partyInstance.delete()      
			personInstance.delete()
			if( personInstance.lastName == null ) {
				personInstance.lastName = ""
			}
			flash.message = "Person ${personInstance} deleted"
			redirect( action:"list", params:[ id:companyId ] )
		}
		else {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		}
	}
	/**
	 * return person details to EDIT form
	 * Note: No reference found to this method
	 */
	def edit() {
		if (!controllerService.checkPermission(this, 'PersonEditView'))
			return

		def personInstance = Person.get( params.id )
		def companyId = params.companyId
		if(!personInstance) {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		}
		else {
			
			return [ personInstance : personInstance, companyId:companyId ]
		}
	}

	/**
	 * Used to update the Person domain objects
	 * Note: No reference found to this method
	 */
	def update() {
		if (!controllerService.checkPermission(this, 'PersonEditView')) 
			return
			
		def person = Person.get( params.id )
			
		def companyId = params.company

		// TODO : Security - Need to harden this

		if(person) {
			person.properties = params
			if ( person.validate() && person.save() ) {
				def userLogin = UserLogin.findByPerson(person)
				userLogin.active = person.active
				if (companyId != null ){
					def companyParty = Party.findById(companyId)
					partyRelationshipService.updatePartyRelationshipPartyIdFrom("STAFF", companyParty, 'COMPANY', person, "STAFF")
				}
				flash.message = "Person '$person' was updated"
				redirect( action:"list", params:[ id:companyId ])
			}
			else {
				flash.message = "Person '$person' not updated due to: " + GormUtil.errorsToUL(person)
				redirect( action:"list", params:[ id:companyId ])
			}
		}
		else {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ])
		}
	}

	/**
	 * Used to save a new Person domain object
	 * @param forWhom - used to indicate if the submit is from a person form otherwise it is invoked from Ajax call
	 */
	def save() {
		if (!controllerService.checkPermission(this, 'PersonCreateView')) 
			return

		// When forWhom == 'person' we're working with the company submitted with the form otherwise we're 
		// going to use the company associated with the current project.
		def isAjaxCall = params.forWhom != "person"
		def companyId
		if (isAjaxCall) {
			// First try to see if the person already exists for the current project
			def project = securityService.getUserCurrentProject()
			companyId = project.client.id
		} else {
			companyId = NumberUtil.toLong(params.company)
		}

		def errMsg
		def isExistingPerson = false 
		def person

		try {
			Person byWhom = securityService.getUserLoginPerson()
			person = personService.savePerson(params, byWhom, companyId, true)
		} catch (e) {
			log.error "save() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = e.getMessage()
		}		

		if (isAjaxCall) {
			def map = errMsg ? [errMsg : errMsg] : [ id: person.id, name:person.lastNameFirst, isExistingPerson: false, fieldName:params.fieldName]
			render map as JSON
		} else {
			if (errMsg) {
				flash.message = errMsg
			} else {
				// Just add a message for the form submission to know that the person was created
				flash.message = "A record for ${person.toString()} was created"
			}
			redirect( action:"list", params:[ companyId:companyId ] )
		}
	}

	/*
	 *  Remote method to edit Staff Details
	 *  Note: No reference found to this method
	 */
	def editShow() {
		if (!controllerService.checkPermission(this, 'PersonEditView')) 
			return

		def personInstance = Person.get( params.id )        
		def companyId = params.companyId
		def companyParty = Party.findById(companyId)
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dateCreatedByFormat = formatter.format(GormUtil.convertInToUserTZ( personInstance.dateCreated, tzId ) )
		def lastUpdatedFormat = formatter.format(GormUtil.convertInToUserTZ( personInstance.lastUpdated, tzId ) )
		if(!personInstance) {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:"list", params:[ id:companyId ] )
		}
		else {       	

			def items = [id: personInstance.id, firstName: personInstance.firstName, lastName: personInstance.lastName, 
						 nickName: personInstance.nickName, title: personInstance.title, active: personInstance.active, 
						 dateCreated: dateCreatedByFormat, lastUpdated: lastUpdatedFormat, companyId: companyId,
						 companyParty:companyParty, email: personInstance.email, department: personInstance.department,
						 location: personInstance.location, workPhone: personInstance.workPhone, mobilePhone: personInstance.mobilePhone]
			render items as JSON
		}
	}
	/*
	 *  Remote method to edit Staff Details
	 *  Note: Used only in projectStaff.gsp
	 */
	def editStaff() {
		if (!controllerService.checkPermission(this, 'EditProjectStaff')) 
			return

		def map = new HashMap()
		def personInstance = Person.read( params.id )
		def role = params.role
		def company = partyRelationshipService.getStaffCompany( personInstance )
		if(company == null){
			map.put("companyId","")
		}else{
			map.put("companyId",company.id)
			map.put("companyName",company.name)
		}
		map.put("id", personInstance.id)
		map.put("firstName", personInstance.firstName)
		map.put("lastName", personInstance.lastName)
		map.put("nickName", personInstance.nickName)
		map.put("title", personInstance.title)
		map.put("email", personInstance.email)
		map.put("active", personInstance.active)
		map.put("role", role)
		render map as JSON
	}
	/*
	 *  Remote method to update Staff Details
	 *  Note: Used only in projectStaff.gsp
	 */
	def updateStaff() {
		if (!controllerService.checkPermission(this, 'EditProjectStaff')) 
			return

		def personInstance = Person.get( params.id )
		def projectId = session.CURR_PROJ.CURR_PROJ
		def roleType = params.roleType
		def companyId = params.company
		//personInstance.lastUpdated = new Date()
		if(personInstance) {
			personInstance.properties = params
			if(personInstance.lastName == null){
				personInstance.lastName = ""	
			}
			if ( !personInstance.hasErrors() && personInstance.save() ) {
				def projectParty = Project.findById(projectId)
				if(companyId != ""){
					def companyParty = Party.findById(companyId)
					if(!personService.isAssociatedTo(personInstance, companyParty)){
						throw new DomainUpdateException("The person ${personInstance} is not associated with the company ${companyParty}")
					}
					partyRelationshipService.updatePartyRelationshipPartyIdFrom("STAFF", companyParty, 'COMPANY', personInstance, "STAFF")
				}
				def partyRelationship = partyRelationshipService.updatePartyRelationshipRoleTypeTo("PROJ_STAFF", projectParty, 'PROJECT', personInstance, roleType)
				 
				flash.message = "Person ${personInstance} updated"
				redirect( action:"projectStaff", params:[ projectId:projectId ])
			} else {
				flash.message = "Person ${personInstance} not updated"
				redirect( action:"projectStaff", params:[ projectId:projectId ])
			}
		} else {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:"projectStaff", params:[ projectId:projectId ])
		}
	}
	/*
	 *  Return Project Staff 
	 *  Note: there is no direct call to this method
	 */
	def projectStaff() {
		if (!controllerService.checkPermission(this, 'ProjectStaffList')) 
			return

		def projectId = session.CURR_PROJ.CURR_PROJ
		def submit = params.submit
		def role = ""
		def subject = SecurityUtils.subject
		def projectStaff = partyRelationshipService.getProjectStaff( projectId )	
		def companiesStaff = partyRelationshipService.getProjectCompaniesStaff( projectId,'' )
		def projectCompanies = partyRelationshipService.getProjectCompanies( projectId )
		return [ projectStaff:projectStaff, companiesStaff:companiesStaff, projectCompanies:projectCompanies, 
				projectId:projectId, submit:submit, personHasPermission:RolePermissions.hasPermission("AddPerson") ]
	}

	/*
	 * Method to add Staff to project through Ajax Overlay 
	 */
	def saveProjectStaff() {
		if (!controllerService.checkPermission(this, 'EditProjectStaff')) 
			return

		def flag = false
		def message = ''
		
		if(request.JSON.personId){
			def personId = request.JSON.personId
			def roleType = request.JSON.roleType
			def projectId = request.JSON.projectId
			//def projectParty = Project.findById( projectId )
			def projectParty = securityService.getUserCurrentProject()
			def personParty = Person.findById( personId )
			def projectStaff
			if(NumberUtil.toInteger(request.JSON.val) == 1) {
				projectStaff = partyRelationshipService.deletePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
				def moveEvents = MoveEvent.findAllByProject(projectParty)
				def results = MoveEventStaff.executeUpdate("delete from MoveEventStaff where moveEvent in (:moveEvents) and person = :person and role = :role",[moveEvents:moveEvents, person:personParty,role:RoleType.read(roleType)])
			} else if(personService.hasAccessToProject(personParty, projectParty) ||  ( ! ( partyRelationshipService.isTdsEmployee(personId) && ! RolePermissions.hasPermission("EditTDSPerson") ) )){
				projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
			}else{
				message = "This person doesn't have access to the selected project"
			}

			flag = message.size() == 0
		}
		
		def data = ['flag':flag, 'message':message]
		try{
			render(ServiceResults.success(['flag':flag, 'message':message]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (InvalidParamException e) {
			render(ServiceResults.fail(e.getMessage()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	/*
	 * Method to save person details and create party relation with Project as well 
	 * Note: Used only in projectStaff.gsp
	 */
	def savePerson() {
		if (! controllerService.checkPermission(this, 'PersonCreateView')) {
			ServiceResults.unauthorized(response)
			return
		}
	
		def person = new Person( params )
		
		if (person.lastName == null) {
			person.lastName = ""	
		}

		def companyId = params.company
		def projectId = session.CURR_PROJ.CURR_PROJ
		def roleType = params.roleType
		if ( !person.hasErrors() && person.save() ) {
			
			if ( companyId != null && companyId != "" ) {
				def companyParty = Party.findById( companyId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
			}

			if ( projectId != null && projectId != "" && roleType != null) {
				def projectParty = Party.findById( projectId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "PROJ_STAFF", projectParty, "PROJECT", personInstance, roleType )
			}

			flash.message = "Person ${person} created"
			redirect( action:'projectStaff', params:[ projectId:projectId, submit:'Add' ] )
		} else {
			flash.message = " Person FirstName cannot be blank. "
			redirect( action:'projectStaff', params:[ projectId:projectId,submit:'Add' ] )
		}
	}

	/**
	 * Update the person details and user password, Return person first name
	 * @param  : person details and user password
	 * @return : person firstname
	 */
	def updatePerson() {
		try {
			Person byWhom = securityService.getUserLoginPerson()
			String tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			/*Party newCompany = Party.findByName(params["Company"])
			if(!personService.isAssociatedTo(person, newCompany)){
				throw new DomainUpdateException("The person ${personInstance} is not associated with the company ${newCompany}")
			}*/
			Person person = personService.updatePerson(params, byWhom, tzId, true)
			if (params.tab) {
				forward( action:'loadGeneral', params:[tab: params.tab, personId:person.id])
			} else { 
				List results = [ name:person.firstName, tz:getSession().getAttribute( "CURR_TZ" )?.CURR_TZ ]
				render results as JSON
			}		
		} catch (e) {
			e.printStackTrace()
			if (log.isDebugEnabled()) {
				log.debug "updatePerson() failed : ${ExceptionUtil.stackTraceToString(e)}"
			}
			ServiceResults.respondWithError(response, e.getMessage())
		}
	}

	/**
	 * Will return person details for a given personId as JSON
	 * @param  params.id - the person id
	 * @return person details as JSON
	 */
	def retrievePersonDetails() {
		try {
			Person currentPerson = securityService.getUserLoginPerson()
			Person person = personService.validatePersonAccess(params.id, currentPerson)
			UserLogin userLogin = securityService.getPersonUserLogin( person )
			
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ

			// TODO : JPM 5/2015 : Move the date formating into a reusable class
			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
			def expiryDate = userLogin.expiryDate ? formatter.format(GormUtil.convertInToUserTZ(userLogin.expiryDate,tzId)) : ""
			
			def personDetails = [person:person, expiryDate: expiryDate, isLocal:userLogin.isLocal]

			render personDetails as JSON
		} catch (e) {
			ServiceResults.respondWithError(response, e.getMessage())
		}
	}

	/**
	 * Update the person account that is invoked by the user himself
	 * @param  : person id and input password
	 * @return : pass:"no" or the return of the update method
	 */
	def updateAccount = {
		String errMsg = ''
		Map results = [:]
		try {
			Person byWhom = securityService.getUserLoginPerson()
			params.id = byWhom.id
			String tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			Person person = personService.updatePerson(params, byWhom, tzId, false)

			if (params.tab) {
				// Funky use-case that we should try to get rid of
				forward( action:'loadGeneral', params:[tab: params.tab, personId:person.id])
				return
			} else { 
				results = [ name:person.firstName, tz:tzId ]
			}	

		} catch (InvalidParamException e) {
			errMsg = e.getMessage()
		} catch (DomainUpdateException e) {
			errMsg = e.getMessage()
		} catch (e) {
			log.warn "updateAccount() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = 'An error occurred during the update process'
		}

		if (errMsg) {
			ServiceResults.respondWithError(response, errMsg)
		} else {
			ServiceResults.respondWithSuccess(response, results)
		}
	}

	/** 
	 * Used to clear out person's preferences. User can clear out own or requires permission
	 */
// TODO : JPM 8/31/2015 : Need to test
	def resetPreferences = {
		try {
			Person currentPerson = securityService.getUserLoginPerson()
			Person person = personService.validatePersonAccess(params.user, currentPerson)

			UserLogin userLogin = securityService.getPersonUserLogin(person)
			if (! userLogin) {
				log.error "resetPreferences() Unable to find UserLogin for person $person.id $person"
				ServiceResults.notFound(response)
				return 
			}
			
			// TODO : JPM 5/2015 : Change the way that the delete is occurring
			def prePreference = UserPreference.findAllByUserLogin(userLogin).preferenceCode
			prePreference.each{ preference->
			  def preferenceInstance = UserPreference.findByPreferenceCodeAndUserLogin(preference,userLogin)
				 preferenceInstance.delete()

			}

			userPreferenceService.setPreference("START_PAGE", "Current Dashboard" )
			render person
		} catch (e) {
			ServiceResults.respondWithError(response, e.getMessage())
		}

	}

	/*
	 * The primary controller method to bootstrap the Project Staff administration screen which then 
	 * leverages the loadFilteredStaff method to populate list in an Ajax call
	 * @return The HTML for the controls at the top of the form and the Javascript to load the data.
	 */
	def manageProjectStaff() {
		def (project, loginUser) = controllerService.getProjectAndUserForPage(this, 'ProjectStaffList')
		if (!project) {
			return
		}

		def loginPerson = loginUser.person
		def start = new Date()
		
		def hasShowAllProjectsPerm = RolePermissions.hasPermission("ShowAllProjects")

		def now = TimeUtil.nowGMT()

		List roleTypes = partyRelationshipService.getStaffingRoles()

		// If no role then default it to ALL (0)
		String role = params.role ? params.role : "0"
		
		def moveEventList = []
		
		// set the defaults for the checkboxes

		def assigned = userPreferenceService.getPreference("ShowAssignedStaff") ?: '1'
		def onlyClientStaff = userPreferenceService.getPreference("ShowClientStaff") ?: '1'
		
		def currRole = params.role ? params.role : (userPreferenceService.getPreference("StaffingRole")?:"0")
		def currLoc = userPreferenceService.getPreference("StaffingLocation")?:"All"
		def currPhase = userPreferenceService.getPreference("StaffingPhases")?:"All"
		def currScale = userPreferenceService.getPreference("StaffingScale")?:"6"
		def moveEvents
		def projectId = Project.findById( project.id) ? project.id : 0
		def reqProjects = projectService.getUserProjectsOrderBy(loginUser, hasShowAllProjectsPerm, ProjectStatus.ACTIVE)

		List projects = personService.getAvailableProjects(loginPerson)
	
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')

		return [
			project: project, 
			projects: projects, 
			projectId: project.id, 
			roleTypes: roleTypes, 
			currRole: currRole, 
			editPermission: editPermission,
			assigned: assigned, 
			onlyClientStaff: onlyClientStaff
		]

		log.error "Loading staff list took ${TimeUtil.elapsed(start)}"
	}

	/*
	 * Generates an HTML table of Project Staffing based on a number of filter parameters called by AJAX service.
	 * The list of staff that appear is going to be contingent based on what user is viewing the page. The use-cases are:
	 *    Staff of Owner - see all owner, partner and client staff without limitations (when the Only Assigned is not checked)
	 *    Staff of Partner - ONLY assigned staff to the project
	 *    Staff of Client - ONLY assigned staff of Owner and Partner and All Client Staff without limitation
	 *
	 * @param project - id of project from select, 0 for ALL
	 * @param role - the code for filtering staff list, '0' for all roles
	 * @param assigned - flag if 1 indicates only assigned staff, 0 indicates all
	 * @param onlyClientStaff - flag if 1 indicates only include staff of the client, 0 indicates all available staff
	 * @return HTML table of the data
	 */
	def loadFilteredStaff() {
		if (!controllerService.checkPermission(this, 'ProjectStaffList')) {
			ServiceResults.unauthorized(response)
			return
		}

		UserLogin userLogin = securityService.getUserLogin()
		Person loginPerson = userLogin.person

		//
		// Deal with filter parameters
		//
		String role = params.role ?: 'AUTO'
		Long projectId = NumberUtil.toPositiveLong(params.project, -1)
		if (projectId < 1) {
			render 'Invalid project number was specified'
			return
		}

		Project project = Project.read(projectId)
		if (! project) {
			render 'Specified Project was not found'
			return
		}
		List accessibleProjects = personService.getAvailableProjects(loginPerson)
		if (! accessibleProjects.find {it.id == projectId } ) {
			securityService.reportViolation("attempted to access project staffing for project $project without necessary access rights", userLogin)
			render 'Specified Project was not found'
			return
		}

		def assigned = ( params.containsKey('assigned') && '01'.contains(params.assigned) ? params.assigned : '1' )
		def onlyClientStaff = ( params.containsKey('onlyClientStaff') && '01'.contains(params.onlyClientStaff) ? params.onlyClientStaff : '1' )
		def location = params.location

		def sortableProps = ['fullName', 'company', 'team']

		def orders = ['asc', 'desc']
		
		// code which is used to resolve the bug in TM-2585: 
		// alphasorting is reversed each time when the user checks or unchecks the two filtering checkboxes.
		if (params.firstProp != 'staff') {
			session.setAttribute("Staff_OrderBy", params.orderBy)
			session.setAttribute("Staff_SortOn", params.sortOn)
		} else {
			params.orderBy = session.getAttribute("Staff_OrderBy")?:'asc'
			params.sortOn = session.getAttribute("Staff_SortOn")?:'fullName'
		}
		
		// TODO : JPM 11/2015 : Do not believe the firstProp is used in method loadFilteredStaff
		def paramsMap = [
			sortOn : params.sortOn in sortableProps ? params.sortOn : 'fullName',
			firstProp : params.firstProp, 
			orderBy : params.orderBy in orders ? params.orderBy : 'asc'
		]

		def sortString = "${paramsMap.sortOn} ${paramsMap.orderBy}"
		sortableProps.each {
			sortString = sortString + ', ' + it + ' asc'
		}
		
		// Save the user preferences from the filter
		//userPreferenceService.setPreference("StaffingLocation",location)
		//userPreferenceService.setPreference("StaffingPhases",phase.toString().replace("[", "").replace("]", ""))
		//userPreferenceService.setPreference("StaffingScale",scale)	
		userPreferenceService.setPreference("StaffingRole",role)		
		userPreferenceService.setPreference("ShowClientStaff",onlyClientStaff.toString())
		userPreferenceService.setPreference("ShowAssignedStaff",assigned.toString())

		def hasShowAllProjectPerm = RolePermissions.hasPermission("ShowAllProjects")
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')
		
		List moveEvents 
		List projectList = [project]
		
		// Find all Events for one or more Projects and the Staffing for the projects
		// Limit the list of events to those that completed within the past 30 days or have no completion and have started in the past 90 days
		if (projectList.size() > 0) {
			moveEvents = MoveEvent.findAll("from MoveEvent m where project in (:project) order by m.project.name , m.name asc",[project:projectList])
			def now = TimeUtil.nowGMT()
		
			moveEvents = moveEvents.findAll {
				def eventTimes = it.eventTimes
				if ( eventTimes 
					&& ( 
						( eventTimes.completion && eventTimes.completion > now.minus(30) ) ||
						( ! eventTimes.completion && eventTimes.start && eventTimes.start > now.minus(90) )
					)
				) {
					return true
				}
				return false
			}
		}
		
		String projects = "${project.id}"
		StringBuffer companies = new StringBuffer("${project.client.id}")
		if (onlyClientStaff == '0') {
			// Add the owner company and any partner companies associated with the project
			companies.append(", ${project.owner.id}")
			def projectPartners = projectService.getPartners(project)
			if (projectPartners) {
				companies.append(',' + projectPartners*.id.join(','))
			}
		}

		// Get the list of staff that should be displayed based on strictly assigned or available staff
		List staff
		if (assigned == '1') {
			staff = projectService.getAssignedStaff(project)
		} else {
			staff = projectService.getAssignableStaff(project, loginPerson)
		}

		List staffList

		// If there is no staff then there is no need to perform the query
		if (staff) {
			// The staffIds will help filter down who can appear in the list
			String staffIds = staff.id.join(',')

			def query = new StringBuffer("""
				SELECT * FROM (
					SELECT pr.party_id_to_id AS personId, 
						p.last_name AS lastName,
						CONCAT( IFNULL(p.first_name,''), IF(p.first_name IS NULL, '', ' '), 
							IFNULL(p.middle_name,''), IF(p.middle_name IS NULL, '', ' '),
							COALESCE(p.last_name, '')
						) AS fullName, 
						company.name AS company, 
						pr.role_type_code_to_id AS role, 
						SUBSTRING(rt.description, INSTR(rt.description, ":")+2) AS team, 
						pr2.party_id_to_id IS NOT NULL AS project, 
						IFNULL(CONVERT(GROUP_CONCAT(mes.move_event_id) USING 'utf8'), 0) AS moveEvents, 
						IFNULL(CONVERT(GROUP_CONCAT(DATE_FORMAT(ed.exception_day, '%Y-%m-%d')) USING 'utf8'),'') AS unavailableDates 
					FROM tdstm.party_relationship pr 
						LEFT OUTER JOIN person p ON p.person_id = pr.party_id_to_id and p.active='Y'
						LEFT OUTER JOIN exception_dates ed ON ed.person_id = p.person_id 
						LEFT OUTER JOIN party_group company ON company.party_group_id = pr.party_id_from_id 
						LEFT OUTER JOIN role_type rt ON rt.role_type_code = pr.role_type_code_to_id 
						LEFT OUTER JOIN party_relationship pr2 ON pr2.party_id_to_id = pr.party_id_to_id 
							AND pr2.role_type_code_to_id = pr.role_type_code_to_id 
							AND pr2.party_id_from_id IN (${projects}) 
							AND pr2.role_type_code_from_id = 'PROJECT'
						LEFT OUTER JOIN move_event_staff mes ON mes.person_id = p.person_id 
							AND mes.role_id = pr.role_type_code_to_id 
					WHERE pr.role_type_code_from_id in ('COMPANY') 
						AND pr.party_relationship_type_id in ('STAFF') 
						AND pr.party_id_from_id IN (${companies}) 
						AND p.active = 'Y'
						AND pr.party_id_to_id IN (${staffIds})
					GROUP BY role, personId 
					ORDER BY fullName ASC 
				) AS companyStaff 
				WHERE 1=1 
			""")
			
			// Filter on the role (aka teams) if there is a filter for it
			if (role != '0')
				query.append("AND companyStaff.role = '${role}' ")

			query.append(" ORDER BY fullName ASC, team ASC ")
			// log.debug "loadFilteredStaff() query=$query"
			staffList = jdbcTemplate.queryForList(query.toString())
		}

		render(
			template: "projectStaffTable", 
			model:[
				staffList:staffList, 
				moveEventList:retrieveBundleHeader(moveEvents),
				projectId:projectId, 
				project:project, 
				editPermission:editPermission,
				sortOn: params.sortOn, 
				firstProp: params.firstProp, 
				orderBy: params.orderBy != 'asc' ? 'asc' :'desc']
		)	

	}

	/*
	 * An internal function used to retrieve staffing for specified project, roles, etc.
	 *@param projectList - array of Projects to get staffing for
	 *@param role - type of role to filter staff list
	 *@param scale - duration in month  to filter staff list
	 *@param location - location to filter staff list
	 *TODO : JPM 11/2015 : There is no reference to PersonController.getStaffList method so it should be removed
	 */
	def retrieveStaffList(def projectList, def role, def scale, def location,def assigned,def paramsMap){
		if (!controllerService.checkPermission(this, 'ProjectStaffList')) {
			return
		}

		def sortOn = paramsMap.sortOn ?:"fullName"
		def orderBy = paramsMap.orderBy?:'asc'
		def firstProp = paramsMap.firstProp ? (paramsMap.firstProp && paramsMap.firstProp == 'company' ? '' :paramsMap.firstProp) : 'staff'
		
		// Adding Company TDS and Project partner in all companies list
		Party tdsCompany = PartyGroup.findByName('TDS')
		def partner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' \
				and p.partyIdFrom in ( :projects ) and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ",[projects:projectList])?.partyIdToId
		def companies = projectList.client
		companies << tdsCompany
		
		
		def staffRelations = partyRelationshipService.getAllCompaniesStaff( companies )
		def c=staffRelations.size()
		
		if (role != '0') {
			// Filter out only the roles requested
			staffRelations = staffRelations.findAll { it.role.id == role }
		}
		
		def staffList = []
		
		def projectStaff =PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' "+
			"and p.partyIdFrom  in (:projects) and p.roleTypeCodeFrom = 'PROJECT' ",[projects:projectList])
		
		staffRelations.each { staff -> 
			// Add additional properties that aren't part of the Staff Relationships
			// TODO - WHAT ARE THIS FIELDS???
			def person = Person.read(staff.staff.id)
			if(person.active=='Y' ){
				def hasAssociation =  projectStaff.find{it.partyIdTo.id == staff.staff.id && it.roleTypeCodeTo.id == staff.role.id }
				if (assigned=="0" || (assigned=="1" && hasAssociation)){
					staff.roleId = 'roleId?'	// I believe that this should be the ROLE code (e.g. MOVE_MGR)
					staff.staffProject = 'staffProj.name?'	// This is the name of the project.
		
					staffList << staff
				}
			}
		}
		
		staffList.sort{ a,b->
			if(orderBy == 'asc'){
				firstProp ? (a."${firstProp}"?."${sortOn}" <=> b."${firstProp}"."${sortOn}") : ((a."${sortOn}").toString() <=> b."${sortOn}".toString())
			} else {
				firstProp ? (b."${firstProp}"."${sortOn}" <=> a."${firstProp}"?."${sortOn}") : (b."${sortOn}".toString() <=> a."${sortOn}".toString())
			}
		}
		
		return staffList
		
	}
	
	/*
	 *@param tab is name of template where it need to be redirect
	 *@param person Id is id of person
	 *@return NA
	 */
	def loadGeneral() {
		if (!controllerService.checkPermission(this, 'ProjectStaffShow')) {
			ServiceResults.unauthorized(response)
			return
		}

		log.debug "loadGeneral() class: ${params.personId.class} value: ${params.personId}"
		def tab = params.tab ?: 'generalInfoShow'
		def person = Person.get(params.personId)
		def blackOutdays = person.blackOutDates?.sort{it.exceptionDay}
		def subject = SecurityUtils.subject
		def company = partyRelationshipService.getStaffCompany( person )
		def companyProject = Project.findByClient( company )
		def personFunctions = []
		personFunctions = partyRelationshipService.getCompanyStaffFunctions(company.id, person.id)
			
		def availabaleFunctions = partyRelationshipService.getStaffingRoles()
		
		def isProjMgr = false
		if( subject.hasRole("PROJ_MGR")){
			isProjMgr = true
		}
		def partyGroupList = partyRelationshipService.associatedCompanies(securityService.getUserLoginPerson())
		
		render(template:tab ,model:[person:person, company:company, personFunctions:personFunctions, availabaleFunctions:availabaleFunctions, 
			sizeOfassigned:(personFunctions.size()+1), blackOutdays:blackOutdays, isProjMgr:isProjMgr, partyGroupList:partyGroupList])
			
	}
	
	/*
	 *To get headers of event at project staff table 
	 *@param moveEvents list of moveEvent for selected project
	 *@return MAP of bundle header containing projectName ,event name, start time and event id
	 */
	private def retrieveBundleHeader(moveEvents) {
		// TODO : JPM 5/2015 : Need to add security controls
		def project = securityService.getUserCurrentProject()
		def moveEventList = []
		def bundleTimeformatter = new SimpleDateFormat("MMM dd")
		def moveEventDateFormatter = new SimpleDateFormat("yyyy-MM-dd")
		if (project) {
			def bundleStartDate = ""
			def personAssignedToME = []
			moveEvents.each{ moveEvent->
				def moveMap = new HashMap()
				def moveBundle = moveEvent?.moveBundles
				def startDate = moveBundle.startTime.sort()
				def eventTimes = moveEvent.eventTimes
				startDate?.removeAll([null])
				if (startDate.size()>0) {
					if (startDate[0]) {
						bundleStartDate = bundleTimeformatter.format(startDate[0])
					}
				}
				moveMap.put("project", moveEvent.project.name)
				moveMap.put("name", moveEvent.name)
				moveMap.put("startTime", bundleStartDate)
				moveMap.put("startDate", (eventTimes.start ? moveEventDateFormatter.format(moveEvent.eventTimes.start) : '') )
				moveMap.put("id", moveEvent.id)
				
				moveEventList << moveMap
				
			}
		}
		return moveEventList
	}
	
	/*
	 * Used to save event for staff in moveEvent staff 
	 * @param id as composite id contains personId , MoveEventId and roleType id with separated of '-'
	 * @return if updated successful return true else return false
	 */
	def saveEventStaff() {
		// Validates the user is logged in.
		if (!controllerService.checkPermission(this, 'EditProjectStaff')) {
			ServiceResults.unauthorized(response)
			return
		}
		try{
			String message = personService.assignToProject(request.JSON.personId, request.JSON.eventId, request.JSON.roleType, NumberUtil.toInteger(request.JSON.val))
			def flag = message.size() == 0
			render(ServiceResults.success(['flag':flag, 'message':message]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (InvalidParamException e) {
			render(ServiceResults.fail(e.getMessage()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
		
	}
	
	/**
	 * This action is used to handle ajax request and to delete current user's individual preferences except 'Current Dashboard'
	 * @param prefCode : Preference Code that is requested for being deleted
	 * @return : boolean
	 */
	def removeUserPreference() {
		// TODO : JPM 5/2015 : Improve removeUserPreference - validate it was successful, return SecurityService.success
		def prefCode = params.prefCode
		if (prefCode != "Current Dashboard")
			userPreferenceService.removePreference(prefCode)
			
		render true
	}
	
	/**
	 * This action is used to display Current logged user's Preferences with preference code (converted to comprehensive words)
	 * with their corresponding value
	 * @param N/A : 
	 * @return : A Map containing key as preference code and value as map'svalue.
	 */
	def editPreference() {
		def loggedUser = securityService.getUserLogin()
		def prefs = UserPreference.findAllByUserLogin( loggedUser ,[sort:"preferenceCode"])
		def prefMap = [:]
		def labelMap = ["CONSOLE_TEAM_TYPE" : "Console Team Type", "SUPER_CONSOLE_REFRESH" : "Console Refresh Time",
			"CART_TRACKING_REFRESH" : "Cart tarcking Refresh Time", "BULK_WARNING" : "Bulk Warning",
			"DASHBOARD_REFRESH" : "Dashboard Refresh Time", "CURR_TZ" : "Time Zone","CURR_POWER_TYPE" : "Power Type",
			"START_PAGE" : "Welcome Page", "StaffingRole" : "Default Project Staffing Role",
			"StaffingLocation" : "Default Project Staffing Location", "StaffingPhases" : "Default Project Staffing Phase",
			"StaffingScale" : "Default Project Staffing Scale", "preference" : "Preference", "DraggableRack" : "Draggable Rack",
			"PMO_COLUMN1" : "PMO Column 1 Filter", "PMO_COLUMN2" : "PMO Column 2 Filter", "PMO_COLUMN3" : "PMO Column 3 Filter",
			"PMO_COLUMN4" : "PMO Column 4 Filter", "ShowAddIcons" : "Rack Add Icons", "MY_TASK":"My Task Refresh Time"
		]
		prefs.each { pref->
			switch( pref.preferenceCode ) {
				case "MOVE_EVENT" :
					prefMap.put((pref.preferenceCode), "Event / "+MoveEvent.get(pref.value).name)
					break;
				
				case "CURR_PROJ" :
					prefMap.put((pref.preferenceCode), "Project / "+Project.get(pref.value).name)
					break;
				
				case "CURR_BUNDLE" :
					prefMap.put((pref.preferenceCode), "Bundle / "+MoveBundle.get(pref.value).name)
					break;
				
				case "PARTYGROUP" :
					prefMap.put((pref.preferenceCode), "Company / "+ (!pref.value.equalsIgnoreCase("All")  ? PartyGroup.get(pref.value).name : 'All'))
					break;
				
				case "CURR_ROOM" :
					prefMap.put((pref.preferenceCode), "Room / "+Room.get(pref.value).roomName)
					break;
				
				case "StaffingRole" :
					def role = pref.value == "0" ? "All" : RoleType.get(pref.value).description
					prefMap.put((pref.preferenceCode), "Default Project Staffing Role / "+role.substring(role.lastIndexOf(':') +1))
					break;
					
				case "AUDIT_VIEW" :
					def value = pref.value == "0" ? "False" : "True"
					prefMap.put((pref.preferenceCode), "Room Audit View / "+value)
					break;
					
				case "JUST_REMAINING" :
					def value = pref.value == "0" ? "False" : "True"
					prefMap.put((pref.preferenceCode), "Just Remaining Check / "+value)
					break;
				
				default :
					prefMap.put((pref.preferenceCode), (labelMap[pref.preferenceCode] ?: pref.preferenceCode )+" / "+ pref.value)
					break;
			}
		}
		
		
		render(template:"showPreference",model:[prefMap:prefMap.sort{it.value}])
	}
	
	/**
	 * This action is Used to populate CompareOrMergePerson dialog.
	 * @param : ids[] is array of 2 id which user want to compare or merge
	 * @return : all column list , person list and userlogin list which we are display at client side
	 */
	def compareOrMerge() {
		if (!controllerService.checkPermission(this, 'PersonEditView')) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def ids = params.list("ids[]")
		def personsMap = [:]
		def userLogins= []
		ids.each{
			def id = it.isLong()?Long.parseLong(it):null
			def person = id?Person.get(id):null
			if(person){
				personsMap << [(person) : partyRelationshipService.getStaffCompany( person )?.id]
				def userLogin = UserLogin.findByPerson(person)
				userLogins << userLogin
			}
		}
		
		// Defined a HashMap as 'columnList' where key is displaying label and value is property of label for Person .
		def columnList =  [ 'Merge To':'','First Name': 'firstName', 'Last Name': 'lastName', 'Nick Name': 'nickName' , 'Active':'active','Title':'title',
							'Email':'email', 'Department':'department', 'Location':'location', 'State Prov':'stateProv',
							'Country':'country', 'Work Phone':'workPhone','Mobile Phone':'mobilePhone',
							'Model Score':'modelScore','Model Score Bonus':'modelScoreBonus', 'Person Image URL':'personImageURL', 
							'KeyWords':'keyWords', 'Tds Note':'tdsNote','Tds Link':'tdsLink', 'Staff Type':'staffType',
							'TravelOK':'travelOK', 'Black Out Dates':'blackOutDates', 'Roles':''
						  ]
		
		// Defined a HashMap as 'columnList' where key is displaying label and value is property of label for UserLogin .
		def loginInfoColumns = ['Username':'username', 'Active':'active', 'Created Date':'createdDate', 'Last Login':'lastLogin',
								'Last Page':'lastPage', 'Expiry Date':'expiryDate'
							   ]
		
		render(template:"compareOrMerge", model:[personsMap:personsMap, columnList:columnList, loginInfoColumns:loginInfoColumns,
					userLogins:userLogins])
	}
	
	/**
	 * This action is used  to merge Person 
	 * @param : toId is requested id of person into which second person will get merge
	 * @param : fromId is requested id of person which will be merged
	 * @return : Appropriate message after merging
	 */
	
	def mergePerson() {
		if (!controllerService.checkPermission(this, 'PersonEditView')) {
			ServiceResults.unauthorized(response)
			return
		}

		def toPerson = Person.get(params.toId)
		def fromPersons = params.list("fromId[]")
		def personMerged = []
		def msg = ""
		
		toPerson.properties = params
		
		if (!toPerson.save(flush:true)) {
			toPerson.errors.allErrors.each{ println it }
		}
		fromPersons.each {
			def fromPerson = Person.get(it)
			personMerged += personService.mergePerson(fromPerson, toPerson)
		}
		msg += "${personMerged.size() ? WebUtil.listAsMultiValueString(personMerged) : 'None of Person '} Merged to ${toPerson}"
		render msg
	}

	/*
	 * Ajax service used to add the staff association to a project
	 * @params params.projectId
	 * @params params.personId
	 * @return JSON response 
	 */
	def addProjectStaff() {
		String userMsg
		try {
			UserLogin userLogin = securityService.getUserLogin()

			// Make the service call - note that the Permission is checked within the service
			personService.addToProject(userLogin, params.projectId, params.personId)

			ServiceResults.respondWithSuccess(response)

		} catch (DomainUpdateException e) {
			userMsg = e.getMessage()
		} catch (InvalidParamException e) {
			userMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			userMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			userMsg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("addProjectTeam()", e)
			ServiceResults.respondWithError(response, ['An error occurred while trying to add the person to the project', e.getMessage()])
		}

		if (userMsg) {
			ServiceResults.respondWithError(response, userMsg)
		}
	}

	/*
	 * Ajax service used to remove the staff association to a project and events and teams
	 * @params params.projectId
	 * @params params.personId
	 * @return JSON response 
	 */
	def removeProjectStaff() {
		String userMsg
		UserLogin userLogin
		try {
			userLogin = securityService.getUserLogin()

			// Make the service call - note that the Permission is checked within the service
			personService.removeFromProject(userLogin, params.projectId, params.personId)

			ServiceResults.respondWithSuccess(response)

		} catch (DomainUpdateException e) {
			userMsg = e.getMessage()
		} catch (InvalidParamException e) {
			userMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			userMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			userMsg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("removeProjectTeam()", e)
			ServiceResults.respondWithError(response, ['An error occurred while trying to unassign the person from the project', e.getMessage()])
		}

		if (userMsg) {
			log.debug "removeProjectTeam($userLogin, $params.projectId, $params.personId, $params.teamCode) failed - $userMsg"
			ServiceResults.respondWithError(response, userMsg)
		}
	}

	/*
	 * Ajax service used to add the staff association to a project for a given team code
	 * @params params.projectId
	 * @params params.personId
	 * @params params.teamCode
	 * @return JSON response 
	 */
	def addProjectTeam() {
		String userMsg
		try {
			UserLogin userLogin = securityService.getUserLogin()

			// Make the service call - note that the Permission is checked within the service
			personService.addToProjectTeam(userLogin, params.projectId, params.personId, params.teamCode)

			ServiceResults.respondWithSuccess(response)

		} catch (DomainUpdateException e) {
			userMsg = e.getMessage()
		} catch (InvalidParamException e) {
			userMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			userMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			userMsg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("addProjectTeam()", e)
			ServiceResults.respondWithError(response, ['An error occurred while trying to add the person to the project', e.getMessage()])
		}

		if (userMsg) {
			ServiceResults.respondWithError(response, userMsg)
		}
	}

	/*
	 * Ajax service used to remove the staff association to a project and events for a given team code
	 * @params params.projectId
	 * @params params.personId
	 * @params params.teamCode
	 * @return JSON response 
	 */
	def removeProjectTeam() {
		String userMsg
		UserLogin userLogin
		try {
			userLogin = securityService.getUserLogin()

			// Make the service call - note that the Permission is checked within the service
			personService.removeFromProjectTeam(userLogin, params.projectId, params.personId, params.teamCode)

			ServiceResults.respondWithSuccess(response)

		} catch (DomainUpdateException e) {
			userMsg = e.getMessage()
		} catch (InvalidParamException e) {
			userMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			userMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			userMsg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("removeProjectTeam()", e)
			ServiceResults.respondWithError(response, ['An error occurred while trying to unassign the person from the project', e.getMessage()])
		}

		if (userMsg) {
			log.debug "removeProjectTeam($userLogin, $params.projectId, $params.personId, $params.teamCode) failed - $userMsg"
			ServiceResults.respondWithError(response, userMsg)
		}
	}

	/*
	 * Method to add Staff to project through Ajax Overlay 
	 * @
	 */
	def addEventStaff() {
		String userMsg
		try {
			UserLogin userLogin = securityService.getUserLogin()

			// Make the service call - note that the Permission is checked within the service
			personService.addToEvent(userLogin, params.projectId, params.eventId, params.personId, params.teamCode)
			ServiceResults.respondWithSuccess(response)

		} catch (DomainUpdateException e) {
			userMsg = e.getMessage()
		} catch (InvalidParamException e) {
			userMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			userMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			userMsg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("addEventStaff()", e)
			ServiceResults.respondWithError(response, ['An error occurred while trying to add the person to the event', e.getMessage()])
		}

		if (userMsg) {
			ServiceResults.respondWithError(response, userMsg)
		}
	}

	/*
	 * Method to add Staff to project through Ajax Overlay 
	 * @
	 */
	def removeEventStaff() {
		String userMsg
		try {
			UserLogin userLogin = securityService.getUserLogin()

			// Make the service call - note that the Permission is checked within the service
			personService.removeFromEvent(userLogin, params.projectId, params.eventId, params.personId, params.teamCode)
			ServiceResults.respondWithSuccess(response)

		} catch (DomainUpdateException e) {
			userMsg = e.getMessage()
		} catch (InvalidParamException e) {
			userMsg = e.getMessage()
		} catch (InvalidRequestException e) {
			userMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			userMsg = e.getMessage()
		} catch (e) {
			log.error ExceptionUtil.messageWithStacktrace("removeEventStaff()", e)
			ServiceResults.respondWithError(response, ['An error occurred while trying to unassign the person from the event', e.getMessage()])
		}

		if (userMsg) {
			ServiceResults.respondWithError(response, userMsg)
		}
	}	

}
