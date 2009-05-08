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
		 comment(blank:true, nullable:true)
		 commentId(blank:true, nullable:true)
		 commentType(inList:['issue','instruction','comment'])
		 mustVerify( blank:true, nullable:true)
		 rowId(blank:false)	
		 assetId(blank:true, nullable:true)
	}	
	
}