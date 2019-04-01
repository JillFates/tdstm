package net.transitionmanager.service

import com.tdsops.common.validation.ConstraintsValidator
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Room
import net.transitionmanager.security.UserLogin
import net.transitionmanager.person.UserPreference
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_BUNDLE
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_DT_FORMAT
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_PROJ
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_ROOM
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_TZ
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.MOVE_BUNDLE
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.MOVE_EVENT
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.sessionOnlyPreferences

@Slf4j
@GrailsCompileStatic(TypeCheckingMode.SKIP)
class UserPreferenceService {
	SpringSecurityService springSecurityService

	/*
	 * Preferences that are unremovable
	 */
	public static final List<String> FIXED_PREFERENCE_CODES = [ CURR_PROJ.name() ]
	private static final List<String> depGraphCheckboxLabels = [
		'bundleConflicts', 'blackBackground', 'appLbl', 'srvLbl', 'dbLbl', 'spLbl', 'slLbl', 'netLbl']
	private static final List<String> depGraphColorBy = [
		'group', 'bundle', 'event', 'environment', 'sourceLocationName', 'targetLocationName']
	private static final List<String> archGraphCheckboxLabels = [
		'showCycles', 'blackBackground', 'appLbl', 'srvLbl', 'dbLbl', 'spLbl', 'slLbl', 'netLbl']
	// ac:Asset Classes, de: Dependencies, hb: Highlight By
	private static final Collection<String> legendTwistieStateValid = ['ac', 'de', 'hb']

	private static final EnumMap<PREF, Map> prefCodeConstraints = [
			(PREF.VIEW_UNPUBLISHED): 		[type: 'boolean'],
			(PREF.ASSET_JUST_PLANNING): 	[type: 'boolean'],
			(PREF.EVENTDB_REFRESH): 		[type: 'integer', inList: ['0', '30', '60', '120', '300', '600']],
			(PREF.TASKMGR_REFRESH): 		[type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
			(PREF.MYTASKS_REFRESH): 		[type: 'integer', inList: ['0', '30', '60', '120', '180', '240', '300']],
			(PREF.TASKGRAPH_REFRESH): 	[type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
			(PREF.TIMELINE_REFRESH): 		[type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
			(PREF.DEP_GRAPH):         	[type: 'string', validator: { String value -> depGraphValidator(value)}],
			(PREF.ARCH_GRAPH): 			[type: 'string', validator: { String value -> archGraphValidator(value)}],
			(PREF.LEGEND_TWISTIE_STATE): 	[type: 'string', validator: { String value -> legendTwistieStateValidator(value)}]
	]

	/**
	 * Validator for Dep Graph.
	 * @param value  The value to be validated
	 * @return  true or false depending on the validation result.
	 */
	static boolean depGraphValidator(String value) {
		def prefs = JSON.parse(value)

		if (!(prefs.colorBy in depGraphColorBy)) {
			return false
		}
		for (label in depGraphCheckboxLabels) {
			if (prefs[label] && prefs[label] != 'true') {
				return false
			}
		}
		Integer maxEdgeCount = NumberUtil.toInteger(prefs.maxEdgeCount, 0)
		if (maxEdgeCount < 1 || maxEdgeCount > 20) {
			return false
		}
		return true
	}

	/**
	 * Validator for Arch Graph.
	 * @param value  The value to be validated
	 * @return  true or false depending on the validation result.
	 */
	static boolean archGraphValidator(String value) {
		def prefs = JSON.parse(value)

		for (label in archGraphCheckboxLabels) {
			// transforming to String because sometimes it's a Boolean.
			if (prefs[label] && prefs[label].toString() != 'true') {
				return false
			}
		}
		Integer levelsUp = NumberUtil.toInteger(prefs.levelsUp, 0)
		if (levelsUp < 0 || levelsUp > 10) {
			return false
		}
		Integer levelsDown = NumberUtil.toInteger(prefs.levelsDown, 0)
		if (levelsDown < 0 || levelsDown > 10) {
			return false
		}
		return true
	}

	/**
	 * Validator for Legend Twistie State.
	 * @param value  The value to be validated
	 * @return  true or false depending on the validation result.
	 */
	static boolean legendTwistieStateValidator(String value) {
		if (!value) {
			return true // it means is just empty (nothing open)
		}
		List<String> prefs = value.split(',')

		if (prefs.size() >= 4) {
			return false
		}
		for (pref in prefs) {
			if (!(pref in legendTwistieStateValid)) {
				return false // Value passed doesn't exist
			}
		}
		true
	}

	/**
	 * Used to retrieve a user preference from the user session or database appropriately.
	 * @param userLogin
	 * @param preference
	 * @param defaultIfNotSet - the default value to return if the preference is not set for the user
	 * @return the found preference value or the default value if not found
	 */
	String getPreference(UserLogin userLogin = null, PREF preference, String defaultIfNotSet = null) {
		getPreference(userLogin, preference.name(), defaultIfNotSet)
	}

	/**
	 * Get the Preference of a user, if the user is the same that the one logged in,
	 * store the value in the session for speed
	 * @param userLogin			User with the requested preference
	 * @param preferenceCode	requested preference code
	 * @param defaultIfNotSet	default value in case that is not set
	 * @return the value of a preference if found or the defaultIfNotSet value
	 */
	@GrailsCompileStatic
	String getPreference(UserLogin userLogin = null, String preferenceCode, String defaultIfNotSet = null) {
		def userPrefValue

		userLogin = resolveUserLogin(userLogin)

		boolean isCurrent = (userLogin != null && userLogin.id == currentUserLoginId)

		// If the userLogin is that of the current user then look in the session first as it maybe cached already
		if (isCurrent && session) {
			userPrefValue = session.getAttribute(preferenceCode)
		}

		// Skip out if the preference is only maintained in the Session
		if (PREF.isSessionOnlyPreference(preferenceCode)) {
			// return the current value from session or the default
			return (userPrefValue != null ? userPrefValue : defaultIfNotSet)
		}

		// If the value was not in the session, then we'll go to the DB for it
		if (userPrefValue == null) {

			// If a user is loggedIn try to get the value from the Preferences Storage of the user
			if (userLogin) {
				UserPreference userPreference = getUserPreference(userLogin, preferenceCode)

				userPrefValue = userPreference?.value
				if (userPrefValue == null) {
					userPrefValue = defaultIfNotSet
				}

				// if we are getting the current user preference store it in the session for speed
				if (isCurrent && session) {
					session.setAttribute(preferenceCode, userPrefValue)
				}

			} else { // If not assign passed default value
				userPrefValue = defaultIfNotSet
			}
		}

		return userPrefValue
	}

	/**
	 * Used to retrieve a specific list or all preference code/values for a user
	 * @param userLogin - the user for which to fetch the data for
	 * @param preferenceCodes - a String of preference codes (comma delimited) to return or blank to fetch all preferences for the user
	 * @return the Map of preference codes and values
	 */
	@GrailsCompileStatic
	@Transactional(readOnly=true)
	Map getPreferences(UserLogin userLogin = null, String preferenceCodes = "") {
		userLogin = resolveUserLogin(userLogin)

		// Split potential preferenceCodes into a List
		List prefCodeList
		if (preferenceCodes) {
			prefCodeList = preferenceCodes.split(',')
		}

		// Query for the user preferences
		List prefList= UserPreference.where {
			userLogin == userLogin
			if (prefCodeList) {
				preferenceCode in prefCodeList
			}
		}.projections {
				property 'preferenceCode'
				property 'value'
		}.list(sort: 'preferenceCode', order: 'asc')

		// Convert to a Map
		Map prefMap = [:]
		prefList.each { prefMap << [ (it[0]) : it[1] ] }

		if (prefCodeList) {
			for (code in prefCodeList) {
				if ( !prefMap.containsKey(code) ) {
					prefMap << [ (code): null ]
				}
			}
		}

		return prefMap
	}

    /**
     * Used by the User Preference Edit dialog. This will return a List<Map> where the map will
     * consist of the following:
     *    code - the Preference Code
     *    label - the human readable name of the code
     *    value - the value of the preference. Note that references will get substituted (e.g. CURR_PROJ returns the name)
	 * @param userLogin - the user for which to retrieve their preferences
     * @return Success Structure with preferences property containing List<Map>
     */
	@GrailsCompileStatic
	@Transactional()
    List<Map> preferenceListForEdit(UserLogin userLogin) {
		List<Map> preferences = []
		List brokenPreferences = []

		List prefList = UserPreference.where {
			userLogin == userLogin
		}.projections {
			property 'preferenceCode'
			property 'value'
		}.list()

		// Convert the list to a List<Map>
		for (pref in prefList) {
				try {
					preferences << [ code: pref[0], value: pref[1], label:  PREF.valueOfName(pref[0]).value() ]
				} catch (InvalidParamException e) {
					brokenPreferences << pref[0] // the preference is invalid/broken, so stage for deletion
				}
			}

		// Sort into an alphabetical list by the Label
		preferences = preferences.sort { a, b -> a.label.toLowerCase() <=> b.label.toLowerCase() }

		// Closure that will be reused in the following for loop
		Closure setValueToReferenceName = { Class domainClass, Map preferenceArg ->
			Object record = domainClass.read(preferenceArg.value)
			if (record) {
				preferenceArg.value = record.toString() + " (${preferenceArg.value})"
			}
		}

		// Transform the value of the preferences that reference domains to display the name of the domain
		// instead of the ID to improve the user experience.
		for (pref in preferences) {
			switch (PREF.valueOfName(pref.code)) {
				case PREF.MOVE_EVENT:
					setValueToReferenceName(MoveEvent.class, pref)
					break
				case PREF.MOVE_BUNDLE:
				case PREF.CURR_BUNDLE:
					setValueToReferenceName(MoveBundle.class, pref)
					break
				case PREF.CURR_PROJ:
					setValueToReferenceName(Project.class, pref)
					break
				case PREF.PARTY_GROUP:
					setValueToReferenceName(PartyGroup.class, pref)
					break
				case PREF.CURR_ROOM:
					setValueToReferenceName(Room.class, pref)
					break
			}
		}

		// if any, remove broken/old preferences remaining in the database
		for (brokePref in brokenPreferences) {
			UserPreference.where {
				userLogin == userLogin
				preferenceCode == brokePref
			}.deleteAll()
			log.warn 'Invalid User preference {} for user {} was deleted', brokePref, userLogin
		}
		
		return preferences
	}

	/**
	 * Sets a list of user preferences defined via map.
	 * @param userLogin - the user to set the preference for
	 * @param preferences - a map containing the preference code and values
	 */
	@Transactional
	boolean setPreferences(UserLogin userLogin = null, Map preferences) {
		preferences.each {
			key, value ->
				setPreference(userLogin, key, value)
		}
	}

	/**
	 * Set the user preference for the provided user account, or the currently authenticated user if null.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call
	 * removeProjectAssociatedPreferences to clear out project specific settings.
	 */
	@Transactional
	boolean setPreference(UserLogin userLogin = null, PREF preferenceCode, value) {
		setPreference(userLogin, preferenceCode.name(), value)
	}

	/**
	 * Set the user preference for the provided user account, or the currently authenticated user if null.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call ??? JPM 11/2017 -- Not sure that this is true
	 * removeProjectAssociatedPreferences to clear out project specific settings.
	 *
	 * @param userLogin - the user to set the preference for
	 * @param preferenceCode - the code to set
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 */
	@Transactional
	boolean setPreference(UserLogin userLogin = null, String preferenceCode, Object value) {
		// If is session only preference just store in the Session and we are done
		if (PREF.isSessionOnlyPreference(preferenceCode)) {
			if (session) {
				def previousValue = session.getAttribute(preferenceCode)
				if (previousValue == null || previousValue != value) {
					session.setAttribute(preferenceCode, value)
				}
			}
			return true
		}

		userLogin = resolveUserLogin(userLogin)
		PREF userPreferenceEnum = PREF.valueOfName(preferenceCode)
		UserPreference userPreference = storePreference(userLogin, userPreferenceEnum, value)

		if (userPreference) {
			// we use the 'value' part of the Enum
			preferenceCode = userPreferenceEnum.name()

			// Note that session does not exist for Quartz jobs so we should check for a session object
			if (session) {
				// Set or update the session with the new value
				def previousValue = session.getAttribute(preferenceCode)
				if (previousValue == null || previousValue != userPreference.value) {
					session.setAttribute(preferenceCode, userPreference.value)

				}
			}
			return true
		}

		return false
	}

	/**
	 * Used to store a user preference into database only and will NOT set the value in to the session.  It will validate preference value
	 * is not null and constraints passes before persisting.
	 * @param userLogin
	 * @param preferenceCode
	 * @param value
	 */
	@Transactional
	UserPreference storePreference(UserLogin userLogin, PREF preferenceCode, Object value) {
		if (!userLogin) return null

		value = value?.toString()

		// TODO : JPM 11/2017 : Testing the value to a string 'null' does not seem correct.
		if (!value || value == 'null') {
			return null
		}

		if (!ConstraintsValidator.validate(value, prefCodeConstraints[preferenceCode])) {
			throw new InvalidParamException()
		}

		UserPreference userPreference = UserPreference.where {
			userLogin == userLogin && preferenceCode == preferenceCode.name()
		}.get()

		boolean isNew = false
		if (userPreference) {
			//	remove the movebundle and event preferences if user switched to different project
			if (preferenceCode == CURR_PROJ && userPreference.value != value) {
				removeProjectAssociatedPreferences(userLogin)
			}
		} else {
			userPreference = new UserPreference(userLogin: userLogin, preferenceCode: preferenceCode.name())
			isNew = true
		}

		if (isNew || userPreference.value != value) {
			log.debug 'storePreference: setting user ({}) preference {}={}', userLogin, preferenceCode, value
			userPreference.value = value
			userPreference.save(flush:true, failOnError:true)
		}
		return userPreference
	}

	/**
	 * Removes an user preference
	 * @param userLogin
	 * @param preference
	 * @return
	 */
	@Transactional
	@GrailsCompileStatic
	boolean removePreference(UserLogin userLogin = null, PREF preference) {
		removePreference(userLogin, preference.name())
	}

	/*
	 * Removes an user preference from database and user http session if present
	 * @param prefCode
	 * @return
	 */
	@Transactional
	@GrailsCompileStatic
	boolean removePreference(String preferenceCode) {
		return removePreference(null, preferenceCode)
	}

	/**
	 * Removes an user preference from database and user http session if present
	 * @param user
	 * @param prefCode
	 * @return
	 */
	@Transactional
	@GrailsCompileStatic
	boolean removePreference(UserLogin userLogin, String prefCode) {
		if (prefCode && ! (prefCode in FIXED_PREFERENCE_CODES) ) {
			userLogin = resolveUserLogin()
			if (! userLogin) {
				throw new UnauthorizedException('User must be logged in to remove preferences')
			}

			int updateCount = UserPreference.where { userLogin == userLogin && preferenceCode == prefCode }.deleteAll()
			if (updateCount) {
				log.debug 'Removed {} preference for user {}', prefCode, userLogin

				//	When removing CURR_PROJ then a number of other preferences should be removed at the same time
				// if ( prefCode == CURR_PROJ.name() ) {
				// 	removeProjectAssociatedPreferences(userLogin)
				// }
			}

			session?.removeAttribute(prefCode)
		}
		return true
	}

	/**
	 * Used to remove all preferences of a UserLogin other than those specified in FIXED_PREFERENCE_CODES list
	 * @param userLogin - the UserLogin to remove preferences for (default current logged in user)
	 * @return void
	 */
	@GrailsCompileStatic
	void resetPreferences(UserLogin userLogin=null) {
		userLogin = resolveUserLogin()
		if (! userLogin) {
			throw InvalidParamException('Must be logged in to reset preferences')
		}

		Map preferences = getPreferences(userLogin)
		for (String prefCode in preferences.keySet()) {
			removePreference(userLogin, prefCode)
		}
	}

	/**
	 * Remove the Move Event and Move Bundle preferences when user switched to different project.
	 */
	@Transactional
	private void removeProjectAssociatedPreferences(UserLogin userLogin) {
		removeProjectAssociatedPreference(userLogin, MOVE_EVENT)
		removeProjectAssociatedPreference(userLogin, CURR_BUNDLE)
		removeProjectAssociatedPreference(userLogin, CURR_ROOM)
	}

	/**
	 * Remove the Move Bundle preferences when user switched to different project.
	 */
	@Transactional
	void removeBundleAssociatedPreferences(UserLogin userLogin) {
		removeProjectAssociatedPreference(userLogin, MOVE_BUNDLE)
		removeProjectAssociatedPreference(userLogin, CURR_BUNDLE)
	}

	/**
	 * Get current project id user preference.
	 * @param userLogin
	 * @return
	 */
	String getCurrentProjectId(UserLogin userLogin = null) {
		getPreference userLogin, CURR_PROJ
	}

	/**
	 * Set current project id user preference
	 * @param userLogin
	 * @param projectId
	 */
	void setCurrentProjectId(UserLogin userLogin = null, projectId) {
		// Session doesn't exist for Quartz jobs
		if (session) {
			// clear Session Lived Preferences
			clearSessionOnlyPreferences()
		}

		// Set the preference
		setPreference userLogin, CURR_PROJ, projectId
	}

	/**
	 * Get current time zone user preference
	 * @param userLogin
	 * @param defaultTimeZoneId
	 * @return
	 */
	String getTimeZone(UserLogin userLogin = null, String defaultTimeZoneId = null) {
		String tzId = getPreference userLogin, CURR_TZ, defaultTimeZoneId
		/* If there's no user preference and no default value's been specified
			we return the TimeUtil default value. */
		if ( !tzId ) {
			tzId = TimeUtil.getDefaultTimeZoneId()
		}
		return tzId
	}

	/**
	 * Set time zone user preference
	 * @param userLogin
	 * @param timeZoneId
	 */
	void setTimeZone(UserLogin userLogin = null, String timeZoneId) {
		setPreference userLogin, CURR_TZ, timeZoneId
	}

	/**
	 * Get current date format user preference
	 * @param userLogin
	 * @return
	 */
	String getDateFormat(UserLogin userLogin = null) {
		getPreference userLogin, CURR_DT_FORMAT, TimeUtil.getDefaultFormatType()
	}

	/**
	 * Set date format user preference
	 * @param userLogin
	 * @param value
	 */
	void setDateFormat(UserLogin userLogin = null, String value) {
		setPreference userLogin, CURR_DT_FORMAT, value
	}

	/**
	 * Get current move event id user preference
	 * @param userLogin
	 * @return
	 */
	String getMoveEventId(UserLogin userLogin = null) {
		getPreference userLogin, MOVE_EVENT
	}

	/**
	 * Set move event id user preference
	 * @param userLogin
	 * @param value
	 */
	void setMoveEventId(UserLogin userLogin = null, value) {
		setPreference userLogin, MOVE_EVENT, value
	}

	/**
	 * Get current move bundle id user preference
	 * @return
	 */
	String getMoveBundleId() {
		getPreference CURR_BUNDLE
	}

	/**
	 * Set move bundle id user preference
	 * @param userLogin
	 * @param value
	 */
	void setMoveBundleId(UserLogin userLogin = null, value) {
		setPreference userLogin, CURR_BUNDLE, value
	}

	/**
	 * Get a map of import user preferences
	 * @return
	 */
	Map<String, String> getImportPreferences() {
		getPreferencesMap(PREF.importPreferenceKeys)
	}

	/**
	 * Get a map of export user preferences
	 * @return
	 */
	Map<String, String> getExportPreferences() {
		getPreferencesMap(PREF.exportPreferenceKeys)
	}

	/**
	 * clear the session only preferences
	 */
	void clearSessionOnlyPreferences(){
		// The session doesn't exist for Quartz jobs
		if (session) {
			for(String pref : sessionOnlyPreferences) {
				session.removeAttribute(pref)
			}
		}
	}

	/**
	 * Return the File Stored Timezones
	 */
	def timezonePickerAreas() {
		JSON.parse(ExportUtil.getResource('templates/timezone/world_map_areas.json').inputStream.text)
	}

	/**
	 * Used to load the current user's UserLogin object if not already loaded
	 * @param userLogin - the reference to the UserLogin object that can be null
	 * @return the passed in userLogin if already assigned otherwise looks up the current thread's UserLogin object
	 */
	private UserLogin resolveUserLogin(UserLogin userLogin=null) {
		if (!userLogin && springSecurityService.loggedIn) {
			return loadCurrentUserLogin()
		} else {
			return userLogin
		}
	}

	/**
	 * Used to read the UserPreference setting from the database
	 * @param userLogin - the user to whom to get the preference for
	 * @param preferenceCode - the code to look for
	 * @return the UserPreference if found otherwise null
	 */
	private UserPreference getUserPreference(UserLogin userLogin, String preferenceCode) {
		UserPreference.get(new UserPreference(userLogin: userLogin, preferenceCode: preferenceCode))
	}

	/**
	 * Get Preference Map by default User by preferenceKeys
	 * @author @tavo_luna
	 */
	private Map<String, String> getPreferencesMap(Collection<PREF> preferenceKeys) {
		Map<String, String> valuesByCode = [:]
		if (springSecurityService.loggedIn&& preferenceKeys) {
			def userPreferences = UserPreference.findAllByUserLoginAndPreferenceCodeInList(
					loadCurrentUserLogin(), preferenceKeys*.value())

			for (preferenceCode in userPreferences) {
				valuesByCode[preferenceCode.preferenceCode] = preferenceCode.value
			}
		}

		valuesByCode
	}

	/**
	 * Remove all project related user preferences
	 * @param userLogin
	 * @param pref
	 */
	private void removeProjectAssociatedPreference(UserLogin userLogin, PREF pref) {
		if (removePreference(userLogin, pref)) {
			log.debug 'Removed {} preference as user switched to other project', pref
			// Load the default preference value into the session?
			getPreference(userLogin, pref)
		}
	}

	/**
	 * Used to gain access to the HttpSession request object when there is an HTTP Request.
	 * For calls when there is no HTTP Session (e.g. Quartz jobs) a null value will be returned.
	 * @return the Http Request session object
	 */
	HttpSession getSession() {
		HttpSession session = null
		if (RequestContextHolder.getRequestAttributes()) {
			GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
			if (grailsWebRequest) {
				HttpServletRequest request = grailsWebRequest.currentRequest
				session = request.session
			}
		}
		return session
	}

	/**
	 * The cached UserLogin id of the authenticated user or null if not authenticated.
	 */
	private Long getCurrentUserLoginId() {
		(Long) springSecurityService.currentUserId
	}

	/**
	 * Creates a proxy UserLogin instance for the currently logged in user's UserLogin for use where
	 * the instance data isn't used, e.g. in a dynamic finder or to set an object reference using the FK.
	 * @return the proxy
	 */
	private UserLogin loadCurrentUserLogin() {
		if (loggedIn) {
			UserLogin.load currentUserLoginId
		}
	}

	/**
	 * Quick check to see if the current user is logged in.
	 * @return true if authenticated and not anonymous
	 */
	boolean isLoggedIn() {
		springSecurityService.loggedIn
	}

}
