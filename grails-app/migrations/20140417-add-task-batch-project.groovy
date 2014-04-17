
databaseChangeLog = {
	// This Changeset is used to add column version to table eav_entity
	changeSet(author: "eluna", id: "20140417 TM-2639-1") {
		comment('Add project_id to task batch table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'project_id' )
			}
		}
		sql("""
			ALTER TABLE `tdstm`.`task_batch` ADD COLUMN `project_id` BIGINT NULL DEFAULT NULL  AFTER `status` , 
			  ADD CONSTRAINT `FK_TASK_BATCH_PROJECT`
			  FOREIGN KEY (`project_id` )
			  REFERENCES `tdstm`.`project` (`project_id` )
			  ON DELETE NO ACTION
			  ON UPDATE NO ACTION
			, ADD INDEX `FK_TASK_BATCH_PROJECT_idx` (`project_id` ASC) 

		""")
	}
	
}