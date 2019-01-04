/**
 * TM-11076 Add fieldLabelMap property to ImportBatch
 */
databaseChangeLog = {
	changeSet(author: "dcorrea", id: "TM-11076") {
		comment('Add fieldLabelMap property to ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch`
					ADD COLUMN `field_label_map` JSON after `field_name_list`;
 		""")

		sql ("""
                 UPDATE `import_batch`
					SET `field_label_map` = '{}'
				  WHERE `field_label_map` IS NULL;
 		""")

		sql("ALTER TABLE `import_batch` MODIFY `field_label_map` JSON NOT NULL")
	}

}
