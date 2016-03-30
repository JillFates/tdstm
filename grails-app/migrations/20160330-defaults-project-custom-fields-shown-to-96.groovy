databaseChangeLog = {
	// Defaults the Project -> Custom Fields Shown to 96
	changeSet(author: "arecordon", id: "20160330 TM-4725-1") {
		comment("Defaults the Project -> Custom Fields Shown to 96")
		sql("UPDATE project SET custom_fields_shown = 96")
	}
}