/**
 * TM-9661 Need updates and corrections to the Test Authentication button
 */
databaseChangeLog = {
	changeSet(author: 'slopez', id: 'TM-9661-1') {
		comment('Add validation_expression column to credential table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'credential', columnName: 'validation_expression' )
			}
		}
		sql("""
			ALTER TABLE `credential` ADD COLUMN `validation_expression` VARCHAR(255) NULL DEFAULT NULL AFTER `session_name` 
		""")
	}
}
