import grails.test.mixin.Mock
import groovy.time.TimeCategory
import net.transitionmanager.project.MoveBundleStep
import spock.lang.Specification

@Mock(MoveBundleStep)
class MoveBundleStepTests extends Specification {

	private MoveBundleStep mbs
	private Date now = new Date()

	void setup() {
		use(TimeCategory) {
			mbs = new MoveBundleStep(
					planStartTime: now - 1.hour, planCompletionTime: now + 1.hour,
					actualStartTime: now - 1.hour, actualCompletionTime: now + 1.hour)
		}
	}

	void testPlanDuration() {
		expect:
		mbs.getPlanDuration() == 7200 //Should be 2 hours (seconds)
	}

	void testActualDuration() {
		expect:
		mbs.getActualDuration(now) == 7200 //Should be 2 hours (seconds)

		when:
		mbs.actualCompletionTime = null
		then:
		mbs.getActualDuration(now) == 3600 //Started an hour ago and not done

		when:
		mbs.actualStartTime = null
		then:
		mbs.getActualDuration(now) == 0 //Hasn't started
	}

	void testIsCompleted() {
		expect:
		mbs.isCompleted() //Step should be completed

		when:
		mbs.actualCompletionTime = null
		then:
		!mbs.isCompleted() //Step should be incompleted
	}
}
