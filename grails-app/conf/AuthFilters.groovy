import org.apache.shiro.SecurityUtils

class AuthFilters {
	// List of controllers that we need to validate authorization on
	static webSvcCtrl = ['moveEventNews', 'wsDashboard']
  
	def filters = {
	
		newAuthFilter(controller:'*', action:'*') {
			before = {
					
				def moveObject
				def subject = SecurityUtils.subject
				if( webSvcCtrl.contains( controllerName ) && subject.principal){
					def person = UserLogin.findByUsername(subject.principal)?.person
					if(params.id){
						if(controllerName == "moveEventNews"){
							moveObject = MoveEvent.get(params.id)
						} else if(controllerName == "wsDashboard"){
							moveObject = MoveBundle.get(params.id)
						}
					}
					// Condition to verify the authentication
					if( !subject.isAuthenticated() ) {
						response.sendError( 401, "Unauthorized" )
						return false
					} else if( !moveObject ){					// condition to verify the MoveEvent / moveBundle exist
						response.sendError( 404 , "Not Found" )
						return false
					} else if( RolePermissions.hasPermission("MoveEventStatus") ){		// verify the user role as ADMIN
						return true;
					} else {
						def moveEventProjectClientStaff = PartyRelationship.find( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
											" and p.partyIdFrom = ${moveObject?.project?.client?.id} and p.roleTypeCodeFrom = 'COMPANY'"+
											" and p.roleTypeCodeTo = 'STAFF' and p.partyIdTo = ${person.id}" )
						if(!moveEventProjectClientStaff){		// if not ADMIN check whether user is associated to the Party that is associate to the Project.client of the moveEvent / MoveBundle
							response.sendError( 403 , "Forbidden" )
							return false
						} else{
							return true;
						}
					}
				}
			} // before
		} // uuidFilter
	} // class
}
