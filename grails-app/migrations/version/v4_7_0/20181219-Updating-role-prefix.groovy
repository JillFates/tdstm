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

	changeSet(author: "jdanahy", id: "20191219 TM-13991") {
		comment('Fixing role type to add ROLE_ prefix')
		sql('UPDATE role_type SET type = CONCAT("ROLE_", type);')
	}


	changeSet(author: "tpelletier", id: "20190627 TM-13321-4") {
		comment('Rolling back role prefix ROLE_.')
		sql('''INSERT INTO role_type  (role_type_code,description,help,type, level)
						  SELECT SUBSTRING(role_type_code,6) AS role_type_code, description AS description, help AS help,
						   SUBSTRING(type,6) AS type, level AS level FROM role_type where type != 'ROLE_SECURITY';''')
		sql('''UPDATE party_role SET role_type_id = SUBSTRING(role_type_id,6)
				WHERE role_type_id NOT IN("ROLE_ADMIN","ROLE_CLIENT_ADMIN","ROLE_CLIENT_MGR","ROLE_EDITOR","ROLE_SUPERVISOR","ROLE_USER");''')
		sql('''UPDATE workflow_transition SET role_id = SUBSTRING(role_id,6) 
					WHERE role_id NOT IN("ROLE_ADMIN","ROLE_CLIENT_ADMIN","ROLE_CLIENT_MGR","ROLE_EDITOR","ROLE_SUPERVISOR","ROLE_USER");''')
	}

	changeSet(author: "tpelletier", id: "20190627 TM-13321-5") {
		comment('Rolling back Cleaning up role prefix ROLE_.')
		sql('''DELETE FROM role_type
				WHERE role_type_code like "ROLE_%" 
				AND role_type_code NOT IN("ROLE_ADMIN","ROLE_CLIENT_ADMIN","ROLE_CLIENT_MGR","ROLE_EDITOR","ROLE_SUPERVISOR","ROLE_USER") ;''')
	}

	changeSet(author: "tpelletier", id: "20190627 TM-13321-6") {
		comment('Rolling back fixing party role prefix ROLE_.')
		sql('''UPDATE party_relationship 
                       SET role_type_code_from_id = SUBSTRING(role_type_code_from_id,6), role_type_code_to_id = SUBSTRING(role_type_code_to_id,6) 
                       WHERE role_type_code_from_id NOT IN("ROLE_ADMIN","ROLE_CLIENT_ADMIN","ROLE_CLIENT_MGR","ROLE_EDITOR","ROLE_SUPERVISOR","ROLE_USER") 
                       AND role_type_code_to_id NOT IN("ROLE_ADMIN","ROLE_CLIENT_ADMIN","ROLE_CLIENT_MGR","ROLE_EDITOR","ROLE_SUPERVISOR","ROLE_USER");''')
	}

	changeSet(author: "tpelletier", id: "20190627 TM-13321-7") {
		comment('Rolling back add ROLE_ prefix')
		sql('UPDATE move_event_staff SET role_id = SUBSTRING(role_id, 6) WHERE  role_id like "ROLE_%" ;')
	}
}


