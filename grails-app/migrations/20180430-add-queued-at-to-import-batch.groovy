databaseChangeLog = {
	changeSet(author: "slopez", id: "TM-10242-01") {
		comment('Add queuedAt column to the ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch` ADD `queued_at` DATETIME DEFAULT NULL AFTER `process_last_updated`;
                ALTER TABLE `import_batch` ADD `queued_by` VARCHAR(50) DEFAULT NULL AFTER `queued_at`;
 		""")
	}
}
