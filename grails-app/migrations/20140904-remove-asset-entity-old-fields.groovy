
databaseChangeLog = {
	changeSet(author: "erobles", id: "20140904 TM-3218-1") {
		comment('Remove missing attributes')
		sql("""
				DELETE FROM `tdstm`.`eav_entity_attribute` WHERE `entity_attribute_id`='19';
				DELETE FROM `tdstm`.`eav_entity_attribute` WHERE `entity_attribute_id`='21';
				DELETE FROM tdstm.data_transfer_attribute_map where eav_attribute_id IN (19, 21);
			""")
	}
}