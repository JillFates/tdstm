import org.codehaus.groovy.runtime.TimeCategory

/** 
 * Unit Test class to test the Domain class StepSnapshot
 */
class StepSnapshotTests extends GroovyTestCase {
	def ss 
	
	void initTest() {
		def now = new Date()
		def mbs 
		use (TimeCategory) {
		 	mbs = new MoveBundleStep(label:"Testing Code", transitionId:666, planStartTime: now - 1.hour, planCompletionTime: now + 1.hour )
		}
		ss = new StepSnapshot(moveBundleStep:mbs, tasksCount:120, tasksCompleted:60, duration:3600, planDelta:0, dialIndicator:0)
		
		print "Testing with: ${mbs}\n"
	}
	
    void testPlanTaskPace() {
		initTest()
		assertEquals "Takes 1 minute per task (seconds)", 60, ss.planTaskPace

		ss.tasksCount = 60
		assertEquals "Takes 2 minutes per task", 120, ss.planTaskPace
		
		ss.tasksCount = 240
		assertEquals "1/2 minute per task", 30, ss.planTaskPace
    }

	void testGetActualTaskPace () {
		initTest()
		
		ss.duration = 3600	// 1 hr
		
		assertEquals "Completed 60 in 1 hour so 1 min (sec)", 60, ss.actualTaskPace
		
		ss.tasksCompleted = 0
		assertEquals "Nothing has been completed", 0, ss.actualTaskPace
	}
	
	void testGetProjectedTimeRemaining () {
		initTest()
		
		assertEquals "Should complete in 1 hour", 3600, ss.projectedTimeRemaining

		ss.tasksCompleted = ss.tasksCount
		assertEquals "Should be all done", 0, ss.projectedTimeRemaining
			
	}
}
