package com.tdssrc.grails

import groovy.time.TimeDuration
import spock.lang.Ignore
import spock.lang.Specification

class StopWatchTests extends Specification {

	private static final String tag = 'foo'

	void 'Test lifecycle of the StopWatch Class'() {
		when:
		StopWatch stopWatch = new StopWatch()
		Date started = stopWatch.begin()
		Date startedTag = stopWatch.begin(tag)
		then:
		stopWatch.hasTag()
		stopWatch.hasTag(tag)
		stopWatch.getStartTime().getTime() == started.getTime()
		stopWatch.getStartTime(tag).getTime() == startedTag.getTime()

		// Test that the clocks can be terminated
		when:
		stopWatch.endDuration()
		stopWatch.endDuration(tag)
		then:
		!stopWatch.hasTag()
		!stopWatch.hasTag(tag)
	}

	//TODO: oluna ignored due to a long watch period, check later
	@Ignore
	void 'Exercise the StopWatch Class'() {
		when:
		StopWatch stopWatch = new StopWatch()
		Date started = stopWatch.begin()
		Date startedTag = stopWatch.begin(tag)
		sleep(50)
		TimeDuration lap = stopWatch.lap()
		TimeDuration lapTag = stopWatch.lap(tag)

		then:
		lap.toMilliseconds() >= 50 && lap.toMilliseconds() <= 500
		lapTag.toMilliseconds() >= 50 && lapTag.toMilliseconds() <= 500

		when:
		sleep(500)
		lap = stopWatch.lap()
		lapTag = stopWatch.lap(tag)
		TimeDuration lastLap = stopWatch.getLastLap()
		TimeDuration lastLapTag = stopWatch.getLastLap(tag)

		then:
		lastLap == lap
		lastLapTag == lapTag
		lastLap.toMilliseconds() >= 500 && lastLap.toMilliseconds() < 1000
		lastLapTag.toMilliseconds() >= 500 && lastLapTag.toMilliseconds() < 1000

		when:
		// Check out the getSinceStart
		TimeDuration since = stopWatch.getSinceStart()
		TimeDuration sinceTag = stopWatch.getSinceStart(tag)

		then:
		since.toMilliseconds() >= 500 && since.toMilliseconds() < 1000
		sinceTag.toMilliseconds() >= 500 && sinceTag.toMilliseconds() < 1000

		when:
		sleep(1000)
		TimeDuration since2 = stopWatch.getSinceStart()
		TimeDuration since2Tag = stopWatch.getSinceStart(tag)

		then:
		since2 > since
		since2Tag > sinceTag
		since2.toMilliseconds() > 1000 && since2.toMilliseconds() < 3000
		since2Tag.toMilliseconds() > 1000 && since2Tag.toMilliseconds() < 3000
	}

	void 'Test Time Lap'() {
		setup:
		def stopWatch = new StopWatch()
		when: "sleep 2s "
		sleep(2000)
		then: 'stopwatch == 2s'
		stopWatch.lap()
		stopWatch.lastLap.seconds == 2
	}

	void 'Test Multiple Time Lap'() {
		setup:
		int max = 5
		long startT = System.currentTimeMillis()
		def stopWatch = new StopWatch()
		def rand = new Random()

		int totalSecs = 0
		List<Integer> steps = (1..max).inject([]) { result, i ->
			int secs = rand.nextInt(max + 1)
			totalSecs += secs
			result << secs
		}

		expect: "that all steps last the computed interval"
		steps.each { int secs ->
			sleep(secs * 1000)
			stopWatch.lap()
			assert stopWatch.lastLap.seconds == secs
		}

		long stopT = System.currentTimeMillis()

		and: "the duration of the Stopwatch is the same that the time consumed for the test"
		stopWatch.sinceStart.seconds == (stopT - startT).intdiv(1000).intValue()
	}
}
