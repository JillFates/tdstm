package version.v4_7_2

databaseChangeLog = {

	changeSet(author: "dcorrea", id: "20191120 TM-16424") {
		comment("Add tags JSON column in ImportBatchRecord domain class")
		sql (""" 
				ALTER TABLE `import_batch_record` 
				ADD COLUMN `tags` VARCHAR(2048) NULL AFTER `comments`; 
		""")
	}
}