println "-------------------------------"

println "Creating missing racks and asset connections..."

def jdbcTemplate = ctx.getBean("jdbcTemplate")

def sourceRackQuery = "select project_id as `project.id`, source_location as location, source_room as room, source_rack as tag, 1 as source " +
	"from asset_entity where asset_type <> 'Blade' and source_rack != '' and source_rack is not null and " +
	"project_id is not null and rack_source_id is null group by source_location, source_rack, source_room"

def targetRackQuery = "select project_id as `project.id`, target_location as location, target_room as room, target_rack as tag, 0 as source " +
	"from asset_entity where asset_type <> 'Blade' and target_rack != '' and target_rack is not null and " +
	"project_id is not null and rack_target_id is null group by target_location, target_rack, target_room"

def sourceRacks = jdbcTemplate.queryForList(sourceRackQuery)
def targetRacks = jdbcTemplate.queryForList(targetRackQuery)

(sourceRacks + targetRacks).each { rackFields ->
	// Search for a Rack matching these criteria
/*	def r = Rack.createCriteria()
	def results = r.list {
		eq('source', rackFields.source.toInteger())
		eq('project.id', rackFields['project.id'])
		if(rackFields.location == null)
			isNull('location')
		else
			eq('location', rackFields.location)
		if(rackFields.room == null)
			isNull('room')
		else
			eq('room', rackFields.room)
		eq('tag', rackFields.tag)
	}
	
	// Create a rack model if it doesn't exist
	def rack = results[0]
	if(rack == null) {
		rack = new Rack(rackFields)
		if(!rack.save()) {
			println "Unable to create rack: ${rack.errors}"
		} else {
			println "Created missing rack ${rack.id} (tag '${rack.tag}')"
		}
	}
*/	
	def rack = Rack.findOrCreateWhere(rackFields)
	
	// Update all assets with this rack info to point to this rack
	if(rack.id == null)
		println "Unable to create rack: ${rack.errors}"
	else {
		def source = rack.source ? 'source' : 'target'
		
		def updateQuery = "update asset_entity set rack_${source}_id='${rack.id}' where project_id='${rack.project.id}' AND rack_${source}_id is null AND "
		if(rackFields.location == null)
			updateQuery += "${source}_location is null AND "
		else
			updateQuery += "${source}_location=\"${rackFields.location}\" AND "
		
		if(rackFields.room == null)
			updateQuery += "${source}_room is null AND "
		else
			updateQuery += "${source}_room=\"${rackFields.room}\" AND "
		updateQuery += "${source}_rack=\"${rackFields.tag}\""
		
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated ${source} rack to ${rack.id} for ${updated} assets"
	}
}

println "-------------------------------"

