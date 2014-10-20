import import com.tds.asset.Files

class StorageService {

	def assetEntityService

	/**
	 * Used to retrieve a model map of the properties to display a Storage asset
	 * @param project - the project that the user is associated with
	 * @param storage - the database object that the user is attempting to look at
	 * @param params - parameters coming from the request
	 */
	Map getModelForShow(Project project, Files storage, params) {
		def model = [ filesInstance: storage ]

		model.putAll( assetEntityService.getCommonModelForShows('Files', project, params) )

		return model
	}
}