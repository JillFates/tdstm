class MoveEventNews {
	MoveEvent moveEvent
	String message
	Integer isArchived = 0
	Date dateCreated = new Date()
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
		dateArchived( nullable:true  )
	}

	static mapping  = {	
		id column: 'move_event_news_id'
		version true
		columns {
			comment sqltype: 'text'
			resolution sqltype: 'text'
			isArchived sqltype: 'tinyint'
		}
	}
	
	String toString() {
		 message
	}
	
}
