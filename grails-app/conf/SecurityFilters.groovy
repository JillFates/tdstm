import org.apache.shiro.SecurityUtils

class SecurityFilters {
    def filters = {
        // Creating, modifying, or deleting a Party,person, project,partyGroup requires the ADMIN role.
        partyCrud(controller: "(party|person|project|partyGroup)", action: "(create|edit|save|update|delete)") {
            before = {
                accessControl {
                    role("ADMIN")
                }
            }
        }
        // for modify and delete a userLogin require ADMIN role 
        userCrud(controller: "userLogin", action: "(edit|update|delete)") {
            before = {
                accessControl {
                    role("ADMIN")
                }
            }
        }
        // for delete require ADMIN role 
        crud(controller: "*", action: "delete") {
            before = {
                accessControl {
                    role("ADMIN")
                }
            }
        }
        
		checkForcePasswordChange(controller:'*', action:'*'){
			before = {
				def subject = SecurityUtils.subject
				if(subject != null){
					def principal = subject.principal
					def userLoginInstance
					if(principal != null){
						userLoginInstance = UserLogin.findByUsername(principal)
						if(userLoginInstance.forcePasswordChange){
							if((controllerName == 'auth' && (actionName == 'login' || actionName == 'signIn' || actionName == 'signOut')) || (controllerName == 'userLogin' && (actionName == 'changePassword' || actionName == 'updatePassword'))){
								return true;
							} else {
								flash.message = "Your password has expired and must be changed"
								redirect(controller:'userLogin', action:'changePassword', params:[ userLoginInstance:userLoginInstance ])
								return false
							}
						}
					}
				}
			}		
		}
		
        /*
         *   Statements to Check the Session status
         */
        sessionExpireCheck(controller:'*', action:'*') {
            before = {
            	def subject = SecurityUtils.subject
                def principal = subject.principal
                if(controllerName == 'moveTech' && principal == null){
                	return true
                } else if( controllerName != 'auth' && principal == null ) {
                	flash.message = "Your login session has expired.  Please login again."
                	redirect(controller:'auth', action:'login')
                	return false					
 	           	} else if( controllerName == 'auth' && principal == null && actionName == 'home') {
 	           		flash.message = "Your login session has expired.  Please login again."
                	redirect(controller:'auth', action:'login')
                	return false
 	           	}
            }
        }
    } 
}