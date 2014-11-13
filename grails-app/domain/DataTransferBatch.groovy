import com.tdssrc.grails.TimeUtil
import com.tdssrc.eav.EavEntityType

class DataTransferBatch {
	Date dateCreated
	String statusCode=LOADING
	Date lastModified
	Integer versionNumber
	String  transferMode
	Date exportDatetime
	Integer hasErrors = 0

	static final String LOADING='LOADING'
	static final String PENDING='PENDING'
	static final String POSTING='POSTING'
	static final String COMPLETED='COMPLETED'	
	static final String ERROR='ERROR'

	static hasMany = [ dataTransferValue:DataTransferValue ]
	
	static belongsTo = [ dataTransferSet : DataTransferSet, project : Project, userLogin : UserLogin, eavEntityType : EavEntityType ]
	
	static mapping = {
		version false
		autoTimestamp false
		columns {
			id column:'batch_id'
			hasErrors sqlType: 'TINYINT(1)'
		}
	}
	static constraints = {
		statusCode( blank:false, size:0..20, inList: [LOADING,PENDING,POSTING,COMPLETED,ERROR])
		dateCreated( nullable:true )
		exportDatetime( nullable:true )
		lastModified( nullable:true )
		transferMode( blank:false, inList:['I', 'E', 'B'] )
		versionNumber( nullable:true )
		hasErrors( nullable:false )
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
