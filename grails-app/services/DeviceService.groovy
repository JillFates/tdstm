import com.tds.asset.AssetEntity
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil

class DeviceService {

	boolean transactional = true

	def assetEntityService
	def projectService
	def rackService
	def roomService	
	def userPreferenceService

	/** 
	 * Used to assign a DEVICE asset to a location/room/rack appropriately. If the referenced room or rack 
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
		log.debug("assignDeviceToLocationRoomRack() START $asset $location/$roomName/$rackName/$isSource")

		if (!asset?.project) {
			return 'Asset project property not properly set'
		}

		if (asset.assetClass != AssetClass.DEVICE) {
			return 'Asset class is invalid '
		}

		// Start by finding or creating the room
		def params = [
			project: asset.project,
			location: location,
			roomName: roomName
		]
		def room = roomService.findOrCreateRoom(asset.project, location, roomName, rackName, isSource)
		if (!room) {
			return "Unable to create room $location/$roomName (${isSource ? 'Source' : 'Target'})"
		}
		if (isSource)
			asset.roomSource = room
		else
			asset.roomTarget = room

		// Look for the rack or have created on the fly
		def rack = rackService.findOrCreateRack(room, rackName)
		if (! rack) {
			return "Unable to create rack $location/$roomName/$rackName (${isSource ? 'Source' : 'Target'})"
		}

		if (isSource)
			asset.rackSource = rack
		else
			asset.rackTarget = rack

		log.debug "assignDeviceToLocationRoomRack() END $asset ${asset.sourceLocation}/${asset.sourceRoom}/${asset.sourceRack}"
		return null
	}

	/**
	 * Used to provide a map/model of the properties used by the DEVICE show view
	 * @param project 
	 * @return a Map that includes the list of common properties
	 */
	Map getModelForShow(Project project, assetEntity, Object params) {

		def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntity.attributeSet.id order by eav.sortOrder ")
		def attributeOptions
		def options
		def frontEndLabel

		entityAttributeInstance.each{
			attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute,[sort:'value',order:'asc'] )
			options = []
			attributeOptions.each{option ->
				options<<[option:option.value]
			}
			if( !assetEntityService.bundleMoveAndClientTeams.contains(it.attribute.attributeCode) && it.attribute.attributeCode != "currentStatus" && it.attribute.attributeCode != "usize" ){
				frontEndLabel = it.attribute.frontendLabel
				if( assetEntityService.customLabels.contains( frontEndLabel ) ){
					frontEndLabel = project[it.attribute.attributeCode] ? project[it.attribute.attributeCode] : frontEndLabel
				}
			}
		}

		def model = [
			assetEntity: assetEntity, 
			label: frontEndLabel
		]

		model.putAll( assetEntityService.getCommonModelForShows('AssetEntity', project, params, assetEntity) )

		model.roomSource=null
		model.roomTarget=null

		if (assetEntity.isaBlade()) {
			model.sourceChassis = ( assetEntity.sourceChassis ? "${assetEntity.sourceChassis.assetName}/${assetEntity.sourceChassis.assetTag}" : '' )
			model.targetChassis = ( assetEntity.targetChassis ? "${assetEntity.targetChassis.assetName}/${assetEntity.targetChassis.assetTag}" : '' )
			model.roomSource = assetEntity.sourceChassis?.roomSource
			model.roomTarget = assetEntity.targetChassis?.roomTarget
		} else {
			model.sourceChassis = ''
			model.targetChassis = ''

			if ( ! assetEntity.isaVM() ) {
				// Rackable item, VM's are not tracked by room - just location
				model.roomSource = assetEntity.roomSource
				model.roomTarget = assetEntity.roomTarget
			}
		}

		if (params.redirectTo == "roomAudit") {
			model << [source:params.source, assetType:params.assetType]
		}

		return model
	}

	/**
	 * Used to save a new device which is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param projectId - the id of the user's project
	 * @param userId - the id of the current user
	 * @param params - the request parameters
	 * @return The device asset that was created
	 * @throws various RuntimeExceptions if there are any errors
	 */
	AssetEntity saveAssetFromForm(controller, session, Long projectId, Long userId, params) {
		def asset = new AssetEntity( )

		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
		
	}

	/**
	 * Used to save a new device which is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param projectId - the id of the user's project
	 * @param userId - the id of the current user
	 * @param params - the request parameters
	 * @param assetId - the id of the asset being updated
	 * @return The device asset that was created
	 * @throws various RuntimeExceptions if there are any errors
	 */
	AssetEntity updateAssetFromForm(controller, session, Long projectId, Long userId, params, Long assetId ) {
		Project project = Project.read(projectId)
		AssetEntity asset = AssetEntityHelper.getAssetById(project, AssetClass.DEVICE, assetId)

		if (!asset)
			throw new RuntimeException("updateAssetFromForm() unable to locate device id $assetId")

		return saveUpdateAssetFromForm(controller, session, projectId, userId, params, asset)
	}

	/**
	 * Used to update a device which is called from the controller
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param device - the device to update
	 * @param params - the request parameters
	 * @throws various RuntimeExceptions if there are any errors
	 * TODO : JPM 10/2014 : refactor updateAssetFromForm into the DeviceService class
	 */
	private AssetEntity saveUpdateAssetFromForm(controller, session, Long projectId, Long userId, params, AssetEntity asset ) {
		Project project = Project.get(projectId)
		UserLogin userLogin = UserLogin.get(userId)

		// If it is a new asset then we need to do some things
		if (! asset.id) {
			asset.project = project
			asset.owner = project.client
			asset.attributeSet = EavAttributeSet.get(1)
			asset.assetClass = AssetClass.DEVICE
		} else if (asset.project != project) {
			securityService.reportViolation("Attempted to access device ${asset.id} not belonging to current project $project", userLogin)
			throw new RuntimeException("updateDeviceFromForm() user access violation")
		}

		// Save who changed it
		asset.modifiedBy = userLogin.person

		applyParamsToDevice(controller, session, project, userLogin, asset, params)
		
		if (! asset.validate() || ! asset.save(flush:true)) {
			log.debug "Unable to update device ${GormUtil.allErrorsString(asset)}"
			throw new DomainUpdateException("Unable to update asset ${GormUtil.allErrorsString(asset)}".toString())
		}

		def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, asset, userLogin, asset.project)
		if (errors) {
			throw new DomainUpdateException("Unable to update dependencies : $errors".toString())
		}

		saveUserPreferencesForDevice(asset)

		return asset
	}

	/**
	 * used by the create and update device service methods to persist changes to a new or existing device asset
	 * @param controller - the controller that called the method
	 * @param session - the user's selected project
	 * @param device - the device object to be persisted which maybe a new or existing asset
	 * @param params - the request parameters
	 * @throws various RuntimeExceptions if there are any errors
	 */
	private AssetEntity applyParamsToDevice(controller, session, Project project, UserLogin userLogin, AssetEntity device, params) {
		boolean isNew = ! device.id

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
			if (params.containsKey(p))
				params[p] = NumberUtil.toInteger(params[p])
		}

		// Assign all of the standard properties to the device class
		(AssetEntityService.ASSET_PROPERTIES + AssetEntityService.CUSTOM_PROPERTIES + AssetEntityService.DEVICE_PROPERTIES).each { p ->
			if (params.containsKey(p))
				device[p] = params[p]
		}

		// Would prefer using the bindData but have been having all sorts of issues with it
		// device.properties = params

		//
		// The following are not handled by the bindData assignment and need to be handled directly
		//
		device.modifiedBy = userLogin.person

		if (! device.assetTag?.size())
			device.assetTag = projectService.getNextAssetTag(project) 

		assetEntityService.assignAssetToBundle(project, device, params['moveBundle.id'])

		// Update the Manufacturer and/or Model for the asset based on what the user has selected. If the model was selected 
		// then we'll use that for everything otherwise we will set the manufacturer from the form. 
		def manuId = NumberUtil.toLong(params.manufacturerId)
		def modelId = NumberUtil.toLong(params.modelId)

		if (modelId) {
			// Check to see if the user gave us a valid model and then assign to the asset
			def model = Model.get(modelId)
			if (model) {
				device.model = model
				device.manufacturer = model.manufacturer
				device.assetType = model.assetType
			} else {
				throw new DomainUpdateException("The model specified was not found")
			}
		} else if (manuId) {
			// Attempt to assign just the manufacturer and asset type to the device
			def manu = Manufacturer.get(manuId)
			if (manu) {
				device.model = null
				device.manufacturer = manu
				// assetType was applied in the bind above
			} else {
				throw new DomainUpdateException("The manufacturer specified was not found")
			}
		}
		
		// Set the source/target location/room which creates the Room if necessary
		assetEntityService.assignAssetToRoom(project, device, params.roomSourceId, params.sourceLocation, params.sourceRoom, true) 
		assetEntityService.assignAssetToRoom(project, device, params.roomTargetId, params.targetLocation, params.targetRoom, false) 

		assetEntityService.assignDeviceToChassisOrRack(project, userLogin, device, params)

		if ( ! device.validate() || ! device.save(flush:true) ) {
			log.debug "Unable to update device ${GormUtil.allErrorsString(device)}"
			throw new DomainUpdateException("Unable to update device ${GormUtil.allErrorsString(device)}".toString())
		}

	}

	/**
	 * Used to set some user preferences for manufacturer and type after creating or editing devices
	 * @param device - the device object that was created or saved
	 */
	private void saveUserPreferencesForDevice(AssetEntity device) {
		// Set some preferences if we were successful
		if (device.manufacturer)
			userPreferenceService.setPreference("lastManufacturer", device.manufacturer.name)
		if (device.model) 
			userPreferenceService.setPreference("lastType", device.model.assetType)
	}

}