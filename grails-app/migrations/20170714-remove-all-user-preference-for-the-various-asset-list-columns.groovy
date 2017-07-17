
/**
 * @author ecantu
 * TM-6718
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "20170714 TM-6718") {
		comment('Delete all user preference for the various asset lists columns to prevent null values.')
		sql("""DELETE FROM user_preference 
			WHERE preference_code IN ('App_Columns', 'Asset_Columns', 'Database_Columns', 'Storage_Columns')""")
	}
}


