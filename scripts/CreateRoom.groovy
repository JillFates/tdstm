println "Creating missing Rooms..."

def jdbcTemplate = ctx.getBean("jdbcTemplate")

def sourceRoomQuery = "select project_id as `project.id`, source_location as location, source_room as roomName, 1 as source " +
	"from asset_entity where asset_type <> 'Blade' and source_room != '' and source_room is not null and source_location is not null and " +
	"source_location != '' and project_id is not null and room_source_id is null group by project_id, source_location, source_room"

def targetRoomQuery = "select project_id as `project.id`, target_location as location, target_room as roomName, 0 as source " +
	"from asset_entity where asset_type <> 'Blade' and target_room != '' and target_room is not null and target_location is not null and " +
	"target_location != '' and project_id is not null and room_target_id is null group by project_id, target_location, target_room"

def sourceRooms = jdbcTemplate.queryForList(sourceRoomQuery)
def targetRooms = jdbcTemplate.queryForList(targetRoomQuery)
(sourceRooms + targetRooms).each { roomFields ->
	def room = Room.findOrCreateWhere(roomFields)
	
	// Update all assets with this room info to point to this room
	if( !room )
		println "Unable to create room: ${room.errors}"
	else {
		
		def source = room.source ? 'source' : 'target'
		
		def updateQuery = "update asset_entity set room_${source}_id='${room.id}' where project_id='${room.project.id}' AND "
		updateQuery += "${source}_location=\"${roomFields.location}\" AND "
		updateQuery += "${source}_room=\"${roomFields.roomName}\""
		
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated ${source} room to ${room.id} for ${updated} assets"
	}
}
	
jdbcTemplate.update("update room set room_depth=25 where room_depth is null OR room_depth = ''")
jdbcTemplate.update("update room set room_width=25 where room_width is null OR room_width = ''")

def rackQuery = """select distinct project_id as `project.id`, location as location, room as roomName, source as source 
					from rack where rack_id is not null and room is not null and location is not null and room != '' and location != ''"""
def racks = jdbcTemplate.queryForList( rackQuery )
racks.each{ roomFields ->
	def room = Room.findOrCreateWhere(roomFields)
	
	// Update all assets with this room info to point to this room
	if( !room )
		println "Unable to create room: ${room.errors}"
	else {
		def source = room.source ? 'source' : 'target'
		
		def updateQuery = "update rack set room_id='${room.id}' where project_id='${room.project.id}' AND "
		updateQuery += "location=\"${roomFields.location}\" AND "
		updateQuery += "room=\"${roomFields.roomName}\" AND "
		updateQuery += "source=${roomFields.source}"
		
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated ${updated} with ${room.id} room id"
	}
}
jdbcTemplate.update("update rack set rack_type='Rack' where rack_type is null OR rack_type = ''")