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

	changeSet(author: "dontiveros", id: "20171010 TM-6622 drop project custom columns.") {
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
	def existingCustomColumns = sql.rows("""
						SELECT column_name
						FROM information_schema.columns
						WHERE table_name = 'project' 
						AND column_name LIKE 'custom%'
						AND column_name NOT IN ('custom_fields_shown')
					""")

	existingCustomColumns.each {
		sql.execute(' ALTER TABLE project DROP COLUMN '+it.column_name)
	}
}