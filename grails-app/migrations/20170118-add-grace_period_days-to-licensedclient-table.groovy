/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170118 TM-5901") {
		comment('Adding "grace_period_days" column in licenced_client')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'grace_period_days')
			}
		}

		addColumn(tableName: "licensed_client") {
			column(name: "grace_period_days", type: "TINYINT", defaultValue: 5) {
				constraints(nullable: "false")
			}
		}

	}

}
