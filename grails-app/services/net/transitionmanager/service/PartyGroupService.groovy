package net.transitionmanager.service

import com.tdsops.tm.enums.domain.ProjectStatus
import grails.transaction.Transactional
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * This service support CRUD operations or PartyGroup(companies) for the PartyGroupController
 */
@Transactional
class PartyGroupService implements ServiceMethods {

	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService   partyRelationshipService
	ProjectService             projectService
	UserPreferenceService      userPreferenceService
	LinkGenerator              grailsLinkGenerator

	/**
	 * Creates a paginated map of data for the companies.
	 *
	 * @param filterParams params used to create filters for the list of companies.
	 * @param sortIndex what field to sort the list of companies on.
	 * @param sortOrder the sort order asc or desc.
	 * @param maxRows the max number of rows.
	 * @param currentPage the current page for pagination.
	 * @param rowOffset the row offset For pagination.
	 *
	 * @return a map containing [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
	 */
	def list(Map filterParams, String sortIndex, String sortOrder, int maxRows, int currentPage, int rowOffset) {
		Person whom = securityService.userLoginPerson
		def queryParams = [:]

		def query = new StringBuilder("""SELECT * FROM (
					SELECT name as companyName, party_group_id as companyId, p.date_created as dateCreated, p.last_updated AS lastUpdated, IF(pr.party_id_from_id IS NULL, '','Yes') as partner
					FROM party_group pg
					INNER JOIN party p ON party_type_id='COMPANY' AND p.party_id=pg.party_group_id
					LEFT JOIN party_relationship pr ON pr.party_relationship_type_id = 'PARTNERS' AND pr.role_type_code_from_id = 'COMPANY' and pr.role_type_code_to_id = 'PARTNER' and pr.party_id_to_id = pg.party_group_id
					WHERE party_group_id in (
						SELECT party_id_to_id FROM party_relationship
						WHERE party_relationship_type_id = 'CLIENTS' AND role_type_code_from_id='COMPANY'
						AND role_type_code_to_id='CLIENT' AND party_id_from_id=:whomCompanyId
						) OR party_group_id =:whomCompanyId
					GROUP BY party_group_id ORDER BY
				""")

		query.append(" $sortIndex $sortOrder ) as companies")

		queryParams.whomCompanyId = whom.company.id
		// Handle the filtering by each column's text field
		def firstWhere = true

		filterParams.each {
			if (it.value) {
				if (firstWhere) {
					query.append(" WHERE companies.$it.key LIKE :$it.key")
					firstWhere = false
				} else {
					query.append(" AND companies.$it.key LIKE :$it.key")
				}
				queryParams[it.key] = "%$it.value%"
			}
		}

		def companies = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)

		// Limit the returned results to the user's page size and number
		int totalRows = companies.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)

		if (totalRows > 0) {
			companies = companies[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}

		String showUrl = grailsLinkGenerator.link(controller: 'partyGroup', action: 'show')

		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = companies?.collect {
			[cell: ['<a href="' + showUrl + '/' + it.companyId + '">' + it.companyName + '</a>',
					it.partner, it.dateCreated, it.lastUpdated],
			 id  : it.companyId]
		}
		return [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
	}

	/**
	 * Deletes a party group, if the party group doesn't have any projects(owned, client, or dependant)
	 *
	 * @param id the id of the party group to delete.
	 *
	 * @return a string message for the front end, describing the status of the delete.
	 */
	String delete(Long id) {
		PartyGroup partyGroupInstance = PartyGroup.get(id)
		if (partyGroupInstance) {
			/*
			   We check for different PartyGroup Dependencies that will prevent this party from being deleted
			   if we hit any of them we send a message back to the user, this is done one by one to avoid unnecessary hits to the DB
			 */
			List<Project> projectsOwned = projectService.getProjectsWhereOwner(partyGroupInstance)

			if (projectsOwned) {
				String strProjectList = abbreviateProjects(projectsOwned)
				return "\"<strong>PartyGroup ${partyGroupInstance} can't be deleted, owns ${projectsOwned.size()} projects: ${strProjectList}"
			}

			List<Project> projectsClient = projectService.getProjectsWhereClient(partyGroupInstance, ProjectStatus.ANY)

			if (projectsClient) {
				String strProjectList = abbreviateProjects(projectsClient)
				return "\"<strong>PartyGroup ${partyGroupInstance} can't be deleted, is Client in ${projectsClient.size()} projects: ${strProjectList}<br/>"
			}

			List<Project> projects = partyRelationshipService.getProjectsDependentOfParty(partyGroupInstance)

			if (projects) {
				String strProjectList = abbreviateProjects(projects)
				return "\"<strong>PartyGroup ${partyGroupInstance} can't be deleted, has ${projects.size()} project depenents: ${strProjectList}"
			}

			try {
				List<PartyRelationship> parties = PartyRelationship.findAllByPartyIdFrom(partyGroupInstance)
				parties*.delete()
				parties = PartyRelationship.findAllByPartyIdTo(partyGroupInstance)
				parties*.delete()

				partyGroupInstance.delete()

				return "PartyGroup ${partyGroupInstance} deleted"

			} catch (Exception ex) {
				return ex
			}
		} else {
			return "PartyGroup not found with id ${params.id}"
		}
	}

	/**
	 * Updates a party group
	 *
	 * @param id the party group to update
	 * @param params the params used to update the party group
	 *
	 * @return The updated party group, or the party group with errors.
	 */
	PartyGroup update(Long id, params) {
		PartyGroup partyGroup = PartyGroup.get(id)

		if (partyGroup) {
			partyGroup.properties = params

			if (!partyGroup.hasErrors() && partyGroup.save()) {

				def company = partyRelationshipService.getCompanyOfStaff(securityService.loadCurrentPerson())

				if (company) {
					if (params.partner && params.partner == "Y" && !isAPartner(partyGroup)) {
						partyRelationshipService.savePartyRelationship("PARTNERS", company, "COMPANY", partyGroup, "PARTNER")
					} else if (!params.partner && !isAProjectPartner(partyGroup)) {
						partyRelationshipService.deletePartyRelationship("PARTNERS", company, "COMPANY", partyGroup, "PARTNER")
					}
				}
			}
		}

		return partyGroup
	}

	/**
	 * Create/Saves a party group.
	 *
	 * @param name The name of the new group.
	 * @param comment A comment/description of the new group.
	 * @param partner if the company is a partner
	 * @param partyType the type of party
	 *
	 * @return the partyGroup created or an instance with errors attached.
	 */
	PartyGroup save(String name, String comment, String partner, PartyType partyType) {
		Person whom = securityService.userLoginPerson

		PartyGroup partyGroup = new PartyGroup()
		partyGroup.name = name
		partyGroup.comment = comment
		partyGroup.partyType = partyType

		if (!partyGroup.hasErrors() && partyGroup.save()) {

			//	Statements to create CLIENT PartyRelationship with the user's Company
			if (partyType.id == "COMPANY") {

				def companyParty = whom.company
				partyRelationshipService.savePartyRelationship("CLIENTS", companyParty, "COMPANY", partyGroup, "CLIENT")

				if (partner && partner == "Y") {
					def company = partyRelationshipService.getCompanyOfStaff(securityService.loadCurrentPerson())
					if (company) {
						partyRelationshipService.savePartyRelationship("PARTNERS", company, "COMPANY", partyGroup, "PARTNER")
					}
				}
			}
		}

		return partyGroup
	}

	/**
	 * This joins a list of project names together and abbreviates them using StringUtils.abbreviate.
	 *
	 * @param projects the list of projects to abbreviate.
	 *
	 * @return a string containing the list of project names abbreviated.
	 */
	private String abbreviateProjects(List<Project> projects) {
		String strProjectList = projects.join(", ")
		return StringUtils.abbreviate(strProjectList, 100)
	}

	/**
	 * Checks to see if a party group is a partner.
	 *
	 * @param partyGroup the group to check if it is a partner.
	 *
	 * @return true if the partyGroup is a partner, and false otherwise.
	 *
	 *  TODO : JPM 3/2016 : should take the company as a company instead of looking it up based on the user
	 */
	boolean isAPartner(PartyGroup partyGroup) {
		Party personCompany = securityService.userLoginPerson.company
		if (personCompany) {
			PartyRelationship.executeQuery('''
   				select count(p) from PartyRelationship p
   				where p.partyRelationshipType = 'PARTNERS'
   				  and p.partyIdFrom.id = :companyId
   				  and p.roleTypeCodeFrom.id = 'COMPANY'
   				  and p.roleTypeCodeTo.id = 'PARTNER'
   				  and	p.partyIdTo = :partyGroup
   			''', [partyGroup: partyGroup, companyId: personCompany.id])[0] > 0
		} else {
			false
		}
	}

	/**
	 * Checks to see if a party group is a project partner.
	 *
	 * @param partyGroup the group to check if it is a project partner.
	 *
	 * @return true if the partyGroup is a project partner, and false otherwise.
	 */
	boolean isAProjectPartner(PartyGroup partyGroup) {
		Party personCompany = securityService.userLoginPerson.company
		if (personCompany) {
			PartyRelationship.executeQuery('''
   				select count(1) from PartyRelationship p
   				where p.partyRelationshipType = 'PROJ_PARTNER'
   				  and p.roleTypeCodeFrom.id = 'PROJECT'
   				  and p.roleTypeCodeTo.id = 'PARTNER'
   				  and p.partyIdTo = :partyGroup
   			''', [partyGroup: partyGroup])[0] > 0
		} else {
			false
		}
	}

}
