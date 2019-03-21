import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.StartPageEnum as STARTPAGE
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.Timezone
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.UserPreferenceService
/**
 * Handles WS calls of the UserService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
class WsUserController implements ControllerMethods {

	UserPreferenceService userPreferenceService
	PersonService personService
	PartyRelationshipService partyRelationshipService

	/**
	 * Access a list of one or more user preferences
	 * @param id - a comma separated list of the preference(s) to be retrieved
	 * @example GET ./ws/user/preferences/EVENT,BUNDLE
	 * @return a MAP of the parameters (e.g. preferences:[EVENT:5, BUNDLE:30])
	 */
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
    def modelForPreferenceManager() {
        Person person = currentPerson()
        UserLogin userLogin = person.userLogin

        Map model = [
            fixedPreferenceCodes: userPreferenceService.FIXED_PREFERENCE_CODES,
            person: person,
            preferences: userPreferenceService.preferenceListForEdit(userLogin)
        ]
    	renderSuccessJson(model)
    }

	/**
	 * Used by the Manage Staff dialog.
	 * @param: id - ID of the person to be requested
	 * This will return the following:
	 *    person - All the info associated with the person requested
	 *    availableTeams - All teams that this person can be assigned to
	 * @return Success Structure with preferences property containing List<Map>
	 */
	def modelForStaffViewEdit(String id) {
		Project project = getProjectForWs()

		Person person = Person.get(Long.valueOf(id))
		List<RoleType> teams = partyRelationshipService.getTeamRoleTypes()

		renderSuccessJson(person: person.toMap(project), availableTeams: teams)
	}

    /**
     * Used to reset all preferences of a user
     */
    def resetPreferences() {
        userPreferenceService.resetPreferences()
        renderSuccessJson()
    }

    @HasPermission(Permission.UserGeneralAccess)
    def getStartPageOptions() {
        def pageList = [STARTPAGE.PROJECT_SETTINGS.value,
            STARTPAGE.PLANNING_DASHBOARD.value,
            STARTPAGE.ADMIN_PORTAL.value,
            STARTPAGE.USER_DASHBOARD.value]

        renderSuccessJson(pages: pageList)
    }

	/**
	* Sets a user preference through an AJAX call
	* @param code - the preference code for the preference that is being set
	* @param value - the value to set the preference to
	*/
	@HasPermission(Permission.UserGeneralAccess)
	def savePreference() {
		Map preference = request.JSON
		def value = ''
		if (preference.value != null) {
			value = preference.value
		}
		userPreferenceService.setPreference(preference.code?.toString() ?: '', value)
		renderSuccessJson()
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getUser() {
		UserLogin userLogin = securityService.getUserLogin()
		renderSuccessJson([id: userLogin.id, username: userLogin.username])
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getMapAreas() {
		renderSuccessJson(userPreferenceService.timezonePickerAreas())
	}

	@HasPermission(Permission.UserGeneralAccess)
	def getTimezones() {
		renderSuccessJson(Timezone.findAll())
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

	/**
	 * Update the person account that is invoked by the user himself
	 * @param  : map of settings for the person
	 * @return : the person that has been updated
	 */
	@HasPermission(Permission.UserUpdateOwnAccount)
	def updateAccount() {
		Map settings = request.JSON
        Person person = personService.updatePerson(settings, false)
		Map preferences = [
				START_PAGE     : settings.startPage,
				CURR_POWER_TYPE: settings.powerType
		]
		userPreferenceService.setPreferences(null, preferences)
		renderSuccessJson(person)
    }

    /**
     * Update the person account that is invoked by the admin
     * @param  : map of settings for the person
     * @return : the person that has been updated
     */
	@HasPermission(Permission.ProjectStaffEdit)
    def updateAccountAdmin() {
        Map settings = request.JSON
        Person person = personService.updatePerson(settings, true)
        renderSuccessJson(person)
    }

	@HasPermission(Permission.UserGeneralAccess)
	def saveDateAndTimePreferences() {
		Map requestParams = request.JSON
		// Checks that timezone is valid
		def timezone = TimeZone.getTimeZone(requestParams?.timezone.toString())
		userPreferenceService.setTimeZone timezone.getID()

		// Validate date time format
		def datetimeFormat = TimeUtil.getDateTimeFormatType(requestParams?.datetimeFormat.toString())
		userPreferenceService.setDateFormat datetimeFormat

		renderSuccessJson(timezone: timezone.getID(), datetimeFormat: datetimeFormat)
	}
}
