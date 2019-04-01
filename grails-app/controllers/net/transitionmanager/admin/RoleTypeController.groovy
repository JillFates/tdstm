package net.transitionmanager.admin

import net.transitionmanager.command.RoleTypeCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.RoleType
import com.tdsops.common.security.spring.HasPermission

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.security.Permission
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.RoleTypeService

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class RoleTypeController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	RoleTypeService roleTypeService

    @HasPermission(Permission.RoleTypeView)
    def list() {
        [roleTypeInstanceList: roleTypeService.list()]
    }

	@HasPermission(Permission.RoleTypeView)
	def show(String id) {
		try {
			[roleTypeInstance: roleTypeService.getById(id, true)]
		} catch (e) {
			flash.message = e.message
			redirect(action: 'list')
		}
	}

	@HasPermission(Permission.RoleTypeDelete)
	def delete(String id) {
		try{
			roleTypeService.delete(id)
			flash.message = "Role type $id deleted"
		} catch (e) {
			flash.message = e.message
		}

		redirect(action: 'list')
	}

	@HasPermission(Permission.RoleTypeEdit)
	def edit(String id) {
		try {
			[roleTypeInstance: roleTypeService.getById(id)]
		} catch (e) {
			flash.message = e.message
			redirect(action: 'list')
		}
	}

	@HasPermission(Permission.RoleTypeEdit)
	def update() {
		RoleTypeCommand command = populateCommandObject(RoleTypeCommand.class)

		try {
			RoleType roleTypeInstance = roleTypeService.update(command)
			flash.message = "Role type $command.description updated"
			redirect(action: 'show', id: roleTypeInstance.id)
		} catch (InvalidParamException e) {
			flash.message = e.message
			render(view: 'edit', model: [roleTypeInstance: command])
		} catch (e) {
			flash.message = e.message
			redirect(action: 'edit', id: params.id)
		}
	}

	@HasPermission(Permission.RoleTypeCreate)
	def create() {
		[roleTypeInstance: new RoleType(params)]
	}

	@HasPermission(Permission.RoleTypeCreate)
	def save() {
		RoleTypeCommand command = populateCommandObject(RoleTypeCommand.class)

		if (roleTypeService.roleTypeExists(command.id)) {
			flash.message = "Role Type $command.id already exists"
			render(view: 'create', model: [roleTypeInstance: command])
			return
		}

		try {
			RoleType roleTypeInstance = roleTypeService.save(command)
			flash.message = "Role type $roleTypeInstance.id created"
			redirect(action: 'list')
		} catch (e) {
			flash.message = e.message
			render(view: 'create', model: [roleTypeInstance: command])
		}
	}
}
