package net.transitionmanager.project

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.person.Person

class MoveEventNews {

	MoveEvent moveEvent
	String    message
	Integer   isArchived = 0
	Date      dateCreated = TimeUtil.nowGMT()
	Date      dateArchived
	String    resolution
	Person    archivedBy
	Person    createdBy

	static constraints = {
		archivedBy   nullable: true
		createdBy    nullable: true
		dateArchived nullable: true
		dateCreated  nullable: true
		isArchived   nullable: true
		message      nullable: true
		resolution   nullable: true
	}

	static mapping = {
		autoTimestamp false
		id column: 'move_event_news_id'
		archivedBy column: 'archived_by'
		createdBy column: 'created_by'
		columns {
			isArchived sqltype: 'tinyint'
			message    sqltype: 'text'
			resolution sqltype: 'text'
		}
	}

	String toString() { message }
}
