/**
 * Assigns 'Server' as assetType to assets without a value for this field.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20150526 TM-3687") {
		comment("Assigns 'Server' as assetType to assets without a value for this field.")
		sql("""
			UPDATE model SET asset_type='Server' WHERE asset_type IS NULL
		""")
	}
}