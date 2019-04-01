package net.transitionmanager.application

import grails.plugin.springsecurity.annotation.Secured
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission

/**
 * CRUD for comments
 *
 * @author Diego Scarpa
 */
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class CommentController implements ControllerMethods {

	@HasPermission(Permission.CommentView)
	def list() {
		render(view: '_list', model: [])
	}

	@HasPermission([Permission.CommentCreate, Permission.CommentEdit])
	def editComment() {
		render(view: '_editComment', model: [])
	}

	@HasPermission(Permission.CommentView)
	def showComment() {
		render(view: '_showComment', model: [])
	}
}
