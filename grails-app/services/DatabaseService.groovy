import com.tds.asset.Database
import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.eav.EavAttributeSet
import com.tdsops.tm.enums.domain.AssetClass
import com.tds.asset.AssetType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil

class DatabaseService {

	def assetEntityService

	/**
	 * Used to retrieve a model map of the properties to display a database asset
	 * @param project - the project that the user is associated with
	 * @param db - the database object that the user is attempting to look at
	 * @param params - parameters coming from the request
	 */
	Map getModelForShow(Project project, Database db, params) {
		Map model = [ databaseInstance: db ]

		model.putAll( assetEntityService.getCommonModelForShows('Database', project, params) )

		return model
	}

	/**
	 * Used to create a new Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param params - the request parameters
	 * @param device - the device to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	Database saveAssetFromForm(controller, session, Long projectId, Long userId, params ) {
		Database asset = new Database()
		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param params - the request parameters
	 * @param id - the id of the asset to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	Database updateAssetFromForm(controller, session, Long projectId, Long userId, params, Long id ) {
		def asset
		if (id) {
			asset = Database.get(id)
		} else {
			throw new RuntimeException("updateAssetFromForm() was missing required id parameter")
		}
		if (! asset) {
			throw new DomainUpdateException("updateAssetFromForm() unable to locate asset id $id")
		}

		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param params - the request parameters
	 * @param device - the device to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	private Database saveUpdateAssetFromForm(controller, session, Long projectId, Long userId, params, Database asset) {
		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userId)
		boolean isNew = ! asset.id
		if (isNew) {
			asset.project = project
			asset.owner = project.client
			asset.attributeSet = EavAttributeSet.get(3)
			asset.assetClass = AssetClass.DATABASE
			asset.assetType = AssetType.DATABASE
			// asset.createdBy = userLogin.person
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access asset $id not belonging to current project $project.id", userLogin)
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		// Validate the optimistic locking to prevent two people from editing the same EXISTING asset
		if (!isNew)
			GormUtil.optimisticLockCheck(asset, params, 'Database')

		// Save who changed it
		asset.modifiedBy = userLogin.person

		// Update the date fields to proper dates

		asset.properties = params

		assetEntityService.assignAssetToBundle(project, asset, params['moveBundle.id'])

		assetEntityService.createOrUpdateAssetEntityAndDependencies(asset.project, userLogin, asset, params)

		return asset
	}

}
