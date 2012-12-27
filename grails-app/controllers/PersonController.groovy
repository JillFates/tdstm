import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.tds.util.jmesa.PersonBean

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jsecurity.SecurityUtils
import org.jsecurity.crypto.hash.Sha1Hash

import com.tdssrc.grails.GormUtil
import com.tds.asset.AssetComment
class PersonController {
    
	def partyRelationshipService
	def userPreferenceService
	def securityService
	def projectService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// return Persons which are related to company
    def list = {
        def companyId = params.id
        List personInstanceList = new ArrayList()
        def companiesList
        def query = "from PartyGroup as p where partyType = 'COMPANY' order by p.name "
        companiesList = PartyGroup.findAll( query )
		def user = securityService.getUserLogin()
		
		if(params.containsKey("companyName") && params.companyName=="All"){
			personInstanceList = Person.findAll( "from Person p order by p.lastName" )
		} else {
		    if(params.containsKey("companyName")){
				companyId  = PartyGroup.findByName(params.companyName)?.id
			}else{
				companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
				if(!companyId){
					def person = user.person
					companyId = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
                                                            "and p.partyIdTo = :partyIdTo and p.roleTypeCodeFrom = 'COMPANY' "+
                                                            "and p.roleTypeCodeTo = 'STAFF' ",[partyIdTo:person]).partyIdFrom[0]?.id
				}
			}
	        if ( companyId!= null && companyId != "" ) {
	        	personInstanceList = partyRelationshipService.getCompanyStaff( companyId )     	
	        } 
		}
		List personsList = new ArrayList()
		personInstanceList.each{
			PersonBean personBean = new PersonBean()
			personBean.setId(it.id)
			personBean.setFirstName(it.firstName)
			personBean.setLastName(it.lastName)
			personBean.setModelScore(it.modelScore)
			def userLogin = UserLogin.findByPerson(it);
			if(userLogin){
				personBean.setUserLogin(userLogin.username)
				personBean.setUserLoginId(userLogin.id)
			} else {
				personBean.setUserLogin("CREATE")
			}
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = :partyTo"+
                                            " and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'",[partyTo:it])?.partyIdFrom
			personBean.setDateCreated(it.dateCreated)
			personBean.setLastUpdated(it.lastUpdated)
			personBean.setUserCompany(userCompany.toString())
			personsList.add(personBean)
		}
		// Statements for JMESA integration
    	TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = personsList
		
		def company
		if(companyId){
			if(params.companyName!="All"){
			    company = PartyGroup.findById(companyId)
			}
		}
		userPreferenceService.setPreference( "PARTYGROUP", companyId.toString() )
		def availabaleRoles = RoleType.findAllByDescriptionIlike("Staff%")
		return [ personsList: personsList, companyId:companyId,totalCompanies:companiesList, company:company, availabaleRoles:availabaleRoles]
    }

    def show = {
			
        def personInstance = Person.get( params.id )
        def companyId = params.companyId
        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else { 
        	//def company = partyRelationshipService.getSatffCompany( personInstance )
        	
        	return [ personInstance : personInstance, companyId:companyId ] 
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

    def update = {       
	        
        def personInstance = Person.get( params.id )
	        
        //personInstance.lastUpdated = new Date()
	        
        def companyId = params.company
        if(personInstance) {
            personInstance.properties = params
			personInstance.tempForUpdate = Math.random().toString()
            if ( !personInstance.hasErrors() && personInstance.save() ) {
            	if(companyId != null ){
    	            def companyParty = Party.findById(companyId)
    	            partyRelationshipService.updatePartyRelationshipPartyIdFrom("STAFF", companyParty, 'COMPANY', personInstance, "STAFF")
                }
                flash.message = "Person ${params.firstName} ${params.lastName} updated"
                redirect( action:list, params:[ id:companyId ])
            }
            else {
                flash.message = "Person ${params.firstName} ${params.lastName} not updated "
                redirect( action:list, params:[ id:companyId ])
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ])
        }
    }
    // return person instance and companies 
    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        // def companies = partyRelationshipService.getCompaniesList()
       	def companyId = params.companyId 
        return [ 'personInstance':personInstance, companyId:companyId ]
    }
    //Save the Person Detais
    def save = {
		
        def personInstance = new Person( params )
        //personInstance.dateCreated = new Date()
        if ( !personInstance.hasErrors() && personInstance.save() ) {
        	def fullName 
			if(params.lastName == ""){
				fullName = params.firstName
			}else{
				fullName = params.firstName+" "+params.lastName
			}
            def companyId = params.company
            if ( companyId != "" ) {
                def companyParty = Party.findById( companyId )
                def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", personInstance, "STAFF" )
            }
			userPreferenceService.setUserRoles([params.role], personInstance.id)
            flash.message = "Person ${fullName} created"
            //redirect( action:list, id:personInstance.id , params:[companyId:companyId] )
            redirect( action:list )
        }
        else {
            def companyId = params.company
            flash.message = "Person FirstName cannot be blank. "
            redirect( action:list )
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
        def personInstance = Person.get( params.id )
        def role = params.role
        def company = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = :person "+
                                            "and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'", [person:personInstance])
        if(company == null){
        	map.put("companyId","")
        }else{
            map.put("companyId",company.partyIdFrom.id)
			map.put("companyName",company.partyIdFrom.name)
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
	 *	Method to add Staff to project through Ajax Overlay 
	 */
	def saveProjectStaff = {
		def id = params.id
		def compositeId = id.split("-")
		def flag = false
		if(compositeId){
	    	def personId = compositeId[1]
	    	def roleType = compositeId[2]
			def projectId = compositeId[3]
	    	def projectParty = Project.findById( projectId )
	    	def personParty = Person.findById( personId )
            def projectStaff
			if(params.val == "0"){
				projectStaff = partyRelationshipService.deletePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
                def moveEvents = MoveEvent.findAllByProject(projectParty)
                def results = MoveEventStaff.executeUpdate("delete from MoveEventStaff where moveEvent in (:moveEvents) and person = :person and role = :role",[moveEvents:moveEvents, person:personParty,role:RoleType.read(roleType)])
			}else{
	    		projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
			}

			flag = projectStaff ? true : false
		}
		
		render flag
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
		
		def userLogin = UserLogin.findByPerson(Person.findById(params.id))
		def password = userLogin.password
		def passwordInput = new Sha1Hash(params.oldPassword).toHex()
		
		if(password.equals(passwordInput))
			return updatePerson(params)
		def ret = []
		ret << [pass:"no"]
		render  ret as JSON
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
			if ( !personInstance.hasErrors() && personInstance.save(flush:true) ) {
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
					if(!userLogin.save()){
						userLogin.errors.allErrors.each{println it}
					}
				}
				if(params.manageRoles != '0' && params.role){
					if(params.role){
						partyRelationshipService.updatePartyRoleByType('staff', personInstance, params.role)
						Set newRoles = []
						newRoles.addAll(params.role)
						userPreferenceService.setUserRoles(newRoles, personInstance.id)
					}
				}
				def personExpDates =params.list("availability")
				if(personExpDates){
					def expFormatter = new SimpleDateFormat("MM/dd/yyyy")
					ExceptionDates.executeUpdate("delete from ExceptionDates where person = '$personInstance.id' ")
					personExpDates.each{
						def expDates = new ExceptionDates()
						expDates.exceptionDay = GormUtil.convertInToGMT(expFormatter.parse(it), tzId)
						expDates.person = personInstance
						
						expDates.save(flush:true)
					}
				}
				
				userPreferenceService.setPreference( "CURR_TZ", params.timeZone )
				userPreferenceService.setPreference( "CURR_POWER_TYPE", params.powerType )
				userPreferenceService.loadPreferences("CURR_TZ")
				userPreferenceService.setPreference("START_PAGE", params.startPage )
				userPreferenceService.loadPreferences("START_PAGE")
				
			}else{
				personInstance.errors.allErrors.each{println it}
			}
			if(params.tab){
				forward( action:'loadGeneral', params:[tab: params.tab, personId:personId])
			}else{
				ret << [name:personInstance.firstName, tz:getSession().getAttribute( "CURR_TZ" )?.CURR_TZ]
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
	def manageProjectStaff ={
		def project = securityService.getUserCurrentProject()
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def now = GormUtil.convertInToGMT( "now",session.getAttribute("CURR_TZ")?.CURR_TZ )
		def projects = projectService.getActiveProject( now, projectHasPermission, 'name', 'asc' )
		def roleTypes = RoleType.findAllByDescriptionIlike("Staff%")
		def role = params.role ? params.role : "MOVE_TECH"
		def moveEventList = []
		
		def user = securityService.getUserLogin()
		def loggedInPerson = user.person
		def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' \
                                        			and p.partyIdTo = :partyIdTo and p.roleTypeCodeFrom = 'COMPANY' \
                                        			and p.roleTypeCodeTo = 'STAFF' ",[partyIdTo:loggedInPerson]).partyIdFrom
		
		def isTdsEmp = userCompany.name == "TDS" ? true :false
		
		def currRole = userPreferenceService.getPreference("StaffingRole")?:"MOVE_TECH"
		def currLoc = userPreferenceService.getPreference("StaffingLocation")?:"All"
		def currPhase = userPreferenceService.getPreference("StaffingPhases")?:"All"
		def currScale = userPreferenceService.getPreference("StaffingScale")?:"6"
        def moveEvents
        def projectId = projects.find{it.id == project.id} ? project.id : 0
        if(projectId == 0){
            moveEvents = MoveEvent.findAll("from MoveEvent m where project in (:project) order by m.project.name , m.name asc",[project:projects])
        } else {
            project = Project.get(projectId)
            moveEvents = MoveEvent.findAll("from MoveEvent m where project =:project order by m.project.name , m.name asc",[project:project])
        }
        // Limit the events to today-30 days and newer (ie. don't show events over a month ago) 
        moveEvents = moveEvents.findAll{it.eventTimes.start && it.eventTimes.start > new Date().minus(30)}
		def paramsMap = [sortOn : 'lastName', firstProp : 'staff', orderBy : 'asc']
		def staffList = getStaffList(projectId, currRole, currScale, currLoc, 0,paramsMap)
		
		def eventCheckStatuses = eventCheckStatus(staffList, moveEvents)
		def staffCheckStatus = staffCheckStatus(staffList,project)
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')
	    [projects:projects, projectId:project.id, roleTypes:roleTypes, staffList:staffList,
			 moveEventList:getBundleHeader( moveEvents ), currRole:currRole, currLoc:currLoc,
			 currPhase:currPhase, currScale:currScale, project:project, eventCheckStatus:eventCheckStatuses,staffCheckStatus:staffCheckStatus,
			 isTdsEmp:isTdsEmp, editPermission:editPermission]
		
	}
	
	/*
	 * 
	 *@param projectId id of project from select
	 *@param role - type of role to filter staff list
	 *@param scale - duration in month  to filter staff list
	 *@param location - location to filter staff list
	 *Redirect to template for staffing list.
	 */
	def loadFilteredStaff={
		def role = params.role
		def projectId = params.project
		def scale = params.scale
		def location = params.location
		def phase = params.list("phaseArr[]")
		def assigned = params.assigned
		userPreferenceService.setPreference("StaffingRole",role)
		userPreferenceService.setPreference("StaffingLocation",location)
		userPreferenceService.setPreference("StaffingPhases",phase.toString().replace("[", "").replace("]", ""))
		userPreferenceService.setPreference("StaffingScale",scale)
		
		def moveEvents 
		def projects
		def project
		if(projectId=="0"){
			def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
			def now = GormUtil.convertInToGMT( "now",session.getAttribute("CURR_TZ")?.CURR_TZ )
			projects = projectService.getActiveProject( now, projectHasPermission, 'name', 'asc' )
			moveEvents = MoveEvent.findAll("from MoveEvent m where project in (:project) order by m.project.name , m.name asc",[project:projects])
		} else {
			project = Project.get(projectId)
			moveEvents = MoveEvent.findAll("from MoveEvent m where project =:project order by m.project.name , m.name asc",[project:project])
		}
        // Limit the events to today-30 days and newer (ie. don't show events over a month ago) 
        moveEvents = moveEvents.findAll{it.eventTimes.start && it.eventTimes.start > new Date().minus(30)}
        def paramsMap = [sortOn : params.sortOn, firstProp : params.firstProp, orderBy : params.orderBy]
		def staffList = getStaffList(projectId,role,scale,location,assigned,paramsMap);
		def eventCheckStatuses = eventCheckStatus(staffList, moveEvents)
		def staffCheckStatuses = []
		if(projectId!="0"){
			 staffCheckStatuses = staffCheckStatus(staffList ,project)
		}
		def editPermission  = RolePermissions.hasPermission('EditProjectStaff')
		render(template:"projectStaffTable" ,model:[staffList:staffList, moveEventList:getBundleHeader(moveEvents),projectId:projectId,
					eventCheckStatus:eventCheckStatuses, project:project,staffCheckStatus:staffCheckStatuses, editPermission:editPermission,
					sortOn : params.sortOn, firstProp : params.firstProp, orderBy : params.orderBy != 'asc' ? 'asc' :'desc'])
		
	}
	
	/*
	 *@param projectId - id of project from select
	 *@param role - type of role to filter staff list
	 *@param scale - duration in month  to filter staff list
	 *@param location - location to filter staff list
	 */
	def getStaffList(def projectId, def role, def scale, def location,def assigned,def paramsMap){
		def sortOn = paramsMap.sortOn ?:"lastName"
		def orderBy = paramsMap.orderBy?:'asc'
		def firstProp = paramsMap.firstProp ? (paramsMap.firstProp && paramsMap.firstProp == 'company' ? '' :paramsMap.firstProp) : 'staff'
		def user = securityService.getUserLogin()
		def loggedInPerson = user.person
		def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' \
												and p.partyIdTo = :partyIdTo and p.roleTypeCodeFrom = 'COMPANY' \
												and p.roleTypeCodeTo = 'STAFF' ",[partyIdTo:loggedInPerson]).partyIdFrom
		
        def subject = SecurityUtils.subject
        def isProjMgr
        if( subject.hasRole("PROJ_MGR") && userCompany.name == "TDS"){
            isProjMgr = true
        }
        
		def project = Project.get( projectId )
		def staffList = []

		StringBuffer queryForStaff = new StringBuffer("FROM PartyRole  p")
		def sqlArgs = [:]
		
		if( role!="0" ){
			def roleType =RoleType.findById(role)
			sqlArgs << [roleArgs : [roleType]]
		} else {
			def roleTypes = RoleType.findAllByDescriptionIlike("Staff%")
			sqlArgs << [roleArgs : roleTypes]
		}
		queryForStaff.append(" WHERE p.roleType IN (:roleArgs) ")
		
		
		if(project && projectId !=0 ){
			def partyIds = partyRelationshipService.getProjectCompaniesStaff(projectId,'',true)
			queryForStaff.append(" AND p.party IN (:partyIds) ")
			sqlArgs << [partyIds : partyIds['staff']]
			
		}
		
		queryForStaff.append("group by p.party, p.roleType order by p.party,p.roleType ")
		
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
                    if(projectId != "0"){
                        hasProjReal = allProjRelations.find{it.partyIdFrom?.id == project.id && it.partyIdTo?.id == party?.id && 
                                                            it.roleTypeCodeTo.id == relation.roleType?.id}
                    } else {
                        hasProjReal = allProjRelations.find{it.partyIdTo.id == party.id && it.roleTypeCodeTo.id == relation.roleType.id}
                    }
                    if(!hasProjReal){
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
		def tab = params.tab ?: 'generalInfoShow'
		def person = Person.get(params.personId)
		def blackOutdays = person.blackOutDates.sort{it.exceptionDay}
		def subject = SecurityUtils.subject
		def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = :person"+
                                                " and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ",[person:person]).partyIdFrom[0]
		
		def rolesForPerson = PartyRole.findAll("from PartyRole where party = :person and roleType.description like 'staff%' group by roleType",[person:person])?.roleType
		def availabaleRoles = RoleType.findAllByDescriptionIlike("Staff%")
		def isProjMgr = false
		if( subject.hasRole("PROJ_MGR")){
			isProjMgr = true
		}
		
		render(template:tab ,model:[person:person, company:company, rolesForPerson:rolesForPerson, availabaleRoles:availabaleRoles, 
            sizeOfassigned:(rolesForPerson.size()+1), blackOutdays:blackOutdays, isProjMgr:isProjMgr])
			
	}
	
	/*
	 *To get headers of event at project staff table 
	 *@param moveEvents list of moveEvent for selected project
	 *@return MAP of bundle header containing projectName ,event name, start time and event id
	 */
	def getBundleHeader(moveEvents){
		def project = securityService.getUserCurrentProject()
		def moveEventList = []
		def bundleTimeformatter = new SimpleDateFormat("MMM dd")
		if(project){
			def bundleStartDate = ""
			def personAssignedToME = []
			moveEvents.each{ moveEvent->
				def moveMap = new HashMap()
				def moveBundle = moveEvent?.moveBundles
				def startDate = moveBundle.startTime.sort()
				startDate?.removeAll([null])
				if(startDate.size()>0){
					if(startDate[0]){
					   bundleStartDate = bundleTimeformatter.format(startDate[0])
					}
				}
				moveMap.put("project", moveEvent.project.name)
				moveMap.put("name", moveEvent.name)
				moveMap.put("startTime", bundleStartDate)
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
	   def compositeId = params.id
	   def flag = true
	   if(compositeId){
		   def project = securityService.getUserCurrentProject()
		   def ids = compositeId.split("-")
		   def personId = ids[1]
		   def eventId = ids[2]
		   def roleType = ids[3]
		   def roleTypeInstance  = RoleType.findById( roleType )
		   def moveEvent = MoveEvent.get( eventId )
		   def person = Person.get( personId )
		   def moveEventStaff = MoveEventStaff.findAllByStaffAndEventAndRole(person, moveEvent, roleTypeInstance)
		   if(moveEventStaff && params.val == "0"){
			  moveEventStaff.delete(flush:true)
		   } else if( !moveEventStaff ){
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
	   }
	   render flag
	}
	
	/*
	 * generates event check box status and id
	 * @param staffList : list of staff 
	 * @param events : list of Events  
	 * @return : id and status of event related check box
	 */
	def eventCheckStatus(staffList, events){
		def checkList = []
		def moveEventStaffList = MoveEventStaff.findAllByPersonInListAndMoveEventInList(staffList.staff, events)
		staffList.each { staffObj ->
			def roleId = staffObj.role.id
			def staffId = staffObj.staff.id
			def eventList = []
			def staffMap = [:]
			events.each { event->
				def eventId = event.id
				def hasAssociation = moveEventStaffList.find{it.person.id == staffId && it.moveEvent.id == eventId && it.role.id == roleId}
				
				def checkMap = [:]
				def checkId = "e-${staffObj.staff?.id}-${event.id}-${staffObj?.role?.id}"
				checkMap.put('id', checkId)
				checkMap.put("status", hasAssociation ? 'checked="checked"' : '' )
				eventList << checkMap
			}
			staffMap.put((staffObj.staff.id+"_"+staffObj.role+'_'+staffObj.project), eventList)
			checkList << staffMap
		}
	 return checkList
	}
	
	/*
	 * generates Project staff check box status and id
	 * @param staffList : list of staff
	 * @param project : instance of project for which generating result
	 * @return : id and status of project related check box
	 */
	def staffCheckStatus(staffList, project){
		def checkList = []
		staffList.each { staffObj ->
			def roleId = staffObj.role.id
			def staffId = staffObj.staff.id
			def projectStaff =PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' "+
                                                        "and p.partyIdFrom = $project.id and p.roleTypeCodeFrom = 'PROJECT' ")
			def hasAssociation = projectStaff.find{it.partyIdTo.id == staffId && it.roleTypeCodeTo.id == roleId && it.partyIdFrom.id == it.partyIdFrom.id}
			def checkMap = [:]
			def checkId = "p-${staffObj.staff?.id}-${staffObj?.role?.id}-${project.id}" 
			checkMap.put('id', checkId)
			checkMap.put("status", hasAssociation ? 'checked="checked"' : '' )
			checkList << [(staffObj.staff.id+"_"+staffObj.role+'_'+staffObj.project): [checkMap]]
		}
	 return checkList
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
						 "START_PAGE" : "Welcome Page", "StaffingRole" : "Default Preoject Staffing Role",
						 "StaffingLocation" : "Default Preoject Staffing Location", "StaffingPhases" : "Default Preoject Staffing Phase",
						 "StaffingScale" : "Default Preoject Staffing Scale", "preference" : "Preference", "DraggableRack" : "Draggable Rack",
						 "PMO_COLUMN1" : "PMO Column 1 Filter", "PMO_COLUMN2" : "PMO Column 2 Filter", "PMO_COLUMN3" : "PMO Column 3 Filter",
						 "PMO_COLUMN4" : "PMO Column 4 Filter", "ShowAddIcons" : "Rack Add Icons"
					  ]
		prefs.each { pref->
			switch( pref.preferenceCode ) {
				case "MOVE_EVENT" :
					prefMap.put((pref.preferenceCode), "Move Event / "+MoveEvent.get(pref.value).name)
					break;
				
				case "CURR_PROJ" :
					prefMap.put((pref.preferenceCode), "Project / "+Project.get(pref.value).name)
					break;
				
				case "CURR_BUNDLE" :
					prefMap.put((pref.preferenceCode), "Move Bundle / "+MoveBundle.get(pref.value).name)
					break;
				
				case "PARTYGROUP" :
					prefMap.put((pref.preferenceCode), "Company / "+PartyGroup.get(pref.value).name)
					break;
				
				case "CURR_ROOM" :
					prefMap.put((pref.preferenceCode), "Room / "+Room.get(pref.value).roomName)
					break;
				
				case "StaffingRole" :
				    def role = RoleType.get(pref.value).description
					prefMap.put((pref.preferenceCode), "Default Preoject Staffing Role / "+role.substring(role.lastIndexOf(':') +1))
					break;
				
				default :
					prefMap.put((pref.preferenceCode), (labelMap[pref.preferenceCode] ?: pref.preferenceCode )+" / "+ pref.value)
					break;
			}
		}
		
		
		render(template:"showPreference",model:[prefMap:prefMap])
	}
}