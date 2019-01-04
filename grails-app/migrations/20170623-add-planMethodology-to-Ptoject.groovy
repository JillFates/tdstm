/**
 * TM-6556 adding the plan_methodology Column into the Project table
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170624 TM-6556 adding the plan_methodology String") {
		comment("Add columns plan_methodology to Project table")
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'project', columnName: 'plan_methodology')
			}
		}

		sql("""
			ALTER TABLE `project`
				ADD COLUMN `plan_methodology` varchar(255) NULL AFTER `project_type`
		""")
	}
}
