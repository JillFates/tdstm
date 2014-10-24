import com.tds.asset.Files
import com.tdssrc.eav.EavAttributeSet
import com.tdsops.tm.enums.domain.AssetClass
import com.tds.asset.AssetType
import com.tdssrc.grails.GormUtil


class StorageService {
	
	def assetEntityService

	/**
	 * Used to retrieve a model map of the properties to display a Storage asset
	 * @param project - the project that the user is associated with
	 * @param storage - the database object that the user is attempting to look at
	 * @param params - parameters coming from the request
	 */
	Map getModelForShow(Project project, Files storage, org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap params) {
		def model = [ filesInstance: storage ]

		model.putAll( assetEntityService.getCommonModelForShows('Files', project, params) )

		return model
	}

	/**
	 * Used to create a new Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the http seesion object
	 * @param projectId - the user's selected project id
	 * @parma userId - the id of the user
	 * @param params - the request parameters
	 * @throws various RuntimeExceptions if there are any errors
	 */
	Files saveAssetFromForm(controller, session, Long projectId, Long userId, params ) {
		Files asset = new Files()
		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
	}		

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param projectId - the user's selected project id
	 * @parma userId - the id of the user
	 * @param params - the request parameters
	 * @param id - the id of the asset to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	Files updateAssetFromForm(controller, session, Long projectId, Long userId, params, Long id ) {
		def asset
		if (id) {
			asset = Files.get(id)
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
	 * @param projectId - the user's selected project id
	 * @parma userId - the id of the user
	 * @param params - the request parameters
	 * @param asset - the asset object to update
	 * @throws various RuntimeExceptions if there are any errors
	 */
	private Files saveUpdateAssetFromForm(controller, session, Long projectId, Long userId, params, Files asset ) {
		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userId)
		boolean isNew = ! asset.id
		if (isNew) {
			asset.project = project
			asset.owner = project.client
			asset.attributeSet = EavAttributeSet.get(4)	// Storage attributeSet
			asset.assetClass = AssetClass.STORAGE
			asset.assetType = AssetType.STORAGE

			// asset.createdBy = userLogin.person
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access asset $id not belonging to current project $project.id", userLogin)
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		// Validate the optimistic locking to prevent two people from editing the same EXISTING asset
		if (!isNew)
			GormUtil.optimisticLockCheck(asset, params, 'Logical Storage')

		// Save who changed it
		asset.modifiedBy = userLogin.person

		asset.properties = params

		assetEntityService.assignAssetToBundle(project, asset, params['moveBundle.id'])

		if (! asset.validate() || ! asset.save(flush:true)) {
			log.debug "Unable to update device ${GormUtil.allErrorsString(asset)}"
			throw new DomainUpdateException("Unable to update asset ${GormUtil.allErrorsString(asset)}".toString())
		}

		def errors = assetEntityService.createOrUpdateAssetEntityDependencies(asset.project, userLogin, asset, params)
		if (errors) {
			throw new DomainUpdateException("Unable to update dependencies : $errors".toString())
		}

		return asset
	}

}