package com.tds.asset

import Manufacturer
import Model
import MoveBundle
import PartyGroup
import Project
import ProjectTeam
import Rack
import Room

import java.util.Date

import com.tdssrc.grails.GormUtil

class AssetEntity extends com.tdssrc.eav.EavEntity {
	
	String application = ""
	String assetName
	String shortName
	String assetType = "Server"
	Integer priority
	String planStatus
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
	String appOwner = ""
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
	
	Integer dependencyBundle = 0
	
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
		planStatus( blank:true, nullable:true )
		purchaseDate( nullable:true )
		purchasePrice( nullable:true )
		department( blank:true, nullable:true )
		costCenter( blank:true, nullable:true )
		maintContract( blank:true, nullable:true )
		maintExpDate( nullable:true )
		retireDate( nullable:true )
		description( blank:true, nullable:true )
		supportType( blank:true, nullable:true )
		environment( blank:true, nullable:true, inList:['Production','DR','Development','QA','Staging'] )
		
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
		appOwner( blank:true, nullable:true )
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

		validation( blank:true, nullable:true, inList:['Discovery','Validated','DependencyReview','DependencyScan','BundleReady'] )		
		currentStatus( nullable:true )
		dependencyBundle( nullable:true )
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
		planStatus column:'new_or_old'
		sourceTeamMt column:'source_team_id'
		targetTeamMt column:'target_team_id'
		moveBundle ignoreNotFound:true
		owner ignoreNotFound: true
		columns {
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
}
