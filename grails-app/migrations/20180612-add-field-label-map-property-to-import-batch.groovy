/**
 * TM-11076 Add fieldLabelMap property to ImportBatch
 */
databaseChangeLog = {
	changeSet(author: "dcorrea", id: "TM-11076") {
		comment('Add fieldLabelMap property to ImportBatch domain')
		sql ("""
                ALTER TABLE `import_batch`
					ADD COLUMN `field_label_map` JSON NOT NULL after `field_name_list`;
 		""")
	}

}
