

import org.springframework.jdbc.core.JdbcTemplate
import org.junit.Ignore

class MoveBundleServiceTests {
	
	def moveBundleService = new MoveBundleService()
	
	def jdbcTemplate = new JdbcTemplate( getDatasource() )
	
	//test case for moveBundleService.assetCount( def moveBundleId )
	@Ignore
    void testassetCount() {
    	moveBundleService.jdbcTemplate = jdbcTemplate
		
		assertEquals 6, moveBundleService.assetCount("27")
    }
	
	// test case for moveBundleService.assetCompletionCount( def moveBundleId, def transitionId )
	@Ignore
	void testassetCompletionCount() {
		moveBundleService.jdbcTemplate = jdbcTemplate
		
		assertEquals 1, moveBundleService.assetCompletionCount("27","60")
		
		assertEquals 1, moveBundleService.assetCompletionCount("27","50")
    }
	
	// test case for moveBundleService.getActualTimes( def moveBundleId, def transitionId )
	@Ignore
	void testgetActualTimes() {
		moveBundleService.jdbcTemplate = jdbcTemplate
		assertEquals false, ! moveBundleService.getActualTimes("27","60").started
    }
	
	// test case for moveBundleService.getAllDashboardSteps( def moveBundleId )
	@Ignore
	void testgetAllDashboardSteps() {
		moveBundleService.jdbcTemplate = jdbcTemplate
		
		def moveBundle = new MoveBundle(name:"Test Bundle")
		moveBundle.project.id = 24
		moveBundle.moveEvent.id = 1
		
		assertEquals true, moveBundleService.getAllDashboardSteps( moveBundle ).dashboardSteps.step.contains(['id':60, 'label':'Unracking', 'name':'Unracked'])
		
		assertEquals false, moveBundleService.getAllDashboardSteps( moveBundle ).dashboardSteps.step.contains(['id':50, 'label':'Unracking', 'name':'Unracking'])
    }
	
	// method to test the MoveEvent Transition Times summary report details
	@Ignore
	void testgetMoveEventSummaryResults() {
		moveBundleService.jdbcTemplate = jdbcTemplate
		def summaryResults = moveBundleService.getMoveEventSummaryResults( 1 )
		assetEquals 5,summaryResults.size()
	}
	
	// method to test the MoveEvent Transition Times detailed report details
	@Ignore
	void testgetMoveEventDetailedResults() {
		moveBundleService.jdbcTemplate = jdbcTemplate
		def detailedResults = moveBundleService.getMoveEventDetailedResults( 1 )
		assetEquals 16,detailedResults.size()
	}
}
