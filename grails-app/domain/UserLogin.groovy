class UserLogin {
    String username
    String password
    Date createdDate
	Date lastLogin
	String active
	Person person

	/*
	 * Fields Validations
	 */
	 static constraints = {
		 person( blank: false, nullable: false )
		 username( blank: false, unique:true, maxLength: 25 )
		 password( blank: false, nullable: false, password: true )
		 createdDate( blank: true, nullable: true )
		 lastLogin( blank: true, nullable: true )
		 active( nullable:false, inList:['Y', 'N'] )
	 }

	 /*
	 *  mapping for COLUMN Relation
	 */
	static mapping  = {
		version false
		id column:'user_login_id'
		username sqlType: 'varchar(25)'
		password sqlType: 'varchar(100)'  // size must me more than 20 because it will store encripted code
		active sqlType:'varchar(20)'
	}

	String toString(){
		username
	}

}
