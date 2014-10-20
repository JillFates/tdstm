import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.eav.EavAttributeOption

class DeviceService {

	boolean transactional = true

	def rackService
	def roomService	
	def assetEntityService

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


}