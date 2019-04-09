package net.transitionmanager.model

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.security.UserLogin
import net.transitionmanager.manufacturer.ManufacturerSync

class ModelSyncBatch {

	String    statusCode = 'PENDING'
	String    source         // Where the batch originated
	Date      dateCreated
	Date      lastModified
	Date      changesSince      // Date passed to the master for filtering changes
	UserLogin createdBy

	static hasMany = [manufacturerSync: ManufacturerSync, modelSync: ModelSync]

	static constraints = {
		dateCreated(nullable: true)
		lastModified(nullable: true)
		statusCode(blank: false, size: 0..20)
	}

	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column: 'batch_id'
			statusCode sqltype: 'varchar(20)'
		}
	}

	def beforeInsert = {
		dateCreated = lastModified = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
