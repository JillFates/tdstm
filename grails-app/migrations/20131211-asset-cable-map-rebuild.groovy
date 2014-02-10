/**
 * This set of Database change to rebuild asset_cable_map table.
 */

databaseChangeLog = {
	// This set of Database change to alter the stucture of asset_cable_map table.
	changeSet(author: "lokanada", id: "20131211 TM-2381") {
		comment("rebiulding asset_cable_map")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_cable_map', columnName:'cable_length' )
			}
		}
		sql("ALTER TABLE tdstm.asset_cable_map ADD COLUMN cable_length INT(10);")
		sql("ALTER TABLE tdstm.asset_cable_map ADD COLUMN cable_comment VARCHAR(255);")
		sql("ALTER TABLE tdstm.asset_cable_map CHANGE color cable_color VARCHAR(6)")
		sql("ALTER TABLE tdstm.asset_cable_map CHANGE status cable_status VARCHAR(100)")
		sql("ALTER TABLE tdstm.asset_cable_map CHANGE from_asset_id asset_from_id BIGINT(20)")
		sql("ALTER TABLE tdstm.asset_cable_map CHANGE to_asset_id asset_to_id BIGINT(20)")
		sql("ALTER TABLE tdstm.asset_cable_map CHANGE from_connector_number_id asset_from_port_id BIGINT(20)")
		sql("ALTER TABLE tdstm.asset_cable_map CHANGE to_connector_number_id asset_to_port_id BIGINT(20)")
	}
	
	//This set of database change to update cable_status from missing to Unknown and cabledDetails to Assigned.
	changeSet(author: "lokanada", id: "20131217 TM-2381-3") {
		comment("updates the status to unknown for missing and Assigned for cabledDetails.")
		sql("UPDATE tdstm.asset_cable_map SET cable_status = 'Unknown' WHERE cable_status= 'missing'")
		sql("UPDATE tdstm.asset_cable_map SET cable_status = 'Assigned' WHERE cable_status= 'cabledDetails'")
		sql("UPDATE tdstm.asset_cable_map SET cable_status = 'Empty' WHERE cable_status= 'empty'")
		sql("UPDATE tdstm.asset_cable_map SET cable_status = 'Cabled' WHERE cable_status= 'cabled'")
	}
	
	//This set of database change to update cable_status from Assigned to Cabled.
	changeSet(author: "lokanada", id: "20140107 TM-2381-4") {
		comment("updates the status to Cabled for Assigned")
		sql("UPDATE tdstm.asset_cable_map SET cable_status = 'Cabled' WHERE cable_status= 'Assigned'")
	}
	
	//This set of database change to Add column asset_loc in asset_cable_map table.
	changeSet(author: "lokanada", id: "20140107 TM-2381-5") {
		comment("Added asset_loc column in asset_cable_map table")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_cable_map', columnName:'asset_loc' )
			}
		}
		sql("ALTER TABLE tdstm.asset_cable_map ADD COLUMN asset_loc VARCHAR(10) DEFAULT 'S'")
	}
	//This set of database change to update asset_los filed with default 'S'.
	changeSet(author: "lokanada", id: "20140210 TM-2410-10") {
		comment("updates the status to Cabled for Assigned")
		sql("UPDATE tdstm.asset_cable_map SET asset_loc = 'S'")
	}
	//TODO :@John, enable below changeSets after ensuring the data transferred to new columns.
	/*// This set of Database change to drop to_asset_rack column from asset_cable_map table.
	changeSet(author: "lokanada", id: "20131211 TM-2381-1") {
		comment("Drop to_asset_rack column from the asset_cable_map table")
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'asset_cable_map', columnName:'to_asset_rack' )
		}
		dropColumn(tableName:'asset_cable_map', columnName:'to_asset_rack')
	}
	// This set of Database change to drop to_asset_uposition column from asset_cable_map table.
	changeSet(author: "lokanada", id: "20131211 TM-2381-2") {
		comment("Drop to_asset_uposition column from the asset_cable_map table")
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'asset_cable_map', columnName:'to_asset_uposition' )
		}
		dropColumn(tableName:'asset_cable_map', columnName:'to_asset_uposition')
	}*/
}