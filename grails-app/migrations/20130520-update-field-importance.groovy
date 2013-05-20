
/**
 * This changeset is used to update "asset_importance" table as "field_importance" and modify column 
 * name 'data' to 'config' and Index project column
 * 
 */

databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20130520 TM-1895-1") {
		comment('Change table name of "asset_importance" table and update its column')
		preConditions(onFail:'MARK_RAN') {
			tableExists(schemaName:'tdstm', tableName:'asset_importance')
		}
		renameTable (schemaName: "tdstm", newTableName:"field_importance", oldTableName: "asset_importance") 
		
		sql("DELETE FROM field_importance")
		sql("ALTER TABLE field_importance MODIFY COLUMN project_id BIGINT(20) NOT NULL, CHANGE COLUMN data config  TEXT  DEFAULT NULL")
		sql("CREATE INDEX pIndex ON field_importance (project_id)")
	}
}
	