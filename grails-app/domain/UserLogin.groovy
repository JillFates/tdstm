import com.tds.asset.AssetTransition
import com.tdssrc.grails.TimeUtil

class UserLogin {
	String username
	String password
	Date createdDate = TimeUtil.nowGMT()
	Date lastLogin
	String active
	Date lastPage
	Date expiryDate
	Date passwordChangedDate = TimeUtil.nowGMT()
	Date lastModified = TimeUtil.nowGMT()
	String forcePasswordChange = 'N'
    
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
		lastModified( nullable: false )
		//forcePasswordChange( nullable: false)
	}

	static mapping  = {
		version false
		autoTimestamp false
		id column:'user_login_id'
		username sqlType: 'varchar(50)'
		password sqlType: 'varchar(100)'  // size must me more than 20 because it will store as encrypted code
		// TODO - active column should not be varchar(20) as it is only 1 char
		active sqlType:'varchar(20)'
		forcePasswordChange sqltype: 'char(1)'
		passwordChangedDate sqltype: 'DateTime'
		lastModified sqltype: 'DateTime'
		//person ignoreNotFound: true
		//passwordChangedDate ignoreNotFound: true
	}

	static transients = ['personDetails', 'userActive']
	
	String toString() {
		return username ?:'no username found'
	}
	/*
	 *  Render person details as firstName + lastName
	 */
	def getPersonDetails() {
		return "${this.person.firstName} ${this.person.lastName}"
	}
	
	// Check if the user has not expired and is flagged as active
	def userActive() {
		return (TimeUtil.nowGMT() < expiryDate) && (active == 'Y') && (person.active == 'Y')
	}
	
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
}
