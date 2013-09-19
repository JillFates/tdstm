/**
 * This changelog add few columns to the TaskDependency and AssetComment tables in relationship to Runbook Optimization
 */
databaseChangeLog = {
	changeSet(author: "John", id: "20130912 TM-2274-1") {
		comment("Add columns downstream_task_count, path_duration and path_depth to task_dependency table")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'task_dependency', columnName:'downstream_task_count' )
			}
		}
		addColumn(tableName: 'task_dependency') {
			column(name: 'downstream_task_count', type: 'int(6)', defaultValue: 0) {
				constraints(nullable: 'false')
			}
		}
		addColumn(tableName: 'task_dependency') {
			column(name: 'path_duration', type: 'int(6)', defaultValue: 0) {
				constraints(nullable: 'false')
			}
		}
		addColumn(tableName: 'task_dependency') {
			column(name: 'path_depth', type: 'int(6)', defaultValue: 0) {
				constraints(nullable: 'false')
			}
		}
	}
	changeSet(author: "John", id: "20130912 TM-2274-2") {
		comment("Add columns to asset_comment")
		preConditions(onFail:'MARK_RAN') {
			not { 
				columnExists(schemaName:'tdstm', tableName:'asset_comment', columnName:'constraint_time' )
			}
		}
		addColumn(tableName: 'asset_comment') {
			column(name: 'constraint_time', type: 'datetime') {
				constraints(nullable: 'true')
			}
		}
		addColumn(tableName: 'asset_comment') {
			column(name: 'constraint_type', type: 'char(4)') {
				constraints(nullable: 'true')
			}
		}

	}
}