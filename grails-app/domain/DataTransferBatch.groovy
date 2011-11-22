import com.tdssrc.grails.GormUtil
import com.tdssrc.eav.EavEntityType
class DataTransferBatch {
	Date dateCreated
	String statusCode
	Date lastModified
	Integer versionNumber
	String  transferMode
	Date exportDatetime
	Integer hasErrors = 0
	
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
		statusCode( blank:false, size:0..20 )
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
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastModified = GormUtil.convertInToGMT( "now", "EDT" )
	}
}
