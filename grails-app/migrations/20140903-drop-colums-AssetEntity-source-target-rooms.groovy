/**
 * Drop sourceRoom and targetRoom columns from the asset_entity table
 */

databaseChangeLog = {
	changeSet(author: "jmartin", id: "20140903 TM-3128-1") {
		comment('Drop sourceRoom and targetRoom columns from the asset_entity table')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'source_room' )
		}
		dropColumn(tableName:'asset_entity', columnName:'source_room')
		dropColumn(tableName:'asset_entity', columnName:'target_room')
	}
}
