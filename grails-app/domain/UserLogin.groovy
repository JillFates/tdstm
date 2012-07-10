import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil
class UserLogin {
	String username
	String password
	Date createdDate = GormUtil.convertInToGMT( "now", "EDT" )
	Date lastLogin
	String active
	Date lastPage
	Date expiryDate
    
	static belongsTo = [ person:Person ]
	static hasMany = [
		assetTransitions : AssetTransition,
		dataTransferBatch : DataTransferBatch
	]
	
	static constraints = {
		person( nullable: false )
		username( blank: false, unique:true, size:2..25 )
		// TODO : Add a password constraint on size, and other rules
		password( blank: false, nullable: false, password: true )
		createdDate( nullable: true )
		lastLogin( nullable: true )
		active( nullable:false, inList:['Y', 'N'] )
		lastPage( nullable: true )
		expiryDate( blank: false, nullable: false )
	}

	static mapping  = {
		version false
		autoTimestamp false
		id column:'user_login_id'
		username sqlType: 'varchar(25)'
		password sqlType: 'varchar(100)'  // size must me more than 20 because it will store as encrypted code
		// TODO - active column should not be varchar(20) as it is only 1 char
		active sqlType:'varchar(20)'
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
}
