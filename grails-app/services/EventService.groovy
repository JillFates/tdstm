
/**
 * The event service handles the logic for CRUD events
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class EventService {

	boolean transactional = true
	
	/**
	 * Provides a list all bundles associated to a specified project
	 * for the user's current project
	 * 
	 * @param user the current user
	 * @param currentProject the current project
	 * @return the list of events with the associated bundles
	 */
	def listEventsAndBundles(user, currentProject) {
		if (currentProject == null) {
			log.info('Current project is null')
			throw new EmptyResultException()
		}
		
		def events = MoveEvent.findAllByProject(currentProject);
		
		def result = []
		
		for (event in events) {
			def bundles = []
			
			for (moveBundle in event.moveBundles) {
				def bundleMap = [
					'id' : moveBundle.id,
					'name' : moveBundle.name,
				];	
			
				bundles.add(bundleMap)
			}
			
			def eventMap = [
				'id' : event.id,
				'name' : event.name,
				'bundles' : bundles
			];
		
			result.add(eventMap)
		}
		
		return result
	}
}
