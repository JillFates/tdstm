/**
 * This changelog is used to modify the UserLogin table to support AD integration
 */
databaseChangeLog = {

	changeSet(author: "John", id: "20140318 TM-2039-1") {
		comment("Add column external_guid")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'user_login', columnName:'external_guid' )
			}
		}
		addColumn(tableName: 'user_login') {
			column(name: 'external_guid', type: 'varchar(64)') {
			}
		}
	}

	changeSet(author: "John", id: "20140318 TM-2039-2") {
		comment("Create index on external_guid")
		preConditions(onFail:'MARK_RAN') {
			not {
				indexExists(schemaName:"tdstm", indexName:"UserLogin_ExternaGuid_idx" )
			}
		}
		createIndex(tableName:'user_login', indexName:'UserLogin_ExternaGuid_idx', unique:'true') {
			column(name:'external_guid')
		}
	}

	changeSet(author: "John", id: "20140318 TM-2039-3") {
		comment("Create index on person.email")
		preConditions(onFail:'MARK_RAN') {
			not {
				indexExists(schemaName:"tdstm", indexName:"Person_Email_idx" )
			}
		}
		createIndex(tableName:'person', indexName:'Person_Email_idx', unique:'false') {
			column(name:'email')
		}
	}

	changeSet(author: "John", id: "20140318 TM-2039-4") {
		comment("Create index on person.lastname")
		preConditions(onFail:'MARK_RAN') {
			not {
				indexExists(schemaName:"tdstm", indexName:"Person_Lastname_idx" )
			}
		}
		createIndex(tableName:'person', indexName:'Person_Lastname_idx', unique:'false') {
			column(name:'last_name')
		}
	}

}