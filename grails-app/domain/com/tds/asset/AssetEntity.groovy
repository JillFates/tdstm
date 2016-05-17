package com.tds.asset

import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.SizeScale;
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.validators.CustomValidators
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil;


class AssetEntity extends com.tdssrc.eav.EavEntity {
	
	AssetClass assetClass = AssetClass.DEVICE
	String application = ""
	String assetName
	String shortName
	String assetType = 'Server'
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
	String custom65
	String custom66
	String custom67
	String custom68
	String custom69
	String custom70
	String custom71
	String custom72
	String custom73
	String custom74
	String custom75
	String custom76
	String custom77
	String custom78
	String custom79
	String custom80
	String custom81
	String custom82
	String custom83
	String custom84
	String custom85
	String custom86
	String custom87
	String custom88
	String custom89
	String custom90
	String custom91
	String custom92
	String custom93
	String custom94
	String custom95
	String custom96
	
	MoveBundle moveBundle
	Project project
	
	String serialNumber
	String assetTag
	Manufacturer manufacturer
	Model model
	String ipAddress
	String os
	Integer usize
	
	Integer sourceRackPosition
	AssetEntity sourceChassis
	Integer sourceBladePosition

	Integer targetRackPosition
	AssetEntity targetChassis
	Integer targetBladePosition

	String virtualHost		// TODO : JPM 9/2014 - drop the column virtualHost as this is no longer legitimate with true dependencies
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
	String validation
	
	String externalRefId
	
	Integer dependencyBundle = 0
	Integer size
	SizeScale scale
	Integer rateOfChange
	Person modifiedBy
		
	static hasMany = [
		// assetEntityVarchars : AssetEntityVarchar,
		comments : AssetComment
	]
	
	static constraints = {
		application( blank:true, nullable:true )
		assetName( blank:false, nullable:false, size:0..255 )
		shortName( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		priority( nullable:true, validator: 
			CustomValidators.inList(
				CustomValidators.optionsClosure(AssetOptions.AssetOptionsType.PRIORITY_OPTION), 
				"priority")
		)
		planStatus( blank:true, nullable:true, validator: 
			CustomValidators.inList(
				CustomValidators.optionsClosure(AssetOptions.AssetOptionsType.STATUS_OPTION), 
				"planStatus")
		)
		purchaseDate( nullable:true )
		purchasePrice( nullable:true )
		department( blank:true, nullable:true )
		costCenter( blank:true, nullable:true )
		maintContract( blank:true, nullable:true )
		maintExpDate( nullable:true )
		retireDate( nullable:true )
		description( blank:true, nullable:true, size:0..255 )
		supportType( blank:true, nullable:true )
		environment( blank:true, nullable:true, size:0..20, validator: 
			CustomValidators.inList(
				CustomValidators.optionsClosure(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION), 
				"environment")
		)
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
		custom65( blank:true, nullable:true )
		custom66( blank:true, nullable:true )
		custom67( blank:true, nullable:true )
		custom68( blank:true, nullable:true )
		custom69( blank:true, nullable:true )
		custom70( blank:true, nullable:true )
		custom71( blank:true, nullable:true )
		custom72( blank:true, nullable:true )
		custom73( blank:true, nullable:true )
		custom74( blank:true, nullable:true )
		custom75( blank:true, nullable:true )
		custom76( blank:true, nullable:true )
		custom77( blank:true, nullable:true )
		custom78( blank:true, nullable:true )
		custom79( blank:true, nullable:true )
		custom80( blank:true, nullable:true )
		custom81( blank:true, nullable:true )
		custom82( blank:true, nullable:true )
		custom83( blank:true, nullable:true )
		custom84( blank:true, nullable:true )
		custom85( blank:true, nullable:true )
		custom86( blank:true, nullable:true )
		custom87( blank:true, nullable:true )
		custom88( blank:true, nullable:true )
		custom89( blank:true, nullable:true )
		custom90( blank:true, nullable:true )
		custom91( blank:true, nullable:true )
		custom92( blank:true, nullable:true )
		custom93( blank:true, nullable:true )
		custom94( blank:true, nullable:true )
		custom95( blank:true, nullable:true )
		custom96( blank:true, nullable:true )
		
		project( nullable:true )
		
		serialNumber( blank:true, nullable:true )
		assetTag( blank:true, nullable:true )
		manufacturer( nullable:true )
		model( nullable:true )
		ipAddress( blank:true, nullable:true )
		os( blank:true, nullable:true )
		usize( nullable:true )
		
		sourceRackPosition( nullable:true )
		sourceChassis( nullable:true )
		sourceBladePosition( nullable:true )
	
		targetRackPosition( nullable:true )
		targetChassis( nullable:true )
		targetBladePosition( nullable:true )
	
		virtualHost( blank:true, nullable:true )
		truck( blank:true, nullable:true )
		cart( blank:true, nullable:true )
		shelf( blank:true, nullable:true )
		railType( blank:true, nullable:true )
		
		// TODO : owner should not be nullable - remove and test
		owner( nullable:true )
		roomSource( nullable:true )
		rackSource( nullable:true )
		roomTarget( nullable:true )
		rackTarget( nullable:true )
		appOwner( nullable:true )
		appSme( blank:true, nullable:true )
	
		// MoveBundleAsset fields
		validation( blank:true, nullable:true, size:0..20, inList:ValidationType.getList() )		
		dependencyBundle( nullable:true )
		externalRefId( blank:true, nullable:true )
				
		size( nullable:true )
		scale( nullable:true, inList:SizeScale.getKeys() )
		rateOfChange( nullable:true )
		modifiedBy( nullable:true )
	}
	
	static mapping  = {	
		version           true
		autoTimestamp     false
		tablePerHierarchy false
		id                column: 'asset_entity_id'
		os                column: 'hinfo'
		appOwner          column: 'app_owner_id'
		modifiedBy        column: 'modified_by'
		moveBundle        ignoreNotFound: true
		owner             ignoreNotFound: true
		retireDate        sqltype: 'date'
		maintExpDate      sqltype: 'date'
		rateOfChange      sqltype: 'int(4)'
	}

	// Need to indicate the getters that would otherwise be mistaken as db properties
	static transients = ['modelName', 'moveBundleName', 'depUp', 'depDown', 'depToResolve', 'depToConflict']

	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
		// modifiedBy = Person.loggedInPerson

	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
		// modifiedBy = Person.loggedInPerson
	}
	
	String toString(){
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
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
	 * Used to access the manufacturer of the asset which will return either the model.manufacturer or this.manufacturer where the model takes precedence
	 */
	Manufacturer getManufacturer() {
		return (this.model ? this.model.manufacturer : this.manufacturer) 
	}

	/**
	 * Used to access the AssetType as we normalize to the model.assetType in the following way:
	 * use the model.assetType for those with a model and assetClass == AssetClass.DEVICE,
	 * in case that there is no model and this.assetType == 'Files' we change this to 'Logical Storage'
	 * return AssetEntity.assetType in any other case.
	 */
	String getAssetType() {
		String at = this.assetType
		if (this.assetClass == AssetClass.DEVICE) {
			at = this.model ? 
					 this.model.assetType : 
					 at == "Files" ? "Logical Storage" : at
		}

		return at
	}

	// Legacy accessor methods to support referencing the pre-normalized loc/room/rack information
	def getSourceLocation() { return this.roomSource?.location }
	def getSourceRack() { return this.rackSource?.tag }
	def getSourceRoom() { return this.roomSource?.roomName }
	def getTargetLocation() { return this.roomTarget?.location }
	def getTargetRack() { return this.rackTarget?.tag }
	def getTargetRoom() { return this.roomTarget?.roomName }

	/**
	 * The usize is now coming from the model vs the asset directly
	 */
	Integer getUsize() {
		Integer sz
		if (this.assetClass == AssetClass.DEVICE && this.model)
			sz = this.model.usize
		return sz
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

	/**
	 * Used to determine if an asset is a VM
	 * @return Boolean true if the device is a VM
	 */
	def isaVM() {
		return ( assetClass==AssetClass.DEVICE && ( model?.assetType==AssetType.VM.toString() || assetType==AssetType.VM.toString() ) )
	}

	/**
	 * Used to determine if an asset is a Blade
	 * @return Boolean true if the device is a VM
	 */
	def isaBlade() {
		return ( assetClass==AssetClass.DEVICE && (model?.assetType==AssetType.BLADE.toString() || assetType==AssetType.BLADE.toString() ) )
	}

	/**
	 * Determines if the asset is a Chassis.
	 * @return Boolean true if the asset is a chassis, false otherwise.
	 */
	def isaChassis(){
		return this.assetType in AssetType.getBladeChassisTypes()
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
	def setRoomAndLoc(def roomId, Boolean source) {
		throw new RuntimeException("AssetEntity.setRoomAndLoc() has been eliminated - see AssetEntityService.assignAssetToRoom")
		/*
		def room = null
		if(roomId && roomId !='0')
			room = Room.read( roomId )
		
		//If flag is source then setting all source to requested value and vice versa .
		if( source ){
			roomSource = room
		//	sourceRoom = room ? room.roomName : null
		//	sourceLocation = room ? room?.location : null
		}
		if( !source ){
			roomTarget = room
		//	targetRoom = room ? room.roomName : null
		}
		*/
	}
	/**
	 * This method is used to set source Rack  or target Rack when we get request from select box
	 * @param rackId : id of the room that we need to set
	 * @param source : a Boolean flag to determine whether request is for source or target
	 * @return : void
	 */
	def setRack(def rackId, Boolean source) {
		throw new RuntimeException("AssetEntity.setRack() has been eliminated - see")
		/*
		def rack = null
		if(rackId && rackId !='0')
			rack = Rack.read( rackId )
		
		//If flag is source then setting all source to requested value and vice versa .
		if( source ){
			rackSource = rack
		}
		if( !source ){
			rackTarget = rack
		}
		*/
	}
	/**
	 * This method is used to know whether a particular asset having cables or not.
	 */
	def isCableExist(){
		return AssetCableMap.findByAssetFrom(this)
	}

}
