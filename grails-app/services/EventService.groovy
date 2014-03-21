
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
	
	/**
	 * Provides a list all bundles associated to a specified move event or if id=0 then unassigned bundles
	 * for the user's current project
	 * 
	 * @param eventId the id of the event
	 * @param useForPlanning - a boolean to filter by the useForPlanning property of the move event
	 * @param user the current user
	 * @param currentProject the current project
	 * @return the list of bundles associated with the event
	 */
	def listBundles(eventId, useForPlanning, loginUser, currentProject) {
		if (currentProject == null) {
			log.info('Current project is null')
			throw new EmptyResultException()
		}
		
		if (eventId != null && !eventId.isNumber()) {
			throw new IllegalArgumentException('Not a eventId number')
		}
		
		eventId = eventId.toInteger()
		def moveEvent = null
		
		if (eventId > 0) {
			moveEvent = MoveEvent.get(eventId)
			if (moveEvent != null) {
				if (!moveEvent.project.equals(currentProject)) {
					throw new IllegalArgumentException('The current project and the Move event project doesn\'t match')
				}
			} else {
				log.info('Move event is null')
				throw new EmptyResultException()
			}
		}
		
		def result = []
		def moveBundles = []
		
		if (moveEvent != null) {
			if (useForPlanning != null) {
				moveBundles = MoveBundle.findAllByMoveEventAndUseForPlanning(moveEvent, useForPlanning.toBoolean())
			} else {
				moveBundles = MoveBundle.findAllByMoveEvent(moveEvent)
			}
		} else {
			if (useForPlanning != null) {
				def up = useForPlanning.toBoolean()
				moveBundles = MoveBundle.findAll('from MoveBundle where moveEvent = null AND project.client = :client and useForPlanning = :useForPlanning', ['useForPlanning' : up, 'client' : currentProject.client])
			} else {
				moveBundles = MoveBundle.findAll('from MoveBundle where moveEvent = null AND project.client = :client', ['client' : currentProject.client])
			}
		}
		
		for (moveBundle in moveBundles) {
			def moveBundleMap = [
				'id' : moveBundle.id,
				'name' : moveBundle.name
			];
		
			result.add(moveBundleMap)
		}
		
		return result
	}

}
