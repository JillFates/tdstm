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
import com.tdssrc.grails.WebUtil

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
	
			// Validate that the runbook recipe syntax is okay
			if (params.runbookRecipe?.size() > 0) {
				def error
				try {
					Eval.me("[${params.runbookRecipe}]")
				} catch (e) {
					error = e.getMessage()
					error = error.replaceAll(/[\r]/, '<br/>')
				}
				if (error) {
					flash.message = "There was an error with the runtime recipe<br/>$error"
					render(view:'edit',model:[moveEventInstance:moveEventInstance])
					return
				}
			}
	
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
     * @param moveEventId
     * @param type (delete/clear)
	 * @return text
	 * @usage ajax
	 */
	def clearTaskData = {		
		def project = securityService.getUserCurrentProject()
		def moveEvent
		def msg 
		
		// TODO - Need to create an ACL instead of using roles for this
		if (!securityService.hasRole(['ADMIN','CLIENT_ADMIN','CLIENT_MGR']) ) {
			msg = "You do not have the proper permissions to perform this task"
		} else {	
			if (params.moveEventId.isNumber()) {
				moveEvent = MoveEvent.findByIdAndProject( params.moveEventId, project)
				if (! moveEvent) {
					msg = "You present do not have access to the move event"
				}
			} else {
				msg = "Invalid Move Event specified"
			}
		}
		if (! msg) {
			try {
				if(params.type == 'clear'){
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
		def  preMoveSize = AssetComment.countByMoveEventAndCategory(moveEventInstance, 'premove')
		def  sheduleSize = AssetComment.countByMoveEventAndCategoryInList(moveEventInstance, ['shutdown','physical','moveday','startup'])
		def  postMoveSize = AssetComment.countByMoveEventAndCategory(moveEventInstance, 'postmove')
		return [applcationAssigned:applcationAssigned, assetCount:assetCount, databaseCount:databaseCount, fileCount:fileCount, otherAssetCount:otherAssetCount,
			    preMoveSize: preMoveSize, sheduleSize:sheduleSize, postMoveSize:postMoveSize, bundles:bundles,moveEventInstance:moveEventInstance]
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
			def formatter = new SimpleDateFormat("yyyy-MM-dd");
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
			}
			preMoveIssue = AssetComment.findAllByMoveEventAndCategory(moveEventInstance, 'premove') 
			def sheduleIssue = AssetComment.findAllByMoveEventAndCategoryInList(moveEventInstance, ['shutdown','physical','moveday','startup'])
			postMoveIssue = AssetComment.findAllByMoveEventAndCategory(moveEventInstance, 'postmove') 
			//TODO - Move controller code into Service .
			def preMoveCheckListError = reportsService.generatePreMoveCheckList(projectId,moveEventInstance).allErrors.size()
			try {
						File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Runbook.xls" ).getFile()
						WorkbookSettings wbSetting = new WorkbookSettings()
						wbSetting.setUseTemporaryFileDuringWrite(true)
						def workbook = Workbook.getWorkbook( file, wbSetting )
						//set MIME TYPE as Excel
						response.setContentType( "application/vnd.ms-excel" )
						def filename = 	"${project.name} - ${moveEventInstance.name} Runbook v${currentVersion} -${today}"
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
						
						def scheduleSheet = book.getSheet("Schedule")
						
						
						def preMoveColumnList = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment','assignedTo', 'status','estStart','','', 'notes',
								         				'duration', 'estStart','estFinish','actStart',
								         				'actFinish', 'workflow']
						
 						def sheduleColumnList = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment', 'role', 'assignedTo', '',
	 						        				'duration', 'estStart','estFinish', 'actStart','actFinish', 'workflow'
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
						
						def dbColumnList = ['id', 'assetName', 'dbFormat', 'dbSize', 'description', 'supportType','retireDate', 'maintExpDate',
											'environment','ipAddress', 'planStatus','custom1','custom2','custom3','custom4','custom5',
											'custom6','custom7','custom8'
											]
						
						def filesColumnList = ['id', 'assetName', 'fileFormat', 'fileSize', 'description', 'supportType','retireDate', 'maintExpDate',
											   'environment','ipAddress', 'planStatus','custom1','custom2','custom3','custom4','custom5',
											   'custom6','custom7','custom8'
											  ]
						
						def othersColumnList = ['id','application','assetName', 'shortName', 'serialNumber', 'assetTag', 'manufacturer',
												'model','assetType','ipAddress', 'os', 'sourceLocation', 'sourceLocation','sourceRack',
												'sourceRackPosition','sourceBladeChassis','sourceBladePosition',
												'targetLocation','targetRoom','targetRack', 'targetRackPosition','targetBladeChassis',
												'targetBladePosition','custom1','custom2','custom3','custom4','custom5','custom6','custom7','custom8',
												'moveBundle','truck','cart','shelf','railType','priority','planStatus','usize'
						   						]
						
						def unresolvedIssueColumnList = ['id', 'comment', 'commentType','commentAssetEntity','resolution','resolvedBy','createdBy',
						              				'dueDate','assignedTo','category','dateCreated','dateResolved', 'assignedTo','status','taskDependencies','duration','estStart','estFinish','actStart',
						              				'actFinish','workflow'
						              			]
						
						
						summarySheet.addCell( new Label( 1, 1, String.valueOf(project.name )) )
						summarySheet.addCell( new Label( 2, 3, String.valueOf(project.name )) )
						summarySheet.addCell( new Label( 2, 4, String.valueOf(moveEventInstance.name )) )
						summarySheet.addCell( new Label( 2, 10, String.valueOf(moveEventInstance.name )) )
						
						moveBundleService.issueExport(assets, serverColumnList, serverSheet, 5)
						
						//moveBundleService.issueExport(applications, appColumnList, appSheet, 5)
						
						moveBundleService.issueExport(databases, dbColumnList, dbSheet, 4)
						
						moveBundleService.issueExport(files, filesColumnList, filesSheet, 1)
						
						moveBundleService.issueExport(others, othersColumnList, otherSheet, 1)
						
						moveBundleService.issueExport(unresolvedIssues, unresolvedIssueColumnList, issueSheet, 1)
						
						moveBundleService.issueExport(sheduleIssue, sheduleColumnList, scheduleSheet, 7)
						
						moveBundleService.issueExport(preMoveIssue, preMoveColumnList, preMoveSheet, 7)
						
						moveBundleService.issueExport(postMoveIssue, postMoveColumnList,  postMoveSheet, 7)
						  
							  
						def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $projectId and p.roleTypeCodeFrom = 'PROJECT' ")
						for ( int r = 8; r <= (projectStaff.size()+7); r++ ) {
							def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = ${projectStaff[0].partyIdTo.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
							personelSheet.addCell( new Label( 1, r, String.valueOf( projectStaff[r-8].partyIdTo.firstName+" "+ projectStaff[r-8].partyIdTo.lastName)) )
							personelSheet.addCell( new Label( 2, r, String.valueOf(projectStaff[r-8].roleTypeCodeTo )) )
							personelSheet.addCell( new Label( 5, r, String.valueOf(projectStaff[r-8].partyIdTo.email ? projectStaff[r-8].partyIdTo.email : '')) )
						}
						book.write()
						book.close()
					} catch( Exception ex ) {
					  println "Exception occurred while exporting data"+ex.printStackTrace()
					  
					  return;
				   }
				return ;
		
	}
	
	/**
	 * Used to set asset's plan-status to 'Moved' for the specified move event
	 * @usage Ajax
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
						assetAffected = bundleForEvent ? jdbcTemplate.update("update asset_entity  \
							set new_or_old = 'Moved', source_location = target_location, room_source_id = room_target_id ,\
								rack_source_id = rack_target_id, source_rack_position = target_rack_position, \
								source_blade_chassis = target_blade_chassis, source_blade_position = target_blade_position, \
								target_location = null, room_target_id = null, rack_target_id = null, target_rack_position = null,\
								target_blade_chassis = null, target_blade_position = null\
							where move_bundle_id in (SELECT mb.move_bundle_id FROM move_bundle mb WHERE mb.move_event_id =  $moveEvent.id) \
								and new_or_old != 'Moved' ") : 0
					}
				} else {
					log.error "markEventAssetAsMoved: Specified moveEvent (${params.moveEventId}) was not found})"
					errorMsg = "An unexpected condition with the move event occurred that is preventing an update."
			    }
			}
		}
		render errorMsg ? errorMsg : assetAffected
	}
	
	/** 
	 * Generates a runbook for a specified move event using a recipe 
	 * @usage Ajax
	 * @param moveEventId
	 * @return Results or error message appropriately
	 */	
	def generateMovedayTasks = {
					
		def message
				 
		if (! securityService.hasRole(['ADMIN','CLIENT_ADMIN', 'CLIENT_MGR'])) {
			message = 'Sorry but you do not have the permissions to perform this action'
		} else {
			def project = securityService.getUserCurrentProject()
			def moveEvent 
		
			if (params.moveEventId?.isNumber()) {
				moveEvent = MoveEvent.findByIdAndProject(params.moveEventId, project) 
			}
		
			if (! moveEvent) {
				message = "Invalid event id or you don't have access to this project/move event"
			} else {
				message = taskService.generateRunbook( securityService.getUserLoginPerson() , moveEvent ) 
			}
		}
		render message
	}
}
