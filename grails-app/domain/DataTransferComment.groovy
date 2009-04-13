class DataTransferComment
{
	String comment
	String commentType
	Integer mustVerify
	DataTransferBatch dataTransferBatch
	Integer rowId
	Integer assetId
	static mapping = {
	 	version false
		columns {
			comment sqlType:'TEXT'
			mustVerify sqlType:'TINYINT(2) default 0'
		}
	}
	static constraints = {
		 comment(blank:true, nullable:true)
		 commentType(inList:['issue','instruction','comment'] )
		 mustVerify( blank:true, nullable:true)
		 rowId(blank:false)	
		 assetId(blank:true, nullable:true)
	}	
	
}