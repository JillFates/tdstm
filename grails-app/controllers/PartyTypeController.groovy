import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.PartyType
import com.tdsops.common.security.spring.HasPermission

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class PartyTypeController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	@HasPermission('PartyTypeView')
	def list() {
		if (!params.max) {
			params.max = 10
		}
		[partyTypeInstanceList: PartyType.list(params)]
	}

	@HasPermission('PartyTypeView')
	def show() {
		PartyType partyType = fromParams()
		if (!partyType) return

		[partyType: partyType]
	}

	@HasPermission('PartyTypeDelete')
	def delete() {
		PartyType partyType = fromParams()
		if (!partyType) return

		partyType.delete()
		flash.message = 'PartyType $params.id deleted'
		redirect(action: 'list')
	}

	@HasPermission('PartyTypeEdit')
	def edit() {
		PartyType partyType = fromParams()
		if (!partyType) return

		[partyTypeInstance: partyType]
	}

	@HasPermission('PartyTypeEdit')
	def update() {
		PartyType partyType = fromParams()
		if (!partyType) return

		partyType.properties = params
		if (!partyType.hasErrors() && partyType.save()) {
			flash.message = 'PartyType ${params.id} updated'
			redirect(action: 'show', id: partyType.id)
		}
		else {
			render(view: 'edit', model: [partyTypeInstance: partyType])
		}
	}

	@HasPermission('PartyTypeCreate')
	def create() {
		[partyTypeInstance: new PartyType(params)]
	}

	@HasPermission('PartyTypeCreate')
	def save() {
		def partyType = new PartyType(params)
		partyType.id = params.id
		if (partyType.save()) {
			flash.message = 'PartyType ${partyType.id} created'
			redirect(action: 'show', id: partyType.id)
			return
		}

		render(view: 'create', model: [partyTypeInstance: partyType])
	}

	private PartyType fromParams() {
		PartyType pt = PartyType.get(params.id)
		if (pt) {
			pt
		}
		else {
			flash.message = 'PartyType not found with id ' + params.id
			redirect action: 'list'
		}
	}
}
