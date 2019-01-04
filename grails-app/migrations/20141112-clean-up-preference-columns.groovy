/**
 * Reset columns preferences
 */

databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20141112 TM-3538-1") {
		comment('Reset columns preferences.')
		sql("""
			DELETE FROM user_preference WHERE preference_code like 'App_Columns';
			DELETE FROM user_preference WHERE preference_code like 'Asset_Columns';
			DELETE FROM user_preference WHERE preference_code like 'Physical_Columns';
			DELETE FROM user_preference WHERE preference_code like 'Database_Columns';
			DELETE FROM user_preference WHERE preference_code like 'Storage_Columns';
		""")
	}
}
