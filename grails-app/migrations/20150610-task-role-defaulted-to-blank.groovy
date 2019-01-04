/**
 * Updates tasks with role 0, changing this value to blank.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20150610 TM-3850 1") {
		comment("Updates tasks with role 0, changing this value to blank.")
		sql("""
			UPDATE asset_comment SET role='' WHERE role = '0' and comment_type='issue'
		""")
	}


}
