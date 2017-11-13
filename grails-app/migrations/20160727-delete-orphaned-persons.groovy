import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin

/**
 * Deletes orphaned persons.
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20160727 TM-4888-0") {
		comment('Delete any duplicate user accounts for users')
		grailsChange{
			change{
				println "**** DELETING DUPLICATE USERS FOR PERSON"
				// Get a list of person and associated user ids for persons that have more than one 
				// UserLogin.
				List duplicateUsers = sql.rows('''
					select person_id, user_login_id, last_login 
					from user_login 
					where person_id = (
						select person_id from (
							select person_id, count(*) as c from user_login group by person_id having c > 1) as p
					)
					order by last_login, user_login_id;
					''')

				if (duplicateUsers.size() > 0) {
					// This logic will delete all of the UserLogin for each person
					def securityService =  ctx.getBean("securityService")
					UserLogin.withNewSession {
						for (data in duplicateUsers) {
							def userLogin = UserLogin.get(data.user_login_id)
							securityService.deleteUserLogin(userLogin)
						}
					}
				}
			}
		}
	}

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
