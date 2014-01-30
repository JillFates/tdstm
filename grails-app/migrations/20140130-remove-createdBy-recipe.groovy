
/**
 * This Changelog will remove the created_by_id from the recipe table
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140130 TM-2418-1") {
		comment('Drop created_by column from recipe table in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'recipe', columnName:'created_by_id')
		}
		//Drop created_by column from recipe table
		sql("""
				ALTER TABLE `tdstm`.`recipe` DROP FOREIGN KEY `FK_CREATED_BY_RECIPE` ;
				ALTER TABLE `tdstm`.`recipe` DROP COLUMN `created_by_id` 
				, DROP INDEX `FK_CREATED_BY_RECIPE_idx` ;
			""")
	}
}