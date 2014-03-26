import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import com.tdssrc.grails.GormUtil

/**
 * The PersonService class provides a number of functions to help in the management and access of Person objects
 */
class PersonService {

	def jdbcTemplate
	def namedParameterJdbcTemplate
	def sessionFactory
	def partyRelationshipService
	
	static List SUFFIXES = [
		"jr.", "jr", "junior", "ii", "iii", "iv", "senior", "sr.", "sr", //family
		"phd", "ph.d", "ph.d.", "m.d.", "md", "d.d.s.", "dds", // doctors
		"k.c.v.o", "kcvo", "o.o.c", "ooc", "o.o.a", "ooa", "g.b.e", "gbe", // knighthoods
		"k.b.e.", "kbe", "c.b.e.", "cbe", "o.b.e.", "obe", "m.b.e", "mbe", //   cont
		"esq.", "esq", "esquire", "j.d.", "jd", // lawyers
		"m.f.a.", "mfa", //misc
		"r.n.", "rn", "l.p.n.", "lpn", "l.n.p.", "lnp", //nurses
		"c.p.a.", "cpa", //money men
		"d.d.", "dd", "d.div.", "ddiv", //preachers
		"ret", "ret."
	]

	static List COMPOUND_NAMES = [
		"de", "la", "st", "st.", "ste", "ste.", "saint", "der", "al", "bin",
		"le", "mac", "di", "del", "vel", "van", "von", "e'", "san", "af", "el", "o'"
	]


	/**
	 * Returns a properly format person's last name with its suffix
	 * @param Map the map of last and suffix
	 * @return String the composite name with the suffix if it exists
	 */
	String lastNameWithSuffix(Map nameMap) {
		def last = ''

		if (nameMap.last && nameMap.suffix)
			last = "${nameMap.last}, ${nameMap.suffix}"
		else if (nameMap.last)
			last = nameMap.last

		return last
	}

	/** 
	 * Used to find a person by their name for a specified client
	 * @param client - The client that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */

	List findByClientAndName(PartyGroup client, Map nameMap) {
		def map = [client:client.id]
		StringBuffer query = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr JOIN person p ON p.person_id=pr.party_id_to_id')
		query.append(' WHERE pr.party_id_from_id=:client')
		query.append(' AND pr.role_type_code_from_id="COMPANY"')
		query.append(' AND pr.role_type_code_to_id="STAFF"')
		// query.append(' ')
		if (nameMap.first) {
			map.first = nameMap.first
			query.append(' AND p.first_name=:first' )
		}
		if (nameMap.last) {
			map.last = nameMap.last
			query.append(' AND p.last_name=:last' )
		}

		def persons
		def pIds = namedParameterJdbcTemplate.queryForList(query.toString(), map)

		if (nameMap.middle) {
			// Try to lookup the person with their middle name as well
			map.middle = nameMap.last
			query.append(' AND p.middle_name=:middle' )
			pIds.addAll( namedParameterJdbcTemplate.queryForList(query.toString(), map) )
		}

		if (pIds) {
			persons = Person.findAll('from Person p where p.id in (:ids)', [ids:pIds*.id])
		}

		return persons
	}  

	/** 
	 * Used to find a person by their name for a specified client
	 * @param client - The client that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */

	List findByClientAndEmail(PartyGroup client, String email) {
		def map = [client:client.id, email:email]
		StringBuffer query = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr JOIN person p ON p.person_id=pr.party_id_to_id')
		query.append(' WHERE pr.party_id_from_id=:client')
		query.append(' AND pr.role_type_code_from_id="COMPANY"')
		query.append(' AND pr.role_type_code_to_id="STAFF"')
		query.append(' AND p.email=:email')

		def persons
		def pIds = namedParameterJdbcTemplate.queryForList(query.toString(), map)
		if (pIds) {
			persons = Person.findAll('from Person p where p.id in (:ids)', [ids:pIds*.id])
		}

		return persons
	}  

	/**
	 * Used to find a person associated with a given project using a string representation of their name.
	 * This method overloads the other findPerson as a convinence so one can just pass the string vs parsing the name and calling the alternate method.
	 * @param A string representing a person's name (e.g. John Doe; Doe, John; John T. Doe)
	 * @param Project the project/client that the person is associated with
	 * @param The staff for the project. This is optional but recommended if the function is used repeatedly (use partyRelationshipService.getCompanyStaff(project?.client.id) to get list). 
	 * @param Flag used to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return Null if name unable to be parsed or a Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is 
	 * found. If more than one match is found then isAmbiguous will be set to true.
	 */
	Map findPerson(String name, Project project, def staffList = null, def clientStaffOnly=true) {
		def map = parseName(name)
		if (map)
			map = findPerson( map, project, staffList, clientStaffOnly )
		log.debug "findPerson(String) results=$map"
		return map
	}

	/**
	 * Used to find a person associated with a given project using a parsed name map
	 * @param Map containing person name elements
	 * @param Project the project/client that the person is associated with
	 * @param The staff for the project. This is optional but recommended if the function is used repeatedly 
	 *        Use partyRelationshipService.getAvailableProjectStaffPersons(project) in order to get list or 
	 *        partyRelationshipService.getCompanyStaff(companyId)
	 * @param Flag used to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return A Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is found. If more than one match is 
	 * found then isAmbiguous will be set to true.
	 */
	Map findPerson(Map nameMap , Project project, def staffList = null, def clientStaffOnly=true) {
		def results = [person:null, isAmbiguous:false]
		
		log.debug "findPersion() attempting to find nameMap=$nameMap in project $project"

		// Make sure we have a person
		if (! nameMap || ! nameMap.containsKey('first')) {
			results.isAmbiguous = true
			return results
		}

		// Get the staffList if not passed into us
		if (! staffList) {
			staffList = partyRelationshipService.getAvailableProjectStaffPersons( project, null, clientStaffOnly )
		}
	
		// log.debug "staffList looks like: \n$staffList"

		/*
		* Here are the steps that we'll go through to find the person
		*
		*    1. First step is to try and find the persons based on an exact match on first, middle and last names
		*    2. Match person to the staffList and save first match as the person. If multiple matches found then set ambiguous true and we're done
		*    3. If no person found or ambiguous is still false and any of the name values are blank, then perform a looser search. This may find additional matches 
		*       (e.g. "George W. Bush" would be found in the case where "George Bush" was passed). 
		*    4. If any persons are found compare to the staff list and previously found person accordingly.
		*
		* Note that the staffList could have multiple entries for staff based on their multiple roles
		*/

		// An inline closure that will validate if the person is in the staff
		def findPersonInStaff = { personList ->
			if (personList?.size()) {
				for (int i=0; i < personList?.size(); i++) {
					log.debug "findPerson() Looking at ${personList[i]} (${personList[i].id})"
					def staffRef = staffList.find { it.id == personList[i].id }
					if (staffRef) {
						log.debug "findPerson() Found person in staff list: ${results.person}, $staffRef"
						if (! results.person) {
							// Found our person!
							results.person = personList[i]
						} else if ( staffRef.id != results.person.id ) {
							// Shoot, we found a second person so we are done since we know that there is ambiguity
							results.isAmbiguous = true
							break
						}
					}
				}
			}
		}
	
		def lastName = lastNameWithSuffix(nameMap)
		def persons = Person.findAll("from Person as p where p.firstName=? and p.middleName=? and p.lastName=?", [ nameMap.first, nameMap.middle, lastName ] )
		findPersonInStaff(persons)

		if (! results.isAmbiguous && ( nameMap.middle == '' || lastNameWithSuffix(nameMap) == '' ) ) {
			// Only need to check 
			def query = "from Person as p where p.firstName=? " 
			def args = [nameMap.first]
			if ( nameMap.middle != '' ) {
				query += " and p.middleName=? "
				args << nameMap.middle
			} 		
			if ( lastName != '' ) {
				query +=" and p.lastName=? "
				args << lastName
			}

			findPersonInStaff( Person.findAll( query, args ) )
		}

		// If the lookup found a fullname by just a first name, then we want to set the isAmbiguous flag as a warning
		if (! results.isAmbiguous && lastName == '' && results.person?.lastName )
			results.isAmbiguous = true

		log.debug "findPerson() found $results"

		return results
	}

	/**
	 * Used to find a person object from their full name and if not found create it
	 * @param String the person's full name
	 * @param Project the project/client that the person is associated with
	 * @param List of staff Person objects (optional). If not passed then the the partyRelationshipService.getAllCompaniesStaffPersons() will be used
	 * @return Map containing person, status or null if unable to parse the name
	 */
	Map findOrCreatePerson(String name , Project project, staffList=null) {
		def nameMap = parseName(name)
		if (nameMap == null) {
			log.error "findOrCreatePersonByName() unable to parse name ($name)"
			return null
		}
		return findOrCreatePerson( nameMap, project, staffList)
	}

	/**
	 * This method is used to find a person object after importing and if not found create it
	 * Used to find a person object from their full name and if not found create it
	 * @param Map the person's full name in map
	 * @param Project the project/client that the person is associated with
	 * @param List of staff Person objects (optional). If not passed then the the partyRelationshipService.getAllCompaniesStaffPersons() will be used
	 * @return Map containing person, status or null if unable to parse the name
	 */
	Map findOrCreatePerson(Map nameMap , Project project, staffList=null) {
		
		if ( ! staffList )
			staffList = partyRelationshipService.getAllCompaniesStaffPersons()

		def results = findPerson(nameMap, project, staffList)
		results.isNew = null

		if ( ! results.person && nameMap.first ) {
			log.debug "findOrCreatePerson() creating new person and associate to Company as staff ($nameMap)"
			def person = new Person('firstName':nameMap.first, 'lastName':nameMap.last, 'middleName': nameMap.middle, staffType:'Salary')
			
			if ( ! person.save(insert:true, flush:true)) {
				log.error "findOrCreatePerson Unable to create Person"+GormUtil.allErrorsString( person )
				results.error = "Unable to create person $nameMap"
			} else {
				if (! partyRelationshipService.addCompanyStaff(project.client, person) ) {
					results.error = "Unable to assign person $results.person.toString() as staff"
					// TODO - JPM (10/13) do we really want to proceed if we can't assign the person as staff otherwise they'll be in limbo.
				} else {
					staffList.add(person)
				}
				results.person = person
				results.isNew = true
			}
		} else {
			results.isNew = false
		}

		return results
	}

	/**
	 * Parses a name into it's various components and returns them in a map
	 * @param String The full name of the person
	 * @return Map - the map of the parsed name that includes first, last, middle, suffix or null if it couldn't be parsed for some odd reason
	 */
	Map parseName(String name) {
		Map map = [first:'', last:'', middle:'', suffix:'']
		def firstLast = true
		def split

		if (! name) return null

		// Check for last, first OR first last, suffix
		if (name.contains(',')){
			split = name.split(',').collect { it.trim() }
			//println "a) split ($split) isa ${split.getClass()}"
			def size = split.size()

			if ( size == 2) { 
				// Check to see if it is a Suffix vs last, first
				def s = split[1]
				if (SUFFIXES.contains( s.toLowerCase() )) {
					// We got first last, suffix
					map.suffix = s
					// Split the rest to be mapped out below
					//println "b) splitting ${split[0]}"
					split = split[0].split("\\s+").collect { it.trim() }
					//println "b) split ($split) isa ${split.getClass()}"
	
				} else {
					firstLast = false
				}

			} else {
				log.error "parseName('$name') encountered multiple commas that is not handled"
				return null
			}

		} else {
			// Must be first [middle] last so parse and handle below
			split = name.split("\\s+").collect { it.trim() }
			//println "0) split ($split) isa ${split.getClass()}"
		}

		def size = split.size()

		if (firstLast) {

			// Deal with First [Middle Last Suffix]
			//println "1) split ($split) isa ${split.getClass()}"

			map.first = split[0]
			split = split.tail()
			size--
			//println "2) split ($split) isa ${split.getClass()}"

			// See if last field is a suffix
			if (size > 1 && SUFFIXES.contains( split[-1].toLowerCase() )) {
				size--
				map.suffix = split[size]
				split.pop()
				//println "3) split ($split) isa ${split.getClass()}"

			}

			// Check to see if we have a middle name or a compound name
			if (size >= 2) {
				//println "4) split ($split) isa ${split.getClass()}"
				def last = split.pop()
				if (COMPOUND_NAMES.contains( split[-1].toLowerCase() )) {
					last = split.pop() + ' ' + last
				}
				map.last = last
				map.middle = split.join(' ')
				size = 0
			} 

			if (size > 0) {
				// Join what ever is left as the last name
				map.last = split.join(' ')
			}

		} else {
			// Deal with Last Suff, First Middle

			// Parse the Last Name element
			def last = split[0].split("\\s+").collect { it.trim() }
			size = last.size()
			if (size > 1 && SUFFIXES.contains( last[-1].toLowerCase() )) {
				size--
				map.suffix = last[size]
				split.pop()
			}
			map.last = last.join(' ')

			// Parse the First Name element
			def first = split[1].split("\\s+").collect { it.trim() }
			map.first = first[0]
			first = first.tail()
			if (first.size() >= 1) {
				map.middle = first.join(' ')
			}
		}

		return map
	}
	
	/**
	 * 
	 * @param fromPerson
	 * @param toPerson
	 * @return
	 */
	def mergePerson(Person fromPerson, Person toPerson){
		def toUserLogin = UserLogin.findByPerson( toPerson )
		def fromUserLogin = UserLogin.findByPerson( fromPerson )
		
		def personDomain = new DefaultGrailsDomainClass( Person.class )
		def notToUpdate = ['beforeDelete','beforeInsert', 'beforeUpdate','id', 'firstName','blackOutDates']
		personDomain.properties.each{
			def prop = it.name
			if(it.isPersistent() && !toPerson."${prop}" && !notToUpdate.contains(prop)){
				toPerson."${prop}" = fromPerson."${prop}"
			}
		}
		
		if(!toPerson.save(flush:true)){
			toPerson.errors.allErrors.each{println it}
		}
		
		//Calling method to merge roles
		mergeUserLogin(toUserLogin, fromUserLogin, toPerson)
		//Updating person reference from 'fromPerson' to 'toPerson'
		updatePersonReference(fromPerson, toPerson)
		//Updating ProjectRelationship relation from 'fromPerson' to 'toPerson'
		updateProjectRelationship(fromPerson, toPerson)
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		fromPerson.delete()
		
		return fromPerson
	}

	/**
	 * This action is used to merge Person's UserLogin according to criteria
	 * 1. If neither account has a UserLogin - nothing to do
	 * 2. If Person being merged into the master has a UserLogin but master doesn't, assign the UserLogin to the master Person record.
	 * 3. If both Persons have a UserLogin,select the UserLogin that has the most recent login activity. If neither have login activity,
	 *	  choose the oldest login account.
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 * @param toPerson: instance of toPerson
	 * @return
	 */
	def mergeUserLogin(toUserLogin, fromUserLogin, toPerson){
		if(fromUserLogin && !toUserLogin){
			fromUserLogin.person = toPerson
			fromUserLogin.save(flush:true)
		} else if(fromUserLogin && toUserLogin){
			if(fromUserLogin.lastLogin && toUserLogin.lastLogin){
				if (fromUserLogin.lastLogin > toUserLogin.lastLogin){
					fromUserLogin.person = toPerson
					toUserLogin.delete()
				} else {
					fromUserLogin.delete()
				}
			} else{
				if(fromUserLogin.createdDate > toUserLogin.createdDate){
					fromUserLogin.person = toPerson
					toUserLogin.delete()
				} else{
					fromUserLogin.delete()
				}
			}
		}
		if(fromUserLogin && toUserLogin)
			updateUserLoginRefrence(fromUserLogin, toUserLogin);
	}
	
	/**
	 * This method is used to update Person reference from 'fromPerson' to  'toPerson'
	 * @param fromPerson : instance of fromPerson
	 * @param toPerson : instance of toPerson
	 * @return
	 */
	def updatePersonReference(fromPerson, toPerson){
		def domainRelatMap = ['application':['sme_id', 'sme2_id', 'shutdown_by', 'startup_by', 'testing_by'],
			'asset_comment':['resolved_by', 'created_by', 'assigned_to_id'], 'comment_note':['created_by_id'],
			'asset_dependency':['created_by','updated_by'], 'asset_entity':['app_owner_id'],
			'exception_dates':['person_id'], 'model':['created_by', 'updated_by', 'validated_by'],
			'model_sync':['created_by_id', 'updated_by_id', 'validated_by_id'], 'move_event_news':['archived_by', 'created_by'],
			'move_event_staff':['person_id'], 'workflow':['updated_by']]
		
		domainRelatMap.each{key, value->
			value.each{prop->
				jdbcTemplate.update("UPDATE ${key} SET ${prop} = '${toPerson.id}' where ${prop}= '${fromPerson.id}'")
			}
		}
	}
	
	
	
	/**
	 * This method is used to update userlogin reference from 'fromUserLogin' to  'toUserLogin'
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 * @return
	 */
	def updateUserLoginRefrence(fromUserLogin, toUserLogin){
		def domainRelatMap = ['asset_transition':['user_login_id'], 'data_transfer_batch':['user_login_id'],'model_sync':['created_by_id']]
		domainRelatMap.each{key, value->
			value.each{prop->
				jdbcTemplate.update("UPDATE ${key} SET ${prop} = ${toUserLogin.id} where ${prop}=${fromUserLogin.id}")
			}
		}
	}
	
	/**
	 * This method is used to update person reference in PartyRelationship table.
	 * @param toPerson : instance of Person
	 * @param fromPerson : instance of Person
	 * @return void
	 */
	def updateProjectRelationship(Party fromPerson, Party toPerson){
		try{
			//Written all sql as GORM blowing up this block of code.
			def allRelations = jdbcTemplate.queryForList("SELECT p.party_relationship_type_id AS prType, p.party_id_from_id AS pIdFrom, \
						p.party_id_to_id AS pIdTo, p.role_type_code_from_id AS rTypeCodeFrom, p.role_type_code_to_id AS rTypeCodeTo \
						FROM party_relationship p WHERE p.party_id_to_id = ${fromPerson.id}")

			allRelations.each{ relation->
				def res = jdbcTemplate.queryForList("SELECT 1 FROM party_relationship p WHERE \
							p.party_relationship_type_id='${relation.prType}' AND p.party_id_from_id =${relation.pIdFrom} \
							AND p.party_id_to_id =${toPerson.id} AND p.role_type_code_from_id='${relation.rTypeCodeFrom}'\
							AND p.role_type_code_to_id ='${relation.rTypeCodeTo}'")
				
				if(res){
					jdbcTemplate.update("DELETE FROM party_relationship   \
					   WHERE party_relationship_type_id = '${relation.prType}'\
					   AND role_type_code_from_id = '${relation.rTypeCodeFrom}' AND role_type_code_to_id='${relation.rTypeCodeTo}' \
					   AND party_id_to_id = ${fromPerson.id} AND party_id_from_id = ${relation.pIdFrom}")
				} else {
				   jdbcTemplate.update("UPDATE party_relationship SET party_id_to_id = ${toPerson.id} \
					   WHERE party_relationship_type_id = '${relation.prType}'\
					   AND role_type_code_from_id = '${relation.rTypeCodeFrom}' AND role_type_code_to_id='${relation.rTypeCodeTo}' \
					   AND party_id_to_id = ${fromPerson.id} AND party_id_from_id = ${relation.pIdFrom}")
				}
			}
		} catch(Exception ex){
			ex.printStackTrace()
		}
	}
}