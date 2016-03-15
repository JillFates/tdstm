/**
 * @author @tavo_luna
 * TM-4697 Fixing orphan records for cookbook tasks by cascade delete using a FK in the Database
 */
databaseChangeLog = {	
	changeSet(author: "oluna", id: "20160315 TM-4697") {
		comment('Fix Orphan records for asset_comment (tasks)')

		//delete and set cascade to assetComment
		sql("DELETE td FROM task_dependency td LEFT OUTER JOIN asset_comment ac on ac.asset_comment_id = td.asset_comment_id WHERE ac.asset_comment_id IS NULL")
		sql("""
			ALTER TABLE task_dependency
			  ADD CONSTRAINT fk_task_dep_asset_comment_id
			  FOREIGN KEY (asset_comment_id) REFERENCES asset_comment (asset_comment_id)
			  ON UPDATE CASCADE
			  ON DELETE CASCADE
		""")		

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