import grails.testing.gorm.DataTest
import groovy.time.TimeCategory
import net.transitionmanager.project.MoveBundleStep
import net.transitionmanager.project.StepSnapshot
import spock.lang.Specification

class StepSnapshotTests extends Specification implements DataTest{

	private StepSnapshot ss

	void setupSpec(){
		mockDomains StepSnapshot
	}

	void setup() {
		def now = new Date()
		MoveBundleStep mbs
		use (TimeCategory) {
			mbs = new MoveBundleStep(label: "Testing Code", transitionId: 666, planStartTime: now - 1.hour,
			                         planCompletionTime: now + 1.hour)
		}
		ss = new StepSnapshot(moveBundleStep: mbs, tasksCount: 120, tasksCompleted: 60, duration: 3600,
		                      planDelta: 0, dialIndicator: 0)
	}

	void testPlanTaskPace() {
		expect: "Takes 1 minute per task (seconds)"
		60 == ss.planTaskPace

		when: "step 1"
		ss.tasksCount = 60
		then:
		120 == ss.planTaskPace //Takes 2 minutes per task

		when: "step 2"
		ss.tasksCount = 240
		then:
		30 == ss.planTaskPace //1/2 minute per task
	}

	void testGetActualTaskPace() {
		when: "step 1"
		ss.duration = 3600   // 1 hr
		then:
		60 == ss.actualTaskPace //Completed 60 in 1 hour so 1 min (sec)

		when: "step 2"
		ss.tasksCompleted = 0
		then:
		0 == ss.actualTaskPace //Nothing has been completed
	}

	void testGetProjectedTimeRemaining() {
		expect: "step 1"
		3600 == ss.projectedTimeRemaining //Should complete in 1 hour

		when: "step 1"
		ss.tasksCompleted = ss.tasksCount
		then:
		0 == ss.projectedTimeRemaining //Should be all done
	}
}
