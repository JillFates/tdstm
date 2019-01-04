/**
 * Delete invalid security references in party role table
 */

databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150319 TM-3705-1") {
		comment('Delete invalid security references in party role table.')
		sql("""
			delete from party_role
			where party_id in (
				select person_id 
				from person 
				where person_id not in (
					select person_id from user_login where user_login.person_id = person_id
				)
			) and
			role_type_id in ('USER', 'EDITOR', 'SUPERVISOR')
		""")
	}
}
