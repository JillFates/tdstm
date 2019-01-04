databaseChangeLog = {
	changeSet(author: "jmartin", id: "20160727-TM-4565") {
		comment('Drop the NOT NULL on createdBy property of several tables')
		List list = [
			['asset_dependency', 'created_by' ], 
			['password_reset', 'created_by_id' ], 
			['recipe_version', 'created_by_id' ], 
			['task_batch', 'created_by_id' ]
		]

		list.each { table, column ->
			sql("ALTER TABLE $table CHANGE COLUMN $column $column bigint(20)")
		}
	}
}
