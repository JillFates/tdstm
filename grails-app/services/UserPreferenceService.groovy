import javax.servlet.http.HttpSession

import org.apache.shiro.SecurityUtils
import org.springframework.web.context.request.RequestContextHolder

// TODO : JPM 6/2015 : Why are all of these packages being loaded???
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tds.asset.AssetDependencyBundle
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tds.asset.FieldImportance

class UserPreferenceService  {

	static transactional = true
	def securityService
	
	protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8']
	
	// defaults holds global defaults for certain values 
	// TODO - load these from application settings
	protected static defaults = ['CURR_TZ':'EST']

	protected static Map SECURITY_ROLES = ['USER':true,'EDITOR':true,'SUPERVISOR':true]

	/*
	 * Return current session object
	 */
	def HttpSession getSession() {
		return RequestContextHolder.currentRequestAttributes().getSession()
	}

	/*
	 * Method to read all of the user's preferences into a MAP and 
	 * saved into the user's session
	 */
	def loadPreferences(String preferenceCode) {
		def userLogin = securityService.getUserLogin()
		if (userLogin) {
			loadPreferences( userLogin, preferenceCode)
		}
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
	
	/*
	 * Method will remove the user preference record for selected preferenceCode and loginUser
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
	 * Used to set the user permission where the user account is looked up through the security service
	 * @param preferenceCode - the code to set
	 * @param value - the value to set for the preference
	 * @return true if the set was successful
	 */
	Boolean setPreference( String preferenceCode, String value ) {
		def principal = SecurityUtils.subject.principal
		def userLogin = UserLogin.findByUsername( principal )
		return setPreference(userLogin, preferenceCode, value)
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
	Boolean setPreference( UserLogin userLogin, String preferenceCode, String value ) {
		def saved = false
		if (log.isDebugEnabled())
			log.debug "setPreference: setting user ($userLogin) preference $preferenceCode=$value"

		if (value && value != "null" && userLogin) {
			def prefValue = getPreference(preferenceCode)
	
			//	remove the movebundle and event preferences if user switched to different project
			if (preferenceCode == "CURR_PROJ" && prefValue && prefValue != value){
				removeProjectAssociatedPreferences(userLogin)	
			}
			
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
				userPreference.value = value;
				saved = userPreference.save(flush:true)
				if (! saved ) {
					log.error "setPreference: failed update : ${GormUtil.allErrorsString(userPreference)}"
				}
			}
			// call loadPreferences() to load CURR_PROJ MAP into session
			loadPreferences(userLogin, preferenceCode)
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
	 *	Set Roles to Persons in PartyRole  
	 */
	// TODO : setUserRoles - Move to SecurityService
	def setUserRoles( def roleType, def personId ){
		def personInstance = Party.findById(personId)
		roleType.each{role ->
			def roleTypeInstance = RoleType.findById(role)
			// Create Role Preferences to User
			def dupPartyRole =  PartyRole.get( new PartyRole( party:personInstance, roleType:roleTypeInstance ) )
			if(dupPartyRole == null){
				def partyRole = new PartyRole( party:personInstance, roleType:roleTypeInstance ).save( insert:true )
			}
		}
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
	/*
	 * Update the lastlogin once user has logged in.
	 * @param  : login username.
	 */
	// TODO : updateLastLogin - Move to SecurityService
	def updateLastLogin( username ){
		if (log.isDebugEnabled()) 
			log.debug "updateLastLogin: updating user=$username, ${username.class}"
		if( username ) {
			username = username.toString()
			def userLogin = UserLogin.findByUsername( username )
			if (log.isDebugEnabled()) 
				log.debug "updateLastLogin: found userLogin=$userLogin"
			def person = userLogin.person
			if (log.isDebugEnabled()) 
				log.debug "updateLastLogin: updating user=$username person=$person"
			getSession().setAttribute( "LOGIN_PERSON", ['name':person.firstName, "id":person.id ])
			userLogin.lastLogin = TimeUtil.nowGMT()
			userLogin.save(flush:true)
		}
	}
		
	/*
	 * Set user preferred columns for For PMO dashboard.
	 * @param: four columns which has to set into preferences  
	 * @return : Preferred columns and Frountend Labels as Map.
	 */
	def setAssetTrackingPreference( def attribute1, def attribute2, def attribute3, def attribute4 ){
		
		def column1 = getPreference("PMO_COLUMN1")
		def column2 = getPreference("PMO_COLUMN2")
		def column3 = getPreference("PMO_COLUMN3")
		def column4 = getPreference("PMO_COLUMN4")
		
		column1 = column1 ? column1 : "application"
		column2 = column2 ? column2 : "appOwner"
		column3 = column3 ? column3 : "appSme"
		column4 = column4 ? column4 : "assetName"
		
		attribute1 = attribute1 ? attribute1 : column1
		attribute2 = attribute2 ? attribute2 : column2
		attribute3 = attribute3 ? attribute3 : column3
		attribute4 = attribute4 ? attribute4 : column4
		
		setPreference("PMO_COLUMN1", attribute1)
		setPreference("PMO_COLUMN2", attribute2)
		setPreference("PMO_COLUMN3", attribute3)
		setPreference("PMO_COLUMN4", attribute4)
		
		def attributeLabel1 = EavAttribute.findByAttributeCode( attribute1 ).frontendLabel
		def attributeLabel2 = EavAttribute.findByAttributeCode( attribute2 ).frontendLabel
		def attributeLabel3 = EavAttribute.findByAttributeCode( attribute3 ).frontendLabel
		def attributeLabel4 = EavAttribute.findByAttributeCode( attribute4 ).frontendLabel
		
		def projectId = getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		def project = Project.findById( projectId )
		
		if( customLabels.contains( attributeLabel1 ) )
			attributeLabel1 = project[attribute1] ? project[attribute1] : attributeLabel1 
		
		if( customLabels.contains( attributeLabel2 ) )
			attributeLabel2 = project[attribute2] ? project[attribute2] : attributeLabel2
		
		if( customLabels.contains( attributeLabel3 ) )
			attributeLabel3 = project[attribute3] ? project[attribute3] : attributeLabel3

		if( customLabels.contains( attributeLabel4 ) )
			attributeLabel4 = project[attribute4] ? project[attribute4] : attributeLabel4
		

		
		def columns = [column1:[label:attributeLabel1, field:attribute1], column2:[label:attributeLabel2, field:attribute2], 
					   column3:[label:attributeLabel3, field:attribute3], column4:[label:attributeLabel4, field:attribute4]]
		return columns;
	}
	/**
	 * Set the preference for the given user and code.
	 * @param userLogin
	 * @param preference
	 * @param value
	 * @return
	 */
	def addOrUpdatePreferenceToUser(def userLogin, def preference, def value){
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
		}
	}
	
	/**
	 * get the preference for the given user and preferenceCode
	 * @param userLogin
	 * @param preference
	 * @return
	 */
	def getPreferenceByUserAndCode(def userLogin, def preference){
		def userPreference = UserPreference.findByUserLoginAndPreferenceCode(userLogin, preference)
		return userPreference?.value
	}

	def deleteSecurityRoles(person) {
		def currentRoles = getAssignedRoles(person);
		def toRemoveRoles = []
		currentRoles.each { r -> 
			if (SECURITY_ROLES[r.id]) {
				toRemoveRoles << r.id
			}
		}
		if (toRemoveRoles.size() > 0) {
			removeUserRoles(toRemoveRoles, person.id);
		}
	}

}
