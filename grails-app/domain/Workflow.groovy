import com.tdssrc.grails.GormUtil
class Workflow {
	
	String process
	Date dateCreated
	Date lastUpdated
	Person updateBy
	
	static hasMany  = [ WorkflowTransition, WorkflowTransitionMap, Swimlane ]
	
	static constraints = {
		process( blank:false, nullable:false, unique:true)
		dateCreated( blank:false, nullable:false  )
		lastUpdated( blank:true, nullable:true  )
		updateBy( blank:true, nullable:true  )
    }
	
	static mapping  = {
		version false
		autoTimestamp false
		id column:'workflow_id'
		updateBy column: 'updated_by'
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
	String toString() {
		process
	}
	
}
