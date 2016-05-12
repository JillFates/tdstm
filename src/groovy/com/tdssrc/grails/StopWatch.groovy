package com.tdssrc.grails

import groovy.time.TimeCategory
import groovy.time.TimeDuration

/**
 * Stop watch to measure the duration of execution code
 * Usage:
 *    def sw = new StopWatch()  //this starts the Clock
 *    sw.start()      // this restarts the Clock and the startTime
 *    sw.lap()    // sets the date when this function was called and returns the duration from the last call (interval from call to call)
 *
 * @author @tavo_luna
 */
class StopWatch{
  private Date startTime
  private Date lastLapTime

  private TimeDuration lap = new TimeDuration(0,0,0,0)

  public StopWatch(){
    start()
  }

  /**
   * Restart the time of the chronometer
   */
  public void start(){
    startTime = new Date()
    lastLapTime = startTime
  }

  /**
   * set a Lapse between the last call to lap or start, time lapse between lap() calls
   */
  public TimeDuration lap(){
    Date currentDate = new Date()
    use (TimeCategory) {
      lap = currentDate - lastLapTime
    }
    lastLapTime = currentDate
    return lap
  }

  /**
   * Get last Time duration of the last lap() call
   */
  public TimeDuration getLastLap(){
    return lap
  }

  /**
   * get Duration between the start of the chronometer and the last call to lap()
   */
  public TimeDuration getSinceStart(){
    use (TimeCategory) {
      return lastLapTime - startTime
    }
  }

  /**
   * Time when the StopWatch was created or the las tall to start()
   */
  public Date getStartTime(){
    return startTime
  }

  /**
   * date of the last call to lap() or 'startTime' in case of never been called.
   */
  public Date getLastLapTime(){
    return lastLapTime
  }
}