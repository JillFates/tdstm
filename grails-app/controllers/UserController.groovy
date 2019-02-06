import net.transitionmanager.controller.ControllerMethods
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.security.Permission
import net.transitionmanager.service.UserService

import static net.transitionmanager.utils.Profiler.KEY_NAME

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class UserController implements ControllerMethods {

	UserService userService

	/**
	 * Toggles the profiler session variable on/off for performance troubleshooting.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def profilerToggle() {
		def value = session[KEY_NAME]
		if (value) {
			session.removeAttribute(KEY_NAME)
		} else {
			session[KEY_NAME] = KEY_NAME
		}

		render "The Profiler is: " + profilerState()
	}

	/**
	 * Show the state of the profiler session variable.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def profilerStatus() {
		render "The Profiler is: " + profilerState()
	}

	private String profilerState() {
		session[KEY_NAME] == KEY_NAME ? "ENABLED" : "DISABLED"
	}

	/**
	 * Return a map with the most relevant information regarding the current user.
	 * @return a map containing information about the user (project, person, login, etc.).
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def context() {
		renderSuccessJson(userService.getUserContext().toMap())
	}
}
