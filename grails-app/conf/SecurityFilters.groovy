import org.jsecurity.SecurityUtils
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
                	flash.message = "Your login has expired and must login again."
                	redirect(controller:'auth', action:'login')
                	return false					
 	           	} else if( controllerName == 'auth' && principal == null && actionName == 'home') {
 	           		flash.message = "Your login has expired and must login again."
                	redirect(controller:'auth', action:'login')
                	return false
 	           	}
            }
        }
    } 
}