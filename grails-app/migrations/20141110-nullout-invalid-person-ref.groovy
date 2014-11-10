
/**
 * This change set is used to change column note to datatype text
 */
databaseChangeLog = {

	changeSet(author: "jmartin", id: "20141110 TM-3549-1") {
		comment('Change asset_dependency updated_by to be NULLABLE')

		sql("ALTER TABLE asset_dependency MODIFY COLUMN updated_by BIGINT(20) DEFAULT NULL")
	}

	changeSet(author: "jmartin", id: "20141110 TM-3549-2") {
		comment('Null out person references that have been orphaned')
	
		List statements = [
			"""UPDATE asset_entity ae 
			LEFT OUTER JOIN person p on p.person_id = ae.modified_by 
			SET ae.modified_by = null
			WHERE ae.modified_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE asset_comment
			LEFT OUTER JOIN person p on p.person_id = created_by 
			SET created_by = null
			WHERE created_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE asset_comment 
			LEFT OUTER JOIN person p on p.person_id = resolved_by 
			SET resolved_by = null
			WHERE resolved_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE asset_dependency
			LEFT OUTER JOIN person p on p.person_id = created_by 
			SET created_by = null
			WHERE created_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE asset_dependency 
			LEFT OUTER JOIN person p on p.person_id = updated_by 
			SET updated_by = null
			WHERE updated_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE comment_note
			LEFT OUTER JOIN person p on p.person_id = created_by_id 
			SET created_by_id = null
			WHERE created_by_id IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE model
			LEFT OUTER JOIN person p on p.person_id = created_by 
			SET created_by = null
			WHERE created_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE model
			LEFT OUTER JOIN person p on p.person_id = updated_by 
			SET updated_by = null
			WHERE updated_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE model
			LEFT OUTER JOIN person p on p.person_id = validated_by 
			SET validated_by = null
			WHERE validated_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE move_event_news 
			LEFT OUTER JOIN person p on p.person_id = created_by 
			SET created_by = null
			WHERE created_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE move_event_news 
			LEFT OUTER JOIN person p on p.person_id = archived_by 
			SET archived_by = null
			WHERE archived_by IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE recipe_version 
			LEFT OUTER JOIN person p on p.person_id = created_by_id 
			SET created_by_id = null
			WHERE created_by_id IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE task_batch
			LEFT OUTER JOIN person p on p.person_id = created_by_id 
			SET created_by_id = null
			WHERE created_by_id IS NOT NULL AND p.person_id IS NULL""",

			"""UPDATE workflow 
			LEFT OUTER JOIN person p on p.person_id = updated_by 
			SET updated_by = null
			WHERE updated_by IS NOT NULL AND p.person_id IS NULL""",

			"""DELETE move_event_staff
			FROM move_event_staff
			LEFT OUTER JOIN person p USING(person_id)
			WHERE p.person_id IS NULL""",

			"""DELETE exception_dates
			FROM exception_dates
			LEFT OUTER JOIN person p USING(person_id)
			WHERE p.person_id IS NULL""",

			"""DELETE user_login
			FROM user_login
			LEFT OUTER JOIN person p USING(person_id)
			WHERE p.person_id IS NULL"""
		]

		grailsChange {
			change {
				statements.each {s ->
					int n = sql.executeUpdate(s)
					println "${s.split(/\n/)[0]} updated $n row${n!=1 ? 's' : ''}"
				}
			}
		}
	}
}

