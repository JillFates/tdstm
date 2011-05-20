println "-------------------------------"

println "Creating missing racks and asset connections..."

def jdbcTemplate = ctx.getBean("jdbcTemplate")
jdbcTemplate.update("update `asset_entity` set rack_source_id = null , rack_target_id = null")
def sourceRackQuery = "select project_id as `project.id`, source_location as location, room_source_id as 'room.id', source_rack as tag, 1 as source " +
	"from asset_entity where asset_type <> 'Blade' and source_rack != '' and source_rack is not null and " +
	"project_id is not null and rack_source_id is null group by source_location, source_rack, room_source_id"

def targetRackQuery = "select project_id as `project.id`, target_location as location, room_target_id as 'room.id', target_rack as tag, 0 as source " +
	"from asset_entity where asset_type <> 'Blade' and target_rack != '' and target_rack is not null and " +
	"project_id is not null and rack_target_id is null group by target_location, target_rack, room_target_id"

def sourceRacks = jdbcTemplate.queryForList(sourceRackQuery)
def targetRacks = jdbcTemplate.queryForList(targetRackQuery)

(sourceRacks + targetRacks).each { rackFields ->
	def rack = Rack.findOrCreateWhere(rackFields)
	// Update all assets with this rack info to point to this rack
	if(!rack)
		println "Unable to create rack: ${rack.errors}"
	else {
		def source = rack.source ? 'source' : 'target'
		
		def updateQuery = "update asset_entity set rack_${source}_id='${rack.id}' where project_id='${rack.project.id}' AND rack_${source}_id is null AND "
		if(rackFields.location == null)
			updateQuery += "${source}_location is null AND "
		else
			updateQuery += "${source}_location=\"${rackFields.location}\" AND "
		
		if(rackFields['room.id'] == null)
			updateQuery += "room_${source}_id is null AND "
		else
			updateQuery += "room_${source}_id = \"${rackFields['room.id']}\" AND "
		updateQuery += "${source}_rack=\"${rackFields.tag}\""
		
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated ${source} rack to ${rack.id} for ${updated} assets"
	}
}

println "-------------------------------"

