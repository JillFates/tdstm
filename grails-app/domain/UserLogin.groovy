class UserLogin {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {
			 table 'USER_LOGIN'
			 version false
			 id column:'USER_LOGIN_ID'
			 	password column:'PASSWD'
			 		person column:'PERSON_ID'
	}
	/*
	  * list of fields
	  */
    String username
    String password
    Date createdDate = new Date();
	Date lastLogin
	// Person object reference
	Person person
	String toString(){
		   return("$username")
	}

	/*
	 * Fields Validations
	 */
	 static constraints = {
		 username(blank:false,unique:true,maxLength:64)
		 password(blank:false,nullable:false,maxLength:64)
		 createdDate(blank:true,nullable:true)
		 lastLogin(blank:true,nullable:true)
		 person(blank:false,nullable:false)
	 }
}
