class Role {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 table 'ROLE_TYPE'
			 id column: 'ROLE_TYPE_ID'
	}
	 /*
	  * list of fields
	  */
	String name
	String toString(){
		   return("$name")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 name(blank:false,nullable:false,maxLength:64)
	 }
}
