import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdssrc.grails.GormUtil
import java.text.SimpleDateFormat
import java.text.DateFormat
class MoveEventController {
	
    // Service initialization
	def moveBundleService
	def jdbcTemplate
	def userPreferenceService
	
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
    	if(currProj) moveEventInstanceList = MoveEvent.findAllByProject( Project.get( currProj ), params )
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
		moveEventId = moveEventId ? moveEventId : session.getAttribute("MOVE_EVENT")?.MOVE_EVENT;
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
        def moveEventInstance = MoveEvent.get( params.id )
        if(moveEventInstance) {
        	def moveEventName = moveEventInstance.name
            moveEventInstance.delete()
            flash.message = "MoveEvent ${moveEventName} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "MoveEvent not found with id ${params.id}"
            redirect(action:list)
        }
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

		if(!moveEventInstance.hasErrors() && moveEventInstance.save()) {
			
			moveBundleService.assignMoveEvent( moveEventInstance, moveBundles )
            
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
    	def projectId = params.projectId
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
				redirect( controller:'moveBundleAsset', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
				return;
			}	
		} else {
			flash.message = "Please select MoveEvent and report type. "
			redirect( controller:'moveBundleAsset', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
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
					redirect( controller:'moveBundleAsset', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
					return;
				}	
			} else {
				flash.message = "Please select MoveEvent and report type. "
				redirect( controller:'moveBundleAsset', action:"getBundleListForReportDialog", params:[reportId:'MoveResults', message:flash.message] )
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
    
}
