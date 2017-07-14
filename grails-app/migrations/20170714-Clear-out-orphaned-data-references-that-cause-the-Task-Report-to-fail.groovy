/**
 * @author ecantu
 * TM-6377
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "20170714 TM-6377") {
		comment('Clear out orphaned data references that cause the Task Report to fail.')
		sql("""UPDATE asset_comment SET asset_entity_id=NULL 
			WHERE asset_entity_id NOT IN (SELECT asset_entity_id FROM asset_entity) AND asset_entity_id IS NOT NULL """)
		sql("""UPDATE asset_comment SET move_event_id=NULL 
			WHERE move_event_id NOT IN (SELECT move_event_id FROM move_event) AND move_event_id IS NOT NULL """)
	}
}


