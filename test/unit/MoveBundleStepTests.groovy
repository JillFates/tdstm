import groovy.time.TimeCategory
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit Test class to test the Domain class MoveBundleStep
 */
@TestFor(MoveBundleStep)
class MoveBundleStepTests extends Specification {

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
        expect:
		  mbs.getPlanDuration() == 7200 //Should be 2 hours (seconds)
    }

    void testActualDuration() {
		initTest()
		when: "step 1"
		  print mbs.toString() + "\n"
        then:
		  mbs.getActualDuration( now ) == 7200 //Should be 2 hours (seconds)

        when: "step 2"
		  mbs.actualCompletionTime = null
        then:
		  mbs.getActualDuration( now ) == 3600 //Started an hour ago and not done

        when: "step 3"
		  mbs.actualStartTime = null
        then:
		  mbs.getActualDuration( now ) == 0 //Hasn't started

    }


	void testIsCompleted () {
		initTest()
		when: "step 1"
        then:
		    mbs.isCompleted() //Step should be completed

        when: "step 2"
		    mbs.actualCompletionTime = null
        then:
		    !mbs.isCompleted() //Step should be incompleted
	}
}
