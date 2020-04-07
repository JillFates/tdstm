package net.transitionmanager.task

import com.tdsops.tm.enums.domain.AssetCommentStatus
import net.transitionmanager.person.Person
import spock.lang.Specification

class AssetCommentSpec extends Specification {

	void 'test if is a task belongs to Person'() {
		when: 'A Person has a task assigned'
			Person me = new Person()
			me.firstName = "Me"
			String roleAssigned = "ROLE_ASSIGNED"
			Task task = new Task()
			task.taskNumber = 12345
			task.comment = "Some Task"
			task.status = AssetCommentStatus.PENDING
			task.assignedTo = me
			task.role = roleAssigned
			List<AssetCommentStatus> assignableStatus = [AssetCommentStatus.PENDING, AssetCommentStatus.PLANNED, AssetCommentStatus.READY]
			List<AssetCommentStatus> notAssignableStatus = [AssetCommentStatus.HOLD, AssetCommentStatus.STARTED, AssetCommentStatus.COMPLETED, AssetCommentStatus.TERMINATED]

		then: 'is my task because is assigned to me in anycase'
			for( String status in (assignableStatus + notAssignableStatus)) {
				task.status = status
				assert task.isMyTask(me, []) == true
				assert task.isMyTask(me, ['ROLE1']) == true
				assert task.isMyTask(me, ['ROLE1', roleAssigned, 'ROLE2']) == true
			}

		when: 'we have another Person to check if the task is assigned'
			Person other = new Person()
			other.firstName = "Other"

		then: "It fails if doesn't belong to one of the assignable roles"
			task.isMyTask(other, ['ROLE1', 'ROLE2'] ) == false

		and: "It succeeds if is in the assignable roles and in the assignable statuses"
			for( String status in assignableStatus) {
				task.status = status
				assert task.isMyTask(other, ['ROLE1', roleAssigned, 'ROLE2']) == true
			}

		and: "It fails if is not in the assignable statuses"
			for( String status in notAssignableStatus) {
				task.status = status
				assert task.isMyTask(other, ['ROLE1', roleAssigned, 'ROLE2']) == false
			}

		when: 'is hard assigned'
			task.hardAssigned = 1

		then: 'it fails even in any assignable statuses'
			for( String status in assignableStatus) {
				task.status = status
				assert task.isMyTask(other, ['ROLE1', roleAssigned, 'ROLE2']) == false
			}
	}
}
