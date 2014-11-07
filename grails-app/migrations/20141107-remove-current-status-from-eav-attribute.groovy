/**
 * Remove attribute current status from table eav_attribute
 */

databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20141107 TM-3315-2") {
		comment('Remove attribute current status from table eav_attribute.')
		sql('DELETE FROM eav_attribute WHERE attribute_code like "currentStatus";')
	}
}