/*
 * We added a couple of indexes to the model and model_alias tables via bootstrap but that is ugly and db migrations are so much nicer...
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20121218 TM-1131-b.1") {
		comment("Add Model_Name_idx to model table")
		preConditions(onFail:'MARK_RAN') {
			// Make sure that the aka data has been migrated to new table before we drop it
			not {
				indexExists(schemaName:"tdstm", indexName:"Model_Name_idx" )
			}
	    }
		createIndex(tableName:'model', indexName:'Model_Name_idx') {
			column(name:'name')
		}
	}
	changeSet(author: "jmartin", id: "20121218 TM-1131-b.2") {
		comment("Add ModelAlias_Name_idx to model_alias table")
		preConditions(onFail:'MARK_RAN') {
			// Make sure that the aka data has been migrated to new table before we drop it
			not {
				indexExists(schemaName:"tdstm", indexName:"ModelAlias_Name_idx" )
			}
	    }
		createIndex(tableName:'model_alias', indexName:'ModelAlias_Name_idx') {
			column(name:'name')
		}
	}
}

/*
// Model
try { sql.execute("CREATE INDEX Model_Name_idx ON model(name)") } catch(e) {}
	
// ModelAlias
try { sql.execute("CREATE INDEX ModelAlias_Name_idx ON model_alias(name)") } catch(e) { }
*/