import net.transitionmanager.security.Permission

/**
 * TM-9406 Additional changes to the Credential domain
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "TM-9523-01") {
		comment('Add importResults column to the ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch`
                    ADD `import_results` MEDIUM_TEXT NOT NULL DEFAULT '' AFTER `http_method`;
 		""")
	}

	changeSet(author: "jmartin", id: "TM-9523-02") {
		comment('Change import_batch_record column fields_info to JSON and drop a few other columns')
		sql ("""
                ALTER TABLE `import_batch_record`
                    CHANGE `fields_info` `fields_info` JSON NOT NULL DEFAULT '{}' AFTER `errors`,
					DROP COLUMN `find_info`,
					DROP COLUMN `create_info`,
					DROP COLUMN `update_info`;
 		""")
	}

}
