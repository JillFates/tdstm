import java.text.DateFormat
import java.text.SimpleDateFormat
import jxl.*
import jxl.write.*
import jxl.read.biff.*

import com.tdssrc.grails.GormUtil
import com.tds.asset.AssetComment

class PartyRelationshipService {

	boolean transactional = true
	def jdbcTemplate
	def securityService
	
	/*
	 * method to save party Relationship
	 */
	def savePartyRelationship( def relationshipType, def partyIdFrom, def roleTypeIdFrom, def partyIdTo, def roleTypeIdTo ) {
		try{
			def partyRelationshipType = PartyRelationshipType.findById( relationshipType )
			def roleTypeFrom = RoleType.findById( roleTypeIdFrom )
			def roleTypeTo = RoleType.findById( roleTypeIdTo )
			
			def partyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType, partyIdFrom:partyIdFrom, roleTypeCodeFrom:roleTypeFrom, partyIdTo:partyIdTo, roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" ).save( insert:true, flush:true )
	
			return partyRelationship
		} catch (Exception e) {
			println"Exception-------------->"+e
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
		partyRelationInstance.delete(flush:true)
		return true
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
			clients?.sort{it.partyIdTo.("$sortOn")}
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
	 * Used to retrieve the Company (Party) for which a person is associated as a "STAFF" member
	 * @param Party - the staff member
	 * @return Party - the company Staff is associated with or NULL if no associations
	 */
	def getStaffCompany( def staff ) {
		def relationship = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF'")

		return relationship?.partyIdFrom
	}
	/*
	 *  Method will return Company Staff
	 */
	def getCompanyStaff( def companyId ){
		
		def query = "from Person s where s.id in " + 
		" (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = $companyId and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ) " + 
		" order by s.lastName, s.firstName"
		def personInstanceList = Person.findAll( query )
		
		return personInstanceList
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
		
		def companies = PartyGroup.findAll( " from PartyGroup p where p.partyType = 'COMPANY' " )
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
				log.error "updatePartyRelationshipPartyIdFrom() failed to create relationship " + GormUtil.allErrorsString(PartyRelationship)				
				partyRelationship = null
			}
		}

		return partyRelationship
	}
	
	/*
	 *  Return the Project Staff
	 */
	def getProjectStaff( def projectId ){
		def list = []
		def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom=$projectId and p.roleTypeCodeFrom = 'PROJECT' ")
		projectStaff.each{staff ->
			def map = [:]
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

		if (! companies instanceof List) 
			companies = [companies]

		def staffing = PartyRelationship.findAll("FROM PartyRelationship p WHERE p.partyRelationshipType='STAFF' AND " +
			"p.partyIdFrom IN (:companies) AND p.roleTypeCodeFrom='COMPANY'", [companies:companies])

		def persons = staffing*.partyIdTo
		persons.sort  { a, b -> a.lastNameFirst.compareToIgnoreCase b.lastNameFirst }

		return persons
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
			company = clientStaffOnly ? getStaffCompany(staff.partyIdTo) : null
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
	/*--------------------------------------------------
	 * To convert Date time into mm/dd/yy format
	 * @author srinivas
	 * @param 
	 *---------------------------------------------------*/
	 def convertDate(def date) {
		 Date dt = date
			String dtStr = dt.getClass().getName().toString();
			String dtParam = dt.toString();	
			DateFormat formatter ; 
			formatter = new SimpleDateFormat("MM/dd/yy");
			dtParam = formatter.format(dt);		
			/* if null or any plain string */
			if (dtParam != "null") {
				dtParam = dtParam.trim();
			}
			return dtParam
	}

	/**
	 * Used to get list of functions that a Staff have
	 * @param Integer staffId - staff person id
	 * @param Integer CompanyId - company id that the staff associate with
	 * @return array of role codes
	 */
	def getCompanyStaffFunctions(def companyId, def staffId) {
		
		def projectRoles = PartyRelationship.findAll("from PartyRelationship p \
			where p.partyRelationshipType='STAFF' \
			and p.roleTypeCodeFrom='COMPANY' \
			and p.roleTypeCodeTo !='STAFF' \
			and p.partyIdFrom.id=? \
			and p.partyIdTo.id=?", [companyId, staffId] )
			
		def functions = projectRoles.roleTypeCodeTo
		
		return functions
	}
	
	/**
	 * Used to get list of functions that a Staff member has been assigned to on a Project
	 * @param Integer staffId - staff person id
	 * @param Integer projectId - project id that the staff may be associate with
	 * @return array of role codes
	 */
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
	 /*-------------------------------------------------------
	  *  TO Add the Title Info to MasterSpreadSheet
	  *  @author srinivas
	  *  @param Title Information as a List and Workbook Sheet Object
	  *-------------------------------------------------------*/
	 def exportTitleInfo(def titleFieldList,def titleSheet){
		 def sheetContent
		 def column = 2
		 for (int titleField = 0; titleField < titleFieldList.size(); titleField++ ) {
			 if( titleField == 2){
				 sheetContent = new Label(2,(column-=1),titleFieldList.get(titleField).toString())
			 }else {
				 sheetContent = new Label(1,column,titleFieldList.get(titleField).toString())
			 }
			 titleSheet.addCell(sheetContent)
			 column+=1
		 }
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
		if(existingRoles){
			PartyRole.executeUpdate("delete from PartyRole where party = '$person.id' and roleType in (:roles)",[roles:existingRoles])
		}
	 }
	 
	/**
	 * Update the user functions based on the functions list.
	 * @param project - project that associated with staff
	 * @param person - to which staff assign function
	 * @param functionIds - functions list that assigned to staff
	 * @return nothing
	 */
	def updateStaffFunctions(partyIdFrom, person, functionIds, def prTypeId="STAFF" ){
		
		def prType = PartyRelationshipType.read(prTypeId)
		def roleType = RoleType.read(prType=="STAFF"?"PROJECT":"COMPANY")
		def roleTypeCodeTo = RoleType.read('STAFF')
		// If user deleted all functions.
		def functionQuery = "from PartyRelationship where partyRelationshipType = :type and partyIdTo =:person and roleTypeCodeTo != :roleTypeCodeTo"
		if(!functionIds){
			def existingFuncToDelete = PartyRelationship.findAll(functionQuery, 
				[type:prType, person:person,roleTypeCodeTo:roleTypeCodeTo ])
			PartyRelationship.withNewSession { existingFuncToDelete*.delete() }
			return;
		}
		
		// If we are here, assuming we have some functions assigned to staff
		
		def functions = RoleType.findAllByIdInList(functionIds)
		
		// Delete the functions that are deleted from UI
		def existingFuncToDelete = PartyRelationship.findAll(functionQuery+" and partyIdFrom = :partyIdFrom  and roleTypeCodeTo not in (:functions)", 
			[type:prType, partyIdFrom:partyIdFrom, person:person, functions:functions, roleTypeCodeTo:roleTypeCodeTo ])
		
		// Delete all functions when deleting from Company
		if(prTypeId=='STAFF'){
			 def deletedFuncts = PartyRelationship.findAll(functionQuery+" and roleTypeCodeTo not in (:functions)", 
					[type:PartyRelationshipType.read('PROJ_STAFF'), person:person, functions:functions, roleTypeCodeTo:roleTypeCodeTo ])
			 
			 def deletedEventStaff = MoveEventStaff.findAll("from MoveEventStaff where person =:person and role not in ( :role)",
				 [person:person, role:functions])
			 
			 PartyRelationship.withNewSession { deletedFuncts*.delete() }
			 MoveEventStaff.withNewSession { deletedEventStaff*.delete() }
		}
		PartyRelationship.withNewSession { existingFuncToDelete*.delete() }		
		
		// get the list of functions that are already exists to ignore
		def existingFuncToIgnore = PartyRelationship.findAll(functionQuery+" and roleTypeCodeTo in (:functions) and partyIdFrom = :partyIdFrom ",
			[type:prType, partyIdFrom:partyIdFrom, person:person, functions:functions,roleTypeCodeTo:roleTypeCodeTo ])?.roleTypeCodeTo		
		
		// remove the existing functions to avoid multiple checks
		functions.removeAll(existingFuncToIgnore)
		
		// Iterate through the functions and assign to staff if not exists
		functions.each{ func ->
			def partyRT = new PartyRelationship( partyRelationshipType : prType, 
													partyIdFrom : partyIdFrom,
													roleTypeCodeFrom:roleType,
													partyIdTo : person, 
													roleTypeCodeTo : func 
											  )
			if ( !partyRT.validate() || !partyRT.save() ) {
				def etext = "Unable to create Staff function " + GormUtil.allErrorsString( partyRT )
				log.error( etext )
			}
		}
		// TODO : return some message to send back to UI
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
	 * Returns a list of the roles/teams that staff can be assigned to. Note that the description has the "Staff : " stripped off.
	 * @param boolean indicating if the Automatic role should be included in the list (default true)
	 * @return A list containing maps of all roles with the description cleaned up. Map format of [id, description]
	 */
	static List<Map> getStaffingRoles(includeAuto = true) {
		def roles = RoleType.findAllByDescriptionIlike("Staff%", [sort:'description'])
		def list = []
		roles.each { r -> 
			if ( ! includeAuto && r.id == AssetComment.AUTOMATIC_ROLE) 
				return
			list << [ id: r.id, description: r.description.replaceFirst('Staff : ', '') ]
		} 
		return list
	}
	/**
	 * Returns whether a person assigned to project or not.
	 */
	
	def isPersonAssignedToProject(){
		def userLogin = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		
		return PartyRelationship.find("from PartyRelationship where roleTypeCodeFrom='PROJECT' and partyIdFrom=${project.id} and partyIdTo=${userLogin.person.id}")
	}
}
