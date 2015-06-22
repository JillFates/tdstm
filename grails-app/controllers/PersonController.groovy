import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha1Hash
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
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder

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
		
		def partyGroupList = PartyGroup.findAllByPartyType( PartyType.read("COMPANY")).sort{it.name}
		
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
		
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = personInstanceList?.collect {
			[ cell: ['<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.firstname+'</a>', 
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.middlename+'</a>', 
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.lastname+'</a>', 
			genCreateEditLink(haveCreateUserLoginPerm, haveEditUserLoginPerm, createUrl, editUrl, it), 
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
	private def genCreateEditLink(haveCreateUserLoginPerm, haveEditUserLoginPerm, createUrl, editUrl, person) {
		def element = ""
		if (person.userLoginId) {
			if (haveEditUserLoginPerm) {
				element = '<a href="' + editUrl + '/' + person.userLoginId + '">' + person.userLogin + '</a>'
			} else {
				element = person.userLogin
			}
		} else {
			if (haveCreateUserLoginPerm) {
				element = '<a href="' + createUrl + '/' + person.personId + '">' + person.userLogin + '</a>'
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
		def companyParty

		if (isAjaxCall) {
			// First try to see if the person already exists for the current project
			def project = securityService.getUserCurrentProject()
			companyId = project.client.id
		} else {
			companyId = params.company
		}
		if ( companyId != "" ) {
			companyParty = Party.findById( companyId )
		}
		def errMsg
		def isExistingPerson = false 
		def name
		def person

		// Look to allow easy breakout for exceptions
		while(true) {
			if (! companyParty) {
				errMsg = 'Unable to locate proper company to associate person to'
				break;
			}

			// Get list of all staff for the company and then try to find the individual so that we don't duplicate
			// the creation.
			def personList = partyRelationshipService.getCompanyStaff(companyId)
			person = personList.find {
				// Find person using case-insensitive search
				StringUtils.equalsIgnoreCase(it.firstName, params.firstName) &&
				( ( StringUtils.isEmpty(params.lastName) && StringUtils.isEmpty(it.lastName) ) ||  StringUtils.equalsIgnoreCase(it.lastName, params.lastName) ) &&
				( ( StringUtils.isEmpty(params.middleName) && StringUtils.isEmpty(it.middleName) ) ||  StringUtils.equalsIgnoreCase(it.middleName, params.middleName) )
			}

			isExistingPerson = person ? true : false
			if (isExistingPerson ) {
				errMsg = 'A person with that name already exists'
				break
			} else {
				// Create the person and relationship appropriately

				person = new Person( params )
				if ( !person.hasErrors() && person.save() ) {			
					//Receiving added functions		
					def functions = params.list("function")
					def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", person, "STAFF" )
					if(functions){
						userPreferenceService.setUserRoles(functions, person.id)
						def staffCompany = partyRelationshipService.getStaffCompany(person)
						//Adding requested functions to Person .
						partyRelationshipService.updateStaffFunctions(staffCompany, person, functions, 'STAFF')
					}
					if (! isAjaxCall) {
						// Just add a message for the form submission to know that the person was created
						flash.message = "A record for ${person.toString()} was created"
					}
				} else {
					errMsg = GormUtil.allErrorsString(person)
					break
				}
			}

			name = person.toString()
			break
		}
		
		if (errMsg)
			log.info "save() had errors: $errMsg"

		if (isAjaxCall) {
			def map = errMsg ? [errMsg : errMsg] : [ id: person.id, name:person.lastNameFirst, isExistingPerson:isExistingPerson, fieldName:params.fieldName]
			render map as JSON
		} else {
			if (errMsg) 
				flash.message = errMsg
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
		Person person = validatePersonAccess(params.id)
		if (!person) {
			return
		}
	
		def personInstance = Person.get(params.id)
		def ret = []
		params.travelOK == "1" ? params : (params.travelOK = 0)
		
		if(!personInstance.staffType && !params.staffType)
			params.staffType = 'Hourly'
		
		personInstance.properties = params
		def personId
		if ( personInstance.validate() && personInstance.save(flush:true) ) {
			personId = personInstance.id
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
			//getSession().setAttribute( "LOGIN_PERSON", ['name':personInstance.firstName, "id":personInstance.id ])
			def userLogin = UserLogin.findByPerson( personInstance )
				if(userLogin){
				if(params.newPassword){
					def password = params.newPassword
					
					if(password != null)
						userLogin.password = new Sha1Hash(params.newPassword).toHex()
				}
					
				if(params.expiryDate && params.expiryDate != "null"){
					def expiryDate = params.expiryDate
					userLogin.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
				}
				userLogin.active = personInstance.active
				if(!userLogin.save()){
					userLogin.errors.allErrors.each{println it}
				}
			}
			def functions = params.list("function")
			if(params.manageFuncs != '0' || functions){
				def staffCompany = partyRelationshipService.getStaffCompany(personInstance)
				def companyProject = Project.findByClient(staffCompany)
				partyRelationshipService.updateStaffFunctions(staffCompany, personInstance, functions, 'STAFF')
				if(companyProject)
					partyRelationshipService.updateStaffFunctions(companyProject, personInstance,functions, "PROJ_STAFF")
			}

			def personExpDates = params.list("availability")
			def expFormatter = new SimpleDateFormat("MM/dd/yyyy")
			personExpDates = personExpDates.collect{GormUtil.convertInToGMT(expFormatter.parse(it), tzId)}
			def existingExp = ExceptionDates.findAllByPerson(personInstance)
			
			if(personExpDates){
				ExceptionDates.executeUpdate("delete from ExceptionDates where person = :person and exceptionDay not in (:dates) ",[person:personInstance, dates:personExpDates])
				personExpDates.each { presentExpDate->
					def exp = ExceptionDates.findByExceptionDayAndPerson(presentExpDate, personInstance)
					if(!exp){
						def expDates = new ExceptionDates()
						expDates.exceptionDay = presentExpDate
						expDates.person = personInstance
						expDates.save(flush:true)
					}
				}
			} else {
				ExceptionDates.executeUpdate("delete from ExceptionDates where person = :person",[person:personInstance])
			}
			
			userPreferenceService.setPreference( "CURR_TZ", params.timeZone )
			userPreferenceService.setPreference( "CURR_POWER_TYPE", params.powerType )
			userPreferenceService.loadPreferences("CURR_TZ")
			userPreferenceService.setPreference("START_PAGE", params.startPage )
			userPreferenceService.loadPreferences("START_PAGE")
			
		} else {

			// TODO : Error handling - this doesn't report to the user that there was any error
			log.warn "updatePerson() unable to save $personInstance due to: " + GormUtil.allErrorsString(personInstance)

		}
		if (params.tab) {
			forward( action:'loadGeneral', params:[tab: params.tab, personId:personId])
		} else { 
			ret << [ name:personInstance.firstName, tz:getSession().getAttribute( "CURR_TZ" )?.CURR_TZ ]
			render  ret as JSON
		}
	}

	/**
	 * Will return person details for a given personId as JSON
	 * @param  params.id - the person id
	 * @return person details as JSON
	 */
	def retrievePersonDetails() {
		Person person = validatePersonAccess(params.id)
		if (!person) {
			return
		}

		def userLogin = UserLogin.findByPerson( person )
		
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ

		// TODO : JPM 5/2015 : Move the date formating into a reusable class
		def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
		def expiryDate = userLogin.expiryDate ? formatter.format(GormUtil.convertInToUserTZ(userLogin.expiryDate,tzId)) : ""
		
		def personDetails = [person:person, expiryDate: expiryDate, isLocal:userLogin.isLocal]

		render personDetails as JSON
	}

	/**
	 * Used by controller methods to validate that the user can access a person and will respond with appropriate 
	 * HTTP responses based on access constraints (e.g. Unauthorized or Not Found)
	 * @param personId - the id of the person to access
	 * @return Person - the person if can access or null
	 */
	private Person validatePersonAccess(String personId) {
		def currentPerson = securityService.getUserLoginPerson()
		if (!currentPerson) {
			ServiceResults.unauthorized(response)
			return null
		}

		// Make sure we have a legit id
		if (! personId || ! personId.isNumber() || ! personId.isLong() ) {
			render Service.Results.invalidParams('Invalid or missing person id requested') as JSON
			return
		}
		def editSelf = ( personId == currentPerson.id.toString() )

		// If not edit own account, the user must have privilege to edit the account
		if ( ! editSelf && ! controllerService.checkPermission(this, 'PersonEditView')) {
			ServiceResults.unauthorized(response)
			return null
		}

		if (! editSelf) {
			// TODO : JPM 5/2015 : Need to make sure showing/editing someone that the user has access to
		}

		def person = Person.findById( personId  )
		if (! person) {
			ServiceResults.notFound()
			return null
		}

		return person
	}


	/**
	 * Check if the user inputed password is correct, and if so call the update method
	 * @author : Ross Macfarlane 
	 * @param  : person id and input password
	 * @return : pass:"no" or the return of the update method
	 */
	def checkPassword() {
		if (params.oldPassword == "")
			return updatePerson()

		def password = params.newPassword ?: ''
			
		def userLogin = UserLogin.findByPerson(Person.findById(params.id))
		if (securityService.validPassword(userLogin.username, params.newPassword)) {
			def truePassword = userLogin.password
			// TODO : JPM 5/2015 : Password encryption should be IN the securityService and NO WHERE ELSE. Should have passwordMatch method or something
			def passwordInput = new Sha1Hash(params.oldPassword).toHex()
			
			if (truePassword.equals(passwordInput)) {
				// TODO : JPM 5/2015 : The checkPassword and updatePerson functions are screwed up. The save button should be calling updatePerson directly
				return updatePerson()
			}
		// TODO : JPM 5/2015 : Method should be returning standard ServiceResponse data	
			def ret = []
			ret << [pass:"no"]
			render  ret as JSON
		} else {
			def ret = []
			ret << [pass:"invalid"]
			render ret as JSON
		}
	}

	/** 
	 * Used to clear out person's preferences. User can clear out own or requires permission
	 */
	def resetPreferences = {
		Person person = validatePersonAccess(params.user)
		if (!person) {
			return
		}

		UserLogin userLogin = UserLogin.findByPerson(person)
		if (! userLogin) {
			log.error "resetPreferences() Unable to find UserLogin for person $person.id $person"
			ServiceResults.notFound()
			return 
		}
		
		// TODO : JPM 5/2015 : Change the way that the delete is occurring
		def prePreference = UserPreference.findAllByUserLogin(userLogin).preferenceCode
		prePreference.each{ preference->
		  def preferenceInstance = UserPreference.findByPreferenceCodeAndUserLogin(preference,userLogin)
			 preferenceInstance.delete(flush:true)
		}

		userPreferenceService.setPreference("START_PAGE", "Current Dashboard" )
		render person
	}

	/*
	 * Redirect to project staff page with relevant data
	 * @ returns - staff list.
	 * 
	 */
	def manageProjectStaff() {
		if (!controllerService.checkPermission(this, 'EditProjectStaff')) {
			return
		}

		def start = new Date()
		
		def hasShowAllProjectsPerm = RolePermissions.hasPermission("ShowAllProjects")
		def hasEditTdsPerm = RolePermissions.hasPermission("EditTDSPerson")
		def now = TimeUtil.nowGMT()
		def projects = []
		
		def project = securityService.getUserCurrentProject()
		def roleTypes = partyRelationshipService.getStaffingRoles()
		def role = params.role ? params.role : "0"
		def moveEventList = []
		
		// set the defaults for the checkboxes
		def assigned = userPreferenceService.getPreference("ShowAssignedStaff") ?: '0'
		def onlyClientStaff = userPreferenceService.getPreference("ShowClientStaff") ?: '0'
		
		def currRole = userPreferenceService.getPreference("StaffingRole")?:"0"
		def currLoc = userPreferenceService.getPreference("StaffingLocation")?:"All"
		def currPhase = userPreferenceService.getPreference("StaffingPhases")?:"All"
		def currScale = userPreferenceService.getPreference("StaffingScale")?:"6"
		def moveEvents
		def projectId = Project.findById( project.id) ? project.id : 0
		def loginPerson = securityService.getUserLoginPerson()
		def loginUser = securityService.getUserLogin()
		def reqProjects = projectService.getUserProjectsOrderBy(loginUser, hasShowAllProjectsPerm, ProjectStatus.ACTIVE)

		if (projectId == 0) {
			projects = reqProjects
		}else{
			// Add only the indicated project if exists and based on user's associate to the project
			project = Project.read( projectId )
			if (project) {
				if ( hasShowAllProjectsPerm ) {
					projects << project
				} else {
					// Lets make sure that the user has access to it (is assoicated with the project)
					def staffProjectRoles = partyRelationshipService.getProjectStaffFunctions(projectId, loginPerson.id)
					if (staffProjectRoles.size() > 0) {
						projects << project
					}
				}
			} else {
				log.error("Didn't find project $projectId")
			}
		}
		if (projectId == 0) {
			moveEvents = MoveEvent.findAll("from MoveEvent m where project in (:project) order by m.project.name , m.name asc",[project:(projects?:project)])
		} else {
			project = Project.read(projectId)
			moveEvents = MoveEvent.findAll("from MoveEvent m where project =:project order by m.project.name , m.name asc",[project:project])
		}
		
		def companies = new StringBuffer('0')
		def projectList = new StringBuffer('0')
		projects.each {
			companies.append(",${it.client.id}")
			projectList.append(",${it.id}")
		}
		// if the "only client staff" button is not checked, show TDS employees as well
		if (onlyClientStaff == '0')
			companies.append(",18")
		
		// if the user doesn't have permission to edit TDS employees, remove TDS from the companies list
		if( ! hasEditTdsPerm ) {
			def tdsIndex = companies.indexOf("18")
			if(tdsIndex != -1)
				companies.replace(tdsIndex, tdsIndex+1, "0")
		}
		

		def staffList = projectService.getStaffList(assigned, currRole, projectList, companies, 'fullName ASC, team ASC ')
		
		// Limit the events to today-30 days and newer (ie. don't show events over a month ago) 
		moveEvents = moveEvents.findAll{it.eventTimes.start && it.eventTimes.start > new Date().minus(30)}
		def paramsMap = [sortOn : 'fullName', firstProp : 'staff', orderBy : 'asc']
		
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')
		return [projects:reqProjects, projectId:project.id, roleTypes:roleTypes, staffList:staffList,
			moveEventList:retrieveBundleHeader( moveEvents ), currRole:currRole, currLoc:currLoc,
			currPhase:currPhase, currScale:currScale, project:project, editPermission:editPermission,
			assigned:assigned, onlyClientStaff:onlyClientStaff]
		log.error "Loading staff list took ${TimeUtil.elapsed(start)}"
	}
	
	/*
	 * Generates an HTML table of Project Staffing based on a number of filters
	 *
	 * @param Integer projectId - id of project from select, 0 for ALL
	 * @param String roletype - of role to filter staff list, '0' for all roles
	 * @param Integer scale - duration in month  to filter staff list (1,2,3,6)
	 * @param location - location to filter staff list
	 * @return HTML 
	 */
	def loadFilteredStaff() {
		if (!controllerService.checkPermission(this, 'ProjectStaffList')) {
			ServiceResults.unauthorized(response)
			return
		}

		def role = request.JSON.role ?: 'AUTO'
		def projectId = (request.JSON.project.isNumber()) ? (request.JSON.project.toLong()) : (0)
		def scale = request.JSON.scale
		def location = request.JSON.location
		def phase = request.JSON["phaseArr[]"]
		def assigned = request.JSON.assigned ? '1' : '0'
		def onlyClientStaff = request.JSON.onlyClientStaff ? '1' : '0'
		def loginPerson = securityService.getUserLoginPerson()
		def sortableProps = [ 'fullName', 'company', 'team']
		def orders = ['asc', 'desc']
		
		//code which is used to resolve the bug in TM-2585: 
		//alphasorting is reversed each time when the user checks or unchecks the two filtering checkboxes.
		if(request.JSON.firstProp != 'staff'){
			session.setAttribute("Staff_OrderBy",request.JSON.orderBy)
			session.setAttribute("Staff_SortOn",request.JSON.sortOn)
		}else{
			request.JSON.orderBy = session.getAttribute("Staff_OrderBy")?:'asc'
			request.JSON.sortOn = session.getAttribute("Staff_SortOn")?:'fullName'
		}
		
		def paramsMap = [sortOn : request.JSON.sortOn in sortableProps ? request.JSON.sortOn : 'fullName',
		firstProp : request.JSON.firstProp, 
		orderBy : request.JSON.orderBy in orders ? request.JSON.orderBy : 'asc']
		def sortString = "${paramsMap.sortOn} ${paramsMap.orderBy}"
		sortableProps.each {
			sortString = sortString + ', ' + it + ' asc'
		}
		
		//log.info("projectId:$projectId, role:$role, scale:$scale, location:$location, assigned:$assigned, paramsMap:$paramsMap")
		
		// Save the user preferences from the filter
		userPreferenceService.setPreference("StaffingRole",role)
		userPreferenceService.setPreference("StaffingLocation",location)
		userPreferenceService.setPreference("StaffingPhases",phase.toString().replace("[", "").replace("]", ""))
		userPreferenceService.setPreference("StaffingScale",scale)
		
		userPreferenceService.setPreference("ShowClientStaff",onlyClientStaff.toString())
		userPreferenceService.setPreference("ShowAssignedStaff",assigned.toString())

		def now = TimeUtil.nowGMT()
		def hasShowAllProjectPerm = RolePermissions.hasPermission("ShowAllProjects")
		def hasEditTdsPerm = RolePermissions.hasPermission("EditTDSPerson")
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')
		
		def project
		def moveEvents 
		def projectList = []
		
		/* Create the list of projects to use in the view.
		 * If the projectId is 0, use all active projects.
		 * Otherwise, use the project specified by projectId. */
		if (projectId == 0) {
			// Just get a list of the active projects
			def loginUser = securityService.getUserLogin()
			projectList = projectService.getUserProjectsOrderBy(loginUser, hasShowAllProjectPerm, ProjectStatus.ACTIVE)
		} else {
			// Add only the indicated project if exists and based on user's associate to the project
			project = Project.read( projectId )
			if (project) {
				if ( hasShowAllProjectPerm ) {
					projectList << project
				} else {
					// Lets make sure that the user has access to it (is assoicated with the project)
					def staffProjectRoles = partyRelationshipService.getProjectStaffFunctions(projectId, loginPerson.id)
					if (staffProjectRoles.size() > 0) {
						projectList << project
					}
				}
			} else {
				log.error("Didn't find project $projectId")
			}
		}
		
		
		// Find all Events for one or more Projects and the Staffing for the projects
		if (projectList.size() > 0) {
			moveEvents = MoveEvent.findAll("from MoveEvent m where project in (:project) order by m.project.name , m.name asc",[project:projectList])
			
			// Limit the events to today-30 days and newer (ie. don't show events over a month ago)
			moveEvents = moveEvents.findAll{it.eventTimes.start && it.eventTimes.start > now.minus(30)}
		}
		
		def companies = new StringBuffer('0')
		def projects = new StringBuffer('0')
		projectList.each {
			companies.append(",${it.client.id}")
			projects.append(",${it.id}")
		}
		
		// if the "only client staff" button is not checked, show TDS employees as well
		if (onlyClientStaff == '0')
			companies.append(",18")
		
		// if the user doesn't have permission to edit TDS employees, remove TDS from the companies list
		if( ! hasEditTdsPerm ) {
			def tdsIndex = companies.indexOf("18")
			if(tdsIndex != -1)
				companies.replace(tdsIndex, tdsIndex+1, "0")
		}


		def staffList = projectService.getStaffList(assigned, role, projects, companies, sortString)
		
		/*staffList.each {
			log.info "A ${it}"
		}*/
		
		render(template:"projectStaffTable" ,model:[staffList:staffList, moveEventList:retrieveBundleHeader(moveEvents),
					projectId:projectId, project:project, editPermission:editPermission,
					sortOn : params.sortOn, firstProp : params.firstProp, orderBy : params.orderBy != 'asc' ? 'asc' :'desc'])
		
	}

	/*
	 * An internal function used to retrieve staffing for specified project, roles, etc.
	 *@param projectList - array of Projects to get staffing for
	 *@param role - type of role to filter staff list
	 *@param scale - duration in month  to filter staff list
	 *@param location - location to filter staff list
	 * Note: There is no reference to this method
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
		
		render(template:tab ,model:[person:person, company:company, personFunctions:personFunctions, availabaleFunctions:availabaleFunctions, 
			sizeOfassigned:(personFunctions.size()+1), blackOutdays:blackOutdays, isProjMgr:isProjMgr])
			
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
				startDate?.removeAll([null])
				if (startDate.size()>0) {
					if (startDate[0]) {
						bundleStartDate = bundleTimeformatter.format(startDate[0])
					}
				}
				moveMap.put("project", moveEvent.project.name)
				moveMap.put("name", moveEvent.name)
				moveMap.put("startTime", bundleStartDate)
				moveMap.put("startDate", moveEventDateFormatter.format(moveEvent.eventTimes.start))
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

	def savePreferences() {
		def timezoneValue = params.timezone
		def datetimeFormatValue = params.datetimeFormat

		// Checks that timezone is valid
		def timezone = TimeZone.getTimeZone(timezoneValue)

		// Validate date time format
		def datetimeFormat = TimeUtil.getDateTimeFormat(datetimeFormatValue)

		userPreferenceService.setPreference( UserPreferenceService.TIMEZONE, timezone.getID() )
		userPreferenceService.setPreference( UserPreferenceService.DATE_TIME_FORMAT, datetimeFormat )

		def model = [
			'timezone' : timezone.getID(),
			'datetimeFormat' : datetimeFormat
		]

		render(ServiceResults.success(model) as JSON)
	}

	/**
	 * This action is used to display Current logged user's Preferences with preference code (converted to comprehensive words)
	 * with their corresponding value
	 * @param N/A : 
	 * @return : A Map containing key as preference code and value as map'svalue.
	 */
	def editPreference() {

		def timezones = Timezone.findAll()
		//TODO: should this me added to the DB?
		timezones << [code: "GTM", label: "GTM"]
		def areas = userPreferenceService.timezonePickerAreas()
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

		def currTimeZone = TimeUtil.defaultTimeZone;
		def currDateTimeFormat = TimeUtil.dateTimeFormats[0];

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

				case UserPreferenceService.DATE_TIME_FORMAT:
					currDateTimeFormat = pref.value;
					break;

				case UserPreferenceService.TIMEZONE:
					currTimeZone = pref.value;
					break;

				default :
					prefMap.put((pref.preferenceCode), (labelMap[pref.preferenceCode] ?: pref.preferenceCode )+" / "+ pref.value)
					break;
			}
		}
		
		
		render(template:"showPreference",model:[prefMap:prefMap.sort{it.value}, areas: areas, 
				timezones: timezones, currTimeZone: currTimeZone, currDateTimeFormat: currDateTimeFormat])
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
		
		if(!toPerson.save(flush:true)){
			toPerson.errors.allErrors.each{ println it }
		}
		fromPersons.each{
			def fromPerson = Person.get(it)
			personMerged += personService.mergePerson(fromPerson, toPerson)
		}
		msg += "${personMerged.size() ? WebUtil.listAsMultiValueString(personMerged) : 'None of Person '} Merged to ${toPerson}"
		render msg
	}
}