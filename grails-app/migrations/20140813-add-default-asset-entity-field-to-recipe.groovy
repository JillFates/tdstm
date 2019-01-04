/**
 * This changeset adds the "default_asset" field to table recipe
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20140813 TM-3136") {
		comment('Add "default_asset" column to recipe table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'recipe', columnName:'default_asset_id' )
			}
		}
		sql(" ALTER TABLE recipe ADD COLUMN default_asset_id BIGINT(20) ")
	}
	
}
