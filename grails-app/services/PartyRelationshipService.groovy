import org.apache.poi.*

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tds.asset.AssetComment
import com.tdsops.common.lang.ExceptionUtil
import static com.tdsops.common.lang.CollectionUtils.caseInsensitiveSorterBuilder
import org.codehaus.groovy.grails.commons.GrailsClassUtils

class PartyRelationshipService {

	boolean transactional = true
	def jdbcTemplate
	def securityService
	def serviceHelperService
	
	/*
	 * method to save party Relationship
	 */
	def savePartyRelationship( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ) {
		try {
			def partyRelationshipType = ((relationshipType instanceof PartyRelationshipType) ?: PartyRelationshipType.findById( relationshipType ))
			def roleTypeFrom = (roleTypeIdFrom instanceof RoleType) ? roleTypeIdFrom : RoleType.findById( roleTypeIdFrom )
			def roleTypeTo = (roleTypeIdTo instanceof RoleType) ? roleTypeIdTo : RoleType.findById( roleTypeIdTo )
			
			assert partyRelationshipType != null
			assert roleTypeFrom != null
			assert roleTypeTo != null
			assert (partyIdFrom instanceof Party)
			assert (partyIdTo instanceof Party)

			// log.debug "savePartyRelationship() partyRelationshipType=$partyRelationshipType, roleTypeFrom=$roleTypeFrom, roleTypeTo=$roleTypeTo, partyIdFrom=$partyIdFrom, partyIdTo=$partyIdTo"

			def partyRelationship = new PartyRelationship( 
				partyRelationshipType:partyRelationshipType, 
				partyIdFrom:partyIdFrom, 
				roleTypeCodeFrom:roleTypeFrom, 
				partyIdTo:partyIdTo, 
				roleTypeCodeTo:roleTypeTo, 
				statusCode:"ENABLED" )

			if (! partyRelationship.save( insert:true, flush:true ) ) {
				log.error "savePartyRelationship() failed : ${GormUtil.allErrorsString(partyRelationship)}"
				return null
			}
	
			return partyRelationship
		} catch (Exception e) {
			log.error "savePartyRelationship() had exception ${e.getMessage()} : ${ExceptionUtil.stackTraceToString(e)}"
		}
	}

	/*
	 * method to delete party Relationship
	 */
	def deletePartyRelationship( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ) {
		//log.info "------------------- relationshipType=${relationshipType} partyIdFrom=${partyIdFrom} roleTypeIdFrom=${roleTypeIdFrom} partyIdTo=${partyIdTo} roleTypeIdTo=${roleTypeIdTo} -------------------"
		def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
		def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
		def roleTypeTo = RoleType.findById( roleTypeIdTo )
		
		def partyRelationInstance = PartyRelationship.getRelationshipInstance(partyIdTo,partyIdFrom,roleTypeTo,roleTypeFrom,partyRelationshipType)
		if(partyRelationInstance){
			partyRelationInstance.delete(flush:true)	
		}
		
		return true
	}
	
	/**
	 * Used to assign a company as a client of another
	 * @param client - the company to be the client in the relationship
	 * @param company - the company that will have the other as a client
	 */
	void assignClientToCompany(PartyGroup client, PartyGroup company) {
		savePartyRelationship( "CLIENTS", company, "COMPANY", client, "CLIENT" )
	}

	/**
	 * Used to assign a company as a client of another
	 * @param client - the company to be the partner in the relationship
	 * @param company - the company that will have the other as a client
	 */
	void assignPartnerToCompany(PartyGroup partner, PartyGroup company) {
		savePartyRelationship( "PARTNERS", company, "COMPANY", partner, "PARTNER" )
	}

	/**
	 * Used to retrieve the Company PartyGroup for a given Party
	 * @param Party - a party object to get the Company PartyGroup
	 * @return PartyGroup - the partyGroup that represents the company
	 */
	def getCompany( def party ) {
		return getPartyGroup(party, 'COMPANY')
	}
	
	/*
	 * Used to get list of clients for the specified company
	 * @param Party - the company that has clients to be found
	 * @param sortOn - property to sort on (default name)
	 * @return Array of PartyRelationship
	 */
	def getCompanyClients( company, sortOn='name') {

		def query = "from PartyRelationship p where p.partyRelationshipType = 'CLIENTS' and p.partyIdFrom = :company and " +
			"p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'CLIENT'"
		def clients = PartyRelationship.findAll( query, [company:company] )

		if (clients && sortOn) {
			//OLB: Check the imports, some Functional programming Magic
			def sorter = caseInsensitiveSorterBuilder({ it.partyIdTo?.("$sortOn") })
			clients?.sort(sorter)
		}
		return clients
	}   

	/*
	 * Used to get list of Partners for the specified company
	 * @param Party - the company that has partners to be found
	 * @param sortOn - property to sort on (default name)
	 * @return Array of PartyRelationship
	 */
	def getCompanyPartners( company, sortOn='name') {

		def query = "from PartyRelationship p where p.partyRelationshipType = 'PARTNERS' and p.partyIdFrom = :company and " +
			"p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'PARTNER'"
		def partners = PartyRelationship.findAll( query, [company:company] )
		if (partners && sortOn) {  
			partners?.sort{it.partyIdTo.("$sortOn")}
		}

		return partners
	}

	/*
	 * Used to get list of Partners for the specified project
	 * @param Party - the project that has partners to be found
	 * @param sortOn - property to sort on (default name)
	 * @return Array of PartyRelationship
	 */
	def getProjectPartners( project, sortOn='name') {
		return searchPartners(project, "PROJ_PARTNER", "PROJECT", sortOn)
	}

	/*
	 * Generic function Used to get list of Partners
	 * @param Party - the project that has partners to be found
	 * @param type - relationship type
	 * @param sortOn - property to sort on (default name)
	 * @return Array of PartyRelationship
	 */
	private def searchPartners( party, rtype, roleTypeCodeFrom, sortOn='name') {
		def result = []
		def query = "from PartyRelationship p where p.partyRelationshipType = '$rtype' and p.partyIdFrom = :party and " +
			"p.roleTypeCodeFrom = '$roleTypeCodeFrom' and p.roleTypeCodeTo = 'PARTNER'"
		def partners = PartyRelationship.findAll( query, [party:party] )
		if (partners && sortOn) {
			partners.each{ p ->
				result << p.partyIdTo
			}
			result.sort{it.("$sortOn")}
		}

		return result
	}

	List<Project> getProjectsDependentOfParty(Party party){
		def query = "\
			from PartyRelationship p \
			where \
				p.partyRelationshipType = 'PROJ_PARTNER' and \
				p.partyIdTo = :party and \
				p.roleTypeCodeFrom = 'PROJECT' and \
				p.roleTypeCodeTo = 'PARTNER'\
		"

		def dependents = PartyRelationship.findAll( query, [party:party] )
		List<Project> projects = dependents.collect{ it.partyIdFrom }

		return projects
	}

	/**
	 * Used to retrieve the a PartyGroup for a given Party and Type
	 * @param Party - a party object to get the Company PartyGroup
	 * @param String - a party group type (e.g. COMPANY, PROJECT, etc)
	 * @return PartyGroup - the partyGroup that represents the type for that party
	 */
	def getPartyGroup( def party, def type ) {
		return PartyGroup.find("from PartyGroup as p where partyType = 'COMPANY' AND party = :party", [party:party])		
	}
	
	/** 
	 * Used to retrieve the Company for which a person is associated as a "STAFF" member
	 * @param Party - the staff member
	 * @return Party - the company Staff is associated with or NULL if no associations
	 */
	PartyGroup getCompanyOfStaff( def staff ) {
		staff = StringUtil.toLongIfString(staff)
		boolean byId = (staff instanceof Long)
		String query = """select pr.partyIdFrom from
			PartyRelationship pr where
			pr.partyRelationshipType.id = 'STAFF'
			and pr.roleTypeCodeFrom.id = 'COMPANY'
			and pr.roleTypeCodeTo.id = 'STAFF'
			and pr.partyIdTo${(byId ? '.id' : '')} = :staff"""
		List<PartyGroup> company = PartyRelationship.executeQuery(query, [staff:staff])

		return (company ? company[0] : null)
	}

	/*
	 * Used to return a list of Persons that are staff of the company
	 * @param company - the company to look up (id or object)
	 * @param includeDisabled - flag to control if disabled staff are included in list (default false)
	 * @return the list of the staff for the company
	 */
	List<Person> getCompanyStaff( def company, Boolean includeDisabled=false ) {
		company = StringUtil.toLongIfString(company)
		boolean byId = (company instanceof Long)
		String query = """from Person p where p.id in (
			select pr.partyIdTo.id from PartyRelationship pr 
			where pr.partyRelationshipType.id = 'STAFF'
				and pr.partyIdFrom${(byId ? '.id' : '')} = :company
				and pr.roleTypeCodeFrom.id = 'COMPANY'
				and pr.roleTypeCodeTo.id = 'STAFF')
			order by p.lastName, p.firstName"""

		def list = Person.executeQuery( query, [company:company] )
		if (! includeDisabled) {
			list = list.findAll { it.isEnabled() }
		}
		return list
	}

	/*
	 *  Return the Application staff
	 */
	def getApplicationStaff( def companyId, def roleTypeTo ){
		def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'APPLICATION'  and p.partyIdFrom = $companyId and p.roleTypeCodeTo = '$roleTypeTo' )"
		def applicationCompaniesStaff = Person.findAll(query)
	  
		return applicationCompaniesStaff
		
	}
   
	/*
	 *  method to return list of companies
	 */
	def getCompaniesList(){
		def companies = PartyGroup.findAll( " from PartyGroup p where p.partyType = 'COMPANY' order by p.name " )
		return companies
	}

	/* 
	 * Used to assign a person to a company as a Staff member
	 * @param Party company
	 * @param Person person to assign
	 * @return The PartyRelationship record or null if it failed
	 */
	def addCompanyStaff( company, person ) {
		return updatePartyRelationshipPartyIdFrom('STAFF', company, 'COMPANY', person, 'STAFF')
	}

	/* 
	 * Used to assign a person to a project as a Staff member
	 * @param Party Project
	 * @param Person person to assign
	 * @return The PartyRelationship record or null if it failed
	 */
	def addProjectStaff( project, person ) {
		return updatePartyRelationshipPartyIdFrom('STAFF', project, 'PROJECT', person, 'STAFF')
	}

	/*
	 *  Method to Update  the roleTypeTo 
	 */
	def updatePartyRelationshipRoleTypeTo( def relationshipType, def partyFrom, def roleTypeIdFrom, def partyTo, def roleTypeIdTo ){
		if(roleTypeIdTo != null && roleTypeIdTo != ""){
			def partyRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyFrom.id and p.partyIdTo = $partyTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
			def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
			def roleTypeTo = RoleType.findById( roleTypeIdTo )
			def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
			if ( partyRelationship == null ) {
				def otherRole = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyFrom.id and p.partyIdTo = $partyTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom' ")
				if ( otherRole != null && otherRole != "" ) {
					otherRole.delete(flush:true)
					def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
				} else {
					def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
				}
			}
		} 
		/*else {
		def otherRole = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyFrom.id and p.partyIdTo = $partyTo.id and p.roleTypeCodeFrom = '$roleTypeIdFrom'")
		if ( otherRole != null && otherRole != "" ) {
		otherRole.delete(flush:true)
		}
		}*/
	}
	/*
	 *  Method to update PartyIdTo
	 */
	def updatePartyRelationshipPartyIdTo( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ){
		if ( partyIdTo != "" && partyIdTo != null ){
			def partyRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyIdFrom and p.partyIdTo = $partyIdTo and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
			def partyTo = Party.get( partyIdTo )
			def partyFrom= Party.get( partyIdFrom )
			def partyRelationshipType = PartyRelationshipType.get( relationshipType )
			def roleTypeFrom = RoleType.get( roleTypeIdFrom )
			def roleTypeTo = RoleType.get( roleTypeIdTo )
			// condition to check whether partner has changed or not
			if ( partyRelationship == null ) {
				def otherRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyIdFrom  and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
				if ( otherRelationship != null && otherRelationship != "" ) {
					//	Delete existing partner and reinsert new partner For Project, if partner changed
					otherRelationship.delete(flush:true)
					def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
				} else {
					// Create Partner if there is no partner for this project
					def newPartyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true )
				}
			}
		} else {
			//	if user select a blank then remove Partner
			def otherRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$relationshipType' and p.partyIdFrom = $partyIdFrom  and p.roleTypeCodeFrom = '$roleTypeIdFrom' and p.roleTypeCodeTo = '$roleTypeIdTo' ")
			if ( otherRelationship != null && otherRelationship != "" ) {
				otherRelationship.delete(flush:true)
			}
		}
	}
	/*
	 * Used to find or create a PartyRelationship m
	 * @param String - the relationship type
	 * @param Party - the from party in the relationship
	 * @param String - the from party type in the relationship
	 * @param Party - the to party in the relationship
	 * @param String - the to party type in the relationship
	 * @return PartyRelationship	 
	 */
	PartyRelationship updatePartyRelationshipPartyIdFrom( String relationshipType, Party partyIdFrom, String roleTypeIdFrom, Party partyIdTo, String roleTypeIdTo ){
		def partyRelationship = PartyRelationship.find(
			'from PartyRelationship p where p.partyRelationshipType.id = ? and p.partyIdFrom = ? and p.roleTypeCodeFrom.id = ? and p.partyIdTo = ?  and p.roleTypeCodeTo.id = ? ',
			[relationshipType, partyIdFrom, roleTypeIdFrom, partyIdTo, roleTypeIdTo] )

		if (! partyRelationship ) {
			def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
			def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
			def roleTypeTo = RoleType.findById( roleTypeIdTo )
			partyRelationship = new PartyRelationship( 
				partyRelationshipType:partyRelationshipType, 
				partyIdFrom:partyIdFrom, 
				roleTypeCodeFrom:roleTypeFrom, 
				partyIdTo:partyIdTo, 
				roleTypeCodeTo:roleTypeTo, 
				statusCode:"ENABLED" )
			if ( ! partyRelationship.validate() || ! partyRelationship.save( insert:true, flush:true ) ) {
				log.error "updatePartyRelationshipPartyIdFrom() failed to create relationship " + GormUtil.allErrorsString(partyRelationship)				
				throw new DomainUpdateException('Unable to update party relationship')
				partyRelationship = null
			}
		}

		return partyRelationship
	}
	
	/*
	 *  Return the Project Staff
	 */
	List<Map> getProjectStaff( def projectId ){
		List list = []
		// TODO : JPM 11/2015 : The getProjectStaff should ONLY return the staff record but presently is returning teams too
		def projectStaff = PartyRelationship.findAll("from PartyRelationship p " + 
			"WHERE p.partyRelationshipType='PROJ_STAFF' AND p.partyIdFrom=$projectId AND p.roleTypeCodeFrom = 'PROJECT' ")
			//	"WHERE p.partyRelationshipType='PROJ_STAFF' AND p.partyIdFrom=$projectId AND p.roleTypeCodeFrom = 'PROJECT' AND p.roleTypeCodeTo = 'STAFF'")
		projectStaff.each { staff ->
			def map = [:]
			// TODO : JPM 11/2015 : company should be singular but someone screwed up a LONG time ago and it is a findAll instead of find
			def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
			map.company = company.partyIdFrom
			map.name = staff.partyIdTo.toString()
			map.role = staff.roleTypeCodeTo
			map.staff = staff.partyIdTo
			list << map
		}
		return list
	}

	/**
	 *  Returns a list of staff for a specified project or list of projects
	 * @param Project
	 * @return Map[] - array of maps contain each Staff relationship to a project
	 */
	def getAllProjectsStaff( def projects ) {
		def list = []

		def relations = PartyRelationship.findAll("FROM PartyRelationship p WHERE p.partyRelationshipType='PROJ_STAFF' AND " + 
			"p.partyIdFrom IN (:projects) AND p.roleTypeCodeFrom='PROJECT'", [projects:projects])
			
		relations.each{ r ->
			def company = PartyRelationship.findAll(
				"from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $r.partyIdTo.id and p.roleTypeCodeFrom='COMPANY' " +
				"and p.roleTypeCodeTo = 'STAFF' "
				)

			def map = [:]
			map.company = company.partyIdFrom
			map.staff = r.partyIdTo
			map.name = r.partyIdTo.toString()
			map.role = r.roleTypeCodeTo
			map.project = r.partyIdFrom
			
			list<<map
		}
		return list
	}

	/**
	 * Returns a list of staff for a specified Company or list of Companies
	 * @param A single Party (company) or list of companies
	 * @return Map[] - array of maps contain each Staff relationship to a project
	 */
	def getAllCompaniesStaff( companies ) {
		def list = []

		if (! companies instanceof List) 
			companies = [companies]

		def relations = PartyRelationship.findAll("FROM PartyRelationship p WHERE p.partyRelationshipType='STAFF' AND " +
			"p.partyIdFrom IN (:companies) AND p.roleTypeCodeFrom='COMPANY'", [companies:companies])
			
		relations.each{ r ->
			def company = PartyRelationship.findAll(
				"from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $r.partyIdTo.id and p.roleTypeCodeFrom='COMPANY' " +
				"and p.roleTypeCodeTo = 'STAFF' "
				)

			def map = [:]
			map.company = company.partyIdFrom
			map.staff = r.partyIdTo
			map.name = r.partyIdTo.toString()
			map.role = r.roleTypeCodeTo
			map.project = r.partyIdFrom
			
			list<<map
		}
		return list
	}

	/**
	 * Returns a unique list of staff (Person) objects for a specified Company or list of Companies sorted by lastname, first middle
	 * @param A single Party (company) or list of companies
	 * @return List of unique persons that are staff of the company or companies
	 */
	def getAllCompaniesStaffPersons( companies ) {

		if (! companies instanceof List) {
			companies = [companies]
		}

		String query = """select p.partyIdTo from PartyRelationship p where
			p.partyRelationshipType='STAFF' AND
			p.partyIdFrom IN (:companies) AND 
			p.roleTypeCodeFrom='COMPANY' AND
			p.roleTypeCodeTo='STAFF'"""

		List staff = PartyRelationship.executeQuery(query, [companies:companies], [sort:'partyIdTo'])

		// def persons = staffing*.partyIdTo

		//persons = persons.unique().sort  { a, b -> a.lastNameFirst.compareToIgnoreCase b.lastNameFirst }

		return staff
	}

	/*
	 *  Method to create string list
	 */
	def createString( def teamMembers ){
		def team = new StringBuffer()
		if(teamMembers){
			def teamSize = teamMembers.size()
			for (int i = 0; i < teamSize; i++) {
				if(i != teamSize -1){
					team.append("'"+teamMembers[i]+"',")
				}else{
					team.append("'"+teamMembers[i]+"'")
				}
			}
		}
		return team
	}

	/**
	 * Return a list of persons associated to a project as part of the staff including the client's staff and optionally the partner's and primary's staff
	 * @param The project that the staff is associated to
	 * @param List of persons to exclude from the list (optional)
	 * @param Flag indicating if the list should only contain the client's Staff only
	 * @return A list containing the map with the following properties [company, staff, name, role]
	 */
	List<Map> getAvailableProjectStaff( Project project, def excludeStaff=null, def clientStaffOnly=false ) {
		def list = []
		def projectStaff
		def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT'"
		def args = [ project ]

		// Filter out staff if specified
		if ( excludeStaff && excludeStaff.size()  ) {
			query += ' and p.partyIdTo not in ( ? )'
			args << excludeStaff
		}
		projectStaff = PartyRelationship.findAll( query, args )
		
		def company
		projectStaff.each { staff ->
			def map = new HashMap()
			company = clientStaffOnly ? staff.partyIdTo.company : null
			if (! clientStaffOnly || ( clientStaffOnly && company?.id == project.client.id ) ) 
				list << [company:company, name:staff.partyIdTo.toString(), staff:staff.partyIdTo, role:staff.roleTypeCodeTo ]
		}
		return list
	}

	/**
	 * Similar to the getAvailableStaff exept that this just returns the distinct list of persons returned instead of the map of staff and their one or more roles
	 * @param The project that the staff is associated to
	 * @param List of persons to exclude from the list (optional)
	 * @param Flag indicating if the list should only contain the client's Staff only
	 * @return A list containing the distinct persons
	 */
	List<Person> getAvailableProjectStaffPersons( def project, def excludeStaff=null, def clientStaffOnly=false ) {
		def staff = getAvailableProjectStaff( project, excludeStaff, clientStaffOnly)
		def persons = []

		// Iterate over the list of staff and only add staff that we haven't see yet
		staff.each { s -> 
			if ( ! persons.find { p -> p.id == s.staff.id } )
				persons << s.staff
		}
		
		return persons
	}

	/*
	 *  Return the Project Team Staff
	 */
	def getProjectTeamStaff( def project, def teamMembers ){
		def list = []
		def query
		if (teamMembers) {
			def team = createString( teamMembers )
			query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $project and p.roleTypeCodeFrom = 'PROJECT' and p.partyIdTo in ( $team ) "
			def projectStaff = PartyRelationship.findAll( query )
			projectStaff.each{staff ->
				def map = new HashMap()
				def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
				map.put("company", company.partyIdFrom)
				map.put("name", staff.toString() )
				map.put("role", staff.roleTypeCodeTo)
				map.put("staff", staff.partyIdTo)
				list << map
			}
		}
		return list
	}
	/**
	 * Return the Companies Staff list, which are not associated with Project Or all based on generate value
	 * @param : prjectId
	 * @param : generate( either 'all' or for ignore project staff
	 * 
	 */
	def getProjectCompaniesStaff( def projectId , def generate, def all=false) {
		def list = []
		def project = Project.get( projectId )
		def projectCompanyQuery = "select pr.partyIdTo from PartyRelationship pr where pr.partyRelationshipType in ('PROJ_CLIENT','PROJ_COMPANY','PROJ_PARTNER','PROJ_VENDOR ') and pr.partyIdFrom = $projectId and pr.roleTypeCodeFrom = 'PROJECT'  "
		def query = " from PartyRelationship p where p.partyRelationshipType = 'STAFF' and ( p.partyIdFrom in ( $projectCompanyQuery ) or p.partyIdFrom = ${project.client.id} ) and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' "
		if( !generate ){
			if(!all){
			def projectStaffQuery = "select ps.partyIdTo from PartyRelationship ps where ps.partyRelationshipType = 'PROJ_STAFF' and ps.partyIdFrom = $projectId and ps.roleTypeCodeFrom = 'PROJECT'"
				query +=" and  p.partyIdTo not in ( $projectStaffQuery )"
			}
			def projectCompaniesStaff = PartyRelationship.findAll(query)
			projectCompaniesStaff.each{staff ->
				def map = new HashMap()
				def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
				map.put("company", company.partyIdFrom)
				map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
				map.put("staff", staff.partyIdTo)
				list<<map
			}
		} else {
			list = PartyRelationship.findAll(query)
		}
		return list
	}
	/*
	 *  Will return the Companies list associated with the Project
	 */
	def getProjectCompanies( def projectId ){
		def projectCompanyQuery = "from PartyRelationship pr where pr.partyRelationshipType in ('PROJ_CLIENT','PROJ_COMPANY','PROJ_PARTNER','PROJ_VENDOR ') and pr.partyIdFrom = $projectId and pr.roleTypeCodeFrom = 'PROJECT'  "
		def projectCompanies = PartyRelationship.findAll(projectCompanyQuery)
		return projectCompanies 
	}
	/*
	 *	Method to Create Project Team Members 
	 */
	def createBundleTeamMembers( def projectTeam, def teamMembers ){
		teamMembers.each{teamMember->
			def personParty = Party.findById( teamMember )
			def projectTeamRel = savePartyRelationship( "PROJ_TEAM", projectTeam, "TEAM", personParty, "TEAM_MEMBER" )
		}
		
	}

	/*
	 *  Method will return the Project Team Members
	 */
	def getBundleTeamMembers( def bundleTeam ){
		def list = []
		def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $bundleTeam.id and p.roleTypeCodeFrom = 'TEAM'  "
		def teamMembers = PartyRelationship.findAll(query)
		teamMembers.each{team ->
			def map = new HashMap()
			def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $team.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
			map.put("company", company.partyIdFrom)
			map.put("name", team.partyIdTo.firstName+" "+ team.partyIdTo.lastName)
			map.put("role", team.roleTypeCodeTo)
			map.put("staff", team.partyIdTo)
			map.put("id", team.partyIdTo?.id)
			list<<map
		}
		return list 
	}
	/*
	 *  Method will return the party last name of members
	 */
	def getBundleTeamMembersDashboard( def bundleTeamId ){
		def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $bundleTeamId and p.roleTypeCodeFrom = 'TEAM'  "
		def teamMembers = PartyRelationship.findAll(query)
		def name = new StringBuffer()
		teamMembers.each{team ->
			name.append(team.partyIdTo.firstName.charAt(0))
			name.append(".")
			name.append(team.partyIdTo.lastName)
			name.append("/")
		}
		return name 
	}
	 
	/*
	 *  Return the Staff which are not assign to projectTeam
	 */
	def getAvailableTeamMembers( def projectId, def projectTeam ){
		def list = []
		def query = "from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' and p.partyIdTo not in ( select pt.partyIdTo from PartyRelationship pt where pt.partyRelationshipType = 'PROJ_TEAM' and pt.partyIdFrom = $projectTeam.id and pt.roleTypeCodeFrom = 'TEAM' ) " 
		def projectStaff = PartyRelationship.findAll( query )
		projectStaff.each{staff ->
			def map = new HashMap()
			def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
			map.put("company", company.partyIdFrom)
			map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
			map.put("role", staff.roleTypeCodeTo)
			map.put("staff", staff.partyIdTo)
			list<<map
		}
		return list
	}
	/*
	 *  Return the Staff which are assign to ProjectTeam
	 */
	def getBundleTeamInstanceList( def bundleInstance ){
		def list = []
		def bundleTeamList = ProjectTeam.findAllByMoveBundle( bundleInstance )
		bundleTeamList.each{bundleTeam->
			def map = new HashMap()
			map.put("projectTeam",bundleTeam)
			map.put("teamMembers",getBundleTeamMembers(bundleTeam))
			list<<map
		}
		return list
	}
	/*
	 * 	Return the PartyIdTo Details from PartyRelationship
	 */
	def getPartyToRelationship( def partyRelationshipType, def partyIdFrom, def roleTypeFrom, def roleTypeTo ){
		def partyToRelationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = '$partyRelationshipType' and p.partyIdFrom = $partyIdFrom and p.roleTypeCodeFrom = '$roleTypeFrom' and p.roleTypeCodeTo = '$roleTypeTo' ")
		return partyToRelationship
	}
	/*-----------------------------------------------------------------
	 * To Return the List of TeamMembersNames as a complete String to display in Reports
	 * @author Srinivas
	 * @param teamId
	 * @return concat of  all teammemberNames 
	 *---------------------------------------------------------------*/
	def getTeamMemberNames(def teamId )
	{
		def roleTypeCodeTo ="TEAM_MEMBER"
		def roleTypeInstance = RoleType.findById('TEAM_MEMBER')
		def teamMembers = PartyRelationship.findAll(" from PartyRelationship pr where pr.partyIdFrom = $teamId and pr.roleTypeCodeTo = 'TEAM_MEMBER' ")
		def memberNames = new StringBuffer()
		teamMembers.each{team ->
			memberNames.append(team.partyIdTo.firstName +" "+ team.partyIdTo.lastName)
			memberNames.append("/")
		}
		if(memberNames.size() > 0) {
			memberNames = memberNames.delete(memberNames.size()-1,memberNames.size())
		}
		return memberNames
	}
	/*------------------------------------------
	 * @To get the TeamMembers List for a Team
	 * @author Srinivas
	 * @param teamId
	 * @return list of TeamMembers 
	 *-------------------------------------------*/
	def getTeamMembers(def teamId )
	{
		def roleTypeCodeTo ="TEAM_MEMBER"
		def roleTypeInstance = RoleType.findById('TEAM_MEMBER')
		def teamMembers = PartyRelationship.findAll(" from PartyRelationship pr where pr.partyRelationshipType = 'PROJ_TEAM' and pr.roleTypeCodeFrom ='TEAM' and pr.partyIdFrom = $teamId and pr.roleTypeCodeTo = 'TEAM_MEMBER' ")
		return teamMembers
	}

	/**
	 * Used to get list of Teams that a staff member of a company has
	 * @param company - the company id or object that the person is staff of
	 * @param staff - the staff id or object of the person to look up
	 * @param teamCode - 
	 * @return a list of the RoleType team codes
	 */
	List<RoleType> getCompanyStaffFunctions(def company, def staff, String teamCode=null) {
		assert company
		assert staff	

		company = StringUtil.toLongIfString(company)
		staff = StringUtil.toLongIfString(staff)
		boolean companyById = (company instanceof Long)
		boolean staffById = (staff instanceof Long)

		Map params = [company:company, staff:staff]
		StringBuffer query = new StringBuffer("""select pr.roleTypeCodeTo from PartyRelationship pr
			where pr.partyRelationshipType.id='STAFF' 
			and pr.roleTypeCodeFrom.id = 'COMPANY'
			and pr.roleTypeCodeTo.id != 'STAFF'
			and pr.partyIdFrom${companyById ? '.id' : ''} = :company
			and pr.partyIdTo${staffById ? '.id' : ''} = :staff """)
		if (teamCode) {
			query.append(" and pr.roleTypeCodeTo.id = :teamCode")
			params.teamCode = teamCode
		} else {
			query.append(" and pr.roleTypeCodeTo.id != 'STAFF'")
		}
		query.append(" order by pr.roleTypeCodeTo.id")

		List teams = PartyRelationship.executeQuery(query.toString(), params)
			
		return teams
	}
	
	/**
	 * Used to get list of functions that a Staff member has been assigned to on a Project
	 * @param project - the project Object or ID of the project to search against
	 * @param staff - the staff person object or ID of the person to search for 
	 * @param includeStaffRecord - flag to determine if 'STAFF' records should be included (default true)
	 * @return list of RoleTypes a person is assigned to for a project
	 */
/*
	def getProjectStaffFunctions(def projectId, def staffId) {
		
		def projectRoles = PartyRelationship.findAll("from PartyRelationship p \
			where p.partyRelationshipType='PROJ_STAFF' \
			and p.roleTypeCodeFrom='PROJECT' \
			and p.partyIdFrom.id=? \
			and p.partyIdTo.id=?", [projectId, staffId] )
			
		def functions = projectRoles.roleTypeCodeTo
		
		// log.info "getProjectStaffFunction(projectId:$projectId, staffId:$staffId) - $functions"
		return functions
	}
*/


	/**
	 * Used by the getProjectStaffFunctionCodes and getProjectStaffFunctions methods to construct the shared
	 * query and query parameters.
	 * @param project - the project object or id to query on
	 * @param staff - the staff person object or id to query on
	 * @param includeStaffRecord - flag to indicate if the STAFF record should be included in the results
	 * @return a list consisting of [query, paramsMap]
	 */
	private List getProjectStaffFunctions_queryAndMap( project, staff, boolean includeStaffRecord=true, boolean queryJustId) {
		project = StringUtil.toLongIfString(project)
		staff = StringUtil.toLongIfString(staff)
		boolean projectById = (project instanceof Long)
		boolean staffById = (staff instanceof Long)

		Map params = [project:project, staff:staff]

		StringBuffer query = new StringBuffer("select p.roleTypeCodeTo${queryJustId ? '.id' : ''} ")

		query.append("""from PartyRelationship p
			where p.partyRelationshipType='PROJ_STAFF'
			and p.roleTypeCodeFrom.id='PROJECT'
			and p.partyIdFrom${(projectById ? '.id' : '')} = :project
			and p.partyIdTo${(projectById ? '.id' : '')} = :staff 
			order by p.roleTypeCodeTo.id""")
		
		if (! includeStaffRecord) {
			query.append(" and p.roleTypeCodeTo.type=:teamType ")
			params.teamType = RoleType.TEAM
		}

		return [query.toString(), params]
	}

	/**
	 * Used to retrieve a list of the Team Codes that a person is assigned to for a specified project
	 * @param project - the project object or id to query on
	 * @param staff - the staff person object or id to query on
	 * @param includeStaffRecord - flag to indicate if the STAFF record should be included in the results
	 * @return a list of team codes that the user is assigned for the project
	 */
	List<String> getProjectStaffFunctionCodes(project, staff, boolean includeStaffRecord=true) {
		def (query, params) = getProjectStaffFunctions_queryAndMap(project, staff, includeStaffRecord, true)
		List teamCodes = PartyRelationship.executeQuery(query, params)
		return teamCodes
	}

	List<RoleType> getProjectStaffFunctions(project, staff, boolean includeStaffRecord=true) {
		/*
		project = StringUtil.toLongIfString(project)
		staff = StringUtil.toLongIfString(staff)
		boolean projectById = (project instanceof Long)
		boolean staffById = (staff instanceof Long)

		StringBuffer query = new StringBuffer("""select roleTypeCodeTo from PartyRelationship p
			where p.partyRelationshipType='PROJ_STAFF'
			and p.roleTypeCodeFrom.id='PROJECT'
			and p.partyIdFrom${(projectById ? '.id' : '')} = :project
			and p.partyIdTo${(projectById ? '.id' : '')} = :staff """)
		
		if (! includeStaffRecord) {
			query.append(" and p.roleTypeCodeTo.id <> 'STAFF'")
		}

		def teams = PartyRelationship.executeQuery(query.toString(), [project:project, staff:staff] )
		*/

		def (query, params) = getProjectStaffFunctions_queryAndMap(project, staff, includeStaffRecord, false)
		List teamRoleTypes = PartyRelationship.executeQuery(query, params)

		return teamRoleTypes
	}
	
	/**
	 * Used to determine if a person/staff is assigned a particular function for a given project
	 * @param projectId
	 * @param staffId
	 * @param function	- a single or array of function codes (e.g. 'PROJ_MGR' or ['PROJ_MGR', 'ACCT_MGR'])
	 * @return boolean
	 */
	boolean staffHasFunction(projectId, staffId, function) {
		def projectRoles = PartyRelationship.findAll("from PartyRelationship p \
			where p.partyRelationshipType='PROJ_STAFF' \
			and p.roleTypeCodeFrom='PROJECT' \
			and p.partyIdFrom.id=? \
			and p.partyIdTo.id=? \
			and p.roleTypeCodeTo.id in (?)", [projectId, staffId, function] )
			
		return projectRoles.size() > 0
	}
	
	
	/*-------------------------------------------------------
	  *  Return the Projectmanagers 
	  *  @author srinivas
	  *  @param projectId
	  *-------------------------------------------------------*/
	 def getProjectManagers( def projectId ){
		 def list = []
		 def projectManagers = PartyRelationship.findAll("from PartyRelationship p \
			where p.partyRelationshipType = 'PROJ_STAFF' \
			and p.roleTypeCodeFrom='PROJECT' \
			and p.partyIdFrom = $projectId \
			and p.roleTypeCodeTo = 'PROJ_MGR' ")
		 def managerNames = new StringBuffer()
		 projectManagers.each{staff ->
			managerNames.append(staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
			managerNames.append(", ")
		 }
		 if(managerNames.size() > 0) {
			 managerNames = managerNames.delete(managerNames.size()-2,managerNames.size())
		 }   
		 return managerNames
	 }
	 /**
	  * To update party role by type 
	  * @param type : type of role
	  * @param person : instance of person
	  * @param assignedRoles : assigned roles to the person
	  * @return void
	  */
	 def updatePartyRoleByType( type, person, assignedRoles ){
		def existingRoles = PartyRole.findAll(
			"from PartyRole where party = :person and roleType.description like '${type}%' and roleType.id not in (:roles) group by roleType",
			[roles:assignedRoles, person:person])?.roleType
		if (existingRoles) {
			PartyRole.executeUpdate("delete from PartyRole where party = '$person.id' and roleType in (:roles)",[roles:existingRoles])
		}
	 }

	/**
	 * Used to add a function/team to staff for a company / project
	 * @param person - the individual to assign the team function to
	 * @param functionName - the name/code for the function/team
	 * @param company - the Company to assign the person to
	 * @param project - the Project to optionally assign the person to
	 */
	void addStaffFunction(Person person, String functionName, Party company, Party project=null) {
		def coStaffPRType = PartyRelationshipType.read('STAFF')
		def projStaffPRType = PartyRelationshipType.read('PROJ_STAFF')
		def companyPRType = PartyRelationshipType.read('COMPANY')
		
		RoleType coRoleType = RoleType.read('COMPANY')
		RoleType projRoleType = RoleType.read('PROJECT')
		RoleType functionRoleType = RoleType.read(functionName)

		String msg 

		if (! functionRoleType) {
			msg =  "Invalid role type $functionName"
			log.error "AddStaffFunction() $msg"
			throw new RuntimeException(msg)
		}

		String query = 'from PartyRelationship where partyRelationshipType = :type and partyIdFrom=:from and roleTypeCodeFrom=:fromType and partyIdTo=:to and roleTypeCodeTo=:toType'
		def pr = PartyRelationship.find(query, [type:coStaffPRType, from:company, fromType: coRoleType, to:person, toType:functionRoleType])
		if (!pr) {
			pr = new PartyRelationship(
				partyRelationshipType : coStaffPRType, 
				partyIdFrom : company,
				roleTypeCodeFrom:coRoleType,
				partyIdTo : person, 
				roleTypeCodeTo : functionRoleType 
			)
			if (! pr.validate() || ! pr.save(flush:true)) {
				msg = "Unable to create Company/Staff/$functionName Relationship - ${GormUtil.allErrorsString(pr)}"
				log.error "AddStaffFunction() $msg"
				throw new RuntimeException(msg)
			}
		}

		if (project) {
			pr = PartyRelationship.find(query, [type:projStaffPRType, from:project, fromType: projRoleType, to:person, toType:functionRoleType])
			if (!pr) {
				pr = new PartyRelationship(
					partyRelationshipType : projStaffPRType, 
					partyIdFrom : project,
					roleTypeCodeFrom:projRoleType,
					partyIdTo : person, 
					roleTypeCodeTo : functionRoleType 
				)
				if (! pr.validate() || ! pr.save(flush:true)) {
					msg = "Unable to create Project/Staff/$functionName Relationship - ${GormUtil.allErrorsString(pr)}"
					log.error "AddStaffFunction() $msg"
					throw new RuntimeException(msg)
				}
			}
		}

	}
	 
	/**
	 * Update the team association(s) of a person to a party
	 * @param partyIdFrom - The company or Project that the person is to be associated with
	 * @param person - the person to manage the team relationship(s) for
	 * @param teamCodes - a list of the team codes that the person should be assigned to
	 * @return nothing
	 */
	def updateAssignedTeams(Party person, List teamCodes) {

		boolean debugEnabled = log.isDebugEnabled()

		List allTeamCodes = getTeamCodes()
		List invalidTeamCodes = ( teamCodes ? teamCodes - allTeamCodes : [])
		if (invalidTeamCodes) {
			securityService.reportViolation("attempted to assign invalid team codes ($invalidTeamCodes) to $person")
			throw new InvalidRequestException("Invalid team code was provided ($invalidTeamCodes)")
		}

		List teamsToRemove = ( teamCodes ? allTeamCodes - teamCodes : allTeamCodes )

		// Query to find appropriate relationships
		String query = "from PartyRelationship pr where " + 
			"pr.partyRelationshipType.id = :type and " +
			"pr.roleTypeCodeFrom.id = :typeFrom and " +
			"pr.partyIdTo = :person and " +
			"pr.roleTypeCodeTo.id in (:teams)"

		// Remove any Team assignment that the person has assigned that are not in the teamCodes list
		List toDelete = PartyRelationship.findAll(query, [type:'STAFF', typeFrom:'COMPANY', person:person, teams:teamsToRemove])
		if (toDelete) {
			log.debug "updateAssignedTeams() for $person - removing Company Team assignments ${toDelete*.roleTypeCodeTo.id}"
			toDelete*.delete()
		}

		// Remove Team assignments to any projects
		toDelete = PartyRelationship.findAll(query, [type:'PROJ_STAFF', typeFrom:'PROJECT', person:person, teams:teamsToRemove])
		if (toDelete) {
			if (debugEnabled) {
				List deleteDetail = toDelete.collect { "Project: ${it.partyIdFrom} Team: ${it.roleTypeCodeTo.id}" }
				log.debug "updateAssignedTeams() for $person - removing Project Team assignments ${deleteDetail}"
			}
			toDelete*.delete()
		}

		// Remove any MoveEvent assignments
		toDelete = MoveEventStaff.findAll(
			"from MoveEventStaff where person=:person and role.id in (:teams)",
			[person:person, teams:teamsToRemove] 
		)
		if (toDelete) {
			if (debugEnabled) {
				List deleteDetails = toDelete.collect { "Event: ${it.moveEvent} Team: ${it.role.id}" }
				log.debug "updateAssignedTeams() for $person - removing team event assignments $deleteDetails"
			}
			toDelete*.delete()
		}

		// Now get the list of Teams that the person is assigned to and determine if we need to assign them to any new ones
		if (teamCodes) {
			def personCompany = person.company 	// lazy loading
			List existingTeamsRoleType = getCompanyStaffFunctions(personCompany.id, person.id)
			List existingTeamCodes = existingTeamsRoleType*.id

			List teamsToAssign = teamCodes - existingTeamCodes
			if (teamsToAssign) {
				log.debug "updateAssignedTeams() for $person - adding team assignments $teamsToAssign"
				PartyRelationshipType coStaffPRType = PartyRelationshipType.read('STAFF')
				RoleType coRoleType = RoleType.read('COMPANY')

				teamsToAssign.each { teamCode ->
					PartyRelationship pr = new PartyRelationship()
					pr.partyRelationshipType = coStaffPRType
					pr.roleTypeCodeFrom = coRoleType
					pr.roleTypeCodeTo = RoleType.read(teamCode)
					pr.partyIdFrom = personCompany
					pr.partyIdTo = person
					pr.save(failOnError:true)
				}
			}
		}
	}
	
	/**
	 * Used to get list of functions that a Staff member has been assigned to on a Project
	 * @param instance function - function for which fetching assignee list
	 * @param instance project - project that the staff may be associate with
	 * @return array of assignee users
	 */
	def getProjectStaffByFunction(def function, def project) {
		
		def partyRelationship = PartyRelationship.findAll("from PartyRelationship p \
			where p.partyRelationshipType='PROJ_STAFF' \
			and p.roleTypeCodeFrom='PROJECT' \
			and p.roleTypeCodeTo=:function \
			and p.partyIdFrom=:project", [project:project, function:function] )
			
		def functions = partyRelationship.partyIdTo
		
		return functions
	}
	
	/**
	 * Checks if a person is part of TDS
	 * @param personId the id of the person to check
	 * @return boolean true if the person is a TDS employee, false otherwise
	 */
	def isTdsEmployee ( personId ) {
		def tdsEmployees = jdbcTemplate.queryForList("""
			SELECT party_id_to_id as personId FROM tdstm.party_relationship p 
				WHERE p.party_id_from_id = 18 
					AND p.party_relationship_type_id = 'STAFF' 
					AND p.role_type_code_from_id = 'COMPANY'
					AND p.role_type_code_to_id = 'STAFF'
		""")
		return personId in tdsEmployees.personId
	}

	/**
	 * Used to fetch a list of Team Role Types sorted by the description
	 * @param includeAuto - a flag to indicate if the AUTO team should be included in the list (default false)
	 * @return The list of the TEAM RoleType
	 */
	List<RoleType> getTeamRoleTypes(boolean includeAuto=false) {
		List roles = RoleType.withCriteria {
			eq ('type', RoleType.TEAM)
			if (! includeAuto) {
				and {
					ne('id', 'AUTO')
				}
			}
			order('description', 'asc')
		}
		return roles
	}

	/**
	 * Used to fetch a list of the Team Codes that are maintained in the RoleType table
	 * @param includeAuto - a flag to indicate if the AUTO team should be included in the list (default false)
	 * @return The list of the TEAM RoleType codes
	 */
	List<String> getTeamCodes(boolean includeAuto=false) {
		return getTeamRoleTypes(includeAuto).id
	}

	/**
	 * Returns a list of the roles/teams that staff can be assigned to. Note that the description has the "Staff : " stripped off.
	 * @param boolean indicating if the Automatic role should be included in the list (default true)
	 * @return A list containing maps of all roles with the description cleaned up. Map format of [id, description]
	 */
	List<Map> getStaffingRoles(includeAuto = true) {
		List roles = getTeamRoleTypes(includeAuto)
		/*
		def roles = RoleType.findAllByDescriptionIlike("Staff%", [sort:'description'])
		def list = []
		roles.each { r -> 
			if ( ! includeAuto && r.id == AssetComment.AUTOMATIC_ROLE) 
				return
			list << [ id: r.id, description: r.description.replaceFirst('Staff : ', '') ]
		} 
		*/
		List list = roles.collect {[id:it.id, description:it.toString()]}
		return list
	}

	/**
	 * Returns whether a person assigned to project or not
	 * TODO : JPM 4/2016 : isPersonAssignedToProject is not properly implemented and should be moved to the ProjectService
	 */	
	def isPersonAssignedToProject(){
		def userLogin = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		
		return PartyRelationship.find("from PartyRelationship where roleTypeCodeFrom='PROJECT' and partyIdFrom=${project.id} and partyIdTo=${userLogin.person.id}")
	}

	/**
	 * Used to validate that a person is somehow related to a company
	 * @param personId - the id of the person in question
	 * @param companyId - the id of the company that is being referenced
	 * @return true if person is associated to the company otherwise false
	 * TODO : JPM 4/2016 : isPersonAssociatedToCompany is not properly implemented and should be moved to the ProjectService
	 */
	boolean isPersonAssociatedToCompany(personId, companyId) {
		def count = PartyRelationship.executeQuery("select count(*) from PartyRelationship where partyIdFrom=:cid and partyIdTo=:pid")[0]
		log.debug "isPersonAssociatedToCompany(person:$personId, companyId:$companyId) found $count associations"
		return count > 0
	}

	/**
	 * Used to get a list of Partners for a specific company
	 * @param company - the company to find partners for
	 * @param sortOn - the property to sort on (default 'name')
	 * @return A list of partners for the company
	 */
	List<PartyGroup> companyPartners(PartyGroup company, String sortOn='name') {
		assert company != null

		def args = [prt:PartyRelationshipType.read('PARTNERS'), rtcf: RoleType.read('COMPANY'), rtct: RoleType.read('PARTNER'), company:company]
		String query = "select partyIdTo from PartyRelationship pr where \
			pr.partyRelationshipType = :prt and \
			pr.roleTypeCodeFrom = :rtcf and \
			pr.roleTypeCodeTo = :rtct and \
			pr.partyIdFrom = :company"
		List partners = PartyRelationship.executeQuery(query, args)

		if (partners && sortOn) {
			partners?.sort{it.("$sortOn")}
		}

		return partners
	}

	/**
	 * Used to get a list of Projects that a company owns, is participating as a Partner, or the client
	 * @param company - the company to find the prjoects for
	 * @param project - used to filter the list to a particular project (optional)
	 * @param sortOn - the property to sort on (default 'name')
	 * @return A list of partners for the company
	 */
	List<Project> companyProjects(PartyGroup company, Project project=null) {
		assert company != null

		def args = [ company:company ]
		if (project) {
			args.project = project
		}

		String query = """select partyIdFrom from PartyRelationship pr where
			(	pr.partyRelationshipType.id = 'PROJ_COMPANY'
				and pr.roleTypeCodeFrom.id = 'PROJECT'
				and pr.roleTypeCodeTo.id = 'COMPANY'
				and pr.partyIdTo = :company
				${project ? 'and pr.partyIdFrom = :project' : ''}
			) or
			( 	pr.partyRelationshipType.id = 'PROJ_PARTNER'
				and pr.roleTypeCodeFrom.id = 'PROJECT'
				and pr.roleTypeCodeTo.id = 'PARTNER'
				and pr.partyIdTo = :company
				${project ? 'and pr.partyIdFrom = :project' : ''}
			)"""

		List projects = PartyRelationship.executeQuery(query, args)
		// log.debug "companyProjects() for company $company : list 1 : projects ${projects*.id}"
		// Add to the list those that for clients
		if (project && ! projects.contains(project) && project.client == company) {
			projects = [project]
			// log.debug "companyProjects() for company $company : list 2 : projects ${projects*.id}"
		} else {
			List clientProjects = Project.findAllByClient(company)
			if (clientProjects) {
				projects += clientProjects
				//log.debug "companyProjects() for company $company : list 3 : projects ${projects*.id}"
			}
		}

		return projects
	}

	/**
	 * Used to look up application staff for a given project; the staff is composed by: 
	 * ALL Client Staff + Project Owner and Partner Staff whom have been assigned to the project.
	 * The lists should exclude any inactive persons
	 * @param project - used to filter the list to a particular project (optional)
	 * @return A list of persons
	 */
	List getProjectApplicationStaff(Project project) {
		def companies = new StringBuffer('0')
		def projService = serviceHelperService.getService('project')

		def projectPartners = projService.getPartners(project)
		if (projectPartners) {
			projectPartners.each {
				companies.append(",${it.id}")
			}
		}

		def projectOwner = projService.getOwner(project)
		if (projectOwner) {
			companies.append(",${projectOwner.id}")
		}

		def query = new StringBuffer()

		// Query for the project owner and partner staff that are associated to the project
		query.append("""
			SELECT * FROM (
			 (
				SELECT pr.party_id_to_id AS personId, 
					CONCAT( IFNULL(p.first_name,''), IF(p.first_name IS NULL, '', ' '), 
						IFNULL(p.middle_name,''), IF(p.middle_name IS NULL, '', ' '),
						COALESCE(p.last_name, ''),
						', ', pg.name
					) AS fullName
				FROM tdstm.party_relationship pr 
					INNER JOIN person p ON p.person_id = pr.party_id_to_id and p.active='Y'
					INNER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id 
					INNER JOIN party_relationship pr2 ON pr2.party_id_to_id = pr.party_id_to_id 
						AND pr2.role_type_code_to_id = pr.role_type_code_to_id 
						AND pr2.party_id_from_id = ${project.id}
						AND pr2.role_type_code_from_id = 'PROJECT'
				WHERE pr.role_type_code_from_id in ('COMPANY') 
					AND pr.party_relationship_type_id in ('STAFF') 
					AND pr.party_id_from_id IN (${companies}) 
					AND p.active = 'Y'
				GROUP BY personId 
				ORDER BY fullName ASC 
			)""" )

		query.append(" UNION ")

		// Query for the client Staff
		query.append("""
			(
				SELECT pr.party_id_to_id AS personId, 
					CONCAT( IFNULL(p.first_name,''), IF(p.first_name IS NULL, '', ' '), 
						IFNULL(p.middle_name,''), IF(p.middle_name IS NULL, '', ' '),
						COALESCE(p.last_name, '')
					) AS fullName
				FROM tdstm.party_relationship pr 
					INNER JOIN person p ON p.person_id = pr.party_id_to_id and p.active='Y'
					INNER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id 
				WHERE pr.role_type_code_to_id in ('STAFF') 
					AND pr.role_type_code_from_id in ('COMPANY') 
					AND pr.party_relationship_type_id in ('STAFF') 
					AND pr.party_id_from_id IN (${project.client.id}) 
					AND p.active = 'Y'
				GROUP BY personId 
				ORDER BY fullName ASC 
			) 
            ) AS appStaff
            ORDER BY fullName
		""")
		
		def staffList = jdbcTemplate.queryForList(query.toString())

		return staffList
	}

	/**
	 * Retrieves a list of companies the person is associated to.
	 * @param person for whom to look up the associated companies.
	 * @return list of companies.
	 */
	List<Party> associatedCompanies(Person forWhom){
		Party employer = forWhom.getCompany()
		List<Party> partners = getCompanyPartners(employer)*.partyIdTo
		List<Party> clients =  getCompanyClients(employer)*.partyIdTo
		List<Party> companies = (partners + clients)
		companies << employer
		companies = companies.unique{ p1, p2 -> p1.id <=> p2.id}.sort { it.name }
		return companies
	}



	/**
	 * This method attempts to assign a person/team to an asset's property.
	 *
	 * It supports:
	 *				Person's Id 		=> value = "123456"
	 *				Person's Email 		=> value = "example@mail.com"
	 *				Person's Name 		=> value = "John Doe"
	 *				Team Code 			=> value = "@APP_COORD"
	 *				Property Reference 	=> value = "#prop"
	 *
	 * @param asset 		- asset being updated.
	 * @param property 		- which asset's property is to be given a value.
	 * @param value 		- value to be assigned.
	 * @param projectStaff 	- List of staff assigned to the project.
	 * @param staffingRoles - List of team codes.
	 * @param project
	 *
	 * @return Map => Keys: errMsg, isAmbiguous, notExists, whom.
	 */
	Map assignWhomToAsset(asset, String property, String value, List projectStaff, List staffingRoles, Project project){
		def whom = null
		String errMsg = null

		boolean isAmbiguous = false
		boolean notExists = false


		def assignTeam = {
			def team = value[1..-1]
			if(staffingRoles.contains(team)){
				whom = team
			}else{
				errMsg = "Unknown team ($team) indirectly referenced."
				notExists = true
			}
			
		}


		def assignByPersonId = { personId ->
			def whomId = NumberUtil.toLong(personId)
			whom = projectStaff.find { it.id == whomId }
			if (!whom) {
				// Look if the person exists
				def person = Person.get(whomId)
				if (person){
					errMsg = "Person $person ($value) is not in project staff, asset name: ${asset?.assetName}."
				} else {
					errMsg = "Person id $person doesn't exist, asset name: ${asset?.assetName}."
					notExists = true
				}
			}
		}


		def assignByPropertyName = {
			def propReference = getIndirectPropertyRef(asset, value)
			if (propReference){
				if(propReference instanceof Party){
					whom = propReference
				}else{
					assignByPersonId(propReference)
				}
			}else{
				errMsg = "Unable to resolve indirect whom reference (${value}), asset name: ${asset?.assetName}."
			}
		}


		/*
		 * This closure looks up a person by name.
		 */
		def assignByName = {
			def personService = serviceHelperService.getService("person")
			def map = personService.findPerson(value, project, projectStaff)
			def personMap = personService.findPersonByFullName(value)
						
			if (!map.person && personMap.person ) {
				errMsg = "Person by name ($whom) found but it is NOT a staff"
			} else if ( map.isAmbiguous ) {
				errMsg = "Staff referenced by name ($value) was ambiguous"
				isAmbiguous = true
			} else if (personMap.person) {
				whom = personMap.person
			} else {
				errMsg = "Person by name ($whom) NOT found"
				notExists = true
			}

		}

		switch(value){
			// Team reference.
		    case ~/@.*/:
		        assignTeam()
		        break
		    // Property reference
		    case ~/#.*/:
				assignByPropertyName()
		        break
		    // Person's Id    
		    case ~/\d+/:
		        assignByPersonId(value)
		        break
		    // Person's email address.
		    case ~/.*@.*/:
		    	whom = projectStaff.find { it.email?.toLowerCase() == whom.toLowerCase() }
		    	if(!whom){
		    		errMsg = "Staff referenced by email ($value) not associated with project."
		    	}
		    	break
		   	// Person's name
		    default:
		    	assignByName()
		        
		}

		if(whom){
			asset[property] = whom
		}else{
			errMsg = "Invalid value: $value for property: $property given for $asset."
			log.error(errMsg)
		}

		return [whom: whom, notExists: notExists, isAmbiguous: isAmbiguous, errMsg: errMsg]
	}

	/**
	 * Helper method lookup indirect property references that will recurse once if necessary
	 * This supports two situations:
	 *    1) taskSpec whom:'#prop' and asset.prop contains name/email
	 *    2) taskSpec whom:'#prop' and asset.prop contains #prop2 reference (indirect reference)
	 * @param AssetComment 
	 * @param String propName
	 * @return String - the string (name or email) from the referenced or indirect referenced property
	 * @throws RuntimeException if a reference is made to an invalid fieldname
	 */
	private Object getIndirectPropertyRef( asset, propertyRef, depth=0) {
		log.info "getIndirectPropertyRef() property=$propertyRef, depth=$depth"

		def value
		def property = propertyRef	// Want to hold onto the original value for the exception message
		if (property[0] == '#') {
			// strip off the #
			property = property[1..-1]
		}	

		// Deal with propery name inconsistency
		def crossRef = [ sme1:'sme', sme2:'sme2', owner:'appOwner' ]

		if ( crossRef.containsKey( property.toLowerCase() ) ) {
			property = crossRef.getAt( property.toLowerCase() )
		}

		// Check to make sure that the asset has the field referenced
		if (! asset.metaClass.hasProperty( asset.getClass(), property) ) {
			throw new RuntimeException("Invalid property name ($propertyRef) used in name lookup in asset $asset")
		}

		// TODO : Need to see if we can eliminate the multiple if statements by determining the asset type upfront
		def type = GrailsClassUtils.getPropertyType(asset.getClass(), property)?.getName()
		if (type == 'java.lang.String') {			
			// Check to see if we're referencing a person object vs a string
			log.debug "getIndirectPropertyRef() $property of type $type has value (${asset[property]})"
			if ( asset[property]?.size() && asset[property][0] == '#' ) {
				if (++depth < 3)  {
					value = getIndirectPropertyRef( asset, asset[property], depth)
				} else {
					throw new RuntimeException("Multiple nested indirection unsupported (${property}..${asset[property][0]}) of asset ($asset), depth=$depth")
				}
			} else {
				value = asset[property]
			}
		} else {
			log.debug "getIndirectPropertyRef() indirect references property $property of type $type"
			value = asset[property]
		}

		return value
	}

}
