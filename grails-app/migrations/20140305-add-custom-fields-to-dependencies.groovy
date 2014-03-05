databaseChangeLog = {
	
	//This changeset will add customs c1 to c4 fileds in asset_dependency table .
	changeSet(author: "lokanada", id: "20150305TM-2507") {
		comment('Add "Customs c1-c4" columns to asset_dependency table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'asset_dependency', columnName:'c1' )
				}
			}
			for(int i=1; i<=4; i++){
				addColumn(tableName: "asset_dependency") {
					column(name: "c${i}", type: "varchar(255)")
				}
			}
	}
	
}