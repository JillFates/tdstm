package com.tds.asset

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SizeScale
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.TagAsset

import static com.tds.asset.AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION
import static com.tds.asset.AssetOptions.AssetOptionsType.PRIORITY_OPTION
import static com.tds.asset.AssetOptions.AssetOptionsType.STATUS_OPTION
import static com.tds.asset.AssetType.BLADE
import static com.tdsops.tm.enums.domain.AssetClass.DEVICE
import static com.tdsops.tm.enums.domain.AssetDependencyStatus.QUESTIONED
import static com.tdsops.tm.enums.domain.AssetDependencyStatus.UNKNOWN
import static com.tdsops.tm.enums.domain.AssetDependencyStatus.VALIDATED
import static com.tdsops.tm.enums.domain.AssetEntityPlanStatus.UNASSIGNED
import static com.tdsops.validators.CustomValidators.inList
import static com.tdsops.validators.CustomValidators.optionsClosure
import static com.tdsops.validators.CustomValidators.validateCustomFields

class AssetEntity {
	static final List<String> COMMON_FIELD_LIST = [
		'assetClass',
		'assetName',
		'description',
		'environment',
		'externalRefId',
		'id',
		'lastUpdated',
		'moveBundle',
		'priority',
		'planStatus',
		'supportType',
		'tagAssets',
		'validation'
	]

	static final List<String> RAIL_TYPES = [
		'Rails',
		'Snap Rails',
		'Screw Rails',
		'Ears',
		'Shelf',
		'None'
	]

	static String alternateKey = 'assetName'

	AssetClass assetClass = DEVICE
	String application = ''
	String assetName
	String shortName
	String assetType = 'Server'
	Integer priority
	String planStatus = UNASSIGNED
	Date   purchaseDate
	Double purchasePrice
	String department
	String costCenter
	String maintContract
	Date   maintExpDate
	Date   retireDate
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

	String virtualHost      // TODO : JPM 9/2014 - drop the column virtualHost as this is no longer legitimate with true dependencies
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
	String appSme = ''

	// MoveBundleAsset fields
	String validation

	String externalRefId

	Integer    dependencyBundle = 0
	Integer    size
	SizeScale  scale
	Integer    rateOfChange
	Person     modifiedBy
	Collection tagAssets
	Collection comments

	Date dateCreated
	Date lastUpdated

	static hasMany = [comments: AssetComment, tagAssets: TagAsset]

	static constraints = {
		application nullable: true
		assetName blank: false
		shortName nullable: true
		assetType nullable: true
		priority nullable: true, validator: inList(optionsClosure(PRIORITY_OPTION), 'priority')
		planStatus nullable: true, validator: inList(optionsClosure(STATUS_OPTION), 'planStatus')
		purchaseDate nullable: true
		purchasePrice nullable: true
		department nullable: true
		costCenter nullable: true
		maintContract nullable: true
		maintExpDate nullable: true
		retireDate nullable: true
		description nullable: true
		supportType nullable: true
		environment nullable: true, size: 0..20, validator: inList(optionsClosure(ENVIRONMENT_OPTION), 'environment')

		// we can save some redundant space by dynamically setting all 96 columns to be nullable in a loop;
		// since the constraints DSL is implemented as method calls with the property name as the method
		// name and a single Map argument containing the data for the various contraints, it's simple
		// to dynamically create a method call to be called on the closure's delegate:
		Map nullableTrue = Collections.singletonMap('nullable', true)
		(1..96).each { "custom$it"(nullableTrue) }

		project nullable: false

		serialNumber nullable: true
		assetTag nullable: true
		manufacturer nullable: true
		model nullable: true
		ipAddress nullable: true
		os nullable: true
		usize nullable: true

		sourceRackPosition nullable: true
		sourceChassis nullable: true
		sourceBladePosition nullable: true

		targetRackPosition nullable: true
		targetChassis nullable: true
		targetBladePosition nullable: true

		virtualHost nullable: true
		truck nullable: true
		cart nullable: true
		shelf nullable: true
		railType nullable: true, inList: RAIL_TYPES

		// TODO : owner should not be nullable - remove and test
		owner nullable: true
		roomSource nullable: true
		rackSource nullable: true
		roomTarget nullable: true
		rackTarget nullable: true
		appOwner nullable: true
		appSme nullable: true

		// MoveBundleAsset fields
		validation nullable: true, size: 0..20, inList: ValidationType.list
		dependencyBundle nullable: true
		externalRefId nullable: true

		size nullable: true
		scale nullable: true, inList: SizeScale.keys
		rateOfChange nullable: true
		modifiedBy nullable: true

		custom1 validator: validateCustomFields()
		dateCreated nullable: true
		lastUpdated nullable: true
	}

	static mapping = {
		appOwner column: 'app_owner_id'
		autoTimestamp false
		id column: 'asset_entity_id'
		maintExpDate sqltype: 'date'
		modifiedBy column: 'modified_by'
		moveBundle ignoreNotFound: true
		os column: 'hinfo'
		owner ignoreNotFound: true
		rateOfChange sqltype: 'int(4)'
		retireDate sqltype: 'date'
		tablePerHierarchy false
		autoTimestamp false
		columns {
			id column: 'asset_entity_id'
		}
	}

	// Need to indicate the getters that would otherwise be mistaken as db properties
	static transients = ['modelName', 'moveBundleName', 'depUp', 'depDown', 'depToResolve', 'depToConflict']

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	String toString() {
		"id:$id name:$assetName tag:$assetTag serial#:$serialNumber"
	}

	/*
	 *  methods for filter/sort
	 */
	String getModelName() {
		model?.modelName
	}

	String getMoveBundleName() {
		moveBundle?.name
	}

	/**
	 * The manufacturer of the asset, either the model.manufacturer or this.manufacturer where the model takes precedence
	 */
	Manufacturer getManufacturer() {
		model ? model.manufacturer : manufacturer
	}

	/**
	 * Used to access the AssetType as we normalize to the model.assetType in the following way:
	 * use the model.assetType for those with a model and assetClass == AssetClass.DEVICE,
	 * in case that there is no model and this.assetType == 'Files' we change this to 'Logical Storage'
	 * return AssetEntity.assetType in any other case.
	 */
	String getAssetType() {
		String at = assetType
		if (assetClass == DEVICE) {
			at = model ? model.assetType : at == 'Files' ? 'Logical Storage' : at
		}
		return at
	}

	// Legacy accessor methods to support referencing the pre-normalized loc/room/rack information
	String getSourceLocationName() { roomSource?.location ?: ''}
	String getSourceRackName()     { rackSource?.tag ?: ''}
	String getSourceRoomName()     { roomSource?.roomName ?: ''}
	String getTargetLocationName() { roomTarget?.location ?: ''}
	String getTargetRackName()     { rackTarget?.tag ?: ''}
	String getTargetRoomName()     { roomTarget?.roomName ?: ''}

	/**
	 * The usize is now coming from the model vs the asset directly
	 */
	Integer getUsize() {
		if (assetClass == DEVICE && model) {
			model.usize
		}
	}

	/**
	 * Whether the asset is a Server / VM
	 */
	boolean isaDevice() {
		!isaApplication() && !isaNetwork() && !isaStorage() && !isaDatabase()
	}

	boolean isaDatabase() {
		assetType?.toLowerCase() == 'database'
	}

	boolean isaApplication() {
		assetType?.toLowerCase() == 'application'
		// return entityType == AssetEntityType.APPLICATION
	}

	boolean isaNetwork() {
		return false
		// TODO - Fix isNetwork when domain is implemented
		// return assetType.toLowerCase() == 'network'
		// return entityType == AssetEntityType.NETWORK
	}

	/**
	 * Whether the asset is a Storage
	 */
	boolean isaStorage() {
		['files', 'storage'].contains(assetType?.toLowerCase())
		// return entityType == AssetEntityType.STORAGE
	}

	/**
	 * Whether the asset is a logic type of object (e.g. Network, Database, Storage)
	 */
	boolean isaLogicalType() {
		!isaDevice() && !isaApplication()
	}

	/**
	 * Whether the asset is a VM
	 */
	boolean isaVM() {
		assetClass == DEVICE && (model?.assetType in AssetType.virtualServerTypes || assetType in AssetType.virtualServerTypes)
	}

	/**
	 * Whether the asset is a Blade
	 */
	boolean isaBlade() {
		assetClass == DEVICE && (model?.assetType == BLADE.toString() || assetType == BLADE.toString())
	}

	/**
	 * Whether the asset is a Chassis.
	 */
	boolean isaChassis() {
		assetType in AssetType.bladeChassisTypes
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
	 * Count of dependencies to dependent and status is not Validated.
	 */
	int getDepUp() {
		AssetDependency.countByDependentAndStatusNotEqual(this, 'Validated')
	}

	/**
	 * Count of dependencies to assets and the status is not Validated.
	 */
	int getDepDown() {
		AssetDependency.countByAssetAndStatusNotEqual(this, 'Validated')
	}

	/**
	 * Count of dependencies to assets where status in QUESTIONED,UNKNOWN.
	 */
	int getDepToResolve() {
		executeQuery('''
			SELECT COUNT(*)
			FROM AssetDependency
			WHERE status IN (:status)
			  AND (asset=:asset OR dependent=:asset)
		''', [asset: this, status: [UNKNOWN, QUESTIONED]])[0]
	}

	/**
	 * Count of dependencies to assets where the status in QUESTIONED,UNKNOWN,VALIDATED
	 */
	int getDepToConflict() {
		executeQuery('''
			SELECT COUNT(*)
			FROM AssetDependency
			WHERE status IN (:status)
			  AND asset=:asset
			  AND dependent.moveBundle!=:bundle
		''', [asset: this, bundle: moveBundle, status: [VALIDATED, UNKNOWN, QUESTIONED]])[0]
	}

	/**
	 * Whether a particular asset has cables.
	 */
	boolean isCableExist() {
		AssetCableMap.countByAssetFrom(this)
	}


	List<AssetDependency> supportedDependencies(){
		return AssetDependency.fetchSupportedDependenciesOf(this)
	}

	List<AssetDependency> requiredDependencies(){
		return AssetDependency.fetchRequiredDependenciesOf(this)
	}

	/**
	 * Clone this Entity and replace properties if a map is specified
	 * @param replaceKeys
	 * @return
	 */
	AssetEntity clone(Map replaceKeys = [:]){
		AssetEntity clonedAsset = GormUtil.cloneDomain(this, replaceKeys) as AssetEntity
		return clonedAsset
	}
}
