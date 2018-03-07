/**
 * @author ecantu
 * TM-8310 Prevent user from creating a Dataview with the same name - Back end Work
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "TM-8310-1") {
		comment("If there are already duplicated records for the same name + it.id, rename before applying the constraint.")
		grailsChange{
			change {
				def duplicatedDataViews = sql.rows('''
					SELECT project_id, name 
					FROM dataview 
					GROUP  BY project_id, name
					HAVING COUNT(*) > 1
					''')

				if (duplicatedDataViews) {
					duplicatedDataViews.each { duplicated ->
						sql.execute("UPDATE dataview SET name = CONCAT(name, ' ', id) WHERE name = '${duplicated.name}' AND project_id = ${duplicated.project_id}")
					}
				}
			}
		}
	}

	changeSet(author: "ecantu", id: "TM-8310-2") {
		comment("Add unique constraint to Dataview table on name and project_id fields.")

		addUniqueConstraint(
			constraintName: 'UK_dataview_project_name',
			tableName: 'dataview',
			columnNames: 'name, project_id'
		)
	}
}
