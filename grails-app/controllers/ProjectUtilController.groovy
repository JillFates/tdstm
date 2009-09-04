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
			def projectList
    		def partyProjectList
    		def isAdmin = SecurityUtils.getSubject().hasRole("ADMIN")
    		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
    	if(isAdmin){	
        	  projectList = Project.findAll( "from Project as p order by p.dateCreated desc" )
    	}else{
    		def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
    							"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
    		def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
    					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT')"
    			projectList = Project.findAll(query)
    	}
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
