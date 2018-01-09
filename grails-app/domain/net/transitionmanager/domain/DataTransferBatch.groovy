package net.transitionmanager.domain

import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.TimeUtil

class DataTransferBatch {

	Date dateCreated
	String statusCode = LOADING
	Date lastModified
	Integer versionNumber
	String transferMode
	String progressKey       // tracks which progress job is presenting processing a step on the batch
	String importFilename    // The name of the import file
	String importResults     // The results of the review and/or the posting results (text field)
	Date exportDatetime
	Integer hasErrors = 0

	public static final String LOADING   = 'LOADING'
	public static final String PENDING   = 'PENDING'
	public static final String POSTING   = 'POSTING'
	public static final String COMPLETED = 'COMPLETED'
	public static final String ERROR     = 'ERROR'

	static hasMany = [dataTransferValue: DataTransferValue]

	static belongsTo = [dataTransferSet: DataTransferSet, project: Project,
	                    userLogin: UserLogin, eavEntityType: EavEntityType]

	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column: 'batch_id'
			hasErrors sqlType: 'TINYINT(1)'
		}
	}

	static constraints = {
    userLogin nullable: true
		dateCreated nullable: true
		exportDatetime nullable: true
		importFilename nullable: true
		importResults nullable: true, maxSize: 16384000
		lastModified nullable: true
		progressKey nullable: true
		statusCode blank: false, size: 0..20, inList: [LOADING, PENDING, POSTING, COMPLETED, ERROR]
		transferMode blank: false, inList: ['I', 'E', 'B']
		versionNumber nullable: true
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
