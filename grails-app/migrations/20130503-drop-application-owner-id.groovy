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
	
	// This set of Database change to drop application column from application table.
	changeSet(author: "lokanada", id: "20130606 TM-1925-1") {
		comment('Drop "environment" column from the application table')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'application', columnName:'environment' )
		}
		dropColumn(tableName:'application', columnName:'environment')
	}
	
	// This set of Database change to drop validation column from application table.
	changeSet(author: "lokanada", id: "20130606 TM-1925-2") {
		comment('Drop "validation" column from the application table')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'application', columnName:'validation' )
		}
		dropColumn(tableName:'application', columnName:'validation')
	}
}