import net.transitionmanager.security.Permission

/**
 * TM-9406 Additional changes to the Credential domain
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "TM-9523-01") {
		comment('Add importResults column to the ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch`
                    ADD COLUMN `import_results` MEDIUMTEXT NOT NULL AFTER `field_name_list`;
 		""")
	}

	changeSet(author: "jmartin", id: "TM-9523-02") {
		comment('Change import_batch_record column fields_info to JSON and drop a few other columns')
		sql ("""
                ALTER TABLE `import_batch_record`
                    CHANGE `fields_info` `fields_info` JSON NOT NULL AFTER `errors`,
					DROP COLUMN `find_info`,
					DROP COLUMN `create_info`,
					DROP COLUMN `update_info`;
 		""")
	}

	changeSet(author: "jmartin", id: "TM-9523-03") {
		comment('Change import_batch_record column batch_import_record_id to import_batch_record_id')
		sql ("""
                ALTER TABLE `import_batch_record`
                    CHANGE `batch_import_record_id` `import_batch_record_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
					CHANGE COLUMN `errors` `error_list` JSON NOT NULL;
 		""")
	}

}
