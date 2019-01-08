/**
 * Updating the role prefix ROLE_
 */
databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20191219 TM-13321-1") {
		comment('Updating role prefix ROLE_.')
		sql('''INSERT INTO role_type  (role_type_code,description,help,type, level)
					 SELECT CONCAT("ROLE_", role_type_code) AS role_type_code, description AS description, help AS help, type AS type, level AS level FROM role_type;''')
		sql('UPDATE role_permissions SET role = CONCAT("ROLE_", role);')
		sql('UPDATE party_role SET role_type_id = CONCAT("ROLE_", role_type_id);')
		sql('UPDATE workflow_transition SET role_id = CONCAT("ROLE_", role_id);')
	}

	changeSet(author: "tpelletier", id: "20191219 TM-13321-2") {
		comment('Cleaning up Updating role prefix ROLE_.')
		sql('DELETE FROM role_type WHERE role_type_code not like "ROLE_%";')
	}

	changeSet(author: "tpelletier", id: "20191219 TM-13321-3") {
			comment('fixing party role prefix ROLE_.')
			sql('UPDATE party_relationship SET role_type_code_from_id = CONCAT("ROLE_", role_type_code_from_id), role_type_code_to_id = CONCAT("ROLE_", role_type_code_to_id);')
		}
}


