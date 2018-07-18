databaseChangeLog = {
	changeSet(author: 'tpelletier', id: 'TM-11077-2-1') {
		comment('drop default_asset_id')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'task_batch', columnName: 'context_type')
		}

		dropColumn(tableName: 'task_batch', columnName: 'context_type')
	}

	changeSet(author: 'tpelletier', id: 'TM-11077-2-2') {
		comment('Make contextId in task batch nullable')

		dropNotNullConstraint(tableName: 'task_batch', columnName: 'context_id', columnDataType:'BIGINT(20)')
	}
}