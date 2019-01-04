/**
 * Drop party column from the project_logo table
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150529 TM-3894-1") {
		comment('project_logo')
		sql("ALTER TABLE project_logo DROP index FK37FD3B11D8F64C6D;")
		sql("ALTER TABLE project_logo DROP COLUMN party_id;")
	}
}
