/**
 * Add times to Move event table
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140227 TM-2490-1") {
		comment('Add times to Move event table')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'move_event', columnName:'est_start_time')
			}
		}
		sql("""
				ALTER TABLE `tdstm`.`move_event` ADD COLUMN `est_start_time` DATETIME NULL DEFAULT NULL  AFTER `runbook_recipe` , ADD COLUMN `est_completion_time` DATETIME NULL DEFAULT NULL  AFTER `est_start_time` ;
			""")
	}
}
