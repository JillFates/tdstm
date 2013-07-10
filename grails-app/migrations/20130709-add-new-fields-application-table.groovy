/**
 * This changelog will add
 * 'shutdownDuration','shutdownBy','shutdownFixed','startupBy','startupDuration',
 * 'startupFixed','testingDuration','testingBy','testingFixed' columns to the application table. 
 * 'external_ref_id' column to asset_entity table.
 */

databaseChangeLog = {
	
	/*
	 * this changeSet will add 'shutdownDuration','startupDuration','testingDuration' 
	 *'shutdownDuration','shutdownBy','shutdownFixed','startupBy','startupDuration',
	 * 'startupFixed','testingDuration','testingBy','testingFixed' columns to the application table.
	 */
	changeSet(author: "lokanada", id: "20130709 TM-1981-6") {
		comment('Add "shutdownDuration" column to application table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'application', columnName:'shutdown_duration' )
				}
			}
			['shutdown', 'startup', 'testing'].each{field ->
				['duration':'INT(10)', 'by':'varchar(255)', 'fixed':'TINYINT(1)'].each{key, value->
					addColumn(tableName: "application") {
						column(name: "${field}_$key", type: value)
					}
				}
			}
	}
	
	//this changeSet will add 'external_ref_id' column in asset_entity table.
	changeSet(author: "lokanada", id: "20130709 TM-1981-7") {
		comment('Add "external_ref_id" column to asset_entity table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'external_ref_id' )
				}
			}
				addColumn(tableName: "asset_entity") {
					column(name: "external_ref_id", type: "varchar(255)")
				}
	}
}