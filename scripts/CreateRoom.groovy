println "-------------------------------"

println "Creating missing Rooms..."

def jdbcTemplate = ctx.getBean("jdbcTemplate")

def sourceRoomQuery = "select project_id as `project.id`, source_location as location, source_room as roomName, 1 as source " +
	"from asset_entity where asset_type <> 'Blade' and source_room != '' and source_room is not null and source_location is not null and " +
	"source_location != '' and project_id is not null and room_source_id is null group by source_location, source_room"

def targetRoomQuery = "select project_id as `project.id`, target_location as location, target_room as roomName, 0 as source " +
	"from asset_entity where asset_type <> 'Blade' and target_room != '' and target_room is not null and target_location is not null and " +
	"target_location != '' and project_id is not null and room_target_id is null group by target_location, target_room"

def sourceRooms = jdbcTemplate.queryForList(sourceRoomQuery)
def targetRooms = jdbcTemplate.queryForList(targetRoomQuery)

(sourceRooms + sourceRooms).each { roomFields ->
	def room = Room.findOrCreateWhere(roomFields)
	
	// Update all assets with this room info to point to this room
	if( !room )
		println "Unable to create room: ${room.errors}"
	else {
		def source = room.source ? 'source' : 'target'
		
		def updateQuery = "update asset_entity set room_${source}_id='${room.id}' where project_id='${room.project.id}' AND room_${source}_id is null AND "
		if(roomFields.location == null)
			updateQuery += "${source}_location is null AND "
		else
			updateQuery += "${source}_location=\"${roomFields.location}\" AND "
		
		if(roomFields.room == null)
			updateQuery += "${source}_room is null AND "
		else
			updateQuery += "${source}_room=\"${roomFields.room}\" AND "
		updateQuery += "${source}_room=\"${roomFields.tag}\""
		
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated ${source} room to ${room.id} for ${updated} assets"
	}
}
// TODO : create script to update Rack.
