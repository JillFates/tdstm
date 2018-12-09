package net.transitionmanager.service

import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import net.transitionmanager.domain.Project

@Transactional
class DatabaseService implements ServiceMethods {

	AssetEntityService assetEntityService

	/**
	 * Used to retrieve a model map of the properties to display a database asset
	 * @param project - the project that the user is associated with
	 * @param db - the database object that the user is attempting to look at
	 * @param params - request parameters
	 */
	@NotTransactional
	Map getModelForShow(Project project, Database db, Map params) {
		Map commonModel = this.getCommonModel(false, project, db, params)

		[databaseInstance: db, currentUserId: securityService.currentPersonId] + commonModel
	}

	/**
	 * Used to retrieve a model map of the properties to display a database asset
	 * @param params - request parameters
	 */
	@Transactional(readOnly = true)
	Map getModelForCreate(Map params=null) {
		Project project = securityService.getUserCurrentProject()
		Database database = new Database(project: project)
		Map commonModel = this.getCommonModel(true, project, database, params)

		return [assetInstance: database] + commonModel
	}

	/**
	 * Used to create a new Database asset that is called from the controller
	 * @param params - the request parameters
	 * @param device - the device to update
	 */
	Database saveAssetFromForm(Project project, Map params) {
		return saveUpdateAssetFromForm(project, params, new Database())
	}

	/**
	 * Used to update a Database asset that is called from the controller
	 * @param params - the request parameters
	 * @param id - the id of the asset to update
	 */
	Database updateAssetFromForm(Project project, Map params, Long id) {
		def asset
		if (id) {
			asset = Database.get(id)
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
	 * @param params - the request parameters
	 * @param device - the device to update
	 */
	private Database saveUpdateAssetFromForm(Project project, Map params, Database asset) {
		boolean isNew = ! asset.id
		if (isNew) {
			asset.project = project
			asset.owner = project.client
			// Removed by TM-6779 - this is not being used since field specs fields implementation
			// asset.attributeSet = EavAttributeSet.get(3)
			asset.assetClass = AssetClass.DATABASE
			asset.assetType = AssetType.DATABASE
			// asset.createdBy = userLogin.person
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access asset $asset.id not belonging to current project $project.id")
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		// Validate the optimistic locking to prevent two people from editing the same EXISTING asset
		if (!isNew) {
			GormUtil.optimisticLockCheck(asset, params, 'Database')
		}

		asset.modifiedBy = securityService.loadCurrentPerson()

		// Update the date fields to proper dates

		asset.properties = params

		assetEntityService.assignAssetToBundle(project, asset, params['moveBundle.id'])

		assetEntityService.createOrUpdateAssetEntityAndDependencies(asset.project, asset, params)

		return asset
	}

	/**
	 * Used to get the model map used to render the create view of an Database domain asset
	 * @param forCreate - is model for create or show/edit
	 * @param project - the project of the user
	 * @param database - current database
	 * @param params - request parameters
	 * @return a map of the properties containing the list values to populate the list controls
	 */
	private Map getCommonModel(Boolean forCreate, Project project, database , Map params) {
		Map commonModel

		if (forCreate) {
			commonModel = assetEntityService.getCommonModelForCreate('Database', project, database)
		} else {
			commonModel = assetEntityService.getCommonModelForShows('Database', project, params)
		}

		// add the list values needed to render this controls as regular control from ControlAngularTab lib
		commonModel.standardFieldSpecs.environment.constraints.put('values', assetEntityService.getAssetEnvironmentOptions())

		return commonModel;
	}
}
