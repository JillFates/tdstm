package net.transitionmanager.strategy.asset

import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.service.SecurityService

import java.text.DateFormat

abstract class AssetSaveUpdateStrategy {

	protected AssetCommand command

	protected Person currentPerson

	protected Project project

	protected static AssetEntityService assetEntityService = Holders.grailsApplication.mainContext.getBean('assetEntityService')

	protected static SecurityService securityService = Holders.grailsApplication.mainContext.getBean('securityService')

	protected final static String DEFAULT_DATE_FORMAT = TimeUtil.FORMAT_DATE_ISO8601


	/**
	 * Constructor that takes a command as argument.
	 * @param assetCommand
	 */
	AssetSaveUpdateStrategy(AssetCommand assetCommand) {
		command = assetCommand
	}

	/**
	 * Save or Update an Asset Instance based on the command object given
	 * when creating the instance.
	 * @return
	 */
	@Transactional
	AssetEntity saveOrUpdateAsset() {
		// Some setting up required before proceeding with the save/update operation.
		prepareForSaving()
		AssetEntity assetEntity = null
		if (isNew()) {
			assetEntity = createAndInitializeAssetEntityInstance()
		} else {
			assetEntity = fetchAsset()
		}

		// Populate the asset.
		populateAsset(assetEntity)

		// Save the asset or fail
		assetEntity.save()

		// Assign the asset to the corresponding Move Bundle.
		assetEntityService.assignAssetToBundle(project, assetEntity, command.asset['moveBundleId'].toString())

		// Create/Update dependencies as needed.
		createOrUpdateDependencies(assetEntity)

		return assetEntity

	}


	/**
	 * Do the actual work of assigning properties, dependencies, the move bundle, etc.
	 * @param assetEntity
	 */
	protected void populateAsset(AssetEntity assetEntity) {
		assetEntity.properties = command.asset
	}

	/**
	 * Initial work required before doing the saving/updating of the asset entity instance.
	 * @return
	 */
	private AssetEntity prepareForSaving() {
		// Determine the corresponding project.
		project = securityService.getUserCurrentProject()
		// Determine the current person
		currentPerson = securityService.loadCurrentPerson()
		// Format command fields.
		formatCommandFields()
	}

	/**
	 * Some fields in the command object require some parsing and/or formatting.
	 */
	protected void formatCommandFields() {
		formatDateFields()
		command.asset.scale = SizeScale.asEnum(command.asset.scale)
	}

	/**
	 * Based on the command object given when instancing this object,
	 * retrieve the corresponding asset (if any).
	 * @return
	 */
	private AssetEntity fetchAsset() {
		Long assetId = NumberUtil.toLong(command.asset.id)
		AssetEntity assetEntity
		if (assetId) {
			assetEntity = GormUtil.findInProject(project, AssetEntity, assetId, true)
		} else {
			throw new InvalidParamException('The requested Asset ID is invalid')
		}
		if (!assetEntity) {
			throw new InvalidParamException('No asset found with the given ID.')
		}
		return assetEntity
	}

	/**
	 * Format all the possible date fields.
	 */
	private void formatDateFields() {
		String[] dateFields = ['maintExpDate', 'purchaseDate', 'retireDate']
		DateFormat dateFormat = TimeUtil.createFormatter(DEFAULT_DATE_FORMAT)
		for (String dateField in dateFields) {
			command.asset[dateField] = TimeUtil.parseDate(command.asset[dateField], dateFormat)
		}
	}

	/**
	 * Determine if a new asset needs to be created or it's just an update operation.
	 * @return
	 */
	protected boolean isNew() {
		return NumberUtil.toLong(command.asset.id) == null
	}

	/**
	 * Create a new Asset Entity instance.
	 * @return
	 */
	protected abstract AssetEntity createAssetInstance()

	/**
	 * Setup initial fields for the newly created Asset Entity.
	 *
	 * @param assetEntity
	 */
	protected void initializeInstance(AssetEntity assetEntity) {
		assetEntity.project = project
		assetEntity.owner = project.client
		assetEntity.assetClass = command.assetClass
		assetEntity.modifiedBy = currentPerson
	}

	/**
	 * Create and initialize a new Asset Entity instance.
	 * @return
	 */
	protected AssetEntity createAndInitializeAssetEntityInstance() {
		AssetEntity assetEntity = createAssetInstance()
		initializeInstance(assetEntity)
		return assetEntity
	}

	/**
	 * Create, update or delete dependencies for the given asset based
	 * on the information provided in the command object.
	 *
	 * @param assetEntity
	 */
	private void createOrUpdateDependencies(AssetEntity assetEntity) {
		// Validate that all dependent/supporting assets belong to the same project.
		validateRelatedAssets()
		// Delete those dependencies not present in the command
		deleteDependencies(assetEntity)
		// Create and/or update supporting assets
		createOrUpdateSupportingAssets(assetEntity)
		// Create and/or update dependents
		createOrUpdateDependentAssets(assetEntity)

	}

	/**
	 * Create or Update the corresponding supporting assets for the given asset instance.
	 * @param assetEntity
	 */
	private void createOrUpdateSupportingAssets(AssetEntity assetEntity) {
		createOrUpdateDependencies(assetEntity, true)
	}

	/**
	 * Create or Update the corresponding dependent assets for the given asset instance.
	 * @param assetEntity
	 */
	private void createOrUpdateDependentAssets(AssetEntity assetEntity) {
		createOrUpdateDependencies(assetEntity, false)
	}

	/**
	 * Create or update all the corresponding dependent or supporting assets.
	 * @param assetEntity
	 * @param isDependent - true: the given asset is the dependent side, false: it's the supporting side.
	 */
	private void createOrUpdateDependencies(AssetEntity assetEntity, boolean isDependent) {
		List depMaps = isDependent ? command.dependencyMap.supportAssets : command.dependencyMap.dependentAssets
		for (depMap in depMaps) {
			createOrUpdateDependency(assetEntity, depMap, isDependent)
		}
	}

	/**
	 * Create or Update a dependency for this asset.
	 * @param assetEntity
	 * @param depMap
	 * @param isDependent
	 */
	@Transactional
	private void createOrUpdateDependency(AssetEntity assetEntity, Map depMap, boolean isDependent) {
		AssetDependency dependency = null
		if (depMap.id) {
			dependency = GormUtil.findInProject(project, AssetDependency, depMap.id, true)
		} else {
			dependency = new AssetDependency()
			dependency.createdBy = currentPerson
		}

		AssetEntity targetAsset = GormUtil.findInProject(project, AssetEntity, depMap.targetAsset.id, true)

		dependency.dependent = isDependent? assetEntity : targetAsset
		dependency.asset = isDependent ? targetAsset : assetEntity
		dependency.dataFlowFreq = depMap.dataFlowFreq
		dependency.status = depMap.status
		dependency.type = depMap.type
		dependency.comment = depMap.comment

		MoveBundle moveBundle = GormUtil.findInProject(project, MoveBundle, depMap.moveBundleId, true)
		// If the selected bundle doesn't match the selected asset's, assign it to that bundle.
		if (targetAsset.moveBundle.id != moveBundle.id) {
			assetEntityService.assignAssetToBundle(project, targetAsset, depMap.moveBundleId.toString())
		}
		dependency.updatedBy = currentPerson
		dependency.save()
	}

	/**
	 * As part of the process of updating an asset's dependencies, we need to remove those
	 * dependencies that the user deleted from the UI.
	 *
	 * In the past, the request contained a list of dependencies to be deleted. This is no longer
	 * the case and we need to infer those ids by comparing existing ids with the ids received
	 * in the request.
	 *
	 * @param assetEntity
	 */
	private void deleteDependencies(AssetEntity assetEntity) {
		// Delete missing supporting dependencies
		deleteDependencies(assetEntity, false)
		// Delete missing dependents
		deleteDependencies(assetEntity, true)
	}

	/**
	 * Determine which dependencies are no longer require and delete them.
	 *
	 * This method works with only one side (dependent or supporting), which is controlled
	 * by the isDependent flag.
	 *
	 * @param assetEntity
	 * @param isDependent - true: the given asset is the dependent side; false: it's the supporting one.
	 */
	private void deleteDependencies(AssetEntity assetEntity, boolean isDependent) {
		// Determine those dependencies no longer valid.
		List<Long> dependents = collectDependencies(isDependent)
		// Delete the dependencies for the asset not included in the dependents list.
		deleteDependencies(dependents, assetEntity, isDependent)
	}

	/**
	 * Collect a list of Dependency IDs (dependent or supporting dependencies)
	 */
	private List<Long> collectDependencies(boolean isDependent) {
		List<Long> dependencies = []
		List depList = isDependent ? command.dependencyMap.supportAssets : command.dependencyMap.dependentAssets
		for (depMap in depList) {
			Long id = NumberUtil.toLong(depMap.id)
			if (id) {
				dependencies << id
			}
		}

		return dependencies
	}

	/**
	 * Validate that all assets associated in the dependencies section (dependent or supporting)
	 * belong to the same project.
	 *
	 */
	private void validateRelatedAssets() {
		// Validate dependent assets.
		List<Long> dependentIds = collectRelatedAssets(true)
		assetEntityService.validateAssetsAssocToProject(dependentIds, project)
		// Validate supporting assets.
		List<Long> supportingIds = collectRelatedAssets(false)
		assetEntityService.validateAssetsAssocToProject(supportingIds, project)
	}

	/**
	 * Collect the list of dependent/supporting assets from the command object.
	 * An exception will be thrown if a duplicate asset is found.
	 *
	 * @param dependents
	 * @return
	 */
	private List<Long> collectRelatedAssets(boolean dependents) {
		List<Long> ids = []
		String label = dependents ? "dependent" : "supporting"
		List<Map> depList = dependents ? command.dependencyMap.dependentAssets : command.dependencyMap.supportAssets
		for (depMap in depList) {
			Long id = depMap.targetAsset.id
			if (id) {
				if (id in ids) {
					throw new InvalidRequestException("Duplicate $label found for asset ${depMap.targetAsset.name}")
				}else {
					ids << id
				}
			}

		}
		return ids
	}

	/**
	 * Delete of the dependencies using a list of ids and an asset, which can be the supporting or the dependent.
	 *
	 * @param ids - dependency ids.
	 * @param assetEntity
	 * @param isDependent - true: the target asset is the dependent side, false if it's the supporting side.
	 */
	private void deleteDependencies(List<Long> ids, AssetEntity assetEntity, boolean isDependent) {
		/* Delete dependencies. As an extra precaution, the query doesn't simply delete dependencies
		* given their id, but also takes the asset being edited into account.*/
		String side = isDependent ? 'dependent' : 'asset'
		String hql = "DELETE FROM AssetDependency ad WHERE $side = :asset"
		Map params = [asset: assetEntity]
		// Add the NOT IN condition only if the dependents list is not empty
		if (ids) {
			hql += " AND id NOT IN (:ids)"
			params['ids'] = ids
		}
		AssetDependency.executeUpdate(hql, params)
	}




	/**
	 * Create and return the appropriate instance for saving/updating
	 * an asset based on the given command object.
	 * @param command
	 * @return
	 */
	static AssetSaveUpdateStrategy getInstanceFor(AssetCommand command) {
		AssetSaveUpdateStrategy strategy = null
		switch(command.assetClass){
		// Applications
			case AssetClass.APPLICATION:
				strategy = new ApplicationSaveUpdateStrategy(command)
				break
		// Databases
			case AssetClass.DATABASE:
				strategy = new DatabaseSaveUpdateStrategy(command)
				break
		// Files
			case AssetClass.STORAGE:
				strategy = new StorageSaveUpdateStrategy(command)
				break
		// Devices
			case AssetClass.DEVICE:
				strategy = new DeviceSaveUpdateStrategy(command)
				break
		}
		return strategy
	}
}
