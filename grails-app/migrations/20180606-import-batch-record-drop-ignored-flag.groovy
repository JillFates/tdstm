databaseChangeLog = {
	changeSet(author: "arecordon", id: "20180606 TM-9794-1") {
		comment('Drop the ignored column from the Import Batch Record table.')
		dropColumn(tableName:'import_batch_record', columnName:'ignored')
	}

}
