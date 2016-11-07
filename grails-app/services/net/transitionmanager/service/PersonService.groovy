package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.ExceptionDates
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyRelationshipType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * Provides a number of functions to help in the management and access of Person objects.
 */
@Slf4j(value='logger')
class PersonService implements ServiceMethods {

	AuditService auditService
	GrailsApplication grailsApplication
	JdbcTemplate jdbcTemplate
	MoveEventService moveEventService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	SecurityService securityService
	UserPreferenceService userPreferenceService

	private static final List<String> SUFFIXES = [
			"jr.", "jr", "junior", "ii", "iii", "iv", "senior", "sr.", "sr", //family
			"phd", "ph.d", "ph.d.", "m.d.", "md", "d.d.s.", "dds", // doctors
			"k.c.v.o", "kcvo", "o.o.c", "ooc", "o.o.a", "ooa", "g.b.e", "gbe", // knighthoods
			"k.b.e.", "kbe", "c.b.e.", "cbe", "o.b.e.", "obe", "m.b.e", "mbe", //   cont
			"esq.", "esq", "esquire", "j.d.", "jd", // lawyers
			"m.f.a.", "mfa", //misc
			"r.n.", "rn", "l.p.n.", "lpn", "l.n.p.", "lnp", //nurses
			"c.p.a.", "cpa", //money men
			"d.d.", "dd", "d.div.", "ddiv", //preachers
			"ret", "ret."]

	private static final List<String> COMPOUND_NAMES = [
			"de", "la", "st", "st.", "ste", "ste.", "saint", "der", "al", "bin",
			"le", "mac", "di", "del", "vel", "van", "von", "e'", "san", "af", "el", "o'"
	]

	private static final Map<String, List<String>> PERSON_DOMAIN_RELATIONSHIP_MAP = [
			application     : ['sme_id', 'sme2_id', 'shutdown_by', 'startup_by', 'testing_by'],
			asset_comment   : ['resolved_by', 'created_by', 'assigned_to_id'],
			comment_note    : ['created_by_id'],
			asset_dependency: ['created_by', 'updated_by'],
			asset_entity    : ['app_owner_id'],
			exception_dates : ['person_id'],
			model           : ['created_by', 'updated_by', 'validated_by'],
			model_sync      : ['created_by_id', 'updated_by_id', 'validated_by_id'],
			model_sync_batch: ['created_by_id'],
			move_event_news : ['archived_by', 'created_by'],
			move_event_staff: ['person_id'],
			workflow        : ['updated_by'],
			recipe_version  : ['created_by_id'],
			task_batch      : ['created_by_id']
	]

	private static final Map<String, Map<String, Boolean>> PERSON_DELETE_EXCEPTIONS_MAP = [
		application: [sme_id: true, sme2_id: true], asset_entity: [app_owner_id: true]
	]

	private static final List<String> notToUpdate = ['beforeDelete', 'beforeInsert', 'beforeUpdate',
	                                                       'blackOutDates', 'firstName', 'id']

	/**
	 * Returns a properly format person's last name with its suffix
	 * @param Map the map of last and suffix
	 * @return String the composite name with the suffix if it exists
	 */
	private String lastNameWithSuffix(Map nameMap) {
		String last = ''

		if (nameMap.last && nameMap.suffix) {
			last = nameMap.last + ', ' + nameMap.suffix
		}
		else if (nameMap.last) {
			last = nameMap.last
		}

		return last
	}

	/**
	 * Find a person by their name that is staff of the specified company
	 * @param company - The company that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */
	// List findByClientAndName(PartyGroup client, Map nameMap) {
	List<Person> findByClientAndName(PartyGroup company, Map nameMap) {
		Map queryParams = [company: company.id]
		def (first, middle, last) = [false, false, false]

		StringBuffer query = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr')
		query.append(' JOIN person p ON p.person_id=pr.party_id_to_id')
		query.append(' WHERE pr.party_id_from_id=:company')
		query.append(' AND pr.role_type_code_from_id="COMPANY"')
		query.append(' AND pr.role_type_code_to_id="STAFF"')
		// query.append(' ')
		if (nameMap.first) {
			queryParams.first = nameMap.first
			query.append(' AND p.first_name=:first')
		}
		if (nameMap.last) {
			queryParams.last = nameMap.last
			query.append(' AND p.last_name=:last')
		}

		def persons
		def pIds = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)

		if (nameMap.middle) {
			// Try to lookup the person with their middle name as well
			queryParams.middle = nameMap.middle
			query.append(' AND p.middle_name=:middle')
			pIds.addAll(namedParameterJdbcTemplate.queryForList(query.toString(), queryParams))
		}

		if (pIds) {
			persons = Person.getAll(pIds*.id).findAll()
		}

		return persons
	}

	/**
	 * Find a person by their name that is staff of the specified company
	 * @param company - The company that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */
	// TODO : JPM 4/2016 : findByCompanyAndName is replacing findByClientAndName
	List<Person> findByCompanyAndName(PartyGroup company, Map nameMap) {
		List persons = []
		Map queryParams = [companyId: company.id]
		def (first, middle, last) = [false, false, false]

		StringBuffer select = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr')
		select.append(' JOIN person p ON p.person_id=pr.party_id_to_id')
		select.append(' WHERE pr.party_id_from_id=:companyId')
		select.append(' AND pr.role_type_code_from_id="COMPANY"')
		select.append(' AND pr.role_type_code_to_id="STAFF" ')

		StringBuffer query = new StringBuffer(select)
		if (nameMap.first) {
			queryParams.first = nameMap.first
			query.append(' AND p.first_name=:first')
			first = true
		}
		if (nameMap.middle) {
			queryParams.middle = nameMap.middle
			query.append(' AND p.middle_name=:middle')
			middle = true
		}
		if (nameMap.last) {
			queryParams.last = nameMap.last
			query.append(' AND p.last_name=:last')
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
			persons = Person.getAll(pIds*.id).findAll()
		}

		return persons
	}

	/**
	 * Find a person by their name for a specified client
	 * @param client - The client that the person would be associated as Staff
	 * @param nameMap - a map of the person's name (map [first, last, middle])
	 * @return A list of the person(s) found that match the name or null if none found
	 */
	List<Person> findByClientAndEmail(PartyGroup client, String email) {
		Map args = [client: client.id, email: email]
		StringBuffer query = new StringBuffer('SELECT party_id_to_id as id FROM party_relationship pr JOIN person p ON p.person_id=pr.party_id_to_id')
		query.append(' WHERE pr.party_id_from_id=:client')
		query.append(' AND pr.role_type_code_from_id="COMPANY"')
		query.append(' AND pr.role_type_code_to_id="STAFF"')
		query.append(' AND p.email=:email')

		logger.debug 'findByClientAndEmail: query {}, map {}', query, args
		List<Person> persons
		def pIds = namedParameterJdbcTemplate.queryForList(query.toString(), args)
		logger.debug 'findByClientAndEmail: query {}, map {}, found ids {}', query, args, pIds
		if (pIds) {
			persons = Person.getAll(pIds*.id).findAll()
		}

		return persons
	}

	/**
	 * Find a person associated with a given project using a string representation of their name.
	 * This method overloads the other findPerson as a convinence so one can just pass the string vs parsing the name and calling the alternate method.
	 * @param A string representing a person's name (e.g. John Doe; Doe, John; John T. Doe)
	 * @param Project the project/client that the person is associated with
	 * @param The staff for the project. This is optional but recommended if the function is used repeatedly (use partyRelationshipService.getCompanyStaff(project?.client.id) to get list).
	 * @param Flag to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return Null if name unable to be parsed or a Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is
	 * found. If more than one match is found then isAmbiguous will be set to true.
	 */
	Map findPerson(String name, Project project, def staffList = null, def clientStaffOnly = true) {
		Map map = parseName(name)
		if (map) {
			map = findPerson(map, project, staffList, clientStaffOnly)
		}
		logger.debug 'findPerson(String) results={}', map
		return map
	}

	/**
	 * Find a person by full name
	 * @param A string representing a person's name (e.g. John Doe; Doe, John; John T. Doe)
	 * @return Null if name unable to be parsed or a Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is
	 * found. If more than one match is found then isAmbiguous will be set to true.
	 */
	Map findPersonByFullName(String name) {
		def map = parseName(name)
		if (map) {
			List<Person> people = Person.findAllByFirstNameAndMiddleNameAndLastName(map.first, map.middle, map.last)
			if (people.size() == 1) {
				map.person = people[0]
			}
			else {
				map.isAmbiguous = true
			}
		}
		else {
			map.isAmbiguous = true
		}

		return map
	}

	/**
	 * Find a person associated with a given project using a parsed name map
	 * @param nameMap - a Map containing person name elements
	 * @param project - the project object that the person is associated with
	 * @param staffList - deprecated argument that is no longer used
	 * @param clientStaffOnly - a flag to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return A Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is found. If more than one match is
	 * found then isAmbiguous will be set to true.
	 */
	Map findPerson(Map nameMap, Project project, List staffList = null, boolean clientStaffOnly = true) {
		String mn = 'findPerson()'
		def results = [person: null, isAmbiguous: false]

		logger.debug 'findPersion() attempting to find nameMap={} in project {}', nameMap, project

		// Make sure we have a person
		if (!nameMap || !nameMap.containsKey('first')) {
			results.isAmbiguous = true
			return results
		}

		String hql = '''
			from PartyRelationship PR
			inner join PR.partyIdTo P
			where PR.partyRelationshipType.id='STAFF'
			  and PR.roleTypeCodeFrom.id='COMPANY'
			  and PR.roleTypeCodeTo.id='STAFF'
			  and PR.partyIdFrom IN (:companies)
		'''
		List companies = [project.client]

		String where = ' and P.firstName=:firstName'
		String lastName = lastNameWithSuffix(nameMap)
		Map<String, Object> queryParams = [companies: companies, firstName: nameMap.first]

		if (lastName) {
			where += " AND P.lastName=:lastName"
			queryParams.lastName = lastName
		}
		if (nameMap.middle) {
			where += " AND P.middleName=:middleName"
			queryParams.middleName = nameMap.middle
		}

		if (!clientStaffOnly) {
			companies << projectService.getOwner(project)
			companies.addAll(projectService.getPartners(project))
		}

		// Try finding the person with an exact match
		List persons = Person.executeQuery(hql + where, queryParams)
		if (persons) {
			persons = persons.collect({ it[1] })
		}
		logger.debug '{} Initial search found {} {}', mn, persons.size(), nameMap

		int s = persons.size()
		if (s > 1) {
			persons.each { person -> logger.debug 'person {} {}', person.id, person }
			results.person = persons[0]
			results.isAmbiguous = true
		}
		else if (s == 1) {
			results.person = persons[0]
		}
		else {

			// Try to find match on partial

			// Closure to construct the where and queryParams used below
			def addQueryParam = { name, value ->
				if (!StringUtil.isBlank(value)) {
					where += " and P.$name=:$name"
					queryParams[name] = value
				}
			}

			where = ''
			queryParams = [companies: companies]
			addQueryParam('firstName', nameMap.first)
			addQueryParam('middleName', nameMap.middle)
			addQueryParam('lastName', lastName)

			logger.debug '{} partial search using {}', mn, queryParams
			persons = Person.findAll(hql + where, queryParams)
			if (persons) {
				persons = persons.collect({ it[1] })
			}
			logger.debug '{} partial search found {}', mn, persons.size()

			s = persons.size()
			if (s > 1) {
				results.isAmbiguous = true
			}
			else if (s == 1) {
				results.person = persons[0]
				results.isAmbiguous = (StringUtil.isBlank(lastName) && !StringUtil.isBlank(results.person.lastName))
			}
		}

		logger.debug '{} results={}', mn, results
		return results
	}

	/**
	 * Finds a person object from their full name and if not found create it
	 * @param name - a String containing the person's full name to lookup
	 * @param project - the project object that the person is associated with
	 * @param staffList - deprecated argument that is no longer used
	 * @param clientStaffOnly - a flag to indicate if the search should only look at staff of the client or all persons associated to the project
	 * @return A Map[person:Person,isAmbiguous:boolean] where the person object will be null if no match is found. If more than one match is
	 * found then isAmbiguous will be set to true.
	 */
	Map findOrCreatePerson(String name, Project project, List<Person> staffList = null, boolean clientStaffOnly = true) {
		def nameMap = parseName(name)
		if (nameMap == null) {
			logger.error 'findOrCreatePersonByName() unable to parse name ({})', name
			return null
		}
		return findOrCreatePerson(nameMap, project, staffList, clientStaffOnly)
	}

	/**
	 * Finds a person object after importing and if not found create it
	 * Finds a person object from their full name and if not found create it
	 * @param nameMap - a Map the person's full name in map
	 * @param project - The Project that the person is associated with
	 * @param staffList - NO LONGER NEEDED
	 * @param clientStaffOnly - a flag to indicate if it should only look for person that is staff of the company or allow anyone assigned to a project (default true)
	 * @return Map containing person, status or null if unable to parse the name
	 */
	Map findOrCreatePerson(Map nameMap, Project project, List staffList = null, boolean clientStaffOnly = true) {
		Person person
		boolean staffListSupplied = (staffList != null)
		try {

			def results = findPerson(nameMap, project, staffList, clientStaffOnly)
			results.isNew = null

			if (!results.person && nameMap.first) {
				logger.info 'findOrCreatePerson() Creating new person ({}) as Staff for {}', nameMap, project.client
				person = new Person('firstName': nameMap.first, 'lastName': nameMap.last, 'middleName': nameMap.middle, staffType: 'Salary')
				save person, true
				if (person.hasErrors()) {
					results.error = "Unable to create person $nameMap${GormUtil.allErrorsString(person)}"
				}
				else {
					if (!partyRelationshipService.addCompanyStaff(project.client, person)) {
						results.error = "Unable to assign person $results.person as staff"
						// TODO - JPM (10/13) do we really want to proceed if we can't assign the person as staff otherwise they'll be in limbo.
					}
					results.person = person
					results.isNew = true
				}
			}
			else {
				results.isNew = false
			}

			return results
		}
		catch (e) {
			String exMsg = e.message
			logger.error 'findOrCreatePerson() received exception for nameMap={} : {}\n{}',
					nameMap, e.message, ExceptionUtil.stackTraceToString(e)
			if (person && !person.id) {
				person.discard()
				person = null
			}
			throw new RuntimeException('Unable to create person : ' + exMsg)
		}
	}

	/**
	 * Parses a name into its various components and returns them in a map
	 * @param String The full name of the person
	 * @return Map - the map of the parsed name that includes first, last, middle, suffix or null if it couldn't be parsed for some odd reason
	 */
	Map parseName(String name) {
		name = StringUtils.strip(name)
		Map map = [first: '', last: '', middle: '', suffix: '']
		def firstLast = true
		def split

		if (!name) {
			return null
		}

		// Check for last, first OR first last, suffix
		if (name.contains(',')) {
			split = name.split(',').collect { it.trim() }
			//println "a) split ($split) isa ${split.getClass()}"
			def size = split.size()

			if (size == 2) {
				// Check to see if it is a Suffix vs last, first
				def s = split[1]
				if (SUFFIXES.contains(s.toLowerCase())) {
					// We got first last, suffix
					map.suffix = s
					// Split the rest to be mapped out below
					//println "b) splitting ${split[0]}"
					split = split[0].split("\\s+").collect { it.trim() }
					//println "b) split ($split) isa ${split.getClass()}"
				}
				else {
					firstLast = false
				}
			}
			else {
				logger.error 'parseName("{}") encountered multiple commas that is not handled', name
				return null
			}
		}
		else {
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
			if (size > 1 && SUFFIXES.contains(split[-1].toLowerCase())) {
				size--
				map.suffix = split[size]
				split.pop()
				//println "3) split ($split) isa ${split.getClass()}"

			}

			// Check to see if we have a middle name or a compound name
			if (size >= 2) {
				//println "4) split ($split) isa ${split.getClass()}"
				def last = split.pop()
				if (COMPOUND_NAMES.contains(split[-1].toLowerCase())) {
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

		}
		else {
			// Deal with Last Suff, First Middle

			// Parse the Last Name element
			def last = split[0].split("\\s+").collect { it.trim() }
			size = last.size()
			if (size > 1 && SUFFIXES.contains(last[-1].toLowerCase())) {
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

	@Transactional
	Person mergePerson(Person fromPerson, Person toPerson) {
		UserLogin toUserLogin = toPerson.userLogin
		UserLogin fromUserLogin = fromPerson.userLogin

		GormUtil.copyUnsetValues(toPerson, fromPerson, notToUpdate)
		save toPerson

		mergeUserLogin(toUserLogin, fromUserLogin, toPerson)
		updatePersonReference(fromPerson, toPerson)
		updateProjectRelationship(fromPerson, toPerson)

		GormUtil.flushAndClearSession()

		fromPerson.delete()

		return fromPerson
	}

	/**
	 * Deletes a person and other entities related to the instance, such as party and party role.
	 *
	 * @param person - Person Instance to be deleted
	 * @param deleteIfUserLogin - boolean that indicates if a person with existing UserLogin must be deleted.
	 * @param deleteIfAssocWithAssets - boolean that indicates if a person with relationships with assets must
	 * 									 be deleted (see PERSON_DELETE_EXCEPTIONS_MAP)
	 * @return Map[
	 * 				String[]	-> messages: errors and other messages.
	 * 				int 		-> cleared: assets cleared
	 * 				Boolean 	-> deleted: the person was deleted.
	 * 			]
	 */
	@Transactional
	Map deletePerson(Person person, boolean deleteIfUserLogin, boolean deleteIfAssocWithAssets) {
		int cleared = 0
		boolean deleted = false
		List<String> messages = []
		UserLogin userLogin = person.userLogin

		boolean isDeletable = !haveRelationship(person) && !(!deleteIfUserLogin && userLogin)
		if (isDeletable) {

			Person.withTransaction { trxStatus ->

				try {

					// Sets additional person references to NULL
					PERSON_DELETE_EXCEPTIONS_MAP.each { table, fields ->
						fields.each { column, status ->
							if (isDeletable) {
								if (jdbcTemplate.queryForObject('SELECT count(*) FROM ' + table + ' WHERE ' + column + '=?', Integer, person.id)) {
									if (deleteIfAssocWithAssets) {
										// Clear out the person's associate with all assets for the given column
										logger.debug 'Disassociated person as {}', column
										cleared += jdbcTemplate.update('UPDATE ' + table +
											' SET ' + column + '=NULL WHERE ' + column + '=?', person.id)
									}
									else {
										logger.debug('Ignoring delete of person {} as it contains {} association with asset(s)', person.id, column)
										messages << "Staff '$person.firstName, $person.lastName' unable to be deleted due it contains $column association with asset(s)."
										isDeletable = false
									}
								}
							}
						}
					}

					if (isDeletable) {
						Map personMap = [person: person]
						Person.executeUpdate('DELETE PartyRole         WHERE party=:person', personMap)
						Person.executeUpdate("DELETE PartyRelationship WHERE partyIdFrom=:person or partyIdTo=:person", personMap)

						Party.load(person.id).delete()

						if (deleteIfUserLogin) {
							if (userLogin) {
								UserPreference.executeUpdate('DELETE UserPreference up WHERE up.userLogin = :user', [user: userLogin])
								userLogin.delete()
							}
						}
						person.delete()
						deleted = true
					}
				}
				catch (Exception e) {
					status.setRollbackOnly()
					messages << "There was an error trying to delete staff '$person.firstName, $person.lastName'"
					logger.debug('An error occurred while trying to delete {}: {}', person.id, e.message)
				}
			}
		}
		else {
			messages << "Staff '$person.firstName, $person.lastName' unable to be deleted due to associations with existing elements in one or more Projects. Please use Person Merge functionality if applicable."
		}

		return [messages: messages, cleared: cleared, deleted: deleted]
	}

	/**
	 * Merges Person's UserLogin according to criteria
	 * 1. If neither account has a UserLogin - nothing to do
	 * 2. If Person being merged into the master has a UserLogin but master doesn't, assign the UserLogin to the master Person record.
	 * 3. If both Persons have a UserLogin,select the UserLogin that has the most recent login activity. If neither have login activity,
	 * 	  choose the oldest login account.
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 * @param toPerson : instance of toPerson
	 * @return
	 */
	@Transactional
	def mergeUserLogin(toUserLogin, fromUserLogin, toPerson) {
		if (fromUserLogin && !toUserLogin) {
			fromUserLogin.person = toPerson
			fromUserLogin.save(flush: true)
		}
		else if (fromUserLogin && toUserLogin) {
			if (fromUserLogin.lastLogin && toUserLogin.lastLogin) {
				if ((fromUserLogin.active == "Y" && toUserLogin.active == "N") || (toUserLogin.active == fromUserLogin.active && fromUserLogin.lastLogin > toUserLogin.lastLogin)) {
					fromUserLogin.person = toPerson
					toUserLogin.delete()
				}
				else {
					fromUserLogin.delete()
				}
			}
			else {
				if (fromUserLogin.createdDate > toUserLogin.createdDate) {
					fromUserLogin.person = toPerson
					toUserLogin.delete()
				}
				else {
					fromUserLogin.delete()
				}
			}
		}
		if (fromUserLogin && toUserLogin) {
			updateUserLoginRefrence(fromUserLogin, toUserLogin)
		}
	}

	/**
	 * Updates Person reference from 'fromPerson' to  'toPerson'
	 * @param fromPerson : instance of fromPerson
	 * @param toPerson : instance of toPerson
	 * @return
	 */
	def updatePersonReference(fromPerson, toPerson) {
		PERSON_DOMAIN_RELATIONSHIP_MAP.each { key, value ->
			value.each { prop ->
				jdbcTemplate.update("UPDATE $key SET $prop = '$toPerson.id' where $prop= '$fromPerson.id'")
			}
		}

		PERSON_DELETE_EXCEPTIONS_MAP.each { table, fields ->
			fields.each { column, status ->
				jdbcTemplate.update("UPDATE $table SET $column = '$toPerson.id' WHERE $column = '$fromPerson.id'")
			}
		}
	}

	/**
	 * Update all UserLogin reference in all domains from on account to another
	 * @param fromUserLogin : instance of fromUserLogin
	 * @param toUserLogin : instance of toUserLogin
	 */
	@Transactional
	void updateUserLoginRefrence(fromUserLogin, toUserLogin) {
		def map = ['data_transfer_batch': ['user_login_id'], 'model_sync': ['created_by_id']]
		map.each { table, columns ->
			columns.each { column ->
				jdbcTemplate.update("UPDATE $table SET $column = $toUserLogin.id where $column=$fromUserLogin.id")
			}
		}
	}

	/**
	 * Update person reference in PartyRelationship table.
	 * @param toPerson : instance of Person
	 * @param fromPerson : instance of Person
	 */
	@Transactional
	void updateProjectRelationship(Party fromPerson, Party toPerson) {
		try {
			// Find all of the relationships that the FROM person has
			def allRelations = jdbcTemplate.queryForList("SELECT p.party_relationship_type_id AS prType, p.party_id_from_id AS pIdFrom, \
				p.party_id_to_id AS pIdTo, p.role_type_code_from_id AS rTypeCodeFrom, p.role_type_code_to_id AS rTypeCodeTo \
				FROM party_relationship p WHERE p.party_id_to_id = $fromPerson.id")

			allRelations.each { relation ->
				// Check to see if the TO person has the particular relationship already. If so we delete the FROM person relationship otherwise
				def toAlreadyHasRelationship = jdbcTemplate.queryForList("SELECT 1 FROM party_relationship p WHERE \
					p.party_relationship_type_id='$relation.prType' AND p.party_id_from_id =$relation.pIdFrom \
					AND p.party_id_to_id =$toPerson.id AND p.role_type_code_from_id='$relation.rTypeCodeFrom'\
					AND p.role_type_code_to_id ='$relation.rTypeCodeTo'")

				def where = " WHERE party_relationship_type_id = '$relation.prType' \
					   AND role_type_code_from_id = '$relation.rTypeCodeFrom' AND role_type_code_to_id='$relation.rTypeCodeTo' \
					   AND party_id_to_id = $fromPerson.id AND party_id_from_id = $relation.pIdFrom"

				if (toAlreadyHasRelationship) {
					jdbcTemplate.update("DELETE FROM party_relationship $where")
				}
				else {
					jdbcTemplate.update("UPDATE party_relationship SET party_id_to_id = $toPerson.id $where")
				}
			}
		}
		catch (Exception ex) {
			logger.error('Cannot update person project relationship: {}', ex.message, ex)
		}
	}

	/**
	 * Check if the user have a relationship with some entity, for example, Recipes
	 * @param person - Person to check
	 * @return boolean value that indicates if a relationship exist
	 */
	boolean haveRelationship(Person person) {
		for (entry in PERSON_DOMAIN_RELATIONSHIP_MAP.entrySet()) {
			String key = entry.key
			List<String> value = entry.value
			for (prop in value) {
				if (!checkRelationshipException(key, prop)) {
					int count = jdbcTemplate.queryForObject('SELECT count(*) FROM ' + key + ' WHERE ' + prop + '=?', Integer, person.id)
					if (count) {
						logger.info 'Found relationship for "{}, {}" on table: "{}" over field: "{}"',
								person.firstName, person.lastName, key, prop
						return true
					}
				}
			}
		}
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
	 * Bulk deletes Person objects as long as they do not have user accounts or assigned tasks and optionally associated with assets
	 * @param user - The user attempting to do the bulk delete
	 * @param ids - the list of person ids to delete
	 * @param deleteIfAssocWithAssets - a flag to indicate that it is okay to delete the person if they're associated to assets
	 * @return A map containing the following
	 *   deleted: number of persons deleted
	 *   skipped: number of persons skipped
	 *   cleared: number of assets references that were cleared/unassigned
	 */
	@Transactional
	Map bulkDelete(ids, Boolean deleteIfAssocWithAssets) {
		if (!ids) {
			throw new InvalidParamException('Must select at least one person to delete')
		}

		logger.info 'Attempted to bulk delete {} persons ({}), deleteIfAssocWithAssets={}', ids?.size(), ids, deleteIfAssocWithAssets

		int deleted = 0
		int skipped = 0
		int cleared = 0
		List<String> messages = []

		for (id in ids) {
			if (!id.isLong()) {
				continue
			}

			Person person = Person.get(id)
			if (!person) {
				continue
			}

			// Deletes the person and other related entities.
			Map deleteResultMap = deletePerson(person, true, deleteIfAssocWithAssets)

			// Updates variables that comput different results.
			cleared += deleteResultMap.cleared
			if (deleteResultMap.deleted) {
				deleted++
			}
			else {
				skipped++
			}
			messages.addAll deleteResultMap.messages

			/*
			// Don't delete if they have a UserLogin
			UserLogin userLogin = person.userLogin
			if (userLogin) {
				messages << "Staff '$person.firstName, $person.lastName', ignoring bulk delete because it is associated to a user login."
				logger.debug('Ignoring bulk delete of {} as it contains userLogin', id)
				skipped++
				continue
			}

			// Don't delete if they have assigned tasks
			def tasks = AssetComment.findAllByAssignedTo(person)
			if (tasks) {
				messages << "Staff '$person.firstName, $person.lastName', ignoring bulk delete because it contains tasks assigned."
				logger.debug('Ignoring bulk delete of {} as it contains tasks assigned', id)
				skipped++
				continue
			}

			Map map = [person:person]

			if (haveRelationship(person)) {
				messages << "Staff '$person.firstName, $person.lastName' unable to be deleted due to associations with existing elements in one or more Projects. Please use Person Merge functionality if applicable."
				skipped++
				continue
			}
			// Optionally don't delete if they are associated with Assets by AppOwner, SME or SME2
			def foundAssoc = false
			PERSON_DELETE_EXCEPTIONS_MAP.each { table, fields ->
				fields.each { column, status ->
					if (foundAssoc) {
						return
					}
					int count = jdbcTemplate.queryForObject('SELECT count(*) FROM ' + table + ' WHERE ' + column ")
					if (count) {
						if (deleteIfAssocWithAssets) {
							// Clear out the person's associate with all assets for the given column
							logger.debug 'Disassociated person as {}', column
							cleared += jdbcTemplate.update("UPDATE $table SET $column = NULL WHERE $column = '$person.id'")
						} else {
							logger.debug('Ignoring bulk delete of person {} {} as it contains {} association with asset(s)', id, person, column)
							messages << "Staff '$person.firstName, $person.lastName' unable to be deleted due it contains $column association with asset(s)."
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
			logger.info 'Bulk deleting person {} {}', id, person
			Person.executeUpdate("DELETE PartyRole p where p.party=:person", map)
			Person.executeUpdate("DELETE PartyRelationship p where p.partyIdFrom=:person or p.partyIdTo=:person", map)
			person.delete(flush:true)
			deleted++
			*/
		}

		return [deleted: deleted, skipped: skipped, cleared: cleared, messages: messages]
	}

	/**
	 * Assigns or unassigns a person to a move event for a specified team
	 * @param personId - the person id to assign to the project
	 * @param eventId - the id of the event to manage the assignment of
	 * @param teamCode - the team code for the assigment
	 * @param toAssign - indicates if the assignment should be added (1) or removed (0)
	 * @return A String that when blank indicates success otherwise will contain an error message
	 */
	@Transactional
	String assignToProjectEvent(personId, eventId, teamCode, toAssign) {
		securityService.requirePermission 'EditProjectStaff'

		String message = ""

		personId = NumberUtil.toPositiveLong(personId, -1)
		eventId = NumberUtil.toPositiveLong(eventId, -1)
		if (personId == -1 || eventId == -1) {
			return "The selected person and/or move event ids are invalid"
		}

		if (!['0', '1'].contains(toAssign)) {
			return "The action was not properly identified"
		}

		MoveEvent moveEvent = MoveEvent.get(eventId)
		Person person = Person.get(personId)
		if (!person || !moveEvent) {
			return "The selected person and/or move event were not found"
		}

		// Check that the individual that is attempting to assign someone has access to the project in the first place
		Project project = moveEvent.project
		if (!hasAccessToProject(project)) {
			securityService.reportViolation("attempted to modify staffing on project $project with proper access")
			return "You do not have access to the project specified"
		}

		// Now make sure that the person being assigned is affiliated with the project in some manor
		if (!getAvailableProjects(person, project)) {
			securityService.reportViolation("attempted to assign a person ($person.id) to project $project that is not affilated with")
			return "$person is not authorized to access the project"
		}

		if (toAssign) {

			// First add to the project for the team if not already
			addToProjectTeamSecured(project, person, teamCode)

			// Add the individual the the move event
			moveEventService.addTeamMember(moveEvent, person, teamCode)
			auditService.logMessage("assigned $person on team $teamCode of project $project.name event $moveEvent")

		}
		else {

			// Attempt to delete the team assignment on the event (assuming it exists)
			if (moveEventService.removeTeamMember(moveEvent, person, teamCode)) {
				auditService.logMessage("$securityService.currentUsername unassigned $person on team $teamCode of project $project.name event $moveEvent")
			}
		}

		return ''
	}

	/**
	 * Validates that a user has the permissions to edit Staffing that the person/project are accessible as well. This will
	 * validate and lookup values for project:project, person:person, teamRoleType:teamRoleType. If there are any problems it will
	 * throw the appropriate Exception.
	 * @param projectId - the id of the project to assign/remove a person from
	 * @param personId - the id of the person being editted
	 * @param teamCode - the code of the team the person to be assigned/removed
	 * @return A map containing the looked up project, person, teamRoleType
	 */
	private Map validateUserCanEditStaffing(projectId, personId, String teamCode) {
		securityService.requirePermission 'EditProjectStaff', false, "attempted to alter staffing for person $personId on project $projectId without permission"

		if (projectId && !NumberUtil.isPositiveLong(projectId)) {
			throw new InvalidParamException("Invalid Project Id was specified")
		}

		// Check if the person and events are not null
		if (!personId || !NumberUtil.isPositiveLong(personId)) {
			logger.debug 'validateUserCanEditStaffing() user {} called with missing or invalid params (personId:{}, projectId:{}, teamCode:{})',
					securityService.currentUsername, personId, projectId, teamCode
			throw new InvalidParamException("The person and event were not properly identified")
		}

		Project project = Project.get(projectId)
		if (!project) {
			logger.warn 'validateUserCanEditStaffing() user {} called with invalid project id {}', securityService.currentUsername, projectId
			throw new InvalidParamException("Invalid project specified")
		}

		Person person = Person.get(personId)
		if (!person) {
			logger.warn 'validateUserCanEditStaffing() user {} called with invalid person id {}', securityService.currentUsername, personId
			throw new InvalidParamException("Invalid person specified")
		}

		RoleType teamRoleType
		if (teamCode) {
			teamRoleType = RoleType.get(teamCode)
			if (!teamRoleType || !teamRoleType.isTeamRole()) {
				logger.warn 'assignToProject() user {} called with invalid team code {}', securityService.currentUsername, teamCode
				throw new InvalidParamException("The specified team code was invalid")
			}
		}

		// Check to see if the user should have access to the project
		if (!getAvailableProjects(securityService.userLoginPerson, project)) {
			securityService.reportViolation("Attempt to assign peson '$person' to project '$project' that the user is not associated with")
			throw new UnauthorizedException("You do not have access to the project and therefore can not do the assignment")
		}

		// Check to see that the project for person to be assigned is one that is available
		if (!getAvailableProjects(person, project)) {
			securityService.reportViolation("Attempt to assign person '$person' to project '$project' that person is not associated with")
			throw new UnauthorizedException("The person you are trying to assign to the project is not associated with the project")
		}

		[project: project, person: person, teamRoleType: teamRoleType]
	}

	/**
	 * Determine if the current user has the permission to access another person
	 * @param personToAccess - the Person to be accessed
	 * @param forEdit - flag that when set to true will validate the accessor has permission to edit
	 * @return true if the current user has access to the person otherwise false
	 */
	boolean hasAccessToPerson(Person personToAccess, boolean forEdit = false, boolean reportViolation = true)
			throws UnauthorizedException {

		//
		// TODO : JPM 3/2016 : hasAccessToPerson() presently does NOT work
		//
		boolean hasAccess = false
		List currentUserProjects = getAvailableProjects(securityService.userLoginPerson)*.id
		List personProjects = getAvailableProjects(personToAccess)*.id

		if (forEdit && !securityService.hasPermission('EditUserLogin')) {
			if (reportViolation) {
				reportViolation("attempted to edit person $personToAccess ($personToAccess.id) without permission")
			}
			return false
			// throw new UnauthorizedException('Do not have required permission to edit user')
		}

		if (currentUserProjects && personProjects) {
			for (int i = 0; i < personProjects.size(); i++) {
				if (currentUserProjects.contains(personProjects[i])) {
					hasAccess = true
					break
				}
			}
		}

		if (!hasAccess) {
			if (reportViolation) {
				securityService.reportViolation("attempted to access person $personToAccess ($personToAccess.id) without proper access")
			}
			throw new UnauthorizedException('Do not have access to specified user')
		}
	}

	/**
	 * Associate a person to a project for a given team code
	 * @param projectId - the id number of the project to assign the person to
	 * @param personId - the id of the person to assign to the project
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @param map - Used to load various data and errors to reference
	 */
	@Transactional
	void addToProjectTeam(def projectId, def personId, String teamCode, Map map = null) {
		// The addToEvent may call this method as well
		if (!map) {
			map = validateUserCanEditStaffing(projectId, personId, teamCode)
		}
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		addToProjectTeamSecured(map.project, map.person, teamCode)
	}

	/**
	 * Associate a person to a project as staff
	 * @param project - the project to assign the person to
	 * @param person - the person to assign update to the project
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 */
	@Transactional
	void addToProjectTeamSecured(Project project, Person person, String teamCode) {
		// Add to the project if not assiged already
		addToProjectSecured(project, person)

		// Add the person to the team at the company level
		addToTeam(person, teamCode)

		if (!isAssignedToProjectTeam(project, person, teamCode)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "PROJECT", person, teamCode)) {
				auditService.logMessage("$securityService.currentUsername assigned $person to project '$project.name' on team $teamCode")
			}
			else {
				throw new DomainUpdateException("An error occurred while trying to assign the person to the event")
			}
		}
		else {
			logger.warn 'addToProjectTeam() called for project {}, person {}, team {} but already exists', project, person, teamCode
		}
	}

	/**
	 * Assign a teamCode to a Person
	 * @param person - the person to assign the team to
	 * @param teamCode - the team code to associate to the person
	 */
	@Transactional
	void addToTeam(Person person, String teamCode) {
		if (!isAssignedToTeam(person, teamCode)) {
			if (partyRelationshipService.savePartyRelationship("STAFF", person.company, "COMPANY", person, teamCode)) {
				auditService.logMessage("$securityService.currentUsername assigned $person to team $teamCode")
			}
			else {
				throw new DomainUpdateException("An error occurred while trying to assign a person to a team")
			}
		}
	}

	/**
	 * Associate a person to a project as staff
	 * @param projectId - the id number of the project to add the person to
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void addToProject(String projectId, String personId) {
		// The addToEvent may call this method as well
		def map = validateUserCanEditStaffing(projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		addToProjectSecured(map.project, map.person)
	}

	/**
	 * Associate a person to a project as staff (Secured) which is only used if permissions were already checked
	 * if the
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	private void addToProjectSecured(Project project, Person person) {
		// Add to the project if not assiged already
		if (!isAssignedToProject(project, person)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "PROJECT", person, 'STAFF')) {
				auditService.logMessage("$securityService.currentUsername assigned $person to project $project.name as STAFF")
			}
			else {
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
	private int deleteFromEvent(Project project, MoveEvent event = null, Person person, RoleType team = null) {
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
	 * Lookup and validate that an event was properly referenced
	 * @param project - the project that the event should belong to
	 * @param eventId - the event id number to lookup
	 * @return the event object
	 */
	// TODO : JPM 11/2015 : Refactor lookupEvent into EventService
	private MoveEvent lookupEvent(Project project, def eventId) {
		if (!NumberUtil.isPositiveLong(eventId)) {
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
	 * Remove a person from a team on a project which will also clear out references to MoveEventStaff
	 * for the given team.
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 * TODO : JPM : 9/23/2015 - removeFromProject() doesn't look like it will work and doesn't appear to be used...
	 */
	String removeFromProjectTeam(String projectId, String personId, String teamCode) {
		Map map = validateUserCanEditStaffing(projectId, personId, teamCode)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		projectService.removeTeamMember(map.project, map.person, teamCode)

		auditService.logMessage("$securityService.currentUsername unassigned $map.person from team $teamCode of project $map.project.name")
	}

	/**
	 * Remove a person from a project as staff and also clear out various association that the individual may have
	 * When disassociating a person from the project there are a few things to be done:
	 * 	  1. Remove their association to the project in PartyRelationship for STAFF and any TEAM relations
	 * 	  2. Remove any TEAM assignments to Events
	 * 	  3. Remove Task assignments that the individual may have had
	 * 	  4. Remove references in the Application By properties (e.g. shutdownBy, startupBy)
	 * 	  5. For individuals that are NOT staff of the client, remove any association they have as app owner or SMEs
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	@Transactional
	String removeFromProject(String projectId, String personId) {
		Map map = validateUserCanEditStaffing(projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		Map metrics = [teamsUnassigned: 0, appOwnerUnassigned: 0, smeUnassigned: 0, sme2Unassigned: 0]

		// Remove all of the person's MoveEventStaff relationships for the project
		metrics.eventsUnassigned = deleteFromEvent(map.project, null, map.person, map.teamRoleType)

		// Remove the Project Staff relationship for the project
		List<RoleType> roles = partyRelationshipService.getProjectStaffFunctions(map.project.id, map.person.id)
		for (RoleType roe in roles) {
			if (role.type == RoleType.TEAM) {
				partyRelationshipService.deletePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, it.id)
				metrics.teamsUnassigned++
			}
		}
		role
		// Remove the person from the project
		PartyRelationship prProjectStaff = getProjectReference(map.project, map.person)
		if (prProjectStaff) {
			logger.debug 'removeFromProject() deleting PartyRelationship {}', prProjectStaff
			prProjectStaff.delete()
			metrics.staffUnassigned = 1
		}
		else {
			logger.warn 'removeFromProject() No Project Staff record found for project {} and person {}', projectId, personId
		}

		Map qparams = [project: map.project, person: map.person]

		// Now lets start clearing out the other references starting with Task assignments
		String sql = 'update AssetComment task set  task.assignedTo=null where task.project=:project and task.assignedTo=:person'
		metrics.tasksUnassigned = AssetComment.executeUpdate(sql, qparams)

		// Clears out the CURR_PROJ preference if it matches projectId and other project related preferences.
		UserLogin targetUserLogin = map.person.userLogin
		if (targetUserLogin) {
			String targetUserProjectId = userPreferenceService.getPreference(targetUserLogin, PREF.CURR_PROJ)
			if (targetUserProjectId == projectId) {
				userPreferenceService.removePreference(targetUserLogin, PREF.CURR_PROJ)
			}
		}

		qparams.person = map.person.id.toString()
		['shutdownBy', 'startupBy', 'testingBy'].each {
			sql = "update Application a set a.$it=null where a.project=:project and a.$it=:person"
			metrics[it + 'Unassigned'] = Application.executeUpdate(sql, qparams)
		}

		def employer = map.person.company
		// logger.debug 'removeFromProject() project={}, employer={} ({}), project client={} ({})', map.project.id, employer, employer.id, map.project.client, map.project.client.id)
		if (map.project.client.id != employer.id) {

			qparams.person = map.person

			sql = 'update AssetEntity a set a.appOwner=null where a.project=:project and a.appOwner=:person'
			metrics.appOwnerUnassigned = AssetEntity.executeUpdate(sql, qparams)

			['sme', 'sme2'].each {
				sql = "update Application a set a.$it=null where a.project=:project and a.$it=:person"
				metrics[it + 'Unassigned'] = Application.executeUpdate(sql, qparams)
			}
		}

		auditService.logMessage("$securityService.currentUsername unassigned $map.person from project $map.project.name - results $metrics")
	}

	/**
	 * Associate a person to a project as staff
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	@Transactional
	void addToEvent(def projectId, def eventId, def personId, String teamCode) {
		Map map = validateUserCanEditStaffing(projectId, personId, teamCode)

		// Add the Staff to the Project if not already assigned
		if (!isAssignedToProjectTeam(map.project, map.person, map.teamRoleType)) {
			addToProjectTeam(projectId, personId, teamCode, map)
		}

		MoveEvent event = lookupEvent(map.project, eventId)

		// Add the person to the Event if not already assigned
		if (!isAssignedToEventTeam(event, map.person, map.teamRoleType)) {
			MoveEventStaff mes = new MoveEventStaff([person: map.person, moveEvent: event, role: map.teamRoleType])
			save mes
			if (!mes.hasErrors()) {
				auditService.logMessage("$user assigned $map.person to project '$map.project.name' event '$event.name' as $teamCode")
			}
			else {
				logger.error 'addToEvent() Unable to save MoveEventStaff record for person {}, project {}, event {}, team {}',
						personId, projectId, eventId, teamCode
				throw new RuntimeException("Unable to save MoveEventStaff record : ${GormUtil.allErrorsString(mes)}")
			}
		}
		else {
			logger.warn 'addToEvent() called for project {}, person {}, team {} but already exists',
					map.project, map.person, map.teamRoleType
		}
	}

	/**
	 * Remove a person from a project as staff and also clear out references to MoveEventStaff
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @param teamCode - the role (aka team) to assign the person to the project/event as
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	@Transactional
	void removeFromEvent(String projectId, String eventId, String personId, String teamCode) {
		Map map = validateUserCanEditStaffing(projectId, personId, teamCode)

		MoveEvent event = lookupEvent(map.project, eventId)

		deleteFromEvent(map.project, event, map.person, map.teamRoleType)

		auditService.logMessage("$securityService.currentUsername unassigned $map.person from team $teamCode for event $event of project $map.project.name")
	}

	/**
	 * Determine if a person is assigned to a project as a STAFF member
	 * @param person - the person whom to check
	 * @param project - the project to check if user is assigned to
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToProject(Project project, Person person) {
		return (getProjectReference(project, person) != null)
	}

	/**
	 * Determine if a person is assigned to a project as a STAFF member
	 * @param person - the person whom to check
	 * @param project - the project to check if user is assigned to
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToTeam(Person person, String teamCode) {
		return (partyRelationshipService.getCompanyStaffFunctions(person.company, person, teamCode).size() > 0)
	}

	/**
	 * Determine if a person is assigned to a project as a STAFF member
	 * @param project - the project to check if user is assigned to
	 * @param person - the person whom to check
	 * @param teamCode - the team code
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToProjectTeam(Project project, Person person, teamCode) {
		def ref = getProjectTeamReference(project, person, teamCode)
		return (ref != null)
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
				eq("roleTypeCodeTo${teamCode instanceof RoleType ? '' : '.id'}", teamCode)
				eq('partyIdFrom', project)
				eq('partyIdTo', person)
			}
		}

		return teamRef
	}

	/**
	 * Determine if a person is assigned to an Event for a particular team role
	 * @param person - the person whom to check
	 * @param event - the event to check if user is assigned to
	 * @param teamCode - the team code
	 * @return The company whom the person is employeed
	 */
	boolean isAssignedToEventTeam(MoveEvent event, Person person, teamCode) {
		return getEventTeamReference(event, person, teamCode) != null
	}

	/**
	 * Retrieve an MoveEventStaff reference for a person assigned to an event with a give team code
	 * @param person - the person whom to check
	 * @param event - the event to check if user is assigned to
	 * @param teamCode - the team code
	 * @return The MoveEventStaff record if found otherwise null
	 */
	MoveEventStaff getEventTeamReference(MoveEvent event, Person person, teamCode) {
		assert person != null
		assert event != null

		// Lookup the RoleType(teamCode) if not already a RoleTeam
		if (!(teamCode instanceof RoleType)) {
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
	 * Get the projects that the given person is assigned to
	 * @param person - the person whom to lookup assigned projects
	 * @param project - if supplied will filter the results to just the one project (optional)
	 * @return The list of projects
	 */
	List<Project> getAssignedProjects(Person person, Project project = null) {
		assert person != null

		PartyRelationship.createCriteria().list {
			eq('partyRelationshipType', PartyRelationshipType.load('PROJ_STAFF'))
			and {
				eq('roleTypeCodeFrom', RoleType.load('PROJECT'))
				eq('partyIdTo', person)
				if (project) {
					eq('partyIdFrom', project)
				}
			}
			projections {
				groupProperty('partyIdFrom')
			}
		}
	}

	/**
	 * Get the projects that the given person could be associated with
	 * @param person - the person whom to lookup the projects for
	 * @param project - if supplied will filter the results to just the one project (optional)
	 * @return The list of projects
	 */
	List<Project> getAvailableProjects(Person person, Project project = null, boolean excludeAssigned = false, Date cutoff = null) {
		assert person != null

		PartyGroup employer = person.company
		List projects = partyRelationshipService.companyProjects(employer, project)

		//logger.debug 'getAvailableProjects() person {} ({}), employer {}({}), # projects {}', person, person.id, employer, employer.id, projects?.size()
		//logger.debug 'getAvailableProjects() list 1: {}', projects*.id

		// Optionally remove the assigned projects
		if (excludeAssigned) {
			List assignedProjects = getAssignedProjects(person, project)
			if (assignedProjects) {
				projects = projects - assignedProjects
			}
		}
		// logger.debug 'getAvailableProjects() list 2: {}', projects*.id
		// Optionally remove the projects by completion date cutoff
		if (cutoff) {
			projects = projects.findAll { it.completionDate >= cutoff }
		}

		// Sort the list descending by completion date
		// projects.sort{ a, b -> a.completionDate < b.completionDate }

		// Filter down to just the one project if the request was for just the one
		if (project && projects) {
			def theProject = projects.find { it.id == project.id }
			projects = (theProject ? [theProject] : [])
			// logger.debug 'getAvailableProjects() list 3: {}', projects*.id
		}

		projects.sort { it.name }
	}

	/**
	 * Determine if the person is associated with the project
	 * @param person - the person to check
	 * @param project - the project to see if person has access to
	 * @return true if person is associated otherwise false
	 */
	boolean hasAccessToProject(Person person = null, Project project) {
		long personId = person ? person.id : securityService.currentPersonId
		def projects = projectService.getUserProjects(false, ProjectStatus.ANY, [personId: personId])
		if (projects) {
			return projects.any { it.id == project.id }
		}
	}

	/**
	 * Validate that the user can access a person and will respond with appropriate
	 * HTTP responses based on access constraints (e.g. Unauthorized or Not Found)
	 * @param personId - the id of the person to access
	 * @return Person - the person if can access or null
	 */
	Person validatePersonAccess(personId) throws UnauthorizedException, InvalidParamException, EmptyResultException {

		if (!NumberUtil.isPositiveLong(personId)) {
			throw new InvalidParamException('Invalid person id requested')
		}

		// If not edit own account, the user must have privilege to edit the account
		boolean editSelf = NumberUtil.toLong(personId) == securityService.currentPersonId
		if (!editSelf) {
			securityService.requirePermission('PersonEditView', false,
				"$securityService.currentUsername attempted to edit Person($personId) without necessary permission")
		}

		if (!editSelf) {
			// TODO : JPM 5/2015 : Need to make sure showing/editing someone that the user has access to
		}

		Person person = Person.get(personId)
		if (!person) {
			throw new EmptyResultException()
		}

		return person
	}

	/**
	 * Used by controller update an actual Person and possibly UserLogin. The logic works for cases where user updating their own
	 * account as well as an administrator updating others. In the case of the latter, there are more things that can be updated.
	 * @param params - request params
	 * @param byAdmin - Flag indicating that it is being done by the admin form (default false)
	 * @return The Person record being updated or throws an exception for various issues
	 */
	@Transactional
	Person updatePerson(Map params, String tzId, boolean byAdmin = false)
			throws DomainUpdateException, UnauthorizedException, InvalidParamException, EmptyResultException {
		Person person = validatePersonAccess(params.id)
		if (!isAssociatedTo(securityService.userLoginPerson, person.company)) {
			throw new UnauthorizedException("You do not have permission to manage staffing for the user's company")
		}

		params.travelOK == "1" ? params : (params.travelOK = 0)

		if (!person.staffType && !params.staffType) {
			params.staffType = 'Hourly'
		}

		// TODO : JPM 8/31/2015 : Replace person.properties = params with proper field assignments
		person.properties = params

		Project project = securityService.userCurrentProject

		Map nameMap = [first: person.firstName, middle: person.middleName, last: person.lastName]
		Map findPersonInfo = findPerson(nameMap, project, null, false)
		boolean isPersonAmbiguous = findPersonInfo.isAmbiguous && findPersonInfo.person?.id != person.id
		if (isPersonAmbiguous) {
			logger.error 'updatePerson() unable to save {} because of conflicting name.', person
			throw new DomainUpdateException("The name of the person is ambiguous.")
		}

		save person
		if (person.hasErrors()) {
			throw new DomainUpdateException('An error occurred while attempting to save person changes')
		}

		UserLogin userLogin = securityService.getPersonUserLogin(person)
		if (userLogin) {
			if (params.newPassword) {
				securityService.validateAllowedToChangePassword(userLogin, byAdmin)

				if (!byAdmin) {
					// Verify that the user entered their old password correctly
					if (!params.oldPassword) {
						throw new InvalidParamException('The old password is required')
					}

					if (!userLogin.comparePassword(params.oldPassword, true)) {
						throw new InvalidParamException('Old password entered does not match the existing password')
					}

					// Verify that the password isn't being changed too often
					if (!securityService.verifyMinPeriodToChangePswd(userLogin)) {
						throw new DomainUpdateException('Minimum period for changing your password has not been met')
					}
				}
				securityService.setUserLoginPassword(userLogin, params.newPassword)
			}

			if (byAdmin && params.expiryDate && params.expiryDate != "null") {
				userLogin.expiryDate = TimeUtil.parseDate(params.expiryDate)
			}

			// When Disabling Person - disable UserLogin
			// When enabling Person - do NOT change UserLogin
			if (person.active == 'N') {
				userLogin.active = 'N'
			}

			save userLogin
			if (userLogin.hasErrors()) {
				throw new DomainUpdateException('An error occurred while attempting to update the user changes')
			}

			if (params.newPassword) {
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
			List<Date> personExpDates = params.list("availability").collect { TimeUtil.parseDate(it) }
			if (personExpDates) {
				ExceptionDates.executeUpdate('DELETE FROM ExceptionDates WHERE person = :person AND exceptionDay NOT IN (:dates)',
						[person: person, dates: personExpDates])
				for (Date presentExpDate in personExpDates) {
					def exp = ExceptionDates.findByExceptionDayAndPerson(presentExpDate, person)
					if (!exp) {
						def expDates = new ExceptionDates(exceptionDay: presentExpDate, person: person)
						save expDates
						if (expDates.hasErrors()) {
							throw new DomainUpdateException('An error occurred while attempting to save exception dates')
						}
					}
				}
			}
			else {
				ExceptionDates.executeUpdate('DELETE FROM ExceptionDates WHERE person = :person', [person: person])
			}
		}

		if (!byAdmin) {
			// Save some preferences
			if (params.timeZone) {
				userPreferenceService.setTimeZone params.timeZone
			}

			if (params.powerType) {
				userPreferenceService.setPreference(PREF.CURR_POWER_TYPE, params.powerType)
			}

			if (params.startPage) {
				userPreferenceService.setPreference(PREF.START_PAGE, params.startPage)
			}
		}

		return person
	}

	/**
	 * Used by controller to create a Person.
	 * @param params - request params
	 * @param companyId - The person company
	 * @param defaultProject - this is the current user's currentProject that the person will be assigned to if the company is the project.client
	 * @param byAdmin - Flag indicating that it is being done by the admin form (default false)
	 * @return The Person record being created or throws an exception for various issues
	 */
	@Transactional
	Person savePerson(Map params, Long companyId, Project defaultProject, boolean byAdmin = false)
			throws DomainUpdateException, InvalidParamException {

		def companyParty
		def person

		// Look to allow easy breakout for exceptions
		while (true) {
			if (companyId != null) {
				companyParty = Party.get(companyId)
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
						((StringUtils.isEmpty(params.lastName) && StringUtils.isEmpty(it.lastName)) ||
						  StringUtils.equalsIgnoreCase(it.lastName, params.lastName)) &&
						((StringUtils.isEmpty(params.middleName) && StringUtils.isEmpty(it.middleName)) ||
						  StringUtils.equalsIgnoreCase(it.middleName, params.middleName))
			}

			if (person != null) {
				throw new DomainUpdateException("A person with that name already exists. Person Id:$person.id")
			}
			else {
				// Create the person and relationship appropriately
				def reducedParams = [:] + params
				reducedParams.remove('company')
				reducedParams.remove('function')

				person = new Person(reducedParams)
				save person
				if (!person.hasErrors()) {
					// Assign the person to the company
					partyRelationshipService.addCompanyStaff(companyParty, person)

					def teamCodes = CollectionUtils.asList(params.containsKey('function') ? params.function : [])
					if (teamCodes) {
						// Assign the person to the appropriate teams
						partyRelationshipService.updateAssignedTeams(person, teamCodes)
					}

					// If the byUser's current project.client is the same as the new person's company then we'll
					// automatically assign the person to the project as well
					if (defaultProject != null && defaultProject.client.id == companyId) {
						if (teamCodes) {
							teamCodes.each { tc ->
								addToProjectTeamSecured(defaultProject, person, tc)
							}
						}
						else {
							// Add the person to the project which is done automatically when adding the team to the project
							addToProjectSecured(defaultProject, person)
						}
					}
				}
				else {
					throw new DomainUpdateException("Unable to create person. $person${GormUtil.allErrorsString(person)}.")
				}
			}
			break
		}

		return person
	}

	/**
	 * Get the team RoleType associated with a person
	 * @param person - the person to lookup the team codes for
	 * @return a list of the teams
	 */
	List<RoleType> getPersonTeamRoleTypes(Person person) {
		partyRelationshipService.getCompanyStaffFunctions(person.company, person)
	}

	/**
	 * Get the team codes associated with a person
	 * @param person - the person to lookup the team codes for
	 * @return a list of the teams
	 */
	List<String> getPersonTeamCodes(Person person) {
		getPersonTeamRoleTypes(person)*.id
	}

	/**
	 * Determines if a person is associated to a company
	 * @param Person trying to access the company
	 * @param Company the person is trying to access
	 */
	boolean isAssociatedTo(Person whom, Party company) {
		partyRelationshipService.associatedCompanies(whom).find { it.id == company.id }
	}

	Person findByUsername(String username) {
		Person.executeQuery('select u.person from UserLogin u where u.username=:username',
			[username: username])[0]
	}
}
