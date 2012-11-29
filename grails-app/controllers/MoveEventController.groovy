import grails.converters.JSON

import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat

import jxl.*
import jxl.read.biff.*
import jxl.write.*

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil

class MoveEventController {
	
	protected static Log log = LogFactory.getLog( StepSnapshotService.class )
    // Service initialization
	def moveBundleService
	def jdbcTemplate
	def userPreferenceService
	def stepSnapshotService
	def stateEngineService
	def reportsService
	def taskService
	def securityService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	/*
	 * will return the list of MoveEvents
	 */
    def list = {
    	def moveEventInstanceList
        if(!params.max) params.max = 10
        def currProj = session.getAttribute("CURR_PROJ").CURR_PROJ;
    	if(currProj) moveEventInstanceList = MoveEvent.findAllByProject( Project.get( currProj ))
		// Statements for JMESA integration
    	TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = moveEventInstanceList
        [ moveEventInstanceList: moveEventInstanceList, projectId : currProj ]
    }
	/*
	 * return the MoveEvent details for selected MoveEvent
	 * @param : MoveEvent Id
	 * @return : MoveEvent details  
	 */
    def show = {
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
	            redirect(action:list)
	        } else { 
	        	return [ moveEventInstance : moveEventInstance ] 
	        }
		} else {
		    redirect(action:list)
		}
    }
	/*
	 * redirect to list once selected record deleted
	 * @param : MoveEvent Id
	 * @return : list of remaining MoveEvents
	 */
    def delete = {
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
    	redirect(action:list)
    }
    /*
	 * return the MoveEvent details for selected MoveEvent to the edit form
	 * @param : MoveEvent Id
	 * @return : MoveEvent details  
	 */
    def edit = {
        def moveEventInstance = MoveEvent.get( params.id )

        if(!moveEventInstance) {
            flash.message = "MoveEvent not found with id ${params.id}"
            redirect(action:list)
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
    def update = {
        def moveEventInstance = MoveEvent.get( params.id )
        if(moveEventInstance) {
            moveEventInstance.properties = params
            def moveBundles = request.getParameterValues("moveBundle")
			
            if(!moveEventInstance.hasErrors() && moveEventInstance.save()) {
            	
            	moveBundleService.assignMoveEvent( moveEventInstance, moveBundles )
				
                flash.message = "MoveEvent '${moveEventInstance.name}' updated"
                redirect(action:show,id:moveEventInstance.id)
            }
            else {
                render(view:'edit',model:[moveEventInstance:moveEventInstance])
            }
        } else {
            flash.message = "MoveEvent not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }
    /*
	 * return blank create page
	 */
    def create = {
        def moveEventInstance = new MoveEvent()
        moveEventInstance.properties = params
        return ['moveEventInstance':moveEventInstance]
    }
    /*
	 * Save the MoveEvent details 
	 * @param : MoveEvent Id
	 * @return : redirect to the show method
	 */
    def save = {
        def moveEventInstance = new MoveEvent(params)
        def moveBundles = request.getParameterValues("moveBundle")
        if(moveEventInstance.project.runbookOn ==1){
            moveEventInstance.calcMethod = MoveEvent.METHOD_MANUAL
        }
		if(!moveEventInstance.hasErrors() && moveEventInstance.save()) {
			
			moveBundleService.assignMoveEvent( moveEventInstance, moveBundles )
            moveBundleService.createManualMoveEventSnapshot( moveEventInstance )
			flash.message = "MoveEvent ${moveEventInstance.name} created"
            redirect(action:show,id:moveEventInstance.id)
        } else {
            render(view:'create',model:[moveEventInstance:moveEventInstance])
        }
    }
    /*
	 * return the list of MoveBundles which are associated to the selected Project 
	 * @param : projectId
	 * @return : return the list of MoveBundles as JSON object
	 */
    def getMoveBundles = {
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
	def getMoveResults = {
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
				
				WorkbookSettings wbSetting = new WorkbookSettings()
				wbSetting.setUseTemporaryFileDuringWrite(true)
				workbook = Workbook.getWorkbook( file, wbSetting )
				//set MIME TYPE as Excel
				response.setContentType( "application/vnd.ms-excel" )
				
				def type = params.reportType == "SUMMARY" ? "summary" : "detailed"
				def filename = 	"MoveResults-${moveEventInstance?.project?.name}-${moveEventInstance?.name}-${type}.xls"
					filename = filename.replace(" ", "_")
				response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
				
				book = Workbook.createWorkbook( response.getOutputStream(), workbook )
				def sheet = book.getSheet("moveEvent_results")
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
				if(reportType != "SUMMARY"){
					moveEventResults = moveBundleService.getMoveEventDetailedResults( moveEvent )
					for ( int r = 1; r <= moveEventResults.size(); r++ ) {
						sheet.addCell( new Label( 0, r, String.valueOf(moveEventResults[r-1].move_bundle_id )) )
						sheet.addCell( new Label( 1, r, String.valueOf(moveEventResults[r-1].bundle_name )) )
						sheet.addCell( new Label( 2, r, String.valueOf(moveEventResults[r-1].asset_id )) )
						sheet.addCell( new Label( 3, r, String.valueOf(moveEventResults[r-1].asset_name )) )
						sheet.addCell( new Label( 4, r, String.valueOf(moveEventResults[r-1].voided )) )
						sheet.addCell( new Label( 5, r, String.valueOf(moveEventResults[r-1].from_name )) )
						sheet.addCell( new Label( 6, r, String.valueOf(moveEventResults[r-1].to_name )) )
						sheet.addCell( new Label( 7, r, String.valueOf(formatter.format(GormUtil.convertInToUserTZ( moveEventResults[r-1].transition_time, tzId ))) ))
						sheet.addCell( new Label( 8, r, String.valueOf(moveEventResults[r-1].username )) )
						sheet.addCell( new Label( 9, r, String.valueOf(moveEventResults[r-1].team_name )) )
					}
				} else {
					moveEventResults = moveBundleService.getMoveEventSummaryResults( moveEvent )
					for ( int r = 1; r <= moveEventResults.size(); r++ ) {
						sheet.addCell( new Label( 0, r, String.valueOf(moveEventResults[r-1].move_bundle_id )) )
						sheet.addCell( new Label( 1, r, String.valueOf(moveEventResults[r-1].bundle_name )) )
						sheet.addCell( new Label( 2, r, String.valueOf(moveEventResults[r-1].state_to )) )
						sheet.addCell( new Label( 3, r, String.valueOf(moveEventResults[r-1].name )) )
						sheet.addCell( new Label( 4, r, String.valueOf(formatter.format(GormUtil.convertInToUserTZ( moveEventResults[r-1].started, tzId )) )) )
						sheet.addCell( new Label( 5, r, String.valueOf(formatter.format(GormUtil.convertInToUserTZ( moveEventResults[r-1].completed, tzId )) )) )
					}	
				}
				tzId ? tzId : 'EDT'
				sheet.addCell( new Label( 0, moveEventResults.size() + 2, "Note: All times are in $tzId time zone") )
				
				book.write()
				book.close()
		        
			} catch( Exception ex ) {
				flash.message = "Exception occurred while exporting data"
				redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
				return;
			}	
		} else {
			flash.message = "Please select MoveEvent and report type. "
			redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
			return;
		}
    }
    /*---------------------------------------------------------
     * Will export MoveEvent Transition time results data in PDF based on user input
     * @author : lokanada Reddy
     * @param  : moveEvent and reportType.
     * @return : redirect to same page once data exported to PDF.
     *-------------------------------------------------------*/
	def getMoveEventResultsAsPDF = {
			def moveEvent = params.moveEvent
			def reportType = params.reportType
			if(moveEvent && reportType){
				def moveEventInstance = MoveEvent.get( moveEvent  )
				try {
					def moveEventResults
					def reportFields =[]
					def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
					DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
					def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
					def filename = 	"MoveResults-${moveEventInstance?.project?.name}-${moveEventInstance?.name}"
					filename = filename.replace(" ", "_")
					if(reportType != "SUMMARY"){
						moveEventResults = moveBundleService.getMoveEventDetailedResults( moveEvent )
						moveEventResults.each { results->
							reportFields <<["move_bundle_id":results.move_bundle_id, "bundle_name":results.bundle_name, 
											"asset_id":results.asset_id, "team_name":results.team_name,
											"asset_name":results.asset_name, "voided":results.voided, 
											"from_name":results.from_name, "to_name":results.to_name, 
											"transition_time":String.valueOf(formatter.format(GormUtil.convertInToUserTZ( results.transition_time, tzId )) ),
											"username":results.username,"timezone":tzId ? tzId : "EDT",
											"rptTime":String.valueOf(formatter.format( currDate ) )]
						}
						chain(controller:'jasper',action:'index',model:[data:reportFields],
								params:["_format":"PDF","_name":"${filename}-detailed","_file":"moveEventDeailedReport"])
					} else {
						moveEventResults = moveBundleService.getMoveEventSummaryResults( moveEvent )
						moveEventResults.each { results->
							reportFields <<["move_bundle_id":results.move_bundle_id, "bundle_name":results.bundle_name, 
											"state_to":results.state_to, "name":results.name,
											"started":String.valueOf(formatter.format(GormUtil.convertInToUserTZ( results.started, tzId )) ),
											"completed":String.valueOf(formatter.format(GormUtil.convertInToUserTZ( results.completed, tzId )) ),
											"timezone":tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
						}
						chain(controller:'jasper',action:'index',model:[data:reportFields],
								params:["_format":"PDF","_name":"${filename}-summary","_file":"moveEventSummaryReport"])
					}
			            
				} catch( Exception ex ) {
					flash.message = "Exception occurred while exporting data"+ex
					redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
					return;
				}	
			} else {
				flash.message = "Please select MoveEvent and report type. "
				redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
				return;
			}
        }
    /*------------------------------------------------------
     * Clear out any snapshot data records and reset any summary steps for given move event.
     * @author : Lokanada Reddy
     * @param  : moveEventId
     *----------------------------------------------------*/
	def clearHistoricData = {
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
	 * Used to clear or reset any Task data for selected move event.
     * @param  : moveEventId 
     * @param : type (delete/clear)
	 */
	def clearTaskData = {
		def moveEventId = params.moveEventId
		if(moveEventId ){
			if(params.type == 'clear'){
				taskService.cleanTaskData(moveEventId)
			} else if(params.type == 'delete'){
				taskService.deleteTaskData(moveEventId)
			}
		}
		render "success"
	}
	
    /*------------------------------------------------
     * Return the list of active news for a selected moveEvent and status of that evnt.
     * @author : Lokanada Reddy
     * @param  : id (moveEvent)
     *----------------------------------------------*/
    def getMoveEventNewsAndStatus = {
    	
    	def moveEvent = MoveEvent.findById(params.id)
		def statusAndNewsList = []
		if(moveEvent){
			def holdId = Integer.parseInt(stateEngineService.getStateId(moveEvent.project.workflowCode,"Hold"))
			
	    	def moveEventNewsQuery = """SELECT mn.date_created as created, mn.message as message from move_event_news mn 
							left join move_event me on ( me.move_event_id = mn.move_event_id ) 
							left join project p on (p.project_id = me.project_id) 
	    					where mn.is_archived = 0 and mn.move_event_id = ${moveEvent.id} and p.project_id = ${moveEvent.project.id} order by created desc"""
	    	
			def moveEventNews = jdbcTemplate.queryForList( moveEventNewsQuery )
			
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			DateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
			def news = new StringBuffer()
			
			moveEventNews.each{
	    		news.append(String.valueOf(formatter.format(GormUtil.convertInToUserTZ( it.created, tzId ))) +"&nbsp;:&nbsp;"+it.message+".&nbsp;&nbsp;")	
	    	}
			
			// append recent asset transitions if moveEvent is ininProgress and project trackChanges is yes.
			def transitionComment = new StringBuffer()
			if(moveEvent.inProgress =="true" && moveEvent.project.trackChanges == "Y"){
				def today = GormUtil.convertInToGMT( "now", tzId );
				def currentPoolTime = new java.sql.Timestamp(today.getTime())
				def recentAssetTransitions = """select p.current_state_id as stateTo, p.asset_id as assetId, p.last_modified as dateModified, p.created_date as dateCreated from project_asset_map p
												left join asset_entity ae on ae.asset_entity_id = p.asset_id
												left join move_bundle mb on mb.move_bundle_id = ae.move_bundle_id
												where mb.move_event_id =  ${moveEvent.id} and
												(p.created_date between SUBTIME('$currentPoolTime','00:15:00') and '$currentPoolTime' OR 
												p.last_modified between SUBTIME('$currentPoolTime','00:15:00') and '$currentPoolTime')"""
				def transitionResultList = jdbcTemplate.queryForList( recentAssetTransitions )
				transitionResultList.each{
					def currentTransition = AssetTransition.findAll("FROM AssetTransition at WHERE at.assetEntity = ${it.assetId} AND at.stateTo = '${it.stateTo}' AND at.voided = 0 ORDER BY at.dateCreated")
					if(currentTransition.size() > 0){
						def asset = currentTransition[0].assetEntity
						def message = asset.assetTag+"-"+asset.assetName +" marked "+stateEngineService.getStateLabel(moveEvent.project.workflowCode,it.stateTo)+" by "+currentTransition[0]?.userLogin?.person?.firstName
						def date = String.valueOf( formatter.format(it.dateModified ? GormUtil.convertInToUserTZ( it.dateModified, tzId ) : GormUtil.convertInToUserTZ( it.dateCreated, tzId ) ) )
						transitionComment.append(date.substring(6,14) +"&nbsp;:&nbsp;"+	message+".&nbsp;&nbsp;")
					}
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

	    	statusAndNewsList << ['news':news.toString() + "<span style='font-weight:normal'>"+transitionComment.toString()+"</span>", 'cssClass':cssClass, 'status':status]
	
		}
		render statusAndNewsList as JSON
    }
    /*
     * will update the moveEvent calcMethod = M and create a MoveEventSnapshot for summary dialIndicatorValue
     * @author : Lokanada Reddy
     * @param  : moveEventId and moveEvent dialIndicatorValue
     */
    def updateEventSumamry = {
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
    	} else {
    		def timeNow = GormUtil.convertInToGMT( "now", "EDT" ).getTime()
			stepSnapshotService.processSummary( moveEvent.id , timeNow)
    	}
		render "success"
     }
	def getMoveEventResultsAsWEB = {
		def moveEvent = params.moveEvent
		def reportType = params.reportType
		if(moveEvent && reportType){
			def moveEventInstance = MoveEvent.get( moveEvent  )
			try {
				def moveEventResults
				def reportFields =[]
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
				def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
				if(reportType != "SUMMARY"){
					moveEventResults = moveBundleService.getMoveEventDetailedResults( moveEvent )
					moveEventResults.each { results->
						reportFields <<["move_bundle_id":results.move_bundle_id, "bundle_name":results.bundle_name,
										"asset_id":results.asset_id, "team_name":results.team_name,
										"asset_name":results.asset_name, "voided":results.voided,
										"from_name":results.from_name, "to_name":results.to_name,
										"transition_time":String.valueOf(formatter.format(GormUtil.convertInToUserTZ( results.transition_time, tzId )) ),
										"username":results.username,"timezone":tzId ? tzId : "EDT",
										"rptTime":String.valueOf(formatter.format( currDate ) )]
					}
					render(view:"moveResultsWeb",model:[moveEventResults:reportFields])
				} else {
					moveEventResults = moveBundleService.getMoveEventSummaryResults( moveEvent )
					moveEventResults.each { results->
						reportFields <<["move_bundle_id":results.move_bundle_id, "bundle_name":results.bundle_name,
										"state_to":results.state_to, "name":results.name,
										"started":String.valueOf(formatter.format(GormUtil.convertInToUserTZ( results.started, tzId )) ),
										"completed":String.valueOf(formatter.format(GormUtil.convertInToUserTZ( results.completed, tzId )) ),
										"timezone":tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
					}
					render(view:"moveResultsWeb",model:[moveEventResults:reportFields, summary:'summary'])
				}
					
			} catch( Exception ex ) {
				flash.message = "Exception occurred while exporting data"+ex
				redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
				return;
			}
		} else {
			flash.message = "Please select MoveEvent and report type. "
			redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
			return;
		}
	}
	
	def exportRunbook ={
		def projectId =  session.CURR_PROJ.CURR_PROJ		
		def project = Project.get(projectId)
		def moveEventList = MoveEvent.findAllByProject(project)
		
		return [moveEventList:moveEventList]
	}
	
	def runBook = {
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
		if(bundles?.size()>0){
			 applcationAssigned = Application.countByMoveBundleInListAndProject(bundles,project)
			 assetCount = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(bundles,['Application','Database','Files'],params).size()
			 databaseCount=AssetEntity.findAllByAssetTypeAndMoveBundleInList('Database',bundles).size()
			 fileCount=AssetEntity.findAllByAssetTypeAndMoveBundleInList('Files',bundles).size()
			 otherAssetCount=AssetEntity.findAllByAssetTypeNotInListAndMoveBundleInList(['Server','VM','Blade','Application','Files','Database'],bundles).size()
		}
		return [applcationAssigned:applcationAssigned, assetCount:assetCount, databaseCount:databaseCount, fileCount:fileCount, otherAssetCount:otherAssetCount,
			       reportService: reportsService.generatePreMoveCheckList(projectId,moveEventInstance).allErrors,bundles:bundles,moveEventInstance:moveEventInstance]
	}
	
	def generateRunbook={
			def projectId =  session.CURR_PROJ.CURR_PROJ
			def project = Project.get(projectId)
			def moveEventInstance = MoveEvent.get(params.eventId)
			def currentVersion = moveEventInstance.runbookVersion
			if(params.version=='on'){
				if(moveEventInstance.runbookVersion){
					moveEventInstance.runbookVersion = currentVersion + 1
					currentVersion = currentVersion + 1
				}else{
					moveEventInstance.runbookVersion = 1
					currentVersion = 1
				}
				moveEventInstance.save(flush:true)
			}
			def bundles = moveEventInstance.moveBundles
			def today = new Date()
			def formatter = new SimpleDateFormat("yy/MM/dd");
			today = formatter.format(today)
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
			if(bundles?.size()>0){
				 applications = Application.findAllByMoveBundleInListAndProject(bundles,project)
				 applcationAssigned = Application.countByMoveBundleInListAndProject(bundles,project)
				 assets = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(bundles,['Application','Database','Files'])
				 assetCount = assets.size()
				 databases = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Database',bundles)
				 databaseCount = databases.size()
				 files = AssetEntity.findAllByAssetTypeAndMoveBundleInList('Files',bundles)
				 fileCount=files.size()
				 others = AssetEntity.findAllByAssetTypeNotInListAndMoveBundleInList(['Server','VM','Blade','Application','Files','Database'],bundles)
				 otherAssetCount=others.size()
				 def allAssets = AssetEntity.findAllByMoveBundleInListAndProject(bundles,project).id
				 String asset = allAssets.toString().replace("[","('").replace(",","','").replace("]","')") 
				 unresolvedIssues = AssetComment.findAll("from AssetComment a where a.assetEntity in ${asset} and a.isResolved = ? and a.commentType = ? and a.category in ('general', 'discovery', 'planning','walkthru')",[0,'issue'])
				 preMoveIssue = AssetComment.findAll("from AssetComment a where a.assetEntity in ${asset}  and a.commentType = ? and a.category = ? ",['issue','premove'])
				 postMoveIssue = AssetComment.findAll("from AssetComment a where a.assetEntity in ${asset}  and a.commentType = ? and a.category = ? ",['issue','postmove'])
			}
			
			//TODO - Move controller code into Service .
			def preMoveCheckListError = reportsService.generatePreMoveCheckList(projectId,moveEventInstance).allErrors.size()
			try {
						File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Runbook.xls" ).getFile()
						WorkbookSettings wbSetting = new WorkbookSettings()
						wbSetting.setUseTemporaryFileDuringWrite(true)
						def workbook = Workbook.getWorkbook( file, wbSetting )
						//set MIME TYPE as Excel
						response.setContentType( "application/vnd.ms-excel" )
						def filename = 	"${project.name} - ${moveEventInstance.name} Runbook v${currentVersion} - ${today}"
						filename = filename.replace(".xls",'')
						response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
						response.setHeader( "Content-Disposition", "attachment; filename=\""+filename+".xls\"" )
						def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
						
						def serverSheet = book.getSheet("Servers")
						def personelSheet = book.getSheet("Personnel")
						def preMoveSheet = book.getSheet("Pre-move")
						def dbSheet = book.getSheet("Database")
						def filesSheet = book.getSheet("Storage")
						def otherSheet = book.getSheet("Other")
						def issueSheet = book.getSheet("Issues")
						def appSheet = book.getSheet("Impacted Applications")
						def postMoveSheet = book.getSheet("Post-move")
						def summarySheet = book.getSheet("Index")
						
						
						  
							  summarySheet.addCell( new Label( 1, 1, String.valueOf(project.name )) )
							  summarySheet.addCell( new Label( 2, 3, String.valueOf(project.name )) )
							  summarySheet.addCell( new Label( 2, 4, String.valueOf(moveEventInstance.name )) )
							  summarySheet.addCell( new Label( 2, 10, String.valueOf(moveEventInstance.name )) )
						
						  for ( int r = 5; r <= (assets.size()+4); r++ ) {
							  serverSheet.addCell( new Label( 0, r, String.valueOf(assets[r-5].id)) )
							  serverSheet.addCell( new Label( 1, r, String.valueOf(assets[r-5].application )) )
							  serverSheet.addCell( new Label( 2, r, String.valueOf(assets[r-5].assetName )) )
							  serverSheet.addCell( new Label( 3, r, String.valueOf('')) )
							  serverSheet.addCell( new Label( 5, r, String.valueOf(assets[r-5].assetTag )) )
							  serverSheet.addCell( new Label( 6, r, String.valueOf(assets[r-5].manufacturer ? assets[r-5].manufacturer : '')) )
							  serverSheet.addCell( new Label( 7, r, String.valueOf(assets[r-5].model ? assets[r-5].model : '')) )
							  serverSheet.addCell( new Label( 8, r, String.valueOf(assets[r-5].assetType )) )
							  serverSheet.addCell( new Label( 9, r, String.valueOf('')) )
							  serverSheet.addCell( new Label(10, r, String.valueOf('')) )
							  serverSheet.addCell( new Label(11, r, String.valueOf('')) )
							  
						}
						  
						  def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' ")
						  for ( int r = 8; r <= (projectStaff.size()+7); r++ ) {
							  def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = ${projectStaff[0].partyIdTo.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
							  personelSheet.addCell( new Label( 1, r, String.valueOf( projectStaff[r-8].partyIdTo.firstName+" "+ projectStaff[r-8].partyIdTo.lastName)) )
							  personelSheet.addCell( new Label( 2, r, String.valueOf(projectStaff[r-8].roleTypeCodeTo )) )
							  personelSheet.addCell( new Label( 5, r, String.valueOf(projectStaff[r-8].partyIdTo.email ? projectStaff[r-8].partyIdTo.email : '')) )
						  }
						  
						  for ( int r = 5; r <= (applications.size()+4); r++ ) {
							  appSheet.addCell( new Label( 0, r, String.valueOf(r-4)) )
							  appSheet.addCell( new Label( 1, r, String.valueOf(applications[r-5].assetName ? applications[r-5].assetName : '')) )
							  /*appSheet.addCell( new Label( 2, r, String.valueOf(applications[r-1].appVendor ? applications[r-1].appVendor : '')) )
							  appSheet.addCell( new Label( 3, r, String.valueOf(applications[r-1].appVersion ? applications[r-1].appVersion :'')) )
							  appSheet.addCell( new Label( 4, r, String.valueOf(applications[r-1].appTech ? applications[r-1].appTech : '')) )
							  appSheet.addCell( new Label( 5, r, String.valueOf(applications[r-1].appAccess ? applications[r-1].appAccess : '' )) )
							  appSheet.addCell( new Label( 6, r, String.valueOf(applications[r-1].appSource ? applications[r-1].appSource : '')) )
							  appSheet.addCell( new Label( 7, r, String.valueOf(applications[r-1].license ? applications[r-1].license : '')) )
							  appSheet.addCell( new Label( 8, r, String.valueOf(applications[r-1].description ? applications[r-1].description : '' )) )
							  appSheet.addCell( new Label( 9, r, String.valueOf(applications[r-1].supportType  ? applications[r-1].supportType : '')) )
							  appSheet.addCell( new Label(10, r, String.valueOf(applications[r-1].sme ? applications[r-1].sme : '')) )
							  appSheet.addCell( new Label(11, r, String.valueOf(applications[r-1].sme2 ? applications[r-1].sme2 : '')) )
							  appSheet.addCell( new Label(12, r, String.valueOf(applications[r-1].businessUnit ? applications[r-1].businessUnit : '')) )
							  appSheet.addCell( new Label(13, r, String.valueOf(applications[r-1].assignedTo  ? applications[r-1].assignedTo  : '' )) )
							  appSheet.addCell( new Label(14, r, String.valueOf(applications[r-1].retireDate ? applications[r-1].retireDate : '')) )
							  appSheet.addCell( new Label(15, r, String.valueOf(applications[r-1].maintExpDate ? applications[r-1].maintExpDate : '')) )
							  appSheet.addCell( new Label(16, r, String.valueOf(applications[r-1].appFunction ? applications[r-1].appFunction : '')) )
							  appSheet.addCell( new Label(17, r, String.valueOf(applications[r-1].environment ? applications[r-1].environment : '' )) )
							  appSheet.addCell( new Label(18, r, String.valueOf(applications[r-1].criticality ? applications[r-1].criticality : '')) )
							  appSheet.addCell( new Label(19, r, String.valueOf(applications[r-1].moveBundle ? applications[r-1].moveBundle : '' )) )
							  appSheet.addCell( new Label(20, r, String.valueOf(applications[r-1].planStatus ? applications[r-1].planStatus : '')) )
							  appSheet.addCell( new Label(21, r, String.valueOf(applications[r-1].userCount ? applications[r-1].userCount : '')) )
							  appSheet.addCell( new Label(22, r, String.valueOf(applications[r-1].userLocations ? applications[r-1].userLocations : '')) )
							  appSheet.addCell( new Label(23, r, String.valueOf(applications[r-1].useFrequency ? applications[r-1].useFrequency : '')) )
							  appSheet.addCell( new Label(24, r, String.valueOf(applications[r-1].drRpoDesc ? applications[r-1].drRpoDesc : '')) )
							  appSheet.addCell( new Label(25, r, String.valueOf(applications[r-1].drRtoDesc ? applications[r-1].drRtoDesc  : '')) )
							  appSheet.addCell( new Label(26, r, String.valueOf(applications[r-1].moveDowntimeTolerance ? applications[r-1].moveDowntimeTolerance : '' )) )
							  appSheet.addCell( new Label(27, r, String.valueOf(applications[r-1].validation ? applications[r-1].validation : '')) )
							  appSheet.addCell( new Label(28, r, String.valueOf(applications[r-1].latency ? applications[r-1].latency : '')) )
							  appSheet.addCell( new Label(29, r, String.valueOf(applications[r-1].testProc ? applications[r-1].testProc : '' )) )
							  appSheet.addCell( new Label(30, r, String.valueOf(applications[r-1].startupProc ? applications[r-1].startupProc : '')) )
							  appSheet.addCell( new Label(31, r, String.valueOf(applications[r-1].url ? applications[r-1].url : '')) )
							  appSheet.addCell( new Label(32, r, String.valueOf(applications[r-1].custom1 ? applications[r-1].custom1 : '' )) )
							  appSheet.addCell( new Label(33, r, String.valueOf(applications[r-1].custom2 ? applications[r-1].custom2 : '' )) )
							  appSheet.addCell( new Label(34, r, String.valueOf(applications[r-1].custom3 ? applications[r-1].custom3 : '')) )
							  appSheet.addCell( new Label(35, r, String.valueOf(applications[r-1].custom4 ? applications[r-1].custom4 : '')) )
							  appSheet.addCell( new Label(36, r, String.valueOf(applications[r-1].custom5 ? applications[r-1].custom5 : '')) )
							  appSheet.addCell( new Label(37, r, String.valueOf(applications[r-1].custom6 ? applications[r-1].custom6 : '')) )
							  appSheet.addCell( new Label(38, r, String.valueOf(applications[r-1].custom7 ? applications[r-1].custom7 : '')) )
							  appSheet.addCell( new Label(39, r, String.valueOf(applications[r-1].custom8 ? applications[r-1].custom8 : '')) )*/
							  
						}
						  
						  for ( int r = 4; r <= (databases.size()+3); r++ ) {
							  dbSheet.addCell( new Label( 0, r, String.valueOf(databases[r-4].id)) )
							  dbSheet.addCell( new Label( 1, r, String.valueOf(databases[r-4].assetName ? databases[r-4].assetName : '' )) )
							  dbSheet.addCell( new Label( 2, r, String.valueOf(databases[r-4].dbFormat ? databases[r-4].dbFormat : '')) )
							  dbSheet.addCell( new Label( 3, r, String.valueOf(databases[r-4].dbSize ? databases[r-4].dbSize : '' )) )
							  dbSheet.addCell( new Label( 4, r, String.valueOf(databases[r-4].description ? databases[r-4].description : '')) )
							  dbSheet.addCell( new Label( 5, r, String.valueOf(databases[r-4].supportType ? databases[r-4].supportType  :'' )) )
							  dbSheet.addCell( new Label( 6, r, String.valueOf(databases[r-4].retireDate ? databases[r-4].retireDate : '')) )
							  dbSheet.addCell( new Label( 7, r, String.valueOf(databases[r-4].maintExpDate ? databases[r-4].maintExpDate : '')) )
							  dbSheet.addCell( new Label( 8, r, String.valueOf(databases[r-4].environment ? databases[r-4].environment : '' )) )
							  dbSheet.addCell( new Label( 9, r, String.valueOf(databases[r-4].ipAddress ? databases[r-4].ipAddress : '' )) )
							  dbSheet.addCell( new Label(10, r, String.valueOf(databases[r-4].planStatus ? databases[r-4].planStatus : '')) )
							  dbSheet.addCell( new Label(11, r, String.valueOf(databases[r-4].custom1 ? databases[r-4].custom1 : '')) )
							  dbSheet.addCell( new Label(12, r, String.valueOf(databases[r-4].custom2 ? databases[r-4].custom2 : '')) )
							  dbSheet.addCell( new Label(13, r, String.valueOf(databases[r-4].custom3 ? databases[r-4].custom3 : '')) )
							  dbSheet.addCell( new Label(14, r, String.valueOf(databases[r-4].custom4 ? databases[r-4].custom4 : '')) )
							  dbSheet.addCell( new Label(15, r, String.valueOf(databases[r-4].custom5 ? databases[r-4].custom5 : '')) )
							  dbSheet.addCell( new Label(16, r, String.valueOf(databases[r-4].custom6 ? databases[r-4].custom6 : '' )) )
							  dbSheet.addCell( new Label(17, r, String.valueOf(databases[r-4].custom7 ? databases[r-4].custom7 : '')) )
							  dbSheet.addCell( new Label(18, r, String.valueOf(databases[r-4].custom8 ? databases[r-4].custom8 : '')) )
						}
						  
						  for ( int r = 1; r <= files.size(); r++ ) {
							  filesSheet.addCell( new Label( 0, r, String.valueOf(files[r-1].id)) )
							  filesSheet.addCell( new Label( 1, r, String.valueOf(files[r-1].assetName ? files[r-1].assetName : '' )) )
							  filesSheet.addCell( new Label( 2, r, String.valueOf(files[r-1].fileFormat ? files[r-1].fileFormat : '')) )
							  filesSheet.addCell( new Label( 3, r, String.valueOf(files[r-1].fileSize ? files[r-1].fileSize : '' )) )
							  filesSheet.addCell( new Label( 4, r, String.valueOf(files[r-1].description ? files[r-1].description : '')) )
							  filesSheet.addCell( new Label( 5, r, String.valueOf(files[r-1].supportType ? files[r-1].supportType  :'' )) )
							  filesSheet.addCell( new Label( 6, r, String.valueOf(files[r-1].retireDate ? files[r-1].retireDate : '')) )
							  filesSheet.addCell( new Label( 7, r, String.valueOf(files[r-1].maintExpDate ? files[r-1].maintExpDate : '')) )
							  filesSheet.addCell( new Label( 8, r, String.valueOf(files[r-1].environment ? files[r-1].environment : '' )) )
							  filesSheet.addCell( new Label( 9, r, String.valueOf(files[r-1].ipAddress ? files[r-1].ipAddress : '' )) )
							  filesSheet.addCell( new Label(10, r, String.valueOf(files[r-1].planStatus ? files[r-1].planStatus : '')) )
							  filesSheet.addCell( new Label(11, r, String.valueOf(files[r-1].custom1 ? files[r-1].custom1 : '')) )
							  filesSheet.addCell( new Label(12, r, String.valueOf(files[r-1].custom2 ? files[r-1].custom2 : '')) )
							  filesSheet.addCell( new Label(13, r, String.valueOf(files[r-1].custom3 ? files[r-1].custom3 : '')) )
							  filesSheet.addCell( new Label(14, r, String.valueOf(files[r-1].custom4 ? files[r-1].custom4 : '')) )
							  filesSheet.addCell( new Label(15, r, String.valueOf(files[r-1].custom5 ? files[r-1].custom5 : '')) )
							  filesSheet.addCell( new Label(16, r, String.valueOf(files[r-1].custom6 ? files[r-1].custom6 : '' )) )
							  filesSheet.addCell( new Label(17, r, String.valueOf(files[r-1].custom7 ? files[r-1].custom7 : '')) )
							  filesSheet.addCell( new Label(18, r, String.valueOf(files[r-1].custom8 ? files[r-1].custom8 : '')) )
						}
						  
						  for ( int r = 1; r <= others.size(); r++ ) {
							  otherSheet.addCell( new Label( 0, r, String.valueOf(others[r-1].id)) )
							  otherSheet.addCell( new Label( 1, r, String.valueOf(others[r-1].application )) )
							  otherSheet.addCell( new Label( 2, r, String.valueOf(others[r-1].assetName)) )
							  otherSheet.addCell( new Label( 3, r, String.valueOf(others[r-1].shortName ? others[r-1].shortName : '' )) )
							  otherSheet.addCell( new Label( 4, r, String.valueOf(others[r-1].serialNumber ? others[r-1].serialNumber : '')) )
							  otherSheet.addCell( new Label( 5, r, String.valueOf(others[r-1].assetTag )) )
							  otherSheet.addCell( new Label( 6, r, String.valueOf(others[r-1].manufacturer ? others[r-1].manufacturer : '')) )
							  otherSheet.addCell( new Label( 7, r, String.valueOf(others[r-1].model ? others[r-1].model : '')) )
							  otherSheet.addCell( new Label( 8, r, String.valueOf(others[r-1].assetType )) )
							  otherSheet.addCell( new Label( 9, r, String.valueOf(others[r-1].ipAddress ? others[r-1].ipAddress : '' )) )
							  otherSheet.addCell( new Label(10, r, String.valueOf(others[r-1].os ? others[r-1].os : '')) )
							  otherSheet.addCell( new Label(11, r, String.valueOf(others[r-1].sourceLocation ? others[r-1].sourceLocation : '')) )
							  otherSheet.addCell( new Label(12, r, String.valueOf(others[r-1].sourceRoom ? others[r-1].sourceRoom : '')) )
							  otherSheet.addCell( new Label(13, r, String.valueOf(others[r-1].sourceRack ? others[r-1].sourceRack : '')) )
							  otherSheet.addCell( new Label(14, r, String.valueOf(others[r-1].sourceRackPosition ? others[r-1].sourceRackPosition : '')) )
							  otherSheet.addCell( new Label(15, r, String.valueOf(others[r-1].sourceBladeChassis ? others[r-1].sourceBladeChassis : '')) )
							  otherSheet.addCell( new Label(16, r, String.valueOf(others[r-1].sourceBladePosition ? others[r-1].sourceBladePosition : '' )) )
							  otherSheet.addCell( new Label(17, r, String.valueOf(others[r-1].targetLocation ? others[r-1].targetLocation : '')) )
							  otherSheet.addCell( new Label(18, r, String.valueOf(others[r-1].targetRoom ? others[r-1].targetRoom : '')) )
							  otherSheet.addCell( new Label(19, r, String.valueOf(others[r-1].targetRack ? others[r-1].targetRack : '')) )
							  otherSheet.addCell( new Label(20, r, String.valueOf(others[r-1].targetRackPosition ? others[r-1].targetRackPosition : '' )) )
							  otherSheet.addCell( new Label(21, r, String.valueOf(others[r-1].targetBladeChassis ? others[r-1].targetBladeChassis : '')) )
							  otherSheet.addCell( new Label(22, r, String.valueOf(others[r-1].targetBladePosition ? others[r-1].targetBladePosition : '')) )
							  otherSheet.addCell( new Label(23, r, String.valueOf(others[r-1].custom1 ? others[r-1].custom1 :'' )) )
							  otherSheet.addCell( new Label(24, r, String.valueOf(others[r-1].custom2 ? others[r-1].custom2 : '')) )
							  otherSheet.addCell( new Label(25, r, String.valueOf(others[r-1].custom3 ? others[r-1].custom3 : '' )) )
							  otherSheet.addCell( new Label(26, r, String.valueOf(others[r-1].custom4 ? others[r-1].custom4 : '')) )
							  otherSheet.addCell( new Label(27, r, String.valueOf(others[r-1].custom5 ? others[r-1].custom5 : '')) )
							  otherSheet.addCell( new Label(28, r, String.valueOf(others[r-1].custom6 ? others[r-1].custom6 : '')) )
							  otherSheet.addCell( new Label(29, r, String.valueOf(others[r-1].custom7 ? others[r-1].custom7 : '' )) )
							  otherSheet.addCell( new Label(30, r, String.valueOf(others[r-1].custom8 ? others[r-1].custom8 : '')) )
							  otherSheet.addCell( new Label(31, r, String.valueOf(others[r-1].moveBundle ? others[r-1].moveBundle : '')) )
							  otherSheet.addCell( new Label(32, r, String.valueOf(others[r-1].sourceTeamMt ? others[r-1].sourceTeamMt : '' )) )
							  otherSheet.addCell( new Label(33, r, String.valueOf(others[r-1].targetTeamMt ? others[r-1].targetTeamMt : '')) )
							  otherSheet.addCell( new Label(34, r, String.valueOf(others[r-1].sourceTeamLog ? others[r-1].sourceTeamLog : '')) )
							  otherSheet.addCell( new Label(35, r, String.valueOf(others[r-1].targetTeamLog ? others[r-1].targetTeamLog :'')) )
							  otherSheet.addCell( new Label(36, r, String.valueOf(others[r-1].sourceTeamSa ? others[r-1].sourceTeamSa : '')) )
							  otherSheet.addCell( new Label(37, r, String.valueOf(others[r-1].targetTeamSa ? others[r-1].targetTeamSa : '')) )
							  otherSheet.addCell( new Label(38, r, String.valueOf(others[r-1].sourceTeamDba ? others[r-1].sourceTeamDba : '')) )
							  otherSheet.addCell( new Label(39, r, String.valueOf(others[r-1].targetTeamDba ? others[r-1].targetTeamDba : '' )) )
							  otherSheet.addCell( new Label(40, r, String.valueOf(others[r-1].truck ? others[r-1].truck :'')) )
							  otherSheet.addCell( new Label(41, r, String.valueOf(others[r-1].cart ? others[r-1].cart :'' )) )
							  otherSheet.addCell( new Label(42, r, String.valueOf(others[r-1].shelf ? others[r-1].shelf : '' )) )
							  otherSheet.addCell( new Label(43, r, String.valueOf(others[r-1].railType ? others[r-1].railType :'' )) )
							  otherSheet.addCell( new Label(44, r, String.valueOf(others[r-1].appOwner ? others[r-1].appOwner : '')) )
							  otherSheet.addCell( new Label(45, r, String.valueOf(others[r-1].appSme ? others[r-1].appSme : '')) )
							  otherSheet.addCell( new Label(46, r, String.valueOf(others[r-1].priority ? others[r-1].priority : '')) )
							  otherSheet.addCell( new Label(47, r, String.valueOf(others[r-1].planStatus ? others[r-1].planStatus : '')) )
							  otherSheet.addCell( new Label(48, r, String.valueOf(others[r-1].usize ? others[r-1].usize : '')) )
							  
						}
						  
						  for ( int r = 1; r <= unresolvedIssues.size(); r++ ) {
							  issueSheet.addCell( new Label( 0, r, String.valueOf(unresolvedIssues[r-1].id)) )
							  issueSheet.addCell( new Label( 1, r, String.valueOf(unresolvedIssues[r-1].comment ? unresolvedIssues[r-1].comment : '' )) )
							  issueSheet.addCell( new Label( 2, r, String.valueOf(unresolvedIssues[r-1].commentType ? unresolvedIssues[r-1].commentType : '')) )
							  issueSheet.addCell( new Label( 3, r, String.valueOf(unresolvedIssues[r-1].assetEntity?.assetName ? unresolvedIssues[r-1].assetEntity?.assetName : '' )) )
							  issueSheet.addCell( new Label( 4, r, String.valueOf(unresolvedIssues[r-1].resolution ? unresolvedIssues[r-1].resolution  :'' )) )
							  issueSheet.addCell( new Label( 5, r, String.valueOf(unresolvedIssues[r-1].resolvedBy ? unresolvedIssues[r-1].resolvedBy : '')) )
							  issueSheet.addCell( new Label( 6, r, String.valueOf(unresolvedIssues[r-1].createdBy ? unresolvedIssues[r-1].createdBy : '')) )
							  issueSheet.addCell( new Label( 7, r, String.valueOf(unresolvedIssues[r-1].dueDate ? unresolvedIssues[r-1].dueDate : '')) )
							  issueSheet.addCell( new Label( 8, r, String.valueOf(unresolvedIssues[r-1].assignedTo ? unresolvedIssues[r-1].assignedTo : '' )) )
							  issueSheet.addCell( new Label( 9, r, String.valueOf(unresolvedIssues[r-1].category ? unresolvedIssues[r-1].category : '' )) )
							  issueSheet.addCell( new Label( 10, r, String.valueOf(unresolvedIssues[r-1].dateCreated ? unresolvedIssues[r-1].dateCreated : '')) )
							  issueSheet.addCell( new Label( 11, r, String.valueOf(unresolvedIssues[r-1].dateResolved ? unresolvedIssues[r-1].dateResolved : '')) )
						}
					   for ( int r = 7; r <= (postMoveIssue.size()+6); r++ ) {
							  postMoveSheet.addCell( new Label( 0, r, String.valueOf(r-6)) )
							  postMoveSheet.addCell( new Label( 1, r, String.valueOf(postMoveIssue[r-7].assetEntity?.assetName ? postMoveIssue[r-7].assetEntity?.assetName : '' )) )
							  postMoveSheet.addCell( new Label( 2, r, String.valueOf(postMoveIssue[r-7].comment ? postMoveIssue[r-7].comment : '')) )
							  postMoveSheet.addCell( new Label( 3, r, String.valueOf(postMoveIssue[r-7].assignedTo ? postMoveIssue[r-7].assignedTo : '' )) )
							  postMoveSheet.addCell( new Label( 4, r, String.valueOf(postMoveIssue[r-7].status ? postMoveIssue[r-7].status  :'' )) )
							  postMoveSheet.addCell( new Label( 5, r, String.valueOf(postMoveIssue[r-7].dueDate ? postMoveIssue[r-7].dueDate : '')) )
							  postMoveSheet.addCell( new Label( 6, r, String.valueOf(postMoveIssue[r-7].dateResolved ? postMoveIssue[r-7].dateResolved : '')) )
							  postMoveSheet.addCell( new Label( 7, r, String.valueOf(postMoveIssue[r-7].resolution ? postMoveIssue[r-7].resolution : '')) )
					  }
					   
					   for ( int r = 7; r <= (preMoveIssue.size()+6); r++ ) {
						      preMoveSheet.addCell( new Label( 0, r, String.valueOf(r-6)) )
							  preMoveSheet.addCell( new Label( 1, r, String.valueOf('' )) )
							  preMoveSheet.addCell( new Label( 2, r, String.valueOf(preMoveIssue[r-7]?.assetEntity?.assetName ? preMoveIssue[r-7]?.assetEntity?.assetName : '' )) )
							  preMoveSheet.addCell( new Label( 3, r, String.valueOf(preMoveIssue[r-7].comment ? preMoveIssue[r-7].comment : '')) )
							  preMoveSheet.addCell( new Label( 4, r, String.valueOf(preMoveIssue[r-7].assignedTo ? preMoveIssue[r-7].assignedTo : '' )) )
							  preMoveSheet.addCell( new Label( 5, r, String.valueOf(preMoveIssue[r-7].status ? preMoveIssue[r-7].status  :'' )) )
							  preMoveSheet.addCell( new Label( 6, r, String.valueOf(preMoveIssue[r-7].dueDate ? preMoveIssue[r-7].dueDate : '')) )
							  preMoveSheet.addCell( new Label( 7, r, String.valueOf(preMoveIssue[r-7].dateResolved ? preMoveIssue[r-7].dateResolved : '')) )
							  preMoveSheet.addCell( new Label( 8, r, String.valueOf(preMoveIssue[r-7].resolution ? preMoveIssue[r-7].resolution : '')) )
					 }
						  
						book.write()
						book.close()
					} catch( Exception ex ) {
					  println "Exception occurred while exporting data"+ex.printStackTrace()
					  
					  return;
				   }
				return ;
		
	}
	/*
	 * markEventAssetAsMoved : Used to set asset's plan-status to 'Moved' for move event .
	 * @param moveEventId
	 * @return  Count of record affected with this update or Error Message if any
	 * 
	 */
	
	def markEventAssetAsMoved = {
		def assetAffected
		def errorMsg
		def project = securityService.getUserCurrentProject()
		if(params.containsKey("moveEventId")){
			if(params.moveEventId.isNumber()){
				def moveEvent = MoveEvent.get(params.moveEventId)
				if(moveEvent){
					if (moveEvent.project.id != project.id) {
						log.error "markEventAssetAsMoved: moveEvent.project (${moveEvent.id}) does not match user's current project (${project.id})"
						errorMsg = "An unexpected condition with the move event occurred that is preventing an update"
					}else{
						def bundleForEvent = moveEvent.moveBundles
						assetAffected = bundleForEvent ? AssetEntity.executeUpdate("update AssetEntity ae set ae.planStatus='Moved' where ae.moveBundle in (:bundles)\
																	and  ae.planStatus !='Moved' ",[bundles:bundleForEvent]) : 0
					}
				} else {
					log.error "markEventAssetAsMoved: Specified moveEvent (${params.moveEventId}) was not found})"
					errorMsg = "An unexpected condition with the move event occurred that is preventing an update."
			    }
			}
		}
		render errorMsg ? errorMsg : assetAffected
	}
}
