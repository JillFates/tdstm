databaseChangeLog = {
	changeSet(author: "jmartin", id: "20130903 TM-2249-1") {
		comment('Delete bogus ModelAlias records with blank name')

		sql("DELETE FROM model_alias WHERE name = ''")
		
	}
	changeSet(author: "jmartin", id: "20130903 TM-2249-2") {
		comment('Create new unique index on ModelAlias for manufacturer and name')
		
		createIndex(indexName:"ModelAlias_Manu_Name", schemaName:"tdstm", tableName:"model_alias", unique:true) {
			column(name:'manufacturer_id')
			column(name:'name')
		}
		
	}
}
