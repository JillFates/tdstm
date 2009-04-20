class StateEngineServiceTests extends GroovyTestCase {
def stateEngineService =  new StateEngineService()
	
	//test for def getTasks( String process, String swimlane, String currentState )
    void testgetTasks() {
		stateEngineService.afterPropertiesSet()
		assertEquals stateEngineService.getTasks("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ["Release", "Hold", "PoweredDown"]
    }
	
	// test for def getTasksExtended( String process, String swimlane, String currentState )
	void testgetTasksExtended() {
		assertEquals stateEngineService.getTasksExtended("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ["Release", "Hold", "PoweredDown"] 
	}
	//test for def getFlags( String process, String swimlane, String currentState, String toState )
	void testgetFlags() {
		assertEquals "skipped", stateEngineService.getFlags("STD_PROCESS","ROLE_WF_MANAGER", "Ready", "Release")  
	}
}
