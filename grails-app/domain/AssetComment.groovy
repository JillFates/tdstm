import com.tdssrc.grails.GormUtil
class AssetComment {
	
	String comment
	String commentType
	Integer mustVerify = 0
	AssetEntity assetEntity
	Date dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
	Integer isResolved = 0
	Date dateResolved 
	String resolution
	Person resolvedBy
	Person createdBy
	String commentCode 
	String category = "general"
	String displayOption = "G"	// Used in dashboard to control display of user entered test (comment) or a generic message
	
	static constraints = {
		
		comment( blank:true, nullable:true  )
		commentType( blank:true, nullable:true, inList: ['issue','instruction','comment'] )
		mustVerify( nullable:true )
		isResolved( nullable:true )
		resolution( blank:true, nullable:true  )
		resolvedBy( nullable:true  )
		createdBy( nullable:true  )
		dateCreated( nullable:true  )
		dateResolved( nullable:true  )
		commentCode( blank:true, nullable:true  )
		category( blank:false, nullable:false )
		displayOption( blank:false, inList: ['G','U'] ) // Generic or User
	}

	static mapping  = {	
		version true
		autoTimestamp false
		id column: 'asset_comment_id'
		resolvedBy column: 'resolved_by'
		createdBy column: 'created_by'
		columns {
			comment sqltype: 'text'
			mustVerify sqltype: 'tinyint'
			isResolved sqltype: 'tinyint'
			resolution sqltype: 'text'
			displayOption sqltype: 'char(1)'
		}
	}
	
	String toString() {
		 comment
	}
	
}