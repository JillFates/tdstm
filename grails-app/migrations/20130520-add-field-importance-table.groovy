databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130520 TM-1895-1") {
		comment('Create "field_importance" in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'field_importance')
			}
		}
		createTable(schemaName: "tdstm", tableName: "field_importance") {
			column(name: "id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: "entity_type", type: "VARCHAR(11)" ){
			    constraints(nullable:"false")
			}
			column(name: "project_id", type: "BIGINT(20)"){
				constraints(nullable:"false")
			}
			column(name: "config", type: "text")
		}
		//TODO : This should be unique with entity_type. 
		createIndex(indexName:"project_index", schemaName:"tdstm", tableName:"field_importance"){
			column(name:"project_id")
		}
	}
}