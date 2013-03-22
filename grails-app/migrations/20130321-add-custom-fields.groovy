databaseChangeLog = {
	
	//First of all adding validation column in asset_entity_table .
	changeSet(author: "lokanada", id: "20130322 TM-1263-3") {
		comment('Add "Customs (9 - 24)" column to project table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'project', columnName:'custom9' )
				}
			}
			for(int i=9; i<=24; i++){
				addColumn(tableName: "project") {
					column(name: "custom${i}", type: "varchar(255)")
				}
			}
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'project', columnName:'custom_fields_shown' )
				}
			}
			addColumn(tableName: "project") {
				column(name: "custom_fields_shown", type: "INT(10)")
			}
	}
	
	changeSet(author: "lokanada", id: "20130322 TM-1263-4") {
		comment('Add "Customs (9 - 24)" column to asset_entity table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'custom9' )
				}
			}
			for(int i=9; i<=24; i++){
				addColumn(tableName: "asset_entity") {
					column(name: "custom${i}", type: "varchar(255)")
				}
			}
	}
}