import com.tdssrc.grails.GormUtil

/**
 * this class is used to provide a mapping between moveEvent and assigned person to that Event with role
 */
class MoveEventStaff {

	Person person
	MoveEvent moveEvent
	RoleType role

    Date dateCreated
    Date lastUpdated
    
	static constraints = {
		moveEvent ( nullable:false )
		person ( nullable:false )
		role( nullable:false )
	}
	
    /**
     *  mapping for COLUMN Relation
     */
    static mapping  = {
        autoTimestamp false
    }
    
	/**
	 * to get moveEventStaff object
	 * @param : person, instance of person for which need to get instance
	 * @param : role, instance of  role for which need to get instance
	 * @param : event, instance of event for which need to get instance
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
    
    /*
     * Date to insert in GMT
     */
    def beforeInsert = {
        dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
        lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
    }
    def beforeUpdate = {
        lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
    }
}
