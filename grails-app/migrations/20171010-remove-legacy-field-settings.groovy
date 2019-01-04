/**
 * @author dontiveros
 * TM-6622
 */
databaseChangeLog = {
	changeSet(author: "dontiveros", id: "20171010 TM-6622 drop field_importance.") {
		comment('Drop table Field Importance')

		preConditions(onFail:'MARK_RAN') {
			tableExists(tableName:'field_importance')
		}
		dropTable(tableName: 'field_importance')
	}

	changeSet(author: "dontiveros", id: "20171010 TM-6622 delete from keyvalue.") {
		comment('Delete data from table KeyValue delete where category like tt_%')

		sql(" DELETE FROM key_value WHERE category LIKE 'tt_%' ")
	}

	changeSet(author: "dontiveros", id: "20171010 TM-6622-v2 drop project custom columns") {
		comment('Drop custom# columns from table Project')

		grailsChange {
			change {
				dropCustomColumns(sql)
			}
		}
	}
}

/**
 * Checks if custom# columns exists in project table, gets its names
 * and then executes a drop sql script.
 * Excluded from the list: custom_fields_shown column.
 * @param sql
 */
void dropCustomColumns(sql) {
	def result = sql.rows("""
						SELECT count(column_name) > 0 as customColumnExists
						FROM information_schema.columns 
						WHERE table_name = 'project' 
						AND column_name LIKE 'custom1' 					 
					""")

	def totalCustomColumns = 96
	if (result.customColumnExists[0] == 1) {
		def dropStatements = ''
		1.upto(totalCustomColumns, {
			def number = it
			dropStatements <<= " DROP COLUMN custom${number}${number == totalCustomColumns ? '' : ', '} "
		})
		sql.execute(' ALTER TABLE project '+dropStatements)
	}
}
