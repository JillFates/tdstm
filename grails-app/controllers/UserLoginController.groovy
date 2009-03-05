import org.jsecurity.crypto.hash.Sha1Hash;
class UserLoginController {
    
	def partyRelationshipService
	def userPreferenceService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ userLoginInstanceList: UserLogin.list( params ) ]
    }

    def show = {
        def userLoginInstance = UserLogin.get( params.id )
        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ userLoginInstance : userLoginInstance ] }
    }

    def delete = {
        def userLoginInstance = UserLogin.get( params.id )
        if(userLoginInstance) {
            userLoginInstance.delete()
            flash.message = "UserLogin ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:list)
        }
    }
	/*
	 *  Return userdetails and roles to Edit form
	 */
    def edit = {
        def userLoginInstance = UserLogin.get( params.id )

        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:list)
        }
        else {
        	def person = userLoginInstance.person
        	def availableRoles = userPreferenceService.getAvailableRoles( person )
            def assignedRoles = userPreferenceService.getAssignedRoles( person )
            return [ userLoginInstance : userLoginInstance, availableRoles:availableRoles, assignedRoles:assignedRoles  ]
        }
    }
	/*
	 * update user details and set the User Roles to the Person
	 */
    def update = {
        def userLoginInstance = UserLogin.get( params.id )
        if(userLoginInstance) {
        	println"old password===========================>"+userLoginInstance.password
        	println"updated password===========================>"+params.password
        	def password = params.password
        	def oldPassword = userLoginInstance.password
        	userLoginInstance.properties = params
        	if(password != ""){
        		//	convert password onto Hash code
                userLoginInstance.password = new Sha1Hash(params['password']).toHex()
        	}else{
        		userLoginInstance.password = oldPassword
        	}
            
            if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
            	def assignedRoles = request.getParameterValues("assignedRole");
            	def person = params.person.id
            	userPreferenceService.setUserRoles(assignedRoles, person)
                flash.message = "UserLogin ${params.id} updated"
                redirect(action:show,id:userLoginInstance.id)
            }
            else {
            	def person = userLoginInstance.person
            	def availableRoles = userPreferenceService.getAvailableRoles( person )
                def assignedRoles = request.getParameterValues( "assignedRole" )
                render(view:'edit',model:[userLoginInstance:userLoginInstance, availableRoles:availableRoles, updatedRoles:assignedRoles ])
            }
        }
        else {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }
	// return userlogin details to create form
    def create = {
		def personId = params.id
		def personInstance
		if(personId != null ){
			personInstance = Person.findById( personId )
		}
        def userLoginInstance = new UserLogin()
        userLoginInstance.properties = params
        return ['userLoginInstance':userLoginInstance, personInstance:personInstance ]
    }
	/*
	 *  Save the User details and set the user roles for Person
	 */
    def save = {
        def userLoginInstance = new UserLogin(params)
        userLoginInstance.createdDate = new Date()
        //convert password onto Hash code
        userLoginInstance.password = new Sha1Hash(params['password']).toHex()
        if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
        	def assignedRoles = request.getParameterValues("assignedRole");
        	def person = params.person.id
        	userPreferenceService.setUserRoles(assignedRoles, person)
        	
            flash.message = "UserLogin ${userLoginInstance.id} created"
            redirect(action:show,id:userLoginInstance.id)
        }
        else {
        	def assignedRole = request.getParameterValues("assignedRole");
        	def personId = params.personId
    		def personInstance
    		if(personId != null ){
    			personInstance = Person.findById( personId )
    		}
            render(view:'create',model:[ userLoginInstance:userLoginInstance,assignedRole:assignedRole,personInstance:personInstance ])
        }
    }
}
