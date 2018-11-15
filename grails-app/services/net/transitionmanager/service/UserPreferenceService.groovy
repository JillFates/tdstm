package net.transitionmanager.service

import com.tdsops.common.validation.ConstraintsValidator
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.transform.TypeCheckingMode
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference

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
class UserPreferenceService implements ServiceMethods {

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

	private static final EnumMap<UserPreferenceEnum, Map> prefCodeConstraints = [
			(UserPreferenceEnum.VIEW_UNPUBLISHED): 		[type: 'boolean'],
			(UserPreferenceEnum.ASSET_JUST_PLANNING): 	[type: 'boolean'],
			(UserPreferenceEnum.EVENTDB_REFRESH): 		[type: 'integer', inList: ['0', '30', '60', '120', '300', '600']],
			(UserPreferenceEnum.TASKMGR_REFRESH): 		[type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
			(UserPreferenceEnum.MYTASKS_REFRESH): 		[type: 'integer', inList: ['0', '30', '60', '120', '180', '240', '300']],
			(UserPreferenceEnum.TASKGRAPH_REFRESH): 	[type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
			(UserPreferenceEnum.TIMELINE_REFRESH): 		[type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
			(UserPreferenceEnum.DEP_GRAPH):         	[type: 'string', validator: { String value -> depGraphValidator(value)}],
			(UserPreferenceEnum.ARCH_GRAPH): 			[type: 'string', validator: { String value -> archGraphValidator(value)}],
			(UserPreferenceEnum.LEGEND_TWISTIE_STATE): 	[type: 'string', validator: { String value -> legendTwistieStateValidator(value)}]
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
	String getPreference(UserLogin userLogin = null, UserPreferenceEnum preference, String defaultIfNotSet = null) {
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
	String getPreference(UserLogin userLogin = null, String preferenceCode, String defaultIfNotSet = null) {
		def userPrefValue

		userLogin = resolveUserLogin(userLogin)

		boolean isCurrent = (userLogin != null && userLogin.id == securityService.currentUserLoginId)

		// If the userLogin is that of the current user then look in the session first as it maybe cached already
		if (isCurrent && session) {
			userPrefValue = session.getAttribute(preferenceCode)
		}

		// Skip out if the preference is only maintained in the Session
		if (UserPreferenceEnum.isSessionOnlyPreference(preferenceCode)) {
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
	@Transactional(readOnly=true)
    List<Map> preferenceEditList(UserLogin userLogin) {
		List<Map> preferences = []

		List prefList = UserPreference.where {
			userLogin == userLogin
		}.projections {
			property 'preferenceCode'
			property 'value'
		}.list()

		// Convert the list to a List<Map>
		for (pref in prefList) {
			preferences << [ code: pref[0], value: pref[1], label:  UserPreferenceEnum.valueOfNameOrValue(pref[0]).toString() ]
		}

		// Sort into an alphabetical list by the Label
		preferences = preferences.sort { a, b -> a.label.toLowerCase() <=> b.label.toLowerCase() }

		return preferences

		/*
		def preferences = UserPreference.findAllByUserLogin(securityService.loadCurrentUserLogin(), [sort:"preferenceCode"])
		for (pref in preferences) {
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

				// This doesn't make any sense - variable is set but never used
				case PREF.CURR_DT_FORMAT:
					currDateTimeFormat = pref.value
					break

				// This doesn't make any sense - variable is set but never used
				case PREF.CURR_TZ:
					currTimeZone = pref.value
					break

				default:
					prefArray << [prefCode:pref.preferenceCode, value:(labelMap[pref.preferenceCode] ?: pref.preferenceCode) + " / " + pref.value]
					break
			}
        }
		*/
	}

	/**
	 * Set the user preference for the provided user account, or the currently authenticated user if null.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call
	 * removeProjectAssociatedPreferences to clear out project specific settings.
	 */
	@Transactional
	boolean setPreference(UserLogin userLogin = null, UserPreferenceEnum preferenceCode, value) {
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
		if (UserPreferenceEnum.isSessionOnlyPreference(preferenceCode)) {
			if (session) {
				def previousValue = session.getAttribute(preferenceCode)
				if (previousValue == null || previousValue != value) {
					session.setAttribute(preferenceCode, value)
				}
			}
			return true
		}

		userLogin = resolveUserLogin(userLogin)
		UserPreferenceEnum userPreferenceEnum = UserPreferenceEnum.valueOfNameOrValue(preferenceCode)
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
	UserPreference storePreference(UserLogin userLogin, UserPreferenceEnum preferenceCode, Object value) {
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
			save(userPreference, true)
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
	boolean removePreference(UserLogin userLogin = null, UserPreferenceEnum preference) {
		removePreference(userLogin, preference.name())
	}

	/*
	 * Removes an user preference from database and user http session if present
	 * @param prefCode
	 * @return
	 */
	@Transactional
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
		getPreferencesMap(UserPreferenceEnum.importPreferenceKeys)
	}

	/**
	 * Get a map of export user preferences
	 * @return
	 */
	Map<String, String> getExportPreferences() {
		getPreferencesMap(UserPreferenceEnum.exportPreferenceKeys)
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
		if (!userLogin && securityService.loggedIn) {
			return securityService.loadCurrentUserLogin()
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
	private Map<String, String> getPreferencesMap(Collection<UserPreferenceEnum> preferenceKeys) {
		Map<String, String> valuesByCode = [:]
		if (securityService.loggedIn && preferenceKeys) {
			def userPreferences = UserPreference.findAllByUserLoginAndPreferenceCodeInList(
					securityService.loadCurrentUserLogin(), preferenceKeys*.value())

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
	private void removeProjectAssociatedPreference(UserLogin userLogin, UserPreferenceEnum pref) {
		if (removePreference(userLogin, pref)) {
			log.debug 'Removed {} preference as user switched to other project', pref
			// Load the default preference value into the session?
			getPreference(userLogin, pref)
		}
	}

}
