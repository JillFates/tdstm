import org.codehaus.groovy.grails.commons.ConfigurationHolder

class MaintService {
	
	static String MAINT_MODE_FILE_PATH = ""
	private static Boolean IN_MAINT_MODE = false
	
	def servletContext
	
	/**
	 * Constructor to load config prop in a class variable.
	 */
	def MaintService(){
		MAINT_MODE_FILE_PATH = ConfigurationHolder.config.tdsops.maintModeFile
	}
	
	/**
	 * This method is used to check whether Maintenance Mode flag file exist or not
	 * @return void
	 */
	def checkForMaintFile(){
		//Check for resource availability at given path.
		def resource = servletContext.getResource( MaintService.MAINT_MODE_FILE_PATH )
		if( resource ) {
			if( !isInMaintMode() ){
				setInMaintMode( true )
				log.info " Maintenance Mode was enabled @ "+new Date();
			}
		} else {
			if( isInMaintMode() ){
				setInMaintMode( false )
				log.info " Maintenance Mode was disabled @ "+new Date();
			}
		}
	}
	
	/**
	 * Setting a variable to determine whether application is maint mode or not
	 * @return void
	 */
	def static setInMaintMode (Boolean inMaintMode ){
		IN_MAINT_MODE = inMaintMode
	}
	
	/**
	 * This method is used to get maint. mode variable flag
	 * @return Boolean 
	 */
	def static Boolean isInMaintMode (){
		return IN_MAINT_MODE
	}
	
	/**
	 * This method allow user to make a backdoor entry either application is in maintenance mode
	 * @return
	 */
	def toggleUsersBackdoor(session){
		def hasAccess = session.getAttribute("MAINT_BACKDOOR_ENTRY")
		if( !hasAccess  ){
			session.MAINT_BACKDOOR_ENTRY = true
			log.info  "User allowed for back door access @ "+ new Date()
		}
	}
	
	/**
	 * This method is returns a flag to make a backDoor entry
	 * @return
	 */
	def hasBackdoorAccess(session){
		return session.MAINT_BACKDOOR_ENTRY ?: false
	}
}