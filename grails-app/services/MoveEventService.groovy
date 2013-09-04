
class MoveEventService {
	
	/**
	 * This function using to verify events for requested project
	 * @param reqEventIds : reqEventIds is list of requested id from browser 
	 * @param project : project list
	 * @return
	 */
	def verifyEventsByProject(ArrayList reqEventIds, def project){
		def nonProjEventIds
		if(project){
			def projEventsId = MoveEvent.findAllByProject( project )?.id
			//Now Checking whether requested event list has any bad or non project associated id.
			nonProjEventIds = reqEventIds - projEventsId
			if(nonProjEventIds){
				log.error "Event ids $nonProjEventIds is not associated with current project.\
						    Kindly request for project associated  Event ids ."
			}
		}
		return nonProjEventIds
	}
}