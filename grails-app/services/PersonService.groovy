import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Application
import Person
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.validation.ValidationException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ProjectStatus

/**
 * The PersonService class provides a number of functions to help in the management and access of Person objects
 */
class PersonService {

	def jdbcTemplate
	def namedParameterJdbcTemplate
	def sessionFactory
	def partyRelationshipService
	def securityService
	def projectService
	
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

	static PERSON_DOMAIN_RELATIONSHIP_MAP = [
		'application':['sme_id', 'sme2_id', 'shutdown_by', 'startup_by', 'testing_by'],
		'asset_comment':['resolved_by', 'created_by', 'assigned_to_id'], 
		'comment_note':['created_by_id'],
		'asset_dependency':['created_by','updated_by'],
		'asset_entity':['app_owner_id'],
		'exception_dates':['person_id'], 
		'model':['created_by', 'updated_by', 'validated_by'],
		'model_sync':['created_by_id', 'updated_by_id', 'validated_by_id'],
		'model_sync_batch':['created_by_id'],
		'move_event_news':['archived_by', 'created_by'],
		'move_event_staff':['person_id'],
		'workflow':['updated_by'],
		'recipe_version':['created_by_id'],
		'task_batch':['created_by_id']
	]

	static PERSON_DELETE_EXCEPTIONS_MAP = [
		'application': [
			'sme_id': true,
			'sme2_id': true
		],
		'asset_entity':[
			'app_owner_id': true
		]
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
	 * Used to find a person by full name
	 * @param A string representing a person's name (e.g. John Doe; Doe, John; John T. Doe)
	 * @return Null if name unable to be parsed or a Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is 
	 * found. If more than one match is found then isAmbiguous will be set to true.
	 */
	Map findPersonByFullName(String name) {
		def map = parseName(name)
		if (map) {
			def people = Person.findAll("from Person as p where p.firstName=? and p.middleName=? and p.lastName=?", [ map.first, map.middle, map.last ] )
			if (people.size() == 1) {
				map.person = people.get(0)
			} else {
				map.isAmbiguous = true;
			}
		} else {
			map.isAmbiguous = true;
		}
		
		return map;
	}
		
	/**
	 * Used to find a person associated with a given project using a parsed name map
	 * @param nameMap - a Map containing person name elements
	 * @param project - the project object that the person is associated with
	 * @param staffList - deprecated argument that is no longer used
	 * @param clientStaffOnly - a flag used to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return A Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is found. If more than one match is 
	 * found then isAmbiguous will be set to true.
	 */
	Map findPerson(Map nameMap , Project project, List staffList=null, boolean clientStaffOnly=true) {
		String mn = 'findPerson()'
		def results = [person:null, isAmbiguous:false]
		
		log.debug "findPersion() attempting to find nameMap=$nameMap in project $project"

		// Make sure we have a person
		if (! nameMap || ! nameMap.containsKey('first')) {
			results.isAmbiguous = true
			return results
		}

		String hql = "from PartyRelationship PR inner join PR.partyIdTo P where PR.partyRelationshipType='STAFF' " +
			"and PR.roleTypeCodeFrom='COMPANY' and PR.roleTypeCodeTo='STAFF' and PR.partyIdFrom IN (:companies)"
		String where = " and P.firstName=:firstName and P.middleName=:middleName and P.lastName=:lastName"
		String lastName = lastNameWithSuffix(nameMap)
		List companies = [project.client]

		Map queryParams = [ 
			companies: companies,
			firstName: nameMap.first,
			middleName: nameMap.middle,
			lastName: lastName 
		]

		// Try finding the person with an exact match
		List persons = Person.findAll(hql+where, queryParams)
		if (persons) 
			persons = persons.collect( {it[1]} )
		log.debug "$mn Initial search found ${persons.size()} $nameMap"

		int s = persons.size()
		if (s > 1) {
			persons.each { person -> log.debug "person ${person.id} $person"}
			results.person = persons[0]
			results.isAmbiguous=true
		} else if (s == 1) {
			results.person = persons[0]
		} else {
			
			// Try to find match on partial

			// Closure to construct the where and queryParams used below
			def addQueryParam = { name, value -> 
				if (! StringUtil.isBlank(value) ) {
					where += " and P.$name=:$name"
					queryParams.put(name, value)
				}
			}

			where = ''
			queryParams = [companies:companies]
			addQueryParam('firstName', nameMap.first)
			addQueryParam('middleName', nameMap.middle)
			addQueryParam('lastName', lastName)

			log.debug "$mn partial search using $queryParams"
			persons = Person.findAll(hql+where, queryParams)
			if (persons) 
				persons = persons.collect( {it[1]} )
			log.debug "$mn partial search found ${persons.size()}"

			s = persons.size()
			if (s > 1) {
				results.isAmbiguous=true
			} else if (s == 1) {
				results.person = persons[0]
				results.isAmbiguous = (StringUtil.isBlank(lastName) && !StringUtil.isBlank(results.person.lastName) )
			}
		}

		log.debug "$mn results=$results"
		return results
	}

	/**
	 * Used to find a person object from their full name and if not found create it
	 * @param name - a String containing the person's full name to lookup
	 * @param project - the project object that the person is associated with
	 * @param staffList - deprecated argument that is no longer used
	 * @param clientStaffOnly - a flag used to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return A Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is found. If more than one match is 
	 * found then isAmbiguous will be set to true.
	 */
	Map findOrCreatePerson(String name , Project project, List staffList=null, boolean clientStaffOnly=true) {
		def nameMap = parseName(name)
		if (nameMap == null) {
			log.error "findOrCreatePersonByName() unable to parse name ($name)"
			return null
		}
		return findOrCreatePerson( nameMap, project, staffList, clientStaffOnly)
	}

	/**
	 * This method is used to find a person object after importing and if not found create it
	 * Used to find a person object from their full name and if not found create it
	 * @param nameMap - a Map the person's full name in map
	 * @param project - The Project that the person is associated with
	 * @param staffList - NO LONGER NEEDED
	 * @param clientStaffOnly - a flag to indicate if it should only look for person that is staff of the company or allow anyone assigned to a project (default true)
	 * @return Map containing person, status or null if unable to parse the name
	 */
	Map findOrCreatePerson(Map nameMap , Project project, List staffList=null, boolean clientStaffOnly=true) {
		Person person
		boolean staffListSupplied = (staffList != null)
		try {

			def results = findPerson(nameMap, project, staffList, clientStaffOnly)
			results.isNew = null

			if ( ! results.person && nameMap.first ) {
				log.info "findOrCreatePerson() Creating new person ($nameMap) as Staff for ${project.client}"
				person = new Person('firstName':nameMap.first, 'lastName':nameMap.last, 'middleName': nameMap.middle, staffType:'Salary')
				
				if ( ! person.validate() || ! person.save(insert:true, flush:true)) {
					log.error "findOrCreatePerson Unable to create Person"+GormUtil.allErrorsString( person )
					results.error = "Unable to create person ${nameMap}${GormUtil.allErrorsString( person )}"
				} else {
					if (! partyRelationshipService.addCompanyStaff(project.client, person) ) {
						results.error = "Unable to assign person $results.person.toString() as staff"
						// TODO - JPM (10/13) do we really want to proceed if we can't assign the person as staff otherwise they'll be in limbo.
					}
					results.person = person
					results.isNew = true
				}
			} else {
				results.isNew = false
			}

			return results
		} catch (e) {
			String exMsg = e.getMessage()
			log.error "findOrCreatePerson() received exception for nameMap=$nameMap : ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			if (person && !person.id) {
				person.discard()
				person = null
			}
			throw new RuntimeException("Unable to create person : $exMsg")
		}
	}

	/**
	 * Parses a name into it's various components and returns them in a map
	 * @param String The full name of the person
	 * @return Map - the map of the parsed name that includes first, last, middle, suffix or null if it couldn't be parsed for some odd reason
	 */
	Map parseName(String name) {
		name = StringUtils.strip(name)
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
		PERSON_DOMAIN_RELATIONSHIP_MAP.each{key, value ->
			value.each{ prop ->
				jdbcTemplate.update("UPDATE ${key} SET ${prop} = '${toPerson.id}' where ${prop}= '${fromPerson.id}'")
			}
		}
	}

	/**
	 * This method is used to update all UserLogin reference in all domains from on account to another
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 * @return
	 */
	def updateUserLoginRefrence(fromUserLogin, toUserLogin){
		def map = ['asset_transition':['user_login_id'], 'data_transfer_batch':['user_login_id'],'model_sync':['created_by_id']]
		map.each { table, columns->
			columns.each { column->
				jdbcTemplate.update("UPDATE ${table} SET ${column} = ${toUserLogin.id} where ${column}=${fromUserLogin.id}")
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
			// Find all of the relationships that the FROM person has
			def allRelations = jdbcTemplate.queryForList("SELECT p.party_relationship_type_id AS prType, p.party_id_from_id AS pIdFrom, \
				p.party_id_to_id AS pIdTo, p.role_type_code_from_id AS rTypeCodeFrom, p.role_type_code_to_id AS rTypeCodeTo \
				FROM party_relationship p WHERE p.party_id_to_id = ${fromPerson.id}")

			allRelations.each{ relation->
				// Check to see if the TO person has the particular relationship already. If so we delete the FROM person relationship otherwise
				def toAlreadyHasRelationship = jdbcTemplate.queryForList("SELECT 1 FROM party_relationship p WHERE \
					p.party_relationship_type_id='${relation.prType}' AND p.party_id_from_id =${relation.pIdFrom} \
					AND p.party_id_to_id =${toPerson.id} AND p.role_type_code_from_id='${relation.rTypeCodeFrom}'\
					AND p.role_type_code_to_id ='${relation.rTypeCodeTo}'")
				
				def where = " WHERE party_relationship_type_id = '${relation.prType}' \
					   AND role_type_code_from_id = '${relation.rTypeCodeFrom}' AND role_type_code_to_id='${relation.rTypeCodeTo}' \
					   AND party_id_to_id = ${fromPerson.id} AND party_id_from_id = ${relation.pIdFrom}"

				if (toAlreadyHasRelationship) {
					jdbcTemplate.update("DELETE FROM party_relationship $where")
				} else {
				   jdbcTemplate.update("UPDATE party_relationship SET party_id_to_id = ${toPerson.id} $where")
				}
			}
		} catch(Exception ex){
			log.error("Can't update person project relationship: " + ex.getMessage(), ex)
		}
	}

	/**
	 * Check if the user have a relationship with some entity, for example, Recipes
	 * @param person - Person to check
	 * @return boolean value that indicates if a relationship exist
	 */
	boolean haveRelationship(Person person) {
		def existRelationship = false
		def result

		PERSON_DOMAIN_RELATIONSHIP_MAP.each{ key, value ->
			value.each{ prop ->
				if (!checkRelationshipException(key, prop)) {
					result = jdbcTemplate.queryForList("SELECT count(*) AS count FROM ${key} WHERE ${prop} = '${person.id}'")
					if (result[0].count > 0) {
						log.info "Found relationship for '${person.firstName}, ${person.lastName}' on table: '${key}' over field: '${prop}'"
					}
					existRelationship = existRelationship || (result[0].count > 0)
				}
			}
		}

		return existRelationship
	}

	/**
	 * Check if the table and field should be checked in the haveRelationship funcion
	 * @param table to check
	 * @param field to check
	 * @return boolean tru
	 */
	boolean checkRelationshipException(table, field) {
		def result = false
		def fields = PERSON_DELETE_EXCEPTIONS_MAP[table]
		if (fields != null) {
			result = (fields[field] != null)
		}
		return result
	}

	/**
	 * Used to bulk delete Person objects as long as they do not have user accounts or assigned tasks and optionally associated with assets
	 * @param user - The user attempting to do the bulk delete
	 * @param ids - the list of person ids to delete
	 * @param deleteIfAssocWithAssets - a flag to indicate that it is okay to delete the person if they're associated to assets
	 * @return A map containing the following
	 *   deleted: number of persons deleted
	 *   skipped: number of persons skipped
	 *   cleared: number of assets references that were cleared/unassigned
	 */
	Map bulkDelete(UserLogin user, Object ids, Boolean deleteIfAssocWithAssets) {
		if (!securityService.hasPermission(user, 'BulkDeletePerson')) {
			log.warn "SECURITY : $user attempted to perform Bulk Delete of persons but doesn't have permission"
			throw new UnauthorizedException('User doesn\'t have a BulkDeletePerson permission')
		}

		if (! ids || ids.size()==0) {
			throw new InvalidParamException('Must select at least one person to delete')
		}

		log.info "$user is attempted to bulk delete ${ids?.size()} persons ($ids), deleteIfAssocWithAssets=$deleteIfAssocWithAssets"

		def deleted = 0
		def skipped = 0
		def cleared = 0
		def messages = []
		
		if (ids) {
			for (id in ids) {
				if (id.isLong()) {
					Person person = Person.get(id)
					if (person) {
						
						// Don't delete if they have a UserLogin
						def userLogin = UserLogin.findByPerson(person)
						if (userLogin) {
							messages << "Staff '${person.firstName}, ${person.lastName}', ignoring bulk delete because it is associated to a user login."
							log.debug("Ignoring bulk delete of ${id} as it contains userLogin")
							skipped++
							continue
						}

						// Don't delete if they have assigned tasks
						def tasks = AssetComment.findAllByAssignedTo(person)
						if (tasks) {
							messages << "Staff '${person.firstName}, ${person.lastName}', ignoring bulk delete because it contains tasks assigned."
							log.debug("Ignoring bulk delete of ${id} as it contains tasks assigned")
							skipped++
							continue
						}

						Map map = [person:person]

						if (haveRelationship(person)) {
							messages << "Staff '${person.firstName}, ${person.lastName}' unable to be deleted due to associations with existing elements in one or more Projects. Please use Person Merge functionality if applicable."
							skipped++
							continue
						}
						// Optionally don't delete if they are associated with Assets by AppOwner, SME or SME2
						def foundAssoc = false
						def result
						PERSON_DELETE_EXCEPTIONS_MAP.each { table, fields ->
							fields.each { column, status ->
								if (foundAssoc)
									return
								result = jdbcTemplate.queryForList("SELECT count(*) AS count FROM ${table} WHERE ${column} = '${person.id}'")
								if (result[0].count > 0) {
									if (deleteIfAssocWithAssets) {
										// Clear out the person's associate with all assets for the given column
										log.debug "Disassociated person as $column"
										cleared += jdbcTemplate.update("UPDATE ${table} SET ${column} = NULL WHERE ${column} = '${person.id}'")
									} else {
										log.debug("Ignoring bulk delete of person ${id} $person as it contains $column association with asset(s)")
										messages << "Staff '${person.firstName}, ${person.lastName}' unable to be deleted due it contains $column association with asset(s)."
										foundAssoc=true
										return
									}								
								}
							}
							if (foundAssoc)
								return
						}

						if (foundAssoc) {
							skipped++
							continue
						}
	
						//delete references
						log.info "Bulk deleting person $id $person"
						Person.executeUpdate("Delete PartyRole p where p.party=:person", map)
						Person.executeUpdate("Delete PartyRelationship p where p.partyIdFrom=:person or p.partyIdTo=:person", map)
						person.delete(flush:true)
						deleted++

					}
				}
			}
		}
		
		return [deleted: deleted, skipped: skipped, cleared: cleared, messages: messages]
	}

	String assignToProject(personId, eventId, roleType, val ){
		String message = ""
		// Check if the user has permission to edit the staff
		if ( RolePermissions.hasPermission("EditProjectStaff") ) {
			// Check if the person and events are not null
			if ( personId && eventId ) {
				// Check if the user is trying to edit a TDS employee without permission
				if ( ! ( partyRelationshipService.isTdsEmployee(personId) && ! RolePermissions.hasPermission("EditTDSPerson") ) ) {
					roleType = roleType ?: 'AUTO'
					def roleTypeInstance = RoleType.findById( roleType )
					def moveEvent = MoveEvent.get( eventId )
					def person = Person.get( personId )
					def project = moveEvent.project ?: securityService.getUserCurrentProject()
					if(hasAccessToProject(person, project)){
						def moveEventStaff = MoveEventStaff.findAllByStaffAndEventAndRole(person, moveEvent, roleTypeInstance)
						if(moveEventStaff && val == "0"){
							moveEventStaff.delete(flush:true)
						} else if( !moveEventStaff ) {
							def projectStaff = partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "PROJECT", person, roleType )
							moveEventStaff = new MoveEventStaff()
							moveEventStaff.person = person
							moveEventStaff.moveEvent = moveEvent
							moveEventStaff.role = RoleType.findById( roleType )
							if(!moveEventStaff.save(flush:true)){
								moveEventStaff.errors.allErrors.each{ println it}
								message = "An error occurred processing your request"
							}
						}	
					}else{
						message = "This person doesn't have access to the selected event."
					}
					
				} else {
					message = "You do not have permission to edit TDS employees"
				}
			} else {
				message = "The selected person and move event are not both valid"
			}
		} else {
			message = "You do not have permission to assign staff to events"
		}
		return message
	}

	Boolean hasAccessToProject(Person person, Project project){
		def projects =  projectService.getUserProjects(null, false, ProjectStatus.ANY, [personId:person.id])
		def found = false
		if(projects){
			projects.each{currentProject ->
				if(currentProject.id == project.id){
					found = true
					return
				}
			}	
		}
		
		return found
	}
}	