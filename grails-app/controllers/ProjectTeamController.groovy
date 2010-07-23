class ProjectTeamController {
    
	def partyRelationshipService
	def userPreferenceService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
    /*
     * 	Return all of the teams associated to the project
     */
    def list = {
        def bundleId = params.bundleId
        if(!bundleId){
        	userPreferenceService.loadPreferences("CURR_BUNDLE")
            bundleId = getSession().getAttribute("CURR_BUNDLE").CURR_BUNDLE
        }
        def bundleInstance = MoveBundle.findById(bundleId)
        def projectTeamInstanceList = partyRelationshipService.getBundleTeamInstanceList( bundleInstance  )
        //ProjectTeam.findAllByProject(projectInstance)
    	 
        return [ projectTeamInstanceList: projectTeamInstanceList, bundleInstance:bundleInstance ]
    }
	/*
	 *  Return the Project Team Details
	 */
    def show = {
		def bundleId = params.bundleId
		def bundleInstance = MoveBundle.findById( bundleId )
        def projectTeamInstance = ProjectTeam.get( params.id )

        if(!projectTeamInstance) {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:list, params:[bundleId:bundleId] )
        }
        else {
        	def teamMembers = partyRelationshipService.getBundleTeamMembers( projectTeamInstance )
        	return [ projectTeamInstance : projectTeamInstance, bundleInstance:bundleInstance, teamMembers:teamMembers ] }
    }
	/*
	 *  Delated the ProjectTeam Deatils
	 */
    def delete = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        
        def bundleId = params.bundleId
        if(projectTeamInstance) {
        	PartyRelationship.executeUpdate("delete from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $projectTeamInstance.id and p.roleTypeCodeFrom = 'TEAM' ")
            AssetEntity.executeUpdate("update AssetEntity ae set ae.sourceTeam = null where ae.sourceTeam = $projectTeamInstance.id")
            AssetEntity.executeUpdate("update AssetEntity ae set ae.targetTeam = null where ae.targetTeam = $projectTeamInstance.id")
        	projectTeamInstance.delete(flush:true)
            flash.message = "ProjectTeam ${projectTeamInstance} deleted"
            redirect( action:list, params:[bundleId:bundleId] )
        }
        else {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:list, params:[bundleId:bundleId] )
        }
    }
	/*
	 *  return Project Team Details to Edit page
	 */
    def edit = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        def bundleId = params.bundleId
        def bundleInstance = MoveBundle.findById( bundleId )
        if(!projectTeamInstance) {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:list, params:[bundleId:bundleId] )
        }
        else {
        	def availableStaff = partyRelationshipService.getAvailableTeamMembers( bundleInstance.project.id, projectTeamInstance )
        	def teamMembers = partyRelationshipService.getBundleTeamMembers( projectTeamInstance )
            return [ projectTeamInstance : projectTeamInstance, bundleInstance:bundleInstance, availableStaff:availableStaff, teamMembers:teamMembers ]
        }
    }
	/*
	 *  Update the Project Team Details
	 */
    def update = {
        def projectTeamInstance = ProjectTeam.get( params.id )
        //projectTeamInstance.lastUpdated = new Date()
        def bundleId = params.bundleId
        def bundleInstance = MoveBundle.findById( bundleId )
        def teamMembers = request.getParameterValues("teamMembers")
        if(projectTeamInstance) {
            projectTeamInstance.properties = params
            if(!projectTeamInstance.hasErrors() && projectTeamInstance.save()) {
            	PartyRelationship.executeUpdate("delete from PartyRelationship p where p.partyRelationshipType = 'PROJ_TEAM' and p.partyIdFrom = $projectTeamInstance.id and p.roleTypeCodeFrom = 'TEAM' ")
            	partyRelationshipService.createBundleTeamMembers( projectTeamInstance, teamMembers )
                flash.message = "ProjectTeam ${projectTeamInstance} updated"
                redirect( action:show, id:projectTeamInstance.id, params:[bundleId:bundleId] )
            }
            else {
            	projectTeamInstance.discard()
            	def availableStaff = partyRelationshipService.getAvailableProjectStaff( bundleInstance.project.id, teamMembers )
            	def projectTeamStaff = partyRelationshipService.getProjectTeamStaff( bundleInstance.project.id, teamMembers )
                render( view:'edit', model:[projectTeamInstance:projectTeamInstance, bundleInstance:bundleInstance, availableStaff:availableStaff, teamMembers:projectTeamStaff  ] )
            }
        }
        else {
            flash.message = "ProjectTeam not found with id ${params.id}"
            redirect( action:edit, id:params.id, params:[bundleId:bundleId] )
        }
    }
    /*
     *  Return the project team details to create form
     */
    def create = {
    	
        def projectTeamInstance = new ProjectTeam()
        def bundleId = params.bundleId
        def bundleInstance = MoveBundle.findById( bundleId )
        projectTeamInstance.properties = params
        def projectStaff = partyRelationshipService.getProjectStaff( bundleInstance.project.id )
        return [ 'projectTeamInstance':projectTeamInstance, bundleInstance:bundleInstance, availableStaff :projectStaff ]
        
    }
	/*
	 *  Save the project team details 
	 */
    def save = {
		def bundleId = params.bundleId
		def bundleInstance = MoveBundle.findById( bundleId )
        def projectTeamInstance = new ProjectTeam(params)
		def teamMembers = request.getParameterValues("teamMembers")
		if ( !projectTeamInstance.hasErrors() && projectTeamInstance.save() ) {
	    	partyRelationshipService.createBundleTeamMembers( projectTeamInstance, teamMembers )
            flash.message = "ProjectTeam ${projectTeamInstance} created"
            redirect( action:show, id:projectTeamInstance.id, params:[bundleId:bundleId])
        }
        else {
        	def availableStaff = partyRelationshipService.getAvailableProjectStaff( bundleInstance.project.id, teamMembers )
        	def projectTeamStaff = partyRelationshipService.getProjectTeamStaff( bundleInstance.project.id, teamMembers )
            render( view:'create',model:[ projectTeamInstance:projectTeamInstance, bundleInstance:bundleInstance, availableStaff:availableStaff, teamMembers:projectTeamStaff ] )
        }
    }
}
