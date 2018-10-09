import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PartyGroupService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.UserPreferenceService
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * This Controller handles CRUD operations for PartyGroups(Companies).
 */
@Secured('isAuthenticated()')
class PartyGroupController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction  = 'list'

	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService   partyRelationshipService
	ProjectService             projectService
	UserPreferenceService      userPreferenceService

	PartyGroupService partyGroupService

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

		String sortIndex = params.sidx ?: 'companyName'
		String sortOrder = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows
		Map filterParams = [companyName: params.companyName, dateCreated: params.dateCreated,
							lastUpdated: params.lastUpdated, partner: params.partner]

		// Validate that the user is sorting by a valid column
		if (!sortIndex in filterParams) {
			sortIndex = 'companyName'
		}

		renderAsJson(partyGroupService.list(filterParams, sortIndex, sortOrder, maxRows, currentPage, rowOffset))
	}

	/**
	 * Renders an individual PartyGroup record.
	 *
	 * @param id the id of the partyGroup to lookup
	 *
	 * @return a map passed to the show view for partyGroup.
	 */
	@HasPermission(Permission.CompanyView)
	def show(Long id) {
		PartyGroup partyGroup = PartyGroup.get(id)
		userPreferenceService.setPreference(PREF.PARTY_GROUP, partyGroup?.id)

		if (!partyGroup) {
			flash.message = "PartyGroup not found with id $id"
			redirect(action: "list")
			return
		}

		[partyGroupInstance: partyGroup, partner: partyGroupService.isAPartner(partyGroup), projectPartner: partyGroupService.isAProjectPartner(partyGroup)]
	}

	/**
	 * Deletes a partyGroup
	 *
	 * @param id the id of the partyGroup to delete.
	 *
	 * @return redirects to the list view.
	 */
	@HasPermission(Permission.CompanyDelete)
	def delete(Long id) {
		flash.message = partyGroupService.delete(id)
		redirect(action: "list")
	}

	/**
	 * Renders the edit view for a partyGroup.
	 *
	 * @param id the id of the party group to edit.
	 *
	 * @return the edit view for the partyGroup.
	 */
	@HasPermission(Permission.CompanyEdit)
	def edit(Long id) {
		PartyGroup partyGroup = PartyGroup.get(id)
		userPreferenceService.setPreference(PREF.PARTY_GROUP, partyGroup?.id)

		if (!partyGroup) {
			flash.message = "PartyGroup not found with id $id"
			redirect(action: "list")
			return
		}

		[partyGroupInstance: partyGroup, partner: partyGroupService.isAPartner(partyGroup), projectPartner: partyGroupService.isAProjectPartner(partyGroup)]
	}

	/**
	 * Updates a party group.
	 *
	 * @param id the id of the party group to update.
	 *
	 * @return redirects to the show view if successful and the edit view otherwise.
	 */
	@HasPermission(Permission.CompanyEdit)
	def update(Long id) {
		PartyGroup partyGroup = partyGroupService.update(id, params)

		if (partyGroup) {
			if (!partyGroup.hasErrors()) {
				flash.message = "PartyGroup $partyGroup updated"
				redirect(action: "show", id: partyGroup.id)
			} else {
				flash.message = "Unable to update due to: ${GormUtil.errorsToUL(partyGroup)}"
				render(view: 'edit', model: [partyGroupInstance: partyGroup])
			}
		} else {
			flash.message = "PartyGroup not found with id $id"
			redirect(action: "edit", id: id)
		}
	}

	/**
	 * Renders the create view using Grails conventions
	 */
	@HasPermission(Permission.CompanyCreate)
	def create() {}

	/**
	 * Creates a new partyGroup.
	 *
	 * @param name the new name of the group.
	 * @param comment a comment/description for the group.
	 * @param partner if the group is a partner Y or N.
	 *
	 * @return if successful renders the show view or goes back to the create view.
	 */
	@HasPermission(Permission.CompanyCreate)
	def save(String name, String comment, String partner) {

		PartyType partyType = PartyType.read(params['partyType.id'])
		if (!partyType) {
			flash.message = 'Invalid PartyType was specified in the request'
			render(view: 'create', model: [name: params.name, comment: params.comment])
			return
		}

		PartyGroup partyGroup = partyGroupService.save(name, comment, partner, partyType)

		if (!partyGroup.hasErrors()) {
			flash.message = "PartyGroup $partyGroup created"
			redirect(action: 'show', id: partyGroup.id)
		} else {
			render(view: 'create', model: [partyGroupInstance: partyGroup])
		}
	}
}
