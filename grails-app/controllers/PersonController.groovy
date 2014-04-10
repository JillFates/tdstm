import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha1Hash
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.apache.commons.lang3.StringUtils
import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.WebUtil;
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

	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	/**
	 * Generates a list view of persons related to company
	 * @param id - company id
	 * @param companyName - optional search by name or 'ALL'
	 */
	def list = {
		def listJsonUrl
		def company
		def currentCompany = securityService.getUserCurrentProject()?.client
		def companyId = params.companyId ?: currentCompany.id
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
	
	def listJson = {
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
			FROM party pa
			LEFT OUTER JOIN person p on p.person_id=pa.party_id 
			LEFT OUTER JOIN user_login u on p.person_id=u.person_id 
			LEFT OUTER JOIN party_relationship r ON r.party_relationship_type_id='STAFF' 
				AND role_type_code_from_id='COMPANY' AND role_type_code_to_id='STAFF' AND party_id_to_id=pa.party_id 
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
		
		
		def map = [controller:'person', action:'listJson', id:"${params.companyId}"]
		def listJsonUrl = HtmlUtil.createLink(map)
		def createUrl = HtmlUtil.createLink([controller:'userLogin', action:'create'])
		def editUrl = HtmlUtil.createLink([controller:'userLogin', action:'edit'])
		
		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = personInstanceList?.collect {
			[ cell: ['<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.firstname+'</a>', 
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.middlename+'</a>', 
			'<a href="javascript:loadPersonDiv('+it.personId+',\'generalInfoShow\')">'+it.lastname+'</a>', 
			'<a href="'+((it.userLoginId)?("${editUrl+'/'+it.userLoginId}"):("${createUrl+'/'+it.personId}"))+'">'+it.userLogin+'</a>', 
			it.company, it.dateCreated, it.lastUpdated, it.modelScore], id: it.personId ]}
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		render jsonData as JSON
	
	}

	def show = {
			
		def personInstance = Person.get( params.id )
		def companyId = params.companyId
		if(!personInstance) {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
		} else { 
			def company = partyRelationshipService.getStaffCompany( personInstance )
			
			return [ personInstance : personInstance, companyId:company.id ] 
		}
	}

	def delete = {
			
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
			redirect( action:list, params:[ id:companyId ] )
		}
		else {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
		}
	}
	// return person details to EDIT form
	def edit = {
		def personInstance = Person.get( params.id )
		def companyId = params.companyId
		if(!personInstance) {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
		}
		else {
			
			return [ personInstance : personInstance, companyId:companyId ]
		}
	}

	/**
	 * Used to update the Person domain objects
	 */
	def update = {
			
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
				redirect( action:list, params:[ id:companyId ])
			}
			else {
				flash.message = "Person '$person' not updated due to: " + GormUtil.errorsToUL(person)
				redirect( action:list, params:[ id:companyId ])
			}
		}
		else {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ])
		}
	}

	/**
	 * Used to save a new Person domain object
	 * @param forWhom - used to indicate if the submit is from a person form otherwise it is invoked from Ajax call
	 */
	def save = {
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
			redirect( action:list, params:[ companyId:companyId ] )
		}

	}


	//	Ajax Overlay for show
	def editShow = {
		
		def personInstance = Person.get( params.id )        
		def companyId = params.companyId
		def companyParty = Party.findById(companyId)
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dateCreatedByFormat = formatter.format(GormUtil.convertInToUserTZ( personInstance.dateCreated, tzId ) )
		def lastUpdatedFormat = formatter.format(GormUtil.convertInToUserTZ( personInstance.lastUpdated, tzId ) )
		if(!personInstance) {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:list, params:[ id:companyId ] )
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
	//ajax overlay for Edit
	def editStaff = {
		def map = new HashMap()
		// TODO - SECURITY - this should have some VALIDATION to who has access to which Staff...
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
	 */
	def updateStaff = {
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
				redirect( action:projectStaff, params:[ projectId:projectId ])
			} else {
				flash.message = "Person ${personInstance} not updated"
				redirect( action:projectStaff, params:[ projectId:projectId ])
			}
		} else {
			flash.message = "Person not found with id ${params.id}"
			redirect( action:projectStaff, params:[ projectId:projectId ])
		}
	}
	/*
	 *  Return Project Staff 
	 */
	def projectStaff = {
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
	def saveProjectStaff = {
		
		def flag = false
		def message = ''
		
		if(params.personId){
			def personId = params.personId
			def roleType = params.roleType
			def projectId = params.projectId
			def projectParty = Project.findById( projectId )
			def personParty = Person.findById( personId )
			def projectStaff
			if(params.val == "0") {
				projectStaff = partyRelationshipService.deletePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
				def moveEvents = MoveEvent.findAllByProject(projectParty)
				def results = MoveEventStaff.executeUpdate("delete from MoveEventStaff where moveEvent in (:moveEvents) and person = :person and role = :role",[moveEvents:moveEvents, person:personParty,role:RoleType.read(roleType)])
			} else {
				projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
			}

			flag = projectStaff ? true : false
		}
		
		def data = ['flag':flag, 'message':message]
		render data as JSON
	}
	/*
	 * Method to save person details and create party relation with Project as well 
	 */
	def savePerson = {
		def personInstance = new Person( params )
		
		//personInstance.dateCreated = new Date()
		def companyId = params.company
		def projectId = session.CURR_PROJ.CURR_PROJ
		def roleType = params.roleType
		if ( !personInstance.hasErrors() && personInstance.save() ) {
			
			if ( companyId != null && companyId != "" ) {
				def companyParty = Party.findById( companyId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
			}
			if ( projectId != null && projectId != "" && roleType != null) {
				def projectParty = Party.findById( projectId )
				def partyRelationship = partyRelationshipService.savePartyRelationship( "PROJ_STAFF", projectParty, "PROJECT", personInstance, roleType )
			}
			if(personInstance.lastName == null){
				personInstance.lastName = ""	
			}
			flash.message = "Person ${personInstance} created"
			redirect( action:'projectStaff', params:[ projectId:projectId, submit:'Add' ] )
		}
		else {
			flash.message = " Person FirstName cannot be blank. "
			redirect( action:'projectStaff', params:[ projectId:projectId,submit:'Add' ] )
		}
	}
	/*-----------------------------------------------------------
	 * Will return person details for a given personId as JSON
	 * @author : Lokanada Reddy 
	 * @param  : person id
	 * @return : person details as JSON
	 *----------------------------------------------------------*/
	def getPersonDetails = {
		def personId = params.id
		def person = Person.findById( personId  )
		def userLogin = UserLogin.findByPerson( person )
		
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
		def expiryDate = userLogin.expiryDate ? formatter.format(GormUtil.convertInToUserTZ(userLogin.expiryDate,tzId)) : ""
		
		def personDetails = [person:person, expiryDate: expiryDate]
		render personDetails as JSON
	}
	/*-----------------------------------------------------------
	 * Check if the user inputed password is correct, and if so call the update method
	 * @author : Ross Macfarlane 
	 * @param  : person id and input password
	 * @return : pass:"no" or the return of the update method
	 *----------------------------------------------------------*/
	def checkPassword = {
		if(params.oldPassword == "")
			return updatePerson(params)
		def password = "" + params.newPassword;
		
			
		def userLogin = UserLogin.findByPerson(Person.findById(params.id))
		if(securityService.validPassword(userLogin.username, params.newPassword)){
			def truePassword = userLogin.password
			def passwordInput = new Sha1Hash(params.oldPassword).toHex()
			
			if(truePassword.equals(passwordInput))
				return updatePerson(params)
			def ret = []
			ret << [pass:"no"]
			render  ret as JSON
		} else {
			def ret = []
			ret << [pass:"invalid"]
			render  ret as JSON
		}
	}
	/*-----------------------------------------------------------
	 * Update the person details and user password, Return person first name
	 * @author : Lokanada Reddy 
	 * @param  : person details and user password
	 * @return : person firstname
	 *----------------------------------------------------------*/
	def updatePerson = {
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

			def personExpDates =params.list("availability")
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
	
	def resetPreferences ={
		def person = Person.findById(params.user)
		def personInstance = UserLogin.findByPerson(person)
		def prePreference = UserPreference.findAllByUserLogin(personInstance).preferenceCode
		prePreference.each{ preference->
		  def preferenceInstance = UserPreference.findByPreferenceCodeAndUserLogin(preference,personInstance)
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
	def manageProjectStaff = {
		def start = new Date()
		
		def hasShowAllProjectsPerm = RolePermissions.hasPermission("ShowAllProjects")
		def hasEditTdsPerm = RolePermissions.hasPermission("EditTDSPerson")
		def now = TimeUtil.nowGMT()
		def projects = []
		
		def project = securityService.getUserCurrentProject()
		def roleTypes = partyRelationshipService.getStaffingRoles()
		def role = params.role ? params.role : "MOVE_TECH"
		def moveEventList = []
		
		// set the defaults for the checkboxes
		def assigned = userPreferenceService.getPreference("ShowAssignedStaff") ?: '1'
		def onlyClientStaff = userPreferenceService.getPreference("ShowClientStaff") ?: '1'
		
		def currRole = userPreferenceService.getPreference("StaffingRole")?:"MOVE_TECH"
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
		def query = new StringBuffer("""
			SELECT * FROM (
				SELECT pr.party_id_to_id AS personId, CONCAT(IFNULL(p.first_name, ''), ' ', IFNULL(CONCAT(p.middle_name, ' '), ''), IFNULL(p.last_name, '')) AS fullName, CONCAT('[',pg.name,']') AS company, 
					pr.role_type_code_to_id AS role, SUBSTRING(rt.description, INSTR(rt.description, ":")+2) AS team, p.last_name AS lastName, 
					pr2.party_id_to_id IS NOT NULL AS project, IFNULL(CONVERT(GROUP_CONCAT(mes.move_event_id) USING 'utf8'), 0) AS moveEvents, IFNULL(CONVERT(GROUP_CONCAT(DATE_FORMAT(ed.exception_day, '%Y-%m-%d')) USING 'utf8'),'') AS unavailableDates 
				FROM tdstm.party_relationship pr 
					LEFT OUTER JOIN person p ON p.person_id = pr.party_id_to_id 
					LEFT OUTER JOIN exception_dates ed ON ed.person_id = p.person_id 
					LEFT OUTER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id 
					LEFT OUTER JOIN role_type rt ON rt.role_type_code = pr.role_type_code_to_id 
					LEFT OUTER JOIN party_relationship pr2 ON pr2.party_id_to_id = pr.party_id_to_id 
						AND pr2.role_type_code_to_id = pr.role_type_code_to_id 
						AND pr2.party_id_from_id IN (${projectList}) 
						AND pr2.role_type_code_from_id = 'PROJECT'
					LEFT OUTER JOIN move_event_staff mes ON mes.person_id = p.person_id 
						AND mes.role_id = pr.role_type_code_to_id 
				WHERE pr.role_type_code_from_id IN ('COMPANY') 
					AND pr.party_relationship_type_id IN ('STAFF') 
					AND pr.party_id_from_id IN (${companies})
                    AND p.active = 'Y' 
				GROUP BY role, personId 
				ORDER BY lastName ASC 
			) AS companyStaff 
			WHERE 1=1
		""")
		
		if (assigned == '1')
			query.append("AND companyStaff.project = 1 ")
		if (currRole != '0')
			query.append("AND companyStaff.role = '${currRole}'")
			
		query.append(" ORDER BY lastName ASC, team ASC ")
		
		def staffList = jdbcTemplate.queryForList(query.toString())
		
		// Limit the events to today-30 days and newer (ie. don't show events over a month ago) 
		moveEvents = moveEvents.findAll{it.eventTimes.start && it.eventTimes.start > new Date().minus(30)}
		def paramsMap = [sortOn : 'lastName', firstProp : 'staff', orderBy : 'asc']
		
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')
		return [projects:reqProjects, projectId:project.id, roleTypes:roleTypes, staffList:staffList,
			moveEventList:getBundleHeader( moveEvents ), currRole:currRole, currLoc:currLoc,
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
	def loadFilteredStaff = {
		def role = params.role ?: 'AUTO'
		def projectId = (params.project.isNumber()) ? (params.project.toLong()) : (0)
		def scale = params.scale
		def location = params.location
		def phase = params.list("phaseArr[]")
		def assigned = params.assigned ?: 1
		def onlyClientStaff = params.onlyClientStaff ?: 1
		def loginPerson = securityService.getUserLoginPerson()
		
		def sortableProps = ['lastName', 'fullName', 'company', 'team']
		def orders = ['asc', 'desc']
		
		//code which is used to resolve the bug in TM-2585: 
		//alphasorting is reversed each time when the user checks or unchecks the two filtering checkboxes.
		if(params.firstProp != 'staff'){
			session.setAttribute("Staff_OrderBy",params.orderBy)
			session.setAttribute("Staff_SortOn",params.sortOn)
		}else{
			params.orderBy = session.getAttribute("Staff_OrderBy")?:'asc'
			params.sortOn = session.getAttribute("Staff_SortOn")?:'lastName'
		}
		
		def paramsMap = [sortOn : params.sortOn in sortableProps ? params.sortOn : 'fullName',
		firstProp : params.firstProp, 
		orderBy : params.orderBy in orders ? params.orderBy : 'asc']
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
		
		def query = new StringBuffer("""
			SELECT * FROM (
				SELECT pr.party_id_to_id AS personId, CONCAT(IFNULL(p.first_name, ''), ' ', IFNULL(CONCAT(p.middle_name, ' '), ''), IFNULL(p.last_name, '')) AS fullName, CONCAT('[',pg.name,']') AS company, 
					pr.role_type_code_to_id AS role, SUBSTRING(rt.description, INSTR(rt.description, ":")+2) AS team, p.last_name AS lastName,
					pr2.party_id_to_id IS NOT NULL AS project, IFNULL(CONVERT(GROUP_CONCAT(mes.move_event_id) USING 'utf8'), 0) AS moveEvents, IFNULL(CONVERT(GROUP_CONCAT(DATE_FORMAT(ed.exception_day, '%Y-%m-%d')) USING 'utf8'),'') AS unavailableDates 
				FROM tdstm.party_relationship pr 
					LEFT OUTER JOIN person p ON p.person_id = pr.party_id_to_id 
					LEFT OUTER JOIN exception_dates ed ON ed.person_id = p.person_id 
					LEFT OUTER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id 
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
				GROUP BY role, personId 
				ORDER BY fullName ASC 
			) AS companyStaff 
			WHERE 1=1 
		""")
		
		if (assigned == '1')
			query.append("AND companyStaff.project = 1 ")
		if (role != '0')
			query.append("AND companyStaff.role = '${role}' ")
		
		query.append("ORDER BY ${sortString}")
		
		def staffList = jdbcTemplate.queryForList(query.toString())
		
		/*staffList.each {
			log.info "A ${it}"
		}*/
		
		render(template:"projectStaffTable" ,model:[staffList:staffList, moveEventList:getBundleHeader(moveEvents),
					projectId:projectId, project:project, editPermission:editPermission,
					sortOn : params.sortOn, firstProp : params.firstProp, orderBy : params.orderBy != 'asc' ? 'asc' :'desc'])
		
	}

	/*
	 * An internal function used to retrieve staffing for specified project, roles, etc.
	 *@param projectList - array of Projects to get staffing for
	 *@param role - type of role to filter staff list
	 *@param scale - duration in month  to filter staff list
	 *@param location - location to filter staff list
	 */
	def getStaffList(def projectList, def role, def scale, def location,def assigned,def paramsMap){
	
		def sortOn = paramsMap.sortOn ?:"lastName"
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
		
		/*

		StringBuffer queryForStaff = new StringBuffer("FROM PartyRole  p")
		def sqlArgs = [:]
		log.info("staffList: $staffList")
		
		StringBuffer queryForStaff = new StringBuffer("FROM PartyRole  p")
		def sqlArgs = [:]
		
		if( role!="0" ){
			def roleType =RoleType.findById(role)
			sqlArgs << [roleArgs : [roleType]]
		} else {
			def roleTypes = partyRelationshipService.getStaffingRoles()
			sqlArgs << [roleArgs : roleTypes]
		}
		queryForStaff.append(" WHERE p.roleType IN (:roleArgs) ")
		
		
		if(project && projectId !=0 ){
			def partyIds = partyRelationshipService.getProjectCompaniesStaff(projectId,'',true)
			queryForStaff.append(" AND p.party IN (:partyIds) ")
			sqlArgs << [partyIds : partyIds['staff']]
		}
		
		queryForStaff.append("group by p.party, p.roleType order by p.party,p.roleType ")

		log.info("queryForStaff: $queryForStaff")
			
		def partyRoles = PartyRole.findAll(queryForStaff,sqlArgs)
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def now = GormUtil.convertInToGMT( "now",session.getAttribute("CURR_TZ")?.CURR_TZ )
		def activeProjects = projectService.getActiveProject( now, projectHasPermission, 'name', 'asc' )
		def allProjRelations = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' \
														and p.roleTypeCodeFrom = 'PROJECT' and p.partyIdFrom in (:partyIdFrom)",
														[partyIdFrom:activeProjects])
		partyRoles.each { relation->
			Party party = relation.party
			def person = Person.read(party.id)
			def addComUsers = isProjMgr ?: false
			if(person.active == "Y"){
				def doAdd = true
				if(assigned == "1"){
					def hasProjReal
					if (projectId != "0") {
						hasProjReal = allProjRelations.find{ it.partyIdFrom?.id == project.id && it.partyIdTo?.id == party?.id && 
						   it.roleTypeCodeTo.id == relation.roleType?.id}
					} else {
						hasProjReal = allProjRelations.find{it.partyIdTo.id == party.id && it.roleTypeCodeTo.id == relation.roleType.id}
					}
					if (!hasProjReal) {
						doAdd = false
					}
				}
				if(doAdd){
					
					def company = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = :party "+
						"and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'",[party:party]).partyIdFrom
					if(!isProjMgr){
						if(company.id == userCompany.id){
							addComUsers = true
						} else if(projectId != "0" ){
							def hasProjReal = allProjRelations.find{it.partyIdFrom?.id == project.id && it.partyIdTo?.id == party?.id && 
								it.roleTypeCodeTo.id == relation.roleType?.id}
							if(hasProjReal){
								addComUsers = true
							}
						}
					}
					if( addComUsers ){
						def projectStaff = allProjRelations.findAll{it.partyIdTo.id == party.id && it.roleTypeCodeTo.id == relation.roleType.id}?.partyIdFrom
						def map = new HashMap()
						map.put("company", company)
						map.put("name", party.firstName+" "+ party.lastName)
						map.put("role", relation.roleType)
						map.put("staff", party)
						map.put("project",projectId)
						map.put("roleId", relation.roleType.id)
						map.put("staffProject", projectStaff?.name)
					
						staffList << map
					}
				}
			}
		}
		*/
		
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
	def loadGeneral = {
		log.info "class: ${params.personId.class}    value: ${params.personId}"
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
	def getBundleHeader(moveEvents) {
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
	def saveEventStaff = {
		
		def flag = true
		def message = ""
		
		// Check if the user has permission to edit the staff
		if ( RolePermissions.hasPermission("EditProjectStaff") ) {
			// Check if the person and events are not null
			if ( params.personId && params.eventId ) {
				// Check if the user is trying to edit a TDS employee without permission
				if ( ! ( partyRelationshipService.isTdsEmployee(params.personId) && ! RolePermissions.hasPermission("EditTDSPerson") ) ) {
					def personId = params.personId
					def eventId = params.eventId
					def roleType = params.roleType ?: 'AUTO'
					def roleTypeInstance = RoleType.findById( roleType )
					def moveEvent = MoveEvent.get( eventId )
					def person = Person.get( personId )
					def project = moveEvent.project ?: securityService.getUserCurrentProject()
					
					def moveEventStaff = MoveEventStaff.findAllByStaffAndEventAndRole(person, moveEvent, roleTypeInstance)
					if(moveEventStaff && params.val == "0"){
						moveEventStaff.delete(flush:true)
					} else if( !moveEventStaff ) {
						def projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "PROJECT", person, roleType )
						moveEventStaff = new MoveEventStaff()
						moveEventStaff.person = person
						moveEventStaff.moveEvent = moveEvent
						moveEventStaff.role = RoleType.findById( roleType )
						if(!moveEventStaff.save(flush:true)){
							moveEventStaff.errors.allErrors.each{ println it}
							flag = false
						}
					}
				} else {
					message = "You do not have permission to edit TDS employees"
					flag = false
				}
			} else {
				message = "The selected person and move event are not both valid"
				flag = false
			}
		} else {
			message = "You do not have permission to assign staff to events"
			flag = false
		}
		
		def data = ['flag':flag, 'message':message]
		render data as JSON
	}
	
	/**
	 * This action is used to handle ajax request and to delete preference except Preference Code : Current Dashboard
	 * @param prefCode : Preference Code that is requested for being deleted
	 * @return : boolean
	 */
	def removeUserPreference = {
		def prefCode = params.prefCode
		if(prefCode != "Current Dashboard")
			userPreferenceService.removePreference(prefCode)
			
		render true
	}
	
	/**
	 * This action is used to display Current logged user's Preferences with preference code (converted to comprehensive words)
	 * with their corresponding value
	 * @param N/A : 
	 * @return : A Map containing key as preference code and value as map'svalue.
	 */
	def editPreference = {
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
	def compareOrMerge ={
		
		def ids = params.list("ids[]")
		def personsMap = [:]
		def userLogins= []
		ids.each{
			def id = Long.parseLong(it)
			def person = Person.get(id)
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
	
	def mergePerson ={
		
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