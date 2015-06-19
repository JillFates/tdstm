/**
 * Drops the NN constraint in the Models table for the asset type column.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20150612 TM-3687") {
		comment("Drops the NN constraint in the Models table for the asset type column")
		sql("""
			ALTER TABLE model CHANGE COLUMN asset_type asset_type VARCHAR(255) NULL
		""")
	}

}
