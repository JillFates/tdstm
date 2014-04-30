import javax.servlet.http.HttpSession

import org.apache.shiro.SecurityUtils
import org.springframework.web.context.request.RequestContextHolder

import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetTransition
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
		def principal = SecurityUtils.subject.principal
		if(principal){
			def userLogin = UserLogin.findByUsername( principal )
			loadPreferences( userLogin, preferenceCode)
		}
	}

	/*
	 * Method to read all of the user's preferences into a MAP and 
	 * saved into the user's session
	 */
	def loadPreferences(UserLogin userLogin, String preferenceCode) {
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
	def String get( String preferenceCode ) {
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
		// log.info "getPreference: preferenceCode=$preferenceCode, currProj=$currProj, prefValue=$prefValue"
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
			loadPreferences( preferenceCode )
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

		if (value && value != "null" && userLogin){
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
	def setUserRoles( def roleType, def person ){
		def personInstance = Party.findById(person)
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
	def removeUserRoles( def roleType, def person ){
		roleType.each{role ->
			PartyRole.executeUpdate("delete from PartyRole where party = '$person' and roleType = '$role' ")
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
			if (username.toString() != 'jmtest@transitionaldata.com')
				log.error "USERNAME DOESN'T MATCH '$username' 'jmtest@transitionaldata.com'"
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
}
