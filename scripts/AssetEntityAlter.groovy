
import com.tds.asset.AssetEntity;
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")
def masterDataTransferSet = DataTransferSet.findBySetCode("MASTER")
def walkThruDataTransferSet = DataTransferSet.findBySetCode("WALKTHROUGH")
def entityType = EavEntityType.findByEntityTypeCode('AssetEntity')
def attributeSet = EavAttributeSet.findByAttributeSetName('Server')

println"**************Delete Asset properties ***************"
/*
 * Power
 */
def powerTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('powerType',entityType)
if(powerTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[powerTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[powerTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[powerTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[powerTypeAttribute.id])
}
/*
 * PDU
 */
def pduPortAttribute = EavAttribute.findByAttributeCodeAndEntityType('pduPort',entityType)
if(pduPortAttribute) {
		EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduPortAttribute])
		EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduPortAttribute])
		DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduPortAttribute])
		EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduPortAttribute.id])
	}

def pduQuantityAttribute = EavAttribute.findByAttributeCodeAndEntityType('pduQuantity',entityType)
if(pduQuantityAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduQuantityAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduQuantityAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduQuantityAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduQuantityAttribute.id])
}

def pduTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('pduType',entityType)
if(pduTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduTypeAttribute.id])
}
 /*
  * NIC
  */
def nicPortAttribute = EavAttribute.findByAttributeCodeAndEntityType('nicPort',entityType)
if(nicPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[nicPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[nicPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[powerTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[nicPortAttribute.id])
}

/*
 * Fiber
 */
def fiberCabinetAttribute = EavAttribute.findByAttributeCodeAndEntityType('fiberCabinet',entityType)
if(fiberCabinetAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberCabinetAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberCabinetAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberCabinetAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberCabinetAttribute.id])
}

def fiberTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('fiberType',entityType)
if(fiberTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberTypeAttribute.id])
}

def fiberQuantityAttribute = EavAttribute.findByAttributeCodeAndEntityType('fiberQuantity',entityType)
if(fiberQuantityAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberQuantityAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberQuantityAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberQuantityAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberQuantityAttribute.id])
}
/*
 *  HBA
 */
def hbaPortAttribute = EavAttribute.findByAttributeCodeAndEntityType('hbaPort',entityType)
if(hbaPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[hbaPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[hbaPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[hbaPortAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[hbaPortAttribute.id])
}
/*
 * KVM 
 */
def kvmDeviceAttribute = EavAttribute.findByAttributeCodeAndEntityType('kvmDevice',entityType)
if(kvmDeviceAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[kvmDeviceAttribute])
 	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[kvmDeviceAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[kvmDeviceAttribute])
 	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[kvmDeviceAttribute.id])
}

def kvmPortAttribute = EavAttribute.findByAttributeCodeAndEntityType('kvmPort',entityType)
if(kvmPortAttribute) {
 	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[kvmPortAttribute])
 	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[kvmPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[kvmPortAttribute])
 	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[kvmPortAttribute.id])
}
/*
 * usize
 */
/*def usizeAttribute = EavAttribute.findByAttributeCodeAndEntityType('usize')
if(usizeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[usizeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[usizeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[usizeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[usizeAttribute.id])
}*/
/*
 * remoteMgmtPort
 */
def remoteMgmtPortAttribute = EavAttribute.findByAttributeCodeAndEntityType('remoteMgmtPort',entityType)
if(remoteMgmtPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[remoteMgmtPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[remoteMgmtPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[remoteMgmtPortAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[remoteMgmtPortAttribute.id])
}
/*
 * replace newOrOld with planStatus
 */
def newOrOldAttribute = EavAttribute.findByAttributeCodeAndEntityType('newOrOld',entityType)
if(newOrOldAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus' where eavAttribute = ?",[newOrOldAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='Plan Status' where id = ?",[newOrOldAttribute.id])
}
def planStatusAttribute = EavAttribute.findByAttributeCodeAndEntityType('planStatus',entityType)
if(planStatusAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus' where eavAttribute = ?",[planStatusAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='Plan Status' where id = ?",[planStatusAttribute.id])
}

/*
 * replace hinfo with os
 */
def hinfoAttribute = EavAttribute.findByAttributeCodeAndEntityType('hinfo',entityType)
if(hinfoAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'OS' where eavAttribute = ?",[hinfoAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'os', frontendLabel = 'OS' where id = ?",[hinfoAttribute.id])
}
def osAttribute = EavAttribute.findByAttributeCodeAndEntityType('os',entityType)
if(osAttribute) {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'OS' where eavAttribute = ?",[osAttribute])
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'os', frontendLabel = 'OS' where id = ?",[osAttribute.id])
}
DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute.id not in ( select ea.id from EavAttribute ea) )")

EavAttribute.executeUpdate("Update from EavAttribute set frontendInput='text' where attributeCode in('model','manufacturer')")

/*
* replace sourceTeam with sourceTeamMt
*/
def sourceTeamAttribute = EavAttribute.findByAttributeCodeAndEntityType('sourceTeam',entityType)
if(sourceTeamAttribute) {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamMt' where eavAttribute = ?",[sourceTeamAttribute])
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sourceTeamMt', frontendLabel='Source Team Mt' where id = ?",[sourceTeamAttribute.id])
}
def sourceTeamMtAttribute = EavAttribute.findByAttributeCodeAndEntityType('sourceTeamMt',entityType)
if(sourceTeamMtAttribute) {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamMt' where eavAttribute = ?",[sourceTeamMtAttribute])
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sourceTeamMt', frontendLabel='Source Team Mt' where id = ?",[sourceTeamMtAttribute.id])
}
/*
* replace targetTeam with targetTeamMt
*/
def targetTeamAttribute = EavAttribute.findByAttributeCodeAndEntityType('targetTeam',entityType)
if(targetTeamAttribute) {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamMt' where eavAttribute = ?",[targetTeamAttribute])
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'targetTeamMt', frontendLabel='Target Team Mt' where id = ?",[targetTeamAttribute.id])
}
def targetTeamMtAttribute = EavAttribute.findByAttributeCodeAndEntityType('targetTeamMt',entityType)
if(targetTeamMtAttribute) {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamMt' where eavAttribute = ?",[targetTeamMtAttribute])
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'targetTeamMt', frontendLabel='Target Team Mt' where id = ?",[targetTeamMtAttribute.id])
}
/**
*  Create Source Team Logistics
*/
def sourceTeamLogAttribute = EavAttribute.findByAttributeCodeAndEntityType('sourceTeamLog',entityType)
if(sourceTeamLogAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sourceTeamLog', frontendLabel='Source Team Log' where id = ?",[sourceTeamLogAttribute.id])
} else {
   sourceTeamLogAttribute = new EavAttribute( attributeCode : "sourceTeamLog",
											   backendType : 'String',
											   frontendInput : 'text',
											   frontendLabel : 'Source Team Log',
											   note : 'The Team that device is associated as Source',
											   sortOrder : 341,
											   entityType:entityType,
											   isRequired:0,
											   isUnique:0,
											   defaultValue:'Log',
											   validation:'No validation'
										   )
	   if ( !sourceTeamLogAttribute.validate() || !sourceTeamLogAttribute.save(flush:true) ) {
		   println"Unable to create sourceTeamLogAttribute : "
		   sourceTeamLogAttribute.errors.allErrors.each() {println"\n"+it }
	   }
}

def logisticsSourceEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(sourceTeamLogAttribute,attributeSet)
if(logisticsSourceEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 341, attributeCode = 'sourceTeamLog' where attributeCode = 'sourceTeamLog'")
} else {
	logisticsSourceEavEntityAttribute = new EavEntityAttribute(sortOrder:341,attribute:sourceTeamLogAttribute,eavAttributeSet:attributeSet)
	if ( !logisticsSourceEavEntityAttribute.validate() || !logisticsSourceEavEntityAttribute.save(flush:true) ) {
		println"Unable to create logisticsSourceEavEntityAttribute : " +
		logisticsSourceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def logisticsSourceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sourceTeamLogAttribute)
if( !logisticsSourceDataTransferMapMaster ){
	logisticsSourceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SourceTeamLog",
											sheetName:"Servers",
											dataTransferSet : masterDataTransferSet,
											eavAttribute:sourceTeamLogAttribute,
											validation:"NO Validation",
											isRequired:0
		)
	if ( !logisticsSourceDataTransferMapMaster.validate() || !logisticsSourceDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create logisticsSourceDataTransferMapMaster : " +
		logisticsSourceDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamLog',sheetName='Servers' where eavAttribute = ?",[sourceTeamLogAttribute])
}

def logisticsSourceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sourceTeamLogAttribute)
if(!logisticsSourceDataTransferMapWalkThru){
	logisticsSourceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SourceTeamLog",
																			sheetName:"Servers",
																			dataTransferSet : walkThruDataTransferSet,
																			eavAttribute:sourceTeamLogAttribute,
																			validation:"NO Validation",
																			isRequired:0
																			)
	if ( !logisticsSourceDataTransferMapWalkThru.validate() || !logisticsSourceDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create logisticsSourceDataTransferMapWalkThru : " +
		logisticsSourceDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamLog',sheetName='Servers' where eavAttribute = ?",[sourceTeamLogAttribute])
}

/**
 *  Create Target Team Logistics
 */
def targetTeamLogAttribute = EavAttribute.findByAttributeCodeAndEntityType('targetTeamLog',entityType)
if(targetTeamLogAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'targetTeamLog', frontendLabel='Target Team Log' where id = ?",[targetTeamLogAttribute.id])
} else {
   targetTeamLogAttribute = new EavAttribute( attributeCode : "targetTeamLog",
											   backendType : 'String',
											   frontendInput : 'text',
											   frontendLabel : 'Target Team Log',
											   note : 'The Team that device is associated as Target',
											   sortOrder : 342,
											   entityType:entityType,
											   isRequired:0,
											   isUnique:0,
											   defaultValue:'Log',
											   validation:'No validation'
										   )
	   if ( !targetTeamLogAttribute.validate() || !targetTeamLogAttribute.save(flush:true) ) {
		   println"Unable to create targetTeamLogAttribute : "
		   targetTeamLogAttribute.errors.allErrors.each() {println"\n"+it }
	   }
}

def logisticsTargetEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(targetTeamLogAttribute,attributeSet)
if(logisticsTargetEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 342, attributeCode = 'targetTeamLog' where attributeCode = 'targetTeamLog'")
} else {
	logisticsTargetEavEntityAttribute = new EavEntityAttribute(sortOrder:342,attribute:targetTeamLogAttribute,eavAttributeSet:attributeSet)
	if ( !logisticsTargetEavEntityAttribute.validate() || !logisticsTargetEavEntityAttribute.save(flush:true) ) {
		println"Unable to create logisticsTargetEavEntityAttribute : " +
		logisticsTargetEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def logisticsTargetDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,targetTeamLogAttribute)
if( !logisticsTargetDataTransferMapMaster ){
	logisticsTargetDataTransferMapMaster = new DataTransferAttributeMap(columnName:"TargetTeamLog",
											sheetName:"Servers",
											dataTransferSet : masterDataTransferSet,
											eavAttribute:targetTeamLogAttribute,
											validation:"NO Validation",
											isRequired:0
		)
	if ( !logisticsTargetDataTransferMapMaster.validate() || !logisticsTargetDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create logisticsTargetDataTransferMapMaster : " +
		logisticsTargetDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamLog',sheetName='Servers' where eavAttribute = ?",[targetTeamLogAttribute])
}

def logisticsTargetDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,targetTeamLogAttribute)
if(!logisticsTargetDataTransferMapWalkThru){
	logisticsTargetDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"TargetTeamLog",
																			sheetName:"Servers",
																			dataTransferSet : walkThruDataTransferSet,
																			eavAttribute:targetTeamLogAttribute,
																			validation:"NO Validation",
																			isRequired:0
																			)
	if ( !logisticsTargetDataTransferMapWalkThru.validate() || !logisticsTargetDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create logisticsTargetDataTransferMapWalkThru : " +
		logisticsTargetDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamLog',sheetName='Servers' where eavAttribute = ?",[targetTeamLogAttribute])
}
/**
*  Create Source Team SysAdmin
*/
def sourceTeamSaAttribute = EavAttribute.findByAttributeCodeAndEntityType('sourceTeamSa',entityType)
if(sourceTeamSaAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sourceTeamSa', frontendLabel='Source Team Sa' where id = ?",[sourceTeamSaAttribute.id])
} else {
   sourceTeamSaAttribute = new EavAttribute( attributeCode : "sourceTeamSa",
											   backendType : 'String',
											   frontendInput : 'text',
											   frontendLabel : 'Source Team Sa',
											   note : 'The Team that device is associated as Source',
											   sortOrder : 343,
											   entityType:entityType,
											   isRequired:0,
											   isUnique:0,
											   defaultValue:'Sa',
											   validation:'No validation'
										   )
	   if ( !sourceTeamSaAttribute.validate() || !sourceTeamSaAttribute.save(flush:true) ) {
		   println"Unable to create sourceTeamSaAttribute : "
		   sourceTeamSaAttribute.errors.allErrors.each() {println"\n"+it }
	   }
}

def sysAdminSourceEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(sourceTeamSaAttribute,attributeSet)
if(sysAdminSourceEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 343, attributeCode = 'sourceTeamSa' where attributeCode = 'sourceTeamSa'")
} else {
	sysAdminSourceEavEntityAttribute = new EavEntityAttribute(sortOrder:343,attribute:sourceTeamSaAttribute,eavAttributeSet:attributeSet)
	if ( !sysAdminSourceEavEntityAttribute.validate() || !sysAdminSourceEavEntityAttribute.save(flush:true) ) {
		println"Unable to create sysAdminSourceEavEntityAttribute : " +
		sysAdminSourceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def sysAdminSourceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sourceTeamSaAttribute)
if( !sysAdminSourceDataTransferMapMaster ){
	sysAdminSourceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SourceTeamSa",
											sheetName:"Servers",
											dataTransferSet : masterDataTransferSet,
											eavAttribute:sourceTeamSaAttribute,
											validation:"NO Validation",
											isRequired:0
		)
	if ( !sysAdminSourceDataTransferMapMaster.validate() || !sysAdminSourceDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create sysAdminSourceDataTransferMapMaster : " +
		sysAdminSourceDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamSa',sheetName='Servers' where eavAttribute = ?",[sourceTeamSaAttribute])
}

def sysAdminSourceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sourceTeamSaAttribute)
if(!sysAdminSourceDataTransferMapWalkThru){
	sysAdminSourceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SourceTeamSa",
																			sheetName:"Servers",
																			dataTransferSet : walkThruDataTransferSet,
																			eavAttribute:sourceTeamSaAttribute,
																			validation:"NO Validation",
																			isRequired:0
																			)
	if ( !sysAdminSourceDataTransferMapWalkThru.validate() || !sysAdminSourceDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create sysAdminSourceDataTransferMapWalkThru : " +
		sysAdminSourceDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamSa',sheetName='Servers' where eavAttribute = ?",[sourceTeamSaAttribute])
}

/**
 *  Create Target Team SysAdmin
 */
def targetTeamSaAttribute = EavAttribute.findByAttributeCodeAndEntityType('targetTeamSa',entityType)
if(targetTeamSaAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'targetTeamSa', frontendLabel='Target Team Sa' where id = ?",[targetTeamSaAttribute.id])
} else {
   targetTeamSaAttribute = new EavAttribute( attributeCode : "targetTeamSa",
											   backendType : 'String',
											   frontendInput : 'text',
											   frontendLabel : 'Target Team Sa',
											   note : 'The Team that device is associated as Target',
											   sortOrder : 344,
											   entityType:entityType,
											   isRequired:0,
											   isUnique:0,
											   defaultValue:'Sa',
											   validation:'No validation'
										   )
	   if ( !targetTeamSaAttribute.validate() || !targetTeamSaAttribute.save(flush:true) ) {
		   println"Unable to create targetTeamSaAttribute : "
		   targetTeamSaAttribute.errors.allErrors.each() {println"\n"+it }
	   }
}

def sysAdminTargetEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(targetTeamSaAttribute,attributeSet)
if(sysAdminTargetEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 344, attributeCode = 'targetTeamSa' where attributeCode = 'targetTeamSa'")
} else {
	sysAdminTargetEavEntityAttribute = new EavEntityAttribute(sortOrder:344,attribute:targetTeamSaAttribute,eavAttributeSet:attributeSet)
	if ( !sysAdminTargetEavEntityAttribute.validate() || !sysAdminTargetEavEntityAttribute.save(flush:true) ) {
		println"Unable to create sysAdminTargetEavEntityAttribute : " +
		sysAdminTargetEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def sysAdminTargetDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,targetTeamSaAttribute)
if( !sysAdminTargetDataTransferMapMaster ){
	sysAdminTargetDataTransferMapMaster = new DataTransferAttributeMap(columnName:"TargetTeamSa",
											sheetName:"Servers",
											dataTransferSet : masterDataTransferSet,
											eavAttribute:targetTeamSaAttribute,
											validation:"NO Validation",
											isRequired:0
		)
	if ( !sysAdminTargetDataTransferMapMaster.validate() || !sysAdminTargetDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create sysAdminTargetDataTransferMapMaster : " +
		sysAdminTargetDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamSa',sheetName='Servers' where eavAttribute = ?",[targetTeamSaAttribute])
}

def sysAdminTargetDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,targetTeamSaAttribute)
if(!sysAdminTargetDataTransferMapWalkThru){
	sysAdminTargetDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"TargetTeamSa",
																			sheetName:"Servers",
																			dataTransferSet : walkThruDataTransferSet,
																			eavAttribute:targetTeamSaAttribute,
																			validation:"NO Validation",
																			isRequired:0
																			)
	if ( !sysAdminTargetDataTransferMapWalkThru.validate() || !sysAdminTargetDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create sysAdminTargetDataTransferMapWalkThru : " +
		sysAdminTargetDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamSa',sheetName='Servers' where eavAttribute = ?",[targetTeamSaAttribute])
}
/**
*  Create Source Team DbAdmin
*/
def sourceTeamDbaAttribute = EavAttribute.findByAttributeCodeAndEntityType('sourceTeamDba',entityType)
if(sourceTeamDbaAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sourceTeamDba', frontendLabel='Source Team Dba' where id = ?",[sourceTeamDbaAttribute.id])
} else {
   sourceTeamDbaAttribute = new EavAttribute( attributeCode : "sourceTeamDba",
											   backendType : 'String',
											   frontendInput : 'text',
											   frontendLabel : 'Source Team Dba',
											   note : 'The Team that device is associated as Source',
											   sortOrder : 345,
											   entityType:entityType,
											   isRequired:0,
											   isUnique:0,
											   defaultValue:'Dba',
											   validation:'No validation'
										   )
	   if ( !sourceTeamDbaAttribute.validate() || !sourceTeamDbaAttribute.save(flush:true) ) {
		   println"Unable to create sourceTeamDbaAttribute : "
		   sourceTeamDbaAttribute.errors.allErrors.each() {println"\n"+it }
	   }
}

def dbAdminSourceEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(sourceTeamDbaAttribute,attributeSet)
if(dbAdminSourceEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 345, attributeCode = 'sourceTeamDba' where attributeCode = 'sourceTeamDba'")
} else {
	dbAdminSourceEavEntityAttribute = new EavEntityAttribute(sortOrder:345,attribute:sourceTeamDbaAttribute,eavAttributeSet:attributeSet)
	if ( !dbAdminSourceEavEntityAttribute.validate() || !dbAdminSourceEavEntityAttribute.save(flush:true) ) {
		println"Unable to create dbAdminSourceEavEntityAttribute : " +
		dbAdminSourceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def dbAdminSourceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sourceTeamDbaAttribute)
if( !dbAdminSourceDataTransferMapMaster ){
	dbAdminSourceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SourceTeamDba",
											sheetName:"Servers",
											dataTransferSet : masterDataTransferSet,
											eavAttribute:sourceTeamDbaAttribute,
											validation:"NO Validation",
											isRequired:0
		)
	if ( !dbAdminSourceDataTransferMapMaster.validate() || !dbAdminSourceDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create dbAdminSourceDataTransferMapMaster : " +
		dbAdminSourceDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamDba',sheetName='Servers' where eavAttribute = ?",[sourceTeamDbaAttribute])
}

def dbAdminSourceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sourceTeamDbaAttribute)
if(!dbAdminSourceDataTransferMapWalkThru){
	dbAdminSourceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SourceTeamDba",
																			sheetName:"Servers",
																			dataTransferSet : walkThruDataTransferSet,
																			eavAttribute:sourceTeamDbaAttribute,
																			validation:"NO Validation",
																			isRequired:0
																			)
	if ( !dbAdminSourceDataTransferMapWalkThru.validate() || !dbAdminSourceDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create dbAdminSourceDataTransferMapWalkThru : " +
		dbAdminSourceDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SourceTeamDba',sheetName='Servers' where eavAttribute = ?",[sourceTeamDbaAttribute])
}

/**
 *  Create Target Team DbAdmin
 */
def targetTeamDbaAttribute = EavAttribute.findByAttributeCodeAndEntityType('targetTeamDba',entityType)
if(targetTeamDbaAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'targetTeamDba', frontendLabel='Target Team Dba' where id = ?",[targetTeamDbaAttribute.id])
} else {
   targetTeamDbaAttribute = new EavAttribute( attributeCode : "targetTeamDba",
											   backendType : 'String',
											   frontendInput : 'text',
											   frontendLabel : 'Target Team Dba',
											   note : 'The Team that device is associated as Target',
											   sortOrder : 346,
											   entityType:entityType,
											   isRequired:0,
											   isUnique:0,
											   defaultValue:'Dba',
											   validation:'No validation'
										   )
	   if ( !targetTeamDbaAttribute.validate() || !targetTeamDbaAttribute.save(flush:true) ) {
		   println"Unable to create targetTeamDbaAttribute : "
		   targetTeamDbaAttribute.errors.allErrors.each() {println"\n"+it }
	   }
}

def dbAdminTargetEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(targetTeamDbaAttribute,attributeSet)
if(dbAdminTargetEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'targetTeamDba' where attributeCode = 'targetTeamDba'")
} else {
	dbAdminTargetEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:targetTeamDbaAttribute,eavAttributeSet:attributeSet)
	if ( !dbAdminTargetEavEntityAttribute.validate() || !dbAdminTargetEavEntityAttribute.save(flush:true) ) {
		println"Unable to create dbAdminTargetEavEntityAttribute : " +
		dbAdminTargetEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def dbAdminTargetDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,targetTeamDbaAttribute)
if( !dbAdminTargetDataTransferMapMaster ){
	dbAdminTargetDataTransferMapMaster = new DataTransferAttributeMap(columnName:"TargetTeamDba",
											sheetName:"Servers",
											dataTransferSet : masterDataTransferSet,
											eavAttribute:targetTeamDbaAttribute,
											validation:"NO Validation",
											isRequired:0
		)
	if ( !dbAdminTargetDataTransferMapMaster.validate() || !dbAdminTargetDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create dbAdminTargetDataTransferMapMaster : " +
		dbAdminTargetDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamDba',sheetName='Servers' where eavAttribute = ?",[targetTeamDbaAttribute])
}

def dbAdminTargetDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,targetTeamDbaAttribute)
if(!dbAdminTargetDataTransferMapWalkThru){
	dbAdminTargetDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"TargetTeamDba",
																			sheetName:"Servers",
																			dataTransferSet : walkThruDataTransferSet,
																			eavAttribute:targetTeamDbaAttribute,
																			validation:"NO Validation",
																			isRequired:0
																			)
	if ( !dbAdminTargetDataTransferMapWalkThru.validate() || !dbAdminTargetDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create dbAdminTargetDataTransferMapWalkThru : " +
		dbAdminTargetDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TargetTeamDba',sheetName='Servers' where eavAttribute = ?",[targetTeamDbaAttribute])
}

/**
 *  Create usize
 */
def usizeAttribute = EavAttribute.findByAttributeCodeAndEntityType('usize',entityType)
if(usizeAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'usize', frontendLabel='Usize' where id = ?",[usizeAttribute.id])
} else {
	usizeAttribute = new EavAttribute( attributeCode : "usize",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'Usize',
			note : 'this field is used for just import',
			sortOrder : 347,
			entityType:entityType,
			isRequired:0,
			isUnique:0,
			defaultValue:"1",
			validation:'No validation'
			)
	if ( !usizeAttribute.validate() || !usizeAttribute.save(flush:true) ) {
		println"Unable to create usizeAttribute : "
		usizeAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def usizeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(usizeAttribute,attributeSet)
if(usizeEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'usize' where attributeCode = 'usize'")
} else {
	usizeEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:usizeAttribute,eavAttributeSet:attributeSet)
	if ( !usizeEavEntityAttribute.validate() || !usizeEavEntityAttribute.save(flush:true) ) {
		println"Unable to create usizeEavEntityAttribute : " +
				usizeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def usizeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,usizeAttribute)
if( !usizeDataTransferMapMaster ){
	usizeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"usize",
			sheetName:"Servers",
			dataTransferSet : masterDataTransferSet,
			eavAttribute:usizeAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !usizeDataTransferMapMaster.validate() || !usizeDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create usizeDataTransferMapMaster : " +
				usizeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'usize',sheetName='Servers' where eavAttribute = ?",[usizeAttribute])
}

def usizeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,usizeAttribute)
if(!usizeDataTransferMapWalkThru){
	usizeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"usize",
			sheetName:"Servers",
			dataTransferSet : walkThruDataTransferSet,
			eavAttribute:usizeAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !usizeDataTransferMapWalkThru.validate() || !usizeDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create usizeDataTransferMapWalkThru : " +
				usizeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'usize',sheetName='Servers' where eavAttribute = ?",[usizeAttribute])
}
/*
 * Set Attributes order 
 */
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10 where attributeCode = 'application'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 10 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('application',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 20 where attributeCode = 'assetName'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 20 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('assetName',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30 where attributeCode = 'shortName'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 30 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('shortName',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 40 where attributeCode = 'serialNumber'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 40 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('serialNumber',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 50 where attributeCode = 'assetTag'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 50 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('assetTag',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 60 where attributeCode = 'assetType'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 60 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('assetType',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 70 where attributeCode = 'manufacturer'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 70 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('manufacturer',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 80 where attributeCode = 'model'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 80 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('model',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 100 where attributeCode = 'ipAddress'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 100 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('ipAddress',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 110 where attributeCode = 'os'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 110 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('os',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 120 where attributeCode = 'sourceLocation'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 120 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceLocation',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 130 where attributeCode = 'sourceRoom'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 130 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceRoom',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 140 where attributeCode = 'sourceRack'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 140 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceRack',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 150 where attributeCode = 'sourceRackPosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 150 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceRackPosition',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 160 where attributeCode = 'sourceBladeChassis'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 160 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceBladeChassis',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 170 where attributeCode = 'sourceBladePosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 170 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceBladePosition',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 180 where attributeCode = 'targetLocation'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 180 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetLocation',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 190 where attributeCode = 'targetRoom'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 190 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetRoom',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 200 where attributeCode = 'targetRack'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 200 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetRack',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 210 where attributeCode = 'targetRackPosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 210 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetRackPosition',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 220 where attributeCode = 'targetBladeChassis'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 220 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetBladeChassis',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 230 where attributeCode = 'targetBladePosition'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 230 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetBladePosition',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 240 where attributeCode = 'custom1'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 240 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom1',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 250 where attributeCode = 'custom2'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 250 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom2',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 260 where attributeCode = 'custom3'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 260 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom3',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 270 where attributeCode = 'custom4'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 270 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom4',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 280 where attributeCode = 'custom5'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 280 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom5',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 290 where attributeCode = 'custom6'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 290 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom6',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 300 where attributeCode = 'custom7'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 300 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom7',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 310 where attributeCode = 'custom8'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 310 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('custom8',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 320 where attributeCode = 'moveBundle'")

EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 320 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('moveBundle',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 330, attributeCode = 'sourceTeamMt' where attributeCode in ('sourceTeam','sourceTeamMt')")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 330 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceTeamMt',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 340, attributeCode = 'targetTeamMt' where attributeCode in ('targetTeam','targetTeamMt')")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 340 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetTeamMt',entityType)])

EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 341, attributeCode = 'sourceTeamLog' where attributeCode = 'sourceTeamLog'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 341 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceTeamLog',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 342, attributeCode = 'targetTeamLog' where attributeCode = 'targetTeamLog'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 342 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetTeamLog',entityType)])

EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 343, attributeCode = 'sourceTeamSa' where attributeCode = 'sourceTeamSa'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 343 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceTeamLog',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 344, attributeCode = 'targetTeamSa' where attributeCode = 'targetTeamSa'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 344 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetTeamSa',entityType)])

EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 345, attributeCode = 'sourceTeamDba' where attributeCode = 'sourceTeamDba'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 345 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('sourceTeamDba',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'targetTeamDba' where attributeCode = 'targetTeamDba'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 346 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('targetTeamDba',entityType)])

EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 347, attributeCode = 'usize' where attributeCode = 'usize'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 347 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('usize',entityType)])


EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 350 where attributeCode = 'truck'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 350 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('truck',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 360 where attributeCode = 'cart'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 360 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('cart',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 370 where attributeCode = 'shelf'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 370 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('shelf',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 380 where attributeCode = 'railType'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 380 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('railType',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 390 where attributeCode = 'appOwner'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 390 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('appOwner',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 400 where attributeCode = 'appSme'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 400 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('appSme',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 410 where attributeCode = 'priority'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 410 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('priority',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 420 where attributeCode = 'planStatus'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 420 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('planStatus',entityType)])
EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 430 where attributeCode = 'currentStatus'")
EavEntityAttribute.executeUpdate("UPDATE from EavEntityAttribute set sortOrder= 430 where attribute = ?",[EavAttribute.findByAttributeCodeAndEntityType('currentStatus',entityType)])
