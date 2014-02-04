
/**
 * Add missing autoincrement fields
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140203 TM-2424-1") {
		comment('Add missing autoincrement fields')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'recipe', columnName:'recipe_id')
		}
		sql("""
				SET FOREIGN_KEY_CHECKS=0;
				ALTER TABLE `tdstm`.`recipe` CHANGE COLUMN `recipe_id` `recipe_id` BIGINT(20) NOT NULL AUTO_INCREMENT  ;
				SET FOREIGN_KEY_CHECKS=1;
			""")
	}
	
	changeSet(author: "eluna", id: "20140203 TM-2424-2") {
		comment('Add missing autoincrement fields')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'recipe_version', columnName:'recipe_version_id')
		}
		sql("""
				SET FOREIGN_KEY_CHECKS=0;
				ALTER TABLE `tdstm`.`recipe_version` CHANGE COLUMN `recipe_version_id` `recipe_version_id` BIGINT(20) NOT NULL AUTO_INCREMENT  ;
				SET FOREIGN_KEY_CHECKS=1;
			""")
	}
}
