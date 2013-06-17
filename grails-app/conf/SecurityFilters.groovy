import org.apache.shiro.SecurityUtils

class SecurityFilters {
	 def filters = {

		def subject = SecurityUtils.subject
		def principal = subject.principal

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

		// Check to see if the userLogin has forcePasswordChange set and only allow him to access appropriate actions
		checkForcePasswordChange(controller:'*', action:'*'){
			before = {
				if (subject != null) {
					if (principal != null) {
						def userLoginInstance = SecurityService.getUserLogin()
						if ( userLoginInstance?.forcePasswordChange == 'Y' ) {
							if ( 
								(controllerName == 'auth' && ['login','signIn','signOut'].contains(actionName) ) ||
							 	(controllerName == 'userLogin' && ['changePassword','updatePassword'].contains(actionName)  )  
							) {
								return true
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
				 if (controllerName == 'moveTech' && principal == null) {
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