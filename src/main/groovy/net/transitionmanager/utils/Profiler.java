package net.transitionmanager.utils;

import com.tdssrc.grails.StopWatch;
import groovy.time.TimeDuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;

import static net.transitionmanager.utils.Profiler.LOG_TYPE.*;

/**
 * Profiler class to measure and log information of execution in different parts of an application
 *
 * The advance profiler Methods that only execute it the Profiler is creater with the doProfile parameter set to true
 * 	begin(tag [, message][, ArrayList])   : Sets a Mark 'TAG' of the begin of a profile, and output the message
 * 	end(tag [, message][, ArrayList])	  : output the message and Gets the duration of the block since begin(TAG) and Removes the Mark 'TAG'
 * 	lap(tag [, message][, ArrayList])	  : output the message with duration since the last call or the begin(TAG)
 * 	lapRoot(message[, ArrayList])		  : lapRoot works with the duration of the profile object default tag
 * 	getLapDuration([tag])				  : returns the Duration since the last call of any lap(TAG) function or the begin(TAG)
 * 	getLastLapDuration([tag])			  : returns the las duration recorded from the previous version
 *
 * All Methods Contains leg4J specific calls according than the LogLevel that are evauated regardless of the value of doProfile
 * 	i.e.  beginInfo, beginError, endInfo, endTrace, lapInfo, lapDebug, lapRootWarn, etc.
 *
 * 	Sample of usage:
 *
 * 	  def someFunction(){
 * 	      ...
 * 	      Profiler profiler = new Profiler(shouldAdvanceProfile, "PROFNAME")
 * 	      profiler.beginInfo("MAIN")    			//Always log utils.Profiler  - PROFNAME BEGIN [MAIN]
 * 	      ...
 * 	      	profiler.lap("MAIN", "value %d", [5]) //this only logs when "shouldAdvanceProfile" is true
 * 	      												//	PROFNAME LAP [MAIN] (nnn seconds) value 5
 * 	  			profiler.begin("INNER", "SOME extra data") //only logs when "shouldAdvanceProfile" is true
 * 	  												// utils.Profiler  - PROFNAME BEGIN [INNER] SOME extra data
 * 	  			...
 * 	  				profiler.lap("INNER", "value %d", [7]) //this only logs when "shouldAdvanceProfile" is true
 * 	      												//	PROFNAME LAP [INNER] (nnn seconds) value 7
 * 	  			...
 * 	  			profiler.end("INNER", "SOME extra finish data")
 * 	  												// utils.Profiler  - PROFNAME END [INNER] SOME extra finish data
 * 	      ...
 * 	      profiler.endInfo("MAIN", "Finished")      //Always log utils.Profiler  - PROFNAME END [MAIN] (nnn seconds) Finished
 * 	  }
 *
 * OTHER USAGE IS TO PROFILE WHEN A TIME RELATED CONDITION IS MEET:
 * 	if(profiler.getLapDuration("Applications").toMilliseconds() > 10000L){
 * 		log.warn("Application Export is taking to long!!: " + profiler.getLastLapDuration("Applications"))
 * 	}
 *
 * 	NOTE: This profiler Relies on Log4J INFO level set to the Class, you may need to change your Log4J config accordly
 * 	log4j = {
 * 		info 'net.transitionmanager.utils.Profiler'
 * 		...
 * 	}
 *
 * @author @tavo_luna
 */
public class Profiler  {
	enum LOG_TYPE {INFO, DEBUG, ERROR, FATAL, TRACE, WARN, SILENT}
	static public final String KEY_NAME = "ADV_PROFILER";
	static private final Logger LOG = Logger.getLogger(Profiler.class);

	/**
	 * Function factory to create instances
	 * @param doProfile should the Advance profiling log?
	 * @param name Name prefix for the profiler instance
	 * @return a Profiler Instance
	 */
	static public Profiler create(boolean doProfile, String name){
		return new Profiler(doProfile, name);
	}

	//Instance
	private boolean doProfile = false;
	private String name = "";
	private StopWatch stopWatch = new StopWatch();

	/**
	 * Main Constructor
	 * @param doProfile should the Advance profiling log?
	 * @param name Name prefix for the profiler instance
	 */
	public Profiler(boolean doProfile, String name){
		this.doProfile = doProfile;
		this.name = name;
	}

	// Public Actions ////////////////////////////////////////////////////

	public void begin(String tag){
		begin(tag, null, null);
	}
	public void begin(String tag, String msg){
		begin(tag, msg, null);
	}
	public void begin(String tag, String msg, ArrayList args){
		if(doProfile){
			beginInfo(tag, msg, args);
		}
	}

	public void beginInfo(String tag){
		beginInfo(tag, null, null);
	}
	public void beginInfo(String tag, String msg){
		beginInfo(tag, msg, null);
	}
	public void beginInfo(String tag, String msg, ArrayList args){
		begin(INFO, tag, msg, args);
	}

	public void beginWarn(String tag){
		beginWarn(tag, null, null);
	}
	public void beginWarn(String tag, String msg){
		beginWarn(tag, msg, null);
	}
	public void beginWarn(String tag, String msg, ArrayList args){
		begin(WARN, tag, msg, args);
	}

	public void beginError(String tag){
		beginError(tag, null, null);
	}
	public void beginError(String tag, String msg){
		beginError(tag, msg, null);
	}
	public void beginError(String tag, String msg, ArrayList args){
		begin(ERROR, tag, msg, args);
	}

	public void beginFatal(String tag){
		beginFatal(tag, null, null);
	}
	public void beginFatal(String tag, String msg){
		beginFatal(tag, msg, null);
	}
	public void beginFatal(String tag, String msg, ArrayList args){
		begin(FATAL, tag, msg, args);
	}

	public void beginDebug(String tag){
		beginDebug(tag, null, null);
	}
	public void beginDebug(String tag, String msg){
		beginDebug(tag, msg, null);
	}
	public void beginDebug(String tag, String msg, ArrayList args){
		begin(DEBUG, tag, msg, args);
	}

	public void beginTrace(String tag){
		beginTrace(tag, null, null);
	}
	public void beginTrace(String tag, String msg){
		beginTrace(tag, msg, null);
	}
	public void beginTrace(String tag, String msg, ArrayList args){
		begin(TRACE, tag, msg, args);
	}

	/**
	 * Used to being a silent block for measuring without logging
	 */
	public void beginSilent(String tag){
		beginSilent(tag, null, null);
	}
	public void beginSilent(String tag, String msg){
		beginSilent(tag, msg, null);
	}
	public void beginSilent(String tag, String msg, ArrayList args){
		begin(SILENT, tag, msg, args);
	}

	public void end(String tag){
		end(tag, null, null);
	}

	public void end(String tag, String msg, ArrayList args){
		if(doProfile){
			endInfo(tag, msg, args);
		}
	}

	public void endInfo(String tag){
		endInfo(tag, null, null);
	}

	public void endInfo(String tag, String msg){
		endInfo(tag, msg, null);
	}

	public void endInfo(String tag, String msg, ArrayList args){
		end(INFO, tag, msg, args);
	}

	public void endWarn(String tag){
		endWarn(tag, null, null);
	}

	public void endWarn(String tag, String msg){
		endWarn(tag, msg, null);
	}

	public void endWarn(String tag, String msg, ArrayList args){
		end(WARN, tag, msg, args);
	}

	public void endError(String tag){
		endError(tag, null, null);
	}

	public void endError(String tag, String msg){
		endError(tag, msg, null);
	}

	public void endError(String tag, String msg, ArrayList args){
		end(ERROR, tag, msg, args);
	}

	public void endDebug(String tag){
		endDebug(tag, null, null);
	}
	public void endDebug(String tag, String msg){
		endDebug(tag, msg, null);
	}
	public void endDebug(String tag, String msg, ArrayList args){
		end(DEBUG, tag, msg, args);
	}

	public void endTrace(String tag){
		endTrace(tag, null, null);
	}
	public void endTrace(String tag, String msg){
		endTrace(tag, msg, null);
	}
	public void endTrace(String tag, String msg, ArrayList args){
		end(TRACE, tag, msg, args);
	}

	public void endFatal(String tag){
		endFatal(tag, null, null);
	}
	public void endFatal(String tag, String msg){
		endFatal(tag, msg, null);
	}
	public void endFatal(String tag, String msg, ArrayList args){
		end(FATAL, tag, msg, args);
	}

	/**
	 * Used to terminate a block without any logging
	 * @param tag - the label/tag to reference the block by
	 */
	public void endSilent(String tag){
		endSilent(tag, null, null);
	}
	public void endSilent(String tag, String msg){
		endSilent(tag, msg, null);
	}
	public void endSilent(String tag, String msg, ArrayList args){
		end(SILENT, tag, msg, args);
	}

	/**
	 * Used to reset a named stopwatch
	 * @param tag - the stopwatch tag name to reset
	 */
	public void lapReset(String tag) {
		stopWatch.begin(tag);
	}

	private void lap(LOG_TYPE ltype, String tag, String msg, ArrayList args){
		TimeDuration duration;
		StringBuilder sb = new StringBuilder("[");
		if (tag != null) {
			sb.append(tag);
			// text += tag;
			duration = stopWatch.lap(tag);
		} else {
			sb.append("ROOT");
			// text += "ROOT";
			duration = stopWatch.lap();
		}

		// Milliseconds sometimes toString to '0' but we want finer granularity
		String duraStr = duration.toString();
		if (duraStr.compareTo("0") == 0) {
			duraStr += " msec";
		}

		// text += "] (" + duraStr + ")";
		sb.append("] (").append(duraStr).append(")");
		if(StringUtils.isNotBlank(msg)) {
			// text += ": " + msg;
			sb.append(": ").append(msg);
		}
		// logPrefix(ltype, "LAP", text, args);
		logPrefix(ltype, "LAP", sb.toString(), args);
	}

	/**
	 * Used to log lap information at the INFO level if the profiler is enabled otherwise nothing logged
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lap(String tag){
		lap(tag, null, null);
	}
	public void lap(String tag, String msg){
		lap(tag, msg, null);
	}

	public void lap(String tag, String msg, ArrayList args){
		if(doProfile){
			lapInfo(tag, msg, args);
		}
	}

	/**
	 * Used to log lap information at the INFO level regardless of if the profiler is enabled
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lapInfo(String tag){
		lapInfo(tag, null, null);
	}
	public void lapInfo(String tag, String msg){
		lapInfo(tag, msg, null);
	}
	public void lapInfo(String tag, String msg, ArrayList args){
		lap(INFO, tag, msg, args);
	}

	/**
	 * Used to log lap information at the DEBUG level regardless of if the profiler is enabled
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lapDebug(String tag){
		lapDebug(tag, null, null);
	}
	public void lapDebug(String tag, String msg){
		lapDebug(tag, msg, null);
	}
	public void lapDebug(String tag, String msg, ArrayList args){
		lap(DEBUG, tag, msg, args);
	}

	/**
	 * Used to log lap information at the TRACE level regardless of if the profiler is enabled
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lapTrace(String tag){
		lapTrace(tag, null, null);
	}
	public void lapTrace(String tag, String msg){
		lapTrace(tag, msg, null);
	}
	public void lapTrace(String tag, String msg, ArrayList args){
		logPrefix(TRACE, tag, msg, args);
	}

	/**
	 * Used to log lap information at the WARN level regardless of if the profiler is enabled
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lapWarn(String tag){
		lapWarn(tag, null, null);
	}
	public void lapWarn(String tag, String msg){
		lapWarn(tag, msg, null);
	}
	public void lapWarn(String tag, String msg, ArrayList args){
		logPrefix(WARN, tag, msg, args);
	}

	/**
	 * Used to log lap information at the ERROR level regardless of if the profiler is enabled
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lapError(String tag){
		lapError(tag, null, null);
	}
	public void lapError(String tag, String msg){
		lapError(tag, msg, null);
	}
	public void lapError(String tag, String msg, ArrayList args){
		logPrefix(ERROR, tag, msg, args);
	}

	/**
	 * Used to log lap information at the FATAL level regardless of if the profiler is enabled
	 * @param tag - the stopwatch tag name to log info for
	 */
	public void lapFatal(String tag){
		lapFatal(tag, null, null);
	}
	public void lapFatal(String tag, String msg){
		lapFatal(tag, msg, null);
	}
	public void lapFatal(String tag, String msg, ArrayList args){
		logPrefix(FATAL, tag, msg, args);
	}

	public void lapRoot(String msg){
		lapRoot(msg, null);
	}
	/**
	 * Lap ROOT is used to log without a TAG (using the global StopWatch)
	 * @param msg
	 * @param args
	 */
	public void lapRoot(String msg, ArrayList args){
		if(doProfile){
			lapRootInfo(msg, args);
		}
	}

	public void lapRootInfo(String msg){
		lapRootInfo(msg, null);
	}
	public void lapRootInfo(String msg, ArrayList args){
		lap(INFO, null, msg, args);
	}

	public void lapRootDebug(String msg){
		lapRootDebug(msg, null);
	}
	public void lapRootDebug(String msg, ArrayList args){
		lap(DEBUG, null, msg, args);
	}

	public void lapRootTrace(String msg){
		lapRootTrace(msg, null);
	}
	public void lapRootTrace(String msg, ArrayList args){
		lap(TRACE, null, msg, args);
	}

	public void lapRootWarn(String msg){
		lapRootWarn(msg, null);
	}
	public void lapRootWarn(String msg, ArrayList args){
		lap(WARN, null, msg, args);
	}

	public void lapRootError(String msg){
		lapRootError(msg, null);
	}
	public void lapRootError(String msg, ArrayList args){
		lap(ERROR, null, msg, args);
	}

	public void lapRootFatal(String msg){
		lapRootFatal(msg, null);
	}
	public void lapRootFatal(String msg, ArrayList args){
		lap(FATAL, null, msg, args);
	}

	public void logSinceStart(String msg){
		if(doProfile){
			log(INFO, "Since Start (" + stopWatch.getSinceStart() + ") " + msg);
		}
	}

	/**
	 * Used to get the TimeDuration of when a given clock (tag) was started
	 * @param tag - the tag to look up
	 * @return the TimeDuration since the timer was started
	 */
	public TimeDuration getSinceStart(String tag) {
		return stopWatch.getSinceStart(tag);
	}

	public TimeDuration getSinceStart() {
		return stopWatch.getSinceStart();
	}

	/**
	 * get the Lap duration of the ROOT of the Profiler
	 * @return Duration since the last call of ROOT
	 */
	public TimeDuration getLapDuration(){
		return stopWatch.lap();
	}

	/**
	 * get the Lap duration of a tagged Block
	 * @param tag Tag that flags a Begin/End Block
	 * @return Duration since the last call of lap(tag)
	 */
	public TimeDuration getLapDuration(String tag){
		return (tag!=null) ? stopWatch.lap(tag) : stopWatch.lap();
	}

	/**
	 * get the last Lap duration of the ROOT of the Profiler
	 * @return Duration of the last call of lap()
	 */
	public TimeDuration getLastLapDuration(){
		return stopWatch.getLastLap();
	}

	/**
	 * get the last Lap duration of a tagged Block
	 * @param tag Tag that flags a Begin/End Block
	 * @return Duration of the last call of lap(tag)
	 */
	public TimeDuration getLastLapDuration(String tag) {
		return (tag!=null) ? stopWatch.getLastLap(tag) : stopWatch.getLastLap();
	}

	/**
	 * Used to log a message directly as part of the Profiler without referencing a lap tag
	 * @param lType - the logger level
	 * @param msg - the message to log
	 */
	public void log(LOG_TYPE lType, String msg){
		log(lType, msg, null);
	}

	/**
	 * Used to log a message directly as part of the Profiler without referencing a lap tag
	 * @param lType - the logger level
	 * @param msg - the message to log
	 * @param args - a list of parameters for a parameterized message
	 */
	public void log(LOG_TYPE lType, String msg, ArrayList args) {
		if (lType != SILENT) {
			// TODO : JPM 6/2016 : the log should be calling the logger to parameterize vs ALWAY formatting the string
			String formattedText = (args != null) ? String.format(msg, args.toArray()) : msg;
			formattedText = name + "= " + formattedText;
			switch(lType){
				case DEBUG :
					LOG.debug(formattedText);
					break;
				case WARN :
					LOG.warn(formattedText);
					break;
				case ERROR:
					LOG.error(formattedText);
					break;
				case FATAL:
					LOG.fatal(formattedText);
					break;
				case TRACE:
					LOG.trace(formattedText);
					break;
				case INFO :
				default:
					LOG.info(formattedText);
			}
		}
	}

	// ---------------------------------
	// PRIVATE HELPER METHODS
	// ---------------------------------

	private void logPrefix(LOG_TYPE lType, String prefix, String msg, ArrayList args) {
		log(lType, prefix + " " + msg, args);
	}

	private void begin(LOG_TYPE lType, String tag, String msg, ArrayList args){
		String text = "["+tag+"]";
		if(StringUtils.isNotBlank(msg)) {
			text += ": " + msg;
		}
		logPrefix(lType, "BEGIN", text, args);
		stopWatch.begin(tag);
	}

	private void end(LOG_TYPE lType, String tag, String msg, ArrayList args){
		TimeDuration duration = stopWatch.endDuration(tag);
		String text = "["+tag+"] (" + duration + ")";
		if(StringUtils.isNotBlank(msg)) {
			text += " " + msg;
		}
		logPrefix(lType, "END", text, args);
	}

}
