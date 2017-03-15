import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.RoleType
import com.tdsops.common.security.spring.HasPermission

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class RoleTypeController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	@HasPermission('RoleTypeView')
	def list() {
		if (!params.max) params.max = 25

		List<RoleType> roleTypes = RoleType.createCriteria().list {
			and {
				order('type','asc')
				order('level','desc')
				order('id', 'asc')
			}
		}
		[roleTypeInstanceList: roleTypes]
	}

	@HasPermission('RoleTypeView')
	def show() {
		RoleType roleType = RoleType.get(params.id)
		if (!roleType) {
			flash.message = "RoleType not found with id $params.id"
			redirect(action: 'list')
			return
		}

		[roleTypeInstance: roleType]
	}

	@HasPermission('RoleTypeDelete')
	def delete() {
		try{
			RoleType roleType = RoleType.get(params.id)
			if (roleType) {
				roleType.delete(flush: true)
				flash.message = "RoleType $params.id deleted"
			}
			else {
				flash.message = "RoleType not found with id $params.id"
			}
		}
		catch (e) {
			flash.message = e.message
		}

		redirect(action: 'list')
	}

	@HasPermission('RoleTypeEdit')
	def edit() {
		RoleType roleType = RoleType.get(params.id)
		if (!roleType) {
			flash.message = "RoleType not found with id $params.id"
			redirect(action: 'list')
			return
		}

		[roleTypeInstance : roleType]
	}

	@HasPermission('RoleTypeEdit')
	def update() {
		RoleType roleType = RoleType.get(params.roleTypeId)
		if (roleType) {
			roleType.properties = params
			if (!roleType.hasErrors() && roleType.save()) {
				flash.message = "RoleType $params.description updated"
				redirect(action: 'show', id: roleType.id)
			}
			else {
				render(view: 'edit', model: [roleTypeInstance: roleType])
			}
		}
		else {
			flash.message = "RoleType not found with id $params.id"
			redirect(action: 'edit', id: params.id)
		}
	}

	@HasPermission('RoleTypeCreate')
	def create() {
		[roleTypeInstance: new RoleType(params)]
	}

	@HasPermission('RoleTypeCreate')
	def save() {
		boolean idCheck = false
		if (RoleType.exists(params.id)) {
			flash.message = "Role Type $params.id already exists"
			idCheck = true
		}

		RoleType roleType = new RoleType(params)
		roleType.id = params.id
		if (!idCheck && !roleType.hasErrors() && roleType.save()) {
			flash.message = "RoleType $roleType.id created"
			redirect(action: 'list')
			return
		}

		render(view: 'create', model: [roleTypeInstance: roleType])
	}
}
