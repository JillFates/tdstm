package net.transitionmanager.imports

class DataTransferComment {

	String comment
	String commentType
	Integer mustVerify = 0
	DataTransferBatch dataTransferBatch
	Integer rowId
	Integer assetId
	Integer commentId

	static constraints = {
		assetId nullable: true
		comment nullable: true
		commentId nullable: true
		commentType inList: ['issue', 'instruction', 'comment']
		mustVerify nullable: true
	}

	static mapping = {
		version false
		columns {
			comment sqlType: 'TEXT'
			mustVerify sqlType: 'TINYINT'
		}
	}
}
