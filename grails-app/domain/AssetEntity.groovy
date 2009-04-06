class AssetEntity extends com.tdssrc.eav.EavEntity {
	
	// Temp Attributes
	//String serverName
	String model
	String sourceLocation
	String targetLocation
	String sourceRack
	String targetRack
	String sourceRackPosition
	String targetRackPosition
	String usize
	String manufacturer
	String fiberCabinet
	String hbaPort
	String hinfo
	String ipAddress
	String kvmDevice
	String kvmPort
	String newOrOld
	String nicPort
	String powerPort
	String remoteMgmtPort
	String truck	
	Project project
	AssetType assetType
	String assetName	
	String assetTag
	String serialNumber	
	String application
	PartyGroup owner
	
	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar
	]
	
	static constraints = {
		//serverName( blank:false, nullable:false)
		assetName( blank:false, nullable:false )
		model( blank:true, nullable:true)
		sourceLocation( blank:true, nullable:true )
		targetLocation( blank:true, nullable:true )
		sourceRack( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		sourceRackPosition( blank:true, nullable:true )
		targetRackPosition( blank:true, nullable:true )
		usize( blank:true, nullable:true )
		manufacturer( blank:true, nullable:true )
		fiberCabinet( blank:true, nullable:true )
		hbaPort( blank:true, nullable:true )
		hinfo( blank:true, nullable:true )
		ipAddress( blank:true, nullable:true )
		kvmDevice( blank:true, nullable:true )
		kvmPort( blank:true, nullable:true )
		newOrOld( blank:true, nullable:true )
		nicPort( blank:true, nullable:true )
		powerPort( blank:true, nullable:true )
		remoteMgmtPort( blank:true, nullable:true )
		truck( blank:true, nullable:true )		
		project( blank:false, nullable:false )
		assetType( blank:true, nullable:true )
		assetTag( blank:true )
		serialNumber( blank:true )				
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
