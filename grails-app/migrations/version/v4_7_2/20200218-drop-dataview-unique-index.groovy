package version.v4_7_2

import net.transitionmanager.common.DatabaseMigrationService
import net.transitionmanager.imports.Dataview

databaseChangeLog = {
	changeSet(author: "jmartin", id: "20200218 TM-15527-1") {
		comment("Remove the unique index from the dataview table")

		sql("ALTER TABLE dataview DROP INDEX `UK_dataview_project_name`")
	}
}
