databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20180807 TM-11431-1') {
		comment("Add last_updated, and date_created to move_event")

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'move_event', columnName: 'last_updated')
			}
		}

		addColumn(tableName: 'move_event') {
			column(name: 'last_updated', type: 'DATETIME') {
				constraints(nullable: 'true')
			}
		}

		addColumn(tableName: 'move_event') {
			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'true')
			}
		}
	}
}
