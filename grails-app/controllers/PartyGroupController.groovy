import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.StringUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PartyGroupController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ControllerService controllerService
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	UserPreferenceService userPreferenceService

	/**
	 * Used to render the Company List view which will call back to the listJson for the actual data
	 */
	@HasPermission(Permission.CompanyView)
	def list() {
		[listJsonUrl: createLink(controller: 'person', action: 'listJson')]
	}

	/**
	 * Used by the List view JQGrid
	 */
	@HasPermission(Permission.CompanyView)
	def listJson() {

    	Person whom = securityService.userLoginPerson

		String sortIndex = params.sidx ?: 'companyName'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows
		def companies
		def filterParams = [companyName: params.companyName, dateCreated: params.dateCreated,
		    lastUpdated: params.lastUpdated, partner: params.partner]

		// Validate that the user is sorting by a valid column
		if( ! sortIndex in filterParams)
			sortIndex = 'companyName'

		String active = params.activeUsers ?: session.getAttribute("InActive") ?: 'Y'

		def queryParams = [:]
		def query = new StringBuffer("""SELECT * FROM (
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
		query.append( " $sortIndex $sortOrder ) as companies")

		queryParams.whomCompanyId = whom.company.id
		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if(it.value) {
				if (firstWhere) {
					query.append(" WHERE companies.$it.key LIKE :$it.key")
					firstWhere = false
				}
				else {
					query.append(" AND companies.$it.key LIKE :$it.key")
				}
				queryParams[it.key] = "%$it.value%"
			}
		}

		companies = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)

		// Limit the returned results to the user's page size and number
		int totalRows = companies.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			companies = companies[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		}

		def showUrl = createLink(controller:'partyGroup', action:'show')

		// Due to restrictions in the way jqgrid is implemented in grails, sending the html directly is the only simple way to have the links work correctly
		def results = companies?.collect { [cell: ['<a href="' + showUrl + '/' + it.companyId + '">' + it.companyName + '</a>',
		                                          it.partner, it.dateCreated, it.lastUpdated],
		                                   id: it.companyId] }
		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	@HasPermission(Permission.CompanyView)
	def show() {
		PartyGroup partyGroup = PartyGroup.get(params.id)
		userPreferenceService.setPreference(PREF.PARTY_GROUP, partyGroup?.id)

		if (!partyGroup) {
			flash.message = "PartyGroup not found with id $params.id"
			redirect(action: "list")
			return
		}

		[partyGroupInstance: partyGroup, partner: isAPartner(partyGroup), projectPartner: isAProjectPartner(partyGroup)]
	}

	@HasPermission(Permission.CompanyDelete)
	def delete() {

		PartyGroup partyGroupInstance = PartyGroup.get(params.id)
		if (partyGroupInstance) {
			/*
			   We check for different PartyGroup Dependencies that will prevent this party from being deleted
			   if we hit any of them we send a message back to the user, this is done one by one to avoid unnecesary hits to the DB
			 */
			List<Project> projectsOwned = projectService.getProjectsWhereOwner(partyGroupInstance)
			if(projectsOwned){
				String strProjectList = projectsOwned.join(", ")
				strProjectList = StringUtils.abbreviate(strProjectList, 100)
				flash.message = "\"<strong>PartyGroup ${partyGroupInstance} can't be deleted, owns ${projectsOwned.size()} projects: ${strProjectList}"
				redirect(action:"list")
				return
			}

			List<Project> projectsClient = projectService.getProjectsWhereClient(partyGroupInstance, ProjectStatus.ANY)
			if(projectsClient){
				String strProjectList = projectsClient.join(", ")
				strProjectList = StringUtils.abbreviate(strProjectList, 100)
				flash.message = "\"<strong>PartyGroup ${partyGroupInstance} can't be deleted, is Client in ${projectsClient.size()} projects: ${strProjectList}<br/>"
				redirect(action:"list")
				return
			}

			List<Project> projects = partyRelationshipService.getProjectsDependentOfParty(partyGroupInstance)
			if(projects) {
				String strProjectList = projects.join(", ")
				strProjectList = StringUtils.abbreviate(strProjectList, 100)
				flash.message = "\"<strong>PartyGroup ${partyGroupInstance} can't be deleted, has ${projects.size()} project depenents: ${strProjectList}"
				redirect(action:"list")
				return
			}


			try {
				PartyGroup.withNewSession { s ->
					def parties
					parties = PartyRelationship.findAllByPartyIdFrom(partyGroupInstance)
					parties*.delete()
					parties = PartyRelationship.findAllByPartyIdTo(partyGroupInstance)
					parties*.delete()
					s.flush()
					s.clear()
				}

				partyGroupInstance.delete(flush: true)
				flash.message = "PartyGroup ${partyGroupInstance} deleted"

			} catch (Exception ex) {
				flash.message = ex
			}
		} else {
			flash.message = "PartyGroup not found with id ${params.id}"
		}
		redirect(action:"list")

	}

	@HasPermission(Permission.CompanyEdit)
	def edit() {
		PartyGroup partyGroup = PartyGroup.get( params.id )
		userPreferenceService.setPreference(PREF.PARTY_GROUP, partyGroup?.id)
		if(!partyGroup) {
			flash.message = "PartyGroup not found with id $params.id"
			redirect(action:"list")
			return
		}

		[partyGroupInstance: partyGroup, partner: isAPartner(partyGroup), projectPartner: isAProjectPartner(partyGroup)]
	}

	@HasPermission(Permission.CompanyEdit)
	def update() {
		PartyGroup partyGroup = PartyGroup.get( params.id )
		//partyGroup.lastUpdated = new Date()
		if(partyGroup) {
			partyGroup.properties = params

			if( !partyGroup.hasErrors() && partyGroup.save()) {

				def company = partyRelationshipService.getCompanyOfStaff(securityService.loadCurrentPerson())
				if (company) {
					if (params.partner && params.partner == "Y" && !isAPartner(partyGroup)) {
						partyRelationshipService.savePartyRelationship("PARTNERS", company, "COMPANY", partyGroup, "PARTNER")
					} else if (!params.partner && !isAProjectPartner(partyGroup)) {
						partyRelationshipService.deletePartyRelationship("PARTNERS", company, "COMPANY", partyGroup, "PARTNER")
					}
				}

				flash.message = "PartyGroup $partyGroup updated"
				redirect(action:"show",id:partyGroup.id)
			} else {
				flash.message = "Unable to update due to: ${GormUtil.errorsToUL(partyGroup)}"
				render(view:'edit',model:[partyGroupInstance:partyGroup])
			}
		}
		else {
			flash.message = "PartyGroup not found with id $params.id"
			redirect(action:"edit",id:params.id)
		}
	}

	@HasPermission(Permission.CompanyCreate)
    def create() {
        [partyGroupInstance: new PartyGroup(params)]
    }

	@HasPermission(Permission.CompanyCreate)
    def save() {

    	Person whom = securityService.userLoginPerson

    	PartyType partyType = PartyType.read(params['partyType.id'])
    	if (! partyType) {
    		flash.message = 'Invalid PartyType was specified in the request'
    		render( view:'create', model:[name:params.name, comment:params.comment])
    		return
    	}

        PartyGroup partyGroup = new PartyGroup()
        partyGroup.name = params.name
        partyGroup.comment = params.comment
        partyGroup.partyType = partyType

        //partyGroup.dateCreated = new Date()
        if (!partyGroup.hasErrors() && partyGroup.save()) {
        	//	Statements to create CLIENT PartyRelationship with the user's Company
        	if ( partyType.id == "COMPANY" ){

	        	def companyParty = whom.company
	        	partyRelationshipService.savePartyRelationship( "CLIENTS", companyParty, "COMPANY", partyGroup, "CLIENT" )

	        	if (params.partner && params.partner == "Y" ) {
					def company = partyRelationshipService.getCompanyOfStaff(securityService.loadCurrentPerson())
					if (company) {
						partyRelationshipService.savePartyRelationship( "PARTNERS", company, "COMPANY", partyGroup, "PARTNER" )
					}
				}
        	}
            flash.message = "PartyGroup $partyGroup created"
            redirect(action:'show',id:partyGroup.id)
        }
        else {
            render(view:'create',model:[partyGroupInstance:partyGroup])
        }
    }

	// TODO : JPM 3/2016 : isAPartner method should be in a service AND shoud take the company as a company instead of looking it up based on the user
	private boolean isAPartner(PartyGroup partyGroup) {
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
		}
		else {
			false
		}
	}

	private boolean isAProjectPartner(PartyGroup partyGroup) {
		Party personCompany = securityService.userLoginPerson.company
		if (personCompany) {
			PartyRelationship.executeQuery('''
				select count(1) from PartyRelationship p
				where p.partyRelationshipType = 'PROJ_PARTNER'
				  and p.roleTypeCodeFrom.id = 'PROJECT'
				  and p.roleTypeCodeTo.id = 'PARTNER'
				  and p.partyIdTo = :partyGroup
			''', [partyGroup: partyGroup])[0] > 0
		}
		else {
			false
		}
	}
}
