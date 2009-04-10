class AssetEntity extends com.tdssrc.eav.EavEntity {
	String assetName	
	String assetType
	String assetTag
	String serialNumber	
	String manufacturer
	String model
	String application
	PartyGroup owner
	String sourceLocation
	String sourceRoom
	String sourceRack
	Integer sourceRackPosition
	String targetLocation
	String targetRoom
	String targetRack
	Integer targetRackPosition
	String usize
	String railType
	String fiberCabinet
	String hbaPort
	String ipAddress
	String hinfo
	String kvmDevice
	String kvmPort
	String newOrOld
	String nicPort
	String powerPort
	String remoteMgmtPort
	String truck	
	Project project

	// MoveBundleAsset fields
	MoveBundle moveBundle
	ProjectTeam sourceTeam
	ProjectTeam targetTeam
	String cart
	String shelf
	
	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar
	]
	
	static constraints = {
		assetName( blank:false, nullable:false )
		manufacturer( blank:true, nullable:true )
		model( blank:true, nullable:true)
		assetType( blank:true, nullable:true )
		assetTag( blank:true )
		serialNumber( blank:true )				
		sourceLocation( blank:true, nullable:true )
		sourceRoom( blank:true, nullable:true )
		sourceRack( blank:true, nullable:true )
		sourceRackPosition( blank:true, nullable:true )
		targetLocation( blank:true, nullable:true )
		targetRoom( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		targetRackPosition( blank:true, nullable:true )
		usize( blank:true, nullable:true )
		railType( blank:true, nullable:true )
		fiberCabinet( blank:true, nullable:true )
		hbaPort( blank:true, nullable:true )
		ipAddress( blank:true, nullable:true )
		hinfo( blank:true, nullable:true )
		kvmDevice( blank:true, nullable:true )
		kvmPort( blank:true, nullable:true )
		newOrOld( blank:true, nullable:true )
		nicPort( blank:true, nullable:true )
		powerPort( blank:true, nullable:true )
		remoteMgmtPort( blank:true, nullable:true )
		truck( blank:true, nullable:true )		
		project( blank:false, nullable:false )
		application( blank:true, nullable:true )
		owner( blank:true, nullable:true )

		// The following were the MoveBundleAsset fields
		moveBundle( blank:true, nullable:true )
		sourceTeam( blank:true, nullable:true )
		targetTeam( blank:true, nullable:true )
		cart( blank:true, nullable:true )
		shelf( blank:true, nullable:true )
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
