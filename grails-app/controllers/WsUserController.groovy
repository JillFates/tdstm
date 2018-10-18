import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.UserPreferenceService
import com.tdsops.common.security.spring.HasPermission

/**
 * Handles WS calls of the UserService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@GrailsCompileStatic
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsUserController')
class WsUserController implements ControllerMethods {

	UserPreferenceService userPreferenceService

	/**
	 * Access a list of one or more user preferences
	 * @param id - a comma separated list of the preference(s) to be retrieved
	 * @example GET ./ws/user/preferences/EVENT,BUNDLE
	 * @return a MAP of the parameters (e.g. preferences:[EVENT:5, BUNDLE:30])
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def preferences() {
		def data = [:]
		for (String preferenceCode in params.id?.toString()?.split(',')) {
			data[preferenceCode] = userPreferenceService.getPreference(preferenceCode)
		}

		renderSuccessJson(preferences: data)
	}

	/**
	 * Sets a user preference through an AJAX call
	 * @param code - the preference code for the preference that is being set
	 * @param value - the value to set the preference to
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def savePreference() {
		userPreferenceService.setPreference(params.code?.toString() ?: '', params.value ?: '')
		renderSuccessJson()
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getUser() {
		UserLogin userLogin = securityService.getUserLogin()
		renderSuccessJson([id: userLogin.id, username: userLogin.username])
	}
}
