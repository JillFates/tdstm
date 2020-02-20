package net.transitionmanager.admin

import net.transitionmanager.command.TeamCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.RoleType
import com.tdsops.common.security.spring.HasPermission

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.security.Permission
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.security.RoleTypeService

/**
 * This is a controller for Team CRUD, a subset of RoleTypes.
 */
@Secured('isAuthenticated()')
class TeamController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	RoleTypeService roleTypeService

	/**
	 * Renders a list view with just the team roles.
	 */
    @HasPermission(Permission.RoleTypeView)
    def list() {
		List teams = roleTypeService.list(RoleType.TYPE_TEAM)
		if (params.containsKey('json')) {
			renderAsJson( teams.collect { [id: it.id, title: it.description] } )
		} else {
			[roleTypeInstanceList: teams]
		}
    }

	/**
	 * Shows and individual Team, based on the id.
	 *
	 * @param id the id of the team.
	 *
	 * @return a rendered view of the team.
	 */
	@HasPermission(Permission.RoleTypeView)
	def show(String id) {
		try {
			[roleTypeInstance: roleTypeService.getById(id, true)]
		} catch (e) {
			flash.message = e.message
			redirect(action: 'list')
		}
	}

	/**
	 * Deletes a Team.
	 *
	 * @param id the team id to delete.
	 *
	 * @return redirects to the list view.
	 */
	@HasPermission(Permission.RoleTypeDelete)
	def delete(String id) {
		try{
			roleTypeService.delete(id)
			flash.message = "Team $id deleted"
		} catch (e) {
			flash.message = e.message
		}

		redirect(action: 'list')
	}

	/**
	 * Renders the exit view of a team.
	 *
	 * @param id The id for the team to edit.
	 * @return
	 */
	@HasPermission(Permission.RoleTypeEdit)
	def edit(String id) {
		try {
			[roleTypeInstance: roleTypeService.getById(id)]
		} catch (e) {
			flash.message = e.message
			redirect(action: 'list')
		}
	}

	/**
	 * Updates a team.
	 *
	 * @param TeamCommand the command object that holds team data for creating and updating a team.
	 *
	 * @return Redirects to the show view on success, and back to the edit view otherwise.
	 */
	@HasPermission(Permission.RoleTypeEdit)
	def update() {
		TeamCommand command = populateCommandObject(TeamCommand.class, false)

		try {
			RoleType roleTypeInstance = roleTypeService.update(command)
			flash.message = "Team $command.description updated"
			redirect(action: 'show', id: roleTypeInstance.id)
		} catch (InvalidParamException e) {
			flash.message = e.message
			render(view: 'edit', model: [roleTypeInstance: command])
		} catch (e) {
			flash.message = e.message
			redirect(action: 'edit', id: params.id)
		}
	}

	/**
	 * Renders the create view.
	 */
	@HasPermission(Permission.RoleTypeCreate)
	def create() {
		[roleTypeInstance: new RoleType(params)]
	}

	/**
	 * Saves a new team.
	 *
	 * @param TeamCommand the command object that holds team data for creating and updating a team.
	 *
	 * @return Redirects to the list view on success, otherwise redirects to the create view.
	 */
	@HasPermission(Permission.RoleTypeCreate)
	def save() {
		TeamCommand command = populateCommandObject(TeamCommand.class, false)

		if (roleTypeService.roleTypeExists(command.id)) {
			flash.message = "Team $command.id already exists"
			render(view: 'create', model: [roleTypeInstance: command])
			return
		}

		try {
			RoleType roleTypeInstance = roleTypeService.save(command)
			flash.message = "Team $roleTypeInstance.id created"
			redirect(action: 'list')
		} catch (e) {
			flash.message = e.message
			render(view: 'create', model: [roleTypeInstance: command])
		}
	}
}
