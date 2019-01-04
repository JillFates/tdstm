/**
 * Assigns 'Server' as assetType to models without a value for this field.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20150526 TM-3687 1") {
		comment("Assigns 'Server' as assetType to models without a value for this field.")
		sql("""
			UPDATE model SET asset_type='Server' WHERE asset_type IS NULL
		""")
	}

	changeSet(author: "arecordon", id: "20150526 TM-3687 2") {
		comment("Makes the asset type column in models to be not null")
		sql("""
			ALTER TABLE model MODIFY asset_type varchar(255) NOT NULL;
		""")
	}

	changeSet(author: "arecordon", id: "20150526 TM-3687 3") {
		comment("Assigns 'Server' as assetType to models with an empty value for this field.")
		sql("""
			UPDATE model SET asset_type='Server' WHERE LENGTH(TRIM(asset_type)) = 0
		""")
	}

}
