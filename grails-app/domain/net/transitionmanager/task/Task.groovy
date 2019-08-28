package net.transitionmanager.task

import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.TimeUtil
import org.apache.commons.lang3.StringUtils

class Task extends AssetComment {

	Task() {
		super()
		commentType = AssetCommentType.TASK
	}

	String toString() {
		return 'Task:' + (taskNumber ? taskNumber.toString() + ':' : '') + StringUtils.left(comment, 25)
	}

	def beforeInsert() {
		commentType = AssetCommentType.TASK
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = dateCreated
	}

	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
		return true
	}
	static mapping = {
		discriminator value: '0'
		autoTimestamp false
	}


}

