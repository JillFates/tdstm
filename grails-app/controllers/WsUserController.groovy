import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.StartPageEnum as STARTPAGE
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
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
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.UserPreferenceService
import com.tdsops.common.security.spring.HasPermission
/**
 * Handles WS calls of the UserService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
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
	@HasPermission(Permission.UserGeneralAccess)
	def preferences() {
		def data = [:]
		for (String preferenceCode in params.id?.toString()?.split(',')) {
			data[preferenceCode] = userPreferenceService.getPreference(preferenceCode)
		}

		renderSuccessJson(preferences: data)
	}

	/**
	 * Update currentUserPreferences from the server (Copied from PersonController)
	 *
	 * return: An array made up of objects containing a preference code and a value to display.
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def getPreferenceMap() {

		def prefArray = []
		def labelMap = [CONSOLE_TEAM_TYPE: "Console Team Type", SUPER_CONSOLE_REFRESH: "Console Refresh Time",
						CART_TRACKING_REFRESH: "Cart tarcking Refresh Time", BULK_WARNING: "Bulk Warning",
						(PREF.DASHBOARD_REFRESH.value()): "Dashboard Refresh Time", (PREF.CURR_TZ.value()): "Time Zone",
						(PREF.CURR_POWER_TYPE.value()): "Power Type", (PREF.START_PAGE.value()): "Welcome Page",
						(PREF.STAFFING_ROLE.value()): "Default Project Staffing Role",
						(PREF.STAFFING_LOCATION.value()): "Default Project Staffing Location",
						(PREF.STAFFING_PHASES.value()): "Default Project Staffing Phase",
						(PREF.STAFFING_SCALE.value()): "Default Project Staffing Scale", preference: "Preference",
						(PREF.DRAGGABLE_RACK.value()): "Draggable Rack", PMO_COLUMN1: "PMO Column 1 Filter",
						PMO_COLUMN2: "PMO Column 2 Filter", PMO_COLUMN3: "PMO Column 3 Filter",
						PMO_COLUMN4: "PMO Column 4 Filter", (PREF.SHOW_ADD_ICONS.value()): "Rack Add Icons",
						MY_TASK: "My Task Refresh Time"]

		String currTimeZone = TimeUtil.defaultTimeZone
		String currDateTimeFormat = TimeUtil.getDefaultFormatType()

		def prefs = UserPreference.findAllByUserLogin(securityService.loadCurrentUserLogin(), [sort:"preferenceCode"])
		for (pref in prefs) {
			switch (pref.preferenceCode) {
				case PREF.MOVE_EVENT.value():
					prefArray << [prefCode:pref.preferenceCode, value:"Event / " + MoveEvent.get(pref.value).name]
					break

				case PREF.CURR_PROJ.value():
					prefArray << [prefCode:pref.preferenceCode, value:"Project / " + Project.get(pref.value).name]
					break

				case PREF.CURR_BUNDLE.value():
					prefArray << [prefCode:pref.preferenceCode, value:"Bundle / " + MoveBundle.get(pref.value).name]
					break

				case PREF.PARTY_GROUP.value():
					prefArray << [prefCode:pref.preferenceCode, value:"Company / " + (!pref.value.equalsIgnoreCase("All") ?
							PartyGroup.get(pref.value).name : 'All')]
					break

				case PREF.CURR_ROOM.value():
					prefArray << [prefCode:pref.preferenceCode, value:"Room / " + Room.get(pref.value).roomName]
					break

				case PREF.STAFFING_ROLE.value():
					def role = pref.value == "0" ? "All" : RoleType.get(pref.value).description
					prefArray << [prefCode:pref.preferenceCode, value:"Default Project Staffing Role / " + role.substring(role.lastIndexOf(':') + 1)]
					break

				case PREF.AUDIT_VIEW.value():
					def value = pref.value == "0" ? "False" : "True"
					prefArray << [prefCode:pref.preferenceCode, value:"Room Audit View / " + value]
					break

				case PREF.JUST_REMAINING.value():
					def value = pref.value == "0" ? "False" : "True"
					prefArray << [prefCode:pref.preferenceCode, value:"Just Remaining Check / " + value]
					break

				case PREF.CURR_DT_FORMAT:
					currDateTimeFormat = pref.value
					break

				case PREF.CURR_TZ:
					currTimeZone = pref.value
					break

				default:
					prefArray << [prefCode:pref.preferenceCode, value:(labelMap[pref.preferenceCode] ?: pref.preferenceCode) + " / " + pref.value]
					break
			}
		}

		renderSuccessJson(prefMap: prefArray)
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
	def removePreference() {
		String prefCode = params.prefCode
		userPreferenceService.removePreference(null, prefCode)
		renderSuccessJson()
	}

	//Copied from PersonController
	@HasPermission(Permission.UserGeneralAccess)
	def resetPreferences() {
		try {
			Person person = securityService.getUserLogin().person
			UserLogin userLogin = securityService.getPersonUserLogin(person)
			if (!userLogin) {
				log.error "resetPreferences() Unable to find UserLogin for person $person.id $person"
				sendNotFound()
				return
			}
			// TODO : JPM 5/2015 : Change the way that the delete is occurring
			def prePreference = UserPreference.findAllByUserLogin(userLogin).preferenceCode
			prePreference.each { preference ->
				def preferenceInstance = UserPreference.findByPreferenceCodeAndUserLogin(preference, userLogin)
				// When clearing preference, the RefreshMyTasks should be the same.
				if (preferenceInstance.preferenceCode != UserPreferenceEnum.MYTASKS_REFRESH.value()) {
					preferenceInstance.delete()
				}
			}
			renderSuccessJson()
		} catch (e) {
			renderErrorJson(e.message)
		}
	}

	/**
	 * Update the person account that is invoked by the user himself
	 * @param  : person id and input password
	 * @return : pass:"no" or the return of the update method
	 */
	@HasPermission(Permission.UserUpdateOwnAccount)
	def updateAccount() {
		String errMsg = ''
		Map results = [:]
		try {
			//params.id = securityService.currentUserLoginId
			String tzId = userPreferenceService.timeZone
			Person person = personService.updatePerson(params, false)

			if (params.tab) {
				// Funky use-case that we should try to get rid of
				forward(action:'loadGeneral', params:[tab: params.tab, personId:securityService.getUserLogin().person])
				return
			} else {
				results = [ name:person.firstName, tz:tzId ]
			}

		} catch (InvalidParamException | DomainUpdateException e) {
			errMsg = e.message
		} catch (e) {
			log.warn "updateAccount() failed : ${ExceptionUtil.stackTraceToString(e)}"
			errMsg = 'An error occurred during the update process'
		}

		if (errMsg) {
			renderErrorJson(errMsg)
		} else {
			renderSuccessJson(results)
		}
	}
}
