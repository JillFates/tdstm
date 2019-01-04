databaseChangeLog = {
	
	changeSet(author: 'jmartin', id: '20141113 TM-3561-1') {
		comment('Add progressKey, importFilename and importResults into DataTransferBatch table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'data_transfer_batch', columnName:'progress_key' )
				}
			}

		addColumn(tableName: "data_transfer_batch") {
			column(name: "progress_key", type: "VARCHAR(128)")
			column(name: "import_filename", type: "VARCHAR(128)")
			column(name: "import_results", type: "TEXT")
		}
	}

}
