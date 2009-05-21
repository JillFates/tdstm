import java.text.SimpleDateFormat 
class MoveBundleController {
    def partyRelationshipService
    def userPreferenceService
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
    	if(!params.max) params.max = 10
    	def projectId = params.projectId
    	if(projectId == null || projectId == ""){
        	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        }
    	def projectInstance = Project.findById( projectId )
    	def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance, params )
        [ moveBundleInstanceList: moveBundleInstanceList, projectId:projectId ]
    }

    def show = {
        def moveBundleInstance = MoveBundle.get( params.id )
        request.getSession(false).setAttribute("MOVEBUNDLE",moveBundleInstance)
        def projectId = params.projectId 
        
        if(!moveBundleInstance) {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list)
        }
        else {
        	userPreferenceService.setPreference( "CURR_BUNDLE", "${moveBundleInstance.id}" )
            def projectManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "PROJ_MGR" ) 
            //PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
        	def moveManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "MOVE_MGR" ) 
            //PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
        	
        	return [ moveBundleInstance : moveBundleInstance, projectId:projectId, projectManager: projectManager, moveManager: moveManager ] 
        }
    }

    def delete = {
        def moveBundleInstance = MoveBundle.get( params.id )
        def projectId = params.projectId
        if(moveBundleInstance) {
            moveBundleInstance.delete()
            flash.message = "MoveBundle ${moveBundleInstance} deleted"
            redirect(action:list, params:[projectId: projectId])
        }
        else {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def moveBundleInstance = MoveBundle.get( params.id )
        def projectId = params.projectId
         
        if(!moveBundleInstance) {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list, params:[projectId: projectId])
        }
        else {
        	def managers = partyRelationshipService.getProjectStaff( projectId )
        	def projectManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "PROJ_MGR" )
            //PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
        	def moveManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "MOVE_MGR" )
            //PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
        	if( projectManager != null ){
        		projectManager = projectManager.partyIdTo.id
        	}
        	if( moveManager != null ){
        		moveManager = moveManager.partyIdTo.id
        	}
        	return [ moveBundleInstance : moveBundleInstance, projectId: projectId, managers: managers, projectManager: projectManager, moveManager: moveManager]
        	
        }
    }

    def update = {
        def moveBundleInstance = MoveBundle.get( params.id )        
        def projectId = params.projectId
        def projectManagerId = params.projectManager
    	def moveManagerId = params.moveManager 
        if( moveBundleInstance ) {
            moveBundleInstance.properties = params
            def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm")
            def startTime = params.startTime
            def completionTime = params.completionTime
            if(startTime != null && startTime != ""){
            	moveBundleInstance.startTime =  formatter.parse( startTime )
            }
            if(completionTime != null && completionTime != ""){
            	moveBundleInstance.completionTime =  formatter.parse( completionTime )
            }
            if(!moveBundleInstance.hasErrors() && moveBundleInstance.save()) {
            	           	
            	//def projectManegerInstance = Party.findById( projectManagerId )
            	def updateMoveBundlePMRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", projectManagerId, "PROJ_MGR" )
            	def updateMoveBundleMMRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", moveManagerId, "MOVE_MGR" )
                flash.message = "MoveBundle ${moveBundleInstance} updated"
                //redirect(action:show,params:[id:moveBundleInstance.id, projectId:projectId])
                redirect(action:show,id:moveBundleInstance.id, params:[projectId: projectId])
            }
            else {
            	def managers = partyRelationshipService.getProjectStaff( projectId )
            	def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
            	def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
                render(view:'edit',model:[moveBundleInstance:moveBundleInstance, projectId: projectId, managers: managers, projectManager: projectManagerId, moveManager: moveManagerId ])
            }
        }
        else {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
    		
        def moveBundleInstance = new MoveBundle()
        def projectId = params.projectId        
        moveBundleInstance.properties = params        
        def managers = partyRelationshipService.getProjectStaff( projectId )        
        return ['moveBundleInstance':moveBundleInstance, managers: managers, projectId: projectId ]	     
	  
    }

    def save = {
    		
        def moveBundleInstance = new MoveBundle(params)
        def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm")
        def startTime = params.startTime
        def completionTime = params.completionTime
        if(startTime != null && startTime != ""){
        	moveBundleInstance.startTime =  formatter.parse( startTime )
        }
        if(completionTime != null && completionTime != "" ){
        	moveBundleInstance.completionTime =  formatter.parse( completionTime )
        }
        def projectId = params.projectId        
        def projectManager = params.projectManager
    	def moveManager = params.moveManager
    	def managers = partyRelationshipService.getProjectStaff( projectId )
    	def projectInstance = Party.findById( projectId )
        if(!moveBundleInstance.hasErrors() && moveBundleInstance.save()) {
        	if( projectManager != null && projectManager != ""){
	        	def projectManegerInstance = Party.findById( projectManager )
	        	def pmPartyRelation = partyRelationshipService.savePartyRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance, "MOVE_BUNDLE", projectManegerInstance, "PROJ_MGR")
        	}
        	if( moveManager != null && moveManager != "" ){
        		def moveManegerInstance = Party.findById( moveManager )
        		def mmPartyRelation = partyRelationshipService.savePartyRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance, "MOVE_BUNDLE", moveManegerInstance, "MOVE_MGR")
        	}
        	
            flash.message = "MoveBundle ${moveBundleInstance} created"
            redirect(action:show,params:[id:moveBundleInstance.id, projectId:projectId])
        }
        else {
            render(view:'create',model:[moveBundleInstance:moveBundleInstance, managers: managers, projectId:projectId, projectManager: projectManager, moveManager: moveManager])
        }
    }
   /* def dashboardView = {
    		println"comg into dash view---->"
    		def projectId = params.projectId
        	def projectInstance = Project.findById( projectId )
        	def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
            if(!params.max) params.max = 10
            return[ moveBundleInstanceList: moveBundleInstanceList, projectId:projectId ]
    		
    }*/
}
