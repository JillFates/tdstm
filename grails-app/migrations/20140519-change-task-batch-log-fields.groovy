/**
 * Increase size of log files
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140519 TM-2713-1") {
		comment('Increase size of log files')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'exception_log')
		}
		sql("""
				ALTER TABLE `tdstm`.`task_batch` CHANGE COLUMN `exception_log` `exception_log` MEDIUMTEXT NULL DEFAULT NULL, CHANGE COLUMN `info_log` `info_log` MEDIUMTEXT NULL DEFAULT NULL;
			""")
	}
}
