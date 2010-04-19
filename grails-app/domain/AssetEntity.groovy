class AssetEntity extends com.tdssrc.eav.EavEntity {
	String assetName	
	String assetType
	String assetTag
	String serialNumber	
	String manufacturer
	String model
	String application = ""
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
	String fiberType
	Integer fiberQuantity
	String hbaPort
	String ipAddress
	String hinfo
	String kvmDevice
	String kvmPort
	Integer hasKvm = 0
	Integer hasRemoteMgmt =0
	String newOrOld
	String nicPort
	String powerType
	String pduType
	String pduPort
	Integer pduQuantity
	String remoteMgmtPort
	String truck
	String appOwner = ""
	String appSme = ""
	Integer priority 
	Project project
	String shortName

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
		sourceRackPosition( nullable:true )
		targetLocation( blank:true, nullable:true )
		targetRoom( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		targetRackPosition( nullable:true )
		usize( blank:true, nullable:true )
		railType( blank:true, nullable:true )
		fiberCabinet( blank:true, nullable:true )
		fiberType( blank:true, nullable:true )
		fiberQuantity( blank:true, nullable:true )
		hbaPort( blank:true, nullable:true )
		ipAddress( blank:true, nullable:true )
		hinfo( blank:true, nullable:true )
		kvmDevice( blank:true, nullable:true )
		kvmPort( blank:true, nullable:true )
		newOrOld( blank:true, nullable:true )
		nicPort( blank:true, nullable:true )
		powerType( blank:true, nullable:true )
		pduType( blank:true, nullable:true )
		pduQuantity( blank:true, nullable:true )
		pduPort( blank:true, nullable:true )
		remoteMgmtPort( blank:true, nullable:true )
		hasKvm( blank:true, nullable:true )
		hasRemoteMgmt( blank:true, nullable:true )
		truck( blank:true, nullable:true )		
		project( nullable:false )
		application( blank:true, nullable:false )
		owner( nullable:true )
		appOwner( blank:true,nullable:false )
		appSme( blank:true, nullable:false )
		priority( nullable:true, inList:[1,2,3] )
		shortName( blank:true, nullable:true )

		// The following were the MoveBundleAsset fields
		moveBundle( nullable:true )
		sourceTeam( nullable:true )
		targetTeam( nullable:true )
		cart( blank:true, nullable:true )
		shelf( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version true
		autoTimestamp false
		id column:'asset_entity_id'
		columns {
			hasKvm sqltype: 'tinyint(1)'
			hasRemoteMgmt sqltype: 'tinyint(1)'
		}
	}
	
	String toString(){
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
	}
}
