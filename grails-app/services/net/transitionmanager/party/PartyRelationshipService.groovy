package net.transitionmanager.party

import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.sql.SqlUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEventStaff
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.project.ProjectTeam
import net.transitionmanager.security.RoleType
import net.transitionmanager.service.ServiceMethods
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.jdbc.core.JdbcTemplate

import static com.tdsops.common.lang.CollectionUtils.caseInsensitiveSorterBuilder

@Transactional
class PartyRelationshipService implements ServiceMethods {

	JdbcTemplate   jdbcTemplate
	ProjectService projectService

	/**
	 * Used to create a party relationship
	 */
	PartyRelationship savePartyRelationship(
		String relationshipTypeId,
		Party from, String fromRoleTypeId,
		Party to, String toRoleTypeId
	) {
		try {
			PartyRelationship partyRelationship = new PartyRelationship(
				partyRelationshipType: PartyRelationshipType.get(relationshipTypeId),
				partyIdFrom: from,
				roleTypeCodeFrom: RoleType.get(fromRoleTypeId),
				partyIdTo: to,
				roleTypeCodeTo: RoleType.get(toRoleTypeId),
				statusCode: 'ENABLED')
			save(partyRelationship, true)
			if (partyRelationship.hasErrors()) {
				// TODO : JPM 2/2017 : we should not return null here because we don't know why it failed
				return null
			}
			return partyRelationship
		} catch (e) {
			log.error 'savePartyRelationship() had exception {}: {}',
				e.message, ExceptionUtil.stackTraceToString(e)
		}
	}



	void deletePartyRelationship(String relationshipTypeId, Party partyFrom, String fromRoleTypeId, Party partyTo, String toRoleTypeId) {

		PartyRelationship.executeUpdate('''
			delete PartyRelationship pr
			where pr.partyIdTo = :partyTo
			  and pr.partyIdFrom = :partyFrom
			  and pr.roleTypeCodeTo = :roleTypeTo
			  and pr.roleTypeCodeFrom = :roleTypeFrom
			  and pr.partyRelationshipType = :relationshipType
		''', [roleTypeTo: RoleType.load(toRoleTypeId), roleTypeFrom: RoleType.load(fromRoleTypeId),
		      partyTo: partyTo, partyFrom: partyFrom, relationshipType: PartyRelationshipType.load(relationshipTypeId)])
	}

	/**
	 * Used to assign a company as a client of a company
	 * @param client - the company to be the client in the relationship
	 * @param company - the company that will have the other as a client
	 * @return the PartyRelationship that was created or null if the creation failed
	 */
	PartyRelationship assignClientToCompany(PartyGroup client, PartyGroup company) {
		savePartyRelationship("CLIENTS", company, RoleType.CODE_PARTY_COMPANY, client, RoleType.CODE_PARTY_CLIENT)
	}

	/**
	 * Used to assign a company as a Partner to a company
	 * @param partner - the company to be the client in the relationship
	 * @param company - the company that will have the other as a client
	 * @return the PartyRelationship that was created or null if the creation failed
	 */
	PartyRelationship assignPartnerToCompany(PartyGroup partner, PartyGroup company) {
		savePartyRelationship("PARTNERS", company, RoleType.CODE_PARTY_COMPANY, partner, RoleType.CODE_PARTY_PARTNER)
	}

	/**
	 * Used to assign a company as a client of another
	 * @param partner - the company to be the client in the relationship
	 * @param company - the company that will have the other as a client
	 * @return the PartyRelationship that was created or null if the creation failed
	 */
	PartyRelationship assignPartnerToProject(PartyGroup partner, Project project) {
		savePartyRelationship("PROJ_PARTNER", project, RoleType.CODE_PARTY_PROJECT, partner, RoleType.CODE_PARTY_PARTNER)
	}

	/**
	 * Get clients for the specified company.
	 * @param company  the company
	 * @param sortOn  property to sort on (optional, defaults to 'name')
	 * @return the clients
	 */
	List<PartyRelationship> getCompanyClients(Party company, String sortOn = 'name') {
		List<PartyRelationship> clients = PartyRelationship.findAllWhere(
			partyRelationshipType: PartyRelationshipType.load('CLIENTS'),
			partyIdFrom: company,
			roleTypeCodeFrom: RoleType.load(RoleType.CODE_PARTY_COMPANY),
			roleTypeCodeTo: RoleType.load(RoleType.CODE_PARTY_CLIENT))

		if (clients && sortOn) {
			//OLB: Check the imports, some Functional programming Magic
			clients.sort(caseInsensitiveSorterBuilder({ it.partyIdTo?.getAt(sortOn) }))
		}
		return clients
	}

	/*
	 * Partners for the specified company.
	 * @param company  the company
	 * @param sortOn  property to sort on (optional, defaults to 'name')
	 * @return  the partners
	 */
	List<PartyRelationship> getCompanyPartners(Party company, String sortOn = 'name') {
		List<PartyRelationship> partners = PartyRelationship.findAllWhere(
			partyRelationshipType: PartyRelationshipType.load('PARTNERS'),
			partyIdFrom: company,
			roleTypeCodeFrom: RoleType.load(RoleType.CODE_PARTY_COMPANY),
			roleTypeCodeTo: RoleType.load(RoleType.CODE_PARTY_PARTNER))

		if (partners && sortOn) {
			partners.sort { it.partyIdTo[sortOn] }
		}

		return partners
	}

	/*
	 * Partners for the specified project.
	 * @param project  the project
	 * @param sortOn  property to sort on (optional, defaults to 'name')
	 * @return  the partners
	 */
	List<Party> getProjectPartners(Project project, String sortOn = 'name') {

		List<Party> partners = Party.executeQuery("""
			select pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType = 'PROJ_PARTNER'
			  and pr.partyIdFrom = :project
			  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			  and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_PARTNER'
		""".toString(), [project: project])

		if (partners && sortOn) {
			partners.sort { it[sortOn] }
		}

		return partners
	}

	/**
	 * Persons that are staff of the company
	 * @param company  the company to look up (id or object)
	 * @param includeDisabled - flag to control if disabled staff are included in list (default false)
	 * @return the list of the staff for the company
	 */
	List<Person> getCompanyStaff(company, boolean includeDisabled = false) {
		company = StringUtil.toLongIfString(company)
		def list = Person.executeQuery("""
			from Person where id in (select partyIdTo.id from PartyRelationship
				where partyRelationshipType = 'STAFF'
					and partyIdFrom.id = :companyId
					and roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
					and roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF')
			order by lastName, firstName
		""".toString(), [companyId: company instanceof Long ? company : company.id])

		if (includeDisabled) {
			list
		}
		else {
			list.findAll { it.enabled }
		}
	}

	List<PartyGroup> getCompaniesList() {
		PartyGroup.executeQuery("from PartyGroup where partyType = 'COMPANY' order by name")
	}

	/*
	 * Assign a person to a company as a Staff member
	 * @param company  the company
	 * @param person  the person
	 * @return The PartyRelationship record or null if it failed
	 */
	PartyRelationship addCompanyStaff(Party company, Person person) {
		updatePartyRelationshipPartyIdFrom('STAFF', company, RoleType.CODE_PARTY_COMPANY, person, RoleType.CODE_PARTY_STAFF)
	}

	/*
	 * Assign a person to a project as a Staff member
	 * @param project  the project
	 * @param person  the person
	 * @return The PartyRelationship record or null if it failed
	 */
	PartyRelationship addProjectStaff(Project project, Person person) {
		updatePartyRelationshipPartyIdFrom('PROJ_STAFF', project, RoleType.CODE_PARTY_PROJECT, person, RoleType.CODE_PARTY_STAFF)
	}

	/**
	 * Used to update or create a party
	 */
	def updatePartyRelationshipRoleTypeTo(
		String relationshipType,
		Party partyFrom, String roleTypeIdFrom,
		Party partyTo, String roleTypeIdTo
	) {

		if (!roleTypeIdTo) return

		PartyRelationship partyRelationship = PartyRelationship.executeQuery('''
			from PartyRelationship
			where partyRelationshipType = :relationshipType
			  and partyIdFrom = :partyFrom
			  and partyIdTo = :partyTo
			  and roleTypeCodeFrom = :roleTypeIdFrom
			  and roleTypeCodeTo = :roleTypeIdTo
		''', [relationshipType: relationshipType, partyFrom: partyFrom, partyTo: partyTo,
		      roleTypeIdFrom: roleTypeIdFrom, roleTypeIdTo: roleTypeIdTo], [max: 1])[0]

		PartyRelationshipType partyRelationshipType = PartyRelationshipType.load(relationshipType)
		if (!partyRelationship) {
			PartyRelationship otherRole = PartyRelationship.executeQuery('''
				from PartyRelationship
				where partyRelationshipType = :relationshipType
				  and partyIdFrom = :partyFrom
				  and partyIdTo = :partyTo
				  and roleTypeCodeFrom = :roleTypeIdFrom
			''', [relationshipType: relationshipType, partyFrom: partyFrom, partyTo: partyTo,
			      roleTypeIdFrom: roleTypeIdFrom], [max: 1])[0]

			PartyRelationship newPartyRelationship = new PartyRelationship(
					partyRelationshipType: partyRelationshipType,
					partyIdFrom: partyFrom,
					roleTypeCodeFrom: RoleType.load(roleTypeIdFrom),
					partyIdTo: partyTo,
					roleTypeCodeTo: RoleType.load(roleTypeIdTo))

			if (otherRole) {
				otherRole.delete(flush:true)
				newPartyRelationship.statusCode = "ENABLED"
			}

			newPartyRelationship.save()
		}
	}

	def updatePartyRelationshipPartyIdTo(String relationshipType, partyIdFrom, String roleTypeIdFrom, partyIdTo, String roleTypeIdTo) {
		if (partyIdTo) {
			PartyRelationship partyRelationship = PartyRelationship.executeQuery('''
				from PartyRelationship
				where partyRelationshipType = :relationshipType
				  and partyIdFrom.id = :partyIdFromId
				  and partyIdTo.id = :partyIdToId
				  and roleTypeCodeFrom = :roleTypeIdFrom
				  and roleTypeCodeTo = :roleTypeIdTo
			''', [relationshipType: PartyRelationshipType.load(relationshipType), roleTypeIdFrom: RoleType.load(roleTypeIdFrom), roleTypeIdTo: RoleType.load(roleTypeIdTo),
			      partyIdFromId: NumberUtil.toLong(partyIdFrom), partyIdToId: NumberUtil.toLong(partyIdTo)], [max: 1])[0]

			// condition to check whether relationship has changed or not
			if (!partyRelationship) {
				PartyRelationship otherRelationship = PartyRelationship.executeQuery('''
					from PartyRelationship
					where partyRelationshipType = :relationshipType
					  and partyIdFrom.id = :partyIdFromId
					  and roleTypeCodeFrom = :roleTypeIdFrom
					  and roleTypeCodeTo = :roleTypeIdTo
				''', [relationshipType: PartyRelationshipType.load(relationshipType), partyIdFromId: NumberUtil.toLong(partyIdFrom),
				      roleTypeIdFrom: RoleType.load(roleTypeIdFrom), roleTypeIdTo: RoleType.load(roleTypeIdTo)], [max: 1])[0]

				if (otherRelationship) {
					otherRelationship.delete(flush:true)
				}

				new PartyRelationship(
						partyRelationshipType: PartyRelationshipType.load(relationshipType),
						partyIdFrom: Party.load(partyIdFrom),
						roleTypeCodeFrom: RoleType.load(roleTypeIdFrom),
						partyIdTo: Party.load(partyIdTo),
						roleTypeCodeTo: RoleType.load(roleTypeIdTo),
						statusCode: 'ENABLED').save()
			}
		} else {

			//	if user select a blank then remove relationship
			PartyRelationship.executeUpdate("delete from PartyRelationship where partyRelationshipType.id = :relationshipType"
				  + " and partyIdFrom.id = :partyIdFromId and roleTypeCodeFrom = :roleTypeIdFrom"
				  + " and roleTypeCodeTo = :roleTypeIdTo"
				  , [relationshipType:relationshipType, partyIdFromId: NumberUtil.toLong(partyIdFrom),
				    roleTypeIdFrom: RoleType.load(roleTypeIdFrom), roleTypeIdTo: RoleType.load(roleTypeIdTo)])
		}
	}

	/*
	 * Find or create a PartyRelationship m
	 * @param String - the relationship type
	 * @param Party - the from party in the relationship
	 * @param String - the from party type in the relationship
	 * @param Party - the to party in the relationship
	 * @param String - the to party type in the relationship
	 * @return PartyRelationship
	 */
	PartyRelationship updatePartyRelationshipPartyIdFrom(
			String relationshipTypeCode,
			Party partyIdFrom, String roleTypeIdFrom,
			Party partyIdTo, String roleTypeIdTo) {

		Map args = [
			partyRelationshipType: PartyRelationshipType.load(relationshipTypeCode),
			partyIdFrom: partyIdFrom, roleTypeCodeFrom: RoleType.load(roleTypeIdFrom),
			partyIdTo: partyIdTo, roleTypeCodeTo: RoleType.load(roleTypeIdTo)]

		PartyRelationship partyRelationship = PartyRelationship.findWhere(args)

		if (!partyRelationship) {
			partyRelationship = new PartyRelationship(args)
			partyRelationship.statusCode = 'ENABLED'
			if (! save(partyRelationship, true)) {
				throw new DomainUpdateException('Unable to update party relationship')
			}
		}

		return partyRelationship
	}

	List<Map> getProjectStaff(projectId) {
		List list = []

		// TODO : JPM 11/2015 : The getProjectStaff should ONLY return the staff record but presently is returning teams too
		List<PartyRelationship> projectStaff = PartyRelationship.findAllWhere(
				partyRelationshipType: PartyRelationshipType.load('PROJ_STAFF'),
				partyIdFrom: Party.load(NumberUtil.toLong(projectId)),
				roleTypeCodeFrom: RoleType.load(RoleType.CODE_PARTY_PROJECT))

		for (PartyRelationship staff in projectStaff) {
			def company = PartyRelationship.executeQuery("""
				select pr.partyIdFrom from PartyRelationship pr
				where pr.partyRelationshipType = 'STAFF'
				  and pr.partyIdTo = :partyTo
				  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
				  and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'
			""".toString(), [partyTo: staff.partyIdTo], [max: 1])[0]

			list << [company: company, name: staff.partyIdTo.toString(),
			         role: staff.roleTypeCodeTo, staff: staff.partyIdTo]
		}

		return list
	}

	/**
	 * Returns a unique list of staff (Person) objects for a specified Company or list of Companies sorted by lastname, first middle
	 * @param A single Party (company) or list of companies
	 * @return List of unique persons that are staff of the company or companies
	 */
	List<Person> getAllCompaniesStaffPersons(companies) {

		Person.executeQuery("""
			select pr.partyIdTo from PartyRelationship pr
			where pr.partyRelationshipType='STAFF'
			  AND pr.partyIdFrom IN (:companies)
			  AND pr.roleTypeCodeFrom='$RoleType.CODE_PARTY_COMPANY'
			  AND pr.roleTypeCodeTo='$RoleType.CODE_PARTY_STAFF'
		""".toString(), [companies: CollectionUtils.asList(companies)], [sort: 'partyIdTo'])

		// def persons = staffing*.partyIdTo

		//persons = persons.unique().sort  { a, b -> a.lastNameFirst.compareToIgnoreCase b.lastNameFirst }
	}

	/**
	 * Return a list of persons associated to a project as part of the staff including the client's staff and optionally the partner's and primary's staff
	 * @param project  the project that the staff is associated to
	 * @param excludeStaff  persons to exclude from the list (optional)
	 * @param clientStaffOnly  Flag indicating if the list should only contain the client's Staff only
	 * @return  maps with the following properties [company, staff, name, role]
	 */
	List<Map> getAvailableProjectStaff(Project project, excludeStaff = null, boolean clientStaffOnly = false) {
		String query = """
			from PartyRelationship
			where partyRelationshipType = 'PROJ_STAFF'
			  and partyIdFrom = :project
			  and roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			"""
		Map args = [project: project]

		// Filter out staff if specified
		if (excludeStaff) {
			query += ' and partyIdTo not in (:excludeStaff)'
			args.excludeStaff = excludeStaff
		}

		List<Map> list = []
		for (PartyRelationship staff in PartyRelationship.executeQuery(query, args)) {
			def company = clientStaffOnly ? staff.partyIdTo.company : null
			if (!clientStaffOnly || (clientStaffOnly && company?.id == project.clientId)) {
				list << [company: company, name: staff.partyIdTo.toString(),
				         staff: staff.partyIdTo, role: staff.roleTypeCodeTo]
			}
		}
		return list
	}

	List<Project> getProjectsDependentOfParty(Party party){
        def query = "\
    	    from PartyRelationship p \
            where \
            	p.partyRelationshipType = 'PROJ_PARTNER' and \
                p.partyIdTo = :party and \
                p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT' and \
                p.roleTypeCodeTo = '$RoleType.CODE_PARTY_PARTNER'\
            ".toString()

        def dependents = PartyRelationship.findAll( query, [party:party] )
        List<Project> projects = dependents.collect{ it.partyIdFrom }

        return projects
    }


	/**
	 * Similar to getAvailableStaff except that this just returns the distinct list of persons instead
	 * of the map of staff and their one or more roles.
	 *
	 * @param project  the project that the staff is associated to
	 * @param excludeStaff  persons to exclude from the list (optional)
	 * @param clientStaffOnly  Flag indicating if the list should only contain the client's Staff only
	 * @return  the distinct persons
	 */
	List<Person> getAvailableProjectStaffPersons(Project project, excludeStaff = null, boolean clientStaffOnly = false) {

		def persons = []

		// Iterate over the list of staff and only add staff that we haven't see yet
		for (Map s in getAvailableProjectStaff(project, excludeStaff, clientStaffOnly)) {
			if (!persons.find { p -> p.id == s.staff.id }) {
				persons << s.staff
			}
		}

		return persons
	}

	def getProjectTeamStaff(project, teamMembers) {
		def list = []
		if (teamMembers) {
			List<PartyRelationship> relationships = PartyRelationship.executeQuery("""
				from PartyRelationship
				where partyRelationshipType = 'PROJ_STAFF'
				  and partyIdFrom = :partyIdFrom
				  and roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
				  and partyIdTo in (:team)
				""".toString(), [team: teamMembers.collect { "'" + it + "'" }.join(','), partyIdFrom: project])

			for (PartyRelationship staff in relationships) {
				def company = PartyRelationship.findWhere(
					partyRelationshipType: PartyRelationshipType.load('STAFF'),
					partyIdTo: staff.partyIdTo,
					roleTypeCodeFrom: RoleType.load(RoleType.CODE_PARTY_COMPANY),
					roleTypeCodeTo: RoleType.load(RoleType.CODE_PARTY_STAFF))
				list << [company: company.partyIdFrom, name: staff.toString(),
				         role: staff.roleTypeCodeTo, staff: staff.partyIdTo]
			}
		}
		return list
	}

	/**
	 * Return the Companies Staff list, which are not associated with Project Or all based on generate value
	 * @param : projectId
	 * @param : generate( either 'all' or for ignore project staff
	 */
	def getProjectCompaniesStaff(projectId, generate, boolean all = false) {
		def list = []
		Project project = Project.get(projectId)

		def projectCompanyQuery = """
			select pr.partyIdTo
			from PartyRelationship pr
			where pr.partyRelationshipType in ('PROJ_CLIENT','PROJ_COMPANY','PROJ_PARTNER','PROJ_VENDOR')
			  and pr.partyIdFrom = $projectId
			  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
		"""

		def query = """
			from PartyRelationship p
			where p.partyRelationshipType = 'STAFF'
			  and (p.partyIdFrom in ($projectCompanyQuery) or p.partyIdFrom = $project.client.id)
			  and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
			  and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'
		"""

		if (generate) {
			return PartyRelationship.findAll(query)
		}

		if (!all) {
			def projectStaffQuery = """
				select pr.partyIdTo
				from PartyRelationship pr
				where pr.partyRelationshipType = 'PROJ_STAFF'
				  and pr.partyIdFrom = :projectId
				  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			"""
			query += ' and  p.partyIdTo not in (' + projectStaffQuery + ')'
			args.projectId = projectId
		}

		PartyRelationship.executeQuery(query).each { staff ->
			def company = PartyRelationship.findAll("""
				from PartyRelationship p
				where p.partyRelationshipType = 'STAFF'
				  and p.partyIdTo = $staff.partyIdTo.id
				  and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
				  and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'
			""".toString(), [:])
			list << [company: company.partyIdFrom, staff: staff.partyIdTo,
			         name: staff.partyIdTo.firstName + " " + staff.partyIdTo.lastName]
		}

		return list
	}

	/**
	 * Used to retrieve the list of commpanies associated to a project which include the owner, client and partners
	 * @param projectId - the id of the project to lookup
	 * @return a list the PartyRelationship records where the partyIdTo and the roleTypeCodeTo indicate the company and relationship
	 */
	List<Party> getProjectCompanies(Project project) {
		List<Party> companies
		if (project) {
			companies = PartyRelationship.where {
				partyIdFrom == project
				roleTypeCodeFrom.id == RoleType.TYPE_PROJECT
				partyRelationshipType.id in ['PROJ_CLIENT', 'PROJ_COMPANY', 'PROJ_VENDOR', 'PROJ_PARTNER']
			}.projections {property('partyIdTo')}.list()

			// Add the client is not already in the list
			if (!companies.find{it.id == project.client.id}) {
				companies << project.client
			}
		} else {
			log.error("PartyRelationshipService::getProjectCompanies called with no project.")
		}

		return companies
	}

	void createBundleTeamMembers(ProjectTeam projectTeam, teamMemberIds) {
		for (teamMemberId in teamMemberIds) {
			savePartyRelationship("PROJ_TEAM", projectTeam, RoleType.CODE_PARTY_TEAM, Party.load(teamMemberId), RoleType.CODE_TEAM_MEMBER)
		}
	}

	List<Map<String, Object>> getBundleTeamMembers(ProjectTeam bundleTeam) {
		List<Map<String, Object>> members = []

		PartyRelationship.executeQuery("""
			from PartyRelationship
			where partyRelationshipType = 'PROJ_TEAM'
			  and partyIdFrom = :team
			  and roleTypeCodeFrom = '$RoleType.CODE_PARTY_TEAM'
		""".toString(), [team: bundleTeam]).each { PartyRelationship team ->

			Person person = (Person)team.partyIdTo
			def company = PartyRelationship.executeQuery("""
				select p.partyIdFrom from PartyRelationship p
				where p.partyRelationshipType = 'STAFF'
				and p.partyIdTo = :person
				and p.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
				and p.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'
			""".toString(), [person: person])

			members << [company: company, name: person.firstName + ' ' + person.lastName,
			            role: team.roleTypeCodeTo, staff: person, id: person?.id]
		}

		return members
	}

	/**
	 *  The Staff which are not assign to projectTeam
	 */
	def getAvailableTeamMembers(long projectId, ProjectTeam projectTeam) {
		def list = []
		List<PartyRelationship> projectStaff = PartyRelationship.executeQuery("""
			from PartyRelationship
			where partyRelationshipType = 'PROJ_STAFF'
			  and partyIdFrom.id = :projectId
			  and roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			  and partyIdTo not in (select pr.partyIdTo from PartyRelationship pr
			                        where pr.partyRelationshipType = 'PROJ_TEAM'
			                          and pr.partyIdFrom = :projectTeam
			                          and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_TEAM')
		""".toString(), [projectId: projectId, projectTeam: projectTeam])

		for (PartyRelationship staff in projectStaff) {
			Person person = (Person) staff.partyIdTo
			def company = PartyRelationship.executeQuery("""
				select pr.partyIdFrom
				from PartyRelationship pr
				where pr.partyRelationshipType = 'STAFF'
				  and pr.partyIdTo = :person
				  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
				  and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'
			""".toString(), [:])
			list << [company: company, name: person.firstName + ' ' + person.lastName,
			         role: staff.roleTypeCodeTo, staff: person]
		}
		return list
	}

	/**
	 * The Staff which are assigned to ProjectTeam
	 */
	List<Map<String, Object>> getBundleTeamInstanceList(MoveBundle bundleInstance) {
		ProjectTeam.findAllByMoveBundle(bundleInstance).collect { ProjectTeam bundleTeam ->
			[projectTeam: bundleTeam, teamMembers: getBundleTeamMembers(bundleTeam)]
		}
	}

	/**
	 * Used to retrieve a single PartyRelationship based on all of the primary keys
	 */
	PartyRelationship getPartyToRelationship(
		String partyRelationshipType, Party partyIdFrom,
		String roleTypeFrom, String roleTypeTo) {

		PartyRelationship.executeQuery('''
			from PartyRelationship
			where partyRelationshipType.id = :partyRelationshipTypeId
			  and partyIdFrom = :partyIdFrom
			  and roleTypeCodeFrom.id = :roleTypeFromId
			  and roleTypeCodeTo.id = :roleTypeToId
		''', [partyRelationshipTypeId: partyRelationshipType, partyIdFrom: partyIdFrom,
		      roleTypeFromId: roleTypeFrom, roleTypeToId: roleTypeTo], [max: 1])[0]
	}

	/**
	 * The List of ProjectTeam member names as a complete string to display in Reports.
	 */
	String getTeamMemberNames(ProjectTeam projectTeam) {

		List<Person> teamMembers = Person.executeQuery("""
			select pr.partyIdTo
			from PartyRelationship pr
			where pr.partyIdFrom = :projectTeam
			  and pr.roleTypeCodeTo = '$RoleType.CODE_TEAM_MEMBER'
		""".toString(), ["projectTeam": projectTeam])

		teamMembers.collect { Person p -> p.firstName << ' ' << p.lastName }.join('/')
	}

	List<PartyRelationship> getTeamMembers(ProjectTeam team) {
		PartyRelationship.executeQuery("""
			from PartyRelationship
			where partyRelationshipType = 'PROJ_TEAM'
			  and roleTypeCodeFrom = '$RoleType.CODE_PARTY_TEAM'
			  and partyIdFrom = :team
			  and roleTypeCodeTo = '$RoleType.CODE_TEAM_MEMBER'
		""".toString(), [team: team])
	}

	/**
	 * Used to get list of Teams that a staff member of a company has
	 * @param company - the company id or object that the person is staff of
	 * @param staff - the staff id or object of the person to look up
	 * @param teamCode -
	 * @return a list of the RoleType team codes
	 */
	List<RoleType> getCompanyStaffFunctions(company, staff, String teamCode = null) {
		assert company
		assert staff

		company = StringUtil.toLongIfString(company)
		staff = StringUtil.toLongIfString(staff)

		Map<String, Object> args = [
			companyId: company instanceof Long ? company : company.id,
		    staffId: staff instanceof Long ? staff : staff.id
		]

		StringBuilder query = new StringBuilder("""
			select pr.roleTypeCodeTo
			from PartyRelationship pr
			where pr.partyRelationshipType='STAFF'
			  and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_COMPANY'
			  and pr.roleTypeCodeTo != '$RoleType.CODE_PARTY_STAFF'
			  and pr.partyIdFrom.id = :companyId
			  and pr.partyIdTo.id = :staffId
		""")

		if (teamCode) {
			query.append(" and pr.roleTypeCodeTo.id = :teamCode")
			args.teamCode = teamCode
		}
		query.append(" order by pr.roleTypeCodeTo.id")

		RoleType.executeQuery(query.toString(), args)
	}

	/**
	 * Used by the getProjectStaffFunctionCodes and getProjectStaffFunctions methods to construct the shared
	 * query and query parameters.
	 * @param project - the project object or id to query on
	 * @param staff - the staff person object or id to query on
	 * @return  a 2-element list consisting of [query, paramsMap]
	 */
	private List getProjectStaffFunctions_queryAndMap(project, staff, boolean queryJustId) {
		project = StringUtil.toLongIfString(project)
		staff = StringUtil.toLongIfString(staff)

		String query = """
			select pr.roleTypeCodeTo""" + (queryJustId ? '.id' : '') + """
			from PartyRelationship pr
			where pr.partyRelationshipType='PROJ_STAFF'
			  and pr.roleTypeCodeFrom='$RoleType.CODE_PARTY_PROJECT'
			  and pr.partyIdFrom.id = :projectId
			  and pr.partyIdTo.id = :staffId
			order by pr.roleTypeCodeTo
		"""

		[query, [projectId: project instanceof Long ? project : project.id,
		         staffId: staff instanceof Long ? staff : staff.id]]
	}

	/**
	 * Used to retrieve a list of the Team Codes that a person is assigned to for a specified project
	 * @param project - the project object or id to query on
	 * @param staff - the staff person object or id to query on
	 * @return a list of team codes that the user is assigned for the project
	 */
	List<String> getProjectStaffFunctionCodes(project, staff) {
		def (String query, Map params) = getProjectStaffFunctions_queryAndMap(project, staff, true)
		PartyRelationship.executeQuery(query, params)
	}

	List<RoleType> getProjectStaffFunctions(project, staff, boolean queryJustId = false) {
		def (String query, Map params) = getProjectStaffFunctions_queryAndMap(project, staff, false)
		RoleType.executeQuery(query, params)
	}

	/**
	 * Determine if a person/staff is assigned a particular function for a given project
	 * @param projectId
	 * @param staffId
	 * @param function	- a single or array of function codes (e.g. 'PROJ_MGR' or ['PROJ_MGR', 'ACCT_MGR'])
	 * @return boolean
	 */
	boolean staffHasFunction(Project project, staffId, functionCodes) {
		PartyRelationship.executeQuery("""
			select count(*) from PartyRelationship
			where partyRelationshipType.id='PROJ_STAFF'
			  and roleTypeCodeFrom.id='$RoleType.CODE_PARTY_PROJECT'
			  and partyIdFrom=:project
			  and partyIdTo.id=:staffId
			  and roleTypeCodeTo in (:codes)
		""".toString(), [project: project, staffId: NumberUtil.toLong(staffId),
			  codes: RoleType.getAll(CollectionUtils.asCollection(functionCodes)).findAll()])[0] > 0
	}

	String getProjectManagers(Project project) {
		def projectManagers = PartyRelationship.executeQuery("""
			from PartyRelationship
			where partyRelationshipType = 'PROJ_STAFF'
			  and roleTypeCodeFrom='$RoleType.CODE_PARTY_PROJECT'
			  and partyIdFrom = :project
			  and roleTypeCodeTo = '$RoleType.CODE_TEAM_PROJ_MGR'
		""".toString(), [project: project])

		def managerNames = new StringBuilder()
		projectManagers.each { PartyRelationship staff ->
			managerNames.append(staff.partyIdTo.firstName + ' ' + staff.partyIdTo.lastName)
			managerNames.append(', ')
		}
		if (managerNames.size() > 0) {
			managerNames = managerNames.delete(managerNames.size()-2,managerNames.size())
		}

		managerNames.toString()
	}

	/**
	 * To update party role by type
	 * @param type : type of role
	 * @param person  the person
	 * @param assignedRoles : assigned roles to the person
	 */
	void updatePartyRoleByType(String type, Person person, List<String> assignedRoles) {
		List<PartyRole> existingRoles = PartyRole.executeQuery('''
			from PartyRole
			where party = :person
			  and roleType.type = :type
			  and roleType.id not in (:roles)
			group by roleType
		''', [person: person, type: type, roles: assignedRoles])?.roleType

		if (existingRoles) {
			PartyRole.executeUpdate('''
				delete from PartyRole
				where party = :person
				  and roleType in (:roles)
			''', [person: person, roles: existingRoles])
		}
	}

	/**
	 * Add a function/team to staff for a company / project
	 * @param person - the individual to assign the team function to
	 * @param functionName - the name/code for the function/team
	 * @param company - the Company to assign the person to
	 * @param project - the Project to optionally assign the person to
	 */
	void addStaffFunction(Person person, String functionName, Party company, Party project = null) {
		def coStaffPRType = PartyRelationshipType.read('STAFF')
		def projStaffPRType = PartyRelationshipType.read('PROJ_STAFF')
		def companyPRType = PartyRelationshipType.read('COMPANY')

		RoleType coRoleType = RoleType.read(RoleType.CODE_PARTY_COMPANY)
		RoleType projRoleType = RoleType.read(RoleType.CODE_PARTY_PROJECT)
		RoleType functionRoleType = RoleType.read(functionName)

		String msg

		if (! functionRoleType) {
			msg =  "Invalid role type $functionName"
			log.error 'AddStaffFunction() {}', msg
			throw new RuntimeException(msg)
		}

		String query = 'from PartyRelationship where partyRelationshipType = :type and partyIdFrom=:from and roleTypeCodeFrom=:fromType and partyIdTo=:to and roleTypeCodeTo=:toType'
		def pr = PartyRelationship.find(query, [type:coStaffPRType, from:company, fromType: coRoleType, to:person, toType:functionRoleType])
		if (!pr) {
			pr = new PartyRelationship(
				partyRelationshipType: coStaffPRType,
				partyIdFrom: company,
				roleTypeCodeFrom:coRoleType,
				partyIdTo: person,
				roleTypeCodeTo: functionRoleType
			)
			if (! pr.save(flush:true, failOnError: false)) {
				msg = "Unable to create Company/Staff/$functionName Relationship - ${GormUtil.allErrorsString(pr)}"
				log.error 'AddStaffFunction() {}', msg
				throw new RuntimeException(msg)
			}
		}

		if (project) {
			pr = PartyRelationship.find(query, [type:projStaffPRType, from:project, fromType: projRoleType, to:person, toType:functionRoleType])
			if (!pr) {
				pr = new PartyRelationship(
					partyRelationshipType: projStaffPRType,
					partyIdFrom: project,
					roleTypeCodeFrom:projRoleType,
					partyIdTo: person,
					roleTypeCodeTo: functionRoleType
				)
				if (! pr.save(flush:true, failOnError: false)) {
					msg = "Unable to create Project/Staff/$functionName Relationship - ${GormUtil.allErrorsString(pr)}"
					log.error 'AddStaffFunction() {}', msg
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
	 */
	void updateAssignedTeams(Person person, List<String> teamCodes) {

		boolean debugEnabled = log.debugEnabled

		// STAFF is not a Team Code (not grouped under TEAM), so remove it if it exists
		teamCodes.removeAll {it == RoleType.CODE_PARTY_STAFF}

		// Get all Role Types that are grouped under 'TEAM' (Team Codes)
		List<String> allTeamCodes = getTeamCodes()
		//if there are codes that are not part of TEAMCODES those CANNOT be assigned.
		List<String> invalidTeamCodes = teamCodes ? teamCodes - allTeamCodes : []
		if (invalidTeamCodes) {
			securityService.reportViolation("attempted to assign invalid team codes ($invalidTeamCodes) to $person")
			throw new InvalidRequestException("Invalid team code was provided ($invalidTeamCodes)")
		}

		List<String> teamsToRemove = teamCodes ? allTeamCodes - teamCodes : allTeamCodes

		String query = "from PartyRelationship pr where " +
			"pr.partyRelationshipType.id = :type and " +
			"pr.roleTypeCodeFrom.id = :typeFrom and " +
			"pr.partyIdTo = :person and " +
			"pr.roleTypeCodeTo.id in (:teams)"

		// Remove any Team assignment that the person has assigned that are not in the teamCodes list
		List<PartyRelationship> toDelete = PartyRelationship.executeQuery(query,
				[type: 'STAFF', typeFrom: RoleType.CODE_PARTY_COMPANY, person: person, teams: teamsToRemove])
		if (toDelete) {
			log.debug 'updateAssignedTeams() for {} - removing Company Team assignments {}', person, toDelete*.roleTypeCodeTo.id
			toDelete*.delete()
		}

		// Remove Team assignments to any projects
		toDelete = PartyRelationship.executeQuery(query,
				[type: 'PROJ_STAFF', typeFrom: RoleType.CODE_PARTY_PROJECT, person: person, teams: teamsToRemove])
		if (toDelete) {
			if (debugEnabled) {
				List deleteDetail = toDelete.collect { "Project: $it.partyIdFrom Team: $it.roleTypeCodeTo.id" }
				log.debug 'updateAssignedTeams() for {} - removing Project Team assignments {}', person, deleteDetail
			}
			toDelete*.delete(flush:true)
		}

		// Remove any MoveEvent assignments
		List<MoveEventStaff> moveEventsToDelete = MoveEventStaff.executeQuery(
			'from MoveEventStaff where person=:person and role.id in (:teams)',
			[person: person, teams: teamsToRemove])
		if (moveEventsToDelete) {
			if (debugEnabled) {
				List deleteDetails = moveEventsToDelete.collect { "Event: $it.moveEvent Team: $it.role.id" }
				log.debug 'updateAssignedTeams() for {} - removing team event assignments {}', person, deleteDetails
			}
			moveEventsToDelete*.delete(flush:true)
		}

		// Now get the list of Teams that the person is assigned to and determine if we need to assign them to any new ones
		if (teamCodes) {
			Party personCompany = person.company 	// lazy loading
			List<String> existingTeamCodes = getCompanyStaffFunctions(personCompany?.id, person.id)*.id
			List<String> teamsToAssign = teamCodes - existingTeamCodes
			if (teamsToAssign) {
				log.debug 'updateAssignedTeams() for {} - adding team assignments {}', person, teamsToAssign
				PartyRelationshipType coStaffPRType = PartyRelationshipType.load('STAFF')
				RoleType coRoleType = RoleType.load(RoleType.CODE_PARTY_COMPANY)

				for (String teamCode in teamsToAssign) {
					new PartyRelationship(
						partyRelationshipType: coStaffPRType,
						roleTypeCodeFrom: coRoleType,
						roleTypeCodeTo: RoleType.load(teamCode),
						partyIdFrom: personCompany,
						partyIdTo: person).save( flush:true)
				}
			}
		}
	}

	/**
	 * Used to get a list of staff for a project for a give function or team code
	 * @param function - the function/team code to look for
	 * @param project - the project to look of staff for
	 * @return a list of the persons associate to the specified team code for the project
	 */
	List<Person> getProjectStaffByFunction(RoleType function, Project project) {
		Person.executeQuery("""
			select pr.partyIdTo
			from PartyRelationship pr
			where pr.partyRelationshipType.id='PROJ_STAFF'
			  and pr.roleTypeCodeFrom.id='$RoleType.CODE_PARTY_PROJECT'
			  and pr.roleTypeCodeTo=:function
			  and pr.partyIdFrom=:project
		""".toString(), [project: project, function: function] )
	}

	/**
	 * Checks if a person is part of TDS
	 * @param personId the id of the person to check
	 * @return  true if the person is a TDS employee
	 */
	boolean isTdsEmployee(personId) {
		def tdsEmployees = jdbcTemplate.queryForList("""
			SELECT party_id_to_id as personId FROM party_relationship p
				WHERE p.party_id_from_id = 18
					AND p.party_relationship_type_id = 'STAFF'
					AND p.role_type_code_from_id = '$RoleType.CODE_PARTY_COMPANY'
					AND p.role_type_code_to_id = '$RoleType.CODE_PARTY_STAFF'
		""")
		return personId in tdsEmployees.personId
	}

	/**
	 * Used to fetch a list of Team Role Types sorted by the description
	 * @param includeAuto - a flag to indicate if the AUTO team should be included in the list (default false)
	 * @return The list of the TEAM RoleType
	 */
	List<RoleType> getTeamRoleTypes(boolean includeAuto=false) {
		RoleType.withCriteria {
			eq ('type', RoleType.TYPE_TEAM)
			if (! includeAuto) {
				and {
					ne('id', RoleType.CODE_TEAM_AUTO)
				}
			}
			//The sort was removed and replaced for a 'code' sort since the mix name (:) miss to sort some names and it's cheap
		}.sort { it.toString() }
	}

	/**
	 * Fetch team codes that are maintained in the RoleType table.
	 * @param includeAuto  whether the AUTO team should be included in the list (default false)
	 * @return The list of the TEAM RoleType codes
	 */
	List<String> getTeamCodes(boolean includeAuto = false) {
		getTeamRoleTypes(includeAuto).id
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
			if (! includeAuto && r.id == AssetComment.AUTOMATIC_ROLE)
				return
			list << [id: r.id, description: r.description.replaceFirst('Staff : ', '')]
		}
		*/
		roles.collect { [id: it.id, description: it.toString()] }
	}

	/**
	 * Used to get a list of Projects that a company owns, is participating as a Partner, or the client
	 * @param company - the company to find the projects for
	 * @param project - used to filter the list to a particular project (optional)
	 * @param sortOn - the property to sort on (default 'name')
	 * @return A list of partners for the company
	 */
	List<Project> companyProjects(PartyGroup company, Project project = null, String sortOn = 'name') {
		assert company

		def args = [company: company]
		if (project) {
			args.project = project
		}

		String query = """select pr.partyIdFrom from PartyRelationship pr where
			(	pr.partyRelationshipType = 'PROJ_COMPANY'
				and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
				and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_COMPANY'
				and pr.partyIdTo = :company
				${project ? 'and pr.partyIdFrom = :project' : ''}
			) or
			( 	pr.partyRelationshipType = 'PROJ_PARTNER'
				and pr.roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
				and pr.roleTypeCodeTo = '$RoleType.CODE_PARTY_PARTNER'
				and pr.partyIdTo = :company
				${project ? 'and pr.partyIdFrom = :project' : ''}
			)"""

		Set projects = PartyRelationship.executeQuery(query, args)
		log.debug 'companyProjects() for company {} : list 1 : projects {}', company, projects*.id
		// Add to the list those that for clients
		if (project && ! projects.contains(project) && project.client == company) {
			projects = [project]
			log.debug 'companyProjects() for company {} : list 2 : projects {}', company, projects*.id
		} else {
			List clientProjects = Project.findAllByClient(company)
			if (clientProjects) {
				projects += clientProjects
				log.debug 'companyProjects() for company {} : list 3 : projects {}', company, projects*.id
			}
		}
		if (projects && sortOn ) {
			projects = projects.sort(caseInsensitiveSorterBuilder({ it?.getAt(sortOn) }))
		}

		return projects as List
	}

	/**
	 * Used to look up application staff for a given project; the staff is composed by:
	 * ALL Client Staff + Project Owner and Partner Staff whom have been assigned to the project.
	 * The lists should exclude any inactive persons
	 * @param project - used to filter the list to a particular project (optional)
	 * @return A list of persons
	 */
	List getProjectApplicationStaff(Project project) {
		def companyIds = new StringBuilder('0')

		for (partner in projectService.getPartners(project)) {
			companyIds << ',' << partner.id
		}

		def projectOwner = projectService.getOwner(project)
		if (projectOwner) {
			companyIds << ',' << projectOwner.id
		}

		def query = new StringBuilder()

		// Query for the project owner and partner staff that are associated to the project
		query.append("""
			SELECT * FROM (
			 (
				SELECT pr.party_id_to_id AS personId,
					${SqlUtil.personFullNameSql('p')} AS fullName
				FROM party_relationship pr
					INNER JOIN person p ON p.person_id = pr.party_id_to_id and p.active='Y'
					INNER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id
					INNER JOIN party_relationship pr2 ON pr2.party_id_to_id = pr.party_id_to_id
						AND pr2.role_type_code_to_id = pr.role_type_code_to_id
						AND pr2.party_id_from_id = $project.id
						AND pr2.role_type_code_from_id = '$RoleType.CODE_PARTY_PROJECT'
				WHERE pr.role_type_code_from_id in ('$RoleType.CODE_PARTY_COMPANY')
					AND pr.party_relationship_type_id in ('STAFF')
					AND pr.party_id_from_id IN ($companyIds)
					AND p.active = 'Y'
				GROUP BY personId
				ORDER BY fullName ASC
			)""" )

		query.append(" UNION ")

		// Query for the client Staff
		query.append("""
			(
				SELECT pr.party_id_to_id AS personId,
					${SqlUtil.personFullNameSql('p')} AS fullName
				FROM party_relationship pr
					INNER JOIN person p ON p.person_id = pr.party_id_to_id and p.active='Y'
					INNER JOIN party_group pg ON pg.party_group_id = pr.party_id_from_id
				WHERE pr.role_type_code_to_id in ('$RoleType.CODE_PARTY_STAFF')
					AND pr.role_type_code_from_id in ('$RoleType.CODE_PARTY_COMPANY')
					AND pr.party_relationship_type_id in ('STAFF')
					AND pr.party_id_from_id IN ($project.client.id)
					AND p.active = 'Y'
				GROUP BY personId
				ORDER BY fullName ASC
			)
            ) AS appStaff
            ORDER BY fullName
		""")
		return jdbcTemplate.queryForList(query.toString())
	}

	/**
	 * Retrieves a list of companies the person is associated to.
	 * @param person for whom to look up the associated companies.
	 * @return list of companies.
	 */
	List<Party> associatedCompanies(Person forWhom) {
		Party employer = forWhom.company
		List<Party> partners = getCompanyPartners(employer)*.partyIdTo
		List<Party> clients =  getCompanyClients(employer)*.partyIdTo
		List<Party> companies = (partners + clients)
		companies << employer

		companies = companies.unique { Party p1, Party p2 ->
			GrailsHibernateUtil.unwrapIfProxy(p1).id <=> GrailsHibernateUtil.unwrapIfProxy(p2).id
		}.sort {
			GrailsHibernateUtil.unwrapIfProxy(it).name
		}

		return companies
	}
}
