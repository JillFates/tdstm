/**
 * Remove asset transition table
 */
databaseChangeLog = {
	
	changeSet(author: "rmacfarlane", id: "20160711 TM-5081-1") {
		comment('Remove contact_mech table')
		sql("DROP TABLE contact_mech")
	}
}
