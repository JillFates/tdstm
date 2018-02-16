import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.UserLogin
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAdminController')
class WsAdminController implements ControllerMethods {

	/**
	 * Provides a list all applications associate to the specified bundle or if id=0 then
	 * it returns all unassigned applications for the user's current project
	 */
	@HasPermission(Permission.UserUnlock)
	def unlockAccount() {
		securityService.unlockAccount(UserLogin.get(params.id))
		renderSuccessJson()
	}
}
