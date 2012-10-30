
/*
 * this class is used to provide a mapping between moveEvent and assigned person to that Event with role
 */
class MoveEventStaff {

	Person person
	MoveEvent moveEvent
	RoleType role


	static constraints = {
		moveEvent ( blank:false, nullable:false )
		person ( blank:false, nullable:false )
		role( blank:false, nullable:false )
	}
	
	/*
	 * to get moveEventStaff object
	 * @person : instance of person for which need to get instance
	 * @person : instance of  role for which need to get instance
	 * @person : instance of event for which need to get instance
	 * @return : moveEvent staff instance
	 */
	static def findAllByStaffAndEventAndRole(person, event, role){
		def result = MoveEventStaff.createCriteria().get {
			and {
				 eq('person', person )
				 eq('moveEvent', event )
				 eq('role', role )
			}
		}
		return result
	}
}
