/**
 * @author @tavo_luna
 * TM-4697 Fixing orphan records for cookbook tasks by cascade delete using a FK in the Database
 * 2016-04-25 Fix a problem when there where no orphan records and validate if the FK constraint is already in the DB
 */

databaseChangeLog = {	
	changeSet(author: "oluna", id: "20160315 TM-4697-1-FIX") {
		def constraint_name = "fk_task_dep_asset_comment_id"

		comment('Clear orphan task_dependencies --FIX to ZERO DIV')
		preConditions(onFail:'MARK_RAN', onFailMessage:"Constraint '$constraint_name' already exists in the Schema") {
			sqlCheck(expectedResult:"0", """
	        select count(*) from information_schema.table_constraints where constraint_schema = database() and CONSTRAINT_NAME='$constraint_name';
	    """)
		}
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
				/*
				2026-04-25 @tavo_luna NOTE: we need to concat the ALTER SQL with the constraint name to execute the code
				correctly, for weird reasons it won't work if interpolated, YOU'VE BEEN WARNED...
				*/
				sql.execute("""
					ALTER TABLE task_dependency
					  ADD CONSTRAINT """ + constraint_name + """
					  FOREIGN KEY (asset_comment_id) REFERENCES asset_comment (asset_comment_id)
					  ON UPDATE CASCADE
					  ON DELETE CASCADE
				""")

				confirm "task_dependencies: Orphans Cleared and Fk-Constraint Created"
			}
		}
	}

	changeSet(author: "oluna", id: "20160315 TM-4697-1.5-FIX") {
		comment('Clear orphan task_dependencies for predecessors note relationship')		
		grailsChange {
			change {
				def limit = 500000				
				
				def totalDeps = sql.firstRow("select count(*) as total FROM task_dependency td LEFT OUTER JOIN asset_comment t2 ON t2.asset_comment_id = td.predecessor_id WHERE t2.asset_comment_id IS NULL")
				log.info("1.5 Orphans: ${totalDeps.total} to clear")

				def numBatchs = Math.ceil(totalDeps.total / limit).intValue()

				//using a for statement to save some memory and avoid an extra if 
				for(int i=1; i<=numBatchs; i++){
					log.info(String.format("Cleared %.2f %%", i*100/numBatchs))
					sql.executeUpdate("""
						DELETE FROM task_dependency WHERE task_dependency_id IN (
							SELECT id FROM (
								SELECT DISTINCT task_dependency_id as id FROM task_dependency td
								LEFT OUTER JOIN asset_comment t2 ON t2.asset_comment_id = td.predecessor_id
								WHERE t2.asset_comment_id IS NULL
								LIMIT ${limit}
							) t
						);
		    	""")
				}
				log.info("1.5 Orphans cleared!")
				confirm "task_dependencies: predecessors Orphans Cleared"
			}
		}
	}

	changeSet(author: "oluna", id: "20160315 TM-4697-2") {
		def constraint_name = "fk_comment_note_asset_comment_id"
		comment('Fix Orphan records for comment_note (tasks)')
		preConditions(onFail:'MARK_RAN', onFailMessage:"Constraint '$constraint_name' already exists in the Schema") {
			sqlCheck(expectedResult:"0", """
	        select count(*) from information_schema.table_constraints where constraint_schema = database() and CONSTRAINT_NAME='$constraint_name';
	    """)
		}
		//delete and set cascade to comment
		sql("DELETE cn FROM comment_note cn LEFT OUTER JOIN asset_comment ac ON ac.asset_comment_id = cn.asset_comment_id WHERE ac.asset_comment_id IS NULL")
		/*
			2026-04-25 @tavo_luna NOTE: we need to concat the ALTER SQL with the constraint name to execute the code
			correctly, for weird reasons it won't work if interpolated, YOU'VE BEEN WARNED...
		*/
		sql("""
			ALTER TABLE comment_note
			  ADD CONSTRAINT """ + constraint_name + """
			  FOREIGN KEY (asset_comment_id) REFERENCES asset_comment (asset_comment_id)
			  ON UPDATE CASCADE
			  ON DELETE CASCADE
		""")
	}
}
