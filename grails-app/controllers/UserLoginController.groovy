import net.tds.util.jmesa.AssetEntityBean
import org.jsecurity.crypto.hash.Sha1Hash;
import org.jsecurity.SecurityUtils;
import com.tdssrc.grails.GormUtil
import java.text.SimpleDateFormat
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import java.text.SimpleDateFormat

class UserLoginController {
    
	def partyRelationshipService
	def userPreferenceService
	def securityService

	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
 		if(params.activeUsers){
		     session.setAttribute("InActive", params.activeUsers)	
	    }
		def project = securityService.getUserCurrentProject()
		def companyId 
		def active = params.activeUsers ? params.activeUsers : session.getAttribute("InActive")
		if(!active){
			active = 'Y'
		}
		boolean filter = params.filter
		if(filter){
			session.userFilters.each{
				if(it.key.contains("tag")){
					request.parameterMap[it.key] = [session.userFilters[it.key]]
				}
			}
		} else {
			session.userFilters = params
		}
		def userLogin = securityService.getUserLogin()

		def userLoginInstanceList
		def userLoginSize

		def userLoginHasPermission = RolePermissions.hasPermission("ShowAllUsers")
		if( userLoginHasPermission ){
			if(params.companyName && params.companyName != "All" ){
				def company  = PartyGroup.get(params.companyName)
				companyId = company ? company.id : null
			} 
			if( !companyId && params.companyName != "All" ){
				companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
				if(!companyId){
					def person = userLogin.person
					companyId = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = ${person.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ").partyIdFrom[0]?.id
				}
			}
			if(companyId || params.companyName=="All"){
				def personsList = partyRelationshipService.getCompanyStaff( companyId )
				def personIds = ""
				personsList.each{
					personIds += "$it.id,"
				}
				if(personIds){
				    personIds = personIds.substring(0,personIds.lastIndexOf(','))
					def sort = params.sort ? params.sort : 'username'
					def order = params.order ? params.order : 'asc'
					
					userLoginInstanceList = UserLogin.findAll("from UserLogin u where u.person in ($personIds) and active = '${active}' order by u.${sort} ${order}")
					 
				} else if(params.companyName=="All" ){
					userLoginInstanceList = UserLogin.findAllByActive(active).sort{it.username}
				}
			}	
		} else {
			userLoginInstanceList = []
		}
		def partyGroupList = PartyGroup.findAllByPartyType( PartyType.read("COMPANY")).sort{it.name}
		def company
		if(companyId){
			if(params.companyName!="All"){
			    company = PartyGroup.findById(companyId)
			}
		}
		def userLoginList = []
		userLoginInstanceList.each{
			AssetEntityBean personLogin = new AssetEntityBean();
			def userCompany = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = ${it.person?.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")?.partyIdFrom
			personLogin.setId(it.id)
			personLogin.setUserName(it.username)
			personLogin.setPerson(it.person.toString())
			personLogin.setCompany(userCompany[0].toString())
			personLogin.setLastLogin(it.lastLogin)
			personLogin.setDateCreated(it.createdDate)
			personLogin.setExpiryDate(it.expiryDate)
			userLoginList.add(personLogin)
		}
		// Statements for JMESA integration
		TableFacade tableFacade = new TableFacadeImpl("tag",request)
		tableFacade.items = userLoginList
        if(params.companyName=="All"){
			 company = "All"
		}
		return [ userLoginInstanceList : userLoginList, companyId:companyId ,partyGroupList:partyGroupList,company:company]
	}

    def show = {
        def userLoginInstance = UserLogin.get( params.id )
        def companyId = params.companyId
        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        } else { 
			def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
			def assignedRoles = userPreferenceService.getAssignedRoles( userLoginInstance.person )
        	return [ userLoginInstance : userLoginInstance, companyId:companyId, roleList:roleList, assignedRoles:assignedRoles ] 
        }
    }

    def delete = {
        def userLoginInstance = UserLogin.get( params.id )
        def companyId = params.companyId
        if(userLoginInstance) {
            userLoginInstance.delete(flush:true)
            flash.message = "UserLogin ${userLoginInstance} deleted"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
    }
	/*
	 *  Return userdetails and roles to Edit form
	 */
    def edit = {
        def userLoginInstance = UserLogin.get( params.id )
        def companyId = params.companyId
        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        }
        else {
        	def person = userLoginInstance.person
        	def availableRoles = userPreferenceService.getAvailableRoles( person )
            def assignedRoles = userPreferenceService.getAssignedRoles( person )
            def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
            return [ userLoginInstance : userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles, companyId:companyId, roleList:roleList  ]
        }
    }
	/*
	 * update user details and set the User Roles to the Person
	 */
    def update = {
		UserLogin.withTransaction { status ->
	        def userLoginInstance = UserLogin.get( params.id )
	        def companyId = params.companyId
	        if(userLoginInstance) {
	        	try{
		        	def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
		            def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		            def expiryDate = params.expiryDate
		            if(expiryDate){
		            	params.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
		            }
	        	} catch (Exception ex){
	        		println"Invalid date format"
	        	}
	        	def password = params.password
	        	def oldPassword = userLoginInstance.password
	        	userLoginInstance.properties = params
	        	if(password != ""){
	        		//	convert password onto Hash code
	                userLoginInstance.password = new Sha1Hash(params['password']).toHex()
	        	}else{
	        		userLoginInstance.password = oldPassword
	        	}
	            if( !userLoginInstance.hasErrors() && userLoginInstance.save(flush:true) ) {
	            	def assignedRoles = request.getParameterValues("assignedRole");
	            	def person = params.person.id
					def personInstance = Person.get(person)
					partyRelationshipService.updatePartyRoleByType('system', personInstance, assignedRoles)
	            	userPreferenceService.setUserRoles(assignedRoles, person)
	                flash.message = "UserLogin ${userLoginInstance} updated"
	                redirect( action:show, id:userLoginInstance.id, params:[ companyId:companyId ] )
	            } else {
	            	def person = userLoginInstance.person
	            	def availableRoles = userPreferenceService.getAvailableRoles( person )
	                def assignedRoles = userPreferenceService.getAssignedRoles( person )
	                status.setRollbackOnly()
	                render(view:'edit',model:[userLoginInstance:userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles, companyId:companyId ])
	            }
	        }
	        else {
	            flash.message = "UserLogin not found with id ${params.id}"
	            redirect( action:edit, id:params.id, params:[ companyId:companyId ])
	        }
		}
    }
	
	// set the User Roles to the Person
	def addRoles = {
			def assignedRoles = params.assignedRoleId.split(',')
        	def person = params.person
        	def actionType = params.actionType
        	if(actionType != "remove"){
        		userPreferenceService.setUserRoles(assignedRoles, person)
        	} else {
        		userPreferenceService.removeUserRoles(assignedRoles, person)
        	}
		render true
	}
	
	// return userlogin details to create form
    def create = {
		def personId = params.id
		def companyId = params.companyId
		def personInstance
		if(personId != null ){
			personInstance = Person.findById( personId )
			if(personInstance.lastName == null){
				personInstance.lastName = ""
			}
		}
		
        def userLoginInstance = new UserLogin()
        userLoginInstance.properties = params
		def expiryDate = new Date(GormUtil.convertInToGMT( "now", "EDT" ).getTime() + 7776000000)
        userLoginInstance.expiryDate = expiryDate
		def roleList = RoleType.findAll("from RoleType r where r.description like 'system%' order by r.description ")
		return ['userLoginInstance':userLoginInstance, personInstance:personInstance, companyId:companyId, roleList:roleList ]
    }
	/*
	 *  Save the User details and set the user roles for Person
	 */
    def save = {
		try{
			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def expiryDate = params.expiryDate
			if(expiryDate){
				params.expiryDate =  GormUtil.convertInToGMT(formatter.parse( expiryDate ), tzId)
			}
		} catch (Exception ex){
			println"Invalid date format"
		}
        def userLoginInstance = new UserLogin(params)
        //userLoginInstance.createdDate = new Date()
        def companyId = params.companyId
        //convert password onto Hash code
        userLoginInstance.password = new Sha1Hash(params['password']).toHex()
        if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
        	def assignedRoles = request.getParameterValues("assignedRole");
        	def person = params.person.id
        	userPreferenceService.setUserRoles(assignedRoles, person)
			def userPreference = new UserPreference()
			userPreference.userLogin = userLoginInstance
			userPreference.preferenceCode = "START_PAGE"
			userPreference.value = "Current Dashboard"
			userPreference.save( insert: true)
			def tZPreference = new UserPreference()
			tZPreference.userLogin = userLoginInstance
			tZPreference.preferenceCode = "CURR_TZ"
			tZPreference.value = "EDT"
			tZPreference.save( insert: true)
            flash.message = "UserLogin ${userLoginInstance} created"
            redirect( action:show, id:userLoginInstance.id, params:[ companyId:companyId ] )
        }
        else {
        	def assignedRole = request.getParameterValues("assignedRole");
        	def personId = params.personId
    		def personInstance
    		if(personId != null ){
    			personInstance = Person.findById( personId )
    		}
            render(view:'create',model:[ userLoginInstance:userLoginInstance,assignedRole:assignedRole,personInstance:personInstance, companyId:companyId ])
        }
    }
	/*======================================================
	 *  Update recent page load time into userLogin
	 *=====================================================*/
	def updateLastPageLoad = {
		def principal = SecurityUtils.subject?.principal
		if( principal ){
			def userLogin = UserLogin.findByUsername( principal )
			userLogin.lastPage = GormUtil.convertInToGMT( "now", "EDT" )
			userLogin.save(flush:true)
			session.REDIRECT_URL = params.url
		}
		render "SUCCESS"
	 }
}
