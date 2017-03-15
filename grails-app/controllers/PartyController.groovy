import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Party
import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PartyController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	@HasPermission('PartyView')
	def list() {
		if (!params.max) params.max = 10
		[partyInstanceList: Party.list(params)]
	}

	@HasPermission('PartyView')
	def show() {
		Party party = fromParams()
		if (!party) return

		[partyInstance: party]
	}

	@HasPermission('PartyDelete')
	def delete() {
		Party party = fromParams()
		if (!party) return

		party.delete(flush: true)
		flash.message = "Party ${params.id} deleted"
		redirect(action: 'list')
	}

	@HasPermission('PartyEdit')
	def edit() {
		Party party = fromParams()
		if (!party) return

		[partyInstance: party]
	}

	@HasPermission('PartyEdit')
	def update() {
		Party party = fromParams()
		if (!party) return

		party.properties = params
		if (!party.hasErrors() && party.save()) {
			flash.message = "Party ${params.id} updated"
			redirect(action: 'show', id: party.id)
		}
		else {
			render(view: 'edit', model: [partyInstance: party
			])
		}
	}

	@HasPermission('PartyCreate')
	def create() {
		[partyInstance: new Party(params)]
	}

	@HasPermission('PartyCreate')
	def save() {
		def party = new Party(params)
		if (!partyww.hasErrors() && party.save()) {
			flash.message = "Party ${party.id} created"
			redirect(action: 'show', id: party.id)
		}
		else {
			render(view: 'create', model: [partyInstance: party])
		}
	}

	private Party fromParams() {
		def party = Party.get(params.id)
		if (party) {
			party
		}
		else {
			flash.message = "Party not found with id ${params.id}"
			redirect(action: 'list')
		}
	}
}
