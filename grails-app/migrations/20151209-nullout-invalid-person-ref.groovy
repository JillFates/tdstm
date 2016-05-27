
/**
 * This change set is used to change column note to datatype text
 */
databaseChangeLog = {

	changeSet(author: "arecordon", id: "20160526 TM-3549-1") {
		comment('Null out person references that have been orphaned for Application')

		sql("ALTER TABLE asset_dependency MODIFY COLUMN updated_by BIGINT(20) DEFAULT NULL")

		List statements = [
			"""UPDATE application app 
			LEFT OUTER JOIN person p on p.person_id = app.shutdown_by 
			SET app.shutdown_by = null
			WHERE app.shutdown_by REGEXP '^[0-9]+\$' AND p.person_id IS NULL""",

			"""UPDATE application app 
			LEFT OUTER JOIN person p on p.person_id = app.startup_by 
			SET app.startup_by = null
			WHERE app.startup_by REGEXP '^[0-9]+\$' AND p.person_id IS NULL""",

			"""UPDATE application app 
			LEFT OUTER JOIN person p on p.person_id = app.testing_by 
			SET app.testing_by = null
			WHERE app.testing_by REGEXP '^[0-9]+\$' AND p.person_id IS NULL""",
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
