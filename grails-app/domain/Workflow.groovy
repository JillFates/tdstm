import com.tdssrc.grails.TimeUtil
class Workflow {
	
	String process
	Date dateCreated
	Date lastUpdated
	Person updatedBy
	
	static hasMany  = [ WorkflowTransition, WorkflowTransitionMap, Swimlane ]
	
	static constraints = {
		process( blank:false, nullable:false, unique:true)
		dateCreated( nullable:true  )
		lastUpdated( nullable:true  )
		updatedBy( nullable:true  )
    }
	
	static mapping  = {
		version false
		autoTimestamp false
		id column:'workflow_id'
		updatedBy column: 'updated_by'
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
	String toString() {
		process
	}
	
}
