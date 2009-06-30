class AssetComment {
	
	String comment
	String commentType
	Integer mustVerify = 0
	AssetEntity assetEntity
	Date dateCreated = new Date()
	Integer isResolved = 0
	Date dateResolved 
	String resolution
	Person resolvedBy
	Person createdBy
	static constraints = {
		
		comment( blank:true, nullable:true  )
		commentType( blank:true, nullable:true, inList: ['issue','instruction','comment'] )
		mustVerify( nullable:true )
		isResolved( nullable:true )
		resolution( blank:true, nullable:true  )
		resolvedBy( nullable:true  )
		createdBy( nullable:true  )
		dateResolved( nullable:true  )
	}

	static mapping  = {	
		version true
		id column: 'asset_comment_id'
		resolvedBy column: 'resolved_by'
		createdBy column: 'created_by'
		columns {
			comment sqltype: 'text'
			mustVerify sqltype: 'tinyint'
			isResolved sqltype: 'tinyint'
			resolution sqltype: 'text'
		}
	}
	
	String toString() {
		 comment
	}
	
}