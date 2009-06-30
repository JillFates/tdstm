class DataTransferComment
{
	String comment
	String commentType
	Integer mustVerify = 0
	DataTransferBatch dataTransferBatch
	Integer rowId
	Integer assetId
	Integer commentId
	static mapping = {
	 	version false
		columns {
			comment sqlType:'TEXT'
			mustVerify sqlType:'TINYINT'
		}
	}
	static constraints = {
		 comment( blank:true, nullable:true )
		 commentId( nullable:true )
		 commentType( inList:['issue','instruction','comment'] )
		 mustVerify( nullable:true )
		 rowId()	
		 assetId( nullable:true )
	}	
	
}