/**
 * Reset Asset_Columns preference
 */

databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20141107 TM-3315-1") {
		comment('Reset Asset_Columns preference.')
		sql('DELETE FROM user_preference WHERE preference_code like "Asset_Columns";')
	}
}
