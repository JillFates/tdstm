import com.tdssrc.grails.GormUtil
class AssetEntity extends com.tdssrc.eav.EavEntity {
	String assetName	
	String assetType
	String assetTag
	String serialNumber	
	String manufacturer
	String model
	String application = ""
	PartyGroup owner
	Rack rackSource
	String sourceLocation
	String sourceRoom
	String sourceRack
	Integer sourceRackPosition
	Rack rackTarget
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
	String bladeSize
	String sourceBladeChassis
	Integer sourceBladePosition
	String targetBladeChassis
	Integer targetBladePosition	

	// MoveBundleAsset fields
	MoveBundle moveBundle
	ProjectTeam sourceTeam
	ProjectTeam targetTeam
	String cart
	String shelf
	
	// Custom fields
	String custom1
	String custom2
	String custom3
	String custom4
	String custom5
	String custom6
	String custom7
	String custom8
	Integer currentStatus
	
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
		rackSource( nullable:true )
		rackTarget( nullable:true )
		bladeSize( blank:true, nullable:true )
		sourceBladeChassis( blank:true, nullable:true )
		sourceBladePosition( nullable:true )
		targetBladeChassis( blank:true, nullable:true )
		targetBladePosition( nullable:true )

		// The following were the MoveBundleAsset fields
		moveBundle( nullable:true )
		sourceTeam( nullable:true )
		targetTeam( nullable:true )
		cart( blank:true, nullable:true )
		shelf( blank:true, nullable:true )
		
		// custom fields
		custom1( blank:true, nullable:true )
		custom2( blank:true, nullable:true )
		custom3( blank:true, nullable:true )
		custom4( blank:true, nullable:true )
		custom5( blank:true, nullable:true )
		custom6( blank:true, nullable:true )
		custom7( blank:true, nullable:true )
		custom8( blank:true, nullable:true )
		currentStatus( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version true
		autoTimestamp false
		id column:'asset_entity_id'
		moveBundle ignoreNotFound:true
		columns {
			hasKvm sqltype: 'tinyint(1)'
			hasRemoteMgmt sqltype: 'tinyint(1)'
		}
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def afterInsert = {
		updateRacks()
	}
	def afterUpdate = {
		updateRacks()
	}
	String toString(){
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
	}
	
	def updateRacks() {
		try{
			// Make sure the asset points to source/target racks if there is enough information for it
			if(assetType != 'Blade' && project != null) {
				if( sourceRack ) {
					rackSource = Rack.findOrCreateWhere(source:1, 'project.id':project.id, location:sourceLocation, room:sourceRoom, tag:sourceRack)
					save(flush:true)
				}
				if( targetRack ) {
					rackTarget = Rack.findOrCreateWhere(source:0, 'project.id':project.id, location:targetLocation, room:targetRoom, tag:targetRack)
					save(flush:true)
				}
			}
		} catch( Exception ex ){
			println"$ex"
		}
	}
	
}
