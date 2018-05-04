package net.transitionmanager.strategy.asset

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.util.Holders
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.SecurityService

abstract class AssetSaveUpdateStrategy {

	protected AssetCommand command

	protected Person currentPerson

	protected Project project

	protected static AssetEntityService assetEntityService = Holders.grailsApplication.mainContext.getBean('assetEntityService')

	protected static SecurityService securityService = Holders.grailsApplication.mainContext.getBean('securityService')


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
		assetEntity.save(failOnError: true)

		// Assign the asset to the corresponding Move Bundle.
		assetEntityService.assignAssetToBundle(project, assetEntity, command.asset['moveBundleId'].toString())

		// Create/Update dependencies as needed.
		//assetEntityService.createOrUpdateAssetEntityAndDependencies(project, assetEntity, command.dependencyMap)

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
			assetEntity = GormUtil.findInProject(project, AssetEntity, assetId)
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
		for (String dateField in dateFields) {
			command.asset[dateField] = TimeUtil.parseDate(command.asset['dateField'])
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
