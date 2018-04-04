/**
 * @author ecantu
 * TM-10091 Create migration script to rename DataScripts sharing the same name
 * This migration script is related to 20180302-Dataview-add-unique-constraint.groovy.
 * It fixes a problem when the views with the same name that should be renamed,
 * differ from each other in a trailing/leading space or tab character.
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "TM-10091-1") {
		comment("If there are already duplicated records for the same name + it.id, rename before applying the constraint." +
				"Take into account spaces and tabs.")
		grailsChange{
			change {
				def duplicatedDataViews = sql.rows('''
					SELECT project_id, TRIM(CHAR(9) FROM TRIM(name)) n
					FROM dataview
					GROUP  BY project_id, n
					HAVING COUNT(*) > 1;
				''')

				if (duplicatedDataViews) {
					duplicatedDataViews.each { duplicated ->
						sql.execute("UPDATE dataview SET name = CONCAT(name, ' ', id) WHERE name = '${duplicated.n}' AND project_id = ${duplicated.project_id}")
					}
				}
			}
		}
	}
}
