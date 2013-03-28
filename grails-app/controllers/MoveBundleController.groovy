import grails.converters.JSON

import java.text.SimpleDateFormat

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.apache.commons.lang.StringUtils

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetTransition
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency;
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil
import com.tdsops.tm.enums.domain.*

class MoveBundleController {

	def commentService
	def jdbcTemplate
	def moveBundleService
	def partyRelationshipService
	def securityService
	def stateEngineService
	def stepSnapshotService
    def taskService
	def userPreferenceService
    
	protected static String dependecyBundlingAssetType = "('server','vm','blade','Application','Files','Database')"  
	
	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {

		if(!params.sort) params.sort = 'startTime'
		if(!params.order) params.order = 'asc'
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def moveBundleList = []
		def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance, [sort:'name'] )
		// Statements for JMESA integration
		TableFacade tableFacade = new TableFacadeImpl("tag",request)
		tableFacade.items = moveBundleInstanceList
		return [moveBundleInstanceList : moveBundleInstanceList]

	}

	def show = {
		userPreferenceService.loadPreferences("MOVE_EVENT")
		def moveBundleId = params.id

		moveBundleId = moveBundleId ? moveBundleId : session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE;
		if(moveBundleId){

			def moveBundleInstance = MoveBundle.get( moveBundleId )
			//request.getSession(false).setAttribute("MOVEBUNDLE",moveBundleInstance)
			def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ

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
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(moveBundleInstance) {
			AssetEntity.withTransaction { status ->
				try{
					// Update asset associations
					AssetEntity.executeUpdate("UPDATE AssetEntity SET moveBundle = null WHERE moveBundle = ?",[moveBundleInstance])
					// Delete Bundle and associations
					moveBundleService.deleteMoveBundleAssociates(moveBundleInstance)

					moveBundleInstance.delete(flush:true)

					flash.message = "MoveBundle ${moveBundleInstance} deleted"
					redirect(action:list)

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
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(moveBundleInstance) {
			AssetEntity.withTransaction { status ->
				try{
					// Update asset associations
					moveBundleService.deleteBundleAssetsAndAssociates(moveBundleInstance)
					// Delete Bundle and associations
					moveBundleService.deleteMoveBundleAssociates(moveBundleInstance)

					moveBundleInstance.delete(flush:true)
					flash.message = "MoveBundle ${moveBundleInstance} deleted"
					redirect(action:list)

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
		def project = securityService.getUserCurrentProject()
		if(!moveBundleInstance) {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:list)
		} else {
			def managers = partyRelationshipService.getProjectStaff( project.id )
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
			def rooms = Room.findAllByProject( project )


			return [ moveBundleInstance : moveBundleInstance, projectId: project.id, managers: managers, projectManager: projectManager,
				moveManager: moveManager, dashboardSteps: allDashboardSteps.dashboardSteps?.sort{it["step"].id}, remainingSteps : remainingSteps, workflowCodes:workflowCodes,
				rooms:rooms]

		}
	}

	def update = {
		def moveBundleInstance = MoveBundle.get( params.id )
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
			
			moveBundleInstance.sourceRoom = params.sourceRoom ? Room.read( params.sourceRoom ) : null
			
			moveBundleInstance.targetRoom = params.targetRoom ? Room.read( params.targetRoom ) : null
			
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
				redirect(action:show,id:moveBundleInstance.id)
			} else {
				//	get the all Dashboard Steps that are associated to moveBundle.project
				def allDashboardSteps = moveBundleService.getAllDashboardSteps( moveBundleInstance )
				def remainingSteps = allDashboardSteps.remainingSteps
				
				moveBundleInstance.discard()
				def project = securityService.getUserCurrentProject()
				def managers = partyRelationshipService.getProjectStaff( project.id )
				def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
				def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
				def workflowCodes = stateEngineService.getWorkflowCode()
				def rooms = Room.findAllByProject( project )
				render(view:'edit',model:[moveBundleInstance:moveBundleInstance, projectId: project.id, managers: managers, projectManager: projectManagerId,
							moveManager: moveManagerId, dashboardSteps:allDashboardSteps.dashboardSteps, remainingSteps : remainingSteps,rooms:rooms,workflowCodes:workflowCodes ])
			}
		}
		else {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:edit,id:params.id)
		}
	}

	def create = {
		def moveBundleInstance = new MoveBundle()
		def project = securityService.getUserCurrentProject()
		def workflowCodes = stateEngineService.getWorkflowCode()
		moveBundleInstance.properties = params
		def managers = partyRelationshipService.getProjectStaff( project.id )
		def rooms = Room.findAllByProject( project )
		return ['moveBundleInstance':moveBundleInstance, managers: managers ,projectInstance:project,workflowCodes:workflowCodes, rooms:rooms]

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
		
		params.sourceRoom = params.sourceRoom ? Room.read( params.sourceRoom ) : null 
		
		params.targetRoom = params.targetRoom ? Room.read( params.targetRoom ) : null

		def moveBundleInstance = new MoveBundle(params)
		def project = securityService.getUserCurrentProject()
		def projectManager = params.projectManager
		def moveManager = params.moveManager
		def managers = partyRelationshipService.getProjectStaff( project.id )
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
			redirect(action:show,params:[id:moveBundleInstance.id])
		}
		else {
			def workflowCodes = stateEngineService.getWorkflowCode()
			def rooms = Room.findAllByProject( project )
			render(view:'create',model:[moveBundleInstance:moveBundleInstance, managers: managers, projectManager: projectManager, moveManager: moveManager,workflowCodes:workflowCodes,rooms:rooms])
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
		 redirect(action:show,params:[id:moveBundleId, projectId:session.CURR_PROJ.CURR_PROJ ])
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
			moveBundleStep.label = stateEngineService.getDashboardLabel( moveBundle.workflowCode, transitionId )
			if ( !moveBundleStep.validate() || !moveBundleStep.save(flush:true) ) {
				def etext = "Unable to create moveBundleStep" +
						GormUtil.allErrorsString( moveBundleStep )
				response.sendError( 500, "Validation Error")
				log.error etext
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
	 * Clear any transitions for the assets in that bundle. 
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
		redirect(action:show,params:[id:params.id])
	}
	/**
	 * 
	 */
	def planningStats = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.get(projectId)
		def bundleTimeformatter = new SimpleDateFormat("dd-MMM")
		def appList = []
		def assetList = []
		def bundleStartDate = []
		def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
	    Set uniqueMoveEventList = moveBundleList.moveEvent
		uniqueMoveEventList.remove(null)
		
		List moveEventList = []
		moveEventList =  uniqueMoveEventList.toList()
		moveEventList.sort{it?.name}
		
		// Forming query for multi-uses
		def countQuery = "SELECT count(ae) FROM AssetEntity ae WHERE ae.assetType IN (:type) AND (ae.moveBundle IN \
			(:moveBundles) OR ae.moveBundle IS NULL) AND ae.project = :project "
		def appCountQuery = "SELECT count(ae) FROM Application ae WHERE ae.assetType =:type AND (ae.moveBundle IN \
			(:moveBundles) OR ae.moveBundle IS NULL) AND ae.project = :project "
		def dbCountQuery = "SELECT count(ae) FROM Database ae WHERE ae.assetType =:type AND (ae.moveBundle IN \
			(:moveBundles) OR ae.moveBundle IS NULL) AND ae.project = :project "
		def filesCountQuery = "SELECT count(ae) FROM Files ae WHERE ae.assetType =:type AND (ae.moveBundle IN \
			(:moveBundles) OR ae.moveBundle IS NULL) AND ae.project = :project "
			
		def otherCountQuery =  StringUtils.replace(countQuery, 'IN', 'NOT IN', 1 )
		def countArgs = [moveBundles:moveBundleList, project:project]
		
		def applicationCount = moveBundleList ? Application.executeQuery(appCountQuery, countArgs<<[type:'Application'])[0] : 0
		def assetCount = AssetEntity.findAllByProjectAndAssetTypeNotInList(project,[
			'Application',
			'Database',
			'Files'
		],params).size()
		
		def databaseCount= moveBundleList ? Database.executeQuery(dbCountQuery, countArgs<<[type:'Database'])[0] : 0
		def fileCount=moveBundleList ? Files.executeQuery(filesCountQuery, countArgs<<[type:'Files'])[0] : 0
		def otherAssetCount= moveBundleList ? AssetEntity.executeQuery(otherCountQuery, 
			countArgs<<[type:['Server','VM','Blade','Application','Files','Database','Appliances']])[0] : 0
		
		def assignedAssetCount
		def assignedApplicationCount
        def dbList = []
		def filesList = []
		def otherTypeList = []
		def openTasks = []
		
		moveEventList.each{ moveEvent->
			// fetching bundles for current moveEvent which was set 'true' for useOfPlanning 
			def moveBundle = moveEvent?.moveBundles?.findAll {it.useOfPlanning == true}
			def eventWiseArgs = [project:project, moveBundles:moveBundle]
			
			// fetching application count that are assigned to current move-event .
			assignedApplicationCount = moveBundle ? Application.executeQuery(appCountQuery, eventWiseArgs << [type:'Application'])[0] : 0
			appList << ['count':assignedApplicationCount , 'moveEvent':moveEvent.id]
			
			def startDate = moveBundle.startTime.sort()
			startDate?.removeAll([null])
			if(startDate.size()>0){
				if(startDate[0]){
				   bundleStartDate << bundleTimeformatter.format(startDate[0]) 
				}
			}
			
			// fetching physicalAsset (e.g. 'Server','Blade' ) and virtualAsset (e.g. 'VM')  count that are assigned to current move-event .
			def physicalAssetCount = moveBundle ? AssetEntity.executeQuery(countQuery, eventWiseArgs << [type:['Server','Blade']])[0] : 0
			def virtualAssetCount = moveBundle ? AssetEntity.executeQuery(countQuery, eventWiseArgs << [type:['VM']])[0] : 0
			def count = moveBundle ? AssetEntity.executeQuery(countQuery, eventWiseArgs << [type:['Server', 'VM', 'Blade']])[0] : 0
			
			def potential = 0
			def optional = 0
			
			def moveEventId = moveEventList.id 
			def allMoveBundle = moveEventList.moveBundles.id.findAll{it != moveBundle.id}
			
			if(allMoveBundle.size()>0){
				def potentialQuery = "from AppMoveEvent am right join am.application a where a.moveBundle.useOfPlanning =:useOfPlanning \
						and a.project=:project and (a.moveBundle.moveEvent != :moveEvent or a.moveBundle.moveEvent is null) \
						and (am.moveEvent = :moveEvent or am.moveEvent is null)\
						and (a.planStatus is null or a.planStatus in ('Unassigned',''))"
				
				def queryArgs = [useOfPlanning:true, project:project, moveEvent:moveEvent]
				
				potential = Application.findAll(potentialQuery+" and (am.value = '?' or am.value is null or am.value = '') ",queryArgs).size()
				optional = Application.findAll(potentialQuery+" and am.value = 'Y' ",queryArgs).size()
			}
			
			assetList << ['physicalCount':physicalAssetCount,'virtualAssetCount':virtualAssetCount,'count':count,
				'potential':potential,'optional':optional,'moveEvent':moveEvent.id]
			
			def dbCount = moveBundle ? Database.executeQuery(dbCountQuery, eventWiseArgs << [type:'Database'])[0] : 0
			dbList << ['moveEvent':moveEvent.id , 'count':dbCount]
			
			def filesCount = moveBundle ? Files.executeQuery(filesCountQuery, eventWiseArgs << [type:'Files'])[0] : 0
			filesList << ['moveEvent':moveEvent.id , 'count':filesCount]
			
			def otherCount = moveBundle ? AssetEntity.executeQuery(otherCountQuery,
			eventWiseArgs << [type:['Server','VM','Blade','Application','Files','Database','Appliances']])[0] : 0
			otherTypeList << ['moveEvent':moveEvent.id , 'count':otherCount]
			
			def openIssues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status in (:status) \
				and a.moveEvent = :event" ,[project:project, type:AssetCommentType.TASK, 
				status: [AssetCommentStatus.READY,AssetCommentStatus.STARTED], event:moveEvent])
			
			openTasks << ['moveEvent':moveEvent.id , 'count':openIssues.size()]
			
		}
		
		def unAssignedCountQuery = countQuery+" AND (ae.planStatus IS NULL OR ae.planStatus IN ('Unassigned',''))"
		def unAssignedDBCountQuery = dbCountQuery+" AND (ae.planStatus IS NULL OR ae.planStatus IN ('Unassigned',''))"
		def unAssignedFilesCountQuery = filesCountQuery+" AND (ae.planStatus IS NULL OR ae.planStatus IN ('Unassigned',''))"
		
		def unassignedAppCount =  moveBundleList ? Application.executeQuery("SELECT COUNT(ae) FROM Application ae WHERE ae.project = :project \
			AND ae.assetType =:type AND ae.moveBundle IN (:moveBundles) AND (ae.planStatus is null OR ae.planStatus IN ('Unassigned',''))",
			countArgs <<[type:'Application'])[0] : 0
			
		def totalAssignedApp = applicationCount - unassignedAppCount ;
		
		int percentageAppCount = moveBundleList ? AssetEntity.executeQuery(appCountQuery+"and ae.moveBundle.moveEvent.runbookStatus=:runbookStatus ",
			countArgs <<[type:'Application', runbookStatus:'Done'])[0] : 0
		
		countArgs.remove('runbookStatus')
		
		if(applicationCount > 0){
			percentageAppCount = Math.round((percentageAppCount/applicationCount)*100)
		}else{
			percentageAppCount = 0;
		}
		
		def unassignedAssetCount = moveBundleList ? AssetEntity.executeQuery(unAssignedCountQuery, countArgs <<[type:['Server','VM','Blade']])[0] : 0
		
		def unassignedPhysialAssetCount = moveBundleList ? AssetEntity.executeQuery(unAssignedCountQuery, countArgs <<[type:['Server','Blade']])[0] : 0
		
		def unassignedVirtualAssetCount = moveBundleList ? AssetEntity.executeQuery(unAssignedCountQuery, countArgs <<[type:['VM']])[0] : 0
		
		def unassignedDbCount = moveBundleList ? Database.executeQuery(unAssignedDBCountQuery, countArgs <<[type:'Database'])[0] : 0
		
		def unassignedFilesCount = moveBundleList ? Files.executeQuery(unAssignedFilesCountQuery, countArgs <<[type:'Files'])[0] : 0
		
		def unassignedOtherCount = moveBundleList ? AssetEntity.executeQuery(otherCountQuery+"and (ae.planStatus is null or ae.planStatus \
			in ('Unassigned',''))", countArgs <<[type:['Server','VM','Blade','Application','Files','Database','Appliances']])[0] : 0

		def assignedPhysicalAsset = moveBundleList ? AssetEntity.executeQuery(countQuery , countArgs <<[type:['Server','Blade']])[0] : 0
		
		def assignedVirtualAsset = moveBundleList ?  AssetEntity.executeQuery(countQuery , countArgs <<[type:['VM']])[0] : 0
		
		def totalPhysicalAssetCount = assignedPhysicalAsset + unassignedPhysialAssetCount ;
		
		def totalVirtualAssetCount = assignedVirtualAsset + unassignedVirtualAssetCount ;
		
		int percentagePhysicalAssetCount = moveBundleList ? AssetEntity.executeQuery(countQuery +"and ae.planStatus = :planStatus", 
			countArgs <<[type:['Server','Blade'], planStatus:'Moved'])[0] : 0
		
		int percentagevirtualAssetCount = moveBundleList ? AssetEntity.executeQuery(countQuery +"and ae.planStatus = :planStatus", 
			countArgs <<[type:['VM']])[0] : 0
		
		if(unassignedPhysialAssetCount==assignedPhysicalAsset){
			percentagePhysicalAssetCount = 0;
		}else if(totalPhysicalAssetCount > 0){
			percentagePhysicalAssetCount = Math.round((percentagePhysicalAssetCount/assignedPhysicalAsset)*100)
		}else if (unassignedPhysialAssetCount==0){
			percentagePhysicalAssetCount=100;
		}
		
		if(unassignedVirtualAssetCount==assignedVirtualAsset){
			percentagevirtualAssetCount = 0;
		}else if(totalVirtualAssetCount > 0){
			percentagevirtualAssetCount = Math.round((percentagevirtualAssetCount/assignedVirtualAsset)*100)
		}else if(unassignedVirtualAssetCount==0){
			percentagevirtualAssetCount = 100;
		}
		
		
		int percentageDBCount = moveBundleList ? Database.executeQuery(dbCountQuery +"and ae.planStatus = :planStatus", 
			countArgs <<[type:'Database'])[0] : 0
		
		if(databaseCount > 0){
			percentageDBCount = Math.round((percentageDBCount/databaseCount)*100)
		}else{
			percentageDBCount = 100;
		}
		
		int percentageFilesCount = moveBundleList ? Files.executeQuery(filesCountQuery +"and ae.planStatus = :planStatus", 
			countArgs <<[type:'Files'])[0] : 0
		if(fileCount > 0){
			percentageFilesCount = Math.round((percentageFilesCount/fileCount)*100)
		}else{
			percentageFilesCount = 100;
		}
		
		int percentageOtherCount = moveBundleList ? AssetEntity.executeQuery(otherCountQuery+"and ae.planStatus = :planStatus",
			countArgs <<[type:['Server','VM','Blade','Application','Files','Database','Appliances']])[0] : 0
		if(otherAssetCount > 0){
			percentageOtherCount = Math.round((percentageOtherCount/otherAssetCount)*100)
		}else{
			percentageOtherCount = 100;
		}
		countArgs.remove('planStatus')
		def physicalCount= moveBundleList ? AssetEntity.executeQuery(countQuery, countArgs <<[type:['Server','Blade']])[0] : 0
		def	virtualCount=moveBundleList ? AssetEntity.executeQuery(countQuery, countArgs <<[type:['VM']])[0] : 0
		def likelyLatencyCount=0;
		def unlikelyLatencyCount=0;
		def unknownLatencyCount=0;

		def applicationsOfPlanningBundle = moveBundleList ? Application.findAll("from Application ae where ae.assetType =:type  \
			and (ae.moveBundle in (:moveBundles) or ae.moveBundle is null) and ae.project = :project", countArgs <<[type:'Application']) : 0
		
		def serversOfPlanningBundle = moveBundleList ? AssetEntity.findAll("from AssetEntity ae where ae.assetType in (:type) \
			and (ae.moveBundle in (:moveBundles) or ae.moveBundle is null) and ae.project = :project", countArgs <<[type:['Server', 'VM', 'Blade']]) : 0
		
		def appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInList(applicationsOfPlanningBundle) : 0
		def serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInList(serversOfPlanningBundle) : 0
		
		def pendingAppDependenciesCount = applicationsOfPlanningBundle ? 
		AssetDependency.countByAssetInListAndStatusInList(applicationsOfPlanningBundle,['Unknown','Questioned']) : 0
		
		def pendingServerDependenciesCount = serversOfPlanningBundle ? 
		AssetDependency.countByAssetInListAndStatusInList(serversOfPlanningBundle,['Unknown','Questioned']) : 0

		def issues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status =:status  \
			and a.category in (:category)",[project:project, type:AssetCommentType.TASK, status: AssetCommentStatus.READY , category:['general','planning']])

		def assetDependencyList = jdbcTemplate.queryForList("select dependency_bundle as dependencyBundle from  asset_dependency_bundle \
			where project_id = $projectId  group by dependency_bundle order by dependency_bundle  limit 48")
		
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time
		def date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated",order:"desc"])?.lastUpdated
		time = date ? formatter.format(date) : ''
		
		def today = new Date()
		def issueQuery = "from AssetComment a  where a.project =:project and a.category in (:category) and a.status != :status and a.commentType =:type"
		def issueArgs = [project:project, status:AssetCommentStatus.COMPLETED, type:AssetCommentType.TASK.toString()]
		
		def openIssue =  AssetComment.findAll(issueQuery,issueArgs << [category : ['discovery']]).size()
		def dueOpenIssue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs<< [category : ['discovery'], dueDate:today]).size()
		def generalOverDue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs<< [category : ['general','planning'], dueDate:today]).size()

		def planningConsoleList = []
		assetDependencyList.each{dependencyBundle->
			def assetDependentlist=AssetDependencyBundle.findAllByDependencyBundleAndProject(dependencyBundle.dependencyBundle,project)
			def appCount = assetDependentlist.findAll{it.asset.assetType == 'Application'}.size()
			def serverCount = assetDependentlist.findAll{it.asset.assetType == 'Server' || it.asset.assetType == 'Blade' }.size()
			def vmCount = assetDependentlist.findAll{it.asset.assetType == 'VM'}.size()
			planningConsoleList << ['dependencyBundle':dependencyBundle.dependencyBundle,'appCount':appCount,'serverCount':serverCount,'vmCount':vmCount]
		}
		
		def latencyQuery = "SELECT COUNT(ap) FROM Application ap WHERE ap.latency = :latency AND (ap.moveBundle IN (:moveBundles) \
			OR ap.moveBundle IS NULL) AND ap.project = :project AND ap.assetType =:type "
		
		def likelyLatency = moveBundleList ? Application.executeQuery(latencyQuery,countArgs <<[latency:'N', type:'Application'])[0] : 0
		def unlikelyLatency = moveBundleList ? Application.executeQuery(latencyQuery,countArgs <<[latency:'Y', type:'Application'])[0] : 0
		
		def unknownLatency = moveBundleList ? Application.executeQuery("select count(ae) from Application ae where ae.project = :project \
			and (ae.latency is null or ae.latency = '') and (ae.moveBundle in (:moveBundles) or ae.moveBundle is null)",
			[project:project, moveBundles:moveBundleList])[0] : 0
		
		countArgs.remove('latency')
		
		def depBundleIDCountSQL = "select count(distinct dependency_bundle) from asset_dependency_bundle where project_id = $projectId"
        def dependencyBundleCount = jdbcTemplate.queryForInt(depBundleIDCountSQL)	
		
		def validateCountQuery = countQuery + "and ae.validation = :validation "
		def appValidateCountQuery = appCountQuery + "and ae.validation = :validation "
		def dbValidateCountQuery = dbCountQuery + "and ae.validation = :validation "
		def filesValidateCountQuery = filesCountQuery + "and ae.validation = :validation "
		
		def dependencyScan = moveBundleList ? Application.executeQuery(appValidateCountQuery ,
			countArgs << [validation:'DependencyScan'] )[0] : 0
	
		def validated = moveBundleList ? Application.executeQuery(appValidateCountQuery ,
			countArgs << [validation:'Validated'])[0] : 0
		
		def dependencyReview = moveBundleList ? Application.executeQuery(appValidateCountQuery ,
			countArgs << [validation:'DependencyReview'])[0] : 0
		
		def bundleReady = moveBundleList ? Application.executeQuery(appValidateCountQuery ,
			countArgs << [validation:'BundleReady'])[0] : 0
		
		countArgs << [validation:'Discovery']
		
		def appToValidate = moveBundleList ? Application.executeQuery(appValidateCountQuery , countArgs)[0] : 0
		def psToValidate = moveBundleList ? AssetEntity.executeQuery(validateCountQuery , countArgs<<[type:['Server','Blade']])[0] : 0
		def vsToValidate = moveBundleList ? AssetEntity.executeQuery(validateCountQuery , countArgs<<[type:['VM']])[0] : 0
		def dbToValidate = moveBundleList ? Database.executeQuery(dbValidateCountQuery , countArgs<<[type:'Database'])[0] : 0
		def fileToValidate = moveBundleList ? Files.executeQuery(filesValidateCountQuery , countArgs<<[type:'Files'])[0] : 0
		
		def otherToValidate = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity  ae where ae.assetType not in (:type)\
			and ae.validation = :validation and ae.project = :project and (ae.moveBundle  in (:moveBundles) or ae.moveBundle.id is null)",
			countArgs <<[type:['Server','VM','Blade','Application','Database','Files'] ])[0] : 0
		
		
		return [moveBundleList:moveBundleList, applicationCount:applicationCount, appList:appList, unassignedAppCount:unassignedAppCount,
			percentageAppCount:percentageAppCount, dbCount:databaseCount, fileCount:fileCount, otherAssetCount:otherAssetCount, 
			assetCount:assetCount,assetList:assetList, percentagePhysicalAssetCount:percentagePhysicalAssetCount, unlikelyLatencyCount:unlikelyLatencyCount,
			unassignedPhysialAssetCount:unassignedPhysialAssetCount, unassignedVirtualAssetCount:unassignedVirtualAssetCount,
			percentagevirtualAssetCount:percentagevirtualAssetCount, physicalCount:physicalCount, virtualCount:virtualCount,
			appDependenciesCount:appDependenciesCount, serverDependenciesCount:serverDependenciesCount,
			pendingAppDependenciesCount:pendingAppDependenciesCount, issuesCount : issues.size(), 
			pendingServerDependenciesCount:pendingServerDependenciesCount, likelyLatencyCount:likelyLatencyCount,
			unknownLatencyCount:unknownLatencyCount, unassignedAssetCount:unassignedAssetCount, project:project, planningConsoleList:planningConsoleList,
			date:time, moveBundle:moveEventList, likelyLatency:likelyLatency, unlikelyLatency:unlikelyLatency, unknownLatency:unknownLatency,
			dependencyBundleCount:dependencyBundleCount, uniqueMoveEventList:uniqueMoveEventList, planningDashboard:'planningDashboard',
			bundleStartDate:bundleStartDate, unassignedDbCount:unassignedDbCount, unassignedFilesCount:unassignedFilesCount,
			unassignedOtherCount:unassignedOtherCount, dbList:dbList, filesList:filesList, otherTypeList:otherTypeList,
			percentageOtherCount:percentageOtherCount, percentageDBCount:percentageDBCount, percentageFilesCount:percentageFilesCount,
			openIssue:openIssue, dueOpenIssue:dueOpenIssue, generalOverDue:generalOverDue, dependencyScan:dependencyScan, validated:validated,
			dependencyReview:dependencyReview, bundleReady:bundleReady, appToValidate:appToValidate, psToValidate:psToValidate, vsToValidate:vsToValidate,
			dbToValidate:dbToValidate, fileToValidate:fileToValidate, otherToValidate:otherToValidate, openTasks:openTasks]
	}
	
	/**
	 * Control function to render the Planning Console 
	 */
	def planningConsole = {
	
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		
		return moveBundleService.getPlanningConsoleMap(projectId)
	}
	
	/*
	 * Controller to render the Dependency Bundle Details
	 */
	def dependencyBundleDetails = { 
		render(template:"dependencyBundleDetails") 
	}
	
	/**
	 * Controller that generates the Dependency Groups and displays the results
	 */
	def generateDependency = {		
		
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		
		String connectionTypes = WebUtil.checkboxParamAsString( request.getParameterValues( "connection" ) )
		String statusTypes = WebUtil.checkboxParamAsString( request.getParameterValues( "status" ) )
		
		// Generate the Dependency Groups
		moveBundleService.generateDependencyGroups(projectId, connectionTypes, statusTypes)

		// Now get the model and display results
		render(template:'dependencyBundleDetails', model:moveBundleService.getPlanningConsoleMap(projectId) )
	}
	
	/**
	 * Assigns one or more assets to a specified bundle
	 */
	def saveAssetsToBundle={
		def assetArray = params.assetVal
		def moveBundleInstance = MoveBundle.findById(Integer.parseInt(params.moveBundleList))
		def assetList = assetArray.split(",")
		assetList.each{assetId->
			def assetInstance = AssetEntity.get(assetId)
			assetInstance.moveBundle = moveBundleInstance
			assetInstance.planStatus = 'Assigned'
			if(!assetInstance.save(flush:true)){
				assetInstance.errors.allErrors.each{
			          log.error it 		
				}
			}
			
		}
		forward(controller:"assetEntity",action:"getLists", params:[entity:params.assetType,dependencyBundle:session.getAttribute('dependencyBundle')])
   }
	
	/**
	 * To generate Moveday Tasks for a specified Bundle
	 * @param - bundleId - Id of the Bundle to generate the tasks for that bundle
	 * @return - error Message - if any else success Message
	 */
	def createTask = {
		
		def bundleId = params.bundleId
		def bundle = MoveBundle.get(bundleId)
        def errMsg = ""

		if (bundle.getAssetQty() == 0 ) {
			errMsg = "No assets are assigned to current bundle (${bundle.name}). As such no action was taken."
		} else {
			
            def bundleMoveEvent = bundle.moveEvent
            def userLogin = securityService.getUserLogin()
            def project = securityService.getUserCurrentProject()
            def person = userLogin.person

			// Get last task # used
			def lastTask = jdbcTemplate.queryForInt("SELECT MAX(task_number) FROM asset_comment WHERE project_id = ${project.id}")
			
			// Create the Begin Event Task
			def commentToBegin = new AssetComment(
				taskNumber:++lastTask,
				comment:'Begin Event',
			    moveEvent:bundleMoveEvent,  
				category:AssetCommentCategory.SHUTDOWN,
				Status: AssetCommentStatus.PENDING, 
				commentType:AssetCommentType.TASK, 
				project:project, 
				estStart:bundle.startTime, estFinish:bundle.completionTime, duration:0,
				createdBy:person, assignedTo:person, role:'PROJ_MGR',
				autoGenerated:true
			)
			if (!commentToBegin.save(flush:true)) {
				log.error "Failed to create Begin Event task (bundle id $bundleId): " + GormUtil.allErrorsString(commentToBegin)
				errMsg = "Failed to create Begin Event Task. Process Failed"
			}
			if (!errMsg) {
				def commentToComplete = new AssetComment(
					taskNumber:++lastTask,
					comment:'Event Completed',
					project:project,
				    moveEvent:bundleMoveEvent, 
					commentType:AssetCommentType.TASK, 
					category:AssetCommentCategory.STARTUP,
					Status:AssetCommentStatus.PENDING,
					duration:0, 
					assignedTo:person, role:'PROJ_MGR',
					createdBy:person,
					autoGenerated:true
				)
				if (!commentToComplete.save(flush:true)) {
					log.error "Failed to create Event Completed task (bundle id $bundleId): " + GormUtil.allErrorsString(commentToComplete)
					errMsg = "Failed to create Event Completed Task. Process Failed"
				}
                if (!errMsg) {
                    def bundledAssets = AssetEntity.findAll("from AssetEntity a where a.moveBundle = :bundle and a.project =:project\
						and a.assetType not in('application', 'database', 'files', 'VM')", [bundle:bundle,project:project])
                    def bundleworkFlow = bundle.workflowCode
                    def workFlow = Workflow.findByProcess(bundleworkFlow)
					// Find all sets that can be applied to servers
                    def workFlowSteps = WorkflowTransition.findAllByWorkflow(workFlow,[sort:'transId'])	
                    workFlowSteps = workFlowSteps.findAll{ 
	  					! [ WorkflowTransitionId.HOLD, WorkflowTransitionId.READY, WorkflowTransitionId.COMPLETED, WorkflowTransitionId.TERMINATED
						].contains(it.transId)}
	
					// Create the end of transit task that will be used for Off-Truck tasks afterward
                    def results = taskService.createTaskBasedOnWorkflow(
	   					[	taskNumber:++lastTask,
							workflow:workFlowSteps.find{it.transId == WorkflowTransitionId.TRANSPORT}, 
							bundleMoveEvent:bundleMoveEvent,
                        	project:project, person:person, bundle:bundle
						] )
                    def transportTask = results.stepTask

					def previousTask
					
					// Iterate over each Asset and create the various work flow steps
                    bundledAssets.each{asset->
                        workFlowSteps.eachWithIndex{ workflow, index->
                            if (workflow.transId != WorkflowTransitionId.TRANSPORT) {
                                results = taskService.createTaskBasedOnWorkflow( 
									[	taskNumber:++lastTask, 
										workflow:workflow, 
										bundleMoveEvent:bundleMoveEvent, assetEntity:asset,
                                    	project:project, person:person, bundle:bundle
									] )
                                def stepTask = results.stepTask
                                errMsg = results.errMsg
                                if (index==0) {
									// Create dependency on Begin Move task
                                    commentService.saveAndUpdateTaskDependency(stepTask, commentToBegin, null, null)
                                } else if(index==workFlowSteps.size()-1) {
									// Create task dependency on previous task and Completed task
                                    commentService.saveAndUpdateTaskDependency(commentToComplete, stepTask, null, null)
									commentService.saveAndUpdateTaskDependency(stepTask, previousTask, null, null)
                                } else if(previousTask){
									// Create task dependency on previous task
									commentService.saveAndUpdateTaskDependency(stepTask, previousTask, null, null) 
								}
								previousTask = stepTask
                            } else {
								// Create dependency on Transport Ending step
                                commentService.saveAndUpdateTaskDependency(transportTask, previousTask, null, null)
                                previousTask = transportTask
                            }
                        }
                    }
                }
                if (!errMsg) {
                    bundle.tasksCreated = true
                    if ( !bundle.save() ) {
                        log.error "Exception while updating bundle.tasksCreated = true \n"+GormUtil.allErrorsString(bundle)
						errMsg = "An unexpected error occurred while updated bundle"
                    }
                }
			}
		}
	    render errMsg ? errMsg : "Generated Tasks for Bundle - ${bundle.name} successfully."
	}
    
    /**
     * To delete all generated task for bundle
     * @Param : bundleId - Id of bundle for which generated task needs to be deleted
     * @return : void
     */
    def deleteWorkflowTasks = {
        def bundleId = params.bundleId
        def bundle = MoveBundle.get(bundleId)
        def errMsg = ""
        if(bundle.getAssetQty() > 0 ){
            def event = bundle.moveEvent
            def project = securityService.getUserCurrentProject()
            taskService.deleteBundleWorkflowTasks(bundle)
            errMsg = "Tasks Deleted Successfully."
        } else {
            errMsg = "No Asset Assigned to current Bundle - ${bundle.name}. So Process Terminated."
        }
        render errMsg
    }
}

