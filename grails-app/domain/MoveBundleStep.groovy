/**
 * The MoveBundleStep domain represents the various steps that are associated with a MoveBundle and contains
 * information entered by the user plus some properties that are determined during the execution of the 
 * move event.  
 */
class MoveBundleStep {	
    MoveBundle moveBundle			// The bundle that the step is associated with
    Integer transitionId			// Maps to the id # of the transition in the workflow XML definition
	String label					// Value to display in UI
    Date planStartTime				// The date/time of when the step will start, entered by the project manager enters while planning to move
    Date planCompletionTime			// The date/time of when the step will complete, entered by the project manager enters while planning to move
	String calcMethod				// The method that will be used to calculate the projection of completion for the step
	Integer showOnDashboard=1		// Used to determine if the Step appears in the dashboard		
	Date dateCreated = new Date()
	Date lastUpdated

	// The following properties are subject to change during the project and will be recomputed on each snapshot process.  Since some of these
	// values could be changed during the course of a move (i.e. quantity of assets +/-) we should create a 1-to-many but will hold off for now.
    Date actualStartTime
    Date actualCompletionTime

	static constraints = { 
		label( blank:false, nullable:false)
		planStartTime( nullable:true )
		planCompletionTime( nullable:true )
		actualStartTime( nullable:true )
		actualCompletionTime( nullable:true )
		calcMethod( blank:false, nullable:false, inList: ['L','M'] )	// Linear and Manual
		showOnDashboard(range:0..1)	
	}

	static hasMany = [
		stepSnapshots : StepSnapshot
	]

	static mapping  = {
		version true
		columns {
			label sqltype: 'varchar(30)'
			calcMethod sqltype: 'char(1)'
			showOnDashboard sqltype: 'tinyint(1)'
			planCompletionTime sqltype : 'DateTime'
			planStartTime sqltype: 'DateTime'
			actualCompletionTime sqltype : 'DateTime'
			actualStartTime sqltype: 'DateTime'			
		}
	}
	
	/**
	 * calculates the total time that step is planned to take to complete
	 * @return int - the number of seconds the step was planned to take
	 */
	def getPlanDuration() {
		// calculate # of seconds planCompletionTime - planStartTime
		def timeDuration = 0
		if(planCompletionTime && planStartTime){
			timeDuration = planCompletionTime.getTime() - planStartTime.getTime()
			if(timeDuration) {
				timeDuration = timeDuration / 1000 
			}
		}
		return timeDuration
	}
		
	/**
	 * calculates the actual total time that the step has taken.  If the step hasn't completed
	 * it will determine the duration from the start to the current time.
	 * @return int - total duration that a step has taken to up to the current time (seconds)
	 */
	def getActualDuration() {
		/* 
		if actualStartTime is null {
			return 0
		} else if actualCompletionTime is null {
			return now - actualStartTime
		} else {
			return actualCompletionTime - actualStartTime
		}
		*/
		def timeDuration = 0
		if( actualStartTime && actualStartTime){
			timeDuration = actualCompletionTime.getTime() - actualStartTime.getTime()
		} else if( !actualCompletionTime && actualStartTime){
			timeDuration = new Date().getTime() - actualStartTime.getTime()
		}
		
		if(timeDuration){
			timeDuration = timeDuration / 1000
		}
		return timeDuration
	}
	
	/**
	 * determines if the step is completed
	 * @return bool - true if completed
	 */
	def isCompleted() {
		 return actualCompletionTime != null
	}

    String toString(){
		label
	}

}
