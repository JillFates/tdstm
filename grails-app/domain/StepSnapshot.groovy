import java.text.SimpleDateFormat;
import org.apache.shiro.SecurityUtils
import com.tdssrc.grails.GormUtil
/**
 * The StepSnapshot domain represents a point in time representation of the status of a Step in a MoveBundle. A group of
 * snapshot records will be created at one time for all MoveBundleStep records associated with a MoveBundle.
 */
class StepSnapshot {
	MoveBundleStep moveBundleStep
	int tasksCount				// The number of tasks/assets to be processed.  For Manual steps, it should be 100
	int	tasksCompleted			// The number of tasks/assets completed.  For manual steps, it will be entered by the user as a percentage of completion
	int duration				// The number of seconds that the task has been executing
	int planDelta				// The number of seconds that the step is over(+)/under(-) the planned finish time.
	int dialIndicator			// Quantity to display in the dial/gage on dashboard
	Date dateCreated = GormUtil.convertInToGMT( "now", "EDT" )

	static constraints = { 
		dateCreated( nullable:true )
	}

	static mapping  = {
		version false
		autoTimestamp false
		columns {
			taskCount sqltype: 'smallint unsigned'
			tasksCompleted sqltype: 'smallint unsigned'
			duration sqltype: 'mediumint unsigned'
			planDelta sqltype: 'int'
			dialIndicator sqltype: 'tinyint'		
		}
	}
	/**
	 * computes the pace of the task in seconds
	 */
	def getPlanTaskPace () {
		def planDuration = moveBundleStep.planDuration
		// print "tasksCount=${tasksCount}, planDuration=${planDuration}\n"		
		if( planDuration ){
			return tasksCount > 0 ? (planDuration / tasksCount ).intValue() : 0
		} else {
			return 0
		}
				
	}
	
	/**
	 * Computes the pace of the task in seconds. Return zero (0) if no tasks have been completed
	 */
	def getActualTaskPace() {
		return (tasksCompleted > 0 && duration) ? (duration / tasksCompleted ).intValue() : 0
	}	
	
	/**
	 * calculates the projected time (seconds) remaining based on the number task uncompleted times the planned pace.
	 * @return int - number of projected time remaining (seconds)
	 * Case: 
	 *    1. step has not started - return the moveBundle.planDuration
	 *    2. step in progress - return (taskCount - taskCompleted) * moveBundleStep.planPace
	 *    3. step is completed - return zero (0)
	 */
	def getProjectedTimeRemaining( ) {
		def timeRemaining = 0
		
		if ( tasksCompleted < tasksCount ){
			// print "tasksCount:${tasksCount}, tasksCompleted=${tasksCompleted}, planTaskPace=${planTaskPace}\n"
			timeRemaining = ((tasksCount - tasksCompleted) * planTaskPace ).intValue()
		}
		 return timeRemaining 
	}
	
	/**
	 * Calculates the projected time (seconds) over(+)/under(-) the planned completion time for the step.  
	 * @return int - projected time over planed over completion time (seconds)
	 * Case:
	 *    1. step has not started: 
	 *       a. planStartTime <= current time: return zero (0)
	 *       b. planStartTime > current time: (current time + projectedTimeRemaining) - moveBundleStep.planCompletionTime
	 *    2. step in progress: (current time + projectedTimeRemaining) - moveBundleStep.planCompletionTime
	 *    3. step is completed:  return zero (0)
	 */
	def getProjectedTimeOver() {
		def timeOver = 0
		def nowTime = GormUtil.convertInToGMT( "now", "EDT" ).getTime()
		if(!hasStarted()){
			if( moveBundleStep.planStartTime.getTime() >  nowTime ) {
				timeOver = ( nowTime + getProjectedTimeRemaining() * 1000 ) - moveBundleStep.planCompletionTime.getTime()
			}
		} else if(!isCompleted()){
			timeOver = ( nowTime + getProjectedTimeRemaining() * 1000 ) - moveBundleStep.planCompletionTime.getTime()
		}
		
		if(timeOver){
			timeOver = timeOver / 1000
		}
		 return timeOver 
	}

	/**
	 * calculates the the projected time that the step will be completed based on the pace
	 * @return date - projected completion time
	 */
	def getProjectedCompletionTime() {
	
	// TODO : JPM : Shouldn't be converting TZ here....  We leave TZ switch in the web service ONLY
	
		// return moveBundleStep.planCompletionTime + projectedTimeOver
		//def offsetTZ =  new Date().getTimezoneOffset() / 60 
		def projectedCompletionTimeInseconds = ( moveBundleStep.planCompletionTime.getTime() / 1000 ) + getProjectedTimeOver()
		def projectedCompletionTime = new Date( (Long)(projectedCompletionTimeInseconds * 1000) )
		def dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return dateformat.format(projectedCompletionTime)
	}

	/**
	 * Used to determine if the Step has been completed
	 * @return boolean - true if step has been completed
	 */
	def isCompleted() {
		return tasksCount == tasksCompleted
	}
	
	/**
	 * Used to determine if the step has started.  It will return true even after completed
	 * @return boolean - true if the step has started
	 */
	def hasStarted() {
		 return moveBundleStep.actualStartTime != null
	}
	
	/**
	 * Returns the status bar color 
	 * @return string - the color green/red indication the step status to planned completion time
	 */
	def getStatusColor() {
		def color = "green"
		// Need to test on current snapshot so that this will work historically
		if(moveBundleStep.showInGreen){
			color = "green"
		} else if (tasksCompleted == tasksCount) {
			color =  moveBundleStep.actualCompletionTime > moveBundleStep.planCompletionTime ? "red" : "green"
		} else {
			if(dialIndicator < 25){
				color = "red"
			} else if(dialIndicator >= 25 && dialIndicator < 50){
				color = "yellow"
			}
		}
		return color
	}	
	
    String toString(){
		moveBundleStep.label + " " + dateCreated
	}

}
