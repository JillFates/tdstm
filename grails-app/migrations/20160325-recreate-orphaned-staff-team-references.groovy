databaseChangeLog = {

	changeSet(author: "jmartin", id: "20160325 TM-4707-1") {
		comment('Recreate orphaned person staff team records where they have been assigned to MOVE EVENTS for given teams')
		sql("""
			INSERT INTO party_relationship 
			(party_relationship_type_id, role_type_code_from_id, role_type_code_to_id, party_id_from_id, party_id_to_id, status_code) (
				SELECT DISTINCT 'STAFF', 'COMPANY', mes.role_id, co.party_id_from_id, p.person_id, 'ENABLED'
				FROM person p 
				JOIN party_relationship co ON
					co.party_id_to_id=p.person_id AND 
					co.party_relationship_type_id='STAFF' AND 
					co.role_type_code_from_id='COMPANY' AND 
					co.role_type_code_to_id='STAFF'
				JOIN move_event_staff mes ON
					mes.person_id=p.person_id
				-- JOIN to find missing STAFF/TEAM relationships that will need to be created
				LEFT OUTER JOIN party_relationship pr ON 
					pr.party_id_to_id=p.person_id AND 
					pr.party_relationship_type_id='STAFF' AND 
					pr.role_type_code_from_id='COMPANY' AND 
					pr.role_type_code_to_id = mes.role_id
				WHERE pr.party_id_to_id IS NULL
			)
		""")
	}

	changeSet(author: "jmartin", id: "20160325 TM-4707-2") {
		comment('Recreate orphaned person staff team records where they have been assigned to PROJECT for given teams')
		sql("""
			INSERT INTO party_relationship 
			(party_relationship_type_id, role_type_code_from_id, role_type_code_to_id, party_id_from_id, party_id_to_id, status_code) (
				SELECT distinct 'STAFF', 'COMPANY', ps.role_type_code_to_id, co.party_id_from_id, p.person_id, 'ENABLED'
				FROM person p 
				-- JOIN to match the person to their company
				JOIN party_relationship co ON 
					co.party_id_to_id=p.person_id AND 
					co.party_relationship_type_id='STAFF' AND 
					co.role_type_code_from_id='COMPANY' AND 
					co.role_type_code_to_id='STAFF'
				-- JOIN for PROJ_STAFF relationships
				JOIN party_relationship ps ON
					ps.party_id_to_id=p.person_id AND 
					ps.party_relationship_type_id='PROJ_STAFF' AND 
					ps.role_type_code_from_id='PROJECT' AND 
					ps.role_type_code_to_id <> 'STAFF'
				-- JOIN to find missing STAFF/TEAM relationships that will need to be created
				LEFT OUTER JOIN party_relationship pr ON 
					pr.party_id_to_id=p.person_id AND 
					pr.party_relationship_type_id='STAFF' AND 
					pr.role_type_code_from_id='COMPANY' AND 
					pr.role_type_code_to_id = ps.role_type_code_to_id
				WHERE pr.party_id_to_id IS NULL
			)
		""")
		
	}	
}
