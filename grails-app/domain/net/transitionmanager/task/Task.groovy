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

	final static String batchUpdateSQLSentence = '''
		update asset_comment 
		   set is_critical_path = ?,
		       slack = ?,
		       est_start = ?,
		       est_finish = ?
         where asset_comment_id = ?
	'''

}

