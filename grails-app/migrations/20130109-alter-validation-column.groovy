/**
 * This changeLog will migrate the validation data of application table into asset_entity tables as
 * we are going to use validation for all asset type.
 */

databaseChangeLog = {
	
	//First of all adding validation column in asset_entity_table .
	changeSet(author: "lokanada", id: "20120109 TM-1156-a.1") {
		comment('Add "validation" column to asset_entity table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'validation' )
			}
		}
		addColumn(tableName: "asset_entity") {
            column(name: "validation", type: "varchar(16)")
        }
	}
	
	//After adding validation column in asset_entity table migrate all "validation" data from application.validation
	// to asset_entity.validation from the application table
	changeSet(author: "lokanada", id: "20120109 TM-1156-b.2") {
		comment('migrate all "validation" data from application.validation to asset_entity.validation from the application table')
		grailsChange {
			change {
				sql.eachRow('SELECT app_id AS id, validation FROM application') { app->
					def validation = app.validation
					if (validation) {
						sql.execute("""UPDATE asset_entity SET validation = ${validation} WHERE asset_entity_id = ${app.id}""")
					}
				}
			}
		}
	}
	
	//TODO : LOK - Write changelog to delete validation from application
}
