import grails.converters.JSON
import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil
class WsDashboardController {
	
	def jdbcTemplate
	def securityService

	/**
	 * This control returns the data used to render the Event Dashboard including the work flow steps and the statistics of 
	 * what there is to do and what has been accomplished.
	 * @param id - the move bundle id
	 * @param moveEventId
	 * @return JSON map 
	 */
    def bundleData = {
		def error = ""
		def project = securityService.getUserCurrentProject()
		def moveEventId = params.moveEventId
		def moveBundleId = params.id
		def moveEvent
		def moveBundle

		// Validate that the user is legitly accessing the proper move event
		if (! moveEventId.isNumber() ) {
			error = "Move event id is invalid"
		} else {
			moveEvent = MoveEvent.findByIdAndProject(moveEventId, project)
			if (! moveEvent) {
				error = "Unable to find referenced move event for your current project"
			} else {
				if (! moveBundleId ) {
					// Take the first move bundle in the event
					if (moveEvent.moveBundles.size() > 0) {
						moveBundle = moveEvent.moveBundles[0]
						moveBundleId = moveBundle.id
					}
				} else if (! moveBundleId.isNumber()) {
		   			error = "Move bundle id is invalid"
		   		} else {
		   			moveBundle = MoveBundle.findByIdAndProject(moveBundleId, project)
		   			if (! moveBundle) {
		   				error = "Unable to find referenced move bundle for your current project"
		   			}
		   		}
			}
		}

		if ( error != "" ) {
			def errorMap = [ 'error': error ] 
			render errorMap as JSON
			return
		}

		def dataPointsForEachStep = []

		// Get the step data either by runbook tasks or     	
    	if (moveBundle) {
	    	if (project.runbookOn) {
				def taskStatsSql = """
					SELECT 
						wt.workflow_transition_id AS wfTranId, 
						mbs.transition_id AS tid,
						ss.id AS snapshotId,
						mbs.label AS label, 
						mbs.calc_method AS calcMethod,
						SUM(IF(t.asset_comment_id IS NULL, 0, 1)) AS tskTot,
						SUM(IF(t.status='Pending',1,0)) AS tskPending,
						SUM(IF(t.status='Ready',1,0)) AS tskReady,
						SUM(IF(t.status='Started',1,0)) AS tskStarted,
						SUM(IF(t.status='Completed',1,0)) AS tskComp,
						SUM(IF(t.status='Hold',1,0)) AS tskHold,
						ROUND(IF(count(*)>0,SUM(IF(t.status='Completed',1,0))/count(*)*100,100)) AS percComp,
						DATE_FORMAT( mbs.plan_start_time ,'%Y/%m/%d %r') AS planStart,
						DATE_FORMAT( mbs.plan_completion_time ,'%Y/%m/%d %r') AS planComp,
						MIN(IF(ISNULL(t.act_start),t.date_resolved, t.act_start)) AS actStart,
						MAX(t.date_resolved) AS actComp
					FROM move_bundle_step mbs
					JOIN move_bundle mb ON mb.move_bundle_id=mbs.move_bundle_id
					JOIN workflow wf ON wf.process=mb.workflow_code
					JOIN workflow_transition wt ON wt.workflow_id=wf.workflow_id AND wt.trans_id=mbs.transition_id 
					LEFT OUTER JOIN asset_entity a ON a.move_bundle_id=mbs.move_bundle_id
					LEFT OUTER JOIN asset_comment t ON t.workflow_transition_id=wt.workflow_transition_id AND t.asset_entity_id=a.asset_entity_id
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id 
					where mbs.move_bundle_id=${moveBundleId}
					group by wt.workflow_transition_id;
				"""
				
				dataPointsForEachStep = jdbcTemplate.queryForList(taskStatsSql)

	    	} else {

	    		// def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) * ( -1 )
	    		/*def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) 
				log.debug "offsetTZ=${offsetTZ}"*/
			
				/* Get the latest step_snapshot record for each step that has started */
				def latestStepsRecordsQuery = """
					SELECT mbs.transition_id as tid, 
						ss.id as snapshotId, 
						mbs.label as label, 
						mbs.calc_method as calcMethod,
						DATE_FORMAT( mbs.plan_start_time ,'%Y/%m/%d %r') as planStart,
						DATE_FORMAT( mbs.plan_completion_time ,'%Y/%m/%d %r') as planComp,
						DATE_FORMAT( mbs.actual_start_time ,'%Y/%m/%d %r') as actStart,
						DATE_FORMAT( mbs.actual_completion_time ,'%Y/%m/%d %r') as actComp,
						DATE_FORMAT( ss.date_created ,'%Y/%m/%d %r') as dateCreated,
						ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd 
					FROM move_bundle mb
					LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id 
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
					INNER JOIN (SELECT move_bundle_step_id, MAX(date_created) as date_created FROM step_snapshot GROUP BY move_bundle_step_id) ss2
					ON ss2.move_bundle_step_id = mbs.id AND ss.date_created = ss2.date_created
					WHERE mb.move_bundle_id = ${moveBundle.id} 
				""" 
					
				/*	Get the steps that have not started / don't have step_snapshot records	*/						
				def stepsNotUpdatedQuery = """
					SELECT mbs.transition_id as tid, ss.id as snapshotId, mbs.label as label, mbs.calc_method as calcMethod,
						DATE_FORMAT( mbs.plan_start_time ,'%Y/%m/%d %r') as planStart,
						DATE_FORMAT( mbs.plan_completion_time ,'%Y/%m/%d %r') as planComp,
						DATE_FORMAT( mbs.actual_start_time ,'%Y/%m/%d %r') as actStart,
						DATE_FORMAT( mbs.actual_completion_time ,'%Y/%m/%d %r') as actComp,
						DATE_FORMAT( ss.date_created ,'%Y/%m/%d %r') as dateCreated,
						ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd 
					FROM move_bundle mb
					LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id 
					WHERE mb.move_bundle_id = ${moveBundle.id} AND ss.date_created IS NULL AND mbs.transition_id IS NOT NULL
				"""
					
				dataPointsForEachStep = jdbcTemplate.queryForList( latestStepsRecordsQuery + " UNION " + stepsNotUpdatedQuery )
				
			}
		}

		def sysTime = GormUtil.convertInToGMT( "now", "EDT" )
		def sysTimeInMs = sysTime.getTime() / 1000
		def sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");

		dataPointsForEachStep.each{ data ->
			
			def snapshot 
			def planCompTime = new Date( data.planComp ).getTime() / 1000  
			def planStartTime = new Date( data.planStart ).getTime() / 1000
			
			if( data.snapshotId ){
				snapshot = StepSnapshot.findById( data.snapshotId )
				data.put( "projComp", snapshot.getProjectedCompletionTime() )
				data.put( "statColor", snapshot.getStatusColor() )
				if(snapshot.moveBundleStep.showInGreen){
					data.put( "percentageStyle", "step_statusbar_good" )
					return;
				}
			} else {
				data.put( "projComp", "" )
				data.put( "statColor", "red" )
			}
			
			if( !data.actComp ){
				if( sysTimeInMs > planCompTime+59 && data.tskComp < data.tskTot){  // 59s added to planCompletion to consider the minuits instead of seconds 
					data.put( "percentageStyle", "step_statusbar_bad" )
				} else{
					def remainingStepTime = planCompTime - sysTimeInMs
					// 20% of planned duration
					def planDurationLeft = (planCompTime - planStartTime) * 0.2 
					// 80% of remainin assets
					def remainingTasks =  data.tskTot ? data.tskTot * 0.6 : 0
					if(remainingStepTime <= planDurationLeft && remainingTasks > data.tskComp){
						data.put( "percentageStyle", "step_statusbar_yellow" )
					} else {
						data.put( "percentageStyle", "step_statusbar_good" )
					}
				}
				/*if(data.projComp){
    				if( new Date( data.projComp ).getTime() > new Date( data.planComp ).getTime() ){
    					data.put( "percentageStyle", "step_statusbar_bad" )
    				} else {
    					data.put( "percentageStyle", "step_statusbar_yellow" )
    				}*/
					// commented for now
    				/*if(data.dialInd < 25){
    					data.put( "percentageStyle", "step_statusbar_bad" )
    				} else if(data.dialInd >= 25 && data.dialInd < 50){
    					data.put( "percentageStyle", "step_statusbar_yellow" )
    				} else {
    					data.put( "percentageStyle", "step_statusbar_good" )
    				}
    				
				} else {
					data.put( "percentageStyle", "step_statusbar_good" )
				}*/
			} else {
				def actCompTime = new Date( data.actComp?.getTime() ).getTime() / 1000
				if( actCompTime > planCompTime+59 ){  // 59s added to planCompletion to consider the minutes instead of seconds 
					data.put( "percentageStyle", "step_statusbar_bad" )
				} else {
					data.put( "percentageStyle", "step_statusbar_good" )
				}
			}
		}
		
		def planSumCompTime
		def moveEventPlannedSnapshot
		def moveEventRevisedSnapshot
		def revisedComp 
		if( moveEvent ){
			
			def resultMap = jdbcTemplate.queryForMap( """
				SELECT DATE_FORMAT( max(mb.completion_time) ,'%Y/%m/%d %r' ) as compTime
				FROM move_bundle mb WHERE mb.move_event_id = ${moveEvent.id}
				""" )

			planSumCompTime = resultMap?.compTime

			/*
			* select the most recent MoveEventSnapshot records for the event for both the P)lanned and R)evised types.
			*/
			def query = "FROM MoveEventSnapshot mes WHERE mes.moveEvent = ? AND mes.type = ? ORDER BY mes.dateCreated DESC"
			// moveEventPlannedSnapshot = MoveEventSnapshot.find( query , [moveEvent , MoveEventSnapshot.TYPE_PLANNED] )[0]
			// moveEventRevisedSnapshot = MoveEventSnapshot.find( query , [moveEvent, MoveEventSnapshot.TYPE_REVISED] )[0]
			moveEventPlannedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent , "P"] )[0]
			moveEventRevisedSnapshot = MoveEventSnapshot.findAll( query , [moveEvent, "R"] )[0]
			revisedComp = moveEvent.revisedCompletionTime
			if(revisedComp){
				def revisedCompTime = revisedComp.getTime()
				revisedComp = new Date(revisedCompTime)
			}
		}

		def bundleMap  = [ 
			"snapshot": [ 
				"revisedComp" : moveEvent?.revisedCompletionTime, 
				"moveBundleId" : moveBundleId,
				"calcMethod":moveEvent?.calcMethod,
				"planDelta" : moveEventPlannedSnapshot?.planDelta,
				"systime": sdf.format(sysTime),
				"planSum": [ 
					"dialInd": moveEventPlannedSnapshot?.dialIndicator, "confText": "High", 
					"confColor": "green", "compTime":planSumCompTime 
				],
				"revSum": [ 
					"dialInd": moveEventRevisedSnapshot?.dialIndicator,
					"compTime": revisedComp ? sdf.format(revisedComp) : "" 
				],
				"steps": dataPointsForEachStep,
				'runbookOn':project.runbookOn,
			] 
		]
		render bundleMap as JSON
	
	}
}
