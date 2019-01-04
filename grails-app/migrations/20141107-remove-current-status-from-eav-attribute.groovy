/**
 * Remove attribute current status from table eav_attribute
 */

databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20141107 TM-3315-2") {
		comment('Remove attribute current status from table eav_attribute.')
		sql('DELETE FROM eav_attribute WHERE attribute_code like "currentStatus";')
	}

	changeSet(author: "dscarpa", id: "20141107 TM-3315-3") {
		comment('Remove reference to the currentStatus attribute')
		sql('DELETE FROM eav_entity_attribute WHERE attribute_id=51')
	}
}
