/**
 * This set of database changes will create the default_bundle_id column in project table and
 * Update all projects to set "default_bundle_id" and update asset_entity move_bundle_id to default bundle if null
 * and set asset_entity's move_bundle_id column to not null
 */

import com.tdssrc.grails.StringUtil
import groovy.sql.GroovyRowResult
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

	changeSet(author: "tpelletier", id: "20180412 TM-10301-2B") {
		comment('Update all projects to set "guid"')

		grailsChange {
			change {
				List<GroovyRowResult> projects = sql.rows("select p.project_id AS id, pg.name AS name from project p join party_group pg on p.project_id = pg.party_group_id")

				sql.withBatch('UPDATE project SET guid = ? where project_id = ?') { stmt ->
					projects.each { GroovyRowResult project ->

						stmt.addBatch([StringUtil.generateGuid(), project.id])
					}
				}

				sql.withBatch('UPDATE project SET collect_metrics = ? where project_id = ?') { stmt ->
					projects.each { GroovyRowResult project ->
						if (project.id != Project.DEFAULT_PROJECT_ID && !project.name.toLowerCase().contains('demo')) {
							stmt.addBatch([1, project.id])
						} else {
							stmt.addBatch([0, project.id])
						}
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

/**
 * TM-10320  This changesets will create the tables metric_definition and metric_result
 */
	changeSet(author: 'ecantu', id: 'TM-10320-3') {
		comment("Create metric_definition table")

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'metric_definition')
			}
		}
		createTable(tableName: 'metric_definition') {
			column(name: 'metric_definition_id', type: 'BIGINT(20)', autoIncrement: 'true'){
				constraints( primaryKey:'true', nullable:'false')
			}
			column(name: 'code', type: 'VARCHAR(255)' ) {
				constraints(nullable:'false')
			}
			column(name: 'mode', type: 'VARCHAR(20)') {
				constraints(nullable:'false')
			}
			column(name: 'description', type: 'VARCHAR(255)') {
				constraints(nullable:'false')
			}
			column(name: 'enabled', type: 'INT(1)') {
				constraints(nullable:'false')
			}
			column(name: 'definition', type: 'json') {
				constraints(nullable:'false')
			}
			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}
			column(name: 'last_updated', type: 'DATETIME') {
				constraints(nullable: 'true')
			}
		}
		createIndex(indexName:'metric_definition_code_idx', tableName:'metric_definition', unique:true) {
			column(name:'code')
		}
	}

	changeSet(author: 'ecantu', id: 'TM-10320-4') {
		comment("Create metric_result table")

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'metric_result')
			}
		}
		createTable(tableName: 'metric_result') {
			column(name: 'project_metric_id', type: 'BIGINT(20)', autoIncrement: 'true'){
				constraints( primaryKey:'true', nullable:'false')
			}
			column(name: 'project_id', type: 'BIGINT') {
				constraints(nullable: 'false')
			}
			column(name: 'metric_definition_code', type: 'VARCHAR(255)') {
				constraints(nullable:'false')
			}
			column(name: 'date', type: 'DATETIME') {
				constraints(nullable: 'false')
			}
			column(name: 'label', type: 'VARCHAR(255)') {
				constraints(nullable:'false')
			}
			column(name: 'value', type: 'BIGINT') {
				constraints(nullable: 'true')
			}
		}
		createIndex(tableName:'metric_result', indexName:'idx_metric_result_project_code_date_label_composite_key', unique:'true') {
			column(name:'project_id')
			column(name:'metric_definition_code')
			column(name:'date')
			column(name:'label')
		}
			addForeignKeyConstraint(
			'baseColumnNames': 'project_id',
			'baseTableName': 'metric_result',
			'constraintName': 'fk_metric_result_project_id',
			'onDelete': 'CASCADE',
			'referencedColumnNames': 'project_id',
			'referencedTableName': 'project'
		)
	}
}
