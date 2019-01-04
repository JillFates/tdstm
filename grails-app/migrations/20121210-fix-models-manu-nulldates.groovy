/**
 * The Manufacturer and Model tables had the dataCreated and lastModified columns added with NOT NULL so this will update the columns with current date. 
 * In addition the property UserLogin was an incorrect name so we are dropping it.
 **/
databaseChangeLog = {

	changeSet(author: "jmartin", id: "20121210 TM-1132.1") {
		comment('Update Manufacturer dateCreated and lastModified')
		sql("UPDATE manufacturer SET date_created=UTC_TIMESTAMP() WHERE date_created IS NULL")
		sql("UPDATE manufacturer SET last_modified=UTC_TIMESTAMP() WHERE last_modified IS NULL")
	}
	changeSet(author: "jmartin", id: "20121210 TM-1132.2") {
		comment('Update Manufacturer dateCreated and lastModified')
		sql("UPDATE model SET date_created=UTC_TIMESTAMP() WHERE date_created IS NULL")
		sql("UPDATE model SET last_modified=UTC_TIMESTAMP() WHERE last_modified IS NULL")
	}
	changeSet(author: "jmartin", id: "20121210 TM-1132.3") {
		comment('Remove the UserLogin column starting with the associated constraint and index')
		preConditions(onFail:'MARK_RAN') {
	        columnExists(schemaName:'tdstm', tableName:'manufacturer', columnName:'userlogin_id')
	    }
		dropForeignKeyConstraint(baseTableName:'manufacturer', constraintName:'FK8A9E23D1D191A8BA')
		dropIndex(tableName:'manufacturer', indexName:'FK8A9E23D1D191A8BA')
	    dropColumn(tableName:'manufacturer', columnName:'userlogin_id')
	}
	
}	
