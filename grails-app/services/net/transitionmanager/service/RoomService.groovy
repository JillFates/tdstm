package net.transitionmanager.service

import net.transitionmanager.asset.AssetEntity
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.RoomCommand
import net.transitionmanager.model.Model
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.security.Permission
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.dao.DataIntegrityViolationException

@Transactional
class RoomService implements ServiceMethods {

	RackService rackService
	UserPreferenceService userPreferenceService

	/**
	 * Used to update the information about Rooms, Racks and the devices within the racks
	 * @param project - the project that the user is accessing
	 * @param params - the parameters map passed in from the browser with all of the data
	 * @return The error message if the update failed otherwise assume that the function was successful
	 */
	String updateRoomAndRacksInfo(Project project, roomId, List rackIds, Map params, String powerType) {

		String username = securityService.currentUsername

		def msg

		try {
			while (true) {

				if (!securityService.hasPermission(Permission.RoomEdit)) {
					log.warn "SECURITY : User $username attempted to edit a room without permission for project $project"
					msg = 'Sorry but you do not appear to have the security rights to modify Room and Rack information'
					break
				}

				if (! roomId || ! roomId.isInteger() ) {
					log.warn "SECURITY : User $username attemtped to edit room with invalid room id ($roomId)"
					msg = 'Sorry but the room reference id appears to be invalid'
					break
				}

				def roomInstance = Room.get(roomId)
				if (roomInstance.project != project) {
					log.warn "SECURITY : User $username attempted to update a room not associated with project $project (roomId=$params.id)"
					msg = 'Unable to locate the room specified'
					break
				}

				def source = params.addTargetRoom == "on" ? 0 : 1

				// Update the room information
				roomInstance.roomName = params.roomName
				roomInstance.location = params.location
				roomInstance.roomWidth = params.roomWidth ? NumberUtils.toDouble(params.roomWidth,0).round() : null
				roomInstance.roomDepth = params.roomDepth ? NumberUtils.toDouble(params.roomDepth,0).round() : null
				roomInstance.address = params.address
				roomInstance.city = params.city
				roomInstance.stateProv = params.stateProv
				roomInstance.postalCode = params.postalCode
				roomInstance.country = params.country
				roomInstance.source = source

				if (! roomInstance.save(failOnError: false)) {
					log.info "Updating room information failed - ${GormUtil.allErrorsString(roomInstance)} - user $username"
					msg = "Updating room failed due to ${GormUtil.allErrorsString(roomInstance)}"
					break
				}

				// Deal with updating the existing racks
				def racks = Rack.findAllByRoom( roomInstance )
				racks.each { rack->
					if (! msg && rackIds?.contains(rack.id.toString())) {
						rack.tag = params["tag_"+rack.id]
						rack.location = roomInstance.location
						rack.roomX = params["roomX_"+rack.id] ? NumberUtils.toDouble(params["roomX_"+rack.id],0).round() :0
						rack.roomY = params["roomY_"+rack.id] ? NumberUtils.toDouble(params["roomY_"+rack.id],0).round() :0
						rack.powerA = params["powerA_"+rack.id] ? NumberUtils.toDouble(params["powerA_"+rack.id],0).round() : 0
						rack.powerB = params["powerB_"+rack.id] ? NumberUtils.toDouble(params["powerB_"+rack.id],0).round() : 0
						rack.powerC = params["powerC_"+rack.id] ? NumberUtils.toDouble(params["powerC_"+rack.id],0).round() : 0
						if(powerType != "Watts"){
							rack.powerA = Math.round(rack.powerA * 120)
							rack.powerB = Math.round(rack.powerB * 120)
							rack.powerC = Math.round(rack.powerC * 120)
						}
						rack.rackType = params["rackType_"+rack.id]
						rack.front = params["front_"+rack.id]
						def model = params["model_"+rack.id] != "null" ?  Model.get(params["model_"+rack.id]) : null
						rack.manufacturer = model?.manufacturer
						rack.model = model

						if (! rack.save(failOnError: false) ) {
							log.info "Updating rack information failed - ${GormUtil.allErrorsString(rack)} - user $username"
							msg = "Updating rack ($rack.tag) failed due to ${GormUtil.allErrorsString(rack)}"
							return
						}
					} else {
						// If the submit doesn't have the rack id in it then it will blow away the rack
						// TODO - change the code to only delete explicitely TM-3066

						AssetEntity.executeUpdate("UPDATE AssetEntity SET rackSource=null, rackTarget=null WHERE rackSource=$rack.id")
						rack.delete(flush:true)
					}
				}

				if (msg) break

				// Add any new racks that were added
				rackIds.each { id ->
					if ( id < params.rackCount && ! msg ) {
						def rack = Rack.get( id )
						if (! rack && ! msg) {
							def newRack = Rack.findOrCreateWhere(source: source, project: roomInstance.project,
							                                     location: roomInstance.location, room: roomInstance,
							                                     tag: params["tag_"+id])
							if (newRack) {
								newRack.location = roomInstance.location
								newRack.roomX = params["roomX_"+id] ? NumberUtils.toDouble(params["roomX_"+id],0).round() :0
								newRack.roomY = params["roomY_"+id] ? NumberUtils.toDouble(params["roomY_"+id],0).round() :0
								newRack.powerA = params["powerA_"+id] ? NumberUtils.toDouble(params["powerA_"+id],0).round() : 0
								newRack.powerB = (params["powerB_"+id]) ? NumberUtils.toDouble(params["powerB_"+id],0).round() : 0
								newRack.powerC = (params["powerC_"+id]) ? NumberUtils.toDouble(params["powerC_"+id],0).round() : 0
								def model = params["model_"+id] != "null" ?  Model.get(params["model_"+id]) : null
								// Find default model
								if (model == null) {
									model = rackService.getDefaultRackModel()
								}
								newRack.manufacturer = model?.manufacturer
								newRack.model = model
								if(powerType != "Watts"){
									newRack.powerA = Math.round(newRack.powerA * 120)
									newRack.powerB = Math.round(newRack.powerB * 120)
									newRack.powerC = Math.round(newRack.powerC * 120)
								}
								newRack.rackType = params["rackType_"+id]
								newRack.front = params["front_"+id]

								if (! newRack.save(failOnError: false) ) {
									log.info "Adding new rack failed - ${GormUtil.allErrorsString(newRack)} - user $username"
									msg = "Adding new rack ($newRack.tag) failed due to ${GormUtil.allErrorsString(newRack)}"
									return
								}
							}
						}
					}
				}
				break

			}	// while (true)
		} catch (Exception ex) {
			msg = "An unexpected error occurred during the update"
			log.error "Updating Rooms and Racks failed with exception ${ex.message ?: ex} \n${ExceptionUtil.stackTraceToString(ex,60)}"
		}

		return msg
	}

	/**
	 * Used to retrieve an existing room or create it on demand. Here are a few rules:
	 *    1. If the location, roomName and rackName are blank then nothing is done
	 *    2. If any of the parameters are not blank then a Room will be created
	 *    3. If either location or roomName is blank then they will default to 'TBD'
	 * @param project - the project that the room is associated with
	 * @param location - the location name
	 * @param roomName - the name of the room to lookup/create
	 * @param rackName - the name of the rack which is used to help determine if a room is created
	 * @param isSource - a flag to indicate if the room is a source (true) or target (false)
	 * @return The room object that was found or created
	 */
	Room findOrCreateRoom(Project project, String location, String roomName, String rackName, boolean isSource) {
		// log.debug "findOrCreateRoom() project: $project, $location/$roomName/$isSource"
		def room

		if (StringUtils.isNotBlank(location) || StringUtils.isNotBlank(roomName) || StringUtils.isNotBlank(rackName)) {

			def query = 'from Room r where project=:project and location=:location and roomName=:roomName and source=:source'
			def params = [
				project: project,
				location: StringUtil.defaultIfEmpty(location, 'TBD'),
				roomName: StringUtil.defaultIfEmpty(roomName, 'TBD'),
				source: (isSource ? 1 : 0)
			]

			room = Room.find(query, params)

			// Attempt to create a new room if it doesn't exist
			if( !room ) {
				room = new Room( params )
				if (! room.save(flush:true, failOnError: false) ) {
					log.error "findOrCreateRoom() Unable to create room $project, $location, $roomName, $isSource : ${GormUtil.allErrorsString(room)}"
					room = null
				}
				log.debug "findOrCreateRoom() Created room $room for project $project"
			}
		}
		return room
	}

	/**
	 * Used to delete a room, which will also delete an empty racks or room objects at the same time
	 * @param project - the project that the room belongs to
	 * @param roomIds - an id or a list of ids of the room to be deleted
	 */
	List deleteRoom(Project project, roomIds) {
		securityService.requirePermission Permission.RoomDelete

		def skippedRooms = []
		def userPrefRoom = userPreferenceService.getPreference(PREF.CURR_ROOM)
		roomIds = CollectionUtils.asList(roomIds)

		String username = securityService.currentUsername

		log.info "User $username is deleting room(s) $roomIds"

		roomIds.each { roomId ->
			if ( roomId instanceof String) {
				if (! roomId.isLong()) {
					log.warn "SECURITY : $username attempted to delete rooms with an invalid id ($roomId), project $project"
					throw new InvalidParamException("An invalid room id was received ($roomId)")
				}
			}

			def room = Room.get(roomId)
			if (! room) {
				skippedRooms << [roomId: 'Missing']
				return
			}

			if (room.project != project) {
				log.warn "SECURITY : $username attempted to delete rooms associated to another project, room $room, project $project"
				skippedRooms << [roomId: 'Missing']
				return
			}

			// Check to see if the room has any associated devices
			// if (AssetEntity.findByRoomSource(room) || AssetEntity.findByRoomTarget(room) ) {
			if (room.sourceAssets || room.targetAssets) {
				skippedRooms << [roomId: 'In use']
				return
			}

			try {
				log.info "Deleting room $room ($roomId)"

				// Delete any room objects and racks
				room.racks.each { rack -> rack.delete(flush: true)}

				// Some odd reason I was getting the following error when attemting to directly delete the room but never solved
				// org.hibernate.hql.ast.tree.IdentNode cannot be cast to org.hibernate.hql.ast.tree.DotNode
				//room.delete(flush: true)
				Room.executeUpdate("delete Room r where r.id=?", [roomId.toLong()])

				// Clear out the user's room preference if it was deleted
				// TODO : JPM 9/2014 : Should delete ALL users CURR_ROOM preference when deleting a room if they exist for the room. Add to Preference service.
				if (roomId == userPrefRoom) {
					userPreferenceService.removePreference(PREF.CURR_ROOM)
				}

			} catch (DataIntegrityViolationException e) {
				log.info "Unable to delete room ($room) due to integrity violation : $e.message"
				skippedRooms << [roomId: 'In use']
			} catch (e) {
				def msg = "Error occured while deleting room ($room)"
				log.error "$msg : $e.message"
				throw new RuntimeException(msg)
			}
		}

		return skippedRooms
	}

	String save(RoomCommand roomCommand) {
		Room room = new Room()
		roomCommand.populateDomain(room, false)

		String retMessage
		if (room.save(flush: true, failOnError: false)) {
			retMessage = "Room : $room.roomName is created"
		}
		else {
			log.error GormUtil.allErrorsString(room)
			if (room.roomName) {
				retMessage = "Room : $room.roomName is not created"
			} else {
				retMessage = "Room not created"
			}
		}

		return retMessage
	}
}
