import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.SecurityService

/**
 * @author oluna@tdsi.com
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger')
class WsSecurityController implements ControllerMethods {
	SecurityService securityService

	@HasPermission(Permission.UserGeneralAccess)
	def permissions(){
		try {
			renderSuccessJson(securityService.currentUserPermissionList())
		}
		catch (e) {
			handleException e, logger
		}
	}
}
