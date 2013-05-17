databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130517 TM-1895-3") {
		comment('Create "asset_importance" in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'asset_importance')
			}
		}
		createTable(schemaName: "tdstm", tableName: "asset_importance") {
			column(name: "id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: "entity_type", type: "VARCHAR(11)" ){
			    constraints(nullable:"false")
			}
			column(name: "project_id", type: "BIGINT(20)")
			column(name: "data", type: "text")
		}
	}
}