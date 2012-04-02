import grails.converters.JSON

import java.text.SimpleDateFormat

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil


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

		if(!params.sort) params.sort = 'startTime'
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
		stateEngineService.loadWorkflowTransitionsIntoMap(moveBundleInstance.workflowCode, 'project')
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
			def workflowCodes = stateEngineService.getWorkflowCode()


			return [ moveBundleInstance : moveBundleInstance, projectId: projectId, managers: managers, projectManager: projectManager,
				moveManager: moveManager, dashboardSteps: allDashboardSteps.dashboardSteps, remainingSteps : remainingSteps, workflowCodes:workflowCodes]

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
			moveBundleInstance.workflowCode = params.workflowCode
			if(params.useOfPlanning){
				moveBundleInstance.useOfPlanning = true
			}else{
				moveBundleInstance.useOfPlanning = false
			}
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
			moveBundleInstance.tempForUpdate = Math.random().toString()
			if(moveBundleInstance.validate(true) && moveBundleInstance.save() ) {
				stateEngineService.loadWorkflowTransitionsIntoMap(moveBundleInstance.workflowCode, 'project')
				def stepsList = stateEngineService.getDashboardSteps( moveBundleInstance.workflowCode )
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
		def projectInstance = Project.get(projectId)
		def workflowCodes = stateEngineService.getWorkflowCode()
		moveBundleInstance.properties = params
		def managers = partyRelationshipService.getProjectStaff( projectId )
		return ['moveBundleInstance':moveBundleInstance, managers: managers, projectId: projectId ,projectInstance:projectInstance,workflowCodes:workflowCodes]

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
		if(params.useOfPlanning){
			moveBundleInstance.useOfPlanning = true
		}else{
			moveBundleInstance.useOfPlanning = false
		}
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
			AssetEntity.executeUpdate("UPDATE AssetEntity ae SET ae.currentStatus = null WHERE ae.moveBundle = ?", [moveBundle])
			ProjectAssetMap.executeUpdate("DELETE FROM ProjectAssetMap WHERE asset in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundle.id})")
			MoveBundleStep.executeUpdate("UPDATE MoveBundleStep mbs SET mbs.actualStartTime = null , mbs.actualCompletionTime = null WHERE mbs.moveBundle = ?", [moveBundle])
			stepSnapshotService.process( bundleId )
		}
		redirect(action:show,params:[id:params.id, projectId:params.projectId])
	}
	/**
	 * 
	 */
	def planningStats = {
		def projectId = params.projectId
		if(!projectId){
			projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		}
		def project = Project.get(projectId)
		def appList = []
		def assetList = []
		def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)

		def applicationCount = AssetEntity.findAllByAssetTypeAndProject('Application',project).size()
		def assetCount = AssetEntity.findAllByProjectAndAssetTypeNotInList(project,[
			'Application',
			'Database',
			'Files'
		],params).size()
		def assignedAssetCount
		def assignedApplicationCount

		moveBundleList.each{ moveBundle->
			assignedApplicationCount = AssetEntity.findAllByMoveBundleAndAssetType(moveBundle,"Application").size()
			appList << ['count':assignedApplicationCount]
			def physicalAssetCount = AssetEntity.findAllByMoveBundleAndAssetType(moveBundle,'Server').size()
			def virtualAssetCount = AssetEntity.findAllByMoveBundleAndAssetType(moveBundle,'VM').size()
			def count = AssetEntity.findAllByMoveBundleAndAssetTypeInList(moveBundle,['Server', 'VM'],params).size()
			def likelyLatency = Application.findAllByMoveBundleAndLatency(moveBundle,'N').size()
			def unlikelyLatency = Application.findAllByMoveBundleAndLatency(moveBundle,'Y').size()
			def unknownLatency = Application.findAllByMoveBundleAndLatency(moveBundle,null).size()
			def potential = 0
			def optional = 0
			potential = AppMoveEvent.findAll("from AppMoveEvent where application.moveBundle = $moveBundle.id and value != 'N' group by application").size()
			optional = AppMoveEvent.findAll("from AppMoveEvent where application.moveBundle = $moveBundle.id and value = 'N' group by application").size()
			assetList << ['physicalCount':physicalAssetCount,'virtualAssetCount':virtualAssetCount,'count':count,'likelyLatency':likelyLatency,
						'unlikelyLatency':unlikelyLatency,'unknownLatency':unknownLatency,'potential':potential,'optional':optional]
		}
		def unassignedAppCount = AssetEntity.findAll("from AssetEntity where project = $projectId and assetType=? and (planStatus is null or planStatus in ('Unassigned',''))",['Application']).size()
		def totalAssignedApp = applicationCount - unassignedAppCount ;
		int percentageAppCount = 0 ;
		if(applicationCount > 0){
			percentageAppCount = Math.round((totalAssignedApp/applicationCount)*100)
		}else{
			percentageAppCount = 100;
		}
		def unassignedAssetCount = AssetEntity.findAll("from AssetEntity where project = $projectId and assetType='Server' and (planStatus is null or planStatus in ('Unassigned',''))").size()
		String moveBundles = moveBundleList.id
		moveBundles = moveBundles.replace("[","('").replace(",","','").replace("]","')")
		def unassignedPhysialAssetCount = AssetEntity.findAll("from AssetEntity where project = $projectId and assetType='Server' and (planStatus is null or planStatus in ('Unassigned','')) and moveBundle in $moveBundles ").size()
		def unassignedVirtualAssetCount = AssetEntity.findAll("from AssetEntity where project = $projectId and assetType='VM' and (planStatus is null or planStatus in ('Unassigned',''))  and moveBundle in $moveBundles ").size()

		def assignedPhysicalAsset = moveBundleList ? AssetEntity.countByAssetTypeAndMoveBundleInList('Server',moveBundleList) : 0
		def assignedVirtualAsset = moveBundleList ? AssetEntity.countByAssetTypeAndMoveBundleInList('VM',moveBundleList) : 0
		def totalPhysicalAssetCount = assignedPhysicalAsset + unassignedPhysialAssetCount ;
		def totalVirtualAssetCount = assignedVirtualAsset + unassignedVirtualAssetCount ;

		int percentagePhysicalAssetCount = 0;
		int percentagevirtualAssetCount = 0;
		if(unassignedPhysialAssetCount==assignedPhysicalAsset){
			percentagePhysicalAssetCount = 0;
		}else if(totalPhysicalAssetCount > 0){
			percentagePhysicalAssetCount = 100-Math.round((unassignedPhysialAssetCount/assignedPhysicalAsset)*100)
		}else if (unassignedPhysialAssetCount==0){
			percentagePhysicalAssetCount=100;
		}
		if(unassignedVirtualAssetCount==assignedVirtualAsset){
			percentagevirtualAssetCount = 0;
		}else if(totalVirtualAssetCount > 0){
			percentagevirtualAssetCount = 100-Math.round((unassignedVirtualAssetCount/assignedVirtualAsset)*100)
		}else if(unassignedVirtualAssetCount==0){
			percentagevirtualAssetCount = 100;
		}
		def physicalCount=0;
		def virtualCount=0;
		def likelyLatencyCount=0;
		def unlikelyLatencyCount=0;
		def unknownLatencyCount=0;
		assetList.each{asset->
			physicalCount = physicalCount + asset.physicalCount
			virtualCount = virtualCount + asset.virtualAssetCount
			likelyLatencyCount = likelyLatencyCount+asset.likelyLatency
			unlikelyLatencyCount = unlikelyLatencyCount + asset.unlikelyLatency
			unknownLatencyCount = unknownLatencyCount+asset.unknownLatency
		}

		def applicationsOfPlanningBundle = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Application',moveBundleList)
		def serversOfPlanningBundle = AssetEntity.findAllByAssetTypeInListAndMoveBundleInList(['Server', 'VM', 'Blade'],moveBundleList)

		def appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInListOrDependentInList(applicationsOfPlanningBundle, applicationsOfPlanningBundle) : 0
		def serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInListOrDependentInList(serversOfPlanningBundle, serversOfPlanningBundle) : 0
		def pendingAppDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByDependentInListAndStatusNotEqual(applicationsOfPlanningBundle,"Validated") : 0
		def pendingServerDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByDependentInListAndStatusNotEqual(serversOfPlanningBundle,"Validated") : 0

		def issues = AssetComment.findAll("FROM AssetComment a where a.assetEntity.project = ? and a.commentType = ? and a.isResolved = 0",[project, "issue"])
		
		def assetDependencyList = jdbcTemplate.queryForList(""" select dependency_bundle as dependencyBundle from  asset_dependency_bundle group by dependency_bundle order by dependency_bundle  limit 10 ;""")
		Date date 
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time
		date = AssetDependencyBundle.findAll().lastUpdated[0]
		time = formatter.format(date)
		def planningConsoleList = []
		assetDependencyList.each{dependencyBundle->
				def assetDependentlist=AssetDependencyBundle.findAllByDependencyBundle(dependencyBundle.dependencyBundle)
				def appCount = assetDependentlist.findAll{it.asset.assetType == 'Application'}.size()
				def serverCount = assetDependentlist.findAll{it.asset.assetType == 'Server'}.size()
				def vmCount = assetDependentlist.findAll{it.asset.assetType == 'VM'}.size()
				planningConsoleList << ['dependencyBundle':dependencyBundle.dependencyBundle,'appCount':appCount,'serverCount':serverCount,'vmCount':vmCount]
		}

		return [moveBundleList:moveBundleList, applicationCount:applicationCount,appList:appList,unassignedAppCount:unassignedAppCount,
			percentageAppCount:percentageAppCount,assetCount:assetCount,assetList:assetList,unassignedPhysialAssetCount:unassignedPhysialAssetCount,
			unassignedVirtualAssetCount:unassignedVirtualAssetCount,percentagePhysicalAssetCount:percentagePhysicalAssetCount,
			percentagevirtualAssetCount:percentagevirtualAssetCount,physicalCount:physicalCount, virtualCount:virtualCount,
			appDependenciesCount:appDependenciesCount,serverDependenciesCount:serverDependenciesCount, pendingAppDependenciesCount:pendingAppDependenciesCount,
			pendingServerDependenciesCount:pendingServerDependenciesCount, issuesCount : issues.size(),likelyLatencyCount:likelyLatencyCount,unlikelyLatencyCount:unlikelyLatencyCount,
			unknownLatencyCount:unknownLatencyCount,unassignedAssetCount:unassignedAssetCount,project:project,planningConsoleList:planningConsoleList,date:time]
	}
	/**
	 * 
	 */
	def planningConsole = {
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		Date date 
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time
		date = AssetDependencyBundle.findAll().lastUpdated[0]
		time = formatter.format(date)
		return[dependencyType:dependencyType,dependencyStatus:dependencyStatus,date:time]
	}
	def dependencyBundleDetails = { render(template:"dependencyBundleDetails") }
	
	def generateDependency ={
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def date = new Date()
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time = formatter.format(date);
		def projectInstance = Project.get(projectId)
		def status = request.getParameterValues( "connection" )
		String connections = status.toString()
		connections = connections.replace("[","('").replace("]","')").replace(",","','").replace(" ","")
		def connectionType = request.getParameterValues( "status" )
		String statusType = connectionType.toString()
		statusType = statusType.replace("[","('").replace("]","')").replace(",","','").replace(" ","")
		String movebundleList = MoveBundle.findAllByUseOfPlanningAndProject(true,projectInstance).id
		movebundleList = movebundleList.replace("[","('").replace("]","')").replace(",","','")
		def queryForAssets = """SELECT COUNT(ad.asset_id) as dependentCount, a.asset_entity_id as assetId FROM asset_entity a
														LEFT JOIN asset_dependency ad on a.asset_entity_id = ad.asset_id
														WHERE a.asset_type in ('server','vm','blade','Application','Files','Database')
													AND a.move_bundle_id in ${movebundleList} """
		if(connections!='null'  ){
			queryForAssets += " AND ad.type in ${connections} "
		}
		if(statusType!='null'){
		    queryForAssets += " AND ad.status in ${statusType} "
		}
		queryForAssets += " GROUP BY a.asset_entity_id ORDER BY COUNT(ad.asset_id) DESC "
		def assetIds = jdbcTemplate.queryForList(queryForAssets )


		int currentDependencyBundleNumber = 1;
		int assetDependencyBundle  = currentDependencyBundleNumber ;
		String AssetDependencyBundleSource = "Initial"
		int loopLevel = 1;
		int assetsAddedTo = 0;
		int assetsAddedFm = 0;
		int assetsAddedX = 0;
		def dependencyList = []
		jdbcTemplate.execute("""DELETE  FROM asset_dependency_bundle""")
		assetIds.each{asset1 ->
			def firstAssetinstance = AssetEntity.get(asset1.assetId)
			assetIds.each{asset2 ->
				def assetInstance = AssetEntity.get(asset1.assetId)
				if(AssetDependency.countByDependent(assetInstance)>0){
					assetDependencyBundle = AssetDependency.countByDependent(assetInstance);
					AssetDependencyBundleSource = "Dependency";
					assetsAddedFm = assetsAddedFm + 1;
					loopLevel = 1;
				}else if(AssetDependency.countByAsset(assetInstance)>0){
					assetDependencyBundle = AssetDependency.countByAsset(assetInstance);
					AssetDependencyBundleSource = "Dependency";
					assetsAddedTo = assetsAddedTo + 1;
					loopLevel = 1;
				}else{
					assetDependencyBundle = currentDependencyBundleNumber
					AssetDependencyBundleSource = "Initial"
					assetsAddedX = assetsAddedX + 1;
					loopLevel = 1;
				}
			}
				loopLevel++
				if(loopLevel == 3 ){
					currentDependencyBundleNumber++
				}
				def aseetDependencyBundle = new AssetDependencyBundle()
				aseetDependencyBundle.asset =  firstAssetinstance
				aseetDependencyBundle.dependencySource = AssetDependencyBundleSource
				aseetDependencyBundle.dependencyBundle = assetDependencyBundle
				aseetDependencyBundle.lastUpdated = date
				
				if(!aseetDependencyBundle.save(flush:true)){
					aseetDependencyBundle.errors.allErrors.each { println it }
				}
			
		}
		def planningConsoleList = []
		def assetDependencyList = jdbcTemplate.queryForList(""" select dependency_bundle as dependencyBundle from  asset_dependency_bundle group by dependency_bundle order by dependency_bundle  limit 10 ;""")
		assetDependencyList.each{dependencyBundle->
			    def assetDependentlist=AssetDependencyBundle.findAllByDependencyBundle(dependencyBundle.dependencyBundle)
				def appCount = assetDependentlist.findAll{it.asset.assetType == 'Application'}.size()
				def serverCount = assetDependentlist.findAll{it.asset.assetType == 'Server'}.size()
				def vmCount = assetDependentlist.findAll{it.asset.assetType == 'VM'}.size()
				planningConsoleList << ['dependencyBundle':dependencyBundle.dependencyBundle,'appCount':appCount,'serverCount':serverCount,'vmCount':vmCount]
		}
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		render(view:'planningConsole',model:[assetDependencyList:assetDependencyList,dependencyType:dependencyType,planningConsoleList:planningConsoleList,date:time,dependencyStatus:dependencyStatus] )
	}
}
