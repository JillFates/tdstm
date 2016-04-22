/**
 * @author @tavo_luna
 * TM-4697 Fixing orphan records for cookbook tasks by cascade delete using a FK in the Database
 */
import org.codehaus.groovy.grails.commons.ApplicationHolder
import groovy.time.TimeDuration

databaseChangeLog = {	
	changeSet(author: "oluna", id: "20160315 TM-4697-1-FIX") {
		comment('Clear orphan task_dependencies')
		grailsChange {
			change {
				def limit = 500000				
				
				def totalDeps = sql.firstRow("select count(*) as total FROM task_dependency td LEFT OUTER JOIN asset_comment ac on ac.asset_comment_id = td.asset_comment_id where ac.asset_comment_id IS NULL")
				log.info("Orphans: ${totalDeps.total} to clear")

				def numBatchs = Math.ceil(totalDeps.total / limit).intValue()

				//using a for statement to save some memory and avoid an extra if 
				for(int i=1; i<=numBatchs; i++){
					log.info(String.format("Cleared %.2f %%", i*100/numBatchs))
					sql.executeUpdate("""
						DELETE FROM task_dependency WHERE task_dependency_id IN (
							SELECT id FROM (
								SELECT DISTINCT task_dependency_id as id FROM task_dependency td
								LEFT OUTER JOIN asset_comment t1 on t1.asset_comment_id = td.asset_comment_id
								WHERE t1.asset_comment_id IS NULL
								LIMIT ${limit}
							) t
						);
		    	""")
				}
				log.info("Orphans cleared!")

				log.info("Creating Fk-Constraint")
				sql.execute("""
					ALTER TABLE task_dependency
					  ADD CONSTRAINT fk_task_dep_asset_comment_id
					  FOREIGN KEY (asset_comment_id) REFERENCES asset_comment (asset_comment_id)
					  ON UPDATE CASCADE
					  ON DELETE CASCADE
				""")

				confirm "task_dependencies: Orphans Cleared and Fk-Constraint Created"
			}
		}
	}

	changeSet(author: "oluna", id: "20160315 TM-4697-2") {
		comment('Fix Orphan records for comment_note (tasks)')
		//delete and set cascade to comment
		sql("DELETE cn FROM comment_note cn LEFT OUTER JOIN asset_comment ac ON ac.asset_comment_id = cn.asset_comment_id WHERE ac.asset_comment_id IS NULL")
		sql("""
			ALTER TABLE comment_note
			  ADD CONSTRAINT fk_comment_note_asset_comment_id
			  FOREIGN KEY (asset_comment_id) REFERENCES asset_comment (asset_comment_id)
			  ON UPDATE CASCADE
			  ON DELETE CASCADE
		""")
	}
}