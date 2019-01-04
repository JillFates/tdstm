/**
 * This change set is used to change column note to datatype text
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20151214 TM-4331") {
		comment('Remove new_or_old column from asset_entity')
		sql("""
			ALTER TABLE asset_entity DROP COLUMN new_or_old
		""")
	}
}
