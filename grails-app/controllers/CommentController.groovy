import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods

/**
 * CRUD for comments
 *
 * @author Diego Scarpa
 */
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class CommentController implements ControllerMethods {

	def list() {
		render(view: '_list', model: [])
	}

	def editComment() {
		render(view: '_editComment', model: [])
	}

	def showComment() {
		render(view: '_showComment', model: [])
	}
}
