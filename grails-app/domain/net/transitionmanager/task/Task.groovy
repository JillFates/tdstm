package net.transitionmanager.task

import com.tdsops.tm.enums.domain.AssetCommentType
import org.apache.commons.lang3.StringUtils

class Task extends AssetComment {

	Boolean isCriticalPath

	Task() {
		super()
		commentType = AssetCommentType.TASK
	}

	String toString() {
		return 'Task:' + (taskNumber ? taskNumber.toString() + ':' : '') + StringUtils.left(comment, 25)
	}

	static mapping = {
		discriminator value: '1'
	}


}

