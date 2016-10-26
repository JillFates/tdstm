import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Party

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PartyController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	def list() {
		if (!params.max) params.max = 10
		[partyInstanceList: Party.list(params)]
	}

	def show() {
		Party party = fromParams()
		if (!party) return

		[partyInstance: party]
	}

	def delete() {
		Party party = fromParams()
		if (!party) return

		party.delete(flush: true)
		flash.message = "Party ${params.id} deleted"
		redirect(action: 'list')
	}

	def edit() {
		Party party = fromParams()
		if (!party) return

		[partyInstance: party]
	}

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

	def create() {
		[partyInstance: new Party(params)]
	}

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
