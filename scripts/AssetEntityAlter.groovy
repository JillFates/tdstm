
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")
/*
 * Set Server as AssetEntity default type
 */
AssetEntity.executeUpdate("UPDATE from AssetEntity set assetType = 'Server' where assetType is null")
def eavAttribute = EavAttribute.findByAttributeCode("assetType")
def assetTypes = AssetEntity.findAll("From AssetEntity group by assetType")
assetTypes?.assetType?.each{ option->
	def eavAttributeOption = EavAttributeOption.findByValueAndAttribute(option,eavAttribute)
	if( !eavAttributeOption ){
		eavAttributeOption = new EavAttributeOption(
													value : option,
													attribute : eavAttribute
													)
		if ( !eavAttributeOption.validate() || !eavAttributeOption.save() ) {
			def etext = "Unable to create eavAttributeOption" +
			GormUtil.allErrorsString( eavAttributeOption )
			println etext
		}
	}
}
println"**************UPDATE MANUFACTURER***************"
// Create manufactures if not exist
def manufacturerResultMap = jdbcTemplate.queryForList("select manufacturer from asset_entity where manufacturer != '' and manufacturer is not null group by manufacturer")
	manufacturerResultMap.each{ result->
		def manufacturer = result.manufacturer.replaceAll("\\s+\$", "").replaceAll("^\\s+", "")
		def manufacturerInstance = Manufacturer.findByName( manufacturer )
		if( !manufacturerInstance ){
			def manufacuturers = Manufacturer.findAllByAkaIsNotNull()
			manufacuturers.each{manufacuturer->
				if(manufacuturer.aka.toLowerCase().contains( manufacturerValue.toLowerCase() )){
					manufacturerInstance = manufacuturer
				}
			}
			if(!manufacturerInstance){
				manufacturerInstance = new Manufacturer( name : manufacturer )
				if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
					def etext = "Unable to create manufacturerInstance" +
					GormUtil.allErrorsString( manufacturerInstance )
					println etext
				}
			}
		}
		manufacturer = manufacturer.replace("'","\\'")
		def updateQuery = "update asset_entity set manufacturer_id = ${manufacturerInstance.id} where manufacturer='${manufacturer}'"
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated '${manufacturer}' Manufacturer id ${manufacturerInstance.id} for ${updated} assets"
	}
println"**************UPDATE MODEL***************"
//Create model if not exist
def modelResultMap = jdbcTemplate.queryForList("select distinct model, manufacturer as manufacturer, asset_type as assetType from asset_entity where model != '' and model is not null and manufacturer != '' and manufacturer is not null and asset_type != '' and asset_type is not null order by model")
	modelResultMap.each{ result->
		def manufacturerInstance = result.manufacturer ? Manufacturer.findByName( result.manufacturer ) : ""
		def model = result.model.replaceAll("\\s+\$", "").replaceAll("^\\s+", "")
		def assetType = result.assetType
		if( !manufacturerInstance ){
			def manufacuturers = Manufacturer.findAllByAkaIsNotNull()
			manufacuturers.each{manufacuturer->
				if(manufacuturer.aka.toLowerCase().contains( result.manufacturer.toLowerCase() )){
					manufacturerInstance = manufacuturer
				}
			}
		}
		if( manufacturerInstance && assetType ){
			def modelInstance = Model.findWhere(modelName:model,assetType : assetType,manufacturer: manufacturerInstance  )
			if(!modelInstance){
				def models = Model.findAllByManufacturerAndAkaIsNotNull( manufacturerInstance ).findAll{it.assetType == assetType}
				models.each{ 
					if(it.aka.toLowerCase().contains( model.toLowerCase() )){
						modelInstance = it
					}
				}
				if(!modelInstance){
					modelInstance = new Model( modelName : model, assetType:assetType, manufacturer : manufacturerInstance )
					if ( !modelInstance.validate() || !modelInstance.save() ) {
						def etext = "Unable to create modelInstance" +
						GormUtil.allErrorsString( modelInstance )
						println etext
					}
				}
			}
			
			model = model.replace("'","\\'")
			def updateQuery = "update asset_entity set model_id = ${modelInstance.id} where model='${model}' and manufacturer='${manufacturerInstance.name}' and asset_type = '${assetType}'"
			def updated = jdbcTemplate.update(updateQuery)
			println "Updated '${model}' Model id ${modelInstance.id} for ${updated} assets"
		}
	}
println"**************Delete Asset properties ***************"
/*
 * Power
 */
def powerTypeAttribute = EavAttribute.findByAttributeCode('powerType')
if(powerTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[powerTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[powerTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[powerTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[powerTypeAttribute.id])
}
/*
 * PDU
 */
def pduPortAttribute = EavAttribute.findByAttributeCode('pduPort')
if(pduPortAttribute) {
		EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduPortAttribute])
		EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduPortAttribute])
		DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduPortAttribute])
		EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduPortAttribute.id])
	}

def pduQuantityAttribute = EavAttribute.findByAttributeCode('pduQuantity')
if(pduQuantityAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduQuantityAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduQuantityAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduQuantityAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduQuantityAttribute.id])
}

def pduTypeAttribute = EavAttribute.findByAttributeCode('pduType')
if(pduTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduTypeAttribute.id])
}
 /*
  * NIC
  */
def nicPortAttribute = EavAttribute.findByAttributeCode('nicPort')
if(nicPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[nicPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[nicPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[powerTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[nicPortAttribute.id])
}

/*
 * Fiber
 */
def fiberCabinetAttribute = EavAttribute.findByAttributeCode('fiberCabinet')
if(fiberCabinetAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberCabinetAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberCabinetAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberCabinetAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberCabinetAttribute.id])
}

def fiberTypeAttribute = EavAttribute.findByAttributeCode('fiberType')
if(fiberTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberTypeAttribute.id])
}

def fiberQuantityAttribute = EavAttribute.findByAttributeCode('fiberQuantity')
if(fiberQuantityAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberQuantityAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberQuantityAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberQuantityAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberQuantityAttribute.id])
}
/*
 *  HBA
 */
def hbaPortAttribute = EavAttribute.findByAttributeCode('hbaPort')
if(hbaPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[hbaPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[hbaPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[hbaPortAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[hbaPortAttribute.id])
}
/*
 * KVM 
 */
def kvmDeviceAttribute = EavAttribute.findByAttributeCode('kvmDevice')
if(kvmDeviceAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[kvmDeviceAttribute])
 	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[kvmDeviceAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[kvmDeviceAttribute])
 	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[kvmDeviceAttribute.id])
}

def kvmPortAttribute = EavAttribute.findByAttributeCode('kvmPort')
if(kvmPortAttribute) {
 	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[kvmPortAttribute])
 	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[kvmPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[kvmPortAttribute])
 	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[kvmPortAttribute.id])
}
/*
 * usize
 */
def usizeAttribute = EavAttribute.findByAttributeCode('usize')
if(usizeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[usizeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[usizeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[usizeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[usizeAttribute.id])
}
/*
 * remoteMgmtPort
 */
def remoteMgmtPortAttribute = EavAttribute.findByAttributeCode('remoteMgmtPort')
if(remoteMgmtPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[remoteMgmtPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[remoteMgmtPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[remoteMgmtPortAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[remoteMgmtPortAttribute.id])
}
/*
 * replace newOrOld with planStatus
 */
def newOrOldAttribute = EavAttribute.findByAttributeCode('newOrOld')
if(newOrOldAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus' where eavAttribute = ?",[newOrOldAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='Plan Status' where id = ?",[newOrOldAttribute.id])
}
def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
if(planStatusAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus' where eavAttribute = ?",[planStatusAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='Plan Status' where id = ?",[planStatusAttribute.id])
}

/*
 * replace hinfo with os
 */
def hinfoAttribute = EavAttribute.findByAttributeCode('hinfo')
if(hinfoAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'OS' where eavAttribute = ?",[hinfoAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'os', frontendLabel = 'OS' where id = ?",[hinfoAttribute.id])
}
def osAttribute = EavAttribute.findByAttributeCode('os')
if(osAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'OS' where eavAttribute = ?",[osAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'os', frontendLabel = 'OS' where id = ?",[osAttribute.id])
}
DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute.id not in ( select ea.id from EavAttribute ea) )")

EavAttribute.executeUpdate("Update from EavAttribute set frontendInput='text' where attributeCode in('model','manufacturer')")

/*
 * Set Attributes order 
 */
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10 where attributeCode = 'application'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 10 where attribute = ?",[EavAttribute.findByAttributeCode('application')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 20 where attributeCode = 'assetName'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 20 where attribute = ?",[EavAttribute.findByAttributeCode('assetName')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30 where attributeCode = 'shortName'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 30 where attribute = ?",[EavAttribute.findByAttributeCode('shortName')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 40 where attributeCode = 'serialNumber'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 40 where attribute = ?",[EavAttribute.findByAttributeCode('serialNumber')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 50 where attributeCode = 'assetTag'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 50 where attribute = ?",[EavAttribute.findByAttributeCode('assetTag')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 60 where attributeCode = 'assetType'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 60 where attribute = ?",[EavAttribute.findByAttributeCode('assetType')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 70 where attributeCode = 'manufacturer'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 70 where attribute = ?",[EavAttribute.findByAttributeCode('manufacturer')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 80 where attributeCode = 'model'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 80 where attribute = ?",[EavAttribute.findByAttributeCode('model')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 100 where attributeCode = 'ipAddress'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 100 where attribute = ?",[EavAttribute.findByAttributeCode('ipAddress')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 110 where attributeCode = 'os'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 110 where attribute = ?",[EavAttribute.findByAttributeCode('os')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 120 where attributeCode = 'sourceLocation'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 120 where attribute = ?",[EavAttribute.findByAttributeCode('sourceLocation')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 130 where attributeCode = 'sourceRoom'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 130 where attribute = ?",[EavAttribute.findByAttributeCode('sourceRoom')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 140 where attributeCode = 'sourceRack'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 140 where attribute = ?",[EavAttribute.findByAttributeCode('sourceRack')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 150 where attributeCode = 'sourceRackPosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 150 where attribute = ?",[EavAttribute.findByAttributeCode('sourceRackPosition')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 160 where attributeCode = 'sourceBladeChassis'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 160 where attribute = ?",[EavAttribute.findByAttributeCode('sourceBladeChassis')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 170 where attributeCode = 'sourceBladePosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 170 where attribute = ?",[EavAttribute.findByAttributeCode('sourceBladePosition')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 180 where attributeCode = 'targetLocation'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 180 where attribute = ?",[EavAttribute.findByAttributeCode('targetLocation')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 190 where attributeCode = 'targetRoom'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 190 where attribute = ?",[EavAttribute.findByAttributeCode('targetRoom')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 200 where attributeCode = 'targetRack'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 200 where attribute = ?",[EavAttribute.findByAttributeCode('targetRack')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 210 where attributeCode = 'targetRackPosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 210 where attribute = ?",[EavAttribute.findByAttributeCode('targetRackPosition')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 220 where attributeCode = 'targetBladeChassis'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 220 where attribute = ?",[EavAttribute.findByAttributeCode('targetBladeChassis')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 230 where attributeCode = 'targetBladePosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 230 where attribute = ?",[EavAttribute.findByAttributeCode('targetBladePosition')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 240 where attributeCode = 'custom1'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 240 where attribute = ?",[EavAttribute.findByAttributeCode('custom1')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 250 where attributeCode = 'custom2'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 250 where attribute = ?",[EavAttribute.findByAttributeCode('custom2')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 260 where attributeCode = 'custom3'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 260 where attribute = ?",[EavAttribute.findByAttributeCode('custom3')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 270 where attributeCode = 'custom4'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 270 where attribute = ?",[EavAttribute.findByAttributeCode('custom4')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 280 where attributeCode = 'custom5'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 280 where attribute = ?",[EavAttribute.findByAttributeCode('custom5')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 290 where attributeCode = 'custom6'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 290 where attribute = ?",[EavAttribute.findByAttributeCode('custom6')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 300 where attributeCode = 'custom7'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 300 where attribute = ?",[EavAttribute.findByAttributeCode('custom7')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 310 where attributeCode = 'custom8'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 310 where attribute = ?",[EavAttribute.findByAttributeCode('custom8')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 320 where attributeCode = 'moveBundle'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 320 where attribute = ?",[EavAttribute.findByAttributeCode('moveBundle')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 330 where attributeCode = 'sourceTeam'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 330 where attribute = ?",[EavAttribute.findByAttributeCode('sourceTeam')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 340 where attributeCode = 'targetTeam'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 340 where attribute = ?",[EavAttribute.findByAttributeCode('targetTeam')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 350 where attributeCode = 'truck'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 350 where attribute = ?",[EavAttribute.findByAttributeCode('truck')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 360 where attributeCode = 'cart'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 360 where attribute = ?",[EavAttribute.findByAttributeCode('cart')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 370 where attributeCode = 'shelf'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 370 where attribute = ?",[EavAttribute.findByAttributeCode('shelf')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 380 where attributeCode = 'railType'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 380 where attribute = ?",[EavAttribute.findByAttributeCode('railType')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 390 where attributeCode = 'appOwner'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 390 where attribute = ?",[EavAttribute.findByAttributeCode('appOwner')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 400 where attributeCode = 'appSme'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 400 where attribute = ?",[EavAttribute.findByAttributeCode('appSme')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 410 where attributeCode = 'priority'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 410 where attribute = ?",[EavAttribute.findByAttributeCode('priority')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 420 where attributeCode = 'planStatus'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 420 where attribute = ?",[EavAttribute.findByAttributeCode('planStatus')])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 430 where attributeCode = 'currentStatus'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 430 where attribute = ?",[EavAttribute.findByAttributeCode('currentStatus')])
