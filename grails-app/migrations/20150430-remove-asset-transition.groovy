/**
 * Remove asset transition table
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150430 TM-3803-1") {
		comment('Remove asset transition table')
		sql("DROP TABLE asset_transition")
	}
}
