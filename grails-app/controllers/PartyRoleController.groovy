import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.RoleType

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PartyRoleController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	def list() {
		if (!params.max) params.max = 10
		[partyRoleInstanceList: PartyRole.list(params)]
	}

	def show() {
		PartyRole partyRole = fromParams()
		if (!partyRole) return

		[partyRoleInstance: partyRole]
	}

	def delete() {
		PartyRole partyRole = fromParams()
		if (!partyRole) return

		partyRole.delete()
		flash.message = "PartyRole ${partyRole.party},${partyRole.roleType} deleted"
		redirect(action: 'list')
	}

	def edit() {
		PartyRole partyRole = fromParams()
		if (!partyRole) return

		[partyRoleInstance: partyRole]
	}

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

	def create() {
		[partyRoleInstance: new PartyRole(params)]
	}

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
