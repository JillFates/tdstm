class AssetType {
	
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			version false
			id column:'ASSET_TYPE_ID'
	}
	 /*
	  * list of fields
	  */
	
	String assetType
	String toString(){
		   return("$assetType")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 assetType(blank:true,nullable:true)
	 }

}
