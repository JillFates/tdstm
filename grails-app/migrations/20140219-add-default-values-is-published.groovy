/**
 * Add default values for is published
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140219 TM-2469-1") {
		comment('Add default values for is published')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'asset_comment', columnName:'is_published')
		}
		sql("""
				UPDATE tdstm.asset_comment SET is_published = 1;
				ALTER TABLE `tdstm`.`asset_comment` CHANGE COLUMN `is_published` `is_published` BIT(1) NOT NULL DEFAULT 1;
			""")
	}
	
	changeSet(author: "eluna", id: "20140219 TM-2469-2") {
		comment('Add default values for is published')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'is_published')
		}
		sql("""
				UPDATE tdstm.task_batch SET is_published = 1;
				ALTER TABLE `tdstm`.`task_batch` CHANGE COLUMN `is_published` `is_published` BIT(1) NOT NULL DEFAULT 1;
			""")
	}
}
