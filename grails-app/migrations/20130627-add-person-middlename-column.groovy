/**
 * This changelog will add the nullable varchar(20) column middle_name with a default value of ''
 */
databaseChangeLog = {
	// This Changeset is used to add the nullable varchar(20) column middle_name with a default value of ''
	changeSet(author: "Ross", id: "20130627 TM-1946-4") {
		comment("Add column middle_name of datatype nullable varchar(20) with a default value of ''")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'person', columnName:'middle_name' )
			}
		}
		addColumn(tableName: 'person') {
			String s = ''
			column(name: 'middle_name', type: 'varchar(20)', defaultValue: s) {
				constraints(nullable: 'true')
			}
        }
	}
}