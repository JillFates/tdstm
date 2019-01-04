/**
 * @author @tavo_luna
 * TM-5193 remove eav_attributes and keys that are not used or required
 */
databaseChangeLog = {
	/*
	 * While deleting the project we were missing to remove the Rooms and Racks so  records became orphan ,
	 * Removing all orphan records in Room and Rack table for which project is not there
	 */
	changeSet(author: "oluna", id: "20160817 TM-5193") {
		sql(" delete from eav_entity_attribute where attribute_id in (select attribute_id from eav_attribute where attribute_code like '%Team%') ");
		sql(" delete from eav_attribute where attribute_code like '%Team%' ");
		sql(" delete from key_value where fi_key like '%team%' ");
	}
}
