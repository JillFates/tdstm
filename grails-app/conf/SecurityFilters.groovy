import org.apache.shiro.SecurityUtils

class SecurityFilters {

	def securityService
	def maintService
	def auditService

	def filters = {

		maintModeCheck(controller:'*', action:'*'){
			before = {
				if( controllerName == "wsSequence") return

				def hasBackdoorAccess = maintService.hasBackdoorAccess(session)
				if( controllerName == "auth" && actionName == "maintMode" ){
					if(!hasBackdoorAccess ){
						maintService.toggleUsersBackdoor( session )
						hasBackdoorAccess = maintService.hasBackdoorAccess( session )
						redirect(controller:'auth', action:'login')
						return
					} else if( MaintService.isInMaintMode()){
						render(status: 503, text: '503 Service Unavailable')
						return
					}
				}

				if( MaintService.isInMaintMode() && !hasBackdoorAccess ){
					render(status: 503, text: '503 Service Unavailable')
				}
			}
		}

		userActivityLog(controller:'*', action:'*') {
			before = {
				auditService.auditRequest(request, params)
			}
		}

		// Creating, modifying, or deleting a Party,person, project,partyGroup requires the ADMIN role.
		partyCrud(controller: "(party|partyGroup)", action: "(create|edit|save|update|delete)") {
			before = {
				accessControl {
					role("ADMIN")
				}
			}
		}

		// for delete require ADMIN role
		crud(controller: "*", action: "delete") {
			before = {
				if (controllerName == "project" || controllerName == "userLogin") {
					return true
				}
				accessControl {
					role("ADMIN")
				}
			}
		}

		// Access to the Admin controll requires ADMIN role
//		crud(controller: "admin", action: "*") {
//			before = {
//				accessControl {
//					role("ADMIN")
//				}
//			}
//		}

		// Check to see if the userLogin has forcePasswordChange set and only allow him to access appropriate actions
		checkForcePasswordChange(controller:'*', action:'*'){
			before = {
				def subject = SecurityUtils.subject
				if (subject) {
					def principal = subject.principal
					if (principal) {
						def userLoginInstance = UserLogin.findByUsername("$principal")//securityService.getUserLogin()
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
		loginSessionCheck(controller:'*', action:'*') {
			before = {
				def subject = SecurityUtils.subject
				def principal = subject.principal
				if (! principal) {
					if (controllerName != 'auth') {
						// Deal with remembering URI requested and then redirect to auth/signIn
						def savedUrlForwardURI = (request.forwardURI - request.contextPath)
						if (savedUrlForwardURI.contains("task/userTask")) {
							session.setAttribute("savedUrlForwardURI", savedUrlForwardURI)
						}
						flash.message = "Your login session has expired. Please login again."
						redirect(controller:'auth', action:'login')
						return false
					}
				}
				return true
			}
		}
	}
}
