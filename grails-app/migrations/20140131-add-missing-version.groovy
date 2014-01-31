
/**
 * Add missing version fields
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140131 TM-2410-1") {
		comment('Add missing version fields')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'recipe', columnName:'version')
			}
		}
		sql("""
				ALTER TABLE `tdstm`.`recipe` ADD COLUMN `version` BIGINT NULL  AFTER `archived` ;
				ALTER TABLE `tdstm`.`recipe_version` ADD COLUMN `version` BIGINT NULL  AFTER `recipe_id` ;
			""")
	}
}