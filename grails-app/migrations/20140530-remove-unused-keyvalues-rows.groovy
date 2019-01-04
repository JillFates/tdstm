databaseChangeLog = {
	
	//This changeset will remove all unused rows from the table key_value
	changeSet(author: "dscarpa", id: "20140530 TM-2747") {
		comment('Remove rows from table key_value that have an invalid project')
		sql("""
			DELETE FROM key_value 
				WHERE key_value.project_id NOT IN 
					(SELECT project_id FROM project);
		""")
	}
}
