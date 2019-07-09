/**
 * Updating the role prefix ROLE_
 */
databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20190627 TM-13321-4") {
		comment('Rolling back role prefix ROLE_.')
		sql('''INSERT INTO role_type  (role_type_code,description,help,type, level)
						  SELECT SUBSTRING(role_type_code,6) AS role_type_code, description AS description, help AS help,
						   SUBSTRING(type,6) AS type, level AS level FROM role_type 
						   WHERE type != 'ROLE_SECURITY';''')

		sql('''UPDATE workflow_transition SET role_id = SUBSTRING(role_id,6);''')
	}

	changeSet(author: "tpelletier", id: "20190627 TM-13321-5") {
		comment('Rolling back Cleaning up role prefix ROLE_.')
		sql('''DELETE FROM role_type
				WHERE type != 'ROLE_SECURITY' AND role_type_code like "ROLE_%" ;''')
	}

	changeSet(author: "tpelletier", id: "20190627 TM-13321-6") {
		comment('Rolling back fixing party role prefix ROLE_.')
		sql('''UPDATE party_relationship 
                       SET role_type_code_from_id = SUBSTRING(role_type_code_from_id,6), role_type_code_to_id = SUBSTRING(role_type_code_to_id,6);''')
	}

	changeSet(author: "tpelletier", id: "20190627 TM-13321-7") {
		comment('Rolling back add ROLE_ prefix')
		sql('UPDATE move_event_staff SET role_id = SUBSTRING(role_id, 6) WHERE  role_id like "ROLE_%" ;')
	}


	changeSet(author: "tpelletier", id: "20190627 TM-13321-8") {
		comment('Rolling back role prefix ROLE_.')
		sql('''UPDATE role_type  SET type = SUBSTRING(type,6) WHERE type = 'ROLE_SECURITY';''')
	}
}


