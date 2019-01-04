/**
* Database migration script for the TransitionManager application
*/
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20151113 TM-4148") {
		comment('Create PROJ_STAFF PartyRelationship records for people that have TEAM assignments but are missing the STAFF assignment')
		sql("""
			INSERT INTO party_relationship 
				SELECT 'PROJ_STAFF', team.party_id_from_id, team.party_id_to_id, 'PROJECT', 'STAFF', '' AS comment, '' AS sc 
				FROM party_relationship team
				LEFT JOIN party_relationship staff ON
				   staff.party_id_from_id = team.party_id_from_id
				   AND staff.party_id_to_id = team.party_id_to_id
				   AND staff.party_relationship_type_id = 'PROJ_STAFF'
				   AND staff.role_type_code_from_id = 'PROJECT'
				   AND staff.role_type_code_to_id = 'STAFF'
				WHERE 
				   team.party_relationship_type_id = 'PROJ_STAFF'
				   AND team.role_type_code_from_id = 'PROJECT'
				   AND team.role_type_code_to_id <> 'STAFF'
				   AND staff.party_relationship_type_id IS NULL
				GROUP BY team.party_id_from_id, team.party_id_to_id
		""")
	}
}
