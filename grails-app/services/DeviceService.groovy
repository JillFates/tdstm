import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil

class DeviceService {

	boolean transactional = true

	def rackService
	def roomService	

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

}