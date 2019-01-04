/**
 * This change set is used to update environment, validation and criticality fields
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150408 TM-3790-1") {
		sql("ALTER TABLE asset_entity MODIFY environment VARCHAR(20) NULL")
		sql("ALTER TABLE asset_entity MODIFY validation VARCHAR(20) NULL")
		sql("ALTER TABLE application MODIFY criticality VARCHAR(20) NULL")
	}
	
}
