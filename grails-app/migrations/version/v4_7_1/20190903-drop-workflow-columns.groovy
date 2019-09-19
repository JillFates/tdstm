package version.v4_7_1

databaseChangeLog = {
	changeSet(author: 'tpelletier', id: 'TM-15170-1') {
		comment('drop workflow_code')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'move_bundle', columnName: 'workflow_code')
		}

		dropColumn(tableName: 'move_bundle', columnName: 'workflow_code')
	}


	changeSet(author: 'tpelletier', id: 'TM-15170-2') {
		comment('drop workflow_code')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'project', columnName: 'workflow_code')
		}

		dropColumn(tableName: 'project', columnName: 'workflow_code')
	}

	changeSet(author: 'tpelletier', id: 'TM-15170-3') {
		comment('drop workflow_transition')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'asset_comment', columnName: 'workflow_transition')
		}

		dropColumn(tableName: 'asset_comment', columnName: 'workflow_transition')
	}


	changeSet(author: 'tpelletier', id: 'TM-15170-4') {
		comment('drop workflow_transition')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'asset_comment', columnName: 'workflow_override')
		}

		dropColumn(tableName: 'asset_comment', columnName: 'workflow_override')
	}




	changeSet(author: 'tpelletier', id: 'TM-15170-5') {
		comment('drop workflow_transition_map')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'workflow_transition_map')
		}

		dropTable(tableName: 'workflow_transition_map')
	}


	changeSet(author: 'tpelletier', id: 'TM-15170-6') {
		comment('drop workflow_transition')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'workflow_transition')
		}

		dropTable(tableName: 'workflow_transition')
	}


	changeSet(author: 'tpelletier', id: 'TM-15170-7') {
		comment('drop workflow_transition')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'swimlane')
		}

		dropTable(tableName: 'swimlane')
	}

	changeSet(author: 'tpelletier', id: 'TM-15170-8') {
		comment('drop workflow_transition')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'workflow')
		}

		dropTable(tableName: 'workflow')
	}

	changeSet(author: 'tpelletier', id: 'TM-15170-9') {
		comment('drop transition_id')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'move_bundle_step', columnName: 'transition_id')
		}

		dropColumn(tableName: 'move_bundle_step', columnName: 'transition_id')
	}


}
