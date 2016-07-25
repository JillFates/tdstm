
/**
 * Sets the Engine for MyISAM tables to InnoDB.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20160725 TM-4962-1") {
		comment('Sets the Engine for MyISAM tables to InnoDB.')
		def tables = ["exception_dates", "move_event_staff"]
		tables.each{
			sql("ALTER TABLE $it ENGINE InnoDB")	
		}
		
	}
}
