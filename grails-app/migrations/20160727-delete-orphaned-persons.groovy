
/**
 * Deletes orphaned persons.
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20160727 TM-4888-2") {
		comment('Deletes orphaned persons using personService.bulkDelete.')
		grailsChange{
			change{
				List orphans = sql.rows("""
						select p.person_id 
						from person p left outer join party_relationship pr 
						on pr.party_id_to_id = p.person_id and pr.role_type_code_from_id = 'COMPANY' 
						and pr.role_type_code_to_id='STAFF'
						left outer join party on party.party_id = p.person_id
						where pr.party_relationship_type_id is null and first_name <> 'Automated' and last_name <> 'Task'; 
						""")
				List ids = orphans.collect{it.person_id.toString()}

				Person.withNewSession{
					def personService = ctx.getBean("personService")
					ids.each{
						personService.deletePerson(Person.get(it), true, true)
					}	
				}
			}
		}
	}
}
