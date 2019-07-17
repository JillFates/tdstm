package version.v4_7_0

databaseChangeLog = {
	//In 4.7 the Grails upgrade I accidentally update this with the Role prefix changes, and since it hadn't been run in 4.7, and the gold db
	//didn't have any missing party assigment no error happened, but this causes and error when update legacy databases. To fix that I rolled
	//back the changes, and set a new id. Running this again shouldn't cause any errors.
	changeSet(author: 'slopez/tpelletier', id: '20190716-TM-12430-1a') {
		comment("People created during the import process are not assigned to the project.")

		grailsChange {
			change {

				// get a list of missing people assignments to projects
				List missingPartiesAssignments = sql.rows("""
					SELECT DISTINCT project_id, person_id FROM (
					  SELECT DISTINCT ae.project_id, ae.app_owner_id AS person_id FROM asset_entity ae
						UNION ALL 
					  SELECT ae.project_id, sme_id AS person_id FROM application a JOIN asset_entity ae WHERE ae.asset_entity_id=a.app_id
						UNION ALL 
					  SELECT ae.project_id, sme2_id AS person_id FROM application a JOIN asset_entity ae WHERE ae.asset_entity_id=a.app_id
						UNION ALL 
					  SELECT ae.project_id, CAST(a.shutdown_by AS UNSIGNED) AS person_id FROM application a JOIN asset_entity ae WHERE ae.asset_entity_id=a.app_id
						UNION ALL 
					  SELECT ae.project_id, CAST(a.startup_by AS UNSIGNED) AS person_id FROM application a JOIN asset_entity ae WHERE ae.asset_entity_id=a.app_id
						UNION ALL 
					  SELECT ae.project_id, CAST(a.testing_by AS UNSIGNED) AS person_id FROM application a JOIN asset_entity ae WHERE ae.asset_entity_id=a.app_id
					) AS tmp 
					LEFT JOIN (
					  SELECT * FROM party_relationship 
					  WHERE party_relationship_type_id = 'PROJ_STAFF'
					  AND role_type_code_from_id = 'PROJECT'
					  AND role_type_code_to_id = 'STAFF') pr ON pr.party_id_from_id=tmp.project_id AND pr.party_id_to_id=tmp.person_id
					WHERE 
					  tmp.person_id > 0 
					  AND pr.party_id_from_id IS NULL 
					  AND pr.party_id_to_id IS NULL
				""")

				// create people assignments to projects for missing people assignments list
				if (missingPartiesAssignments && missingPartiesAssignments.size() > 0) {
					missingPartiesAssignments.each { missingParty ->
						sql.execute("""
							INSERT INTO party_relationship (party_relationship_type_id, party_id_from_id, party_id_to_id, role_type_code_from_id, role_type_code_to_id, status_code)
							VALUES ('PROJ_STAFF', ${missingParty[0]}, ${missingParty[1]}, 'PROJECT', 'STAFF', 'ENABLED')
						""")
					}
				}

			}
		}
	}
}
