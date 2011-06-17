import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil
import grails.converters.JSON
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit


class MoveBundleController {

	def stepSnapshotService
    def partyRelationshipService
    def userPreferenceService
	def stateEngineService
	def moveBundleService
	def jdbcTemplate
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
    	
    	if(!params.sort) params.sort = 'name'
    	if(!params.order) params.order = 'asc'
    	def projectId = params.projectId
    	if(projectId == null || projectId == ""){
        	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        }
    	def moveBundleList = []
    	def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance, params )
		// Statements for JMESA integration
    	TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = moveBundleInstanceList
		return [moveBundleInstanceList : moveBundleInstanceList, projectId:projectId]
    	
    }

    def show = {
		userPreferenceService.loadPreferences("MOVE_EVENT")
		def moveBundleId = params.id
		
		moveBundleId = moveBundleId ? moveBundleId : session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE;
		if(moveBundleId){
			
			def moveBundleInstance = MoveBundle.get( moveBundleId )
	        //request.getSession(false).setAttribute("MOVEBUNDLE",moveBundleInstance)
	        def projectId = params.projectId 
	        
	        if(!moveBundleInstance) {
	            flash.message = "MoveBundle not found with id ${moveBundleId}"
	            redirect(action:list)
	        } else {
	        	userPreferenceService.setPreference( "CURR_BUNDLE", "${moveBundleInstance.id}" )
	            def projectManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "PROJ_MGR" ) 
	            //PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
	        	def moveManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "MOVE_MGR" ) 
	            //PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
	        	
				
				// get the list of Manual Dashboard Steps that are associated to moveBundle.project
				def moveBundleSteps = MoveBundleStep.findAll('FROM MoveBundleStep mbs WHERE mbs.moveBundle = :mb ORDER BY mbs.transitionId',[mb:moveBundleInstance]) 
				def dashboardSteps = []

				moveBundleSteps .each{
					def stepSnapshot = StepSnapshot.findAll("FROM StepSnapshot ss WHERE ss.moveBundleStep = :msb ORDER BY ss.dateCreated DESC",[msb:it, max:1])
					dashboardSteps << [moveBundleStep : it, stepSnapshot : stepSnapshot[0] ]
				}
	        	def showHistoryButton = false
				def bundleTransition = AssetTransition.findAll("FROM AssetTransition at WHERE at.assetEntity in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundleInstance.id})" )
				
				if( bundleTransition.size() > 0 )
					showHistoryButton = true
					
	        	return [ moveBundleInstance : moveBundleInstance, projectId:projectId, projectManager: projectManager, 
						 moveManager: moveManager, dashboardSteps:dashboardSteps, showHistoryButton : showHistoryButton ] 
	        }
		} else {
			redirect(action:list)
		}
    }

    def delete = {
        def moveBundleInstance = MoveBundle.get( params.id )
        def projectId = params.projectId
        if(moveBundleInstance) {
        	AssetEntity.withTransaction { status ->
	            try{
	            	// Update asset associations
	            	AssetEntity.executeUpdate("UPDATE AssetEntity SET moveBundle = null WHERE moveBundle = ?",[moveBundleInstance])
					// Delete Bundle and associations
					moveBundleService.deleteMoveBundleAssociates(moveBundleInstance)
					
					moveBundleInstance.delete(flush:true)
					
					flash.message = "MoveBundle ${moveBundleInstance} deleted"
	                redirect(action:list, params:[projectId: projectId])
	            
	            }catch(Exception ex){
	            	status.setRollbackOnly()
	            	flash.message = "Unable to Delete MoveBundle Assosiated with Teams "+ex
	            	redirect(action:list)
	            }
        	}
        } else {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list)
        }
    }
	def deleteBundleAndAssets = {
		def moveBundleInstance = MoveBundle.get( params.id )
		def projectId = params.projectId
		if(moveBundleInstance) {
			AssetEntity.withTransaction { status ->
				try{
					// Update asset associations
					moveBundleService.deleteBundleAssetsAndAssociates(moveBundleInstance)
					// Delete Bundle and associations
					moveBundleService.deleteMoveBundleAssociates(moveBundleInstance)
					
					moveBundleInstance.delete(flush:true)
					flash.message = "MoveBundle ${moveBundleInstance} deleted"
					redirect(action:list, params:[projectId: projectId])
				
				}catch(Exception ex){
					status.setRollbackOnly()
					flash.message = "Unable to Delete MoveBundle Assosiated with Teams "+ex
					redirect(action:list)
				}
			}
		} else {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:list)
		}
	}
    def edit = {
        def moveBundleInstance = MoveBundle.get( params.id )
        def projectId = params.projectId
        projectId = projectId ? projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        if(!moveBundleInstance) {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list, params:[projectId: projectId])
        } else {
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
        	
        	
        	//get the all Dashboard Steps that are associated to moveBundle.project
			def allDashboardSteps = moveBundleService.getAllDashboardSteps( moveBundleInstance )
			def remainingSteps = allDashboardSteps.remainingSteps
		       	
        	return [ moveBundleInstance : moveBundleInstance, projectId: projectId, managers: managers, projectManager: projectManager, 
					 moveManager: moveManager, dashboardSteps: allDashboardSteps.dashboardSteps, remainingSteps : remainingSteps]
        	
        }
    }

    def update = {
        def moveBundleInstance = MoveBundle.get( params.id )        
        def projectId = params.projectId
        def projectManagerId = params.projectManager
    	def moveManagerId = params.moveManager 
        if( moveBundleInstance ) {
            moveBundleInstance.name = params.name
            moveBundleInstance.description = params.description
			if(params.moveEvent.id){
				moveBundleInstance.moveEvent = MoveEvent.get(params.moveEvent.id)
			} else {
				moveBundleInstance.moveEvent = null
			}
            moveBundleInstance.operationalOrder = params.operationalOrder ? Integer.parseInt(params.operationalOrder) : 1
            def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
            def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
            def startTime = params.startTime
            def completionTime = params.completionTime
            if( startTime ){
            	moveBundleInstance.startTime =  GormUtil.convertInToGMT(formatter.parse( startTime ), tzId)
            }
            if( completionTime ){
            	moveBundleInstance.completionTime =  GormUtil.convertInToGMT(formatter.parse( completionTime ), tzId)
            }
            if(moveBundleInstance.validate(true) && moveBundleInstance.save() ) {
            	def stepsList = stateEngineService.getDashboardSteps( moveBundleInstance.project.workflowCode )
				stepsList.each{
					def checkbox = params["checkbox_"+it.id]
					if(checkbox  && checkbox == 'on'){
						def moveBundleStep = moveBundleService.createMoveBundleStep(moveBundleInstance, it.id, params)
						def tasksCompleted = params["tasksCompleted_"+it.id] ? Integer.parseInt(params["tasksCompleted_"+it.id]) : 0
						def calcMethod = params["calcMethod_"+it.id]
						if(calcMethod == "L"){
							stepSnapshotService.createLinearSnapshot( moveBundleInstance.id, moveBundleStep.id )	
						} else {
							stepSnapshotService.createManualSnapshot( moveBundleInstance.id, moveBundleStep.id, tasksCompleted )
						}
					} else {
						def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundleInstance , it.id)
						if( moveBundleStep ){
							moveBundleService.deleteMoveBundleStep( moveBundleStep )
						}
					}
				}
            	
            	//def projectManegerInstance = Party.findById( projectManagerId )
            	def updateMoveBundlePMRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", projectManagerId, "PROJ_MGR" )
            	def updateMoveBundleMMRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", moveManagerId, "MOVE_MGR" )
                flash.message = "MoveBundle ${moveBundleInstance} updated"
                //redirect(action:show,params:[id:moveBundleInstance.id, projectId:projectId])
                redirect(action:show,id:moveBundleInstance.id, params:[projectId: projectId])
            } else {
            	//	get the all Dashboard Steps that are associated to moveBundle.project
    			def allDashboardSteps = moveBundleService.getAllDashboardSteps( moveBundleInstance )
				def remainingSteps = allDashboardSteps.remainingSteps
				
            	moveBundleInstance.discard()
            	def managers = partyRelationshipService.getProjectStaff( projectId )
            	def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
            	def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
                render(view:'edit',model:[moveBundleInstance:moveBundleInstance, projectId: projectId, managers: managers, projectManager: projectManagerId, 
										  moveManager: moveManagerId, dashboardSteps:allDashboardSteps.dashboardSteps, remainingSteps : remainingSteps ])
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

		def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
        def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
        def startTime = params.startTime
        def completionTime = params.completionTime
        if(startTime){
        	params.startTime =  GormUtil.convertInToGMT(formatter.parse( startTime ), tzId)
        }
        if(completionTime){
        	params.completionTime =  GormUtil.convertInToGMT(formatter.parse( completionTime ), tzId)
        }
        
        def moveBundleInstance = new MoveBundle(params)
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

	/**
	 * Used to create StepSnapshot records for specified MoveBundle/MoveBundleStep.  
	 * @param moveBundleId
	 * @param moveBundleStepId 
	 * @param tasksCompleted value 0-100 representing % completed
	 * @return Returns 200 okay or appropriate error message
	 */
	def createManualStep = {
	/*
		// render( "HELLO WORLD!" )
		// return
		MoveBundleStep.withTransaction { status ->
			try {
				def moveBundleId = Integer.parseInt(params.moveBundleId)
				def moveBundleSteps = request.getParameterValues( "moveBundleStepId" )
			
				moveBundleSteps.each{ moveStep->
					def tasksCompleted = Integer.parseInt( request.getParameter("tasksCompleted_"+moveStep) )
					if (  tasksCompleted < 0 || tasksCompleted > 100 ) {
						response.sendError( 400, "Bad Request P")
						// render("400 Bad Request")
						status.setRollbackOnly()
						return false
					}
					def moveBundleStepId = Integer.parseInt(moveStep)
					
					def result = stepSnapshotService.createManualSnapshot( moveBundleId, moveBundleStepId, tasksCompleted )
				
					if (result == 200){
						render ("Record created")
						flash.result = "Record created"
					} else {
						response.sendError( result , "Error ${result}" )
						flash.result = "Error ${result}"
						status.setRollbackOnly()
					}
				}
				redirect(action:show,params:[id:moveBundleId, projectId:params.projectId ])
			} catch(NumberFormatException nfe) {
				response.sendError( 400, "Bad Request NFE")
				status.setRollbackOnly()
			}
		}
	*/
		try {
			def moveBundleId = Integer.parseInt(params.moveBundleId)
			def moveBundleStepId = Integer.parseInt(params.moveBundleStepId)
			def tasksCompleted = Integer.parseInt(params.tasksCompleted)
			if ( tasksCompleted < 0 || tasksCompleted > 100 ) {
				response.sendError( 400, "Bad Request P")
				// render("400 Bad Request")
				return false
			}
			
			def result = stepSnapshotService.createManualSnapshot( moveBundleId, moveBundleStepId, tasksCompleted)
			
			if (result == 200)
				render ("Record created")
				else
					response.sendError( result , "Error ${result}" )
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace()
			response.sendError( 400, "Bad Request NFE")
		}
	 
	}
	/* if the checkbox is subsequently checked and the form submitted, a new MoveBundleStep shall be created for that transition.
	 *  @param moveBundleId
	 * 	@param transitionId
	 * 	@return  new moveBundleStep
	 */
	def createMoveBundleStep = {
		def moveBundle = MoveBundle.get( params.moveBundleId )
		def transitionId = Integer.parseInt( params.transitionId )
		def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundle , transitionId) 
		if( !moveBundleStep ){	
			moveBundleStep = new MoveBundleStep(moveBundle:moveBundle, transitionId:transitionId, calcMethod:"L")
			moveBundleStep.label = stateEngineService.getDashboardLabel( moveBundle.project.workflowCode, transitionId )
			if ( !moveBundleStep.validate() || !moveBundleStep.save(flush:true) ) {
				def etext = "Unable to create moveBundleStep" +
				GormUtil.allErrorsString( moveBundleStep )
				response.sendError( 500, "Validation Error")
		       	println etext
			}
		}
		render moveBundleStep 
	}
	/*-----------------------------------------------------
	 * remote function to verify stepSnapshot records for a list of steps.
	 * if there are more than one snapshots associated with any of the step in list 
	 * then return failure otherwise success.
	 * @param  : moveBundleId, list of unchecked steps
	 * @return : success / failure
	 *---------------------------------------------------*/
	def checkStepSnapshotRecord = {
		def steps = params.steps
		def moveBundle = MoveBundle.findById( params.moveBundleId )
		def transitionIds
		def message = "success"
		if(steps){
			transitionIds =	steps.split(",")
		}
		transitionIds.each{ transitionId ->
			def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId( moveBundle, transitionId )
			if(moveBundleStep){
				def stepSnapshot = StepSnapshot.findAllByMoveBundleStep( moveBundleStep )
				if(stepSnapshot.size() > 1){
					message = "failure"
					return
				}
			}
		}
		 render message;
	}
	
	def projectMoveBundles = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def moveBundlesList 
		if(projectId){
			moveBundlesList = MoveBundle.findAllByProject(Project.get(projectId),[sort:'name',order:'asc'])
	 	}
		render moveBundlesList as JSON
	}
	/*
	 * Clear any transitions for the assets in that move bundle. 
	 * @param : bundleId, ProjectId
	 * */
	def clearBundleAssetHistory = {
		def bundleId = params.id
		if(bundleId){
			def moveBundle = MoveBundle.get( bundleId )
			AssetTransition.executeUpdate("DELETE FROM AssetTransition at WHERE at.assetEntity in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundle.id})" )
			AssetEntity.executeUpdate("UPDATE AssetEntity ae SET ae.currentStatus = null WHERE ae.moveBundle = ?", [ moveBundle ] )
			ProjectAssetMap.executeUpdate("DELETE FROM ProjectAssetMap WHERE asset in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundle.id})")
			MoveBundleStep.executeUpdate("UPDATE MoveBundleStep mbs SET mbs.actualStartTime = null , mbs.actualCompletionTime = null WHERE mbs.moveBundle = ?", [ moveBundle ] )
			stepSnapshotService.process( bundleId )
		}
		redirect(action:show,params:[id:params.id, projectId:params.projectId])
	}
}
