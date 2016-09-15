import groovy.time.TimeCategory
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit Test class to test the Domain class StepSnapshot
 */
@TestFor(StepSnapshot)
class StepSnapshotTests extends Specification {

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

        when: "step 1"
		   ss.tasksCount = 60
        then:
		   120 == ss.planTaskPace //Takes 2 minutes per task

        when: "step 2"
		   ss.tasksCount = 240
        then:
		   30 == ss.planTaskPace //1/2 minute per task
    }

	void testGetActualTaskPace () {
		initTest()

        when: "step 1"
		   ss.duration = 3600	// 1 hr
		then:
		   60 == ss.actualTaskPace //Completed 60 in 1 hour so 1 min (sec)

        when: "step 2"
		   ss.tasksCompleted = 0
        then:
		   0 == ss.actualTaskPace //Nothing has been completed
	}

	void testGetProjectedTimeRemaining () {
		initTest()

        when: "step 1"
        then:
		   3600 == ss.projectedTimeRemaining //Should complete in 1 hour

        when: "step 1"
		   ss.tasksCompleted = ss.tasksCount
        then:
		   0 == ss.projectedTimeRemaining //Should be all done

	}
}
