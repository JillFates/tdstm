/**
 * This changelog add column 'dep_console_grouping_criteria' to the project table in order
 * to log dependency console grouping criteria
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130924 TM-2292") {
		comment("Add column dep_console_criteria to project table")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'project', columnName:'dep_console_criteria' )
			}
		}
		addColumn(tableName: 'project') {
			column(name: 'dep_console_criteria', type: 'text') {
				constraints(nullable: 'true')
			}
		}
	}
}