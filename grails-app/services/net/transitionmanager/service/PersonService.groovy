package net.transitionmanager.service

import net.transitionmanager.asset.Application
import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetEntity
import com.tdsops.common.builder.UserAuditBuilder
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.PersonCO
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventStaff
import net.transitionmanager.party.Party
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.party.PartyRelationshipType
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.Permission
import org.apache.commons.lang3.StringUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
/**
 * Provides a number of functions to help in the management and access of Person objects.
 */
class PersonService implements ServiceMethods {

	def auditService
	def jdbcTemplate
	def moveEventService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	def partyRelationshipService
	def projectService
	def userPreferenceService

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

	// A list of the Domain class properties that perform cross references to prevent deletions
	private static final Map<String, Map<String, Boolean>> PERSON_DELETE_EXCEPTIONS_MAP = [
		application: [sme_id: true, sme2_id: true],
		asset_entity: [app_owner_id: true]
	]

	// The Person properties that are updated by the Person Merge functionality
	private static final List<String> MERGE_PERSON_DOMAIN_PROPERTIES = [
		'firstName', 'middleName', 'lastName', 'nickName', 'title', 'email',
		'department', 'location', 'stateProv', 'country',
		'workPhone', 'mobilePhone',
		'personImageURL',
		'tdsNote', 'tdsLink', 'keyWords', 'travelOK'
	]

	/* ***************************
	private static final List<String> notToUpdate = [
		'beforeDelete', 'beforeInsert', 'beforeUpdate',
		'blackOutDates', 'firstName', 'id']
	*/

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

		StringBuilder query = new StringBuilder('SELECT party_id_to_id as id FROM party_relationship pr')
		query.append(' JOIN person p ON p.person_id=pr.party_id_to_id')
		query.append(' WHERE pr.party_id_from_id=:company')
		query.append(' AND pr.role_type_code_from_id="ROLE_COMPANY"')
		query.append(' AND pr.role_type_code_to_id="ROLE_STAFF"')
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
		if (! (nameMap.containsKey('first') && nameMap.containsKey('middle') && nameMap.containsKey('last')) ) {
			throw new InvalidParamException("Invalid nameMap object $nameMap")
		}

		// TM-7169 Added test to prevent searching if missing the required first name at a minimum
		if (! nameMap.first) {
			throw new InvalidParamException('User has no first name associated with account')
		}

		List persons = []
		Map queryParams = [company: company]
		def (first, middle, last) = [false, false, false]

		StringBuilder select = new StringBuilder("select pr.partyIdTo from PartyRelationship pr")
		select.append(" where pr.partyIdFrom = :company")
		select.append(" and pr.roleTypeCodeFrom.id = 'ROLE_COMPANY'")
		select.append(" and pr.roleTypeCodeTo = 'ROLE_STAFF'")

		StringBuilder query1 = new StringBuilder(select)
		if (nameMap.first) {
			queryParams.first = nameMap.first
			query1.append(" AND pr.partyIdTo.firstName=:first")
			first = true
		}
		if (nameMap.middle) {
			queryParams.middle = nameMap.middle
			query1.append(" AND pr.partyIdTo.middleName=:middle")
			middle = true
		}
		if (nameMap.last) {
			queryParams.last = nameMap.last
			query1.append(" AND pr.partyIdTo.lastName=:last")
			last = true
		}

		StringBuilder query2
		if (first && middle && last) {
			// Try and find individuals with just first and last, middle not set
			query2 = new StringBuilder(select)
			query2.append(" AND pr.partyIdTo.firstName=:first AND pr.partyIdTo.lastName=:last")
			query2.append(" AND pr.partyIdTo.middleName is null")
		}
		StringBuilder query3
		if (first) {
			// Try and find individuals with just first, middle and last not set
			query3 = new StringBuilder(select)
			query3.append(" AND pr.partyIdTo.firstName=:first")
			query3.append(" AND pr.partyIdTo.lastName is null")
			query3.append(" AND pr.partyIdTo.middleName is null")
		}
		 log.debug "findByCompanyAndName() Query = ${query.toString()}"
		persons = PartyRelationship.executeQuery(query1.toString(), queryParams)

		if (query2) {
			queryParams.remove("middle")
			persons.addAll(PartyRelationship.executeQuery(query2.toString(), queryParams))
		}
		if (query3) {
			queryParams.remove("last")
			persons.addAll(PartyRelationship.executeQuery(query3.toString(), queryParams))
		}
		persons = persons.unique()

		return persons
	}

	/**
	 * Find a list of persons associated as Staff to a given company, queried by company and person email.
	 * @param company - The company that the person would be associated as Staff
	 * @param email - Person email
	 * @return A list of the person(s) found that match the filter or an empty list if none found
	 */
	List<Person> findByCompanyAndEmail(PartyGroup company, String email) {
		List<Person> persons = []
		if (email) {
			Map args = [company: company, email: email]
			persons = PartyRelationship.executeQuery("select pr.partyIdTo from PartyRelationship pr " +
					"where pr.partyIdFrom = :company " +
					"and pr.roleTypeCodeFrom.id = 'ROLE_COMPANY' " +
					"and pr.roleTypeCodeTo = 'ROLE_STAFF' " +
					"and pr.partyIdTo.email = :email", args)
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
	Map findPerson(String name, Project project, def staffList = null, def clientStaffOnly = true, boolean checkAmbiguity = false) {
		Map map = parseName(name)
		if (map) {
			map = findPerson(map, project, staffList, clientStaffOnly, checkAmbiguity)
		}
		log.debug 'findPerson(String) results={}', map
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
	 * found then isAmbiguous will be set to true and person will be null.
	 */
	Map findPerson(Map nameMap, Project project, List staffList = null, boolean clientStaffOnly = true, boolean checkAmbiguity = false) {
		String mn = 'findPerson()'
		Map results = [person: null, isAmbiguous: false, partial: false]

		log.debug 'findPersion() attempting to find nameMap={} in project {}', nameMap, project

		// Make sure we have a person
		if (!nameMap || !nameMap.containsKey('first')) {
			results.isAmbiguous = true
			return results
		}

		String hql = "from PartyRelationship PR inner join PR.partyIdTo P where PR.partyRelationshipType.id='STAFF' " +
			  "and PR.roleTypeCodeFrom.id='ROLE_COMPANY' and PR.roleTypeCodeTo.id='ROLE_STAFF' and PR.partyIdFrom IN (:companies)"

		List companies = [project.client]

		String where = ' and P.firstName=:firstName'
		String lastName = lastNameWithSuffix(nameMap)
		Map<String, Object> queryParams = [companies: companies, firstName: nameMap.first]

		if (lastName || checkAmbiguity) {
			where += " AND P.lastName=:lastName"
			queryParams.lastName = lastName ?: ''
		}
		if (nameMap.middle || checkAmbiguity) {
			where += " AND P.middleName=:middleName"
			queryParams.middleName = nameMap.middle ?: ''
		}

		// If the flag is false, we also need to look for the partners and owner staff.
		if (!clientStaffOnly) {
			queryParams.companies = partyRelationshipService.getProjectCompanies(project)
		}

		// Try finding the person with an exact match
		List persons = Person.executeQuery(hql + where, queryParams)
		if (persons) {
			persons = persons.collect({ it[1] })
		}
		log.debug '{} findPerson() Initial search found {} {}', mn, persons.size(), nameMap

		int s = persons.size()
		if (s > 1) {
			persons.each { person -> log.debug '{} person {} {}', mn, person.id, person }
			// results.person = persons[0]
			results.isAmbiguous = true
		} else if (s == 1) {
			results.person = persons[0]
		} else {

			// Try to find match on partial

			// Closure to construct the where and queryParams used below
			def addQueryParam = { name, value ->
				if (!StringUtil.isBlank(value)) {
					where += " and P.$name=:$name"
					queryParams[name] = value
				}
			}

			where = ''
			queryParams = [companies: queryParams.companies]
			addQueryParam('firstName', nameMap.first)
			addQueryParam('middleName', nameMap.middle)
			addQueryParam('lastName', lastName)

			log.debug '{} partial search using {}', mn, queryParams
			persons = Person.findAll(hql + where, queryParams)
			if (persons) {
				persons = persons.collect({ it[1] })
			}
			log.debug '{} partial search found {}', mn, persons.size()

			s = persons.size()
			if (s > 1) {
				results.isAmbiguous = true
				results.partial = true
			} else if (s == 1) {
				results.person = persons[0]
				results.isAmbiguous = (StringUtil.isBlank(lastName) && !StringUtil.isBlank(results.person.lastName))
				results.partial = true
			}
		}

		log.debug '{} results={}', mn, results
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
	@Transactional
	Map findOrCreatePerson(String name, Project project, List<Person> staffList = null, boolean clientStaffOnly = true) {
		def nameMap = parseName(name)
		if (nameMap == null) {
			log.error 'findOrCreatePersonByName() unable to parse name ({})', name
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
	@Transactional
	Map findOrCreatePerson(Map nameMap, Project project, List staffList = null, boolean clientStaffOnly = true) {
		Person person
		boolean staffListSupplied = (staffList != null)
		try {

			def results = findPerson(nameMap, project, staffList, clientStaffOnly)
			results.isNew = null

			if (!results.person && nameMap.first) {
				log.info 'findOrCreatePerson() Creating new person ({}) as Staff for {}', nameMap, project.client
				person = new Person('firstName': nameMap.first, 'lastName': nameMap.last, 'middleName': nameMap.middle, staffType: 'Salary')
				save(person, true)
				if (person.hasErrors()) {
					results.error = "Unable to create person $nameMap${GormUtil.allErrorsString(person)}"
				} else {
					if (!partyRelationshipService.addCompanyStaff(project.client, person)) {
						results.error = "Unable to assign person $results.person as staff"
						// TODO - JPM (10/13) do we really want to proceed if we can't assign the person as staff otherwise they'll be in limbo.
					}
					results.person = person
					results.isNew = true
				}

				// Associate a person to a project as staff
				addToProject(securityService.getUserLogin(), project.id as String, person.id as String)
			} else {
				results.isNew = false
			}

			return results
		} catch (e) {
			String exMsg = e.message
			log.error 'findOrCreatePerson() received exception for nameMap={} : {}\n{}',
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
				log.error 'parseName("{}") encountered multiple commas that is not handled', name
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

	/**
	 * Called from the Person Merge form which will update all of the properties for the person referenced by
	 * the toId parameter and then will merge each of the persons referenced in the fromId List parameter.
	 */
	@Transactional
	String processMergePersonRequest(UserLogin byWhom, PersonCO cmdObj, params) {
		Person toPerson = Person.get(params.long('toId'))

		hasAccessToPerson(byWhom.person, toPerson, true, true)

		List fromPersons = params.list("fromId[]")
		List personMerged = []

		// toPerson.properties = cmdObj
		cmdObj.populateDomain(toPerson, true, ['constraintsMap'])
		if (!toPerson.save(flush:true, failOnError: false)) {
			throw new DomainUpdateException('Unable to update person ' + GormUtil.allErrorsString(toPerson))
			toPerson.errors.allErrors.each{ println it }
		}

		for (personId in fromPersons) {
			Person fromPerson = Person.get(personId)
			hasAccessToPerson(byWhom.person, fromPerson, true, true)
			personMerged << fromPerson
			mergePerson(byWhom, fromPerson, toPerson)
		}

		String results
		int size = personMerged.size()
		if (size) {
			results = "${personMerged.join(', ')} ${size==1 ? 'was' : 'where'} merged to ${toPerson}"
		} else {
			results = 'No merges were performed'
		}
		results
	}

	/**
	 * Used to merge one person into another account which will move all of the teams, security roles, user account, and references
	 * of the fromPerson and assign to the toPerson. This will be responsible for deleting any duplicate references as well.
	 * @param fromPerson
	 * @param toPerson
	 */
	@Transactional
	void mergePerson(UserLogin byWhom, Person fromPerson, Person toPerson) {
		//
		// Perform some validation before performing the merge
		//
		if (fromPerson.isSystemUser() || toPerson.isSystemUser()) {
			throw new InvalidParamException('Merging with system accounts is prohibited')
		}

		if (fromPerson.id == toPerson.id) {
			throw new InvalidParamException('The To and From persons must be different')
		}

		def toCompany = toPerson.getCompany()
		def fromCompany = fromPerson.getCompany()
		if (toCompany.id != fromCompany.id) {
			throw new InvalidParamException('Merging people is only allowed within the same company')
		}

		// TODO : JPM 8/2016 : Change mergePerson to validate if the user has access to the persons being merged

		auditService.logMessage("$byWhom is merging $fromPerson (${fromPerson.id}) to $toPerson (${toPerson.id})")

		// Merge the UserLogin accounts and security rolls associated with them
		securityService.mergePersonsUserLogin(byWhom, fromPerson, toPerson)
		// Merge the references and delete the fromPerson when finished
		GormUtil.mergeDomainReferences(fromPerson, toPerson, true)

		GormUtil.flushAndClearSession()
	}

	/**
	 * This method deletes a person and other entities related to the instance such as partyRelationships,
	 * UserLogin, etc.  This version of the method is called from untrusted/non-validated consumers of the service.
	 * @param byWhom - the User that invoked the deleting of the person
	 * @param person - Person Instance to be deleted
	 * @param deleteIfUserLogin - boolean that indicates if a person with existing UserLogin must be deleted.
	 * @param deleteIfAssocWithAssets - boolean that indicates if a person with relationships with assets must
	 * 		be deleted (see PERSON_DELETE_EXCEPTIONS_MAP)
	 * @return A map containing the following:
	 *		messages: String[] containing errors and other messages
	 *		cleared: the number of assets cleared
	 *		deleted: a boolean indicating if the person was deleted
	 */
	@Transactional
	Map deletePerson(Person byWhom, Person person, boolean deleteIfUserLogin, boolean deleteIfAssocWithAssets) {
		validatePersonAccess(person.id, byWhom)

		return deletePersonSecure(person, deleteIfUserLogin, deleteIfAssocWithAssets)
	}

	/**
	 * This method deletes a person and other entities related to the instance such as partyRelationships,
	 * UserLogin, etc.  This version of the method is called from trusted/validated consumers of the service.
	 * @param person - Person Instance to be deleted
	 * @param deleteIfUserLogin - boolean that indicates if a person with existing UserLogin must be deleted.
	 * @param deleteIfAssocWithAssets - boolean that indicates if a person with relationships with assets must
	 * 		be deleted (see PERSON_DELETE_EXCEPTIONS_MAP)
	 * @return A map containing the following:
	 *		messages: String[] containing errors and other messages
	 *		cleared: the number of assets cleared
	 *		deleted: a boolean indicating if the person was deleted
	 */
	@Transactional
	Map deletePersonSecure(Person person, boolean deleteIfUserLogin, boolean deleteIfAssocWithAssets){

		int cleared = 0
		boolean deleted = false
		def messages = []

		UserLogin userLogin = person.userLogin

		// Determine if person can be deleted based on if there are key referenced or if there is a user account for the person
		boolean isDeletable = true
		if (! deleteIfAssocWithAssets && hasKeyReferences(person)) {
			messages << "$person was unable to be deleted due to being associated with assets"
			isDeletable = false
		}
		if (userLogin && !deleteIfUserLogin) {
			messages << "$person was unable to be deleted due to having an associated UserLogin account"
			isDeletable = false
		} else if (userLogin == securityService.userLogin) {
			messages << "You cannot delete your own user login account while logged in with it"
			isDeletable = false
		}
		if (person.isSystemUser()) {
			messages << "$person is a system account and can not be deleted"
			isDeletable = false
		}
		if (isDeletable) {
			Person.withTransaction { trxStatus ->

				try {
					// Delete the UserLogin
					if (userLogin) {
						person.userLogin = null
						securityService.deleteUserLogin(userLogin)
					}

					def (deletedCount, clearedCount) = GormUtil.deleteOrNullDomainReferences(person, true)
					deleted = deletedCount
					cleared = clearedCount

				} catch(Exception e) {
					messages << "An error occurred while attempting to delete $person"
					log.error ExceptionUtil.stackTraceToString("Attempted to delete person $person (${person.id}", e)
					trxStatus.setRollbackOnly()
				}
			}
		}
		return [messages: messages, cleared: cleared, deleted: deleted]
	}

	/**
	 * Check if the Person has any key relationships (e.g. Application owner or SME) that could prevent it from being deleted
	 * @param person - Person to check
	 * @return true if a relationship exist otherwise false
	 */
	boolean hasKeyReferences(Person person) {
		boolean hasRef = false
		PERSON_DELETE_EXCEPTIONS_MAP.each { table, columns ->
			if (!hasRef) {
				StringBuilder sb = new StringBuilder("SELECT count(*) AS count FROM ${table} WHERE ")
				boolean first=true
				columns.each { col, delete ->
					if (delete){
						if (first) {
							first=false
						} else {
							sb.append(' OR ')
						}
						sb.append("$col = ${person.id}")
						String sql = sb.toString()
						int count = jdbcTemplate.queryForObject(sql, Integer.class)
						hasRef = (count > 0)
					}
				}
			}
		}

		return hasRef
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
	Map bulkDelete(Person byWhom, List ids, Boolean deleteIfAssocWithAssets) {
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
				try {
					Person person = validatePersonAccess(id, byWhom)
					if (person) {
						// Deletes the person and other related entities.
						Map deleteResultMap = deletePerson(byWhom, person, false, deleteIfAssocWithAssets)
						// Updates variables that comput different results.
						cleared += deleteResultMap.cleared
						if (deleteResultMap.deleted) {
							deleted++
						} else {
							skipped++
						}
						log.info("bulkDelete() ${deleteResultMap["messages"]}")
						messages.addAll(deleteResultMap["messages"])

					} else {
						messages << 'Invalid ID(s) were submitted in the request'
					}
				} catch (UnauthorizedException ue) {
					securityService.reportViolation("attempted to delete person ($id) without neccessary access", byWhom.userLogin.username)
					messages << "You do not have the required access to delete the specified person"
				} catch (InvalidParamException ipe) {
					log.error "bulkDelete() was invoked with invalid id ($id) value by $byWhom"
					messages << "One of the parameters specified was invalid and an error was logged."
				} catch (EmptyResultException ere) {
					securityService.reportViolation("attempted to delete a non-existent person ($id)", byWhom.userLogin.username)
					messages << "Specified person was not found"
				}
			}
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
		securityService.requirePermission Permission.ProjectStaffEdit

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
		if (! moveEvent) {
			return 'The specified event was not found'
		}

		Person person = Person.get(personId)
		if (!person) {
			return "The specified person was not found"
		}

		// Check that the individual that is attempting to assign someone has access to the project in the first place
		Project project = moveEvent.project
		if (!hasAccessToProject(person, project)) {
			securityService.reportViolation("attempted to modify staffing on project $project with proper access")
			return "$person does not have access to the project specified"
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
		UserLogin user = securityService.userLogin
		return validateUserCanEditStaffing(user, projectId, personId, teamCode)
	}

	/**
	 * Validates that a user has the permissions to edit Staffing that the person/project are accessible as well. This will
	 * validate and lookup values for project:project, person:person, teamRoleType:teamRoleType. If there are any problems it will
	 * throw the appropriate Exception.
	 * @param user - the user whom is trying to edit the staff
	 * @param projectId - the id of the project to assign/remove a person from
	 * @param personId - the id of the person being editted
	 * @param teamCode - the code of the team the person to be assigned/removed
	 * @return A map containing the looked up project, person, teamRoleType
	 */
	private Map validateUserCanEditStaffing(UserLogin user, projectId, personId, String teamCode) {
		if (!securityService.hasPermission(user, Permission.ProjectStaffEdit)) {
			throw new UnauthorizedException("Do not have permission to edit staff")
		}

		if (projectId && !NumberUtil.isPositiveLong(projectId)) {
			throw new InvalidParamException("Invalid Project Id was specified")
		}

		// Check if the person and events are not null
		if (!personId || !NumberUtil.isPositiveLong(personId)) {
			log.debug 'validateUserCanEditStaffing() user {} called with missing or invalid params (personId:{}, projectId:{}, teamCode:{})',
					securityService.currentUsername, personId, projectId, teamCode
			throw new InvalidParamException("The person and event were not properly identified")
		}

		Project project = Project.get(projectId)
		if (!project) {
			log.warn 'validateUserCanEditStaffing() user {} called with invalid project id {}', securityService.currentUsername, projectId
			throw new InvalidParamException("Invalid project specified")
		}

		Person person = Person.get(personId)
		if (!person) {
			log.warn 'validateUserCanEditStaffing() user {} called with invalid person id {}', securityService.currentUsername, personId
			throw new InvalidParamException("Invalid person specified")
		}

		RoleType teamRoleType
		if (teamCode) {
			teamRoleType = RoleType.get(teamCode)
			if (!teamRoleType || !teamRoleType.isTeamRole()) {
				log.warn 'assignToProject() user {} called with invalid team code {}', securityService.currentUsername, teamCode
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
		hasAccessToPerson(securityService.userLoginPerson, personToAccess, forEdit, reportViolation)
	}

	/**
	 * Determine if the current user has the permission to access another person
	 * @param personToAccess - the Person to be accessed
	 * @param forEdit - flag that when set to true will validate the accessor has permission to edit
	 * @return true if the current user has access to the person otherwise false
	 */
	boolean hasAccessToPerson(Person byWhom, Person personToAccess, boolean forEdit = false, boolean reportViolation = true)
			throws UnauthorizedException {

		//
		// TODO : JPM 3/2016 : hasAccessToPerson() presently does NOT work
		//
		boolean hasAccess = false
		List currentUserProjects = getAvailableProjects(byWhom)*.id
		List personProjects = getAvailableProjects(personToAccess)*.id

		if (forEdit && !securityService.hasPermission(Permission.UserEdit)) {
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
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "ROLE_PROJECT", person, teamCode)) {
				auditService.logMessage("$securityService.currentUsername assigned $person to project '$project.name' on team $teamCode")
			}
			else {
				throw new DomainUpdateException("An error occurred while trying to assign the person to the event")
			}
		}
		else {
			log.warn 'addToProjectTeam() called for project {}, person {}, team {} but already exists', project, person, teamCode
		}
	}

	/**
	 * Assign a teamCode to a Person
	 * @param person - the person to assign the team to
	 * @param teamCode - the team code to associate to the person
	 */
	void addToTeam(Person person, String teamCode) {
		if (!isAssignedToTeam(person, teamCode)) {
			if (partyRelationshipService.savePartyRelationship("STAFF", person.company, "ROLE_COMPANY", person, teamCode)) {
				auditService.logMessage("$securityService.currentUsername assigned $person to team $teamCode")
			}
			else {
				throw new DomainUpdateException("An error occurred while trying to assign a person to a team")
			}
		}
	}

	/**
	 * Associate a person to a project as staff
	 * @param byWhom - The person that is performing the update
	 * @param projectId - the id number of the project to add the person to
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means succes
	 */
	void addToProject(UserLogin byWhom, String projectId, String personId) {
		// Check that the individual that is attempting to assign someone has access to the project in the first place
		Project project = Project.get(projectId)
		if (! hasAccessToProject(byWhom.person, project)) {
			auditService.logSecurityViolation(byWhom.username, "attempted to modify staffing on project $project with proper access")
			throw new UnauthorizedException('You do not have access to the specified project')
		}

		// The addToEvent may call this method as well
		def map = validateUserCanEditStaffing(projectId, personId, null)
		if (map.error) {
			throw new InvalidRequestException(map.error)
		}

		addToProjectSecured(map.project, map.person)
	}

	/**
	 * Associate a person to a project as staff (Secured) which is only used if permissions were already checked
	 * @param projectId - the id number of the project to remove the person from
	 * @param personId - the id of the person to update
	 * @return String - any value indicates an error otherwise blank means success
	 */
	private void addToProjectSecured(Project project, Person person) {
		// Add to the project if not assigned already
		if (!isAssignedToProject(project, person)) {
			if (partyRelationshipService.savePartyRelationship("PROJ_STAFF", project, "ROLE_PROJECT", person, 'ROLE_STAFF')) {
				auditService.logMessage("$securityService.currentUsername assigned $person to project $project.name as STAFF")
			} else {
				throw new DomainUpdateException("An error occurred while attempting to assign the person to the project")
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
	@Transactional
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

		// Remove all of the person's MoveEventStaff relationships for the proje ct
		metrics.eventsUnassigned = deleteFromEvent(map.project, null, map.person, map.teamRoleType)

		// Remove the Project Staff relationship for the project
		List<RoleType> roles = partyRelationshipService.getProjectStaffFunctions(map.project.id, map.person.id)
		for (RoleType role in roles) {
			if (role.type == RoleType.TEAM) {
				partyRelationshipService.deletePartyRelationship("PROJ_STAFF", map.project, "PROJECT", map.person, role.type)
				metrics.teamsUnassigned++
			}
		}

		// Remove the person from the project
		PartyRelationship prProjectStaff = getProjectReference(map.project, map.person)
		if (prProjectStaff) {
			log.debug 'removeFromProject() deleting PartyRelationship {}', prProjectStaff
			prProjectStaff.delete()
			metrics.staffUnassigned = 1
		}
		else {
			log.warn 'removeFromProject() No Project Staff record found for project {} and person {}', projectId, personId
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
		// log.debug 'removeFromProject() project={}, employer={} ({}), project client={} ({})', map.project.id, employer, employer.id, map.project.client, map.project.client.id)
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
				auditService.logMessage("assigned $map.person to project '$map.project.name' event '$event.name' as $teamCode")
			}
			else {
				log.error 'addToEvent() Unable to save MoveEventStaff record for person {}, project {}, event {}, team {}',
						personId, projectId, eventId, teamCode
				throw new RuntimeException("Unable to save MoveEventStaff record : ${GormUtil.allErrorsString(mes)}")
			}
		}
		else {
			log.warn 'addToEvent() called for project {}, person {}, team {} but already exists',
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

		auditService.logMessage("${securityService.currentUsername} unassigned $map.person from team $teamCode for event $event of project $map.project.name")
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
		return getProjectTeamReference(project, person, 'ROLE_STAFF')
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
				eq('roleTypeCodeFrom.id', 'ROLE_PROJECT')
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
			eq('partyRelationshipType', PartyRelationshipType.load('ROLE_PROJ_STAFF'))
			and {
				eq('roleTypeCodeFrom', RoleType.load('ROLE_PROJECT'))
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

		//log.debug 'getAvailableProjects() person {} ({}), employer {}({}), # projects {}', person, person.id, employer, employer.id, projects?.size()
		//log.debug 'getAvailableProjects() list 1: {}', projects*.id

		// Optionally remove the assigned projects
		if (excludeAssigned) {
			List assignedProjects = getAssignedProjects(person, project)
			if (assignedProjects) {
				projects = projects - assignedProjects
			}
		}
		// log.debug 'getAvailableProjects() list 2: {}', projects*.id
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
			// log.debug 'getAvailableProjects() list 3: {}', projects*.id
		}

		return projects
	}

	/**
	 * Used to determine if the person is associated with the project and therefore has access. Access is allowed
	 * for the following scenarios:
	 *    a) System User account(s) (e.g. Automatic User)
	 *    b) Staff of the project owner with the 'ProjectShowAll' permission
	 *    c) Anybody assigned to the project regardless as to if the person is staff of the owner, client or partner of the project
	 * @param project - the project to see if person has access to
	 * @return true if person is associated otherwise false
	 */
	boolean hasAccessToProject(Project project) {
		Person person = securityService.getUserLoginPerson()
		return hasAccessToProject(person, project)
	}

	/**
	 * Used to determine if the person is associated with the project and therefore has access. Access is allowed
	 * for the following scenarios:
	 *    a) System User account(s) (e.g. Automatic User)
	 *    b) Staff of the project owner with the 'ProjectShowAll' permission
	 *    c) Anybody assigned to the project regardless as to if the person is staff of the owner, client or partner of the project
	 * @param person - the person to check
	 * @param project - the project to see if person has access to
	 * @return true if person is associated otherwise false
	 */
	boolean hasAccessToProject(Person person, Project project) {
		boolean hasAccess = false
		if (person.isSystemUser()) {
			hasAccess = true
		} else {
			UserLogin user = person.userLogin
			if (user) {
				boolean hasShowAllProj = securityService.hasPermission(user, Permission.ProjectShowAll)
				if (hasShowAllProj && (person.company.id == projectService.getOwner(project).id || person.company.id == project.client.id)) {
					hasAccess = true
				} else if (hasShowAllProj) {
					// Check if person is staff for a partner on the project
					List partnerProjects = partyRelationshipService.getProjectPartners(project)
					hasAccess = partnerProjects.find { it.id == project.id }
				}
				if (!hasAccess ) {
					// Check to see if person is assigned directly to project
					hasAccess = isAssignedToProject(project, person)
				}
			} else {
				log.warn "hasAccessToProject() called for a person that has no UserLogin (${person.id}:$person"
				//throw new InvalidParamException('Person specified has no UserLogin')
			}
		}
		hasAccess
	}


	/**
	 * Used to validate that a personId specified is valid, that the person exists and that the
	 * person attempting to access the individual has rights to access the person. It will throw
	 * applicable exceptions accordingly.
	 * HTTP responses based on access constraints (e.g. Unauthorized or Not Found)
	 * @param personId - the id of the person to access
	 * @param byWhom - the Person that is attempting to access the Person
	 * @return Person - the person if can access or null
	 * @throws UnauthorizedException - byWhom doesn't have access to the person
	 * @throws InvalidParamException - the specified personId is not valid
	 * @throws EmptyResultException - the specified personId does not exist
	 */
	Person validatePersonAccess(personId) throws UnauthorizedException, InvalidParamException, EmptyResultException {
		Person byWhom = securityService.getUserLoginPerson()
		validatePersonAccess(personId, byWhom)
	}


	/**
	 * Used to validate that a personId specified is valid, that the person exists and that the
	 * person attempting to access the individual has rights to access the person. It will throw
	 * applicable exceptions accordingly.
	 * HTTP responses based on access constraints (e.g. Unauthorized or Not Found)
	 * @param personId - the id of the person to access
	 * @param byWhom - the Person that is attempting to access the Person
	 * @return Person - the person if can access or null
	 * @throws UnauthorizedException - byWhom doesn't have access to the person
	 * @throws InvalidParamException - the specified personId is not valid
	 * @throws EmptyResultException - the specified personId does not exist
	 */
	Person validatePersonAccess(personId, Person byWhom) throws UnauthorizedException, InvalidParamException, EmptyResultException {

		if (!NumberUtil.isPositiveLong(personId)) {
			throw new InvalidParamException('Invalid person id requested')
		}

		// If not edit own account, the user must have privilege to edit the account
		boolean editSelf = NumberUtil.toLong(personId) == byWhom.id
		if (!editSelf) {
			if (! securityService.hasPermission(byWhom.userLogin, Permission.PersonEdit, true)) {
				throw new EmptyResultException('You do not have access to referenced person')
			}
			// securityService.requirePermission(Permission.PersonEdit', false,
			//	"$securityService.currentUsername attempted to edit Person($personId) without necessary permission")
		}

		Person person = Person.get(personId)
		if (!person) {
			throw new EmptyResultException('Unable to find referenced person')
		}

		if (!editSelf) {
			hasAccessToPerson(byWhom, person, true, true)
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
	Person updatePerson(Map params, boolean byAdmin = false)
			throws DomainUpdateException, UnauthorizedException, InvalidParamException, EmptyResultException {
		Person person = validatePersonAccess(params.id)
		if (!isAssociatedTo(securityService.userLoginPerson, person.company)) {
			throw new UnauthorizedException("You do not have permission to manage staffing for the user's company")
		}

		params.travelOK = params.travelOK == 1 || params.travelOK == "1" ? 1 : 0

		if (!person.staffType && !params.staffType) {
			params.staffType = 'Hourly'
		}

		GormUtil.bindMapToDomain(person, params, ["blackoutDates","userLogin"])
		Project project = securityService.userCurrentProject

		Map nameMap = [first: person.firstName, middle: person.middleName, last: person.lastName]
		Map findPersonInfo = findPerson(nameMap, project, null, false, true)
		boolean isPersonAmbiguous = (findPersonInfo.isAmbiguous || (findPersonInfo.person && findPersonInfo.person?.id != person.id)) && !findPersonInfo.partial
		if (isPersonAmbiguous) {
			log.error 'updatePerson() unable to save {} because of conflicting name.', person
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
				securityService.setUserLoginPassword(userLogin, params.newPassword, params.confirmPassword)
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
			if (params.manageFuncs != '0' || params.teams) {
				partyRelationshipService.updateAssignedTeams(person, params.teams)
			}
		}

		if (!byAdmin) {

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

		PartyGroup companyParty
		Person person
		Person byWhom = securityService.loadCurrentPerson()

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
			List personList = partyRelationshipService.getCompanyStaff(companyId)

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

			// Create the person and relationship appropriately
			Map reducedParams = [:] + params
			reducedParams.remove('company')
			reducedParams.remove('function')

			// TODO : JPM 12/2016 : The save method should assign params to the person with command or list of properties
			person = new Person(reducedParams)
			save person, true

			if (person.hasErrors()) {
				throw new DomainUpdateException("Unable to create person. $person${GormUtil.allErrorsString(person)}.")
			}

			auditService.logMessage("$byWhom created person $person as staff of $companyParty")
			// Assign the person to the company
			partyRelationshipService.addCompanyStaff(companyParty, person)

			def teamCodes = CollectionUtils.asList(params.containsKey('function') ? params.function : [])
			if (teamCodes) {
				// Assign the person to the appropriate teams
				partyRelationshipService.updateAssignedTeams(person, teamCodes)
			}

			// If the byUser's current project.client is the same as the new person's company then we'll
			// automatically assign the person to the project as well
//			if (defaultProject && defaultProject.client.id == companyId) {
			if (defaultProject) {
				if (teamCodes) {
					teamCodes.each { tc ->
						addToProjectTeamSecured(defaultProject, person, tc)
					}
				} else {
					// Add the person to the project which is done automatically when adding the team to the project
					addToProjectSecured(defaultProject, person)
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

	/**
	 * Used to find the person by their username
	 * @param username - the user's username to search on
	 * @return the Person domain object if found otherwise null
	 */
	Person findByUsername(String username) {
		Person.executeQuery('select u.person from UserLogin u where u.username=:username',
			[username: username])[0]
	}
}
