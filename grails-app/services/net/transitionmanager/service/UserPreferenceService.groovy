package net.transitionmanager.service

import com.tdsops.common.validation.ConstraintsValidator
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference

import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_BUNDLE
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_DT_FORMAT
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_PROJ
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_ROOM
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.CURR_TZ
import static com.tdsops.tm.enums.domain.UserPreferenceEnum.MOVE_EVENT

@Slf4j(value='logger')
class UserPreferenceService implements ServiceMethods {

	SecurityService securityService

	// defaults holds global defaults for certain values
	// TODO - load these from application settings
	protected static final Map<String, Object> defaults = [
		CURR_TZ        :'GMT',
		PRINTER_COPIES : 2
	]

	private static final List<String> depGraphCheckboxLabels = ['bundleConflicts', 'blackBackground', 'appLbl', 'srvLbl',
	                                                            'dbLbl', 'spLbl', 'slLbl', 'netLbl']
	private static final List<String> depGraphColorBy = ['group', 'bundle', 'event', 'environment',
	                                                     'sourceLocation', 'targetLocation']
	private static final List<String> archGraphCheckboxLabels = ['showCycles', 'blackBackground', 'appLbl', 'srvLbl',
	                                                             'dbLbl', 'spLbl', 'slLbl', 'netLbl']
	private static final Collection<String> legendTwistieStateValid = ['ac', 'de', 'hb'] // ac:Asset Classes, de: Dependencies, hb: Highlight By

	private static final Map<String, Map> prefCodeConstraints = [
		viewUnpublished:  [type: 'boolean'],
		RefreshEventDB:   [type: 'integer', inList: ['0', '30', '60', '120', '300', '600']],
		RefreshTaskMgr:   [type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
		RefreshMyTasks:   [type: 'integer', inList: ['0', '30', '60', '120', '180', '240', '300']],
		RefreshTaskGraph: [type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
		RefreshTimeline:  [type: 'integer', inList: ['0', '60', '120', '180', '240', '300']],
		depGraph:         [type: 'string', validator: { String value ->
			def prefs = JSON.parse(value)

			if (!(prefs.colorBy in depGraphColorBy)) {
				return false
			}

			for (label in depGraphCheckboxLabels) {
				if (prefs[label] && prefs[label] != 'true') {
					return false
				}
			}

			if (!(Integer.parseInt(prefs.maxEdgeCount) in (1..20))) {
				return false
			}

			return true
		}],
		archGraph: [type: 'string', validator: { String value ->
			def prefs = JSON.parse(value)

			for (label in archGraphCheckboxLabels) {
				if (prefs[label] && prefs[label] != 'true') {
					return false
				}
			}

			if (!(Integer.parseInt(prefs.levelsUp) in (0..10))) {
				return false
			}

			if (!(Integer.parseInt(prefs.levelsDown) in (0..10))) {
				return false
			}

			return true
		}],
		legendTwistieState: [type: 'string', validator: { String value ->
			def prefs = value.split(',')
			if (prefs.length == 0) {
				return true // it means is just empty (nothing open)
			}
			if (prefs.length > 4) {
				return false
			}

			for (pref in prefs) {
				if (!(pref in legendTwistieStateValid)) {
					return false // Value passed doesn't exist
				}
			}

			true
		}]
	]

	String getPreference(UserLogin userLogin = null, UserPreferenceEnum preference, String defaultIfNotSet = null) {
		getPreference userLogin, preference.value(), defaultIfNotSet
	}

	String getPreference(UserLogin userLogin = null, String preferenceCode, String defaultIfNotSet = null) {
		userLogin = resolve(userLogin)
		if (!userLogin) return

		boolean isCurrent = userLogin.id == securityService.currentUserLoginId
		if (isCurrent) {
			Map holder = (Map) session.getAttribute(preferenceCode)
			if (holder != null) {
				return holder[preferenceCode] ?: defaultIfNotSet
			}
		}

		UserPreference userPreference = getUserPreference(userLogin, preferenceCode)
		if (isCurrent) {
			session.setAttribute preferenceCode, [(preferenceCode): userPreference?.value]
		}

		userPreference?.value ?: defaultIfNotSet
	}

	/**
	 * Set the user preference for the provided user account, or the currently authenticated user if null.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call
	 * removeProjectAssociatedPreferences to clear out project specific settings.
	 */
	@Transactional
	boolean setPreference(UserLogin userLogin = null, UserPreferenceEnum preferenceCode, value) {
		setPreference(userLogin, preferenceCode.value(), value)
	}

	/**
	 * Set the user preference for the provided user account, or the currently authenticated user if null.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call
	 * removeProjectAssociatedPreferences to clear out project specific settings.
	 *
	 * @param userLogin - the user to set the preference for
	 * @param preferenceCode - the code to set
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 */
	@Transactional
	boolean setPreference(UserLogin userLogin = null, String preferenceCode, value) {
		userLogin = resolve(userLogin)
		if (!userLogin) return

		boolean saved = false
		value = value?.toString()

		logger.debug 'setPreference: setting user ({}) preference {}={}', userLogin, preferenceCode, value

		// Date start = new Date()

		if (value && value != "null" && userLogin) {
			//remove from the session cache
			session.removeAttribute(preferenceCode)

			UserPreference userPreference = getUserPreference(userLogin, preferenceCode)
			String prefValue = userPreference?.value

			//logger.debug 'setPreference() phase 1 took {}', TimeUtil.elapsed(start)
			//start = new Date()

			//	remove the movebundle and event preferences if user switched to different project
			if (preferenceCode == CURR_PROJ.value() && prefValue && prefValue != value) {
				removeProjectAssociatedPreferences(userLogin)
			}

			//logger.debug 'setPreference() phase 2 took {}', TimeUtil.elapsed(start)
			//start = new Date()

			if (userPreference == null) {
				userPreference = new UserPreference(userLogin: userLogin, preferenceCode: preferenceCode)
			}
			userPreference.value = value
			save userPreference, true
			saved = !userPreference.hasErrors()

			// logger.debug 'setPreference() phase 3 took {}', TimeUtil.elapsed(start)
			// start = new Date()

			// call getPreference() to load map into session
			getPreference(userLogin, preferenceCode)

			// logger.debug 'setPreference() phase 4 took {}', TimeUtil.elapsed(start)
		}

		return saved
	}

	/* Saves a user preference after making sure that it passes validation using the constraints map */
	@Transactional
	void savePreference(String code, value) {

		if (!(code in prefCodeConstraints)) {
			throw new InvalidRequestException()
		}

		if (!ConstraintsValidator.validate(value, prefCodeConstraints[code])) {
			throw new InvalidParamException()
		}

		setPreference(code, value)
	}

	@Transactional
	boolean removePreference(UserLogin userLogin = null, UserPreferenceEnum preference) {
		removePreference(userLogin, preference.value())
	}

	/**
	 * @deprecated
	 */
	@Transactional
	boolean removePreference(UserLogin user = null, String prefCode) {
		user = resolve(user)
		if (!user) return

		int updateCount = UserPreference.where { userLogin == user && preferenceCode == prefCode }.deleteAll()
		if (updateCount) {
			logger.debug 'Removed {} preference', prefCode

			//	remove the movebundle and event preferences
			if (prefCode == CURR_PROJ.value()) {
				removeProjectAssociatedPreferences(user)
			}
		}

		session.removeAttribute prefCode
	}

	/**
	 * Remove the Move Event and Move Bundle preferences when user switched to different project.
	 */
	@Transactional
	private void removeProjectAssociatedPreferences(UserLogin userLogin) {
		removeProjectAssociatedPreference userLogin, MOVE_EVENT
		removeProjectAssociatedPreference userLogin, CURR_BUNDLE
		removeProjectAssociatedPreference userLogin, CURR_ROOM
	}

	String getCurrentProjectId(UserLogin userLogin = null) {
		getPreference userLogin, CURR_PROJ
	}
	void setCurrentProjectId(UserLogin userLogin = null, projectId) {
		setPreference userLogin, CURR_PROJ, projectId
	}

	String getTimeZone(UserLogin userLogin = null, String defaultTimeZoneId = null) {
		getPreference userLogin, CURR_TZ, defaultTimeZoneId
	}
	void setTimeZone(UserLogin userLogin = null, String timeZoneId) {
		setPreference userLogin, CURR_TZ, timeZoneId
	}

	String getDateFormat(UserLogin userLogin = null) {
		getPreference userLogin, CURR_DT_FORMAT, TimeUtil.getDefaultFormatType()
	}
	void setDateFormat(UserLogin userLogin = null, String value) {
		setPreference userLogin, CURR_DT_FORMAT, value
	}

	String getMoveEventId(UserLogin userLogin = null) {
		getPreference userLogin, MOVE_EVENT
	}
	void setMoveEventId(UserLogin userLogin = null, value) {
		setPreference userLogin, MOVE_EVENT, value
	}

	String getMoveBundleId() {
		getPreference CURR_BUNDLE
	}
	void setMoveBundleId(UserLogin userLogin = null, value) {
		setPreference userLogin, CURR_BUNDLE, value
	}

	Map<String, String> getImportPreferences() {
		getPreferencesMap(UserPreferenceEnum.importPreferenceKeys)
	}

	Map<String, String> getExportPreferences() {
		getPreferencesMap(UserPreferenceEnum.exportPreferenceKeys)
	}

	/**
	 * Return the File Stored Timezones
	 */
	def timezonePickerAreas() {
		JSON.parse(ExportUtil.getResource('timezone/world_map_areas.json').inputStream.text)
	}

	private UserLogin resolve(UserLogin userLogin) {
		if (!userLogin && securityService.loggedIn) {
			securityService.loadCurrentUserLogin()
		}
		else {
			userLogin
		}
	}

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

	private void removeProjectAssociatedPreference(UserLogin userLogin, UserPreferenceEnum pref) {
		if (removePreference(userLogin, pref)) {
			logger.debug 'Removed {} preference as user switched to other project', pref
			getPreference(userLogin, pref)
		}
	}
}
