import grails.converters.JSON
import java.text.SimpleDateFormat
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
    		def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) 
log.debug "offsetTZ=${offsetTZ}"
			def sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
			if( moveBundle ){
				
				/* Get the latest step_snapshot record for each step that has started */
				def latestStepsRecordsQuery = """
					SELECT mbs.transition_id as tid, ss.id as snapshotId, mbs.label as label,
						DATE_FORMAT( ADDDATE( mbs.plan_start_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as planStart,
						DATE_FORMAT( ADDDATE( mbs.plan_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as planComp,
						DATE_FORMAT( ADDDATE( mbs.actual_start_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as actStart,
						DATE_FORMAT( ADDDATE( mbs.actual_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as actComp,
						DATE_FORMAT( ADDDATE( ss.date_created , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as dateCreated,
						ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd 
					FROM move_bundle mb
					LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id 
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id
					WHERE mb.move_bundle_id = ${moveBundle.id}
						AND ss.date_created = 
							(SELECT MAX(date_created) FROM step_snapshot ss2 WHERE ss2.move_bundle_step_id = mbs.id)
				""" 
					
				/*	Get the steps that have not started / don't have step_snapshot records	*/						
				def stepsNotUpdatedQuery = """
					SELECT mbs.transition_id as tid, ss.id as snapshotId, mbs.label as label,
						DATE_FORMAT( ADDDATE( mbs.plan_start_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as planStart,
						DATE_FORMAT( ADDDATE( mbs.plan_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as planComp,
						DATE_FORMAT( ADDDATE( mbs.actual_start_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as actStart,
						DATE_FORMAT( ADDDATE( mbs.actual_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as actComp,
						DATE_FORMAT( ADDDATE( ss.date_created , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r') as dateCreated,
						ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd 
					FROM move_bundle mb
					LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id
					LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id 
					WHERE mb.move_bundle_id = ${moveBundle.id} AND ss.date_created IS NULL AND mbs.transition_id IS NOT NULL
				"""
					
				dataPointsForEachStep = jdbcTemplate.queryForList( latestStepsRecordsQuery + " UNION " + stepsNotUpdatedQuery + " ORDER BY tid" )
				
			}
    		def sysTime  = jdbcTemplate.queryForMap("SELECT DATE_FORMAT( ADDDATE( CURRENT_TIMESTAMP , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %r' ) as sysTime").get("sysTime")
			
			def sysTimeInMs = new Date(sysTime).getTime()
			
    		dataPointsForEachStep.each{ data ->
    			def snapshot 
				if( data.snapshotId ){
					snapshot = StepSnapshot.findById( data.snapshotId )
					data.put( "projComp", snapshot.getProjectedCompletionTime() )
					data.put( "statColor", snapshot.getStatusColor() )
				} else {
					data.put( "projComp", "" )
					data.put( "statColor", "red" )
				}
    			if( !data.actStart ){
    				if( sysTimeInMs > new Date(data.planStart).getTime() ){
    					data.put( "percentageStyle", "step_statusbar_bad" )
    				} else {
    					data.put( "percentageStyle", "step_statusbar_good" )
    				}
    			} else if( !data.actComp ){
    				if(data.projComp){
	    				if( new Date( data.projComp ).getTime() > new Date( data.planComp ).getTime() ){
	    					data.put( "percentageStyle", "step_statusbar_bad" )
	    				} else {
	    					data.put( "percentageStyle", "step_statusbar_good" )
	    				}
    				} else {
    					data.put( "percentageStyle", "step_statusbar_good" )
    				}
    			} else {
    				if( new Date( data.actComp ).getTime() > new Date( data.planComp ).getTime() ){
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
					SELECT DATE_FORMAT( ADDDATE( max(mb.completion_time) , INTERVAL ${offsetTZ} HOUR),'%Y/%m/%d %h:%m:%s' ) as compTime
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
					def revisedCompTime = revisedComp.getTime() + (3600000 * offsetTZ)
					revisedComp = new Date(revisedCompTime)
				}
    		}

    		def dataPointStepMap  = [ 
				"snapshot": [ 
					"revisedComp" : moveEvent?.revisedCompletionTime, 
					"moveBundleId" : moveBundleId,
					"planDelta" : moveEventPlannedSnapshot?.planDelta,
					"systime": sysTime,
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
