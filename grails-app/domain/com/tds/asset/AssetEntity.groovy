package com.tds.asset

import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.SizeScale;
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil;


class AssetEntity extends com.tdssrc.eav.EavEntity {
	
	String application = ""
	String assetName
	String shortName
	String assetType = "Server"
	Integer priority
	String planStatus = AssetEntityPlanStatus.UNASSIGNED
	Date purchaseDate
	Double purchasePrice
	String department
	String costCenter
	String maintContract
	Date maintExpDate
	Date retireDate
	String description
	String supportType
	String environment
	
	String custom1
	String custom2
	String custom3
	String custom4
	String custom5
	String custom6
	String custom7
	String custom8
	String custom9
	String custom10
	String custom11
	String custom12
	String custom13
	String custom14
	String custom15
	String custom16
	String custom17
	String custom18
	String custom19
	String custom20
	String custom21
	String custom22
	String custom23
	String custom24
	String custom25
	String custom26
	String custom27
	String custom28
	String custom29
	String custom30
	String custom31
	String custom32
	String custom33
	String custom34
	String custom35
	String custom36
	String custom37
	String custom38
	String custom39
	String custom40
	String custom41
	String custom42
	String custom43
	String custom44
	String custom45
	String custom46
	String custom47
	String custom48
	String custom49
	String custom50
	String custom51
	String custom52
	String custom53
	String custom54
	String custom55
	String custom56
	String custom57
	String custom58
	String custom59
	String custom60
	String custom61
	String custom62
	String custom63
	String custom64
	
	MoveBundle moveBundle
	Project project
	
	String serialNumber
	String assetTag
	Manufacturer manufacturer
	Model model
	String ipAddress
	String os
	Integer usize
	
	String sourceLocation
	String sourceRoom
	String sourceRack
	Integer sourceRackPosition
	String sourceBladeChassis
	Integer sourceBladePosition

	String targetLocation
	String targetRoom
	String targetRack
	Integer targetRackPosition
	String targetBladeChassis
	Integer targetBladePosition

	String virtualHost
	String truck
	String cart
	String shelf
	String railType
	
	PartyGroup owner
	Rack rackSource
	Room roomSource
	Rack rackTarget
	Room roomTarget
	Person appOwner 
	String appSme = ""

	// MoveBundleAsset fields
	ProjectTeam sourceTeamMt
	ProjectTeam targetTeamMt
	ProjectTeam sourceTeamLog
	ProjectTeam targetTeamLog
	ProjectTeam sourceTeamSa
	ProjectTeam targetTeamSa
	ProjectTeam sourceTeamDba
	ProjectTeam targetTeamDba
	
	Integer currentStatus
	String validation
	
	String externalRefId
	
	Integer dependencyBundle = 0
    Integer size
    SizeScale scale
    Integer rateOfChange
	Person modifiedBy
    
	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar,
		comments : AssetComment
	]
	
	static constraints = {
		application( blank:true, nullable:true )
		assetName( blank:true, nullable:true )
		shortName( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		priority( nullable:true )
		planStatus( blank:true, nullable:true, inList:AssetEntityPlanStatus.list )
		purchaseDate( nullable:true )
		purchasePrice( nullable:true )
		department( blank:true, nullable:true )
		costCenter( blank:true, nullable:true )
		maintContract( blank:true, nullable:true )
		maintExpDate( nullable:true )
		retireDate( nullable:true )
		description( blank:true, nullable:true )
		supportType( blank:true, nullable:true )
		environment( blank:true, nullable:true )
		
		custom1( blank:true, nullable:true )
		custom2( blank:true, nullable:true )
		custom3( blank:true, nullable:true )
		custom4( blank:true, nullable:true )
		custom5( blank:true, nullable:true )
		custom6( blank:true, nullable:true )
		custom7( blank:true, nullable:true )
		custom8( blank:true, nullable:true )
		custom9( blank:true, nullable:true )
		custom10( blank:true, nullable:true )
		custom11( blank:true, nullable:true )
		custom12( blank:true, nullable:true )
		custom13( blank:true, nullable:true )
		custom14( blank:true, nullable:true )
		custom15( blank:true, nullable:true )
		custom16( blank:true, nullable:true )
		custom17( blank:true, nullable:true )
		custom18( blank:true, nullable:true )
		custom19( blank:true, nullable:true )
		custom20( blank:true, nullable:true )
		custom21( blank:true, nullable:true )
		custom22( blank:true, nullable:true )
		custom23( blank:true, nullable:true )
		custom24( blank:true, nullable:true )
		custom25( blank:true, nullable:true )
		custom26( blank:true, nullable:true )
		custom27( blank:true, nullable:true )
		custom28( blank:true, nullable:true )
		custom29( blank:true, nullable:true )
		custom30( blank:true, nullable:true )
		custom31( blank:true, nullable:true )
		custom32( blank:true, nullable:true )
		custom33( blank:true, nullable:true )
		custom34( blank:true, nullable:true )
		custom35( blank:true, nullable:true )
		custom36( blank:true, nullable:true )
		custom37( blank:true, nullable:true )
		custom38( blank:true, nullable:true )
		custom39( blank:true, nullable:true )
		custom40( blank:true, nullable:true )
		custom41( blank:true, nullable:true )
		custom42( blank:true, nullable:true )
		custom43( blank:true, nullable:true )
		custom44( blank:true, nullable:true )
		custom45( blank:true, nullable:true )
		custom46( blank:true, nullable:true )
		custom47( blank:true, nullable:true )
		custom48( blank:true, nullable:true )
		custom49( blank:true, nullable:true )
		custom50( blank:true, nullable:true )
		custom51( blank:true, nullable:true )
		custom52( blank:true, nullable:true )
		custom53( blank:true, nullable:true )
		custom54( blank:true, nullable:true )
		custom55( blank:true, nullable:true )
		custom56( blank:true, nullable:true )
		custom57( blank:true, nullable:true )
		custom58( blank:true, nullable:true )
		custom59( blank:true, nullable:true )
		custom60( blank:true, nullable:true )
		custom61( blank:true, nullable:true )
		custom62( blank:true, nullable:true )
		custom63( blank:true, nullable:true )
		custom64( blank:true, nullable:true )
		
		moveBundle( nullable:true )
		project( nullable:true )
		
		serialNumber( blank:true, nullable:true )
		assetTag( blank:true, nullable:true )
		manufacturer( nullable:true )
		model( nullable:true )
		ipAddress( blank:true, nullable:true )
		os( blank:true, nullable:true )
		usize( nullable:true )
		
		sourceLocation( blank:true, nullable:true )
		sourceRoom( blank:true, nullable:true )
		sourceRack( blank:true, nullable:true )
		sourceRackPosition( nullable:true )
		sourceBladeChassis( blank:true, nullable:true )
		sourceBladePosition( nullable:true )
	
		targetLocation( blank:true, nullable:true )
		targetRoom( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		targetRackPosition( nullable:true )
		targetBladeChassis( blank:true, nullable:true )
		targetBladePosition( nullable:true )
	
		virtualHost( blank:true, nullable:true )
		truck( blank:true, nullable:true )
		cart( blank:true, nullable:true )
		shelf( blank:true, nullable:true )
		railType( blank:true, nullable:true )
		
		// TODO : owner should not be nullable - remove and test
		owner( nullable:true )
		rackSource( nullable:true )
		roomSource( nullable:true )
		rackTarget( nullable:true )
		roomTarget( nullable:true )
		appOwner( nullable:true )
		appSme( blank:true, nullable:true )
	
		// MoveBundleAsset fields
		sourceTeamMt( nullable:true )
		targetTeamMt( nullable:true )
		sourceTeamLog( nullable:true )
		targetTeamLog( nullable:true )
		sourceTeamSa( nullable:true )
		targetTeamSa( nullable:true )
		sourceTeamDba( nullable:true )
		targetTeamDba( nullable:true )

		validation( blank:true, nullable:true, inList:ValidationType.getList() )		
		currentStatus( nullable:true )
		dependencyBundle( nullable:true )
		externalRefId( blank:true, nullable:true )
        
        size( nullable:true )
        scale( nullable:true, inList:SizeScale.getKeys() )
        rateOfChange( nullable:true )
		modifiedBy( nullable:true )
	}
	
	static mapping  = {	
		version true
		autoTimestamp false
		sourceTeamMt ignoreNotFound: true
	    targetTeamMt ignoreNotFound: true
	    sourceTeamLog ignoreNotFound: true
	    targetTeamLog ignoreNotFound: true
	    sourceTeamSa ignoreNotFound: true
	    targetTeamSa ignoreNotFound: true
	    sourceTeamDba ignoreNotFound: true
	    targetTeamDba ignoreNotFound: true
		tablePerHierarchy false
		id column:'asset_entity_id'
		os column:'hinfo'
		sourceTeamMt column:'source_team_id'
		targetTeamMt column:'target_team_id'
		appOwner column:'app_owner_id'
		moveBundle ignoreNotFound:true
		owner ignoreNotFound: true
		columns {
			hasRemoteMgmt sqltype: 'tinyint(1)'
		}
		modifiedBy column:'modified_by'
	}

	// Need to indicate the getters that would otherwise be mistaken as db properties
	static transients = ['modelName', 'moveBundleName', 'depUp', 'depDown', 'depToResolve', 'depToConflict']

	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
		modifiedBy = Person.loggedInPerson

	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
		modifiedBy = Person.loggedInPerson
	}
	
	/*def afterInsert = {
		updateRacks()
	}
	def afterUpdate = {
		updateRacks()
	}*/
	String toString(){
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
	}
	
	def updateRacks() {
		try{
			// Make sure the asset points to source/target racks if there is enough information for it
			if ( project != null ) {
				if( sourceLocation && sourceRoom ){
					roomSource = Room.findOrCreateWhere(source:1, 'project.id':project.id, location:sourceLocation, roomName:sourceRoom )
					if( sourceRack && assetType != 'Blade') {
						rackSource = Rack.findOrCreateWhere(source:1, 'project.id':project.id, location:sourceLocation, 'room.id':roomSource?.id, tag:sourceRack)
					}
					save(flush:true)
					
				}
				if (targetLocation && targetRoom){
					roomTarget = Room.findOrCreateWhere(source:0, 'project.id':project.id, location:targetLocation, roomName:targetRoom )
					if( targetRack && assetType != 'Blade') {
						rackTarget = Rack.findOrCreateWhere(source:0, 'project.id':project.id, location:targetLocation, 'room.id':roomTarget?.id, tag:targetRack)
					}
					save(flush:true)
				}
			}
		} catch( Exception ex ){
			log.error "$ex"
		}
	}
	/*
	 *  methods for JMESA filter/sort
	 */
	def getModelName(){
		return this.model?.modelName
	}
	def getMoveBundleName(){
		return this.moveBundle?.name
	}

	/**
	 * Use to determine if the asset is a Server / VM
	 * @return boolean
	 */
	def isaDevice() {
		return ( ! isaApplication() && ! isaNetwork() && ! isaStorage() && ! isaDatabase() ) 
	}

	/**
	 * Use to determine if the asset is a Database
	 * @return boolean
	 */

	def isaDatabase() {
		return assetType?.toLowerCase() == 'database'		
	}

	/**
	 * Use to determine if the asset is an Application
	 * @return boolean
	 */

	def isaApplication() {
		return assetType?.toLowerCase() == 'application'
		// return entityType == AssetEntityType.APPLICATION
	}

	/**
	 * Use to determine if the asset is a Network (presently stubbed out to return FALSE)
	 * @return boolean
	 */
	def isaNetwork() {
		return false
		// TODO - Fix isNetwork when domain is implemented
		// return assetType.toLowerCase() == 'network'		
		// return entityType == AssetEntityType.NETWORK
	}

	/**
	 * Use to determine if the asset is a Storage
	 * @return boolean
	 */
	def isaStorage() {
		return ['files','storage'].contains(assetType?.toLowerCase())
		// return entityType == AssetEntityType.STORAGE
	}

	/**
	 * Used to determine if the asset is considered a logic type of object (e.g. Network, Database, Storage)
	 * @return Boolean true if asset is logical
	 */
	def isaLogicalType() {
		return (! isaDevice() && ! isaApplication() )
	}

	/*
	// Used to get the super class of the asset such as Application or Database
	def getSuperObject(readonly = false) {
		if (isApplication()) {
			return readonly ? Application.read(id) : Application.get(id)
		} else if (isStorage() ) {
			return readonly ? Files.read(id) : Files.get(id)			
		} else if (isNetwork()) {
			// TODO - Fix getSuper when Network domain is implemented
			return this
		} else {
			return this
		}
	} 
	*/

	/**
	 * this method is used to count of dependencies to dependent and status is not Validated.
	 * @return dependencyUp Count
	 */
	def transient getDepUp(){
		return AssetDependency.countByDependentAndStatusNotEqual(this, 'Validated')
	}
	
	/**
	 * this method is used to count of dependencies to assets and the status is not Validated.
	 * @return dependencyDown Count
	 */
	def transient getDepDown(){
		return AssetDependency.countByAssetAndStatusNotEqual(this, 'Validated')
	}
	
	/**
	 * this method is used to count of dependencies to assets where status in QUESTIONED,UNKNOWN.
	 * @return
	 */
	def getDepToResolve(){
		return AssetDependency.findAll(" FROM AssetDependency ad \
				WHERE (ad.status IN (:status)) AND (ad.asset=:asset OR ad.dependent=:asset)",
				[asset:this, status:[AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.QUESTIONED]]).size()
		/*return AssetDependency.countByAssetAndStatusInList(this, 
			[AssetDependencyStatus.QUESTIONED,AssetDependencyStatus.UNKNOWN])*/
	}
	
	/**
	 * this method is used to count of dependencies to assets where the status in QUESTIONED,UNKNOWN,VALIDATED 
	 * of different MoveBundle.
	 * @return
	 */
	def getDepToConflict(){
		return AssetDependency.findAll(" FROM AssetDependency ad \
				WHERE ad.status IN (:status) AND ad.asset=:asset AND ad.dependent.moveBundle!=:bundle",
				[asset:this, bundle:this.moveBundle,
				 status:[AssetDependencyStatus.VALIDATED, AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.QUESTIONED]]).size()
	}
	
	/**
	 * This method is used to set source Room and location or target Room and Location when we get request from select box
	 * @param roomId : id of the room that we need to set
	 * @param source : a Boolean flag to determine whether request is for source or target
	 * @return : void
	 */
	def setRoomAndLoc(def roomId, Boolean source){
		def room = null
		if(roomId && roomId !='0')
			room = Room.read( roomId )
		
		//If flag is source then setting all source to requested value and vice versa .
		if( source ){
			roomSource = room
			sourceRoom = room ? room.roomName : null
			sourceLocation = room ? room?.location : null
		}
		if( !source ){
			roomTarget = room
			targetRoom = room ? room.roomName : null
			targetLocation = room ? room?.location : null
		}
	}
	/**
	 * This method is used to set source Rack  or target Rack when we get request from select box
	 * @param rackId : id of the room that we need to set
	 * @param source : a Boolean flag to determine whether request is for source or target
	 * @return : void
	 */
	def setRack(def rackId, Boolean source){
		def rack = null
		if(rackId && rackId !='0')
			rack = Rack.read( rackId )
		
		//If flag is source then setting all source to requested value and vice versa .
		if( source ){
			rackSource = rack
			sourceRack = rack ? rack.tag : null
		}
		if( !source ){
			rackTarget = rack
			targetRack = rack ? rack.tag : null
		}
	}
	/**
	 * This method is used to know whether a particular asset having cables or not.
	 */
	def isCableExist(){
		return AssetCableMap.findByAssetFrom(this)
	}
}
