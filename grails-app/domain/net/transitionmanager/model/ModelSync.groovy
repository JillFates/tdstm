package net.transitionmanager.model

import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.manufacturer.Manufacturer

class ModelSync {

	String modelName
	String description
	String assetType
	String modelStatus
	String layoutStyle

	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	String bladeHeight = 'Half'

	// Product information
	Integer usize
	Integer useImage
	Integer height
	Integer weight
	Integer depth
	Integer width
	Integer powerUse
	Integer powerNameplate
	Integer powerDesign
	String productLine
	String modelFamily
	Date endOfLifeDate
	String endOfLifeStatus
	String sourceURL        // URL of where model data was derived from

	// Room Associated properties
	Boolean roomObject      // TODO change to tinyint 0/1

	Integer masterVersion   // This contains the model's version from the master

	Person createdBy
	Person updatedBy
	Person validatedBy

	// Properties distinct to ModelSync

	// TO BE DELETED
	String  importStatus
	Integer sourceTDS
	Integer sourceTDSVersion
	long    modelTempId
	String  aka
	long    manufacturerTempId
	String  manufacturerName
	byte[]  frontImage
	byte[]  rearImage
	Project modelScope

	static belongsTo = [batch: ModelSyncBatch, manufacturer: Manufacturer]

// 	static hasMany = [ modelConnectors : ModelConnectorSync ]

	static constraints = {
		assetType nullable: true
		bladeCount nullable: true
		bladeHeight nullable: true, inList: ['Half', 'Full']
		bladeLabelCount nullable: true
		bladeRows nullable: true
		createdBy nullable: true
		depth nullable: true
		description nullable: true
		endOfLifeDate nullable: true
		endOfLifeStatus nullable: true
		height nullable: true
		layoutStyle nullable: true
		modelFamily nullable: true
		modelName blank: false, unique: ['manufacturer']
		modelStatus nullable: true, inList: ['new', 'full', 'valid']
		powerDesign nullable: true
		powerNameplate nullable: true
		powerUse nullable: true
		productLine nullable: true
		roomObject nullable: true
		sourceURL nullable: true
		updatedBy nullable: true
		usize nullable: true, inList: 1..52
		validatedBy nullable: true
		weight nullable: true
		width nullable: true

		// TODO - DELETE THIS
		frontImage nullable: true
		modelScope nullable: true
		rearImage nullable: true
		sourceTDS nullable: true
		sourceTDSVersion nullable: true
	}

	static mapping = {
		version false
		columns {
			frontImage sqlType: 'LONGBLOB'
			id         column:  'model_id'
			modelName  column:  'name'
			rearImage  sqlType: 'LONGBLOB'
			sourceTDS  sqltype: 'tinyint'
			useImage   sqltype: 'tinyint'
		}
	}

	String toString() { modelName }
}
