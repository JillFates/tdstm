/**
 * This change set is used to change column importResults to datatype text
 */
databaseChangeLog = {

	changeSet(author: "dscarpa", id: "2015027 TM-3665-1") {
		comment('Change data_transfer_batch field import_results to be MEDIUMTEXT')

		sql("ALTER TABLE data_transfer_batch MODIFY COLUMN import_results MEDIUMTEXT DEFAULT NULL")
	}
}
