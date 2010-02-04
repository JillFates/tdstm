import org.jsecurity.SecurityUtils
class AuthFilters {
	// List of controllers that we need to validate autorization on
	static webSvcCtrl = ['moveEventNews']
  
	def filters = {
	
		newAuthFilter(controller:'*', action:'*') {
			before = {
					
				def moveEvent
				def subject = SecurityUtils.subject
				def person = UserLogin.findByUsername(principal)?.person
		        
				if( webSvcCtrl.contains( controllerName ) ){
					if(params.id){
						moveEvent = MoveEvent.get(params.id)
					}
					// Condition to verify the authentication
					if( !subject.isAuthenticated() ) {
						response.sendError( 401, "Unauthorized" )
						return false
					} else if( !moveEvent ){					// condition to verify the MoveEvent exist
						response.sendError( 404 , "Not Found" )
						return false
					} else if( !subject.hasRole("ADMIN") ){		// verify the user role as ADMIN
						def moveEventProjectClientStaff = PartyRelationship.find( "from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
															" and p.partyIdFrom = ${moveEvent?.project.client} and p.roleTypeCodeFrom = 'COMPANY'"+
															" and p.roleTypeCodeTo = 'STAFF' and p.partyIdFrom = ${person.id}" )
						if(!moveEventProjectClientStaff){		// if not ADMIN check whether user is associated to the Party that is associate to the Project.client of the moveEvent0 
							response.sendError( 404 , "Not Found" )
							return false
						} else{
							return true;
						}
					} else {
						return true;
					}
				}
			} // before
		} // uuidFilter
	} // class
}
