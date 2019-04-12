package net.transitionmanager.imports

import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin

class DataTransferBatch {

	// Current status of a batch
	String statusCode = LOADING

	// JPM 2/2018 : Do not recall what transferMode is used for. The batch listing only
	// searchs for 'I' that is the default.
	String transferMode = 'I'

	// JPM 2/2018 : Do not believe that versionNumber is used for anything
	Integer versionNumber

	// References the progress job (Quartz) that is presenting processing the batch
	String progressKey

	// The name of the import file for historical reference
	String importFilename

	// The results of the review and/or the posting results (text field)
	String importResults

	AssetClass assetClass

	// Used by the import process to compare the time of the last change of the domain
	// object to that of when the data was originally exported. If the exported time is
	// earlier it will cause a change conflict to occur and the data will not be updated.
	Date exportDatetime

	// Flat that there is one or more errors in the batch
	Integer hasErrors = 0

	Date dateCreated = TimeUtil.nowGMT()
	Date lastModified

	public static final String LOADING   = 'LOADING'
	public static final String PENDING   = 'PENDING'
	public static final String POSTING   = 'POSTING'
	public static final String COMPLETED = 'COMPLETED'
	public static final String ERROR     = 'ERROR'

	static hasMany = [dataTransferValue: DataTransferValue]

	static belongsTo = [
			dataTransferSet: DataTransferSet,
			project: Project,
			userLogin: UserLogin
		]

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
		statusCode blank: false, inList: [LOADING, PENDING, POSTING, COMPLETED, ERROR]
		transferMode blank: false, inList: ['I', 'E', 'B']
		versionNumber nullable: true
	}

}
