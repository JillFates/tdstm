
/**
 * Fix autoincrement fields
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140204 TM-2437-1") {
		comment('Fix autoincrement fields')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'recipe', columnName:'recipe_id')
		}
		sql("""
				LOCK TABLES 
				    `tdstm`.`task_batch` WRITE,
				    `tdstm`.`recipe_version` WRITE,
				    `tdstm`.`recipe` WRITE;
				
				ALTER TABLE `tdstm`.`task_batch` DROP FOREIGN KEY `FK_RECIPE_VERSION_TASK_BATCH` ;
				ALTER TABLE `tdstm`.`recipe_version` DROP FOREIGN KEY `FK_RECIPE_RECIPE_VERSION` , DROP FOREIGN KEY `FK_CLONED_FROM_RECIPE_VERSION` ;
				ALTER TABLE `tdstm`.`recipe_version` DROP INDEX `FK_CLONED_FROM_RECIPE_VERSION_idx` ;
				ALTER TABLE `tdstm`.`recipe` DROP FOREIGN KEY `FK_RELEASED_VERSION_RECIPE` ;
				
				
				ALTER TABLE `tdstm`.`recipe` CHANGE COLUMN `recipe_id` `recipe_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
				ALTER TABLE `tdstm`.`recipe_version` CHANGE COLUMN `recipe_version_id` `recipe_version_id` BIGINT(20) NOT NULL AUTO_INCREMENT  ;
				
				
				ALTER TABLE `tdstm`.`task_batch` ADD CONSTRAINT `FK_RECIPE_VERSION_TASK_BATCH` FOREIGN KEY (`recipe_version_used_id` ) REFERENCES `tdstm`.`recipe_version` (`recipe_version_id`);
				ALTER TABLE `tdstm`.`recipe_version` ADD CONSTRAINT `FK_RECIPE_RECIPE_VERSION` FOREIGN KEY (`recipe_id` ) REFERENCES `tdstm`.`recipe` (`recipe_id` ); 
				ALTER TABLE `tdstm`.`recipe_version` ADD CONSTRAINT `FK_CLONED_FROM_RECIPE_VERSION` FOREIGN KEY (`cloned_from_id` ) REFERENCES `tdstm`.`recipe_version` (`recipe_version_id` );
				ALTER TABLE `tdstm`.`recipe` ADD CONSTRAINT `FK_RELEASED_VERSION_RECIPE` FOREIGN KEY (`released_version_id` ) REFERENCES `tdstm`.`recipe_version` (`recipe_version_id` ) ;
				
				ALTER TABLE `tdstm`.`recipe_version` ADD INDEX `FK_CLONED_FROM_RECIPE_VERSION_idx` (`cloned_from_id` ASC) ;
				
				UNLOCK TABLES;
			""")
	}
}
