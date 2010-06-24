import org.jsecurity.SecurityUtils
import javax.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import com.tdssrc.eav.*
class UserPreferenceService  {

	static transactional = true
	
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
    
    def loadPreferences(def preferenceCode) {
    	
    	def principal = SecurityUtils.subject.principal
    	def userLogin = UserLogin.findByUsername( principal )
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
	
    /*
     * Method will access the map stored into the user's session and 
     * return the value if found otherwise return null
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
     * method will call getPreference and if null insert the value 
     * otherwise it will lookup the UserPreference record in the database and 
     * perform an update
     */
    
    def setPreference( String preferenceCode, String value ) {
		if(preferenceCode == "CURR_PROJ"){
			def principal = SecurityUtils.subject.principal
	    	def userLogin = UserLogin.findByUsername( principal )
	    	removeProjectAssociatedPreferences(userLogin )	
		}
    	def prefValue = getPreference(preferenceCode)
    	def principal = SecurityUtils.subject.principal
    	def userLogin = UserLogin.findByUsername( principal )
    	
    	if ( prefValue == null ) {
    		//	Statements to create UserPreference to login user
    		def userPreference = new UserPreference( value: value )
    		userPreference.userLogin = userLogin
    		userPreference.preferenceCode = preferenceCode
    		userPreference.save( insert: true)
    	} else {
    		//	Statements to Update UserPreference to login user
    		def userPreference = UserPreference.get( new  UserPreference( userLogin:userLogin, preferenceCode: preferenceCode ) )
    		userPreference.value = value;
    		userPreference.save();
    	}
    	// call loadPreferences() to load CURR_PROJ MAP into session
    	loadPreferences(preferenceCode)
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
    }
    /*
     *	Set Roles to Persons in PartyRole  
     */
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
    def removeUserRoles( def roleType, def person ){
    	roleType.each{role ->
    		PartyRole.executeUpdate("delete from PartyRole where party = '$person' and roleType = '$role' ")
    	}
    }
    
    /*
     *  Method to return List of Roles Available for User
     */
    def getAvailableRoles( def person ){
        def availableRoles = RoleType.findAll("from RoleType r where r.id not in (select roleType.id from PartyRole where party = $person.id group by roleType.id )")
    	 
        return availableRoles
    }
    /*
     *  Method to return List of Roles Assigned to User
     */
    def getAssignedRoles( def person ){

        def assignedRoles = RoleType.findAll("from RoleType r where r.id in (select roleType.id from PartyRole where party = $person.id group by roleType.id )")
    	 
        return assignedRoles
    }
    /*
     *  Check the User Authentication and return the boolean value 
     */
    def checkActiveStatus(){
    	def activeStatus = true
    	def principal = SecurityUtils.subject.principal
    	def userLogin = UserLogin.findByUsername( principal )
    	def userActive = userLogin.active
    	def personActive = userLogin.person.active
    	if(personActive != "Y" || userActive != "Y" ){
    		activeStatus = false
    	}
    	return activeStatus
    }
    /*
     * Update the lastlogin once user has logged in.
     * @param  : login username.
     */
	def updateLastLogin( username ){
    	if( username ){
    		def userLogin = UserLogin.findByUsername( username )
			getSession().setAttribute( "LOGIN_PERSON", ['name':userLogin.person.firstName, "id":userLogin.person.id ])
			userLogin.lastLogin = new Date()
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

		def columns = [column1:[label:attributeLabel1, field:attribute1], column2:[label:attributeLabel2, field:attribute2], 
					   column3:[label:attributeLabel3, field:attribute3], column4:[label:attributeLabel4, field:attribute4]]
		return columns;
    }
}
