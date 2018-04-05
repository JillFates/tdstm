databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-10004-1') {
		comment('Purge the Import Batch table (and the Batch Records)')
		sql("DELETE FROM import_batch")
	}
}
