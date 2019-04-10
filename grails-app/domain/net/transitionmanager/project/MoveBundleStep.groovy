package net.transitionmanager.project

import com.tdssrc.grails.TimeUtil

/**
 * Represents the various steps that are associated with a MoveBundle and
 * contains information entered by the user plus some properties that are
 * determined during the execution of the move event.
 */
class MoveBundleStep {

	public static final String METHOD_LINEAR = 'L'
	public static final String METHOD_MANUAL = 'M'

	MoveBundle moveBundle           // The bundle that the step is associated with
	Integer transitionId            // Maps to the id # of the transition in the workflow XML definition
	String label                    // Value to display in UI
	Date planStartTime              // The date/time of when the step will start, entered by the project manager enters while planning to move
	Date planCompletionTime         // The date/time of when the step will complete, entered by the project manager enters while planning to move
	String calcMethod = MoveBundleStep.METHOD_LINEAR  // The method that will be used to calculate the projection of completion for the step
	Integer showOnDashboard = 1     // Used to determine if the Step appears in the dashboard
	Date dateCreated
	Date lastUpdated
	Integer showInGreen = 0         // used to show the step progress in green when user set to 1
	// The following properties are subject to change during the project and will be recomputed on each snapshot process.  Since some of these
	// values could be changed during the course of a move (i.e. quantity of assets +/-) we should create a 1-to-many but will hold off for now.
	Date actualStartTime
	Date actualCompletionTime

	static constraints = {
		actualCompletionTime nullable: true
		actualStartTime nullable: true
		calcMethod blank: false, inList: [METHOD_LINEAR, METHOD_MANUAL]
		dateCreated nullable: true
		label blank: false
		lastUpdated nullable: true
		planCompletionTime nullable: true
		planStartTime nullable: true
		showInGreen range: 0..1
		showOnDashboard range: 0..1
	}

	static hasMany = [stepSnapshots: StepSnapshot]

	static mapping = {
		autoTimestamp false
		columns {
			actualCompletionTime sqltype: 'DateTime'
			actualStartTime      sqltype: 'DateTime'
			calcMethod           sqltype: 'char(1)'
			label                sqltype: 'varchar(30)'
			planCompletionTime   sqltype: 'DateTime'
			planStartTime        sqltype: 'DateTime'
			showInGreen          sqltype: 'tinyint(1)'
			showOnDashboard      sqltype: 'tinyint(1)'
		}
	}

	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	/**
	 * calculates the total time that step is planned to take to complete
	 * @return the number of seconds the step was planned to take
	 */
	int getPlanDuration() {
		// calculate # of seconds planCompletionTime - planStartTime
		int timeDuration = 0
		if (planCompletionTime && planStartTime) {
			timeDuration = planCompletionTime.time - planStartTime.time
			if (timeDuration) {
				timeDuration = (timeDuration / 1000).intValue()
			}
		}
		timeDuration
	}

	/**
	 * calculates the actual time that the step has taken.  If the step hasn't completed
	 * it will determine the duration from the start to the current time or time passed into method.
	 * @param asOfTime the date to base the duration off of if Step is not completed
	 * @return total duration that a step has taken to up to the current time (seconds)
	 */
	int getActualDuration(Date asOfTime) {
		int timeDuration
		if (!asOfTime) {
			asOfTime = TimeUtil.nowGMT()
		}

		if (actualStartTime && actualCompletionTime) {
			timeDuration = actualCompletionTime.time - actualStartTime.time
		}
		else if (!actualCompletionTime && actualStartTime) {
			timeDuration = asOfTime.time - actualStartTime.time
		}

		timeDuration ? (timeDuration / 1000).intValue() : 0
	}

	/**
	 * determines if the step is completed
	 * @return true if completed
	 */
	boolean isCompleted() {
		actualCompletionTime
	}

	String toString() {
		"MoveBundleStep(label: ${label}, tranId=${transitionId}, method=${calcMethod}, planStart=${planStartTime}, planCompletionTime=${planCompletionTime}, " +
				"actualStartTime=${actualStartTime}, actualCompletionTime=${actualCompletionTime} )"
	}
}
