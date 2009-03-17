class Asset {

	Project project
	String assetName
	AssetType assetType
	String assetTag
	String serialNumber
	String deviceFunction
	PartyGroup	owner

	/*
	 * Fields Validations
	 */
	static constraints = {
		project( blank:false, nullable:false )
		assetType( blank:true, nullable:true )
		assetName( blank:true )
		assetTag( blank:true )
		serialNumber( blank:true )
		deviceFunction( blank:true, nullable:true )
		owner( blank:true, nullable:true )
		
	}
	
	/*
	 *  mapping for COLUMN Relation
	 */
	 
	static mapping  = {	
		version true
		id column:'asset_id'			
	}
	
	String toString(){
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
	}
}
