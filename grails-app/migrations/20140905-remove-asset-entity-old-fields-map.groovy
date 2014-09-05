
databaseChangeLog = {
	changeSet(author: "erobles", id: "20140905 TM-3218-1") {
		comment('Remove missing attributes')
		sql("""
				DELETE FROM tdstm.data_transfer_attribute_map where eav_attribute_id IN (15,17);
			""")
	}
}