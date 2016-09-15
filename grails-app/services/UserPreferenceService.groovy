import com.tdsops.common.validation.ConstraintsValidator
import com.tdssrc.grails.GormUtil
import grails.converters.JSON
import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.web.util.WebUtils

import javax.servlet.http.HttpSession

/**
 * The Service is transactional (http://docs.grails.org/2.3.11/guide/services.html#declarativeTransactions)
 */
class UserPreferenceService {

	static transactional = true
	def grailsApplication
	def securityService

	// defaults holds global defaults for certain values
	// TODO - load these from application settings
	protected static defaults = [
		'CURR_TZ'        :'GMT',
		'PRINTER_COPIES' : 2
	]

	protected static Map SECURITY_ROLES = ['USER':true,'EDITOR':true,'SUPERVISOR':true]

	private static Map prefCodeConstraints = [
		viewUnpublished: [
			type: 'boolean'
		],
		RefreshEventDB: [
			type: 'integer',
			inList: ['0', '30', '60', '120', '300', '600']
		],
		RefreshTaskMgr: [
			type: 'integer',
			inList: ['0', '60', '120', '180', '240', '300']
		],
		RefreshMyTasks: [
			type: 'integer',
			inList: ['0', '30', '60', '120', '180', '240', '300']
		],
		RefreshTaskGraph: [
			type: 'integer',
			inList: ['0', '60', '120', '180', '240', '300']
		],
		RefreshTimeline: [
			type: 'integer',
			inList: ['0', '60', '120', '180', '240', '300']
		],
		depGraph: [
			type: 'string',
			validator: {
				def prefs = JSON.parse(it)
				def checkboxLabels = ['bundleConflicts', 'blackBackground', 'appLbl', 'srvLbl', 'dbLbl', 'spLbl', 'slLbl', 'netLbl']

				if ( ! (prefs.colorBy in ['group', 'bundle', 'event', 'environment', 'sourceLocation', 'targetLocation']) )
					return false

				checkboxLabels.each { label ->
					if (prefs[label] && prefs[label] != 'true')
						return false
				}
				if ( ! (Integer.parseInt(prefs.maxEdgeCount) in (1..20)) )
					return false
				return true
			}
		],
		archGraph: [
			type: 'string',
			validator: {
				def prefs = JSON.parse(it)
				def checkboxLabels = ['showCycles', 'blackBackground', 'appLbl', 'srvLbl', 'dbLbl', 'spLbl', 'slLbl', 'netLbl']
				checkboxLabels.each { label ->
					if (prefs[label] && prefs[label] != 'true')
						return false
				}

				if ( ! (Integer.parseInt(prefs.levelsUp) in (0..10)) )
					return false
				if ( ! (Integer.parseInt(prefs.levelsDown) in (0..10)) )
					return false

				return true
			}
		],
		legendTwistieState: [
			type: 'string',
			validator: {
				def prefs = it.toCharArray()
				prefs.each { pref ->
					if (pref != '0' && pref != '1')
						return false
				}

				if (prefs.length > 4)
					return false

				return true
			}
		]
	]

	/*
	 * Return current session object
	 */
	private HttpSession getSession() {
		return WebUtils.retrieveGrailsWebRequest().session
	}

	/**
	 * Method to read all of the user's preferences into a MAP and
	 * saved into the user's session
	 *
	 * @deprecated To Be Removed!!!
	 */
	def loadPreferences(String preferenceCode) {
		def userLogin = securityService.getUserLogin()
		if (userLogin) {
			loadPreferences( userLogin, preferenceCode)
		}
	}

	/**
	 * Method to read all of the user's preferences into a MAP and
	 * saved into the user's session
	 * THIS METHOD SHOULD BE REMOVED!!! the preferences should be stateless
	 */
	def loadPreferences(UserPreferenceEnum preference) {
		loadPreferences(preference.toString())
	}

	/*
	 * Method to read all of the user's preferences into a MAP and
	 * saved into the user's session
	 */
	def loadPreferences(UserLogin userLogin, String preferenceCode) {
		// TODO : JPM 6/2015 : Just want to PUKE - this is loading all of the users' preferences into a map and storing as individual preferences - WTF?
		if(userLogin){
			def userPreference = UserPreference.findAllByUserLogin( userLogin )

			if ( userPreference != null ) {
				// Initialize Map
				def currProj = new HashMap()

				for (int i = 0; i < userPreference.size(); i++) {
					currProj.put( userPreference[i].preferenceCode, userPreference[i].value )
				}
				// Set CURR_PROJ into User session
				getSession().setAttribute( preferenceCode, currProj )
			}
		}
	}

	/*
	 * Method will access the map stored into the user's session and
	 * return the value if found otherwise return null
	 */
	String get( String preferenceCode ) {
		def value = null
		def userLogin = securityService.getUserLogin()
		if (userLogin) {
			def userPref = UserPreference.findByUserLoginAndPreferenceCode( userLogin, preferenceCode)
			if (userPref) {
				value = userPref.value
			}
		}

		if (value == null && defaults.containsKey(preferenceCode)) {
			value = defaults[preferenceCode]
			// log.info "user preference $preferenceCode=$value from defaults"
		}

		// log.info "user preference $preferenceCode=$value"
		return value
	}

	/*
	 * Used to retrieve a map of user preferences based on the list of codes that are passed. Those preferences that are not found
	 * will default to a blank string value
	 * @param codes - a list of one or more String preference codes to lookup
	 * @param userLogin - the user whom to lookup the preference for. This is optional and if not supplied it will lookup the user from the session
	 * @return A map containing the values of the various preferences
	 */
	Map getPreferences(List<String> codes, UserLogin userLogin=null) {
		Map codesWithDefaults = [:]
		codes.each { codesWithDefaults[it] = '' }
		return getPreferences(codesWithDefaults, userLogin)
	}

	/*
	 * Used to retrieve a map of user preferences based on the list of codes that are passed with their default values if not found
	 * @param codesWithDefaults - a map of preference codes with their default values
	 * @param userLogin - the user whom to lookup the preference for, if not supplied it will lookup the user from the session
	 * @return A map containing the values of the various preferences
	 */
	Map getPreferences(Map codesWithDefaults, UserLogin userLogin=null) {
		// TODO : JPM 6/2015 : getPreferences should look to see if the value is already in the user session instead of always going back to the database
		// TODO : JPM 6/2015 : How should we be handling the default values??? Should they come from the caller or should we just maintain a master list?
		Map map = [:]
		if (! userLogin) userLogin = securityService.getUserLogin()
		if (! userLogin) return map

		def codes = codesWithDefaults.keySet()
		def ups = UserPreference.findAllByUserLoginAndPreferenceCodeInList( userLogin, codes)
		codesWithDefaults.each { code, defVal ->
			def up = ups.find { it.preferenceCode == code }
			map[code] = up ? up.value : defVal
		}

		return map
	}

	/*
	 * Reads the preference stored in the database for a user instead of the mess that is going on with
	 * the getPreference()
	 * @param String preferenceCode
	 * @return String the user's saved preference or null if not found
	 * @deprecated
	 */
	def String getPreference( String preferenceCode ) {
		loadPreferences(preferenceCode)
		def currProj = getSession().getAttribute( preferenceCode )
		def prefValue
		if ( currProj != null ) {
			prefValue = currProj[preferenceCode]
		}
		return prefValue
	}

	/*
	 * Reads the preference stored in the database for a user instead of the mess that is going on with
	 * the getPreference()
	 * @param String preferenceCode
	 * @return String the user's saved preference or null if not found
	 * @deprecated
	 */
	def String getPreference( UserPreferenceEnum preference ) {
		return getPreference(preference.toString())
	}

	/*
	 * Reads the preference stored in the database for the user specified by the parameters
	 * @param String preferenceCode
	 * @param UserLogin userLogin
	 * @return String the user's saved preference or null if not found
	 */
	def String getPreference (String preferenceCode, UserLogin userLogin) {
		loadPreferences(userLogin, preferenceCode)
		def currProj = getSession().getAttribute(preferenceCode)
		def prefValue
		if (currProj != null) {
			prefValue = currProj[preferenceCode]
		}
		return prefValue
	}

	/**
	 * Get Preference Map by default User by preferenceKeys
	 * @author @tavo_luna
	 */
	def getPreferencesMap(Collection<UserPreferenceEnum> preferenceKeys){
		def mapPref = [:]
		def userLogin = securityService.getUserLogin()
		if (userLogin && preferenceKeys) {
			def keyNames = preferenceKeys.collect{ it.name() }
			def userPreferences = UserPreference.findAllByUserLoginAndPreferenceCodeInList(userLogin, keyNames) ?: []

			userPreferences.each{ preferenceCode ->
				mapPref[preferenceCode.preferenceCode] = preferenceCode.value
			}
		}

		return mapPref
	}

	/**
	 * get ImportPreferences
	 */
	def getImportPreferences(){
		def prefKeys = UserPreferenceEnum.getImportPreferenceKeys()
		return getPreferencesMap(prefKeys)
	}

	/**
	 * get ExportPreferences
	 */
	def getExportPreferences(){
		def prefKeys = UserPreferenceEnum.getExportPreferenceKeys()
		return getPreferencesMap(prefKeys)
	}

	/**
	 * Method will remove the user preference record for selected preferenceCode and loginUser
	 * @deprecated
	 */
	def removePreference( String preferenceCode ) {
		def principal = SecurityUtils.subject.principal
		def userLogin = UserLogin.findByUsername( principal )
		def userPreference = UserPreference.findByUserLoginAndPreferenceCode( userLogin, preferenceCode)
		if ( userPreference ) {
			userPreference.delete(flush:true)
		}
	}

	/**
	 * Method will remove the user preference record for selected preferenceCode and loginUser
	 */
	def removePreference(UserPreferenceEnum preference ) {
		removePreference(preference.toString())
	}

	/**
	 * Used to set the user permission where the user account is looked up through the security service
	 * @param preferenceCode - the code to set
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 * @deprecated you should rather use setPreference( UserPreferenceEnum, String)
	 */
	Boolean setPreference( String preferenceCode, String value ) {
		def principal = SecurityUtils.subject.principal
		def userLogin = UserLogin.findByUsername( principal )
		return setPreference(userLogin, preferenceCode, value)
	}

	/**
	 * Used to set the user permission where the user account is looked up through the security service
	 * @param preferenceCode - the code to set using an ENUM
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 */
	Boolean setPreference( UserPreferenceEnum preferenceCode, String value ) {
		return setPreference(preferenceCode.toString(), value)
	}

	/**
	 * Used to set the user permission for the user account passed into the method.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call removeProjectAssociatedPreferences
	 * to clear out project specific settings.
	 *
	 * @param userLogin - the user to set the preference for
	 * @param preferenceCode - the code to set
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 */
	Boolean setPreference(UserLogin userLogin, UserPreferenceEnum preference, String value ) {
		return setPreference(userLogin, preference.toString(), value)
	}

	/**
	 * Used to set the user permission for the user account passed into the method.
	 * Note that if it is setting CURR_PROJ to a new value it will automatically call removeProjectAssociatedPreferences
	 * to clear out project specific settings.
	 *
	 * @param userLogin - the user to set the preference for
	 * @param preferenceCode - the code to set
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 * @deprecated
	 */
	Boolean setPreference( UserLogin userLogin, String preferenceCode, String value ) {
		def saved = false
		if (log.isDebugEnabled())
			log.debug "setPreference: setting user ($userLogin) preference $preferenceCode=$value"

		// Date start = new Date()

		if (value && value != "null" && userLogin) {
			def prefValue = getPreference(preferenceCode)

			//log.debug "setPreference() phase 1 took ${TimeUtil.elapsed(start)}"
			//start = new Date()

			//	remove the movebundle and event preferences if user switched to different project
			if (preferenceCode == "CURR_PROJ" && prefValue && prefValue != value){
				removeProjectAssociatedPreferences(userLogin)
			}

			//log.debug "setPreference() phase 2 took ${TimeUtil.elapsed(start)}"
			//start = new Date()

			if ( prefValue == null ) {
				//	Statements to create UserPreference to login user
				def userPreference = new UserPreference( value: value )
				userPreference.userLogin = userLogin
				userPreference.preferenceCode = preferenceCode
				if (! userPreference.save( insert: true, flush:true)) {
					log.error "setPreference: failed insert : ${GormUtil.allErrorsString(userPreference)}"
				}
			} else {
				//	Statements to Update UserPreference to login user
				def userPreference = UserPreference.get( new UserPreference( userLogin:userLogin, preferenceCode: preferenceCode ) )
				userPreference.value = value
				saved = userPreference.save(flush:true)
				if (! saved ) {
					log.error "setPreference: failed update : ${GormUtil.allErrorsString(userPreference)}"
				}
			}

			// log.debug "setPreference() phase 3 took ${TimeUtil.elapsed(start)}"
			// start = new Date()

			// call loadPreferences() to load CURR_PROJ MAP into session
			loadPreferences(userLogin, preferenceCode)

			// log.debug "setPreference() phase 4 took ${TimeUtil.elapsed(start)}"

		}
		return saved

	}
	/*-------------------------------------------------------------------------------------------
	 * Remove the Move Event and Move Bundle preferences when user switched to different project.
	 * @param : login user
	 * ----------------------------------------------------------------------------------------*/
	def removeProjectAssociatedPreferences(def userLogin ){
		def eventPreference = UserPreference.findByUserLoginAndPreferenceCode( userLogin, "MOVE_EVENT")
		if( eventPreference ){
			eventPreference.delete(flush:true)
			println"Removed MOVE_EVENT preference as user switched to other project"
			loadPreferences("MOVE_EVENT")
		}

		def bundlePreference = UserPreference.findByUserLoginAndPreferenceCode( userLogin, "CURR_BUNDLE")
		if( bundlePreference ){
			bundlePreference.delete(flush:true)
			println"Removed MOVE_BUNDLE preference as user switched to other project"
			loadPreferences("CURR_BUNDLE")
		}
		def roomPreference = UserPreference.findByUserLoginAndPreferenceCode( userLogin, "CURR_ROOM")
		if( roomPreference  ){
			roomPreference .delete(flush:true)
			println"Removed CURR_ROOM preference as user switched to other project"
			loadPreferences("CURR_ROOM")
		}
	}
	/*
	 * Set Roles to Persons in PartyRole
	 * @return true or false indicating the occurrence of Security Violations.
	 */
	// TODO : setUserRoles - Move to SecurityService
	def setUserRoles( def roleTypeList, def personId ) {
		def person = Party.findById(personId)
		def login = securityService.getUserLogin()
		def securityViolations = false
		roleTypeList.each { roleCode ->
			if (roleCode) {
				if (! securityService.isRoleAssignable(login.person, roleCode)) {
					securityService.reportViolation("Attempted to update user $person permission to assign security role $roleCode, which is not permissible", login)
					securityViolations = true
				} else {
					RoleType roleType = RoleType.findById(roleCode)
					if (!roleType) {
						securityService.reportViolation("attempted to update user $person permission with undefined role $roleCode", login)
						securityViolations = true
					} else {
						// Create Role Preferences to User if it doesn't exist
						PartyRole partyRole = PartyRole.findByPartyAndRoleType(person, roleType)
						if (! partyRole) {
							partyRole = new PartyRole( party:person, roleType:roleType )
							if (! partyRole.save( insert:true )) {
								log.error "setUserRoles() failed to add partyRole $partyRole : " + GormUtil.allErrorsString(partyRole)
								securityViolations =  true
							}
						}
					}
				}
			}
		}

		return securityViolations

	}
	/*----------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : person and rols
	 * @return : Remove Roles to Persons in PartyRole
	 *----------------------------------------------------------*/
	// TODO : setUserRoles - Move to SecurityService
	def removeUserRoles( def roleType, def personId ){
		roleType.each{role ->
			PartyRole.executeUpdate("delete from PartyRole where party = '$personId' and roleType = '$role' ")
		}
	}

	/*
	 *  Method to return List of Roles Available for User
	 */
	// TODO : getAvailableRoles - Move to SecurityService
	def getAvailableRoles( def person ){
		def availableRoles = RoleType.findAll("from RoleType r where \
			r.id not in (select roleType.id from PartyRole where party = $person.id  group by roleType.id ) \
			and r.description like 'staff%' OR r.description like 'system%' order by r.description ")

		return availableRoles
	}
	/*
	 *  Method to return List of Roles Assigned to User
	 */
	// TODO : getAssignedRoles - Move to SecurityService
	def getAssignedRoles( def person ){

		def assignedRoles = RoleType.findAll("from RoleType r where r.id in (select roleType.id from PartyRole where party = $person.id group by roleType.id )")

		return assignedRoles
	}

	/**
	 * Set the preference for the given user and code.
	 * @param userLogin
	 * @param preference
	 * @param value
	 * @return true if successfule
	 */
	boolean addOrUpdatePreferenceToUser(UserLogin userLogin, String preference, value) {
		boolean success=true
		def userPreference = UserPreference.findByUserLoginAndPreferenceCode(userLogin, preference)
		if(userPreference){
			userPreference.value = value
		} else {
			userPreference = new UserPreference(
				userLogin:userLogin,
				preferenceCode:preference,
				value:value
			)
		}
		if (! userPreference.save( insert: true, flush:true)) {
			log.error "addPreference: failed insert : ${GormUtil.allErrorsString(userPreference)}"
			success=false
		}
		return success
	}

	/**
	 * get the preference for the given user and preferenceCode
	 * @param userLogin
	 * @param preference
	 * @return
	 * @deprecated
	 */
	def getPreferenceByUserAndCode(def userLogin, def preference){
		def userPreference = UserPreference.findByUserLoginAndPreferenceCode(userLogin, preference)
		return userPreference?.value
	}

	/**
	 * get the preference for the given user and preferenceCode
	 * @param userLogin
	 * @param preference
	 * @return
	 */
	def getPreferenceByUserAndCode(def userLogin, UserPreferenceEnum preference){
		return getPreferenceByUserAndCode(userLogin, preference.toString())
	}

	def deleteSecurityRoles(person) {
		def currentRoles = getAssignedRoles(person)
		def toRemoveRoles = []
		currentRoles.each { r ->
			if (SECURITY_ROLES[r.id]) {
				toRemoveRoles << r.id
			}
		}
		if (toRemoveRoles.size() > 0) {
			removeUserRoles(toRemoveRoles, person.id)
		}
	}

	/* Saves a user preference after making sure that it passes validation using the constraints map */
	def savePreference (String code, def value) {
		if (code in prefCodeConstraints) {
			def validated = ConstraintsValidator.validate(value, prefCodeConstraints[code])
			if (! validated)
				throw new InvalidParamException()

			setPreference(code, value)
		} else {
			throw new InvalidRequestException()
		}

		return true
	}
	/**
	 * Return the File Stored Timezones
	 */
	def timezonePickerAreas() {
		File worldMapAreasFile = grailsApplication.parentContext.getResource( "/templates/timezone/world_map_areas.json" ).getFile()
		def timezones = JSON.parse(worldMapAreasFile.text)
		return timezones
	}

}
