import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.MaintService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserService

class SecurityFilters {

	// the controllers that we validate authorization on
	private static final List<String> webSvcCtrl = ['moveEventNews', 'wsDashboard']

	AuditService auditService
	MaintService maintService
	SecurityService securityService
	UserService userService

	def filters = {
		/*
		maintModeCheck(controller: '*', action: '*') {
			before = {
				if (controllerName == "wsSequence") return

				boolean hasBackdoorAccess = maintService.hasBackdoorAccess(session)
				if (controllerName == "auth" && actionName == "maintMode") {
					if (!hasBackdoorAccess) {
						maintService.toggleUsersBackdoor(session)
						redirect(controller: 'auth', action: 'login')
						return
					}

					if (MaintService.isInMaintMode()) {
						render(status: 503, text: '503 Service Unavailable')
						return
					}
				}

				if (MaintService.isInMaintMode() && !hasBackdoorAccess) {
					render(status: 503, text: '503 Service Unavailable')
				}
			}
		}
		*/

		userActivityLog(controller: '*', action: '*') {
			before = {
				auditService.auditRequest(request, params)
			}
		}

//		securityChecks(controller: '*', action: '*') {
//			before = {
//				securityService.checkAccess controllerName, actionName
//			}
//		}

		// TODO BB revisit logic
		// newAuthFilter(controller: '*', action: '*') {
		primaryFilter(controller: '*', action: '*') {
			before = {
				request.setAttribute 'tds_initialRequest', controllerName + '/' + actionName

				// The logic below was for functionality originally dealing with loading moveEvent or MoveBundle
				// TODO : JPM 12/2016 : Need to review security filter for 'moveEventNews' and 'wsDashboard' that doesn't make any sense
				if (!webSvcCtrl.contains(controllerName)) {
					return
				}

				if (securityService.loggedIn) {
					def moveObject
					if (params.id) {
						if (controllerName == 'moveEventNews') {
							moveObject = MoveEvent.get(params.id)
						}
						else if (controllerName == 'wsDashboard') {
							moveObject = MoveBundle.get(params.id)
						}
					}

					// If user is loggedIn then they're authenticated so this logic doesn't make sense...
					// if (!/*subject. TODO BB */isAuthenticated()) {
					// 	response.sendError(401, 'Unauthorized')
					// 	return false
					// }

					if (!moveObject) {
						response.sendError(404, "Not Found")
						return false
					}

					if (securityService.hasPermission('EventChangeStatus')) { // verify the user role as ADMIN
						return true
					}

					// TODO : JPM 12/2016 -- need to figure out the moveEventProjectClientStaff permission check here
					def moveEventProjectClientStaff = PartyRelationship.find(
							"from PartyRelationship p where p.partyRelationshipType = 'STAFF' " +
							"and p.partyIdFrom = ${moveObject?.project?.client?.id} and p.roleTypeCodeFrom = 'COMPANY' " +
							"and p.roleTypeCodeTo = 'STAFF' and p.partyIdTo = $securityService.currentPersonId")
					if (!moveEventProjectClientStaff) {
						// if not ADMIN check whether user is associated to the Party that is associate to
						// the Project.client of the moveEvent / MoveBundle
						response.sendError 403, "Forbidden"
						return false
					}
				}
			}

			after = {
				if (request.getAttribute('tds_initialRequest') != 'auth/signIn') {
					// We don't want to update lastPageLoad when logging in
					if (securityService.loggedIn) {
						userService.updateLastPageLoad()
					}
				}
			}
		}

		// Check to see if the userLogin has forcePasswordChange set and only allow him to access appropriate actions
		checkForcePasswordChange(controller: '*', action: '*') {
			before = {
				UserLogin userLogin = securityService.userLogin
				if (userLogin?.forcePasswordChange == 'Y') {
					if ((controllerName == 'auth' && ['login','signIn','signOut'].contains(actionName)) ||
					    (controllerName == 'userLogin' && ['changePassword', 'updatePassword'].contains(actionName))) {
						return true
					}

					flash.message = "Your password has expired and must be changed"
					redirect(controller: 'userLogin', action: 'changePassword', params: [userLoginInstance: userLogin])
					return false
				}
			}
		}
	}
}
