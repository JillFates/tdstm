import org.codehaus.groovy.grails.commons.GrailsClassUtils

import grails.converters.JSON
import grails.converters.*

import java.io.*

import org.apache.poi.*
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors

import org.hibernate.criterion.Order

import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil

class MoveEventController {
	
	protected static Log log = LogFactory.getLog( MoveEventController.class )

    // Service initialization IOC
	def controllerService
	def cookbookService
	def jdbcTemplate
	def moveBundleService
	def projectService
	def reportsService
	def runbookService
	def securityService
	def taskService
	def userPreferenceService

    def index() { redirect(action:"list",params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	/*
	 * will return the list of MoveEvents
	 */
    def list() {
    }
	
	/**
	 * 
	 */
	def listJson() {
		
		def sortIndex = params.sidx ?: 'name'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows =  Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def project = securityService.getUserCurrentProject()
		def newsBM = params.newsBarMode ? retrieveNewsBMList(params.newsBarMode) : null
		
		def events = MoveEvent.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			if (params.name)
				ilike('name', "%${params.name.trim()}%")
			if (params.description)
				ilike('description', "%${params.description}%")
			if (params.runbookStatus)
				ilike('runbookStatus', "%${params.runbookStatus}%")
			if (newsBM)
				'in'('newsBarMode', newsBM)
				
			order(new Order(sortIndex, sortOrder=='asc').ignoreCase())
		}
		
		def totalRows = events.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
			
		def results = events?.collect { [ cell: [it.name, it.description, g.message(code: "event.newsBarMode.${it.newsBarMode}"), it.runbookStatus,
					it.moveBundlesString], id: it.id,
			]}
		
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		
		render jsonData as JSON
	}
	
	/*
	 * return the MoveEvent details for selected MoveEvent
	 * @param : MoveEvent Id
	 * @return : MoveEvent details  
	 */
    def show() {
		userPreferenceService.loadPreferences("MOVE_EVENT")
		def moveEventId = params.id
		if(moveEventId){
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
			def moveBundleId = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE;
			if(moveBundleId){
				def moveBundle = MoveBundle.get( moveBundleId )
				if(moveBundle?.moveEvent?.id != Integer.parseInt(moveEventId)){
					userPreferenceService.removePreference( "CURR_BUNDLE" )
				}
			}
		} else {
			moveEventId = session.getAttribute("MOVE_EVENT")?.MOVE_EVENT;
		}
		
		if(moveEventId){
	        def moveEventInstance = MoveEvent.get( moveEventId )
	
	        if(!moveEventInstance) {
	            flash.message = "MoveEvent not found with id ${moveEventId}"
	            redirect(action:"list")
	        } else { 
	        	return [ moveEventInstance : moveEventInstance ] 
	        }
		} else {
		    redirect(action:"list")
		}
    }
	/*
	 * redirect to list once selected record deleted
	 * @param : MoveEvent Id
	 * @return : list of remaining MoveEvents
	 */
    def delete() {
    	try{
        	def moveEventInstance = MoveEvent.get( params.id )
	        if(moveEventInstance) {
    	    	def moveEventName = moveEventInstance.name
    	    	jdbcTemplate.update("DELETE FROM move_event_snapshot WHERE move_event_id = ${moveEventInstance?.id} " )
				jdbcTemplate.update("DELETE FROM move_event_news WHERE move_event_id = ${moveEventInstance?.id} " )
				jdbcTemplate.update("UPDATE move_bundle SET move_event_id = null WHERE move_event_id = ${moveEventInstance?.id} " )
				jdbcTemplate.update("DELETE FROM user_preference WHERE value = ${moveEventInstance?.id}")
				AppMoveEvent.executeUpdate("DELETE FROM AppMoveEvent where move_event_id = ${moveEventInstance?.id} ")
        	    moveEventInstance.delete()
            	flash.message = "MoveEvent ${moveEventName} deleted"
        	} else {
	            flash.message = "MoveEvent not found with id ${params.id}"
        	}
    	} catch(Exception ex){
    		flash.message = ex
    	}
    	redirect(action:"list")
    }
    /*
	 * return the MoveEvent details for selected MoveEvent to the edit form
	 * @param : MoveEvent Id
	 * @return : MoveEvent details  
	 */
    def edit() {
        def moveEventInstance = MoveEvent.get( params.id )

        if(!moveEventInstance) {
            flash.message = "MoveEvent not found with id ${params.id}"
            redirect(action:"list")
        } else {
        	def moveBundles = MoveBundle.findAllByProject( moveEventInstance.project )
        	return [ moveEventInstance : moveEventInstance, moveBundles : moveBundles ]
        }
    }
    /*
	 * update the MoveEvent details 
	 * @param : MoveEvent Id
	 * @return : redirect to the show method
	 */
    def update() {
        def moveEventInstance = MoveEvent.get( params.id )
        
		if(moveEventInstance) {
			def estStartTime = params.estStartTime
			if(estStartTime){
				params.estStartTime = TimeUtil.parseDateTime(getSession(), estStartTime)
			}
				
            moveEventInstance.properties = params
			
            def moveBundles = request.getParameterValues("moveBundle")
			
            if(!moveEventInstance.hasErrors() && moveEventInstance.save()) {
            	
            	moveBundleService.assignMoveEvent( moveEventInstance, moveBundles )
				
                flash.message = "MoveEvent '${moveEventInstance.name}' updated"
                redirect(action:"show",id:moveEventInstance.id)
            }
            else {
                render(view:'edit',model:[moveEventInstance:moveEventInstance])
            }
        } else {
            flash.message = "MoveEvent not found with id ${params.id}"
            redirect(action:"edit",id:params.id)
        }
    }
    /*
	 * return blank create page
	 */
    def create() {
        def moveEventInstance = new MoveEvent()
        moveEventInstance.properties = params
        return ['moveEventInstance':moveEventInstance]
    }
    /*
	 * Save the MoveEvent details 
	 * @param : MoveEvent Id
	 * @return : redirect to the show method
	 */
    def save() {
		def estStartTime = params.estStartTime
		if(estStartTime){
			params.estStartTime = TimeUtil.parseDateTime(getSession(), estStartTime)
		}
		
        def moveEventInstance = new MoveEvent(params)
        def moveBundles = request.getParameterValues("moveBundle")
        if(moveEventInstance.project.runbookOn ==1){
            moveEventInstance.calcMethod = MoveEvent.METHOD_MANUAL
        }
		if(!moveEventInstance.hasErrors() && moveEventInstance.save()) {
			
			moveBundleService.assignMoveEvent( moveEventInstance, moveBundles )
            moveBundleService.createManualMoveEventSnapshot( moveEventInstance )
			flash.message = "MoveEvent ${moveEventInstance.name} created"
            redirect(action:"show",id:moveEventInstance.id)
        } else {
            render(view:'create',model:[moveEventInstance:moveEventInstance])
        }
    }
    /*
	 * return the list of MoveBundles which are associated to the selected Project 
	 * @param : projectId
	 * @return : return the list of MoveBundles as JSON object
	 */
    def retrieveMoveBundles() {
    	def projectId = session.CURR_PROJ.CURR_PROJ
		def moveBundles
		def project
		if( projectId ){
			project = Project.get( projectId )
			moveBundles = MoveBundle.findAllByProject( project )
		}
    	render moveBundles as JSON
    }
        
    /*---------------------------------------------------------
     * Will export MoveEvent Transition time results data in XLS based on user input
     * @author : lokanada Reddy
     * @param  : moveEvent and reportType.
     * @return : redirect to same page once data exported to Excel.
     *-------------------------------------------------------*/
	def retrieveMoveResults() {
    	def workbook
		def book
		def moveEvent = params.moveEvent
		def reportType = params.reportType
		if(moveEvent && reportType){
			def moveEventInstance = MoveEvent.get( moveEvent  )
			try {
				def moveEventResults
				File file 
				if(reportType != "SUMMARY"){
					file =  ApplicationHolder.application.parentContext.getResource( "/templates/MoveResults_Detailed.xls" ).getFile()
				} else {
					file =  ApplicationHolder.application.parentContext.getResource( "/templates/MoveResults_Summary.xls" ).getFile()	
				}
				
				//set MIME TYPE as Excel
				response.setContentType( "application/vnd.ms-excel" )
				
				def type = params.reportType == "SUMMARY" ? "summary" : "detailed"
				def filename = 	"MoveResults-${moveEventInstance?.project?.name}-${moveEventInstance?.name}-${type}.xls"
					filename = filename.replace(" ", "_")
				response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
				
				book = new HSSFWorkbook(new FileInputStream( file ));
				def sheet = book.getSheet("moveEvent_results")
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ

				if(reportType != "SUMMARY"){
					moveEventResults = moveBundleService.getMoveEventDetailedResults( moveEvent )
					for ( int r = 1; r <= moveEventResults.size(); r++ ) {
						WorkbookUtil.addCell(sheet, 0, r, String.valueOf(moveEventResults[r-1].move_bundle_id ))
						WorkbookUtil.addCell(sheet, 1, r, String.valueOf(moveEventResults[r-1].bundle_name ))
						WorkbookUtil.addCell(sheet, 2, r, String.valueOf(moveEventResults[r-1].asset_id ))
						WorkbookUtil.addCell(sheet, 3, r, String.valueOf(moveEventResults[r-1].asset_name ))
						WorkbookUtil.addCell(sheet, 4, r, String.valueOf(moveEventResults[r-1].voided ))
						WorkbookUtil.addCell(sheet, 5, r, String.valueOf(moveEventResults[r-1].from_name ))
						WorkbookUtil.addCell(sheet, 6, r, String.valueOf(moveEventResults[r-1].to_name ))
						WorkbookUtil.addCell(sheet, 7, r, String.valueOf( TimeUtil.formatDateTime(getSession(), moveEventResults[r-1].transition_time) ))
						WorkbookUtil.addCell(sheet, 8, r, String.valueOf(moveEventResults[r-1].username ))
						WorkbookUtil.addCell(sheet, 9, r, String.valueOf(moveEventResults[r-1].team_name ))
					}
				} else {
					moveEventResults = moveBundleService.getMoveEventSummaryResults( moveEvent )
					for ( int r = 1; r <= moveEventResults.size(); r++ ) {
						WorkbookUtil.addCell(sheet, 0, r, String.valueOf(moveEventResults[r-1].move_bundle_id ))
						WorkbookUtil.addCell(sheet, 1, r, String.valueOf(moveEventResults[r-1].bundle_name ))
						WorkbookUtil.addCell(sheet, 2, r, String.valueOf(moveEventResults[r-1].state_to ))
						WorkbookUtil.addCell(sheet, 3, r, String.valueOf(moveEventResults[r-1].name ))
						WorkbookUtil.addCell(sheet, 4, r, String.valueOf( TimeUtil.formatDateTime(getSession(), moveEventResults[r-1].started) ))
						WorkbookUtil.addCell(sheet, 5, r, String.valueOf( TimeUtil.formatDateTime(getSession(), moveEventResults[r-1].completed) ))
					}	
				}
				WorkbookUtil.addCell(sheet, 0, moveEventResults.size() + 2, "Note: All times are in $tzId time zone")
				
				book.write(response.getOutputStream())

			} catch( Exception ex ) {
				flash.message = "Exception occurred while exporting data"
				redirect( controller:'reports', action:"retrieveBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
				return;
			}	
		} else {
			flash.message = "Please select MoveEvent and report type. "
			redirect( controller:'reports', action:"retrieveBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
			return;
		}
    }
    
    /*------------------------------------------------------
     * Clear out any snapshot data records and reset any summary steps for given event.
     * @author : Lokanada Reddy
     * @param  : moveEventId
     *----------------------------------------------------*/
	def clearHistoricData() {
		def moveEventId = params.moveEventId
		if(moveEventId ){
			jdbcTemplate.update("DELETE FROM move_event_snapshot WHERE move_event_id = $moveEventId " )
			def moveBundleSteps = jdbcTemplate.queryForList("""SELECT mbs.id FROM move_bundle_step mbs LEFT JOIN move_bundle mb
										ON (mb.move_bundle_id = mbs.move_bundle_id) WHERE mb.move_event_id = $moveEventId """)
			if(moveBundleSteps.size() > 0){
				def ids = (moveBundleSteps.id).toString().replace("[","(").replace("]",")")
				jdbcTemplate.update("DELETE FROM step_snapshot WHERE move_bundle_step_id in $ids " )
			}
			jdbcTemplate.update("""UPDATE move_bundle_step mbs SET mbs.actual_start_time = null, mbs.actual_completion_time = null 
						WHERE move_bundle_id in (SELECT mb.move_bundle_id FROM move_bundle mb WHERE mb.move_event_id =  $moveEventId )""" )
		}
		render "success"
    }
	
	/**
	 * Used to clear or reset any Task data for selected event.
     * @param moveEventId
     * @param type (delete/clear)
	 * @return text
	 * @usage ajax
	 */
	def clearTaskData() {		
		def project = securityService.getUserCurrentProject()
		def moveEvent
		def msg = ""
		
		// TODO - Need to create an ACL instead of using roles for this
		if (!securityService.hasRole(['ADMIN','CLIENT_ADMIN','CLIENT_MGR']) ) {
			msg = "You do not have the proper permissions to perform this task"
		} else {	
			if (params.moveEventId.isNumber()) {
				moveEvent = MoveEvent.findByIdAndProject( params.moveEventId, project)
				if (! moveEvent) {
					msg = "You present do not have access to the event"
				}
			} else {
				msg = "Invalid Event specified"
			}
		}
		if (! msg) {
			try {
				if(params.type == 'reset'){
					msg = taskService.resetTaskData(moveEvent)
				} else if(params.type == 'delete'){
					msg = taskService.deleteTaskData(moveEvent)
				}
			} catch(e) {
				msg = e.getMessage()
			}
		}
		render msg
	}
	
    /**
      * Return the list of active news for a selected moveEvent and status of that evnt.
      * @param id - the moveEvent to get the news for
      */
    def retrieveMoveEventNewsAndStatus() {

		// Make sure that the user is trying to access a valid event
		def (project, user) = controllerService.getProjectAndUserForPage(this)
		if (! project) {
			// TODO - switch to getProjectAndUserForWS when available to avoid the flash.message
			flash.message = ''
			ServiceResults.respondWithWarning(response, "User presently has no selected project")
			return
    	}
    	def moveEvent = controllerService.getEventForPage(this, project, user, params.id)
    	if (!moveEvent) {
			// TODO - switch to getEventForWS when available to avoid the flash.message
			flash.message = ''
			ServiceResults.respondWithWarning(response, "Event id was not found")
			return
		}

		def statusAndNewsList = []
		if (moveEvent) {
			
	    	def moveEventNewsQuery = """SELECT mn.date_created as created, mn.message as message from move_event_news mn 
				left join move_event me on ( me.move_event_id = mn.move_event_id ) 
				left join project p on (p.project_id = me.project_id) 
				where mn.is_archived = 0 and mn.move_event_id = ${moveEvent.id} and p.project_id = ${moveEvent.project.id} order by created desc"""
	    	
			def moveEventNews = jdbcTemplate.queryForList( moveEventNewsQuery )
			
			def news = new StringBuffer()
			
			moveEventNews.each{
	    		news.append(String.valueOf( TimeUtil.formatDateTime(getSession(), it.created) +"&nbsp;:&nbsp;"+it.message+".&nbsp;&nbsp;"))
	    	}
			
			// append recent tasks  whose status is completed, moveEvent is newsBarMode
			def transitionComment = new StringBuffer()
			if(moveEvent.newsBarMode =="on"){
				def today = new Date()
				def currentPoolTime = new java.sql.Timestamp(today.getTime())
				def tasksCompQuery="""SELECT comment,date_resolved AS dateResolved FROM asset_comment WHERE project_id= ${moveEvent.project.id} AND 
					move_event_id=${moveEvent.id} AND status='Completed' AND
					(date_resolved BETWEEN SUBTIME('$currentPoolTime','00:15:00') AND '$currentPoolTime')"""
				def tasksCompList=jdbcTemplate.queryForList( tasksCompQuery )
				tasksCompList.each{
				 	def comment = it.comment
					def dateResolved = it.dateResolved ? TimeUtil.formatDateTime(getSession(), it.dateResolved) : ''
					transitionComment.append(comment+":&nbsp;&nbsp;"+dateResolved+".&nbsp;&nbsp;")
				}
			}
			
			def query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent = ? AND mes.type = ? ORDER BY mes.dateCreated DESC"    					
	    	def moveEventSnapshot = MoveEventSnapshot.findAll( query , [moveEvent , "P"] )[0]
	    	def cssClass = "statusbar_good"
			def status = "GREEN"
			def dialInd = moveEventSnapshot?.dialIndicator
			dialInd = dialInd || dialInd == 0 ? dialInd : 100
			if(dialInd < 25){
				cssClass = "statusbar_bad"
				status = "RED"
			} else if(dialInd >= 25 && dialInd < 50){
				cssClass = "statusbar_yellow"
				status = "YELLOW"
			}
	    	statusAndNewsList << ['news':news.toString()+ "<span style='font-weight:normal'>"+transitionComment.toString()+"</span>", 'cssClass':cssClass, 'status':status]
	
		}
		render statusAndNewsList as JSON
    }
    /*
     * will update the moveEvent calcMethod = M and create a MoveEventSnapshot for summary dialIndicatorValue
     * @author : Lokanada Reddy
     * @param  : moveEventId and moveEvent dialIndicatorValue
     */
    def updateEventSumamry() {
    	def moveEvent = MoveEvent.get( params.moveEventId )
    	def dialIndicator
    	def checkbox = params.checkbox;
    	if(checkbox == "true") {
			dialIndicator = params.value
		}
		if(dialIndicator  || dialIndicator == 0){
			def moveEventSnapshot = new MoveEventSnapshot(moveEvent : moveEvent, planDelta:0, dialIndicator:dialIndicator, type:"P")
	    	if ( ! moveEventSnapshot.save( flush : true ) ) 
	    		log.errlor("Unable to save changes to MoveEventSnapshot: ${moveEventSnapshot}")
			
			moveEvent.calcMethod = MoveEvent.METHOD_MANUAL
		} else {
			moveEvent.calcMethod = MoveEvent.METHOD_LINEAR
		}
    	if ( ! moveEvent.save( flush : true ) ) {
    		log.error("Unable to save changes to MoveEvent: ${moveEvent}")
    	}
		render "success"
     }
	
	/**
	 * The front-end UI to exporting a Runbook spreadsheet
	 */
	def exportRunbook = {
		def projectId =  session.CURR_PROJ.CURR_PROJ		
		def project = securityService.getUserCurrentProject();
		if (!project) {
			flash.message = "Please select project to view Export Runbook"
			redirect(controller:'project',action:'list')
			return
		}
		def moveEventList = MoveEvent.findAllByProject(project)
		
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference("viewUnpublished") == 'true')
		
		return [moveEventList:moveEventList, viewUnpublished:(viewUnpublished ? '1' : '0')]
	}
	
	/**
	 * This provides runbookStats that is rendered into a window of the runbook exporting
	 */
	def runbookStats() {
		def moveEventId = params.id
		def projectId =  session.CURR_PROJ.CURR_PROJ
		def project = Project.get(projectId)
		def moveEventInstance = MoveEvent.get(moveEventId)
		def bundles = moveEventInstance.moveBundles
		def applcationAssigned = 0
		def assetCount = 0
		def databaseCount = 0
		def fileCount = 0
		def otherAssetCount = 0

		if (bundles?.size() > 0) {
			applcationAssigned = Application.countByMoveBundleInListAndProject(bundles,project)
			assetCount = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(bundles,['Application','Database','Files'],params).size()
			databaseCount = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Database',bundles).size()
			fileCount = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Files',bundles).size()
			otherAssetCount = AssetEntity.findAllByAssetTypeNotInListAndMoveBundleInList(['Server','VM','Blade','Application','Files','Database'],bundles).size() 
		}
		
		if (params.containsKey('viewUnpublished')) {
			if (params.viewUnpublished == '1')
				userPreferenceService.setPreference("viewUnpublished", 'true')
			else
				userPreferenceService.setPreference("viewUnpublished", 'false')
		}
		
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference("viewUnpublished") == 'true')
		def publishedValues = [true]
		if (viewUnpublished)
			publishedValues = [true, false]
		
		def preMoveSize = AssetComment.countByMoveEventAndCategoryAndIsPublishedInList(moveEventInstance, 'premove', publishedValues)
		def scheduleSize = AssetComment.countByMoveEventAndCategoryInListAndIsPublishedInList(moveEventInstance, ['shutdown','physical','moveday','startup'], publishedValues)
		def postMoveSize = AssetComment.countByMoveEventAndCategoryAndIsPublishedInList(moveEventInstance, 'postmove', publishedValues)


		return [applcationAssigned:applcationAssigned, assetCount:assetCount, databaseCount:databaseCount, fileCount:fileCount, otherAssetCount:otherAssetCount,
			    preMoveSize: preMoveSize, scheduleSize:scheduleSize, postMoveSize:postMoveSize, bundles:bundles,moveEventInstance:moveEventInstance]

	}
	
	/**
	 * The controller that actually does the runbook export generation to an Excel spreadsheet
	 */
	def exportRunbookToExcel() {

		def (project, userLogin) = controllerService.getProjectAndUserForPage(this)
		if (! project) {
			return
		}
		def moveEvent = controllerService.getEventForPage(this, project, userLogin, params.eventId)
		if (! moveEvent) {
			return
		}

		def currentVersion = moveEvent.runbookVersion

		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def userDTFormat = getSession().getAttribute( TimeUtil.DATE_TIME_FORMAT_ATTR )?.CURR_DT_FORMAT
		if (params.version=='on'){
			if (moveEvent.runbookVersion) {
				moveEvent.runbookVersion = currentVersion + 1
				currentVersion = currentVersion + 1
			} else {
				moveEvent.runbookVersion = 1
				currentVersion = 1
			}
			moveEvent.save(flush:true)
		}

		def bundles = moveEvent.moveBundles
		def today = TimeUtil.formatDateTime(getSession(), new Date(), TimeUtil.FORMAT_DATE_TIME_6)
		def moveEventList = MoveEvent.findAllByProject(project)
		def applcationAssigned = 0
		def assetCount = 0
		def databaseCount = 0
		def fileCount = 0
		def otherAssetCount = 0
		def applications = []
		def assets = []
		def databases = []
		def files = []
		def others = []
		def unresolvedIssues = []
		def preMoveIssue = []
		def postMoveIssue = []
		
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference("viewUnpublished") == 'true')
		def publishedValues = [true]
		if (viewUnpublished)
			publishedValues = [true, false]
		
		if (bundles?.size() > 0) {
			applications = Application.findAllByMoveBundleInListAndProject(bundles,project)
			applcationAssigned = Application.countByMoveBundleInListAndProject(bundles,project)
			assets = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(bundles,['Application','Database','Files'])
			assetCount = assets.size()
			databases = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Database',bundles)
			databaseCount = databases.size()
			files = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Files',bundles)
			fileCount = files.size()
			others = AssetEntity.findAllByAssetTypeNotInListAndMoveBundleInList(['Server','VM','Blade','Application','Files','Database'],bundles)
			otherAssetCount = others.size()
			def allAssets = AssetEntity.findAllByMoveBundleInListAndProject(bundles,project).id
			String asset = allAssets.toString().replace("[","('").replace(",","','").replace("]","')")
			unresolvedIssues = AssetComment.findAll("from AssetComment a where a.assetEntity in ${asset} and a.isResolved = :isResolved and a.commentType = :commentType and a.category in ('general', 'discovery', 'planning','walkthru') AND a.isPublished IN :publishedValues",[isResolved:0, commentType:'issue', publishedValues:publishedValues])

		}
		
		preMoveIssue = AssetComment.findAllByMoveEventAndCategoryAndIsPublishedInList(moveEvent, 'premove', publishedValues) 
		def sheduleIssue = AssetComment.findAllByMoveEventAndCategoryInListAndIsPublishedInList(moveEvent, ['shutdown','physical','moveday','startup'], publishedValues)
		postMoveIssue = AssetComment.findAllByMoveEventAndCategoryAndIsPublishedInList(moveEvent, 'postmove', publishedValues) 

		//TODO - Move controller code into Service .
		def preMoveCheckListError = reportsService.generatePreMoveCheckList(project.id, moveEvent, viewUnpublished).allErrors.size()

		try {
			File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Runbook.xls" ).getFile()
			//set MIME TYPE as Excel
			response.setContentType( "application/vnd.ms-excel" )
			def filename = 	"${project.name} - ${moveEvent.name} Runbook v${currentVersion} -${today}"
			filename = filename.replace(".xls",'')
			response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
			response.setHeader( "Content-Disposition", "attachment; filename=\""+filename+".xls\"" )

			def book = new HSSFWorkbook(new FileInputStream( file ));
			
			def serverSheet = book.getSheet("Servers")
			def personelSheet = book.getSheet("Staff")
			def preMoveSheet = book.getSheet("Pre-move")
			def dbSheet = book.getSheet("Database")
			def filesSheet = book.getSheet("Storage")
			def otherSheet = book.getSheet("Other")
			def issueSheet = book.getSheet("Issues")
			def appSheet = book.getSheet("Applications")
			def postMoveSheet = book.getSheet("Post-move")
			def summarySheet = book.getSheet("Index")
			
			def scheduleSheet = book.getSheet("Schedule")
			
			def preMoveColumnList = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment','assignedTo', 'status','estStart','','', 'notes',
					         				'duration', 'estStart','estFinish','actStart',
					         				'actFinish', 'workflow']
			
			def sheduleColumnList = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'role', 'assignedTo', 'instructionsLink' ,'',
					        				'duration', 'estStart','estFinish', 'actStart','actFinish', 'workflow'
				        				]

			def scheduleColumnList = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'role', 'assignedTo', '',
				{ it.durationInMinutes() }, '', 
				'estStart', 'estFinish', 'actStart', 'actFinish', 'workflow'
			]

			def postMoveColumnList = ['taskNumber', 'assetEntity', 'comment','assignedTo', 'status', 'estFinish', 'dateResolved' , 'notes',
				'taskDependencies','duration','estStart','estFinish','actStart',
				'actFinish','workflow'
			]
						
			def serverColumnList = ['id', 'application', 'assetName', '','serialNumber', 'assetTag', 'manufacturer', 'model', 'assetType', '', '', '']
			
			def appColumnList = ['assetName', 'appVendor', 'appVersion', 'appTech', 'appAccess', 'appSource','license','description',
				'supportType', 'sme', 'sme2', 'businessUnit','','retireDate','maintExpDate','appFunction',
				'environment','criticality','moveBundle', 'planStatus','userCount','userLocations','useFrequency',
				'drRpoDesc','drRtoDesc','moveDowntimeTolerance','validation','latency','testProc','startupProc',
				'url','custom1','custom2','custom3','custom4','custom5','custom6','custom7','custom8'
			]
			
			def impactedAppColumnList =['id' ,'assetName' ,'' ,'startupProc' ,'description' ,'sme' ,'' ,'' ,'' ,'' ,'' ,'' ]
			
			def dbColumnList = ['id', 'assetName', 'dbFormat', 'size', 'description', 'supportType','retireDate', 'maintExpDate',
				'environment','ipAddress', 'planStatus','custom1','custom2', 'custom3', 'custom4','custom5',
				'custom6','custom7','custom8'
			]
			
			def filesColumnList = ['id', 'assetName', 'fileFormat', 'size', 'description', 'supportType','retireDate', 'maintExpDate',
				'environment','ipAddress', 'planStatus','custom1','custom2','custom3','custom4','custom5',
				'custom6','custom7','custom8'
			]
			
			def othersColumnList = ['id','application','assetName', 'shortName', 'serialNumber', 'assetTag', 'manufacturer',
				'model','assetType','ipAddress', 'os', 'sourceLocation', 'sourceLocation','sourceRack',
				'sourceRackPosition','sourceChassis','sourceBladePosition',
				'targetLocation','targetRoom','targetRack', 'targetRackPosition','targetChassis',
				'targetBladePosition','custom1','custom2','custom3','custom4','custom5','custom6','custom7','custom8',
				'moveBundle','truck','cart','shelf','railType','priority','planStatus','usize'
			]
			
			def unresolvedIssueColumnList = ['id', 'comment', 'commentType','commentAssetEntity','resolution','resolvedBy','createdBy',
				'dueDate','assignedTo','category','dateCreated','dateResolved', 'assignedTo','status','taskDependencies','duration','estStart','estFinish','actStart',
				'actFinish','workflow'
			]			
			
			def projManagers = projectService.getProjectManagersByProject(project.id)?.partyIdTo

			def projectNameFont = book.createFont()
			projectNameFont.setFontHeightInPoints((short)14)
			projectNameFont.setFontName("Arial")
			projectNameFont.setBoldweight(Font.BOLDWEIGHT_BOLD)

			def projectNameCellStyle
			projectNameCellStyle = book.createCellStyle()
			projectNameCellStyle.setFont(projectNameFont)
			projectNameCellStyle.setFillBackgroundColor(IndexedColors.SEA_GREEN .getIndex())
			projectNameCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)

			WorkbookUtil.addCell(summarySheet, 1, 1, String.valueOf(project.name ))
			WorkbookUtil.applyStyleToCell(summarySheet, 1, 1, projectNameCellStyle)
			WorkbookUtil.addCell(summarySheet, 2, 3, String.valueOf(project.name ))
			WorkbookUtil.addCell(summarySheet, 2, 6, String.valueOf(projManagers.join(",")))
			WorkbookUtil.addCell(summarySheet, 4, 6, String.valueOf(""))
			WorkbookUtil.addCell(summarySheet, 2, 4, String.valueOf(moveEvent.name ))
			WorkbookUtil.addCell(summarySheet, 2, 10, String.valueOf(moveEvent.name ))
			
			moveBundleService.issueExport(assets, serverColumnList, serverSheet, tzId, userDTFormat, 5, viewUnpublished)
			
			moveBundleService.issueExport(applications, impactedAppColumnList, appSheet, tzId, userDTFormat, 5, viewUnpublished)
			
			moveBundleService.issueExport(databases, dbColumnList, dbSheet, tzId, userDTFormat, 4, viewUnpublished)
			
			moveBundleService.issueExport(files, filesColumnList, filesSheet, tzId, userDTFormat, 1, viewUnpublished)
			
			moveBundleService.issueExport(others, othersColumnList, otherSheet,tzId, userDTFormat, 1, viewUnpublished)
			
			moveBundleService.issueExport(unresolvedIssues, unresolvedIssueColumnList, issueSheet, tzId, userDTFormat, 1, viewUnpublished)
			
			moveBundleService.issueExport(sheduleIssue, sheduleColumnList, scheduleSheet, tzId, userDTFormat, 7, viewUnpublished)
			
			moveBundleService.issueExport(preMoveIssue, preMoveColumnList, preMoveSheet, tzId, userDTFormat, 7, viewUnpublished)
			
			moveBundleService.issueExport(postMoveIssue, postMoveColumnList,  postMoveSheet, tzId, userDTFormat, 7, viewUnpublished)

			// Update the Schedule/Tasks Sheet with the correct start/end times
			Map times = moveEvent.getEventTimes()
			WorkbookUtil.addCell(scheduleSheet, 5, 1, TimeUtil.formatDateTime(getSession(), times.start))
			WorkbookUtil.addCell(scheduleSheet, 5, 3, TimeUtil.formatDateTime(getSession(), times.completion))

			// Update the project staff
			// TODO : JPM 11/2015 : Project staff should get list from ProjectService instead of querying PartyRelationship
			def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = ${project.id} and p.roleTypeCodeFrom = 'PROJECT' ")

			for ( int r = 8; r <= (projectStaff.size()+7); r++ ) {
				def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = ${projectStaff[0].partyIdTo.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
				WorkbookUtil.addCell(personelSheet, 1, r, String.valueOf( projectStaff[r-8].partyIdTo?.toString() ))
				WorkbookUtil.addCell(personelSheet, 2, r, String.valueOf(projectStaff[r-8].roleTypeCodeTo ))
				WorkbookUtil.addCell(personelSheet, 5, r, String.valueOf(projectStaff[r-8].partyIdTo?.email ? projectStaff[r-8].partyIdTo?.email : ''))
			}
			
			book.write(response.getOutputStream())
		} catch( Exception ex ) {
			println "Exception occurred while exporting data: "+ex.printStackTrace()
		  
			return
		}
		return
	
	}
	
	/**
	 * Used to set asset's plan-status to 'Moved' for the specified event
	 * @usage Ajax
	 * @param moveEventId
	 * @return  Count of record affected with this update or Error Message if any
	 * 
	 */
	def markEventAssetAsMoved() {
		def assetAffected
		def errorMsg
		def project = securityService.getUserCurrentProject()
		if(params.containsKey("moveEventId")){
			if(params.moveEventId.isNumber()){
				def moveEvent = MoveEvent.get(params.moveEventId)
				if(moveEvent){
					if (moveEvent.project.id != project.id) {
						log.error "markEventAssetAsMoved: moveEvent.project (${moveEvent.id}) does not match user's current project (${project.id})"
						errorMsg = "An unexpected condition with the event occurred that is preventing an update"
					}else{
						def bundleForEvent = moveEvent.moveBundles
						assetAffected = bundleForEvent ? jdbcTemplate.update("update asset_entity  \
							set plan_status = 'Moved', source_location = target_location, room_source_id = room_target_id ,\
								rack_source_id = rack_target_id, source_rack_position = target_rack_position, \
								source_blade_chassis = target_blade_chassis, source_blade_position = target_blade_position, \
								target_location = null, room_target_id = null, rack_target_id = null, target_rack_position = null,\
								target_blade_chassis = null, target_blade_position = null\
							where move_bundle_id in (SELECT mb.move_bundle_id FROM move_bundle mb WHERE mb.move_event_id =  $moveEvent.id) \
								and plan_status != 'Moved' ") : 0
					}
				} else {
					log.error "markEventAssetAsMoved: Specified moveEvent (${params.moveEventId}) was not found})"
					errorMsg = "An unexpected condition with the event occurred that is preventing an update."
			    }
			}
		}
		render errorMsg ? errorMsg : assetAffected
	}
	
	/**
	 * This method is used to filter newsBarMode property , As we are displaying different label in list so user may serch according to
	 * displayed label but in DB we have different values what we are displaying in label 
	 * e.g. for auto - Auto Start, true - Started ..
	 * @param newsBarMode with what character user filterd newsBarMode property
	 * @return matched db property of newsBarMode
	 */
	def retrieveNewsBMList(newsBarMode){
		def progList = ['Auto Start':'auto', 'Started':'on', 'Stopped':'off']
		def returnList = []
		progList.each{key, value->
			if(StringUtils.containsIgnoreCase(key, newsBarMode))
				returnList <<  value
		}
		return returnList
	}

}
