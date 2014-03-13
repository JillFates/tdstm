/**
 * Add Task batch columns
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140313 TM-2533-1") {
		comment('Refactor task batch tables')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'asset_entity_id' )
		}
		sql("""

				DELETE FROM task_batch;

				ALTER TABLE `tdstm`.`task_batch` DROP FOREIGN KEY `FK_MOVE_EVENT_TASK_BATCH` , DROP FOREIGN KEY `FK_MOVE_BUNDLE_TASK_BATCH` , DROP FOREIGN KEY `FK_ASSET_ENTITY_TASK_BATCH` ;
				ALTER TABLE `tdstm`.`task_batch` DROP COLUMN `asset_entity_id` , DROP COLUMN `move_bundle_id` , DROP COLUMN `move_event_id` 
				, DROP INDEX `FK_MOVE_EVENT_TASK_BATCH_idx1` 
				, DROP INDEX `FK_MOVE_BUNDLE_TASK_BATCH_idx` 
				, DROP INDEX `FK_MOVE_EVENT_TASK_BATCH_idx` ;
				
				
				ALTER TABLE `tdstm`.`task_batch` ADD COLUMN `context_type` VARCHAR(1) NOT NULL  AFTER `last_updated` , ADD COLUMN `context_id` BIGINT NOT NULL  AFTER `context_type` , ADD COLUMN `status` VARCHAR(10) NULL  AFTER `context_id` ;
			""")
	}
}
