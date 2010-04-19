import com.tdssrc.grails.GormUtil
class MoveEventNews {
	MoveEvent moveEvent
	String message
	Integer isArchived = 0
	Date dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
	Date dateArchived 
	String resolution
	Person archivedBy
	Person createdBy
	
	static constraints = {
		message( blank:true, nullable:true  )
		isArchived( nullable:true )
		resolution( blank:true, nullable:true  )
		archivedBy( nullable:true  )
		createdBy( nullable:true  )
		dateCreated( nullable:true  )
		dateArchived( nullable:true  )
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'move_event_news_id'
		archivedBy column: 'archived_by'
		createdBy column: 'created_by'
		columns {
			message sqltype: 'text'
			resolution sqltype: 'text'
			isArchived sqltype: 'tinyint'
		}
	}
	
	String toString() {
		 message
	}
	
}
