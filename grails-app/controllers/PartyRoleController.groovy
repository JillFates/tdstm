import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.RoleType
import com.tdsops.common.security.spring.HasPermission

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PartyRoleController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	@HasPermission(Permission.PartyRoleView)
	def list() {
		if (!params.max) params.max = 10
		[partyRoleInstanceList: PartyRole.list(params)]
	}

	@HasPermission(Permission.PartyRoleView)
	def show() {
		PartyRole partyRole = fromParams()
		if (!partyRole) return

		[partyRoleInstance: partyRole]
	}

	@HasPermission(Permission.PartyRoleDelete)
	def delete() {
		PartyRole partyRole = fromParams()
		if (!partyRole) return

		partyRole.delete()
		flash.message = "PartyRole ${partyRole.party},${partyRole.roleType} deleted"
		redirect(action: 'list')
	}

	@HasPermission(Permission.PartyRoleEdit)
	def edit() {
		PartyRole partyRole = fromParams()
		if (!partyRole) return

		[partyRoleInstance: partyRole]
	}

	@HasPermission(Permission.PartyRoleEdit)
	def update() {
		PartyRole partyRoleDel = fromParams()
		if (!partyRoleDel) return

		def partyRole = new PartyRole(params)
		if (!partyRole.hasErrors() && partyRole.save()) {
			partyRoleDel.delete()
			flash.message = "PartyRole ${partyRole.partyId},${partyRole.roleTypeId} Updated"
			redirect(action: 'show', params: [partyId: partyRole.partyId, roleTypeId: partyRole.roleTypeId])
		}
		else {
			render(view: 'edit', model: [partyRoleInstance: partyRole])
		}
	}

	@HasPermission(Permission.PartyRoleCreate)
	def create() {
		[partyRoleInstance: new PartyRole(params)]
	}

	@HasPermission(Permission.PartyRoleCreate)
	def save() {
		def partyRole = new PartyRole(params)
		if (!partyRole.hasErrors() && partyRole.save()) {
			flash.message = "PartyRole ${partyRole.party},${partyRole.roleType} created"
			redirect(action: 'show', params: [partyId: partyRole.party.id, roleTypeId: partyRole.roleType.id])
		}
		else {
			render(view: 'create', model: [partyRoleInstance: partyRole])
		}
	}

	private PartyRole fromParams() {
		def party = Party.load(params.partyId)
		def roleType = RoleType.load(params.roleTypeId)
		def partyRole = PartyRole.get(new PartyRole(party: party, roleType: roleType))
		if (partyRole) {
			partyRole
		}
		else {
			flash.message = "PartyRole not found with id ${party.id},${roleType.id}"
			redirect(action: 'list')
		}
	}
}
