package version.v4_6_1

databaseChangeLog = {

	changeSet(author: "dcorrea", id: "20181227 TM-11482") {
		comment("Add comments JSON column in ImportBatchRecord domain class")
		sql (""" 
				ALTER TABLE `import_batch_record` 
				ADD COLUMN `comments` JSON AFTER `fields_info`; 
		""")

		sql ("""
               UPDATE `import_batch_record` 
                  SET `comments` = '[]'
                WHERE `comments` IS NULL;
 		""")

		sql ("""
                ALTER TABLE `import_batch_record` 
                     MODIFY `comments` JSON NOT NULL ;
 		""")
	}
}