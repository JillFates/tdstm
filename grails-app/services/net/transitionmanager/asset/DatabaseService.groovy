package net.transitionmanager.asset

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.DataViewMap
import com.tdssrc.grails.GormUtil
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.imports.DataviewService
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods

@Transactional
class DatabaseService implements ServiceMethods {

	AssetEntityService assetEntityService
	DataviewService    dataviewService

	/**
	 * Used to retrieve a model map of the properties to display a database asset
	 * @param project - the project that the user is associated with
	 * @param db - the database object that the user is attempting to look at
	 * @param params - request parameters
	 * @param includeDependencies - whether or not dependencies should be included in the model.
	 */
	@NotTransactional
	Map getModelForShow(Project project, Database db, Map params, boolean includeDependencies = true) {
		Map commonModel = assetEntityService.getCommonModel(false, project, db, 'Database', params, includeDependencies)
		List fields =  dataviewService.fetch(DataViewMap.DATABASES.id).toMap(project, securityService.userLoginPerson).schema.columns.collect{it.label}

		return [
				   databaseInstance: db,
				   currentUserId   : securityService.currentPersonId,
				   fields          : fields
			   ] + commonModel
	}

	/**
	 * Used to retrieve a model map of the properties to display a database asset
	 * @param params - request parameters
	 */
	@Transactional(readOnly = true)
	Map getModelForCreate(Map params=null) {
		Project project = securityService.getUserCurrentProject()
		Database database = new Database(project: project)
		Map commonModel = assetEntityService.getCommonModel(true, project, database, 'Database', params)

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
}
