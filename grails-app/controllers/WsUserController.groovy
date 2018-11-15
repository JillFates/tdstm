import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.TimeUtil
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PersonService
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
	PersonService personService

	/**
	 * Access a list of one or more user preferences
	 * @param id - a comma separated list of the preference(s) to be retrieved
	 * @example GET ./ws/user/preferences/EVENT,BUNDLE
	 * @return a MAP of the parameters (e.g. preferences:[EVENT:5, BUNDLE:30])
	 */
	@GrailsCompileStatic(TypeCheckingMode.SKIP)
	@HasPermission(Permission.UserGeneralAccess)
	def preferences(String id) {
        UserLogin userLogin = currentPerson().userLogin
        Map preferences = userPreferenceService.getPreferences(userLogin, id)
    	renderSuccessJson(preferences: preferences)
	}

    /**
     * Used by the User Preference Edit dialog. This will return a List<Map> where the map will
     * consist of the following:
     *    code - the Preference Code
     *    label - the human readable name of the code
     *    value - the value of the preference. Note that references will get substituted (e.g. CURR_PROJ returns the name)
     * @return Success Structure with preferences property containing List<Map>
     */
    def preferencesForEdit() {
        Person person = currentPerson()
        UserLogin userLogin = person.userLogin
        List<Map> preferences = userPreferenceService.preferenceEditList(userLogin)
        Map model = [
            fixedPreferenceCodes: userPreferenceService.FIXED_PREFERENCE_CODES,
            person: [firstName: person.firstName],
            preferences: preferences
        ]
    	renderSuccessJson(model)
    }

    /**
     * Used to reset all preferences of a user
     */
    def resetPreferences() {
        userPreferenceService.resetPreferences()
        renderSuccessJson()
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

	@HasPermission(Permission.UserGeneralAccess)
	def getPerson() {
		Person person = securityService.getUserLogin().person
		renderSuccessJson([person:person])
	}

	@HasPermission(Permission.UserGeneralAccess)
	def removePreference(String id) {
		userPreferenceService.removePreference(id)
		renderSuccessJson()
	}
}
