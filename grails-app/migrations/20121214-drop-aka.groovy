/**
 * This set of Database change to drop aka field from manufacturer and model table as we no more using it.
 */

databaseChangeLog = {	
	changeSet(author: "lokanada", id: "20121214 TM-1132.1") {
		comment('Delete the "aka" column from manufacturer and model tables')
		preConditions(onFail:'MARK_RAN') {
	        columnExists(schemaName:'tdstm', tableName:'manufacturer', columnName:'aka')
	        columnExists(schemaName:'tdstm', tableName:'model', columnName:'aka')
	    }
	    dropColumn(tableName:'manufacturer', columnName:'aka')
	    dropColumn(tableName:'model', columnName:'aka')
	}
}