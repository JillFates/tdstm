import org.jsecurity.crypto.hash.Sha1Hash;
import org.jsecurity.SecurityUtils;
class UserLoginController {
    
	def partyRelationshipService
	def userPreferenceService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
		def companyId = params.id
		
        if(!params.max) params.max = '10'
        def max = Integer.parseInt( params.max )
		def offset = params.offset ? Integer.parseInt( params.offset ) : 0
		
		def userLoginInstanceList 
		def userLoginSize
		
		def isCompanyAdmin = SecurityUtils.getSubject().hasRole("ADMIN")
		if( isCompanyAdmin ){
			if( !companyId ){
				companyId = session.getAttribute("PARTYGROUP")?.PARTYGROUP
			}
			if(companyId){
				def personsList = partyRelationshipService.getCompanyStaff( companyId )
				def personIds = ""
				personsList.each{
					personIds += "$it.id,"
				}
				personIds = personIds.substring(0,personIds.lastIndexOf(','))
				userLoginInstanceList = UserLogin.findAll("from UserLogin u where u.person in ($personIds)",[max:max, offset:offset])
				userLoginSize =  UserLogin.findAll("from UserLogin u where u.person in ($personIds)").size()
			} else {
				flash.message = "Please select Company before navigating to Users"
		        redirect(controller:'partyGroup',action:'list')
			}
		} else {
			userLoginInstanceList = UserLogin.list( [max:max, offset:offset] )
			userLoginSize = UserLogin.count() 
		}
        return [ userLoginInstanceList : userLoginInstanceList, companyId:companyId ,userLoginSize:userLoginSize]
    }

    def show = {
        def userLoginInstance = UserLogin.get( params.id )
        def companyId = params.companyId
        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect( action:list, params:[ id:companyId ] )
        } else { 
        	return [ userLoginInstance : userLoginInstance, companyId:companyId ] 
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
            return [ userLoginInstance : userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles, companyId:companyId  ]
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
	            	/*def assignedRoles = request.getParameterValues("assignedRole");
	            	def person = params.person.id
	            	userPreferenceService.setUserRoles(assignedRoles, person)*/
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
        return ['userLoginInstance':userLoginInstance, personInstance:personInstance, companyId:companyId ]
    }
	/*
	 *  Save the User details and set the user roles for Person
	 */
    def save = {
        def userLoginInstance = new UserLogin(params)
        //userLoginInstance.createdDate = new Date()
        def companyId = params.companyId
        //convert password onto Hash code
        userLoginInstance.password = new Sha1Hash(params['password']).toHex()
        if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
        	def assignedRoles = request.getParameterValues("assignedRole");
        	def person = params.person.id
        	userPreferenceService.setUserRoles(assignedRoles, person)
        	
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
}
