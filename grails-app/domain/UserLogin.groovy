import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
class UserLogin {
	String username
	String password
	Date createdDate = GormUtil.convertInToGMT( "now", "EDT" )
	Date lastLogin
	String active
	Date lastPage
	Date expiryDate
	Date passwordChangedDate = TimeUtil.nowGMT()
	Boolean forcePasswordChange = false
    
	static belongsTo = [ person:Person ]
	static hasMany = [
		assetTransitions : AssetTransition,
		dataTransferBatch : DataTransferBatch
	]
	
	static constraints = {
		person( nullable: false )
		username( blank: false, unique:true, size:2..50 )
		// TODO : Add a password constraint on size, and other rules
		password( blank: false, nullable: false, password: true )
		createdDate( nullable: true )
		lastLogin( nullable: true )
		active( nullable:false, inList:['Y', 'N'] )
		lastPage( nullable: true )
		expiryDate( nullable: false )
		passwordChangedDate( nullable: false)
		forcePasswordChange( nullable: false)
	}

	static mapping  = {
		version false
		autoTimestamp false
		id column:'user_login_id'
		username sqlType: 'varchar(50)'
		password sqlType: 'varchar(100)'  // size must me more than 20 because it will store as encrypted code
		// TODO - active column should not be varchar(20) as it is only 1 char
		active sqlType:'varchar(20)'
		forcePasswordChange sqltype: 'boolean'
		passwordChangedDate sqltype: 'DateTime'
		person ignoreNotFound: true
		passwordChangedDate ignoreNotFound: true
	}

	String toString(){
		username
	}
	/*
	 *  Render person details as firstName + lastName
	 */
	def getPersonDetails(){
		return "${this.person.firstName} ${this.person.lastName}"
	}
	
	def isActive(){
		return TimeUtil.nowGMT() < expiryDate && active == 'Y' && person.active == 'Y'
	}
}
