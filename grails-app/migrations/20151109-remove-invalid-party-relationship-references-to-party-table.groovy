/**
 * Delete invalid references in party relationship taable
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20151109 TM-4197-1") {
		comment('Remove invalid references.')
		sql("""
			DELETE FROM party_relationship WHERE party_id_from_id NOT IN (SELECT party.party_id FROM party);
			DELETE FROM party_relationship WHERE party_id_to_id NOT IN (SELECT party.party_id FROM party);
		""")
	}
}
