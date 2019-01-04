databaseChangeLog = {	
	changeSet(author: "jmartin", id: "20160428 TM-4829-1") {
		comment('Remove PartyRole where the party does not exist')		
		sql("""
			DELETE FROM party_role WHERE party_id IN (
		        SELECT * FROM (
		                SELECT pr.party_id FROM party_role pr
		                LEFT OUTER JOIN party p ON p.party_id = pr.party_id
		                WHERE p.party_id IS NULL
		        ) AS x
			)
		""")
	}

	changeSet(author: "jmartin", id: "20160428 TM-4829-2") {
		comment('Remove security related PartyRole where the party does not have a UserLogin')		
		sql("""
			DELETE pr
			FROM party_role AS pr
			JOIN role_type rt ON rt.role_type_code = pr.role_type_id AND rt.type='SECURITY'
			LEFT OUTER JOIN user_login u ON u.person_id = pr.party_id
			WHERE u.user_login_id IS NULL
		""")
	}

	changeSet(author: "jmartin", id: "20160428 TM-4829-3") {
		comment('Remove party relationship where the role_type_from and/or to is missing')		
		sql("""
			DELETE pr
			FROM party_relationship pr
			LEFT OUTER JOIN role_type rtfrom ON rtfrom.role_type_code = pr.role_type_code_from_id
			LEFT OUTER JOIN role_type rtto ON rtto.role_type_code = pr.role_type_code_to_id
			WHERE rtfrom.role_type_code IS NULL OR rtto.role_type_code IS NULL
		""")
	}

	changeSet(author: "jmartin", id: "20160428 TM-4829-4") {
		comment('Remove Party Relationships where either party is missing')		
		sql("""
			DELETE pr
			FROM party_relationship pr
			LEFT OUTER JOIN party pfrom ON pfrom.party_id = party_id_from_id
			LEFT OUTER JOIN party pto ON pto.party_id = party_id_to_id
			WHERE pfrom.party_id IS NULL OR pto.party_id IS NULL
		""")
	}

	changeSet(author: "jmartin", id: "20160428 TM-4829-5") {
		comment('Remove PartyRelationships that are TEAM/TEAM_MEMBER relations')		
		sql("""
			DELETE pr
			FROM party_relationship pr 
			WHERE role_type_code_from_id='TEAM' AND role_type_code_to_id='TEAM_MEMBER'
		""")
	}

	changeSet(author: "jmartin", id: "20160428 TM-4829-6") {
		comment('Remove invalid PROJECT/STAFF assignments')		
		sql("""
			DELETE pr
			FROM party_relationship pr 
			WHERE party_relationship_type_id='STAFF' 
				AND role_type_code_from_id='PROJECT' 
				AND role_type_code_to_id='STAFF'
		""")
	}

}
