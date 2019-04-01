package net.transitionmanager.interceptors

import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AuditService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserService

class SecurityInterceptor {

	// the controllers that we validate authorization on
	private static final List<String> webSvcCtrl = ['moveEventNews', 'wsDashboard']

	AuditService    auditService
	SecurityService securityService
	UserService     userService

	SecurityInterceptor() {
		matchAll()
			.excludes(uri: '/')
			.excludes(uri: '/login/auth')
			.excludes(controller: 'auth')
	}


	boolean before() {
		try {
			auditService.auditRequest(request, params)

			request.setAttribute 'tds_initialRequest', controllerName + '/' + actionName

			// The logic below was for functionality originally dealing with loading moveEvent or MoveBundle
			// TODO : JPM 12/2016 : Need to review security filter for 'moveEventNews' and 'wsDashboard' that doesn't make any sense
			if (!webSvcCtrl.contains(controllerName)) {
				return true
			}

			if (securityService.loggedIn) {
				def moveObject
				if (params.id) {
					if (controllerName == 'moveEventNews') {
						moveObject = MoveEvent.get(params.id)
					} else if (controllerName == 'wsDashboard') {
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

				if (securityService.hasPermission(Permission.EventChangeStatus)) { // verify the user role as ADMIN
					return true
				}

				// TODO : JPM 12/2016 -- need to figure out the moveEventProjectClientStaff permission check here
				def moveEventProjectClientStaff = PartyRelationship.find(
					"from PartyRelationship p where p.partyRelationshipType = 'STAFF' " +
					"and p.partyIdFrom = ${moveObject?.project?.client?.id} and p.roleTypeCodeFrom = 'ROLE_COMPANY' " +
					"and p.roleTypeCodeTo = 'ROLE_STAFF' and p.partyIdTo = $securityService.currentPersonId")
				if (!moveEventProjectClientStaff) {
					// if not ADMIN check whether user is associated to the Party that is associate to
					// the Project.client of the moveEvent / MoveBundle
					response.sendError 403, "Forbidden"
					return false
				}
			}

			return true
		}catch(Exception e){
			log.error("Error in interceptor", e)
			return false
		}
	}
}
