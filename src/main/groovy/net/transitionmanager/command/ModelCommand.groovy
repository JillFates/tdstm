package net.transitionmanager.command

import net.transitionmanager.asset.Rack
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.ModelConnector
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project

class ModelCommand implements CommandObject{

	Long id

	String assetType

	Integer bladeCount
	String bladeHeight
	Integer bladeLabelCount
	Integer bladeRows
	Integer cpuCount
	String cpuType
	Double depth
	String description
	Date endOfLifeDate
	String endOfLifeStatus
	Double height
	String layoutStyle
	Manufacturer manufacturer
	Double memorySize
	String modelFamily
	String modelName
	String modelStatus
	Float powerDesign
	Float powerNameplate
	String powerType
	Float powerUse
	String productLine
	Boolean roomObject
	String sourceURL
	Integer sourceTDS = 1
	Integer sourceTDSVersion = 1
	Double storageSize
	Integer useImage
	Integer usize
	Double weight
	Integer width

}
