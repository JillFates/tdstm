databaseChangeLog = {
	// This Changeset is used to add column last_modified to table user_login
	changeSet(author: "lokanada", id: "20140306-TM-2517") {
		comment('Add "is_local" column in user_login table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'user_login', columnName:'is_local' )
			}
		}
		sql("ALTER TABLE tdstm.user_login ADD column is_local BIT(1) NOT null DEFAULT true")
	}
}