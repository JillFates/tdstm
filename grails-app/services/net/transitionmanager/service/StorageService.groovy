package net.transitionmanager.service

import com.tds.asset.AssetType
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project

class StorageService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Used to retrieve a model map of the properties to display a Storage asset
	 * @param project - the project that the user is associated with
	 * @param storage - the database object that the user is attempting to look at
	 * @param params - parameters coming from the request
	 */
	Map getModelForShow(Project project, Files storage, Map params) {
		Map commonModel = this.getCommonModel(false, project, storage, params)
		[filesInstance: storage, currentUserId: securityService.currentPersonId] + commonModel
	}

	/**
	 * Used to retrieve a model map of the properties to display a Storage asset
	 * @param params - parameters coming from the request
	 */
	@Transactional(readOnly = true)
	Map getModelForCreate(Map params=null) {
		Project project = securityService.getUserCurrentProject()
		Files file = new Files(project: project);
		Map commonModel = this.getCommonModel(true, project, file, params)

		return [assetInstance: file] + commonModel
	}

	/**
	 * Used to create a new Database asset that is called from the controller
	 * @param project - the user's selected project
	 * @param params - the request parameters
	 */
	Files saveAssetFromForm(Project project, Map params) {
		saveUpdateAssetFromForm(project, params, new Files())
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param project - the user's selected project
	 * @param params - the request parameters
	 * @param id - the id of the asset to update
	 */
	Files updateAssetFromForm(Project project, Map params, Long id) {
		def asset
		if (id) {
			asset = Files.get(id)
		} else {
			throw new RuntimeException("updateAssetFromForm() was missing required id parameter")
		}
		if (! asset) {
			throw new DomainUpdateException("updateAssetFromForm() unable to locate asset id $id")
		}

		return saveUpdateAssetFromForm(project, params, asset)
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param project - the user's selected project
	 * @param params - the request parameters
	 * @param asset - the asset object to update
	 */
	private Files saveUpdateAssetFromForm(Project project, Map params, Files asset) {
		boolean isNew = ! asset.id
		if (isNew) {
			asset.project = project
			asset.owner = project.client
			// Removed by TM-6779 - this is not being used since field specs fields implementation
			// asset.attributeSet = EavAttributeSet.get(4)	// Storage attributeSet
			asset.assetClass = AssetClass.STORAGE
			asset.assetType = AssetType.STORAGE
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access asset $id not belonging to current project $project.id")
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		// Validate the optimistic locking to prevent two people from editing the same EXISTING asset
		if (!isNew) {
			GormUtil.optimisticLockCheck(asset, params, 'Logical Storage')
		}

		// Save who changed it
		asset.modifiedBy = securityService.loadCurrentPerson()
		asset.properties = params

		assetEntityService.assignAssetToBundle(project, asset, params['moveBundle.id'])
		assetEntityService.createOrUpdateAssetEntityAndDependencies(asset.project, asset, params)

		return asset
	}

	/**
	 * Used to get the model map used to render the create view of an Storage domain asset
	 * @param forCreate - is model for create or show/edit
	 * @param project - the project of the user
	 * @param file - current file
	 * @param params - request parameters
	 * @return a map of the properties containing the list values to populate the list controls
	 */
	private Map getCommonModel(Boolean forCreate, Project project, file , Map params) {
		Map commonModel

		if (forCreate) {
			commonModel = assetEntityService.getCommonModelForCreate('Files', project, file)
		} else {
			commonModel = assetEntityService.getCommonModelForShows('Files', project, params)
		}

		// add the list values needed to render this controls as regular control from ControlAngularTab lib
		commonModel.standardFieldSpecs.environment.constraints.put('values', assetEntityService.getAssetEnvironmentOptions())

		return commonModel;
	}
}
