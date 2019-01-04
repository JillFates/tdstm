/**
 * This changelog will rename the use_of_planning field to use_for_planning
 */

databaseChangeLog = {
   // This Changeset is used to rename the new_or_old asset_entity field to plan_status
   changeSet(author: "lokanada", id: "20140320 TM-2539") {
	   comment("Rename the use_of_planning field to use_for_planning")
	   preConditions(onFail:'MARK_RAN') {
		   columnExists(schemaName:'tdstm', tableName:'move_bundle', columnName:'use_of_planning' )
	   }
	   renameColumn(tableName:'move_bundle', oldColumnName:'use_of_planning', newColumnName:'use_for_planning', columnDataType:'BIT(1)')
   }
}
