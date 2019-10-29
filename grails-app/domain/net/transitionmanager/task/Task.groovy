package net.transitionmanager.task

import com.tdsops.tm.enums.domain.AssetCommentType
import org.apache.commons.lang3.StringUtils

class Task extends AssetComment {

	Task() {
		super()
		commentType = AssetCommentType.TASK
	}

	static constraints = {
		actStart nullable: true
	}

	static mapping = {
		discriminator value: '1'
	}

	String toString() {
		return 'Task:' + (taskNumber ? taskNumber.toString() + ':' : '') + StringUtils.left(comment, 25)
	}
}

