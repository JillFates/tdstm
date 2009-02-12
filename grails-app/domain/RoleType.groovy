class RoleType {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 id column: 'ROLE_TYPE_ID'
				 roleTypeCode column: 'CODE'
	}
	 /*
	  * list of fields
	  */
	String roleTypeCode
	String description
	String toString(){
		   return("$roleTypeCode")
	}
	/*
	 * Field Validations
	 */
	 static constraints = {
		 roleTypeCode(blank:false,nullable:false)
		 description(blank:true,nullable:true)
	 }
}
