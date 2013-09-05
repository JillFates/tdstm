/**
 * This change set is used to update assets sourceRacks,targetRacks,sourceRooms,targetRooms not having any rackId or roomId.
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130905 TM-2250-1") {
		sql("update asset_entity set rack_source_id = null, source_rack = null where rack_source_id not in (select rack_id from rack)")
		sql("update asset_entity set rack_target_id = null, target_rack = null where rack_target_id not in (select rack_id from rack)")
		sql("update asset_entity set room_source_id = null, source_room = null where room_source_id not in (select room_id from room)")
		sql("update asset_entity set room_target_id = null, target_room = null where room_target_id not in (select room_id from room)")
		
		sql("update asset_entity set source_rack = null where rack_source_id is null")
		sql("update asset_entity set target_rack = null where rack_target_id is null")
		sql("update asset_entity set source_room = null where room_source_id is null")
		sql("update asset_entity set target_room = null where room_target_id is null")
	}
}