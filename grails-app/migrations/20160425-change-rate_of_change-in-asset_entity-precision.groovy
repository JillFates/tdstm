/**
 * @author @tavo_luna
 * TM-4764 Change AssetEntity.rateOfChange precision to Int(4) supporting larger values, adding migration and mapping
 */

databaseChangeLog = {	
	changeSet(author: "oluna", id: "20160425 TM-4764") {
		comment('Change rate_of_change in asset_entity precision from TinyInt to Int(4)')		
		sql("""
			ALTER TABLE asset_entity MODIFY rate_of_change Int(4)
		""")
	}
}
