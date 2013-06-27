/**
 * This changelog will add the nullable varchar(20) column middle_name with a default value of ''
 */
databaseChangeLog = {
	// This Changeset is used to add the nullable varchar(20) column middle_name with a default value of ''
	changeSet(author: "Ross", id: "20130627 TM-1946-3") {
		comment("Add column middle_name of datatype nullable varchar(20) with a default value of ''")
		sql("ALTER TABLE tdstm.person ADD column middle_name varchar(20) NULL DEFAULT ''")
	}
}