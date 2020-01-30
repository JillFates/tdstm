package version.v4_7_2

import net.transitionmanager.common.DatabaseMigrationService

databaseChangeLog = {

	changeSet(author: "arecordon", id: "20191219 TM-16638-1") {
		comment("Delete orphan dataviews associated to projects that have been deleted.")
		sql ("""DELETE FROM dataview WHERE project_id NOT IN (SELECT project_id FROM project)""")
	}


	changeSet(author: "arecordon", id: "20191219 TM-16638-2") {
		comment("Delete foreign keys that need to be created again having ON DELETE CASCADE.")

		grailsChange {
			change {
				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				List<Map> fkInfoList = [
						[table: 'dataview', column: 'project_id'],
						[table: 'favorite_dataview', column: 'dataview_id']
				]
				fkInfoList.each { Map fkInfo ->
					String fkName = databaseMigrationService.getForeignKey(sql, fkInfo.table, fkInfo.column)
					databaseMigrationService.deleteForeignKey(sql, fkInfo.table, fkName)
				}
			}
		}

	}

	changeSet(author: "arecordon", id: "20191219 TM-16638-3") {
		comment("Generate appropriate foreign keys for dataview and favorite_dataview.")

		addForeignKeyConstraint(
				constraintName: 'fk_dataview_project',
				onDelete: 'CASCADE',
				baseTableName: 'dataview',
				baseColumnNames: 'project_id',
				referencedTableName: 'project',
				referencedColumnNames: 'project_id'
		)

		addForeignKeyConstraint(
				constraintName: 'fk_favorite_dataview_dataview',
				onDelete: 'CASCADE',
				baseTableName: 'favorite_dataview',
				baseColumnNames: 'dataview_id',
				referencedTableName: 'dataview',
				referencedColumnNames: 'id'
		)
	}
}