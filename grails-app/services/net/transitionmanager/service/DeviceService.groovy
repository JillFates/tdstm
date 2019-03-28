package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SizeScale
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission

@Transactional
class DeviceService implements ServiceMethods {

	AssetEntityService assetEntityService
	ProjectService projectService
	RackService rackService
	RoomService roomService
	UserPreferenceService userPreferenceService

	/**
	 * Assigns a DEVICE asset to a location/room/rack appropriately. If the referenced room or rack
	 * does not exists then it will be created.
	 *
	 * Note that the function does NOT commit the change to the Asset and is left up to the caller.
	 *
	 * @param asset - the asset being associated
	 * @param location - the location to assign the asset to
	 * @param roomName - the room name to assign the asset to
	 * @param rackName - the rackname to assign the asset to
	 * @param isSource - flag that when true indicates that the associate is to the source otherwise to the target
	 * @return Null if successful otherwise a string indicating the error
	 */
	String assignDeviceToLocationRoomRack(AssetEntity asset, String location, String roomName, String rackName, boolean isSource) {
		log.debug('assignDeviceToLocationRoomRack() START {} {}/{}/{}/{}', asset, location, roomName, rackName, isSource)

		if (!asset?.project) {
			return 'Asset project property not properly set'
		}

		if (asset.assetClass != AssetClass.DEVICE) {
			return 'Asset class is invalid '
		}

		// Start by finding or creating the room
		def room = roomService.findOrCreateRoom(asset.project, location, roomName, rackName, isSource)
		if (!room) {
			return "Unable to create room $location/$roomName (${isSource ? 'Source' : 'Target'})"
		}
		if (isSource)
			asset.roomSource = room
		else
			asset.roomTarget = room




    /* @See TM-7806
       If an asset is of type 'VM', 'Virtual' or 'Blade',
       this should not be tracked at the rack level, since this are not physical devices. */
    String assetType = asset.assetType.toUpperCase()
    if (!(assetType == 'VM' || assetType == 'VIRTUAL' || assetType == 'BLADE')) {
       // Look for the rack or have created on the fly
       def rack = rackService.findOrCreateRack(room, rackName)
       if (!rack) {
          return "Unable to create rack $location/$roomName/$rackName (${isSource ? 'Source' : 'Target'})"
       }
       if (isSource) {
          asset.rackSource = rack
       }
       else {
          asset.rackTarget = rack
       }
    }


		log.debug 'assignDeviceToLocationRoomRack() END {} {}/{}/{}', asset, asset.sourceLocationName,
				asset.sourceRoomName, asset.sourceRackName
		return null
	}

	/**
	 * Used to provide a map/model of the properties used by the DEVICE show view
	 * @param project
	 * @return a Map that includes the list of common properties
	 */
	@Transactional(readOnly = true)
	Map getModelForShow(Project project, assetEntity, Map params) {

		boolean deleteChassisWarning = false
		if (assetEntity.isaChassis()) {
			int count = AssetEntity.executeQuery(
					"SELECT COUNT(*) FROM AssetEntity WHERE sourceChassis=:sc OR targetChassis=:tc",
					[sc: assetEntity, tc: assetEntity])[0]
			deleteChassisWarning = count > 0
		}
		Map commonModel = this.getCommonModel(false, project, assetEntity, params)
		Map model = [
			assetEntity: assetEntity,
			/*label: frontEndLabel,*/
			canEdit: securityService.hasPermission(Permission.AssetEdit),
			deleteChassisWarning: deleteChassisWarning,
			currentUserId: securityService.currentPersonId
		] + commonModel

		model.roomSource = null
		model.roomTarget = null

		if (assetEntity.isaBlade()) {
			AssetEntity sourceChassis = assetEntity.sourceChassis
			AssetEntity targetChassis = assetEntity.targetChassis
			model.sourceChassis = sourceChassis ? sourceChassis.assetName + '/' + sourceChassis.assetTag : ''
			model.targetChassis = targetChassis ? targetChassis.assetName + '/' + targetChassis.assetTag : ''
			model.roomSource = sourceChassis?.roomSource
			model.roomTarget = targetChassis?.roomTarget
		} else {
			model.sourceChassis = ''
			model.targetChassis = ''

			if (!assetEntity.isaVM()) {
				// Rackable item, VM's are not tracked by room - just location
				model.roomSource = assetEntity.roomSource
				model.roomTarget = assetEntity.roomTarget
			}
		}

		/*
		// Stick questionmark on the end of the model name if it is unvalidated
		String modelName = device.model?.modelName ?: 'Undetermined'
		if (!device.model?.isValid())
			modelName += ' ?'
		model.modelName = modelName
		*/

		if (params.redirectTo == "roomAudit") {
			model.source = params.source
			model.assetType = params.assetType
		}

		return model
	}

	/**
	 * Used to provide a map/model of the properties used by the DEVICE Create view
	 * @param project
	 * @return a Map that includes the list of common properties
	 */
	@Transactional(readOnly = true)
	Map getModelForCreate(Map params=null) {
		Project project = securityService.getUserCurrentProject()
		AssetEntity assetEntity = new AssetEntity(project: project)
		Map model = this.getCommonModel(true, project, assetEntity, params)

		model.assetInstance = assetEntity
		model.roomSource = null
		model.roomTarget = null
		model.sourceChassis = ''
		model.targetChassis = ''

		return model
	}

	/**
	 * Used to save a new device which is called from the controller
	 * @param project - the user's project
	 * @param params - the request parameters
	 * @return The device asset that was created
	 */
	AssetEntity saveAssetFromForm(Project project, Map params) {
		saveUpdateAssetFromForm(project, params, new AssetEntity())
	}

	/**
	 * Used to save a new device which is called from the controller
	 * @param project - the user's project
	 * @param params - the request parameters
	 * @param assetId - the id of the asset being updated
	 * @return The device asset that was created
	 */
	AssetEntity updateAssetFromForm(Project project, Map params, Long assetId) {
		AssetEntity asset = AssetEntityHelper.getAssetById(project, AssetClass.DEVICE, assetId)
		if (!asset) {
			throw new RuntimeException("updateAssetFromForm() unable to locate device id $assetId")
		}

		return saveUpdateAssetFromForm(project, params, asset)
	}

	/**
	 * Used to update a device which is called from the controller
	 * @param device - the device to update
	 * @param params - the request parameters
	 * TODO : JPM 10/2014 : refactor updateAssetFromForm into the DeviceService class
	 */
	private AssetEntity saveUpdateAssetFromForm(Project project, Map params, AssetEntity asset) {
		// If it is a new asset then we need to do some things
		if (!asset.id) {
			asset.project = project
			asset.owner = project.client
			// Removed by TM-6779 - this is not being used since field specs fields implementation
			// asset.attributeSet = EavAttributeSet.get(1)
			asset.assetClass = AssetClass.DEVICE
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access device $asset.id not belonging to current project $project")
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		asset.modifiedBy = securityService.loadCurrentPerson()

		applyParamsToDevice(project, asset, params)
		/*
		save asset, true
		if (asset.hasErrors()) {
			throw new DomainUpdateException('Unable to update asset ' + GormUtil.allErrorsString(asset))
		}
		*/
		assetEntityService.createOrUpdateAssetEntityAndDependencies(asset.project, asset, params)

		saveUserPreferencesForDevice(asset)

		return asset
	}

	/**
	 * used by the create and update device service methods to persist changes to a new or existing device asset
	 * @param device - the device object to be persisted which maybe a new or existing asset
	 * @param params - the request parameters
	 */
	private AssetEntity applyParamsToDevice(Project project, AssetEntity device, Map params) {
		boolean isNew = !device.id

		if (isNew) {
			device.project = project
			device.owner = project.client
		} else {
			// Validate the optimistic locking to prevent two people from editing the same EXISTING asset
			GormUtil.optimisticLockCheck(device, params, 'Device')
		}

		//
		// Set some of the params that need to be massaged from their string state into something more appropriate
		//
		params.scale = SizeScale.asEnum(params.scale)

		AssetEntityService.ASSET_INTEGER_PROPERTIES.each { p ->
			if (params.containsKey(p)) {
				params[p] = NumberUtil.toInteger(params[p])
			}
		}

		// Assign all of the standard properties to the device class
		(AssetEntityService.ASSET_PROPERTIES + AssetEntityService.CUSTOM_PROPERTIES + AssetEntityService.DEVICE_PROPERTIES).each { p ->
			if (params.containsKey(p)) {
				if (AssetEntityService.ASSET_DATE_PROPERTIES.contains(p)) {
					if (params[p] instanceof String) {
						if (StringUtil.isBlank(params[p])) {
							device[p] = null
						} else {
							device[p] = TimeUtil.parseDate(params[p])
						}
					} else {
						device[p] = params[p]
					}
				} else {
					device[p] = params[p]
				}
			}
		}

		// Would prefer using the bindData but have been having all sorts of issues with it
		// device.properties = params

		//
		// The following are not handled by the bindData assignment and need to be handled directly
		//
		device.modifiedBy = securityService.loadCurrentPerson()

		if (!device.assetTag?.size()) {
			device.assetTag = projectService.getNextAssetTag(project)
		}

		assetEntityService.assignAssetToBundle(project, device, params['moveBundle.id'])

		// Update the Manufacturer and/or Model for the asset based on what the user has selected. If the model was selected
		// then we'll use that for everything otherwise we will set the manufacturer from the form.

		if (params.modelId) {
			// Check to see if the user gave us a valid model and then assign to the asset
			def model = Model.get(params.modelId)
			device.model = model
			device.manufacturer = model.manufacturer
			device.assetType = model.assetType
		} else {
			device.model = null
			if (params.manufacturerId) {
				// Attempt to assign just the manufacturer and asset type to the device
				def manu = Manufacturer.get(params.manufacturerId)
				device.manufacturer = manu
				// assetType was applied in the bind above
			} else {
				device.manufacturer = null
			}

			device.assetType = params.currentAssetType ?: null
		}

		// Set the source/target location/room which creates the Room if necessary
		assetEntityService.assignAssetToRoom(project, device, params.roomSourceId, params.locationSource, params.roomSource, true)
		assetEntityService.assignAssetToRoom(project, device, params.roomTargetId, params.locationTarget, params.roomTarget, false)

		assetEntityService.assignDeviceToChassisOrRack(project, device, params)

		if (!device.save(failOnError: false)) {
			throw new DomainUpdateException('Unable to update device ' + GormUtil.allErrorsString(device))
		}
	}

	private void saveUserPreferencesForDevice(AssetEntity device) {
		// Set some preferences if we were successful
		if (device.manufacturer) {
			userPreferenceService.setPreference(PREF.LAST_MANUFACTURER, device.manufacturer.name)
		}
		if (device.model) {
			userPreferenceService.setPreference(PREF.LAST_TYPE, device.model.assetType)
		}
	}

	/**
	 * Used to get the model map used to render the create view of an Device domain asset
	 * @param forCreate - is model for create or show/edit
	 * @param project - the project of the user
	 * @param assetEntity - current assetEntity
	 * @param params - request parameters
	 * @return a map of the properties containing the list values to populate the list controls
	 */
	private Map getCommonModel(Boolean forCreate, Project project, AssetEntity assetEntity , Map params) {
		Map commonModel = assetEntityService.getCommonModel(forCreate, project, assetEntity, 'AssetEntity', params)
		commonModel.standardFieldSpecs.priority.constraints.put('values', assetEntityService.getAssetPriorityOptions())

		return commonModel;
	}
}
