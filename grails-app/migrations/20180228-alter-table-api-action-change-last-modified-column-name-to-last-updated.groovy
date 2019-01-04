databaseChangeLog = {
	changeSet(author: "ecantu", id: "TM-9546-1") {
		comment("Change column name last_modified to last_updated")
		sql("""
      ALTER TABLE `api_action` CHANGE COLUMN `last_modified` `last_updated` datetime DEFAULT NULL;
    """)
	}
}
