databaseChangeLog = {
	changeSet(author: "jmartin", id: "20150408 TM-3749-1") {
		comment('Update the PartyRole codes to uppercase')
			sql('update party_role set role_type_id=upper(role_type_id)')
	}
}
