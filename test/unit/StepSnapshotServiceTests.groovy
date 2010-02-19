// import groovy.time.TimeCategory // 
import org.codehaus.groovy.runtime.TimeCategory


class StepSnapshotServiceTests extends GroovyTestCase {

	def stepSnapshotService =  new StepSnapshotService()
	
    void testCalcDialIndicator() {
		assertEquals stepSnapshotService.calcDialIndicator( 100, 0), 50
		assertEquals stepSnapshotService.calcDialIndicator( 100, -40), 82
		assertEquals stepSnapshotService.calcDialIndicator( 100, 26), 24
		assertEquals stepSnapshotService.calcDialIndicator( 100, -100), 100		
    }

	void testCalcPlanDelta() {
		def mbs = new MoveBundleStep()
		def ss = new StepSnapshot()
		
		use ( TimeCategory ) {
			// Initialize objects as necessary
			def now = new Date()
			mbs.planStartTime = now - 1.hour 
			mbs.planCompletionTime = now + 1.hour 
			ss.moveBundleStep = mbs
			
			ss.tasksCount = 100
			ss.tasksCompleted = 50
		
			// half way through and 50% so delta should be zero
			assertEquals stepSnapshotService.calcPlanDelta( now, ss ), 0
		}	
			
	}
}
