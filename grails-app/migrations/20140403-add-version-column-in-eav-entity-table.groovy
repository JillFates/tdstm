
databaseChangeLog = {
	// This Changeset is used to add column version to table eav_entity
	changeSet(author: "lokanada", id: "20140403 TM-2556-2") {
		comment('Add "version" column in eav_entity table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'eav_entity', columnName:'version' )
			}
		}
		sql(" ALTER TABLE eav_entity ADD COLUMN version BIGINT(11) DEFAULT 0 ")
	}
	
}
