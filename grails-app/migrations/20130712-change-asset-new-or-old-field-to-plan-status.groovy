/**
 * This changelog will rename the new_or_old asset_entity field to plan_status
 */
databaseChangeLog = {
	// This Changeset is used to rename the new_or_old asset_entity field to plan_status
	changeSet(author: "Ross", id: "20130712 TM-1994") {
		comment("Rename the new_or_old asset_entity field to plan_status")
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'new_or_old' )
		}
		renameColumn(tableName:'asset_entity', oldColumnName:'new_or_old', newColumnName:'plan_status', columnDataType:'varchar(255)')
	}
}