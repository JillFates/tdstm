import UserPreferenceEnum as PREF
import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.util.WebUtils
import org.hibernate.SessionFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * The PersonService class provides a number of functions to help in the management and access of Person objects
 */
class PersonService {

	AuditService auditService
	GrailsApplication grailsApplication
	JdbcTemplate jdbcTemplate
	MoveEventService moveEventService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	SecurityService securityService
	SessionFactory sessionFactory
	UserPreferenceService userPreferenceService

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
	 * Used to find a person by their name that is staff of the specified company
	 * @param company - The company that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */
	// List findByClientAndName(PartyGroup client, Map nameMap) {
	List<Person> findByClientAndName(PartyGroup company, Map nameMap) {
		Map queryParams = [company:company.id]
		def (first,middle,last) = [false,false,false]

		StringBuffer query = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr')
		query.append(' JOIN person p ON p.person_id=pr.party_id_to_id')
		query.append(' WHERE pr.party_id_from_id=:company')
		query.append(' AND pr.role_type_code_from_id="COMPANY"')
		query.append(' AND pr.role_type_code_to_id="STAFF"')
		// query.append(' ')
		if (nameMap.first) {
			queryParams.first = nameMap.first
			query.append(' AND p.first_name=:first' )
		}
		if (nameMap.last) {
			queryParams.last = nameMap.last
			query.append(' AND p.last_name=:last' )
		}

		def persons
		def pIds = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)

		if (nameMap.middle) {
			// Try to lookup the person with their middle name as well
			queryParams.middle = nameMap.middle
			query.append(' AND p.middle_name=:middle' )
			pIds.addAll( namedParameterJdbcTemplate.queryForList(query.toString(), queryParams) )
		}

		if (pIds) {
			persons = Person.findAll('from Person p where p.id in (:ids)', [ids:pIds*.id])
		}

		return persons
	}

	/**
	 * Used to find a person by their name that is staff of the specified company
	 * @param company - The company that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */
	// TODO : JPM 4/2016 : findByCompanyAndName is replacing findByClientAndName
	List<Person> findByCompanyAndName(PartyGroup company, Map nameMap) {
		List persons = []
		Map queryParams = [company:company.id]
		def (first,middle,last) = [false,false,false]

		StringBuffer select = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr')
			select.append(' JOIN person p ON p.person_id=pr.party_id_to_id')
			select.append(' WHERE pr.party_id_from_id=:company')
			select.append(' AND pr.role_type_code_from_id="COMPANY"')
			select.append(' AND pr.role_type_code_to_id="STAFF" ')

		StringBuffer query = new StringBuffer( select )
		if (nameMap.first) {
			queryParams.first = nameMap.first
			query.append(' AND p.first_name=:first' )
			first = true
		}
		if (nameMap.middle) {
			queryParams.middle = nameMap.middle
			query.append(' AND p.middle_name=:middle' )
			middle = true
		}
		if (nameMap.last) {
			queryParams.last = nameMap.last
			query.append(' AND p.last_name=:last' )
			last = true
		}
		if (first && middle && last) {
			// Union to try and find individuals with just first and last, middle not set
			query.append(' UNION ')
			query.append(select)
			query.append(" AND p.first_name=:first AND p.last_name=:last AND COALESCE(p.middle_name,'') = '' ")
		}
		if (first) {
			// Union to try and find individuals with just first, middle and last not set
			query.append(' UNION ')
			query.append(select)
			query.append(" AND p.first_name=:first AND COALESCE(p.last_name,'') = '' AND COALESCE(p.middle_name,'') = '' ")
		}
		List pIds = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
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
				map.isAmbiguous = true
			}
		} else {
			map.isAmbiguous = true
		}

		return map
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
		List companies = [project.client]

		String where = " and P.firstName=:firstName"
		String lastName = lastNameWithSuffix(nameMap)
		Map queryParams = [
			companies: companies,
			firstName: nameMap.first
		]

		if(lastName){
			where += " AND P.lastName=:lastName"
			queryParams["lastName"] = lastName
		}
		if(nameMap.middle){
			where += " AND P.middleName=:middleName"
			queryParams["middleName"] = nameMap.middle
		}

		if (! clientStaffOnly) {
			companies << projectService.getOwner(project)
			companies.addAll( projectService.getPartners(project) )
		}

		// Try finding the person with an exact match
		List persons = Person.findAll(hql+where, queryParams)
		if (persons)
			persons = persons.collect( {it[1]} )
		log.debug "$mn Initial search found ${persons.size()} $nameMap"

		int s = persons.size()
		if (s > 1) {
			persons.each { person -> log.debug "person ${person.id} $person"}
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
	@Transactional
	def mergePerson(Person fromPerson, Person toPerson) {
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

		sessionFactory.getCurrentSession().flush()
		sessionFactory.getCurrentSession().clear()
		fromPerson.delete()

		return fromPerson
	}

	/**
	 * This method deletes a person and other entities
	 * related to the instance, such as party and party role.
	 *
	 * @param person - Person Instance to be deleted
	 * @param deleteIfUserLogin - boolean that indicates if a person with existing UserLogin must be deleted.
	 * @param deleteIfAssocWithAssets - boolean that indicates if a person with relationships with assets must
	 *									 be deleted (see PERSON_DELETE_EXCEPTIONS_MAP)
	 * @return Map[
	 *				String[]	-> messages: errors and other messages.
	 *				int 		-> cleared: assets cleared
	 *				Boolean 	-> deleted: the person was deleted.
	 *			]
	 */
	@Transactional
	Map deletePerson(Person person, boolean deleteIfUserLogin, boolean deleteIfAssocWithAssets) {
		int cleared = 0
		boolean deleted = false
		def messages = []
		def userLogin = UserLogin.find("from UserLogin ul where ul.person = ${person.id}")

		boolean isDeletable = !person.isSystemUser() && !haveRelationship(person) && !(!deleteIfUserLogin && userLogin)
		if(isDeletable){

			Person.withTransaction { trxStatus ->

				try{

					// Sets additional person references to NULL
					PERSON_DELETE_EXCEPTIONS_MAP.each { table, fields ->
						fields.each { column, status ->
							if(isDeletable){
								def rels = jdbcTemplate.queryForList("SELECT count(*) AS count FROM ${table} WHERE ${column} = '${person.id}'")
								if(rels[0].count > 0){
									if(deleteIfAssocWithAssets){
										// Clear out the person's associate with all assets for the given column
										log.debug "Disassociated person as $column"
										cleared += jdbcTemplate.update("UPDATE ${table} SET ${column} = NULL WHERE ${column} = '${person.id}'")
									}else{
										log.debug("Ignoring delete of person ${person.id} as it contains $column association with asset(s)")
										messages << "Staff '${person.firstName}, ${person.lastName}' unable to be deleted due it contains $column association with asset(s)."
										isDeletable = false
									}
								}
							}
						}
					}

					if(isDeletable){
						Map map = [person: person]
						// Deletes Party Roles
						Person.executeUpdate("Delete PartyRole p where p.party=:person", map)
						// Deletes Party Relationships
						Person.executeUpdate("Delete PartyRelationship p where p.partyIdFrom=:person or p.partyIdTo=:person", map)
						def partyInstance = Party.get(person.id)
						// Deletes Party
						partyInstance.delete()

						if(deleteIfUserLogin){
							if (userLogin) {
								def preferenceInst = UserPreference.findAll("from UserPreference up where up.userLogin = ${userLogin.id}")
								preferenceInst.each{
								  it.delete()
								}
								userLogin.delete()
							}
						}
						person.delete()
						deleted = true

					}

				}catch(Exception e){
					status.setRollbackOnly()
					messages << "There was an error trying to delete staff '${person.firstName}, ${person.lastName}'"
					log.debug("An error occurred while trying to delete ${person.id}: " + e)
				}
			}
		}else{
			String msg
			if(person.isSystemUser()){
				msg = "Staff '${person.firstName}, ${person.lastName}' is a USER required by the SYSTEM and can't be deleted"
			}else if(haveRelationship(person)){
				msg = "Staff '${person.firstName}, ${person.lastName}' unable to be deleted due to associations with existing elements in one or more Projects. Please use Person Merge functionality if applicable."
			}else (!deleteIfUserLogin && userLogin){
				msg = "Staff '${person.firstName}, ${person.lastName}' as a user associated and you're not allowed to delete it"
			}

			messages << msg
		}

		return [messages: messages, cleared: cleared, deleted: deleted]
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
	@Transactional
	def mergeUserLogin(toUserLogin, fromUserLogin, toPerson) {
		if(fromUserLogin && !toUserLogin){
			fromUserLogin.person = toPerson
			fromUserLogin.save(flush:true)
		} else if(fromUserLogin && toUserLogin){
			if(fromUserLogin.lastLogin && toUserLogin.lastLogin){
				if ((fromUserLogin.active == "Y" && toUserLogin.active == "N") ||  (toUserLogin.active == fromUserLogin.active && fromUserLogin.lastLogin > toUserLogin.lastLogin)){
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
			updateUserLoginRefrence(fromUserLogin, toUserLogin)
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

		PERSON_DELETE_EXCEPTIONS_MAP.each { table, fields ->
			fields.each { column, status ->
				jdbcTemplate.update("UPDATE ${table} SET ${column} = '${toPerson.id}' WHERE ${column} = '${fromPerson.id}'")
			}
		}
	}

	/**
	 * This method is used to update all UserLogin reference in all domains from on account to another
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 * @return
	 */
	@Transactional
	def updateUserLoginRefrence(fromUserLogin, toUserLogin) {
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
	 */
	@Transactional
	void updateProjectRelationship(Party fromPerson, Party toPerson) {
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

		def existingRelationship = PERSON_DOMAIN_RELATIONSHIP_MAP.find{key, value ->
			return value.find{prop ->
				if (!checkRelationshipException(key, prop)) {
					def result = jdbcTemplate.queryForList("SELECT count(*) AS count FROM ${key} WHERE ${prop} = '${person.id}'")
					if (result[0].count > 0) {
						log.info "Found relationship for '${person.firstName}, ${person.lastName}' on table: '${key}' over field: '${prop}'"
						return true
					}
				}
				return false
			}

		}

/*

		def existRelationship = false
		def result

		PERSON_DOMAIN_RELATIONSHIP_MAP.each{ key, value ->
			value.find{ prop ->
				if (!checkRelationshipException(key, prop)) {
					result = jdbcTemplate.queryForList("SELECT count(*) AS count FROM ${key} WHERE ${prop} = '${person.id}'")
					if (result[0].count > 0) {
						log.info "Found relationship for '${person.firstName}, ${person.lastName}' on table: '${key}' over field: '${prop}'"
					}
					existRelationship = existRelationship || (result[0].count > 0)
				}
			}
		}

		return existRelationship*/
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
	@Transactional
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
						// Deletes the person and other related entities.
						Map deleteResultMap = deletePerson(person, true, deleteIfAssocWithAssets)

						// Updates variables that comput different results.
						cleared += deleteResultMap["cleared"]
						if(deleteResultMap["deleted"]){
							deleted++
						}else{
							skipped++
						}
						log.info("MEssages: ${deleteResultMap["messages"]}")
						messages << deleteResultMap["messages"]


						/*
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
						*/
					}
				}
			}
		}

		return [deleted: deleted, skipped: skipped, cleared: cleared, messages: messages]
	}

	/**
	 * Used to assign or unassign a person to a move event for a specified team
	 * @param personId - the person id to assign to the project
	 * @param eventId - the id of the event to manage the assignment of
	 * @param teamCode - the team code for the assigment
	 * @param toAssign - indicates if the assignment should be added (1) or removed (0)
	 * @return A String that when blank indicates success otherwise will contain an error message
	 */
	@Transactional
	String assignToProjectEvent(UserLogin byWhom, personId, eventId, teamCode, toAssign) {
		String message = ""

		// Check if the user has permission to edit the staff
		if ( ! securityService.hasPermission(byWhom, 'EditProjectStaff', true) ) {
			return "You do not have permission to assign staff to events"
		}

		personId = NumberUtil.toPositiveLong(personId, -1)
		eventId = NumberUtil.toPositiveLong(eventId, -1)
		if (personId == -1 || eventId == -1) {
			return "The selected person and/or move event ids are invalid"
		}

		if ( ! ['0','1'].contains(toAssign) ) {
			return "The action was not properly identified"
		}

		MoveEvent moveEvent = MoveEvent.get( eventId )
		Person person = Person.get( personId )
		if (! person || ! moveEvent) {
			return "The selected person and/or move event were not found"
		}

		// Check that the individual that is attempting to assign someone has access to the project in the first place
		Project project = moveEvent.project
		if (! hasAccessToProject(byWhom.person, project)) {
			securityService.reportViolation("attempted to modify staffing on project $project with proper access", byWhom)
			return "You do not have access to the project specified"
		}

		// Now make sure that the person being assigned is affiliated with the project in some manor
		if (! getAvailableProjects(person, project)) {
			securityService.reportViolation("attempted to assign a person (${person.id}) to project $project that is not affilated with", byWhom)
			return "$person is not authorized to access the project"
		}

		if (toAssign) {

			// First add to the project for the team if not already
			addToProjectTeamSecured(byWhom, project, person, teamCode)

			// Add the individual the the move event
			moveEventService.addTeamMember(moveEvent, person, teamCode)
			auditService.logMessage("assigned ${person} on team $teamCode of project ${project.name} event $moveEvent")

		} else {

			// Attempt to delete the team assignment on the event (assuming it exists)
			if (moveEventService.removeTeamMember(moveEvent, person, teamCode)) {
				auditService.logMessage("$byWhom unassigned ${person} on team $teamCode of project ${project.name} event $moveEvent")
			}
		}

		return ''
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
	Map validateUserCanEditStaffing(UserLogin user, def projectId, def personId, String teamCode) {
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
	 * @param personToAccess - the Person to be accessed
	 * @param forEdit - flag that when set to true will validate the accessor has permission to edit
	 * @return true if byWhom has access to the person otherwise false
	 */
	boolean hasAccessToPerson(Person byWhom, Person personToAccess, boolean forEdit=false, boolean reportViolation=true)
		throws UnauthorizedException {

	//
	// TODO : JPM 3/2016 : hasAccessToPerson() presently does NOT work
	//
		boolean hasAccess = false
		List byWhomProjects = getAvailableProjects(byWhom)*.id
		List personProjects = getAvailableProjects(personToAccess)*.id

		if (forEdit && ! securityService.hasPermission(byWhom, 'EditUserLogin')) {
			if (reportViolation) {
				reportViolation("attempted to edit person $personToAccess (${personToAccess.id}) without permission", byWhom)
			}
			return false
			// throw new UnauthorizedException('Do not have required permission to edit user')
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
				securityService.reportViolation("attempted to access person $personToAccess (${personToAccess.id}) without proper access", byWhom)
			}
			throw new UnauthorizedException('Do not have access to specified user')
		}
	}

	/**
	 * Used to associate a person to a project for a given team code
	 * @param byWhom - the user performing the action
	 * @param projectId - the id number of the project to assign the person to
	 * @param personId - the id of the person to assign to the project
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @param map - Used to load various data and errors to reference
	 */
	@Transactional
	void addToProjectTeam(UserLogin byWhom, def projectId, def personId, String teamCode, Map map = null) {
		// The addToEvent may call this method as well
		if (! map) {
			map = validateUserCanEditStaffing(byWhom, projectId, personId, teamCode)
		}
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		addToProjectTeamSecured(byWhom, map.project, map.person, teamCode)

	}

	/**
	 * Used to associate a person to a project as staff
	 * @param byWhom - the user performing the action
	 * @param project - the project to assign the person to
	 * @param person - the person to assign update to the project
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 */
	@Transactional
	void addToProjectTeamSecured(UserLogin byWhom, Project project, Person person, String teamCode) {
		// Add to the project if not assiged already
		addToProjectSecured(byWhom, project, person)

		// Add the person to the team at the company level
		addToTeam(byWhom, person, teamCode)

		if (! isAssignedToProjectTeam(project, person, teamCode)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "PROJECT", person, teamCode)) {
				auditService.logMessage("$byWhom assigned ${person} to project '${project.name}' on team $teamCode")
			} else {
				throw new DomainUpdateException("An error occurred while trying to assign the person to the event")
			}
		} else {
			println "addToProjectSecured - isAssignedToProjectTeam returned that it was assigned to project team"
			log.warn "addToProjectTeam() called for project ${project}, person ${person}, team ${teamCode} but already exists"
		}

	}

	/**
	 * Used to assign a teamCode to a Person
	 * @param person - the person to assign the team to
	 * @param teamCode - the team code to associate to the person
	 */
	@Transactional
	void addToTeam(UserLogin byWhom, Person person, String teamCode) {
		if (! isAssignedToTeam(person, teamCode)) {
			if (partyRelationshipService.savePartyRelationship("STAFF", person.company, "COMPANY", person, teamCode)) {
				auditService.logMessage("$byWhom assigned ${person} to team $teamCode")
			} else {
				throw new DomainUpdateException("An error occurred while trying to assign a person to a team")
			}
		}
	}

	/**
	 * Used to associate a person to a project as staff
	 * @param byWhom - the user performing the action
	 * @param projectId - the id number of the project to add the person to
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void addToProject(UserLogin byWhom, String projectId, String personId) {
		// The addToEvent may call this method as well
		def	map = validateUserCanEditStaffing(byWhom, projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		addToProjectSecured(byWhom, map.project, map.person)
	}

	/**
	 * Used to associate a person to a project as staff (Secured) which is only used if permissions were already checked
	 * if the
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	private void addToProjectSecured(UserLogin byWhom, Project project, Person person) {
		// Add to the project if not assiged already
		if (! isAssignedToProject(project, person)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "PROJECT", person, 'STAFF')) {
				auditService.logMessage("$byWhom assigned ${person} to project ${project.name} as STAFF")
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
	private MoveEvent lookupEvent(Project project, def eventId) {
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
	 * Used to remove a person from a team on a project which will also clear out references to MoveEventStaff
	 * for the given team.
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

		projectService.removeTeamMember(map.project, map.person, teamCode)

		auditService.logMessage("$user unassigned ${map.person} from team $teamCode of project ${map.project.name}")
	}

	/**
	 * Used to remove a person from a project as staff and also clear out various association that the individual may have
	 * When disassociating a person from the project there are a few things to be done:
	 * 	  1. Remove their association to the project in PartyRelationship for STAFF and any TEAM relations
	 * 	  2. Remove any TEAM assignments to Events
	 * 	  3. Remove Task assignments that the individual may have had
	 * 	  4. Remove references in the Application By properties (e.g. shutdownBy, startupBy)
	 * 	  5. For individuals that are NOT staff of the client, remove any association they have as app owner or SMEs
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	@Transactional
	String removeFromProject(UserLogin user, String projectId, String personId) {
		Map map = validateUserCanEditStaffing(user, projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		Map metrics = [teamsUnassigned:0, appOwnerUnassigned:0, smeUnassigned:0, sme2Unassigned:0]

		// Remove all of the person's MoveEventStaff relationships for the project
		metrics.eventsUnassigned = deleteFromEvent(map.project, null, map.person, map.teamRoleType)

		// Remove the Project Staff relationship for the project
		List roles = partyRelationshipService.getProjectStaffFunctions(map.project.id, map.person.id)
		roles?.each {
			if (it.type.equals(RoleType.TEAM)) {
				partyRelationshipService.deletePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, it.id)
				metrics.teamsUnassigned++
			}
		}

		// Remove the person from the project
		PartyRelationship prProjectStaff = getProjectReference(map.project, map.person)
		if (prProjectStaff) {
			log.debug "removeFromProject() deleting PartyRelationship $prProjectStaff"
			prProjectStaff.delete()
			metrics.staffUnassigned = 1
		} else {
			log.warn "removeFromProject() No Project Staff record found for project $projectId and person $personId"
		}

		Map qparams = [project:map.project, person:map.person]

		// Now lets start clearing out the other references starting with Task assignments
		String sql = 'update AssetComment task set  task.assignedTo=null where task.project=:project and task.assignedTo=:person'
		metrics.tasksUnassigned = AssetComment.executeUpdate(sql, qparams)


		// Clears out the CURR_PROJ preference if it matches projectId and other project related preferences.
		UserLogin targetUserLogin = UserLogin.findByPerson(map.person)
		if(targetUserLogin){
			UserPreference projectPreference = UserPreference.findByUserLoginAndPreferenceCode( targetUserLogin, "CURR_PROJ")
			if(projectPreference?.value == projectId){
				projectPreference.delete()
				userPreferenceService.removeProjectAssociatedPreferences(targetUserLogin)
			}
		}



		qparams.person = map.person.id.toString()
		['shutdownBy', 'startupBy', 'testingBy'].each {
			sql = "update Application a set a.${it}=null where a.project=:project and a.${it}=:person"
			metrics["${it}Unassigned".toString()] = Application.executeUpdate(sql, qparams)
		}

		def employer = map.person.company
		// log.debug "removeFromProject() project=${map.project.id}, employer=$employer (${employer.id}), project client=${map.project.client} (${map.project.client.id})"
		if (map.project.client.id != employer.id) {

			qparams.person = map.person

			sql = 'update AssetEntity a set a.appOwner=null where a.project=:project and a.appOwner=:person'
			metrics.appOwnerUnassigned = AssetEntity.executeUpdate(sql, qparams)

			['sme', 'sme2'].each {
				sql = "update Application a set a.${it}=null where a.project=:project and a.${it}=:person"
				metrics["${it}Unassigned".toString()] = Application.executeUpdate(sql, qparams)
			}

		}

		auditService.logMessage("$user unassigned ${map.person} from project ${map.project.name} - results $metrics")
	}

	/**
	 * Used to associate a person to a project as staff
	 * @param user - the user performing the action
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	@Transactional
	void addToEvent(UserLogin user, def projectId, def eventId, def personId, String teamCode) {
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
	@Transactional
	void removeFromEvent(UserLogin user, String projectId, String eventId, String personId, String teamCode) {
		Map map = validateUserCanEditStaffing(user, projectId, personId, teamCode)

		MoveEvent event = lookupEvent(map.project, eventId)

		deleteFromEvent(map.project, event, map.person, map.teamRoleType)

		auditService.logMessage("$user unassigned ${map.person} from team $teamCode for event $event of project ${map.project.name}")
	}

	/**
	 * Used to determine if a person is assigned to a project as a STAFF member
	 * @param person - the person whom to check
	 * @param project - the project to check if user is assigned to
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToProject(Project project, Person person) {
		return (getProjectReference(project, person) != null)
	}

	/**
	 * Used to determine if a person is assigned to a project as a STAFF member
	 * @param person - the person whom to check
	 * @param project - the project to check if user is assigned to
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToTeam(Person person, String teamCode) {
		return (partyRelationshipService.getCompanyStaffFunctions(person.company, person, teamCode).size()>0)
	}

	/**
	 * Used to determine if a person is assigned to a project as a STAFF member
	 * @param project - the project to check if user is assigned to
	 * @param person - the person whom to check
	 * @param teamCode - the team code
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToProjectTeam(Project project, Person person, teamCode) {
		def ref = getProjectTeamReference(project, person, teamCode)
		println "isAssignedToProjectTeam($project, $person, $teamCode) = $ref"
		return ( ref != null)
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

		def teamRef = PartyRelationship.createCriteria().get {
			and {
				eq('partyRelationshipType.id', 'PROJ_STAFF')
				eq('roleTypeCodeFrom.id', 'PROJECT')
				eq("roleTypeCodeTo${(teamCode instanceof RoleType) ? '' : '.id'}", teamCode)
				eq('partyIdFrom', project)
				eq('partyIdTo', person)
			}
		}

		println "getProjectTeamReference($project, $person, $teamCode) returns $teamRef"
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

		projects = projects.sort { it.name }
		return projects
	}

	/**
	 * Used to determine if the person is associated with the project
	 * @param person - the person to check
	 * @param project - the project to see if person has access to
	 * @return true if person is associated otherwise false
	 */
	boolean hasAccessToProject(Person person, Project project) {
		def projects =  projectService.getUserProjects(null, false, ProjectStatus.ANY, [personId:person.id])
		def found = false
		if (projects){
			found = ( projects.find { it.id == project.id } != null )
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

		Person person = Person.get(personId)
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
	@Transactional
	Person updatePerson(Map params, Person byWhom, String tzId, boolean byAdmin = false)
		throws DomainUpdateException, UnauthorizedException, InvalidParamException, EmptyResultException {
		Person person = validatePersonAccess(params.id, byWhom)
		def session = WebUtils.retrieveGrailsWebRequest().session
		if(!isAssociatedTo(byWhom, person.company)){
			throw new UnauthorizedException("You do not have permission to manage staffing for the user's company")
		}

		def ret = []
		params.travelOK == "1" ? params : (params.travelOK = 0)

		if (!person.staffType && !params.staffType) params.staffType = 'Hourly'

		// TODO : JPM 8/31/2015 : Replace person.properties = params with proper field assignments
		person.properties = params

		def project = securityService.getUserCurrentProject()

		def isPersonAmbiguous = { personInstance ->
			Map nameMap = [first: personInstance.firstName, middle: personInstance.middleName, last: personInstance.lastName]
			Map findPersonInfo = findPerson(nameMap, project, null, false)
			boolean isPersonAmbiguous = findPersonInfo.isAmbiguous && findPersonInfo.person?.id != personInstance.id
			return isPersonAmbiguous
		}

		if(isPersonAmbiguous(person)){
			log.error "updatePerson() unable to save $person because of conflicting name."
			throw new DomainUpdateException("The name of the person is ambiguous.")
		}

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
			List teams = params.list("function")
			if (params.manageFuncs != '0' || teams) {
				partyRelationshipService.updateAssignedTeams(person, teams)
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
				userPreferenceService.setPreference(PREF.CURR_TZ, params.timeZone)
				userPreferenceService.loadPreferences(PREF.CURR_TZ)
			}

			if (params.powerType) {
				userPreferenceService.setPreference(PREF.CURR_POWER_TYPE, params.powerType)
			}

			if (params.startPage) {
				userPreferenceService.setPreference(PREF.START_PAGE, params.startPage)
				userPreferenceService.loadPreferences(PREF.START_PAGE)
			}
		}

		return person

	}

	/**
	 * Used by controller to create a Person.
	 * @param params - the form params that were passed
	 * @param byWhom - The person that is performing the update
	 * @param companyId - The person company
	 * @param defaultProject - this is the byWhom's currentProject that the person will be assigned to if the company is the project.client
	 * @param byAdmin - Flag indicating that it is being done by the admin form (default false)
	 * @return The Person record being created or throws an exception for various issues
	 */
	@Transactional
	Person savePerson(Map params, Person byWhom, Long companyId, Project defaultProject, boolean byAdmin = false)
		throws DomainUpdateException, InvalidParamException {

		def companyParty
		def person

		// Look to allow easy breakout for exceptions
		while (true) {
			if (companyId != null) {
				companyParty = Party.get( companyId )
			}

			if (!companyParty) {
				throw new InvalidParamException('Unable to locate proper company to associate person to')
			}

			// Get list of all staff for the company and then try to find the individual so that we don't duplicate
			// the creation
			def personList = partyRelationshipService.getCompanyStaff(companyId)
			// TODO : JPM 3/2016 : savePerson() Switch the person lookup to use the finder service
			person = personList.find {
				// Find person using case-insensitive search
				StringUtils.equalsIgnoreCase(it.firstName, params.firstName) &&
				( ( StringUtils.isEmpty(params.lastName) && StringUtils.isEmpty(it.lastName) ) ||  StringUtils.equalsIgnoreCase(it.lastName, params.lastName) ) &&
				( ( StringUtils.isEmpty(params.middleName) && StringUtils.isEmpty(it.middleName) ) ||  StringUtils.equalsIgnoreCase(it.middleName, params.middleName) )
			}

			if (person != null) {
				throw new DomainUpdateException("A person with that name already exists. Person Id:${person.id}")
			} else {
				// Create the person and relationship appropriately
				def reducedParams = new HashMap(params)
				reducedParams.remove('company')
				reducedParams.remove('function')

				person = new Person( reducedParams )
				if ( person.save() ) {
					// Assign the person to the company
					partyRelationshipService.addCompanyStaff(companyParty, person)

					def teamCodes = params.containsKey('function') ? params.function : []
					if (teamCodes instanceof String) {
						teamCodes = [ teamCodes ]
					}
					if (teamCodes) {
						// Assign the person to the appropriate teams
						partyRelationshipService.updateAssignedTeams(person, teamCodes)
					}

					// If the byUser's current project.client is the same as the new person's company then we'll
					// automatically assign the person to the project as well
					if (defaultProject != null && defaultProject.client.id == companyId) {
						if (teamCodes) {
							teamCodes.each { tc ->
								addToProjectTeamSecured(byWhom.userLogin, defaultProject, person, tc)
							}
						} else {
							// Add the person to the project which is done automatically when adding the team to the project
							addToProjectSecured(byWhom.userLogin, defaultProject, person)
						}
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

	/**
	 * Used to get a list of the team RoleType associated with a person
	 * @param person - the person to lookup the team codes for
	 * @return a list of the teams
	 */
	List<RoleType> getPersonTeamRoleTypes(Person person) {
		List teams = partyRelationshipService.getCompanyStaffFunctions(person.company, person)
	}

	/**
	 * Used to get a list of the team codes associated with a person
	 * @param person - the person to lookup the team codes for
	 * @return a list of the teams
	 */
	List<String> getPersonTeamCodes(Person person) {
		List teams = getPersonTeamRoleTypes(person)

		return teams*.id
	}

	/**
	 * Determines if a person is associated to a company
	 * @param Person trying to access the company
	 * @param Company the person is trying to access
	 */
	boolean isAssociatedTo(Person whom, Party company){
		List<Party> companies = partyRelationshipService.associatedCompanies(whom)
		def c = companies.find{it.id == company.id }
		return c != null
	}

}
