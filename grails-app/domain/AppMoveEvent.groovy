import com.tds.asset.Application



class AppMoveEvent {
	Application application
    MoveEvent moveEvent
	String value
	
    static constraints = {
		application(blank:true, nullable:true)
		moveEvent (blank:false, nullable:true)
		value (blank:true,nullable:true)
    }
}
