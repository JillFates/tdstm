class Asset {
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
			version false
			id column:'ASSET_ID'			
			projectName column:'PROJECT_ID'
			assetType column:'ASSET_TYPE'
	}
	 /*
	  * list of fields
	  */
	Project projectName
	AssetType assetType
	String assetName
	String assetTag
	String serialNumber
	String deviceFunction
	String toString(){
		   return("$assetName")
	}
	/*
	 * Fields Validations
	 */
	 static constraints = {
		 projectName(blank:false,nullable:false)
		 assetType(blank:true,nullable:true)
		 assetName(blank:true)
		 assetTag(blank:true)
		 serialNumber(blank:true)
		 deviceFunction(blank:true)
	 }
}
