package com.tdssrc.grails

import grails.test.*
import spock.lang.Specification

class StopWatchTests extends Specification {
  def 'Test Time Lap'(){
    setup:
      def stopWatch = new StopWatch()
    when: "sleep 2s "
      sleep(2000)      
    then: 'stopwatch == 2s'
      stopWatch.lap()
      stopWatch.lastLap.seconds == 2 
  }

  def 'Test Multiple Time Lap'(){
    setup:
      def max = 5 
      def startT = System.currentTimeMillis()
      def stopWatch = new StopWatch()
      def rand = new Random()
    
      def totalSecs = 0
      def steps = (1..max).inject([]){result, i -> 
        def secs = rand.nextInt(max+1)
        totalSecs += secs  
        result << secs
      }

    expect: "that all steps last the computed interval"
      steps.each{ secs ->
        sleep(secs * 1000) 
        stopWatch.lap()       
        assert stopWatch.lastLap.seconds == secs
      }

      def stopT = System.currentTimeMillis()

    and: "the duration of the Stopwatch is the same that the time consumed for the test"
      stopWatch.sinceStart.seconds == (stopT - startT).intdiv(1000)

  }

}