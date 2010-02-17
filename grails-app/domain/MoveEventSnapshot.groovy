/**
 * The MoveEventSnapshot domain is a point in time representation of the status of a MoveEvent. The values are computed via an
 * aggregate of MoveBundle.MoveBundleStep.StepSnapshot records.  These records are created as part of the MoveBundleService.
 */
class MoveEventSnapshot {
	String type="P"				// Indicates if the snapshot is for P)lann or R)evised
	int planDelta				// The number of seconds that the move is over(+)/under(-) the planned finish time.
	int dialIndicator			// Quantity to display in the dial/gage on dashboard
	Date dateCreated = new Date()

	static belongsTo = [moveEvent:MoveEvent]
	
	static constraints = { 
		type( nullable:false, inList:['P','R'] )
	}

	static mapping  = {
		version false
		columns {
			type sqltype: "char(1)"
			planDelta sqltype: 'int'
			dialIndicator sqltype: 'tinyint'		
		}
	}
    String toString(){
		moveBundleStep.name + " " + dateCreated
	}

}
