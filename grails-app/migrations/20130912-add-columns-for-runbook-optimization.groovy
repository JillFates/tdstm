/**
 * This changelog is used to modify the TaskDependency and AssetComment tables in relationship to Runbook Optimization enhancements
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

	changeSet(author: "John", id: "20130912 TM-2274-3") {
		comment("Change AssetComment.duration and durationScale to not null with default values ")
		sql("UPDATE asset_comment SET duration=0 WHERE duration IS NULL")
		sql("UPDATE asset_comment SET duration_scale='m' WHERE duration_scale IS NULL")
		sql("ALTER TABLE asset_comment MODIFY duration INT(11) NOT NULL DEFAULT 0, MODIFY duration_scale CHAR(1) NOT NULL DEFAULT 'd' AFTER duration")
	}

	changeSet(author: "John", id: "20130912 TM-2274-4") {
		comment("Change TaskDependency.type default from 'FS' to 'FR'")
		sql("UPDATE task_dependency SET type='FR' WHERE type IS NULL OR type='FS'")
	}

}
