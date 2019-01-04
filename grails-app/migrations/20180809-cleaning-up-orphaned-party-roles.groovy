/**
 * This changelog convert any black tags from testing to grey tags.
 */
databaseChangeLog = {
	changeSet(author: "tpelletier", id: "20180809 TM-11645-1") {
		comment("Delete orphaned party roles.")
		sql("""
			DELETE pr FROM party_role pr
			LEFT JOIN user_login ul ON ul.person_id = pr.party_id
			WHERE ul.user_login_id is NULL;
		""")
	}
}
