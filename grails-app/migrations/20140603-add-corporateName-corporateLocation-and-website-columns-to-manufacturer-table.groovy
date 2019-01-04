/**
 * This changeset adds the "corporate_name", "corporate_location", and "website" columns to the manufacturer and manufacturer_sync tables
 */
databaseChangeLog = {
	changeSet(author: "rmacfarlane", id: "20140603 TM-2743") {
		comment('Add "corporate_name", "corporate_location", and "website" columns to the manufacturer and manufacturer_sync tables')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'manufacturer', columnName:'corporate_name' )
				columnExists(schemaName:'tdstm', tableName:'manufacturer', columnName:'corporate_location' )
				columnExists(schemaName:'tdstm', tableName:'manufacturer', columnName:'website' )
				columnExists(schemaName:'tdstm', tableName:'manufacturer_sync', columnName:'corporate_name' )
				columnExists(schemaName:'tdstm', tableName:'manufacturer_sync', columnName:'corporate_location' )
				columnExists(schemaName:'tdstm', tableName:'manufacturer_sync', columnName:'website' )
			}
		}
		sql(" ALTER TABLE manufacturer ADD COLUMN corporate_name VARCHAR(255) ")
		sql(" ALTER TABLE manufacturer ADD COLUMN corporate_location VARCHAR(255) ")
		sql(" ALTER TABLE manufacturer ADD COLUMN website VARCHAR(255) ")
		sql(" ALTER TABLE manufacturer_sync ADD COLUMN corporate_name VARCHAR(255) ")
		sql(" ALTER TABLE manufacturer_sync ADD COLUMN corporate_location VARCHAR(255) ")
		sql(" ALTER TABLE manufacturer_sync ADD COLUMN website VARCHAR(255) ")
	}
	
}
