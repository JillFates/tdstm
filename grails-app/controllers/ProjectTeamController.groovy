class ProjectTeamController {
    
	def partyRelationshipService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
    /*
     * 	Return all of the teams associated to the project
     */
    def list = {
    	 def projectId = params.projectId
    	 def projectInstance = Project.findById(projectId)
    	 def projectTeamInstanceList = ProjectTeam.findAllByProject(projectInstance)
         return [ projectTeamInstanceList: projectTeamInstanceList, projectId:projectId ]
    }

    def show = {
        def projectTeamInstance = ProjectTeam.get( params.id )

        if(!projectTeamInstance) {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ projectTeamInstance : projectTeamInstance ] }
    }

    def delete = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        if(projectTeamInstance) {
            projectTeamInstance.delete()
            flash.message = "ProjectTeam ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def projectTeamInstance = ProjectTeam.get( params.id )

        if(!projectTeamInstance) {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ projectTeamInstance : projectTeamInstance ]
        }
    }

    def update = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        if(projectTeamInstance) {
            projectTeamInstance.properties = params
            if(!projectTeamInstance.hasErrors() && projectTeamInstance.save()) {
                flash.message = "ProjectTeam ${params.id} updated"
                redirect(action:show,id:projectTeamInstance.id)
            }
            else {
                render(view:'edit',model:[projectTeamInstance:projectTeamInstance])
            }
        }
        else {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }
    /*
     *  Return the project team details to create form
     */
    def create = {
    	
        def projectTeamInstance = new ProjectTeam()
        def projectId = params.projectId
        projectTeamInstance.properties = params
        def projectStaff = partyRelationshipService.getProjectStaff( projectId )
        println"projectStaff------------->"+projectStaff.staff
        return [ 'projectTeamInstance':projectTeamInstance, projectId:projectId, projectStaff :projectStaff ]
        
    }
	/*
	 *  Save the project team details 
	 */
    def save = {
		def projectId = params.projectId
        def projectTeamInstance = new ProjectTeam(params)
        if ( !projectTeamInstance.hasErrors() && projectTeamInstance.save() ) {
            flash.message = "ProjectTeam ${projectTeamInstance.id} created"
            redirect(action:show,id:projectTeamInstance.id)
        }
        else {
        	def projectStaff = partyRelationshipService.getProjectStaff( projectId )
            render( view:'create',model:[ projectTeamInstance:projectTeamInstance, projectId:projectId, projectStaff:projectStaff ] )
        }
    }
}
