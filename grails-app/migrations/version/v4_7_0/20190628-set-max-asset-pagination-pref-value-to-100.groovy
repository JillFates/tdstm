package version.v4_7_0

databaseChangeLog = {
	changeSet(author: "arecordon", id: "20190628 TM-15404") {
		comment('Set to 100 the preference value for the asset pagination when above that value.')
		sql("""UPDATE user_preference 
					SET value='100' WHERE preference_code='ASSET_LIST_SIZE' AND CAST(value AS SIGNED) > 100""")
	}
}