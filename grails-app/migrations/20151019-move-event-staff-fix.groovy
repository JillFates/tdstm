/**
 * Remove duplicated entries from move event staff
 * and add keys to the table
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20151019 TM-4174-1") {
		comment('Remove duplicated entries from move event staff and add keys to the table')
		
		grailsChange {
			change {
				// Delete duplicated entries
				def sequenceList = sql.rows("""
					SELECT min(id) as idToDelete, count(id) as count, move_event_id, person_id, role_id 
					FROM move_event_staff GROUP BY move_event_id, person_id, role_id 
					HAVING count > 1;""")

				sequenceList.each{
					sql.execute("DELETE FROM move_event_staff WHERE id = ${it.idToDelete} ")
				}
			}
		}

		// Creates a new primary key
		createIndex(tableName:'move_event_staff', indexName:'move_event_staff_primary_key', unique:'true') {
			column(name:'move_event_id')
			column(name:'person_id')
			column(name:'role_id')
		}

	}
}
