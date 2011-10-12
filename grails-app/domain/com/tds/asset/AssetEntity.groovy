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
	
	String custom1
	String custom2
	String custom3
	String custom4
	String custom5
	String custom6
	String custom7
	String custom8
	
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
	
	static hasMany = [
		assetEntityVarchars : AssetEntityVarchar
	]
	
	static constraints = {
		application( blank:true, nullable:true )
		assetName( blank:true, nullable:true )
		shortName( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		priority( blank:true, nullable:true )
		planStatus( blank:true, nullable:true )
		purchaseDate( blank:true, nullable:true )
		purchasePrice( blank:true, nullable:true )
		department( blank:true, nullable:true )
		costCenter( blank:true, nullable:true )
		maintContract( blank:true, nullable:true )
		maintExpDate( blank:true, nullable:true )
		
		custom1( blank:true, nullable:true )
		custom2( blank:true, nullable:true )
		custom3( blank:true, nullable:true )
		custom4( blank:true, nullable:true )
		custom5( blank:true, nullable:true )
		custom6( blank:true, nullable:true )
		custom7( blank:true, nullable:true )
		custom8( blank:true, nullable:true )
		
		moveBundle( blank:true, nullable:true )
		project( blank:true, nullable:true )
		
		serialNumber( blank:true, nullable:true )
		assetTag( blank:true, nullable:true )
		manufacturer( blank:true, nullable:true )
		model( blank:true, nullable:true )
		ipAddress( blank:true, nullable:true )
		os( blank:true, nullable:true )
		usize( blank:true, nullable:true )
		
		sourceLocation( blank:true, nullable:true )
		sourceRoom( blank:true, nullable:true )
		sourceRack( blank:true, nullable:true )
		sourceRackPosition( blank:true, nullable:true )
		sourceBladeChassis( blank:true, nullable:true )
		sourceBladePosition( blank:true, nullable:true )
	
		targetLocation( blank:true, nullable:true )
		targetRoom( blank:true, nullable:true )
		targetRack( blank:true, nullable:true )
		targetRackPosition( blank:true, nullable:true )
		targetBladeChassis( blank:true, nullable:true )
		targetBladePosition( blank:true, nullable:true )
	
		virtualHost( blank:true, nullable:true )
		truck( blank:true, nullable:true )
		cart( blank:true, nullable:true )
		shelf( blank:true, nullable:true )
		railType( blank:true, nullable:true )
		
		
		owner( blank:true, nullable:true )
		rackSource( blank:true, nullable:true )
		roomSource( blank:true, nullable:true )
		rackTarget( blank:true, nullable:true )
		roomTarget( blank:true, nullable:true )
		appOwner( blank:true, nullable:true )
		appSme( blank:true, nullable:true )
	
		// MoveBundleAsset fields
		sourceTeamMt( blank:true, nullable:true )
		targetTeamMt( blank:true, nullable:true )
		sourceTeamLog( blank:true, nullable:true )
		targetTeamLog( blank:true, nullable:true )
		sourceTeamSa( blank:true, nullable:true )
		targetTeamSa( blank:true, nullable:true )
		sourceTeamDba( blank:true, nullable:true )
		targetTeamDba( blank:true, nullable:true )
		
		
		currentStatus( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version true
		autoTimestamp false
		tablePerHierarchy false
		id column:'asset_entity_id'
		os column:'hinfo'
		planStatus column:'new_or_old'
		sourceTeamMt column:'source_team_id'
		targetTeamMt column:'target_team_id'
		moveBundle ignoreNotFound:true
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
			if(assetType != 'Blade' && project != null ) {
				if(sourceLocation && sourceRoom ){
					roomSource = Room.findOrCreateWhere(source:1, 'project.id':project.id, location:sourceLocation, roomName:sourceRoom )
					if( sourceRack ) {
						rackSource = Rack.findOrCreateWhere(source:1, 'project.id':project.id, location:sourceLocation, 'room.id':roomSource?.id, tag:sourceRack)
					}
					save(flush:true)
					
				}
				if(targetLocation && targetRoom){
					roomTarget = Room.findOrCreateWhere(source:0, 'project.id':project.id, location:targetLocation, roomName:targetRoom )
					if( targetRack ) {
						rackTarget = Rack.findOrCreateWhere(source:0, 'project.id':project.id, location:targetLocation, 'room.id':roomTarget?.id, tag:targetRack)
					}
					save(flush:true)
				}
			}
		} catch( Exception ex ){
			println"$ex"
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
