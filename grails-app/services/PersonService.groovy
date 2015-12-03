import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.Application
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.validation.ValidationException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.builder.UserAuditBuilder

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
	def userPreferenceService
	def auditService
	
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
		'task_batch':['created_by_id'],
		'application':['shutdown_by', 'startup_by', 'testing_by']
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

		log.debug "findByClientAndEmail: query $query, map $map"
		def persons
		def pIds = namedParameterJdbcTemplate.queryForList(query.toString(), map)
		log.debug "findByClientAndEmail: query $query, map $map, found ids $pIds"
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
		if (! clientStaffOnly) {
			companies << projectService.getOwner(project)
			companies.addAll( projectService.getPartners(project) )
		}

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
		def map = ['data_transfer_batch':['user_login_id'],'model_sync':['created_by_id']]
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
	Map bulkDelete(Object ids, Boolean deleteIfAssocWithAssets) {
		if (! ids || ids.size()==0) {
			throw new InvalidParamException('Must select at least one person to delete')
		}

		log.info "Attempted to bulk delete ${ids?.size()} persons ($ids), deleteIfAssocWithAssets=$deleteIfAssocWithAssets"

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
						if(moveEventStaff && val == 0){
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

	/**
	 * Used to validate that a user has the permissions to edit Staffing that the person/project are accessible as well. This will
	 * validate and lookup values for project:project, person:person, teamRoleType:teamRoleType. If there are any problems it will 
	 * throw the appropriate Exception.
	 * @param user- the UserLogin that is attempting to edit staffing
	 * @param projectId - the id of the project to assign/remove a person from
	 * @param personId - the id of the person being editted
	 * @param teamCode - the code of the team the person to be assigned/removed
	 * @return A map containing the looked up project, person, teamRoleType
	 */
	Map validateUserCanEditStaffing(UserLogin user, String projectId, String personId, String teamCode) {
		// Check if the user has permission to edit the staff
		if ( ! securityService.hasPermission(user, "EditProjectStaff") ) {
			securityService.reportViolation("attempted to alter staffing for person $personId on project $projectId without permission", user)
			throw new UnauthorizedException("You do not have permission to manage staffing for projects and events")
		}

		if (projectId && ! NumberUtil.isPositiveLong(projectId)) {
			throw new InvalidParamException("Invalid Project Id was specified")
		}
		def project = Project.get( projectId )
		if (! project) {
			log.warn  "validateUserCanEditStaffing() user $user called with invalid project id $projectId"
			throw new InvalidParamException("Invalid project specified")
		}

		// Check if the person and events are not null
		if ( !personId  || ! NumberUtil.isPositiveLong(personId)) {
			log.debug "validateUserCanEditStaffing() user $user called with missing or invalid params (personId:$personId, projectId:$projectId, teamCode:$teamCode)"
			throw new InvalidParamException("The person and event were not properly identified")
		}
		Person person = Person.get(personId)
		if (! person) {
			log.warn  "validateUserCanEditStaffing() user $user called with invalid person id $personId"
			throw new InvalidParamException("Invalid person specified")
		}

		RoleType teamRoleType
		if (teamCode) {
			teamRoleType = RoleType.get(teamCode) 
			if (! teamRoleType || ! teamRoleType.isTeamRole() ) {
				log.warn "assignToProject() user $user called with invalid team code $teamCode"
				throw new InvalidParamException("The specified team code was invalid")
			}
		}

		// Check to see if the user should have access to the project
		if (! getAvailableProjects(user.person, project)) {
			securityService.reportViolation("Attempt to assign peson '$person' to project '$project' that the user is not associated with", user)
			throw new UnauthorizedException("You do not have access to the project and therefore can not do the assignment")
		}

		// Check to see that the project for person to be assigned is one that is available
		if (! getAvailableProjects(person, project)) {
			securityService.reportViolation("Attempt to assign person '$person' to project '$project' that person is not associated with", user)
			throw new UnauthorizedException("The person you are trying to assign to the project is not associated with the project")
		}

		return [project:project, person:person, teamRoleType:teamRoleType]
	}

	/**
	 * Used to determine a person has the permission to access another person
	 * @param byWhom - the Person that wants to access another Person
	 * @param person - the Person to be accessed
	 * @param forEdit - flag that when set to true will validate the accessor has permission to edit
	 * @return true if byWhom has access to the person otherwise false
	 */
	boolean hasAccessToPerson(Person person, Person byWhom, boolean forEdit=false, boolean reportViolation=true) throws UnauthorizedException {

		boolean hasAccess = false
		List byWhomProjects = getAvailableProjects(byWhom)*.id
		List personProjects = getAvailableProjects(person)*.id

log.debug "hasAccessToPerson() byWhom projects: $byWhomProjects"
log.debug "hasAccessToPerson() person projects: $personProjects"

		if (forEdit && ! securityService.hasPermission(byWhom, 'EditUserLogin')) {
			if (reportViolation) {
				reportViolation("attempted to edit person $person (${person.id})", byWhom)
			}
			throw new UnauthorizedException('Do not have required permission to edit user')
		}

		if (byWhomProjects && personProjects) {
			for(int i=0; i < personProjects.size(); i++) {
				if (byWhomProjects.contains(personProjects[i])) {
					hasAccess = true
					break
				}
			}
		}

		if (! hasAccess) {
			if (reportViolation) {
				securityService.reportViolation("attempted to access person $person (${person.id}) without proper access", byWhom)
			}
			throw new UnauthorizedException('Do not have access to specified user')
		}
	}

	/** 
	 * Used to associate a person to a project as staff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void addToProjectTeam(UserLogin user, String projectId, String personId, String teamCode, Map map=null) {
		// The addToEvent may call this method as well
		if (! map) {
			map = validateUserCanEditStaffing(user, projectId, personId, teamCode)
		}
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		// Add to the project if not assiged already
		if (! isAssignedToProject(map.project, map.person)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, 'STAFF')) {
				auditService.logMessage("$user assigned ${map.person} to project ${map.project.name} as STAFF")
			} else {
				throw new DomainUpdateException("An error occurred while trying to assign the person to the event")
			}			
		}

		if (! isAssignedToProjectTeam(map.project, map.person, map.teamRoleType)) {
			log.debug "addToProjectTeam() map=$map" 
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, map.teamRoleType)) {
				auditService.logMessage("$user assigned ${map.person} to project '${map.project.name}' on team $teamCode")
			} else {
				throw new DomainUpdateException("An error occurred while trying to assign the person to the event")
			}			
		} else {
			log.warn "addToProjectTeam() called for project ${map.project}, person ${map.person}, team ${map.teamRoleType} but already exists"
		}
	}

	/** 
	 * Used to associate a person to a project as staff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void addToProject(UserLogin user, String projectId, String personId) {
		// The addToEvent may call this method as well
		def	map = validateUserCanEditStaffing(user, projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		// Add to the project if not assiged already
		if (! isAssignedToProject(map.project, map.person)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, 'STAFF')) {
				auditService.logMessage("$user assigned ${map.person} to project ${map.project.name} as STAFF")
			} else {
				throw new DomainUpdateException("An error occurred while trying to assign the person to the event")
			}			
		}
	}

	/**
	 * Used by a few methods to delete the Project Staffing assignments to an event
	 * @param project - the Project that the events belong to
	 * @param event - if supplied will delete just a particular event (optional)
	 * @param person - the person to remove
	 * @param team - if supplied will delete just the team (optional)
	 * @return count of how many were deleted
	 */
	private int deleteFromEvent(Project project, MoveEvent event=null, Person person, RoleType team=null) {
		int c = 0

		assert project != null
		assert person != null

		List mes = MoveEventStaff.withCriteria {
			and {
				moveEvent {
					eq('project', project)
				}
				eq('person', person)
				if (event) {
					eq('moveEvent', event)
				}
				if (team) {
					eq('role', team)
				}
			}
		}
		mes?.each { 
			it.delete() 
			c++
		}
		return c
	}

	/**
	 * A helper method used to lookup and validate that an event was properly referenced
	 * @param project - the project that the event should belong to
	 * @param eventId - the event id number to lookup
	 * @return the event object
	 */
	 // TODO : JPM 11/2015 : Refactor lookupEvent into EventService
	private MoveEvent lookupEvent(Project project, String eventId) {
		if (! NumberUtil.isPositiveLong(eventId)) {
			throw new InvalidParamException('The event id number was invalid')
		}
		MoveEvent event = MoveEvent.get(eventId)
		if (!event) {
			throw new InvalidParamException('The event was not found')
		}
		if (event.project.id != project.id) {
			securityService.reportViolation("attempted to access event ($eventId) that doen't match project ($project.id)")
			throw new InvalidParamException('The event was not found')
		}
		return event
	}

	/** 
	 * Used to remove a person from a project as staff and also clear out references to MoveEventStaff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 * TODO : JPM : 9/23/2015 - removeFromProject() doesn't look like it will work and doesn't appear to be used...
	 */
	String removeFromProjectTeam(UserLogin user, String projectId, String personId, String teamCode) {
		Map map = validateUserCanEditStaffing(user, projectId, personId, teamCode)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		// Remove all of the person's MoveEventStaff relationships for the project
		deleteFromEvent(map.project, null, map.person, map.teamRoleType)

		// Remove the Project Staff Team relationship for the project
		partyRelationshipService.deletePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, map.teamRoleType.id)

		// Remove from project staff if don't have more teams
		List roles = partyRelationshipService.getProjectStaffFunctions(map.project.id, map.person.id)
		if (roles.size() == 1 && roles[0].id.equals("STAFF")) {
			partyRelationshipService.deletePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, "STAFF")
		}

		auditService.logMessage("$user unassigned ${map.person} from project ${map.project.name}")
	}

	/** 
	 * Used to remove a person from a project as staff and also clear out references to MoveEventStaff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 * TODO : JPM : 9/23/2015 - removeFromProject() doesn't look like it will work and doesn't appear to be used...
	 */
	String removeFromProject(UserLogin user, String projectId, String personId) {
		Map map = validateUserCanEditStaffing(user, projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		// Remove all of the person's MoveEventStaff relationships for the project
		deleteFromEvent(map.project, null, map.person, map.teamRoleType)


		// Remove the Project Staff relationship for the project
		List roles = partyRelationshipService.getProjectStaffFunctions(map.project.id, map.person.id)
		roles?.each {
			if (it.type.equals(RoleType.TEAM)) {
				partyRelationshipService.deletePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, it.id)
			}
		}

		// Remove the person from the project
		PartyRelationship prProjectStaff = getProjectReference(map.project, map.person) 
		if (prProjectStaff) {
			log.debug "removeFromProject() deleting PartyRelationship $prProjectStaff"
			prProjectStaff.delete()
		} else {
			log.warn "removeFromProject() No Project Staff record found for project $projectId and person $personId"
		} 

		auditService.logMessage("$user unassigned ${map.person} from project ${map.project.name}")
	}

	/** 
	 * Used to associate a person to a project as staff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void addToEvent(UserLogin user, String projectId, String eventId, String personId, String teamCode) {
		Map map = validateUserCanEditStaffing(user, projectId, personId, teamCode)

		// Add the Staff to the Project if not already assigned
		if (! isAssignedToProjectTeam(map.project, map.person, map.teamRoleType)) {
			addToProjectTeam(user, projectId, personId, teamCode, map)
		}

		MoveEvent event = lookupEvent(map.project, eventId)

		// Add the person to the Event if not already assigned
		if (! isAssignedToEventTeam(event, map.person, map.teamRoleType)) {
			MoveEventStaff mes = new MoveEventStaff([person: map.person, moveEvent: event, role: map.teamRoleType])
			if (mes.validate() && mes.save(flush:true)) {
				auditService.logMessage("$user assigned ${map.person} to project '${map.project.name}' event '${event.name}' as $teamCode")
			} else {
				log.error "addToEvent() Unable to save MoveEventStaff record for person $personId, project $projectId, event $eventId, team $teamCode : " +
					GormUtil.allErrorsString(mes)
				throw new RuntimeException("Unable to save MoveEventStaff record : " + GormUtil.allErrorsString(mes))
			}
		} else {
			log.warn "addToEvent() called for project ${map.project}, person ${map.person}, team ${map.teamRoleType} but already exists"
		}
	}

	/** 
	 * Used to remove a person from a project as staff and also clear out references to MoveEventStaff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void removeFromEvent(UserLogin user, String projectId, String eventId, String personId, String teamCode) {
		Map map = validateUserCanEditStaffing(user, projectId, personId, teamCode)

		MoveEvent event = lookupEvent(map.project, eventId)

		deleteFromEvent(map.project, event, map.person, map.teamRoleType)

		auditService.logMessage("$user unassigned ${map.person} from project '${map.project.name}' team $teamCode")
	}

	/**
	 * Used to determine if a person is assigned to a project as a STAFF member
	 * @param person - the person whom to check 
	 * @param project - the project to check if user is assigned to
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToProject(Project project, Person person) {
		return (getProjectTeamReference(project, person, 'STAFF') != null)
	}

	/**
	 * Used to determine if a person is assigned to a project as a STAFF member
	 * @param project - the project to check if user is assigned to
	 * @param person - the person whom to check 
	 * @param teamCode - the team code
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToProjectTeam(Project project, Person person, teamCode) {
		return (getProjectTeamReference(project, person, teamCode) != null)
	}

	/**
	 * Used retrieve the 'STAFF' PartyRelationship reference for a person to a particular project
	 * @param project - the project to check if user is assigned to
	 * @param person - the person whom to check 
	 * @param teamCode - the team code
	 * @return The PartyRelationshipReference that represents the person's relationship to a project
	 */
	PartyRelationship getProjectReference(Project project, Person person) {
		return getProjectTeamReference(project, person, 'STAFF')
	}

	/**
	 * Used retrieve the Team PartyRelationship reference for a person to a particular project
	 * @param project - the project to check if user is assigned to
	 * @param person - the person whom to check 
	 * @param teamCode - the team code
	 * @return The company whom the person is employeed
	 */
	PartyRelationship getProjectTeamReference(Project project, Person person, teamCode) {
		assert person != null
		assert project != null

		PartyRelationshipType prtProjectStaff = PartyRelationshipType.get('PROJ_STAFF')
		assert prtProjectStaff != null

		RoleType rtProject = RoleType.get('PROJECT')
		assert rtProject != null

		// Lookup the RoleType(teamCode) if not already a RoleTeam
		if (! (teamCode instanceof RoleType)) {
			assert (teamCode instanceof String)
			teamCode = RoleType.get(teamCode)
			assert teamCode != null
		}

		def teamRef = PartyRelationship.createCriteria().get {
			and {
				eq('partyRelationshipType', prtProjectStaff)
				eq('roleTypeCodeFrom', rtProject)
				eq('roleTypeCodeTo', teamCode)
				eq('partyIdFrom', project)
				eq('partyIdTo', person)
			}
		}

		return teamRef
	}

	/**
	 * Used to determine if a person is assigned to an Event for a particular team role 
	 * @param person - the person whom to check 
	 * @param event - the event to check if user is assigned to
	 * @param teamCode - the team code
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToEventTeam(MoveEvent event, Person person, teamCode) {
		return getEventTeamReference(event, person, teamCode) != null
	}

	/**
	 * Used to retrieve an MoveEventStaff reference for a person assigned to an event with a give team code
	 * @param person - the person whom to check 
	 * @param event - the event to check if user is assigned to
	 * @param teamCode - the team code
	 * @return The MoveEventStaff record if found otherwise null
	 */
	MoveEventStaff getEventTeamReference(MoveEvent event, Person person, teamCode) {
		assert person != null
		assert event != null

		// Lookup the RoleType(teamCode) if not already a RoleTeam
		if (! (teamCode instanceof RoleType)) {
			assert (teamCode instanceof String)
			teamCode = RoleType.get(teamCode)
			assert teamCode != null
		}

		return MoveEventStaff.createCriteria().get {
			and {
				eq('moveEvent', event)
				eq('person', person)
				eq('role', teamCode)
			}
		}
	}

	/**
	 * Used to get a list of projects that the given person is assigned to
	 * @param person - the person whom to lookup assigned projects
	 * @param project - if supplied will filter the results to just the one project (optional)
	 * @return The list of projects
	 */
	List<Project> getAssignedProjects(Person person, Project project=null) {
		assert person != null

		def c = PartyRelationship.createCriteria()
		List projectList = c.list() {
			eq('partyRelationshipType', PartyRelationshipType.get('PROJ_STAFF'))
			and {
				eq('roleTypeCodeFrom', RoleType.get('PROJECT'))
				eq('partyIdTo', person)
				if (project) {
					eq('partyIdFrom', project)
				}
			}
			projections {
				groupProperty('partyIdFrom')
			}
		}
		return projectList
	}

	/**
	 * Used to get a list of projects that the given person could be associated with
	 * @param person - the person whom to lookup the projects for
	 * @param project - if supplied will filter the results to just the one project (optional)
	 * @return The list of projects
	 */
	List<Project> getAvailableProjects(Person person, Project project=null, boolean excludeAssigned=false, Date cutoff=null) {
		assert person != null

		PartyGroup employer = person.company
		List projects = partyRelationshipService.companyProjects(employer, project)

		//log.debug "getAvailableProjects() person $person ($person.id), employer $employer($employer.id), # projects ${projects?.size()}"
		//log.debug "getAvailableProjects() list 1: ${projects*.id}"

		// Optionally remove the assigned projects
		if (excludeAssigned) {
			List assignedProjects = getAssignedProjects(person, project)
			if (assignedProjects) {
				projects = projects - assignedProjects
			}
		}
		// log.debug "getAvailableProjects() list 2: ${projects*.id}"
		// Optionally remove the projects by completion date cutoff
		if (cutoff)
			projects = projects.findAll { it.completionDate >= cutoff }

		// Sort the list descending by completion date
		// projects.sort{ a, b -> a.completionDate < b.completionDate }

		// Filter down to just the one project if the request was for just the one
		if (project && projects) {
			def theProject = projects.find { it.id == project.id }
			projects = (theProject ? [theProject] : [])
			// log.debug "getAvailableProjects() list 3: ${projects*.id}"
		}

		return projects
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

	/**
	 * Used to validate that the user can access a person and will respond with appropriate 
	 * HTTP responses based on access constraints (e.g. Unauthorized or Not Found)
	 * @param personId - the id of the person to access
	 * @param byWhom - the Person that is attempting to access the Person
	 * @return Person - the person if can access or null
	 */
	Person validatePersonAccess(personId, Person byWhom) 
		throws UnauthorizedException, InvalidParamException, EmptyResultException {

		if (!byWhom) throw new UnauthorizedException('Must specify whom is accessing person')
		
		if (! NumberUtil.isPositiveLong(personId))  throw new InvalidParamException('Invalid person id requested')	

		// If not edit own account, the user must have privilege to edit the account
		boolean editSelf = ( NumberUtil.toLong(personId) == NumberUtil.toLong(byWhom.id) )
		if ( ! editSelf && ! securityService.hasPermission(byWhom, 'PersonEditView')) {
			securityService.reportViolation("$byWhom attempted to edit Person($personId) without necessary permission")
			throw new UnauthorizedException('Missing require permission to edit person')
		}

		if (! editSelf) {
			// TODO : JPM 5/2015 : Need to make sure showing/editing someone that the user has access to
		}

		Person person = Person.findById(personId)
		if (! person) {
			throw new EmptyResultException()
		}

		return person
	}

	/**
	 * Used by controller update an actual Person and possibly UserLogin. The logic works for cases where user updating their own 
	 * account as well as an administrator updating others. In the case of the latter, there are more things that can be updated.
	 * @param params - the form params that were passed 
	 * @param byWhom - The person that is performing the update
	 * @param byAdmin - Flag indicating that it is being done by the admin form (default false)
	 * @return The Person record being updated or throws an exception for various issues
	 */
	Person updatePerson(Map params, Person byWhom, String tzId, boolean byAdmin=false) 
		throws DomainUpdateException, UnauthorizedException, InvalidParamException, EmptyResultException {
		Person person = validatePersonAccess(params.id, byWhom)
		def session = userPreferenceService.getSession()
		if(!isAssociatedTo(byWhom, person.company)){
			throw new UnauthorizedException("You do not have permission to manage staffing for the user's company")
		}
	
		def ret = []
		params.travelOK == "1" ? params : (params.travelOK = 0)
		
		if (!person.staffType && !params.staffType) params.staffType = 'Hourly'
		
		// TODO : JPM 8/31/2015 : Replace person.properties = params with proper field assignments
		person.properties = params

		if ( ! person.save(flush:true) ) {
			log.error "updatePerson() unable to save $person : " + GormUtil.allErrorsString(person)
			throw new DomainUpdateException('An error occurred while attempting to save person changes')
		}			
	
		UserLogin userLogin = securityService.getPersonUserLogin( person )
		if (userLogin) {
			if (params.newPassword) {
				securityService.validateAllowedToChangePassword(userLogin, byAdmin)

				if (! byAdmin) {
					// Verify that the user entered their old password correctly
					if (!params.oldPassword) {
						throw new InvalidParamException('The old password is required')
					}

					if (! userLogin.comparePassword(params.oldPassword, true)) {
						throw new InvalidParamException('Old password entered does not match the existing password')
					}

					// Verify that the password isn't being changed to often
					if (! securityService.verifyMinPeriodToChangePswd(userLogin) ) {
						throw new DomainUpdateException('Minimum period for changing your password has not been met')
					}
				}
				securityService.setUserLoginPassword(userLogin, params.newPassword)
			}

			if (byAdmin && params.expiryDate && params.expiryDate != "null") {
				def expiryDate = params.expiryDate
				userLogin.expiryDate = TimeUtil.parseDate(session, expiryDate)
			}

			// When Disabling Person - disable UserLogin
			// When enabling Person - do NOT change UserLogin
			if (person.active == 'N') {
				userLogin.active = 'N'
			}

			if (!userLogin.save()) {
				log.error "updatePerson() failed for $userLogin : " + GormUtil.allErrorsString(userLogin)
				throw new DomainUpdateException('An error occurred while attempting to update the user changes')
			} else if (params.newPassword) {
				auditService.saveUserAudit(UserAuditBuilder.userLoginPasswordChanged(userLogin))
			}
		}

		// Additional changes allowed by adminstrator of a person
		if (byAdmin) {
			def functions = params.list("function")
			if (params.manageFuncs != '0' || functions){
				def staffCompany = partyRelationshipService.getStaffCompany(person)
				def companyProject = Project.findByClient(staffCompany)
				partyRelationshipService.updateStaffFunctions(staffCompany, person, functions, 'STAFF')
				if (companyProject) {
					partyRelationshipService.updateStaffFunctions(companyProject, person,functions, "PROJ_STAFF")
				}
			}

			// TODO : JPM 8/31/2015 : Overhaul how exception dates are handled - shouldn't delete all then re-add
			def personExpDates = params.list("availability")
			personExpDates = personExpDates.collect{ TimeUtil.parseDate(session, it) }
			def existingExp = ExceptionDates.findAllByPerson(person)
			if (personExpDates) {
				ExceptionDates.executeUpdate("delete from ExceptionDates where person = :person and exceptionDay not in (:dates) ",[person:person, dates:personExpDates])
				personExpDates.each { presentExpDate->
					def exp = ExceptionDates.findByExceptionDayAndPerson(presentExpDate, person)
					if (!exp){
						def expDates = new ExceptionDates()
						expDates.exceptionDay = presentExpDate
						expDates.person = person
						if (! expDates.save(flush:true) ) {
							log.error "updatePerson() unable to save $person exception dates : " + GormUtil.allErrorsString(person)
							throw new DomainUpdateException('An error occurred while attempting to save exception dates')
						}
					}
				}
			} else {
				ExceptionDates.executeUpdate("delete from ExceptionDates where person = :person",[person:person])
			}
		}

		if (! byAdmin) {
			// Save some preferences
			if (params.timeZone) {
				userPreferenceService.setPreference("CURR_TZ", params.timeZone)
				userPreferenceService.loadPreferences("CURR_TZ")
			}

			if (params.powerType) {
				userPreferenceService.setPreference("CURR_POWER_TYPE", params.powerType)
			}

			if (params.startPage) {
				userPreferenceService.setPreference("START_PAGE", params.startPage)
				userPreferenceService.loadPreferences("START_PAGE")
			}
		}

		return person

	}

	/**
	 * Used by controller to create a Person. 
	 * @param params - the form params that were passed 
	 * @param byWhom - The person that is performing the update
	 * @param companyId - The person company
	 * @param byAdmin - Flag indicating that it is being done by the admin form (default false)
	 * @return The Person record being created or throws an exception for various issues
	 */
	Person savePerson(Map params, Person byWhom, Long companyId, boolean byAdmin=false) 
		throws DomainUpdateException, InvalidParamException {

		def companyParty
		def person

		// Look to allow easy breakout for exceptions
		while(true) {
			if (companyId != null) {
				companyParty = Party.findById( companyId )
			}

			if (!companyParty) {
				throw new InvalidParamException('Unable to locate proper company to associate person to')
			}

			// Get list of all staff for the company and then try to find the individual so that we don't duplicate
			// the creation.
			def personList = partyRelationshipService.getCompanyStaff(companyId)
			person = personList.find {
				// Find person using case-insensitive search
				StringUtils.equalsIgnoreCase(it.firstName, params.firstName) &&
				( ( StringUtils.isEmpty(params.lastName) && StringUtils.isEmpty(it.lastName) ) ||  StringUtils.equalsIgnoreCase(it.lastName, params.lastName) ) &&
				( ( StringUtils.isEmpty(params.middleName) && StringUtils.isEmpty(it.middleName) ) ||  StringUtils.equalsIgnoreCase(it.middleName, params.middleName) )
			}

			if (person != null) {
				throw new DomainUpdateException('A person with that name already exists')
			} else {
				// Create the person and relationship appropriately
				def reducedParams = new HashMap(params)
				reducedParams.remove("company")
				person = new Person( reducedParams )
				if ( person.validate() && person.save() ) {
					//Receiving added functions		
					def functions = params.list("function")
					def partyRelationship = partyRelationshipService.savePartyRelationship( "STAFF", companyParty, "COMPANY", person, "STAFF" )
					if (functions) {
						userPreferenceService.setUserRoles(functions, person.id)
						def staffCompany = partyRelationshipService.getStaffCompany(person)
						//Adding requested functions to Person .
						partyRelationshipService.updateStaffFunctions(staffCompany, person, functions, 'STAFF')
					}
				} else {
					log.error "savePerson() failed for $person : " + GormUtil.allErrorsString(person)
					throw new DomainUpdateException("Unable to create person. $person${GormUtil.allErrorsString( person )}.")
				}
			}
			break
		}

		return person
	}

	def getPersonTeams() {

	}

	/**
	 * Determines if a person is associated to a company.
	 * @param Person trying to access the company.
	 * @param Company the person is trying to access.
	 */
	boolean isAssociatedTo(Person whom, Party company){
		List<Party> companies = partyRelationshipService.associatedCompanies(whom)
		def c = companies.find{it.id == company.id }
		return c != null
	}

}
