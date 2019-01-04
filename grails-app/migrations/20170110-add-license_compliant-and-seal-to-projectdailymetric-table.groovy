import net.transitionmanager.ProjectDailyMetric

/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170110 TM-5812 A") {
		comment('Adding "license_compliant" column to "project_daily_metric" table')

		addColumn(tableName: "project_daily_metric") {
			column(name: "license_compliant", type: "Boolean", defaultValue: 1) {
				constraints(nullable: "false")
			}
		}

	}

	changeSet(author: "oluna", id: "20170110 TM-5812 B") {
		comment('Adding "seal" column to "project_daily_metric" table')

		addColumn(tableName: "project_daily_metric") {
			column(name: "seal", type: "VARCHAR(50)")
		}
	}

}
