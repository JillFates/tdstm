
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.ProjectService

/**
 * This set of database changes will create the default_bundle_id column in project table and
 * Update all projects to set "default_bundle_id" and update asset_entity move_bundle_id to default bundle if null
 * and set asset_entity's move_bundle_id column to not null
 */

databaseChangeLog = {

	def projectService = new ProjectService()


	/**
	 * Add project.default_bundle_id (nullable) 
	 * 
	 */
	changeSet(author: "lokanada", id: "20140709 TM-2965-1") {
		comment('Add "default_bundle_id" column in project table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'project', columnName:'default_bundle_id' )
			}
		}
		sql(" ALTER TABLE project ADD COLUMN default_bundle_id BIGINT(20) DEFAULT null")

	}

	/**
	 * Update all projects to set "default_bundle_id" to their TBD bundle where they exist
	 * 
	 */
	changeSet(author: "lokanada", id: "20140709 TM-2965-20") {
		comment('Update all projects to set "default_bundle_id" to their TBD bundle where they exist')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'project', columnName:'default_bundle_id' )
		}
		grailsChange {
			change {
				def projects = Project.findAllByDefaultBundleIsNull()
				projects.each{ proj->
					proj.defaultBundle =  projectService.getDefaultBundle( proj )
					if ( ! proj.save(flush:true) ) {
						throw new RuntimeException('updating project to default bundle failed');
					}
				}
			 }
		}
	}


	/**
	 * 	update asset_entity move_bundle_id to default bundle if null
	 */
	changeSet(author: "lokanada", id: "20140709 TM-2965-30") {
		comment('update asset_entity move_bundle_id to default bundle if null')
		sql(""" UPDATE  asset_entity ae 
			  	LEFT JOIN project p
				ON ae.project_id = p.project_id
				SET ae.move_bundle_id = p.default_bundle_id
				WHERE ae.move_bundle_id IS NULL
			""")

	}


	/**
	 * 
	 * set asset_entity's move_bundle_id column to not null
	 */
	changeSet(author: "lokanada", id: "20140709 TM-2965-40") {
		comment("set asset_entity's move_bundle_id column to not null")
		sql(" ALTER TABLE asset_entity MODIFY COLUMN move_bundle_id  BIGINT(20) NOT NULL ")
	}
}
