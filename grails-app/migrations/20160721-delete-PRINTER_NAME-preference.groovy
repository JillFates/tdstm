/**
 * Deletes orphaned asset dependency bundles (with no project or invalid asset type)
 */
databaseChangeLog = {
	
	changeSet(author: "oluna", id: "20160721 TM-2727") {
		comment("Deletes PRINTER_NAME user preference from all users.")
		sql("""
			DELETE FROM user_preference WHERE preference_code = 'PRINTER_NAME'
		""")
	}
}
