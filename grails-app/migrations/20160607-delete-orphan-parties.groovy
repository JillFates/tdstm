databaseChangeLog = {
	changeSet(author: "arecordon", id: "20160607-TM-4565") {
		comment('Deletes orphan parties.')
		sql("""DELETE pa FROM party pa
			LEFT OUTER JOIN person  ON pa.party_id = person_id
			LEFT OUTER JOIN move_bundle ON pa.party_id = move_bundle_id 
			LEFT OUTER JOIN party_group ON pa.party_id = party_group_id 
			LEFT OUTER JOIN party_relationship pr1 ON  pa.party_id = pr1.party_id_from_id
			LEFT OUTER JOIN party_relationship pr2 ON pa.party_id = pr2.party_id_to_id
			LEFT OUTER JOIN contact_mech cm ON pa.party_id = cm.party_id
			LEFT OUTER JOIN party_role pr ON pa.party_id = pr.party_id
			WHERE person_id IS NULL AND party_type_id='PERSON' AND move_bundle_id IS NULL 
			AND party_group_id IS NULL AND pr1.party_id_from_id IS NULL AND pr2.party_id_to_id IS NULL 
			AND cm.party_id IS NULL AND pr.party_id IS NULL""")
	}
}
