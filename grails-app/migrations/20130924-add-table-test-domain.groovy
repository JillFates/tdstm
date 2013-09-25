databaseChangeLog = {
	changeSet(author: "jmartin", id: "20130924 TM-2051-1") {
		comment('Create test_domain table')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'test_domain')
			}
		}
		createTable(schemaName: "tdstm", tableName: "test_domain") {
			column(name: "id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: "name", type: "VARCHAR(30)" ) {
			    constraints(nullable:"false")
			}
			column(name: "color", type: "VARCHAR(10)") {
				constraints(nullable:"false")
			}
			column(name: "label", type: "VARCHAR(10)") {
				constraints(nullable:"true")
			}
			column(name: "note", type: "text") {
				constraints(nullable:"false")
			}
			column(name: "age", type: "INT(3)") {
				constraints(nullable:"true")
			}
			column(name: "score", type: "TINYINT(1)") {
				constraints(nullable:"false")
			}

		}
		createIndex(indexName:"TestDomain_name_idx", schemaName:"tdstm", tableName:"test_domain", unique:true) {
			column(name:"name")
		}

	}
}