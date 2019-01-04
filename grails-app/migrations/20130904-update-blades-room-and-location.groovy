
/**
 * This change set is used to update blades's location,room name,rack name as their blade chassis  
 */
databaseChangeLog = {
	//This change set is used to update blades's location and room name as their blade chassis  
	changeSet(author: "lokanada", id: "20130904 TM-2248-1") {
		sql("""update asset_entity a1 left join asset_entity a2 on a1.source_blade_chassis = a2.asset_tag 
			   set a1.source_location = a2.source_location, a1.source_room = a2.source_room, 
			   a1.source_rack = a2.source_rack, a1.rack_source_id = a2.rack_source_id, a1.room_source_id = a2.room_source_id
			   where a1.asset_type = 'Blade' and a2.asset_type='Blade chassis' and a1.project_id = a2.project_id""")
		
		sql("""update asset_entity a1 left join asset_entity a2 on a1.target_blade_chassis = a2.asset_tag
			   set a1.target_location = a2.target_location , a1.target_room = a2.target_room,
			   a1.target_rack = a2.target_rack, a1.rack_target_id = a2.rack_target_id, a1.room_target_id = a2.room_target_id 
			   where a1.asset_type = 'Blade' and a2.asset_type='Blade chassis' and a1.project_id = a2.project_id""")
	}
}
