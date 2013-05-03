/**
 * This set of Database change to drop owner_id field from application table.
 */

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130503 TM-1883-1") {
		comment('Drop "ownerId" column from the application table')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'application', columnName:'owner_id' )
		}
		dropColumn(tableName:'application', columnName:'owner_id')
	}
}