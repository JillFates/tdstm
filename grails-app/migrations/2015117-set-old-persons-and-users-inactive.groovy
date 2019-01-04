/**
* Database migration script for the TransitionManager application
*/
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20151117 TM-4148-01") {
		comment('Set TDS and EMC persons as inactive that have not used the system in long time')
		sql("""
			CREATE TEMPORARY TABLE inactivate_old_staff
				SELECT p.person_id as pid
				FROM person p
				JOIN party pp ON 
					pp.party_id = p.person_id
				JOIN party_relationship pr ON
					pr.party_relationship_type_id='STAFF' AND
					pr.party_id_from_id in (
						SELECT party_id_to_id FROM party_relationship 
						WHERE party_id_from_id=18 AND party_relationship_type_id='PARTNERS'
					) AND
					pr.role_type_code_from_id = 'COMPANY' AND
					pr.party_id_to_id = p.person_id AND
					pr.role_type_code_to_id = 'STAFF'    
				LEFT JOIN user_login u ON 
					u.person_id=p.person_id
				WHERE
					p.active = 'Y'
					AND pp.last_updated < '2015-01-01 00:00:00'
					AND p.person_id NOT IN (5, 59, 100, 177, 335, 5648, 9, 314, 580, 2366, 3642, 5345, 2270, 5677, 380)
					AND (u.username IS NULL OR u.last_login < '2015-06-01 00:00:00' OR u.active = 'N')
		""")

		// Set the person active='N'
		sql("UPDATE person SET active = 'N' WHERE person_id IN (SELECT pid FROM inactivate_old_staff)")

		// Update the person (aka party) last_updated
		sql("UPDATE party SET last_updated = now() WHERE party_id IN (SELECT pid FROM inactivate_old_staff)")

		sql("DROP TEMPORARY TABLE IF EXISTS inactivate_old_staff")
	}
	
	changeSet(author: "jmartin", id: "20151117 TM-4148-02") {
		comment('Inactivate any user account where the person is also inactive')
		sql("update user_login set active='N' where active='Y' AND person_id in (select person_id from person where active='N')")
	}
}
