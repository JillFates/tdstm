/**
 * The PersonService class provides a number of functions to help in the management and access of Person objects
 */
class PersonService {

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
	 * Used to find a person using a string of their name
	 * @param Map containing person name elements
	 * @param Project the project/client that the person is associated with
	 * @return Person an instance of person or null if unable to lookup
	 */
	Person findPerson(String name , Project project) {
		def nameMap = parseName(name)
		return nameMap ? findPerson(nameMap, project) : null
	}

	/**
	 * This method is used to find a person using a name map
	 * @param Map containing person name elements
	 * @param Project the project/client that the person is associated with
	 * @return Person an instance of person or null if unable to lookup
	 */
	Person findPerson(Map nameMap , Project project) {
		def person=null
log.info "findPersion() nameMap=$nameMap"
		// Make sure we have a person
		if (! nameMap || ! nameMap.containsKey('first'))
			return person 

		// Find all people with the same name
		def personList = Person.findAll("from Person as p where p.firstName=? and p.middleName=? and p.lastName=?",
			[ nameMap.first, nameMap.middle, lastNameWithSuffix(nameMap) ] )

log.info "findPerson() found ${personList.size()} that matched"

		// If we found them, then match against the company's staff
		if (personList?.size() > 0) {
			// Try finding the person within the Project's Staff by their ID
			def staffList = partyRelationshipService.getCompanyStaff(project?.client.id)
			for (int i=0; i < personList?.size(); i++) {
				log.info "Looking at ${personList[i]}"
				person = staffList.find { it.id == personList[i].id }
				if (person) break 
			}
		}

		return person
	}

	/**
	 * Used to find a person object from their full name and if not found create it
	 * @param String the person's full name
	 * @param Project the project/client that the person is associated with
	 * @return instance of person or null if unable to lookup
	 */
	Person findOrCreatePerson(String name , Project project) {
		def nameMap = parseName(name)
		if (nameMap == null) {
			log.warn "findOrCreatePersonByName() unable to parse name ($name)"
			return null
		}
		return findOrCreatePerson( nameMap, project)
	}

	/**
	 * This method is used to find a person object after importing and if not found create it
	 * Used to find a person object from their full name and if not found create it
	 * @param Map the person's full name in map
	 * @param Project the project/client that the person is associated with
	 * @return instance of person or null if unable to lookup
	 */
	Person findOrCreatePerson(Map nameMap , Project project) {

		def person = findPerson(nameMap, project)

		if ( !person && nameMap.first ) {
			log.debug "Person $firstName $lastName does not found in selected company"
			person = new Person('firstName':firstName, 'lastName':lastName, 'staffType':'Salary')
			if (!person.save(insert:true, flush:true)) {
				def etext = "findOrCreatePerson Unable to create Person"+GormUtil.allErrorsString( person )
				log.error( etext )
			}
			def partyRelationshipType = PartyRelationshipType.findById( "STAFF" )
			def roleTypeFrom = RoleType.findById( "COMPANY" )
			def roleTypeTo = RoleType.findById( "STAFF" )

			def partyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType,
				partyIdFrom :project.client, roleTypeCodeFrom:roleTypeFrom, partyIdTo:person,
				roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true, flush:true )
		}

		return person
	}

	/**
	 * Parses a name into it's various components and returns them in a map
	 * @param String The full name of the person
	 * @return Map - the map of the parsed name that includes first, last, middle, suffix
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
				def msg = "parseName('$name') encountered multiple commas that is not handled"
				log.error msg
				throw new RuntimeException(msg)
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

}