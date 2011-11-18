
import com.tds.asset.AssetEntity;
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")
def masterDataTransferSet = DataTransferSet.findBySetCode("MASTER")
def walkThruDataTransferSet = DataTransferSet.findBySetCode("WALKTHROUGH")

def filesEntityType = EavEntityType.findByEntityTypeCode("Files")
if(!filesEntityType){
	filesAttributeSet = new EavEntityType( entityTypeCode:'Files', domainName:'Files', isAuditable:1  ).save(flush:true)
}
def filesAttributeSet = EavAttributeSet.findByAttributeSetName("Files")
if(!filesAttributeSet){
	filesAttributeSet = new EavAttributeSet( attributeSetName:'Files', entityType:filesEntityType, sortOrder:20 ).save(flush:true)
}
/**
 *  Create Name
 */
def nameAttribute = EavAttribute.findByAttributeCodeAndEntityType('assetName',filesEntityType)
if(nameAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'assetName', frontendLabel='Name' where id = ?",[nameAttribute.id])
} else {
	nameAttribute = new EavAttribute( attributeCode : "assetName",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'Name',
			note : 'this field is used for just import',
			sortOrder : 10,
			entityType:filesEntityType,
			isRequired:0,
			isUnique:0,
			defaultValue:"1",
			validation:'No validation'
			)
	if ( !nameAttribute.validate() || !nameAttribute.save(flush:true) ) {
		println"Unable to create nameAttribute : "
		nameAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def nameEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(nameAttribute,filesAttributeSet)
if(nameEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'assetName' where attributeCode = 'assetName'")
} else {
	nameEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:nameAttribute,eavAttributeSet:filesAttributeSet)
	if ( !nameEavEntityAttribute.validate() || !nameEavEntityAttribute.save(flush:true) ) {
		println"Unable to create nameEavEntityAttribute : " +
				nameEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def nameDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,nameAttribute)
if( !nameDataTransferMapMaster ){
	nameDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Name",
			sheetName:"Files",
			dataTransferSet : masterDataTransferSet,
			eavAttribute:nameAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !nameDataTransferMapMaster.validate() || !nameDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create nameDataTransferMapMaster : " +
				nameDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Name',sheetName='Files' where eavAttribute = ?",[nameAttribute])
}

def nameDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,nameAttribute)
if(!nameDataTransferMapWalkThru){
	nameDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Name",
			sheetName:"Files",
			dataTransferSet : walkThruDataTransferSet,
			eavAttribute:nameAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !nameDataTransferMapWalkThru.validate() || !nameDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create nameDataTransferMapWalkThru : " +
				nameDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Name',sheetName='Files' where eavAttribute = ?",[nameAttribute])
}
/**
*  Create Format
*/
def formatAttribute = EavAttribute.findByAttributeCodeAndEntityType('fileFormat',filesEntityType)
if(formatAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'fileFormat', frontendLabel='Format' where id = ?",[formatAttribute.id])
} else {
   formatAttribute = new EavAttribute( attributeCode : "fileFormat",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Format',
		   note : 'this field is used for just import',
		   sortOrder : 20,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !formatAttribute.validate() || !formatAttribute.save(flush:true) ) {
	   println"Unable to create formatAttribute : "
	   formatAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def formatEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(formatAttribute,filesAttributeSet)
if(formatEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 20, attributeCode = 'fileFormat' where attributeCode = 'fileFormat'")
} else {
   formatEavEntityAttribute = new EavEntityAttribute(sortOrder:20,attribute:formatAttribute,eavAttributeSet:filesAttributeSet)
   if ( !formatEavEntityAttribute.validate() || !formatEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create formatEavEntityAttribute : " +
			   formatEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def formatDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,formatAttribute)
if( !formatDataTransferMapMaster ){
   formatDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Format",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:formatAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !formatDataTransferMapMaster.validate() || !formatDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create formatDataTransferMapMaster : " +
			   formatDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Format', sheetName='Files' where eavAttribute = ?",[formatAttribute])
}

def formatDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,formatAttribute)
if(!formatDataTransferMapWalkThru){
   formatDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Format",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:formatAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !formatDataTransferMapWalkThru.validate() || !formatDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create formatDataTransferMapWalkThru : " +
			   formatDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Format',sheetName='Files' where eavAttribute = ?",[formatAttribute])
}
/**
*  Create FileSize
*/
def fileSizeAttribute = EavAttribute.findByAttributeCodeAndEntityType('fileSize',filesEntityType)
if(fileSizeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'fileSize', frontendLabel='FileSize' where id = ?",[fileSizeAttribute.id])
} else {
   fileSizeAttribute = new EavAttribute( attributeCode : "fileSize",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'FileSize',
		   note : 'this field is used for just import',
		   sortOrder : 30,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !fileSizeAttribute.validate() || !fileSizeAttribute.save(flush:true) ) {
	   println"Unable to create fileSizeAttribute : "
	   fileSizeAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def fileSizeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(fileSizeAttribute,filesAttributeSet)
if(fileSizeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30, attributeCode = 'fileSize' where attributeCode = 'fileSize'")
} else {
   fileSizeEavEntityAttribute = new EavEntityAttribute(sortOrder:30,attribute:fileSizeAttribute,eavAttributeSet:filesAttributeSet)
   if ( !fileSizeEavEntityAttribute.validate() || !fileSizeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create fileSizeEavEntityAttribute : " +
			   fileSizeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def fileSizeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,fileSizeAttribute)
if( !fileSizeDataTransferMapMaster ){
   fileSizeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"FileSize",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:fileSizeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !fileSizeDataTransferMapMaster.validate() || !fileSizeDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create fileSizeDataTransferMapMaster : " +
			   fileSizeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'FileSize',sheetName='Files' where eavAttribute = ?",[fileSizeAttribute])
}

def fileSizeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,fileSizeAttribute)
if(!fileSizeDataTransferMapWalkThru){
   fileSizeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"FileSize",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:fileSizeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !fileSizeDataTransferMapWalkThru.validate() || !fileSizeDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create fileSizeDataTransferMapWalkThru : " +
			   fileSizeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'FileSize',sheetName='Files' where eavAttribute = ?",[fileSizeAttribute])
}
/**
*  Create Description
*/
def descriptionAttribute = EavAttribute.findByAttributeCodeAndEntityType('description',filesEntityType)
if(descriptionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'description', frontendLabel='Description' where id = ?",[descriptionAttribute.id])
} else {
   descriptionAttribute = new EavAttribute( attributeCode : "description",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Description',
		   note : 'this field is used for just import',
		   sortOrder : 40,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !descriptionAttribute.validate() || !descriptionAttribute.save(flush:true) ) {
	   println"Unable to create descriptionAttribute : "
	   descriptionAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def descriptionEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(descriptionAttribute,filesAttributeSet)
if(descriptionEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 40, attributeCode = 'description' where attributeCode = 'description'")
} else {
   descriptionEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:descriptionAttribute,eavAttributeSet:filesAttributeSet)
   if ( !descriptionEavEntityAttribute.validate() || !descriptionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create descriptionEavEntityAttribute : " +
			   descriptionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def descriptionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,descriptionAttribute)
if( !descriptionDataTransferMapMaster ){
   descriptionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Description",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:descriptionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !descriptionDataTransferMapMaster.validate() || !descriptionDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create descriptionDataTransferMapMaster : " +
			   descriptionDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Description',sheetName='Files' where eavAttribute = ?",[descriptionAttribute])
}

def descriptionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,descriptionAttribute)
if(!descriptionDataTransferMapWalkThru){
   descriptionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Description",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:descriptionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !descriptionDataTransferMapWalkThru.validate() || !descriptionDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create descriptionDataTransferMapWalkThru : " +
			   descriptionDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Description',sheetName='Files' where eavAttribute = ?",[descriptionAttribute])
}
/**
 *  Create SupportType
 */
def supportTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('supportType',filesEntityType)
if(supportTypeAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'supportType', frontendLabel='SupportType' where id = ?",[supportTypeAttribute.id])
} else {
	supportTypeAttribute = new EavAttribute( attributeCode : "supportType",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'SupportType',
			note : 'this field is used for just import',
			sortOrder : 50,
			entityType:filesEntityType,
			isRequired:0,
			isUnique:0,
			defaultValue:"1",
			validation:'No validation'
			)
	if ( !supportTypeAttribute.validate() || !supportTypeAttribute.save(flush:true) ) {
		println"Unable to create supportTypeAttribute : "
		supportTypeAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def supportTypeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(supportTypeAttribute,filesAttributeSet)
if(supportTypeEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 50, attributeCode = 'supportType' where attributeCode = 'supportType'")
} else {
	supportTypeEavEntityAttribute = new EavEntityAttribute(sortOrder:50,attribute:supportTypeAttribute,eavAttributeSet:filesAttributeSet)
	if ( !supportTypeEavEntityAttribute.validate() || !supportTypeEavEntityAttribute.save(flush:true) ) {
		println"Unable to create supportTypeEavEntityAttribute : " +
				supportTypeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def supportTypeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,supportTypeAttribute)
if( !supportTypeDataTransferMapMaster ){
	supportTypeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SupportType",
			sheetName:"Files",
			dataTransferSet : masterDataTransferSet,
			eavAttribute:supportTypeAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !supportTypeDataTransferMapMaster.validate() || !supportTypeDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create supportTypeDataTransferMapMaster : " +
				supportTypeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SupportType',sheetName='Files' where eavAttribute = ?",[supportTypeAttribute])
}

def supportTypeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,supportTypeAttribute)
if(!supportTypeDataTransferMapWalkThru){
	supportTypeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SupportType",
			sheetName:"Files",
			dataTransferSet : walkThruDataTransferSet,
			eavAttribute:supportTypeAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !supportTypeDataTransferMapWalkThru.validate() || !supportTypeDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create supportTypeDataTransferMapWalkThru : " +
				supportTypeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SupportType',sheetName='Files' where eavAttribute = ?",[supportTypeAttribute])
}
/**
*  Create Retire
*/
def retireDateAttribute = EavAttribute.findByAttributeCodeAndEntityType('retireDate',filesEntityType)
if(retireDateAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'retireDate', frontendLabel='Retire' where id = ?",[retireDateAttribute.id])
} else {
   retireDateAttribute = new EavAttribute( attributeCode : "retireDate",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Retire',
		   note : 'this field is used for just import',
		   sortOrder : 60,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !retireDateAttribute.validate() || !retireDateAttribute.save(flush:true) ) {
	   println"Unable to create retireDateAttribute : "
	   retireDateAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireDateEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(retireDateAttribute,filesAttributeSet)
if(retireDateEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 60, attributeCode = 'retireDate' where attributeCode = 'retireDate'")
} else {
   retireDateEavEntityAttribute = new EavEntityAttribute(sortOrder:60,attribute:retireDateAttribute,eavAttributeSet:filesAttributeSet)
   if ( !retireDateEavEntityAttribute.validate() || !retireDateEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create retireDateEavEntityAttribute : " +
			   retireDateEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireDateDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,retireDateAttribute)
if( !retireDateDataTransferMapMaster ){
   retireDateDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Retire",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:retireDateAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !retireDateDataTransferMapMaster.validate() || !retireDateDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create retireDateDataTransferMapMaster : " +
			   retireDateDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Retire',sheetName='Files' where eavAttribute = ?",[retireDateAttribute])
}

def retireDateDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,retireDateAttribute)
if(!retireDateDataTransferMapWalkThru){
   retireDateDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Retire",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:retireDateAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !retireDateDataTransferMapWalkThru.validate() || !retireDateDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create retireDateDataTransferMapWalkThru : " +
			   retireDateDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Retire',sheetName='Files' where eavAttribute = ?",[retireDateAttribute])
}
/**
*  Create MaintExp
*/
def maintExpDateAttribute = EavAttribute.findByAttributeCodeAndEntityType('maintExpDate',filesEntityType)
if(maintExpDateAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'maintExpDate', frontendLabel='MaintExp' where id = ?",[maintExpDateAttribute.id])
} else {
   maintExpDateAttribute = new EavAttribute( attributeCode : "maintExpDate",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'MaintExp',
		   note : 'this field is used for just import',
		   sortOrder : 70,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !maintExpDateAttribute.validate() || !maintExpDateAttribute.save(flush:true) ) {
	   println"Unable to create maintExpDateAttribute : "
	   maintExpDateAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def maintExpDateEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(maintExpDateAttribute,filesAttributeSet)
if(maintExpDateEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 70, attributeCode = 'maintExpDate' where attributeCode = 'maintExpDate'")
} else {
   maintExpDateEavEntityAttribute = new EavEntityAttribute(sortOrder:70,attribute:maintExpDateAttribute,eavAttributeSet:filesAttributeSet)
   if ( !maintExpDateEavEntityAttribute.validate() || !maintExpDateEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create maintExpDateEavEntityAttribute : " +
			   maintExpDateEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def maintExpDateDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,maintExpDateAttribute)
if( !maintExpDateDataTransferMapMaster ){
   maintExpDateDataTransferMapMaster = new DataTransferAttributeMap(columnName:"MaintExp",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:maintExpDateAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !maintExpDateDataTransferMapMaster.validate() || !maintExpDateDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create maintExpDateDataTransferMapMaster : " +
			   maintExpDateDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MaintExp',sheetName='Files' where eavAttribute = ?",[maintExpDateAttribute])
}

def maintExpDateDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,maintExpDateAttribute)
if(!maintExpDateDataTransferMapWalkThru){
   maintExpDateDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"MaintExp",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:maintExpDateAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !maintExpDateDataTransferMapWalkThru.validate() || !maintExpDateDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create maintExpDateDataTransferMapWalkThru : " +
			   maintExpDateDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MaintExp',sheetName='Files' where eavAttribute = ?",[maintExpDateAttribute])
}
/**
*  Create Environment
*/
def environmentAttribute = EavAttribute.findByAttributeCodeAndEntityType('environment',filesEntityType)
if(environmentAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'environment', frontendLabel='Environment' where id = ?",[environmentAttribute.id])
} else {
   environmentAttribute = new EavAttribute( attributeCode : "environment",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Environment',
		   note : 'this field is used for just import',
		   sortOrder : 80,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !environmentAttribute.validate() || !environmentAttribute.save(flush:true) ) {
	   println"Unable to create environmentAttribute : "
	   environmentAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def environmentEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(environmentAttribute,filesAttributeSet)
if(environmentEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 80, attributeCode = 'environment' where attributeCode = 'environment'")
} else {
   environmentEavEntityAttribute = new EavEntityAttribute(sortOrder:80,attribute:environmentAttribute,eavAttributeSet:filesAttributeSet)
   if ( !environmentEavEntityAttribute.validate() || !environmentEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create environmentEavEntityAttribute : " +
			   environmentEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def environmentDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,environmentAttribute)
if( !environmentDataTransferMapMaster ){
   environmentDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Environment",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:environmentAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !environmentDataTransferMapMaster.validate() || !environmentDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create environmentDataTransferMapMaster : " +
			   environmentDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Environment',sheetName='Files' where eavAttribute = ?",[environmentAttribute])
}

def environmentDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,environmentAttribute)
if(!environmentDataTransferMapWalkThru){
   environmentDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Environment",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:environmentAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !environmentDataTransferMapWalkThru.validate() || !environmentDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create environmentDataTransferMapWalkThru : " +
			   environmentDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Environment',sheetName='Files' where eavAttribute = ?",[environmentAttribute])
}
/**
*  Create MoveBundle
*/
def moveBundleAttribute = EavAttribute.findByAttributeCodeAndEntityType('moveBundle',filesEntityType)
if(moveBundleAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'moveBundle', frontendLabel='MoveBundle' where id = ?",[moveBundleAttribute.id])
} else {
   moveBundleAttribute = new EavAttribute( attributeCode : "moveBundle",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'MoveBundle',
		   note : 'this field is used for just import',
		   sortOrder : 90,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !moveBundleAttribute.validate() || !moveBundleAttribute.save(flush:true) ) {
	   println"Unable to create moveBundleAttribute : "
	   moveBundleAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def moveBundleEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(moveBundleAttribute,filesAttributeSet)
if(moveBundleEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 90, attributeCode = 'moveBundle' where attributeCode = 'moveBundle'")
} else {
   moveBundleEavEntityAttribute = new EavEntityAttribute(sortOrder:90,attribute:moveBundleAttribute,eavAttributeSet:filesAttributeSet)
   if ( !moveBundleEavEntityAttribute.validate() || !moveBundleEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create moveBundleEavEntityAttribute : " +
			   moveBundleEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def moveBundleDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,moveBundleAttribute)
if( !moveBundleDataTransferMapMaster ){
   moveBundleDataTransferMapMaster = new DataTransferAttributeMap(columnName:"MoveBundle",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:moveBundleAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !moveBundleDataTransferMapMaster.validate() || !moveBundleDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create moveBundleDataTransferMapMaster : " +
			   moveBundleDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MoveBundle',sheetName='Files' where eavAttribute = ?",[moveBundleAttribute])
}

def moveBundleDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,moveBundleAttribute)
if(!moveBundleDataTransferMapWalkThru){
   moveBundleDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"MoveBundle",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:moveBundleAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !moveBundleDataTransferMapWalkThru.validate() || !moveBundleDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create moveBundleDataTransferMapWalkThru : " +
			   moveBundleDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MoveBundle',sheetName='Files' where eavAttribute = ?",[moveBundleAttribute])
}
/**
*  Create PlanStatus
*/
def planStatusAttribute = EavAttribute.findByAttributeCodeAndEntityType('planStatus',filesEntityType)
if(planStatusAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='PlanStatus' where id = ?",[planStatusAttribute.id])
} else {
   planStatusAttribute = new EavAttribute( attributeCode : "planStatus",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'PlanStatus',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:filesEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !planStatusAttribute.validate() || !planStatusAttribute.save(flush:true) ) {
	   println"Unable to create planStatusAttribute : "
	   planStatusAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def planStatusEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(planStatusAttribute,filesAttributeSet)
if(planStatusEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 100, attributeCode = 'planStatus' where attributeCode = 'planStatus'")
} else {
   planStatusEavEntityAttribute = new EavEntityAttribute(sortOrder:100,attribute:planStatusAttribute,eavAttributeSet:filesAttributeSet)
   if ( !planStatusEavEntityAttribute.validate() || !planStatusEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create planStatusEavEntityAttribute : " +
			   planStatusEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def planStatusDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,planStatusAttribute)
if( !planStatusDataTransferMapMaster ){
   planStatusDataTransferMapMaster = new DataTransferAttributeMap(columnName:"PlanStatus",
		   sheetName:"Files",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:planStatusAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !planStatusDataTransferMapMaster.validate() || !planStatusDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create planStatusDataTransferMapMaster : " +
			   planStatusDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus',sheetName='Files' where eavAttribute = ?",[planStatusAttribute])
}

def planStatusDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,planStatusAttribute)
if(!planStatusDataTransferMapWalkThru){
   planStatusDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"PlanStatus",
		   sheetName:"Files",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:planStatusAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !planStatusDataTransferMapWalkThru.validate() || !planStatusDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create planStatusDataTransferMapWalkThru : " +
			   planStatusDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus',sheetName='Files' where eavAttribute = ?",[planStatusAttribute])
}




