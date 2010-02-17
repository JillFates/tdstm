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
    		def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) * ( -1 )
			 
			if( moveBundle ){
				
				/* Get the latest step_snapshot record for each step that has started */
				def latestStepsRecordsQuery = "SELECT mbs.transition_id as tid, ss.id as snapshotId, mbs.label as label, "+
												" DATE_FORMAT(ADDDATE( mbs.plan_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planStart, "+
												" DATE_FORMAT(ADDDATE( mbs.plan_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planComp, "+
												" DATE_FORMAT(ADDDATE( mbs.actual_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actStart, "+
												" DATE_FORMAT(ADDDATE( mbs.actual_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actComp, "+
												" DATE_FORMAT(ADDDATE( ss.date_created , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as dateCreated, "+
												" ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd FROM move_bundle mb "+
												" LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id " +
												" LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id "+
												" WHERE mb.move_bundle_id = ${moveBundle.id}"+
												" AND ss.date_created = (SELECT MAX(date_created) FROM step_snapshot ss2 WHERE ss2.move_bundle_step_id = mbs.id) " 
					
				/*	Get the steps that have not started / don't have step_snapshot records	*/						
				def stepsNotUpdatedQuery = "SELECT mbs.transition_id as tid, ss.id as snapshotId, mbs.label as label, "+
											" DATE_FORMAT(ADDDATE( mbs.plan_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planStart, "+
											" DATE_FORMAT(ADDDATE( mbs.plan_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planComp, "+
											" DATE_FORMAT(ADDDATE( mbs.actual_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actStart, "+
											" DATE_FORMAT(ADDDATE( mbs.actual_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actComp,"+
											" DATE_FORMAT(ADDDATE( ss.date_created , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as dateCreated,"+
											" ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd FROM move_bundle mb "+
											" LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id "+
											" LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id "+
											" WHERE mb.move_bundle_id = ${moveBundle.id} AND ss.date_created IS NULL AND mbs.transition_id IS NOT NULL" 
					
				dataPointsForEachStep = jdbcTemplate.queryForList( latestStepsRecordsQuery + " UNION " + stepsNotUpdatedQuery + " ORDER BY tid" )
				
			}
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
    		}
    		def moveEvent 
			if( moveEventId ){
				moveEvent = MoveEvent.findById( moveEventId );
			}
    		def planSumCompTime
    		if( moveEvent ){
    			planSumCompTime = jdbcTemplate.queryForMap( "SELECT max(mb.completion_time) as compTime "+
    							" FROM move_bundle mb WHERE mb.move_event_id = ${moveEvent.id}" )?.compTime
    		}
    		
    		def dataPointStepMap  = [ 
									  "snapshot": [ 
													'moveEvent' : moveEvent, 
													'moveBundleId' : moveBundleId,
													"systime": "2010-04-27T21:15:18.20Z",
													"planSum": [ "dialInd": 48, "confText": "High", "confColor": "green", 'compTime':planSumCompTime ],
													"revSum": [ "dialInd": -1,'compTime':moveEvent?.revisedCompletionTime ],
													"steps": dataPointsForEachStep,
													] 
    								]
    	
			render dataPointStepMap as JSON
	
	}
}
