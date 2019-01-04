databaseChangeLog = {
	// This Changeset is used to add column help to table role_type
	changeSet(author: "lokanada", id: "20140521 TM-2714") {
		comment('Add "help" column in role_type table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'role_type', columnName:'help' )
			}
		}
		sql(" ALTER TABLE role_type ADD COLUMN help VARCHAR(255) ")
	}
	
}
