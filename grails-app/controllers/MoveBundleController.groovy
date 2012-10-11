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
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil


class MoveBundleController {

	def stepSnapshotService
	def partyRelationshipService
	def userPreferenceService
	def stateEngineService
	def moveBundleService
	def jdbcTemplate

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
		def projectId =  getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(!moveBundleInstance) {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:list)
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
				moveManager: moveManager, dashboardSteps: allDashboardSteps.dashboardSteps?.sort{it["step"].id}, remainingSteps : remainingSteps, workflowCodes:workflowCodes]

		}
	}

	def update = {
		def moveBundleInstance = MoveBundle.get( params.id )
		def projectId =  getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
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
				redirect(action:show,id:moveBundleInstance.id)
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
		def projectId =  getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def projectInstance = Project.get(projectId)
		def workflowCodes = stateEngineService.getWorkflowCode()
		moveBundleInstance.properties = params
		def managers = partyRelationshipService.getProjectStaff( projectId )
		return ['moveBundleInstance':moveBundleInstance, managers: managers ,projectInstance:projectInstance,workflowCodes:workflowCodes]

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
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
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
			redirect(action:show,params:[id:moveBundleInstance.id])
		}
		else {
			def workflowCodes = stateEngineService.getWorkflowCode()
			render(view:'create',model:[moveBundleInstance:moveBundleInstance, managers: managers, projectManager: projectManager, moveManager: moveManager,workflowCodes:workflowCodes])
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
			moveBundleStep.label = stateEngineService.getDashboardLabel( moveBundle.project.workflowCode, transitionId )
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
		String mbList = moveBundleList.id.toString().replace('[', '(').replace(']', ')')
		def applicationCount = moveBundleList ? Application.executeQuery("select count(ae) from Application ae where ae.assetType = 'Application' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def assetCount = AssetEntity.findAllByProjectAndAssetTypeNotInList(project,[
			'Application',
			'Database',
			'Files'
		],params).size()
		def databaseCount= moveBundleList ? Database.executeQuery("select count(ae) from Database ae where ae.assetType = 'Database' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def fileCount=moveBundleList ? Files.executeQuery("select count(ae) from Files ae where ae.assetType = 'Files' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def otherAssetCount= moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType not in ('Server','VM','Blade','Application','Files','Database','Appliances') and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def assignedAssetCount
		def assignedApplicationCount
        def dbList = []
		def filesList = []
		def otherTypeList = []
		moveEventList.each{ moveEvent->
			def moveBundles = moveEvent?.moveBundles
			def moveBundle = moveBundles.findAll {it.useOfPlanning == true}
			def moveBundleIds =  moveBundle.id.toString().replace("[","(").replace("]",")")
			assignedApplicationCount = moveBundleIds ? Application.executeQuery("select count(ae) from Application ae where ae.assetType = 'Application' and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
			appList << ['count':assignedApplicationCount , 'moveEvent':moveEvent.id]
			def startDate = moveBundle.startTime.sort()
			startDate?.removeAll([null])
			if(startDate.size()>0){
				if(startDate[0]){
				   bundleStartDate << bundleTimeformatter.format(startDate[0]) 
				}
			}
			def physicalAssetCount = moveBundleIds ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType in ('Server','Blade') and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : ""
			def virtualAssetCount = moveBundleIds ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType = 'VM' and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : ""
			def count = moveBundleIds ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType in ('Server', 'VM', 'Blade') and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : ""
			def potential = 0
			def optional = 0
			String moveBundleId =  moveBundle.id
			moveBundleId = moveBundleId.replace("[","('").replace(",","','").replace("]","')")
			def moveEventId = moveEventList.id 
			String moveEvents = moveEventId
			moveEvents = moveEvents.replace("[","('").replace(",","','").replace("]","')")
			def allMoveBundle = moveEventList.moveBundles.id
			allMoveBundle.remove(moveBundle.id)
			String eventMoveBundles = allMoveBundle
			eventMoveBundles = eventMoveBundles.replace("[[","('").replace(",", "','").replace("], [","','").replace("]]","')").replace("]',' [", "','")
			if(allMoveBundle.size()>0){
				potential = Application.findAll("from AppMoveEvent am right join am.application a where a.moveBundle.useOfPlanning = true and a.project=$projectId and (a.moveBundle.moveEvent != ${moveEvent.id} or a.moveBundle.moveEvent is null) and (am.moveEvent = ${moveEvent.id} or am.moveEvent is null) and (am.value = '?' or am.value is null or am.value = '') and (a.planStatus is null or a.planStatus in ('Unassigned',''))").size()
				optional = Application.findAll("from AppMoveEvent am right join am.application a where a.moveBundle.useOfPlanning = true  and a.project=$projectId and (a.moveBundle.moveEvent != ${moveEvent.id} or a.moveBundle.moveEvent is null) and (am.moveEvent = ${moveEvent.id} or am.moveEvent is null) and am.value = 'Y' and (a.planStatus is null or a.planStatus in ('Unassigned',''))").size()
			}
			assetList << ['physicalCount':physicalAssetCount,'virtualAssetCount':virtualAssetCount,'count':count,'potential':potential,'optional':optional,'moveEvent':moveEvent.id]
			def dbCount = moveBundleIds ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType = 'Database' and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle.id is null) and ae.project.id = ${projectId}")[0] : ""
			dbList << ['moveEvent':moveEvent.id , 'count':dbCount]
			def filesCount = moveBundleIds ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType = 'Files' and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle.id is null) and ae.project.id = ${projectId}")[0] : ""
			filesList << ['moveEvent':moveEvent.id , 'count':filesCount]
			def otherCount = moveBundleIds ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType not in ('Server','VM','Blade','Application','Files','Database') and (ae.moveBundle.id in ${moveBundleIds} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : ""
			otherTypeList << ['moveEvent':moveEvent.id , 'count':otherCount]
		}
		def unassignedAppCount = Application.executeQuery("select count(ap) from Application ap where ap.project = $projectId and ap.assetType='Application' and ap.moveBundle in $mbList and (ap.planStatus is null or ap.planStatus in ('Unassigned',''))")[0]
		def totalAssignedApp = applicationCount - unassignedAppCount ;
		int percentageAppCount = Application.executeQuery("select count(ap) from Application ap where ap.project = $projectId and ap.assetType='Application' and (ap.moveBundle in $mbList or ap.moveBundle is null ) and ap.moveBundle.moveEvent.runbookStatus = 'Done' ")[0]
		if(applicationCount > 0){
			percentageAppCount = Math.round((percentageAppCount/applicationCount)*100)
		}else{
			percentageAppCount = 100;
		}
		String moveBundles = moveBundleList.id
		moveBundles = moveBundles.replace("[","('").replace(",","','").replace("]","')")
		def unassignedAssetCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType in ('Server','VM','Blade') and (ae.planStatus is null or ae.planStatus in ('Unassigned','')) and (ae.moveBundle in $mbList or ae.moveBundle is null) ")[0] : 0
		def unassignedPhysialAssetCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType in ('Server','Blade') and (ae.planStatus is null or ae.planStatus in ('Unassigned','')) and (ae.moveBundle in $mbList or ae.moveBundle is null)")[0] : 0
		def unassignedVirtualAssetCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType='VM' and (ae.planStatus is null or ae.planStatus in ('Unassigned',''))  and (ae.moveBundle in $mbList or ae.moveBundle is null) ")[0] : 0
		def unassignedDbCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType ='Database' and (ae.planStatus is null or ae.planStatus in ('Unassigned','')) and (ae.moveBundle in $mbList or ae.moveBundle is null) ")[0] : 0
		def unassignedFilesCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType ='Files' and (ae.planStatus is null or ae.planStatus in ('Unassigned','')) and (ae.moveBundle in $mbList or ae.moveBundle is null) ")[0] : 0
		def unassignedOtherCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType not in ('Server','VM','Blade','Application','Database','Files') and (ae.planStatus is null or ae.planStatus in ('Unassigned','')) and (ae.moveBundle in $mbList or ae.moveBundle is null)")[0] : 0

		def assignedPhysicalAsset = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType in ('Server','Blade') and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def assignedVirtualAsset = moveBundleList ?  AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType in ('VM') and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def totalPhysicalAssetCount = assignedPhysicalAsset + unassignedPhysialAssetCount ;
		def totalVirtualAssetCount = assignedVirtualAsset + unassignedVirtualAssetCount ;
		int percentagePhysicalAssetCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType in ('Server','Blade')  and (ae.moveBundle in $mbList or ae.moveBundle is null) and ae.planStatus = 'Moved'")[0] : 0
		int percentagevirtualAssetCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType = 'VM'  and (ae.moveBundle in $mbList or ae.moveBundle is null) and ae.planStatus = 'Moved'")[0] : 0
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
		
		
		int percentageDBCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType = 'Database'  and (ae.moveBundle in $mbList or ae.moveBundle is null) and ae.planStatus = 'Moved'")[0] : 0
		if(databaseCount > 0){
			percentageDBCount = Math.round((percentageDBCount/databaseCount)*100)
		}else{
			percentageDBCount = 100;
		}
		
		int percentageFilesCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType = 'Files'  and (ae.moveBundle in $mbList or ae.moveBundle is null) and  ae.planStatus = 'Moved'")[0] : 0
		if(fileCount > 0){
			percentageFilesCount = Math.round((percentageFilesCount/fileCount)*100)
		}else{
			percentageFilesCount = 100;
		}
		
		int percentageOtherCount = moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.project = $projectId and ae.assetType not in ('Server','VM','Blade','Application','Database','Files') and (ae.moveBundle in $mbList or ae.moveBundle is null) and ae.planStatus = 'Moved'")[0] : 0
		if(otherAssetCount > 0){
			percentageOtherCount = Math.round((percentageOtherCount/otherAssetCount)*100)
		}else{
			percentageOtherCount = 100;
		}
		def physicalCount= moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType in ('Server','Blade') and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def	virtualCount=moveBundleList ? AssetEntity.executeQuery("select count(ae) from AssetEntity ae where ae.assetType = 'VM' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def likelyLatencyCount=0;
		def unlikelyLatencyCount=0;
		def unknownLatencyCount=0;

		def applicationsOfPlanningBundle = moveBundleList ? AssetEntity.findAll("from AssetEntity ae where ae.assetType = 'Application' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}") : 0
		def serversOfPlanningBundle = moveBundleList ? AssetEntity.findAll("from AssetEntity ae where ae.assetType in ('Server', 'VM', 'Blade') and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}") : 0

		def appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInList(applicationsOfPlanningBundle) : 0
		def serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInList(serversOfPlanningBundle) : 0
		def pendingAppDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInListAndStatusInList(applicationsOfPlanningBundle,['Unknown','Questioned']) : 0
		def pendingServerDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInListAndStatusInList(serversOfPlanningBundle,['Unknown','Questioned']) : 0

		def issues = AssetComment.findAll("FROM AssetComment a where a.assetEntity.project = ? and a.commentType = ? and a.isResolved = 0 and category in ('general','planning')",[project, "issue"])

		def assetDependencyList = jdbcTemplate.queryForList(""" select dependency_bundle as dependencyBundle from  asset_dependency_bundle where project_id = $projectId group by dependency_bundle order by dependency_bundle  limit 48 ;""")
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time
		def date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated",order:"desc"])?.lastUpdated
		if(date){
			time = formatter.format(date)
		}
		def today = new Date()
		def dueOpenIssue = AssetComment.findAll('from AssetComment a  where a.assetEntity.project = ? and a.category= ? and a.dueDate < ? and a.isResolved = 0',[project,'discovery',today]).size()
		def openIssue =  AssetComment.findAll('from AssetComment a  where a.assetEntity.project = ? and a.category= ? and a.isResolved = 0',[project,'discovery']).size()
		def generalOverDue = AssetComment.findAll("from AssetComment a  where a.assetEntity.project = ? and a.category in ('general','planning')  and a.dueDate < ? and a.isResolved = 0",[project,today]).size()

		def planningConsoleList = []
		assetDependencyList.each{dependencyBundle->
			def assetDependentlist=AssetDependencyBundle.findAllByDependencyBundleAndProject(dependencyBundle.dependencyBundle,project)
			def appCount = assetDependentlist.findAll{it.asset.assetType == 'Application'}.size()
			def serverCount = assetDependentlist.findAll{it.asset.assetType == 'Server' || it.asset.assetType == 'Blade' }.size()
			def vmCount = assetDependentlist.findAll{it.asset.assetType == 'VM'}.size()
			planningConsoleList << ['dependencyBundle':dependencyBundle.dependencyBundle,'appCount':appCount,'serverCount':serverCount,'vmCount':vmCount]
		}
		
		def likelyLatency = moveBundleList ? Application.executeQuery("select count(ae) from Application ae where ae.latency = 'N' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def unlikelyLatency = moveBundleList ? Application.executeQuery("select count(ae) from Application ae where ae.latency = 'Y' and (ae.moveBundle.id in ${mbList} or ae.moveBundle is null) and ae.project.id = ${projectId}")[0] : 0
		def unknownLatency = moveBundleList ? Application.executeQuery("select count(ae) from Application ae where ae.project = $projectId  and (ae.latency is null or ae.latency = '') and (ae.moveBundle in $mbList or ae.moveBundle is null)")[0] : 0
		
		def depBundleIDCountSQL = "select count(distinct dependency_bundle) from asset_dependency_bundle where project_id = $projectId"
        def dependencyBundleCount = jdbcTemplate.queryForInt(depBundleIDCountSQL)		
		
		def dependencyScan = moveBundleList ? Application.executeQuery("select count(ap) from Application ap where ap.assetType = 'Application' and ap.project.id = ${projectId} and validation = 'DependencyScan' and (ap.moveBundle.id in ${mbList} or ap.moveBundle is null)")[0] : 0
		def validated = moveBundleList ? Application.executeQuery("select count(ap) from Application ap where ap.assetType ='Application' and ap.project.id = ${projectId} and validation = 'Validated' and (ap.moveBundle.id in ${mbList} or ap.moveBundle is null)")[0] : 0
		def dependencyReview = moveBundleList ? Application.executeQuery("select count(ap) from Application ap where ap.assetType  ='Application' and ap.project.id = ${projectId} and validation = 'DependencyReview' and (ap.moveBundle.id in ${mbList} or ap.moveBundle.id is null)")[0] : 0
		def bundleReady = moveBundleList ? Application.executeQuery("select count(ap) from Application  ap where ap.assetType ='Application' and ap.project.id = ${projectId} and validation = 'BundleReady' and (ap.moveBundle.id in ${mbList} or ap.moveBundle.id is null)")[0] : 0
		
		def appToValidate = moveBundleList ? Application.executeQuery("select count(ap) from Application  ap where ap.assetType ='Application' and ap.validation = 'Discovery' and ap.project.id = ${projectId}  and (ap.moveBundle.id in ${mbList} or ap.moveBundle.id is null)")[0] : 0
		
		return [moveBundleList:moveBundleList, applicationCount:applicationCount,appList:appList,unassignedAppCount:unassignedAppCount,
			percentageAppCount:percentageAppCount, dbCount:databaseCount, fileCount:fileCount, otherAssetCount:otherAssetCount, assetCount:assetCount,assetList:assetList,
			unassignedPhysialAssetCount:unassignedPhysialAssetCount, unassignedVirtualAssetCount:unassignedVirtualAssetCount,percentagePhysicalAssetCount:percentagePhysicalAssetCount,
			percentagevirtualAssetCount:percentagevirtualAssetCount,physicalCount:physicalCount, virtualCount:virtualCount,
			appDependenciesCount:appDependenciesCount,serverDependenciesCount:serverDependenciesCount, pendingAppDependenciesCount:pendingAppDependenciesCount,
			pendingServerDependenciesCount:pendingServerDependenciesCount, issuesCount : issues.size(),likelyLatencyCount:likelyLatencyCount,unlikelyLatencyCount:unlikelyLatencyCount,
			unknownLatencyCount:unknownLatencyCount,unassignedAssetCount:unassignedAssetCount,project:project,planningConsoleList:planningConsoleList,date:time,moveBundle:moveEventList,likelyLatency:likelyLatency,
			unlikelyLatency:unlikelyLatency,unknownLatency:unknownLatency,dependencyBundleCount:dependencyBundleCount,uniqueMoveEventList:uniqueMoveEventList,planningDashboard:'planningDashboard',bundleStartDate:bundleStartDate,
			unassignedDbCount:unassignedDbCount,unassignedFilesCount:unassignedFilesCount,unassignedOtherCount:unassignedOtherCount,dbList:dbList,filesList:filesList,otherTypeList:otherTypeList,percentageOtherCount:percentageOtherCount,
			percentageDBCount:percentageDBCount,percentageFilesCount:percentageFilesCount,openIssue:openIssue,dueOpenIssue:dueOpenIssue,generalOverDue:generalOverDue, dependencyScan:dependencyScan, validated:validated,
			dependencyReview:dependencyReview, bundleReady:bundleReady, appToValidate:appToValidate]
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
	 * Assigns one or more assets to a specified move bundle
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
}

