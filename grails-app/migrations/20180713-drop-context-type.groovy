databaseChangeLog = {
	changeSet(author: 'tpelletier', id: 'TM-11077-2-1') {
		comment('drop default_asset_id')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'task_batch', columnName: 'context_type')
		}

		dropColumn(tableName: 'task_batch', columnName: 'context_type')
	}
}