databaseChangeLog = {

	changeSet(author: "John", id: "20141114 TM-3540-1") {
		comment("Create index on external_guid")
		preConditions(onFail:'MARK_RAN') {
			not {
				indexExists(schemaName:"tdstm", indexName:"idx_dataTranferValue_batchAndRowIds" )
			}
		}
		createIndex(tableName:'data_transfer_value', indexName:'idx_dataTranferValue_batchAndRowIds', unique:'false') {
			column(name:'data_transfer_batch_id')
			column(name:'row_id')
		}
	}

}
