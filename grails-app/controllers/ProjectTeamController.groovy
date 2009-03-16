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
    	 def projectTeamInstanceList = partyRelationshipService.getProjectTeamInstanceList( projectInstance )
    		 //ProjectTeam.findAllByProject(projectInstance)
    	 
         return [ projectTeamInstanceList: projectTeamInstanceList, projectId:projectId ]
    }
	/*
	 *  Return the Project Team Details
	 */
    def show = {
		def projectId = params.projectId	
        def projectTeamInstance = ProjectTeam.get( params.id )

        if(!projectTeamInstance) {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:list, params:[projectId:projectId] )
        }
        else {
        	def teamMembers = partyRelationshipService.getProjectTeamMembers( projectTeamInstance )
        	return [ projectTeamInstance : projectTeamInstance, projectId:projectId, teamMembers:teamMembers ] }
    }
	/*
	 *  Delated the ProjectTeam Deatils
	 */
    def delete = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        def projectId = params.projectId
        if(projectTeamInstance) {
        	PartyRelationship.executeUpdate("delete from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $projectTeamInstance.id and p.roleTypeCodeFrom = 'TEAM' ")
            projectTeamInstance.delete()
            flash.message = "ProjectTeam ${projectTeamInstance} deleted"
            redirect( action:list, params:[projectId:projectId] )
        }
        else {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:list, params:[projectId:projectId] )
        }
    }
	/*
	 *  return Project Team Details to Edit page
	 */
    def edit = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        def projectId = params.projectId
        if(!projectTeamInstance) {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:list, params:[projectId:projectId] )
        }
        else {
        	def availableStaff = partyRelationshipService.getAvailableTeamMembers( projectId, projectTeamInstance )
        	def teamMembers = partyRelationshipService.getProjectTeamMembers( projectTeamInstance )
            return [ projectTeamInstance : projectTeamInstance, projectId:projectId, availableStaff:availableStaff, teamMembers:teamMembers ]
        }
    }
	/*
	 *  Update the Project Team Details
	 */
    def update = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        projectTeamInstance.lastUpdated = new Date()
        def projectId = params.projectId
        def teamMembers = request.getParameterValues("teamMembers")
        if(projectTeamInstance) {
            projectTeamInstance.properties = params
            if(!projectTeamInstance.hasErrors() && projectTeamInstance.save()) {
            	PartyRelationship.executeUpdate("delete from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $projectTeamInstance.id and p.roleTypeCodeFrom = 'TEAM' ")
            	partyRelationshipService.createProjectTeamMembers( projectTeamInstance, teamMembers )
                flash.message = "ProjectTeam ${projectTeamInstance} updated"
                redirect( action:show, id:projectTeamInstance.id, params:[projectId:projectId] )
            }
            else {
            	def availableStaff = partyRelationshipService.getAvailableProjectStaff( projectId, teamMembers )
            	def projectTeamStaff = partyRelationshipService.getProjectTeamStaff( projectId, teamMembers )
                render( view:'edit', model:[projectTeamInstance:projectTeamInstance, projectId:projectId, availableStaff:availableStaff, teamMembers:projectTeamStaff  ] )
            }
        }
        else {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:edit, id:params.id, params:[projectId:projectId] )
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
        return [ 'projectTeamInstance':projectTeamInstance, projectId:projectId, availableStaff :projectStaff ]
        
    }
	/*
	 *  Save the project team details 
	 */
    def save = {
		def projectId = params.projectId
        def projectTeamInstance = new ProjectTeam(params)
		def teamMembers = request.getParameterValues("teamMembers")
		if ( !projectTeamInstance.hasErrors() && projectTeamInstance.save() ) {
	    	partyRelationshipService.createProjectTeamMembers( projectTeamInstance, teamMembers )
            flash.message = "ProjectTeam ${projectTeamInstance} created"
            redirect( action:show, id:projectTeamInstance.id, params:[projectId:projectId])
        }
        else {
        	def availableStaff = partyRelationshipService.getAvailableProjectStaff( projectId, teamMembers )
        	def projectTeamStaff = partyRelationshipService.getProjectTeamStaff( projectId, teamMembers )
            render( view:'create',model:[ projectTeamInstance:projectTeamInstance, projectId:projectId, availableStaff:availableStaff, teamMembers:projectTeamStaff ] )
        }
    }
}
