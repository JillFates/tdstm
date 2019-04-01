package net.transitionmanager.project

import com.tdssrc.grails.TimeUtil

/**
 * A point in time representation of the status of a MoveEvent. The values are computed via an
 * aggregate of MoveBundle.MoveBundleStep.StepSnapshot records.  These records are created as part of the MoveBundleService.
 */
class MoveEventSnapshot {

	public static final String TYPE_PLANNED = 'P'
	public static final String TYPE_REVISED = 'R'

	String type = TYPE_PLANNED   // Indicates if the snapshot is for P)lann or R)evised
	int planDelta = 0            // The number of seconds that the move is over(+)/under(-) the planned finish time.
	int dialIndicator = 0        // Quantity to display in the dial/gage on dashboard
	Date dateCreated

	static belongsTo = [moveEvent: MoveEvent]

	static constraints = {
		dateCreated nullable: true
		type inList: [TYPE_PLANNED, TYPE_REVISED]
	}

	static mapping = {
		version false
		autoTimestamp false
		columns {
			dialIndicator sqltype: 'tinyint'
			planDelta sqltype: 'int'
			type sqltype: 'char(1)'
		}
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
}
