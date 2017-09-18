import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.RoleType
import com.tdsops.common.security.spring.HasPermission

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.security.Permission
import org.grails.datastore.mapping.query.api.Criteria

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class RoleTypeController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

    @HasPermission(Permission.RoleTypeView)
    def list() {

		Criteria query = RoleType.where {
            type == RoleType.TEAM
        }

        query.order('type', 'asc')
                .order('level', 'desc')
                .order('id', 'asc')

        [roleTypeInstanceList: query.list()]
    }

	@HasPermission(Permission.RoleTypeView)
	def show() {
		RoleType roleType = RoleType.get(params.id)
		if (!roleType) {
			flash.message = "RoleType not found with id $params.id"
			redirect(action: 'list')
			return
		}

		[roleTypeInstance: roleType]
	}

	@HasPermission(Permission.RoleTypeDelete)
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

	@HasPermission(Permission.RoleTypeEdit)
	def edit() {
		RoleType roleType = RoleType.get(params.id)
		if (!roleType) {
			flash.message = "RoleType not found with id $params.id"
			redirect(action: 'list')
			return
		}

		[roleTypeInstance : roleType]
	}

	@HasPermission(Permission.RoleTypeEdit)
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

	@HasPermission(Permission.RoleTypeCreate)
	def create() {
		[roleTypeInstance: new RoleType(params)]
	}

	@HasPermission(Permission.RoleTypeCreate)
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
