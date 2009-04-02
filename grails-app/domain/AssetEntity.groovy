class AssetEntity extends com.tdssrc.eav.EavEntity {
	
	// Temp Attributes
	String serverName
	String model
	String sourceLocation
	String targetLocation
	String sourceRack
	String targetRack
	String position
	String usize
	Project project
	AssetType assetType
	String assetName	
	String assetTag
	String serialNumber
	String deviceFunction
	String application
	PartyGroup owner
	
	
	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar
	]
	
	static constraints = {
		serverName( blank:false, nullable:false)
		model( blank:true, nullable:true)
		sourceLocation( blank:true, nullable:true )
		targetLocation( blank:true, nullable:true )
		sourceRack( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		position( blank:true, nullable:true )
		usize( blank:true, nullable:true )
		project( blank:false, nullable:false )
		assetType( blank:true, nullable:true )
		assetName( blank:true )
		assetTag( blank:true )
		serialNumber( blank:true )
		deviceFunction( blank:true, nullable:true )		
		application( blank:true, nullable:true )
		owner( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version true
		id column:'assetEntity_id'			
	}
	
	String toString(){
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
	}
	// This is where we will/would define special details about the class
	// TBD...
	//static eavModel = {
	//	attributeDomain: AssetAttribute
	//	,decendent: this
	//}
}
