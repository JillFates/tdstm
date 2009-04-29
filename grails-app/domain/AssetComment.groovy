class AssetComment {
	
	String comment
	String commentType
	Integer mustVerify
	AssetEntity assetEntity
	Date dateCreated = new Date()
	
	static constraints = {
		
		comment( blank:true, nullable:true  )
		commentType( blank:true, nullable:true, inList: ['issue','instruction','comment'] )
		mustVerify( blank:true, nullable:true )
		
	}

	static mapping  = {	
		version true
		id column: 'asset_comment_id'
		columns {
			comment sqltype: 'text'
			mustVerify sqltype: 'tinyint'
		}
	}
	
	String toString() {
		 comment
	}
	
}