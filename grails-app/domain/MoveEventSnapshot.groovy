/**
 * The MoveEventSnapshot domain is a point in time representation of the status of a MoveEvent. The values are computed via an
 * aggregate of MoveBundle.MoveBundleStep.StepSnapshot records.  These records are created as part of the MoveBundleService.
 */
class MoveEventSnapshot {
	static final String TYPE_PLANNED="P"
	static final String TYPE_REVISED="R"
	
	String type=TYPE_PLANNED	// Indicates if the snapshot is for P)lann or R)evised
	int planDelta=0				// The number of seconds that the move is over(+)/under(-) the planned finish time.
	int dialIndicator=0			// Quantity to display in the dial/gage on dashboard
	Date dateCreated = new Date()

	static belongsTo = [moveEvent:MoveEvent]
	
	static constraints = { 
		type( nullable:false, inList:[TYPE_PLANNED, TYPE_REVISED] )
	}

	static mapping  = {
		version false
		columns {
			type sqltype: "char(1)"
			planDelta sqltype: 'int'
			dialIndicator sqltype: 'tinyint'		
		}
	}

}
