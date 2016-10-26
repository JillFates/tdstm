import net.transitionmanager.service.StateEngineService
import org.junit.Ignore
import org.springframework.jdbc.core.JdbcTemplate
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StateEngineService)
class StateEngineServiceTests extends Specification {

	def stateEngineService =  new StateEngineService()

	def jdbcTemplate = new JdbcTemplate( getDatasource() )

	//test for def getTasks( String process, String swimlane, String currentState )
	@Ignore //if this test is enabled again expect should be added
    void testgetTasks() {
    	stateEngineService.jdbcTemplate = jdbcTemplate
		stateEngineService.afterPropertiesSet()
		assertEquals stateEngineService.getTasks("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ["Release", "Hold", "PoweredDown"]
    }

	// test for def getTasksExtended( String process, String swimlane, String currentState )
	@Ignore //if this test is enabled again expect should be added
	void testgetTasksExtended() {
		assertEquals stateEngineService.getTasksExtended("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ["Release", "Hold", "PoweredDown"]
	}
	//test for def getFlags( String process, String swimlane, String currentState, String toState )
	@Ignore //if this test is enabled again expect should be added
	void testgetFlags() {
		assertEquals "skipped", stateEngineService.getFlags("STD_PROCESS","ROLE_WF_MANAGER", "Ready", "Release")
	}

	// test for def getPredecessor( String process, Integer transitionId )
	@Ignore //if this test is enabled again expect should be added
	void testgetPredecessor(){

		assertEquals "50", stateEngineService.getPredecessor("STD_PROCESS",60)

		assertEquals false, stateEngineService.getPredecessor("STD_PROCESS",50)
	}

	// test for def getDashboardLabel( String process, Integer transitionId )
	@Ignore //if this test is enabled again expect should be added
	void testgetDashboardLabel(){

		assertEquals "Unracking", stateEngineService.getDashboardLabel("STD_PROCESS",60)

		assertEquals "Unracking", stateEngineService.getDashboardLabel("STD_PROCESS",50)
	}

	// test for def getDashboardLabel( String process, Integer transitionId )
	@Ignore //if this test is enabled again expect should be added
	void testgetDashboardSteps(){

		assertEquals true, stateEngineService.getDashboardSteps("STD_PROCESS").contains(['id':60, 'label':'Unracking', 'name':'Unracked'])

		assertEquals false, stateEngineService.getDashboardSteps("STD_PROCESS").contains(['id':50, 'label':'Unracking', 'name':'Unracking'])
	}
}
