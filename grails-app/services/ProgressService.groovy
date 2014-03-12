
/**
 * The progress services handles the logic for holding the status of async tasks
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class ProgressService {

	def getProgress(id) {
		//TODO esteban
		return [
			'percentComp' : 20,
			'status' : 'In progress',
			'remainingTime' : '2 min 12 sec',
			'lastUpdated' : new Date().getTime()
		];
	}
}
