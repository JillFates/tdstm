package net.transitionmanager.party

import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.ProjectStatus
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import net.transitionmanager.exception.LogicException
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.service.ServiceMethods
import org.apache.commons.lang3.StringUtils
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
		def queryParams = [:]
		Person whom = securityService.userLoginPerson

		StringBuilder query = new StringBuilder("""
			SELECT * FROM (
				SELECT name as companyName, party_group_id as companyId, p.date_created as dateCreated, p.last_updated AS lastUpdated, IF(pr.party_id_from_id IS NULL, '','Yes') as partner
				FROM party_group pg
				INNER JOIN party p ON party_type_id = 'COMPANY' AND p.party_id = pg.party_group_id
				LEFT JOIN party_relationship pr ON pr.party_relationship_type_id = 'PARTNERS' AND pr.role_type_code_from_id = 'ROLE_COMPANY' and pr.role_type_code_to_id = 'ROLE_PARTNER' and pr.party_id_to_id = pg.party_group_id
				WHERE party_group_id in (
					SELECT party_id_to_id FROM party_relationship
					WHERE party_relationship_type_id = 'CLIENTS' AND role_type_code_from_id = 'ROLE_COMPANY'
					AND role_type_code_to_id = 'ROLE_CLIENT' AND party_id_from_id = ${whom.company.id}
				) OR party_group_id = ${whom.company.id}
			GROUP BY party_group_id ORDER BY
		""")

		query << ' ' << sortIndex << ' ' << sortOrder << ' ) as companies '

		// Handle the filtering by each column's text field
		def firstWhere = true

		filterParams.each {
			if (it.value) {
				firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
				query.append(' companies.').append(it.key).append(' LIKE :').append(it.key)
				queryParams[it.key] = SqlUtil.formatForLike(it.value)
			}
		}

		def companies
		try {
			companies = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		} catch(e) {
			log.error "listJson() encountered SQL error : ${e.getMessage()}"
			throw new LogicException("Unabled to perform query based on parameters and user preferences")
		}

		// Limit the returned results to the user's page size and number
		int totalRows = companies.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)

		if (totalRows > 0) {
			companies = companies[rowOffset..Math.min(rowOffset + maxRows - 1, totalRows - 1)]
		}

		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = companies?.collect { [cell: [it.companyName, it.partner, it.dateCreated, it.lastUpdated], id: it.companyId] }

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

			if (!partyGroup.hasErrors() && partyGroup.save(failOnError:false)) {

				def company = securityService.loadCurrentPerson().company

				if (company) {
					if (params.partner && params.partner == "Y" && !isAPartner(partyGroup)) {
						partyRelationshipService.savePartyRelationship("PARTNERS", company, "ROLE_COMPANY", partyGroup, "ROLE_PARTNER")
					} else if (!params.partner && !isAProjectPartner(partyGroup)) {
						partyRelationshipService.deletePartyRelationship("PARTNERS", company, "ROLE_COMPANY", partyGroup, "ROLE_PARTNER")
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

		if (!partyGroup.hasErrors() && partyGroup.save(failOnError:false)) {

			//	Statements to create CLIENT PartyRelationship with the user's Company
			if (partyType.id == "COMPANY") {

				def companyParty = whom.company
				partyRelationshipService.savePartyRelationship("CLIENTS", companyParty, "ROLE_COMPANY", partyGroup, "ROLE_CLIENT")

				if (partner && partner == "Y") {
					def company = securityService.loadCurrentPerson().company
					if (company) {
						partyRelationshipService.savePartyRelationship("PARTNERS", company, "ROLE_COMPANY", partyGroup, "ROLE_PARTNER")
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
   				  and p.roleTypeCodeFrom.id = 'ROLE_COMPANY'
   				  and p.roleTypeCodeTo.id = 'ROLE_PARTNER'
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
   				  and p.roleTypeCodeFrom.id = 'ROLE_PROJECT'
   				  and p.roleTypeCodeTo.id = 'ROLE_PARTNER'
   				  and p.partyIdTo = :partyGroup
   			''', [partyGroup: partyGroup])[0] > 0
		} else {
			false
		}
	}

}
