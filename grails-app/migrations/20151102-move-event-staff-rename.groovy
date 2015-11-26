/**
 * Rename index move_event_staff_primary_key to idx_move_event_staff_composite_key
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20151102 TM-4174-2") {
		comment('Rename index move_event_staff_primary_key to idx_move_event_staff_composite_key')

		// Drop previous index
		dropIndex(tableName: "move_event_staff", indexName: "move_event_staff_primary_key")

		// Creates a new composite index
		createIndex(tableName:'move_event_staff', indexName:'idx_move_event_staff_composite_key', unique:'true') {
			column(name:'move_event_id')
			column(name:'person_id')
			column(name:'role_id')
		}
	}
}
