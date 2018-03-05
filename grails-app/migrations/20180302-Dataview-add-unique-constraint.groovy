import java.util.List
/**
 * @author ecantu
 * TM-8310 Prevent user from creating a Dataview with the same name - Back end Work
 */
databaseChangeLog = {
	changeSet(author: "ecantu", id: "TM-8310-1") {
		comment("If there are already duplicated records for the same name + project_id, rename after applying the constraint.")
		grailsChange{
			change{
				println 'THIS IS INNNNN'
				// Check for duplicate records for name and project_id
				List duplicateRecords = sql.rows('''
					select count(*), name 
					from datavdiew d
					group by d.name, d.project_id
					having count(*) > 1
					''')
				if (duplicateRecords.size() > 0) {
					println ${duplicateRecords}
					// This logic will rename duplicates, so we can apply the constraint after
					int i = 1
					for (data in duplicateRecords) {
						data.name = data.name + i
						i++
					}
				}
			}
		}
	}

	changeSet(author: "ecantu", id: "TM-8310-2") {
		comment("Add unique constraint to Dataview table on name and project_id fields.")

		addUniqueConstraint(
						constraintName: 'uq_dataview_name_project_id',
						tableName: 'dataview',
						columnNames: 'name, project_id'
		)
	}
}


/**
 * If there are duplicated" records for the same name + project_id,
 * rename, so that will put the data consistent to apply
 * the unique constraint on those two fields
 * @param sql
 */
void renameDuplicateColumns(sql) {

	println 'THIS IS INNNNN'
	// Check for duplicate records for name and project_id
	List duplicateRecords = sql.rows('''
					select count(*), name 
					from dataview d
					group by d.name, d.project_id
					having count(*) > 1
					''')
	if (duplicateRecords.size() > 0) {
		println ${duplicateRecords}
		// This logic will rename duplicates, so we can apply the constraint after
		int i = 1
		for (data in duplicateRecords) {
			data.name = data.name + i
			i++
		}
	}
}
