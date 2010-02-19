import org.codehaus.groovy.runtime.TimeCategory

/** 
 * Unit Test class to test the Domain class MoveBundleStep
 */
class MoveBundleStepTests extends GroovyTestCase {
	def mbs 
	def now
	
	void initTest() {
	 	now = new Date()
		mbs 
		use (TimeCategory) {
		 	mbs = new MoveBundleStep(planStartTime: now - 1.hour, planCompletionTime: now + 1.hour,
		 		actualStartTime: now - 1.hour, actualCompletionTime: now + 1.hour)
		}
	}
	
    void testPlanDuration() {
		initTest()
		assertEquals "Should be 2 hours (seconds)", 7200, mbs.getPlanDuration()
    }

    void testActualDuration() {
		initTest()
		
		print mbs.toString() + "\n"
		assertEquals "Completed in 2 hours", 7200, mbs.getActualDuration( now )
				
		mbs.actualCompletionTime = null
		assertEquals "Started an hour ago and not done", 3600, mbs.getActualDuration( now )
		
		mbs.actualStartTime = null
		assertEquals "Hasn't started", 0, mbs.getActualDuration( now )
		
    }


	void testIsCompleted () {
		initTest()
		
		assertTrue "Step should be completed", mbs.isCompleted()
		
		mbs.actualCompletionTime = null
		assertFalse "Step should be incompleted", mbs.isCompleted()
		
	}
}
