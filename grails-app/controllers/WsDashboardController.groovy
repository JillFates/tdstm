import grails.converters.JSON
import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil
class WsDashboardController {
	
	def jdbcTemplate

        
    def bundleData = {
    		def moveBundleId = params.id
			
			def moveEventId = params.moveEventId
			def moveBundle
			def dataPointsForEachStep = []
			if( moveBundleId ){ 
				moveBundle = MoveBundle.findById( moveBundleId )
			}
    		
    		// def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) * ( -1 )
    		/*def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) 
			log.debug "offsetTZ=${offsetTZ}"*/
			
			def sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
			if( moveBundle ){
				
				/* Get the latest step_snapshot record for each step that has started */
				def latestStepsRecordsQuery = """
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
			def sysTime = GormUtil.convertInToGMT( "now", "EDT" )
			def sysTimeInMs = sysTime.getTime() / 1000
			
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
    				def actCompTime = new Date( data.actComp ).getTime() / 1000
    				if( actCompTime > planCompTime+59 ){  // 59s added to planCompletion to consider the minuits instead of seconds 
    					data.put( "percentageStyle", "step_statusbar_bad" )
    				} else {
    					data.put( "percentageStyle", "step_statusbar_good" )
    				}
    			}
    		}
    		def moveEvent 
			if( moveEventId ){
				moveEvent = MoveEvent.findById( moveEventId );
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

    		def dataPointStepMap  = [ 
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
				] 
    		]
    		render dataPointStepMap as JSON
	
	}
}
