import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import net.tds.util.jmesa.PersonBean

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jsecurity.SecurityUtils
import org.jsecurity.crypto.hash.Sha1Hash

import com.tdssrc.grails.GormUtil
class PersonController {
    
	def partyRelationshipService
	def userPreferenceService
	def securityService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	// return Persons which are related to company
    def list = {
		//def	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        def companyId = params.id
        List personInstanceList = new ArrayList()
        def companiesList
        def query = "from PartyGroup as p where partyType = 'COMPANY' "
        companiesList = PartyGroup.findAll( query )
		
		if(params.companyName ){
			companyId  = PartyGroup.findByName(params.companyName)?.id
		}
		if(params.companyName!="All"){
			if( !companyId ){
				companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
			}
		}
		if(companyId == null && params.companyName=="All"){
			personInstanceList = Person.findAll( "from Person" )
		}
        if ( companyId!= null && companyId != "" ) {
        	
        	personInstanceList = partyRelationshipService.getCompanyStaff( companyId )     	
        } else if(params.companyName!="All"){
        	flash.message = "Please select Company before navigating to Staff"
            redirect(controller:'partyGroup',action:'list')
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
			personBean.setDateCreated(it.dateCreated)
			personBean.setLastUpdated(it.lastUpdated)
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
		
		return [ personsList: personsList, companyId:companyId,totalCompanies:companiesList, company:company]
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
            flash.message = "Person ${fullName} created"
            //redirect( action:list, id:personInstance.id , params:[companyId:companyId] )
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
            def companyId = params.company
            flash.message = " Person FirstName cannot be blank. "
            redirect( action:list, params:[ id:companyId ] )
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
        def company = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $personInstance.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'")
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
		return [ projectStaff:projectStaff, companiesStaff:companiesStaff, projectCompanies:projectCompanies, projectId:projectId, submit:submit, personHasPermission:RolePermissions.hasPermission("AddPerson") ]
	}
	/*
	 *	Method to add Staff to project through Ajax Overlay 
	 */
	def saveProjectStaff = {
    	def projectId = session.CURR_PROJ.CURR_PROJ
    	def personId = params.person
    	def roleType = params.roleType
    	def submit = params.submit
    	def projectParty = Project.findById( projectId )
    	def personParty = Person.findById( personId )
    	def projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", projectParty, "PROJECT", personParty, roleType )
    	redirect(action:'projectStaff', params:[projectId:projectId, submit:submit] )
    }
	/*
	 * Method to save person detais and create party relation with Project as well 
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
			personInstance.properties = params
			if ( !personInstance.hasErrors() && personInstance.save() ) {
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
				//getSession().setAttribute( "LOGIN_PERSON", ['name':personInstance.firstName, "id":personInstance.id ])
				def userLogin = UserLogin.findByPerson( personInstance )
				if(userLogin){
					def password = params.newPassword
					
					if(password != null)
						userLogin.password = new Sha1Hash(params.newPassword).toHex()
						
					if(params.expiryDate && params.expiryDate != "null"){
						def expiryDate = params.expiryDate
						userLogin.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
					}
					if(!userLogin.save()){
						userLogin.errors.allErrors.each{println it}
					}
				}
				if(params.manageRoles != '0' && params.role){
					PartyRole.executeUpdate("delete from PartyRole where party = '$personInstance.id'  ")
					if(params.role){
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
				userPreferenceService.loadPreferences("START_PAGE")
				userPreferenceService.setPreference("START_PAGE", params.startPage )
			}
			if(params.tab){
				forward( action:'loadGeneral', params:[tab: params.tab, personId:personInstance.id])
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
		def projects = Project.findAll()
		def roleTypes = RoleType.findAllByDescriptionIlike("Staff%")
		def role = params.role ? params.role : "MOVE_TECH"
		def moveEventList = []
		def bundleTimeformatter = new SimpleDateFormat("dd-MMM")
		if(project){
			def moveMap = new HashMap()
			def moveEvents = MoveEvent.findAllByProject(project)
			def bundleStartDate = ""
			moveEvents.each{ moveEvent->
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
				
				moveEventList << moveMap
			}
			
		}
		def staffList = getStaffList(project.id)
		
	    [projects:projects, projectId:project.id, roleTypes:roleTypes, staffList:staffList,
			 moveEventList:moveEventList,
			 currRole:userPreferenceService.getPreference("StaffingRole")?:"MOVE_TECH",
			 currLoc:userPreferenceService.getPreference("StaffingLocation")?:"All",
			 currPhase:userPreferenceService.getPreference("StaffingPhases")?:"All",
			 currScale:userPreferenceService.getPreference("StaffingScale")?:"1"]
		
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
		def project = params.project
		def scale = params.scale
		def location = params.location
		def phase = params.list("phaseArr[]")
		
		userPreferenceService.setPreference("StaffingRole",role)
		userPreferenceService.setPreference("StaffingLocation",location)
		userPreferenceService.setPreference("StaffingPhases",phase.toString().replace("[", "").replace("]", ""))
		userPreferenceService.setPreference("StaffingScale",scale)
		
		def getStaffList = getStaffList(project,role,scale,location);
		
		render(template:"projectStaffTable" ,model:[staffList:getStaffList])
		
	}
	
	/*
	 *@param projectId - id of project from select
	 *@param role - type of role to filter staff list
	 *@param scale - duration in month  to filter staff list
	 *@param location - location to filter staff list
	 */
	
	def getStaffList(def projectId, def role="MOVE_TECH", def scale=1, def location= "All"){
		def queryForStaff = "from PartyRelationship p where p.roleTypeCodeTo ='${role}'"
		def project = Project.get( projectId )
		if(project && projectId!=0){
			queryForStaff+=" and p.partyIdFrom = ${projectId} "
		}
		queryForStaff+=" group by p.partyIdTo"
		def list = []
		def staffList = PartyRelationship.findAll(queryForStaff)
		staffList.each{staff ->
			def map = new HashMap()
			def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
			map.put("company", company.partyIdFrom)
			map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
			map.put("role", staff.roleTypeCodeTo)
			map.put("staff", staff.partyIdTo)
			list<<map
		}
		return list
		
	}
	
	/*
	 *@param tab is name of template where it need to be redirect
	 *@param person Id is id of person
	 *@return NA
	 */
	
	def loadGeneral = {
		def tab = params.tab ?: 'generalInfoShow'
		def person = Person.get(params.personId)
		def blackOutdays = person.blackOutDates
		def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $person.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ").partyIdFrom[0]
		
		def rolesForPerson = PartyRole.findAll("from PartyRole where party = :person and roleType.description like 'staff%' group by roleType",[person:person])?.roleType
		def availabaleRoles = RoleType.findAllByDescriptionIlike("Staff%")//RoleType.findAll("from RoleType r where r.id in description like 'staff%' (select roleType.id from PartyRole where  description like 'staff%' group by roleType.id )")
		
		
		render(template:tab ,model:[person:person, company:company, rolesForPerson:rolesForPerson, availabaleRoles:availabaleRoles, sizeOfassigned:(rolesForPerson.size()+1),
									blackOutdays:blackOutdays])
	}
}
