/*
 * We recently added the version property to the manufacturer and model tables and need to set the version to something other than NULL.
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20121218 TM-1131.1") {
		comment("Set the version=1 on the manufacturer and model tables where the value is null")
		sql("UPDATE manufacturer SET version=1 WHERE version IS NULL")
		sql("UPDATE model SET version=1 WHERE version IS NULL")
	}
}
