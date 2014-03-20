import com.tds.asset.Application


/**
 * The application service handles the logic for CRUD applications
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ApplicationService {

	boolean transactional = true
	
	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then it returns all unassigned
	 * applications for the user's current project
	 * 
	 * @param bundleId the id of the bundle
	 * @param user the current user
	 * @param currentProject the current project
	 * @return the list of applications associated with the bundle
	 */
	def listInBundle(bundleId, user, currentProject) {
		if (currentProject == null) {
			log.info('Current project is null')
			throw new EmptyResultException()
		}
		
		if (bundleId != null && !bundleId.isNumber()) {
			throw new IllegalArgumentException('Not a valid number')
		}
		
		bundleId = bundleId.toInteger()
		def mb = null
		
		if (bundleId > 0) {
			mb = MoveBundle.get(bundleId)
			if (mb != null) {
				if (!mb.project.equals(currentProject)) {
					throw new IllegalArgumentException('The current project and the Move event project doesn\'t match')
				}
			} else {
				log.info('Move bundle is null')
				throw new EmptyResultException()
			}
		}
		
		def result = []
		def applications = []
		
		if (mb != null) {
			applications = Application.findAllByMoveBundle(mb)	
		} else {
			applications = Application.findAllByMoveBundleIsNullAndOwner(currentProject.client)
		}
		
		for (application in applications) {
			def applicationMap = [
				'id' : application.id,
				'name' : application.assetName
			];
		
			result.add(applicationMap)
		}
		
		return result
	}
}
