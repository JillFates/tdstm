import com.tdssrc.grails.GormUtil
import com.tdsops.common.lang.ExceptionUtil
import org.apache.commons.lang.math.NumberUtils 

class RoomService {
	
	boolean transactional = true

	/**
	 * Used to update the information about Rooms, Racks and the devices within the racks
	 * @param project - the project that the user is accessing
	 * @param user - The userLogin of the individual performing the update
	 * @param params - the parameters map passed in from the browser with all of the data
	 * @return The error message if the update failed otherwise assume that the function was successful
	 */
	String updateRoomAndRacksInfo(Project project, UserLogin user, roomId, List rackIds, Map params, String powerType) {
		def msg

		try {
			while (true) {

				if ( ! RolePermissions.hasPermission('RoomEditView')) {
					log.warning "SECURITY : User $user attempted to edit a room without permission for project $project"
					msg = 'Sorry but you do not appear to have the security rights to modify Room and Rack information'
					break
				}
				
				if (! roomId || ! roomId.isInteger() ) {
					log.warning "SECURITY : User $user attemtped to edit room with invalid room id ($roomId)"
					msg = 'Sorry but the room reference id appears to be invalid'
					break
				}

				def roomInstance = Room.get(roomId)
				if (roomInstance.project != project) {
					log.warning "SECURITY : User $user attempted to update a room not associated with project $project (roomId=${params.id})"
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
				
				if (! roomInstance.validate() || ! roomInstance.save()) {
					log.info "Updating room information failed - ${GormUtil.allErrorsString(roomInstance)} - user $user"
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

						if (! rack.validate() || ! rack.save() ) {
							log.info "Updating rack information failed - ${GormUtil.allErrorsString(rack)} - user $user"
							msg = "Updating rack (${rack.tag}) failed due to ${GormUtil.allErrorsString(rack)}"
							return
						}
					} else {
						// If the submit doesn't have the rack id in it then it will blow away the rack
						// TODO - change the code to only delete explicitely TM-3066

						AssetEntity.executeUpdate("UPDATE AssetEntity SET rackSource=null, rackTarget=null WHERE rackSource=${rack.id}")
						rack.delete(flush:true)
					}
				}

				if (msg) break

				// Add any new racks that were added
				rackIds.each { id ->
					if ( id < params.rackCount && ! msg ) {
						def rack = Rack.get( id )
						if (! rack && ! msg) {
							def newRack = Rack.findOrCreateWhere(source:source, 'project.id':roomInstance.project.id, location:roomInstance.location, 'room.id':roomInstance?.id, tag:params["tag_"+id])
							if (newRack){
								newRack.location = roomInstance.location
								newRack.roomX = params["roomX_"+id] ? NumberUtils.toDouble(params["roomX_"+id],0).round() :0
								newRack.roomY = params["roomY_"+id] ? NumberUtils.toDouble(params["roomY_"+id],0).round() :0
								newRack.powerA = params["powerA_"+id] ? NumberUtils.toDouble(params["powerA_"+id],0).round() : 0
								newRack.powerB = (params["powerB_"+id]) ? NumberUtils.toDouble(params["powerB_"+id],0).round() : 0
								newRack.powerC = (params["powerC_"+id]) ? NumberUtils.toDouble(params["powerC_"+id],0).round() : 0
								def model = params["model_"+id] != "null" ?  Model.get(params["model_"+id]) : null
								newRack.manufacturer = model?.manufacturer
								newRack.model = model
								if(powerType != "Watts"){
									newRack.powerA = Math.round(newRack.powerA * 120)
									newRack.powerB = Math.round(newRack.powerB * 120)
									newRack.powerC = Math.round(newRack.powerC * 120)
								}
								newRack.rackType = params["rackType_"+id]
								newRack.front = params["front_"+id]

								if (! newRack.validate() || ! newRack.save() ) {
									log.info "Adding new rack failed - ${GormUtil.allErrorsString(newRack)} - user $user"
									msg = "Adding new rack (${newRack.tag}) failed due to ${GormUtil.allErrorsString(newRack)}"
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
			log.error "Updating Rooms and Racks failed with exception ${ ex.getMessage() ?: ex} \n${ExceptionUtil.stackTraceToString(ex,60)}"
		}

		return msg
	}
/*
	static String GormUtil.allErrorsString(domain, separator=" : ", locale=null) {
//		def messageSource = ApplicationHolder.application.mainContext.messageSource
		//def request = RequestContextHolder.currentRequestAttributes().request
		//def localeX = RequestContextUtils.getLocale(request)

		def messageSource = ApplicationContextHolder.getApplicationContext().messageSource
		println "messageSource = $messageSource"
		StringBuilder text = new StringBuilder()
		domain?.errors?.allErrors?.each() { 
			println "Processing it = $it"
			text.append("$separator ${messageSource.getMessage(it, java.util.Locale.US)}") 
//			text.append("$separator ${messageSource.getMessage(it, locale.ENGLISH)}") 
		}
		text.toString()
	}
*/
}