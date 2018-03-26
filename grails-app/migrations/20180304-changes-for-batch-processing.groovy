import net.transitionmanager.security.Permission

/**
 * TM-9406 Additional changes to the Credential domain
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "TM-9675-01") {
		comment('Add importResults column to the ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch`
                    ADD COLUMN `process_progress` TINYINT(3) AFTER `date_format`,
					ADD COLUMN `process_last_updated` DATETIME AFTER `process_progress`,
					ADD COLUMN `process_stop_flag` TINYINT(1) after `process_last_updated`;
 		""")
	}

}
