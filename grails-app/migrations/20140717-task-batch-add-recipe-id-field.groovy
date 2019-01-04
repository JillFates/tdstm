
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.ProjectService

/**
 * This set of database changes will:
 *  - Add "recipe_id" column in task_batch table
 *  - Update all task_batch to set "recipe_id" to the recipe associated to the recipe_version_used_id
 */

databaseChangeLog = {

	def projectService = new ProjectService()


	/**
	 * Add task_batch.recipe_id (nullable) 
	 * 
	 */
	changeSet(author: "dscarpa", id: "20140717 TM-2995-1") {
		comment('Add "recipe_id" column in task_batch table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'recipe_id' )
			}
		}
		sql(" ALTER TABLE task_batch ADD COLUMN recipe_id BIGINT(20) DEFAULT null")

	}

	/**
	 * Update all task_batch to set "recipe_id" to the recipe associated to the recipe_version_used_id
	 * 
	 */
	changeSet(author: "dscarpa", id: "20140717 TM-2995-2") {
		comment('Update all task_batch to set "recipe_id" to the recipe associated to the recipe_version_used_id')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'task_batch', columnName:'recipe_id' )
		}
		sql("""
			UPDATE task_batch tb 
            SET tb.recipe_id = (SELECT rv.recipe_id FROM recipe_version rv WHERE rv.recipe_version_id = tb.recipe_version_used_id)
            WHERE tb.recipe_version_used_id is not null
			""")
	}

}
