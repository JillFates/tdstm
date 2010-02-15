import grails.converters.JSON
class WsDashboardController {
	
	def jdbcTemplate
        
    def bundleData = {
    		def moveBundleId = params.id
			def moveBundle
			def dataPointsForEachStep = []
			if( moveBundleId ){ 
				moveBundle = MoveBundle.findById( moveBundleId )
			}
    		def offsetTZ = ( new Date().getTimezoneOffset() / 60 ) * ( -1 )
			 
			if( moveBundle ){
				def latestStepsRecordsQuery = "SELECT mbs.transition_id as tid, mbs.label as label, "+
										" DATE_FORMAT(ADDDATE( mbs.plan_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planStart, "+
										" DATE_FORMAT(ADDDATE( mbs.plan_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planComp"+
										" DATE_FORMAT(ADDDATE( mbs.actual_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actStart, "+
										" DATE_FORMAT(ADDDATE( mbs.actual_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actComp, "+
										" DATE_FORMAT(ADDDATE( ss.date_created , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as projComp, "+
										" ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd FROM move_bundle mb "+
										" LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id " +
										" LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id "+
										" WHERE mb.move_bundle_id = ${moveBundle.id}"+
										" AND ss.date_created = (SELECT MAX(date_created) FROM step_snapshot ss2 WHERE ss2.move_bundle_step_id = mbs.id) "
										
				def stepsNotUpdatedQuery = "SELECT mbs.transition_id as tid, mbs.label as label, "+
										" DATE_FORMAT(ADDDATE( mbs.plan_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planStart, "+
										" DATE_FORMAT(ADDDATE( mbs.plan_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as planComp, "+
										" DATE_FORMAT(ADDDATE( mbs.actual_start_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actStart, "+
										" DATE_FORMAT(ADDDATE( mbs.actual_completion_time , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as actComp,"+
										" DATE_FORMAT(ADDDATE( ss.date_created , INTERVAL ${offsetTZ} HOUR),'%Y-%m-%d %h:%m:%s') as projComp,"+
										" ss.tasks_count as tskTot, ss.tasks_completed as tskComp, ss.dial_indicator as dialInd FROM move_bundle mb "+
										" LEFT JOIN move_bundle_step mbs ON mbs.move_bundle_id = mb.move_bundle_id "+
										" LEFT JOIN step_snapshot ss ON ss.move_bundle_step_id = mbs.id "+
										" WHERE mb.move_bundle_id = ${moveBundle.id} AND ss.date_created IS NULL "
				
				dataPointsForEachStep = jdbcTemplate.queryForList( stepsNotUpdatedQuery + " UNION " + stepsNotUpdatedQuery + " ORDER BY tid" )
			}
    		
    		def list  = [ "snapshot": [
							"systime": "2010-04-27T21:15:18.20Z",
							"planSum": [ "dialInd": 48, "confText": "High", "confColor": "green" ],
							"revSum": [ "dialInd": -1 ],
							"steps": [
									  [
									   	"tid": 60, 
										"label": "Unracking", 
										"statColor": "green", 
										"planStart": "2010-04-27T20:00:00.00Z", 
										"planComp": "2010-04-27T21:00:00.00Z", 
										"actStart": "2010-04-27T20:01:02.03Z", 
										"actComp": "2010-04-27T20:52:10.33Z", 
										"projComp": "", 
										"tskTot": 50, 
										"tskComp": 50, 
										"dialInd": -1],
									  [
									   	"tid" : 70, 
										"label": "Staging", 
										"statColor": "green", 
										"planStart": "2010-04-27T20:10:50.52Z", 
										"planComp": "2010-04-27T21:10:00.52Z", 
										"actStart": "2010-04-27T20:04:10.41Z", 
										"actComp": "2010-04-27T21:04:18.52Z", 
										"projComp": "", 
										"tskTot": 50, 
										"tskComp": 50, 
										"dialInd": -1],
									  [
									   "tid": 110, 
									   "label": "Transport", 
									   "statColor": "red", 
									   "planStart": "2010-04-27T21:15:00.00Z", 
									   "planComp": "2010-04-27T22:15:00.00Z",  
									   "actStart": "2010-04-27T21:15:10.04Z", 
									   "actComp": "", 
									   "projComp": "2010-04-27T22:20:50.52Z", 
									   "tskTot": 100, 
									   "tskComp": 25, 
									   "dialInd": 47
									   ]
									  ],
							] 
    		]
    		/*list.get('snapshot').get('steps').each{
    			println"it------------------->"+it
				it.put('tid',100)
				println"it------------------->"+it
    		}*/

			render list as JSON
	
	}
}
