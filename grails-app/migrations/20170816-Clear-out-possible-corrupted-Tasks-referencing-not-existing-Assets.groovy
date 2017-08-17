/**
 * @author ecantu
 * TM-6377
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "20170816 TM-6847") {
		comment('Clear out possible corrupted Tasks referencing non-existing Assets.')
		sql("""UPDATE asset_comment SET asset_entity_id=null WHERE asset_entity_id NOT IN (SELECT asset_entity_id FROM asset_entity) """)
	}
}


