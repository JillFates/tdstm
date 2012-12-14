/**
 * This set of Database change to drop aka field from manufacturer and model table as we no more using it.
 */

databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20121214 TM-1132.1") {
		comment('Alter "aka" field from manufacturer and model table')
		sql("ALTER TABLE manufacturer DROP COLUMN aka")
		sql("ALTER TABLE model DROP COLUMN aka")
	}
}