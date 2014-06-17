
/**
 * Add missing autoincrement fields
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140506 TM-2673-1") {
		comment('Add missing autoincrement fields')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'task_batch_id')
		}
		sql("""
				SET FOREIGN_KEY_CHECKS=0;
				ALTER TABLE `tdstm`.`task_batch` CHANGE COLUMN `task_batch_id` `task_batch_id` BIGINT(20) NOT NULL AUTO_INCREMENT  ;
				SET FOREIGN_KEY_CHECKS=1;
			""")
	}
}
