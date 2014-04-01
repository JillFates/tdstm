// import groovy.time.TimeCategory // 
import org.codehaus.groovy.runtime.TimeCategory
import org.junit.Ignore;


class StepSnapshotServiceTests extends GroovyTestCase {

	def stepSnapshotService =  new StepSnapshotService()
	
	/**
	 * Validate the calcDialIndicator method which should return values between 0 and 100 for various
	 * durations and adjustments to durations.
	 */
	@Ignore
    void testCalcDialIndicator() {
		assertEquals stepSnapshotService.calcDialIndicator( 100, 0), 50
		assertEquals stepSnapshotService.calcDialIndicator( 100, -40), 82
		assertEquals stepSnapshotService.calcDialIndicator( 100, 26), 24
		assertEquals stepSnapshotService.calcDialIndicator( 100, -100), 100		
    }

	/**
	 * Validates the calcPlanDelta method which determine the delta time period that a Step will
	 * complete in.
	 */
	@Ignore
	void testCalcPlanDelta() {
		def mbs = new MoveBundleStep()
		def ss = new StepSnapshot()
		
		use ( TimeCategory ) {
			// Initialize objects as necessary
			def now = new Date()
			mbs.planStartTime = now - 1.hour 
			mbs.planCompletionTime = now + 1.hour
			mbs.actualStartTime = mbs.planStartTime
			ss.moveBundleStep = mbs
			ss.duration = 3600
			
			ss.tasksCount = 120
			ss.tasksCompleted = 60

			assertEquals "50% completed in middle of step, delta should be zero", 0, stepSnapshotService.calcProjectedDelta( ss, now )

			ss.tasksCompleted = 0
			assertEquals "0 completed in middle of step, delta should 1 hour", 3600, stepSnapshotService.calcProjectedDelta( ss, now )

		}	
			
	}
}
