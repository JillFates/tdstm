import org.jsecurity.SecurityUtils
import javax.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
class UserPreferenceService  {

	static transactional = true
	
    /*
     * Method to read all of the user's preferences into a MAP and 
     * saved into the user's session
     */
    
    def loadPreferences() {
    	
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
    		getSession().setAttribute( "CURR_PROJ", currProj )
    	}
    }
	
    /*
     * Method will access the map stored into the user's session and 
     * return the value if found otherwise return null
     */
   
    def String getPreference( String preferenceCode ) {

    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	
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
    	loadPreferences()
    }
    /*
     * Return current session object
     */
    def HttpSession getSession() {
        return RequestContextHolder.currentRequestAttributes().getSession()
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
    
}
