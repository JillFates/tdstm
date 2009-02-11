class Project {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			 version false
			 id column:'PROJECT_ID'
			projectName column:'NAME'
	}
	 /*
	  * list of fields
	  */
	String projectName
	String trackChanges
	String toString(){
		   return("$projectName")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 projectName(blank:false,nullable:false,maxLength:64)
		 trackChanges(blank:false,nullable:false)
	 }
}
