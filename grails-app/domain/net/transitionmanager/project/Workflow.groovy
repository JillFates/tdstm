package net.transitionmanager.project

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.person.Person

class Workflow {

	String process
	Date   dateCreated
	Date   lastUpdated
	Person updatedBy

	static hasMany = [WorkflowTransition, WorkflowTransitionMap, Swimlane]

	static constraints = {
		dateCreated nullable: true
		lastUpdated nullable: true
		process blank: false, unique: true
		updatedBy nullable: true
	}

	static mapping = {
		version false
		autoTimestamp false
		id column: 'workflow_id'
		updatedBy column: 'updated_by'
	}

	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() { process }
}
