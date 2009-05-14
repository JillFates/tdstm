import org.jsecurity.SecurityUtils
class ProjectUtilController {
	def userPreferenceService

    def index = { 
    		 
        try{
            def principal = SecurityUtils.subject.principal
            def userLogin = UserLogin.findByUsername( principal )
            def userPreference = UserPreference.findAllByUserLoginAndPreferenceCode( userLogin, "CURR_PROJ" )
            if ( userPreference != null && userPreference != [] ) {
            	def projectInstance = Project.findById( userPreference.value[0] )
                redirect( controller:"project", action:"show",id:projectInstance.id)
            } else {
            	redirect( action:"searchList" )
            }
        } catch (Exception e){
            flash.message = "Your login has expired and must login again"
            redirect(controller:'auth', action:'login')
        }
    }
    
    /*
     * Action to return a list of projects , sorted desc by dateCreated 
     */
        
    def searchList = {
   		def query = "from Project as p order by p.dateCreated desc"
    	def projectList = Project.findAll( query )
    	return [ projectList:projectList ]
    }
    /*
     * Action to setPreferences
     */
    def addUserPreference = {
    		
        def projectInstance = Project.findByProjectCode(params.selectProject)
    		
        userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )

        redirect(controller:'project', action:"show", id: projectInstance.id )
    		
    }
}
