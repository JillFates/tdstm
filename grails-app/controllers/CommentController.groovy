import grails.plugin.springsecurity.annotation.Secured
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods

/**
 * CRUD for comments
 *
 * @author Diego Scarpa
 */
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class CommentController implements ControllerMethods {

	@HasPermission('CommentView')
	def list() {
		render(view: '_list', model: [])
	}

	@HasPermission('CommentView')
	def editComment() {
		render(view: '_editComment', model: [])
	}

	@HasPermission('CommentView')
	def showComment() {
		render(view: '_showComment', model: [])
	}
}
