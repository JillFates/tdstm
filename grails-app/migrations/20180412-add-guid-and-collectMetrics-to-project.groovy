/**
 * This set of database changes will create the default_bundle_id column in project table and
 * Update all projects to set "default_bundle_id" and update asset_entity move_bundle_id to default bundle if null
 * and set asset_entity's move_bundle_id column to not null
 */
import net.transitionmanager.domain.Project

databaseChangeLog = {

	/**
	 * Update all projects to set "default_bundle_id" to their TBD bundle where they exist
	 *
	 */
	changeSet(author: "tpelletier", id: "20180412 TM-10301-1") {
		comment('Adding guid and collectMetrics columns')
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'project', columnName: 'guid')
				columnExists(tableName: 'project', columnName: 'collect_metrics')
			}
		}

		addColumn(tableName: 'project') {
			column(name: 'guid', type: 'char(36)') {
				constraints(nullable: "false")
			}
		}

		addColumn(tableName: "project") {
			column(name: "collect_metrics", type: 'TINYINT(1)', defaultValueNumeric: 1 ) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "tpelletier", id: "20180412 TM-10301-2") {
		comment('Update all projects to set "guid"')

		grailsChange {
			change {
				Project.all.each { Project project ->
					project.guid = project.generateGuid()
					if (project.id != Project.DEFAULT_PROJECT_ID && project.projectType != 'Demo') {
						project.collectMetrics = 1
					} else {
						project.collectMetrics = 0
					}
					if (!project.save(flush: true)) {
						throw new RuntimeException('updating project to default bundle failed');
					}
				}
			}
		}
	}

	changeSet(author: "tpelletier", id: "20180412 TM-10301-3") {
		comment('Adding guid and collectMetrics columns')
		preConditions(onFail: 'MARK_RAN') {
			not {
				indexExists(indexName: 'UK_project_guid')
			}
		}

		addUniqueConstraint(
				constraintName: 'UK_project_guid',
				tableName: 'project',
				columnNames: 'guid'
		)
	}
}