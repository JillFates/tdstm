package version.v4_6_2

databaseChangeLog = {

	changeSet(author: "oluna", id: "20190530 TM-14902-1") {
 		comment('Adding guid column')
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'guid')
			}
		}

		addColumn(tableName: 'licensed_client') {
			column(name: 'guid', type: 'char(36)') {
				constraints(nullable: true)
			}
		}
	}

	changeSet(author: "oluna", id: "20190530 TM-14902-2") {
		comment('Adding metrics_gathering columns')
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'metrics_gathering')
			}
		}

		addColumn(tableName: "licensed_client") {
			column(name: "metrics_gathering", type: 'TINYINT(1)', defaultValueNumeric: 0) {
				constraints(nullable: false)
			}
		}
	}
}